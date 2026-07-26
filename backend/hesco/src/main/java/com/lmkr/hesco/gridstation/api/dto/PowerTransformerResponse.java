package com.lmkr.hesco.gridstation.api.dto;

import com.lmkr.hesco.gridstation.entity.PowerTransformer;

import java.math.BigDecimal;

public record PowerTransformerResponse(
    Long id, Long gridStationId, String transformerName, String cableSize, String ctRatio, BigDecimal capacityKva
) {
    public static PowerTransformerResponse from(PowerTransformer pt) {
        return new PowerTransformerResponse(pt.getId(), pt.getGridStation().getId(), pt.getTransformerName(),
            pt.getCableSize(), pt.getCtRatio(), pt.getCapacityKva());
    }
}
