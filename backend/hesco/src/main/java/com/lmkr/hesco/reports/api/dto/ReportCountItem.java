package com.lmkr.hesco.reports.api.dto;

/**
 * One row of a count-only report (pole structure summary, transformer
 * capacity summary) — one row per item_type value in the relevant
 * item_category, per the SRS's fixed-column requirement (§2.4/§3.15.2).
 */
public record ReportCountItem(String code, String label, long count) {
}
