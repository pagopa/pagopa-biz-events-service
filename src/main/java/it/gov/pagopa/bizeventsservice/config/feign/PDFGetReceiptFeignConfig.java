package it.gov.pagopa.bizeventsservice.config.feign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PDFGetReceiptFeignConfig extends AuthFeignConfig {

  private static final String RECEIPT_SUBKEY_PLACEHOLDER = "${pdf.get.receipt.subscription-key}";

  @Autowired
  public PDFGetReceiptFeignConfig(@Value(RECEIPT_SUBKEY_PLACEHOLDER) String subscriptionKey) {
    this.subscriptionKey = subscriptionKey;
  }
}
