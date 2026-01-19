package com.supermarket.supermarket.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<Map<String, Object>> buildResponse(String error, Object message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("error", error);
        response.put("message", message);
        response.put("status", status.value());
        return ResponseEntity.status(status).body(response);
    }

    // 1. Validation Errors (@Valid) - HTTP 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> error.getDefaultMessage(),
                        (existing, replacement) -> existing));

        return buildResponse("Validation Failed", fieldErrors, HttpStatus.BAD_REQUEST);
    }

    // 2. Resource Not Found (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse("Not Found", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 3. Insufficient Stock (400)
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(InsufficientStockException ex) {
        log.warn("Inventory conflict: {}", ex.getMessage());
        return buildResponse("Inventory Conflict", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 4. Duplicate Resources (409)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResource(DuplicateResourceException ex) {
        log.warn("Duplicate record: {}", ex.getMessage());
        return buildResponse("Conflict", ex.getMessage(), HttpStatus.CONFLICT);
    }

    // 5. Invalid Sale State or Operation (400)
    @ExceptionHandler({ InvalidSaleStateException.class, InvalidOperationException.class })
    public ResponseEntity<Map<String, Object>> handleInvalidOperations(RuntimeException ex) {
        log.warn("Invalid operation attempt: {}", ex.getMessage());
        return buildResponse("Invalid Operation", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 6. Concurrency Control (409) - Critical for Supermarkets
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleConcurrencyError(ObjectOptimisticLockingFailureException ex) {
        log.error("Concurrency conflict: Product stock was updated by another process.");
        return buildResponse("Conflict", "The record was updated by another user. Please refresh and try again.",
                HttpStatus.CONFLICT);
    }

    // 7. Database Integrity (409)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("Database integrity error: {}", ex.getMessage());
        return buildResponse("Data Integrity Error",
                "Cannot complete operation. The record is linked to other existing data.",
                HttpStatus.CONFLICT);
    }

    // 8. General Internal Server Error (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        log.error("Unexpected error occurred: ", ex);
        return buildResponse("Internal Server Error",
                "An unexpected error occurred. Please contact support.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}