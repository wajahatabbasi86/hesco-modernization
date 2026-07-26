-- =====================================================================
-- HESCO Network Survey & Asset Management System
-- Schema: Admin Bound hierarchy (Circle/Division/Sub-Division) + Roles
-- Target: PostgreSQL 16+, consumed via Spring Boot 3.3+/Hibernate 6.5/Java 21
-- Ref: revamp plan §1-§3, SRS §2.1, §3.1, §3.2
--
-- REVISION NOTE (this version): all PL/pgSQL triggers/functions removed
-- per architecture decision to keep business/validation logic in the
-- application layer (Java), not the database. Only structural integrity
-- the database is good at is kept here: NOT NULL, UNIQUE, FK, and basic
-- single-column format CHECKs (regex on a column against itself, no
-- cross-table lookups). Everything that previously required a trigger
-- (cross-table code-prefix validation, role<->bound-type matching, IMEI
-- requirement, deletion dependency guards) now lives in Java validators/
-- services — see hesco-backend/src/main/java/.../adminbound and .../user.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. ADMIN BOUND HIERARCHY
-- ---------------------------------------------------------------------
-- Coding convention (SRS §3.1.1):
--   circle:       3 digits, e.g. 110
--   division:     4 digits, first 3 = parent circle code, e.g. 1101
--   sub_division: 5 digits, first 4 = parent division code, e.g. 11011
--
-- The "first N digits must equal parent's code" rule is CROSS-TABLE and is
-- therefore NOT expressed as a DB CHECK/trigger anymore — it is enforced by
-- AdminBoundCodeValidator (Java) before any INSERT/UPDATE is attempted, and
-- by AdminBoundService which is the only path allowed to write these
-- tables. The single-column format (exactly N digits) IS still a DB CHECK
-- since it needs no other table to verify.

CREATE TABLE circle (
    id              BIGSERIAL PRIMARY KEY,
    code            CHAR(3)      NOT NULL UNIQUE,
    name            VARCHAR(150) NOT NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_circle_code_format CHECK (code ~ '^[0-9]{3}$')
);

CREATE TABLE division (
    id              BIGSERIAL PRIMARY KEY,
    circle_id       BIGINT       NOT NULL REFERENCES circle(id) ON DELETE RESTRICT,
    code            CHAR(4)      NOT NULL UNIQUE,
    name            VARCHAR(150) NOT NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_division_code_format CHECK (code ~ '^[0-9]{4}$')
    -- code-prefix-matches-parent-circle rule: enforced in
    -- AdminBoundCodeValidator.validateDivisionCode(), not here.
);

CREATE TABLE sub_division (
    id              BIGSERIAL PRIMARY KEY,
    division_id     BIGINT       NOT NULL REFERENCES division(id) ON DELETE RESTRICT,
    code            CHAR(5)      NOT NULL UNIQUE,
    name            VARCHAR(150) NOT NULL,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_sub_division_code_format CHECK (code ~ '^[0-9]{5}$')
    -- code-prefix-matches-parent-division rule: enforced in
    -- AdminBoundCodeValidator.validateSubDivisionCode(), not here.
);

-- Deletion guards (SRS §3.1.5): "cannot delete a bound with dependent
-- feeders/users/work orders" is a cross-table read (COUNT queries against
-- app_user/feeder/work_order), not a structural DB rule, and the error
-- needs to say *why* — so this is entirely owned by
-- AdminBoundService.assertDeletable() in Java, via plain repository count
-- queries. No dependency views are defined in this schema (see the
-- revision note at the top of 02_feeder_grid_station.sql for why the
-- earlier view-based version of this was also moved to Java).

-- ---------------------------------------------------------------------
-- 2. ROLES
-- ---------------------------------------------------------------------
-- SRS §3.2.2 table lists 9 rows despite the prose claiming "six distinct
-- roles" (open question flagged for HESCO/LMKR — not yet resolved).
-- Modeling all 9 here; bound scoping is driven by assigned_bound_type,
-- not by which literal role that happens to map to, so collapsing this
-- to 6 later (if confirmed) is a data change, not a schema change.

