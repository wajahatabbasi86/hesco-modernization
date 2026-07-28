package com.lmkr.hesco.reports.service;

import com.lmkr.hesco.reports.api.dto.FeederConductorReportRow;
import com.lmkr.hesco.reports.api.dto.FeederDeviceReportRow;
import com.lmkr.hesco.reports.api.dto.FeederStructureReportRow;
import com.lmkr.hesco.reports.api.dto.MeterReportRow;
import com.lmkr.hesco.reports.api.dto.MeterSummaryResponse;
import com.lmkr.hesco.reports.api.dto.PageResponse;
import com.lmkr.hesco.reports.api.dto.ReportCountItem;
import com.lmkr.hesco.reports.api.dto.ReportLengthItem;
import com.lmkr.hesco.reports.exception.MissingReportScopeException;
import com.lmkr.hesco.reports.repository.ConductorSummaryRow;
import com.lmkr.hesco.reports.repository.DeviceSummaryRow;
import com.lmkr.hesco.reports.repository.ReportQueryRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.lmkr.hesco.reports.repository.StructureSummaryRow;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Feeder Assets Reports (SRS §3.15 / revamp plan §2.4). Every method
 * requires at least one of circleId/divisionId/subDivisionId/feederId —
 * by decision, reports don't allow an unscoped utility-wide pull the
 * way the plain list endpoints elsewhere in the app do. dateFrom/dateTo
 * don't count toward that requirement on their own.
 */
@AllArgsConstructor
@Service
public class ReportService {

    private final ReportQueryRepository reportRepository;

    @Transactional(readOnly = true)
    public List<ReportCountItem> poleStructureSummary(Long circleId, Long divisionId, Long subDivisionId,
                                                        Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo) {
        requireScope(circleId, divisionId, subDivisionId, feederId);
        return reportRepository.poleStructureSummary(circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);
    }

    @Transactional(readOnly = true)
    public List<ReportLengthItem> conductorSummary(Long circleId, Long divisionId, Long subDivisionId,
                                                     Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo) {
        requireScope(circleId, divisionId, subDivisionId, feederId);
        return reportRepository.conductorSummary(circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);
    }

    @Transactional(readOnly = true)
    public List<ReportCountItem> transformerCapacitySummary(Long circleId, Long divisionId, Long subDivisionId,
                                                              Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo) {
        requireScope(circleId, divisionId, subDivisionId, feederId);
        return reportRepository.transformerCapacitySummary(circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);
    }

    @Transactional(readOnly = true)
    public MeterSummaryResponse meterSummary(Long circleId, Long divisionId, Long subDivisionId,
                                              Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo) {
        requireScope(circleId, divisionId, subDivisionId, feederId);
        return new MeterSummaryResponse(
            reportRepository.meterSummary(circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo));
    }

    // -- Feeder Assets Reports (SRS §3.15.2), feeder-row shape --

