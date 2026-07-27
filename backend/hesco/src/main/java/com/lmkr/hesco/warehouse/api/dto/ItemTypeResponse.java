package com.lmkr.hesco.warehouse.api.dto;

import com.lmkr.hesco.warehouse.entity.ItemType;

public record ItemTypeResponse(
    Integer id, Integer categoryId, String categoryCode, String code,
    String displayLabel, Integer sortOrder, boolean active
) {
    public static ItemTypeResponse from(ItemType t) {
        return new ItemTypeResponse(
            t.getId(), t.getCategory().getId(), t.getCategory().getCode(),
            t.getCode(), t.getDisplayLabel(), t.getSortOrder(), t.isActive());
    }
}
