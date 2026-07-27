package com.lmkr.hesco.reports.service;

import com.lmkr.hesco.reports.api.dto.MeterSummaryResponse;
import com.lmkr.hesco.reports.api.dto.ReportCountItem;
import com.lmkr.hesco.reports.api.dto.ReportLengthItem;
import com.lmkr.hesco.reports.exception.MissingReportScopeException;
import com.lmkr.hesco.reports.repository.ReportRepository;
import java.time.OffsetDateTime;
import java.util.List;
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

    private final ReportRepository reportRepository;

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

    private void requireScope(Long circleId, Long divisionId, Long subDivisionId, Long feederId) {
        if (circleId == null && divisionId == null && subDivisionId == null && feederId == null) {
            throw new MissingReportScopeException(
                "At least one of circleId, divisionId, subDivisionId, or feederId is required");
        }
    }
}
