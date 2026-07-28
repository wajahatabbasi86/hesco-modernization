package com.lmkr.hesco.reports.repository;

/**
 * Internal flat row used only to build FeederDeviceReportRow — one
 * (feeder, duty type, transformer capacity item_type) tuple. Grouped
 * into the nested per-feeder shape in ReportService. Not exposed via
 * the API. Mirrors StructureSummaryRow/ConductorSummaryRow, but the
 * "group" axis here is EQUIPMENT_USE (DEDICATED / GENERAL_DUTY)
 * rather than a second item_category on the same detail table.
 */
public record DeviceSummaryRow(
        Long feederId,
        String feederCode,
        String feederName,
        String substationName,
        String dutyCode,
        String itemCode,
        String itemLabel,
        long count
) {}
