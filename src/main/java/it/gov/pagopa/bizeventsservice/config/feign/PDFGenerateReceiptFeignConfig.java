package it.gov.pagopa.bizeventsservice.config.feign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PDFGenerateReceiptFeignConfig extends AuthFeignConfig {

    private static final String RECEIPT_SUBKEY_PLACEHOLDER = "${pdf.generate.receipt.subscription-key}";

    @Autowired
    public PDFGenerateReceiptFeignConfig(@Value(RECEIPT_SUBKEY_PLACEHOLDER) String subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
    }
}
