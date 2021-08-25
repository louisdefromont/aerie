/*
 *  Copyright (C) 2021 Gwinnett County Experimental Aircraft Association
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.eaa690.aerie.communication;

import java.util.function.Predicate;

import org.eaa690.aerie.model.Member;
import org.springframework.stereotype.Component;

/**
 * Predicate that tests if a given member accepts emails.
 */
@Component
public class AcceptsEmailPredicate implements Predicate<Member> {

    /**
     * Tests whether the member accepts emails.
     * @param member The member that is being tested
     * @return Whether or not the member accepts emails.
     */
    @Override
    public boolean test(final Member member) {
        return (member != null && member.getEmail() != null && member.isEmailEnabled());
    }
}
