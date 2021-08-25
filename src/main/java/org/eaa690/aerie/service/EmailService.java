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

import java.util.List;
import java.util.Optional;

import org.eaa690.aerie.communication.CommunicatorService;
import org.eaa690.aerie.communication.SendGridEmailSender;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.MemberRepository;
import org.eaa690.aerie.model.QueuedEmail;
import org.eaa690.aerie.model.QueuedEmailRepository;
import org.eaa690.aerie.model.communication.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * EmailService.
 */
@Service("emailService")
public class EmailService extends CommunicatorService<Email> {

    @Autowired
    public EmailService(SendGridEmailSender messageSender) {
        super(messageSender);
    }

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
                                try {
                                    Email email = new Email(
                                        memberOpt.get().getEmail(), 
                                        memberOpt.get(), 
                                        propertyService.get(qe.getSubjectKey()).getValue(), 
                                        propertyService.get(qe.getTemplateIdKey()).getValue(), null);
                                    sendMessage(email);

                                } catch (ResourceNotFoundException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }

                                queuedEmailRepository.delete(qe);
                            }
                        });
            } catch (ResourceNotFoundException e) {
                LOGGER.error("Error", e);
            }
        }
    }

}
