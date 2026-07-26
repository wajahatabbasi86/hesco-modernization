-- =====================================================================
-- HESCO Network Survey & Asset Management System
-- Schema: Work Order (4-tier approval state machine) + Survey Section
-- Database: hesco_local
-- Depends on: 01_admin_bound_and_roles.sql, 02_feeder_grid_station.sql,
--             03_reference_data_and_equipment_sequence.sql
-- Ref: revamp plan §1 (work-order-service), §2.5; SRS §3.6, §8.3
--
-- REVISION NOTE: no triggers/functions in this file. The state machine
-- (legal from_status+action -> to_status per role), the mandatory
-- rejection comment rule, and Creator/feeder bound-scope enforcement are
-- ALL enforced by WorkOrderStateMachineService (Java) — this file only
-- holds the data shape and the transition table as plain reference data
-- (same pattern as equipment_sequence_transition in 03_*.sql).
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. WORK ORDER STATUS / ACTION / TRANSITION (reference data, not logic)
-- ---------------------------------------------------------------------
-- SRS §3.6.2 lifecycle: Creator creates+assigns -> Surveyor completes ->
-- Creator validates/submits -> Approver 1 (Division) -> Approver 2
-- (Circle) -> GIS Admin posts. Rejections drop the work order back a
-- step (SRS §3.6.4/5/6: Approver 1 reject -> back to Creator; Approver 2
-- reject -> back to Approver 1; GIS Admin reject -> back to Approver 2)
-- rather than introducing new terminal statuses.

CREATE TABLE work_order_status (
    id      SMALLSERIAL PRIMARY KEY,
    code    VARCHAR(40) NOT NULL UNIQUE,
    label   VARCHAR(100) NOT NULL
);

INSERT INTO work_order_status (code, label) VALUES
    ('CREATED',                 'Created'),
    ('ASSIGNED',                'Assigned to Surveyor'),
    ('SURVEY_COMPLETED',        'Survey Completed (synced)'),
    ('VALIDATED',               'Validated by Creator'),
    ('SUBMITTED',               'Submitted to Approver 1'),
    ('REJECTED_BY_APPROVER_1',  'Rejected by Approver 1'),
    ('APPROVED_BY_APPROVER_1',  'Approved by Approver 1'),
    ('REJECTED_BY_APPROVER_2',  'Rejected by Approver 2'),
    ('APPROVED_BY_APPROVER_2',  'Approved by Approver 2'),
    ('REJECTED_BY_GIS_ADMIN',   'Rejected by GIS Admin'),
    ('POSTED',                  'Posted (live on GIS map)'),
    ('DELETED',                 'Deleted');

CREATE TABLE work_order_action (
    id      SMALLSERIAL PRIMARY KEY,
    code    VARCHAR(30) NOT NULL UNIQUE   -- ASSIGN, COMPLETE_SURVEY, VALIDATE, REVERT, SUBMIT, APPROVE, REJECT, POST, DELETE
);

INSERT INTO work_order_action (code) VALUES
    ('ASSIGN'), ('COMPLETE_SURVEY'), ('VALIDATE'), ('REVERT'),
    ('SUBMIT'), ('APPROVE'), ('REJECT'), ('POST'), ('DELETE');

-- Legal (from_status, action) -> to_status, tagged with the role allowed
-- to perform it and whether a comment is mandatory (SRS §3.6.4/5/6:
-- Reject always requires a comment). WorkOrderStateMachineService looks
-- up exactly one row for (current status, requested action, caller's
-- role) and rejects the request if no row matches, or if requires_comment
-- is true and no comment was supplied — this is the single source of
-- truth the plan called for, just owned by Java instead of a trigger.
CREATE TABLE work_order_transition (
    id                  SERIAL PRIMARY KEY,
    from_status_id      SMALLINT NOT NULL REFERENCES work_order_status(id),
    action_id           SMALLINT NOT NULL REFERENCES work_order_action(id),
    role_id             SMALLINT NOT NULL REFERENCES role(id),
    to_status_id        SMALLINT NOT NULL REFERENCES work_order_status(id),
    requires_comment    BOOLEAN  NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_wo_transition UNIQUE (from_status_id, action_id, role_id)
);

INSERT INTO work_order_transition (from_status_id, action_id, role_id, to_status_id, requires_comment)
SELECT s1.id, a.id, r.id, s2.id, req
FROM (VALUES
    ('CREATED',                'ASSIGN',          'CREATOR',   'ASSIGNED',               FALSE),
    ('ASSIGNED',                'COMPLETE_SURVEY', 'SURVEYOR',  'SURVEY_COMPLETED',       FALSE),
    ('SURVEY_COMPLETED',        'VALIDATE',        'CREATOR',   'VALIDATED',              FALSE),
    ('SURVEY_COMPLETED',        'REVERT',          'CREATOR',   'ASSIGNED',               FALSE),
    ('VALIDATED',                'SUBMIT',          'CREATOR',   'SUBMITTED',              FALSE),
    ('SUBMITTED',                'APPROVE',         'APPROVER_1','APPROVED_BY_APPROVER_1', FALSE),
    ('SUBMITTED',                'REJECT',          'APPROVER_1','REJECTED_BY_APPROVER_1', TRUE),
    ('REJECTED_BY_APPROVER_1',   'SUBMIT',          'CREATOR',   'SUBMITTED',              FALSE),
    ('APPROVED_BY_APPROVER_1',   'APPROVE',         'APPROVER_2','APPROVED_BY_APPROVER_2', FALSE),
    ('APPROVED_BY_APPROVER_1',   'REJECT',          'APPROVER_2','REJECTED_BY_APPROVER_2', TRUE),
    ('REJECTED_BY_APPROVER_2',   'APPROVE',         'APPROVER_1','APPROVED_BY_APPROVER_1', FALSE),
    ('APPROVED_BY_APPROVER_2',   'POST',            'GIS_ADMIN', 'POSTED',                 FALSE),
    ('APPROVED_BY_APPROVER_2',   'REJECT',          'GIS_ADMIN', 'REJECTED_BY_GIS_ADMIN',  TRUE),
    ('REJECTED_BY_GIS_ADMIN',    'APPROVE',         'APPROVER_2','APPROVED_BY_APPROVER_2', FALSE),
    ('CREATED',                  'DELETE',          'CREATOR',   'DELETED',                FALSE),
    ('ASSIGNED',                  'DELETE',          'CREATOR',   'DELETED',                FALSE),
    ('VALIDATED',                 'DELETE',          'CREATOR',   'DELETED',                FALSE)
) AS v(from_code, action_code, role_code, to_code, req)
JOIN work_order_status s1 ON s1.code = v.from_code
JOIN work_order_action a  ON a.code = v.action_code
JOIN role r                ON r.code = v.role_code
JOIN work_order_status s2 ON s2.code = v.to_code;

