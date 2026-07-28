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

    @Override
    public List<DeviceSummaryRow> deviceReportRaw(
            Long circleId, Long divisionId, Long subDivisionId,
            Long feederId, OffsetDateTime dateFrom, OffsetDateTime dateTo) {

        // Capacity tiers come from item_type (category TRANSFORMER_CAPACITY)
        // instead of a hardcoded KVA list, so this stays correct if tiers
        // are added/renamed/removed via seed data — same item_type-driven
        // approach as structureReportRaw/conductorReportRaw. Confirm
        // 'TRANSFORMER_CAPACITY' against actual item_category.code seed
        // data and adjust the WHERE clause below if different.
        StringBuilder sql = new StringBuilder("""
            SELECT f.id, f.code, f.name, gs.name,
                   it.code, it.display_label, COUNT(td.id)
            FROM transformer_detail td
            JOIN item_type it ON it.id = td.capacity_id
            JOIN item_category ic ON ic.id = it.category_id
            JOIN survey_form sf ON sf.id = td.survey_form_id
            JOIN work_order wo ON wo.id = sf.work_order_id
            JOIN feeder f ON f.id = wo.feeder_id
            LEFT JOIN grid_station gs ON gs.id = f.grid_station_id
            LEFT JOIN sub_division sd ON sd.id = f.sub_division_id
            LEFT JOIN division d ON d.id = sd.division_id
            WHERE ic.code = 'TRANSFORMER_CAPACITY'
        """);

        Map<String, Object> params = new HashMap<>();
        applyFilters(sql, params, circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);

        sql.append("""
            GROUP BY f.id, f.code, f.name, gs.name, it.code, it.display_label, it.sort_order
            ORDER BY f.code, it.sort_order
        """);

        return jdbc.query(sql.toString(), params,
                (rs, i) -> new DeviceSummaryRow(
                        rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getLong(7)
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
                   ic.code, it.code, it.display_label, COUNT(pd.id)
            FROM pole_detail pd
            JOIN item_type it ON it.id = pd.structure_type_id
            JOIN item_category ic ON ic.id = it.category_id
            JOIN survey_form sf ON sf.id = pd.survey_form_id
            JOIN work_order wo ON wo.id = sf.work_order_id
            JOIN feeder f ON f.id = wo.feeder_id
            LEFT JOIN grid_station gs ON gs.id = f.grid_station_id
            LEFT JOIN sub_division sd ON sd.id = f.sub_division_id
            LEFT JOIN division d ON d.id = sd.division_id
            WHERE ic.code IN ('PRIMARY_STRUCTURE', 'SECONDARY_STRUCTURE')
        """);

        Map<String, Object> params = new HashMap<>();
        applyFilters(sql, params, circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);

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
                   COUNT(cd.id), COALESCE(SUM(sf.line_length_meters), 0)
            FROM conductor_detail cd
            JOIN item_type it ON it.id = cd.conductor_type_id
            JOIN item_category ic ON ic.id = it.category_id
            JOIN survey_form sf ON sf.id = cd.survey_form_id
            JOIN work_order wo ON wo.id = sf.work_order_id
            JOIN feeder f ON f.id = wo.feeder_id
            LEFT JOIN grid_station gs ON gs.id = f.grid_station_id
            LEFT JOIN sub_division sd ON sd.id = f.sub_division_id
            LEFT JOIN division d ON d.id = sd.division_id
            WHERE ic.code IN ('HT_CONDUCTOR', 'LT_CONDUCTOR')
        """);

        Map<String, Object> params = new HashMap<>();
        applyFilters(sql, params, circleId, divisionId, subDivisionId, feederId, dateFrom, dateTo);

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