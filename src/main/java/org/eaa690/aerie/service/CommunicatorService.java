package org.eaa690.aerie.service;

import java.util.function.Predicate;

import org.eaa690.aerie.communication.Message;
import org.eaa690.aerie.communication.MessageSender;
import org.eaa690.aerie.model.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CommunicatorService {
    private MessageSender messageSender;
    private Predicate<Member> acceptsMessagePredicate;

    private static final Logger LOGGER = LoggerFactory.getLogger(SMSService.class);

    public CommunicatorService(MessageSender messageSender, Predicate<Member> sendMessagePredicate) {
        this.messageSender = messageSender;
        this.acceptsMessagePredicate = sendMessagePredicate;
    }

    public String sendMessage(Message message) {
        String response = null;
        if (acceptsMessagePredicate.test(message.getRecipientMember())) {
            LOGGER.info(String.format("Sending %s to member of id: %d", messageSender.getMessageType(), message.getRecipientMember().getId()));
            response = messageSender.sendMessage(message);
            LOGGER.info(response);
        } else {
            LOGGER.info(String.format("The specified memeber is not accepting messages of type %s", messageSender.getMessageType()));
        }

        return response;
    }
}
