package it.gov.pagopa.bizeventsservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.MediaType;

import feign.FeignException;
import it.gov.pagopa.bizeventsservice.config.feign.PDFGenerateReceiptFeignConfig;

@FeignClient(value = "generateReceiptPDF", url = "${service.generate.pdf.receipt.host}", configuration = PDFGenerateReceiptFeignConfig.class)
public interface IReceiptGeneratePDFClient {

	@Retryable(
			exclude = FeignException.FeignClientException.class,
			maxAttemptsExpression = "${generate.pdf.retry.maxAttempts}",
			backoff = @Backoff(delayExpression = "${generate.pdf.retry.maxDelay}"))
	@PostMapping(value = "${service.generate.pdf.receipt.path}", consumes = MediaType.APPLICATION_JSON_VALUE)
	String generateReceipt(@PathVariable("event-id") String eventId, @RequestParam("isCart") String isCart, @RequestBody String body);
}
