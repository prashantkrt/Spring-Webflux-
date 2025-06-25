package com.mylearning.productdomainservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseDomainException extends RuntimeException {
    private final HttpStatus status;

    protected BaseDomainException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}