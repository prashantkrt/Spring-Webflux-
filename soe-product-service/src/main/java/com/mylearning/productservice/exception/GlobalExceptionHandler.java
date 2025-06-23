package com.mylearning.productservice.exception;

import com.mylearning.productservice.dto.ApiError;
import com.mylearning.productservice.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AggregatorUnavailableException.class)
    public ResponseEntity<ApiResponse<Object>> handleAggregatorException(
            AggregatorUnavailableException ex,
            ServerWebExchange exchange) {

        List<ApiError> errorList = ex.getErrors() != null
                ? ex.getErrors()
                : List.of(ApiError.builder().message(ex.getMessage()).build());

        return buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                exchange,
                ex.getMessage(),
                errorList,
                null
        );
    }

    @ExceptionHandler(WebClientResponseException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse<Object>> handleWebClientError(
            WebClientResponseException ex, ServerWebExchange exchange) {

        List<ApiError> errorList = List.of(ApiError.builder()
                .message(ex.getResponseBodyAsString())
                .build());

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exchange,
                "Aggregator error",
                errorList,
                null
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
            ConstraintViolationException ex, ServerWebExchange exchange) {

        List<ApiError> errorList = ex.getConstraintViolations().stream()
                .map(v -> ApiError.builder()
                        .message(v.getPropertyPath() + ": " + v.getMessage())
                        .build())
                .collect(Collectors.toList());

        Map<String, String> fieldErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage(),
                        (msg1, msg2) -> msg1
                ));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, exchange, "Validation failed", errorList, fieldErrors);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Object>> handleInvalidArgs(
            MethodArgumentNotValidException ex, ServerWebExchange exchange) {

        List<ApiError> errorList = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> ApiError.builder()
                        .message(e.getField() + ": " + e.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        e -> e.getField(),
                        e -> e.getDefaultMessage(),
                        (msg1, msg2) -> msg1
                ));

        return buildErrorResponse(HttpStatus.BAD_REQUEST, exchange, "Validation failed", errorList, fieldErrors);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse<Object>> handleGeneralError(
            Exception ex, ServerWebExchange exchange) {

        log.error("Unexpected error occurred", ex);

        List<ApiError> errorList = List.of(ApiError.builder()
                .message(ex.getMessage())
                .build());

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exchange,
                "Unexpected error",
                errorList,
                null
        );
    }

    private ResponseEntity<ApiResponse<Object>> buildErrorResponse(
            HttpStatus status,
            ServerWebExchange exchange,
            String message,
            List<ApiError> errors,
            Map<String, String> fieldErrors) {

        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .timestamp(Instant.now())
                .status(status.value())
                .path(exchange.getRequest().getPath().value())
                .message(message)
                .data(null)
                .errors(errors)
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
