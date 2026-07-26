package com.lmkr.hesco.user.service;

import com.lmkr.hesco.user.entity.AppUser;
import com.lmkr.hesco.user.entity.BoundType;
import com.lmkr.hesco.user.entity.Role;
import com.lmkr.hesco.user.exception.RoleBoundMismatchException;
import org.springframework.stereotype.Component;

/**
 * Owns the app_user <-> role bound-scoping and IMEI rules (SRS §3.2.3,
 * §8.1.1). Previously enforced by fn_validate_user_role_bound (a Postgres
 * trigger that looked up role.assigned_bound_type / role.requires_imei);
 * now enforced here, called by UserService before every create/update, so
 * the check is one JOIN-free method call and testable without a DB.
 *
 * Rule: exactly the bound column matching the role's assigned_bound_type
 * may be populated (SYSTEM_WIDE/NONE roles must have none set), and IMEI
 * is mandatory when role.requiresImei() is true (mobile-primary roles,
 * i.e. Surveyor).
 */
@Component
public class UserRoleBoundValidator {

    public void validate(AppUser user) {
        Role role = user.getRole();
        BoundType boundType = role.getAssignedBoundType();

        switch (boundType) {
            case CIRCLE -> requireOnly("Circle", user.getCircle() != null,
                user.getDivision(), user.getSubDivision());
            case DIVISION -> requireOnly("Division", user.getDivision() != null,
                user.getCircle(), user.getSubDivision());
            case SUB_DIVISION -> requireOnly("Sub-Division", user.getSubDivision() != null,
                user.getCircle(), user.getDivision());
            case SYSTEM_WIDE, NONE -> {
                if (user.getCircle() != null || user.getDivision() != null || user.getSubDivision() != null) {
                    throw new RoleBoundMismatchException(
                        "Role " + role.getCode() + " (" + boundType + ") does not accept a bound assignment");
                }
            }
        }

        if (role.isRequiresImei() && (user.getImei() == null || user.getImei().isBlank())) {
            throw new RoleBoundMismatchException(
                "IMEI is required for role " + role.getCode()
                    + " (mobile-primary user, SRS §3.2.3/§8.1.1)");
        }
    }

    private void requireOnly(String requiredBoundName, boolean requiredPresent, Object... othersMustBeNull) {
        if (!requiredPresent) {
            throw new RoleBoundMismatchException("Role requires a " + requiredBoundName + " assignment");
        }
        for (Object other : othersMustBeNull) {
            if (other != null) {
                throw new RoleBoundMismatchException(
                    "Only the " + requiredBoundName + " bound may be set for this role");
            }
        }
    }
}
