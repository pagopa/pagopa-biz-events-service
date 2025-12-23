package it.gov.pagopa.bizeventsservice.client;

import feign.FeignException;
import it.gov.pagopa.bizeventsservice.config.feign.PDFGenerateReceiptFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "generateReceiptPDF", url = "${service.generate.pdf.receipt.host}", configuration = PDFGenerateReceiptFeignConfig.class)
public interface IReceiptGeneratePDFClient {

    @Retryable(
            exclude = FeignException.FeignClientException.class,
            maxAttemptsExpression = "${generate.pdf.retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "${generate.pdf.retry.maxDelay}"))
    @PostMapping(value = "${service.generate.pdf.receipt.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    String generateReceipt(@PathVariable("event-id") String eventId, @RequestBody String body);

    @Retryable(
            exclude = FeignException.FeignClientException.class,
            maxAttemptsExpression = "${generate.pdf.retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "${generate.pdf.retry.maxDelay}"))
    @PostMapping(value = "${service.generate.pdf.cart-receipt.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
    String generateReceiptCart(@PathVariable("cart-id") String eventId, @RequestBody String body);
}
