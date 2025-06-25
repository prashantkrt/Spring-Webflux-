package com.mylearning.productdomainservice.exception;

import org.springframework.http.HttpStatus;

public abstract class BaseDomainException extends RuntimeException {
    private final HttpStatus status;

    public BaseDomainException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}