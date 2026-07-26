package com.lmkr.hesco.user.api.dto;

import com.lmkr.hesco.user.entity.AppUser;

public record AppUserResponse(
    Long id, String username, String firstName, String lastName, String contactNumber,
    String roleCode, String roleDisplayName,
    Long circleId, Long divisionId, Long subDivisionId,
    String imei, boolean active
) {
    public static AppUserResponse from(AppUser u) {
        return new AppUserResponse(
            u.getId(), u.getUsername(), u.getFirstName(), u.getLastName(), u.getContactNumber(),
            u.getRole().getCode(), u.getRole().getDisplayName(),
            u.getCircle() != null ? u.getCircle().getId() : null,
            u.getDivision() != null ? u.getDivision().getId() : null,
            u.getSubDivision() != null ? u.getSubDivision().getId() : null,
            u.getImei(), u.isActive());
    }
}
