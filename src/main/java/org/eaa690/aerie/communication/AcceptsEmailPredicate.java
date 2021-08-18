package org.eaa690.aerie.communication;

import java.util.function.Predicate;

import org.eaa690.aerie.model.Member;

public class AcceptsEmailPredicate implements Predicate<Member> {

    @Override
    public boolean test(Member member) {
        return (member != null && member.getEmail() != null && member.emailEnabled());
    }
    
}
