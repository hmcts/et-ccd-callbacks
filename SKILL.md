---
name: et-ccd-callbacks
description: Working knowledge for the HMCTS Employment Tribunals CCD Callbacks service (et-cos) — build/test commands, local CFT Lib environment, CCD definitions workspace, code layout, conventions and gotchas. Use when building, testing, running, debugging or changing code, CCD definitions, Flyway migrations or scheduled tasks in this repository.
---

# Employment Tribunals CCD Callbacks (et-cos)

Spring Boot service that handles every CCD callback for Employment Tribunal (ET) cases in England/Wales and Scotland. Also known as **et-cos** (Employment Tribunals Case Orchestration Service) in config, S2S names and the jar (`et-cos.jar`).

## Stack (verified against `build.gradle`)

| Component | Version / note |
|---|---|
| Java | 21 (Gradle toolchain; Java 17 no longer builds) |
| Spring Boot | **3.5.x** (plugin `3.5.16`). README/old docs saying 3.3.0 are stale |
| Spring Security | 6.5.x |
| Build | Gradle wrapper (`./gradlew`), multi-project: root + `et-shared` |
| DB | PostgreSQL + Flyway |
| HTTP clients | Feign |
| Docs generation | Docmosis Tornado (`TORNADO_ACCESS_KEY`, `TORNADO_URL`; see `docs/docmosis.md`) |
| API docs | SpringDoc OpenAPI → `http://localhost:8081/swagger-ui.html` |
| Codegen | Lombok (`io.freefair.lombok`; IDE needs Lombok plugin + annotation processing) |
| Local CCD | RSE CFT Lib plugin (`com.github.hmcts.rse-cft-lib`) and `hmcts.ccd.sdk` |
| Node | `.nvmrc` = 22.18.0 (Yarn 4 / Berry) for UI tests and CCD definitions |
| Default port | 8081 |

## Repository map

```
src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/
  DocmosisApplication.java   # entrypoint; sets Europe/London TZ; runs TASK_NAME one-off tasks
  controllers/               # CCD callback endpoints (+ admin/, applications/, multiples/, notifications/)
  service/                   # business logic, named by domain
  helpers/                   # large set of static transformation helpers
  domain/                    # repository/ (JPA), referencedata/, documents/, admin/
  client/ config/ constants/ exceptions/ utils/
  idam/ rdprofessional/      # IDAM and RD Professional integrations
  reports/                   # listing / report generation
  servicebus/                # Azure Service Bus messaging
  tasks/                     # scheduled & command-line tasks
  wa/                        # Work Allocation (incl. BatchReconfigurationTask)
src/main/resources/
  application.yaml, application-cftlib.yaml, application-dev.yaml
  db/migration/              # Flyway migrations (all envs)
  db/dev/                    # dev-only Flyway data (V003.x__*)
  *.pdf                      # ET1/ET3 PDF templates (CY_ = Welsh)
src/cftlib/                  # CFT Lib bootstrap code (cftlib source set)
src/test/
  java/                      # unit tests (mirror main packages)
  integration/               # integrationTest source set (Testcontainers Postgres)
  apiTest/                   # functional API tests (Serenity), run by `functional`
  contractTest/              # Pact consumer tests
  e2e/                       # Playwright UI tests (playwright.config.ts)
src/cftlibTest/              # tests that boot CFT Lib (run in CI)
et-shared/                   # Gradle subproject: merged et-common, ecm-common, et-data-model, ecm-data-model
et-shared-test-library/      # TS library used by the Playwright tests
ccd-definitions/             # Yarn workspace with ALL CCD definitions (admin, england-wales, scotland)
bin/                         # env, import, cleanup and task scripts
docs/                        # cftlib.md, ecm-ccd-docker.md, docmosis.md, preview-environment.md, work-allocation-cftlib.md
config/checkstyle, config/pmd, config/owasp
charts/, infrastructure/, Jenkinsfile_CNP, Jenkinsfile_nightly   # deployment / CI
```

### `et-shared`
Shared model/common code lives in the `et-shared` subproject, **not** an external library. Packages kept for compatibility: `uk.gov.hmcts.et.common...`, `uk.gov.hmcts.ecm.common...`. Where ET and ECM implementations differed, the ECM one is at `uk.gov.hmcts.ecm.compat.common...`. Case data models (e.g. `CaseData`, `CCDRequest`) come from here. Check it separately: `./gradlew :et-shared:check`.

