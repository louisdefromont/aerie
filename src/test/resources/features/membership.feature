@membership
Feature: membership
  Membership features

  @newmember @disabled
  Scenario: New chapter member providing only a cell phone number as a contact method.
    Given I am not a chapter member
    And I do not provide an email address
    And I provide a cell phone number
    And I do not provide a Slack name
    When I submit a new membership Jot Form
    Then I should have a record in the roster management system
    And I should be listed in the members MailChimp audience
    And my membership expiration should be set to 1 year from now
    And I should not receive a new member welcome email message
    And I should receive a new member SMS/Text message
    And I should not receive a new member Slack message

  @newmember @disabled
  Scenario: New chapter member providing only an email address as a contact method.
    Given I am not a chapter member
    And I provide an email address
    And I do not provide a cell phone number
    And I do not provide a Slack name
    When I submit a new membership Jot Form
    Then I should have a record in the roster management system
    And I should be listed in the members MailChimp audience
    And my membership expiration should be set to 1 year from now
    And I should receive a new member welcome email message
    And I should not receive a new member SMS/Text message
    And I should not receive a new member Slack message

  @renewingmember @disabled
  Scenario: Existing chapter member submits a new membership Jot Form.
    Given I have a record in the roster management system
    And I have enabled sending of messages by email
    When I submit a new membership Jot Form
    Then I should have a record in the roster management system
    And I should be listed in the members MailChimp audience
    And my membership expiration should be set to 1 year from my previous expiration date
    And I should receive a renew member welcome email message

  @renewingmember @disabled
  Scenario: Renewing chapter member providing all contact methods.
    Given I am a chapter member
    And I have enabled sending of messages by email
    And I have enabled sending of messages by SMS/Text
    And I have enabled sending of messages by Slack
    When I submit a renew membership Jot Form
    Then I should have a record in the roster management system
    And I should be listed in the members MailChimp audience
    And my membership expiration should be set to 1 year from my previous expiration date
    And I should receive a new member welcome email message
    And I should receive a new member SMS/Text message
    And I should receive a new member Slack message

  @renewingmember @disabled
  Scenario: Existing chapter member providing only an email address as a contact method.
    Given I am a chapter member
    And I have enabled sending of messages by email
    And I have disabled sending of messages by SMS/Text
    And I have disabled sending of messages by Slack
    When I submit a renew membership Jot Form
    Then I should have a record in the roster management system
    And I should be listed in the members MailChimp audience
    And my membership expiration should be set to 1 year from my previous expiration date
    And I should receive a new member welcome email message
    And I should not receive a new member SMS/Text message
    And I should not receive a new member Slack message

  @newmember @disabled
  Scenario: New member submits a renew membership Jot Form.
    Given I do not have a record in the roster management system
    And I provide an email address
    And I do not provide a cell phone number
    And I do not provide a Slack name
    When I submit a renew membership Jot Form
    Then I should have a record in the roster management system
    And I should be listed in the members MailChimp audience
    And my membership expiration should be set to 1 year from now
    And I should receive a new member welcome email message
    And I should not receive a new member SMS/Text message
    And I should not receive a new member Slack message

  @status @disabled
  Scenario: Chapter member checks their membership status
    Given I am a chapter member
    When I check the membership status for member with ID 42648
    Then I should receive my membership details

  @status @disabled
  Scenario: Non-member checks their membership status
    Given I am not a chapter member
    When I check the membership status for member with ID 42648
    Then A not found exception should be thrown

  @unsubscribe @email @disabled
  Scenario: Email recipient wishes to un-subscribe from future emails
    Given I am a chapter member
    When I unsubscribe from receiving email messages
    Then The request should be successful
    And I should see a message stating that I have been unsubscribed
    And I have an emailEnabled status of false

  @unsubscribe @sms @disabled
  Scenario: SMS/Text message recipient wishes to un-subscribe from future SMS/Text messages
    Given I am a chapter member
    When I unsubscribe from receiving sms messages
    Then The request should be successful
    And I should see a message stating that I have been unsubscribed
    And I have an smsEnabled status of false

  @unsubscribe @slack @disabled
  Scenario: Slack message recipient wishes to un-subscribe from future Slack messages
    Given I am a chapter member
    When I unsubscribe from receiving slack messages
    Then The request should be successful
    And I should see a message stating that I have been unsubscribed
    And I have an slackEnabled status of false
