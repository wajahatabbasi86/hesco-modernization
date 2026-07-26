package com.lmkr.hesco.adminbound.exception;

/**
 * Thrown when a Division/Sub-Division code does not start with its
 * parent's code, per the HESCO coding convention (SRS §3.1.1). This used
 * to be enforced by a Postgres trigger (fn_validate_division_code /
 * fn_validate_sub_division_code); it now lives entirely here so it is
 * unit-testable without a live database and surfaces as a clean 4xx from
 * the service layer instead of an opaque JDBC SQLException.
 */
public class InvalidCodeHierarchyException extends RuntimeException {
    public InvalidCodeHierarchyException(String message) {
        super(message);
    }
}
