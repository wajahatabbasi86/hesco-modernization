package com.lmkr.hesco.adminbound.api.dto;

import com.lmkr.hesco.adminbound.entity.Division;

public record DivisionResponse(Long id, Long circleId, String circleCode, String code, String name, boolean active) {
    public static DivisionResponse from(Division d) {
        return new DivisionResponse(d.getId(), d.getCircle().getId(), d.getCircle().getCode(),
            d.getCode(), d.getName(), d.isActive());
    }
}
