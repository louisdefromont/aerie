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

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.communication.SMS;
import org.eaa690.aerie.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * TwilioSMSSender.
 */
@Component
public class TwilioSMSSender extends MessageSender<SMS> {

    /**
     * PropertyService.
     */
    @Autowired
    private PropertyService propertyService;

    /**
     * TwilioSMSSender.
     * @param acceptsMessagePredicate {@inheritDoc}
     */
    @Autowired
    public TwilioSMSSender(final AcceptsSMSPredicate acceptsMessagePredicate) {
        super("Twilio_SMS", acceptsMessagePredicate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String sendMessage(final SMS message) {
        try {
            Message createdMessage = Message
                        .creator(new PhoneNumber(message.getRecipientAddress()),
                                new PhoneNumber(
                                    propertyService.get(PropertyKeyConstants.SMS_FROM_ADDRESS_KEY).getValue()),
                                message.getBody())
                        .create();

                        return createdMessage.getBody();
        } catch (ResourceNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }
}
