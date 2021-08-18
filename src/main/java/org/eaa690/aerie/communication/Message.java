package org.eaa690.aerie.communication;

import org.eaa690.aerie.model.Member;

public class Message {
    private String recipientAddress = null;
    private Member recipientMember = null;

    private String messageBody = null;
    private String messageSubject = null;
    private String templateId = null;

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public Member getRecipientMember() {
        return recipientMember;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public String getMessageSubject() {
        return messageSubject;
    }

    public String getTemplateId() {
        return templateId;
    }
    public void setRecipientAddress(String recipientAddress) {
        this.recipientAddress = recipientAddress;
    }
    public void setRecipientMember(Member recipientMember) {
        this.recipientMember = recipientMember;
    }
    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }
    public void setMessageSubject(String messageSubject) {
        this.messageSubject = messageSubject;
    }
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

}
