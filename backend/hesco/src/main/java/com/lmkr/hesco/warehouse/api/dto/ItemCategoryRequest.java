package com.lmkr.hesco.warehouse.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ItemCategoryRequest(
    @NotBlank String code,
    @NotBlank String name
) {}
