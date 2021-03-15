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
     */
    @Scheduled(cron = "0 0 * * * *")
    public void getSubmissions() {
        LOGGER.info("Called");
        try {
            final String dateStr = sdf.format(new Date());
            final JotForm client = new JotForm(propertyService.get(PropertyKeyConstants.JOTFORM_API_KEY_KEY).getValue());
            final HashMap<String, String> submissionFilter = new HashMap<String, String>();
            submissionFilter.put("id:gt",
                    propertyService.get(PropertyKeyConstants.JOTFORM_NEW_MEMBER_FORM_ID_KEY).getValue());
            submissionFilter.put("created_at:gt", dateStr);
            final Map<String, Member> membersMap = new HashMap<>();
            LOGGER.info("Querying for new member form submissions after " + dateStr);
            parseMembers(membersMap, client.getSubmissions("0", "1000", submissionFilter, "created_at"));

            submissionFilter.put("id:gt",
                    propertyService.get(PropertyKeyConstants.JOTFORM_MEMBER_RENEWAL_FORM_ID_KEY).getValue());
            submissionFilter.put("created_at:gt", dateStr);
            LOGGER.info("Querying for member renewal form submissions after " + dateStr);
            parseMembers(membersMap, client.getSubmissions("0", "1000", submissionFilter, "created_at"));

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

    private void parseMembers(Map<String, Member> membersMap, JSONObject submission) {
        LOGGER.info("Parsing: " + submission);
        JSONArray content = submission.getJSONArray("content");
        for (int i = 0; i < content.length(); i++) {
            JSONObject object = content.getJSONObject(i);
            LOGGER.info(object.get("id"));
        }
    }

}