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
