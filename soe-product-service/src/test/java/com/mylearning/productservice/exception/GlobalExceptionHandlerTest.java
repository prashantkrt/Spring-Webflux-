package com.mylearning.productservice.exception;

import com.mylearning.productservice.dto.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private ConstraintViolation<Object> constraintViolation;

    @Mock
    private Path path;

    private MockServerWebExchange exchange;

    @BeforeEach
    void setUp() {
        exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/products"));
    }

    @Test
    void handleAggregatorException_ShouldReturnServiceUnavailable() {
        // Given
        String errorMessage = "Service temporarily unavailable";
        AggregatorUnavailableException ex = new AggregatorUnavailableException(errorMessage);

        // When
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleAggregatorException(ex, exchange);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertFalse(response.getBody().getErrors().isEmpty());
        assertEquals(errorMessage, response.getBody().getErrors().get(0).getMessage());
    }

    @Test
    void handleWebClientError_ShouldReturnInternalServerError() {
        // Given
        String responseBody = "Error from external service";
        WebClientResponseException ex = mock(WebClientResponseException.class);
        when(ex.getResponseBodyAsString()).thenReturn(responseBody);

        // When
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleWebClientError(ex, exchange);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Aggregator error", response.getBody().getMessage());
        assertFalse(response.getBody().getErrors().isEmpty());
        assertEquals(responseBody, response.getBody().getErrors().get(0).getMessage());
    }

    @Test
    void handleConstraintViolation_ShouldReturnBadRequest() {
        // Given
        ConstraintViolationException ex = mock(ConstraintViolationException.class);
        when(ex.getConstraintViolations()).thenReturn(Set.of(constraintViolation));
        when(constraintViolation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("fieldName");
        when(constraintViolation.getMessage()).thenReturn("must not be null");

        // When
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleConstraintViolation(ex, exchange);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertFalse(response.getBody().getErrors().isEmpty());
        assertTrue(response.getBody().getErrors().get(0).getMessage().contains("must not be null"));
        assertNotNull(response.getBody().getFieldErrors());
        assertTrue(response.getBody().getFieldErrors().containsKey("fieldName"));
    }

    @Test
    void handleInvalidArgs_ShouldReturnBadRequest() {
        // Given
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        FieldError fieldError = new FieldError("objectName", "fieldName", "must not be empty");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // When
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleInvalidArgs(methodArgumentNotValidException, exchange);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertFalse(response.getBody().getErrors().isEmpty());
        assertTrue(response.getBody().getErrors().get(0).getMessage().contains("must not be empty"));
        assertNotNull(response.getBody().getFieldErrors());
        assertTrue(response.getBody().getFieldErrors().containsKey("fieldName"));
    }

    @Test
    void handleGeneralError_ShouldReturnInternalServerError() {
        // Given
        Exception ex = new RuntimeException("Unexpected error occurred");

        // When
        ResponseEntity<ApiResponse<Object>> response = 
            globalExceptionHandler.handleGeneralError(ex, exchange);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unexpected error", response.getBody().getMessage());
        assertFalse(response.getBody().getErrors().isEmpty());
        assertEquals("Unexpected error occurred", response.getBody().getErrors().get(0).getMessage());
    }

    // Note: buildErrorResponse is a private method and should not be tested directly.
    // Its functionality is already covered by testing the public exception handler methods.
}
