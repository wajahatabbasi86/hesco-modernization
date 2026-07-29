package com.lmkr.hesco.auth.api.dto;

/**
 * DEV/DEMO ONLY: this codebase has no email/SMS delivery mechanism, so
 * the raw reset token is returned directly in the API response instead
 * of being sent out-of-band. Before any real deployment, replace this
 * with a generic "if that account exists, a reset link has been sent"
 * message and actually email/SMS the token via whatever delivery
 * channel HESCO uses - returning it here is a deliberate placeholder,
 * not a production-safe pattern.
 */
public record ForgotPasswordResponse(
        String message,
        String devOnlyRawToken,
        java.time.Instant expiresAt
) {
}