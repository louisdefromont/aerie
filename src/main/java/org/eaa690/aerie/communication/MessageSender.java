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
import org.eaa690.aerie.model.communication.Message;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Sends Messages of type T.
 * @param <T> Type of message to be sent
 */
@Getter
@AllArgsConstructor
public abstract class MessageSender<T extends Message> {
    /**
     * String representation of the type of message that can be sent.
     */
    private String messageType;
    /**
     * Predicate used to test if a give member accepts messages of type <T>.
     */
    private Predicate<Member> acceptsMessagePredicate;

    /**
     * Sends a message of type T.
     * @param message The message to be sent.
     * @return Response of sending the message.
     */
    public abstract String sendMessage(T message, Member recipientMember);
}
