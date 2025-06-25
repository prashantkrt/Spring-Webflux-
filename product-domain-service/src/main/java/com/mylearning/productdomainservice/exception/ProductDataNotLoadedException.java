package com.mylearning.productdomainservice.exception;

public class ProductDataNotLoadedException extends RuntimeException {
    public ProductDataNotLoadedException(String message) {
        super(message);
    }
}
