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
    @Scheduled(cron = "0 0,10,20,30,40,50 * * * *")
    public void getSubmissions() {
        LOGGER.info("Called");
        try {
            final String dateStr = "2021-03-13 00:00:00"; //sdf.format(new Date());
            final JotForm client = new JotForm(propertyService.get(PropertyKeyConstants.JOTFORM_API_KEY_KEY).getValue());
            final HashMap<String, String> submissionFilter = new HashMap<String, String>();
            submissionFilter.put("id:gt",
                    propertyService.get(PropertyKeyConstants.JOTFORM_NEW_MEMBER_FORM_ID_KEY).getValue());
            submissionFilter.put("created_at:gt", dateStr);
            final Map<String, Member> newMembersMap = new HashMap<>();
            LOGGER.info("Querying for new member form submissions after " + dateStr);
            parseNewMember(newMembersMap, client.getSubmissions("0", "1000", submissionFilter, "created_at"));

            final Map<String, Member> renewMembersMap = new HashMap<>();
            submissionFilter.put("id:gt",
                    propertyService.get(PropertyKeyConstants.JOTFORM_MEMBER_RENEWAL_FORM_ID_KEY).getValue());
            submissionFilter.put("created_at:gt", dateStr);
            LOGGER.info("Querying for member renewal form submissions after " + dateStr);
            parseRenewingMember(renewMembersMap, client.getSubmissions("0", "1000", submissionFilter, "created_at"));

            final Map<String, Member> membersMap = new HashMap<>();
            membersMap.putAll(newMembersMap);
            membersMap.putAll(renewMembersMap);
            if (!membersMap.isEmpty()) {
                LOGGER.info("MembersMap size is " + membersMap.size());
                for (String key : membersMap.keySet()) {
                    if (submissionsCache.getIfPresent(key) == null) {
                        submissionsCache.put(key, key);
                        rosterService.saveMember(membersMap.get(key));
                    }
                }
            }
        } catch (ResourceNotFoundException rnfe) {
            LOGGER.error(rnfe);
        }
        LOGGER.info("Finished");
    }

    private void parseNewMember(Map<String, Member> membersMap, JSONObject submission) {
        final JSONArray content = submission.getJSONArray("content");
        for (int i = 0; i < content.length(); i++) {
            final Member member = new Member();
            final OtherInfoBuilder otherInfoBuilder = new OtherInfoBuilder();
            final JSONObject object = content.getJSONObject(i);
            final JSONObject answers = object.getJSONObject("answers");
            final JSONObject fullName = answers.getJSONObject("3");
            final JSONObject fullNameAnswer = fullName.getJSONObject("answer");
            member.setFirstName(fullNameAnswer.getString("first"));
            member.setLastName(fullNameAnswer.getString("last"));
            final JSONObject address = answers.getJSONObject("4");
            final JSONObject addressAnswer = address.getJSONObject("answer");
            member.setAddressLine1(addressAnswer.getString("addr_line1"));
            member.setCity(addressAnswer.getString("city"));
            member.setState(deriveState(addressAnswer.getString("state"));
            member.setZipCode(addressAnswer.getString("postal"));
            final JSONObject phone = answers.getJSONObject("5");
            final JSONObject phoneAnswer = phone.getJSONObject("answer");
            member.setHomePhone(phoneAnswer.getString("full"));
            final JSONObject email = answers.getJSONObject("6");
            member.setEmail(email.getString("answer"));
            final JSONObject additionalFamily = answers.getJSONObject("9");
            otherInfoBuilder.setAdditionalFamily(additionalFamily.getString("answer"));
            JSONObject additionalInfo = answers.getJSONObject("11");
            otherInfoBuilder.setAdditionalInfo(additionalInfo.getString("answer"));
            JSONObject eaaNumber = answers.getJSONObject("15");
            member.setEaaNumber(eaaNumber.getString("answer"));
            // TODO
            //JSONObject membershipType = answers.getJSONObject("16");
            //member.setMemberType();
            JSONObject numOfFamily = answers.getJSONObject("17");
            otherInfoBuilder.setNumberOfFamily(numOfFamily.getString("answer"));
            member.setOtherInfo(otherInfoBuilder.getRaw());
            membersMap.put((String)object.get("id"), member);
        }
    }

    private void parseRenewingMember(Map<String, Member> membersMap, JSONObject submission) {
        final JSONArray content = submission.getJSONArray("content");
        for (int i = 0; i < content.length(); i++) {
            final Member member = new Member();
            final OtherInfoBuilder otherInfoBuilder = new OtherInfoBuilder();
            final JSONObject object = content.getJSONObject(i);
            final JSONObject answers = object.getJSONObject("answers");
            final JSONObject fullName = answers.getJSONObject("3");
            final JSONObject fullNameAnswer = fullName.getJSONObject("answer");
            member.setFirstName(fullNameAnswer.getString("first"));
            member.setLastName(fullNameAnswer.getString("last"));
            final JSONObject address = answers.getJSONObject("4");
            final JSONObject addressAnswer = address.getJSONObject("answer");
            member.setAddressLine1(addressAnswer.getString("addr_line1"));
            member.setCity(addressAnswer.getString("city"));
            member.setState(deriveState(addressAnswer.getString("state"));
            member.setZipCode(addressAnswer.getString("postal"));
            final JSONObject phone = answers.getJSONObject("5");
            final JSONObject phoneAnswer = phone.getJSONObject("answer");
            member.setHomePhone(phoneAnswer.getString("full"));
            final JSONObject email = answers.getJSONObject("6");
            member.setEmail(email.getString("answer"));
            final JSONObject additionalFamily = answers.getJSONObject("9");
            otherInfoBuilder.setAdditionalFamily(additionalFamily.getString("answer"));
            JSONObject additionalInfo = answers.getJSONObject("11");
            otherInfoBuilder.setAdditionalInfo(additionalInfo.getString("answer"));
            JSONObject eaaNumber = answers.getJSONObject("15");
            member.setEaaNumber(eaaNumber.getString("answer"));
            // TODO
            //JSONObject membershipType = answers.getJSONObject("16");
            //member.setMemberType();
            JSONObject numOfFamily = answers.getJSONObject("17");
            otherInfoBuilder.setNumberOfFamily(numOfFamily.getString("answer"));
            member.setOtherInfo(otherInfoBuilder.getRaw());
            membersMap.put((String)object.get("id"), member);
        }
    }

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