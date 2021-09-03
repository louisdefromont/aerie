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

package org.eaa690.aerie.controller;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.communication.Email;
import org.eaa690.aerie.model.communication.SMS;
import org.eaa690.aerie.service.CommunicationService;
import org.eaa690.aerie.service.MailChimpService;
import org.eaa690.aerie.service.RosterService;
import org.eaa690.aerie.service.SlackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * AdminController.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping({
        "/admin"
})
@Tag(name = "admin", description = "the Admin API")
public class AdminController {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(AdminController.class);

    /**
     * Send message string.
     */
    private static final String SEND_MSG_MESSAGE = "Sending %s %s to %s %s at %s";

    /**
     * RosterService.
     */
    private RosterService rosterService;

    /**
     * MailChimpService.
     */
    private MailChimpService mailChimpService;

    /**
     * SMSService.
     */
    @Autowired
    private CommunicationService<SMS> smsService;

    /**
     * EmailService.
     */
    @Autowired
    private CommunicationService<Email> emailService;

    /**
     * SlackService.
     */
    @Autowired
    private SlackService slackService;

    /**
     * Sets RosterService.
     *
     * @param value RosterService
     */
    @Autowired
    public void setRosterService(final RosterService value) {
        rosterService = value;
    }

    /**
     * Sets MailChimpService.
     *
     * @param value MailChimpService
     */
    @Autowired
    public void setMailChimpService(final MailChimpService value) {
        mailChimpService = value;
    }

    /**
     * Sends SMS Message to a member.
     *
     * @param rosterId RosterId of member.
     * @param textBody SMS Body to be sent.
     */
    @PostMapping(path = {"/sms/{rosterId}/{textBody}"})
    public void sendSMS(
            @PathVariable("rosterId") final Long rosterId,
            @PathVariable("textBody") final String textBody) {
        try {
            final Member member = rosterService.getMemberByRosterID(rosterId);
            final SMS sms = new SMS(textBody, member.getId(), member.getCellPhone());
            smsService.queueMsg(sms);
        } catch (ResourceNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Gets queued email count.
     *
     * @return queue count
     */
    @Operation(summary = "Email queue count",
            description = "The current number of emails in the email queue waiting to be sent",
            tags = {"admin"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "successful operation")
    })
    @GetMapping(path = {"/email/queue-count"})
    public int getQueuedEmailCount() {
        return emailService.getQueuedMsgCount();
    }

    /**
     * Sends a renew membership SMS, Email, and/or Slack message to the provided member.
     *
     * @param rosterId Member Roster ID
     * @throws ResourceNotFoundException when member is not found
     */
    @PostMapping(path = {"/{rosterId}/renew-membership"})
    public void testRenewMembershipSMS(@PathVariable("rosterId") final Long rosterId) throws ResourceNotFoundException {
        final Member member = rosterService.getMemberByRosterID(rosterId);
        LOGGER.info(String.format(SEND_MSG_MESSAGE, "renew-membership", "sms",
                member.getFirstName(), member.getLastName(), member.getCellPhone()));
        smsService.buildRenewMembershipMsg(member);
    }

    /**
     * Sends a new membership SMS, Email, and/or Slack message to the provided member.
     *
     * @param rosterId Member Roster ID
     * @throws ResourceNotFoundException when member is not found
     */
    @PostMapping(path = {"/{rosterId}/new-membership"})
    public void testNewMembershipSMS(@PathVariable("rosterId") final Long rosterId) throws ResourceNotFoundException {
        final Member member = rosterService.getMemberByRosterID(rosterId);
        LOGGER.info(String.format(SEND_MSG_MESSAGE, "new-membership", "sms",
                member.getFirstName(), member.getLastName(), member.getCellPhone()));
        smsService.buildNewMembershipMsg(member);
    }

    /**
     * Gets all Slack users.
     *
     * @return All slack users
     * @throws ResourceNotFoundException when member is not found
     */
    @Operation(summary = "All Slack users",
            description = "List of all Slack users",
            tags = {"admin"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "successful operation",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))))
    })
    @GetMapping(path = {"/slack/users"})
    public List<String> getAllSlackUsers() throws ResourceNotFoundException {
        return slackService.allSlackUsers();
    }

    /**
     * Adds a person to the member audience in Mail Chimp.
     *
     * @param rosterId Member Roster ID
     * @throws ResourceNotFoundException when member is not found
     */
    @PostMapping(path = {"/mailchimp/{rosterId}/add-member"})
    public void addOrUpdateMemberToMailChimp(@PathVariable("rosterId") final Long rosterId)
            throws ResourceNotFoundException {
        final Member member = rosterService.getMemberByRosterID(rosterId);
        mailChimpService.addOrUpdateMember(member.getFirstName(), member.getLastName(), member.getEmail());
    }

    /**
     * Adds a person to the non-member audience in Mail Chimp.
     *
     * @param rosterId Member Roster ID
     * @throws ResourceNotFoundException when member is not found
     */
    @PostMapping(path = {"/mailchimp/{rosterId}/add-non-member"})
    public void addOrUpdateNonMemberToMailChimp(@PathVariable("rosterId") final Long rosterId)
            throws ResourceNotFoundException {
        final Member member = rosterService.getMemberByRosterID(rosterId);
        mailChimpService.addOrUpdateNonMember(member.getFirstName(), member.getLastName(), member.getEmail());
    }
}
