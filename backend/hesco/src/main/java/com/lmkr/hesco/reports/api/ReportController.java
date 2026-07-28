package com.lmkr.hesco.reports.api;

import com.lmkr.hesco.common.api.ApiResponse;
import com.lmkr.hesco.reports.api.dto.FeederConductorReportRow;
import com.lmkr.hesco.reports.api.dto.FeederDeviceReportRow;
import com.lmkr.hesco.reports.api.dto.FeederStructureReportRow;
import com.lmkr.hesco.reports.api.dto.MeterReportRow;
import com.lmkr.hesco.reports.api.dto.MeterSummaryResponse;
import com.lmkr.hesco.reports.api.dto.PageResponse;
import com.lmkr.hesco.reports.api.dto.ReportCountItem;
import com.lmkr.hesco.reports.api.dto.ReportLengthItem;
import com.lmkr.hesco.reports.service.ReportService;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Feeder Assets Reports (SRS §3.15). Every endpoint requires at least
 * one of circleId/divisionId/subDivisionId/feederId (see
 * ReportService.requireScope) — a 400 VALIDATION-style rejection via
 * MissingReportScopeException if none are supplied. Fixed DTO shape per
 * endpoint, not a dynamic pivot, per the revamp plan §2.4 decision.
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/pole-structure-summary")
    public ApiResponse<List<ReportCountItem>> poleStructureSummary(
            @RequestParam(required = false) Long circleId,
            @RequestParam(required = false) Long divisionId,
            @RequestParam(required = false) Long subDivisionId,
            @RequestParam(required = false) Long feederId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo) {
        return ApiResponse.ok(
            reportService.poleStructureSummary(circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo));
    }

    @GetMapping("/conductor-summary")
    public ApiResponse<List<ReportLengthItem>> conductorSummary(
            @RequestParam(required = false) Long circleId,
            @RequestParam(required = false) Long divisionId,
            @RequestParam(required = false) Long subDivisionId,
            @RequestParam(required = false) Long feederId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo) {
        return ApiResponse.ok(
            reportService.conductorSummary(circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo));
    }

    @GetMapping("/transformer-capacity-summary")
    public ApiResponse<List<ReportCountItem>> transformerCapacitySummary(
            @RequestParam(required = false) Long circleId,
            @RequestParam(required = false) Long divisionId,
            @RequestParam(required = false) Long subDivisionId,
            @RequestParam(required = false) Long feederId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo) {
        return ApiResponse.ok(
            reportService.transformerCapacitySummary(circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo));
    }

    @GetMapping("/meter-summary")
    public ApiResponse<MeterSummaryResponse> meterSummary(
            @RequestParam(required = false) Long circleId,
            @RequestParam(required = false) Long divisionId,
            @RequestParam(required = false) Long subDivisionId,
            @RequestParam(required = false) Long feederId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo) {
        return ApiResponse.ok(
            reportService.meterSummary(circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo));
    }

    // -- Feeder Assets Reports (SRS §3.15.2), feeder-row shape --

    @GetMapping("/device-report")
    public ApiResponse<List<FeederDeviceReportRow>> deviceReport(
            @RequestParam(required = false) Long circleId,
            @RequestParam(required = false) Long divisionId,
            @RequestParam(required = false) Long subDivisionId,
            @RequestParam(required = false) Long feederId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo) {
        return ApiResponse.ok(
            reportService.deviceReport(circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo));
    }

    @GetMapping("/structure-report")
    public ApiResponse<List<FeederStructureReportRow>> structureReport(
            @RequestParam(required = false) Long circleId,
            @RequestParam(required = false) Long divisionId,
            @RequestParam(required = false) Long subDivisionId,
            @RequestParam(required = false) Long feederId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo) {
        return ApiResponse.ok(
            reportService.structureReport(circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo));
    }

    @GetMapping("/conductor-report")
    public ApiResponse<List<FeederConductorReportRow>> conductorReport(
            @RequestParam(required = false) Long circleId,
            @RequestParam(required = false) Long divisionId,
            @RequestParam(required = false) Long subDivisionId,
            @RequestParam(required = false) Long feederId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo) {
        return ApiResponse.ok(
            reportService.conductorReport(circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo));
    }

    @GetMapping("/meter-report")
    public ApiResponse<PageResponse<MeterReportRow>> meterReport(
            @RequestParam(required = false) Long circleId,
            @RequestParam(required = false) Long divisionId,
            @RequestParam(required = false) Long subDivisionId,
            @RequestParam(required = false) Long feederId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo,
            @RequestParam(required = false) String meterNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(
            reportService.meterReport(circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo, meterNo, page, size));
    }
}
