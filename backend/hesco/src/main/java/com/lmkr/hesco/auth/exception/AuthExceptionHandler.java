package com.lmkr.hesco.auth.exception;

import com.lmkr.hesco.auth.api.AuthController;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Scoped to AuthController only (basePackageClasses) — deliberately NOT
 * added to the existing shared GlobalExceptionHandler, since this patch
 * doesn't have that file's current content to edit safely. If HESCO
 * already has a shared error-envelope shape, fold these three handlers
 * into it by hand instead of keeping this separate class.
 */
@RestControllerAdvice(basePackageClasses = AuthController.class)
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(InactiveAccountException.class)
    public ResponseEntity<Map<String, Object>> handleInactiveAccount(InactiveAccountException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(MobileLoginNotAllowedException.class)
    public ResponseEntity<Map<String, Object>> handleMobileLoginNotAllowed(MobileLoginNotAllowedException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(Map.of("status", status.value(), "message", message, "timestamp", Instant.now().toString()));
    }
}