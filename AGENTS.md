# AGENTS.md

This file provides guidance to AI coding agents working with this repository.

## Project Overview

**Employment Tribunals CCD Callbacks Service** (also referred to as `et-cos` — Employment Tribunals Case Orchestration Service) is a Spring Boot 3.5.14 / Java 21 application that handles all CCD (Case Configuration Data) callback requests for Employment Tribunal cases in the UK. It serves England/Wales and Scotland jurisdictions with separate CCD definitions and integrates with HMCTS common platform components.

The bootJar produces `et-cos.jar`.

## Technology Stack

- **Java 21** (required — breaking change from Java 17)
- **Spring Boot 3.5.14**, Spring Security 6.x, Spring Framework 6.2.19
- **PostgreSQL** with Flyway migrations (auto-applied on startup)
- **Feign** HTTP clients for external service communication
- **Docmosis Tornado** for document generation
- **Lombok** (requires IDE plugin + annotation processing)
- **Gradle** wrapper (do not install Gradle separately)

## Build & Test Commands

### Build
```bash
./gradlew build           # Full build including tests and checks
./gradlew check           # All checks: Checkstyle, PMD, unit tests, integration tests
```

### Tests
```bash
# Unit tests (excludes ComponentTest classes)
./gradlew test

# Run a single test class
./gradlew test --tests "uk.gov.hmcts.ethos.replacement.docmosis.service.ClassName"

# Run a single test method
./gradlew test --tests "uk.gov.hmcts.ethos.replacement.docmosis.service.ClassName.methodName"

# Integration tests (uses Testcontainers + PostgreSQL)
./gradlew integration

# Functional / API tests (requires AAT environment + F5 VPN)
./gradlew functional

# Contract (Pact) tests
./gradlew runContractTests

# UI functional tests (requires Node 18+)
yarn test:fullfunctional
yarn test:local --grep @<yourtag>
```

### Code Quality
```bash
./gradlew dependencyCheckAggregate    # OWASP dependency vulnerability scan
```

Code quality is **enforced on every build**: PMD and Checkstyle failures will break the build. Fix them before committing.

## Running the Application

### Recommended: RSE CFT Lib
```bash
az acr login --name hmctsprivate && az acr login --name hmctsprod && az acr login --name hmctspublic
./bin/pull-cftlib-images.sh
source ./bin/set_env.sh
./gradlew bootWithCCD
```

| Endpoint | URL |
|----------|-----|
| API | http://localhost:8081 |
| ExUI | http://localhost:3000 |
| Swagger | http://localhost:8081/swagger-ui.html |

Default password for all users: `password`. See `docs/cftlib.md` for the full user list.

### Scheduled / One-off Tasks
```bash
# Via environment variables
SERVER_PORT=4551 TASK_NAME=BatchReconfigurationTask SPRING_PROFILES_ACTIVE=cftlib \
  CRON_RECONFIGURATION_CASE_IDS=1756969501994958 ./gradlew bootRun

# Via shell scripts (where available)
./bin/tasks/BatchReconfigurationTask.sh 1756969501994958
```

Always set `SERVER_PORT` to an unused port when running tasks locally.

