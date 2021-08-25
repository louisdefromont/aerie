package org.eaa690.aerie.communication;

import java.util.function.Predicate;

import org.eaa690.aerie.model.Member;
import org.springframework.stereotype.Component;

@Component
public class AcceptsEmailPredicate implements Predicate<Member> {

    @Override
    public boolean test(Member member) {
        return (member != null && member.getEmail() != null && member.isEmailEnabled());
    }
    
}
