package com.lmkr.hesco.feeder.api.dto;

import jakarta.validation.constraints.NotBlank;

public record FeederRequest(
    @NotBlank String code,
    @NotBlank String name,
    Long gridStationId
) {}