-- ---------------------------------------------------------------------
-- 2. WORK ORDER
-- ---------------------------------------------------------------------
-- SRS §3.6.3: Creator can only create work orders for feeders assigned
-- to their own Sub-Division. This is a cross-table check (creator's
-- app_user.sub_division_id vs feeder.sub_division_id) and is therefore
-- NOT a DB CHECK/trigger — enforced by
-- WorkOrderStateMachineService.assertCreatorScope() before insert.

CREATE TYPE work_order_type AS ENUM ('HT', 'LT', 'FULL_UPDATE');

CREATE TABLE work_order (
    id              BIGSERIAL PRIMARY KEY,
    feeder_id       BIGINT           NOT NULL REFERENCES feeder(id) ON DELETE RESTRICT,
    wo_type         work_order_type  NOT NULL,
    status_id       SMALLINT         NOT NULL REFERENCES work_order_status(id),
    created_by      BIGINT           NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT, -- Creator
    assigned_to     BIGINT           REFERENCES app_user(id) ON DELETE RESTRICT,           -- Surveyor
    location_lat    DOUBLE PRECISION,
    location_lng    DOUBLE PRECISION,
    created_at      TIMESTAMPTZ      NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ      NOT NULL DEFAULT now()
);

CREATE INDEX ix_work_order_feeder_id   ON work_order (feeder_id);
CREATE INDEX ix_work_order_status_id   ON work_order (status_id);
CREATE INDEX ix_work_order_created_by  ON work_order (created_by);
CREATE INDEX ix_work_order_assigned_to ON work_order (assigned_to);

-- Full audit trail of every transition actually applied (who, when, from
-- what status, via what action, to what status, with what comment). This
-- is what backs the Status-wise Logs report (SRS §3.15.3.2) and the
-- Feeder-wise Logs report (§3.15.3.1) — WorkOrderStateMachineService
-- writes exactly one row here per successful transition, in the same
-- transaction as the work_order.status_id update.
CREATE TABLE work_order_transition_log (
    id              BIGSERIAL PRIMARY KEY,
    work_order_id   BIGINT       NOT NULL REFERENCES work_order(id) ON DELETE CASCADE,
    from_status_id  SMALLINT     NOT NULL REFERENCES work_order_status(id),
    action_id       SMALLINT     NOT NULL REFERENCES work_order_action(id),
    to_status_id    SMALLINT     NOT NULL REFERENCES work_order_status(id),
    performed_by    BIGINT       NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
    comment         TEXT,        -- mandatory on REJECT, enforced in Java before this row is written
    performed_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX ix_wo_transition_log_work_order_id ON work_order_transition_log (work_order_id);

-- ---------------------------------------------------------------------
-- 3. SURVEY FORM (Section Information only — SRS §8.3.1)
-- ---------------------------------------------------------------------
-- One row per submitted survey form (one pole/point in a conductor
-- section). Deliberately holds ONLY Section Information here; the
-- Pole/Conductor/Transformer/Meter detail sub-entities (SRS §8.3.3-8.3.6)
-- are separate tables in a follow-up migration, each FK'd back to
-- survey_form.id, since which of the four applies depends on
-- equipment_type_id and mixing them into one wide table fights the
-- "only shown when Equipment Type = X" mobile UI logic.
--
-- Equipment Start/End point legality (SRS §8.3.2) is enforced by
-- EquipmentSequenceValidator (Java) against equipment_sequence_transition
-- (03_reference_data_and_equipment_sequence.sql) before insert — not a
-- DB constraint, since it depends on the *previous* form's end equipment
-- type within the same work order.

CREATE TABLE survey_form (
    id                  BIGSERIAL PRIMARY KEY,
    work_order_id       BIGINT        NOT NULL REFERENCES work_order(id) ON DELETE CASCADE,
    se_point            se_point_type NOT NULL,  -- START_POINT / MID_POINT / END_POINT (type from 03_*.sql)
    gps_number          VARCHAR(30)   NOT NULL UNIQUE, -- <UserNo><DDMMYY><Serial>, see GpsNumberService
    equipment_type_id   INTEGER       NOT NULL REFERENCES equipment_type(id),
    line_length_meters  NUMERIC(10,2),
    submitted_by        BIGINT        NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT, -- Surveyor
    latitude            DOUBLE PRECISION,
    longitude           DOUBLE PRECISION,
    remarks             TEXT,
    synced_at           TIMESTAMPTZ,   -- NULL until synced from the offline mobile app
    created_at          TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX ix_survey_form_work_order_id     ON survey_form (work_order_id);
CREATE INDEX ix_survey_form_equipment_type_id ON survey_form (equipment_type_id);
