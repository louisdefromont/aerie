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
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * SlackService.
 */
@Service("aerieSlackService")
public class SlackService implements SlackMessagePostedListener {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SlackService.class);

    /**
     * SimpleDateFormat.
     */
    private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy");

    /**
     * PropertyService.
     */
    private PropertyService propertyService;

    /**
     * SlackSession
     */
    private SlackSession slackSession = null;

    /**
     * Sets PropertyService.
     *
     * @param value PropertyService
     */
    @Autowired
    public void setPropertyService(final PropertyService value) {
        propertyService = value;
    }

    public void sendNewMembershipMsg(final Member member) {
        if (member.getSlack() == null) {
            return;
        }
        try {
            String to = member.getSlack();
            if (Boolean.valueOf(propertyService.get(PropertyKeyConstants.SLACK_TEST_MODE_ENABLED_KEY).getValue())) {
                to = propertyService.get(PropertyKeyConstants.SLACK_TEST_MODE_RECIPIENT_KEY).getValue();
            }
            final String qualifier =
                    Boolean.valueOf(propertyService.get(PropertyKeyConstants.SLACK_ENABLED_KEY).getValue()) ?
                            "S" : "Not s";
            LOGGER.info(String.format("%sending new membership slack message... toAddress [%s];", qualifier, to));
            if (Boolean.parseBoolean(propertyService.get(PropertyKeyConstants.SLACK_ENABLED_KEY).getValue())) {
                sendMessage(String.format("Welcome %s to EAA 690!", member.getFirstName()), to);
            }
        } catch (ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public void sendRenewMembershipMsg(final Member member) {
        if (member.getSlack() == null) {
            return;
        }
        try {
            final String expiration = member.getExpiration() != null ?
                    sdf.format(member.getExpiration()) : sdf.format(new Date());
            String to = member.getSlack();
            if (Boolean.valueOf(propertyService.get(PropertyKeyConstants.SLACK_TEST_MODE_ENABLED_KEY).getValue())) {
                to = propertyService.get(PropertyKeyConstants.SLACK_TEST_MODE_RECIPIENT_KEY).getValue();
            }
            final String qualifier =
                    Boolean.valueOf(propertyService.get(PropertyKeyConstants.SLACK_ENABLED_KEY).getValue()) ?
                            "S" : "Not s";
            LOGGER.info(String.format("%sending membership renewal slack message... toAddress [%s];", qualifier, to));
            if (Boolean.parseBoolean(propertyService.get(PropertyKeyConstants.SLACK_ENABLED_KEY).getValue())) {
                sendMessage(String.format("Hi %s, please be sure to renew your chapter membership before %s!",
                        member.getFirstName(), expiration), to);
            }
        } catch (ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    @Override
    public void onEvent(SlackMessagePosted event, SlackSession session) {
        // Ignore bot user messages
        if (session.sessionPersona().getId().equals(event.getSender().getId())) {
            return;
        }
        final String message = event.getMessageContent();
        final String user = event.getUser().getUserName();
        final String msg = String.format(
                "Slack message received: user [%s]; message [%s]",
                user,
                message);
        LOGGER.info(msg);
    }

    /**
     * Sends a message via the Slack bot.
     *
     * @param msg message to be sent
     * @param slackUserName Slack User Name
     * @throws ResourceNotFoundException when properties are not found
     */
    private void sendMessage(final String msg, final String slackUserName) throws ResourceNotFoundException {
        if (slackSession == null || !slackSession.isConnected()) {
            slackSession = SlackSessionFactory
                    .createWebSocketSlackSession(
                            propertyService.get(PropertyKeyConstants.SLACK_TOKEN_KEY).getValue());
            try {
                slackSession.connect();
                slackSession.addMessagePostedListener(this);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        slackSession.sendMessageToUser(slackSession.findUserByUserName(slackUserName), msg, null);
    }

}