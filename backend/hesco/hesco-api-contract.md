# API Contract

Base path: `/api`
All endpoints return the same envelope on success and the same error
shape on failure. Module-agnostic — every module's controller follows
this exact pattern.

## Response envelope

Every endpoint returns `ApiResponse<T>`:

```json
{
  "success": true,
  "data": { },
  "message": "Optional human-readable message, may be null"
}
```

## Error shape

Every non-2xx response returns `ApiError`, produced centrally by the
global exception handler — no controller builds its own error body.

```json
{
  "error": "BAD_REQUEST",
  "message": "Human-readable reason",
  "timestamp": "2026-07-26T07:30:00Z"
}
```

| HTTP status | `error` | Thrown when |
|---|---|---|
| 400 | `BAD_REQUEST` | A business-rule validator rejects the input (invalid code hierarchy, role/bound mismatch, invalid equipment sequence, missing rejection comment) |
| 400 | `VALIDATION_ERROR` | A `@Valid` request DTO fails field-level validation (`@NotBlank`, `@NotNull`, `@Pattern`, etc.) |
| 403 | `FORBIDDEN` | The actor is not permitted to perform the action (e.g. creator/feeder scope mismatch) |
| 404 | `NOT_FOUND` | A referenced entity (path variable or a foreign-key-style ID in the request body) does not exist |
| 409 | `CONFLICT` | The request conflicts with current state (dependent records exist, duplicate identifier, illegal state transition) |
| 500 | `INTERNAL_ERROR` | Unhandled/unexpected error |

Every module maps its own exceptions into this same table — no module
introduces a new status code or a new error shape.

## Authentication

Not yet implemented. No endpoint currently requires a token. Any field
named `*UserId` (e.g. `performedByUserId`, `actorUserId`,
`createdByUserId`, `submittedByUserId`) is a placeholder the caller
supplies directly; once an auth module exists, these are expected to
be replaced by the identity carried in the caller's session/token
rather than passed in the request body.

---

## Module: Admin Bound

Hierarchical organizational unit management: three coded levels, each
nested inside its parent, enforced by a code-prefix rule.

### List units (level 1 — top of hierarchy)

`GET /api/admin-bound/circles`

**Response** `data`: array of

| Field | Type | Notes |
|---|---|---|
| id | number | |
| code | string | 3-digit unique code |
| name | string | |
| active | boolean | |

### Get one (level 1)

`GET /api/admin-bound/circles/{id}`

**Response** `data`: object, same shape as above. `404` if not found.

### Create (level 1)

`POST /api/admin-bound/circles`

**Request body**

| Field | Type | Constraints |
|---|---|---|
| code | string | required, exactly 3 digits |
| name | string | required |

**Response** `data`: the created unit (same shape as List). `message`: `"Circle created"`.

### Update (level 1)

`PUT /api/admin-bound/circles/{id}`

**Request body**: same as Create.
**Response**: updated unit. `message`: `"Circle updated"`. `404` if not found.

### Delete (level 1)

`DELETE /api/admin-bound/circles/{id}`

**Response** `data`: `null`. `message`: `"Circle deleted"`.
**409** if any dependent record exists at any level below this unit — the
`message` in the error body names exactly which dependent counts are
non-zero.

---

### List units (level 2 — nested inside level 1)

`GET /api/admin-bound/divisions?circleId={id}`

`circleId` is optional; omitted returns all level-2 units.

**Response** `data`: array of

| Field | Type | Notes |
|---|---|---|
| id | number | |
| circleId | number | parent unit id |
| circleCode | string | parent unit code |
| code | string | 4-digit unique code, first 3 digits must equal the parent's code |
| name | string | |
| active | boolean | |

### Get one (level 2)

`GET /api/admin-bound/divisions/{id}` — same response shape. `404` if not found.

### Create (level 2)

`POST /api/admin-bound/divisions`

**Request body**

| Field | Type | Constraints |
|---|---|---|
| circleId | number | required, must reference an existing level-1 unit |
| code | string | required, exactly 4 digits |
| name | string | required |

**Response**: created unit. `message`: `"Division created"`.
**400** if the code's first 3 digits don't match the parent's code.
**404** if `circleId` doesn't resolve.

