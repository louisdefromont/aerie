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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.MemberRepository;
import org.eaa690.aerie.model.QueuedEmail;
import org.eaa690.aerie.model.QueuedEmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;

/**
 * EmailService.
 */
@Service("emailService")
public class EmailService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

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
     * QueuedEmailRepository.
     */
    @Autowired
    private QueuedEmailRepository queuedEmailRepository;

    /**
     * SendGrid.
     */
    @Autowired
    private SendGrid sendGrid;

    /**
     * Count of manually sent messages.
     */
    private long manualMsgSentCount = 0;

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
     * Sets SendGrid.
     * Note: mostly used for unit test mocks
     *
     * @param value SendGrid
     */
    @Autowired
    public void setSendGrid(final SendGrid value) {
        sendGrid = value;
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
    public void setQueuedEmailRepository(final QueuedEmailRepository qeRepository) {
        queuedEmailRepository = qeRepository;
    }

    /**
     * Queues email to be sent.
     *
     * @param templateIdKey template ID
     * @param subjectKey subject
     * @param member Member
     */
    public void queueMsg(final String templateIdKey, final String subjectKey, final Member member) {
        queuedEmailRepository.save(new QueuedEmail(templateIdKey, subjectKey, member.getId()));
    }

    /**
     * Gets the count of the number of queued messages.
     *
     * @return queued message count
     */
    public int getQueuedMsgCount() {
        return queuedEmailRepository.findAll().map(List::size).orElse(0);
    }

    /**
     * Manually increments the message count.
     */
    public void incrementManualMessageCount() {
        manualMsgSentCount++;
    }

    /**
     * Looks for any messages in the send queue, and sends up to X (see configuration) messages per day.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void clearManualMessageCount() {
        manualMsgSentCount = 0;
    }

    /**
     * Looks for any messages in the send queue, and sends up to X (see configuration) messages per day.
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void processQueue() {
        final Optional<List<QueuedEmail>> allQueuedMessages = queuedEmailRepository.findAll();
        if (allQueuedMessages.isPresent()) {
            try {
                final long sendGridLimit = Long.parseLong(
                        propertyService.get(PropertyKeyConstants.SEND_GRID_LIMIT).getValue());
                allQueuedMessages
                        .get()
                        .stream()
                        .limit(sendGridLimit - manualMsgSentCount)
                        .forEach(qe -> {
                            final Optional<Member> memberOpt = memberRepository.findById(qe.getMemberId());
                            if (memberOpt.isPresent()) {
                                sendMsg(qe.getTemplateIdKey(), qe.getSubjectKey(), memberOpt.get());
                                queuedEmailRepository.delete(qe);
                            }
                        });
            } catch (ResourceNotFoundException e) {
                LOGGER.error("Error", e);
            }
        }
    }

    /**
     * Sends email to a member.
     *
     * @param templateIdKey Template ID
     * @param subjectKey Subject
     * @param member Member to be messaged
     */
    public void sendMsg(final String templateIdKey, final String subjectKey, final Member member) {
        if (member != null && member.getEmail() != null && member.isEmailEnabled()) {
            try {
                String to = member.getEmail();
                if (Boolean.parseBoolean(
                        propertyService.get(PropertyKeyConstants.EMAIL_TEST_MODE_ENABLED_KEY).getValue())) {
                    to = propertyService.get(PropertyKeyConstants.EMAIL_TEST_MODE_RECIPIENT_KEY).getValue();
                }
                String qualifier = "Not s";
                if (Boolean.parseBoolean(propertyService.get(PropertyKeyConstants.EMAIL_ENABLED_KEY).getValue())) {
                    qualifier = "S";
                }
                LOGGER.info(String.format("%sending email... toAddress [%s];", qualifier, to));
                final Mail mail = new Mail();
                mail.setSubject(propertyService.get(subjectKey).getValue());
                mail.setTemplateId(propertyService.get(templateIdKey).getValue());
                mail.setFrom(new Email(propertyService
                        .get(PropertyKeyConstants.SEND_GRID_FROM_ADDRESS_KEY).getValue()));
                mail.addPersonalization(personalize(member, to));
                sendEmail(mail);
            } catch (IOException | ResourceNotFoundException ex) {
                LOGGER.error(ex.getMessage());
            }
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
    private Personalization personalize(final Member member, final String to) throws ResourceNotFoundException {
        final Personalization personalization = new Personalization();
        personalization.addTo(new Email(to));
        personalization.addBcc(new Email(propertyService.get(PropertyKeyConstants.EMAIL_BCC_KEY).getValue()));
        personalization.addDynamicTemplateData("firstName", member.getFirstName());
        personalization.addDynamicTemplateData("lastName", member.getLastName());
        personalization.addDynamicTemplateData("url", jotFormService.buildRenewMembershipUrl(member));
        if (member.getExpiration() == null) {
            personalization.addDynamicTemplateData("expirationDate",
                    ZonedDateTime.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        } else {
            personalization.addDynamicTemplateData("expirationDate",
                    ZonedDateTime.ofInstant(member.getExpiration().toInstant(),
                    ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
        }
        return personalization;
    }

    /**
     * Sends email to member.
     *
     * @param mail message
     * @throws ResourceNotFoundException when property is not found
     * @throws IOException upon message delivery failure
     */
    private void sendEmail(final Mail mail) throws ResourceNotFoundException, IOException {
        final Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        if (Boolean.parseBoolean(propertyService.get(PropertyKeyConstants.EMAIL_ENABLED_KEY).getValue())) {
            final Response response = sendGrid.api(request);
            LOGGER.info(String.format("Response... statusCode [%s]; body [%s]; headers [%s]",
                    response.getStatusCode(), response.getBody(), response.getHeaders()));
        } else {
            LOGGER.info("Not sending email due to enabled flag set to false.");
        }
    }

}
