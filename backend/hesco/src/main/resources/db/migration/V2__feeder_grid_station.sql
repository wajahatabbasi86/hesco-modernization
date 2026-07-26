-- =====================================================================
-- HESCO Network Survey & Asset Management System
-- Schema: Feeder, Grid Station, Power Transformer
-- Database: hesco_local
-- Depends on: 01_admin_bound_and_roles.sql (sub_division, app_user)
-- Ref: revamp plan §2.3, §3.3, §3.4; SRS §3.3, §3.4
--
-- REVISION NOTE (this version): the dependency-count VIEWS from the prior
-- version (v_sub_division_dependency_counts etc.) are removed. They were
-- read-only, so they were not "business logic" in the trigger sense, but
-- per the decision to keep a single source of truth for anything the app
-- needs a friendly error message from, these counts are now plain SELECT/
-- COUNT queries issued by AdminBoundService / FeederService (Java) at
-- delete-time, not a DB-side view the app happens to query. No PL/pgSQL
-- was ever in this file, so the tables below are unchanged in shape.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. GRID STATION + POWER TRANSFORMER
-- ---------------------------------------------------------------------
-- SRS §3.4.1: Grid Station has Name + Code, and a list of Power
-- Transformers. transmission-line-service will later hang MVA-rated
-- transformers, capacitor/battery banks, tower assets off grid_station
-- too — kept generic enough here that grid_station itself doesn't need
-- to change when that lands.

CREATE TABLE grid_station (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(20)  NOT NULL UNIQUE,
    name            VARCHAR(150) NOT NULL,
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- SRS §3.4.2 table: Transformer Name, Cable Size, CT Ratio, Capacity (KVA).
CREATE TABLE power_transformer (
    id              BIGSERIAL PRIMARY KEY,
    grid_station_id BIGINT       NOT NULL REFERENCES grid_station(id) ON DELETE RESTRICT,
    transformer_name VARCHAR(100) NOT NULL,
    cable_size      VARCHAR(50),   -- e.g. "500 MCM"
    ct_ratio        VARCHAR(20),   -- e.g. "400/5"
    capacity_kva    NUMERIC(10,2), -- power rating; see item_type (03_reference_data.sql)
                                   -- for the fixed KVA-bucket lookup used by reports-service
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_power_transformer_name_per_station UNIQUE (grid_station_id, transformer_name)
);

CREATE INDEX ix_power_transformer_grid_station_id ON power_transformer (grid_station_id);

-- ---------------------------------------------------------------------
-- 2. FEEDER
-- ---------------------------------------------------------------------
-- SRS §3.3: a feeder can exist unassigned (sub_division_id NULL) before
-- being handed to a Sub-Division (SRS §3.3.4), which is why that FK is
-- nullable rather than NOT NULL.

CREATE TABLE feeder (
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(30)  NOT NULL UNIQUE,
    name            VARCHAR(150) NOT NULL,
    grid_station_id BIGINT       REFERENCES grid_station(id) ON DELETE RESTRICT,
    sub_division_id BIGINT       REFERENCES sub_division(id) ON DELETE RESTRICT,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_feeder_sub_division_id ON feeder (sub_division_id);
CREATE INDEX ix_feeder_grid_station_id ON feeder (grid_station_id);

-- Assignment history (SRS §3.3.4). Append-only log rather than just
-- overwriting feeder.sub_division_id, so "mapped feeders" / survey-progress
-- reporting (§3.3.1) and the Status-wise Logs report (§3.15.3.2) have an
-- audit trail of who assigned what, when. Writing this log row happens in
-- the same @Transactional FeederService.assign()/unassign() call that
-- updates feeder.sub_division_id — application-managed, not trigger-managed.
CREATE TABLE feeder_assignment_log (
    id              BIGSERIAL PRIMARY KEY,
    feeder_id       BIGINT       NOT NULL REFERENCES feeder(id) ON DELETE CASCADE,
    sub_division_id BIGINT       REFERENCES sub_division(id) ON DELETE RESTRICT, -- NULL row = "unassigned" event
    action          VARCHAR(10)  NOT NULL CHECK (action IN ('ASSIGN', 'UNASSIGN')),
    performed_by    BIGINT       NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
    performed_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_feeder_assignment_log_feeder_id ON feeder_assignment_log (feeder_id);
