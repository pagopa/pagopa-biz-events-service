package it.gov.pagopa.bizeventsservice.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ErrorHandlerTest {

    @Test
    void handleFeignException() throws JsonProcessingException {
        var errorHandler = new ErrorHandler();
        FeignException feignException = Mockito.mock(FeignException.class);
        when(feignException.contentUTF8()).thenReturn("{}");
        when(feignException.status()).thenReturn(400);
        WebRequest request = Mockito.mock(WebRequest.class);
        var response = errorHandler.handleFeignException(feignException, request);
        assertEquals(HttpStatus.BAD_REQUEST , response.getStatusCode());
    }
}