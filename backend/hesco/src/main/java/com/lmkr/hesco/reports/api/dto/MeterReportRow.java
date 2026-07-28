package com.lmkr.hesco.reports.api.dto;

import java.math.BigDecimal;

/**
 * One row of the Meter Report (SRS §3.15.2.4) — replaces the
 * aggregate-only MeterSummaryResponse for this endpoint. Row-level,
 * one entry per meter_detail, filterable by Meter No.
 */
public record MeterReportRow(
        String feederCode,
        String feederName,
        String substationName,
        String subDivisionName,
        String divisionName,
        String circleName,
        String equipmentType,
        String referenceNumber,
        BigDecimal sanctionedLoad,
        String meterNumber,
        String meterMake
) {
