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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.sendgrid.SendGrid;
import com.ullink.slack.simpleslackapi.SlackSession;
import lombok.Getter;
import org.eaa690.aerie.communication.AcceptsEmailPredicate;
import org.eaa690.aerie.communication.AcceptsSMSPredicate;
import org.eaa690.aerie.communication.AcceptsSlackPredicate;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.MemberRepository;
import org.eaa690.aerie.model.QueuedMessage;
import org.eaa690.aerie.model.QueuedMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * CommunicationService.
 */
@Getter
public abstract class CommunicationService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationService.class);

    /**
     * SendGrid API.
     */
    @Autowired
    private SendGrid sendGrid;

    /**
     * PropertyService.
     */
    @Autowired
    private PropertyService propertyService;

    /**
     * SlackSession.
     */
    @Autowired
    private SlackSession slackSession;

    /**
     * JotFormService.
     */
    @Autowired
    private JotFormService jotFormService;

    /**
     * MemberRepository.
     */
    @Autowired
    private MemberRepository memberRepository;

    /**
     * QueuedMessageRepository.
     */
    @Autowired
    private QueuedMessageRepository queuedMessageRepository;

    /**
     * AcceptsEmailPredicate.
     */
    @Autowired
    private AcceptsEmailPredicate acceptsEmailPredicate;

    /**
     * AcceptsSMSPredicate.
     */
    @Autowired
    private AcceptsSMSPredicate acceptsSMSPredicate;

    /**
     * AcceptsSlackPredicate.
     */
    @Autowired
    private AcceptsSlackPredicate acceptsSlackPredicate;

    /**
     * Sets SlackSession.
     * Note: mostly used for unit test mocks
     *
     * @param value SlackSession
     */
    @Autowired
    public void setSlackSession(final SlackSession value) {
        slackSession = value;
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
     * Sets JotFormService.
     * Note: mostly used for unit test mocks
     *
     * @param value JotFormService
     */
    @Autowired
    public void setJotFormService(final JotFormService value) {
        jotFormService = value;
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
     * Sets QueuedEmailRepository.
     * Note: mostly used for unit test mocks
     *
     * @param qeRepository QueuedEmailRepository
     */
    @Autowired
    public void setQueuedMessageRepository(final QueuedMessageRepository qeRepository) {
        queuedMessageRepository = qeRepository;
    }

    /**
     * Queues message to be sent.  Slack only messages are sent immediately.
     *
     * @param queuedMessage QueuedMessage
     */
    public void queueMsg(final QueuedMessage queuedMessage) {
        queuedMessageRepository.save(queuedMessage);
    }

    /**
     * Gets the count of the number of queued messages.
     *
     * @return queued message count
     */
    public int getQueuedMsgCount() {
        return queuedMessageRepository.findAll().map(List::size).orElse(0);
    }

    /**
     * Looks for any messages in the send queue, and sends up to X (see configuration) messages per day.
     */
    public abstract void processQueue();

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
     * Builds message to be sent to member.
     *
     * @param member Member
     * @param msgKey message key
     * @return message
     */
    protected String getSMSOrSlackMessage(final Member member, final String msgKey) {
        try {
            final String expiration;
            if (member.getExpiration() != null) {
                expiration = ZonedDateTime.ofInstant(member.getExpiration().toInstant(),
                        ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
            } else {
                expiration = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
            }
            return propertyService
                    .get(msgKey)
                    .getValue()
                    .replaceAll("\\{\\{firstName\\}\\}", member.getFirstName())
                    .replaceAll("\\{\\{lastName\\}\\}", member.getLastName())
                    .replaceAll("\\{\\{expirationDate\\}\\}", expiration)
                    .replaceAll("\\{\\{url\\}\\}", jotFormService.buildRenewMembershipUrl(member));
        } catch (ResourceNotFoundException e) {
            LOGGER.error("Error", e);
        }
        return null;
    }

    /**
     * Sends a message.
     *
     * @param queuedMessage QueuedMessage
     */
    public abstract void sendMessage(QueuedMessage queuedMessage);

    /**
     * Sends new member message.
     *
     * @param member Member
     */
    public abstract void sendNewMembershipMsg(Member member);

    /**
     * Sends membership renewal message.
     *
     * @param member Member
     */
    public abstract void sendRenewMembershipMsg(Member member);

}
