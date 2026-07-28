package com.lmkr.hesco.survey.api.dto;

import com.lmkr.hesco.survey.entity.SurveyForm;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SurveyFormResponse(
        Long id, Long workOrderId, String sePoint, String gpsNumber, String equipmentTypeCode,
        BigDecimal lineLengthMeters, Double latitude, Double longitude, String remarks, OffsetDateTime syncedAt,
        PoleDetailResponse poleDetail,
        ConductorDetailResponse conductorDetail,
        TransformerDetailResponse transformerDetail,
        MeterDetailResponse meterDetail
) {
    /** No detail sub-object populated — use the overload below when detail rows are available. */
    public static SurveyFormResponse from(SurveyForm f) {
        return from(f, null, null, null, null);
    }

    public static SurveyFormResponse from(
            SurveyForm f,
            PoleDetailResponse poleDetail,
            ConductorDetailResponse conductorDetail,
            TransformerDetailResponse transformerDetail,
            MeterDetailResponse meterDetail
    ) {
        return new SurveyFormResponse(
                f.getId(), f.getWorkOrder().getId(), f.getSePoint().name(), f.getGpsNumber(),
                f.getEquipmentType().getCode(), f.getLineLengthMeters(), f.getLatitude(), f.getLongitude(),
                f.getRemarks(), f.getSyncedAt(),
                poleDetail, conductorDetail, transformerDetail, meterDetail);
    }
}