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

import com.sendgrid.SendGrid;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
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
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

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
     * Flag to indicate if Freemarker Configuration has been initialized.
     */
    private boolean freemarkerConfigurationInitialized = false;

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
     * {@inheritDoc} Required implementation.
     */
    public void sendRenewMembershipMsg(final Member member) {
        Response response = null;
        try {
            String to = member.getEmail();
            if (Boolean.valueOf(propertyService.get(PropertyKeyConstants.EMAIL_TEST_MODE_ENABLED_KEY).getValue())) {
                to = propertyService.get(PropertyKeyConstants.EMAIL_TEST_MODE_RECIPIENT_KEY).getValue();
            }
            final String qualifier = Boolean.valueOf(propertyService.get(PropertyKeyConstants.EMAIL_ENABLED_KEY).getValue()) ? "S" : "Not s";
            LOGGER.info(String.format("%sending membership renewal email... toAddress [%s];", qualifier, to));
            final Mail mail =
                    new Mail(new Email(propertyService.get(PropertyKeyConstants.SEND_GRID_FROM_ADDRESS_KEY).getValue()),
                    propertyService.get(PropertyKeyConstants.SEND_GRID_MEMBERSHIP_RENEWAL_EMAIL_SUBJECT_KEY).getValue(),
                            new Email(to), new Content("text/html", ""));
            mail.personalization.get(0).addSubstitution("-firstName-", member.getFirstName());
            mail.personalization.get(0).addSubstitution("-lastName-", member.getLastName());
            mail.personalization.get(0).addSubstitution("-expirationDate-", sdf.format(member.getExpiration()));
            mail.setTemplateId(propertyService.get(
                    PropertyKeyConstants.SEND_GRID_MEMBERSHIP_RENEWAL_EMAIL_TEMPLATE_ID).getValue());

            if (!sendgridInitialized) {
                sendGrid = new SendGrid(propertyService.get(PropertyKeyConstants.SEND_GRID_EMAIL_API_KEY).getValue());
                sendgridInitialized = true;
            }
            final Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            if (Boolean.valueOf(propertyService.get(PropertyKeyConstants.EMAIL_ENABLED_KEY).getValue())) {
                response = sendGrid.api(request);
                LOGGER.info(String.format("Response... statusCode [%s]; body [%s]; headers [%s]",
                        response.getStatusCode(), response.getBody(), response.getHeaders()));
            } else {
                LOGGER.info("Not sending email due to enabled flag set to false.");
            }
        } catch (IOException | ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public void sendNewMembershipMsg(final Member member) {
        Response response = null;
        try {
            String to = member.getEmail();
            if (Boolean.valueOf(propertyService.get(PropertyKeyConstants.EMAIL_TEST_MODE_ENABLED_KEY).getValue())) {
                to = propertyService.get(PropertyKeyConstants.EMAIL_TEST_MODE_RECIPIENT_KEY).getValue();
            }
            final String qualifier = Boolean.valueOf(propertyService.get(PropertyKeyConstants.EMAIL_ENABLED_KEY).getValue()) ? "S" : "Not s";
            LOGGER.info(String.format("%sending new membership email... toAddress [%s];", qualifier, to));
            final Mail mail =
                    new Mail(new Email(propertyService.get(PropertyKeyConstants.SEND_GRID_FROM_ADDRESS_KEY).getValue()),
                            propertyService.get(PropertyKeyConstants.SEND_GRID_NEW_MEMBERSHIP_EMAIL_SUBJECT_KEY).getValue(),
                            new Email(to), new Content("text/html", ""));
            mail.personalization.get(0).addSubstitution("-firstName-", member.getFirstName());
            mail.personalization.get(0).addSubstitution("-lastName-", member.getLastName());
            mail.personalization.get(0).addSubstitution("-expirationDate-", sdf.format(member.getExpiration()));
            mail.setTemplateId(propertyService.get(
                    PropertyKeyConstants.SEND_GRID_NEW_MEMBERSHIP_EMAIL_TEMPLATE_ID).getValue());

            if (!sendgridInitialized) {
                sendGrid = new SendGrid(propertyService.get(PropertyKeyConstants.SEND_GRID_EMAIL_API_KEY).getValue());
                sendgridInitialized = true;
            }
            final Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            if (Boolean.valueOf(propertyService.get(PropertyKeyConstants.EMAIL_ENABLED_KEY).getValue())) {
                response = sendGrid.api(request);
                LOGGER.info(String.format("Response... statusCode [%s]; body [%s]; headers [%s]",
                        response.getStatusCode(), response.getBody(), response.getHeaders()));
            } else {
                LOGGER.info("Not sending email due to enabled flag set to false.");
            }
        } catch (IOException | ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage());
        }
    }
}