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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.JotForm;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.roster.OtherInfoBuilder;
import org.eaa690.aerie.model.roster.State;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Retrieves form submissions to JotForm.
 */
public class JotFormService {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(JotFormService.class);

    /**
     * Answers.
     */
    private static final String ANSWERS = "answers";

    /**
     * Answer.
     */
    private static final String ANSWER = "answer";

    /**
     * Used to filter form submissions retrieval.
     */
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");

    static Cache<String, String> submissionsCache =
            CacheBuilder.newBuilder().expireAfterWrite(36, TimeUnit.HOURS).build();

    /**
     * PropertyService.
     */
    @Autowired
    private PropertyService propertyService;

    /**
     * RosterService.
     */
    @Autowired
    private RosterService rosterService;

    /**
     * Sets PropertyService.
     *
     * @param value PropertyService
     */
    @Autowired
    public void setPropertyService(final PropertyService value) {
        propertyService = value;
    }

    /**
     * Sets RosterService.
     *
     * @param value RosterService
     */
    @Autowired
    public void setRosterService(final RosterService value) {
        rosterService = value;
    }

    /**
     * Retrieves JotForm submissions.
     *
     * second, minute, hour, day of month, month, day(s) of week
     */
    @Scheduled(cron = "0 0 * * * *")
    public void getSubmissions() {
        try {
            final String dateStr = sdf.format(new Date());
            final JotForm client = new JotForm(propertyService.get(PropertyKeyConstants.JOTFORM_API_KEY_KEY).getValue());
            processNewMemberSubmissions(dateStr, client);
            processRenewingMemberSubmissions(dateStr, client);
        } catch (ResourceNotFoundException rnfe) {
            LOGGER.error(rnfe);
        }
    }

    /**
     * Processes renewing member submissions.
     *
     * @param dateStr Date
     * @param client JotFormClient
     * @throws ResourceNotFoundException when property is not found
     */
    private void processRenewingMemberSubmissions(String dateStr, JotForm client) throws ResourceNotFoundException {
        final HashMap<String, String> submissionFilter = new HashMap<>();
        final Map<String, Member> renewMembersMap = new HashMap<>();
        submissionFilter.put("id:gt",
                propertyService.get(PropertyKeyConstants.JOTFORM_MEMBER_RENEWAL_FORM_ID_KEY).getValue());
        submissionFilter.put("created_at:gt", dateStr);
        LOGGER.info("Querying for member renewal form submissions after " + dateStr);
        parseRenewingMember(renewMembersMap, client.getSubmissions("0", "1000", submissionFilter, "created_at"));

        if (!renewMembersMap.isEmpty()) {
            LOGGER.info("RenewMembersMap size is " + renewMembersMap.size());
            for (String key : renewMembersMap.keySet()) {
                if (submissionsCache.getIfPresent(key) == null) {
                    submissionsCache.put(key, key);
                    rosterService.saveRenewingMember(renewMembersMap.get(key));
                }
            }
        }
    }

    /**
     * Processes new member submissions.
     *
     * @param dateStr Date
     * @param client JotFormClient
     * @throws ResourceNotFoundException when property is not found
     */
    private void processNewMemberSubmissions(String dateStr, JotForm client) throws ResourceNotFoundException {
        final HashMap<String, String> submissionFilter = new HashMap<>();
        submissionFilter.put("id:gt",
                propertyService.get(PropertyKeyConstants.JOTFORM_NEW_MEMBER_FORM_ID_KEY).getValue());
        submissionFilter.put("created_at:gt", dateStr);
        final Map<String, Member> newMembersMap = new HashMap<>();
        LOGGER.info("Querying for new member form submissions after " + dateStr);
        parseNewMember(newMembersMap, client.getSubmissions("0", "1000", submissionFilter, "created_at"));

        if (!newMembersMap.isEmpty()) {
            LOGGER.info("NewMembersMap size is " + newMembersMap.size());
            for (String key : newMembersMap.keySet()) {
                if (submissionsCache.getIfPresent(key) == null) {
                    submissionsCache.put(key, key);
                    rosterService.saveNewMember(newMembersMap.get(key));
                }
            }
        }
    }

