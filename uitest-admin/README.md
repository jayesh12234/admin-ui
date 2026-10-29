# Admin Portal UI Automation

Playwright + Cucumber (BDD) UI automation suite for the MOSIP Admin Portal, run through JUnit5's
Platform Suite engine. It covers create → filter → edit → activate → edit → deactivate (and
decommission, for resources) for every master data entity and resource the portal manages, plus
master data bulk upload (insert, update, delete).

Built on the shared [`uitest-commons`](https://github.com/mosip/mosip-functional-tests/tree/develop/uitestrig-platform)
library (browser lifecycle, Cucumber hooks, Extent reporting, Keycloak test users), the same way
as the Resident Portal suite in [`mosip/resident-ui/ui-test`](https://github.com/mosip/resident-ui/tree/develop/ui-test).

## Setup

`src/test/resources/config/adminui.properties` ships tracked in git with blank/default values. At
minimum, fill in `baseUrl`, `apiInternalEndPoint`, `keycloak-external-url`,
`mosip_admin_client_secret` and `mosip_testrig_client_secret` for your target environment —
**never commit real values back into this file**. Consider
`git update-index --skip-worktree src/test/resources/config/adminui.properties` locally so your
filled-in values never show up in `git status` (`--no-skip-worktree` to undo).

Every key can also be supplied as an environment variable of the same name, which wins over the file.

## Run the full suite

```
mvn test
```

Surefire only picks up `AdminTestRunner`, which runs every `.feature` file under
`src/test/resources/features/` through the Cucumber JUnit5 engine, 4 scenarios in parallel
(`-Dcucumber.execution.parallel.config.fixed.parallelism=<n>` to change).

## Run a subset

By exact scenario name (case-insensitive, comma-separated):
```
mvn test -DscenarioToRun="Center type CRUD,Device CRUD"
```
or set `scenarioToRun=` in `adminui.properties`; the `-D` flag wins if both are set.

By tag — each feature carries the name of the TestNG test it replaces, so the old
`setExcludedGroups` codes map as follows:
```
mvn test -Dcucumber.filter.tags="@centerCRUD or @deviceCRUD"
mvn test -Dcucumber.filter.tags="not @bulkUploadCRUD"
```

| Old code | Tag | Feature |
|---|---|---|
| BL | `@blocklistedwordsCRUD` | `blocklisted-words.feature` |
| BU | `@bulkUploadCRUD` | `bulk-upload.feature` |
| CTR | `@centerCRUD` | `center.feature` |
| CT | `@centerTypeCRUD` | `center-type.feature` |
| DS | `@deviceSpecCRUD` | `device-specification.feature` |
| D | `@deviceCRUD` | `device.feature` |
| DT | `@deviceTypesCRUD` | `device-type.feature` |
| DOC | `@documentCategoriesCRUD` | `document-category.feature` |
| DOCT | `@documentTypesCRUD` | `document-type.feature` |
| DF | `@dynamicFieldCRUD` | `dynamic-field.feature` |
| H | `@holidaysCRUD` | `holiday.feature` |
| MS | `@machineSpecCRUD` | `machine-specification.feature` |
| M | `@machineCRUD` | `machine.feature` |
| MT | `@machineTypesCRUD` | `machine-type.feature` |
| T | `@templateCRUD` | `template.feature` |

Area tags `@masterdata`, `@resources` and `@bulkupload` select whole groups.

## Run in Docker

```
docker build -t uitest-admin .
docker run -e ADMINUI_CONFIG_PATH=/home/mosip/config/adminui.properties \
  -v /path/to/local/adminui.properties:/home/mosip/config/adminui.properties \
  uitest-admin
```

The image (Microsoft's Playwright Java base image) compiles the test sources at build time and
runs `mvn -o test` at container start. `entrypoint.sh` copies the mounted config file over the
tracked `adminui.properties`; plain environment variables work too. The pre-Playwright
`adminPortalPath` variable is still accepted when `baseUrl` is not set.

## Local portal (ng serve)

When `baseUrl` points at `localhost`, the suite skips the Keycloak login (localhost is not an
allowed redirect, KER-ACP-009) and instead stores the admin token as an `Authorization` cookie on
`masterdatabaseurl` (e.g. a local kernel-masterdata-service on `http://localhost:8086`), launches
Chromium with web security off for the cross-origin calls, and answers the portal's audit-log
calls itself, since a local admin service does not serve them.

## Configuration

Property layers (lowest → highest), loaded by `AdminConfigManager.init()`:
1. `Kernel.properties` (apitest-commons JAR — MOSIP-wide defaults)
2. `uitest.properties` (uitest-commons JAR — browser defaults)
3. `adminui.properties` (this module)
4. Environment variables

Notable keys in `adminui.properties`: `loginlang` (Keycloak login language and the
`BulkUploadFiles/<loginlang>/` folder), `preappend` (prefix of the generated `${data}` value),
`bulkUploadStatusWaitSeconds`, and `cleanupMasterData` with `db-server`/`postgres-password`
(post-run deletion of the master data rows this run created; skipped while the password is blank).

## Architecture

1. `AdminTestRunner` (`runner/`) — JUnit5 `@Suite` entry point, no logic.
2. `AdminHooks` (`hooks/`) — suite lifecycle: loads config, creates the run's Keycloak admin user
   (`<run-id>_admin-ui-auto`) and maps it to a leaf zone, generates the run's test data
   (`AdminTestData`), and after the run cleans up master data (`AdminDbCleanup`) and the user.
3. Feature files drive generic steps in `steps/` (`MasterDataSteps` for every CRUD page,
   `CenterSteps`, `BulkUploadSteps`), which drive page objects in `pages/`.

Feature files reference generated test data as `${name}` placeholders: `${data}`,
`${blocklistedWord}`, `${holidayDate}`, `${holidayDateCenter}`, `${validityDate}`, `${publicKey}`,
`${signPublicKey}`.

Scenarios that create types/specs and scenarios whose forms pick the first type/spec from a
dropdown are kept from running at the same time via Cucumber exclusive resources in
`junit-platform.properties`.

## Known issues

Add `bugId------Scenario name` lines to `src/test/resources/config/Known_Issues.txt`; matching
scenarios are skipped and reported with a link to the bug.

## Reports

Each run writes an Extent HTML report to `reportOutputDir` (default `test-output/`), named
`admin-ui-<env>-<date>-<time>-T-<total>-P-<passed>-F-<failed>-S-<skipped>-KI-<known>.html`,
with every action and its locator, a screenshot at every failing step, and the portal's own
result message after each create/update/status change. Optionally pushed to S3/MinIO when
`push-reports-to-s3=yes`.

## License
This project is licensed under the terms of [Mozilla Public License 2.0](../LICENSE).
