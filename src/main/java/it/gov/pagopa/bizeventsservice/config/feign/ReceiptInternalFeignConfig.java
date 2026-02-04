package it.gov.pagopa.bizeventsservice.config.feign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReceiptInternalFeignConfig extends AuthFeignConfig {

    private static final String RECEIPT_SUBKEY_PLACEHOLDER = "${receipt.internal.subscription-key}";

    @Autowired
    public ReceiptInternalFeignConfig(@Value(RECEIPT_SUBKEY_PLACEHOLDER) String subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
    }
}
