package com.lmkr.hesco.user.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AppUserRequest(
    @NotBlank String username,
    @NotBlank String password,
    @NotBlank String firstName,
    @NotBlank String lastName,
    String contactNumber,
    @NotNull Short roleId,
    Long circleId,
    Long divisionId,
    Long subDivisionId,
    String imei
) {}
