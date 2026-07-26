package com.lmkr.hesco.feeder.api.dto;

import jakarta.validation.constraints.NotNull;

public record FeederUnassignRequest(
    @NotNull Long performedByUserId
) {}
