package com.lmkr.hesco.user.service;

import com.lmkr.hesco.adminbound.entity.Circle;
import com.lmkr.hesco.adminbound.entity.Division;
import com.lmkr.hesco.adminbound.entity.SubDivision;
import com.lmkr.hesco.user.entity.AppUser;
import com.lmkr.hesco.user.entity.BoundType;
import com.lmkr.hesco.user.entity.Role;
import com.lmkr.hesco.user.exception.RoleBoundMismatchException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SRS §3.2.3 / §8.1.1: the bound column populated on app_user must match
 * the role's assigned_bound_type, and IMEI is mandatory for roles with
 * requiresImei = true (Surveyor). Previously fn_validate_user_role_bound
 * (a Postgres trigger); now enforced here.
 */
class UserRoleBoundValidatorTest {

    private final UserRoleBoundValidator validator = new UserRoleBoundValidator();

    private Role role(String code, BoundType boundType, boolean requiresImei) {
        Role role = new Role();
        role.setId(1L);
        role.setCode(code);
        role.setDisplayName(code);
        role.setAssignedBoundType(boundType);
        role.setRequiresImei(requiresImei);
        return role;
    }

    @Test
    void subDivisionRole_withSubDivisionSet_isAccepted() {
        SubDivision subDivision = SubDivision.builder().id(1L).code("11011").name("SD").build();
        AppUser user = AppUser.builder()
            .role(role("CREATOR", BoundType.SUB_DIVISION, false))
            .subDivision(subDivision)
            .build();

        validator.validate(user); // no exception == pass
    }

    @Test
    void subDivisionRole_withNoBoundSet_throws() {
        AppUser user = AppUser.builder()
            .role(role("CREATOR", BoundType.SUB_DIVISION, false))
            .build();

        assertThatThrownBy(() -> validator.validate(user))
            .isInstanceOf(RoleBoundMismatchException.class)
            .hasMessageContaining("requires a Sub-Division assignment");
    }

    @Test
    void subDivisionRole_withCircleAlsoSet_throws() {
        Circle circle = Circle.builder().code("110").name("Circle").build();
        SubDivision subDivision = SubDivision.builder().id(1L).code("11011").name("SD").build();
        AppUser user = AppUser.builder()
            .role(role("CREATOR", BoundType.SUB_DIVISION, false))
            .subDivision(subDivision)
            .circle(circle)
            .build();

        assertThatThrownBy(() -> validator.validate(user))
            .isInstanceOf(RoleBoundMismatchException.class)
            .hasMessageContaining("Only the Sub-Division bound may be set");
    }

    @Test
    void systemWideRole_withAnyBoundSet_throws() {
        Circle circle = Circle.builder().code("110").name("Circle").build();
        AppUser user = AppUser.builder()
            .role(role("HESCO_ADMIN", BoundType.SYSTEM_WIDE, false))
            .circle(circle)
            .build();

        assertThatThrownBy(() -> validator.validate(user))
            .isInstanceOf(RoleBoundMismatchException.class)
            .hasMessageContaining("does not accept a bound assignment");
    }

    @Test
    void systemWideRole_withNoBoundSet_isAccepted() {
        AppUser user = AppUser.builder()
            .role(role("HESCO_ADMIN", BoundType.SYSTEM_WIDE, false))
            .build();

        validator.validate(user);
    }

    @Test
    void surveyorRole_withoutImei_throws() {
        SubDivision subDivision = SubDivision.builder().id(1L).code("11011").name("SD").build();
        AppUser user = AppUser.builder()
            .role(role("SURVEYOR", BoundType.SUB_DIVISION, true))
            .subDivision(subDivision)
            .build();

        assertThatThrownBy(() -> validator.validate(user))
            .isInstanceOf(RoleBoundMismatchException.class)
            .hasMessageContaining("IMEI is required");
    }

    @Test
    void surveyorRole_withImei_isAccepted() {
        SubDivision subDivision = SubDivision.builder().id(1L).code("11011").name("SD").build();
        AppUser user = AppUser.builder()
            .role(role("SURVEYOR", BoundType.SUB_DIVISION, true))
            .subDivision(subDivision)
            .imei("123456789012345")
            .build();

        validator.validate(user);
    }
}
