# Work Allocation with CFTLib (Local Development)

Work Allocation runs entirely within `et-ccd-callbacks` when the `ET_WORK_ALLOCATION`
environment variable is set to `true`.

## Architecture

```
[bootWithCCD (ET_WORK_ALLOCATION=true)]
  ├── CCD Data Store (port 4452) — writes to et_cos.ccd.message_queue_candidates on Publish events
  ├── wa-task-management-api (port 8087) — evaluates DMN, creates tasks in cft_task_db
  ├── Role Assignment Service (port 4096)
  ├── IDAM Simulator (port 5062)
  └── In-JVM WA bridge (MessageQueueCandidateTask)
        └── polls et_cos.ccd.message_queue_candidates → et_cos.wa_case_event_messages

[wa-docker-compose.yml — loaded automatically when ET_WORK_ALLOCATION=true]
  ├── camunda (port 9090) — BPMN/DMN engine
  ├── wa-workflow-api (port 9192) — triggers Camunda on case events
  ├── wa-case-event-handler (port 8088) — reads wa_case_event_messages → workflow-api
  └── wa-task-monitor (port 9194) — initiates/reconfigures/terminates tasks
```

ET uses the *decentralised data store* pattern (`decentralised = true` in `build.gradle`).
CCD stores ET case data in the `et_cos` database (not `datastore`), so all WA-related tables
live in `et_cos` too.

## Starting with Work Allocation

Before starting, authenticate with HMCTS ACR registries and pull the required Docker images:
```bash
az acr login --name hmctsprivate && az acr login --name hmctsprod && az acr login --name hmctspublic
./bin/pull-cftlib-images.sh
```

```bash
./gradlew bootWithCcdAndWa
```

Or equivalently:

```bash
ET_WORK_ALLOCATION=true ./gradlew bootWithCCD
```

Either approach:
- Activates the `et.work-allocation.enabled` Spring property, enabling all WA scheduled tasks
- Loads `src/cftlib/resources/compose/wa-docker-compose.yml` alongside the standard CFTLib containers
- Applies the Flyway dev migration `V003.3_WaCaseEventMessages.sql` which creates the
  `wa_case_event_messages` table in `et_cos` automatically on first startup

Running `./gradlew bootWithCCD` without the flag starts normally with no WA containers or tasks.

## Environment Variables

The following environment variables can be configured when running CFTLib with Work Allocation:

### Core & Integration Variables

| Variable | Default Value | Purpose |
|---|---|---|
| `ET_WORK_ALLOCATION` | `false` | Set to `true` (or run `./gradlew bootWithCcdAndWa`) to enable Work Allocation mode. |
| `WA_LAUNCH_DARKLY_SDK_KEY` | `sdk-key` | LaunchDarkly SDK key for Work Allocation services (`wa-workflow-api`, `wa-case-event-handler`). Defaults to `sdk-key` for offline development. |
| `WA_SYSTEM_USERNAME` | `wa-system-user@fake.hmcts.net` | Internal WA system user used by `wa-task-management-api` for CCD calls. |
| `WA_SYSTEM_PASSWORD` | `password` | Password for the WA system user. |
| `CAMUNDA_URL` | `http://localhost:9090/engine-rest` | Camunda engine REST API endpoint (must include `/engine-rest`). |
| `TASK_MONITOR_URL` | `http://localhost:9194` | `wa-task-monitor` service URL. |
| `CASE_EVENT_HANDLER_SERVICE_URL` | `http://localhost:8088` | `wa-case-event-handler` service URL. |

### Setup & Import Script Variables

| Variable | Default Value | Purpose |
|---|---|---|
| `WA_STANDALONE_TASK_REPO_PATH` | *(none)* | Path to local `wa-standalone-task-bpmn` repository (used by `import-camunda-definitions-cftlib.sh`). |
| `WA_TASK_CONFIGURATION_REPO_PATH` | *(none)* | Path to local `et-wa-task-configuration` repository (used by `import-camunda-definitions-cftlib.sh`). |

### Scheduled Task Intervals & S2S Configuration

| Variable | Default Value | Purpose |
|---|---|---|
| `S2S_SECRET_TASK_MONITOR` | `AAAAAAAAAAAAAAAA` | S2S secret for WA scheduled task monitor calls. |
| `WA_MESSAGE_QUEUE_CANDIDATE_TASK_INTERVAL` | `5000` | Polling interval (ms) for the message queue candidate task. |
| `WA_TASK_MONITOR_INITIATION_TASK_INTERVAL` | `5000` | Polling interval (ms) for WA task initiation. |
| `WA_TASK_MONITOR_RECONFIGURATION_TASK_INTERVAL` | `60000` | Polling interval (ms) for WA task reconfiguration. |
| `WA_TASK_MONITOR_TERMINATION_TASK_INTERVAL` | `60000` | Polling interval (ms) for WA task termination. |
| `WA_CASE_EVENT_HANDLER_FIND_PROBLEM_MESSAGES_TASK_INTERVAL` | `60000` | Polling interval (ms) for finding problem messages. |

