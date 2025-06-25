package com.mylearning.productdomainservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ProductDataLoadException extends RuntimeException {
    public ProductDataLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}