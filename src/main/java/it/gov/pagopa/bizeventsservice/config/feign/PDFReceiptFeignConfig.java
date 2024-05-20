package it.gov.pagopa.bizeventsservice.config.feign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PDFReceiptFeignConfig extends AuthFeignConfig {

  private static final String RECEIPT_SUBKEY_PLACEHOLDER = "${pdf.receipt.subscription-key}";

  @Autowired
  public PDFReceiptFeignConfig(@Value(RECEIPT_SUBKEY_PLACEHOLDER) String subscriptionKey) {
    this.subscriptionKey = subscriptionKey;
  }
}
