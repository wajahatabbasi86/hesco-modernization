package com.lmkr.hesco.auth.api.dto;

import java.time.Instant;

public record LoginResponse(
        String token,
        Instant expiresAt,
        Long userId,
        String username,
        String firstName,
        String lastName,
        String roleCode,
        String boundType,
        Long circleId,
        Long divisionId,
        Long subDivisionId,
        boolean passwordExpired,
        boolean mustChangePassword,
        // null when passwordChangedAt has never been tracked (legacy
        // account) - there is no baseline to count down from. Present
        // and non-null whenever passwordExpired is true (0) or the
        // account is within the expiry-warning window.
        Long passwordExpiresInDays
) {}
