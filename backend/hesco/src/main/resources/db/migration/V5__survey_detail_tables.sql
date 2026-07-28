-- =====================================================================
-- HESCO Network Survey & Asset Management System
-- Schema: Survey detail tables (Pole / Conductor / Transformer / Meter)
-- Ref: SRS §8.3.3-§8.3.6, §3.15.2
-- =====================================================================
-- REVISION NOTE: this file was referenced in the backend README ("Pass 4
-- — survey detail tables wired end-to-end") as already created, but was
-- never actually committed — entities (PoleDetail/ConductorDetail/
-- TransformerDetail/MeterDetail) existed with no backing migration, so
-- ddl-auto: validate would fail at startup the moment Flyway ran against
-- a real DB. This is that migration, written directly against the
-- current entity shapes, with the richer field set from the reports-service
-- polish pass folded in from the start (no separate later ALTER needed
-- for fields that were never live) plus the meter_report columns that
-- previously lived in the placeholder V9999__add_meter_report_columns.sql
-- (deleted — superseded by this file).

-- ---------------------------------------------------------------------
-- 0. New reference-data categories these tables depend on
-- ---------------------------------------------------------------------
-- EQUIPMENT_USE is explicitly enumerated in the SRS ("General Duty and
-- Dedicated" — §8.3.5) so it's seeded, same as TRANSFORMER_CAPACITY etc.
-- POLE_END_TYPE / POLE_ASSEMBLY are, like POLE_CLASS/TRANSFORMER_MOUNTING/
-- TRANSFORMER_FUSE before them, mentioned only as "selected from a
-- dropdown list" with no enumerated values given — categories created so
-- warehouse-service has somewhere to hang admin-entered values, left
-- unseeded pending an actual list from HESCO/LMKR.
INSERT INTO item_category (code, name) VALUES
    ('EQUIPMENT_USE',  'Transformer Equipment Use'),
    ('POLE_END_TYPE',  'Pole End Type'),
    ('POLE_ASSEMBLY',  'Pole Assembly');

INSERT INTO item_type (category_id, code, display_label, sort_order)
SELECT id, v.code, v.label, v.ord FROM item_category,
  (VALUES ('GENERAL_DUTY', 'General Duty', 1), ('DEDICATED', 'Dedicated', 2)) AS v(code, label, ord)
WHERE item_category.code = 'EQUIPMENT_USE';

-- ---------------------------------------------------------------------
-- 1. POLE_DETAIL (SRS §8.3.3)
-- ---------------------------------------------------------------------
-- structure_type_id keeps the existing PRIMARY_STRUCTURE/SECONDARY_STRUCTURE
-- lookup (drives the Structure Report, SRS §3.15.2.2). end_type_id and
-- pole_assembly_id are optional FKs into the two new unseeded categories
-- above — nullable, since HESCO/LMKR haven't supplied the dropdown values
-- yet; the app must not block survey submission on missing reference data
-- it doesn't control.
CREATE TABLE pole_detail (
    id                  BIGSERIAL PRIMARY KEY,
    survey_form_id      BIGINT      NOT NULL UNIQUE REFERENCES survey_form(id) ON DELETE CASCADE,
    structure_type_id   INTEGER     NOT NULL REFERENCES item_type(id),
    pole_number         VARCHAR(30),
    height_meters       NUMERIC(6, 2),
    no_of_feeders       INTEGER,
    end_type_id         INTEGER     REFERENCES item_type(id),
    pole_assembly_id    INTEGER     REFERENCES item_type(id),
    pole_earthing       BOOLEAN,
    asset_code          VARCHAR(50),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- 2. CONDUCTOR_DETAIL (SRS §8.3.4)
-- ---------------------------------------------------------------------
-- One row PER PHASE, not one row per End Point form — the SRS records a
-- conductor type for each of R/Y/B (HT and LT) and N (LT only), with an
-- "All Phases" input shortcut on the client that just means the same
-- conductor_type_id gets written to every applicable phase row rather
-- than being a distinct data shape server-side. This replaces the
-- earlier single-row-per-survey_form design, which had no way to
-- represent per-phase conductor types at all.
CREATE TYPE conductor_phase AS ENUM ('R', 'Y', 'B', 'N');

CREATE TABLE conductor_detail (
    id                  BIGSERIAL PRIMARY KEY,
    survey_form_id      BIGINT           NOT NULL REFERENCES survey_form(id) ON DELETE CASCADE,
    phase               conductor_phase  NOT NULL,
    conductor_type_id   INTEGER          NOT NULL REFERENCES item_type(id),
    created_at          TIMESTAMPTZ      NOT NULL DEFAULT now(),
    CONSTRAINT uq_conductor_detail_form_phase UNIQUE (survey_form_id, phase)
);

CREATE INDEX ix_conductor_detail_survey_form_id ON conductor_detail (survey_form_id);

-- ---------------------------------------------------------------------
-- 3. TRANSFORMER_DETAIL (SRS §8.3.5)
-- ---------------------------------------------------------------------
-- equipment_use_id -> EQUIPMENT_USE (seeded above). mounting_id/fuses_id
-- reuse the TRANSFORMER_MOUNTING/TRANSFORMER_FUSE categories already
-- created (unseeded) in V3. equipment_number is the auto-generated
-- 'T-' + gps_number value (SurveyService computes it, not the client).
-- equipment_phase is a derived summary string of the phases captured in
-- conductor_detail for this survey_form's End Point (e.g. "R,Y,B"),
-- written at submit time per the SRS's "auto-filled from Pole Phases"
-- note — a snapshot, not a live join, since conductor_detail rows for a
-- pole are captured on a *different* form (the pole's own End Point)
-- than the transformer's own row.
CREATE TABLE transformer_detail (
    id                    BIGSERIAL PRIMARY KEY,
    survey_form_id        BIGINT      NOT NULL UNIQUE REFERENCES survey_form(id) ON DELETE CASCADE,
    capacity_id           INTEGER     NOT NULL REFERENCES item_type(id),
    transformer_name      VARCHAR(100),
    cable_size            VARCHAR(30),
    ct_ratio              VARCHAR(30),
    equipment_number      VARCHAR(40),
    equipment_phase       VARCHAR(10),
    equipment_use_id      INTEGER     REFERENCES item_type(id),
    mounting_id           INTEGER     REFERENCES item_type(id),
    fuses_id              INTEGER     REFERENCES item_type(id),
    asset_code            VARCHAR(50),
    consumer_name         VARCHAR(150),
    equipment_location    VARCHAR(255),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- 4. METER_DETAIL (SRS §8.3.6)
-- ---------------------------------------------------------------------
-- sanctioned_load/meter_make were previously added via the placeholder
-- V9999__add_meter_report_columns.sql (deleted) for the Meter Report
-- (SRS §3.15.2.4) — folded directly into the create here since that
-- migration never actually ran against a real DB.
CREATE TABLE meter_detail (
    id                    BIGSERIAL PRIMARY KEY,
    survey_form_id        BIGINT      NOT NULL UNIQUE REFERENCES survey_form(id) ON DELETE CASCADE,
    meter_number           VARCHAR(30),
    consumer_reference     VARCHAR(50),
    sanctioned_load        NUMERIC(10, 2),
    meter_make             VARCHAR(100),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);