## Build, test, quality

```bash
./gradlew build                     # compile + all checks + unit tests
./gradlew check                     # checkstyle + PMD + unit tests
./gradlew test                      # unit tests (excludes *ComponentTest*)
./gradlew test --tests "uk.gov.hmcts.ethos.replacement.docmosis.service.ClassName"
./gradlew test --tests "uk.gov.hmcts.ethos.replacement.docmosis.service.ClassName.methodName"
./gradlew :et-shared:check          # shared module checks/tests
./gradlew integration               # integrationTest source set (needs Docker for Testcontainers)
./gradlew cftlibTest                # tests against a booted CFT Lib (needs ACR login)
./gradlew functional                # API tests vs AAT (F5 VPN + IDAM_API_URL, ET_COS_URL)
./gradlew smoke                     # @smokeTest tagged tests
./gradlew runContractTests          # Pact consumer tests (skip with SKIP_CONTRACT_TESTS=true)
./gradlew jacocoTestReport          # coverage (unit + integration)
./gradlew dependencyCheckAggregate  # OWASP; suppressions in config/owasp
```

Quality gates are strict: **Checkstyle `maxWarnings = 0`, PMD `maxFailures = 0`** (main uses `config/pmd/ruleset.xml`, tests use `config/pmd/rulesetTest.xml`). Run `./gradlew check` before declaring work done. Suppress PMD only with a justified `@SuppressWarnings("PMD.Rule") // reason`.

Functional tests against AAT:
```bash
export IDAM_API_URL=https://idam-api.aat.platform.hmcts.net
export ET_COS_URL=http://et-cos.service.core-compute-aat.internal
./gradlew functional
```
Locally they use `et.dev@hmcts.net` by default; some tests may fail unless you change the user in `getLocalAccessToken`.

### UI tests (Playwright)
The old `yarn test:fullfunctional` / `yarn test:local` (CodeceptJS) scripts **no longer exist** in `package.json`. Use:
```bash
yarn install
yarn test:smoke                         # chromium, @smoke
yarn test:full-functional-chromium      # everything except @accessibility
yarn test:functional-ccd-callback       # @ccd-callback-tests
yarn test:accessibility
yarn pwtest --project=chromium --grep @<tag>   # specific tag
yarn build:shared                       # rebuild et-shared-test-library
```
Environment URLs and users are in `src/test/config.js`.

## Running locally (RSE CFT Lib — recommended)

Prereqs: Java 21, Docker, Azure CLI with ACR access:
```bash
az acr login --name hmctsprivate && az acr login --name hmctsprod && az acr login --name hmctspublic
```
Common setup (both modes):
```bash
source ./bin/set_env.sh            # edit paths first; must be `source`d
./bin/pull-cftlib-images.sh        # pre-fetch IdAM sim, Postgres, ES, XUI, DM Store, Wiremock, WA images
cd ccd-definitions && yarn generate-excel:cftlib && cd ..   # build cftlib definitions
```
- API `http://localhost:8081`, ExUI `http://localhost:3000`, Swagger `/swagger-ui.html`
- All users' password: `password` (user list in `docs/cftlib.md`)
- Key env vars: `ET_COS_CFTLIB_DB_PASSWORD=postgres`, `SPRING_PROFILES_ACTIVE=cftlib`, `CALLBACKS_PROJECT_PATH` (repo path, used for definition import), `CFTLIB_IMPORT_CCD_DEFS_ON_BOOT` (set `false` to skip import), `XUI_LD_ID`, `ET_LAUNCH_DARKLY_SDK_KEY` (from et-aat Key Vault)
- Services are up when the log output stops.

### Without Work Allocation (default)
```bash
./gradlew bootWithCCD
```
- Make sure `ET_WORK_ALLOCATION` is unset or `false` (check what `set_env.sh` exports).
- No WA containers start and no WA beans or scheduled tasks load.
- Spring Boot DevTools automatic restart is enabled.

