package com.lmkr.hesco.gridstation.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PowerTransformerRequest(
    @NotNull Long gridStationId,
    @NotBlank String transformerName,
    String cableSize,
    String ctRatio,
    BigDecimal capacityKva
) {}
