# GEPCO Modernization — Strangler Fig Migration

## Project Overview
This repository contains the strangler-fig modernization of the legacy GEPCO webapp from **Java 8 / Spring MVC 4.3 / Spring Security 4.2 / JSP** to **Angular + Spring Boot 3.x microservices + PostgreSQL/PostGIS**.

**Active Module:** MapViewer/GIS (Phase 0: Environment & Risk Containment)

## Repository Structure

```
gepco-modernization/
├── backend/
│   ├── gis-map-service/          (Spring Boot microservice + GeoTools 24)
│   ├── gateway-service/          (Spring Cloud Gateway)
│   └── common-lib/               (Optional shared utilities)
├── frontend/
│   └── mapviewer-ui/             (Angular + Leaflet)
├── infra/
│   ├── docker/                   (Optional: Docker Compose for fresh local DB)
│   └── scripts/                  (Database migrations & stored procedures)
├── docs/
│   ├── architecture.md           (High-level architecture)
│   ├── phase-0-checklist.md      (Phase 0 progress tracker)
│   └── api-contracts.md          (REST API specifications)
├── .env.example                  (Environment variables template)
└── README.md                      (This file)
```

## Quick Start (Phase 0)

### Prerequisites
- PostgreSQL 12+ with PostGIS enabled (**already installed and running locally**)
- Java 17+
- Maven 3.8+
- Node.js 18+ (for Angular, optional for Phase 0)

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/wajahatabbasi86/gepco-modernization.git
   cd gepco-modernization
   ```

2. **Configure environment:**
   ```bash
   cp .env.example .env
   # Edit .env with your LOCAL PostgreSQL connection details:
   # - DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
   # This project connects to an existing local PostgreSQL/PostGIS instance
   ```

3. **Confirm PostGIS is enabled:**
   ```bash
   psql -U postgres -d gepco_local -c "SELECT PostGIS_version();"
   # If this errors, enable PostGIS:
   # psql -U postgres -d gepco_local -c "CREATE EXTENSION IF NOT EXISTS postgis;"
   ```

4. **Load MapViewer-scoped stored procedures (optional, for reference):**
   ```bash
   psql -U postgres -d gepco_local -f infra/scripts/03_gis_map_service_only.sql
   ```

5. **Start GIS Map Service:**
   ```bash
   cd backend/gis-map-service
   mvn clean spring-boot:run
   # Service will start on http://localhost:8082 (or port from .env GIS_MAP_SERVICE_PORT)
   ```

6. **Start Gateway Service (in another terminal):**
   ```bash
   cd backend/gateway-service
   mvn clean spring-boot:run
   # Gateway will start on http://localhost:8081 (or port from .env GATEWAY_PORT)
   ```

7. **Test Gateway routing:**
   ```bash
   curl http://localhost:8081/api/gis/layers/health
   # Expected response: {"status": "UP", "service": "gis-map-service"}
   ```

8. **Start Angular Frontend (optional, Phase 2+):**
   ```bash
   cd frontend/mapviewer-ui
   npm install
   ng serve
   # Frontend will be available at http://localhost:4200
   ```

## Phase 0 Goals

✅ Prove ONE endpoint end-to-end: **`GET /api/gis/layers/Feeder_PERM/identify`**
- Request: `?lat=30.1234&lng=72.5678&radius=50`
- Response: GeoJSON of intersecting features from PostGIS

✅ Gateway routing for `/api/gis/**` and `/mapviewer/**`

✅ Database connectivity via GeoTools DataStore

✅ Verify against existing local PostgreSQL/PostGIS instance

## Key Decisions (Locked In)

1. **Path A:** Keep GeoTools server-side, wrap it behind a clean REST API
2. **Map Library:** Leaflet (matches legacy, fastest parity)
3. **Tile Rendering:** Raster (direct port of `GetMap`)
4. **Auth:** JWT-based (independent of legacy session)
5. **Gateway:** Spring Cloud Gateway
6. **Database:** Connect to existing local PostgreSQL instance (no containerization by default)
7. **Scope:** Dashboard, Reports, Area Planning, Work Order, Synergee are separate future modules

## Optional: Docker-Based PostgreSQL

If you **don't** have PostgreSQL installed locally, an optional Docker Compose file is available:

```bash
cd infra/docker
docker-compose -f docker-compose.optional-local-db.yml up -d
# PostgreSQL will run on localhost:5433 (non-standard port to avoid conflict)
# Update .env with: DB_PORT=5433, DB_NAME=gepco_local_docker
```

**Note:** This creates a fresh, empty database — it does NOT include the real GEPCO data.
The default workflow assumes you have an existing local PostgreSQL instance with the data already loaded.

## Next Steps (Phase 1+)

- Phase 1: Layer abstraction (generalize 3 hardcoded controllers → config-driven)
- Phase 2: Rendering & legend generation
- Phase 3: Angular frontend (reuse Figma benchmarks)
- Phase 4: Parallel-run & cutover

## Documentation

See `/docs` for detailed guides:
- `architecture.md` — System design & data flow
- `phase-0-checklist.md` — Current progress & blockers
- `api-contracts.md` — REST endpoint specifications

## Scope (What's In / What's Out)

### ✅ In Scope (gis-map-service)
- Show Feeders on Map (single + multiple feeder selection)
- Bookmark management
- Print Map
- Identify (spatial query results)
- Legend display
- Symbology/filter panel

### ❌ Out of Scope (separate modules)
- Dashboard & Work Order statistics → `dashboard-service` (Phase 5+)
- Feeder Assets Reports (Conductor/Structure/Device/Meter stats) → `reports-service` (Phase 5+)
- Area Planning → `area-planning-service` (Phase 6+)
- Work Order CRUD → `work-order-service` (Phase 7+)
- Synergee/GIS Export → `synergee-service` (Phase 8+)

## License

Internally maintained. Not for public distribution.
