package com.lmkr.hesco.workorder.api.dto;

import com.lmkr.hesco.workorder.entity.WorkOrder;

import java.time.OffsetDateTime;

public record WorkOrderResponse(
    Long id, Long feederId, String feederCode, String woType, String statusCode, String statusLabel,
    Long createdByUserId, Long assignedToUserId, Double locationLat, Double locationLng, OffsetDateTime createdAt
) {
    public static WorkOrderResponse from(WorkOrder w) {
        return new WorkOrderResponse(
            w.getId(), w.getFeeder().getId(), w.getFeeder().getCode(), w.getWoType().name(),
            w.getStatus().getCode(), w.getStatus().getLabel(),
            w.getCreatedBy().getId(), w.getAssignedTo() != null ? w.getAssignedTo().getId() : null,
            w.getLocationLat(), w.getLocationLng(), w.getCreatedAt());
    }
}
