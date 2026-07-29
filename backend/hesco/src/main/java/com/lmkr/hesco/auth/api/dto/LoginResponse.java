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
        long passwordExpiresInDays,   // null if not within the warning window; negative-safe not needed since expired is a separate flag
        boolean mustChangePassword
) {
}