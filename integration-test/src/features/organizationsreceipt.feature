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

  Scenario: A user asks for its transactions
    Given 3 view user with taxCode "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-1" and isCart "false" and isPayer "true" on cosmos
    And 3 view general with payer tax code "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-1" and isCart "false" on cosmos
    And 3 view cart with debtor taxCode "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-1" and isCart "false" on cosmos
    And Save all views on CosmosDB
    When the user with fiscal code "INTTST00A00A000A" asks for its transactions
    Then the user gets the status code 200
    And the user gets 3 transactions
    And the transactions with cart items "false" for taxCode "INTTST00A00A000A" have the correct amount and subject

 Scenario: A user asks for a transaction detail
    Given 1 view user with taxCode "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-3" and isCart "false" and isPayer "true" on cosmos
    And 1 view general with payer tax code "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-3" and isCart "false" on cosmos 
    And 1 view cart with debtor taxCode "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-3" and isCart "false" on cosmos 
    And Save all views on CosmosDB
    When the user with fiscal code "INTTST00A00A000A" asks the transaction with id "biz-event-service-int-test-transaction-30"
    Then the user gets the status code 200
    And the user with tax code "INTTST00A00A000A" gets the transaction detail with id "biz-event-service-int-test-transaction-30" and it has the correct amount

 Scenario: A payer user asks for a transaction detail with all cart items
    Given 1 view user with taxCode "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-4" and isCart "true" and isPayer "true" on cosmos
    And 3 view general with payer tax code "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-4" and isCart "true" on cosmos 
    And 1 view cart with debtor taxCode "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-4" and isCart "true" on cosmos 
    And 2 view cart with debtor taxCode "INTTST00A00A000C", id prefix "biz-event-service-int-test-transaction-4" and isCart "true" on cosmos 
    And Save all views on CosmosDB
    When the user with fiscal code "INTTST00A00A000A" asks the transaction with id "biz-event-service-int-test-transaction-4"
    Then the user gets the status code 200
    And the user with tax code "INTTST00A00A000A" gets the transaction detail with id "biz-event-service-int-test-transaction-4" and it has the correct amount
  
  Scenario: A debtor user asks for a transaction detail with only their cart items
    Given 1 view user with taxCode "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-5" and isCart "true" and isPayer "true" on cosmos
    And 3 view general with payer tax code "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-5" and isCart "true" on cosmos 
    And 1 view cart with debtor taxCode "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-5" and isCart "true" on cosmos 
    And 2 view cart with debtor taxCode "INTTST00A00A000C", id prefix "biz-event-service-int-test-transaction-5" and isCart "true" on cosmos 
    And Save all views on CosmosDB
    When the user with fiscal code "INTTST00A00A000C" asks the transaction with id "biz-event-service-int-test-transaction-5"
    Then the user gets the status code 200
    And the user with tax code "INTTST00A00A000C" gets the transaction detail with id "biz-event-service-int-test-transaction-5" and it has the correct amount

  Scenario: A user hides a transaction
    Given 3 view user with taxCode "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-6" and isCart "false" and isPayer "true" on cosmos
    And 3 view general with payer tax code "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-6" and isCart "false" on cosmos
    And 3 view cart with debtor taxCode "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-6" and isCart "false" on cosmos
    And Save all views on CosmosDB
    When the user with taxCode "INTTST00A00A000A" disables the transaction with id "biz-event-service-int-test-transaction-60"
    And the user with fiscal code "INTTST00A00A000A" asks for its transactions
    Then the user gets the status code 200
    And the user gets 2 transactions