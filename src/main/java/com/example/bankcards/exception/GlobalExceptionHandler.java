package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Global REST exception handler that standardizes error responses across the API.
 * <p>
 * Annotated with {@link org.springframework.web.bind.annotation.RestControllerAdvice},
 * this component captures exceptions thrown by controllers and produces consistent,
 * machine-readable error responses wrapped in an {@link ApiError} record.
 * <p>
 * Each error response includes:
 * <ul>
 *   <li>{@code type} — a short string identifier for the error category</li>
 *   <li>{@code status} — HTTP status code</li>
 *   <li>{@code message} — human-readable error message</li>
 *   <li>{@code path} — the request URI that triggered the error</li>
 *   <li>{@code timestamp} — the time the error occurred (ISO 8601)</li>
 *   <li>{@code details} — optional structured context, such as field validation errors</li>
 * </ul>
 *
 * <p><b>Example JSON response:</b>
 * <pre>
 * {
 *   "type": "validation_error",
 *   "status": 400,
 *   "message": "Request validation failed",
 *   "path": "/api/users",
 *   "timestamp": "2025-11-04T13:40:12.518Z",
 *   "details": {
 *     "fields": {
 *       "email": "must be a valid email address",
 *       "password": "must contain at least 8 characters"
 *     }
 *   }
 * }
 * </pre>
 *
 * @see org.springframework.web.bind.annotation.ExceptionHandler
 * @see org.springframework.web.bind.annotation.RestControllerAdvice
 * @see org.springframework.http.ResponseEntity
 * @see org.springframework.web.server.ResponseStatusException
 * @see org.springframework.web.bind.MethodArgumentNotValidException
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    record ApiError(String type, int status, String message, String path, OffsetDateTime timestamp,
                    Map<String, Object> details) {}


    /**
     * Handles {@link org.springframework.web.bind.MethodArgumentNotValidException}
     * thrown when request body validation fails.
     * <p>
     * Collects field-specific error messages and returns an {@link ApiError}
     * with {@code type="validation_error"} and HTTP 400 status.
     *
     * @param ex  the validation exception containing field errors
     * @param req the current web request context
     * @return {@link ResponseEntity} containing a structured {@link ApiError}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, org.springframework.web.context.request.WebRequest req) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        ApiError body = new ApiError(
                "validation_error",
                HttpStatus.BAD_REQUEST.value(),
                "Request validation failed",
                path(req),
                OffsetDateTime.now(),
                Map.of("fields", fieldErrors)
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }


    /**
     * Handles {@link org.springframework.web.server.ResponseStatusException}
     * thrown explicitly by controllers or services.
     * <p>
     * Returns an {@link ApiError} with {@code type="http_error"} and the status code
     * specified in the exception.
     *
     * @param ex  the exception containing an explicit HTTP status and message
     * @param req the current web request context
     * @return {@link ResponseEntity} containing a structured {@link ApiError}
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleRSE(ResponseStatusException ex, org.springframework.web.context.request.WebRequest req) {
        ApiError body = new ApiError(
                "http_error",
                ex.getStatusCode().value(),
                ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString(),
                path(req),
                OffsetDateTime.now(),
                Map.of()
        );
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }


    /**
     * Handles any unhandled {@link Exception} that occurs within the application.
     * <p>
     * Returns a generic {@link ApiError} with {@code type="server_error"}
     * and HTTP 500 status code.
     *
     * @param ex  the uncaught exception
     * @param req the current web request context
     * @return {@link ResponseEntity} containing a structured {@link ApiError}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, org.springframework.web.context.request.WebRequest req) {
        ApiError body = new ApiError(
                "server_error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Unexpected error",
                path(req),
                OffsetDateTime.now(),
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }


    /**
     * Extracts the request URI from a {@link org.springframework.web.context.request.WebRequest}.
     *
     * @param req the current web request
     * @return the extracted request path (e.g., {@code /api/users})
     */
    private String path(org.springframework.web.context.request.WebRequest req) {
        String d = req.getDescription(false); // like "uri=/api/..."
        return d != null && d.startsWith("uri=") ? d.substring(4) : d;
    }
}
