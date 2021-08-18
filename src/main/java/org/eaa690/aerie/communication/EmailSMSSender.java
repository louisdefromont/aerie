package org.eaa690.aerie.communication;

import org.springframework.beans.factory.annotation.Autowired;

public class EmailSMSSender extends MessageSender {

    public EmailSMSSender() {
        super("SMS_by_Email");
    }

    private MessageSender messageSender;

    @Autowired
    public void setMessageSender(SendGridEmailSender messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    public String sendMessage(Message message) {
        return messageSender.sendMessage(message);
    }
    
}
