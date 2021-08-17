package org.eaa690.aerie.service;

import org.eaa690.aerie.communication.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class CommunicatorService {
    private MessageSender messageSender;

    public CommunicatorService(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public String sendMessage(String recipientAddress, String message, PropertyService propertyService) {
        return messageSender.sendMessage(recipientAddress, message, propertyService);
    }
}
