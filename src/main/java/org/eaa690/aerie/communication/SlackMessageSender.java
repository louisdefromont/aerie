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

import com.ullink.slack.simpleslackapi.SlackMessageHandle;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.replies.SlackMessageReply;

import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.communication.SlackMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SlackMessageSender extends MessageSender<SlackMessage> {

    /**
     * SlackSession.
     */
    @Autowired
    private SlackSession slackSession;

    @Autowired
    public SlackMessageSender(final AcceptsSlackPredicate acceptsMessagePredicate) {
        super("Slack Message", acceptsMessagePredicate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String sendMessage(final SlackMessage message, final Member recipientMember) {
        SlackMessageHandle<SlackMessageReply> reply = slackSession.sendMessageToUser(
                        slackSession.findUserByUserName(message.getRecipientAddress()),
                        message.getBody(), null);

        return reply.getReply().getErrorMessage();


    }
}
