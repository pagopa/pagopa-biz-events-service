package it.gov.pagopa.bizeventsservice.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorHandlerTest {

    @Test
    void handleFeignException() throws JsonProcessingException {
        var errorHandler = new ErrorHandler();

        FeignException feignException = FeignException.errorStatus(
                "testMethod",
                Response.builder()
                        .status(400)
                        .reason("Bad Request")
                        .request(Request.create(
                                Request.HttpMethod.GET,
                                "/test",
                                Collections.emptyMap(),
                                null,
                                StandardCharsets.UTF_8,
                                new RequestTemplate()
                        ))
                        .body("{}".getBytes(StandardCharsets.UTF_8))
                        .build()
        );

        WebRequest request = Mockito.mock(WebRequest.class);

        var response = errorHandler.handleFeignException(feignException, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}