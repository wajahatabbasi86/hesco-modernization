package com.lmkr.hesco.survey.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * SRS §8.3.5 — shown when Equipment Type is Transformer. capacityCode
 * must resolve to an item_type in the TRANSFORMER_CAPACITY category.
 */
public record TransformerDetailRequest(
        @NotBlank String capacityCode,
        String transformerName,
        String cableSize,
        String ctRatio
) {}