### Delete (level 2)

`DELETE /api/admin-bound/divisions/{id}`

Same semantics as level-1 delete: `409` with a named-dependent message
if any level-3 units or other dependents exist underneath it.

---

### List units (level 3 — nested inside level 2)

`GET /api/admin-bound/sub-divisions?divisionId={id}`

`divisionId` is optional; omitted returns all level-3 units.

**Response** `data`: array of

| Field | Type | Notes |
|---|---|---|
| id | number | |
| divisionId | number | parent unit id |
| divisionCode | string | parent unit code |
| code | string | 5-digit unique code, first 4 digits must equal the parent's code |
| name | string | |
| active | boolean | |

### Get one (level 3)

`GET /api/admin-bound/sub-divisions/{id}` — same response shape. `404` if not found.

### Create (level 3)

`POST /api/admin-bound/sub-divisions`

**Request body**

| Field | Type | Constraints |
|---|---|---|
| divisionId | number | required, must reference an existing level-2 unit |
| code | string | required, exactly 5 digits |
| name | string | required |

**Response**: created unit. `message`: `"Sub-Division created"`.
**400** if the code's first 4 digits don't match the parent's code.
**404** if `divisionId` doesn't resolve.

### Delete (level 3)

`DELETE /api/admin-bound/sub-divisions/{id}`

`409` with a named-dependent message if any records (users, assets,
work items) are still assigned to this unit.

---

## Module: User

### List

`GET /api/users`

**Response** `data`: array of

| Field | Type | Notes |
|---|---|---|
| id | number | |
| username | string | |
| firstName | string | |
| lastName | string | |
| contactNumber | string \| null | |
| roleCode | string | |
| roleDisplayName | string | |
| circleId | number \| null | populated only if the role's bound type is circle-level |
| divisionId | number \| null | populated only if the role's bound type is division-level |
| subDivisionId | number \| null | populated only if the role's bound type is sub-division-level |
| imei | string \| null | required by certain roles, see below |
| active | boolean | |

### Get one

`GET /api/users/{id}` — same shape. `404` if not found.

### Create

`POST /api/users`

**Request body**

| Field | Type | Constraints |
|---|---|---|
| username | string | required |
| password | string | required, hashed server-side before storage |
| firstName | string | required |
| lastName | string | required |
| contactNumber | string | optional |
| roleId | number | required, must reference an existing role |
| circleId | number | required only if the role's bound type is circle-level; must otherwise be omitted |
| divisionId | number | required only if the role's bound type is division-level; must otherwise be omitted |
| subDivisionId | number | required only if the role's bound type is sub-division-level; must otherwise be omitted |
| imei | string | required for roles flagged as mobile-primary; ignored otherwise |

**Response**: created user. `message`: `"User created"`.
**400** if exactly one matching bound field isn't set for the role's
bound type, if a bound field is set for a role that doesn't accept one,
or if IMEI is missing for a role that requires it.
**404** if `roleId` or a referenced bound id doesn't resolve.

### Update

`PUT /api/users/{id}`

**Request body**: same as Create, minus `username`/`password` semantics
being create-only in intent (the current implementation accepts the
same body shape; username/password fields are not re-validated for
uniqueness on update).
**Response**: updated user. `message`: `"User updated"`.
Same 400/404 conditions as Create.

---

## Module: Feeder

### List

`GET /api/feeders`

**Response** `data`: array of

| Field | Type | Notes |
|---|---|---|
| id | number | |
| code | string | |
| name | string | |
| gridStationId | number \| null | |
| gridStationCode | string \| null | |
| subDivisionId | number \| null | null when unassigned |
| subDivisionCode | string \| null | null when unassigned |
| active | boolean | |

### Get one

`GET /api/feeders/{id}` — same shape. `404` if not found.

### Create

`POST /api/feeders`

**Request body**

| Field | Type | Constraints |
|---|---|---|
| code | string | required |
| name | string | required |
| gridStationId | number | optional |

**Response**: created feeder (unassigned by default). `message`: `"Feeder created"`.
**404** if `gridStationId` is supplied but doesn't resolve.

### Assign

`POST /api/feeders/{id}/assign`

**Request body**

