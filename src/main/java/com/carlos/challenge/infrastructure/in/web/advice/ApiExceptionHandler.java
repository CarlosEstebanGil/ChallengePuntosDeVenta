package com.carlos.challenge.infrastructure.in.web.advice;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({ IllegalArgumentException.class, NoSuchElementException.class })
    public ResponseEntity<ApiError> handleNotFound(RuntimeException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of("NOT_FOUND", ex.getMessage(), req.getRequestURI()));
    }

    public static record ApiError(String code, String message, String path, Instant timestamp) {
        public static ApiError of(String code, String message, String path) {
            return new ApiError(code, message, path, Instant.now());
        }
    }

}


