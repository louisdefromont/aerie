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
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.eaa690.aerie.communication.MessageSender;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.MemberRepository;
import org.eaa690.aerie.model.communication.Message;
import org.eaa690.aerie.model.communication.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.Getter;

/**
 * CommuincationService.
 * @param <T> Type of Message sent by this Commuinication Service.
 */
@Getter
public abstract class CommunicationService<T extends Message> {

    /**
     * MessageSender used to send Messages.
     */
    private MessageSender<T> messageSender;
    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicationService.class);

    /**
     * PropertyService.
     */
    @Autowired
    private PropertyService propertyService;

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
     * MessageRepository.
     */
    @Autowired
    private MessageRepository<T> messageRepository;

    /**
     * CommunicationService.
     * @param messageSenderInput MessageSender
     */
    public CommunicationService(final MessageSender<T> messageSenderInput) {
        this.messageSender = messageSenderInput;
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
     * @param input QueuedEmailRepository
     */
    @Autowired
    public void setMessageRepository(final MessageRepository<T> input) {
        messageRepository = input;
    }

    /**
     * Gets acceptsMessagePredicate.
     * @return acceptsMessagePredicate.
     */
    public Predicate<Member> getAcceptsMessagePredicate() {
        return getMessageSender().getAcceptsMessagePredicate();
    }

    /**
     * Queues message to be sent.  Slack only messages are sent immediately.
     *
     * @param message QueuedMessage
     * @throws ResourceNotFoundException
     */
    public void queueMsg(final T message) throws ResourceNotFoundException {
        Optional<Member> foundMember = memberRepository.findById(message.getRecipientMemberId());
        if (foundMember.isPresent()) {
            Member recipientMember = foundMember.get();
            if (messageSender.getAcceptsMessagePredicate().test(recipientMember)) {
                LOGGER.info(String.format(
                    "Queuing %s to member of id: %d",
                    messageSender.getMessageType(),
                    recipientMember.getId()));
                    messageRepository.save(message);
            } else {
                LOGGER.info(
                    String.format(
                        "The specified memeber is not accepting messages of type %s",
                        messageSender.getMessageType()));
            }
        } else {
            throw new ResourceNotFoundException(
                String.format("No member found with id %d", message.getRecipientMemberId()));
        }
    }

    /**
     * Gets the count of the number of queued messages.
     *
     * @return queued message count
     */
    public int getQueuedMsgCount() {
        return messageRepository.findAll().map(List::size).orElse(0);
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
     * Sends a message with the configured MessageSender.
     * @param message The message to be sent
     * @return response from the MessageSender sending the message.
     * @throws ResourceNotFoundException if no Member is found associated with the message.
     */
    public String sendMessage(final T message) throws ResourceNotFoundException {
        String response = null;

        Optional<Member> foundMember = memberRepository.findById(message.getRecipientMemberId());
        if (foundMember.isPresent()) {
            Member recipientMember = foundMember.get();
            if (messageSender.getAcceptsMessagePredicate().test(recipientMember)) {
                LOGGER.info(String.format(
                    "Sending %s to member of id: %d",
                    messageSender.getMessageType(),
                    recipientMember.getId()));
                    response = messageSender.sendMessage(message, recipientMember);
                    LOGGER.info(response);
            } else {
                LOGGER.info(
                    String.format(
                        "The specified memeber is not accepting messages of type %s",
                        messageSender.getMessageType()));
            }
        } else {
            throw new ResourceNotFoundException(
                String.format("No member found with id %d", message.getRecipientMemberId()));
        }
        return response;
    }

    /**
     * Sends new member message.
     *
     * @param member Member
     * @return Message for new members.
     */
    public abstract T buildNewMembershipMsg(Member member);

    /**
     * Sends membership renewal message.
     *
     * @param member Member
     * @return Message to remind members to renew membership.
     */
    public abstract T buildRenewMembershipMsg(Member member);

    /**
     * Looks for any messages in the send queue, and sends up to X (see configuration) messages per day.
     * @param maxMessagesSent The maximum amount of messages to be sent.
     */
    public void processQueue(final Long maxMessagesSent) {
        final Optional<List<T>> allQueuedMessages = messageRepository.findAll();
        if (allQueuedMessages.isPresent()) {
            allQueuedMessages
                    .get()
                    .stream()
                    .limit(maxMessagesSent)
                    .forEach(queuedMessage -> {
                        try {
                            sendMessage(queuedMessage);
                        } catch (ResourceNotFoundException e) {
                            LOGGER.error("ERROR", e);
                        }
                        messageRepository.delete(queuedMessage);
                    });
        }
    }

}
