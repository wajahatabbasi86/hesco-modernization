package com.lmkr.hesco.warehouse.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemTypeRequest(
    @NotNull Integer categoryId,
    @NotBlank String code,
    @NotBlank String displayLabel,
    Integer sortOrder
) {}
