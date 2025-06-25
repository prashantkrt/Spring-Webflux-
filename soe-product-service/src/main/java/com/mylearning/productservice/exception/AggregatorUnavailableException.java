package com.mylearning.productservice.exception;

import com.mylearning.productservice.dto.ApiError;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@Getter
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class AggregatorUnavailableException extends RuntimeException {

    private final transient List<ApiError> errors;

    public AggregatorUnavailableException(String message) {
        super(message);
        this.errors = null;
    }

    public AggregatorUnavailableException(String message, Throwable cause) {
        super(message, cause);
        this.errors = null;
    }

    public AggregatorUnavailableException(String message, List<ApiError> errors) {
        super(message);
        this.errors = errors;
    }
}