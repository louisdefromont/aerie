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
import com.google.common.cache.LoadingCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.JotForm;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.MemberRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    /**
     * Jot Form API Key
     */
    private String API_KEY = "";

    private String NEW_MEMBER_FORM_ID = "203084910640145";

    private String RENEW_MEMBER_FORM_ID = "203205658119150";

    private String MEMBER_SUBSCRIPTION_FORM_ID = "210335742062143";

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
        final JotForm client = new JotForm(API_KEY);
        final HashMap<String, String> submissionFilter = new HashMap<String, String>();
        submissionFilter.put("id:gt", NEW_MEMBER_FORM_ID);
        submissionFilter.put("created_at:gt", sdf.format(new Date()));
        final Map<String, Member> membersMap = new HashMap<>();
        parseMembers(membersMap, client.getSubmissions("0", "1000", submissionFilter, "created_at"));

        submissionFilter.put("id:gt", RENEW_MEMBER_FORM_ID);
        submissionFilter.put("created_at:gt", sdf.format(new Date()));
        parseMembers(membersMap, client.getSubmissions("0", "1000", submissionFilter, "created_at"));

        submissionFilter.put("id:gt", MEMBER_SUBSCRIPTION_FORM_ID);
        submissionFilter.put("created_at:gt", sdf.format(new Date()));
        parseMembers(membersMap, client.getSubmissions("0", "1000", submissionFilter, "created_at"));

        if (!membersMap.isEmpty()) {
            for (String key : membersMap.keySet()) {
                if (submissionsCache.getIfPresent(key) == null) {
                    submissionsCache.put(key, key);
                    rosterService.saveMember(membersMap.get(key));
                }
            }
        }
    }

    private void parseMembers(Map<String, Member> membersMap, JSONObject submission) {
        JSONArray content = submission.getJSONArray("content");
        for (int i = 0; i < content.length(); i++) {
            JSONObject object = content.getJSONObject(i);
            System.out.println(object.get("id"));
        }
    }

}