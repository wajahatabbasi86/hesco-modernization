package com.lmkr.hesco.survey.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * SRS §8.3.5 — shown when Equipment Type is Transformer. capacityCode
 * must resolve to an item_type in the TRANSFORMER_CAPACITY category.
 * equipmentUseCode resolves against EQUIPMENT_USE (seeded: GENERAL_DUTY,
 * DEDICATED). mountingCode/fusesCode resolve against the existing
 * TRANSFORMER_MOUNTING/TRANSFORMER_FUSE categories (still unseeded
 * pending HESCO/LMKR's dropdown values, so optional here).
 * equipmentNumber is NOT part of this request — SurveyService derives
 * it from the form's own GPS number ('T-' + gpsNumber) per the spec;
 * equipmentPhase is likewise derived, from this form's conductorDetail
 * phases, not client-supplied.
 */
public record TransformerDetailRequest(
        @NotBlank String capacityCode,
        String transformerName,
        String cableSize,
        String ctRatio,
        String equipmentUseCode,
        String mountingCode,
        String fusesCode,
        String assetCode,
        String consumerName,
        String equipmentLocation
) {}
