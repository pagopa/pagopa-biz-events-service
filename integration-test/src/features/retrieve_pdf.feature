Feature: Retrieve receipts' PDF

  Background:
    Given Biz-Events Service running

  Scenario: A user retrieve a receipt's PDF
    Given a PDF stored on the receipts' blob storage with name "biz-event-service-int-test-pdf-1.pdf"
    And a receipt with eventId "biz-event-service-int-test-pdf-1", pdf name "biz-event-service-int-test-pdf-1.pdf", status "IO_NOTIFIED" and errCode "null"
    When the user with fiscal code "INTTST00A00A000E" asks for the PDF with thirdPartyId "biz-event-service-int-test-pdf-1"
    Then the user gets the status code 200 for generatePDF
    And the PDF content is valid
  
  Scenario: A user tries to retrieve the PDF of a receipt that is being processed
    Given a receipt with eventId "biz-event-service-int-test-pdf-2", pdf name "biz-event-service-int-test-pdf-2.pdf", status "INSERTED" and errCode "null"
    When the user with fiscal code "INTTST00A00A000E" asks for the PDF with thirdPartyId "biz-event-service-int-test-pdf-2"
    Then the user gets the status code 404 for generatePDF
    And the user gets the custom code "AT_404_002"

  Scenario: A user tries to retrieve the PDF of a receipt that is being processed
    Given a receipt with eventId "biz-event-service-int-test-pdf-3", pdf name "biz-event-service-int-test-pdf-3.pdf", status "INSERTED" and errCode "null"
    When the user with fiscal code "INTTST00A00A000E" asks for the PDF with thirdPartyId "biz-event-service-int-test-pdf-3"
    Then the user gets the status code 404 for generatePDF
    And the user gets the custom code "AT_404_002"

  Scenario: A user tries to retrieve the PDF of a receipt that failed but is retryable
    Given a receipt with eventId "biz-event-service-int-test-pdf-4", pdf name "biz-event-service-int-test-pdf-4.pdf", status "FAILED" and errCode "900"
    When the user with fiscal code "INTTST00A00A000E" asks for the PDF with thirdPartyId "biz-event-service-int-test-pdf-4"
    Then the user gets the status code 404 for generatePDF
    And the user gets the custom code "AT_404_002"

  Scenario: A user tries to retrieve the PDF of a receipt that failed and needs manual review
    Given a receipt with eventId "biz-event-service-int-test-pdf-5", pdf name "biz-event-service-int-test-pdf-5.pdf", status "FAILED" and errCode "903"
    When the user with fiscal code "INTTST00A00A000E" asks for the PDF with thirdPartyId "biz-event-service-int-test-pdf-5"
    Then the user gets the status code 404 for generatePDF
    And the user gets the custom code "AT_404_001"

  Scenario: A user tries to retrieve the PDF of a receipt that has not been created
    Given Biz-Event to test with id "biz-event-service-int-test-pdf-6" and save it on Cosmos DB
    When the user with fiscal code "INTTST00A00A000E" asks for the PDF with thirdPartyId "biz-event-service-int-test-pdf-6"
    Then the user gets the status code 404 for generatePDF
    And the user gets the custom code "AT_404_002"

  Scenario: A user payer tries to retrieve the PDF of a cart that has not been created
    Given Biz-Event to test with id "biz-event-service-int-test-pdf-7" and save it on Cosmos DB
    When the user with fiscal code "INTTST00A00A000E" asks for the PDF with thirdPartyId "biz-event-service-int-test-pdf-7_CART_"
    Then the user gets the status code 404 for generatePDF
    And the user gets the custom code "AT_404_002"

  Scenario: A user debtor tries to retrieve the PDF of a cart that has not been created
    Given Biz-Event to test with id "biz-event-service-int-test-pdf-8" and save it on Cosmos DB
    When the user with fiscal code "INTTST00A00A000E" asks for the PDF with thirdPartyId "transactionIdPdfTest_CART_biz-event-service-int-test-pdf-8"
    Then the user gets the status code 404 for generatePDF
    And the user gets the custom code "AT_404_002"

  Scenario: A user tries to retrieve the PDF that is not its own
    Given a receipt with eventId "biz-event-service-int-test-pdf-9", pdf name "biz-event-service-int-test-pdf-9.pdf", status "IO_NOTIFIED" and errCode "null"
    When the user with fiscal code "INTTST00A00A000X" asks for the PDF with thirdPartyId "biz-event-service-int-test-pdf-9"
    Then the user gets the status code 400 for generatePDF
    And the user gets the custom code "GN_400_001"