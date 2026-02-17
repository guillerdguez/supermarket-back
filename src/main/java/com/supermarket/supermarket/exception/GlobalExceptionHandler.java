package com.supermarket.supermarket.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> buildResponse(String error, Object message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("error", error);
        response.put("message", message);
        response.put("status", status.value());
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing));
        return buildResponse("Validation Failed", fieldErrors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidToken(InvalidTokenException ex) {
        log.warn("Invalid token: {}", ex.getMessage());
        return buildResponse("Unauthorized", ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredJwt(ExpiredJwtException ex) {
        log.warn("Expired JWT token");
        return buildResponse("Unauthorized", "Token has expired", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({MalformedJwtException.class, SignatureException.class})
    public ResponseEntity<Map<String, Object>> handleMalformedJwt(Exception ex) {
        log.warn("Malformed JWT token: {}", ex.getMessage());
        return buildResponse("Unauthorized", "Invalid token format", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials provided");
        return buildResponse("Unauthorized", "Invalid email or password", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return buildResponse("Unauthorized", "Authentication failed", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return buildResponse("Forbidden", "You don't have permission to access this resource", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InsufficientPermissionsException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientPermissions(InsufficientPermissionsException ex) {
        log.warn("Insufficient permissions: {}", ex.getMessage());
        return buildResponse("Forbidden", ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse("Not Found", ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(InsufficientStockException ex) {
        log.warn("Inventory conflict: {}", ex.getMessage());
        return buildResponse("Inventory Conflict", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({InvalidSaleStateException.class, InvalidOperationException.class})
    public ResponseEntity<Map<String, Object>> handleInvalidOperations(RuntimeException ex) {
        log.warn("Invalid operation attempt: {}", ex.getMessage());
        return buildResponse("Invalid Operation", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResource(DuplicateResourceException ex) {
        log.warn("Duplicate record: {}", ex.getMessage());
        return buildResponse("Conflict", ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleConcurrencyError(ObjectOptimisticLockingFailureException ex) {
        log.warn("Concurrency conflict: {}", ex.getMessage());
        return buildResponse("Concurrency Conflict",
                "The resource was modified by another process. Please refresh and try again.",
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.error("Database integrity error: {}", ex.getMessage());
        return buildResponse("Data Integrity Error",
                "Cannot complete operation. The record is linked to other existing data.",
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler({RateLimitExceededException.class, RateLimitServiceException.class})
    public ResponseEntity<Map<String, Object>> handleRateLimitExceeded(Exception ex) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());

        String message = ex.getMessage();
        if (ex instanceof RateLimitServiceException && ex.getCause() != null) {
            message = ex.getCause().getMessage();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("error", "Too Many Requests");
        response.put("message", message);
        response.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        response.put("retryAfter", "300");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        log.error("Unexpected error occurred: ", ex);
        return buildResponse("Internal Server Error",
                "An unexpected error occurred. Please contact support.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}