package com.lmkr.hesco.survey.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * SRS §8.3.4 — shown when S/E is End Point (any equipment type).
 * conductorTypeCode must resolve to an item_type in the HT_CONDUCTOR or
 * LT_CONDUCTOR category.
 */
public record ConductorDetailRequest(
        @NotBlank String conductorTypeCode
) {}