package com.lmkr.hesco.auth.exception;

/** Covers not-found, already-used, AND expired - do not distinguish
 * these to callers; that would let someone probe which tokens once
 * existed. */
public class InvalidResetTokenException extends RuntimeException {
    public InvalidResetTokenException(String message) {
        super(message);
    }
}