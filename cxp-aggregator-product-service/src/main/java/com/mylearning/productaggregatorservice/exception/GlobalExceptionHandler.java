package com.mylearning.productaggregatorservice.exception;

import com.mylearning.productaggregatorservice.dto.ApiError;
import com.mylearning.productaggregatorservice.dto.ApiResponse;
import com.mylearning.productaggregatorservice.dto.FieldValidationError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());

        List<FieldValidationError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> new FieldValidationError(
                        err.getField(),
                        String.valueOf(err.getRejectedValue()),
                        err.getDefaultMessage()))
                .toList();

        ApiError apiError = new ApiError("VALIDATION_ERROR", "Invalid input", fieldErrors);
        return buildErrorResponse(apiError, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());

        List<FieldValidationError> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(this::toFieldError)
                .toList();

        ApiError apiError = new ApiError("CONSTRAINT_VIOLATION", "Invalid parameter(s)", fieldErrors);
        return buildErrorResponse(apiError, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(DownstreamException.class)
    public ResponseEntity<ApiResponse<Object>> handleDownstreamException(DownstreamException ex) {
        log.error("Downstream exception: {}", ex.getMessage(), ex);
        ApiError apiError = new ApiError("DOWNSTREAM_FAILURE", ex.getMessage(), null);
        return buildErrorResponse(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ApiError apiError = new ApiError("INTERNAL_ERROR", "Something went wrong", null);
        return buildErrorResponse(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private FieldValidationError toFieldError(ConstraintViolation<?> violation) {
        String field = violation.getPropertyPath().toString();
        String rejected = violation.getInvalidValue() != null ? violation.getInvalidValue().toString() : "null";
        return new FieldValidationError(field, rejected, violation.getMessage());
    }

    private ResponseEntity<ApiResponse<Object>> buildErrorResponse(ApiError error, HttpStatus status) {
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .apiSuccess(false)
                .timeStamp(Instant.now())
                .data(null)
                .errors(List.of(error))
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
