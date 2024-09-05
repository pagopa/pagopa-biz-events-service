package it.gov.pagopa.bizeventsservice.client;

import feign.FeignException;
import it.gov.pagopa.bizeventsservice.config.feign.PDFGetReceiptFeignConfig;
import it.gov.pagopa.bizeventsservice.model.response.AttachmentsDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "getReceiptPDF", url = "${service.get.pdf.receipt.host}", configuration = PDFGetReceiptFeignConfig.class)
public interface IReceiptGetPDFClient {

    @Retryable(
            exclude = FeignException.FeignClientException.class,
            maxAttemptsExpression = "${get.pdf.retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "${get.pdf.retry.maxDelay}"))
    @GetMapping(value = "/messages/{id}")
    AttachmentsDetailsResponse getAttachments(@RequestHeader("fiscal_code") String fiscalCode, @PathVariable("id") String id);

    @Retryable(
            exclude = FeignException.FeignClientException.class,
            maxAttemptsExpression = "${get.pdf.retry.maxAttempts}",
            backoff = @Backoff(delayExpression = "${get.pdf.retry.maxDelay}"))
    @GetMapping(value = "/messages/{id}/{attachment_url}")
    byte[] getReceipt(@RequestHeader("fiscal_code") String fiscalCode, @PathVariable("id") String id, @PathVariable("attachment_url") String attachmentUrl);
}
