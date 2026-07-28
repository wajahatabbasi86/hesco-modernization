package com.lmkr.hesco.survey.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

/**
 * SRS §8.3.3 — shown when Equipment Type is Feeder Pole, Primary Pole,
 * or Secondary Pole. structureTypeCode must resolve to an item_type in
 * the PRIMARY_STRUCTURE or SECONDARY_STRUCTURE category (SurveyService
 * checks which, based on this form's equipment type). endTypeCode and
 * poleAssemblyCode resolve against POLE_END_TYPE / POLE_ASSEMBLY —
 * optional, since those two categories are still unseeded pending
 * HESCO/LMKR sign-off on their dropdown values.
 */
public record PoleDetailRequest(
        @NotBlank String structureTypeCode,
        String poleNumber,
        BigDecimal heightMeters,
        Integer noOfFeeders,
        String endTypeCode,
        String poleAssemblyCode,
        Boolean poleEarthing,
        String assetCode
) {}
