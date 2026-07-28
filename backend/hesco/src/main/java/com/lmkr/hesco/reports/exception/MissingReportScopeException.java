package com.lmkr.hesco.reports.exception;

/**
 * Thrown when a report endpoint is called with none of
 * circleId/divisionId/subDivisionId/feederId set. Reports scope to at
 * least one admin-bound level or a feeder by design decision — no
 * utility-wide unscoped pull, unlike the plain list endpoints elsewhere
 * in the app.
 */
public class MissingReportScopeException extends RuntimeException {
    public MissingReportScopeException(String message) {
        super(message);
    }
}
