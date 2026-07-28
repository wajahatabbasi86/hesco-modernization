package com.lmkr.hesco.reports.repository;

import com.lmkr.hesco.reports.api.dto.PageResponse;
import com.lmkr.hesco.reports.api.dto.ReportCountItem;
import com.lmkr.hesco.reports.api.dto.ReportLengthItem;
import com.lmkr.hesco.reports.api.dto.MeterReportRow;

import java.time.OffsetDateTime;

import java.util.List;

/**
 * Aggregate queries backing ReportService. Each query joins its detail
 * table up through survey_form -> work_order -> feeder -> sub_division
 * -> division -> circle so a single set of optional filters can scope
 * by any admin-bound level or by feeder directly. Filters use the
 * standard JPQL "(:param is null or x = :param)" null-safe pattern —
 * same style as the rest of the app's optional-filter list endpoints,
 * just expressed as one @Query instead of a derived method name (the
 * filter combination here doesn't fit Spring Data's method-name
 * derivation cleanly).
 *
 * Not a full Spring Data repository (no single natural aggregate root
 * across all four queries) — kept as a thin @Query-only interface, same
 * pattern as AdminBoundDependencyRepository.
 */
public interface ReportQueryRepository {

    List<ReportCountItem> poleStructureSummary(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo);

    List<ReportLengthItem> conductorSummary(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo);

    List<ReportCountItem> transformerCapacitySummary(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo);

    PageResponse<ReportCountItem> poleStructureSummary(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo,
            int page, int size);

    PageResponse<ReportLengthItem> conductorSummary(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo,
            int page, int size);

    PageResponse<ReportCountItem> transformerCapacitySummary(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo,
            int page, int size);


    long meterSummary(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo);

    // -- Feeder Assets Reports (SRS §3.15.2), feeder-row shape --

    List<DeviceSummaryRow> deviceReportRaw(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo);

    List<StructureSummaryRow> structureReportRaw(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo);

    List<ConductorSummaryRow> conductorReportRaw(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo);

    PageResponse<MeterReportRow> meterReport(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo,
            String meterNo, int page, int size);
}