| Field | Type | Constraints |
|---|---|---|
| subDivisionId | number | required, must reference an existing unit |
| performedByUserId | number | required, must reference an existing user |

**Response**: the feeder with its updated assignment. `message`: `"Feeder assigned"`.
Writes an audit-log entry in the same transaction as the assignment.
**404** if the feeder, the unit, or the user doesn't resolve.

### Unassign

`POST /api/feeders/{id}/unassign`

**Request body**

| Field | Type | Constraints |
|---|---|---|
| performedByUserId | number | required, must reference an existing user |

**Response**: the feeder, now unassigned. `message`: `"Feeder unassigned"`.
Writes an audit-log entry in the same transaction as the unassignment.
**404** if the feeder or the user doesn't resolve.

---

## Module: Grid Station

### List

`GET /api/grid-stations`

**Response** `data`: array of

| Field | Type | Notes |
|---|---|---|
| id | number | |
| code | string | |
| name | string | |
| latitude | number \| null | |
| longitude | number \| null | |
| active | boolean | |

### Get one

`GET /api/grid-stations/{id}` — same shape. `404` if not found.

### Create

`POST /api/grid-stations`

**Request body**

| Field | Type | Constraints |
|---|---|---|
| code | string | required |
| name | string | required |
| latitude | number | optional |
| longitude | number | optional |

**Response**: created station. `message`: `"Grid Station created"`.

### Update

`PUT /api/grid-stations/{id}`

**Request body**: same as Create.
**Response**: updated station. `message`: `"Grid Station updated"`. `404` if not found.

### Delete

`DELETE /api/grid-stations/{id}`

**Response** `data`: `null`. `message`: `"Grid Station deleted"`. `404` if not found.

---

### Sub-entity: Power Transformer

Belongs to a grid station.

### List

`GET /api/power-transformers?gridStationId={id}`

`gridStationId` is optional; omitted returns all transformers.

**Response** `data`: array of

| Field | Type | Notes |
|---|---|---|
| id | number | |
| gridStationId | number | |
| transformerName | string | |
| cableSize | string \| null | e.g. cable size descriptor |
| ctRatio | string \| null | e.g. current-transformer ratio |
| capacityKva | number \| null | capacity, decimal |

### Create

`POST /api/power-transformers`

**Request body**

| Field | Type | Constraints |
|---|---|---|
| gridStationId | number | required, must reference an existing station |
| transformerName | string | required |
| cableSize | string | optional |
| ctRatio | string | optional |
| capacityKva | number | optional, decimal |

**Response**: created transformer. `message`: `"Power Transformer created"`.
**404** if `gridStationId` doesn't resolve.

### Delete

`DELETE /api/power-transformers/{id}`

**Response** `data`: `null`. `message`: `"Power Transformer deleted"`. `404` if not found.

---

## Module: Survey

Field submissions synced from a mobile survey app against a parent
work item.

### List by work item

`GET /api/survey-forms?workOrderId={id}`

`workOrderId` is required.

**Response** `data`: array of, ordered by submission order

| Field | Type | Notes |
|---|---|---|
| id | number | |
| workOrderId | number | |
| sePoint | string | one of the defined point-type codes (start/end point) |
| gpsNumber | string | unique per submission |
| equipmentTypeCode | string | |
| lineLengthMeters | number \| null | decimal |
| latitude | number \| null | |
| longitude | number \| null | |
| remarks | string \| null | |
| syncedAt | string (ISO 8601 timestamp) | |

### Sync (submit)

`POST /api/survey-forms/sync`

**Request body**

| Field | Type | Constraints |
|---|---|---|
| workOrderId | number | required, must reference an existing work item |
| sePoint | string | required, must be a valid point-type code |
| equipmentTypeCode | string | required, must reference an existing equipment type |
| gpsNumber | string | required, must be unique — collision is rejected, not overwritten |
| lineLengthMeters | number | optional, decimal |
| submittedByUserId | number | required, must reference an existing user |
| latitude | number | optional |
| longitude | number | optional |
| remarks | string | optional |

