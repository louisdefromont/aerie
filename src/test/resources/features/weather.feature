@weather
Feature: weather
  METAR, TAF, and Station information

  @metar
  Scenario Outline: Retrieve METAR information for a single station
    Given I am an unauthenticated user
    When I request the <icao> METAR
    Then The request should be successful

    Examples:
      | icao  |
      | KATL  |
      | KLZU  |
      | KFFC  |

  @metar @disabled
  Scenario: Retrieve METAR information for the Atlanta TAC
    Given I am an unauthenticated user
    When I request the Atlanta METAR
    Then The request should be successful

  @metar @disabled
  Scenario: Retrieve METAR information for the Atlanta Sectional
    Given I am an unauthenticated user
    When I request the AtlantaSectional METAR
    Then The request should be successful

  @metar
  Scenario Outline: Retrieve specific METAR information for a single station
    Given I am an unauthenticated user
    And I want <field> information
    When I request the <icao> METAR
    Then The request should be successful
    And I should receive the <field> data

    Examples:
      | icao  | field           |
      | KATL  | flight_category |
      | KLZU  | raw_text        |
      | KFFC  | observed        |

  @metar @disabled
  Scenario: Retrieve METAR information for a single station
    Given I am an unauthenticated user
    When I request the atlanta METAR
    Then The request should be successful
    And I should receive data for multiple stations

  @taf @disabled
  Scenario Outline: Retrieve TAF information for a single station
    Given I am an unauthenticated user
    When I request the <icao> TAF
    Then The request should be successful

    Examples:
      | icao  |
      | KATL  |
      | KLZU  |
      | KFFC  |

  @station @disabled
  Scenario Outline: Retrieve Station information for a single station
    Given I am an unauthenticated user
    When I request the <icao> station
    Then The request should be successful

    Examples:
      | icao  |
      | KATL  |
      | KLZU  |
      | KFFC  |