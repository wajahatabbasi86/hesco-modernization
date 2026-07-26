package com.lmkr.hesco.workorder.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WorkOrderTransitionRequest(
    @NotBlank String actionCode,
    @NotNull Long actorUserId,
    String comment
) {}
