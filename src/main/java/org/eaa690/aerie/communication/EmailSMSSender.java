package org.eaa690.aerie.communication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailSMSSender extends MessageSender {

    @Autowired
    public EmailSMSSender(AcceptsSMSPredicate acceptsMessagePredicate) {
        super("SMS_by_Email", acceptsMessagePredicate);
    }

    private MessageSender messageSender;

    @Autowired
    public void setMessageSender(SendGridEmailSender messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    public String sendMessage(Message message) {
        //TODO: Phone number to email conversion
        return messageSender.sendMessage(message);
    }
    
}
