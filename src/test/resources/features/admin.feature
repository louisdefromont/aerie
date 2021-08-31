@admin
Feature: admin
  Various administrative functions

  @slack
  Scenario: Retrieve all Slack users
    Given I am an unauthenticated user
    When I request all Slack users
    Then The request should be successful

  @email @queue
  Scenario: Retrieve email queue count
    Given I am an unauthenticated user
    When I request the email queue count
    Then The request should be successful

  @email @newmember
  Scenario: Send new membership email
    Given I am an unauthenticated user
    When I request an email be sent to new member 42648
    Then The request should be successful

  @email @renewmember
  Scenario: Send renew membership email
    Given I am an unauthenticated user
    When I request a message be sent to member 42648 to renew their membership
    Then The request should be successful

  @mailchimp @member @disabled
  Scenario: Add a new member to MailChimp with an invalid rosterId
    Given I am an unauthenticated user
    When I request member 42648 be added to the MailChimp member list
    Then The request should be successful

  @mailchimp @nonmember @disabled
  Scenario: Add a non member to MailChimp with an invalid rosterId
    Given I am an unauthenticated user
    When I request non-member 42648 be added to the MailChimp non-member list
    Then The request should be successful

  @email @newmember @invaliddata
  Scenario: Send new membership email to an invalid member
    Given I am an unauthenticated user
    When I request an email be sent to new member 0
    Then A not found exception should be thrown

  @sms
  Scenario:
    Given I am a chapter member
    When I send a SMS/Text message to myself with the following message:
       | This is a test of the Aerie broadcast system, this is only a test |
    Then The request should be successful

  @mailchimp @member @invaliddata
  Scenario: Add a new member to MailChimp with an invalid rosterId
    Given I am an unauthenticated user
    When I request member 0 be added to the MailChimp member list
    Then A not found exception should be thrown

  @mailchimp @nonmember @invaliddata
  Scenario: Add a non member to MailChimp with an invalid rosterId
    Given I am an unauthenticated user
    When I request non-member 0 be added to the MailChimp non-member list
    Then A not found exception should be thrown

  @resubscribe @email @disabled
  Scenario: Former email recipient wishes to re-subscribe to future emails
    Given I am a chapter member
    When I resubscribe to receiving email messages
    Then The request should be successful
    And I should see a message stating that I have been resubscribed
    And I have an emailEnabled status of true

  @resubscribe @sms @disabled
  Scenario: Former SMS/Text message recipient wishes to re-subscribe to future SMS/Text messages
    Given I am a chapter member
    When I resubscribe to receiving sms messages
    Then The request should be successful
    And I should see a message stating that I have been resubscribed
    And I have an smsEnabled status of true

  @resubscribe @slack @disabled
  Scenario: Former Slack message recipient wishes to re-subscribe to future Slack messages
    Given I am a chapter member
    When I resubscribe to receiving slack messages
    Then The request should be successful
    And I should see a message stating that I have been resubscribed
    And I have an slackEnabled status of true

  @weather @update
  Scenario: Update weather data from AviationWeather.gov
    Given I am an unauthenticated user
    When I request the weather data to be updated
    Then The request should be successful
