package org.eaa690.aerie.communication;

import org.eaa690.aerie.service.PropertyService;

public class EmailSMSSender implements MessageSender {

    private MessageSender messageSender;

    public void setMessageSender(SendGridEmailSender messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    public String sendMessage(String recipientAddress, String message, PropertyService propertyService) {
        return messageSender.sendMessage(recipientAddress, message, propertyService);
    }
    
}
