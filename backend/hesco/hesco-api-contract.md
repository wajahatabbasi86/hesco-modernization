# HESCO Backend — API Contract

Reflects the actual code in `backend/hesco/src/main/java/com/lmkr/hesco`
as of this pass (all 9 modules: adminbound, user, auth, feeder,
gridstation, survey, workorder, warehouse, reports).

## Response Envelope

**Success** — `ApiResponse<T>`:
```json
{ "success": true, "message": "...", "data": { } }
```

**Error** — `ApiErrorResponse` (thrown by `GlobalExceptionHandler`):
```json
{
  "success": false,
  "message": "human-readable reason",
  "errorCode": "BAD_REQUEST | VALIDATION_ERROR | UNAUTHORIZED | FORBIDDEN | NOT_FOUND | CONFLICT | TOO_MANY_REQUESTS | INTERNAL_ERROR",
  "timestamp": "2026-07-29T12:00:00",
  "path": "/api/..."
}
```

## Global Error Codes (all endpoints)

| HTTP | errorCode | Trigger |
|---|---|---|
| 400 | `VALIDATION_ERROR` | `@Valid` field-level failure (`MethodArgumentNotValidException`) — every endpoint below with a request body |
| 400 | `BAD_REQUEST` | Business-rule rejection (see per-exception table) |
| 401 | `UNAUTHORIZED` | Bad credentials |
| 403 | `FORBIDDEN` | Actor not permitted / inactive account |
| 404 | `NOT_FOUND` | Entity does not exist (`EntityNotFoundException`) |
| 409 | `CONFLICT` | Conflicts with current state (dependent records, duplicate, illegal transition) |
| 429 | `TOO_MANY_REQUESTS` | Rate limit exceeded (login) |
| 500 | `INTERNAL_ERROR` | Unhandled exception (fallback only) |

| Exception | HTTP | errorCode |
|---|---|---|
| `InvalidCodeHierarchyException` | 400 | BAD_REQUEST |
| `RoleBoundMismatchException` | 400 | BAD_REQUEST |
| `InvalidEquipmentSequenceException` | 400 | BAD_REQUEST |
| `InvalidSurveyDetailException` | 400 | BAD_REQUEST |
| `MissingRejectionCommentException` | 400 | BAD_REQUEST |
| `MissingReportScopeException` | 400 | BAD_REQUEST |
| `PasswordPolicyViolationException` | 400 | BAD_REQUEST |
| `PasswordReuseException` | 400 | BAD_REQUEST |
| `InvalidResetTokenException` | 400 | BAD_REQUEST (deliberately not 404 — avoids token enumeration) |
| `InvalidCredentialsException` | 401 | UNAUTHORIZED (covers both unknown user and wrong password — avoids user enumeration) |
| `CreatorScopeViolationException` | 403 | FORBIDDEN |
| `InactiveAccountException` | 403 | FORBIDDEN |
| `MobileLoginNotAllowedException` | 403 | FORBIDDEN |
| `RateLimitExceededException` | 429 | TOO_MANY_REQUESTS |
| `EntityNotFoundException` | 404 | NOT_FOUND |
| `DependentRecordsExistException` | 409 | CONFLICT |
| `DuplicateGpsNumberException` | 409 | CONFLICT |
| `InvalidWorkOrderTransitionException` | 409 | CONFLICT |

---

## 1. Admin Bound — Circles

Base: `/api/admin-bound/circles`

| Method | Path | Success | Failure |
|---|---|---|---|
| GET | `/` | 200, `List<CircleResponse>` (empty list if none) | — |
| GET | `/{id}` | 200, `CircleResponse` | 404 NOT_FOUND if id doesn't exist |
| POST | `/` | 200, created `CircleResponse`, message "Circle created" | 400 VALIDATION_ERROR (missing/blank `code`/`name`); 400 BAD_REQUEST via `InvalidCodeHierarchyException` if code format/hierarchy invalid |
| PUT | `/{id}` | 200, updated `CircleResponse`, message "Circle updated" | 404 NOT_FOUND if id doesn't exist; 400 VALIDATION_ERROR; 400 BAD_REQUEST invalid hierarchy |
| DELETE | `/{id}` | 200, `data: null`, message "Circle deleted" | 404 NOT_FOUND; 409 CONFLICT via `DependentRecordsExistException` if Divisions still reference this Circle |

