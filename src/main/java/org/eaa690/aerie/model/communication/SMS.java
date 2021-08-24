package org.eaa690.aerie.model.communication;

import org.eaa690.aerie.model.Member;

public class SMS extends Message {
    private String body;

    public SMS(String recipientAddress, Member recipientMember, String body) {
        super(recipientAddress, recipientMember);
        this.body = body;
    }

    public String getBody() {
        return this.body;
    }
}
