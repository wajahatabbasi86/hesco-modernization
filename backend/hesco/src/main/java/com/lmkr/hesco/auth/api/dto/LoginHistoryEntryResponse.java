package com.lmkr.hesco.auth.api.dto;

import com.lmkr.hesco.auth.entity.LoginHistory;

import java.time.OffsetDateTime;

public record LoginHistoryEntryResponse(
        Long id,
        String usernameAttempted,
        OffsetDateTime loginAt,
        OffsetDateTime logoutAt,
        String ipAddress,
        String userAgent,
        String status,
        String failureReason
) {
    public static LoginHistoryEntryResponse from(LoginHistory h) {
        return new LoginHistoryEntryResponse(
                h.getId(), h.getUsernameAttempted(), h.getLoginAt(), h.getLogoutAt(),
                h.getIpAddress(), h.getUserAgent(), h.getStatus().name(), h.getFailureReason());
    }
}