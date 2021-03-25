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

import org.eaa690.aerie.constant.PropertyKeyConstants;
import org.eaa690.aerie.exception.ResourceNotFoundException;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.service.EmailService;
import org.eaa690.aerie.service.MailChimpService;
import org.eaa690.aerie.service.RosterService;
import org.eaa690.aerie.service.SMSService;
import org.eaa690.aerie.service.SlackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AdminController.
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping({
        "/admin"
})
public class AdminController {

    /**
     * RosterService.
     */
    private RosterService rosterService;

    /**
     * EmailService.
     */
    private EmailService emailService;

    /**
     * SMSService.
     */
    private SMSService smsService;

    /**
     * SlackService.
     */
    private SlackService slackService;

    /**
     * MailChimpService.
     */
    private MailChimpService mailChimpService;

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
     * Sets EmailService.
     *
     * @param value EmailService
     */
    @Autowired
    public void setEmailService(final EmailService value) {
        emailService = value;
    }

    /**
     * Sets SMSService.
     *
     * @param value SMSService
     */
    @Autowired
    public void setSMSService(final SMSService value) {
        smsService = value;
    }

    /**
     * Sets SlackService.
     *
     * @param value SlackService
     */
    @Autowired
    public void setSlackService(final SlackService value) {
        slackService = value;
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
     * Sends a renew membership email to the provided address.
     *
     * @param rosterId Member Roster ID
     * @param order first, second, or third reminder message
     */
    @PostMapping(path = {"/email/{rosterId}/renew-membership/{order}"})
    public void testRenewMembershipEmail(
            @PathVariable("rosterId") final Long rosterId,
            @PathVariable("order") final String order) throws ResourceNotFoundException {
        final Member member = rosterService.getMemberByRosterID(rosterId);
        switch (order) {
            case "first":
                emailService.sendMsg(
                        PropertyKeyConstants.SEND_GRID_FIRST_MEMBERSHIP_RENEWAL_EMAIL_TEMPLATE_ID,
                        PropertyKeyConstants.SEND_GRID_FIRST_MEMBERSHIP_RENEWAL_EMAIL_SUBJECT_KEY,
                        member);
                emailService.incrementManualMessageCount();
                break;
            case "second":
                emailService.sendMsg(
                        PropertyKeyConstants.SEND_GRID_SECOND_MEMBERSHIP_RENEWAL_EMAIL_TEMPLATE_ID,
                        PropertyKeyConstants.SEND_GRID_SECOND_MEMBERSHIP_RENEWAL_EMAIL_SUBJECT_KEY,
                        member);
                emailService.incrementManualMessageCount();
                break;
            case "third":
                emailService.sendMsg(
                        PropertyKeyConstants.SEND_GRID_THIRD_MEMBERSHIP_RENEWAL_EMAIL_TEMPLATE_ID,
                        PropertyKeyConstants.SEND_GRID_THIRD_MEMBERSHIP_RENEWAL_EMAIL_SUBJECT_KEY,
                        member);
                emailService.incrementManualMessageCount();
                break;
            default:
                // Do nothing
        }
    }

    /**
     * Sends a new membership email to the provided address.
     *
     * @param rosterId Member Roster ID
     */
    @PostMapping(path = {"/email/{rosterId}/new-membership"})
    public void testNewMembershipEmail(@PathVariable("rosterId") final Long rosterId) throws ResourceNotFoundException {
        final Member member = rosterService.getMemberByRosterID(rosterId);
        emailService.sendMsg(
                PropertyKeyConstants.SEND_GRID_NEW_MEMBERSHIP_EMAIL_TEMPLATE_ID,
                PropertyKeyConstants.SEND_GRID_NEW_MEMBERSHIP_EMAIL_SUBJECT_KEY,
                member);
        emailService.incrementManualMessageCount();
    }

    /**
     * Gets queued email count.
     */
    @GetMapping(path = {"/email/queue-count"})
    public int getQueuedEmailCount() {
        return emailService.getQueuedMsgCount();
    }

    /**
     * Sends a renew membership SMS to the provided address.
     *
     * @param rosterId Member Roster ID
     */
    @PostMapping(path = {"/sms/{rosterId}/renew-membership"})
    public void testRenewMembershipSMS(@PathVariable("rosterId") final Long rosterId) throws ResourceNotFoundException {
        final Member member = rosterService.getMemberByRosterID(rosterId);
        smsService.sendRenewMembershipMsg(member);
    }

    /**
     * Sends a new membership SMS to the provided address.
     *
     * @param rosterId Member Roster ID
     */
    @PostMapping(path = {"/sms/{rosterId}/new-membership"})
    public void testNewMembershipSMS(@PathVariable("rosterId") final Long rosterId) throws ResourceNotFoundException {
        final Member member = rosterService.getMemberByRosterID(rosterId);
        smsService.sendNewMembershipMsg(member);
    }

    /**
     * Sends a renew membership Slack message to the provided address.
     *
     * @param rosterId Member Roster ID
     */
    @PostMapping(path = {"/slack/{rosterId}/renew-membership"})
    public void testRenewMembershipSlack(@PathVariable("rosterId") final Long rosterId)
            throws ResourceNotFoundException {
        final Member member = rosterService.getMemberByRosterID(rosterId);
        slackService.sendRenewMembershipMsg(member);
    }

    /**
     * Sends a new membership Slack message to the provided address.
     *
     * @param rosterId Member Roster ID
     */
    @PostMapping(path = {"/slack/{rosterId}/new-membership"})
    public void testNewMembershipSlack(@PathVariable("rosterId") final Long rosterId) throws ResourceNotFoundException {
        final Member member = rosterService.getMemberByRosterID(rosterId);
        slackService.sendNewMembershipMsg(member);
    }

    /**
     * Gets all Slack users.
     */
    @GetMapping(path = {"/slack/users"})
    public List<String> getAllSlackUsers() throws ResourceNotFoundException {
        return slackService.allSlackUsers();
    }

    /**
     * Adds a person to the member audience in Mail Chimp.
     *
     * @param rosterId Member Roster ID
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
     */
    @PostMapping(path = {"/mailchimp/{rosterId}/add-non-member"})
    public void addOrUpdateNonMemberToMailChimp(@PathVariable("rosterId") final Long rosterId)
            throws ResourceNotFoundException {
        final Member member = rosterService.getMemberByRosterID(rosterId);
        mailChimpService.addOrUpdateNonMember(member.getFirstName(), member.getLastName(), member.getEmail());
    }
}