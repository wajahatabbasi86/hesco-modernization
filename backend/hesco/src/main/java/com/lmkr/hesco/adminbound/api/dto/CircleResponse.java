package com.lmkr.hesco.adminbound.api.dto;

import com.lmkr.hesco.adminbound.entity.Circle;

public record CircleResponse(Long id, String code, String name, boolean active) {
    public static CircleResponse from(Circle c) {
        return new CircleResponse(c.getId(), c.getCode(), c.getName(), c.isActive());
    }
}
