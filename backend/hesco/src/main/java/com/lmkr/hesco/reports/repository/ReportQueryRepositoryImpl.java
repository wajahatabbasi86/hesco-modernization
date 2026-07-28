package com.lmkr.hesco.reports.repository;

import com.lmkr.hesco.reports.api.dto.MeterReportRow;
import com.lmkr.hesco.reports.api.dto.PageResponse;
import com.lmkr.hesco.reports.api.dto.ReportCountItem;
import com.lmkr.hesco.reports.api.dto.ReportLengthItem;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ReportQueryRepositoryImpl implements ReportQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    private void applyFilters(StringBuilder sql, Map<String, Object> params,
                              Long circleId, Long divisionId, Long subDivisionId,
                              Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo) {

        if (circleId != null) {
            sql.append(" AND d.circle_id = :circleId");
            params.put("circleId", circleId);
        }
        if (divisionId != null) {
            sql.append(" AND sd.division_id = :divisionId");
            params.put("divisionId", divisionId);
        }
        if (subDivisionId != null) {
            sql.append(" AND f.sub_division_id = :subDivisionId");
            params.put("subDivisionId", subDivisionId);
        }
        if (feederId != null) {
            sql.append(" AND wo.feeder_id = :feederId");
            params.put("feederId", feederId);
        }
        if (dateFrom != null) {
            sql.append(" AND sf.synced_at >= :dateFrom");
            params.put("dateFrom", dateFrom);
        }
        if (dateTo != null) {
            sql.append(" AND sf.synced_at <= :dateTo");
            params.put("dateTo", dateTo);
        }
    }

    /**
     * Scope-only filter for the three zero-fill feeder-row reports below
     * (deviceReportRaw/structureReportRaw/conductorReportRaw). These drive
     * their FROM clause off `feeder f` directly (not off the detail table
     * via survey_form/work_order), and dateFrom/dateTo are skipped for
     * these reports per explicit decision — so this only applies the
     * location filters, unlike applyFilters() above which also filters on
     * survey_form.synced_at.
     */
    private void applyFeederScopeOnly(StringBuilder sql, Map<String, Object> params,
                                      Long circleId, Long divisionId, Long subDivisionId, Long feederId) {
        if (circleId != null) {
            sql.append(" AND d.circle_id = :circleId");
            params.put("circleId", circleId);
        }
        if (divisionId != null) {
            sql.append(" AND sd.division_id = :divisionId");
            params.put("divisionId", divisionId);
        }
        if (subDivisionId != null) {
            sql.append(" AND f.sub_division_id = :subDivisionId");
            params.put("subDivisionId", subDivisionId);
        }
        if (feederId != null) {
            sql.append(" AND f.id = :feederId");
            params.put("feederId", feederId);
        }
    }

    @Override
    public List<ReportCountItem> poleStructureSummary(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo) {

        StringBuilder sql = new StringBuilder("""
            SELECT it.code, it.display_label, COUNT(pd.id)
            FROM pole_detail pd
            JOIN item_type it ON it.id = pd.structure_type_id
            JOIN survey_form sf ON sf.id = pd.survey_form_id
            JOIN work_order wo ON wo.id = sf.work_order_id
            JOIN feeder f ON f.id = wo.feeder_id
            LEFT JOIN sub_division sd ON sd.id = f.sub_division_id
            LEFT JOIN division d ON d.id = sd.division_id
            WHERE 1=1
        """);

        Map<String, Object> params = new HashMap<>();
        applyFilters(sql, params, circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);

        sql.append("""
            GROUP BY it.code, it.display_label, it.sort_order
            ORDER BY it.sort_order
        """);

        return jdbc.query(sql.toString(), params,
                (rs, i) -> new ReportCountItem(
                        rs.getString("code"),
                        rs.getString("display_label"),
                        rs.getLong(3)
                )
        );
    }

    @Override
    public PageResponse<ReportCountItem> poleStructureSummary(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo,
            int page, int size) {

        String base = """
        FROM pole_detail pd
        JOIN item_type it ON it.id = pd.structure_type_id
        JOIN survey_form sf ON sf.id = pd.survey_form_id
        JOIN work_order wo ON wo.id = sf.work_order_id
        JOIN feeder f ON f.id = wo.feeder_id
        LEFT JOIN sub_division sd ON sd.id = f.sub_division_id
        LEFT JOIN division d ON d.id = sd.division_id
        WHERE 1=1
    """;

        StringBuilder dataSql = new StringBuilder("""
        SELECT it.code, it.display_label, COUNT(pd.id)
    """).append(base);

        StringBuilder countSql = new StringBuilder("""
        SELECT COUNT(DISTINCT it.id)
    """).append(base);

        Map<String, Object> params = new HashMap<>();
        applyFilters(dataSql, params, circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);
        applyFilters(countSql, params, circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);

        dataSql.append("""
        GROUP BY it.code, it.display_label, it.sort_order
        ORDER BY it.sort_order
        LIMIT :limit OFFSET :offset
    """);

        params.put("limit", size);
        params.put("offset", offset(page, size));

        List<ReportCountItem> data = jdbc.query(dataSql.toString(), params,
                (rs, i) -> new ReportCountItem(
                        rs.getString("code"),
                        rs.getString("display_label"),
                        rs.getLong(3)
                )
        );

        long total = jdbc.queryForObject(countSql.toString(), params, Long.class);

        return new PageResponse<>(data, page, size, total);
    }

    @Override
    public List<ReportLengthItem> conductorSummary(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo) {

        StringBuilder sql = new StringBuilder("""
            SELECT it.code, it.display_label,
                   COUNT(cd.id),
                   COALESCE(SUM(sf.line_length_meters), 0)
            FROM conductor_detail cd
            JOIN item_type it ON it.id = cd.conductor_type_id
            JOIN survey_form sf ON sf.id = cd.survey_form_id
            JOIN work_order wo ON wo.id = sf.work_order_id
            JOIN feeder f ON f.id = wo.feeder_id
            LEFT JOIN sub_division sd ON sd.id = f.sub_division_id
            LEFT JOIN division d ON d.id = sd.division_id
            WHERE 1=1
        """);

        Map<String, Object> params = new HashMap<>();
        applyFilters(sql, params, circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);

        sql.append("""
            GROUP BY it.code, it.display_label, it.sort_order
            ORDER BY it.sort_order
        """);

        return jdbc.query(sql.toString(), params,
                (rs, i) -> new ReportLengthItem(
                        rs.getString("code"),
                        rs.getString("display_label"),
                        rs.getLong(3),
                        rs.getBigDecimal(4)
                )
        );
    }

    @Override
    public PageResponse<ReportLengthItem> conductorSummary(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo,
            int page, int size) {

        String base = """
        FROM conductor_detail cd
        JOIN item_type it ON it.id = cd.conductor_type_id
        JOIN survey_form sf ON sf.id = cd.survey_form_id
        JOIN work_order wo ON wo.id = sf.work_order_id
        JOIN feeder f ON f.id = wo.feeder_id
        LEFT JOIN sub_division sd ON sd.id = f.sub_division_id
        LEFT JOIN division d ON d.id = sd.division_id
        WHERE 1=1
    """;

        StringBuilder dataSql = new StringBuilder("""
        SELECT it.code, it.display_label,
               COUNT(cd.id),
               COALESCE(SUM(sf.line_length_meters), 0)
    """).append(base);

        StringBuilder countSql = new StringBuilder("""
        SELECT COUNT(DISTINCT it.id)
    """).append(base);

        Map<String, Object> params = new HashMap<>();
        applyFilters(dataSql, params, circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);
        applyFilters(countSql, params, circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);

        dataSql.append("""
        GROUP BY it.code, it.display_label, it.sort_order
        ORDER BY it.sort_order
        LIMIT :limit OFFSET :offset
    """);

        params.put("limit", size);
        params.put("offset", offset(page, size));

        List<ReportLengthItem> data = jdbc.query(dataSql.toString(), params,
                (rs, i) -> new ReportLengthItem(
                        rs.getString("code"),
                        rs.getString("display_label"),
                        rs.getLong(3),
                        rs.getBigDecimal(4)
                )
        );

        long total = jdbc.queryForObject(countSql.toString(), params, Long.class);

        return new PageResponse<>(data, page, size, total);
    }


    @Override
    public PageResponse<ReportCountItem> transformerCapacitySummary(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo,
            int page, int size) {

        String base = """
        FROM transformer_detail td
        JOIN item_type it ON it.id = td.capacity_id
        JOIN survey_form sf ON sf.id = td.survey_form_id
        JOIN work_order wo ON wo.id = sf.work_order_id
        JOIN feeder f ON f.id = wo.feeder_id
        LEFT JOIN sub_division sd ON sd.id = f.sub_division_id
        LEFT JOIN division d ON d.id = sd.division_id
        WHERE 1=1
    """;

        StringBuilder dataSql = new StringBuilder("""
        SELECT it.code, it.display_label, COUNT(td.id)
    """).append(base);

        StringBuilder countSql = new StringBuilder("""
        SELECT COUNT(DISTINCT it.id)
    """).append(base);

        Map<String, Object> params = new HashMap<>();
        applyFilters(dataSql, params, circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);
        applyFilters(countSql, params, circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);

        dataSql.append("""
        GROUP BY it.code, it.display_label, it.sort_order
        ORDER BY it.sort_order
        LIMIT :limit OFFSET :offset
    """);

        params.put("limit", size);
        params.put("offset", offset(page, size));

        List<ReportCountItem> data = jdbc.query(dataSql.toString(), params,
                (rs, i) -> new ReportCountItem(
                        rs.getString("code"),
                        rs.getString("display_label"),
                        rs.getLong(3)
                )
        );

        long total = jdbc.queryForObject(countSql.toString(), params, Long.class);

        return new PageResponse<>(data, page, size, total);
    }


    @Override
    public List<ReportCountItem> transformerCapacitySummary(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo) {

        StringBuilder sql = new StringBuilder("""
            SELECT it.code, it.display_label, COUNT(td.id)
            FROM transformer_detail td
            JOIN item_type it ON it.id = td.capacity_id
            JOIN survey_form sf ON sf.id = td.survey_form_id
            JOIN work_order wo ON wo.id = sf.work_order_id
            JOIN feeder f ON f.id = wo.feeder_id
            LEFT JOIN sub_division sd ON sd.id = f.sub_division_id
            LEFT JOIN division d ON d.id = sd.division_id
            WHERE 1=1
        """);

        Map<String, Object> params = new HashMap<>();
        applyFilters(sql, params, circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);

        sql.append("""
            GROUP BY it.code, it.display_label, it.sort_order
            ORDER BY it.sort_order
        """);

        return jdbc.query(sql.toString(), params,
                (rs, i) -> new ReportCountItem(
                        rs.getString("code"),
                        rs.getString("display_label"),
                        rs.getLong(3)
                )
        );
    }

    @Override
    public long meterSummary(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo) {

        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(md.id)
            FROM meter_detail md
            JOIN survey_form sf ON sf.id = md.survey_form_id
            JOIN work_order wo ON wo.id = sf.work_order_id
            JOIN feeder f ON f.id = wo.feeder_id
            LEFT JOIN sub_division sd ON sd.id = f.sub_division_id
            LEFT JOIN division d ON d.id = sd.division_id
            WHERE 1=1
        """);

        Map<String, Object> params = new HashMap<>();
        applyFilters(sql, params, circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);

        return jdbc.queryForObject(sql.toString(), params, Long.class);
    }



    // -- Feeder Assets Reports (SRS §3.15.2), feeder-row shape --
    //
    // The three methods below (deviceReportRaw/structureReportRaw/
    // conductorReportRaw) all zero-fill: every (feeder, item_type)
    // combination is emitted even when no detail rows exist for it, via
    // CROSS JOIN feeder x item_type + LEFT JOIN <detail table>. Feeders
    // with no survey data at all still appear as an all-zero row. This is
    // by explicit decision, at the cost of a per-feeder correlated
    // subquery to scope the LEFT JOIN to that feeder's survey forms.
    // dateFrom/dateTo filtering is intentionally skipped on these three
    // for now — see applyFeederScopeOnly() above.

    @Override
    public List<DeviceSummaryRow> deviceReportRaw(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo) {

        // Capacity tiers: item_type under TRANSFORMER_CAPACITY (confirmed
        // against data-item_type.csv: KVA_10...KVA_1500).
        // Duty type: item_type under EQUIPMENT_USE (DEDICATED /
        // GENERAL_DUTY), a second FK on transformer_detail — confirm the
        // FK column name below (assumed td.equipment_use_id) against the
        // actual entity/table.
        // Capacitor banks are NOT queried here: there is no capacitor
        // capacity (KVR) item_category in current seed data.
        StringBuilder sql = new StringBuilder("""
            SELECT f.id, f.code, f.name, gs.name,
                   duty.code, cap.code, cap.display_label,
                   COUNT(td.id)
            FROM feeder f
            CROSS JOIN item_type cap
            JOIN item_category capCat ON capCat.id = cap.category_id AND capCat.code = 'TRANSFORMER_CAPACITY'
            CROSS JOIN item_type duty
            JOIN item_category dutyCat ON dutyCat.id = duty.category_id AND dutyCat.code = 'EQUIPMENT_USE'
            LEFT JOIN transformer_detail td ON td.capacity_id = cap.id AND td.equipment_use_id = duty.id
                AND td.survey_form_id IN (
                    SELECT sf.id FROM survey_form sf
                    JOIN work_order wo ON wo.id = sf.work_order_id
                    WHERE wo.feeder_id = f.id
                )
            LEFT JOIN grid_station gs ON gs.id = f.grid_station_id
            LEFT JOIN sub_division sd ON sd.id = f.sub_division_id
            LEFT JOIN division d ON d.id = sd.division_id
            WHERE 1=1
        """);

        Map<String, Object> params = new HashMap<>();
        applyFeederScopeOnly(sql, params, circleId, divisionId, subDivisionId, feederId);

        sql.append("""
            GROUP BY f.id, f.code, f.name, gs.name, duty.code, cap.code, cap.display_label, cap.sort_order
            ORDER BY f.code, duty.code, cap.sort_order
        """);

        return jdbc.query(sql.toString(), params,
                (rs, i) -> new DeviceSummaryRow(
                        rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getString(7), rs.getLong(8)
                )
        );
    }

    @Override
    public List<StructureSummaryRow> structureReportRaw(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo) {

        // ic.code distinguishes Primary vs Secondary per the class
        // javadoc on PoleDetail (PRIMARY_STRUCTURE / SECONDARY_STRUCTURE).
        StringBuilder sql = new StringBuilder("""
            SELECT f.id, f.code, f.name, gs.name,
                   ic.code, it.code, it.display_label,
                   COUNT(pd.id)
            FROM feeder f
            CROSS JOIN item_type it
            JOIN item_category ic ON ic.id = it.category_id
                AND ic.code IN ('PRIMARY_STRUCTURE', 'SECONDARY_STRUCTURE')
            LEFT JOIN pole_detail pd ON pd.structure_type_id = it.id
                AND pd.survey_form_id IN (
                    SELECT sf.id FROM survey_form sf
                    JOIN work_order wo ON wo.id = sf.work_order_id
                    WHERE wo.feeder_id = f.id
                )
            LEFT JOIN grid_station gs ON gs.id = f.grid_station_id
            LEFT JOIN sub_division sd ON sd.id = f.sub_division_id
            LEFT JOIN division d ON d.id = sd.division_id
            WHERE 1=1
        """);

        Map<String, Object> params = new HashMap<>();
        applyFeederScopeOnly(sql, params, circleId, divisionId, subDivisionId, feederId);

        sql.append("""
            GROUP BY f.id, f.code, f.name, gs.name, ic.code, it.code, it.display_label, it.sort_order
            ORDER BY f.code, ic.code, it.sort_order
        """);

        return jdbc.query(sql.toString(), params,
                (rs, i) -> new StructureSummaryRow(
                        rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5).equals("PRIMARY_STRUCTURE") ? "PRIMARY" : "SECONDARY",
                        rs.getString(6), rs.getString(7), rs.getLong(8)
                )
        );
    }

    @Override
    public List<ConductorSummaryRow> conductorReportRaw(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo) {

        // ic.code distinguishes HT vs LT per the class javadoc on
        // ConductorDetail (HT_CONDUCTOR / LT_CONDUCTOR).
        StringBuilder sql = new StringBuilder("""
            SELECT f.id, f.code, f.name, gs.name,
                   ic.code, it.code, it.display_label,
                   COUNT(cd.id), COALESCE(SUM(cdsf.line_length_meters), 0)
            FROM feeder f
            CROSS JOIN item_type it
            JOIN item_category ic ON ic.id = it.category_id
                AND ic.code IN ('HT_CONDUCTOR', 'LT_CONDUCTOR')
            LEFT JOIN conductor_detail cd ON cd.conductor_type_id = it.id
                AND cd.survey_form_id IN (
                    SELECT sf.id FROM survey_form sf
                    JOIN work_order wo ON wo.id = sf.work_order_id
                    WHERE wo.feeder_id = f.id
                )
            LEFT JOIN survey_form cdsf ON cdsf.id = cd.survey_form_id
            LEFT JOIN grid_station gs ON gs.id = f.grid_station_id
            LEFT JOIN sub_division sd ON sd.id = f.sub_division_id
            LEFT JOIN division d ON d.id = sd.division_id
            WHERE 1=1
        """);

        Map<String, Object> params = new HashMap<>();
        applyFeederScopeOnly(sql, params, circleId, divisionId, subDivisionId, feederId);

        sql.append("""
            GROUP BY f.id, f.code, f.name, gs.name, ic.code, it.code, it.display_label, it.sort_order
            ORDER BY f.code, ic.code, it.sort_order
        """);

        return jdbc.query(sql.toString(), params,
                (rs, i) -> new ConductorSummaryRow(
                        rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5).equals("HT_CONDUCTOR") ? "HT" : "LT",
                        rs.getString(6), rs.getString(7), rs.getLong(8), rs.getBigDecimal(9)
                )
        );
    }

    @Override
    public PageResponse<MeterReportRow> meterReport(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo,
            String meterNo, int page, int size) {

        String base = """
        FROM meter_detail md
        JOIN survey_form sf ON sf.id = md.survey_form_id
        JOIN equipment_type et ON et.id = sf.equipment_type_id
        JOIN work_order wo ON wo.id = sf.work_order_id
        JOIN feeder f ON f.id = wo.feeder_id
        LEFT JOIN grid_station gs ON gs.id = f.grid_station_id
        LEFT JOIN sub_division sd ON sd.id = f.sub_division_id
        LEFT JOIN division d ON d.id = sd.division_id
        LEFT JOIN circle c ON c.id = d.circle_id
        WHERE 1=1
    """;

        StringBuilder dataSql = new StringBuilder("""
        SELECT f.code, f.name, gs.name, sd.name, d.name, c.name,
               et.code, md.consumer_reference, md.sanctioned_load,
               md.meter_number, md.meter_make
    """).append(base);

        StringBuilder countSql = new StringBuilder("SELECT COUNT(md.id)").append(base);

        Map<String, Object> params = new HashMap<>();
        applyFilters(dataSql, params, circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);
        applyFilters(countSql, params, circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);

        if (meterNo != null && !meterNo.isBlank()) {
            dataSql.append(" AND md.meter_number = :meterNo");
            countSql.append(" AND md.meter_number = :meterNo");
            params.put("meterNo", meterNo);
        }

        dataSql.append(" ORDER BY f.code LIMIT :limit OFFSET :offset");
        params.put("limit", size);
        params.put("offset", offset(page, size));

        List<MeterReportRow> data = jdbc.query(dataSql.toString(), params,
                (rs, i) -> new MeterReportRow(
                        rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8),
                        rs.getBigDecimal(9), rs.getString(10), rs.getString(11)
                )
        );

        long total = jdbc.queryForObject(countSql.toString(), params, Long.class);
        return new PageResponse<>(data, page, size, total);
    }

    private int offset(int page, int size) {
        return page * size;
    }
}