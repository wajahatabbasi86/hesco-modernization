package com.lmkr.hesco.adminbound.api.dto;

import com.lmkr.hesco.adminbound.entity.SubDivision;

public record SubDivisionResponse(Long id, Long divisionId, String divisionCode, String code, String name, boolean active) {
    public static SubDivisionResponse from(SubDivision sd) {
        return new SubDivisionResponse(sd.getId(), sd.getDivision().getId(), sd.getDivision().getCode(),
            sd.getCode(), sd.getName(), sd.isActive());
    }
}
