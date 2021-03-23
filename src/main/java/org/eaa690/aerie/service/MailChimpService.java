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

import com.github.alexanderwe.bananaj.connection.MailChimpConnection;
import com.github.alexanderwe.bananaj.exceptions.TransportException;
import com.github.alexanderwe.bananaj.model.list.MailChimpList;
import com.github.alexanderwe.bananaj.model.list.member.EmailType;
import com.github.alexanderwe.bananaj.model.list.member.Member;
import com.github.alexanderwe.bananaj.model.list.member.MemberStatus;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MailChimpService.
 */
public class MailChimpService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MailChimpService.class);

    /**
     * PropertyService.
     */
    private PropertyService propertyService;

    /**
     * MailChimp Connection.
     */
    private MailChimpConnection mailChimpConnection;

    /**
     * Sets PropertyService.
     *
     * @param value PropertyService
     */
    @Autowired
    public void setPropertyService(final PropertyService value) {
        propertyService = value;
    }

    public void addOrUpdateMember(String firstName, String lastName, String emailAddress)
            throws ResourceNotFoundException {
        final String listId = propertyService.get(PropertyKeyConstants.MAILCHIMP_MEMBERS_LIST_ID_KEY).getValue();
        addOrUpdateMember(listId, firstName, lastName, emailAddress);
        final List<Member> members = getMemberList(listId);
        for (Member member : members) {
            final String email = member.getEmailAddress();
            final Map<String, Object> mergeFields = member.getMergeFields();
            final String first = (String)mergeFields.get("FNAME");
            final String last = (String)mergeFields.get("LNAME");
            final String memberId = member.getId();
            if (emailAddress.equalsIgnoreCase(email) &&
                    firstName.equalsIgnoreCase(first) &&
                    lastName.equalsIgnoreCase(last)) {
                deleteMember(propertyService
                        .get(PropertyKeyConstants.MAILCHIMP_NON_MEMBERS_LIST_ID_KEY).getValue(), memberId);
            }
        }
    }

    public void addOrUpdateNonMember(String firstName, String lastName, String emailAddress)
            throws ResourceNotFoundException {
        final String listId = propertyService.get(PropertyKeyConstants.MAILCHIMP_NON_MEMBERS_LIST_ID_KEY).getValue();
        addOrUpdateMember(listId, firstName, lastName, emailAddress);
        final List<Member> members = getMemberList(listId);
        for (Member member : members) {
            final String email = member.getEmailAddress();
            final Map<String, Object> mergeFields = member.getMergeFields();
            final String first = (String)mergeFields.get("FNAME");
            final String last = (String)mergeFields.get("LNAME");
            final String memberId = member.getId();
            if (emailAddress.equalsIgnoreCase(email) &&
                    firstName.equalsIgnoreCase(first) &&
                    lastName.equalsIgnoreCase(last)) {
                deleteMember(propertyService
                        .get(PropertyKeyConstants.MAILCHIMP_MEMBERS_LIST_ID_KEY).getValue(), memberId);
            }
        }
    }

    private List<Member> getMemberList(String listId) throws ResourceNotFoundException {
        initMailChimp();
        try {
            final MailChimpList yourList = mailChimpConnection.getList(listId);
            return yourList.getMembers(0,0);
        } catch (URISyntaxException | TransportException | MalformedURLException e) {
            LOGGER.error("ERROR", e);
        }
        throw new ResourceNotFoundException();
    }

    private void addOrUpdateMember(String listId, String firstName, String lastName, String emailAddress) {
        initMailChimp();
        try {
            Map<String, Object> mergeFields = new HashMap<>();
            mergeFields.put("FNAME", firstName);
            mergeFields.put("LNAME", lastName);
            LocalDateTime timeStamp = LocalDateTime.now();

            final MailChimpList yourList = mailChimpConnection.getList(listId);
            Member member = new Member.Builder()
                    .emailAddress(emailAddress)
                    .list(yourList)
                    .emailType(EmailType.HTML)
                    .status(MemberStatus.SUBSCRIBED)
                    .mergeFields(mergeFields)
                    .statusIfNew(MemberStatus.SUBSCRIBED)
                    .ipSignup("")
                    .timestampSignup(timeStamp)
                    .ipOpt("")
                    .timestampOpt(timeStamp)
                    .build();
            yourList.addOrUpdateMember(member);
        } catch (URISyntaxException | TransportException | MalformedURLException e) {
            LOGGER.error("ERROR", e);
        }
    }

    private void deleteMember(String listId, String memberId) {
        initMailChimp();
        try {
            final MailChimpList yourList = mailChimpConnection.getList(listId);
            yourList.deleteMemberFromList(memberId);
        } catch (URISyntaxException | TransportException | MalformedURLException e) {
            LOGGER.error("ERROR", e);
        }
    }

    private void initMailChimp() {
        try {
            if (mailChimpConnection == null) {
                mailChimpConnection = new MailChimpConnection(propertyService
                        .get(PropertyKeyConstants.MAILCHIMP_API_KEY).getValue());
            }
        } catch (ResourceNotFoundException e) {
            LOGGER.error("ERROR", e);
        }
    }
}
