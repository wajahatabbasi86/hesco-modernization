package com.lmkr.hesco.reports.repository;

/**
 * Internal flat row used only to build FeederDeviceReportRow — one
 * (feeder, transformer capacity item_type) tuple. Grouped into the
 * nested per-feeder shape in ReportService. Not exposed via the API.
 * Mirrors StructureSummaryRow/ConductorSummaryRow.
 */
public record DeviceSummaryRow(
        Long feederId,
        String feederCode,
        String feederName,
        String substationName,
        String itemCode,
        String itemLabel,
        long count
) {}
