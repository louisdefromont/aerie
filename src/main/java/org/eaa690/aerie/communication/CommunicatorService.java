package org.eaa690.aerie.communication;

import org.eaa690.aerie.model.communication.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CommunicatorService<T extends Message> {
    private MessageSender<T> messageSender;

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicatorService.class);

    public CommunicatorService(MessageSender<T> messageSender) {
        this.messageSender = messageSender;
    }

    public String sendMessage(T message) {
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
