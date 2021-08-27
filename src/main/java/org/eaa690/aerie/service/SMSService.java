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

import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.MessageType;
import org.eaa690.aerie.model.QueuedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * SMSService.
 */
@Service
public class SMSService extends CommunicationService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SMSService.class);

    /**
     * EmailService.
     */
    @Autowired
    private EmailService emailService;

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
                        .filter(qm -> qm.getMessageType() == MessageType.SMS)
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
     * Sends an SMS message via SendGrid.
     *
     * @param queuedMessage QueuedMessage
     */
    @Override
    public void sendMessage(final QueuedMessage queuedMessage) {
        if (queuedMessage.getMessageType() == MessageType.SMS) {
            final Optional<Member> memberOpt = getMemberRepository().findById(queuedMessage.getMemberId());
            if (memberOpt.isPresent()) {
                final Member member = memberOpt.get();
                if (getAcceptsSMSPredicate().test(member)) {
                    queuedMessage.setRecipientAddress(String.format("%s@%s",
                            queuedMessage.getRecipientAddress(),
                            member.getCellPhoneProvider().getCellPhoneProviderEmailDomain()));
                    emailService.sendMessage(queuedMessage);
                }
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
            final QueuedMessage queuedSMSMessage = new QueuedMessage();
            queuedSMSMessage.setMemberId(member.getId());
            queuedSMSMessage.setBody(getSMSOrSlackMessage(member, PropertyKeyConstants.SMS_NEW_MEMBER_MSG_KEY));
            queuedSMSMessage.setMessageType(MessageType.SMS);
            queuedSMSMessage.setRecipientAddress(member.getCellPhone());
            queueMsg(queuedSMSMessage);
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
            final QueuedMessage queuedSMSMessage = new QueuedMessage();
            queuedSMSMessage.setMemberId(member.getId());
            queuedSMSMessage.setBody(getSMSOrSlackMessage(member, PropertyKeyConstants.SMS_RENEW_MEMBER_MSG_KEY));
            queuedSMSMessage.setMessageType(MessageType.SMS);
            queuedSMSMessage.setRecipientAddress(member.getCellPhone());
            queueMsg(queuedSMSMessage);
        }
    }

}