    @Transactional(readOnly = true)
    public List<FeederDeviceReportRow> deviceReport(Long circleId, Long divisionId, Long subDivisionId,
                                                      Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo) {
        requireScope(circleId, divisionId, subDivisionId, feederId);
        List<DeviceSummaryRow> raw =
            reportRepository.deviceReportRaw(circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);

        Map<Long, List<DeviceSummaryRow>> byFeeder = raw.stream()
            .collect(Collectors.groupingBy(DeviceSummaryRow::feederId, LinkedHashMap::new, Collectors.toList()));

        return byFeeder.values().stream().map(rows -> {
            var first = rows.get(0);

            List<ReportCountItem> dedicated = rows.stream()
                .filter(r -> "DEDICATED".equals(r.dutyCode()))
                .map(r -> new ReportCountItem(r.itemCode(), r.itemLabel(), r.count()))
                .toList();
            List<ReportCountItem> generalDuty = rows.stream()
                .filter(r -> "GENERAL_DUTY".equals(r.dutyCode()))
                .map(r -> new ReportCountItem(r.itemCode(), r.itemLabel(), r.count()))
                .toList();

            long dedicatedTotal = dedicated.stream().mapToLong(ReportCountItem::count).sum();
            long generalDutyTotal = generalDuty.stream().mapToLong(ReportCountItem::count).sum();

            return new FeederDeviceReportRow(
                first.feederCode(), first.feederName(), first.substationName(),
                dedicated, dedicatedTotal,
                generalDuty, generalDutyTotal,
                dedicatedTotal + generalDutyTotal);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<FeederStructureReportRow> structureReport(Long circleId, Long divisionId, Long subDivisionId,
                                                            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo) {
        requireScope(circleId, divisionId, subDivisionId, feederId);
        List<StructureSummaryRow> raw =
            reportRepository.structureReportRaw(circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);

        Map<Long, List<StructureSummaryRow>> byFeeder = raw.stream()
            .collect(Collectors.groupingBy(StructureSummaryRow::feederId, LinkedHashMap::new, Collectors.toList()));

        return byFeeder.values().stream().map(rows -> {
            var first = rows.get(0);
            List<ReportCountItem> primary = rows.stream()
                .filter(r -> r.structureGroup().equals("PRIMARY"))
                .map(r -> new ReportCountItem(r.itemCode(), r.itemLabel(), r.count()))
                .toList();
            List<ReportCountItem> secondary = rows.stream()
                .filter(r -> r.structureGroup().equals("SECONDARY"))
                .map(r -> new ReportCountItem(r.itemCode(), r.itemLabel(), r.count()))
                .toList();
            long secondaryTotal = secondary.stream().mapToLong(ReportCountItem::count).sum();
            return new FeederStructureReportRow(
                first.feederCode(), first.feederName(), first.substationName(),
                primary, primary.stream().mapToLong(ReportCountItem::count).sum(),
                secondary, secondaryTotal);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<FeederConductorReportRow> conductorReport(Long circleId, Long divisionId, Long subDivisionId,
                                                            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo) {
        requireScope(circleId, divisionId, subDivisionId, feederId);
        List<ConductorSummaryRow> raw =
            reportRepository.conductorReportRaw(circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);

        Map<Long, List<ConductorSummaryRow>> byFeeder = raw.stream()
            .collect(Collectors.groupingBy(ConductorSummaryRow::feederId, LinkedHashMap::new, Collectors.toList()));

        BigDecimal metersPerKm = BigDecimal.valueOf(1000);

        return byFeeder.values().stream().map(rows -> {
            var first = rows.get(0);
            List<ReportLengthItem> ht = rows.stream()
                .filter(r -> r.zone().equals("HT"))
                .map(r -> new ReportLengthItem(r.itemCode(), r.itemLabel(), r.count(),
                    r.totalLengthMeters().divide(metersPerKm, 3, java.math.RoundingMode.HALF_UP)))
                .toList();
            List<ReportLengthItem> lt = rows.stream()
                .filter(r -> r.zone().equals("LT"))
                .map(r -> new ReportLengthItem(r.itemCode(), r.itemLabel(), r.count(),
                    r.totalLengthMeters().divide(metersPerKm, 3, java.math.RoundingMode.HALF_UP)))
                .toList();
            BigDecimal htTotal = ht.stream().map(ReportLengthItem::totalLengthMeters)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal ltTotal = lt.stream().map(ReportLengthItem::totalLengthMeters)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new FeederConductorReportRow(
                first.feederCode(), first.feederName(), first.substationName(),
                ht, htTotal, lt, ltTotal);
        }).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<MeterReportRow> meterReport(Long circleId, Long divisionId, Long subDivisionId,
                                                      Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo,
                                                      String meterNo, int page, int size) {
        requireScope(circleId, divisionId, subDivisionId, feederId);
        return reportRepository.meterReport(
            circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo, meterNo, page, size);
    }

    private void requireScope(Long circleId, Long divisionId, Long subDivisionId, Long feederId) {
        if (circleId == null && divisionId == null && subDivisionId == null && feederId == null) {
            throw new MissingReportScopeException(
                "At least one of circleId, divisionId, subDivisionId, or feederId is required");
        }
    }
}
