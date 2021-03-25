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
    public Member getMemberByID(final Long id) throws ResourceNotFoundException {
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
     * @param id Member ID
     * @param rfid new RFID value
     * @throws ResourceNotFoundException when no member matches
     */
    public void updateMemberRFID(final Long id, final String rfid) throws ResourceNotFoundException {
        final Member member = getMemberByID(id);
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
     * Performs login to EAA's roster management system.
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

    private String buildUpdateUserRequestBodyString(final Member member) {
        final StringBuilder sb = new StringBuilder();
        sb
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.EVENT_TARGET)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.EVENT_ARGUMENT)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.LAST_FOCUS)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.VIEW_STATE)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(headers
                        .get(RosterConstants.VIEW_STATE)
                        .replaceAll("/", "%2F")
                        .replaceAll("=", "%3D")
                        .replaceAll("\\+", "%2B"))
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.VIEW_STATE_GENERATOR)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(headers.get(RosterConstants.VIEW_STATE_GENERATOR))
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.VIEW_STATE_ENCRYPTED)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.FIRST_NAME)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.LAST_NAME)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getLastName())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.STATUS)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getStatus())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.SEARCH_MEMBER_TYPE)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.CURRENT_STATUS)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.UPDATE_THIS_MEMBER_BUTTON)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append("Update")
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.TEXT_FIRST_NAME)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getFirstName())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.TEXT_LAST_NAME)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getLastName())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.TEXT_NICK_NAME)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getNickname())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.SPOUSE)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getSpouse())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.GENDER)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(Gender.getDisplayString(member.getGender()))
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.MEMBER_ID)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getEaaNumber())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.MEMBER_TYPE)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(MemberType.toDisplayString(member.getMemberType()))
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.CURRENT_STANDING)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(Status.getDisplayString(member.getStatus()))
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.USER_NAME)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getUsername())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.ADMIN_LEVEL)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(WebAdminAccess.getDisplayString(member.getWebAdminAccess()))
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.ADDRESS_LINE_1)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getAddressLine1())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.ADDRESS_LINE_2)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getAddressLine2())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.CITY)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getCity())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.STATE)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(State.getDisplayString(member.getState()))
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.ZIP_CODE)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getZipCode())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.COUNTRY)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(Country.toDisplayString(member.getCountry()))
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.BIRTH_DATE)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(MDY_SDF.format(member.getBirthDateAsDate()))
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.JOIN_DATE)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(MDY_SDF.format(member.getJoinedAsDate()))
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.EXPIRATION_DATE)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(MDY_SDF.format(member.getExpiration()))
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.OTHER_INFO)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getOtherInfo())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.HOME_PHONE)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getHomePhone())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.CELL_PHONE)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getCellPhone())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.EMAIL)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getEmail())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.RATINGS)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getRatings())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.AIRCRAFT_OWNED)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getAircraftOwned())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.AIRCRAFT_PROJECT)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getAircraftProject())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.AIRCRAFT_BUILT)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.getAircraftBuilt())
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.IMC)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.isImcClub() ? "on" : "off")
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.VMC)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.isVmcClub() ? "on" : "off")
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.YOUNG_EAGLE_PILOT)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.isYePilot() ? "on" : "off")
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.EAGLE_PILOT)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(member.isEaglePilot() ? "on" : "off")
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.PHOTO)
                .append("\"; filename=\"\"\n")
                .append("Content-Type: application/octet-stream\n\n")
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.PHOTO_FILE_NAME)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.PHOTO_FILE_TYPE)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append(RosterConstants.FORM_BOUNDARY)
                .append(RosterConstants.CONTENT_DISPOSITION_FORM_DATA_PREFIX)
                .append(RosterConstants.ROW_COUNT)
                .append(RosterConstants.FORM_DATA_SEPARATOR)
                .append("50")
                .append(RosterConstants.FORM_BOUNDARY)
                .append("--");
        return sb.toString();
    }

    /**
     * Parses select values from Excel spreadsheet.
     *
     * @return list of parsed values
     */
    private List<Member> parseRecords() {
        final List<Member> records = new ArrayList<>();
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
                                member.setHomePhone(column.text().trim());
                                break;
                            case 15:
                                // Ignore HomePhonePrivate
                                break;
                            case 16:
                                member.setCellPhone(column.text().trim());
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
