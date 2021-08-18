package org.eaa690.aerie.communication;

import java.util.function.Predicate;

import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;

public class AcceptsSMSPredicate implements Predicate<Member> {

    @Autowired
    private PropertyService propertyService;

    @Override
    public boolean test(Member member) {
        String to = member.getCellPhone() != null ? member.getCellPhone() : member.getHomePhone();

        try {
            return (to != null &&
                    Boolean.parseBoolean(propertyService.get(PropertyKeyConstants.SMS_ENABLED_KEY).getValue()));
        } catch (ResourceNotFoundException e) {
            return false;
        }
    }
    
}
