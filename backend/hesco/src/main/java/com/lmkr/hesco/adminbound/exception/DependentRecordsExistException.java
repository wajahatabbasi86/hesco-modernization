package com.lmkr.hesco.adminbound.exception;

/**
 * Thrown by AdminBoundService.assertDeletable() when an Admin Bound
 * (Circle/Division/Sub-Division) has dependent feeders, users, or work
 * orders and therefore cannot be deleted (SRS §3.1.5). Carries a
 * human-readable reason so the UI can show *why*, not just that the
 * delete failed.
 */
public class DependentRecordsExistException extends RuntimeException {
    public DependentRecordsExistException(String message) {
        super(message);
    }
}
