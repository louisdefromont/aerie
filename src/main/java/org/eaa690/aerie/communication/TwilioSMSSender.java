package org.eaa690.aerie.communication;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.service.PropertyService;
import org.springframework.stereotype.Component;

@Component
public class TwilioSMSSender implements MessageSender {

    @Override
    public String sendMessage(String recipientAddress, String messageBody, PropertyService propertyService) {
        try {
            Message createdMessage = Message
                        .creator(new PhoneNumber(recipientAddress),
                                new PhoneNumber(propertyService.get(PropertyKeyConstants.SMS_FROM_ADDRESS_KEY).getValue()),
                                messageBody)
                        .create();

                        return createdMessage.getBody();
        } catch (ResourceNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }
    
}
