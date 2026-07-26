package com.lmkr.hesco.gridstation.api.dto;

import com.lmkr.hesco.gridstation.entity.GridStation;

public record GridStationResponse(Long id, String code, String name, Double latitude, Double longitude, boolean active) {
    public static GridStationResponse from(GridStation gs) {
        return new GridStationResponse(gs.getId(), gs.getCode(), gs.getName(), gs.getLatitude(), gs.getLongitude(), gs.isActive());
    }
}
