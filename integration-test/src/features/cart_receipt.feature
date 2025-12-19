Feature: All about Cart Receipt

  Background:
    Given Biz-Events Service running

  Scenario: A user hides a cart transaction
    Given 3 view user with taxCode "INTTST00A00A000D", transactionId "biz-event-service-int-test-cart" on cosmos
    And 3 view general with payer tax code "INTTST00A00A000D", transactionId "biz-event-service-int-test-cart" on cosmos
    And 3 view cart with debtor taxCode "INTTST00A00A000D", id prefix "biz-event-service-int-test-cart" and isCart "true" on cosmos
    And Save all views on CosmosDB
    When the user with taxCode "INTTST00A00A000D" disables the transaction with id "biz-event-service-int-test-cart_CART_"
    And the user with fiscal code "INTTST00A00A000D" asks for its transactions
    Then the user gets the status code 200
    And the user gets 0 transactions