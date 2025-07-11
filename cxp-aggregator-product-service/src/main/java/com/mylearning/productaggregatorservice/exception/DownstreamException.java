package com.mylearning.productaggregatorservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class DownstreamException extends RuntimeException {
    public DownstreamException(String message) {
        super(message);
    }

    public DownstreamException(String message, Throwable cause) {
        super(message, cause);
    }


}
