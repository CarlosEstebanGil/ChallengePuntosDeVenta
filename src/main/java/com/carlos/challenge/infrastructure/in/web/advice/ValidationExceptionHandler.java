package com.carlos.challenge.infrastructure.in.web.advice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ValidationExceptionHandler {

    public static final String INVALID = "Invalid";
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String MALFORMED_JSON_REQUEST = "Malformed JSON request";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleBodyValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> Optional.ofNullable(fe.getDefaultMessage()).orElse(INVALID),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        return ResponseEntity.badRequest()
                .body(ApiError.of(BAD_REQUEST, VALIDATION_FAILED, req.getRequestURI(), errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleParamValidation(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        return ResponseEntity.badRequest()
                .body(ApiError.of(BAD_REQUEST, VALIDATION_FAILED, req.getRequestURI(), errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest()
                .body(ApiError.of(BAD_REQUEST, MALFORMED_JSON_REQUEST, req.getRequestURI()));
    }

    public static record ApiError(String code, String message, String path, Instant timestamp, Map<String, String> errors) {
        public static ApiError of(String code, String message, String path) {
            return new ApiError(code, message, path, Instant.now(), null);
        }
        public static ApiError of(String code, String message, String path, Map<String, String> errors) {
            return new ApiError(code, message, path, Instant.now(), errors);
        }
    }
}
