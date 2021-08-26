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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;

import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.service.JotFormService;
import org.eaa690.aerie.service.PropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Sends Emails through SendGrid.
 */
@Component
public class SendGridEmailSender extends MessageSender<org.eaa690.aerie.model.communication.Email> {

    /**
     * SendGridEmailSender.
     * @param acceptsMessagePredicate {@inheritDoc}
     */
    @Autowired
    public SendGridEmailSender(final AcceptsEmailPredicate acceptsMessagePredicate) {
        super("Send_Grid_Email", acceptsMessagePredicate);
    }

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
     * Logger.
     */
    private Logger logger = LoggerFactory.getLogger(SendGridEmailSender.class);

    /**
     * JotFormService.
     */
    @Autowired
    private JotFormService jotFormService;

    /**
     * SimpleDateFormat.
     */
    private static final SimpleDateFormat SDF = new SimpleDateFormat("MMM d, yyyy");

    /**
     * {@inheritDoc}
     */
    public String sendMessage(final org.eaa690.aerie.model.communication.Email message) {
        try {
            Email from = new Email(propertyService.get(PropertyKeyConstants.SEND_GRID_FROM_ADDRESS_KEY).getValue());
            String subject = message.getSubject();
            Email to = new Email(message.getRecipientAddress());
            if (Boolean.parseBoolean(
                    propertyService.get(PropertyKeyConstants.EMAIL_TEST_MODE_ENABLED_KEY).getValue())) {
                to = new Email(propertyService.get(PropertyKeyConstants.EMAIL_TEST_MODE_RECIPIENT_KEY).getValue());
            }

            final Mail mail;
            if (message.getTemplateID() != null) {
                mail = new Mail();
                mail.setSubject(subject);
                mail.setTemplateId(message.getTemplateID());
                mail.addPersonalization(personalize(message.getRecipientMember(), to));
                mail.setFrom(from);
            } else {
                mail = new Mail(from, subject, to, new Content("text/plain", message.getBody()));
            }

            final Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);

            return String.format("Response... statusCode [%s]; body [%s]; headers [%s]",
            response.getStatusCode(), response.getBody(), response.getHeaders());

        } catch (IOException | ResourceNotFoundException ex) {
            logger.error(ex.getMessage());
            return null;
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
        personalization.addBcc(new Email(propertyService.get(PropertyKeyConstants.EMAIL_BCC_KEY).getValue()));
        personalization.addDynamicTemplateData("firstName", member.getFirstName());
        personalization.addDynamicTemplateData("lastName", member.getLastName());
        personalization.addDynamicTemplateData("url", jotFormService.buildRenewMembershipUrl(member));
        if (member.getExpiration() == null) {
            personalization.addDynamicTemplateData("expirationDate", SDF.format(new Date()));
        } else {
            personalization.addDynamicTemplateData("expirationDate", SDF.format(member.getExpiration()));
        }
        return personalization;
    }
}
