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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;

import org.eaa690.aerie.communication.SlackMessageSender;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.communication.SlackMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.cucumber.messages.internal.com.google.protobuf.Extension.MessageType;
import io.cucumber.messages.internal.com.google.protobuf.Message;

/**
 * SlackService.
 */
@Service("aerieSlackService")
public class SlackService extends CommunicationService<SlackMessage> implements SlackMessagePostedListener {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SlackService.class);

    /**
     * SlackSession.
     */
    @Autowired
    private SlackSession slackSession;

    @Autowired
    public SlackService(SlackMessageSender messageSender) {
        super(messageSender);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void queueMsg(final SlackMessage message) throws ResourceNotFoundException {
        super.queueMsg(message);
        // TODO: Immediately processes queue, inefficient
        processQueue((long) 1);
    }

    /**
     * Processes messages received by membership slack bot.
     *
     * @param event SlackMessagePosted
     * @param session SlackSession
     */
    @Override
    public void onEvent(final SlackMessagePosted event, final SlackSession session) {
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
     * Gets all Slack users.
     *
     * @return list of users
     */
    public List<String> allSlackUsers() {
        final List<String> users = new ArrayList<>();
        slackSession
                .getUsers()
                .forEach(user -> users.add(user.getRealName() + "|" + user.getUserName()));
        return users;
    }

    /**
     * Sends new member message.
     *
     * @param member Member
     */
    @Override
    public SlackMessage buildNewMembershipMsg(final Member member) {
        try {
            final SlackMessage newMembershipMessage = new SlackMessage(
                getSlackName(member),
                member.getId(),
                getSMSOrSlackMessage(member, PropertyKeyConstants.SLACK_NEW_MEMBER_MSG_KEY));
            return newMembershipMessage;

        } catch (ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage());
        }
        return null;
    }

    /**
     * Sends membership renewal message.
     *
     * @param member Member
     */
    @Override
    public SlackMessage buildRenewMembershipMsg(final Member member) {
        try {
            SlackMessage renewMembershipMessage = new SlackMessage(
                getSlackName(member),
                member.getId(),
                getSMSOrSlackMessage(member,PropertyKeyConstants.SLACK_RENEW_MEMBER_MSG_KEY));
            return renewMembershipMessage;

        } catch (ResourceNotFoundException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

    /**
     * Gets Slack name, or overridden value, if in test mode.
     *
     * @param member Member
     * @return Slack name
     * @throws ResourceNotFoundException if properties are not found
     */
    private String getSlackName(final Member member) throws ResourceNotFoundException {
        String to = member.getSlack();
        if (Boolean.parseBoolean(
                getPropertyService().get(PropertyKeyConstants.SLACK_TEST_MODE_ENABLED_KEY).getValue())) {
            to = getPropertyService().get(PropertyKeyConstants.SLACK_TEST_MODE_RECIPIENT_KEY).getValue();
        }
        return to;
    }

}
