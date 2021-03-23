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
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
     * SimpleDateFormat.
     */
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy (EEEE)");

    /**
     * SendGrid Initialized.
     */
    private boolean sendgridInitialized = false;

    /**
     * PropertyService.
     */
    private PropertyService propertyService;

    /**
     * SendGrid.
     */
    private SendGrid sendGrid;

    /**
     * Sets PropertyService.
     *
     * @param value PropertyService
     */
    @Autowired
    public void setPropertyService(final PropertyService value) {
        propertyService = value;
    }

    /**
     * Sends email to a member.
     *
     * @param member Member to be messaged
     */
    public void sendMsg(final String templateIdKey, final String subjectKey, final Member member) {
        if (member == null || member.getEmail() == null) {
            return;
        }
        if (member.emailEnabled()) {
            try {
                String to = member.getEmail();
                if (Boolean.parseBoolean(
                        propertyService.get(PropertyKeyConstants.EMAIL_TEST_MODE_ENABLED_KEY).getValue())) {
                    to = propertyService.get(PropertyKeyConstants.EMAIL_TEST_MODE_RECIPIENT_KEY).getValue();
                }
                final String qualifier =
                        Boolean.parseBoolean(propertyService.get(PropertyKeyConstants.EMAIL_ENABLED_KEY).getValue()) ?
                                "S" : "Not s";
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
    private Personalization personalize(Member member, String to) throws ResourceNotFoundException {
        final Personalization personalization = new Personalization();
        personalization.addTo(new Email(to));
        personalization.addBcc(new Email(propertyService.get(PropertyKeyConstants.EMAIL_BCC_KEY).getValue()));
        personalization.addDynamicTemplateData("firstName", member.getFirstName());
        personalization.addDynamicTemplateData("lastName", member.getLastName());
        if (member.getExpiration() == null) {
            personalization.addDynamicTemplateData("expirationDate", sdf.format(new Date()));
        } else {
            personalization.addDynamicTemplateData("expirationDate", sdf.format(member.getExpiration()));
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
    private void sendEmail(Mail mail) throws ResourceNotFoundException, IOException {
        if (!sendgridInitialized) {
            sendGrid = new SendGrid(propertyService
                    .get(PropertyKeyConstants.SEND_GRID_EMAIL_API_KEY).getValue());
            sendgridInitialized = true;
        }
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