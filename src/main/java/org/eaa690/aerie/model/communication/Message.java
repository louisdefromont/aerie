package org.eaa690.aerie.model.communication;

import org.eaa690.aerie.model.Member;

public abstract class Message {

    private String recipientAddress;
    private Member recipientMember;

    public Message(String recipientAddress, Member recipientMember) {
        this.recipientAddress = recipientAddress;
        this.recipientMember = recipientMember;
    }


    public String getRecipientAddress() {
        return this.recipientAddress;
    }

    public Member getRecipientMember() {
        return this.recipientMember;
    }

    
}
