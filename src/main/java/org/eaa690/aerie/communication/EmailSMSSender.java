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

package org.eaa690.aerie.communication;

import org.eaa690.aerie.model.communication.Email;
import org.eaa690.aerie.model.communication.SMS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Sends Emails as SMS messages using a Member's phone carrier.
 */
@Component
public class EmailSMSSender extends MessageSender<SMS> {

    /**
     * Email Message Sender used to send SMS messages as Emails.
     */
    private MessageSender<Email> messageSender;

    /**
     * Creates a EmailSMSSender object.
     * @param acceptsMessagePredicate Predicate used to test if a give member accepts SMS messages
     */
    @Autowired
    public EmailSMSSender(final AcceptsSMSPredicate acceptsMessagePredicate) {
        super("SMS_by_Email", acceptsMessagePredicate);
    }

    /**
     * Sets the MessageSender.
     * @param emailSender The MessageSender that sends emails.
     */
    @Autowired
    public void setMessageSender(final SendGridEmailSender emailSender) {
        this.messageSender = emailSender;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String sendMessage(final SMS message) {
        String recipientAddress = String.format(
            "%s@%s",
            message.getRecipientAddress(),
            message.getRecipientMember().getCellPhoneProvider().getCellPhoneProviderEmailDomain());

        Email email = new Email(recipientAddress, message.getRecipientMember(), "eaa", null, message.getBody());

        return messageSender.sendMessage(email);
    }
}
