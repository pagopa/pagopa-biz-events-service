package it.gov.pagopa.bizeventsservice.exception;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;

class ErrorHandlerTest {

    @Test
    void handleFeignException() {
        var errorHandler = new ErrorHandler();
        FeignException feignException = Mockito.mock(FeignException.class);
        WebRequest request = Mockito.mock(WebRequest.class);
        var response = errorHandler.handleFeignException(feignException, request);
        assertEquals(response.getStatusCode() , HttpStatus.BAD_GATEWAY);
    }
}