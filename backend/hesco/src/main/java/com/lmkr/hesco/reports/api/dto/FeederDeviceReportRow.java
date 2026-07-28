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
 * NOTE: the legacy report also has a "Capacitor Banks (KVA)" column
 * group. There is currently no item_category for capacitor capacity
 * (KVR) in seed data — TRANSFORMER_CAPACITY only has the 9 KVA
 * tiers — so that group is intentionally omitted here rather than
 * faked. Add a CAPACITOR_CAPACITY item_category (+ KVR item_types)
 * first if that column group needs to come back.
 */
public record FeederDeviceReportRow(
        String feederCode,
        String feederName,
        String substationName,
        List<ReportCountItem> dedicatedTransformers,
        long dedicatedTotal,
        List<ReportCountItem> generalDutyTransformers,
        long generalDutyTotal,
        long total
) {}
