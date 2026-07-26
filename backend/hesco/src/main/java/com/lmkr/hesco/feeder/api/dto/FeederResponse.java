package com.lmkr.hesco.feeder.api.dto;

import com.lmkr.hesco.feeder.entity.Feeder;

public record FeederResponse(
    Long id, String code, String name, Long gridStationId, String gridStationCode,
    Long subDivisionId, String subDivisionCode, boolean active
) {
    public static FeederResponse from(Feeder f) {
        return new FeederResponse(
            f.getId(), f.getCode(), f.getName(),
            f.getGridStation() != null ? f.getGridStation().getId() : null,
            f.getGridStation() != null ? f.getGridStation().getCode() : null,
            f.getSubDivision() != null ? f.getSubDivision().getId() : null,
            f.getSubDivision() != null ? f.getSubDivision().getCode() : null,
            f.isActive());
    }
}
