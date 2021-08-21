package org.eaa690.aerie.communication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CommunicatorService {
    private MessageSender messageSender;

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicatorService.class);

    public CommunicatorService(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public String sendMessage(Message message) {
        String response = null;
        if (messageSender.getAcceptsMessagePredicate().test(message.getRecipientMember())) {
            LOGGER.info(String.format("Sending %s to member of id: %d", messageSender.getMessageType(), message.getRecipientMember().getId()));
            response = messageSender.sendMessage(message);
            LOGGER.info(response);
        } else {
            LOGGER.info(String.format("The specified memeber is not accepting messages of type %s", messageSender.getMessageType()));
        }

        return response;
    }
}