    /**
     * Parses new member information from JotForm.
     *
     * @param membersMap map of new members
     * @param submission JotForm
     */
    private void parseNewMember(Map<String, Member> membersMap, JSONObject submission) {
        final JSONArray content = submission.getJSONArray("content");
        for (int i = 0; i < content.length(); i++) {
            final Member member = new Member();
            final OtherInfoBuilder otherInfoBuilder = new OtherInfoBuilder();
            final JSONObject object = content.getJSONObject(i);
            if (object.has(ANSWERS)) {
                final JSONObject answers = object.getJSONObject(ANSWERS);
                parseName(member, answers);
                parseAddress(member, answers);
                parsePhone(member, answers);
                parseEmail(member, answers);
                parseAdditionalFamily(otherInfoBuilder, answers);
                parseAdditionalInfo(otherInfoBuilder, answers);
                parseEAANumber(member, answers);
                parseMembershipType(member, answers);
                parseNumOfFamily(otherInfoBuilder, answers);
                member.setOtherInfo(otherInfoBuilder.getRaw());
            }
            membersMap.put((String)object.get("id"), member);
        }
    }

    /**
     * Parses renewing member information from JotForm.
     *
     * @param membersMap map of new members
     * @param submission JotForm
     */
    private void parseRenewingMember(Map<String, Member> membersMap, JSONObject submission) {
        final JSONArray content = submission.getJSONArray("content");
        for (int i = 0; i < content.length(); i++) {
            final Member member = new Member();
            final OtherInfoBuilder otherInfoBuilder = new OtherInfoBuilder();
            final JSONObject object = content.getJSONObject(i);
            if (object.has(ANSWERS)) {
                final JSONObject answers = object.getJSONObject(ANSWERS);
                parseName(member, answers);
                parseAddress(member, answers);
                parsePhone(member, answers);
                parseEmail(member, answers);
                parseAdditionalFamily(otherInfoBuilder, answers);
                parseAdditionalInfo(otherInfoBuilder, answers);
                parseEAANumber(member, answers);
                parseMembershipType(member, answers);
                parseNumOfFamily(otherInfoBuilder, answers);
                member.setOtherInfo(otherInfoBuilder.getRaw());
            }
            membersMap.put((String)object.get("id"), member);
        }
    }

    /**
     * Parses number of family members.
     *
     * @param otherInfoBuilder OtherInfoBuilder
     * @param answers JotForm
     */
    private void parseNumOfFamily(OtherInfoBuilder otherInfoBuilder, JSONObject answers) {
        if (answers.has("17")) {
            JSONObject numOfFamily = answers.getJSONObject("17");
            if (numOfFamily.has(ANSWER)) {
                otherInfoBuilder.setNumberOfFamily(numOfFamily.getString(ANSWER));
            }
        }
    }

    /**
     * Parses membership type.
     *
     * @param member Member
     * @param answers JotForm
     */
    private void parseMembershipType(Member member, JSONObject answers) {
        if (answers.has("16")) {
            // TODO
            //JSONObject membershipType = answers.getJSONObject("16");
            //member.setMemberType();
        }
    }

    /**
     * Parses EAA number.
     *
     * @param member Member
     * @param answers JotForm
     */
    private void parseEAANumber(Member member, JSONObject answers) {
        if (answers.has("15")) {
            JSONObject eaaNumber = answers.getJSONObject("15");
            if (eaaNumber.has(ANSWER)) {
                member.setEaaNumber(eaaNumber.getString(ANSWER));
            }
        }
    }

