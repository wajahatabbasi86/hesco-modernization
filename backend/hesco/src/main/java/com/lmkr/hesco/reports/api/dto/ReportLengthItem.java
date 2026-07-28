package com.lmkr.hesco.reports.api.dto;

import java.math.BigDecimal;

/**
 * One row of the conductor summary report — count of segments plus
 * total surveyed length, per conductor type.
 */
public record ReportLengthItem(String code, String label, long count, BigDecimal totalLengthMeters) {
}
