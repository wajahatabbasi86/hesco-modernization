package com.lmkr.hesco.reports.repository;

import com.lmkr.hesco.reports.api.dto.ReportCountItem;
import com.lmkr.hesco.reports.api.dto.ReportLengthItem;
import com.lmkr.hesco.survey.entity.PoleDetail;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

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
public interface ReportRepository extends Repository<PoleDetail, Long> {

    @Query("""
        select new com.lmkr.hesco.reports.api.dto.ReportCountItem(it.code, it.displayLabel, count(pd))
        from PoleDetail pd
          join pd.structureType it
          join pd.surveyForm sf
          join sf.workOrder wo
          join wo.feeder f
          left join f.subDivision sd
          left join sd.division d
          left join d.circle c
        where (:circleId is null or c.id = :circleId)
          and (:divisionId is null or d.id = :divisionId)
          and (:subDivisionId is null or sd.id = :subDivisionId)
          and (:feederId is null or f.id = :feederId)
          and (:dateFrom is null or sf.syncedAt >= :dateFrom)
          and (:dateTo is null or sf.syncedAt <= :dateTo)
        group by it.code, it.displayLabel, it.sortOrder
        order by it.sortOrder
        """)
    List<ReportCountItem> poleStructureSummary(@Param("circleId") Long circleId,
                                                @Param("divisionId") Long divisionId,
                                                @Param("subDivisionId") Long subDivisionId,
                                                @Param("feederId") Long feederId,
                                                @Param("dateFrom") OffsetDateTime dateFrom,
                                                @Param("dateTo") OffsetDateTime dateTo);

    @Query("""
        select new com.lmkr.hesco.reports.api.dto.ReportLengthItem(
            it.code, it.displayLabel, count(cd), coalesce(sum(sf.lineLengthMeters), 0))
        from ConductorDetail cd
          join cd.conductorType it
          join cd.surveyForm sf
          join sf.workOrder wo
          join wo.feeder f
          left join f.subDivision sd
          left join sd.division d
          left join d.circle c
        where (:circleId is null or c.id = :circleId)
          and (:divisionId is null or d.id = :divisionId)
          and (:subDivisionId is null or sd.id = :subDivisionId)
          and (:feederId is null or f.id = :feederId)
          and (:dateFrom is null or sf.syncedAt >= :dateFrom)
          and (:dateTo is null or sf.syncedAt <= :dateTo)
        group by it.code, it.displayLabel, it.sortOrder
        order by it.sortOrder
        """)
    List<ReportLengthItem> conductorSummary(@Param("circleId") Long circleId,
                                             @Param("divisionId") Long divisionId,
                                             @Param("subDivisionId") Long subDivisionId,
                                             @Param("feederId") Long feederId,
                                             @Param("dateFrom") OffsetDateTime dateFrom,
                                             @Param("dateTo") OffsetDateTime dateTo);

    @Query("""
        select new com.lmkr.hesco.reports.api.dto.ReportCountItem(it.code, it.displayLabel, count(td))
        from TransformerDetail td
          join td.capacity it
          join td.surveyForm sf
          join sf.workOrder wo
          join wo.feeder f
          left join f.subDivision sd
          left join sd.division d
          left join d.circle c
        where (:circleId is null or c.id = :circleId)
          and (:divisionId is null or d.id = :divisionId)
          and (:subDivisionId is null or sd.id = :subDivisionId)
          and (:feederId is null or f.id = :feederId)
          and (:dateFrom is null or sf.syncedAt >= :dateFrom)
          and (:dateTo is null or sf.syncedAt <= :dateTo)
        group by it.code, it.displayLabel, it.sortOrder
        order by it.sortOrder
        """)
    List<ReportCountItem> transformerCapacitySummary(@Param("circleId") Long circleId,
                                                       @Param("divisionId") Long divisionId,
                                                       @Param("subDivisionId") Long subDivisionId,
                                                       @Param("feederId") Long feederId,
                                                       @Param("dateFrom") OffsetDateTime dateFrom,
                                                       @Param("dateTo") OffsetDateTime dateTo);

    @Query("""
        select count(md)
        from MeterDetail md
          join md.surveyForm sf
          join sf.workOrder wo
          join wo.feeder f
          left join f.subDivision sd
          left join sd.division d
          left join d.circle c
        where (:circleId is null or c.id = :circleId)
          and (:divisionId is null or d.id = :divisionId)
          and (:subDivisionId is null or sd.id = :subDivisionId)
          and (:feederId is null or f.id = :feederId)
          and (:dateFrom is null or sf.syncedAt >= :dateFrom)
          and (:dateTo is null or sf.syncedAt <= :dateTo)
        """)
    long meterSummary(@Param("circleId") Long circleId,
                       @Param("divisionId") Long divisionId,
                       @Param("subDivisionId") Long subDivisionId,
                       @Param("feederId") Long feederId,
                       @Param("dateFrom") OffsetDateTime dateFrom,
                       @Param("dateTo") OffsetDateTime dateTo);
}