## 2. Admin Bound — Divisions

Base: `/api/admin-bound/divisions`

| Method | Path | Success | Failure |
|---|---|---|---|
| GET | `/?circleId=` | 200, `List<DivisionResponse>` (optionally filtered by `circleId`) | — |
| GET | `/{id}` | 200, `DivisionResponse` | 404 NOT_FOUND |
| POST | `/` | 200, created `DivisionResponse`, "Division created" | 400 VALIDATION_ERROR; 400 BAD_REQUEST invalid code hierarchy; 404 NOT_FOUND if `circleId` doesn't exist |
| PUT | `/{id}` | 200, updated `DivisionResponse`, "Division updated" | 404 NOT_FOUND; 400 VALIDATION_ERROR / BAD_REQUEST |
| DELETE | `/{id}` | 200, "Division deleted" | 404 NOT_FOUND; 409 CONFLICT if Sub-Divisions still reference it |

## 3. Admin Bound — Sub-Divisions

Base: `/api/admin-bound/sub-divisions`

| Method | Path | Success | Failure |
|---|---|---|---|
| GET | `/?divisionId=` | 200, `List<SubDivisionResponse>` | — |
| GET | `/{id}` | 200, `SubDivisionResponse` | 404 NOT_FOUND |
| POST | `/` | 200, created `SubDivisionResponse`, "Sub-Division created" | 400 VALIDATION_ERROR; 400 BAD_REQUEST invalid code hierarchy; 404 NOT_FOUND if `divisionId` doesn't exist |
| PUT | `/{id}` | 200, updated, "Sub-Division updated" | 404 NOT_FOUND; 400 VALIDATION_ERROR / BAD_REQUEST |
| DELETE | `/{id}` | 200, "Sub-Division deleted" | 404 NOT_FOUND; 409 CONFLICT if Feeders/Users still reference it |

## 4. Users

Base: `/api/users`

| Method | Path | Success | Failure |
|---|---|---|---|
| GET | `/` | 200, `List<AppUserResponse>` | — |
| GET | `/{id}` | 200, `AppUserResponse` | 404 NOT_FOUND |
| POST | `/` | 200, created `AppUserResponse`, "User created" | 400 VALIDATION_ERROR (missing required fields); 400 BAD_REQUEST via `RoleBoundMismatchException` if the role's expected bound type (Circle/Division/Sub-Division/none) doesn't match the assigned bound |
| PUT | `/{id}` | 200, updated `AppUserResponse`, "User updated" | 404 NOT_FOUND; 400 VALIDATION_ERROR / `RoleBoundMismatchException` |

> Note: password is not set/changed through this endpoint — see Auth
> module. `actorUserId`/`createdByUserId` are still plain request
> fields (JWT-claim wiring is a roadmap item).

## 5. Feeders

Base: `/api/feeders`

| Method | Path | Success | Failure |
|---|---|---|---|
| GET | `/` | 200, `List<FeederResponse>` | — |
| GET | `/{id}` | 200, `FeederResponse` | 404 NOT_FOUND |
| POST | `/` | 200, created `FeederResponse`, "Feeder created" | 400 VALIDATION_ERROR; 404 NOT_FOUND if `gridStationId` doesn't exist |
| POST | `/{id}/assign` | 200, `FeederResponse` with new Sub-Division, "Feeder assigned"; also writes a `FeederAssignmentLog` row in the same transaction | 404 NOT_FOUND (feeder or `subDivisionId`); 400 VALIDATION_ERROR |
| POST | `/{id}/unassign` | 200, `FeederResponse` with assignment cleared, "Feeder unassigned"; logs the unassignment | 404 NOT_FOUND; 400 VALIDATION_ERROR |

## 6. Grid Stations

Base: `/api/grid-stations`

| Method | Path | Success | Failure |
|---|---|---|---|
| GET | `/` | 200, `List<GridStationResponse>` | — |
| GET | `/{id}` | 200, `GridStationResponse` | 404 NOT_FOUND |
| POST | `/` | 200, created, "Grid Station created" | 400 VALIDATION_ERROR |
| PUT | `/{id}` | 200, updated, "Grid Station updated" | 404 NOT_FOUND; 400 VALIDATION_ERROR |
| DELETE | `/{id}` | 200, "Grid Station deleted" | 404 NOT_FOUND; 409 CONFLICT if Feeders/Power Transformers still reference it |

