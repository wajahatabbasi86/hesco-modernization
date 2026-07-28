package com.lmkr.hesco.reports.repository;

/**
 * Internal flat row used only to build FeederStructureReportRow —
 * one (feeder, structure group, item_type) tuple. Grouped into the
 * nested per-feeder shape in ReportService. Not exposed via the API.
 */
record StructureSummaryRow(
        Long feederId,
        String feederCode,
        String feederName,
        String substationName,
        String structureGroup, // "PRIMARY" or "SECONDARY"
        String itemCode,
        String itemLabel,
        long count
) {
