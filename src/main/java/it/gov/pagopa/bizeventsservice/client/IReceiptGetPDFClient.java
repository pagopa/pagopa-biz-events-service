package it.gov.pagopa.bizeventsservice.client;

import feign.FeignException;
import it.gov.pagopa.bizeventsservice.config.feign.PDFGetReceiptFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "getReceiptPDF", url = "${service.get.pdf.receipt.host}", configuration = PDFGetReceiptFeignConfig.class)
public interface IReceiptGetPDFClient {

    @Retryable(
            exclude = FeignException.FeignClientException.class,
            maxAttemptsExpression = "${get.pdf.retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "${get.pdf.retry.maxDelay}"))
    @GetMapping(value = "/messages/{third-party-id}/pdf")
    ResponseEntity<byte[]> getReceiptPdf(@PathVariable("third-party-id") String thirdPartyId, @RequestParam("fiscal_code") String fiscalCode);

}