## 7. Power Transformers

Base: `/api/power-transformers`

| Method | Path | Success | Failure |
|---|---|---|---|
| GET | `/?gridStationId=` | 200, `List<PowerTransformerResponse>` | — |
| POST | `/` | 200, created, "Power Transformer created" | 400 VALIDATION_ERROR; 404 NOT_FOUND if `gridStationId` doesn't exist |
| DELETE | `/{id}` | 200, "Power Transformer deleted" | 404 NOT_FOUND; 409 CONFLICT if referenced elsewhere |

## 8. Survey Forms

Base: `/api/survey-forms`

| Method | Path | Success | Failure |
|---|---|---|---|
| GET | `/?workOrderId=` (required) | 200, `List<SurveyFormResponse>` (includes matching detail sub-object) | 400 VALIDATION_ERROR if `workOrderId` missing |
| POST | `/sync` | 200, `SurveyFormResponse` incl. saved detail row, "Survey form synced" | 400 VALIDATION_ERROR; 400 BAD_REQUEST via `InvalidEquipmentSequenceException` (equipment sequence rule violated); 400 BAD_REQUEST via `InvalidSurveyDetailException` (wrong/missing detail object for the form's `equipmentTypeCode`, or `sePoint=END_POINT` missing `conductorDetail`); 409 CONFLICT via `DuplicateGpsNumberException` (GPS number already used); 404 NOT_FOUND if `workOrderId` doesn't exist |

## 9. Work Orders

Base: `/api/work-orders`

| Method | Path | Success | Failure |
|---|---|---|---|
| GET | `/` | 200, `List<WorkOrderResponse>` | — |
| GET | `/{id}` | 200, `WorkOrderResponse` | 404 NOT_FOUND |
| POST | `/` | 200, created in `CREATED` status, no assignee, "Work Order created" | 400 VALIDATION_ERROR |
| POST | `/{id}/assign` | 200, `WorkOrderResponse` (status `ASSIGNED`), "Work Order assigned" — drives `CREATED -> ASSIGN -> ASSIGNED` via the state machine | 404 NOT_FOUND; 409 CONFLICT via `InvalidWorkOrderTransitionException` if not currently in `CREATED`; 400 VALIDATION_ERROR |
| POST | `/{id}/transition` | 200, `WorkOrderResponse` with new status, "Work Order transitioned"; writes a `WorkOrderTransitionLog` row | 404 NOT_FOUND; 409 CONFLICT via `InvalidWorkOrderTransitionException` (illegal status change for current state); 400 BAD_REQUEST via `MissingRejectionCommentException` (rejecting without a comment); 403 FORBIDDEN via `CreatorScopeViolationException` (actor not the WO's creator/assignee where required); 400 VALIDATION_ERROR |

## 10. Warehouse — Item Categories

Base: `/api/warehouse/categories`

| Method | Path | Success | Failure |
|---|---|---|---|
| GET | `/` | 200, `List<ItemCategoryResponse>` | — |
| GET | `/{id}` | 200, `ItemCategoryResponse` | 404 NOT_FOUND |
| POST | `/` | 200, created, "Item Category created" | 400 VALIDATION_ERROR |

## 11. Warehouse — Item Types

Base: `/api/warehouse/item-types`

| Method | Path | Success | Failure |
|---|---|---|---|
| GET | `/?categoryId=` (required) | 200, `List<ItemTypeResponse>` | 400 VALIDATION_ERROR if `categoryId` missing; 404 NOT_FOUND if category doesn't exist |
| GET | `/{id}` | 200, `ItemTypeResponse` | 404 NOT_FOUND |
| POST | `/` | 200, created, "Item Type created" | 400 VALIDATION_ERROR; 404 NOT_FOUND if `categoryId` doesn't exist |
| PUT | `/{id}` | 200, updated, "Item Type updated" | 404 NOT_FOUND; 400 VALIDATION_ERROR |

## 12. Reports

Base: `/api/reports`. **All 9 endpoints require at least one of**
`circleId` / `divisionId` / `subDivisionId` / `feederId` — otherwise
400 BAD_REQUEST via `MissingReportScopeException`. `dateFrom`/`dateTo`
(ISO `OffsetDateTime`) are optional filters on `survey_form.synced_at`,
except on `device-report`/`structure-report`/`conductor-report` where
date filtering is currently **not applied** (known gap — see README).

| Method | Path | Success | Failure |
|---|---|---|---|
| GET | `/pole-structure-summary` | 200, `List<ReportCountItem>` | 400 BAD_REQUEST (no scope param) |
| GET | `/conductor-summary` | 200, `List<ReportLengthItem>` | 400 BAD_REQUEST (no scope param) |
| GET | `/transformer-capacity-summary` | 200, `List<ReportCountItem>` | 400 BAD_REQUEST (no scope param) |
| GET | `/meter-summary` | 200, `MeterSummaryResponse` | 400 BAD_REQUEST (no scope param) |
| GET | `/device-report` | 200, `List<FeederDeviceReportRow>` — zero-filled per feeder × item_type, split into `dedicatedTransformers[]`/`generalDutyTransformers[]` with subtotals + `total` | 400 BAD_REQUEST (no scope param) |
| GET | `/structure-report` | 200, `List<FeederStructureReportRow>` — zero-filled per feeder × item_type | 400 BAD_REQUEST (no scope param) |
| GET | `/conductor-report` | 200, `List<FeederConductorReportRow>` — zero-filled per feeder × item_type, lengths not counts | 400 BAD_REQUEST (no scope param) |
| GET | `/meter-report?meterNo=&page=&size=` | 200, `PageResponse<MeterReportRow>` (default `page=0`, `size=20`) | 400 BAD_REQUEST (no scope param); 400 VALIDATION_ERROR on malformed `page`/`size`/date |

## 13. Auth

Base: `/api/auth`. `change-password` and `login-history` act on the
**caller's own account** — identity comes from the JWT
(`Authentication.getName()`), never a request-body user id.

| Method | Path | Success | Failure |
|---|---|---|---|
| POST | `/login` | 200, `LoginResponse` (JWT + expiry); records a successful `LoginHistory` row (IP from `X-Forwarded-For` or socket, `User-Agent`) | 400 VALIDATION_ERROR (missing username/password); 401 UNAUTHORIZED via `InvalidCredentialsException` (unknown user OR wrong password — same message for both, by design); 403 FORBIDDEN via `InactiveAccountException` (account disabled); 403 FORBIDDEN via `MobileLoginNotAllowedException` (role not permitted to log in from mobile); 429 TOO_MANY_REQUESTS via `RateLimitExceededException` (too many failed attempts) |
| POST | `/change-password` | 200, `data: null`, "Password changed"; writes `PasswordChangeAudit` + `PasswordHistory` | 401 UNAUTHORIZED (no/invalid JWT — filtered before controller); 400 VALIDATION_ERROR; 400 BAD_REQUEST via `PasswordPolicyViolationException` (new password fails policy) or `PasswordReuseException` (matches a recent password); 401 UNAUTHORIZED via `InvalidCredentialsException` if `oldPassword` is wrong |
| POST | `/forgot-password` | 200, `ForgotPasswordResponse` (always returns success-shaped response regardless of whether the username exists, to avoid enumeration); issues a `PasswordResetToken` when the user does exist | 400 VALIDATION_ERROR (missing username) |
| POST | `/reset-password` | 200, `data: null`, "Password reset" | 400 VALIDATION_ERROR; 400 BAD_REQUEST via `InvalidResetTokenException` (token invalid/expired/already used — deliberately 400 not 404); 400 BAD_REQUEST via `PasswordPolicyViolationException`/`PasswordReuseException` |
| GET | `/login-history` | 200, `List<LoginHistoryEntryResponse>` for the caller | 401 UNAUTHORIZED (no/invalid JWT) |

---

## Change Log (this pass)

- Replaced the previous draft contract (pre-dated the `auth` module and
  several report endpoints) with the actual routes, request/response
  shapes, and exception mappings read directly from
  `GlobalExceptionHandler` and each controller.
- Added the full `auth` module (5 endpoints).
- Added the `warehouse` module (categories + item types).
- Added all 9 `reports` endpoints, including the 4 feeder-row reports
  from Pass 5 (device/structure/conductor/meter) with their zero-fill
  and dedicated/general-duty behavior.
- Documented the `feeder` assign/unassign and `work-order`
  assign/transition split (Pass 3 change — creation no longer sets an
  assignee directly).
- Added a single global error-code / exception table instead of
  repeating it per module.
