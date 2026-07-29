package com.lmkr.hesco.reports.api.dto;

import java.util.List;

/**
 * One feeder row of the Device Report (SRS §3.15.2.1) — transformer
 * counts broken out by capacity (item_type under the
 * TRANSFORMER_CAPACITY item_category), split into Dedicated vs.
 * General Duty transformers (item_type under the EQUIPMENT_USE
 * item_category, per data-item_type.csv: codes DEDICATED /
 * GENERAL_DUTY), each with its own subtotal, plus a per-feeder grand
 * total. Both axes are item_type-driven — same approach as
 * FeederStructureReportRow / FeederConductorReportRow — so the report
 * stays correct if capacity tiers or duty types are added, renamed,
 * or removed via seed data, with no code change required.
 *
 * Capacitor Banks (legacy's "Capacitor Banks (KVA)" column group) are
 * now included too: capacitorBanks / capacitorTotal, backed by the new
 * CAPACITOR_CAPACITY item_category + capacitor_detail table
 * (V6__capacitor_detail.sql) — same item_type-driven, zero-filled
 * approach as the transformer groups above.
 *
 * grandTotalKva is count-weighted KVA, e.g. 3 x KVA_100 + 1 x KVA_200
 * = 500 — NOT a row count. It's derived from the numeric value in
 * each capacity item_type's code (KVA_100 -> 100), computed in
 * ReportService rather than stored, since item_type only has a
 * display_label string, not a numeric column, to key off of.
 */
public record FeederDeviceReportRow(
        String feederCode,
        String feederName,
        String substationName,
        List<ReportCountItem> dedicatedTransformers,
        long dedicatedTotal,
        List<ReportCountItem> generalDutyTransformers,
        long generalDutyTotal,
        long total,
        long grandTotalKva,
        List<ReportCountItem> capacitorBanks,
        long capacitorTotal
) {
}