## One-time Setup Steps

These steps need to be run **once** after the environment is first set up, or after the
databases are wiped.

### Step 1: Build and import CCD definitions

```bash
cd ccd-definitions && yarn generate-excel:cftlib
```

### Step 2: Generate CCD definition snapshots

The CFTLib SDK reads from `build/cftlib/definition-snapshots/` to understand which case types
exist. Without this, the `MessagePublisher` logs `Case type ET_EnglandWales is not known`
and WA events are not published. Note - CFTLIB_IMPORT_CCD_DEFS_ON_BOOT needs to be set to false for this to work.

```bash
# Stop any running Gradle daemons first (they cache the old environment)
./gradlew --stop
./gradlew dumpCCDDefinitions
```
### Step 3: Import Camunda BPMN/DMN definitions

After `bootWithCCD` is fully up (with `ET_WORK_ALLOCATION=true`):

```bash
WA_STANDALONE_TASK_REPO_PATH=/path/to/wa-standalone-task-bpmn \
WA_TASK_CONFIGURATION_REPO_PATH=/path/to/et-wa-task-configuration \
./bin/wa/import-camunda-definitions-cftlib.sh
```

Required repositories:
- `wa-standalone-task-bpmn` — BPMN process definitions
- `et-wa-task-configuration` — ET-specific DMN files

Re-run this after wiping the database or restarting camunda.

---

## WA Components in this Repository

All WA logic lives in the `cftlib` source set under
`src/cftlib/java/.../cftlib/wa/` and is **not** included in the production build:

| Package | Purpose |
|---|---|
| `entity/` | JPA entities: `MessageQueueCandidate` (polls `ccd.message_queue_candidates`), `WaCaseEventMessage` |
| `repository/` | Spring Data repositories for both entities |
| `client/taskmonitor/` | Feign client for `wa-task-monitor` (port 9194) |
| `client/wacaseeventhandler/` | Feign client for `wa-case-event-handler` (port 8088) |
| `task/` | Scheduled tasks: message bridge, task monitor calls, case event handler calls |
| `config/WaConfiguration.java` | `@EnableScheduling`, `@EnableFeignClients`, WA S2S token bean |

All beans are gated by `@ConditionalOnProperty(name = "et.work-allocation.enabled", havingValue = "true")`.

The S2S token for WA calls uses a dedicated `@Bean("waAuthTokenGenerator")` backed by
`wa.s2s-auth.totp-secret` (defaults to `AAAAAAAAAAAAAAAA` locally) with microservice name
`wa_task_monitor`.

---

## Users for Testing WA in XUI

All passwords are `password`.

| User | WA Role |
|---|---|
| `et.legalops@hmcts.net` | LEGAL_OPERATIONS — sees/claims legal ops tasks (e.g. ReviewReferralLegalOps) |
| `et.caseadmin@hmcts.net` | ADMIN — sees/claims admin tasks (e.g. Et1Vetting) |
| `admin@hmcts.net` | ADMIN — task supervisor / case allocator, assigns tasks to others |
| `wa-system-user@fake.hmcts.net` | WA system user (internal, not for UI login) |

Role assignments are configured dynamically in `CftlibConfig.java` — IDAM UUIDs are resolved
at startup so the configuration works on any machine without manual intervention.

---

## Troubleshooting

| Symptom | Cause | Fix |
|---|---|---|
| `Case type ET_EnglandWales is not known` | Missing definition snapshots | Run `./gradlew dumpCCDDefinitions` |
| `404 case not found` in wa-task-management-api | Wrong WA system user | Check `WA_SYSTEM_USERNAME` is `wa-system-user@fake.hmcts.net` in `build.gradle` |
| DMN 404 (`wa-task-configuration-employment-et_englandwales`) | Camunda definitions not imported | Run `import-camunda-definitions.sh` |
| WA tasks not appearing after case events | `wa_case_event_messages` table missing | Check Flyway ran `V003.3`; restart `bootWithCCD` with `ET_WORK_ALLOCATION=true` |
| `Connection refused` on port 8489 (S2S) | WA Docker containers started before CFTLib was ready | Wait for `bootWithCCD` to show "0 remaining" before starting |
| `No Role Assignments for user` in XUI | Role assignments not applied | Check startup logs for errors in `createWaRoleAssignments` |
| 401 on `/workallocation/caseworker/getUsersByServiceName` | `cwd_system@mailinator.com` missing | Already handled in `CftlibConfig.java` |
| JSON parse error on `/api/wa-supported-jurisdiction/detail` | `SERVICES_LOCATION_REF_API_URL` pointing to AAT | Already fixed in `build.gradle` with Wiremock stub |
