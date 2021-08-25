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

import org.eaa690.aerie.model.communication.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;

/**
 * Service that sends messages through MessageSender and logs the transaction.
 * @param <T> Type of message that will be sent
 */
@AllArgsConstructor
public abstract class CommunicatorService<T extends Message> {
    /**
     * MessageSender used to send Messages.
     */
    private MessageSender<T> messageSender;
    /**
     * Logger used to log Messages sent.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunicatorService.class);

    /**
     * Sends a message with the configured MessageSender.
     * @param message The message to be sent
     * @return response from the MessageSender sending the message.
     */
    public String sendMessage(final T message) {
        String response = null;
        if (messageSender.getAcceptsMessagePredicate().test(message.getRecipientMember())) {
            LOGGER.info(String.format(
                "Sending %s to member of id: %d",
                messageSender.getMessageType(),
                message.getRecipientMember().getId()));
            response = messageSender.sendMessage(message);
            LOGGER.info(response);
        } else {
            LOGGER.info(
                String.format(
                    "The specified memeber is not accepting messages of type %s",
                    messageSender.getMessageType()));
        }

        return response;
    }
}
