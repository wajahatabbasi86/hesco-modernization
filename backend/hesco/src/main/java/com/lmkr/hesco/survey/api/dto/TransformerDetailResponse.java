package com.lmkr.hesco.survey.api.dto;

import com.lmkr.hesco.survey.entity.TransformerDetail;

public record TransformerDetailResponse(
        String capacityCode, String capacityLabel, String transformerName, String cableSize, String ctRatio
) {
    public static TransformerDetailResponse from(TransformerDetail d) {
        return new TransformerDetailResponse(
                d.getCapacity().getCode(), d.getCapacity().getDisplayLabel(),
                d.getTransformerName(), d.getCableSize(), d.getCtRatio());
    }
}