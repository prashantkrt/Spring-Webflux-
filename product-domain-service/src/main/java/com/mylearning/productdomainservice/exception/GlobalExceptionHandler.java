package com.mylearning.productdomainservice.exception;

import com.mylearning.productdomainservice.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleProductNotFound(ProductNotFoundException ex, ServerWebExchange exchange) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ApiResponse<>(Instant.now(), 404, ex.getMessage(), exchange.getRequest().getPath().value(), null)
        );
    }
}