    /**
     * Parses additional info.
     *
     * @param otherInfoBuilder OtherInfoBuilder
     * @param answers JotForm
     */
    private void parseAdditionalInfo(OtherInfoBuilder otherInfoBuilder, JSONObject answers) {
        if (answers.has("11")) {
            JSONObject additionalInfo = answers.getJSONObject("11");
            if (additionalInfo.has(ANSWER)) {
                otherInfoBuilder.setAdditionalInfo(additionalInfo.getString(ANSWER));
            }
        }
    }

    /**
     * Parses additional family.
     *
     * @param otherInfoBuilder OtherInfoBuilder
     * @param answers JotForm
     */
    private void parseAdditionalFamily(OtherInfoBuilder otherInfoBuilder, JSONObject answers) {
        if (answers.has("9")) {
            final JSONObject additionalFamily = answers.getJSONObject("9");
            if (additionalFamily.has(ANSWER)) {
                otherInfoBuilder.setAdditionalFamily(additionalFamily.getString(ANSWER));
            }
        }
    }

    /**
     * Parses email.
     *
     * @param member Member
     * @param answers JotForm
     */
    private void parseEmail(Member member, JSONObject answers) {
        if (answers.has("6")) {
            final JSONObject email = answers.getJSONObject("6");
            if (email.has(ANSWER)) {
                member.setEmail(email.getString(ANSWER));
            }
        }
    }

    /**
     * Parses phone number.
     *
     * @param member Member
     * @param answers JotForm
     */
    private void parsePhone(Member member, JSONObject answers) {
        if (answers.has("5")) {
            final JSONObject phone = answers.getJSONObject("5");
            if (phone.has(ANSWER)) {
                final JSONObject phoneAnswer = phone.getJSONObject(ANSWER);
                if (phoneAnswer.has("full")) {
                    member.setHomePhone(phoneAnswer.getString("full"));
                }
            }
        }
    }

    /**
     * Parses address.
     *
     * @param member Member
     * @param answers JotForm
     */
    private void parseAddress(Member member, JSONObject answers) {
        if (answers.has("4")) {
            final JSONObject address = answers.getJSONObject("4");
            if (address.has(ANSWER)) {
                final JSONObject addressAnswer = address.getJSONObject(ANSWER);
                if (addressAnswer.has("addr_line1")) {
                    member.setAddressLine1(addressAnswer.getString("addr_line1"));
                }
                if (addressAnswer.has("city")) {
                    member.setCity(addressAnswer.getString("city"));
                }
                if (addressAnswer.has("state")) {
                    member.setState(deriveState(addressAnswer.getString("state")));
                }
                if (addressAnswer.has("postal")) {
                    member.setZipCode(addressAnswer.getString("postal"));
                }
            }
        }
    }

    /**
     * Parses name.
     *
     * @param member Member
     * @param answers JotForm
     */
    private void parseName(Member member, JSONObject answers) {
        if (answers.has("3")) {
            final JSONObject fullName = answers.getJSONObject("3");
            if (fullName.has(ANSWER)) {
                final JSONObject fullNameAnswer = fullName.getJSONObject(ANSWER);
                if (fullNameAnswer.has("first")) {
                    member.setFirstName(fullNameAnswer.getString("first"));
                }
                if (fullNameAnswer.has("last")) {
                    member.setLastName(fullNameAnswer.getString("last"));
                }
            }
        }
    }

    /**
     * Translates state string to enum.
     *
     * @param state String
     * @return enum
     */
    private State deriveState(String state) {
        if ("AL".equalsIgnoreCase(state)) {
            return State.ALABAMA;
        } else if ("FL".equalsIgnoreCase(state)) {
            return State.FLORIDA;
        } else if ("NC".equalsIgnoreCase(state)) {
            return State.NORTH_CAROLINA;
        } else if ("SC".equalsIgnoreCase(state)) {
            return State.SOUTH_CAROLINA;
        } else if ("TN".equalsIgnoreCase(state)) {
            return State.TENNESSEE;
        }
        return State.GEORGIA;
    }

}