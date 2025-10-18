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

@RestControllerAdvice
public class GlobalExceptionHandler {
    record ApiError(String type, int status, String message, String path, OffsetDateTime timestamp,
                    Map<String, Object> details) {}

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

    private String path(org.springframework.web.context.request.WebRequest req) {
        String d = req.getDescription(false); // like "uri=/api/..."
        return d != null && d.startsWith("uri=") ? d.substring(4) : d;
    }
}
