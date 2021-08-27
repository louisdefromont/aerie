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

  @email @newmember @disabled
  Scenario: Send new membership email
    Given I am an unauthenticated user
    When I request an email be sent to new member 42648
    Then The request should be successful

  @email @renewmember @disabled
  Scenario Outline: Send renew membership email
    Given I am an unauthenticated user
    When I request a <order> email be sent to member <rosterId> to renew their membership
    Then The request should be successful

    Examples:
      | order  | rosterId |
      | first  | 42648    |
      | second | 42648    |
      | third  | 42648    |

  @sms @newmember @disabled
  Scenario: Send new membership SMS/Text
    Given I am an unauthenticated user
    When I request a text be sent to new member 42648
    Then The request should be successful

  @sms @renewmember @disabled
  Scenario Outline: Send renew membership SMS/Text
    Given I am an unauthenticated user
    When I request a text be sent to member <rosterId> to renew their membership
    Then The request should be successful

    Examples:
      | order  | rosterId |
      | first  | 42648    |
      | second | 42648    |
      | third  | 42648    |

  @slack @newmember @disabled
  Scenario: Send new membership Slack message
    Given I am an unauthenticated user
    When I request a Slack message be sent to new member 42648
    Then The request should be successful

  @slack @renewmember @disabled
  Scenario Outline: Send renew membership Slack message
    Given I am an unauthenticated user
    When I request a Slack message be sent to member <rosterId> to renew their membership
    Then The request should be successful

    Examples:
      | order  | rosterId |
      | first  | 42648    |
      | second | 42648    |
      | third  | 42648    |

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

  @email @renewmember @invaliddata
  Scenario Outline: Send renew membership email to an invalid member
    Given I am an unauthenticated user
    When I request a <order> email be sent to member <rosterId> to renew their membership
    Then A not found exception should be thrown

    Examples:
      | order  | rosterId |
      | first  | 0        |
      | second | 0        |
      | third  | 0        |

  @sms @newmember @invaliddata @disabled
  Scenario: Send new membership SMS/Text to an invalid member
    Given I am an unauthenticated user
    When I request a text be sent to new member 0
    Then A not found exception should be thrown

  @sms @renewmember @invaliddata @disabled
  Scenario: Send renew membership SMS/Text to an invalid member
    Given I am an unauthenticated user
    When I request a text be sent to member 0 to renew their membership
    Then A not found exception should be thrown

  @slack @newmember @invaliddata
  Scenario: Send new membership Slack message to an invalid member
    Given I am an unauthenticated user
    When I request a Slack message be sent to new member 0
    Then A not found exception should be thrown

  @slack @renewmember @invaliddata
  Scenario: Send renew membership Slack message to an invalid member
    Given I am an unauthenticated user
    When I request a Slack message be sent to member 0 to renew their membership
    Then A not found exception should be thrown

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
