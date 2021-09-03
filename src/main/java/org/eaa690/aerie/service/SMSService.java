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

import org.eaa690.aerie.communication.EmailSMSSender;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.communication.SMS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * SMSService.
 */
@Service
public class SMSService extends CommunicationService<SMS> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SMSService.class);

    /**
     * EmailService.
     */
    @Autowired
    private EmailService emailService;

    @Autowired
    public SMSService(final EmailSMSSender messageSender) {
        super(messageSender);
    }

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
    @Scheduled(cron = "0 0 10 * * *")
    public void processQueue() {
        try {
            // Currently shares message rates with emails but doesn't take that into account!
            super.processQueue(Long.parseLong(
                getPropertyService().get(PropertyKeyConstants.SEND_GRID_LIMIT).getValue()));
        } catch (NumberFormatException e) {
            LOGGER.error("ERROR", e);
        } catch (ResourceNotFoundException e) {
            LOGGER.error("ERROR", e);
        }
    }

    /**
     * Sends new member message.
     *
     * @param member Member
     */
    @Override
    public SMS buildNewMembershipMsg(final Member member) {
        final SMS newMembershipMessage = new SMS(
            member.getCellPhone(),
            member.getId(),
            getSMSOrSlackMessage(member, PropertyKeyConstants.SMS_NEW_MEMBER_MSG_KEY));
        return newMembershipMessage;
    }

    /**
     * Sends membership renewal message.
     *
     * @param member Member
     */
    @Override
    public SMS buildRenewMembershipMsg(final Member member) {
        final SMS newMembershipMessage = new SMS(
            member.getCellPhone(),
            member.getId(),
            getSMSOrSlackMessage(member, PropertyKeyConstants.SMS_NEW_MEMBER_MSG_KEY));
        return newMembershipMessage;
    }

}
