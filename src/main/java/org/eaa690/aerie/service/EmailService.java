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

import com.sendgrid.helpers.mail.objects.Email;

import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.communication.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.cucumber.messages.internal.com.google.protobuf.Extension.MessageType;


/**
 * EmailService.
 */
@Service
public class EmailService extends CommunicationService<org.eaa690.aerie.model.communication.Email> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    /**
     * {@inheritDoc}
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void processQueue() {
        try {
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
    public org.eaa690.aerie.model.communication.Email buildNewMembershipMsg(final Member member) {
        if (getAcceptsEmailPredicate().test(member)) {

            final org.eaa690.aerie.model.communication.Email newMembershipEmail =
                new org.eaa690.aerie.model.communication.Email(
                    member.getEmail(),
                    member.getId(),
                    PropertyKeyConstants.SEND_GRID_NEW_MEMBERSHIP_EMAIL_SUBJECT_KEY,
                    PropertyKeyConstants.SEND_GRID_NEW_MEMBERSHIP_EMAIL_TEMPLATE_ID,
                    null);
            return newMembershipEmail;
        }
        return null;
    }

    /**
     * Sends membership renewal message.
     *
     * @param member Member
     */
    @Override
    public org.eaa690.aerie.model.communication.Email buildRenewMembershipMsg(final Member member) {
        if (getAcceptsMessagePredicate().test(member)) {
            final org.eaa690.aerie.model.communication.Email renewMembershipEmail =
                new org.eaa690.aerie.model.communication.Email(
                    member.getEmail(),
                    member.getId(),
                    PropertyKeyConstants.SEND_GRID_FIRST_MEMBERSHIP_RENEWAL_EMAIL_SUBJECT_KEY,
                    PropertyKeyConstants.SEND_GRID_FIRST_MEMBERSHIP_RENEWAL_EMAIL_TEMPLATE_ID,
                    null);
            return renewMembershipEmail;
        }
        return null;
    }

}
