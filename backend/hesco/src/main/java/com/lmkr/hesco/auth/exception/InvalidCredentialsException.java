package com.lmkr.hesco.auth.exception;

/**
 * Used for BOTH "user not found" and "wrong password" — a JSON API
 * should not let callers enumerate valid usernames via distinct errors.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
