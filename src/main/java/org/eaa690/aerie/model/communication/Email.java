package org.eaa690.aerie.model.communication;

import org.eaa690.aerie.model.Member;

public class Email extends Message {

    private String subject;
    private String templateID;
    private String body;

    public Email(String recipientAddress, Member recipientMember, String subject, String templateID, String body) {
        super(recipientAddress, recipientMember);
        this.subject = subject;
        this.templateID = templateID;
        this.body = body;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getTemplateID() {
        return this.templateID;
    }

    public String getBody() {
        return this.body;
    }

}
