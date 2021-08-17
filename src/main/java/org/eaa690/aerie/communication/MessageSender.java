package org.eaa690.aerie.communication;

import org.eaa690.aerie.service.PropertyService;

public interface MessageSender {
    public String sendMessage(final String recipientAddress, final String message, PropertyService propertyService);
}
