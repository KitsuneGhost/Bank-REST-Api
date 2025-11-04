package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


/**
 * Global REST exception handler that standardizes error responses across the API.
 * <p>
 * Annotated with {@link org.springframework.web.bind.annotation.RestControllerAdvice},
 * this component intercepts exceptions thrown by controllers and services,
 * returning consistent JSON error bodies with appropriate HTTP status codes.
 * <p>
 * Each handler method builds a structured response containing:
 * <ul>
 *   <li>{@code timestamp} — ISO 8601 timestamp of the error occurrence</li>
 *   <li>{@code status} — HTTP status code</li>
 *   <li>{@code error} — short description of the error type</li>
 *   <li>{@code details} — context-specific information (e.g., validation messages)</li>
 * </ul>
 *
 * <p><b>Example JSON error response:</b>
 * <pre>
 * {
 *   "timestamp": "2025-11-04T10:23:45.123Z",
 *   "status": 400,
 *   "error": "Validation failed",
 *   "details": {
 *     "email": "must be a valid email address",
 *     "password": "must contain at least 8 characters"
 *   }
 * }
 * </pre>
 *
 * @see org.springframework.web.bind.annotation.ExceptionHandler
 * @see org.springframework.web.bind.annotation.RestControllerAdvice
 * @see org.springframework.validation.FieldError
 * @see org.springframework.web.bind.MethodArgumentNotValidException
 */
@RestControllerAdvice
public class ApiExceptionHandler {


    /**
     * Handles validation errors triggered by {@link jakarta.validation.Valid} or
     * {@link org.springframework.web.bind.annotation.RequestBody} constraints.
     * <p>
     * Collects field-level validation messages from {@link MethodArgumentNotValidException}
     * and returns a structured response with HTTP 400 (Bad Request).
     *
     * @param ex the exception containing validation errors
     * @return a response body with validation messages for each invalid field
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,Object> handleValidation(MethodArgumentNotValidException ex) {
        Map<String,String> details = new HashMap<>();
        for (var e : ex.getBindingResult().getAllErrors()) {
            String field = e instanceof FieldError fe ? fe.getField() : e.getObjectName();
            details.put(field, e.getDefaultMessage());
        }
        return body(400, "Validation failed", details);
    }


    /**
     * Handles {@link org.springframework.security.access.AccessDeniedException}
     * thrown when an authenticated user attempts an action they are not authorized for.
     * <p>
     * Returns HTTP 403 (Forbidden) with the reason included in the response.
     *
     * @param e the exception indicating insufficient permissions
     * @return a structured error body with status and reason
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String,Object> handleDenied(AccessDeniedException e) {
        return body(403, "Forbidden", Map.of("reason", e.getMessage()));
    }


    /**
     * Handles {@link IllegalArgumentException} exceptions,
     * commonly thrown when a requested resource cannot be found or input is invalid.
     * <p>
     * Returns HTTP 404 (Not Found) with the exception message included in the details.
     *
     * @param e the exception indicating an invalid or missing resource
     * @return structured error response with status 404
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String,Object> handleNotFound(IllegalArgumentException e) {
        return body(404, "Not found", Map.of("reason", e.getMessage()));
    }


    /**
     * Handles any unhandled exceptions not covered by other specific handlers.
     * <p>
     * Returns HTTP 500 (Internal Server Error) and includes the exception message
     * for diagnostic purposes.
     *
     * @param e the uncaught exception
     * @return structured error response with status 500
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String,Object> handleAny(Exception e) {
        return body(500, "Internal error", Map.of("reason", e.getMessage()));
    }


    /**
     * Builds a standardized error response map with the given status, message, and details.
     * <p>
     * This utility method ensures all error responses share the same structure.
     *
     * @param status  HTTP status code
     * @param error   short description of the error
     * @param details additional error details (may be a map of field messages)
     * @return map representing the structured error body
     */
    private Map<String,Object> body(int status, String error, Map<String,?> details) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", status,
                "error", error,
                "details", details
        );
    }
}