**Response**: the synced submission. `message`: `"Survey form synced"`.
**400** if the submitted equipment type is not a legal continuation of
the previous submission's end-point equipment for this work item, or is
not a legal start/end type at all.
**404** if `workOrderId`, `equipmentTypeCode`, or `submittedByUserId`
doesn't resolve.
**409** if `gpsNumber` already exists (sync-time collision — caller
should regenerate and resubmit rather than retry the same value).

---

## Module: Work Item (approval workflow)

A unit of work against a feeder, moving through a fixed multi-tier
approval chain.

### List

`GET /api/work-orders`

**Response** `data`: array of

| Field | Type | Notes |
|---|---|---|
| id | number | |
| feederId | number | |
| feederCode | string | |
| woType | string | one of the defined work-item type codes |
| statusCode | string | current status |
| statusLabel | string | human-readable status |
| createdByUserId | number | |
| assignedToUserId | number \| null | |
| locationLat | number \| null | |
| locationLng | number \| null | |
| createdAt | string (ISO 8601 timestamp) | |

### Get one

`GET /api/work-orders/{id}` — same shape. `404` if not found.

### Create

`POST /api/work-orders`

**Request body**

| Field | Type | Constraints |
|---|---|---|
| feederId | number | required, must reference an existing feeder |
| woType | string | required, must be a valid work-item type code |
| createdByUserId | number | required, must reference an existing user |
| locationLat | number | optional |
| locationLng | number | optional |

**Response**: created work item, in its initial status. `message`: `"Work Order created"`.
**403** if the creator's assigned unit does not match the feeder's
assigned unit.
**404** if `feederId` or `createdByUserId` doesn't resolve.

### Assign

`POST /api/work-orders/{id}/assign`

