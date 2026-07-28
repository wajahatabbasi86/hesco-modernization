package com.lmkr.hesco.survey.api.dto;

import com.lmkr.hesco.survey.entity.ConductorDetail;

public record ConductorDetailResponse(String conductorTypeCode, String conductorTypeLabel) {
    public static ConductorDetailResponse from(ConductorDetail d) {
        return new ConductorDetailResponse(d.getConductorType().getCode(), d.getConductorType().getDisplayLabel());
    }
}