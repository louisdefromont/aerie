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

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.MessageType;
import org.eaa690.aerie.model.QueuedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SlackService.
 */
@Service("aerieSlackService")
public class SlackService extends CommunicationService implements SlackMessagePostedListener {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SlackService.class);

    /**
     * Looks for any messages in the send queue, and sends up to X (see configuration) messages per day.
     */
    @Override
    @Scheduled(cron = "0 0 10 * * *")
    public void processQueue() {
        final Optional<List<QueuedMessage>> allQueuedMessages = getQueuedMessageRepository().findAll();
        if (allQueuedMessages.isPresent()) {
            try {
                allQueuedMessages
                        .get()
                        .stream()
                        .filter(qm -> qm.getMessageType() == MessageType.Slack)
                        .limit(Long.parseLong(
                                getPropertyService().get(PropertyKeyConstants.SEND_GRID_LIMIT).getValue()))
                        .forEach(qe -> {
                            sendMessage(qe);
                            getQueuedMessageRepository().delete(qe);
                        });
            } catch (ResourceNotFoundException e) {
                LOGGER.error("Error", e);
            }
        }
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
        getSlackSession()
                .getUsers()
                .forEach(user -> users.add(user.getRealName() + "|" + user.getUserName()));
        return users;
    }

    /**
     * Sends a message via the Slack bot.
     *
     * @param queuedMessage QueuedMessage
     */
    @Override
    public void sendMessage(final QueuedMessage queuedMessage) {
        if (queuedMessage.getMessageType() == MessageType.Slack) {
            Optional<Member> memberOpt = getMemberRepository().findById(queuedMessage.getMemberId());
            if (memberOpt.isPresent() && getAcceptsSlackPredicate().test(memberOpt.get())) {
                LOGGER.info(String.format("Sending Slack message [%s] to [%s]", queuedMessage.getBody(),
                        queuedMessage.getRecipientAddress()));
                getSlackSession().sendMessageToUser(
                        getSlackSession().findUserByUserName(queuedMessage.getRecipientAddress()),
                        queuedMessage.getBody(), null);
            }
        }
    }

    /**
     * Sends new member message.
     *
     * @param member Member
     */
    @Override
    public void sendNewMembershipMsg(final Member member) {
        if (member != null && member.getSlack() != null && member.isSlackEnabled()) {
            try {
                final QueuedMessage queuedSlackMessage = new QueuedMessage();
                queuedSlackMessage.setMemberId(member.getId());
                queuedSlackMessage.setBody(getSMSOrSlackMessage(member, PropertyKeyConstants.SLACK_NEW_MEMBER_MSG_KEY));
                queuedSlackMessage.setMessageType(MessageType.Slack);
                queuedSlackMessage.setRecipientAddress(getSlackName(member));
                queueMsg(queuedSlackMessage);
            } catch (ResourceNotFoundException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
    }

    /**
     * Sends membership renewal message.
     *
     * @param member Member
     */
    @Override
    public void sendRenewMembershipMsg(final Member member) {
        if (member != null && member.getSlack() != null && member.isSlackEnabled()) {
            try {
                final QueuedMessage queuedSlackMessage = new QueuedMessage();
                queuedSlackMessage.setMemberId(member.getId());
                queuedSlackMessage.setBody(getSMSOrSlackMessage(member,
                        PropertyKeyConstants.SLACK_RENEW_MEMBER_MSG_KEY));
                queuedSlackMessage.setMessageType(MessageType.Slack);
                queuedSlackMessage.setRecipientAddress(getSlackName(member));
                queueMsg(queuedSlackMessage);
            } catch (ResourceNotFoundException ex) {
                LOGGER.error(ex.getMessage());
            }
        }
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
