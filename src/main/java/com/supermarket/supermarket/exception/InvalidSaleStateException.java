package com.supermarket.supermarket.exception;

public class InvalidSaleStateException extends RuntimeException {
    public InvalidSaleStateException(String message) {
        super(message);
    }
}