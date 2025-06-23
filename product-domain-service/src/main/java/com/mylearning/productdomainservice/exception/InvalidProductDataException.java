package com.mylearning.productdomainservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class InvalidProductDataException extends RuntimeException {
    public InvalidProductDataException(String message, Throwable cause) {
        super(message, cause);
    }
}