/*
 *  Copyright (C) 2021 Gwinnett County Experimental Aircraft Association
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.eaa690.aerie.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.constant.RosterConstants;
import org.eaa690.aerie.exception.ResourceExistsException;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.MemberRepository;
import org.eaa690.aerie.model.OtherInfo;
import org.eaa690.aerie.model.roster.Country;
import org.eaa690.aerie.model.roster.Gender;
import org.eaa690.aerie.model.roster.MemberType;
import org.eaa690.aerie.model.roster.MembershipReport;
import org.eaa690.aerie.model.roster.State;
import org.eaa690.aerie.model.roster.Status;
import org.eaa690.aerie.model.roster.WebAdminAccess;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Logs into EAA's roster management system, downloads the EAA 690 records as an Excel spreadsheet.
 * Then parses the spreadsheet for member details, and inserts (or updates) member data in a local MySQL database.
 */
public class RosterService {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(RosterService.class);

    /**
     * Base URL for EAA Chapters.
     */
    private final String EAA_CHAPTERS_SITE_BASE = "https://www.eaachapters.org";

    /**
     * Empty string constant.
     */
    private final static String EMPTY_STRING = "";

    /**
     * PropertyService.
     */
    @Autowired
    private PropertyService propertyService;

    /**
     * EmailService.
     */
    @Autowired
    private EmailService emailService;

    /**
     * SMSService.
     */
    @Autowired
    private SMSService smsService;

    /**
     * MailChimpService.
     */
    @Autowired
    private MailChimpService mailChimpService;

    /**
     * SlackService.
     */
    @Autowired
    private SlackService slackService;

    /**
     * MemberRepository.
     */
    @Autowired
    private MemberRepository memberRepository;

    /**
     * HttpClient.
     */
    @Autowired
    private HttpClient httpClient;

    /**
     * HttpHeaders.
     */
    private final Map<String, String> headers = new HashMap<>();

    /**
     * Date formatter.
     */
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Date formatter.
     */
    private static final SimpleDateFormat MDY_SDF = new SimpleDateFormat("MM/dd/yyyy");

    /**
     * Sets HttpClient.
     * Note: mostly used for unit test mocks
     *
     * @param value HttpClient
     */
    @Autowired
    public void setHttpClient(final HttpClient value) {
        httpClient = value;
    }

    /**
     * Sets PropertyService.
     * Note: mostly used for unit test mocks
     *
     * @param value PropertyService
     */
    @Autowired
    public void setPropertyService(final PropertyService value) {
        propertyService = value;
    }

    /**
     * Sets MailChimpService.
     * Note: mostly used for unit test mocks
     *
     * @param value MailChimpService
     */
    @Autowired
    public void setMailChimpService(final MailChimpService value) {
        mailChimpService = value;
    }

    /**
     * Sets EmailService.
     * Note: mostly used for unit test mocks
     *
     * @param value EmailService
     */
    @Autowired
    public void setEmailService(final EmailService value) {
        emailService = value;
    }

    /**
     * Sets SMSService.
     * Note: mostly used for unit test mocks
     *
     * @param value SMSService
     */
    @Autowired
    public void setSMSService(final SMSService value) {
        smsService = value;
    }

    /**
     * Sets SlackService.
     * Note: mostly used for unit test mocks
     *
     * @param value SlackService
     */
    @Autowired
    public void setSlackService(final SlackService value) {
        slackService = value;
    }

    /**
     * Sets MemberRepository.
     * Note: mostly used for unit test mocks
     *
     * @param mRepository MemberRepository
     */
    @Autowired
    public void setMemberRepository(final MemberRepository mRepository) {
        memberRepository = mRepository;
    }

