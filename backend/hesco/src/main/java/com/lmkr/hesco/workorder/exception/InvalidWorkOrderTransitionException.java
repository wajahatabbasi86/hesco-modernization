package com.lmkr.hesco.workorder.exception;

public class InvalidWorkOrderTransitionException extends RuntimeException {
    public InvalidWorkOrderTransitionException(String message) {
        super(message);
    }
}
