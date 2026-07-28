package com.lmkr.hesco.reports.api.dto;

import java.util.List;

/**
 * One feeder row of the Structure Report (SRS §3.15.2.2). Primary and
 * Secondary structure counts are two separate column groups per the
 * spec; kept as item_type-driven lists (not hardcoded columns) so the
 * report stays correct if PRIMARY_STRUCTURE/SECONDARY_STRUCTURE item
 * types are added or renamed later — the frontend renders each list as
 * its own set of columns, same as it already must for the existing
 * flat pole-structure-summary endpoint.
 */
public record FeederStructureReportRow(
        String feederCode,
        String feederName,
        String substationName,
        List<ReportCountItem> primaryStructures,
        long primaryTotal,
        List<ReportCountItem> secondaryStructures,
        long sum) {}