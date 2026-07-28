package com.lmkr.hesco.reports.api.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * One feeder row of the Conductor Report (SRS §3.15.2.3). HT and LT
 * conductor lengths are two separate column groups per the spec, split
 * by the conductor's item_category (HT_CONDUCTOR / LT_CONDUCTOR) —
 * same item_type-driven-list rationale as FeederStructureReportRow.
 * Lengths are in KM per the spec (converted from line_length_meters).
 */
public record FeederConductorReportRow(
        String feederCode,
        String feederName,
        String substationName,
        List<ReportLengthItem> htConductors,
        BigDecimal htTotalKm,
        List<ReportLengthItem> ltConductors,
        BigDecimal ltTotalKm
) {
