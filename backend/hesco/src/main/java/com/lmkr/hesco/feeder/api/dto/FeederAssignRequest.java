package com.lmkr.hesco.feeder.api.dto;

import jakarta.validation.constraints.NotNull;

public record FeederAssignRequest(
    @NotNull Long subDivisionId,
    @NotNull Long performedByUserId
) {}
