package com.supermarket.supermarket.exception;

public class RateLimitServiceException extends RuntimeException {
    public RateLimitServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}