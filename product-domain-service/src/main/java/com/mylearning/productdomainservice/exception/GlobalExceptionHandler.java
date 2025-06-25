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

    @ExceptionHandler(BaseDomainException.class)
    public ResponseEntity<ApiResponse<String>> handleBaseException(BaseDomainException ex, ServerWebExchange exchange) {
        return ResponseEntity.status(ex.getStatus()).body(
                new ApiResponse<>(Instant.now(), ex.getStatus().value(), ex.getMessage(), exchange.getRequest().getPath().value(), null)
        );
    }
}