    /**
     * Updates every 6 hours.
     *
     * second, minute, hour, day of month, month, day(s) of week
     */
    @Scheduled(cron = "0 0 0,6,12,18 * * *")
    public void update() {
        try {
            getHttpHeaders();
            doLogin();
            getSearchMembersPage();
            final List<Member> members = parseRecords();
            for (Member member : members) {
                memberRepository
                        .findByRosterId(member.getRosterId())
                        .ifPresent(value -> member.setId(value.getId()));
                if (member.getCreatedAt() == null) {
                    member.setCreatedAt(new Date());
                }
                member.setUpdatedAt(new Date());
                memberRepository.save(member);
            }
        } catch (ResourceNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void sendMembershipRenewalMessages() {
        memberRepository
                .findAll()
                .ifPresent(members -> members
                        .stream()
                        .filter(member -> member.getMemberType() == MemberType.Regular ||
                                member.getMemberType() == MemberType.Family ||
                                member.getMemberType() == MemberType.Student)
                        .forEach(member -> {
                            try {
                                final String expirationDate = SDF.format(member.getExpiration());
                                if (expirationDate.equals(
                                        getDateStr(PropertyKeyConstants.MEMBERSHIP_RENEWAL_FIRST_MSG_DAYS_KEY))) {
                                    emailService.queueMsg(
                                            PropertyKeyConstants.SEND_GRID_FIRST_MEMBERSHIP_RENEWAL_EMAIL_TEMPLATE_ID,
                                            PropertyKeyConstants.SEND_GRID_FIRST_MEMBERSHIP_RENEWAL_EMAIL_SUBJECT_KEY,
                                            member);
                                    smsService.sendRenewMembershipMsg(member);
                                    slackService.sendRenewMembershipMsg(member);
                                }
                                if (expirationDate.equals(
                                        getDateStr(PropertyKeyConstants.MEMBERSHIP_RENEWAL_SECOND_MSG_DAYS_KEY))) {
                                    emailService.queueMsg(
                                            PropertyKeyConstants.SEND_GRID_SECOND_MEMBERSHIP_RENEWAL_EMAIL_TEMPLATE_ID,
                                            PropertyKeyConstants.SEND_GRID_SECOND_MEMBERSHIP_RENEWAL_EMAIL_SUBJECT_KEY,
                                            member);
                                    smsService.sendRenewMembershipMsg(member);
                                    slackService.sendRenewMembershipMsg(member);
                                }
                                if (expirationDate.equals(
                                        getDateStr(PropertyKeyConstants.MEMBERSHIP_RENEWAL_THIRD_MSG_DAYS_KEY))) {
                                    emailService.queueMsg(
                                            PropertyKeyConstants.SEND_GRID_THIRD_MEMBERSHIP_RENEWAL_EMAIL_TEMPLATE_ID,
                                            PropertyKeyConstants.SEND_GRID_THIRD_MEMBERSHIP_RENEWAL_EMAIL_SUBJECT_KEY,
                                            member);
                                    smsService.sendRenewMembershipMsg(member);
                                    slackService.sendRenewMembershipMsg(member);
                                }
                                if (expirationDate.equals(SDF.format(new Date()))) {
                                    // TODO: move member to non-member distro list in MailChimp
                                    //mailChimpService.addOrUpdateNonMember(
                                    //        member.getFirstName(),
                                    //        member.getLastName(),
                                    //        member.getEmail());
                                }
                            } catch (ResourceNotFoundException rnfe) {
                                LOGGER.error("Error", rnfe);
                            }
                        }));
    }

    /**
     * Retrieves the member affiliated with the provided RFID.
     *
     * @param rfid RFID
     * @return Member
     * @throws ResourceNotFoundException when no member matches
     */
    public Member getMemberByRFID(final String rfid) throws ResourceNotFoundException {
        final Optional<Member> member = memberRepository.findByRfid(rfid);
        if (member.isPresent()) {
            return member.get();
        }
        throw new ResourceNotFoundException("No member found matching RFID="+rfid);
    }

    /**
     * Retrieves the member affiliated with the provided ID.
     *
     * @param id Member ID
     * @return Member
     * @throws ResourceNotFoundException when no member matches
     */
    public Member getMemberByRosterID(final Long id) throws ResourceNotFoundException {
        Optional<Member> member = memberRepository.findByRosterId(id);
        if (member.isPresent()) {
            return member.get();
        }
        throw new ResourceNotFoundException("No member found matching ID="+id);
    }

    /**
     * Gets all members.
     *
     * @return list of Member
     */
    public List<Member> getAllMembers() {
        return memberRepository.findAll().orElse(null);
    }

    /**
     * Updates a member's RFID to the provided value.
     *
     * @param id Member Roster ID
     * @param rfid new RFID value
     * @throws ResourceNotFoundException when no member matches
     */
    public void updateMemberRFID(final Long id, final String rfid) throws ResourceNotFoundException {
        final Member member = getMemberByRosterID(id);
        member.setRfid(rfid);
        memberRepository.save(member);
    }

    /**
     * Saves member information to roster.
     *
     * @param member Member to be saved
     */
    public Member saveNewMember(final Member member) throws ResourceExistsException {
        LOGGER.info("Saving new member: " + member);
        try {
            getHttpHeaders();
            doLogin();
            getSearchMembersPage();
            if (!existsUser(member.getFirstName(), member.getLastName())) {
                // TODO: add user
            }
        } catch (ResourceNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return member;
    }

    /**
     * Saves member information to roster.
     *
     * @param member Member to be saved
     */
    public Member saveRenewingMember(final Member member) {
        LOGGER.info("Saving renewing member: " + member);
        try {
            getHttpHeaders();
            doLogin();
            getSearchMembersPage();
            if (existsUser(member.getFirstName(), member.getLastName())) {
                // TODO: update user
                LOGGER.info(buildUpdateUserRequestBodyString(member));
            }
        } catch (ResourceNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return member;
    }

    /**
     * Generates a MembershipReport.
     *
     * @return MembershipReport
     */
    public MembershipReport getMembershipReport() {
        final Date today = new Date();
        final Date thirtyDays = Date.from(Instant.now().plus(30, ChronoUnit.DAYS));
        final Date sevenDays = Date.from(Instant.now().plus(7, ChronoUnit.DAYS));
        final MembershipReport membershipReport = new MembershipReport();
        final List<Member> allMembers = memberRepository.findAll().orElse(new ArrayList<>());
        setActiveCounts(today, membershipReport, allMembers);
        setExpiredCounts(today, membershipReport, allMembers);
        setWillExpire30DaysCounts(today, thirtyDays, sevenDays, membershipReport, allMembers);
        setWillExpire7DaysCounts(today, sevenDays, membershipReport, allMembers);
        membershipReport.setLifetimeMemberCount(
                allMembers.stream().filter(m -> MemberType.Lifetime == m.getMemberType()).count());
        membershipReport.setHonoraryMemberCount(
                allMembers.stream().filter(m -> MemberType.Honorary == m.getMemberType()).count());
        membershipReport.setProspectMemberCount(
                allMembers.stream().filter(m -> MemberType.Prospect == m.getMemberType()).count());
        membershipReport.setNonMemberCount(
                allMembers.stream().filter(m -> MemberType.NonMember == m.getMemberType()).count());
        return membershipReport;
    }

    private void setActiveCounts(final Date today,
                                 final MembershipReport membershipReport,
                                 final List<Member> allMembers) {
        membershipReport.setRegularMemberCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Regular == m.getMemberType())
                        .filter(m -> today.before(m.getExpiration()))
                        .count());
        membershipReport.setFamilyMembershipCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> today.before(m.getExpiration()))
                        .count());
        membershipReport.setFamilyMemberCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> today.before(m.getExpiration()))
                        .map(Member::getNumOfFamily)
                        .reduce(0L, Long::sum));
        membershipReport.setStudentMemberCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Student == m.getMemberType())
                        .filter(m -> today.before(m.getExpiration()))
                        .count());
    }

    private void setWillExpire7DaysCounts(final Date today,
                                          final Date sevenDays,
                                          final MembershipReport membershipReport,
                                          final List<Member> allMembers) {
        membershipReport.setRegularMemberWillExpire7DaysCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Regular == m.getMemberType())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> sevenDays.after(m.getExpiration()))
                        .count());
        membershipReport.setFamilyMembershipWillExpire7DaysCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> sevenDays.after(m.getExpiration()))
                        .count());
        membershipReport.setStudentMemberWillExpire7DaysCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Student == m.getMemberType())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> sevenDays.after(m.getExpiration()))
                        .count());
    }

    private void setWillExpire30DaysCounts(final Date today,
                                           final Date thirtyDays,
                                           final Date sevenDays,
                                           final MembershipReport membershipReport,
                                           final List<Member> allMembers) {
        membershipReport.setRegularMemberWillExpire30DaysCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Regular == m.getMemberType())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> thirtyDays.after(m.getExpiration()))
                        .filter(m -> sevenDays.before(m.getExpiration()))
                        .count());
        membershipReport.setFamilyMembershipWillExpire30DaysCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> thirtyDays.after(m.getExpiration()))
                        .filter(m -> sevenDays.before(m.getExpiration()))
                        .count());
        membershipReport.setStudentMemberWillExpire30DaysCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Student == m.getMemberType())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> thirtyDays.after(m.getExpiration()))
                        .filter(m -> sevenDays.before(m.getExpiration()))
                        .count());
    }

    private void setExpiredCounts(final Date today,
                                  final MembershipReport membershipReport,
                                  final List<Member> allMembers) {
        membershipReport.setRegularMemberExpiredCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Regular == m.getMemberType())
                        .filter(m -> today.after(m.getExpiration()))
                        .count());
        membershipReport.setFamilyMembershipExpiredCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> today.after(m.getExpiration()))
                        .count());
        membershipReport.setStudentMemberExpiredCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Student == m.getMemberType())
                        .filter(m -> today.after(m.getExpiration()))
                        .count());
    }

    /**
     * Performs login to EAA's roster management system.
     */
    private void doLogin() {
        final String uriStr = EAA_CHAPTERS_SITE_BASE + "/main.aspx";
        final String requestBodyStr = buildLoginRequestBodyString();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
        for (final String key : headers.keySet()) {
            builder.setHeader(key, headers.get(key));
        }
        final HttpRequest request = builder.build();

        try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            LOGGER.error("[Login] Error", e);
        }
    }

    /**
     * Gets searchmembers page in EAA's roster management system.
     */
    private void getSearchMembersPage() {
        final String uriStr = EAA_CHAPTERS_SITE_BASE + "/searchmembers.aspx";
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .GET();
        for (final String key : headers.keySet()) {
            builder.setHeader(key, headers.get(key));
        }
        final HttpRequest request = builder.build();

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            final Document doc = Jsoup.parse(response.body());
            final Element viewState = doc.getElementById(RosterConstants.VIEW_STATE);
            headers.put(RosterConstants.VIEW_STATE, viewState.attr("value"));
        } catch (Exception e) {
            LOGGER.error("[Search Page] Error", e);
        }
    }

    /**
     * Checks if a user exists in EAA's roster management system.
     */
    private boolean existsUser(final String firstName, final String lastName) {
        final String uriStr = EAA_CHAPTERS_SITE_BASE + "/searchmembers.aspx";
        final String requestBodyStr = buildExistsUserRequestBodyString(firstName, lastName);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
        headers.remove(RosterConstants.VIEW_STATE);
        headers.put(RosterConstants.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        for (final String key : headers.keySet()) {
            builder.setHeader(key, headers.get(key));
        }
        final HttpRequest request = builder.build();

        StringBuilder sb = new StringBuilder();
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            sb.append(response.body());
        } catch (Exception e) {
            LOGGER.error("[FETCH] Error", e);
        }
        return sb.toString().contains("lnkViewUpdateMember");
    }

    /**
     * Fetch's data from EAA's roster management system.
     */
    private String fetchData() {
        final String uriStr = EAA_CHAPTERS_SITE_BASE + "/searchmembers.aspx";
        final String requestBodyStr = buildFetchDataRequestBodyString();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
        headers.remove(RosterConstants.VIEW_STATE);
        headers.put(RosterConstants.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        for (final String key : headers.keySet()) {
            builder.setHeader(key, headers.get(key));
        }
        final HttpRequest request = builder.build();

        StringBuilder sb = new StringBuilder();
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            sb.append(response.body());
        } catch (Exception e) {
            LOGGER.error("[FETCH] Error", e);
        }
        return sb.toString();
    }

    private void getHttpHeaders() throws ResourceNotFoundException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(EAA_CHAPTERS_SITE_BASE + "/main.aspx")).GET().build();
        try {
            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final HttpHeaders responseHeaders = response.headers();
            final String cookieStr = responseHeaders.firstValue("set-cookie").orElse("");
            headers.put("cookie", cookieStr.substring(0, cookieStr.indexOf(";")));
        } catch (Exception e) {
            LOGGER.error("Error", e);
        }
        headers.put(RosterConstants.EVENT_TARGET, "");
        headers.put(RosterConstants.EVENT_ARGUMENT, "");
        headers.put(RosterConstants.VIEW_STATE, "/wEPDwUKMTY1NDU2MTA1MmRkuOlmdf9IlE5Upbw3feS5bMlNeitv2Tys6h3WSL105GQ=");
        headers.put(RosterConstants.VIEW_STATE_GENERATOR, "202EA31B");
        headers.put(RosterConstants.EVENT_VALIDATION, "/wEdAAaUkhCi8bB8A8YPK1mx/fN+Ob9NwfdsH6h5T4oBt2E/NC/PSAvxybIG70Gi7lMSo2Ha9mxIS56towErq28lcj7mn+o6oHBHkC8q81Z+42F7hK13DHQbwWPwDXbrtkgbgsBJaWfipkuZE5/MRRQAXrNwOiJp3YGlq4qKyVLK8XZVxQ==");
        headers.put(RosterConstants.USERNAME, propertyService.get(PropertyKeyConstants.ROSTER_USER_KEY).getValue());
        headers.put(RosterConstants.PASSWORD, propertyService.get(PropertyKeyConstants.ROSTER_PASS_KEY).getValue());
        headers.put(RosterConstants.BUTTON, "Submit");
        headers.put(RosterConstants.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36");
        headers.put(RosterConstants.CONTENT_TYPE, "application/x-www-form-urlencoded");
        headers.put(RosterConstants.EXPORT_BUTTON, "Results+To+Excel");
        headers.put(RosterConstants.STATUS, "Active");
        headers.put(RosterConstants.FIRST_NAME, "");
        headers.put(RosterConstants.LAST_NAME, "");
        headers.put(RosterConstants.SEARCH_MEMBER_TYPE, "");
        headers.put(RosterConstants.CURRENT_STATUS, "");
        headers.put(RosterConstants.ROW_COUNT, "");
        headers.put(RosterConstants.VIEW_STATE_ENCRYPTED, "");
        headers.put(RosterConstants.LAST_FOCUS, "");
    }

    private String buildLoginRequestBodyString() {
        final StringBuilder sb = new StringBuilder();
        final List<String> data = new ArrayList<>();
        data.add(RosterConstants.EVENT_TARGET);
        data.add(RosterConstants.EVENT_ARGUMENT);
        data.add(RosterConstants.VIEW_STATE);
        data.add(RosterConstants.VIEW_STATE_GENERATOR);
        data.add(RosterConstants.EVENT_VALIDATION);
        data.add(RosterConstants.USERNAME);
        data.add(RosterConstants.PASSWORD);
        data.add(RosterConstants.BUTTON);
        for (final String key : headers.keySet()) {
            if (data.contains(key)) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                if (RosterConstants.USERNAME.equals(key) ||
                        RosterConstants.PASSWORD.equals(key) || RosterConstants.BUTTON.equals(key)) {
                    sb.append(key.replaceAll("\\$", "%24"));
                } else {
                    sb.append(key);
                }
                sb.append("=");
                if (RosterConstants.VIEW_STATE.equals(key) || RosterConstants.EVENT_VALIDATION.equals(key)) {
                    sb.append(headers.get(key)
                            .replaceAll("/", "%2F")
                            .replaceAll("=", "%3D")
                            .replaceAll("\\+", "%2B"));
                } else {
                    sb.append(headers.get(key));
                }
            }
        }
        return sb.toString();
    }

    private String buildFetchDataRequestBodyString() {
        final StringBuilder sb = new StringBuilder();
        final List<String> data = new ArrayList<>();
        data.add(RosterConstants.EVENT_TARGET);
        data.add(RosterConstants.EVENT_ARGUMENT);
        data.add(RosterConstants.LAST_FOCUS);
        data.add(RosterConstants.VIEW_STATE);
        data.add(RosterConstants.VIEW_STATE_GENERATOR);
        data.add(RosterConstants.VIEW_STATE_ENCRYPTED);
        data.add(RosterConstants.FIRST_NAME);
        data.add(RosterConstants.LAST_NAME);
        data.add(RosterConstants.EXPORT_BUTTON);
        data.add(RosterConstants.STATUS);
        data.add(RosterConstants.SEARCH_MEMBER_TYPE);
        data.add(RosterConstants.CURRENT_STATUS);
        data.add(RosterConstants.ROW_COUNT);
        for (final String key : headers.keySet()) {
            if (data.contains(key)) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                if (RosterConstants.FIRST_NAME.equals(key) ||
                        RosterConstants.LAST_NAME.equals(key) ||
                        RosterConstants.EXPORT_BUTTON.equals(key) ||
                        RosterConstants.STATUS.equals(key) ||
                        RosterConstants.SEARCH_MEMBER_TYPE.equals(key) ||
                        RosterConstants.CURRENT_STATUS.equals(key) ||
                        RosterConstants.ROW_COUNT.equals(key)) {
                    sb.append(key.replaceAll("\\$", "%24"));
                } else {
                    sb.append(key);
                }
                sb.append("=");
                if (RosterConstants.VIEW_STATE.equals(key) || RosterConstants.EVENT_VALIDATION.equals(key)) {
                    sb.append(headers.get(key)
                            .replaceAll("/", "%2F")
                            .replaceAll("=", "%3D")
                            .replaceAll("\\+", "%2B"));
                } else {
                    sb.append(headers.get(key));
                }
            }
        }
        return sb.toString();
    }

    private String buildExistsUserRequestBodyString(final String firstName, final String lastName) {
        final StringBuilder sb = new StringBuilder();
        final List<String> data = new ArrayList<>();
        data.add(RosterConstants.EVENT_TARGET);
        data.add(RosterConstants.EVENT_ARGUMENT);
        data.add(RosterConstants.VIEW_STATE);
        data.add(RosterConstants.VIEW_STATE_GENERATOR);
        data.add(RosterConstants.VIEW_STATE_ENCRYPTED);
        data.add(RosterConstants.SEARCH_BUTTON);
        data.add(RosterConstants.FIRST_NAME + "=" + firstName);
        data.add(RosterConstants.LAST_NAME + "=" + lastName);
        data.add(RosterConstants.STATUS + "=Active");
        data.add(RosterConstants.SEARCH_MEMBER_TYPE);
        data.add(RosterConstants.CURRENT_STATUS);
        for (final String key : headers.keySet()) {
            if (data.contains(key)) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                if (RosterConstants.FIRST_NAME.equals(key) ||
                        RosterConstants.LAST_NAME.equals(key) ||
                        RosterConstants.EXPORT_BUTTON.equals(key) ||
                        RosterConstants.STATUS.equals(key) ||
                        RosterConstants.SEARCH_MEMBER_TYPE.equals(key) ||
                        RosterConstants.CURRENT_STATUS.equals(key) ||
                        RosterConstants.ROW_COUNT.equals(key)) {
                    sb.append(key.replaceAll("\\$", "%24"));
                } else {
                    sb.append(key);
                }
                sb.append("=");
                if (RosterConstants.VIEW_STATE.equals(key) || RosterConstants.EVENT_VALIDATION.equals(key)) {
                    sb.append(headers.get(key)
                            .replaceAll("/", "%2F")
                            .replaceAll("=", "%3D")
                            .replaceAll("\\+", "%2B"));
                } else {
                    sb.append(headers.get(key));
                }
            }
        }
        return sb.toString();
    }

    private String buildNewUserRequestBodyString(final Member member) {
        /*
    }
        :method: POST
        :authority: eaachapters.org
:scheme: https
        :path: /searchmembers.aspx
        content-length: 44484
        cache-control: max-age=0
        sec-ch-ua: "Google Chrome";v="89", "Chromium";v="89", ";Not A Brand";v="99"
        sec-ch-ua-mobile: ?0
        upgrade-insecure-requests: 1
        origin: https://eaachapters.org
        content-type: application/x-www-form-urlencoded
        user-agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 11_2_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36
        accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9
sec-fetch-site: same-origin
sec-fetch-mode: navigate
sec-fetch-user: ?1
sec-fetch-dest: document
referer: https://eaachapters.org/searchmembers.aspx
accept-encoding: gzip, deflate, br
accept-language: en-US,en;q=0.9
cookie: ASP.NET_SessionId=cl2optidvrurkckbuhyejyet

        __EVENTTARGET=&__EVENTARGUMENT=&__LASTFOCUS=&__VIEWSTATE=uYIyGl%2BrWrMNCm9OeC3IWRIlzeREjj7ueIiiqQzkhiAU4aGsyLsKDHT5Jxycg%2BlhgAdx%2BL8bU1AcdZaTRET4vFx0x1w0XC%2FY3EfQ%2FpXGEAjqQTFoVSgYzz1k5Cz5RO%2Bh5hhA5AGbvCOrSWiSiq6U%2BSsZZGBb8oNrMtNSiT42RioscRrHwtUvRYTQuMvxPeFDDBWNWvpgt20x66v4qLy%2B3VTFpeDIbIE2V8CCrWxkicyVuI4JctB81M954Tj8hobOatumB%2F6d0mleqG8Pjr5lvI7YOX4WgBu8IxoUfMpqFhHSFpljHZR4iNjYyQY1KaXbC8ADLw2NRhMNiVFWizb9X32l%2Fu8roITYKrNsZuN6cxhMHInm0YgHNMij3VaTRSytLrvm65QY856miCk0xltTSh5hc8v8X86v85zo52fKe6faLI2O1YGN7qq57yxycXCczal0EHxRsGP16krSMydAf2yrLU1B%2BSx0uCEVQFlkzXgbfC5LgzfBWWNHgXCRHWSX%2FHROvy0JCxPkW4xNaiMnFcE%2FOHv31qIoxeT3%2BwuFfZOlqS4j9huEEyRzqcbyBuHZK%2B0hm2%2BudJ9biKZB54yZLxuvR7YkxiAe45RdphScWYJJECHa6EuoEBUs5ENkYr1ZUqg3FR07sN9vVHbA1ZjfCrNS9c%2BaSjPLBOa%2FUcWwP3NDYlfmidHzrMpsv4p9iLI%2FgxSvYBUqUuC10UvT58uJVwRh2MDD%2Bjd03uLiQux8TKwyrbtPq98VWiljrpd1OUzji%2B6vNwuTD0hsODh%2F5cA3Y8V4tQSUlZErRU%2FHc7%2BKQAS3%2FmvYGOjmMKDqpdDGqmb4XvMcaIF8bwvzjk9jFn9bUopH2EGnly3DcoJHDcpMqMknrvgJ%2FDm9CEoFEIcD79w9n0KLBJkrzH%2FYW93OA%2FMjwTFbEJRC7bXjGoEnH2vTw2%2BMcET1%2FbqWhfuVgnJg4Gil4L7aPV18sGu2n%2Bl1arS4lwKJXzOZ%2FtGFVa9du5GcGURi7sIpWZqQMV7i5GpUaHkUbP1N9GDizxxkOOMtA%2FJYo8c4kssLIJh9fCVjtscUdY4N9Ql20ReYsVGvjAPaiGFuvPeBPx7ifV%2Blt9HWrWCI3dPFIF5MiZvo9HaktAjgMMJOTB8QJzJ4eP9duTgJNTLzWYkC7h%2BGzsEXQv9o4y4i0WwljdUOWmqIknN0rG%2FmbVWx3%2B1woKBh%2F83aZxlAKODKCeNmcSftAz%2B5iLTis3jL%2Fe2orItVYZIre5hDs%2BtzFMOL2Q7GyOGRxqHNi8HmRHwptwXBp4m%2FspH%2BiaxtELZ7WbNihls%2F6kVCLPpjqTddL%2FBqklACzGKSCuy0RMLAuYvLiSddOQOwsvlUDbqaDvwlw84yir7f2DUFm14U0P4Io5eoJykpOdgAngNl7JH7BvSb8d73WlpGtVFl3FP%2FIy5SsBkluIApN3oXrOPSC0uUG%2FTyAP6z%2BP7jYkDHkuPK9eMXyU%2FP%2BNnTL%2BJScsSMmAXFSEQA6ptAwjVAkGWHrduttFqalugh72yCEuXtvNctrZIQgzoRp9mIJlGPTDKPnohkfyPiOaj04T17NXCgSB%2Bq0CeMsuVdWkMiYKijsSd5NXsnngzbFfTvXL0DlvgF9QaDDNW1hkRjQngMhvJsai7r4LsozPo%2BBbR1C21JeS%2BEoeTZ7wBzHFvHsAxCHWXf%2FkhN9lQt%2B2JEy1BI5%2FVN%2F%2F%2B3gvTf%2B4nvAgmmaPbW9DgOBWfLaLXBxXSPNCU%2ByEWAuieR4NpTMPcKNr6jOwETWToVsz9gq2oSkH5%2Fc818UJR33YEKF3iZGFLq7f%2BvIoNcnWfL3bq9XYltXOti3RwlzOOcEVF6MfpEa5P9SCdUgvtJke6Uala8dhM8xZMKVDbfzGwiJoa4F2RrYW0fUcaXeOZEG2lTnyn2HmYRMcEHInoD%2F8r%2B9OjtUdaCz6ChrZyuMYUzD%2BIc2HPrMjkAtpeyEQagHN9yzqZtJ8ocjm3dqAtZtESzRXu0xWz3Ah%2Bb6GitxTztUNSML6KGIk5VMn854h4MmHjLpjIQT3%2BTmQT2lOOQsPmCjYh3zyAAdzkubWO621l5Myd6tiSKaxPLQax2jGGHDosaI3V4SMt2uRTkde5ttayd53N6c9JOih4gQdZfALVW8%2FP5PgBNyKL8p04a%2FC7W6lOrnK%2BFsUB8LbiXIsXwIQPC4ztsVngEXIWK1jGG5uXiTGWR3RxC8B1DtquHMHBgbiOx5rBW3F0xGufx1NukgAuSOKbjbGmfAP7t%2F8oMw3xnTir8KsC%2FG8vQtgSO80orHPPjYlpMO9LsotqdicmQwrGWvWISXOXh26F89bqs0NrpNdMnpPueH%2Bzxjus9gfTywHlHnhi1DAPmjN%2BFcHvKYHqx5pV1722lGbqzyQ31QEZKTeBH6gCCCz%2BPzRkwGc5tgBVzgAAozNRjskaF9tWOqVNhR6sHsF4Z%2BMCK%2FAnJnKyYNtFa5b9xAJmSNxUlg85TmXaz11najEJEQcZG8vPaCyxYiEPqNoceNkXI%2B6fbNZBw1YUcn92L1MwMnMM9eRjuF9bvthRDiebbLrN3cr6L7mqmfw0cjUu7qBEpZWIjpxQIesDfvxD2%2Bgl62OxxoPPzBNNaH%2BA1wbt%2FoRaiRFFKZDo9mjZvFvVu0L4nrO1ys6Uftrco0kBwzwZZOvYs6NTyFz3W%2BDpqEJh1ThQ7WoSg1Bk%2B9FKetmZNFhsSo8NRI0m4slOHndQ3LiQHo0rXyW%2Fo93CTqAWeK3mAT%2BU9XfHUAdrSn46m1ivTdLf1yaH8%2BxBy1A01K0xrxWbUrZOJRCWuF%2B6%2BW1oflPHurWP54ehJMP3OXOyKcrsUJF9Y%2FO5tZFe6KncGUYg7eTprNsSvoOCCal0zhtpDNKoc2o8sDo1xdxNl935ZFNaBhMCi3RU4g4sqWv4qt91rBfmuEIKwAxyVUGiS6Tngw21gaji5RUKHuc03qW1S8KvA3lXfRK5A2BLf07Xdre9uO2FNEcQI6FgnSNT5mj2DB5dcCKjPqz3gdDfimio2pS71pckl6rFzWVRVMRwLJFqM9fkyj1QeNdQguex9qqitBL1EFOKCJsmyD0YirbOhzL9qD9HWx8%2B%2Bz31rkLI7HXZNwId6uKSzRYUUs4ccIwC%2Be8lQDkzzbaF9abUoqrjUcAen751tRzlgcm7xmbzXB8MUlJpJR86CnjSCVK%2BQN9%2FcZh%2Fodj3XZjhqmsZwzltl1agi9sOpdFJPToIpBXewZWkIu8t7dGJMt7aVB8DuSZTUukho1NJjsZRER80SIwgpq6viXbPdWaCR1rCit1v2WxvcU3AqPCxFq8%2F4Sab3ie1u%2F1%2FXe4khquF5AWpo8JBJHlHQsI2CnNCx9Dly9SS5xujQCnKELriwVcq9Hk84wNxtEtV7jc5LosKhA0aWxZqQgS0KfJvrSq5wBCBGUl7KhI2an9ZunGtDHHpCgyLwr5OijwrOxWOERj9Al1YNaJC5cgYJmqIE0xnk5kyG9QEwonxAUf6chxGi0LyGZ%2FBZ2WWjeReyE1nBAnzkhiLQtgwwFZ2t7wWDShksR33dymeDwLbZnBGeMrYnL7eL3xGskzxM2bbMUyXAiiLBJ1jN8ly5U8pfIa60mnPwLzJxgDLfY0bA7blaROflZyI5Hu1CM%2BiFTH0dZotxVqd5WoTc%2BOJafyT%2FVo15IQZYB%2FjRY073RxhESFP4rS%2BxUyn7PdGA%2B7%2FvDPC%2FS22lM7mXAH0qF93jqgVCOJbEbvsYEgM2OGiMujo5K8%2FPMemJkJKj4a3aJHOSyvPIqpmYA0LatQqxW8YQZyJ8ke3NMfkFcyKvKsCATktOlVYLQbrEaMrUPF0gJL5btvB7YhlCKUdD0Bm4vHFSLlNa8OQQBAlot4wzwU4I0056hUGN9delAv7%2FeTmYMpfu9%2BvEYDOPh9ngqGnVUqXE2fAvfDrJoZ3HVcaI8%2FKJ0%2BiWItBVRFGyIbtwepJJn3wvkeK1tcpvWsNdpQTvDhmNi3s2%2Bowe9g5oCbUiWd%2Fdf6Jv%2BeCNx%2BNPbUQGaXQL6rq1SGM%2F%2Bfss9ZpswX%2F4NsqmmFYDVXVPI09B%2BbLU0kKA0oJqA6TH69w8A84cmScBTJa1GRPxeWirw7t%2FrNu7mvQe1HZMERQVkeoQXOyiuziIjfqvm3GcDR%2BRhnpNchY64Xek1OAramWTLbs9XnNIyNLNXJpv%2BBt1y0LXDcft5OTY71BgcihRz52nMNAj3iKf6enbCiABVddS6IQ64enKZdUq1trZuu0dTnOIphJDiOVMsQjaEVH3XzKjf3fS50lBOHJuhGrQDi5BuKRDHLeJlaHnIfTTm3hW0XWN70E2rxVOd9rlMWHIBAU7f54XMUqXjUHLRjpT2rUilRKECyFVcRTuIphrcKGDvRblPKke0rQwopHBmObjnXNRPvdPB%2F%2BDQpNZIcRkYTQBn9p%2BQcIPiFLtCnpTaED4VqsrCNaphSQC8Pw53WBUAUK%2FWVcYIusgvmUzBjHF4EfBHhMhL2mtFG0us5r%2F47gzmI3XelMseYrPVHWQoBpSsrV%2FhLTe9P6e%2FA0Zinv86WX1LFIu%2B1bmhy3SsOx8%2FWmNHvuMPdRSlboY%2FGXNu6wEycIASSJ4Zh1ffpgbjfJs70H2BQlIdJCcHIINCIJh%2BQj3H1pdBGKDJAXp8JOQkQD8xfmrl%2FBLcF6ScbtBWMgs7DK32syW3sP%2Fe4mp6L5fSevvaR3dAjrHTuwWKa2w5vk7qNaAings5wFlALhadPYZ61qJbGzMWA2M88go9kOM%2FQ2ij88KiFGMIomKr%2FGoyy7r1bqC1%2F1XIZV833CpY81enefTLhPKVXU9qt0kegWtyekaQRnmOTM49Skpt3Lqm2ZgWgjdeCE0jZR26HsYIjB8ol0kFbSZ%2B5B5EdY2w0sl8IYf8KteDbQpJpF3Gq7hUjpTciIaCWQI92HEi76a9c5ICxHRDFHCYEfCegEEStMRLlMfD0E40FUCdoBL%2FZloscrzG1s7FWBlA40%2FgwyjDzfVfTWrwA0xA57RyHKYswWk85NU62bwU8%2Bv1n4AjmiMWRTZcEgU6SRaMBNC0x2YCB%2F%2BChXOxrDOW%2BpAj%2Byybl5xlxrt8y6CN%2BwPnmEr1c9c6t0orWMm7ls8Qstd5yjFSzkhTj0gosGRH9MV7G8rNDA5k008rgLio7nzFdzHGunc7xiYtA2VfWl9v9EJVDeYqYpzRPrmaxPhIM2%2BOhk0fHkrUmUG9XocMWTgZufueO%2FcK%2Bm7u2hnvb7q%2F6PE6nZ8n1nMfa6ukaHS7O4BMqrsQ1aXgqRNZGxAcPkJxVED1Jy2HVrdDbsLA5ag6hr6oylJjiwxoxZWIfMzctf%2BUBSsC%2FfSVa4OOXfuHhEIkpBLEtmEy6Zt5sGPbA1Es5pWxD01VyhskM4VUU1DjEujgVt4HzAgPY%2FgRpUTUFnpWRknk1kHJ8qAjhM9lvig6GO1IjnYlLcC402lT2RHmwHJUwi8VmDQP28AAJu1o8a0YLX4xdIm2lygQ%2FoHQVJEWbGovFZv2bgweDC4V2mlxZncgnQiGAvO4v6W2R4Oi4ClfU%2FMTZaeYXhN0FP3VotVs59FTVbr1%2FgfQxgi8Hdj9c16SXpS1mwHscfTlh9YvhKNP1VtLCYrwPwNGN01Iq8j8HEZq%2FtGsTfYG%2BFK04lyYPIgBcOisj1eMYlc84waDS5DwN%2Fk9gRx%2F0ki9R89o2Z%2Byh0OL6uR7oUJ50JOZNk3l%2FrcgJ3aB6rBKlRjKZSoJzUJZsD%2FoU0lwUzPC%2BL16bDyGLzhLl%2B3gA2dmArCLaltZpiVvRqOi4UJuOmUhi3hFpGtTZi3rzRKGpcxAtR3W%2BsyXg3Gv8ufRMhrDgICuCKy8QEqhhRnQQJ%2FXDXCpLd0b%2BNHG0Eq0TiSE6CULt7R4cPb8%2B%2BUz%2Fg3S9EopJ05PUKBTLOkNbT6nITWKLmFLFGzlYMuG4s6fg8lGhqMCpO3hpqRdln2CAFgGzDbsoVfngZUnZkGcuqZe2Zi1zzXAQ3Ub9kYdL8ZrTpcuzD%2FVDBzIBdnEo0vjFeLEZ6Xj3Ie1Ie009hZ3Gg2OCcr7b%2FL7ayeLsgse7tqFC0FSyQFtPjJU2hnzaW7BRwpssxLJo0CK3TksaZB2CQtT6eF9EIQQJhifjzCI9ga0yD1P2ua1KTm96xAdRmmfBBiKTqWAAViqc5s9m9Ibuv8rYEkJ0e91NuYbOU5W6vKjE1DhMAwCYecKHGiWseciNu2%2BCsl51ndvs99OonChtiLplSeB%2F46Frza%2FxQMJJzb87dJCD02zFMp3UjJYSi9MCaBAnSRgUarIyPpv9wIg2s42ISEuN%2BHm2rd50qRQNlucx9k%2FqZlbICAYvF0dkUM2CDKm7dZ8SNedbpCl6ZEl0prF9osSavnMThQc8b6sgOVVFxDssR4r3Vb70gkINKzdhisPMExXfyz1XphfaGLKU17EX8PCw7WqlWcVUc6TF7anXEoWwr7OTKsZIwByjiiScJCTqPkVUQ9k5bSqscsTleg1K2NLTdqAhMl9DEoQS7VA8DsBjABZ3Ji%2F6OFDkCIH9CMiZZJJITl6YctMMKCIshuw%2FVeVWwnIvmakPPuLorD84RC3OlVrzEO2MueVwNjXIMV2XKD2jqowvMB7suPjsyVVsqsLwtLel245qK%2BcO%2By2hzyoLfO3Tt4IhFFK0592AEjpMA26Q%2B3r4ZdvX%2BMPKpDKjEjWf8T0dsLFKhCamAaUndDOGQVV2c0StJHkRz9KF2uUitcoQW3Tm24gCzeB2BdkAGnuqrdsMYOXdhs%2BI7%2F%2B6YX%2FNoxEXYR5KV5mq2cyoIAvyhm2VZivGQy8Bq%2BR4jAjjmmS5i%2FVdbKXAg4DMhZi9inUEw1kOsCRgGcJM9e7KZ4ddQD4COBulw9Mn1rZI8%2ByDesOf%2FmnUUEE1dqUsx%2Fx2xwqMYvMsjLmrlbmLOpxunfhvxuzIIM8LYNwrCcyaxtW4BeyVV4AsviILaWsH9kbH7VQ%2F66Ql%2FjXJf8Yjl6Ct3ahehMJvxvIalgU5STx3m%2BD4G4Y6z7gst9VY4oTTFmlml4PJbqybd5fInfkpGyIxnl5%2BwFM%2Bkaf7KJIFIelqO2F0uVrG3ZJx%2FJcloufMH675%2FDJxE1VWHI9KFNTTgLtFiFvqBdlzdURkHcFuqYXT39IjAwreBJ1fbGuLQ1A0bbg22ZPuHYriPCLxq8K7zkAqPZBAyli4dW%2BrMEBpNj9JTLXMFa8gnXnCDoLjs9rLAitQrJyVqk0Q%2BEwbWlnAZ0C254rpgNLAL5CZx6hyAw%2BejqjFyTYQkmvewb7RzNOwkSac2Bfd3ssgf4zUTy4wU01if3j3FQX1luceXTZxEm5noMWkLc%2BHAi02MGVTP2VirMKYT6%2BvXhXpYbL%2Fpak2ACyhJgm%2FEOw8bWMT750mwGz%2BvT%2FH0b1yRgF1SLpuu6QprJL0UDOnOXPN5dk7JZjrhwEzcsuehnMdem02ChNTZD0M7NjRpxkFtso2J67HzUFkPsY0jDnh0N0CrsdAU5YvXTR743LTimCHMou%2FK7TCJxgYks525eZTceLG0uPnx3abu2BQxnIfdbyZQ%2FoYaB9tLbA0H7kq%2BKxZK9JbuYH%2BXLGel05WvGLtnG4lLt%2FjYsEiTgvVPnwyjDS%2Byncc1eIyghkj%2FQOlklcXf78cVr89PgiZuk1vwogjgeH%2F8n29ypCm3WrWVuK1mfrIqFiEAH%2Ft%2BDRM%2BhTols0Dl5sTDYa7O6NiBuUq8ywO3NgV1L%2F7U7KFbhad4J3BSXIHi5QhNfgh%2BtNPyK%2BaUg6tcs6IdOdTN0yoKdd3761r8REhGtBGHFitV2ikamGHT9sVNrryIp5be5B5M%2FV7n1nuTUD85t3qUrb0j0jgVKmOsET0SWbkzr5E61id7Pvjtj01YDgngjKablU4KECtCV21kr2Q7NcquHe2xW5cS1ZwJKab6TxcnWgtYUwnXrnW9EWXzwx1zM%2BILbEMd2M6%2FWNw4IFFhpaRFZ8zMel6Nj065lPUxsWE8Vw2Sowk244OEmGHWHPcZaz4fNE9Y8EJBs%2FLKH977gzaF66uqfAJCd%2B92dR9ljzJR6Vx50HSgyFdJ3DEf2jJPcy2j%2BrgmlmE4xdCaReWOb5ybYmGujVGBkKvWCQKgedjrYRTb8%2FHyC2htGub22tTDA80devl5ER1%2ByCHJZqX4F%2BPVGJ70CjskFWq79P386166C8dGRFL534i5zJxroqkJJgQjSen7wNtA49oBf%2BxGqx4FGifiUhTasfSXy93Ne8%2BMKSE8HbR3SaO2%2B0uxr6DSIivvGRHV41yg206VFJKv69%2FKwFZfHLNxjJaUWpvE%2Fyk8xW5PbNxLVPSLEk0Z00B5PVrVCY0ruwkmGulAj8E4zjWX6I8%2BC7eiX4WfriZyA%2Fc2D2Zt%2B7LXk9T20lj7vyQf0Siy3hFNhtI4m%2B17LKRMbRzpwNvTAkCx2cAv%2BLyMK9qpSMZfNJRBqjgNuVdQBTaqe5ARG%2F%2F9kJGWUUDwmzS%2B3weZVJ8xqR3qlJ%2BNsxHRJ1Zfo9y5JiBawCcPhZSltZXEIMmWN0mLXAt22FP5ZARCg0ILdfIgtKxpV%2F%2BFyP6KmL5lTH7PLZxdtSyRl%2Bb%2B3RGLnDS4ivE1juLXnX%2BbXVgf9Jp5PEhYyyPc5gTNYx8d1BcihUuOzEz2FDMPxK1%2F3pRHEQhG0HH7jZGnhuW8NjnIQUo%2B36rjS6Yz0RkhZ6MVnonpoz8czACZZWo%2BUuEDz%2F6cueCkjvefoo68jNS9cTdoNTKhWvKSm7%2Bp572phrLFn9PFDOQcQZ1lryAukgLmMdPslmB3RNS6EE2P1yy4UnJ%2Fylc%2FMguey9NgoiXb4eDGia0zhJyhJ894R0iXIZckPIaLQ3SNO8%2FS38jU1I8f%2F95alFr0xbLG7ssSx3s9duVAQsubf5qqzD9rVKXnG1G5TbJNGhPVY4%2B28h8y0kPl2tpBg3eSid%2Bt6RuHiKZimlc1RizvKSqTi48esTGjXMQjCVQ6URCYQI7IYg7jzDgA4wxTB2S1i9SfF24RqOtCgphBSbA%2F%2F1r%2BjRMu2WIl%2FK7uJGaynEBuU%2BC%2BMJY07%2F7rP1ntvNE6u9YDUlkZM%2F5Z1NzjccnIwikL0q5XkI9QU3sdlvw6RYlW%2BJlXWxZUhfiz4CNQ7e0BXw0hFinbYGcCfRYYVn2%2FMTYX7BfQ4FAFAnoODCHR8Zks5nQZ6F39cim7S89wh3wmyhez%2BRKQ%2BwHxbkZ2RL%2BO4LAUwgXo7JZcJBRPH7YS4fTRYM%2FX9S6V%2Fpn%2BN55ZSmJ%2BVNJXkseWpj17V8XAzTQJOlbVbarU%2BKwBRdPR6A9ZROsD%2BfQzqJA8XW%2BFYkU%2F6rFBiFYs7xuSQBRqVnbBPxg%2FhfH7cPVOlTRrcO0ngKXM8EqItL3Vahei9Sv0%2BzobTT0dYUcQUHuHseoEvFE3Uip%2B8nohTKAe8Vsd2UZ5Yq1J3SNLGgB7nd2ykmV%2BgsQi7mulvs8CO8Fmnk0SquwhmmNmKcfyGsRcsp0p%2FTgQ3Wd6sOKZJHORampqL4WEq0k0fMQCNcL5UERhTngiz8jiCVlqCHvZAITrKcaX0k6Wg3LG55%2FWQWK8jr2tGZ2v9ZHUQAxIGHK3EMQaGlJ4pCanA3t3xkzfKxYQ6R%2FVgyOeyRuHGuxaVD4VOgQKENNGrJ4V9caARBrgtyaItOc9qXgtN8t7uOFzyM4pmthUPY1MJNRuNaY6562MpnBjYitunppMJVb0t0%2BiyIgkTWRZghdu0sejdv0ZuLbR0HdpQGKdeGJSonGsxsXSJWA1CCXzJxWtAgTts7sz9Ab1qgAlUAt1%2Fx0eg2zMo7S1Rp9kKX8CTOHB95wFeyiOnfubuL3pGXK7IIwe37tqQCaITcuzbo1OyTEcGhGRR74VTmq9NlIkklPV9t54S9LedFf3r0vjPxHh7rzWypQ18QWwjrRXIDmpRWtnQj6ikoyphfFOmoMBrpyK60HuCgLssOsGHLQC%2BzBMT43Y5B23QpgMP9qPy20uXKGbCI6lvMMMoJjDy9qwBZlSGAsU2m448NM3xv8QUXoVYaKIemLu7sAwSs3w0ESaCP92QvbJquvsZAsitAVD4dvHz4ZHjVI%2BG9c7ORG92fsQUKbnoyJHyPwUDsDET%2FYDsEOYywspxamGiFUwPxUStO%2FPSEWpdfBfCA0tVwn6LlhtT%2BSJbAW0ION10y3o%2FQcney9WiAadb98K2CHgx2eihKUS1zIs%2FjLOCv1oVLqxvFGOnvAUfV%2FChEEV5QEVgQLY9WZ0xSALDahry97t%2B8fpkBf%2BMxceg0zOtZyPNTB76Uivdkbay2M4loAcfpHajnIQHHBfk8XP4PvkmuIiSbV41%2FVogjU3vO09eIUOUw0hR7kU5xUf5pFuyyKT890hw%2FnYDH2vgJxfrUkY2k8tdO9D3MZ%2BRRaMBI5aqKWo5sle%2FG4p1UrqPMvFUDE62iKCKxKfTvB4TKDEIf4P2yLyQnRbkfHGPPH6xrU6kr0MQFjskQ%2BbrDJx2h0UP2s68KzkxrZoUW85bdU%2BNXLS8nuzRAGc9gK2lRuHutB%2BSeZzfO2Vp1eAPftOYbQoDxedGKodandjfKfxiulVglxHNpN3vORA3FJ3JZVAEHz31juhk%2FLV8SHdS%2Fpi7apcjN9%2FVbWjuKqqEZVmtxOd1oVpvXDNXPiwGwPtboAdDCwlUoYQHRtYjE%2FTgemZi8m6XlYcTfOJsNJZvxhQQBfMdoUGRPfPrESxL7wXnQvhT55aMzcggev0ORdPIj%2F6XvArH7uIORGtTXgPsUm1LNRiqxANtwG7ydSJXNpzC6bX6VmSMChCrX6KvuzpbY5wk0f0Rdj0tjnETLsHLgO5WWyXvsHJ%2FCtFN%2FBvoujXep3i99Os87w46D8C9OcD5HwE9%2BD6hw2CjZrQ0ljmteKMN3tYKjNKdlkc8rkxAe0mvmBqxGslSt1jMg5IbNomIJcVyMzlp6BgPbj8KB%2FouIkHdcvpJUp%2BS3sKzye3lsR%2FJPUBqoYXHF3cF9HOdSNT2xRf73OvEeVXEcDhnRbEZJt1h7yF%2BSGIFeNSSK%2FU7hY1%2BcdMIjqfgXTdDgBIhQ1T3QacOmIGHgEP40vGKs9l%2Fm9VAVEHCOqwrP3kC2AaFSVOMVM0raZMsGBOs02Q9aKXVZjGdS1wjR3sm3xloyZOLkAR58PqVFwPUmvrZkGX2Fq%2FexuSbd%2FQy1f06ZmS873O%2BiYWROz9uQP5nDL5kevV7QFVgtfVz%2FZW4fH9e%2BW0krcgzl2RAyStd0qC05m1ZJ%2BpaiJPR9ybFH9Ba%2F6fNWqZZYfoU2Um7t%2F7IlLzEn25uFlbMqspW43rTdJaiwbKYz4PuedKrGp0t5p5EkW%2B24q1kktpNVV%2B4LtducDnX%2BBrzosCm1lloelsE7noPDx3do7NLjgZ3ovANfz%2FZcdUZWf34fjPEQ4M1jXUYbKaI%2FflZjqe98tD37coTpZY4pBnfofI7azjbhSsguGnaU%2FIcy%2BP3QXRx5xM2dTKtKVCP3o3KOwTsKwYpiPOamN9407LdjseQTdyrOo7ZECsxRD423G9RQuge64UnC1KnpcGqY3SvILNh3gQibhLQj63UBE3A1Lf2e%2BL0hcW5vs4focqwg9CcWLrwHy0WnLzF1kvOlHm9ZnYuI2ha5TAXq0Ynyb%2Fu%2FvRHjOZtFxvaWTejiBzKWD%2Fz7FXBaXeoh%2BgFzehDA0cA7NgOrXtGjHR8%2FyAGrrjrcMfK1RCH2QhUHjeSyXE9T2XWkWrwCqAtoMgrdnTGC1%2FStHJMsZgxGOQ7yKS1wYSlMg1gR2koZgWq1RM5tS2I8OtrG3e8jqfxFoUipt4PGKprfibiM0tavaD%2Fhss%2Fc3JlPliMnvxv4mKihiBQE8GrIRauSsgOMORVfcIaFaYv0%2BjyrWgfADKOrUADm1Q77fvuYPbV1i7MZ8t%2BEG1yOJtsc1Bx%2BX%2BfRnyuHoN8RXUlPswmLr%2FY9i%2BwdMBuVEC8Yy%2BMhYhTIQrsTLNwoI1u49lA7Ndzlt2fVhhYBDU5eOEBHWfpYSXAUgAGmmuCTlyG8yN9lnkq7tAJdDutvEeL2aRxkb80KDX9I7zavgQUsP2cbEE7rVDmoV6T663DQdp2FQlDjSZ%2BjwFJQZ1DFtvchlX1RlfKo9jOpLfobzXruNfZGTJpwr4D4I3ew19QpuLHCGan3A%2FfTQDH0QDDiFtcc7c0TPhAumXdtZVteJnD7rFsDkTjSuIPAIenuIuiqy2xVspFlNVx3KYU2RCmzsA6PRI094Mwi7dKafht%2Br%2FE%2BlUEryMvq5eazjPTPyd%2Bcf%2B5pRu8%2BQPKwhuocdePibBk08DecScZk%2F98jy3%2Frjbk1grsxGpWLlg4OsFprE55ZKgjFFXSkii0KhM0Q3ixQ%2BiBgqFePPj8I3itqdGgywDpx4waB%2BY51UjHn1CG8wT6R4XKBS1sXsDZUQujVA85bsa7TS5Z6MkYzIpiVOsAneoKhX%2B1xdTaEPI3yp2ldz9MeWLyLNmYvYBisDEHtHoMqgKGH7HW46ySHcjpbRxKMADWCXHfpNxczkh0AdZcluf0amAMv1lSzfIdg4NAv2PtZOi6c7nmHmur2Nci9mjQfhOCQdVGRQmlWHisttyaY%2FgdbGLG41zBdpLTg0Ndyj2i8Z0T0GaCMOCqoDW6Xi2PH3OtBuCkd78Vp6dds2Y7KEDAlASR1XDb1EttdmN8ZLrVjSDq5rwIG%2FggDo33UCKEUakGD8SmN8Titc%2Fhu6XLRCmMOBb7aJNKKLQFbYDnpgcg685IegeDIjmzA1W75z%2B84h3vWW68rHZnztd%2Fc73WJPtL6yd%2B52tZzLPHyB2B7Cb5QjWt0VevPiivEuS6N8de%2BT8rpDt7GJZOEWOahdLfpt%2F7Mh48a3a8pz2w0rDejPOtMjtFYtMQxFhDKlGsCESZsxmwIl%2F8HwC4TW5vNZz5rp6NgZMC7Smb96dEb0fZpV%2FseTv0l24vDGbm0ADxsg4%2FwkCsUiEnfOkanN51alOuAoZW1NttIXSgKnLlTQ%2FHVIJp62gDkL7E1X9%2FD6NWO8nCf9nitOUW7I7gc0Eo3rujjRLwULq22CH1Ua08t4wCpEq2L6JKr5KfYmUC6QW98eifM14L%2FK%2FGl4sMlMBlslCfmU%2FVzBMabdOosrkIiP0JJC2YJyBnlf%2By%2BH9ZW3ncMkAxDpqqBAa7Ymg8%2BO5EDV5R9BI831drIJnlPKDOKFYQeeVlZxvYUpJxftlF%2FGtPCNRhgshSose4MW8No0iHVRDZrliV6tVsHNk%2FDlCxeCLARU9kcH3EwjSYL%2F0ztlsXO8Nb5huI0EnNTRiSQOblaUbbza0bUaZD93cu%2FcjT6Yq0cwjbZ8reS%2FsN9SBG6aPwK9Kj96CWgk9X5GpmyZ089L471G5UWerYa1RhchvY4qc5r63yJbPiFIShFsulL9XNcQxuRUMol0ttui9leNh2Mz6wtU3QtV8Zwim6%2BxjXfsz9i%2FyyAgl4NkCGFs1ejaYyVgWkj%2FxYZknZD1mIH4wkFwLqqEVMYI9fnedP1gBAsHMS1wEcSz8WIVgbCnZ8H%2FUIYN1UZPxBN3jVmRycROxctyrkjXxs%2F1ZuNMhyhqKUq8dk7uAGZ8VfxHdmyuzZVijCyjdA1aQQoyroRQzcFKQMkYGgWh8Ye2oRiTzioM494TOWT%2BDvA5naC7D1wwQZEj6mbhTjlHGMTxkv6nYwWCLA12cv0yE%2Fdw5sciHGhZfIRdZvTPk0Zfq1dztD19ECh28zRkuK0m3oO%2BHqwXjMNL8BC%2FFy7DXRgj8JegzHJdaITZI4wgA1JIyWByfpNL4%2FAZZfzCwOVNlkRF%2BqOazVRE2xsQjnVDxKS%2BZvANRsb0MNF8%2FD1%2F1NfCdYrciTA9LqK%2BmRa6NNn7PX3yDMxXdyY0sYp9V063O5%2B4osOtiP1MY%2FIDj26tkAiGldAfvO6kzmftZLqE%2BvYwMAThh0nTxhGK6ht37bIb3nTwfNHouVZ6AxfH%2BTSCrRCShelvWbx2MKwsPliSroYlk7sGyKrg3kh4MkoMQUOXxnAJGCWPV9Sb0vQjeTPc9GydZopyuW3ZooGKWO78mM6qhJjPLqPDMEHZ%2BBsGEYXqy6bBxdW5biU8%2BU54RMrjBRG1K%2BGfdnYB8A8WfUDpScVQadcAWJwGh2v6%2FiDPlhVRsjuJgZuIwbHPWlRKkNJhBPbQEUsfiO%2BvZSKoHPeP%2BFZA68b%2FSgKd461g8vneJR5S7mcCVJgb1si%2Bhh%2FoxvLkaEZGECnu%2FFE7LQx2%2FHCaacMsiDhaHO%2FQfx9v6CrcnDQmseLDJt9wPIcTL7Fnb46I3HvwxukUuXmDCX74PEb49oBkuMsXWTImwp8e2C2VRsYgZdkn2j%2B1skPEZyA3j6uaiojemzZz1dn%2FlvH8hTMiACb%2BiPcoaqhv6f%2FRETfZh5ejAHk91OzlleBliDdtEPnCnt2dUR92And3gu%2FFxaTUQnXQx5QPwRpLyKSML60J5mZnhjWg52WZGNRkEHhhfvKptk1lnC7%2FU83hGGJe3SbvzbpPo9zYuHpxkBuwMKtDJFOC%2BI2FaOuWLTTlCPmMpGB5iPgrBVvJ3BBWPxiqW3ATksLXujS0BHH0yhVjoMMoTI8SJKfPEIOzCb5PpNFZTKm0CdD515t2B3V8YH5X7SkxNPUpVlSJFUvtqbcjTTRp%2B4uE%2BWy9wtDbU%2BDhJERtbbTEAgavsE7SK9ZCsrr%2Fas5czIgUq%2B9TFfAIUG1doYuyJuEmalCwGZ3gKhCl0HKkafe3EGfn39Ys4pvqjOfahv%2F2shH96nfeJHNoXHHdBYNqYFimhYwmR3RrjLSQl2XM%2FRYwGedfvRYCXJj53v0tr0FisNOti%2B92FsnXQApsfa0Dn5w67Kv4LSzmfrVlTeoZ%2FcXUHNAKug9KrqPcnInGSfCoEztLB800qtqJx%2FRSPLket57jDoIDHAMZBQACiXPcLYs88t89AUAuEEfbF1Ccuf6Jb6EPoP36SGw5iw%2FKbxk8e8hTaY5cJkdCKy7r7yC2aGDNW3rdufzynEj2J%2BUJlMBF9cWZpABli9KCQz%2F6zQevTyUAEmLhhHmYwuJ%2BAs1eMxMlLBRnFuDAQvZRHwSVIzmcl1GSmbBOeo630WHdPhrpznSDmnk1vDYlNSHZnJ6qQGzWRMyFtRv%2Fg4tuvSbQ%2Bbz3%2FPIEFuUIfE8cqPzeHR18ZJLruixaQWsDX02ffjrGRKAIrn3bgLS5zs9Nrste3TmgdAFxla4w7pzmehIwrq%2BsAIvJGSkh4qqDsQlC%2B2d8GmF8Eq3Pc9mkt%2Bm%2B4W0h94FNfx%2FD%2FL3sPJHHgOFudfs2xs%2BLclEmj3u5xNe8DhafEUGWhyS0YUnjuXik3LzCFel3qQzy6gGVEDKLkZkp0F2e7Vq3VsEKWNktif3JRgkWEqccv77CrJMCFxD7ku1wIwu4KoxUtmyPfQaRAnVeKH9A3DNn9vHWmBcieshRwk3cirJiOli8A5191E0ohTSUeYoz67NvlyKpGE2Aj%2FAFgo9uWAFMYofUMTBfUeIoT2avwG8HwaZjB43JIDpNG%2F2td9sK81MbCnxVhAiRv8boqSK74Svhy73heejF7Plha4Xa7oZxHWVzLft2tQQeT2uKDZyMjq3nZTj%2FkX%2FTU9tk8wgN9ZdGKxJ9rqmdzOgnvZ8t%2B6%2BZAqO8WqHnBGTncfwrhfaB%2BOMWcA2HfjBfcYMfAuvtsG4ITodGDGK0InHqK6HhoAy4VRp4HRJYiloPvLJpbvi5XAnABIpCcEwUEnmyPEDsASVEdQMKDZXr1ZUEZ7B%2FIZOjA95dLUI1PPYzKY8A1jY0PjapdVisEZ4V1v7T7hiQNGWjCbORN0REBCAKVesmGdtTsRJ4u4Rcl1p5ZShhYiwau3VxB3%2BaVhKzP4l4AqftaQrQKvaE15vBuyK7Hd%2Bv07ceaB3NOqfAPh8eMNZThy%2B%2FB1Q2uH0V3NS7eF1v7QytcJzOhfhVBx4dlC%2FGax7LzYWj98b7ih7rUZfz4ceo5r%2BfX2N5yrYJ3wHzDiAm7hrF56P47RtWBC4vN%2BZJKkLB1vCzIMPQJ2CHUNRFgbpjU8uv4HNOnlyTyyAw6WaxAalQ8MlUrQugf9vcPla3UsUoTu5RjAK%2BBfX9p9Pf9PWp%2FLJv4QVgXgh92cu%2Bgylss6y2W0a2JK%2FrA6rD%2BWsSCgu6lWfVI1rrVH3JQCTEWmtBCVL7H9rjlh8Qwo%2BAdGRxDW1IhltlbEgcB%2FsBpqqTKDUq2oD%2F032MA7a%2FlhR39R9vs2aHYxtZAXgktz%2BNZYsQsTQiY7FAcDbHDRg7gWXl90YUvrY23CQdWXGuCecZGBuK%2BqL4XdV37SW378S87J2rRCEvIqWicTVxI9ga6T7m3hxUysaksVei%2BqSLcr%2FH6mf3QSCEOAT20lpIgimdEIL7P8AgEkeKvetYXXBSAbFot09SRdjdMIJlArzdIB6hsfaMZabi8FieT0BiDJfccLrfFCOlK3wARuffVNgpejVD20YzuFCePLQoopAa1XE09vT6wRR9HrNJsGH3MCE4z5R54UpTKW%2FB3RaOVS7jkUmcFpgu2jsnmDY%2FMYlcLb3LBI6E4f25XgLR9WhCRnLIk%2Bbrvroax6O8YWSMv3UwZSRgjW4BMyUe9WmU5ndqcj7fU22tyeyGaIPtZSRuohUBukRjhWCy%2B6HmCG%2F23XRf49turMaZVxkeT7Uv9YLubpNVScSELQwOKnfoIEiSQch4eQSNQJnTamWO8aJdVaWpL2feC%2BSBBVMjPzYDNMMrnjKGre56eNg%2F1VxGWwvXmQEnvTFwOpQcueizvtpSQmy%2BKHs5wyz5xIyLfOxeo9%2Brn3dLAG5gNw08vFXg23OtpJ92tq33PZOFQdeEwhIOWm5TEqWQ0FIeo7PY2h%2BwlAGpI%2BtOiexr1ZElckLrTlPjjXiYg6SOn1Vt2WLZuB8A5xA5ZkISqeApdOLESLLUHFEpwThW297nOqkGZwCryNvDy9g1iVXLkq1TXslLNXQU84xYWhYFrE70iU1aiCOGlTsg%2FNPU5DAUpXPR0nwJGieo%2FhRvd6UAGXta2gncXjANZcLfo0aMqBQMUirTpMTv5qs%2BR9uWtpjfBzykgtvu2kGN5sGoXnSZNwUhjoaK9dXW%2BEDiRIygjY%2BmNtBK6UITRmpqSflB8hgzEkEt%2BCgYRUlX8DEwYwiLcLd2t9qQVEbHZGCml8c%2B%2FLHq0h4LEL0Eir6L3pnQ4VlZ0XtKlL9PGDUI2KgkGEa7fSllCi6TOOHnJMxjIJqZiRGsqKor04h%2FJC8FBEzOxmzE9WjGEjfjap2H6N%2Bykws26W653rikhFgdJ9U1lGYozwxuOr3CbIyijLfjx5C%2BBnKiaowI06Z01UU5kShPPs9EuAE74FL2eKKbXG8l9KIHpk1n9BQsIaxhiVRMRECWiHRb4VVaxFQ8Zbcwe%2BFmcdhAS9VHxqjFW%2Fdm2ZrPswbf5%2Fzi8m9yei6k9ut%2FnuhhPcX9mnJyOqCtpGxBQ52MdTPI4h0UE0HC%2B0xeg48jdqmwqxV953KprOXWc0XgeNlIDPlYzX65fufN5FwRtWQ9uZlIi1BQpNJdmApAwx%2FJoW%2B5YzVviotViObokXY31SJTLniLKTAzIGT7Ppn6KywHCmMkx0lZ%2FG09XBORAgOVItc1jdJoWQEOzO67eSRceyNhVguUhnwewaGPm8rDdh6JJBMa%2BUYeY%2Bo3Iaq%2F%2FJyvERYKOuXh5slBPnmTJ%2BVuLYl%2BYpKLU3MhJ8Glau9h7en5i%2FQImQxNQu7bw5XXDTFmFYUSqJEhjZlTE8YCWKfwATjMxA%2BtYOwzUYfEPwVTwLdUi6QNBa0lWq13AwqQ97rdJEwInSwJcvErhY4l9Puay6f1JgA8gHtG6FKyyOCdz7NtXqqZemRk4eqEpmob4mY2392RLlDLXJs9dzhA%2BYWcZnGn2OhR2CQC%2BnCReGraNq5xl%2BgnM%2FC2MLWaloJzvvV87LyyuCO43znqp8TsEupm9gq8h3mRhjOmYMqPq6oQKt1KoxEiZ%2F8JNxhOPyGH7bLlxQ839hc%2BOVB2Q%2BA%2FHenv3E9v9JyN9WD%2BSifw8CK8o%2Fei3wSk3unJbNRAnpNKdhK0A4yi%2FWr%2B%2BX8%2BZgnPF5ht%2FcQ%2FyFoD67MaE6cz6Wb7yttDoNO8g6hm4aG4qoeVabRBuEfZSjlPPZH6gctDTyRdWfepM%2F34eQq9nlj6sr8PU8yKUPYdnaa3k3ZZyPaibh2YQwBJ4vdBUJyFEugedndaQayCyglP1F4iiXAKDoqDe7cDdZHvKrzEHIYquGPK2qO7n0UkFmV4sDUO8N898wXhTOqawIrvmcKqztU5%2BFJzitl6DrTUTLyTI4NOEg%2BfbC3%2BqFO%2FBJTnOHqnoocOIQvkbcp2VkloAN0Oq%2FoFXCEMfn4A8IOREs7T1w%2BHEvISqb3fh80f9a7L8NlgjC1AVy2lyjG0WW0T78D7vk6gCNCHcPpN6NNCeotC0nP0mDdfiya4gEwPv96bRcL4PXm5kA1uUuEZiXBPNAIHEHk7PZzh2ULW%2BTYFVpjnDRHM7t73jtDrYL7CvKnotgtXn4x7dNmG4fLUsCi4LFqDN7mqy9U6d5CdX3WDJ42tTfV8j5Fhly224vFvx7ys9iigFbLI1pVa3XVGgaWeLYltJYTkCnLjfJOLsuwuyn71tHz3PmiPceZa0M4kHfHvuTSrdIoRQDSwOC2l6B4TLHzHF3Y%2FyuQTnPdbvSYFvgmAgJEWkNcgMnCmDZbFQsC%2Fi%2Fzx2zp7c9PUFVQZtJNnz2sCxtWg%2BnOP6nSa8cViirUkqBajrz0ByV63hET9XFa9hb6wwNgoYCngnaFFiZSSXWUEItOcNTwSQK8xB6IUd5KyfhTabXnG59v3xHjWghnvJCg0CIdu7CA4%2BYM%2FjSZTMbHxSWsMqByXYxI%2B1bqTmPyNSPUYUUkjqp04TGb8ArwVUy3ovS4RzzH%2BThLw8x0Fak7HBlzB29mHACAlkzeVCEtScbIsbRFejZy8HvfFFCykAt%2FtE4e6xXMxEk0174hR%2BdazbZrxE4lizuQirjUXIwmP6mELS%2FJtvStjtujTFTPf8cIXAYpXJnoaiXd68AMLuY0GbSR4hVcyZ%2BvXzQGdMrmfaDXW3qstCcvhEHHoicV8UYxvAfP9leZgg%2FKWUQ1p1ffn6pXd0qxTLxAYSRiwfxo6VcQsxNCstFqKm0FD8HKFbtM1gkFKWrjhWFumHYiiFdsbFZ3EB0qJRjxjugpayqhJumI7cxIPLy96vwuoxfB0ccKsvBclaaWJe39KCG2pOXERc7PHd%2FFqxYHQHctyFA6bCpU8M8rC7YfRVK1ax2T3I6vFtq4GoDj0y2tirV0O1HgGJwXEwsgHhiyEF0Zv9NnwWo%2FzmA4wQULfLuRYXut7WuU%2BPxjPI8vcwPnDwzdph6k6jvn7gyKm2wE7MjEEyiqhHj5MdPymslvrtaOCnIJd%2FmtiYZbh5uosg%2F592X9fgnY5ZkTXK1PP9n0%2F%2FPJTyWf8r4iw3ffUXpKJEh20MfS5eQ1IxjL%2BMqJK9L14nTka3BRzM%2BKlCGGcjXGrgoLNiP4TNqTcJPX61SE6wd70X7vF3GNuhQygF%2FJV%2FdXa%2Fm61iweuLOfPqVM04R70i%2FNyAA%2F5Z29QvufDs6%2BVuMZQFZsy9J9%2Fnr0eKx8zGb3m7%2Brb5asiUV3PWVzZ6ZhHWPg90dTnc5pobrrYw6n2ODNhcPA7a9q7SdiB1Gy8HF7Yc7%2Fw3VQ%2FNrUOjl30U0otT9iBbfRGbDHhrKbcwm1mz5CjZPk%2B7mh%2BxFfVdiOwYkiAk5tiK1NC5sDmNyV1w8jWqYtVN7g3TmwfUyKDOLMp0bKx2qnlNFf%2FLOOscZYHBeJnFZD61U5cdwsWLzZz4G00Y9uCwbZcxw2uwvYa9Ae%2F1DW9ojy49xenUeIsGFC3EWLR9IlyJj8tOeOzV9zqLZUaXUnQlcQIMTn5YZDuXIOEU0LGC%2FQGAk78ILgZrSONQ9aJNmTOvsm9PC%2FVfJrhin12gaS%2FRs20WV93qRMhfNGiyFz3ng8aNgxFasnvkEe0fG10Y1ytWNppsL0jfqCU9U8uGzEEfWEIDeTkQuSBcuM26uVPR757D7qbO7FcjRPOlycwSt9nqq%2FxFL%2Fnm5cGqmo%2Feeqrs2KY%2BazZD%2FDrrzryH4MdA2nJxnW256B5DFCvHawRIOHiOKGK3fkWZrK5rfUnkJtetChd4Ru8yIrzFJ14k7RJ7Mlz85sc7XT8GXp4BinXNPjhYzdLpYYg8WADCjzhSvAf2jvJ6BJPbQodi2DcLfpKWEciOE08%2F8CSHCnPlk1eEfxOC0y9vo5xXLtBa66Dkq1vgvdRTNp2tUEAs2OCV4%2F1ztXBklNVAgCvWfVgGKB8B7KqGm4DcFnuVLKa5XHq0YPl9u5ld%2Fs2D%2FUoNhIMaawIsRBCsgqxAMVQUfdm3tFkMCU4WlZdpA3R2VsxWI72hEZxomKECKmZdBphS6sXyOqj4hq4QxM3G%2BHA5WADxZ02Bm3Mz8QyXEukU1kGd75fX%2BOv6dZVxjHz38G4gFqlbXJ79XJ9%2F1GpkGHgoJSM65HHCk7l7UPFfZBctcjUrToMNVErPVhJ7P2WRTI3lpFeXoLSH2y5O8LzXeGrejXTyAAdfe%2FgZOS5jD%2BEu4kSnkA3P6zWzAP0af6JG2Z1MpSlmmLEw36Bol1W0mIB3wGbddU7bZHNCmGAam4SePejG%2FCmIeyeZ5KZoyVm11n1Nf8QotltIdtF%2FGXRrwXnBgUwvhMOdKgGzPbnxEmBalfaReh7L%2F2OCQ6Ym8nvlQGNC0ktxVUBXDFGw%2Baza9ObcVGxFZtZIuYS3wxcRacjmZcEGMGio018LWm0xj8J0kppF4pHhEOXp3klLpWCJ77W4da06rUHmonlkm24GJJN70qFBm4hxU1s0QGp3i%2FRo2gd10kQrj3aNMzaEu5gqkydNboqhA1yGL88mMKBD8fequ2A%2BBtoVzMQygGKEfLvQs69msu%2FTtYUbht2Gjmp%2Bmn8YDTaM6q3pX28GwdvcD24c14i%2B8Mzwd%2BzYQbUCGd8ZY0OMdwjRxbbxeuzdT%2BAHTSwTKLT2l4qx1v%2FE%2BJQuFaDM9qFmE%2F1zUgy9JASALwb6wA2vqcdFjuq4GMDFnJYPQnojCsgEB%2Fmo8wgyw3hRIkZbO0ePX1vQj85pnnSiaIqj8qH5S6iTWMPWWtU8jXydnj27OAZnMhUQNFIVyif0YI2KiaoX%2FtkmIFqBZIgvB6qz3CWEQ1MY58IBdn5k8WdWlLBdRyDbNebaw0aCu5fcqYW%2BOyRWmLqS9%2Fl%2FYK%2B60XPSDitrPug6XUenP9ROT2ojPBefaCba4HNVY16teEY%2FmmGYDmIAAiK%2FTps70LjJWgLgwRgX9jcntJ%2B3%2BaPDUhvmVRuGHLpBgnMvK0dt5pVtcqQfPlHc%2BM266T55DCur%2F%2BV1c7QBpFDKDe86se6Hvj%2Fdjaa5MSJwk1L3d8QjTZ98qIJpWTTHo7LAdYF%2BHuNfvzAXEbdNSV5gocUBnA0NS%2FJOKu3VIcXRBB5g0XTZI1Im2CBiuZ9MxjPvemy01g%2BSGEooemBh1qhfzA7MpOCgQ72b61dwRQz95kKs519AzfDpF6UWiIzl6GFtANTM2gPWQKSpvGyCYcUQGDgVQgwQN%2BFu512MWLOe9dxDDBqZUuizjWfJheTP0sq7TZcnD9jbWyk%2BD4vrNYZzLxuqj66mTcJbzh5YP4SMpB9Y8FLh9kJuqGes9fUbJGzX0UVmlSaQLG4x%2FWfSaFWiWcJrXqsI6zswFRWVQL20OAs6HzPyPxfHA2rghbunbLD0s3Vvd1C0QSnSqBe%2BLT4QMrkyxuNOiXfMsBdygCLYT8ERslIPh6wZuHbD89JR47DcixiKQedHB69AKlrwJqq2wk0m1QneO5nPz%2B61h68Jkrrx5MzoA07F9F5a80HKVgPCFbsH1dmf0YlLB9TVzTKyx08Uy8vXplPqGSpph%2Fiy8byG2apvp%2B7h33mcJDY2LsJ8Scb0KkQQBOSTam%2FFsiiZeS%2FBhWma2E9l5xhkkAvAr1EFs7jk%2F4JtgR%2BLYe3pfkI5okbylJC3h%2FWVws7cY3XHIyGz36F3w1%2FfEKUIfSn1V1hlbGI8DzWpPrCy6hZYBi2j8GMoidIy9d11qyN%2FPHhfxbnV3kbGMYElmCWrFCKjRuj1GYU6JK5y3gYQVXUDNPW58l6cDil79E6ivNEg98FOfjM87sQxb5ajZyHfyOclOJMq%2FSP3M3WoKThdNk5nTsBvsPT5tzwvu9BdmgDwazWi2b3gYWOuIkH7bIB8C6xzmGTDJk4%2Bmlb4ZDlwXiVcr7CvokGOidNF3%2BhvxFQiLXwpGLfFPy4Z%2BFj2lxW1ZP2bQjVUynOfvT0hnYLdOcr3tOlSE9vv%2BEqZBc2oKZrh5n3FCZ5NTXX%2B311lzvAwha10%2F%2BMKLSXN93udtYonE9CPke0Z7eClgi82nKdqY4QKbnS%2FPJEdq2%2FhTGsGXx0sowoNBRXlCWfo0WqWNdxRHkbmMaZzgrjTWXuKIy4aSvcuvEY8Vs2eslMfkAHzCxdgICo51rtbfgaA5WMeCWcMoNWDe4m2j4Ho%2Buj%2FxoGvAGsgSZv2ryEftegprPRtUZksesjfdc1BjqSckdplusFliEq850hn3u5J%2FfyrSTXt46L18KFrF4PpNshxBqp%2FIeNpgDeXI7bFGUPizVaaxKVupDsYRlqAnjwPPTPaPQimm71fAZLzU62UKGxCdZTp5z50qah62TvBXdqIsEEKviJ2URJMody%2FUVEVT8LKzTCAt5tutYjqzo6GUSWjlLouEEgt5TvhEwEVN5j6oHa%2FMwkRYLIrZ2iUPDM%2Fjevb%2F%2FvDlCw%2FQElvotv71jY%2BpqoY%2FpPc0RsNYoU354TmrqzbDQ%2FHQWi3wxUQXWoiDbC6KcpN8MO%2B4qtxNk9bUEmhDObua37Jbi5LUN0p74jO334uuT2epJ0gvciwOTSpQjo%2F8iTFvzCZgv5rZpNdH8m2Z8UeGgz44i3DfHzvz9OOmuBeodjBAARcTdmJcEI5jDB2RIWhTU6TkLX%2BzdmZfeB13zZ3uq4dbBsK%2BTn2NlQGXzbdj062IUNyL8LMPi6lt2oT9hBzwcGwnbtmNa6vq0FyD4Tv4QyCtoZPA4ST8Z8jFbW7DDdi8%2FnH8z9L5Xmw%2BfwoQJzDeo97Uo%2FSl3MeFuzjxz6FIE%2BFwdUq1gH7D1A0SffBSAOl5FJheXwC%2BCY3EWyliFN5J7dKD%2BaBardjgolz551%2BoWv8aUzb3jTJBeswdZ81%2FcBJxoislKUt4pk6EOvr%2FaKEdYksdo4MtLUNqbk1viPOFm3pjLL0Rb7rHg65U%2BS%2BLxVDgf%2B%2FYehZSrbuK4tkSAGXcDVixhRer0InTjebS4hn4it708k7zMWFqlKsadV%2FIf2QrUmrkDmVbyYGNkkvo77lHqEOIHTSh0CjBHynIkiqT4Fanxjbpdsaj8D2PjGb6%2BE2ppVSactAa57aCHuRyeNil77eapYZLTxK7PoDuaBE9Piauh0wB5kPKgPJ61rETxgh7HNxFi0m9Qyp5zlG9OhsT4TvCVCRrXJxEkaO7GWYZJWTv324%2BJ6ukERH94m%2FX%2F%2BqoqluWeblRqjf7ec%2BEsVDpwJ0qngUW7Fjrk%2BZsSakKyiPdVhaZOz%2FwdgfBkrhHb%2BxaHhbf9eD%2F6ibn2edjXUCFjKlQw7RVVWxHOF2defxN2i6t1DuEH46HWY3UDjEe3qYaR5LlHuRf%2BUGDv5KrBpUJ2Fa6GdiDRVJpuuy59b86uZ7kKBN2ZanOSFqs4yHj0%2Bu5YBt0dPS5JtrCUNKfhNe1mDCMkPicOBemLa7x0hcZjyqW4wAYl%2FP5CfNmLoPbmeftjFnOg7sjnMcU7EReSZlVEON7fluL1bU7XUVvzbw3yQHwTy6Nr8rB7SUUDGImiAUdby5dJsiqWS9ZTfhlOy%2F6aHB0Qhmvx%2BHGaGALak12nFQ7ch%2BcX6bdiMyiBDxcKCCPRYibfJuo8sglT4BEJ%2FTzNWyUBPYk1lfSTqkzo1eSBO4HO5B%2B65tvVg5TM2d%2B941rP2lVFRr2oMrtMcZqSoz4iNANNb9h%2BbFR5oOTc%2BscM5TJAyA4%2FHjNeb9GHRNVBEj9pZkrKxnZ%2BzQqsrRwE8CPtqzwr7lmQa4hDFyB%2BLgJMIvygA2jT33GmzkbO8e4UWWGB5d4I%2FC37LKArbjzHq65w10y9qfJil85moq%2F7Fh5oPGvSe%2FPnWZjYQQsnTsL74a7b%2Fpqj5iYZ4cjp38kt7%2Fele%2BL89WgTPzfaVowyJINyX4sT2W9EZcxyoIYU43CemagL3PFsAgU88XwJbpNSBw%2BOoPFzArshuQBr7ELUAvuhKML4AsiMOI0GjEZPhx2Qa1PANSUeb0GZRUusNrq8%2F0UheLMjaY0YLYSLA3cNn6TKhddYSMLalYD5fyCmhtEFzOivmPm4aU%2BvvN%2BQoWexIaBlMFFnoI985s%2B%2FN5xSP57pBm3ppyRrj6GPlGy%2Fon7RISI3IVuM37t7oXeRMXw18I5y7WJ5cmpUraqNTzmO9%2BmPgDpuBI2bCvOfRa%2B%2Feqch3ZHorLXKX560MHWNyVePzyDflsQNAyUzUhWDj%2FlLrQUTv%2Bh%2FfEPwojwllxaE9VCF3wgY6GPcb%2BMUPNOKOrF6y72HfN8OlIYVm8TLtbM%2BOznVkImXiHaCVmN8LrpuAYky3waBGqeJ39Kb4dDMLJ4TkzvAs328opFuStn6jL8Nvvn8CFVg%2FVqjSDNOQ%2BkAQaViC4DHqiM32gtNGQDIfhMVy9t03llKsrLVxqqDCYKPc%2BEoiLiC7WLfUNge%2Bgm63vdI3OM9qSNTLgsM984WrHmSr6qqVjTjPRTFvWyh%2FB6fOMeLX6EESrdGamv4BXoExyInOJ1XL4nh2GLpatLWKmeP59m8fbHVLmqD6auixFwVngvl%2BG0ZQD1Qri9MXKiaNxaDZim7Zdcf4yQWRE4OdxrfgqUdG9AO%2FQNpjekzz4ilI%2BTDzZYvFJU5bp60e%2BZDyMF0VZpAHPPgyzw%2FA17T%2FfA9yuQV7mWMo7NObkwknTdWARx2xSEDRgFOzYHlkC52brRx6W3hqLMOli2rzyME%2Bg0Ic6pYbz5esEdR0Kx%2B%2B404TEq5bnUATxZiDiyo7YKTrGHYrQ1CZ%2FZOfVefQRvPRh0cvUGS1YqVSwPimkHq0sdolW6t140hKAuj6vkE0cyVgcT6EbyayTgQk%2FBN1yg9AEMuNyasRgBxETSCMd7A0CzMDQnj7uUxUOkeFucH8QtN07WHyOO3EjrcTZPgrmIj842502BVqRBTTvJkg0HIx30RLkQwTf7vz3Z5eSAAkYuBE1GnrYSVYaP9BVsittuapAYXN4yZqzfIPCFc9XvTcNSDuV5dCfrHCZBXAfXQ0%2BpipIDD4si7kKP%2BlSyt6CVhMxsJVAtlLnK9cahRurqe4HtCgcHNkXMlJ1QD4PEtuZ50FCW5LV7UWtTzE6pMi%2BL9qbJBFtU7NZBInXgJXIZXoBBvtWKVTxTNLSarJpdYHhmewFreJ7QLv0iB5gVPgnHNdoMxC8Ji2coqFVuYTsEehx9Qtc5BT6SVOnUmM7jjPpevKOkjkVhR73cUr%2Bc%2Bm06JRVM%2BnJu6GCiZTOOb5haXxwCLylWHXwKQN%2BSXbSBKcIw50Gm6d5HHLQRjZBTkjKlb9wOpc6moYcmv1MEbBJ7YXJRRCcYrRSuXmCZuIDU7g8EmNMZZHKSknCc4bSVYau61JX65WI%2FtQEBQQYTkF0jAiggLb7bB8dwfOBfbax1MylQgru%2FNwAOTYlxf3YktOcH5M96llrLK9TBAwmhN3vtFfMLwiIl5hMeK5phvM0w8uZKBi8MmjzNt5SHnbjd4mH%2BlVLKlFtFthRi%2FMiTTxTBC397nFFeU3%2Bs8iSeqzBiWertzDMe0btqtm2KbYffxBDM7yLMPResPnDIlFLoeGo2UG7mDXnu%2B3trQTDexahH2zzmzFg1Xr2AB0ChGyLj%2BKf7h3z4%2F0VPJk3V5%2BlIGagCakTbyiyGoQ6bdLMFzap6gmNe4q7127KdzOj3bsPvSRA9%2B%2By%2BFo8Ic8kWSUggCcBHh3gXMLYsG%2FQ%2FgfgoX9ukFElFKD3bDIch1PXJuwIqstHUN8kE3EE4LG1a1JKyUnjg5ymo05RSiZkcgn8B3KOtr2143wLWfT1nSlyg9Ia1VUfgs%2BfgS0sxCeorxSreLyMSJjGIS6xEEhDnfSGtwS6TRC%2Bpg6TKxfbW3EVsNsTegmHVYzpYITBRrEC7jxEheMMk5ZNmCbzCc2AARPBFk0dubrHY5MkliVzp%2Frm8e4ttr0fLIRNZvIG5Avi1F0CeXMYWaw5wPvmkA2RAvD4D3q3xtW0xRibPkTcNEJotPEBC9S2Y0zzCyAp0XZFBHe7uEeh3EraLCMSfk41HhAZrPP8sy0jW5muwKpByOY0YO7wuGx1CyUdZstGN0JgQwyOIvmA04bLHisEt9ZEcJUCysXmwkVQNMQQfkkpsItv6VOO5eCJY2dMNfkxr870YavZ%2FQA0muRUL8u9EsfLIbz2an0pXgJeB1lBEOO5AZckT6nyvxuNtLJOxkHzcprUyrviE5AM3jjLB4u0%2BlcdchJMaGy7qYCEON7Mh%2FmjX55TxYJfdPnxTtUSQkFLGXC8s7ptqgpA5qChUlTK6Idj7bAjDwUVHFzZ5eXa1J9AYMo0h3seF6boyyr3h9ZZfJ3kCBk7EWS6yWnOuGTw54iKO%2BOpfZjqiDwwB%2BnKjR77kF8UsnbOKzjo2CpsR2CPgxoNh6gxj3dL2wQ3asbfsm5FI9gCGxBfTh8UWQ7s%2Be7hVfBW3%2FcThO23RCkZqAb%2BMNfDXwO8QyMWiafC%2F8zgLq76Y3cnukEFRErn7bmqV5%2F0iKEKGejMA0X%2Bk4udOGavbN5p7qqXstwKwPZuj%2B51IhoSyCKoJKv7orif6EPmcxtarIOtZVqPC%2F0GlYrVb2k4o157jEqiPmswg2gWhshQHsmsIedHFoeqE6zBZSNDFAfFCz87zYlH6sAoJ3N0Ggi244coUoLA0wpReSF7qq88wIVnhsPHzOdlLnMWK4d4MB9%2FPw2OGI64EonE4y2KCCn8oLTk2Ng82jOdujVJorke81jmLBk3524V7Y3MzKRB4cVokCm5reLhG2%2BeOwfOVm9AD0PBjxKChDC9prqNhoj84qnne8kXj2inmbbwvlZUqWGVwHBKOCJkX8kpu%2F0ewh7bYFvPCrPTyqXqY57GnKbyO6Y%2FjCr3i1oU0%2BygFHFcbnOuqTzMV4r9992J8rnV4YESkfB8O7Fm6YRlZu14BgPLkUYhj6UdDc6yzUVyZq95OMcantRaEV1koVa%2BiIso8IAnCSU3zZXPdmITDq%2BmOj0f5JWxcImJ0XDrVilMCPR5vRdFxhVeLmkXCc1KF%2FgeLN1FOu5C4Jlx36Csu4stjorHhQlsSya4rFG5fnvXbhBXPq4%2Fg3U%2B2oZgY3EtMmWIbgTOnZMLHWWCGsbsVtMSzWHIMt34LOmR7fQ4En4pgrHh5QmqICVGNhecQu8iH5rCPXMEuepUVM9SJWVbHVZ4P%2Fat5GeQhvtQpCWzdER%2Bcy3SJhMetqpKfJQq06VncYgNy1U4imtK0akQedmP68cXP2hTOqOCJvJ2AzF5C8vDJOFNvmqDj89I7ap7qH6I%2F2rvOhX%2F%2Fc5%2BXkYHvhhxv6nuUcu4p1LDwklyCBSvCzEpjrgwwMH2k2UbJ1sebhDpolm9GrbtowQO16mJDbq%2FGGwAS%2B1PYfsmUe%2FKVaZ6R%2FV%2BBbxx15boOJpWeBd%2BeQY%2F0XsAtoTGCFI%2FKa3mg6MEAf%2B%2FME3y0Q8e8PtlS%2FNQbPVYZQvZEzCtW88pIPU%2FGc4mWlesiTRP484SBoV5CoSqgiwbzILOjtUuifxBCdvS%2BFpcIDkCyVV5ba9UkV%2BzCBuquNdyWisAygI%2FjFlr7piEmPPeTrPnHCZmJTdeo8Xyx2jElkc0Qleg%2Bg9QkJfKHsDHD39CkuWjUDJAAjNy8%2B8lqfR0Vq0y7Kvg673vfFe2TaEWNdtqb4vaiJFkCkrWqBtZUZZWYlClvJkuHdkB2CvFqPtNdjRKhUTFLKKgt%2F5Q1DFjKSLHbFpRrpKWElYZATPt%2Bv7%2BBIwilUsXefEC7PBZSfgfj%2BE1eDR3cx0V5pIMmH4Ya3YaNoG6ipI0cSghitV6V6j28ixI5IhZ4u8MLwK5CpzSB4xIzGtnxgDs4mJFqs3ANjhDVtVQUMmSOBKHsRIrtBBIdt4TTE5Khnr9taodOyt%2BKcM6UAb5YTy4oQr0yfviRwyJDS%2BbkaTa7dwvjABuxqf2%2BlgZrBsqhEvgFFby20JU3FVmJEYFgqN39Nn%2BcM22MDh7ToF%2FpsVIsflNaaTUcqQDNaXJo3Ok7AywV1XvSeE7hzeYZ2gbiP3IB2zy5%2FvO3swP9GUVtu3bPG3OYs3xH6%2Bx%2FImwhJc5QpPxNBEeEDarmR6cZloKw0rbl%2BHXJGL7AJKrKN1wlctQfAdroSWLkPZ9Ro2uSMWoiIJS15NyIvBLke24Ih21n8bgRb62cqfE%2F20%2FawI5%2BzddEPUkP4IW7TACHW8WQ6F7CgVWEsjpRVtYswu0eKOx%2Fjtjl93soLHFNvhM5WNSaPbXG6%2BAoGC3s2uZpfZo%2Fbc0xawwG4kzFnnP%2FnVvPwwrEa0DmfgT9XYraG4gvqA4rzqv%2F%2Fl66%2F7%2B8qOc8AJV0Z5Og063w2TNnrPXJun9fHCC6xLkvGC4KCUm3PB%2FhctKWetw3368PTtNLFDzgsqi0tmbq94KbouCcnvGkz46e2ayhmOILnz8c%2Bwgpfz%2F3fZUqeL6ELPzknwgBEU9S%2BnhNo1J3RWXPJFhhhNlnBVv0ayEOKAHM3VXXgCgOI9qYlrESHWh%2FfvPnG6F2rf2egjF7094gYR3hNAmXI82BXJd2OePzpt4qkXkGexY9%2BLBHFU18Fx4feVXckUxJrT7xzL7FHJgSUwGjIGuZCHm6aipMVKtiTGu3hXqRaIyTegYFs5DmagDO3SxMSoF7JRaxYSK%2FprlJ0SOkrKXIrx1QRCChS2pdLzpK032lwNGuJIuVjjXW8szWU4p1XEhhsldadbrXaPxWXg99Z55X83Wp9Le4RKBIjalHwCJ2lC%2Fu1d5pnwwcGY6iNW2BmgAhC211WY3Ujol2TjLsF9cdWY7ZmEO%2F1inHs9c3KoJWeHzEWv%2BlyboaTd%2BhU8V%2FsFMSPSnDdfnqYN3JRYc6823EXOF1UISCUJ%2BMsbqSKLmMFB2p96K2MEVgnYW%2BQlEghPhYPaWF9RqP%2Ff5PCfSl%2Fu0CUNNBzkvjc4f%2FIgGnIEZDHZOoqcictyiv80%2FcDgA3YZ6noihvtXJI7e%2FQ4pGArgHDV7mo4gEPqTOLTuFogIFg9PSR6jXt1OmpApjaHpDGlrz70Sa6rOU8ZWLbye14Ep6s834r5CiTLWPWhS%2FQoqJOK90ldT6DAZg%2BzQo%2FgRjbASLbL5Rv%2B%2BKch1MujMmIWKGlIOJ9TI1CCjlp7uljjTjAFc3OyUgqwdVqULh9cn%2FZ4BEz5wiXnNhunnuBvboZ1kE%2FeqNuvSRCW7nfeSTwWEN0fgT35S%2BtMSlTL%2FwohWKoIx%2FFzzt0if0CSo8ncdQvrPHouVB0bJI80ZV8P6AvpFWaXCA0TGPV2cNCX0nBua5%2FSeD5P2bWxHw0866Ilk8mOMLynHeV2fz5ZweT9yrxpCuQdNkIllsDqaAERZxALCdXfZB0T0A7PUSJynw1J5MM%2F1dpYU%2B9awo4AckvkLtUYAeHd4rt3zWgXAkRb64zkofcmrUSmOr2ZvlLdat5B8sYdmg%2FDYn6Y6WxhYyUO7mwei3NwVT11zVu6GXyo7XW%2BUWPyN0QgxlN8KQXGNWiXYnW0CYjvNk48GqRZx5U6bQOxvNnWSctD5gytZDYDMah%2Fwa6INjNcpIgtM0CjMkIghylcbL4Qr49gPePqei3Jhmd6R9kvw84IEa7XH9fYTlIAKPUF37Iw1Z2dB5gKNopagEJXX4%2Fw4tRRf5Senpp7d1rA8VKo4q9zI6YHlUrciAwcFMi%2F8kfqf1TvsZYBNMK0GcocLc7N%2FzrJm2LiwyTLR7b9ThWVAJKt0Ii6wJfK%2BX%2BdREIYtCAwuHqfWI6XfuTHyw16lA37spvK%2B27wr0hiFHpeOoAIoGfsIVpqi%2BlcL%2FZcCi0gKNXMI29mdl03a%2FJ45p5i8EI5iDT9TZeNh%2FRKUnsYHYVkgJVP4pSRzuFYR%2BLX2yPVjuYhBAGcM4xWPJJ8P8W6KEyK77yxNmgq9%2BZ9VjnopkBPV6IY%2BncvmL18spIpk52OG%2BeYWGPFb4uE4XiF77OtPf8%2FtC7WZc2lFBdP6P0Su%2F5pAfbD62UDI0uGIL%2F0%2F%2BrsgqBxCRpnSpVecQCcuGX8rlgy8ClW2E7qLE%2FoVKOZEhRprDU74ATmWps7k3qct3TtxdHGzhqSmHkpkxw%2F296x12jx%2BV8vpUmezjYS2jKpjAWrA0GQd25tbgCbHbt1LHC002LlkBv6oavG%2BFh%2BF6L0GN3fnZSqNjEDNEAhJ0XKMJfBGo4z3srA0jAGcCfGbHE559hO97apRqW%2BDKNs3cNwK9BJwtdvO2afQThpnlL1daAQGYaG3rUBqK54iy9bSdwavxf0EWKc6y7wmyeO4yRDIlqwCmVbDFdS%2BwTmy72KO%2Bk37kgKlEYxvpfJIQ0ACYjVqjDHALR8mzrTWhUqqOgKEUomSmx7tAywix00pq9gE9k3SWbI%2BX74feZpAt9mICn2K1w%2F6FLRAHdPkSW1nkSbU%2BHNdFN2U4U3AxUPPvo83W69%2FCjLfxg0M7P3O05nJJgigimzRru1DbXygu3E1N8MA5%2BPFszjMCukpt7PCIY4dMzmfLNlXfqcYU5%2Bb7AcTCcWGQUdWtgvbuy%2BPzIgg5srrUHsEsu8q0DAC0nthmqiceXFi9Fu8Jtrq%2FKTjC1Bj8FsH2DixzK58UJ3WsG0hFNvbYXAu%2BIiy%2Bc7N4eaTWYZuatZ5xIn6aNEHI5txzLaussQIkOeHmMWFmVBpvAXc%2By9TnnRZbateCT9dfxPCEAQpT%2FmiPHlAQ56IRg%2FG1AdMk8R6UUOuqrrVLsattgknFmaBjOjojgO3lecNGk%2BKRIC5eRMyBr3Wf6rWYKkOmNSw%2FvEgTLNFWXzyIuADJxQRkNKeCWb7SRtxtSBZq2MP%2FsSthuRa4nRv8%2BwScWIe12jA65jeK5E53xVYUUM6wCCJmkoSo4JIcU7NaOTVdB6EcGD25yFRo1Q4IgdpH%2B43efTbUDK1k5My%2FzzPsH6QwBqu8svbf%2FZNpNfZAsGQRGi%2Ftyiknafq3I1lbvtgERwoudQ4AZH3mNw69niGH4LX1tUu6lSBU9x5WFvIFlUI%2BelqcRKaDb3xv%2BpJhq9Ss2GzYdi7%2BXDEXIFjDpdf5HGeGtHf06pINbLTyJ4%2BHxw19AQiyGeWmA2PG5y7oDnU7EDDKqJoYCOeJw9WzaGxc65XQBFnJUnjPtP63RfmRT9qOUwry62THa3ujwhUb8odwgzJWLSWr30w084qQm03CVsIQ7gNodKOMYq7HbaNPrsKCeqTxtsVgAiqwJOmhC3ksaUGFREBjrVdLW3Eql2gijJG8qKS1NRAM96JMZVq75IWPIwjjfuQGup9bAKuArg1hM%2FUkyn9DxZ%2BIP8EuS9CKY4kWQ3oHg9ak5q7nyu%2FUARJAyyBUvfvjKWltRC9%2F4o51BjT%2FRC2jvrSJaEy%2FeDs0oZEC8AkWYe1QwhkXXYiXHS4UfDXIQpSf%2FSAS%2FuYo5Td6c8H5iI%2Fewc2sbkFx3DQABflICeO4OM9xt8bweMlA%2FJucubRabZKYWsnc5t9zIDsPyWWC1pMan48BtKhtadQFDcJ09JnWgaNxepLzxJD%2FnAf4KK9Bgi1DwytZrHnsFayPt33HpsVJU0FzWMUwk%2B6rJHyhDHpTr6hZudHLiMlBAK0B2x4yvhNKlzeqJpUpuKi0Pl%2FN1uvLjScvqKOi72tnJZsJsqx0pKD%2BVYMccp%2BVMvKCXAgN5DD59MMUs2DUdEp8M9sctjEQ5OeXZehurkYR%2BcQgy8dXplmJNi8WPcdmRKcWJZjEG4nhQBfyPw29bNyrRhCNf2W7%2FOeU7%2Fr8Lr3exKh6EAbmay2q8XWfUmeAtd6frDHJq5OVCZBVqBDR2pi0MicqpgIbjvxl9sMa3XFBYrNuRsjN5BFiL3SXcxqnXmuBAdtGFmx5z0hKihhQB1axp%2FDd5%2BuZAKVDgC%2Bmwm70JcukzXMAzHUV8cQc8P04yWkc3BRIz8dGcDW7SS8gdxfNRzWdUrVA%2BPUAONnqitOaR0xfiqaoJjfA%2FLvgYUsZNhvn14IywWatnmg%2B%2Br62Zr86HqGLbI8MogK4%2BXaEZVRqPyqad0E8oZwTuMVPhv5PA8bRcNGOm7gRn3k9I5malRMN2G6btbqiauw%2FgV9s5eyXs%2BuO6tSymklBDq3A0mIioJ%2F7wHq7%2Bc81EbbokOxI%2BjAHHoyEbJC7Ob0F3iaaSnIk%2BRkyvraeBI87dw%2BGZxbX3bySJa1Nh5JThssM%2FFw3Jb1CtBOFZKSR3Y9ZB4qphd60QjLvCfWx9Z9Pg%2BU9TEO8EWm5noxdB%2FtBNxt%2BF1m%2FjIh9bYdeiEJ%2Bqv%2BuSYL2xE7iHmpM6UKNi42j2Ed0MFgGnJ%2Fn3wrh%2BNXbeXmMQa6i3PNQw%2F1XcClChQIWZSOJairHJIULSxchU%2BQfY8INwUeC2JYRvlg3FxwqKQVbac9sAdKogOnjn92fz6EnqyunQcV%2FEkU%2FIHfNrBaKBavFMDxrWl7ke%2F7CZWO56p7roZT0RRLfPYKdadMixLlb54N7MuORZCx2%2F57DjGVHozpm8CzqvivR6NUsFbDrEgvSw4Hv%2BEj%2BsKHLusbQewZOkHkpUaV%2Fw0P9YVdSyTD%2BSgLwLKsWirYs1v9v5prO9DXdE6X87KStjQ2YMYUi0cvsm8RRZBakLVt8Zmb3CB1Px9OjJxGKPPAQGqk0adN98D1y2J2OVfHau46vSn7vLraCx33rLpFzKTxaC1yh9Tcl2gx9Chr6Siz9GVJxxW%2BV4u7LZClpkk5a%2FdNja6n%2BeUpvnTkZWI2In6ujTojT9YzwGPJ7iiwoqU5MevxFgIfUK7PDFyzn%2B8bpKSPz%2FA7yU3wkzKKdHR0H33Xt4%2FM8sbyMtNNlMIo5Sz46%2B5ytaw0SjBEqvb83MuaEf2XXFU3kdmLjDTEUmlTjJdHRTNPJ2M9A8B%2BsIY7V%2B0TnKQ0ZkMmhzdmJXgZTUQXb93kNEYa5zKx7w23SxnALA%2BS6iI5AEZFb%2B8sav6NS%2FFQa8iUBZP0xMDTgw0h7P%2B3fw8wsAnS8TGZWjcBXMgZVgjfeyB2ooxpBB%2FDMP2Z3pj70Co%2Fz%2FbpDK3%2Bb9eL3d%2FvY6H8lXzugxkMjPPwwPiyy5l8wDIWFk26V5xKwsAlRYM0FtQs9bK6GVRDUi6S85lqaZwxMKjnrFLRxbwmu8pIGb4UJXTypwwzlEI1lA9mqNKiBB0Ql8ykK1Vmgj7TEadGXlwkoN45cxcj4aP3DRtoxkqAG7PMNOsTJN0%2FUU3YccTpel5fxd9fgZYi6xiWOSNM%2BoMPwOMLvgTbK8%2BfuuaK0Ebq5F1gd5MxEQeUVBeYOrcFPDqUbqK6KNc9qzYDEN1Tg19Cfabc81XYKAAC5cEFYaBSi0%2BdepDt1v8pnybSTBfNTxPaqM2qKPv1A38f10Dx1aRyHfKibndtv4mzxAyFUFG28x0yrj2jVXiJ1gXhCYM4fKHdPDbmzu1OK9ppD8G3T8%2BQD%2FJSAnn4ecG71dYZBfbE8i9Hh8iDuSIeCyO7jmErv%2Bxk2yG42xdgEoG6hOogSEHYjnmAS0wvaqT7H9iIIVVvS76ybj4CE3mzQp0MJED4xr%2FtZxqrnWGei1NzAfkFQjyxGE662k9WBj%2Bu67ZN5xygRfuu0bMrmTYzgWCt0Z%2FRRG5agkLVsv%2BDHcZ8VzsN%2BKviicUFFnRbkKj4YOoW97vVAD4fFPMIdm8IfI9xEHilyOVXoBciFJk5RkIcBCsZPdA048xWOstA28ITkfl6%2FOvk%2F0KLZyJSe9MsYfgON8eCsyk%2Bin8QCQpWop0U0bkE8%2BVQ4JAg7tFWjajmgFn4UXqGpMDiW909K3zV93uUgS3bf0v6Ygf4S8FSD9Jv%2F%2Fb3WgPaVokEOY%2FIEzvMcntcO6C688VlhrLkdkz3yBlm7QqPLSZFB%2FIT%2F4W%2B3YHOSDNJPfkPSyJk5PgAAFKIu11rjxDnrQGIRpyT6x%2B1JwpMKX8shmEIjs%2FwDizdq1qfEGVejzzeVLnCtOxNqHkjRhWrU9fvsXCK375YeauvDDASHR%2BiD7v7EE82fXKYRl5sFyKA4D%2FMmFnXmrhbZMSN7TXaUWYIFSAS1hEBiQyJekzxG7Do%2FR4eg7gSu5%2FXYUq28HPHF7ZNTKOFyPj4iIOJ7rH2RHxWwvGsRYLdQ3BvUQfxbhnMrMK6aOcfT5tOlWMwgmt4JauqDa571HmgR5Nc5Mjzw6Pwvuz%2B1lOh%2FpkUqU9Ghie3aptst8lL8gF72WL664mkdEn5130MS8qSS5y2YelgpzQKpRk5CwlIRRG0W9dxrgpzQDG9SFE6z5nxsmtuBnE6%2BLtIKveqPHUd2oiKS2tZsksQmKGDqPeutB7rgIFSoPFdBWFz0DeVqRgmTo0wGLMWGU8XEew4b9Yoe3oresc7rngVFPpF3K30Do0pfi6g%2FagHTajd4nUgOfp5lNMrUA6jTm215G3Q5yrVWMNDO6MnRjPM2ErVrlcgmutDk%2B8%2BJ8STWTaaQ93sJdAmewf6fMK39SlL0BA%2B%2B0lfC%2BywiI2o72E3s%2F1GTYdfq0Me2rwQCEE66FOuygcB7dRnCEUW11Ck27%2BmMFbZCV%2FVD3S16AacEBdrU%2Fkg44zAemqJTMvG840Irath%2BL7ZpoRnxI2uuR2lgXe27o4%2BZvh%2BjJ%2Ftpx7ncpKwqLApacLancxU%2Bu7M%2F%2F0uKW0PUy3ioOmr%2Fz0TiCnA24lH9jt%2BdP0nh9eVDf8eTqycco960UAOW4G81fWuaeMDlxaSbh1WZ%2Fsa2cDl%2FxiQPTBJQ%2FO%2FLYZDGfs%2F0wk8pB45EdTeBO41SX3u2BROQo%2FH%2FTFahQRcMelQOnYsBcsObKU%2F7tit2kNhjYCr8Snw6qR%2FjOmKOnRCD4QTkuFzLW7uiSgU1KkSPjpBTv7eD6Pm9aazgEZjZ6siTvHvkUFtPYWEGBHIrJhNFNj%2BHeoIQ%2BJNARScdJprytx24jPT%2BIdRiR3F67BzsQ0ycsl%2B0E%2BFxDx1qUMgQ2uLZSv9VOzGAj49Y%2BSViPbZMWPAbJlPbVMrBreUqL2oxc68taU5bJp8CjMa3F12dwlzdF6TbUUPAUd5B0HiGo0XVwkky206vaCzahy%2FWmyI4I7t1c0mF4xUR9LPZhMUrMS4T8udbdDF7uMObiJtmby8Ww1Ac4RyRyMoImU42FLpXv%2FFhjn7tOHJ2PAROJTzGCFjagHmf1rXHdNAyrxTZMSAktT7Ca3VT2tSbEWLxmBmGLYtjvn3amPMcQBfDLH6fXKzvJWINtbmTryU6qxblT2W4hdALwINXQX%2FLovZk%2F8LFyYNBAGJ%2B48MZZHxWD8dXk2jKS1A0TQL%2BXgRXinVTxDeF72%2F0MoK3z9RvXcuaKprE0AjzktuoRxglopF%2Bb5KOREtrpkcY9Jr6URVMQngO%2Bnrxdo9xA1aQ0gXToo8UR8RuYtHph6OWWh8cZnHQ3%2Bau5eYKgKnsONF9kuKWYTnNTUJ%2Fv9KIOhzeHESvtXG6nDy%2BICNFumeqBLm15NZ3yEmT9m9DzlUOJIoKs8tmyknz5%2B%2BnyTnpgV8gcyUVaN%2BebzcS4jlR%2FrMCIg79rnn57Trva8rwMqdITihNn7vmB5Gn7ccq5NRNQSVlc0T0hOv5glDMeszhehpJTpvVkKeqNDgikmbfsP9laA4%2BknRFB9l6%2FxlrqIcrHQqhKbw%2F3S8Bjyuh7gizDuoX3XnHa%2F2dusWGNmBdjY%2F7U15sbhQI8QauD8n9IRuyQqd9bTzThvBflvPycDiQaP9QgMoolBO8xySnJdUb0DSWZjMlAYnoseqVS1B1CsRGkvrImrRqrH95Wyi8MWVR%2BD9TdtvyR87k9Eb3uV4W0jGjhkmGnuvp2A3gUWJ7UI2R3mXPDIvlh5%2FZuCCQFEjAGYvxBmDPW6MAZfAW01Zx6zyjRp6awy94zz9SjKyUa2rH129qQtE2J0CneOKl%2BbyoeEG54gYXT0G6aZwrxH7eD0I%2BLYgh78L1Ax1V6gF%2BZ2t5iogQ2qWmiXJek6bhkDEFOxZQU7uj01dmzjVtOPEk2wii5Xewhfuld4RyvgIp4ZG4Ms9sn0%2BcW1zWtdzfQDJifGA55yvnGh%2Bz6acV3sYFwq%2B9dIIaYQewQNvn7QfV4jgIhR8YFmwCJT0EFPIDOJJI%2BXCeMqdTSp0rGS1qAbx8E%2FQdONCMaDwFARsre2t08hDqmDUDLpvUh7UYukZCnKcsU5Q8YRBH%2F88nk4MTePIkc9mExn2BTFBYBWCAhDorE4Ydapv4pqqnD2D2sk%2FNI6E00OISy1dr4UPvukZ003R4oENNkMb1%2F0NZ9EgIcyKQB1Be2OpiZTe8Q%2FOGpZB7Ud3iywStwYw%2BkgZIxR4eLikGAh5VEjO8ZwyfeLf5rJc2Fh%2BEbwFe4p5FvpJ2fKgxVea%2FVaHSlqMpbZsKmjbEavv1AnVCj%2BdDbNFYTOhXhyv7eiYLTcKWTuZIMlmOPYMx%2BVqvZXfNzs0pxvMmFmgz%2FoOzNPQI0Vjl8DwKUWd2RRr6BseTghwbZuXRURFF1GOj3Q3m4x2DFTLDbGrNM5vcKCXbUa20fQUf44p6VvQCYQPFdOSn4Zs5Z8cC5ZIE7u6GAUZXlJh3ieyHBZrLsB7gi2JbQJY2LvFEboaf2AZ8U6vcibuR8l3H3WXYMLF0cvEYmFBszZ3fPPVsUfSpwLN0H%2FEcDLZ2jPx87gBuSiFp2t%2FzsnyslhlcIAsWGGvI1fy0hFw%2F8BR3A2sI0Ip2fig4GZF5Hb%2BpUbRuYY%2Fdeb4kHqMayB1pitNRBTM0XVQSL%2FfNk488so2%2Bfe3bcr8ndfKXgoxfSu1Ct03LHUf9SuuanlL79G3uLPXQpbrCC4JDfDnHTwhj9f2Jhdu50vFGXz4iqIDbKkMDndFg9PwCszjZpfUTcg1OFMVx%2BMYdLrsNPdgNEEfORlI%2B6R9G1Zf0a6BGtfquYQ03iIuE2odMsMCuMS0dwkhGFSmbhyeVKyoxh6APPUvRBjW95OtmKvZ9rcoOHiwCMQHsAwP7%2FZUYOQIqhJ%2BSBlKkuE2IKzwUbLY8Atm3FW7y25brWJaraYLNifCvS6sIIfwFyYrQcyynRWz4DciibVECwki8rCoW3p82857eJJraVkAIep6frPTFI0CdTcDHyEheCUsj%2FslRunWd9yKX7ibueBxY1%2FddE0jWi%2FCHWiqR9isAowU77dU%2F9mydpqFm3mPThPVdYzcFLALX0i210Of5xWCjOLTkwNLkumuRCjdILdsVnHQePtplB8AItLuwGHmUyqdx3ed8PIeRq%2Bp3iMDY7oZy8NiILUnNXVygW2vKOHZGPivxf%2FPOMU%2FHhIW%2FK4ep8XDxF9AhwKEprpUeOAz9tZMAc6YfLa5XzMivG5Xl3O6su4WQsru0USOk2A7TOKe%2F3jjIX38bY59isMnadGHLMx5lzZI5IK6ovxv68a0mfgMqVCvnxJT7pvzznLBMF0knMQfLZgve7wSECUdlpK1%2BZMdBQ06ewwhzCymZIl%2FUAHShH4PCcQ57vgdSMLQJM7GseO0HNUfbawS6Jm9thMW6QJWc17F2mxbdp7XSrXCwADMk3P6AmcnirFLf4oo6VylJPNWE49nT%2F7U2iLKKsgrycJfbgErDXF1tr38vQlTSfUb9c37ntvFu2O%2BqbS2Ed%2FPBK4jB%2F%2BJXMmK3glZzHGEy%2FMS%2BxFc3%2F0qFT49%2BBNIIbNEuRLXoyMjeTlbRomLVCyDuCWucWqzLA4ni960UXBI4E3bvvHME%2BEFPdP5%2Bhz2oiAYV2SPckeWi3D%2BB1LmE1UF1ihVUE2qIcQjZRE0wDxxCi09qbiM8oXp7G5Ud5kchjcelGAD2L2mDB8czh4WUy4vaWiuj1dliTo1k00dMjMey%2Fh22U9z3W7gSE8RIvLZ%2BcyyZDwSwpOM7ch0%2Fwwjh9rCLkKSyn47AaCIaCrjurqYJMe2tMcTeb4NTwrr0yv9jXkMaLexql1wFmXlB4bYG6%2BEZXMBn6lSOjTPO0ZjHmsR%2Fszu7Foelt2xcVCqJp2uFEBzYAYdNXok7A0F1JXXPSaSv4N3i8lcCtD8PVs%2FLoCfYJbGuspthP6kbw2ef5cLkbsqwxvInrmCeDKfHnR9OXW4Xc2UUTrhlNcrY2ByRZ0xJN%2BSmS5pP5ZyN%2FcF%2FnT2BSoUJHLmKPqtNZU625XvXukCLG9zY45mnU5yHeFrz6bVnUvg1HV3AHvfDPwgh%2BwEwN4JcTfHUgMMXKpU0L7T8zMl0I5Bxh9us1cjiVAETYGO0f2oOmYErws6WHsf59EjQcm3twYxjSnc%2BScDEOpDskz%2BCD%2FG%2B9mtNMqe8K2uC8uKss5iQeBHqC4dcX%2BwJLKjMRFAAl0sGuXEpG9jfb8SXgkouLuPUUM6n8at96GovF2G%2FrsrnaG8RcAQUcmOTzNZjkS45t%2F9k%2FcjZ6I5gwSpgLQfU94%2F6g%2Bc%2FlKmyVNF7yLd%2BIB4%2BLBFapu3lL6s88BnI3PFb57n66aZUsE5Ur2y%2FU26bivba27nloxa18ctyp373ySorkoGwxUmIMNwmfBo88wW%2FV8SWw2CS5cjokZ4xAvcRY43Bn89x1ezY%2Byk5FoQfDFG%2BVM1xc6Ix7vll4jOPsW2Kumt6Y30MkyuH3msqUolwphhfNEL9Vm1m75D%2Fkcg%2Fi%2F79po%2BXkRKpoFITST0urOuQ4GHyFN2j9GKHd5KDdYcosIrv5QzrvCcYck6W7TrskucqIgSVUJeXRe4UjBzUCtT%2BUI2s%2BXZIN2bh5JZkRXGpy55Enqz1mh7y0LyZuMvdmg7VZkgLGv%2BSVR8lLRsZ%2BYfU4iZ%2BsReLCuLC1t7oLdqiN%2B%2Br0qoJA0vNd5QbRfeEa3EYtNFyn4Xiq5a2%2FB%2F14V4pZQGsyDFQskhEbEQlPhXZNBkd7GHMGHQwEJyvVpGJyOiN%2FQGiGUsDL%2FatV27cUtXPItJ08Hl0KLJXg%2FfJJckQyyOukiB6VPIcE%2FqqJQp9NNV4l7Jf%2BH9wgShiAyYXKCGj4yR8uv9dpHAjqKxFQVEKLmVGJodqeM9Axbv8mheDQemCPgOe2QSx11Jnai%2FtWWR5U5vE0PnKBpsjbVzjdlRgNTLYdmite7WLfhAZFx7KD0%2F9PSuoqXJ3tA3kHMAEzuERcplQGw0c4cO6wKdMIGl5mz%2BCB47DEOJSX1lyvBQ7C07IxeN7YFcYpLQrX7fw9cMqXtNLBfavrFXyc8VMXcVi5aW%2Bzf3tHxk6jZmcl05iAVrjW9ZGC0y14Z88k%2BNfE7lwXJvxa2M2wbkEMMA1UGO7IAGyZPfJT0StwpayvFqb63%2F0iJ8ZVrr4K2wp33CyQKfL60gLQ3YvbfnFp2ZtfP%2BivRV7EK8slFPqVoRJEt47QQQc88vZF7RIo9xIsPfWv4sm2wTZXvvsdDH9cIsNrQdP14xmaETcywbtFuyWEzjUQ4ydXg9hKsHBjGGN0whprZDT44A4qiedC4OGWVULwKwgwdv25V7hi%2FxRnsQl90EjV9DA0Qk%2BXKJRSjgxzZW7z91CE%2FAwrxivWWXFuXmxfRSaZymwTlKDe%2FwLxOpfBxnlC0HB%2FMav7fZ6wF5eTNrVt6H6uBhceCW1DUMATxFaELmaHbrTkBGZQpNsv8C39LmiqGdi9mVUyrqT4eNO8hTb8kiIwSAdKp%2Flz3kAM%2FMq4J6Q9KG2%2BXfvIjbGiq8l0kHhmbhWRLHsf%2Fi4vF1iRugzfNhe0YcQ9RsGG7KvTzRo1JnyFrzGJKtU8upsOWEsL8wNkr%2FNKGD6GsS9ubbMtDP91BtB3y0C85D%2FcU59KA2VTN4DxiwCrVVDm1EvxUKgeFEbVKsl9OGGktdbAN4svo313iK67C%2BewaOcMCbBVZr%2BIhP6YlDjklBXc50VxVqR8pgKFGhUTgbCCBkfPAtGCH5YCY%2Bx57QdKSWcQNzprF%2B8JLNjxJRC%2BKATlnfoMEDhA8kHPpxTHZKWs%2Foo3vjXPkxVfHV1glhzwtCoCBls5XdGoS9lWvCy8jAoiFhH5t0dF%2FOHTLh8a64NY%2Futukn2vdLz2XBlr29h%2Bb50u8tAyTI1iUtOgxmsruAhMpM77f%2BMvnu23BA6ZmkYTO9NCjGLIm5dhZDapMpArK6pel6Seiwc%2F0rbRjyy4KPgXEIWd7IV%2BPD5SVt8YbzpG0oXkB7qWwXVNysxY9OWSXomyKu7HZ7h9Ml2cmGwwwQvzP1ueXtilFz1jVMat0ikrTWPgI9HWtsVXBrsBWUtnMA3cFrjJgpKBJwHxFxjUzXf%2FdeZQ9b1uytqqlg%3D%3D&__VIEWSTATEGENERATOR=55FE2EBC&__VIEWSTATEENCRYPTED=&ctl00%24ContentPlaceHolder1%24txtFirstName=&ctl00%24ContentPlaceHolder1%24txtLastName=Michael&ctl00%24ContentPlaceHolder1%24ddlStatus=Active&ctl00%24ContentPlaceHolder1%24ddlSearchMemberType=&ctl00%24ContentPlaceHolder1%24ddlCurrentStatus=&ctl00%24ContentPlaceHolder1%24btnAddThisNewMember=Add+This+Person&ctl00%24ContentPlaceHolder1%24textFirstName=Kate&ctl00%24ContentPlaceHolder1%24textLastName=Michael&ctl00%24ContentPlaceHolder1%24textNickName=&ctl00%24ContentPlaceHolder1%24txtSpouse=&ctl00%24ContentPlaceHolder1%24ddlGender=Female&ctl00%24ContentPlaceHolder1%24txtMemberID=9999999&ctl00%24ContentPlaceHolder1%24ddlMemberType=Non-Member&ctl00%24ContentPlaceHolder1%24ddlCurrentStanding=Active&ctl00%24ContentPlaceHolder1%24ddlAdminLevel=4&ctl00%24ContentPlaceHolder1%24txtAddress1=4040+Whispering+Pines+Trail+NW&ctl00%24ContentPlaceHolder1%24txtAddress2=&ctl00%24ContentPlaceHolder1%24txtPreferredCity=Conyers&ctl00%24ContentPlaceHolder1%24ddlPreferredState=GA&ctl00%24ContentPlaceHolder1%24txtZipCode=30012&ctl00%24ContentPlaceHolder1%24ddlCountry=USA&ctl00%24ContentPlaceHolder1%24txtBirthDate=&ctl00%24ContentPlaceHolder1%24txtJoinDate=04%2F02%2F2021&ctl00%24ContentPlaceHolder1%24txtExpirationDate=3%2F31%2F2022&ctl00%24ContentPlaceHolder1%24txtOtherInfo=&ctl00%24ContentPlaceHolder1%24txtHomePhone=&ctl00%24ContentPlaceHolder1%24txtCellPhone=404-735-5660&ctl00%24ContentPlaceHolder1%24txtEmail=kate%40kstreetkate.net&ctl00%24ContentPlaceHolder1%24txtRatings=&ctl00%24ContentPlaceHolder1%24txtAircraftOwned=&ctl00%24ContentPlaceHolder1%24txtAircraftProject=&ctl00%24ContentPlaceHolder1%24txtAircraftBuilt=&ctl00%24ContentPlaceHolder1%24ddlRowCount=50
*/
    }

    private String buildUpdateUserRequestBodyString(final Member member) {
        final StringBuilder sb = new StringBuilder();
        addFormContent(sb, RosterConstants.EVENT_TARGET, EMPTY_STRING);
        addFormContent(sb, RosterConstants.EVENT_ARGUMENT, EMPTY_STRING);
        addFormContent(sb, RosterConstants.LAST_FOCUS, EMPTY_STRING);
        addFormContent(sb, RosterConstants.VIEW_STATE, headers
                .get(RosterConstants.VIEW_STATE)
                .replaceAll("/", "%2F")
                .replaceAll("=", "%3D")
                .replaceAll("\\+", "%2B"));
        addFormContent(sb, RosterConstants.VIEW_STATE_GENERATOR, headers.get(RosterConstants.VIEW_STATE_GENERATOR));
        addFormContent(sb, RosterConstants.VIEW_STATE_ENCRYPTED, EMPTY_STRING);
        addFormContent(sb, RosterConstants.FIRST_NAME, EMPTY_STRING);
        addFormContent(sb, RosterConstants.LAST_NAME, member.getLastName());
        addFormContent(sb, RosterConstants.STATUS, member.getStatus());
        addFormContent(sb, RosterConstants.SEARCH_MEMBER_TYPE, EMPTY_STRING);
        addFormContent(sb, RosterConstants.CURRENT_STATUS, EMPTY_STRING);
        addFormContent(sb, RosterConstants.UPDATE_THIS_MEMBER_BUTTON, "Update");
        addFormContent(sb, RosterConstants.TEXT_FIRST_NAME, member.getFirstName());
        addFormContent(sb, RosterConstants.TEXT_LAST_NAME, member.getLastName());
        addFormContent(sb, RosterConstants.TEXT_NICK_NAME, member.getNickname());
        addFormContent(sb, RosterConstants.SPOUSE, member.getSpouse());
        addFormContent(sb, RosterConstants.GENDER, Gender.getDisplayString(member.getGender()));
        addFormContent(sb, RosterConstants.MEMBER_ID, member.getEaaNumber());
        addFormContent(sb, RosterConstants.MEMBER_TYPE, MemberType.toDisplayString(member.getMemberType()));
        addFormContent(sb, RosterConstants.CURRENT_STANDING, Status.getDisplayString(member.getStatus()));
        addFormContent(sb, RosterConstants.USER_NAME, member.getUsername());
        addFormContent(sb, RosterConstants.ADMIN_LEVEL, WebAdminAccess.getDisplayString(member.getWebAdminAccess()));
        addFormContent(sb, RosterConstants.ADDRESS_LINE_1, member.getAddressLine1());
        addFormContent(sb, RosterConstants.ADDRESS_LINE_2, member.getAddressLine2());
        addFormContent(sb, RosterConstants.CITY, member.getCity());
        addFormContent(sb, RosterConstants.STATE, State.getDisplayString(member.getState()));
        addFormContent(sb, RosterConstants.ZIP_CODE, member.getZipCode());
        addFormContent(sb, RosterConstants.COUNTRY, Country.toDisplayString(member.getCountry()));
        addFormContent(sb, RosterConstants.BIRTH_DATE, MDY_SDF.format(member.getBirthDateAsDate()));
        addFormContent(sb, RosterConstants.JOIN_DATE, MDY_SDF.format(member.getJoinedAsDate()));
        addFormContent(sb, RosterConstants.EXPIRATION_DATE, MDY_SDF.format(member.getExpiration()));
        addFormContent(sb, RosterConstants.OTHER_INFO, member.getOtherInfo());
        addFormContent(sb, RosterConstants.HOME_PHONE, member.getHomePhone());
        addFormContent(sb, RosterConstants.CELL_PHONE, member.getCellPhone());
        addFormContent(sb, RosterConstants.EMAIL, member.getEmail());
        addFormContent(sb, RosterConstants.RATINGS, member.getRatings());
        addFormContent(sb, RosterConstants.AIRCRAFT_OWNED, member.getAircraftOwned());
        addFormContent(sb, RosterConstants.AIRCRAFT_PROJECT, member.getAircraftProject());
        addFormContent(sb, RosterConstants.AIRCRAFT_BUILT, member.getAircraftBuilt());
        addFormContent(sb, RosterConstants.IMC, member.isImcClub() ? "on" : "off");
        addFormContent(sb, RosterConstants.VMC, member.isVmcClub() ? "on" : "off");
        addFormContent(sb, RosterConstants.YOUNG_EAGLE_PILOT, member.isYePilot() ? "on" : "off");
        addFormContent(sb, RosterConstants.EAGLE_PILOT, member.isEaglePilot() ? "on" : "off");
        sb
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.PHOTO)
                .append("\"; filename=\"\"\n")
                .append("Content-Type: application/octet-stream\n\n");
        addFormContent(sb, RosterConstants.PHOTO_FILE_NAME, EMPTY_STRING);
        addFormContent(sb, RosterConstants.PHOTO_FILE_TYPE, EMPTY_STRING);
        addFormContent(sb, RosterConstants.ROW_COUNT, "50");
        sb
                .append(RosterConstants.FORM_BOUNDARY)
                .append("--");
        return sb.toString();
    }

    /**
     * Adds a form content section to the provided StringBuilder object.
     */
    private void addFormContent(final StringBuilder sb, final String key, final String value) {
        sb
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(key)
                .append(RosterConstants.FORM_DATA_SEPARATOR_DOUBLE_NL)
                .append(value);
    }

    /**
     * Parses select values from Excel spreadsheet.
     *
     * @return list of parsed values
     */
    private List<Member> parseRecords() {
        final List<Member> records = new ArrayList<>();
        final List<String> slackUsers = new ArrayList<>();
        try {
            slackUsers.addAll(slackService.allSlackUsers());
        } catch (ResourceNotFoundException e) {
            // Do nothing
        }
        final Document doc = Jsoup.parse(fetchData());
        final Elements tableRecords = doc.getElementsByTag("tr");
        int rowCount = 0;
        for (Element tr : tableRecords) {
            if (rowCount > 0) {
                try {
                    final Elements columns = tr.getElementsByTag("td");
                    int columnCount = 0;
                    final Member member = new Member();
                    for (Element column : columns) {
                        switch (columnCount) {
                            case 0:
                                member.setRosterId(Long.parseLong(column.text().trim()));
                                break;
                            case 1:
                                member.setMemberType(MemberType.valueOf(column.text().trim().replaceAll("-", "")));
                                break;
                            case 2:
                                member.setNickname(column.text().trim());
                                break;
                            case 3:
                                member.setFirstName(column.text().trim());
                                break;
                            case 4:
                                member.setLastName(column.text().trim());
                                break;
                            case 5:
                                member.setSpouse(column.text().trim());
                                break;
                            case 6:
                                member.setGender(Gender.fromDisplayString(column.text().trim().toUpperCase()));
                                break;
                            case 7:
                                member.setEmail(column.text().trim());
                                break;
                            case 8:
                                // Ignore EmailPrivate
                                break;
                            case 9:
                                member.setUsername(column.text().trim());
                                break;
                            case 10:
                                member.setBirthDate(column.text().trim());
                                break;
                            case 11:
                                member.setAddressLine1(column.text().trim());
                                break;
                            case 12:
                                member.setAddressLine2(column.text().trim());
                                break;
                            case 13:
                                // Ignore AddressPrivate
                                break;
                            case 14:
                                member.setHomePhone(column
                                        .text()
                                        .trim()
                                        .replaceAll(" ", "")
                                        .replaceAll("-", "")
                                        .replaceAll("\\(", "")
                                        .replaceAll("\\)", ""));
                                break;
                            case 15:
                                // Ignore HomePhonePrivate
                                break;
                            case 16:
                                member.setCellPhone(column
                                        .text()
                                        .trim()
                                        .replaceAll(" ", "")
                                        .replaceAll("-", "")
                                        .replaceAll("\\(", "")
                                        .replaceAll("\\)", ""));
                                break;
                            case 17:
                                // Ignore CellPhonePrivate
                                break;
                            case 18:
                                member.setEaaNumber(column.text().trim());
                                break;
                            case 19:
                                member.setStatus(Status.valueOf(column.text().trim().toUpperCase()));
                                break;
                            case 20:
                                member.setJoined(column.text().trim());
                                break;
                            case 21:
                                member.setExpiration(SDF.parse(column.text().trim()));
                                break;
                            case 22:
                                final OtherInfo otherInfo = new OtherInfo(column.text().trim());
                                member.setRfid(otherInfo.getRfid());
                                member.setSlack(otherInfo.getSlack());
                                member.setOtherInfo(otherInfo.getRaw());
                                member.setAdditionalInfo(otherInfo.getDescription());
                                if (otherInfo.getFamily() != null) {
                                    member.setFamily(String.join(", ", otherInfo.getFamily()));
                                }
                                if (member.getSlack() == null || "NULL".equalsIgnoreCase(member.getSlack())) {
                                    setSlack(slackUsers, member);
                                }
                                if (otherInfo.getNumOfFamily() != null) {
                                    member.setNumOfFamily(otherInfo.getNumOfFamily());
                                }
                                break;
                            case 23:
                                member.setCity(column.text().trim());
                                break;
                            case 24:
                                member.setState(State.fromDisplayString(column.text().trim()));
                                break;
                            case 25:
                                member.setCountry(Country.fromDisplayString(column.text().trim()));
                                break;
                            case 26:
                                member.setZipCode(column.text().trim());
                                break;
                            case 27:
                                member.setRatings(column.text().trim());
                                break;
                            case 28:
                                member.setAircraftOwned(column.text().trim());
                                break;
                            case 29:
                                member.setAircraftProject(column.text().trim());
                                break;
                            case 30:
                                member.setAircraftBuilt(column.text().trim());
                                break;
                            case 31:
                                member.setImcClub("yes".equalsIgnoreCase(column.text().trim()) ?
                                        Boolean.TRUE : Boolean.FALSE);
                                break;
                            case 32:
                                member.setVmcClub("yes".equalsIgnoreCase(column.text().trim()) ?
                                        Boolean.TRUE : Boolean.FALSE);
                                break;
                            case 33:
                                member.setYePilot("yes".equalsIgnoreCase(column.text().trim()) ?
                                        Boolean.TRUE : Boolean.FALSE);
                                break;
                            case 34:
                                member.setYeVolunteer("yes".equalsIgnoreCase(column.text().trim()) ?
                                        Boolean.TRUE : Boolean.FALSE);
                                break;
                            case 35:
                                member.setEaglePilot("yes".equalsIgnoreCase(column.text().trim()) ?
                                        Boolean.TRUE : Boolean.FALSE);
                                break;
                            case 36:
                                member.setEagleVolunteer("yes".equalsIgnoreCase(column.text().trim()) ?
                                        Boolean.TRUE : Boolean.FALSE);
                                break;
                            case 37:
                                // Ignore DateAdded
                                break;
                            case 38:
                                // Ignore DateUpdated
                                break;
                            case 39:
                                member.setEaaExpiration(column.text().trim());
                                break;
                            case 40:
                                member.setYouthProtection(column.text().trim());
                                break;
                            case 41:
                                member.setBackgroundCheck(column.text().trim());
                                break;
                            case 42:
                                // Ignore UpdatedBy
                                break;
                            case 43:
                                member.setWebAdminAccess(WebAdminAccess.fromDisplayString(column.text().trim()));
                                break;
                            default:
                                // Do nothing
                        }
                        columnCount++;
                    }
                    records.add(member);
                } catch (Exception e) {
                    LOGGER.error("Error", e);
                }
            }
            rowCount++;
        }
        return records;
    }

    /**
     * Assigns slack username if not already assigned and a first/last name match is found.
     *
     * @param slackUsers list of all Slack users
     * @param member Member
     */
    private void setSlack(final List<String> slackUsers, final Member member) {
        final String username = member.getFirstName() + " " + member.getLastName();
        slackUsers.forEach(str -> {
            final String split[] = str.split("\\|");
            if (!"NULL".equalsIgnoreCase(split[1]) && str.contains(username)) {
                member.setSlack(split[1]);
            }
        });
    }

    /**
     * Gets date string for provided property key.
     *
     * @param key proprty key
     * @return date string
     * @throws ResourceNotFoundException when property not found
     */
    private String getDateStr(final String key) throws ResourceNotFoundException {
        return SDF.format(Date.from(Instant
                .now()
                .plus(Integer.parseInt(propertyService
                                .get(key)
                                .getValue()),
                        ChronoUnit.DAYS)));
    }

}
