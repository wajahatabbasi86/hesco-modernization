package com.lmkr.hesco.auth.exception;

/** AppUser.active == false. */
public class InactiveAccountException extends RuntimeException {
    public InactiveAccountException(String message) {
        super(message);
    }
}
