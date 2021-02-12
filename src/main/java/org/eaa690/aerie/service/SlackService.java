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
import java.util.Map;

import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * SlackService.
 */
@Service("aerieSlackService")
public class SlackService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SlackService.class);

    /**
     * SLACK_DIR.
     */
    public static final String SLACK_DIR = "slack";

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
        /* TODO Implement this!
        final Map<String, Object> model = TemplateUtil.getModel(person, null, null, null, null, null);
        try {
            if (Boolean.parseBoolean(propertyService.get(PropertyKeyConstants.SLACK_ENABLED_KEY).getValue())
                    && EventType.GROUNDSCHOOL.equals(event.getEventType())) {
                final String msg = getTemplatedMessage(model, NotificationConstants.GS_EVENT_UPCOMING);
                final String channel = personService.getNotificationValue(person.getId(), NotificationType.SLACK);
                messageDeliveryService.sendGroundSchoolSlackMessage(msg, channel);
                storeSlackMsgSentMsg(person, event, null, null, msg, NotificationEventType.EVENT_UPCOMING, channel);
            }
        } catch (IOException | TemplateException | ResourceNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
        */
    }

    /**
     * Initializes Freemarker configuration.
     *
     * @throws ResourceNotFoundException when properties are not found
     */
    private void initFreemarkerConfiguration() throws ResourceNotFoundException {
        /* TODO Implement this!
        if (!freemarkerConfigurationInitialized) {
            TemplateLoader templateLoader = new TemplateLoader(
                    propertyService.get(PropertyKeyConstants.NOTIFICATION_GCP_STORAGE_URL_BASE_KEY).getValue(),
                    NotificationType.SLACK);
            freemarkerConfig.setTemplateLoader(templateLoader);
            freemarkerConfigurationInitialized = true;
        }
        */
    }

    /**
     * Gets message based upon provided template.
     *
     * @param model Map<String, Object>
     * @param templateName Template name
     * @return message
     * @throws IOException when an error occurs
     * @throws TemplateException when an error occurs
     * @throws ResourceNotFoundException when properties are not found
     */
    private String getTemplatedMessage(final Map<String, Object> model, final String templateName) throws IOException,
            TemplateException, ResourceNotFoundException {
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
     * Sends a message via the Aerie Slack bot.
     *
     * @param msg message to be sent
     * @param channel Slack channel
     * @throws ResourceNotFoundException when properties are not found
     */
    public void sendAerieSlackMessage(final String msg, final String channel) throws ResourceNotFoundException {
        /* TODO Implement this!
        if (aerieSlackSession == null || !aerieSlackSession.isConnected()) {
            aerieSlackSession = SlackSessionFactory
                    .createWebSocketSlackSession(
                            propertyService.get(PropertyKeyConstants.SLACK_AERIE_TOKEN_KEY).getValue());
            try {
                aerieSlackSession.connect();
                aerieSlackSession.addMessagePostedListener(this);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        aerieSlackSession.sendMessageToUser(channel, msg, null);
        */
    }

    public void sendNewMembershipMsg(final Member member) {
        LOGGER.info(String.format("Sending new membership Slack message... toAddress [%s];", member.getSlack()));
    }
}