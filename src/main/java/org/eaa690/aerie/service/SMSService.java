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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eaa690.aerie.communication.CommunicatorService;
import org.eaa690.aerie.communication.Message;
import org.eaa690.aerie.communication.TwilioSMSSender;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * SMSService.
 */
@Service
public class SMSService extends CommunicatorService{

    @Autowired
    public SMSService(TwilioSMSSender messageSender) {
        super(messageSender);
    }

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SMSService.class);

    /**
     * SimpleDateFormat.
     */
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy");

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
     * Sends new member message.
     *
     * @param member Member
     */
    public void sendNewMembershipMsg(final Member member) {
        //"Welcome %s to EAA 690!"
        sendSMSMessage(member, getMessage(member, PropertyKeyConstants.SMS_NEW_MEMBER_MSG_KEY));
    }

    /**
     * Sends membership renewal message.
     *
     * @param member Member
     */
    public void sendRenewMembershipMsg(final Member member) {
        //"Hi %s, please be sure to renew your EAA 690 chapter membership before %s!"
        sendSMSMessage(member, getMessage(member, PropertyKeyConstants.SMS_RENEW_MEMBER_MSG_KEY));
    }

    /**
     * Builds message to be sent to member.
     *
     * @param member Member
     * @param msgKey message key
     * @return message
     */
    private String getMessage(final Member member, final String msgKey) {
        try {
            final String expiration = member.getExpiration() != null ?
                    sdf.format(member.getExpiration()) : sdf.format(new Date());
            return propertyService
                    .get(msgKey)
                    .getValue()
                    .replaceAll("\\{\\{firstName\\}\\}", member.getFirstName())
                    .replaceAll("\\{\\{lastName\\}\\}", member.getLastName())
                    .replaceAll("\\{\\{expirationDate\\}\\}", expiration)
                    .replaceAll("\\{\\{url\\}\\}", jotFormService.buildRenewMembershipUrl(member));
        } catch (ResourceNotFoundException e) {
            LOGGER.error("Error", e);
        }
        return null;
    }

    /**
     * Sends a SMS message.
     *
     * @param to To
     * @param text Message text
     * @throws ResourceNotFoundException when SMS properties are not found
     */
    private void sendSMSMessage(final Member member, final String messageBody) {
        try {
            String to = member.getCellPhone() != null ? member.getCellPhone() : member.getHomePhone();
            if (Boolean.parseBoolean(propertyService.get(PropertyKeyConstants.SMS_TEST_MODE_ENABLED_KEY).getValue())) {
                to = propertyService.get(PropertyKeyConstants.SMS_TEST_MODE_RECIPIENT_KEY).getValue();
            }
            Message message = new Message();
            message.setMessageBody(messageBody);
            message.setRecipientAddress(to);
            message.setRecipientMember(member);
            sendMessage(message);
            
        } catch (ResourceNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

}
