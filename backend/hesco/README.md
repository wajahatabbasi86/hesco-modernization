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

## Suggested next step

This hasn't been compiled — there's no Maven/network access in the
environment this was written in to verify against the real dependency
tree. Run `mvn clean compile` first; after that, the next real step
per the revamp plan's build order is still `auth-service` with
bound-scoped JWT claims, since every controller here currently trusts
whatever user ID the caller passes in.

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
