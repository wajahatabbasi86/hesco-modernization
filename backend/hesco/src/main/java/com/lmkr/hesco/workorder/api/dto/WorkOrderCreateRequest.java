package com.lmkr.hesco.workorder.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WorkOrderCreateRequest(
    @NotNull Long feederId,
    @NotBlank String woType,
    @NotNull Long createdByUserId,
    Double locationLat,
    Double locationLng
) {}
