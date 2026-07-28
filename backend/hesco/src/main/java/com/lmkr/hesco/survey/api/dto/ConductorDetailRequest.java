package com.lmkr.hesco.survey.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import java.util.List;

/**
 * SRS §8.3.4 — shown when S/E is End Point (any equipment type). The
 * SRS's "All Phases" shortcut and explicit per-phase entry are both
 * modeled here rather than as two different endpoints:
 *   - allPhases=true: allPhasesConductorTypeCode is applied to every
 *     phase that's active for this survey (R/Y/B, plus N if the
 *     resolved category is LT_CONDUCTOR).
 *   - allPhases=false: phases must contain exactly one entry per active
 *     phase, each with its own conductorTypeCode.
 * Either way, SurveyService always persists one ConductorDetail row per
 * active phase — this DTO just controls how the client supplies them.
 */
public record ConductorDetailRequest(
        boolean allPhases,
        String allPhasesConductorTypeCode,
        @Valid List<ConductorPhaseEntry> phases
) {
    @AssertTrue(message = "allPhasesConductorTypeCode is required when allPhases is true")
    private boolean isAllPhasesCodePresentWhenNeeded() {
        return !allPhases || (allPhasesConductorTypeCode != null && !allPhasesConductorTypeCode.isBlank());
    }

    @AssertTrue(message = "phases is required and must be non-empty when allPhases is false")
    private boolean isPhaseListPresentWhenNeeded() {
        return allPhases || (phases != null && !phases.isEmpty());
    }
}
