-- Capacitor Banks support for the Device Report (legacy "Capacitor
-- Banks (KVA)" column group). Previously omitted because
-- TRANSFORMER_CAPACITY only had the 9 transformer KVA tiers — no
-- capacitor capacity category existed at all.
--
-- Decision: a SEPARATE capacitor_detail table (mirrors
-- transformer_detail's shape) rather than overloading
-- transformer_detail with capacitor-only columns. KVR tiers seeded
-- from the legacy report's legend (0/100/150/300/400/450 KVR) —
-- confirm against actual HESCO capacitor inventory before relying on
-- this list being complete; more tiers can be added as item_type rows
-- with no code change (same item_type-driven pattern as everything
-- else in reports-service).

INSERT INTO item_category (code, name, is_active)
VALUES ('CAPACITOR_CAPACITY', 'Capacitor Bank Capacity (KVR)', true);

INSERT INTO item_type (category_id, code, display_label, sort_order, is_active)
SELECT ic.id, v.code, v.display_label, v.sort_order, true
FROM item_category ic
CROSS JOIN (VALUES
    ('KVR_0',   '0 KVR',   1),
    ('KVR_100', '100 KVR', 2),
    ('KVR_150', '150 KVR', 3),
    ('KVR_300', '300 KVR', 4),
    ('KVR_400', '400 KVR', 5),
    ('KVR_450', '450 KVR', 6)
) AS v(code, display_label, sort_order)
WHERE ic.code = 'CAPACITOR_CAPACITY';

CREATE TABLE capacitor_detail (
    id BIGSERIAL PRIMARY KEY,
    survey_form_id BIGINT NOT NULL REFERENCES survey_form(id),
    capacity_id BIGINT NOT NULL REFERENCES item_type(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

/* SCRIPTS FOR DATA CREATION
INSERT INTO capacitor_detail (survey_form_id, capacity_id)
SELECT sample.survey_form_id, it.id
FROM (
         -- Pick 5 distinct survey forms that already have transformer data,
         -- so each one belongs to a real feeder/work_order.
         SELECT DISTINCT survey_form_id
         FROM transformer_detail
         ORDER BY survey_form_id
             LIMIT 5
     ) AS sample
         CROSS JOIN LATERAL (
    -- Give each sampled survey form 1-2 capacitor readings at varying
    -- tiers, so counts differ across feeders in the report.
    SELECT code FROM item_type where category_id=13

        ) AS tier(code)
         JOIN item_type it ON it.code = tier.code
         JOIN item_category ic ON ic.id = it.category_id AND ic.code = 'CAPACITOR_CAPACITY';*/
