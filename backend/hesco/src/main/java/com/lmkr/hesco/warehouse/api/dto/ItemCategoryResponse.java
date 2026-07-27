package com.lmkr.hesco.warehouse.api.dto;

import com.lmkr.hesco.warehouse.entity.ItemCategory;

public record ItemCategoryResponse(Integer id, String code, String name, boolean active) {
    public static ItemCategoryResponse from(ItemCategory c) {
        return new ItemCategoryResponse(c.getId(), c.getCode(), c.getName(), c.isActive());
    }
}
