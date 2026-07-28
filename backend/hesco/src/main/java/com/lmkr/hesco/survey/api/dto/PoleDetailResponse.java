package com.lmkr.hesco.survey.api.dto;

import com.lmkr.hesco.survey.entity.PoleDetail;

import java.math.BigDecimal;

public record PoleDetailResponse(
        String structureTypeCode, String structureTypeLabel, String poleNumber, BigDecimal heightMeters,
        Integer noOfFeeders, String endTypeCode, String endTypeLabel,
        String poleAssemblyCode, String poleAssemblyLabel, Boolean poleEarthing, String assetCode
) {
    public static PoleDetailResponse from(PoleDetail d) {
        return new PoleDetailResponse(
                d.getStructureType().getCode(), d.getStructureType().getDisplayLabel(),
                d.getPoleNumber(), d.getHeightMeters(), d.getNoOfFeeders(),
                d.getEndType() != null ? d.getEndType().getCode() : null,
                d.getEndType() != null ? d.getEndType().getDisplayLabel() : null,
                d.getPoleAssembly() != null ? d.getPoleAssembly().getCode() : null,
                d.getPoleAssembly() != null ? d.getPoleAssembly().getDisplayLabel() : null,
                d.getPoleEarthing(), d.getAssetCode());
    }
}
