package com.lmkr.hesco.survey.api.dto;

import com.lmkr.hesco.survey.entity.PoleDetail;

import java.math.BigDecimal;

public record PoleDetailResponse(
        String structureTypeCode, String structureTypeLabel, String poleNumber, BigDecimal heightMeters
) {
    public static PoleDetailResponse from(PoleDetail d) {
        return new PoleDetailResponse(
                d.getStructureType().getCode(), d.getStructureType().getDisplayLabel(),
                d.getPoleNumber(), d.getHeightMeters());
    }
}