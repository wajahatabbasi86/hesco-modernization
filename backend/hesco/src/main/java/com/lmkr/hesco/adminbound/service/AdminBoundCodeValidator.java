package com.lmkr.hesco.adminbound.service;

import com.lmkr.hesco.adminbound.entity.Circle;
import com.lmkr.hesco.adminbound.entity.Division;
import com.lmkr.hesco.adminbound.exception.InvalidCodeHierarchyException;
import org.springframework.stereotype.Component;

/**
 * Owns the HESCO Admin Bound coding convention (SRS §3.1.1):
 *   - Division code (4 digits): first 3 digits must equal the parent
 *     Circle's code.
 *   - Sub-Division code (5 digits): first 4 digits must equal the parent
 *     Division's code.
 *
 * This is a cross-table rule, so it cannot be expressed as a plain DB
 * CHECK constraint (Postgres CHECKs can't reference another table). It
 * previously lived in trg_validate_division_code / trg_validate_sub_division_code
 * (Postgres triggers); it now lives here so AdminBoundService can call it
 * BEFORE attempting an insert/update and return a friendly error, and so
 * it's testable with a plain JUnit test instead of a live Postgres instance.
 *
 * The DB still enforces the single-column format (exactly N digits) via
 * chk_division_code_format / chk_sub_division_code_format — that part
 * doesn't need another table, so it stays a DB CHECK.
 */
@Component
public class AdminBoundCodeValidator {

    public void validateDivisionCode(String divisionCode, Circle parentCircle) {
        String parentCode = parentCircle.getCode();
        if (divisionCode == null || divisionCode.length() != 4) {
            throw new InvalidCodeHierarchyException(
                "Division code must be exactly 4 digits: " + divisionCode);
        }
        String prefix = divisionCode.substring(0, 3);
        if (!prefix.equals(parentCode)) {
            throw new InvalidCodeHierarchyException(
                "Division code " + divisionCode + " must start with parent circle code "
                    + parentCode + " (HESCO coding convention, SRS §3.1.1)");
        }
    }

    public void validateSubDivisionCode(String subDivisionCode, Division parentDivision) {
        String parentCode = parentDivision.getCode();
        if (subDivisionCode == null || subDivisionCode.length() != 5) {
            throw new InvalidCodeHierarchyException(
                "Sub-Division code must be exactly 5 digits: " + subDivisionCode);
        }
        String prefix = subDivisionCode.substring(0, 4);
        if (!prefix.equals(parentCode)) {
            throw new InvalidCodeHierarchyException(
                "Sub-Division code " + subDivisionCode + " must start with parent division code "
                    + parentCode + " (HESCO coding convention, SRS §3.1.1)");
        }
    }
}
