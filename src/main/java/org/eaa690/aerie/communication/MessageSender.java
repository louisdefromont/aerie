package org.eaa690.aerie.communication;


public abstract class MessageSender {
    private String messageType;

    public MessageSender(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageType() {
        return messageType;
    }

    public abstract String sendMessage(Message message);
}
