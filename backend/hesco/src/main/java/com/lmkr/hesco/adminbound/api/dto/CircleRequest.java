package com.lmkr.hesco.adminbound.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CircleRequest(
    @NotBlank @Pattern(regexp = "\\d{3}", message = "Circle code must be exactly 3 digits") String code,
    @NotBlank String name
) {}
