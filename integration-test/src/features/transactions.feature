Feature: All about Organizations Receipt

  Background:
    Given Biz-Events Service running

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

  Scenario: A user hides a transaction
    Given 3 view user with taxCode "INTTST00A00A000D", id prefix "biz-event-service-int-test-transaction-6" and isCart "false" and isPayer "true" on cosmos
    And 3 view general with payer tax code "INTTST00A00A000D", id prefix "biz-event-service-int-test-transaction-6" and isCart "false" on cosmos
    And 3 view cart with debtor taxCode "INTTST00A00A000D", id prefix "biz-event-service-int-test-transaction-6" and isCart "false" on cosmos
    And Save all views on CosmosDB
    When the user with taxCode "INTTST00A00A000D" disables the transaction with id "biz-event-service-int-test-transaction-60"
    And the user with fiscal code "INTTST00A00A000D" asks for its transactions
    Then the user gets the status code 200
    And the user gets 2 transactions

  Scenario: An operator re-enables a transaction
    Given 3 view user with taxCode "INTTST00A00A000D", id prefix "biz-event-service-int-test-transaction-7" and isCart "false" and isPayer "true" and hidden "true" on cosmos
    And 3 view general with payer tax code "INTTST00A00A000D", id prefix "biz-event-service-int-test-transaction-7" and isCart "false" on cosmos
    And 3 view cart with debtor taxCode "INTTST00A00A000D", id prefix "biz-event-service-int-test-transaction-7" and isCart "false" on cosmos
    And Save all views on CosmosDB
    When the operator enables the transaction with id "biz-event-service-int-test-transaction-70" and taxCode "INTTST00A00A000D"
    And the user with fiscal code "INTTST00A00A000D" asks for its transactions
    Then the user gets the status code 200
    And the user gets 1 transactions