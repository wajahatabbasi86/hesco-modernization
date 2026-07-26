package com.lmkr.hesco.gridstation.api.dto;

import jakarta.validation.constraints.NotBlank;

public record GridStationRequest(
    @NotBlank String code,
    @NotBlank String name,
    Double latitude,
    Double longitude
) {}