CREATE TYPE bound_type AS ENUM ('SYSTEM_WIDE', 'CIRCLE', 'DIVISION', 'SUB_DIVISION', 'NONE');

CREATE TABLE role (
    id                  SMALLSERIAL PRIMARY KEY,
    code                VARCHAR(50)  NOT NULL UNIQUE,   -- e.g. 'HESCO_ADMIN', 'APPROVER_1'
    display_name        VARCHAR(100) NOT NULL,
    assigned_bound_type bound_type   NOT NULL,          -- scoping level this role operates at
    requires_imei       BOOLEAN      NOT NULL DEFAULT FALSE, -- true only for Surveyor (mobile-primary)
    is_active           BOOLEAN      NOT NULL DEFAULT TRUE
);

INSERT INTO role (code, display_name, assigned_bound_type, requires_imei) VALUES
    ('HESCO_ADMIN',                'HESCO Admin',                          'SYSTEM_WIDE',  FALSE),
    ('GIS_ADMIN',                  'GIS Admin',                            'NONE',         FALSE),
    ('APPROVER_2',                 'Approver 2',                           'CIRCLE',       FALSE),
    ('APPROVER_1',                 'Approver 1',                           'DIVISION',     FALSE),
    ('CREATOR',                    'Creator',                              'SUB_DIVISION', FALSE),
    ('SURVEYOR',                   'Surveyor',                             'SUB_DIVISION', TRUE),
    ('PLANNING_ENGINEER_AREA',     'Planning Engineer - Area Planning',    'NONE',         FALSE),
    ('PLANNING_ENGINEER_LINELOSS', 'Planning Engineer - Line Loss',        'NONE',         FALSE),
    ('DASHBOARD_USER',             'Dashboard User',                       'NONE',         FALSE);

-- ---------------------------------------------------------------------
-- 3. APP USER
-- ---------------------------------------------------------------------
-- Three nullable bound FK columns (rather than one polymorphic bound_id)
-- so real FK integrity is kept per bound type. chk_user_single_bound below
-- is a plain structural CHECK (counts non-null columns, no other table
-- involved) so it stays in the DB. The rule that the *populated* column
-- must match the *role's* assigned_bound_type, and that IMEI is required
-- when role.requires_imei = true, both need a lookup into the role table
-- and therefore move to UserRoleBoundValidator (Java) — enforced by
-- UserService before any INSERT/UPDATE, not by a DB trigger anymore.

CREATE TABLE app_user (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    contact_number  VARCHAR(20),
    role_id         SMALLINT     NOT NULL REFERENCES role(id) ON DELETE RESTRICT,

    circle_id       BIGINT       REFERENCES circle(id)       ON DELETE RESTRICT,
    division_id     BIGINT       REFERENCES division(id)     ON DELETE RESTRICT,
    sub_division_id BIGINT       REFERENCES sub_division(id) ON DELETE RESTRICT,

    imei            VARCHAR(20),  -- required only when role.requires_imei = true (checked in Java)
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT chk_user_single_bound CHECK (
        (circle_id IS NOT NULL)::int
      + (division_id IS NOT NULL)::int
      + (sub_division_id IS NOT NULL)::int <= 1
    )
);

CREATE UNIQUE INDEX ux_app_user_imei ON app_user (imei) WHERE imei IS NOT NULL;

-- ---------------------------------------------------------------------
-- 4. Indexes for common access patterns (bound-scoped queries everywhere)
-- ---------------------------------------------------------------------
CREATE INDEX ix_division_circle_id       ON division (circle_id);
CREATE INDEX ix_sub_division_division_id ON sub_division (division_id);
CREATE INDEX ix_app_user_role_id         ON app_user (role_id);
CREATE INDEX ix_app_user_circle_id       ON app_user (circle_id);
CREATE INDEX ix_app_user_division_id     ON app_user (division_id);
CREATE INDEX ix_app_user_sub_division_id ON app_user (sub_division_id);
