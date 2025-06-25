package com.mylearning.productaggregatorservice.exception;

import com.mylearning.productaggregatorservice.dto.ApiError;
import com.mylearning.productaggregatorservice.dto.ApiResponse;
import com.mylearning.productaggregatorservice.dto.FieldValidationError;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
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
    private ConstraintViolationException constraintViolationException;

    @Mock
    private ConstraintViolation<Object> constraintViolation;

    @Mock
    private Path path;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(methodArgumentNotValidException, bindingResult, constraintViolationException, constraintViolation, path);
    }

    @Test
    void handleMethodArgumentNotValid_ShouldReturnBadRequest() {
        // Given
        FieldError fieldError = new FieldError("objectName", "fieldName", "defaultMessage");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler
                .handleMethodArgumentNotValid(methodArgumentNotValidException);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isApiSuccess());
        assertNotNull(response.getBody().getErrors());
        assertEquals(1, response.getBody().getErrors().size());
        
        ApiError<?> error = response.getBody().getErrors().get(0);
        assertEquals("VALIDATION_ERROR", error.getCode());
        assertEquals("Invalid input", error.getMessage());
        assertNotNull(error.getFieldErrors());
        assertTrue(error.getFieldErrors() instanceof List);
        
        List<FieldValidationError> fieldErrors = error.getFieldErrors();
        assertEquals(1, fieldErrors.size());
        assertEquals("fieldName", fieldErrors.get(0).getField());
        assertEquals("defaultMessage", fieldErrors.get(0).getMessage());
    }

    @Test
    void handleConstraintViolation_ShouldReturnBadRequest() {
        // Given
        when(constraintViolation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("fieldName");
        when(constraintViolation.getInvalidValue()).thenReturn("invalidValue");
        when(constraintViolation.getMessage()).thenReturn("must not be null");
        when(constraintViolationException.getConstraintViolations())
                .thenReturn(Set.of(constraintViolation));

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler
                .handleConstraintViolation(constraintViolationException);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isApiSuccess());
        assertNotNull(response.getBody().getErrors());
        assertEquals(1, response.getBody().getErrors().size());
        
        ApiError<?> error = response.getBody().getErrors().get(0);
        assertEquals("CONSTRAINT_VIOLATION", error.getCode());
        assertEquals("Invalid parameter(s)", error.getMessage());
        assertNotNull(error.getFieldErrors());
        assertTrue(error.getFieldErrors() instanceof List);
        
        List<FieldValidationError> fieldErrors = error.getFieldErrors();
        assertEquals(1, fieldErrors.size());
        assertEquals("fieldName", fieldErrors.get(0).getField());
        assertEquals("must not be null", fieldErrors.get(0).getMessage());
        assertEquals("invalidValue", fieldErrors.get(0).getRejectedValue());
    }

    @Test
    void handleDownstreamException_ShouldReturnInternalServerError() {
        // Given
        String errorMessage = "Downstream service unavailable";
        DownstreamException ex = new DownstreamException(errorMessage);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler
                .handleDownstreamException(ex);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isApiSuccess());
        assertNotNull(response.getBody().getErrors());
        assertEquals(1, response.getBody().getErrors().size());
        
        ApiError<?> error = response.getBody().getErrors().get(0);
        assertEquals("DOWNSTREAM_FAILURE", error.getCode());
        assertEquals(errorMessage, error.getMessage());
        assertNull(error.getFieldErrors());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        // Given
        String errorMessage = "Unexpected error occurred";
        Exception ex = new Exception(errorMessage);

        // When
        ResponseEntity<ApiResponse<Object>> response = globalExceptionHandler
                .handleGeneric(ex);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isApiSuccess());
        assertNotNull(response.getBody().getErrors());
        assertEquals(1, response.getBody().getErrors().size());
        
        ApiError<?> error = response.getBody().getErrors().get(0);
        assertEquals("INTERNAL_ERROR", error.getCode());
        assertEquals("Something went wrong", error.getMessage());
        assertNull(error.getFieldErrors());
    }

    @Test
    void toFieldError_ShouldConvertConstraintViolationToFieldError() {
        // Given
        when(constraintViolation.getPropertyPath()).thenReturn(path);
        when(path.toString()).thenReturn("testField");
        when(constraintViolation.getInvalidValue()).thenReturn("invalid");
        when(constraintViolation.getMessage()).thenReturn("must be valid");

        // When
        FieldValidationError error = (FieldValidationError) ReflectionTestUtils.invokeMethod(
                globalExceptionHandler, "toFieldError", constraintViolation);

        // Then
        assertNotNull(error);
        assertEquals("testField", error.getField());
        assertEquals("invalid", error.getRejectedValue());
        assertEquals("must be valid", error.getMessage());
    }

    @Test
    void buildErrorResponse_ShouldCreateProperResponse() {
        // Given
        ApiError<Object> apiError = new ApiError<>("TEST_CODE", "Test message", Collections.emptyList());
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // When
        ResponseEntity<ApiResponse<Object>> response = (ResponseEntity<ApiResponse<Object>>) 
                ReflectionTestUtils.invokeMethod(globalExceptionHandler, "buildErrorResponse", apiError, status);

        // Then
        assertNotNull(response);
        assertEquals(status, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isApiSuccess());
        assertNotNull(response.getBody().getErrors());
        assertEquals(1, response.getBody().getErrors().size());
        assertEquals(apiError, response.getBody().getErrors().get(0));
        assertNotNull(response.getBody().getTimeStamp());
    }
}
