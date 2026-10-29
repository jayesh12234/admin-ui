# AGENTS.md

This file provides guidance to AI agents when working with code in this repository.

## Project Overview

MOSIP Admin Portal — an Angular 8 SPA used by privileged administrative personnel to manage master data (Registration Centers, Devices, Machines, Users), run key management operations, check packet/RID status, and perform bulk uploads. It is the **authoritative interface** for post-deployment master data updates in a MOSIP deployment; changes should go through the portal (not raw DB scripts) to enforce business rule constraints.

The repo has two independently buildable modules:
- `admin-ui/` — the Angular frontend
- `uitest-admin/` — a Playwright + Cucumber UI automation suite on the shared `uitest-commons` library (Java 21, Maven)

---

## Commands

### Frontend (`admin-ui/`)

```bash
cd admin-ui
npm install           # install dependencies
ng serve              # dev server at http://localhost:4200 (auto-reloads)
ng build              # development build → dist/
ng build --prod       # production build (AOT, minified)
ng test               # unit tests via Karma
ng lint               # lint
ng e2e                # end-to-end tests via Protractor
```

### UI Test Suite (`uitest-admin/`)

```bash
cd uitest-admin
mvn test                                                # run every feature against the configured env
mvn test -DscenarioToRun="Center type CRUD"             # by exact scenario name (comma-separated)
mvn test -Dcucumber.filter.tags="not @bulkUploadCRUD"   # by tag
```

Configure the target environment in `src/test/resources/config/adminui.properties` (tracked with blank secrets — never commit real values) or via environment variables of the same names: `baseUrl`, `apiInternalEndPoint`, `keycloak-external-url`, `mosip_admin_client_secret`, `mosip_testrig_client_secret`. `mvn package` skips the live suite when `CI=true`.

---

## Architecture

### Runtime Configuration

The app does **not** use Angular `environment.ts` for the API base URL. At startup, `AppConfigService` fetches `assets/config.json` and exposes it globally. `app.constants.ts` reads `config.baseUrl` at module load time. This means API targets are set at deploy time, not build time. Key config fields: `baseUrl`, `primaryLangCode`, `locationHierarchyLevel`, `supportedLanguages`, `countryCode`, `filterValueMaxRecords`.

### Module & Routing Structure

Hash routing (`useHash: true`) with `PreloadAllModules`. The root route redirects to `/admin`, which is guarded by `LanguageGuard` (initialises i18n) and `AuthguardService` (Keycloak token check).

All feature areas are **lazy-loaded modules** under `/admin`:

| Route | Module | Roles |
|---|---|---|
| `/admin/resources` | ResourcesModule | GLOBAL_ADMIN, ZONAL_ADMIN, MASTERDATA_ADMIN |
| `/admin/masterdata` | MasterdataModule | GLOBAL_ADMIN, ZONAL_ADMIN, MASTERDATA_ADMIN |
| `/admin/bulkupload` | BulkuploadModule | all roles |
| `/admin/keymanager` | KeymanagerModule | KEY_MAKER |
| `/admin/packet-status`, `/admin/rid-status`, `/admin/lost-rid-status` | — | REGISTRATION_ADMIN |

Menu item visibility is driven by `RolesService` reading Keycloak roles from the JWT.

### HTTP & Auth

`AuthInterceptor` (registered in `CoreModule`) attaches the Keycloak bearer token to every outgoing request and handles 401/403 redirects. All API calls go through Angular's `HttpClient` — never `fetch` or XHR directly.

API paths are constructed in `app.constants.ts` as `<baseUrl> + <MASTERDATA_BASE_URL> + <entity>/search` (POST with filter/sort/pagination payloads). The search pattern is uniform across all master data entities.

### Core Services

- **`DataStorageService`** — in-memory cache for filter values, languages, and lookup data to avoid redundant API calls across components.
- **`AuditService`** — logs every significant user action with MOSIP audit event IDs (ADM-002 through ADM-085+). Every feature interaction should call this.
- **`CommonService`** — shared utility calls (language list, zone tree, etc.).
- **`CanDeactivateGuard`** — prevents navigation away from forms with unsaved changes.

### MOSIP Domain Concepts (relevant to code decisions)

- **Zone hierarchy**: Admins are assigned to a zone; `ZONAL_ADMIN` can only see/edit resources within their zone. Zone filtering is applied server-side via the masterdata APIs.
- **Master data search API**: All list/search pages POST to `.../search` with a `{ filters: [], sort: [], pagination: {} }` body — not GET with query params.
- **Language support**: All master data entities have language-specific fields. The UI sends `langCode` in payloads. Supported: `eng`, `ara`, `fra`, `tam`, `kan`, `hin`. i18n strings live in `admin-ui/src/assets/i18n/<lang>.json`.
- **Deactivation vs deletion**: Master data records are deactivated (`isActive: false`), not deleted. UI should reflect this.

### UI Test Suite Structure

15 Cucumber feature files (`src/test/resources/features/`), one per master-data entity/resource plus bulk upload, run 4 at a time through `AdminTestRunner` (JUnit5 Platform). Step definitions in `steps/` are generic (`MasterDataSteps` covers every CRUD page) and drive Playwright page objects in `pages/`; feature files use `${data}`-style placeholders for per-run test data. `AdminHooks` creates a per-run Keycloak admin user, maps it to a zone, and after the run deletes the master data it created. Browser lifecycle, Extent reporting (`test-output/`) and `Known_Issues.txt` handling come from `uitest-commons`. See `uitest-admin/README.md`.

---

## CI/CD

`.github/workflows/push-trigger.yml` triggers on push to `develop`, `release-*`, `master`, `1.*`, and on PRs. Pipeline jobs in order:

1. `build-admin-ui` — npm build
2. `build-docker-admin-ui` — Docker image
3. `sonar-analysis` — SonarCloud (skipped on PRs)
4. `build-maven-uitest-admin` → `build-uitest-admin-local` → `build-docker-uitest-admin`
5. `sonar-analysis-uitest-admin`

Helm chart publishing is triggered by changes under `helm/**` via `chart-lint-publish.yml`.
