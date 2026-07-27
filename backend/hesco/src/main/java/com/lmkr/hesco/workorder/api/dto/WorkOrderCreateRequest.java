package com.lmkr.hesco.workorder.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Creates a Work Order in CREATED status with no assignee (SRS §3.6.3:
 * the Creator picks the feeder/type/location here; assigning it to a
 * Surveyor is a separate step — see WorkOrderAssignRequest — so the
 * CREATED -> ASSIGNED transition actually goes through the state machine
 * instead of being set directly on create.
 */
public record WorkOrderCreateRequest(
    @NotNull Long feederId,
    @NotBlank String woType,
    @NotNull Long createdByUserId,
    Double locationLat,
    Double locationLng
) {}
