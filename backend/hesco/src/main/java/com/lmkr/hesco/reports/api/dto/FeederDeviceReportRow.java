package com.lmkr.hesco.reports.api.dto;

import java.util.List;

/**
 * One feeder row of the Device Report (SRS §3.15.2.1) — transformer
 * counts broken out by capacity (item_type under the
 * TRANSFORMER_CAPACITY item_category), plus a per-feeder total.
 * Column set is item_type-driven — same approach as
 * FeederStructureReportRow / FeederConductorReportRow — so the report
 * stays correct if capacity tiers are added, renamed, or removed via
 * seed data, with no code change required.
 */
public record FeederDeviceReportRow(
        String feederCode,
        String feederName,
        String substationName,
        List<ReportCountItem> devices,
        long total
) {
}
