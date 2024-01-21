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

  Scenario: An user asks for a its transactions
    Given 3 Biz-Event with debtor fiscal code "INTTST00A00A000A"
    And 3 Biz-Event with payer fiscal code "INTTST00A00A000A"
    And Save all on Cosmos DB
    When the user with fiscal code "INTTST00A00A000A" asks for its transactions
    Then the user gets the status code 200
    And the user gets 6 transactions

  Scenario: An user asks for a its transactions with cart transaction
    Given 3 Biz-Event with debtor fiscal code "INTTST00A00A000A"
    And 3 Biz-Event with payer fiscal code "INTTST00A00A000A"
    And 3 cart Biz-Event with transactionId "biz-event-service-int-test-transaction-1", debtor fiscal code "INTTST00A00A000A" and amount 1000
    And Save all on Cosmos DB
    When the user with fiscal code "INTTST00A00A000A" asks for its transactions
    Then the user gets the status code 200
    And the user gets 7 transactions
    And one of the transactions is a cart with id "biz-event-service-int-test-transaction-1" and amount 30.00

  Scenario: An user asks for a transaction
    Given Biz-Event with debtor fiscal code "INTTST00A00A000A" and id "biz-event-service-int-test-transaction-2"
    And Save all on Cosmos DB
    When the user with fiscal code "INTTST00A00A000A" asks the transaction with id "biz-event-service-int-test-transaction-2" and isCart "false"
    Then the user gets the status code 200
    And the user gets the transaction with id "biz-event-service-int-test-transaction-2"

  Scenario: An user asks for a cart transaction
    Given 3 cart Biz-Event with transactionId "biz-event-service-int-test-transaction-3", debtor fiscal code "INTTST00A00A000A" and amount 1000
    And Save all on Cosmos DB
    When the user with fiscal code "INTTST00A00A000A" asks the transaction with id "biz-event-service-int-test-transaction-3" and isCart "true"
    Then the user gets the status code 200
    And the user gets the transaction with id "biz-event-service-int-test-transaction-3"