### With Work Allocation
```bash
./gradlew bootWithCcdAndWa
# or equivalently
ET_WORK_ALLOCATION=true ./gradlew bootWithCCD
```
Either one:
- sets `et.work-allocation.enabled=true`, which turns on the WA beans in `src/cftlib/java/.../cftlib/wa/` (local-only; **not** part of the production build)
- also starts `src/cftlib/resources/compose/wa-docker-compose.yml`: Camunda `:9090`, wa-workflow-api `:9192`, wa-case-event-handler `:8088`, wa-task-monitor `:9194`. wa-task-management-api `:8087` and Role Assignment `:4096` also run.
- applies the dev Flyway migration `V003.3__WaCaseEventMessages.sql` (`et_cos.wa_case_event_messages`)
- disables DevTools restart

The in-JVM bridge `MessageQueueCandidateTask` copies rows from `et_cos.ccd.message_queue_candidates` to `et_cos.wa_case_event_messages`. ET uses CCD's decentralised data store pattern, so CCD stores case data and all WA tables in `et_cos`.

**One-time WA setup** (repeat after wiping the DBs):
1. Build the definitions: `cd ccd-definitions && yarn generate-excel:cftlib`
2. Generate the definition snapshots. Without them you get `Case type ET_EnglandWales is not known` and no WA events are published. This step needs `CFTLIB_IMPORT_CCD_DEFS_ON_BOOT=false`:
   ```bash
   ./gradlew --stop            # daemons cache the old environment
   ./gradlew dumpCCDDefinitions
   ```
3. Once `bootWithCcdAndWa` is fully up, import the Camunda BPMN/DMN definitions. This needs local clones of `wa-standalone-task-bpmn` and `et-wa-task-configuration`. Re-run it after restarting Camunda.
   ```bash
   WA_STANDALONE_TASK_REPO_PATH=/path/to/wa-standalone-task-bpmn \
   WA_TASK_CONFIGURATION_REPO_PATH=/path/to/et-wa-task-configuration \
   ./bin/wa/import-camunda-definitions-cftlib.sh
   ```

WA env vars (defaults shown): `WA_LAUNCH_DARKLY_SDK_KEY=sdk-key`, `WA_SYSTEM_USERNAME=wa-system-user@fake.hmcts.net`, `WA_SYSTEM_PASSWORD=password`, `CAMUNDA_URL=http://localhost:9090/engine-rest`, `TASK_MONITOR_URL=http://localhost:9194`, `CASE_EVENT_HANDLER_SERVICE_URL=http://localhost:8088`, `S2S_SECRET_TASK_MONITOR=AAAAAAAAAAAAAAAA`. Polling intervals in ms: `WA_MESSAGE_QUEUE_CANDIDATE_TASK_INTERVAL=5000`, `WA_TASK_MONITOR_INITIATION_TASK_INTERVAL=5000`, `WA_TASK_MONITOR_RECONFIGURATION_TASK_INTERVAL=60000`, `WA_TASK_MONITOR_TERMINATION_TASK_INTERVAL=60000`, `WA_CASE_EVENT_HANDLER_FIND_PROBLEM_MESSAGES_TASK_INTERVAL=60000`.

WA test users in ExUI (password `password`):
| User | WA role |
|---|---|
| `et.legalops@hmcts.net` | LEGAL_OPERATIONS (e.g. ReviewReferralLegalOps) |
| `et.caseadmin@hmcts.net` | ADMIN (e.g. Et1Vetting) |
| `admin@hmcts.net` | ADMIN, task supervisor / case allocator |
| `wa-system-user@fake.hmcts.net` | internal system user, not for UI login |

Role assignments are created at startup in `CftlibConfig.java`. Full details are in `docs/work-allocation-cftlib.md`.

### General
- Clean-up helpers: `bin/cftlib-clean.sh`, `bin/docker-clean.sh`, `bin/drop-db.sh`, `bin/kill-residual-processes.sh`; debug with `bin/debugCFTLIB.sh`
- Alternative full-Docker environment: `docs/ecm-ccd-docker.md`

CCD case types routed to this service: `ET_EnglandWales`, `ET_Scotland`, `ET_EnglandWales_Listings`, `ET_Scotland_Listings`, `ET_EnglandWales_Multiple`, `ET_Scotland_Multiple`, `ET_Admin`.

## CCD definitions (in this repo)

Definitions were merged in from the three former repos and now live in `ccd-definitions/` (Yarn workspace). Edit **JSON** in `ccd-definitions/jurisdictions/{admin,england-wales,scotland}/json/`; XLSX files are generated and gitignored.

