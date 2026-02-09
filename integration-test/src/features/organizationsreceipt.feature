Feature: All about Organizations Receipt

  Background:
    Given Biz-Events Service running

  Scenario: An organization asks for a receipt
    Given Biz-Event to test with id "123456789" and save it on Cosmos DB
    When the organization asks for a receipt with fiscal code PA "fiscalCode-123456789" and iur "iur-123456789" and iuv "iuv-123456789"
    Then the organization gets the status code 200
    And the details of the receipt are returned to the organization with receiptId "123456789"
    And the Biz-Event to test with id "123456789" is removed

  Scenario: An operator asks for a Biz-Event with fiscal code PA and iuv
    Given Biz-Event to test with id "123456789" and save it on Cosmos DB
    When the operator asks for a Biz-Event with fiscal code PA "fiscalCode-123456789" and iuv "iuv-123456789"
    Then the operator gets the status code 200
    And the details of the Biz-Event are returned to the operator with id "123456789"
    And the Biz-Event to test with id "123456789" is removed

  Scenario: An operator asks for a Biz-Event id
    Given Biz-Event to test with id "123456789" and save it on Cosmos DB
    When the operator asks for a Biz-Event with id "123456789"
    Then the operator gets the status code 200
    And the details of the Biz-Event are returned to the operator with id "123456789"
    And the Biz-Event to test with id "123456789" is removed