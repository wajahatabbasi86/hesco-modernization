package com.lmkr.hesco.reports.repository;

import java.math.BigDecimal;

/**
 * Internal flat row used only to build FeederConductorReportRow — one
 * (feeder, HT/LT zone, item_type) tuple. Grouped into the nested
 * per-feeder shape in ReportService. Not exposed via the API.
 */
public record ConductorSummaryRow(
        Long feederId,
        String feederCode,
        String feederName,
        String substationName,
        String zone, // "HT" or "LT"
        String itemCode,
        String itemLabel,
        long count,
        BigDecimal totalLengthMeters
) {}