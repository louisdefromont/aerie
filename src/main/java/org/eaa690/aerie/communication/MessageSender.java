package org.eaa690.aerie.communication;

import java.util.function.Predicate;

import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.communication.Message;

public abstract class MessageSender<T extends Message> {
    private String messageType;
    private Predicate<Member> acceptsMessagePredicate;

    public MessageSender(String messageType, Predicate<Member> acceptsMessagePredicate) {
        this.messageType = messageType;
        this.acceptsMessagePredicate = acceptsMessagePredicate;
    }

    public String getMessageType() {
        return messageType;
    }

    public Predicate<Member> getAcceptsMessagePredicate() {
        return acceptsMessagePredicate;
    }

    public abstract String sendMessage(T message);
}