```bash
cd ccd-definitions
yarn setup                          # first time
yarn generate-excel:cftlib          # build for local CFT Lib (also run by Gradle task generateCftlibCcdDefinitions)
yarn generate-excel:local | :aat | :demo | :ithc | :perftest | :preview | :prod
yarn lint && yarn format:check
```
Import into a running CFT Lib (reads `$CALLBACKS_PROJECT_PATH/ccd-definitions/jurisdictions/*/xlsx/*-cftlib.xlsx`):
```bash
./bin/import-ccd-definition.sh e|s|a|all   # England/Wales | Scotland | Admin | all
```
When adding a callback, update both the Java endpoint **and** the matching definition JSON (usually both E&W and Scotland).

## Code conventions

### Controllers (CCD callback pattern)
- `@PostMapping(value = "/aboutToStart" | "/aboutToSubmit" | "/submitted" | "/<midEventName>", consumes = APPLICATION_JSON_VALUE)` with `@Operation(summary = ...)`.
- Accept `@RequestBody CCDRequest` and the `Authorization` header; return `ResponseEntity<CCDCallbackResponse>` via `getCallbackRespEntityNoErrors(caseData)` or `getCallbackRespEntityErrors(errors, caseData)`.
- Keep controllers thin; logic goes in `service/` or `helpers/`.
- Mid-event validations return errors list rather than throwing.

### Services, helpers, tests
- Constructor injection via Lombok `@RequiredArgsConstructor`; logging via `@Slf4j`.
- Tests: JUnit 5 + Mockito 5 (ByteBuddy ≥ 1.17.7 for Java 21), named `<ClassName>Test`, mirroring the main package. Controller tests use `@WebMvcTest` + `MockMvc`.
- `*ComponentTest*` classes are excluded from `./gradlew test`.

### Database (Flyway)
- Migrations in `src/main/resources/db/migration`, named `V<NNN>__Description.sql`; latest is `V023` (V019 skipped) — next is `V024`. Never edit an applied migration.
- Dev-only seed data uses `db/dev` (`V003.x__...`).
- Migrations run automatically on startup.

### Scheduled tasks
- Classes in `tasks/` (and `wa/BatchReconfigurationTask`) use `@Scheduled(cron = "${cron.<name>}")`; cron/config keys live under `cron:` in `application.yaml`.
- One-off run: `DocmosisApplication` checks `TASK_NAME`; `ScheduledTaskRunner` looks up the **bean name** and runs it, then the context closes.
```bash
SERVER_PORT=4551 TASK_NAME=BatchReconfigurationTask SPRING_PROFILES_ACTIVE=cftlib \
  CRON_RECONFIGURATION_CASE_IDS=1756969501994958 ./gradlew bootRun
./bin/tasks/BatchReconfigurationTask.sh 1756969501994958
./bin/tasks/NoticeOfChangeFieldsTask_Run.sh
```
Always set `SERVER_PORT` to a free port so it doesn't clash with the running app.

### Feature flags
Toggles live under `feature:` / `feature-flags:` in `application.yaml` and LaunchDarkly (`ET_LAUNCH_DARKLY_SDK_KEY`).

## External dependencies
CCD Data Store, CCD Definition Store, IDAM (RSE IdAM Simulator locally), S2S, Docmosis Tornado, DM Store, Case Document AM, RD Professional, ExUI, Work Allocation (optional; `docs/work-allocation-cftlib.md`), Azure Service Bus, LaunchDarkly, Pact Broker (`PACT_BROKER_FULL_URL`).

## CI / deployment
- Jenkins: `Jenkinsfile_CNP` (PR/master), `Jenkinsfile_nightly`; Helm chart in `charts/`; Terraform in `infrastructure/`.
- GitHub Actions (`.github/workflows/`): `gradle.yml` runs `:et-shared:check` and `cftlibTest`; others publish `et-shared` / the test library and post preview links.
- Preview environments: `docs/preview-environment.md`.

## Gotchas
- All timestamps use **Europe/London** (set in `DocmosisApplication.init()`).
- `README.md` and `docs/cftlib.md` contain stale versions (Spring Boot 3.3.0, old CFT Lib version) — trust `build.gradle`.
- `bin/set_env.sh` must be `source`d, not executed.
- `createManifestApplication` depends on `:et-shared:jar`; if `bootWithCCD` fails on a missing jar, run `./gradlew :et-shared:jar`.
- Untracked demo config zips under `ccd-definitions/` are not part of the build.
