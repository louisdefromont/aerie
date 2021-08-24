package org.eaa690.aerie.communication;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.communication.SMS;
import org.eaa690.aerie.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TwilioSMSSender extends MessageSender<SMS> {

    @Autowired
    PropertyService propertyService;

    @Autowired
    public TwilioSMSSender(AcceptsSMSPredicate acceptsMessagePredicate) {
        super("Twilio_SMS", acceptsMessagePredicate);
    }

    @Override
    public String sendMessage(SMS message) {
        try {
            Message createdMessage = Message
                        .creator(new PhoneNumber(message.getRecipientAddress()),
                                new PhoneNumber(propertyService.get(PropertyKeyConstants.SMS_FROM_ADDRESS_KEY).getValue()),
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
