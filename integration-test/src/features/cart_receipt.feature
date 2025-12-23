Feature: All about Cart Receipt

  Background:
    Given Biz-Events Service running


  Scenario: A payer user asks for a transaction detail with all cart items
    Given 1 view user with taxCode "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-4" and isCart "true" and isPayer "true" on cosmos
    And 3 view general with payer tax code "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-4" and isCart "true" on cosmos
    And 1 view cart with debtor taxCode "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-4" and isCart "true" on cosmos
    And 2 view cart with debtor taxCode "INTTST00A00A000C", id prefix "biz-event-service-int-test-transaction-4" and isCart "true" on cosmos
    And Save all views on CosmosDB
    When the user with fiscal code "INTTST00A00A000A" asks the transaction with id "biz-event-service-int-test-transaction-4_CART_"
    Then the user gets the status code 200
    And the user with tax code "INTTST00A00A000A" gets the transaction detail with id "biz-event-service-int-test-transaction-4_CART_" and it has the correct amount

  Scenario: A debtor user asks for a transaction detail with only their cart items
    Given 1 view user with taxCode "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-5" and isCart "true" and isPayer "true" on cosmos
    And 3 view general with payer tax code "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-5" and isCart "true" on cosmos
    And 1 view cart with debtor taxCode "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-5" and isCart "true" on cosmos
    And 2 view cart with debtor taxCode "INTTST00A00A000C", id prefix "biz-event-service-int-test-transaction-5" and isCart "true" on cosmos
    And Save all views on CosmosDB
    When the user with fiscal code "INTTST00A00A000C" asks the transaction with id "biz-event-service-int-test-transaction-5_CART_biz-event-service-int-test-transaction-50INTTST00A00A000C"
    Then the user gets the status code 200
    And the user with tax code "INTTST00A00A000C" gets the transaction detail with id "biz-event-service-int-test-transaction-5_CART_biz-event-service-int-test-transaction-50INTTST00A00A000C" and it has the correct amount

  Scenario: A user hides a cart transaction
    Given 3 view user with taxCode "INTTST00A00A000D", id prefix "biz-event-service-int-test-transaction-7" and isCart "true" and isPayer "true" on cosmos
    And 3 view general with payer tax code "INTTST00A00A000D", id prefix "biz-event-service-int-test-transaction-7" and isCart "true" on cosmos
    And 3 view cart with debtor taxCode "INTTST00A00A000D", id prefix "biz-event-service-int-test-transaction-7" and isCart "true" on cosmos
    And Save all views on CosmosDB
    When the user with taxCode "INTTST00A00A000D" disables the transaction with id "biz-event-service-int-test-transaction-7_CART_"
    And the user with fiscal code "INTTST00A00A000D" asks for its transactions
    Then the user gets the status code 200
    And the user gets 0 transactions

  Scenario: A user retrieves the pdf of a cart transaction
    Given 1 view user with taxCode "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-8" and isCart "true" and isPayer "true" on cosmos
    And 3 view general with payer tax code "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-8" and isCart "true" on cosmos
    And 1 view cart with debtor taxCode "INTTST00A00A000A", id prefix "biz-event-service-int-test-transaction-8" and isCart "true" on cosmos
    And 2 view cart with debtor taxCode "INTTST00A00A000C", id prefix "biz-event-service-int-test-transaction-8" and isCart "true" on cosmos
    And Biz-Event with debtor fiscal code "INTTST00A00A000A" and id "biz-event-service-int-test-transaction-8"
    And Save all views on CosmosDB
    When the user with taxCode "INTTST00A00A000A" try to retrieve the pdf of the transaction "biz-event-service-int-test-transaction-8_CART_"
    Then the user gets the status code 200
