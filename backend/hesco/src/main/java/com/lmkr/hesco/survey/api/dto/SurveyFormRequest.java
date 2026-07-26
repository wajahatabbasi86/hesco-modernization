package com.lmkr.hesco.survey.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SurveyFormRequest(
    @NotNull Long workOrderId,
    @NotBlank String sePoint,
    @NotBlank String equipmentTypeCode,
    @NotBlank String gpsNumber,
    BigDecimal lineLengthMeters,
    @NotNull Long submittedByUserId,
    Double latitude,
    Double longitude,
    String remarks
) {}
