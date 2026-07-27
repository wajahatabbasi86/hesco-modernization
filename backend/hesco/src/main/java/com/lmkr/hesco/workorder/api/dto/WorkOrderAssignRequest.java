package com.lmkr.hesco.workorder.api.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Assigns a CREATED work order to a Surveyor (SRS §3.6.3). Runs the
 * CREATED -> ASSIGN -> ASSIGNED transition through WorkOrderStateMachineService,
 * same as any other state change — this is not a direct field set.
 *
 * actorUserId is the Creator performing the assignment (must hold the
 * CREATOR role for the ASSIGN transition to be legal); surveyorUserId is
 * who the work order is being handed to.
 */
public record WorkOrderAssignRequest(
    @NotNull Long surveyorUserId,
    @NotNull Long actorUserId
) {}
