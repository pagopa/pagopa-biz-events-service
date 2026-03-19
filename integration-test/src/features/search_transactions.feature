Feature: Biz Events Service - Search Transaction
  
  Background:
    Given Biz-Events Service running

  Scenario: Successful transaction search
    Given i use a valid token
    And i send a valid x-fiscal-code header
    And i use a valid notice number
    When i perform a transaction search
    Then the response status code is 200
    And the body contain the expected transaction data

  Scenario: Request without x-fiscal-code
    Given i use a valid token
    And i do not send a valid x-fiscal-code header
    And i use a valid notice number
    When i perform a transaction search
    Then the response status code is 500

  Scenario: Not-existing NAV
    Given i use a valid token
    And i send a valid x-fiscal-code header
    And i use an invalid notice number
    When i perform a transaction search
    Then the response status code is 404
    And the not found error body is correct

  Scenario: Invalid Token
    Given i use an invalid token
    And i send a valid x-fiscal-code header
    And i use a valid notice number
    When i perform a transaction search
    Then the response status code is 401
    And the unauthorized error body is correct 



