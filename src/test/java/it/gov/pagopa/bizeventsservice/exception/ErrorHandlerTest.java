package it.gov.pagopa.bizeventsservice.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import it.gov.pagopa.bizeventsservice.model.ProblemJson;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();
    private final WebRequest request = mock(WebRequest.class);

    @Test
    void handleHttpMessageNotReadable() {
        var exception = mockThrowable(HttpMessageNotReadableException.class);

        when(exception.getMessage()).thenReturn("Invalid JSON");

        var response =
                errorHandler.handleHttpMessageNotReadable(
                        exception,
                        new HttpHeaders(),
                        HttpStatus.BAD_REQUEST,
                        request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        var body = (ProblemJson) response.getBody();

        assertNotNull(body);
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.getStatus());
        assertEquals(ErrorHandler.BAD_REQUEST, body.getTitle());
        assertEquals("Invalid input format", body.getDetail());
        assertEquals(ErrorCode.GN_400_002.name(), body.getCode());
    }

    @Test
    void handleMissingServletRequestParameter() {
        var exception =
                new MissingServletRequestParameterException(
                        "fiscalCode",
                        "String");

        var response =
                errorHandler.handleMissingServletRequestParameter(
                        exception,
                        new HttpHeaders(),
                        HttpStatus.BAD_REQUEST,
                        request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        var body = (ProblemJson) response.getBody();

        assertNotNull(body);
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.getStatus());
        assertEquals(ErrorHandler.BAD_REQUEST, body.getTitle());
        assertEquals(exception.getMessage(), body.getDetail());
        assertEquals(ErrorCode.GN_400_003.name(), body.getCode());
    }

    @Test
    void handleTypeMismatch() {
        var exception = mockThrowable(MethodArgumentTypeMismatchException.class);

        when(exception.getMessage()).thenReturn("Type mismatch");
        when(exception.getValue()).thenReturn("invalid");
        when(exception.getName()).thenReturn("size");

        var response =
                errorHandler.handleTypeMismatch(
                        exception,
                        new HttpHeaders(),
                        HttpStatus.BAD_REQUEST,
                        request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        var body = (ProblemJson) response.getBody();

        assertNotNull(body);
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.getStatus());
        assertEquals(ErrorHandler.BAD_REQUEST, body.getTitle());
        assertEquals(
                "Invalid value invalid for property size",
                body.getDetail());
        assertEquals(ErrorCode.GN_400_004.name(), body.getCode());
    }

    @Test
    void handleMethodArgumentNotValid() {
        var exception = mock(MethodArgumentNotValidException.class);
        var bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors())
                .thenReturn(
                        List.of(
                                new FieldError(
                                        "request",
                                        "fiscalCode",
                                        "must not be blank")));

        var response =
                errorHandler.handleMethodArgumentNotValid(
                        exception,
                        new HttpHeaders(),
                        HttpStatus.BAD_REQUEST,
                        request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        var body = (ProblemJson) response.getBody();

        assertNotNull(body);
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.getStatus());
        assertEquals(ErrorHandler.BAD_REQUEST, body.getTitle());
        assertEquals(
                "fiscalCode: must not be blank",
                body.getDetail());
        assertEquals(ErrorCode.GN_400_005.name(), body.getCode());
    }

    @Test
    void handleAppExceptionWith4xxStatus() {
        var exception = mockThrowable(AppException.class);

        when(exception.getHttpStatus()).thenReturn(HttpStatus.BAD_REQUEST);
        when(exception.getCode()).thenReturn(ErrorCode.GN_400_003);
        when(exception.getTitle()).thenReturn("Bad request");
        when(exception.getMessage()).thenReturn("Invalid fiscal code");

        var response =
                errorHandler.handleAppException(
                        exception,
                        request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        var body = response.getBody();

        assertNotNull(body);
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.getStatus());
        assertEquals("Bad request", body.getTitle());
        assertEquals("Invalid fiscal code", body.getDetail());
        assertEquals(ErrorCode.GN_400_003.name(), body.getCode());
    }

    @Test
    void handleAppExceptionWith5xxStatus() {
        var exception = mockThrowable(AppException.class);

        when(exception.getHttpStatus())
                .thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(exception.getCode())
                .thenReturn(ErrorCode.GN_500_003);
        when(exception.getTitle())
                .thenReturn("Internal server error");
        when(exception.getMessage())
                .thenReturn("Unexpected error");

        var response =
                errorHandler.handleAppException(
                        exception,
                        request);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode());

        var body = response.getBody();

        assertNotNull(body);
        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                body.getStatus());
        assertEquals("Internal server error", body.getTitle());
        assertEquals("Unexpected error", body.getDetail());
        assertEquals(ErrorCode.GN_500_003.name(), body.getCode());
    }

    @Test
    void handleFeignException() throws JsonProcessingException {
        FeignException feignException =
                FeignException.errorStatus(
                        "testMethod",
                        Response.builder()
                                .status(400)
                                .reason("Bad Request")
                                .request(
                                        Request.create(
                                                Request.HttpMethod.GET,
                                                "/test",
                                                Collections.emptyMap(),
                                                null,
                                                StandardCharsets.UTF_8,
                                                new RequestTemplate()))
                                .body(
                                        "{}".getBytes(
                                                StandardCharsets.UTF_8))
                                .build());

        var response =
                errorHandler.handleFeignException(
                        feignException,
                        request);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode());
    }

    @Test
    void handleGenericException() {
        var exception =
                new RuntimeException("Unexpected error");

        var response =
                errorHandler.handleGenericException(
                        exception,
                        request);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode());

        var body = response.getBody();

        assertNotNull(body);
        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                body.getStatus());
        assertEquals(
                ErrorHandler.INTERNAL_SERVER_ERROR,
                body.getTitle());
        assertEquals("Unexpected error", body.getDetail());
        assertEquals(ErrorCode.GN_500_003.name(), body.getCode());
    }

    /**
     * Creates a Mockito mock of a Throwable while preserving the internal
     * collections expected by logging frameworks.
     */
    private static <T extends Throwable> T mockThrowable(Class<T> type) {
        T throwable = mock(type);

        doReturn(new Throwable[0])
                .when(throwable)
                .getSuppressed();

        doReturn(new StackTraceElement[0])
                .when(throwable)
                .getStackTrace();

        return throwable;
    }
}