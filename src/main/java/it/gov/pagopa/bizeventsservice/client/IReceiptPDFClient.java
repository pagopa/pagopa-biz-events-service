package it.gov.pagopa.bizeventsservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import feign.FeignException;
import it.gov.pagopa.bizeventsservice.config.feign.PDFReceiptFeignConfig;
import it.gov.pagopa.bizeventsservice.model.response.AttachmentsDetailsResponse;

@FeignClient(value = "receiptPDF", url = "${service.pdf.receipt.host}", configuration = PDFReceiptFeignConfig.class)
public interface IReceiptPDFClient {

	@Retryable(
			exclude = FeignException.FeignClientException.class,
			maxAttemptsExpression = "${retry.maxAttempts}",
			backoff = @Backoff(delayExpression = "${retry.maxDelay}"))
	@GetMapping(value = "/messages/{id}")
	AttachmentsDetailsResponse getAttachments(@RequestHeader("fiscal_code") String fiscalCode, @PathVariable("id") String id);

	@Retryable(
			exclude = FeignException.FeignClientException.class,
			maxAttemptsExpression = "${retry.maxAttempts}",
			backoff = @Backoff(delayExpression = "${retry.maxDelay}"))
	@GetMapping(value = "/messages/{id}/{attachment_url}")
	byte[] getReceipt(@RequestHeader("fiscal_code") String fiscalCode, @PathVariable("id") String id, @PathVariable("attachment_url") String attachmentUrl);
}
