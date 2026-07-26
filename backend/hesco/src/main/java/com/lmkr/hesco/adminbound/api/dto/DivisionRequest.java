package com.lmkr.hesco.adminbound.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DivisionRequest(
    @NotNull Long circleId,
    @NotBlank @Pattern(regexp = "\\d{4}", message = "Division code must be exactly 4 digits") String code,
    @NotBlank String name
) {}
