Feature: All about Organizations Receipt

  Background:
    Given Biz-Events Service running

  Scenario: An organization asks for a receipt
    Given Biz-Event to test with id "123456789" and save it on Cosmos DB
    When the organization asks for a receipt with fiscal code PA "66666666666" and iur "66666666666302111144093833700-17735" and iuv "111144093833700"
    Then the organization gets the status code 200
    And the details of the receipt are returned to the organization with receiptId "123456789"
