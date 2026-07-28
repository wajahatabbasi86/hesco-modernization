package com.lmkr.hesco.survey.api.dto;

import com.lmkr.hesco.survey.entity.ConductorDetail;
import java.util.List;

public record ConductorDetailResponse(String phase, String conductorTypeCode, String conductorTypeLabel) {
    public static ConductorDetailResponse from(ConductorDetail d) {
        return new ConductorDetailResponse(
                d.getPhase().name(), d.getConductorType().getCode(), d.getConductorType().getDisplayLabel());
    }

    public static List<ConductorDetailResponse> fromAll(List<ConductorDetail> rows) {
        return rows.stream().map(ConductorDetailResponse::from).toList();
    }
}