### CCD Definitions
Before importing, build the `cftlib` xlsx files first. See [CCD Definitions Structure and Build Pipeline](#ccd-definitions-structure-and-build-pipeline) for the full workflow.

## Architecture & Package Structure

The `et-shared` module is an internal subproject (`:et-shared`) providing shared models and utilities and is a compile dependency of the main application.

All production code lives under `uk.gov.hmcts.ethos.replacement.docmosis`:

| Package | Purpose |
|---------|---------|
| `controllers/` | REST endpoints for CCD callbacks (POST, `APPLICATION_JSON`) |
| `service/` | Business logic layer |
| `domain/` | Domain models, JPA entities, reference data |
| `helpers/` | Utility / transformation classes |
| `client/` | Feign clients for external services |
| `config/` | Spring configuration (security, database, Feign) |
| `tasks/` | Scheduled and command-line tasks |
| `wa/` | Work Allocation integration (includes `BatchReconfigurationTask`) |
| `idam/` | Identity and Access Management integration |
| `rdprofessional/` | Reference Data Professional API integration |
| `servicebus/` | Postgres queuing which replaced the Azure Service Bus |
| `reports/` | Report generation by report type |
| `constants/` | Shared string/code constants |
| `exceptions/` | Custom exception types |
| `utils/` | Low-level utility classes |

### CCD Callback Pattern

Every controller exposes three lifecycle endpoints that CCD calls in order:

- `POST /aboutToStart` — called when a case event starts
- `POST /aboutToSubmit` — called before the event is submitted
- `POST /submitted` — called after the event is submitted

All endpoints consume and produce `application/json` in CCD's format.

### Jurisdiction Separation

The system handles three CCD definition sets, all consolidated under `ccd-definitions/` (a Node.js/Yarn workspace):

| Jurisdiction | Directory | CCD Case Type IDs |
|---|---|---|
| England & Wales | `jurisdictions/england-wales/` | `ET_EnglandWales`, `ET_EnglandWales_Multiple`, `ET_EnglandWales_Listings` |
| Scotland | `jurisdictions/scotland/` | `ET_Scotland`, `ET_Scotland_Multiple`, `ET_Scotland_Listings` |
| Admin | `jurisdictions/admin/` | (shared admin/reporting case types) |

All three share the same CCD Jurisdiction ID: `EMPLOYMENT`.

The Scotland definition differs from England & Wales primarily by: the presence of `CaseEvent-ECC.json` (Employer Contract Claim events are Scotland-only) and jurisdiction-specific `Scotland Scrubbed` / `EnglandWales Scrubbed` data. The callback service code handles jurisdiction branching at runtime using the `caseTypeId` on the request (e.g. `ET_EnglandWales` vs `ET_Scotland`).

### CCD Definitions Structure and Build Pipeline

#### Source of truth: JSON files

Each jurisdiction's definitions live as JSON under `jurisdictions/<name>/json/`. These files are version-controlled and are the authoritative source. The Excel (`.xlsx`) files are **generated artifacts** — never edit `.xlsx` directly.

Each JSON directory mirrors the CCD spreadsheet tab structure:

```
json/
├── CaseField/          # Field definitions (split into multiple files by feature)
├── CaseEvent/          # Event definitions (split by feature)
├── CaseEventToFields/  # Event→field mappings
├── AuthorisationCaseField/   # Role-based field permissions
├── AuthorisationCaseEvent/   # Role-based event permissions
├── CaseType/           # Case type declarations
├── State/              # Case states
├── ComplexTypes/       # Complex/nested field type definitions
├── <Jurisdiction> Scrubbed/  # Fields whose values CCD nullifies on state transitions
└── ... (other CCD tabs)
```

The **Admin jurisdiction** (`jurisdictions/admin/`) is a flat structure — all definitions live directly as single files in `json/` with no sub-directories. Changes there affect all ET jurisdictions.

Within each tab directory, definitions are **split into multiple JSON files by feature** to keep changes reviewable. Files with no environment suffix (e.g. `CaseField.json`) are included in **all** builds. For example:
- `CaseField/CaseField.json` — core fields
- `CaseField/CaseField-Bundles.json` — bundle-related fields
- `CaseField/CaseField-ET1Repped.json` — ET1 Repped fields
- `CaseField/CaseField-HMC-nonprod.json` — HMC fields (non-prod only)

#### Nonprod vs Prod file variants

Files suffixed `-nonprod.json` are included only in non-production builds (local, demo, AAT, preview, cftlib). Files suffixed `-prod.json` are included only in production builds. This is enforced via exclude patterns in `ccd-definitions/configs/build.config.js`:

- All non-prod environments exclude `*-prod.json`
- The `prod` environment excludes `*-nonprod.json`

Use `-nonprod.json` files to add experimental events/fields that should not yet reach production.

#### Building Excel definitions

The `json2xlsx` processor (in `ccd-definitions/tools/ccd-definition-processor/`) converts JSON to Excel. Generated `.xlsx` files go into `ccd-definitions/dist/<env>/`.

```bash
# From ccd-definitions/ directory:
yarn generate-excel:local      # All three jurisdictions, local (nonprod) config
yarn generate-excel:cftlib     # For use with bootWithCCD / RSE CFT Lib
yarn generate-excel:aat        # AAT nonprod
yarn generate-excel:prod       # Production (excludes *-nonprod.json files)

# Single jurisdiction:
node tools/build-package.js england-wales --env=local
node tools/build-package.js scotland --env=cftlib
node tools/build-package.js admin --env=aat
```

The build reads a per-jurisdiction `data/ccd-template.xlsx` as the base template and substitutes `${CCD_DEF_*}` variables from the environment. The key variable is `CCD_DEF_URL` (used in callback URL fields) which resolves to the service base URL for that environment.

#### Converting Excel back to JSON

If definitions are edited via Excel (e.g. when using the CCD Definition Importer UI to export), convert back with:

```bash
yarn generate-json     # Runs xlsx2json for the admin jurisdiction by default
# Or per-jurisdiction:
./bin/xlsx2json -i jurisdictions/england-wales/xlsx/et-englandwales-ccd-config-base.xlsx -D jurisdictions/england-wales/json
```

After converting back, `git diff` the JSON to review what changed before committing.

#### Importing into a local CCD instance

The `bin/import-ccd-definition.sh` script imports the `cftlib` variant of each xlsx into a running local CCD (via `bootWithCCD`). It requires `CALLBACKS_PROJECT_PATH` to be set:

```bash
export CALLBACKS_PROJECT_PATH=/path/to/et-ccd-callbacks
./bin/import-ccd-definition.sh e    # England & Wales (et-englandwales-ccd-config-cftlib.xlsx)
./bin/import-ccd-definition.sh s    # Scotland (et-scotland-ccd-config-cftlib.xlsx)
./bin/import-ccd-definition.sh a    # Admin
./bin/import-ccd-definition.sh all  # All three in sequence
```

You must first build the `cftlib` xlsx files (`yarn generate-excel:cftlib`) before importing.

#### Adding or modifying case fields / events

When adding a new field or event:
1. Add to the appropriate JSON file(s) in both `jurisdictions/england-wales/json/` and `jurisdictions/scotland/json/` unless it is jurisdiction-specific.
2. Add corresponding `AuthorisationCaseField` / `AuthorisationCaseEvent` entries for all required roles.
3. If the feature is not yet production-ready, use a `-nonprod.json` suffixed file.
4. Rebuild and re-import: `yarn generate-excel:cftlib && ./bin/import-ccd-definition.sh all`.
5. The Admin jurisdiction (`jurisdictions/admin/`) uses a flat single-file structure (no sub-directories). Edit the single JSON file for each CCD tab directly.

### Multi-Source Set Structure

| Source set | Location | Purpose |
|------------|----------|---------|
| `test` | `src/test/java` | Unit tests |
| `integrationTest` | `src/test/integration/java` | Integration tests (Testcontainers) |
| `functional` / `apiTest` | `src/test/functional/java`, `src/test/apiTest/java` | Functional / API tests (Serenity BDD) |
| `contractTest` | `src/test/contractTest/java` | Pact contract tests |
| `cftlib` | (cftlib source set) | CFT Lib specific code |

## Testing Guidelines

- **Framework**: JUnit 5 (Jupiter) + Mockito 5.x
- **Test naming**: `<ClassName>Test.java`
- **Integration tests**: Testcontainers for PostgreSQL; H2 used for unit tests
- **Functional tests**: Serenity BDD
- **ByteBuddy 1.17.7+** is required for Mockito to work correctly with Java 21

## Important Configuration Files

| File | Purpose |
|------|---------|
| `build.gradle` | Build config, dependencies, source sets, tasks |
| `src/main/resources/application.yaml` | Base application config |
| `src/main/resources/application-cftlib.yaml` | CFT Lib profile |
| `src/main/resources/application-dev.yaml` | Dev profile |
| `lombok.config` | Lombok settings |
| `config/checkstyle/` | Checkstyle rules |
| `config/pmd/` | PMD rules (separate ruleset for tests: `rulesetTest.xml`) |
| `config/owasp/suppressions.xml` | OWASP suppression rules |

## External Service Dependencies

| Service | Role |
|---------|------|
| CCD Data Store API | Core case data service |
| IDAM | Identity and Access Management (RSE IdAM Simulator locally) |
| Docmosis Tornado | Document generation (`TORNADO_ACCESS_KEY`, `TORNADO_URL` required) |
| DM Store | Document storage |
| Case Document AM API | Document access management |
| RD Professional API | Reference data for professional users |
| XUI | Expert UI for case workers |
| Work Allocation | Task management (optional, requires extra setup) |
| GOV.UK Notify | Email/SMS notifications |

## Important Notes for Agents

- **Java 21 is required** — do not suggest or use Java 17 or earlier.
- All **timestamps use Europe/London timezone** (set in `DocmosisApplication.init()`).
- PMD and Checkstyle are **enforced on build** — always run `./gradlew check` after making code changes.
- The application identifier in service-to-service auth is `et_cos`.
- The bootJar is named `et-cos.jar`, not the default Spring Boot name.
- Do **not** commit changes unless explicitly asked.
- Database changes must be implemented as **Flyway migrations** under `src/main/resources/db`.
- New Feign clients should be added under `client/` and configured in `config/`.
- New controllers must follow the CCD callback pattern (`/aboutToStart`, `/aboutToSubmit`, `/submitted`).
- Sonar exclusions are defined in `build.gradle` — keep them up to date when adding new config/model packages.
