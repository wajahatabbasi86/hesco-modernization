package com.lmkr.hesco.survey.api.dto;

import com.lmkr.hesco.survey.entity.TransformerDetail;

public record TransformerDetailResponse(
        String capacityCode, String capacityLabel, String transformerName, String cableSize, String ctRatio,
        String equipmentNumber, String equipmentPhase,
        String equipmentUseCode, String equipmentUseLabel,
        String mountingCode, String mountingLabel,
        String fusesCode, String fusesLabel,
        String assetCode, String consumerName, String equipmentLocation
) {
    public static TransformerDetailResponse from(TransformerDetail d) {
        return new TransformerDetailResponse(
                d.getCapacity().getCode(), d.getCapacity().getDisplayLabel(),
                d.getTransformerName(), d.getCableSize(), d.getCtRatio(),
                d.getEquipmentNumber(), d.getEquipmentPhase(),
                d.getEquipmentUse() != null ? d.getEquipmentUse().getCode() : null,
                d.getEquipmentUse() != null ? d.getEquipmentUse().getDisplayLabel() : null,
                d.getMounting() != null ? d.getMounting().getCode() : null,
                d.getMounting() != null ? d.getMounting().getDisplayLabel() : null,
                d.getFuses() != null ? d.getFuses().getCode() : null,
                d.getFuses() != null ? d.getFuses().getDisplayLabel() : null,
                d.getAssetCode(), d.getConsumerName(), d.getEquipmentLocation());
    }
}
