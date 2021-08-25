package org.eaa690.aerie.communication;

import org.eaa690.aerie.model.communication.Email;
import org.eaa690.aerie.model.communication.Message;
import org.eaa690.aerie.model.communication.SMS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailSMSSender extends MessageSender<SMS> {

    @Autowired
    public EmailSMSSender(AcceptsSMSPredicate acceptsMessagePredicate) {
        super("SMS_by_Email", acceptsMessagePredicate);
    }

    private MessageSender<Email> messageSender;

    @Autowired
    public void setMessageSender(SendGridEmailSender messageSender) {
        this.messageSender = messageSender;
    }

    @Override
    public String sendMessage(SMS message) {
        String recipientAddress = message.getRecipientAddress() + "@" + message.getRecipientMember().getCellPhoneProvider().getCellPhoneProviderEmailDomain();

        Email email = new Email(recipientAddress, message.getRecipientMember(), "eaa", null, message.getBody());

        return messageSender.sendMessage(email);
    }
    
}
