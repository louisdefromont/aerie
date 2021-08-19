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

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eaa690.aerie.constant.CommonConstants;
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
    private String eaaChaptersSiteBase = "https://www.eaachapters.org";

    /**
     * Empty string constant.
     */
    private static final String EMPTY_STRING = "";

    /**
     * Ampersand.
     */
    private static final String AMPERSAND = "&";

    /**
     * Equals.
     */
    private static final String EQUALS = "=";

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
    //private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * View State.
     */
    private Element viewState = null;

    /**
     * View State Generator.
     */
    private Element viewStateGenerator = null;

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

    /**
     * Sends membership renewal messages on a scheduled basis.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendMembershipRenewalMessages() {
        memberRepository
                .findAll()
                .ifPresent(members -> members
                        .stream()
                        .filter(member -> member.getMemberType() == MemberType.Regular
                                || member.getMemberType() == MemberType.Family
                                || member.getMemberType() == MemberType.Student)
                        .forEach(member -> {
                            try {
                                final String expirationDate =
                                        ZonedDateTime.ofInstant(member.getExpiration().toInstant(),
                                                ZoneId.systemDefault()).format(
                                                        DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                if (expirationDate.equals(
                                        getFutureDateStr(PropertyKeyConstants.MEMBERSHIP_RENEWAL_FIRST_MSG_DAYS_KEY))) {
                                    emailService.queueMsg(
                                            PropertyKeyConstants.SEND_GRID_FIRST_MEMBERSHIP_RENEWAL_EMAIL_TEMPLATE_ID,
                                            PropertyKeyConstants.SEND_GRID_FIRST_MEMBERSHIP_RENEWAL_EMAIL_SUBJECT_KEY,
                                            member);
                                    smsService.sendRenewMembershipMsg(member);
                                    slackService.sendRenewMembershipMsg(member);
                                }
                                if (expirationDate.equals(getFutureDateStr(
                                                PropertyKeyConstants.MEMBERSHIP_RENEWAL_SECOND_MSG_DAYS_KEY))) {
                                    emailService.queueMsg(
                                            PropertyKeyConstants.SEND_GRID_SECOND_MEMBERSHIP_RENEWAL_EMAIL_TEMPLATE_ID,
                                            PropertyKeyConstants.SEND_GRID_SECOND_MEMBERSHIP_RENEWAL_EMAIL_SUBJECT_KEY,
                                            member);
                                    smsService.sendRenewMembershipMsg(member);
                                    slackService.sendRenewMembershipMsg(member);
                                }
                                if (expirationDate.equals(
                                        getFutureDateStr(PropertyKeyConstants.MEMBERSHIP_RENEWAL_THIRD_MSG_DAYS_KEY))) {
                                    emailService.queueMsg(
                                            PropertyKeyConstants.SEND_GRID_THIRD_MEMBERSHIP_RENEWAL_EMAIL_TEMPLATE_ID,
                                            PropertyKeyConstants.SEND_GRID_THIRD_MEMBERSHIP_RENEWAL_EMAIL_SUBJECT_KEY,
                                            member);
                                    smsService.sendRenewMembershipMsg(member);
                                    slackService.sendRenewMembershipMsg(member);
                                }
                                if (expirationDate.equals(ZonedDateTime.ofInstant(Instant.now(),
                                        ZoneId.systemDefault()).format(
                                        DateTimeFormatter.ofPattern("yyyy-MM-dd")))) {
                                    LOGGER.info("addOrUpdateNonMember");
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
        throw new ResourceNotFoundException("No member found matching RFID=" + rfid);
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
        throw new ResourceNotFoundException("No member found matching ID=" + id);
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
     * @return saved member
     * @throws ResourceExistsException when member is not found
     */
    public Member saveNewMember(final Member member) throws ResourceExistsException {
        LOGGER.info("Saving new member: " + member);
        try {
            getHttpHeaders();
            doLogin();
            getSearchMembersPage();
            if (!existsUser(member.getFirstName(), member.getLastName())) {
                LOGGER.info(buildNewUserRequestBodyString(member));
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
     * @return saved member
     */
    public Member saveRenewingMember(final Member member) {
        LOGGER.info("Saving renewing member: " + member);
        try {
            getHttpHeaders();
            doLogin();
            getSearchMembersPage();
            if (existsUser(member.getFirstName(), member.getLastName())) {
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
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .count());
        membershipReport.setFamilyMembershipCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .count());
        membershipReport.setFamilyMemberCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .map(Member::getNumOfFamily)
                        .reduce(0L, Long::sum));
        membershipReport.setStudentMemberCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Student == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
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
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> sevenDays.after(m.getExpiration()))
                        .count());
        membershipReport.setFamilyMembershipWillExpire7DaysCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> sevenDays.after(m.getExpiration()))
                        .count());
        membershipReport.setFamilyMemberWillExpire7DaysCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> sevenDays.after(m.getExpiration()))
                        .map(Member::getNumOfFamily)
                        .reduce(0L, Long::sum));
        membershipReport.setStudentMemberWillExpire7DaysCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Student == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
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
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> thirtyDays.after(m.getExpiration()))
                        .filter(m -> sevenDays.before(m.getExpiration()))
                        .count());
        membershipReport.setFamilyMembershipWillExpire30DaysCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> thirtyDays.after(m.getExpiration()))
                        .filter(m -> sevenDays.before(m.getExpiration()))
                        .count());
        membershipReport.setFamilyMemberWillExpire30DaysCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.before(m.getExpiration()))
                        .filter(m -> thirtyDays.after(m.getExpiration()))
                        .filter(m -> sevenDays.before(m.getExpiration()))
                        .map(Member::getNumOfFamily)
                        .reduce(0L, Long::sum));
        membershipReport.setStudentMemberWillExpire30DaysCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Student == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
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
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.after(m.getExpiration()))
                        .count());
        membershipReport.setFamilyMembershipExpiredCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.after(m.getExpiration()))
                        .count());
        membershipReport.setFamilyMemberExpiredCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Family == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.after(m.getExpiration()))
                        .map(Member::getNumOfFamily)
                        .reduce(0L, Long::sum));
        membershipReport.setStudentMemberExpiredCount(
                allMembers
                        .stream()
                        .filter(m -> MemberType.Student == m.getMemberType())
                        .filter(m -> Status.ACTIVE == m.getStatus())
                        .filter(m -> today.after(m.getExpiration()))
                        .count());
    }

    /**
     * Performs login to EAA's roster management system.
     */
    private void doLogin() {
        final String uriStr = eaaChaptersSiteBase + "/main.aspx";
        final String requestBodyStr = buildLoginRequestBodyString();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            builder.setHeader(entry.getKey(), entry.getValue());
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
        final String uriStr = eaaChaptersSiteBase + "/searchmembers.aspx";
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
            viewState = doc.getElementById(RosterConstants.VIEW_STATE);
            viewStateGenerator = doc.getElementById(RosterConstants.VIEW_STATE_GENERATOR);
            headers.put(RosterConstants.VIEW_STATE, getViewStateValue());
        } catch (IOException | InterruptedException e) {
            LOGGER.error("[Search Page] Error", e);
        }
    }

    /**
     * Checks if a user exists in EAA's roster management system.
     *
     * @param firstName First name
     * @param lastName Last name
     * @return if user exists
     */
    private boolean existsUser(final String firstName, final String lastName) {
        final String uriStr = eaaChaptersSiteBase + "/searchmembers.aspx";
        final String requestBodyStr = buildExistsUserRequestBodyString(firstName, lastName);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
        headers.remove(RosterConstants.VIEW_STATE);
        headers.put(RosterConstants.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image"
                + "/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        for (final String key : headers.keySet()) {
            builder.setHeader(key, headers.get(key));
        }
        final HttpRequest request = builder.build();

        StringBuilder sb = new StringBuilder();
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            sb.append(response.body());
        } catch (InterruptedException | IOException e) {
            LOGGER.error("[FETCH] Error", e);
        }
        return sb.toString().contains("lnkViewUpdateMember");
    }

    /**
     * Fetch's data from EAA's roster management system.
     *
     * @return fetched data
     */
    private String fetchData() {
        final String uriStr = eaaChaptersSiteBase + "/searchmembers.aspx";
        final String requestBodyStr = buildFetchDataRequestBodyString();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
        headers.remove(RosterConstants.VIEW_STATE);
        headers.put(RosterConstants.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image"
                + "/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        for (final String key : headers.keySet()) {
            builder.setHeader(key, headers.get(key));
        }
        final HttpRequest request = builder.build();

        StringBuilder sb = new StringBuilder();
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            sb.append(response.body());
        } catch (IOException | InterruptedException e) {
            LOGGER.error("[FETCH] Error", e);
        }
        return sb.toString();
    }

    private void getHttpHeaders() throws ResourceNotFoundException {
        HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(eaaChaptersSiteBase + "/main.aspx")).GET().build();
        try {
            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final HttpHeaders responseHeaders = response.headers();
            final String cookieStr = responseHeaders.firstValue("set-cookie").orElse("");
            headers.put("cookie", cookieStr.substring(0, cookieStr.indexOf(";")));
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error", e);
        }
        headers.put(RosterConstants.EVENT_TARGET, "");
        headers.put(RosterConstants.EVENT_ARGUMENT, "");
        headers.put(RosterConstants.VIEW_STATE, getViewStateValue());
        if (viewStateGenerator != null) {
            headers.put(RosterConstants.VIEW_STATE_GENERATOR, viewStateGenerator.attr("value"));
        } else {
            headers.put(RosterConstants.VIEW_STATE_GENERATOR, "202EA31B");
        }
        headers.put(RosterConstants.EVENT_VALIDATION, "/wEdAAaUkhCi8bB8A8YPK1mx/fN+Ob9NwfdsH6h5T4oBt2E/NC/PSAvxybIG70"
                + "Gi7lMSo2Ha9mxIS56towErq28lcj7mn+o6oHBHkC8q81Z+42F7hK13DHQbwWPwDXbrtkgbgsBJaWfipkuZE5/MRRQAXrNwOiJp"
                + "3YGlq4qKyVLK8XZVxQ==");
        headers.put(RosterConstants.USERNAME, propertyService.get(PropertyKeyConstants.ROSTER_USER_KEY).getValue());
        headers.put(RosterConstants.PASSWORD, propertyService.get(PropertyKeyConstants.ROSTER_PASS_KEY).getValue());
        headers.put(RosterConstants.BUTTON, "Submit");
        headers.put(RosterConstants.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 "
                + "(KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36");
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
        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            final String key = entry.getKey();
            if (data.contains(key)) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                if (RosterConstants.USERNAME.equals(key)
                        || RosterConstants.PASSWORD.equals(key)
                        || RosterConstants.BUTTON.equals(key)) {
                    sb.append(key.replaceAll("\\$", "%24"));
                } else {
                    sb.append(key);
                }
                sb.append("=");
                if (RosterConstants.VIEW_STATE.equals(key) || RosterConstants.EVENT_VALIDATION.equals(key)) {
                    sb.append(entry.getValue()
                            .replaceAll("/", "%2F")
                            .replaceAll("=", "%3D")
                            .replaceAll("\\+", "%2B"));
                } else {
                    sb.append(entry.getValue());
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
        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            final String key = entry.getKey();
            if (data.contains(key)) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                if (RosterConstants.FIRST_NAME.equals(key)
                        || RosterConstants.LAST_NAME.equals(key)
                        || RosterConstants.EXPORT_BUTTON.equals(key)
                        || RosterConstants.STATUS.equals(key)
                        || RosterConstants.SEARCH_MEMBER_TYPE.equals(key)
                        || RosterConstants.CURRENT_STATUS.equals(key)
                        || RosterConstants.ROW_COUNT.equals(key)) {
                    sb.append(key.replaceAll("\\$", "%24"));
                } else {
                    sb.append(key);
                }
                sb.append("=");
                if (RosterConstants.VIEW_STATE.equals(key) || RosterConstants.EVENT_VALIDATION.equals(key)) {
                    sb.append(entry.getValue()
                            .replaceAll("/", "%2F")
                            .replaceAll("=", "%3D")
                            .replaceAll("\\+", "%2B"));
                } else {
                    sb.append(entry.getValue());
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
        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            final String key = entry.getKey();
            if (data.contains(key)) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                if (RosterConstants.FIRST_NAME.equals(key)
                        || RosterConstants.LAST_NAME.equals(key)
                        || RosterConstants.EXPORT_BUTTON.equals(key)
                        || RosterConstants.STATUS.equals(key)
                        || RosterConstants.SEARCH_MEMBER_TYPE.equals(key)
                        || RosterConstants.CURRENT_STATUS.equals(key)
                        || RosterConstants.ROW_COUNT.equals(key)) {
                    sb.append(key.replaceAll("\\$", "%24"));
                } else {
                    sb.append(key);
                }
                sb.append("=");
                if (RosterConstants.VIEW_STATE.equals(key) || RosterConstants.EVENT_VALIDATION.equals(key)) {
                    sb.append(entry.getValue()
                            .replaceAll("/", "%2F")
                            .replaceAll("=", "%3D")
                            .replaceAll("\\+", "%2B"));
                } else {
                    sb.append(entry.getValue());
                }
            }
        }
        return sb.toString();
    }

    private String buildNewUserRequestBodyString(final Member member) {
        String requestBody = RosterConstants.EVENT_TARGET + EQUALS
                + AMPERSAND + RosterConstants.EVENT_ARGUMENT + EQUALS
                + AMPERSAND + RosterConstants.LAST_FOCUS + EQUALS
                + AMPERSAND + RosterConstants.VIEW_STATE + EQUALS + getViewStateValue()
                + AMPERSAND + RosterConstants.VIEW_STATE_GENERATOR + EQUALS + getViewStateGeneratorValue()
                + AMPERSAND + RosterConstants.VIEW_STATE_ENCRYPTED + EQUALS
                + AMPERSAND + RosterConstants.FIRST_NAME + EQUALS
                + AMPERSAND + RosterConstants.LAST_NAME + EQUALS + member.getLastName()
                + AMPERSAND + RosterConstants.STATUS + EQUALS + member.getStatus()
                + AMPERSAND + RosterConstants.SEARCH_MEMBER_TYPE + EQUALS
                + AMPERSAND + RosterConstants.CURRENT_STATUS + EQUALS
                + AMPERSAND + RosterConstants.ADD_NEW_MEMBER_BUTTON + EQUALS + "Add+This+Person"
                + AMPERSAND + RosterConstants.TEXT_FIRST_NAME + EQUALS + member.getFirstName()
                + AMPERSAND + RosterConstants.TEXT_LAST_NAME + EQUALS + member.getLastName()
                + AMPERSAND + RosterConstants.TEXT_NICK_NAME + EQUALS + member.getNickname()
                + AMPERSAND + RosterConstants.SPOUSE + EQUALS + member.getSpouse()
                + AMPERSAND + RosterConstants.GENDER + EQUALS + member.getGender()
                + AMPERSAND + RosterConstants.MEMBER_ID + EQUALS + member.getEaaNumber()
                + AMPERSAND + RosterConstants.MEMBER_TYPE + EQUALS + member.getMemberType()
                + AMPERSAND + RosterConstants.CURRENT_STANDING + EQUALS + member.getStatus()
                + AMPERSAND + RosterConstants.ADMIN_LEVEL + EQUALS + member.getWebAdminAccess()
                + AMPERSAND + RosterConstants.ADDRESS_LINE_1 + EQUALS + member.getAddressLine1()
                + AMPERSAND + RosterConstants.ADDRESS_LINE_2 + EQUALS + member.getAddressLine2()
                + AMPERSAND + RosterConstants.CITY + EQUALS + member.getCity()
                + AMPERSAND + RosterConstants.STATE + EQUALS + member.getState()
                + AMPERSAND + RosterConstants.ZIP_CODE + EQUALS + member.getZipCode()
                + AMPERSAND + RosterConstants.COUNTRY + EQUALS + member.getCountry()
                + AMPERSAND + RosterConstants.BIRTH_DATE + EQUALS + member.getBirthDate()
                + AMPERSAND + RosterConstants.JOIN_DATE + EQUALS + member.getJoined()
                + AMPERSAND + RosterConstants.EXPIRATION_DATE + EQUALS + member.getExpiration()
                + AMPERSAND + RosterConstants.OTHER_INFO + EQUALS + member.getOtherInfo()
                + AMPERSAND + RosterConstants.HOME_PHONE + EQUALS + member.getHomePhone()
                + AMPERSAND + RosterConstants.CELL_PHONE + EQUALS + member.getCellPhone()
                + AMPERSAND + RosterConstants.EMAIL + EQUALS + member.getEmail()
                + AMPERSAND + RosterConstants.RATINGS + EQUALS + member.getRatings()
                + AMPERSAND + RosterConstants.AIRCRAFT_OWNED + EQUALS + member.getAircraftOwned()
                + AMPERSAND + RosterConstants.AIRCRAFT_PROJECT + EQUALS + member.getAircraftProject()
                + AMPERSAND + RosterConstants.AIRCRAFT_BUILT + EQUALS + member.getAircraftBuilt()
                + AMPERSAND + RosterConstants.ROW_COUNT + EQUALS + "50";
        return URLEncoder.encode(requestBody, StandardCharsets.UTF_8);
    }

    private String getViewStateValue() {
        if (viewState != null) {
            return viewState.attr("value");
        }
        return "/wEPDwUKMTY1NDU2MTA1MmRkuOlmdf9IlE5Upbw3feS5bMlNeitv2Tys6h3WSL105GQ=";
    }

    private String getViewStateGeneratorValue() {
        if (viewStateGenerator != null) {
            return viewStateGenerator.attr("value");
        }
        return "55FE2EBC";
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
        addFormContent(sb, RosterConstants.STATUS, member.getStatus().toString());
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
        addFormContent(sb, RosterConstants.BIRTH_DATE, ZonedDateTime.ofInstant(member.getBirthDateAsDate().toInstant(),
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        addFormContent(sb, RosterConstants.JOIN_DATE, ZonedDateTime.ofInstant(member.getJoinedAsDate().toInstant(),
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        addFormContent(sb, RosterConstants.EXPIRATION_DATE, ZonedDateTime.ofInstant(member.getExpiration().toInstant(),
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        addFormContent(sb, RosterConstants.OTHER_INFO, member.getOtherInfo());
        addFormContent(sb, RosterConstants.HOME_PHONE, member.getHomePhone());
        addFormContent(sb, RosterConstants.CELL_PHONE, member.getCellPhone());
        addFormContent(sb, RosterConstants.EMAIL, member.getEmail());
        addFormContent(sb, RosterConstants.RATINGS, member.getRatings());
        addFormContent(sb, RosterConstants.AIRCRAFT_OWNED, member.getAircraftOwned());
        addFormContent(sb, RosterConstants.AIRCRAFT_PROJECT, member.getAircraftProject());
        addFormContent(sb, RosterConstants.AIRCRAFT_BUILT, member.getAircraftBuilt());
        addFormContent(sb, RosterConstants.IMC, isInIMCClub(member));
        addFormContent(sb, RosterConstants.VMC, isInVMCClub(member));
        addFormContent(sb, RosterConstants.YOUNG_EAGLE_PILOT, isYoungEaglePilot(member));
        addFormContent(sb, RosterConstants.EAGLE_PILOT, isEaglePilot(member));
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
     * Is member in VMC club.
     *
     * @param member Member
     * @return member
     */
    private String isInVMCClub(final Member member) {
        String value = "off";
        if (member.isVmcClub()) {
            value = "on";
        }
        return value;
    }

    /**
     * Is member in VMC club.
     *
     * @param member Member
     * @return member
     */
    private String isInIMCClub(final Member member) {
        String value = "off";
        if (member.isImcClub()) {
            value = "on";
        }
        return value;
    }

    /**
     * Is member a YoungEagle pilot.
     *
     * @param member Member
     * @return member
     */
    private String isYoungEaglePilot(final Member member) {
        String value = "off";
        if (member.isYePilot()) {
            value = "on";
        }
        return value;
    }

    /**
     * Is member an Eagle pilot.
     *
     * @param member Member
     * @return member
     */
    private String isEaglePilot(final Member member) {
        String value = "off";
        if (member.isEaglePilot()) {
            value = "on";
        }
        return value;
    }

    /**
     * Adds a form content section to the provided StringBuilder object.
     *
     * @param sb StringBuilder
     * @param key Key
     * @param value Value
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
        final List<String> slackUsers = getAllSlackUsers();
        final Document doc = Jsoup.parse(fetchData());
        final Elements tableRecords = doc.getElementsByTag("tr");
        int rowCount = 0;
        for (Element tr : tableRecords) {
            if (rowCount > 0) {
                try {
                    final Elements columns = tr.getElementsByTag("td");
                    final Member member = new Member();
                    parseColumn(slackUsers, columns, member);
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
     * Parses a column of data.
     *
     * @param slackUsers List of Slack users
     * @param columns Elements
     * @param member Member
     * @throws ParseException
     */
    private void parseColumn(final List<String> slackUsers,
                             final Elements columns,
                             final Member member)
            throws ParseException {
        int columnCount = 0;
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
                case CommonConstants.THREE:
                    member.setFirstName(column.text().trim());
                    break;
                case CommonConstants.FOUR:
                    member.setLastName(column.text().trim());
                    break;
                case CommonConstants.FIVE:
                    member.setSpouse(column.text().trim());
                    break;
                case CommonConstants.SIX:
                    member.setGender(Gender.fromDisplayString(column.text().trim().toUpperCase()));
                    break;
                case CommonConstants.SEVEN:
                    member.setEmail(column.text().trim());
                    break;
                case CommonConstants.EIGHT:
                    // Ignore EmailPrivate
                    break;
                case CommonConstants.NINE:
                    member.setUsername(column.text().trim());
                    break;
                case CommonConstants.TEN:
                    member.setBirthDate(column.text().trim());
                    break;
                case CommonConstants.ELEVEN:
                    member.setAddressLine1(column.text().trim());
                    break;
                case CommonConstants.TWELVE:
                    member.setAddressLine2(column.text().trim());
                    break;
                case CommonConstants.THIRTEEN:
                    // Ignore AddressPrivate
                    break;
                case CommonConstants.FOURTEEN:
                    setHomePhone(member, column);
                    break;
                case CommonConstants.FIFTEEN:
                    // Ignore HomePhonePrivate
                    break;
                case CommonConstants.SIXTEEN:
                    setCellPhone(member, column);
                    break;
                case CommonConstants.SEVENTEEN:
                    // Ignore CellPhonePrivate
                    break;
                case CommonConstants.EIGHTEEN:
                    member.setEaaNumber(column.text().trim());
                    break;
                case CommonConstants.NINETEEN:
                    member.setStatus(Status.valueOf(column.text().trim().toUpperCase()));
                    break;
                case CommonConstants.TWENTY:
                    member.setJoined(column.text().trim());
                    break;
                case CommonConstants.TWENTY_ONE:
                    member.setExpiration(Date.from(ZonedDateTime.parse(column.text().trim(),
                            DateTimeFormatter.ofPattern("yyyy-MM-dd")).toInstant()));
                    break;
                case CommonConstants.TWENTY_TWO:
                    setOtherInfo(slackUsers, member, column);
                    break;
                case CommonConstants.TWENTY_THREE:
                    member.setCity(column.text().trim());
                    break;
                case CommonConstants.TWENTY_FOUR:
                    member.setState(State.fromDisplayString(column.text().trim()));
                    break;
                case CommonConstants.TWENTY_FIVE:
                    member.setCountry(Country.fromDisplayString(column.text().trim()));
                    break;
                case CommonConstants.TWENTY_SIX:
                    member.setZipCode(column.text().trim());
                    break;
                case CommonConstants.TWENTY_SEVEN:
                    member.setRatings(column.text().trim());
                    break;
                case CommonConstants.TWENTY_EIGHT:
                    member.setAircraftOwned(column.text().trim());
                    break;
                case CommonConstants.TWENTY_NINE:
                    member.setAircraftProject(column.text().trim());
                    break;
                case CommonConstants.THIRTY:
                    member.setAircraftBuilt(column.text().trim());
                    break;
                case CommonConstants.THIRTY_ONE:
                    setImcClub(member, column);
                    break;
                case CommonConstants.THIRTY_TWO:
                    setVmcClub(member, column);
                    break;
                case CommonConstants.THIRTY_THREE:
                    setYePilot(member, column);
                    break;
                case CommonConstants.THIRTY_FOUR:
                    setYeVolunteer(member, column);
                    break;
                case CommonConstants.THIRTY_FIVE:
                    setEaglePilot(member, column);
                    break;
                case CommonConstants.THIRTY_SIX:
                    setEagleVolunteer(member, column);
                    break;
                case CommonConstants.THIRTY_SEVEN:
                    // Ignore DateAdded
                    break;
                case CommonConstants.THIRTY_EIGHT:
                    // Ignore DateUpdated
                    break;
                case CommonConstants.THIRTY_NINE:
                    member.setEaaExpiration(column.text().trim());
                    break;
                case CommonConstants.FORTY:
                    member.setYouthProtection(column.text().trim());
                    break;
                case CommonConstants.FORTY_ONE:
                    member.setBackgroundCheck(column.text().trim());
                    break;
                case CommonConstants.FORTY_TWO:
                    // Ignore UpdatedBy
                    break;
                case CommonConstants.FORTY_THREE:
                    member.setWebAdminAccess(WebAdminAccess.fromDisplayString(column.text().trim()));
                    break;
                default:
                    // Do nothing
            }
            columnCount++;
        }
    }

    /**
     * Gets all Slack users as a List.
     *
     * @return List of all Slack users
     */
    private List<String> getAllSlackUsers() {
        final List<String> slackUsers = new ArrayList<>();
        try {
            slackUsers.addAll(slackService.allSlackUsers());
        } catch (ResourceNotFoundException e) {
            LOGGER.error("Error: " + e.getMessage(), e);
        }
        return slackUsers;
    }

    /**
     * Set Eagle Volunteer info.
     *
     * @param member Member
     * @param column Element
     */
    private void setEagleVolunteer(final Member member, final Element column) {
        if ("yes".equalsIgnoreCase(column.text().trim())) {
            member.setEagleVolunteer(Boolean.TRUE);
        } else {
            member.setEagleVolunteer(Boolean.FALSE);
        }
    }

    /**
     * Set Eagle Pilot info.
     *
     * @param member Member
     * @param column Element
     */
    private void setEaglePilot(final Member member, final Element column) {
        if ("yes".equalsIgnoreCase(column.text().trim())) {
            member.setEaglePilot(Boolean.TRUE);
        } else {
            member.setEaglePilot(Boolean.FALSE);
        }
    }

    /**
     * Set YE Volunteer info.
     *
     * @param member Member
     * @param column Element
     */
    private void setYeVolunteer(final Member member, final Element column) {
        if ("yes".equalsIgnoreCase(column.text().trim())) {
            member.setYeVolunteer(Boolean.TRUE);
        } else {
            member.setYeVolunteer(Boolean.FALSE);
        }
    }

    /**
     * Sets YE Pilot info.
     *
     * @param member Member
     * @param column Element
     */
    private void setYePilot(final Member member, final Element column) {
        if ("yes".equalsIgnoreCase(column.text().trim())) {
            member.setYePilot(Boolean.TRUE);
        } else {
            member.setYePilot(Boolean.FALSE);
        }
    }

    /**
     * Sets VMC Club info.
     *
     * @param member Member
     * @param column Element
     */
    private void setVmcClub(final Member member, final Element column) {
        if ("yes".equalsIgnoreCase(column.text().trim())) {
            member.setVmcClub(Boolean.TRUE);
        } else {
            member.setVmcClub(Boolean.FALSE);
        }
    }

    /**
     * Set IMC Club info.
     *
     * @param member Member
     * @param column Element
     */
    private void setImcClub(final Member member, final Element column) {
        if ("yes".equalsIgnoreCase(column.text().trim())) {
            member.setImcClub(Boolean.TRUE);
        } else {
            member.setImcClub(Boolean.FALSE);
        }
    }

    /**
     * Set OtherInfo.
     *
     * @param slackUsers List of Slack Users
     * @param member Member
     * @param column Element
     */
    private void setOtherInfo(final List<String> slackUsers, final Member member, final Element column) {
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
    }

    /**
     * Set Cell Phone info.
     *
     * @param member Member
     * @param column Element
     */
    private void setCellPhone(final Member member, final Element column) {
        member.setCellPhone(column
                .text()
                .trim()
                .replaceAll(" ", "")
                .replaceAll("-", "")
                .replaceAll("\\(", "")
                .replaceAll("\\)", ""));
    }

    /**
     * Set Home Phone info.
     *
     * @param member Member
     * @param column Element
     */
    private void setHomePhone(final Member member, final Element column) {
        member.setHomePhone(column
                .text()
                .trim()
                .replaceAll(" ", "")
                .replaceAll("-", "")
                .replaceAll("\\(", "")
                .replaceAll("\\)", ""));
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
            final String[] split = str.split("\\|");
            if (!"NULL".equalsIgnoreCase(split[1]) && str.contains(username)) {
                member.setSlack(split[1]);
            }
        });
    }

    /**
     * Gets date string for provided property key.
     *
     * @param key property key
     * @return date string
     * @throws ResourceNotFoundException when property not found
     */
    private String getFutureDateStr(final String key) throws ResourceNotFoundException {
        return ZonedDateTime.ofInstant(Instant
                .now()
                .plus(Integer.parseInt(propertyService
                                .get(key)
                                .getValue()),
                        ChronoUnit.DAYS),
                ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

}
