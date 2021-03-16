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
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;

import org.eaa690.aerie.constant.CommonConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.SMSMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * SMSService.
 */
@Service
public class SMSService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SMSService.class);

    /**
     * SMS_DIR.
     */
    public static final String SMS_DIR = "sms";

    /**
     * PropertyService.
     */
    private PropertyService propertyService;

    /**
     * FreeMarker Configuration.
     */
    private Configuration freemarkerConfig;

    /**
     * Flag to indicate if Freemarker Configuration has been initialized.
     */
    private boolean freemarkerConfigurationInitialized = false;

    /**
     * Sets Configuration.
     *
     * @param value Configuration
     */
    @Autowired
    public void setFreemarkerConfiguration(final Configuration value) {
        freemarkerConfig = value;
    }

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
        LOGGER.info(String.format("Sending membership renewal SMS... toAddress [%s];", member.getCellPhone()));
        /* TODO: Implement this!
        final Map<String, Object> model = TemplateUtil.getModel(person, null, null, null, null, null);
        try {
            final Message message = buildMessage(
                    person,
                    NotificationEventType.USER_DELETE,
                    getTemplatedMessage(model, NotificationConstants.USER_DELETE));
            messageDeliveryService.sendSMSMessage(message.getTo(), message.getFrom(), message.getText());
            storeMsgSentMsg(message);
        } catch (IOException | TemplateException | ResourceNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
        */
    }

    /**
     * {@inheritDoc} Required implementation.
     */
    public String receiveMessage(final SMSMessage message) {
        /* TODO Implement this!
        String response = null;
        try {
            ResponseValidator.validate(message.getBody());
        } catch (InvalidPayloadException e) {
            return response;
        }

        messageProcessorService
                .processUserResponse(stripCountryCode(message.getFrom()), message.getBody(), NotificationType.SMS);
        return response;
        */
        return null;
    }

    /**
     * Strips the country code from a phone number.
     *
     * @param from number
     * @return phone number minus country code
     */
    private static String stripCountryCode(final String from) {
        if (from == null || from.length() == CommonConstants.TEN) {
            return from;
        }
        String response = from;
        Matcher matcher = CommonConstants.TN_PATTERN.matcher(from);
        if (matcher.find()) {
            response = matcher.group(1);
        }
        return response;
    }

    /**
     * Gets message based upon provided template.
     *
     * @param model Map<String, Object>
     * @param templateName Template name
     * @return message
     * @throws IOException when an error occurs
     * @throws TemplateException when an error occurs
     */
    private String getTemplatedMessage(final Map<String, Object> model, final String templateName) throws IOException,
            TemplateException {
        /* TODO: Implement this!
        initFreemarkerConfiguration();
        return FreeMarkerTemplateUtils
                .processTemplateIntoString(
                        freemarkerConfig
                                .getTemplate(templateName + NotificationConstants.FTL_FILE_EXTENSION),
                        model);
        */
        return null;
    }

    /**
     * Initializes Freemarker Configuration.
     */
    private void initFreemarkerConfiguration() {
        /* TODO: Implement this!
        if (!freemarkerConfigurationInitialized) {
            try {
                TemplateLoader templateLoader = new TemplateLoader(
                        propertyService.get(PropertyKeyConstants.NOTIFICATION_GCP_STORAGE_URL_BASE_KEY).getValue(),
                        NotificationType.EMAIL);
                freemarkerConfig.setTemplateLoader(templateLoader);
                freemarkerConfigurationInitialized = true;
            } catch (ResourceNotFoundException rnfe) {
                LOGGER.error(rnfe.getMessage(), rnfe);
            }
        }
        */
    }

    /**
     * Builds a Message.
     *
     * @param person Person
     * @param notificationEventType NotificationEventType
     * @param messageText Message text
     * @return Message
     * @throws ResourceNotFoundException when SMS address is not found
     */
    private void buildMessage(/*
            final Person person,
            final NotificationEventType notificationEventType,
            final String messageText */) throws ResourceNotFoundException {
        /* TODO: Implement this!
        final Message message = new Message();
        message.setPersonId(person.getId());
        message.setTo(personService.getNotificationValue(person.getId(), NotificationType.SMS));
        message.setFrom(propertyService.get(PropertyKeyConstants.SMS_FROM_ADDRESS_KEY).getValue());
        message.setTime(new Date());
        message.setNotificationEventType(notificationEventType);
        message.setText(messageText);
        return message;
        */
    }

    /**
     * Sends a SMS message.
     *
     * @param to To
     * @param from From
     * @param text Message text
     * @throws ResourceNotFoundException when SMS properties are not found
     */
    public void sendSMSMessage(final String to, final String from, final String text) throws ResourceNotFoundException {
        /* TODO: Implement this!
        if (Boolean.parseBoolean(propertyService.get(PropertyKeyConstants.SMS_ENABLED).getValue())) {
            if (!twilioInitialized) {
                Twilio
                        .init(propertyService.get(PropertyKeyConstants.SMS_ACCOUNT_SID_KEY).getValue(),
                                propertyService.get(PropertyKeyConstants.SMS_AUTH_ID_KEY).getValue());
                twilioInitialized = true;
            }
            com.twilio.rest.api.v2010.account.Message
                    .creator(new PhoneNumber(to), new PhoneNumber(from), text)
                    .create();
        }
        */
    }

    public void sendNewMembershipMsg(final Member member) {
        LOGGER.info(String.format("Sending new membership SMS... toAddress [%s];", member.getCellPhone()));
    }
}
