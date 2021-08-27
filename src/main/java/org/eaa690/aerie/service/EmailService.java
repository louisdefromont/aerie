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

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.MessageType;
import org.eaa690.aerie.model.QueuedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * EmailService.
 */
@Service
public class EmailService extends CommunicationService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

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
                        .filter(qm -> qm.getMessageType() == MessageType.Email)
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
     * Sends an Email message via SendGrid.
     *
     * @param queuedMessage QueuedMessage
     */
    @Override
    public void sendMessage(final QueuedMessage queuedMessage) {
        if (queuedMessage.getMessageType() == MessageType.Email) {
            try {
                final Optional<Member> memberOpt = getMemberRepository().findById(queuedMessage.getMemberId());
                if (memberOpt.isPresent()) {
                    final Member member = memberOpt.get();
                    if (getAcceptsEmailPredicate().test(member)) {
                        Email from = new Email(getPropertyService().get(PropertyKeyConstants.SEND_GRID_FROM_ADDRESS_KEY)
                                .getValue());
                        String subject = getPropertyService().get(queuedMessage.getSubjectKey()).getValue();
                        Email to = new Email(queuedMessage.getRecipientAddress());
                        if (Boolean.parseBoolean(getPropertyService()
                                .get(PropertyKeyConstants.EMAIL_TEST_MODE_ENABLED_KEY).getValue())) {
                            to = new Email(getPropertyService().get(PropertyKeyConstants.EMAIL_TEST_MODE_RECIPIENT_KEY)
                                    .getValue());
                        }

                        final Mail mail;
                        if (queuedMessage.getTemplateIdKey() != null) {
                            mail = new Mail();
                            mail.setSubject(subject);
                            mail.setTemplateId(getPropertyService().get(queuedMessage.getTemplateIdKey()).getValue());
                            mail.addPersonalization(personalize(member, to));
                            mail.setFrom(from);
                        } else {
                            mail = new Mail(from, subject, to, new Content("text/plain",
                                    queuedMessage.getBody()));
                        }

                        final Request request = new Request();
                        request.setMethod(Method.POST);
                        request.setEndpoint("mail/send");
                        request.setBody(mail.build());
                        Response response = getSendGrid().api(request);
                        LOGGER.info(String.format("Response... statusCode [%s]; body [%s]; headers [%s]",
                                response.getStatusCode(), response.getBody(), response.getHeaders()));
                    }
                }
            } catch (IOException | ResourceNotFoundException ex) {
                LOGGER.error(ex.getMessage());
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
        if (getAcceptsEmailPredicate().test(member)) {
            final QueuedMessage queuedEmailMessage = new QueuedMessage();
            queuedEmailMessage.setMemberId(member.getId());
            queuedEmailMessage.setSubjectKey(PropertyKeyConstants.SEND_GRID_NEW_MEMBERSHIP_EMAIL_SUBJECT_KEY);
            queuedEmailMessage.setTemplateIdKey(PropertyKeyConstants.SEND_GRID_NEW_MEMBERSHIP_EMAIL_TEMPLATE_ID);
            queuedEmailMessage.setRecipientAddress(member.getEmail());
            queuedEmailMessage.setMessageType(MessageType.Email);
            queueMsg(queuedEmailMessage);
        }
    }

    /**
     * Sends membership renewal message.
     *
     * @param member Member
     */
    @Override
    public void sendRenewMembershipMsg(final Member member) {
        if (getAcceptsEmailPredicate().test(member)) {
            final QueuedMessage queuedEmailMessage = new QueuedMessage();
            queuedEmailMessage.setMemberId(member.getId());
            queuedEmailMessage.setSubjectKey(PropertyKeyConstants.SEND_GRID_NEW_MEMBERSHIP_EMAIL_SUBJECT_KEY);
            queuedEmailMessage.setTemplateIdKey(PropertyKeyConstants.SEND_GRID_NEW_MEMBERSHIP_EMAIL_TEMPLATE_ID);
            queuedEmailMessage.setRecipientAddress(member.getEmail());
            queuedEmailMessage.setMessageType(MessageType.Email);
            queueMsg(queuedEmailMessage);
        }
    }

    /**
     * Personalizes an email to the member.
     *
     * @param member Member
     * @param to address
     * @return Personalization
     * @throws ResourceNotFoundException when property is not found
     */
    private Personalization personalize(final Member member, final Email to) throws ResourceNotFoundException {
        final Personalization personalization = new Personalization();
        personalization.addTo(to);
        personalization.addBcc(new Email(getPropertyService().get(PropertyKeyConstants.EMAIL_BCC_KEY).getValue()));
        personalization.addDynamicTemplateData("firstName", member.getFirstName());
        personalization.addDynamicTemplateData("lastName", member.getLastName());
        personalization.addDynamicTemplateData("url", getJotFormService().buildRenewMembershipUrl(member));
        if (member.getExpiration() == null) {
            personalization.addDynamicTemplateData("expirationDate", ZonedDateTime.ofInstant(Instant.now(),
                    ZoneId.systemDefault()).format(
                    DateTimeFormatter.ofPattern("MMM d, yyyy")));
        } else {
            personalization.addDynamicTemplateData("expirationDate", ZonedDateTime.ofInstant(
                    member.getExpiration().toInstant(),
                    ZoneId.systemDefault()).format(
                    DateTimeFormatter.ofPattern("MMM d, yyyy")));
        }
        return personalization;
    }

}
