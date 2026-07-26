-- =====================================================================
-- HESCO Network Survey & Asset Management System
-- Schema: Warehouse/Reference lookup tables + Equipment Sequence Rules
-- Database: hesco_local
-- Depends on: 01_admin_bound_and_roles.sql
-- Ref: revamp plan §2.4, §2.5; SRS §3.5, §3.15.2, §8.3.2
-- =====================================================================
-- REVISION NOTE: unchanged from prior version -- this file never used
-- triggers/functions; equipment_sequence_transition is plain reference
-- data read by EquipmentSequenceValidator (Java), enforcement happens there.

-- ---------------------------------------------------------------------
-- 1. WAREHOUSE ITEM CATEGORY / TYPE (SRS §3.5)
-- ---------------------------------------------------------------------
-- Generic category -> item-type shape, reused (per the plan's §2.4
-- recommendation) as the backing store for the SRS's fixed enumerated
-- lists (transformer KVA buckets, pole/structure types, conductor types)
-- so reports-service can pivot against configured data instead of
-- hardcoded Java enums — satisfying the §6.4 "Administrative Expansion...
-- configurable without code modification" NFR by extension.

CREATE TABLE item_category (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,  -- e.g. 'TRANSFORMER_CAPACITY', 'PRIMARY_STRUCTURE', 'HT_CONDUCTOR'
    name        VARCHAR(150) NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE item_type (
    id              SERIAL PRIMARY KEY,
    category_id     INTEGER      NOT NULL REFERENCES item_category(id) ON DELETE RESTRICT,
    code            VARCHAR(50)  NOT NULL,   -- machine-safe key, e.g. 'KVA_10', 'DOG', 'PC_SPUN'
    display_label   VARCHAR(150) NOT NULL,   -- human label as it appears in reports, e.g. "10 KVA", "Dog"
    sort_order      INTEGER      NOT NULL DEFAULT 0,  -- controls fixed column ordering in reports-service DTOs
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_item_type_category_code UNIQUE (category_id, code)
);

CREATE INDEX ix_item_type_category_id ON item_type (category_id);

-- Seed categories
INSERT INTO item_category (code, name) VALUES
    ('TRANSFORMER_CAPACITY', 'Transformer Capacity (KVA)'),
    ('PRIMARY_STRUCTURE',    'Primary Structure Type'),
    ('SECONDARY_STRUCTURE',  'Secondary Structure Type'),
    ('HT_CONDUCTOR',         'HT Conductor Type'),
    ('LT_CONDUCTOR',         'LT Conductor Type'),
    ('POLE_CLASS',           'Pole Class'),           -- 8.3.3 dropdown, not enumerated in SRS but same shape
    ('TRANSFORMER_MOUNTING', 'Transformer Mounting'), -- 8.3.5 dropdown
    ('TRANSFORMER_FUSE',     'Transformer Fuse Type'); -- 8.3.5 dropdown

-- SRS §3.15.2.1: transformer capacity buckets
INSERT INTO item_type (category_id, code, display_label, sort_order)
SELECT id, v.code, v.label, v.ord FROM item_category,
  (VALUES ('KVA_10','10 KVA',1),('KVA_15','15 KVA',2),('KVA_25','25 KVA',3),('KVA_50','50 KVA',4),
          ('KVA_100','100 KVA',5),('KVA_200','200 KVA',6),('KVA_400','400 KVA',7),
          ('KVA_630','630 KVA',8),('KVA_1500','1500 KVA',9)) AS v(code, label, ord)
WHERE item_category.code = 'TRANSFORMER_CAPACITY';

-- SRS §3.15.2.2: primary structure types
INSERT INTO item_type (category_id, code, display_label, sort_order)
SELECT id, v.code, v.label, v.ord FROM item_category,
  (VALUES ('DOUBLE_TUBE_WELL_PIPE_POLE','Double Tube Well Pipe Pole',1),('FOUNDATION','Foundation',2),
          ('OTHER','Other',3),('PANEL','Panel',4),('PC_ORDINARY','PC Ordinary',5),('PC_SPUN','PC Spun',6),
          ('STEEL_STRUCTURE','Steel Structure',7),('TERMINAL_POLE','Terminal Pole',8),
          ('TUBE_WELL_PIPE_POLE','Tube Well Pipe Pole',9),('TUBULAR_STEEL','Tubular Steel',10),
          ('WOODEN_TRUNK','Wooden Trunk',11)) AS v(code, label, ord)
WHERE item_category.code = 'PRIMARY_STRUCTURE';

-- SRS §3.15.2.2: secondary structure types — same list minus "Terminal Pole",
-- with "TS" replacing "Steel Structure" per the SRS's literal wording. Kept
-- as the SRS states it (flagged in the revamp plan as possibly a typo, not
-- yet confirmed by HESCO/LMKR) rather than silently "fixing" it — if it's
-- confirmed a typo, this is a one-row UPDATE, not a re-migration.
INSERT INTO item_type (category_id, code, display_label, sort_order)
SELECT id, v.code, v.label, v.ord FROM item_category,
  (VALUES ('DOUBLE_TUBE_WELL_PIPE_POLE','Double Tube Well Pipe Pole',1),('FOUNDATION','Foundation',2),
          ('OTHER','Other',3),('PANEL','Panel',4),('PC_ORDINARY','PC Ordinary',5),('PC_SPUN','PC Spun',6),
          ('TS','TS',7),
          ('TUBULAR_STEEL','Tubular Steel',8),('WOODEN_TRUNK','Wooden Trunk',9)) AS v(code, label, ord)
WHERE item_category.code = 'SECONDARY_STRUCTURE';

-- SRS §3.15.2.3: HT conductor types (23 distinct types)
INSERT INTO item_type (category_id, code, display_label, sort_order)
SELECT id, v.code, v.label, v.ord FROM item_category,
  (VALUES ('2_0_AWG','2/0 AWG',1),('2C_07_052','2/C 07/.052',2),('4_0_AWG','4/0 AWG',3),
          ('4C_07_052','4/C 07/.052',4),('4C_19_052','4/C 19/.052',5),('4C_37_083','4/C 37/.083',6),
          ('120_MM','120 MM',7),('500_MCM','500 MCM',8),('1000_MCM','1000 MCM',9),('ANT','ANT',10),
          ('DOG','Dog',11),('GNAT','GNAT',12),('GOPHER','GOPHER',13),('GSL','GSL',14),('LYNX','Lynx',15),
          ('OSPREY_2','OSPREY 2',16),('OSPREY','OSPREY',17),('PANTHER','Panther',18),
          ('PVC_7_0_052','PVC 7/0.052',19),('PVC_19_0_052','PVC 19/0.052',20),('PVC_19_0_083','PVC 19/0.083',21),
          ('RABBIT','RABBIT',22),('WASP','WASP',23)) AS v(code, label, ord)
WHERE item_category.code = 'HT_CONDUCTOR';

-- SRS §3.15.2.3: LT conductor types (4 types)
INSERT INTO item_type (category_id, code, display_label, sort_order)
SELECT id, v.code, v.label, v.ord FROM item_category,
  (VALUES ('ANT','ANT',1),('PVC_7_0_052','PVC 7/0.052',2),
          ('PVC_19_0_052','PVC 19/0.052',3),('PVC_19_0_083','PVC 19/0.083',4)) AS v(code, label, ord)
WHERE item_category.code = 'LT_CONDUCTOR';

-- NOTE: POLE_CLASS, TRANSFORMER_MOUNTING, TRANSFORMER_FUSE (§8.3.3, §8.3.5)
-- are mentioned in the SRS only as "selected from a dropdown list" with no
-- enumerated values given — categories created above so warehouse-service
-- has somewhere to hang admin-entered values, but left unseeded pending an
-- actual list from HESCO/LMKR.

-- ---------------------------------------------------------------------
-- 2. EQUIPMENT SURVEY SEQUENCING (SRS §8.3.2, revamp plan §2.5)
-- ---------------------------------------------------------------------
-- Feeder Pole -> Primary Pole -> Transformer -> Secondary Pole -> Meter,
-- with per-equipment-type rules on which S/E point types are legal. Modeled
-- as a transitions table (mirroring GEPCO's WorkOrderStatus.valid_post_operation
-- array pattern per the plan) rather than hardcoded if/else chains, so a
-- future new equipment type doesn't require a code change.

CREATE TYPE se_point_type AS ENUM ('START_POINT', 'MID_POINT', 'END_POINT');

CREATE TABLE equipment_type (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(30)  NOT NULL UNIQUE,   -- FEEDER_POLE, PRIMARY_POLE, TRANSFORMER, SECONDARY_POLE, METER
    display_name VARCHAR(100) NOT NULL,
    can_be_start BOOLEAN     NOT NULL,           -- SRS §8.3.2 "Sequence Rules" column, condensed to booleans
    can_be_end   BOOLEAN     NOT NULL,
    sort_order  INTEGER      NOT NULL
);

INSERT INTO equipment_type (code, display_name, can_be_start, can_be_end, sort_order) VALUES
    ('FEEDER_POLE',    'Feeder Pole',    TRUE,  FALSE, 1), -- always the first Start Point; never an End Point
    ('PRIMARY_POLE',   'Primary Pole',   TRUE,  TRUE,  2), -- can be both Start and End
    ('TRANSFORMER',    'Transformer',    FALSE, TRUE,  3), -- End Point of HT survey only, never Start
    ('SECONDARY_POLE', 'Secondary Pole', TRUE,  FALSE, 4), -- can start any survey, never an End Point
    ('METER',          'Meter',         FALSE,  TRUE,  5); -- End Point of LT survey only, never Start

-- Legal "previous End Point equipment type" -> "next form's Start Point
-- equipment type" transitions, per §8.3.2's note: "The Start Point Equipment
-- Type for a new form is always auto-filled based on the Equipment Type from
-- the previous End Point form." Encodes continuity across forms explicitly,
-- so survey-mobile-app / survey-service just look this up instead of
-- re-deriving the rule in code on both the mobile client and server.
CREATE TABLE equipment_sequence_transition (
    id                      SERIAL PRIMARY KEY,
    from_end_equipment_id   INTEGER NOT NULL REFERENCES equipment_type(id),
    to_start_equipment_id   INTEGER NOT NULL REFERENCES equipment_type(id),
    CONSTRAINT uq_equipment_transition UNIQUE (from_end_equipment_id, to_start_equipment_id)
);

-- Valid transitions derived from §8.3.2's rule table:
--  Primary Pole (end) -> next form can start at Primary Pole or Secondary Pole
--  Transformer (end)  -> next form starts at Secondary Pole (transformer can't be a start)
--  Secondary Pole (end) -- not legal, Secondary Pole can't be an End Point, so no row
--  Meter (end)        -> terminal; no further form continues from it, so no row
INSERT INTO equipment_sequence_transition (from_end_equipment_id, to_start_equipment_id)
SELECT e1.id, e2.id FROM equipment_type e1, equipment_type e2
WHERE (e1.code = 'PRIMARY_POLE' AND e2.code IN ('PRIMARY_POLE', 'SECONDARY_POLE'))
   OR (e1.code = 'TRANSFORMER'  AND e2.code = 'SECONDARY_POLE');

-- Enforcement point: when a new survey form is created, survey-service
-- looks up equipment_sequence_transition WHERE from_end_equipment_id =
-- <previous form's end equipment>, and rejects/auto-fills accordingly. This
-- table is the single source of truth rather than duplicating the rule in
-- both the offline mobile app and the server-side sync validator — the
-- mobile app ships a cached copy of this table for offline enforcement and
-- the server re-validates on sync.
