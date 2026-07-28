package com.lmkr.hesco.survey.api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * One phase's conductor type, used inside ConductorDetailRequest when
 * allPhases is false — i.e. different conductor types per phase.
 * phase must be one of R/Y/B/N (SRS §8.3.4); N is only legal when the
 * resolved conductor category for this survey is LT_CONDUCTOR —
 * SurveyService rejects it otherwise.
 */
public record ConductorPhaseEntry(
        @NotBlank String phase,
        @NotBlank String conductorTypeCode
) {}