Hands a `CREATED` work order to a Surveyor. This is the only way a work
item's `assignedToUserId` is ever set — `create()` never sets it directly
(see Known Issues fix #2). Internally runs the `CREATED -> ASSIGN ->
ASSIGNED` transition through the same state machine as every other
action, so the status and the assignee can never end up out of sync.

**Request body**

| Field | Type | Constraints |
|---|---|---|
| surveyorUserId | number | required, must reference an existing user (the Surveyor being assigned) |
| actorUserId | number | required, must reference an existing user (the Creator performing the assignment; must hold the Creator role for the transition to be legal) |

**Response**: the work item, now `ASSIGNED` with `assignedToUserId` set. `message`: `"Work Order assigned"`.
**404** if `surveyorUserId` or `actorUserId` doesn't resolve.
**409** if the work item isn't currently `CREATED`, or the actor's role can't perform `ASSIGN`.

### Transition

`POST /api/work-orders/{id}/transition`

Every status change — including approve, reject, and any other step in
the chain — goes through this single endpoint as a named action rather
than a direct status update.

**Request body**

| Field | Type | Constraints |
|---|---|---|
| actionCode | string | required, must be a legal action for the item's current status and the actor's role |
| actorUserId | number | required, must reference an existing user |
| comment | string | required only for actions flagged as requiring a comment (e.g. rejection); optional otherwise |

**Response**: the work item in its new status. `message`: `"Work Order transitioned"`.
**400** if a required comment is missing.
**404** if `actorUserId` doesn't resolve.
**409** if there is no legal transition for the given action, current
status, and actor's role.

---

## Cross-module conventions

- All numeric IDs are server-generated; none are client-supplied.
- All `*Id` request fields referencing another module's records are
  resolved server-side and return `404` if they don't exist — no
  module trusts a foreign key without checking it first.
- All timestamps are ISO 8601 with offset.
- List endpoints that support an optional parent-scoping query
  parameter (e.g. `?circleId=`, `?gridStationId=`, `?workOrderId=`)
  return every record when the parameter is omitted, except where the
  parameter is explicitly marked required above.

---

## Module: Warehouse (reference data)

Configurable lookup lists (SRS §3.5) — transformer capacity buckets,
pole/structure types, HT/LT conductor types, etc. — organized as a
category (`item_category`) containing ordered values (`item_type`).
Backs both the mobile survey form's dropdowns and reports-service's
fixed enumerated columns (SRS §3.15.2), rather than either hardcoding
the SRS's lists as Java enums.

### List categories

`GET /api/warehouse/categories`

**Response** `data`: array of

| Field | Type | Notes |
|---|---|---|
| id | number | |
| code | string | e.g. `TRANSFORMER_CAPACITY`, `HT_CONDUCTOR` |
| name | string | |
| active | boolean | |

### Get one category

`GET /api/warehouse/categories/{id}` — same shape. `404` if not found.

### Create category

`POST /api/warehouse/categories`

**Request body**: `code` (string, required), `name` (string, required).
**Response**: created category. `message`: `"Item Category created"`.

### List item types in a category

`GET /api/warehouse/item-types?categoryId={categoryId}`

**Response** `data`: array of

| Field | Type | Notes |
|---|---|---|
| id | number | |
| categoryId | number | |
| categoryCode | string | |
| code | string | machine-safe key, e.g. `KVA_10` |
| displayLabel | string | human label, e.g. `"10 KVA"` |
| sortOrder | number | controls display/report column ordering |
| active | boolean | |

Ordered by `sortOrder` ascending.

### Get one item type

`GET /api/warehouse/item-types/{id}` — same shape. `404` if not found.

### Create item type

`POST /api/warehouse/item-types`

**Request body**

| Field | Type | Constraints |
|---|---|---|
| categoryId | number | required, must reference an existing category |
| code | string | required |
| displayLabel | string | required |
| sortOrder | number | optional, defaults to 0 |

**Response**: created item type. `message`: `"Item Type created"`. `404` if `categoryId` doesn't resolve.

### Update item type

`PUT /api/warehouse/item-types/{id}`

**Request body**: same as Create.
**Response**: updated item type. `message`: `"Item Type updated"`. `404` if not found.

---

## Module: Reports

Feeder Assets Reports (SRS §3.15). Every endpoint requires at least one
of `circleId`, `divisionId`, `subDivisionId`, `feederId` — unlike other
modules' list endpoints, an unscoped call is rejected rather than
returning a utility-wide result. `dateFrom`/`dateTo` alone do not
satisfy this requirement.

Each report returns a fixed set of rows/fields — the enumerated lists
from SRS §2.4 — not a dynamic pivot.

### Common query parameters (all four endpoints)

| Param | Type | Constraints |
|---|---|---|
| circleId | number | optional |
| divisionId | number | optional |
| subDivisionId | number | optional |
| feederId | number | optional |
| dateFrom | string (ISO 8601 timestamp) | optional, filters on survey sync time |
| dateTo | string (ISO 8601 timestamp) | optional, filters on survey sync time |

At least one of `circleId`/`divisionId`/`subDivisionId`/`feederId` is
required. **400** (`BAD_REQUEST`) if none are supplied.

### Pole structure summary

`GET /api/reports/pole-structure-summary`

**Response** `data`: array of, one row per pole structure type with at
least one surveyed pole in scope

| Field | Type | Notes |
|---|---|---|
| code | string | structure type code (from the `PRIMARY_STRUCTURE`/`SECONDARY_STRUCTURE` lookup category) |
| label | string | structure type display label |
| count | number | count of surveyed poles of this type in scope |

### Conductor summary

`GET /api/reports/conductor-summary`

**Response** `data`: array of, one row per conductor type with at least
one surveyed segment in scope

| Field | Type | Notes |
|---|---|---|
| code | string | conductor type code (from the `HT_CONDUCTOR`/`LT_CONDUCTOR` lookup category) |
| label | string | conductor type display label |
| count | number | count of surveyed segments of this type in scope |
| totalLengthMeters | number | sum of surveyed segment length, decimal |

### Transformer capacity summary

`GET /api/reports/transformer-capacity-summary`

**Response** `data`: array of, one row per capacity bucket with at
least one surveyed transformer in scope

| Field | Type | Notes |
|---|---|---|
| code | string | capacity bucket code (from the `TRANSFORMER_CAPACITY` lookup category) |
| label | string | capacity bucket display label, e.g. "100 KVA" |
| count | number | count of surveyed transformers of this capacity in scope |

### Meter summary

`GET /api/reports/meter-summary`

**Response** `data`: object — flat count, no lookup dimension (meters
aren't typed against a lookup category)

| Field | Type | Notes |
|---|---|---|
| count | number | count of surveyed meters in scope |
