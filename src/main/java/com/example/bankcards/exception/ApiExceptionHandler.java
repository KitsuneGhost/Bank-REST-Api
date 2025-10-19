package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

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

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String,Object> handleDenied(AccessDeniedException e) {
        return body(403, "Forbidden", Map.of("reason", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String,Object> handleNotFound(IllegalArgumentException e) {
        return body(404, "Not found", Map.of("reason", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String,Object> handleAny(Exception e) {
        return body(500, "Internal error", Map.of("reason", e.getMessage()));
    }

    private Map<String,Object> body(int status, String error, Map<String,?> details) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", status,
                "error", error,
                "details", details
        );
    }
}
