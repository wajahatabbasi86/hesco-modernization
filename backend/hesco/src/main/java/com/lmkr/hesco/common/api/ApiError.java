package com.lmkr.hesco.common.api;

import java.time.OffsetDateTime;

public record ApiError(String error, String message, OffsetDateTime timestamp) {

    public static ApiError of(String error, String message) {
        return new ApiError(error, message, OffsetDateTime.now());
    }
}
