# HESCO Backend — Monolith (Maven build + full api layer)

Continued from the prior session's README (schema + Java validation layer).
This pass turns that scaffold into a buildable, single-module monolith:
one `pom.xml`, one Spring Boot app, each domain as its own package with
its own `entity / repository / service / exception / api` layers.

## What changed in this pass

1. **`pom.xml`** — Spring Boot 3.3.4, Java 21, `spring-boot-starter-web`,
   `-data-jpa`, `-validation`, Postgres driver, Flyway, and
   `spring-security-crypto` (BCrypt only — not the full security starter,
   since `auth-service`/JWT isn't in this drop yet).
2. **`HescoApplication.java`** + **`application.yml`** — standard Spring
   Boot entry point and datasource/Flyway/JPA config (env-var overridable).
3. **`sql/*.sql` -> `src/main/resources/db/migration/V1-V4__*.sql`** —
   renamed to Flyway's convention, otherwise untouched.
4. **`common/api/`** — `ApiResponse<T>` (uniform envelope), `ApiError`,
   and `GlobalExceptionHandler` (`@RestControllerAdvice`) mapping every
   domain exception from the previous session (`InvalidCodeHierarchyException`,
   `RoleBoundMismatchException`, `DependentRecordsExistException`,
   `InvalidEquipmentSequenceException`, `DuplicateGpsNumberException`,
   `CreatorScopeViolationException`, `InvalidWorkOrderTransitionException`,
   `MissingRejectionCommentException`) to the right HTTP status.
5. **Repositories added** for every module (previously only the
   query-only helper interfaces existed): `CircleRepository`,
   `DivisionRepository`, `SubDivisionRepository`, `AppUserRepository`,
   `RoleRepository`, `FeederRepository`, `FeederAssignmentLogRepository`,
   `GridStationRepository`, `PowerTransformerRepository`,
   `SurveyFormRepository`, `EquipmentTypeRepository`,
   `WorkOrderRepository`, `WorkOrderStatusRepository`,
   `WorkOrderTransitionLogRepository`.
6. **The two `// TODO` stubs from the last session are wired**:
   - `FeederService.assign()/unassign()` now actually persist the feeder
     and the `FeederAssignmentLog` row in the same transaction.
   - `WorkOrderStateMachineService.applyTransition()` now saves the
     `WorkOrder` status change and the `WorkOrderTransitionLog` row, and
     returns the saved `WorkOrder`.
7. **`api/` layer added for all 6 modules** (controllers + request/
   response DTOs) — this was entirely missing before:

   | Module | Controllers |
   |---|---|
   | `adminbound` | `CircleController`, `DivisionController`, `SubDivisionController` |
   | `user` | `AppUserController` (backed by new `UserService`, which runs `UserRoleBoundValidator` before every save) |
   | `feeder` | `FeederController` (create + assign/unassign) |
   | `gridstation` | `GridStationController`, `PowerTransformerController` |
   | `survey` | `SurveyFormController` (backed by new `SurveyService`, which runs `EquipmentSequenceValidator` + `GpsNumberService` before every sync) |
   | `workorder` | `WorkOrderController` (create + `/transition`, backed by new `WorkOrderService`) |

8. **Entities got the getters/setters/constructors the DTOs and services
   needed** (`AppUser`, `Role`, `Feeder`, `GridStation`,
   `PowerTransformer`, `SurveyForm`, `WorkOrder`, `WorkOrderStatus`) —
   the previous drop only exposed what the validators themselves used.

## Still open (carried over, unchanged from last session)

- No `auth-service` yet — these endpoints have no authentication/
  authorization wired in. `actorUserId` / `createdByUserId` etc. are
  passed as plain request fields for now; once `auth-service` exists,
  those should come from the JWT claims instead (per the revamp plan's
  bound-scoped-claims design), not the request body.
- Pole/Conductor/Transformer/Meter detail tables (SRS section 8.3.3-8.3.6) —
  still a follow-up migration.
- HT/LT/Full-Update work-order-type constraint on top of equipment
  sequencing — still an open decision (new column vs. second Java check).
- 6-vs-9 roles SRS inconsistency — still open, `role` table seeds all 9.
- GPS Number format disagreement (SRS body vs. Appendix A) — still needs
  HESCO/LMKR sign-off; `GpsNumberService` still implements Appendix A.
- `line-loss-service` (Python/Power Panda) and `transmission-line-service`
  aren't in this pom at all — per the revamp plan's build order, they're
  intentionally last and have no code yet.

## ## Roadmap — remaining modules, in build order

Reordered from the original revamp-plan sequence based on actual
dependency analysis (not just plan order). Auth was pushed later
because nothing currently reads real identity anyway — building
reports/dashboard read-heavy modules first surfaces the reference-data
gaps before auth work has to be threaded through them.

1. **`reports-service`** — blocked on two things that don't exist yet:
   (a) fixed enumerated lookup tables (transformer capacity buckets,
   structure types, HT/LT conductor types — SRS §2.4), which should
   reuse the `warehouse` category→item-type pattern rather than
   hardcoded Java enums; (b) Pole/Conductor/Transformer/Meter detail
   tables (SRS §8.3.3–8.3.6) so there's actual asset data to aggregate.
   Porting the refcursor/JDBC pattern from GEPCO's reports-service is
   the easy part once those two exist.
2. **`dashboard-service`** — Circle/Division/Sub-Division/date-range
   filters, 11 named summary counts (SRS §3.9). Well-specified, and
   `admin-bound` + `work-order` are already queryable — no open design
   questions.
3. **`auth-service`** — JWT + IMEI device binding + bound-scoped claims
   (Circle/Division/Sub-Division baked into the token) so
   `work-order-service`'s per-role query scoping stops being
   theoretical. Every existing controller needs a follow-up pass to
   pull actor identity from the token instead of the request body's
   `*UserId` fields.
4. **`line-loss-service`** — standalone Python microservice wrapping
   "Power Panda," called from the gateway/monolith rather than ported
   to Java. Architecturally isolated — second language, own
   deployment story.
5. **`transmission-line-service`** — entirely new domain, no GEPCO
   equivalent: second mobile survey app, separate approval flow
   (Planning Engineer only, not the 4-tier WO chain), new asset types
   (towers, capacitor/battery banks, circuits).
6. **`area-planning-service`** — reconductoring, bifurcation,
   rerouting, line-loss rerun trigger. Extends GEPCO's
   `AreaPlanningController` domain; the rerun hook depends on
   `line-loss-service` existing.
7. **`gis-map-service`** — direct port of GEPCO's already-built Path A
   scaffold (Show Feeders on Map, identify, legend, print,
   measurement, search-by-pole-number). Lowest design risk of
   everything remaining — proven pattern, mostly reuse — but placed
   last in this pass since nothing else depends on it.

## Known gaps inside modules already built

- Pole/Conductor/Transformer/Meter detail tables (SRS §8.3.3–8.3.6) —
  needed by `reports-service`, see roadmap item 1.
- HT/LT/Full-Update work-order-type constraint on top of equipment
  sequencing — still an open decision (new column vs. second Java check).
- 6-vs-9 roles SRS inconsistency — still open, `role` table seeds all 9.
- GPS Number format disagreement (SRS body vs. Appendix A) — still needs
  HESCO/LMKR sign-off; `GpsNumberService` still implements Appendix A.
- Flyway is currently disabled — migrations exist but aren't applied
  automatically; trivial to re-enable once pointed at a real DB.

## Pass 3 — fixes from code review (this drop)

Per discussion, addressed 4 of the 5 known issues (Flyway enablement
explicitly deferred — not required for now):

1. **`GlobalExceptionHandler` now maps every custom exception** —
   `InvalidCodeHierarchyException`, `RoleBoundMismatchException`,
   `InvalidEquipmentSequenceException`, `MissingRejectionCommentException`
   → 400; `CreatorScopeViolationException` → 403;
   `DuplicateGpsNumberException`, `InvalidWorkOrderTransitionException`,
   `DependentRecordsExistException` → 409 — matching
   `hesco-api-contract.md`'s error-code table exactly. Nothing falls
   through to the generic 500 handler anymore except truly unexpected errors.
2. **`WorkOrderService.create()` no longer sets `assignedTo` directly.**
   A work order is created in `CREATED` status with no assignee. New
   `POST /api/work-orders/{id}/assign` endpoint (`WorkOrderAssignRequest`:
   `surveyorUserId`, `actorUserId`) drives the `CREATED -> ASSIGN ->
   ASSIGNED` transition through `WorkOrderStateMachineService`, same as
   every other status change — status and assignee can't drift apart.
3. ~~`flyway.enabled: false`~~ — deferred, not required right now.
4. **New `warehouse` module** (`item_category`/`item_type`, SRS §3.5):
   full `entity/repository/service/api` layer, matching the `V3`
   migration schema exactly. `GET/POST /api/warehouse/categories`,
   `GET/POST/PUT /api/warehouse/item-types`.
5. **Tests added** for the four core validators/services:
   `AdminBoundCodeValidatorTest`, `UserRoleBoundValidatorTest`,
   `EquipmentSequenceValidatorTest`, `WorkOrderStateMachineServiceTest`
   — all pure Mockito/JUnit 5, no live database required, which was the
   whole point of moving this logic out of Postgres triggers.

`hesco-api-contract.md` updated with the new `/assign` endpoint and the
full `Warehouse` module section.

## Pass 4 — survey detail tables wired end-to-end (this drop)

Fixed the two blocking issues found in review of `feature/reports-service`:

1. **New `V5__survey_detail_tables.sql`** — creates `pole_detail`,
   `conductor_detail`, `transformer_detail`, `meter_detail`, matching the
   `PoleDetail`/`ConductorDetail`/`TransformerDetail`/`MeterDetail`
   entities exactly (these existed with no migration before this pass —
   `ddl-auto: validate` would have failed at startup the moment Flyway
   actually ran).
2. **`SurveyService.submit()` now actually writes the detail rows.**
   `SurveyFormRequest` gained four optional nested fields (`poleDetail`,
   `conductorDetail`, `transformerDetail`, `meterDetail`); exactly the
   one matching a form's `equipmentTypeCode` is now required (SRS
   §8.3.3/§8.3.5/§8.3.6), and `conductorDetail` is required independently
   whenever `sePoint` is `END_POINT` (§8.3.4) — enforced by new
   `InvalidSurveyDetailException` (400), wired into
   `GlobalExceptionHandler`. Each detail code (`structureTypeCode`,
   `conductorTypeCode`, `capacityCode`) is resolved against `item_type`
   and checked against the expected `item_category`
   (`PRIMARY_STRUCTURE`/`SECONDARY_STRUCTURE`, `HT_CONDUCTOR`/
   `LT_CONDUCTOR` per work-order type, `TRANSFORMER_CAPACITY`) — so
   `reports-service`'s joins against these tables will actually return
   data instead of always being empty.
3. **Bonus one-liner**: `SurveyForm.syncedAt` is now set at submit time —
   it was never set before, so `reports-service`'s `dateFrom`/`dateTo`
   filters (which filter on `syncedAt`) would have silently excluded
   every row.
4. `SurveyFormResponse` now includes the matching detail sub-object
   (`poleDetail`/`conductorDetail`/`transformerDetail`/`meterDetail`,
   whichever applies) in both `GET /api/survey-forms` and
   `POST /api/survey-forms/sync` — previously would have been read back
   as `null` even after being saved, since nothing populated it.

Deliberately NOT addressed in this pass (unchanged, still open):
Flyway is still disabled; the `common/config/JpaConfig.java` question
from the last discussion; the HT/LT-vs-FULL_UPDATE conductor-category
ambiguity is handled pragmatically (either category accepted for
FULL_UPDATE) rather than resolved, same open-question status as before.