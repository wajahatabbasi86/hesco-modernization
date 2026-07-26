package com.lmkr.hesco.adminbound.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SubDivisionRequest(
    @NotNull Long divisionId,
    @NotBlank @Pattern(regexp = "\\d{5}", message = "Sub-Division code must be exactly 5 digits") String code,
    @NotBlank String name
) {}
