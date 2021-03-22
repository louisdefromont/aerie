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

import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;
import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

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
     * SimpleDateFormat.
     */
    private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy");

    /**
     * PropertyService.
     */
    private PropertyService propertyService;

    private boolean twilioInitialized = false;

    /**
     * Sets PropertyService.
     *
     * @param value PropertyService
     */
    @Autowired
    public void setPropertyService(final PropertyService value) {
        propertyService = value;
    }

    public void sendNewMembershipMsg(final Member member) {
        if (member.getCellPhone() == null && member.getHomePhone() == null) {
            return;
        }
        try {
            String to = member.getCellPhone() != null ? member.getCellPhone() : member.getHomePhone();
            if (Boolean.valueOf(propertyService.get(PropertyKeyConstants.SMS_TEST_MODE_ENABLED_KEY).getValue())) {
                to = propertyService.get(PropertyKeyConstants.SMS_TEST_MODE_RECIPIENT_KEY).getValue();
            }
            final String qualifier =
                    Boolean.valueOf(propertyService.get(PropertyKeyConstants.SLACK_ENABLED_KEY).getValue()) ?
                            "S" : "Not s";
            LOGGER.info(String.format("%sending new membership SMS message... toAddress [%s];", qualifier, to));
            sendSMSMessage(to, propertyService.get(PropertyKeyConstants.SMS_FROM_ADDRESS_KEY).getValue(),
                    String.format("Welcome %s to EAA 690!", member.getFirstName()));
        } catch (ResourceNotFoundException ex) {
            LOGGER.error(ex.getMessage());
        }
    }

    public void sendRenewMembershipMsg(final Member member) {
        if (member.getCellPhone() == null && member.getHomePhone() == null) {
            return;
        }
        try {
            final String expiration = member.getExpiration() != null ?
                    sdf.format(member.getExpiration()) : sdf.format(new Date());
            String to = member.getCellPhone() != null ? member.getCellPhone() : member.getHomePhone();
            if (Boolean.valueOf(propertyService.get(PropertyKeyConstants.SMS_TEST_MODE_ENABLED_KEY).getValue())) {
                to = propertyService.get(PropertyKeyConstants.SMS_TEST_MODE_RECIPIENT_KEY).getValue();
            }
            final String qualifier =
                    Boolean.valueOf(propertyService.get(PropertyKeyConstants.SLACK_ENABLED_KEY).getValue()) ?
                            "S" : "Not s";
            LOGGER.info(String.format("%sending new membership SMS message... toAddress [%s];", qualifier, to));
            sendSMSMessage(to, propertyService.get(PropertyKeyConstants.SMS_FROM_ADDRESS_KEY).getValue(),
                    String.format("Hi %s, please be sure to renew your EAA 690 chapter membership before %s!",
                    member.getFirstName(), expiration));
        } catch (ResourceNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
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
        if (Boolean.parseBoolean(propertyService.get(PropertyKeyConstants.SMS_ENABLED_KEY).getValue())) {
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
    }

}
