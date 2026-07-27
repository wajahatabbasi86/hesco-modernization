package com.lmkr.hesco.reports.api.dto;

/**
 * Meter summary has no lookup dimension to group by (meter_detail
 * doesn't reference item_type) — a flat scoped count.
 */
public record MeterSummaryResponse(long count) {
}
