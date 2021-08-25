@weather @disabled
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

  @metar
  Scenario: Retrieve METAR information for the Atlanta TAC
    Given I am an unauthenticated user
    When I request the Atlanta METAR
    Then The request should be successful
    And I should receive data for multiple stations

  @metar
  Scenario: Retrieve METAR information for an unprovided station
    Given I am an unauthenticated user
    When I request a METAR for an unprovided station
    Then A not found exception should be thrown

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

  @metar
  Scenario Outline: Retrieve METAR information for an invalid station
    Given I am an unauthenticated user
    When I request the <icao> METAR
    Then A bad request exception should be thrown

    Examples:
      | icao  |
      | KCLT  |
      | KDEN  |

  @taf
  Scenario: Retrieve TAF information for the Atlanta TAC
    Given I am an unauthenticated user
    When I request the Atlanta TAF
    Then The request should be successful

  @taf
  Scenario Outline: Retrieve TAF information for a single station
    Given I am an unauthenticated user
    When I request the <icao> TAF
    Then The request should be successful

    Examples:
      | icao  |
      | KATL  |
      | KPDK  |

  @taf
  Scenario: Retrieve TAF information for an unprovided station
    Given I am an unauthenticated user
    When I request a TAF for an unprovided station
    Then A not found exception should be thrown

  @taf
  Scenario Outline: Retrieve TAF information for an invalid station
    Given I am an unauthenticated user
    When I request the <icao> TAF
    Then A not found exception should be thrown

    Examples:
      | icao  |
      | KLZU  |
      | KFFC  |

  @station
  Scenario: Retrieve Station information for the Atlanta TAC
    Given I am an unauthenticated user
    When I request the Atlanta station
    Then The request should be successful

  @station
  Scenario Outline: Retrieve Station information for a single station
    Given I am an unauthenticated user
    When I request the <icao> station
    Then The request should be successful

    Examples:
      | icao  |
      | KATL  |
      | KLZU  |
      | KFFC  |

  @station
  Scenario Outline: Retrieve Station information for an invalid station
    Given I am an unauthenticated user
    When I request the <icao> station
    Then A bad request exception should be thrown

    Examples:
      | icao  |
      | KCLT  |
      | KDEN  |

  @station
  Scenario: Retrieve Station information for an unprovided station
    Given I am an unauthenticated user
    When I request a station for an unprovided station
    Then A not found exception should be thrown
