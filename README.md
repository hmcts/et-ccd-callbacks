# Employment Tribunals CCD Callbacks Service

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-Wrapper-blue.svg)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE.md)

This application is responsible for handling all CCD callback requests for Employment Tribunal cases.


## CCD Definitions

This repository now includes consolidated CCD definitions for all Employment Tribunal jurisdictions in the `ccd-definitions/` directory. These definitions were merged from three separate repositories:

- **Admin configurations** (`jurisdictions/admin/`) - Administrative case management
- **England & Wales configurations** (`jurisdictions/england-wales/`) - England & Wales specific cases
- **Scotland configurations** (`jurisdictions/scotland/`) - Scotland specific cases

For detailed information about working with the CCD definitions, see [ccd-definitions/README.md](ccd-definitions/README.md).

### Key Features:
- Consolidated multi-jurisdiction definitions
- JSON to Excel conversion tools
- Environment-specific configuration management
- Automated build and deployment scripts
=======
## Supported Versions

| Component | Version | Status |
|-----------|---------|--------|
| Java | 21 | ✅ Supported |
| Spring Boot | 3.3.0 | ✅ Current |
| Spring Security | 6.x | ✅ Current |
| Node.js | 18+ | ✅ Supported |
| Gradle | Wrapper | ✅ Current |

> ⚠️ **Breaking Change**: Java 21 is now required. Java 17 and earlier versions are no longer supported.

## Prerequisites

### Java
- [JDK 21](https://www.oracle.com/java) (upgraded from JDK 17)

**Note: This application has been upgraded to Spring Boot 3.3.0 and requires Java 21.**

### CCD Common Components
The application should be run locally in an environment that includes CCD common components.

There are two options for achieving this:
- [RSE CFT lib](docs/cftlib.md)
- [ECM CCD Docker](docs/ecm-ccd-docker.md)

### Postgres Database
A local database is required. This is provided by one of the CCD common components environments.

[flyway](https://flywaydb.org/) migrations are automatically applied on startup.

### Docmosis Tornado
[Docmosis Tornado](https://www.docmosis.com/products/tornado.html) is a third-party product used by et-ccd-callbacks to
generate documents and reports.

More information about using Docmosis Tornado in a local development environment can be found [here](docs/docmosis.md).

## Building
The project uses [Gradle](https://gradle.org) as a build tool, but you don't have to install it locally since there is a
`./gradlew` wrapper script.

To build the project execute the following command:

```bash
./gradlew build
```

To get the project to build in IntelliJ IDEA, you have to:

- Install the Lombok plugin: Preferences -> Plugins
- Enable Annotation Processing: Preferences -> Build, Execution, Deployment -> Compiler -> Annotation Processors

## Running
To run the application locally, you should follow the instructions above for one of the CCD environments.

## Tasks

There are a number of tasks within the project; some make use of Spring Scheduling and run within the main application, and others are standalone which can be run from the command line.

*Note: the examples below assume you are using the RSE CFT lib environment, but the tasks can be run from any environment.*

To run the tasks from the command line, you can use the following command as an example:
```bash
SERVER_PORT=4551 TASK_NAME=BatchReconfigurationTask SPRING_PROFILES_ACTIVE=cftlib CRON_RECONFIGURATION_CASE_IDS=1756969501994958 ./gradlew bootRun
```

This will run the `BatchReconfigurationTask` on port 4551 with the cftlib profile active. 

Alternatively, some tasks can be executed if a shell script has been created for them. These can be found in the `./bin/tasks` directory.

An example of this is the `BatchReconfigurationTask` which can be run as follows:

```bash
./bin/tasks/BatchReconfigurationTask.sh 1756969501994958
```

When running a task locally, it is important to set the `SERVER_PORT` environment variable to a port that is not already in use.

## API documentation
API documentation is provided with SpringDoc OpenAPI

http://localhost:8081/swagger-ui.html

**Note:** After the Spring Boot 3 upgrade, the OpenAPI documentation is now provided by SpringDoc OpenAPI 2.5.0. The URL remains the same, but the underlying implementation has been updated.

## Developing

### Database
All database updates are applied using [flyway](https://flywaydb.org/). See src/main/resources/db

### Unit Tests
To run all unit tests:

```bash
./gradlew test
```
### Functional API Tests
To run all Functional API tests against AAT instances:
Ensure F5 VPN is on.
These three variables need to be set in your WSL:
```bash
IDAM_API_URL=https://idam-api.aat.platform.hmcts.net
ET_COS_URL=http://et-cos.service.core-compute-aat.internal
FT_SYA_URL=http://et-sya-api-aat.service.core-compute-aat.internal
```
Then run
```bash
./gradlew functional
```

To run all Functional API tests against local instances (useful for debugging purposes):
Note that some tests may fail as it uses the et.dev@hmcts.net user by default when being run locally, 
the workaround is to create a new user for test (need to replace username and password in getLocalAccessToken method).
Ensure your local environment is up and running (see instructions in ecm-ccd-docker), Callback and SYA API instances are started in separate terminals.
Then run
```bash
./gradlew functional
```

### UI Functional Tests
To run all UI functional test :
Ensure your node version is `18` or greater
proceed to `cofig.js` file change the test url to the desired environment
Add corresponding user details for those environment

### Cutover Seed Step
To seed a cutover manifest with one case per controller category covered by the existing ET E2E scenarios:
```bash
yarn test:seed-cutover
```
This writes a manifest to `functional-output/cutover/seed-manifest.json`.

For a cutover rehearsal, run the steps in this order:

1. Seed cases before migration.
2. Migrate CCD data into the ET decentralised store.
3. Enable decentralisation/routing for ET.
4. Run `yarn test:verify-cutover`.

Do not reseed after migration unless you intentionally want a new pre-cutover baseline.

Required environment variables:
```bash
ET_CCD_CASEWORKER_USER_NAME=<caseworker username>
ET_CCD_CASEWORKER_PASSWORD=<caseworker password>
IDAM_CLIENT_SECRET=<xuiwebapp client secret>
MICROSERVICE_CCD_GW=<ccd_gw s2s secret>
```

Optional environment variables:
```bash
RUNNING_ENV=aat
IDAM_URL=https://idam-api.aat.platform.hmcts.net
REDIRECT_URI=https://xui-<preview-service-fqdn>/oauth2/callback
CUTOVER_CCD_DATA_STORE_URL=https://ccd-data-store-api-<preview-service-fqdn>
CUTOVER_S2S_URL=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal
CUTOVER_PREVIEW_SERVICE_FQDN=<preview-service-fqdn>
CUTOVER_SEED_OUTPUT_FILE=/tmp/seed-manifest.json
CUTOVER_SEED_PROFILE_IDS=accepted-case-details,accepted-jurisdiction
```

For preview, either set `CUTOVER_CCD_DATA_STORE_URL` directly or set
`CUTOVER_PREVIEW_SERVICE_FQDN`/`SERVICE_FQDN`; the scripts will use
`https://ccd-data-store-api-${SERVICE_FQDN}`.

### Cutover Verify Step
After the decentralisation cutover, verify the seeded cases still read correctly and can start the expected CCD event triggers:
```bash
yarn test:verify-cutover
```
This reads `functional-output/cutover/seed-manifest.json` and writes a report to
`functional-output/cutover/verify-report.json`.
It uses the same required IDAM and S2S environment variables as the cutover seed step.

Optional environment variables:
```bash
CUTOVER_CCD_DATA_STORE_URL=https://ccd-data-store-api-<preview-service-fqdn>
CUTOVER_S2S_URL=http://rpe-service-auth-provider-aat.service.core-compute-aat.internal
CUTOVER_PREVIEW_SERVICE_FQDN=<preview-service-fqdn>
CUTOVER_VERIFY_MANIFEST_FILE=/tmp/seed-manifest.json
CUTOVER_VERIFY_OUTPUT_FILE=/tmp/verify-report.json
CUTOVER_VERIFY_PROFILE_IDS=accepted-case-details,accepted-jurisdiction
CUTOVER_VERIFY_REQUIRE_COMPLETE_SEED=false
CUTOVER_VERIFY_SKIP_EVENT_TRIGGERS=true
```

Use `CUTOVER_VERIFY_SKIP_EVENT_TRIGGERS=true` for a read/state-only check. A full cutover verification should run
without that flag after decentralisation/routing has been enabled.

Example preview smoke run:
```bash
export RUNNING_ENV=aat
export SERVICE_FQDN=et-cos-pr-1234.preview.platform.hmcts.net
export REDIRECT_URI=https://xui-${SERVICE_FQDN}/oauth2/callback
export CUTOVER_SEED_PROFILE_IDS=accepted-case-details
yarn test:seed-cutover
yarn test:verify-cutover
```

### Cutover DB Check Step
After the cases have been moved to the ET decentralised store, compare the seeded case rows in CCD and ET:
```bash
yarn test:check-cutover-db
```
This reads `functional-output/cutover/seed-manifest.json` and writes a report to
`functional-output/cutover/db-check-report.json`. It compares case state, revision, event count,
latest event details, case data hash and supplementary data hash.

Required environment variables:
```bash
CUTOVER_CCD_DB_URL=postgresql://<user>@<host>:5432/<ccd-data-store-db>?sslmode=require
CUTOVER_CCD_DB_PASSWORD=<ccd data store db password>
CUTOVER_ET_DB_URL=postgresql://<user>@<host>:5432/<et-cos-db>?sslmode=require
CUTOVER_ET_DB_PASSWORD=<et cos db password>
```

Optional environment variables:
```bash
CUTOVER_DB_CHECK_MANIFEST_FILE=/tmp/seed-manifest.json
CUTOVER_DB_CHECK_OUTPUT_FILE=/tmp/db-check-report.json
CUTOVER_DB_CHECK_PROFILE_IDS=accepted-case-details,accepted-jurisdiction
```

For preview, `CUTOVER_PREVIEW_PR_ID`, `CHANGE_ID` or `PR_ID` can be used to infer
`pr-<id>-data-store` for CCD and `pr-<id>-et_cos` for ET on
`et-preview.postgres.database.azure.com`. Set the `CUTOVER_*_DB_URL` values directly if
the preview deployment is using different database names.

### Cutover Search and Indexing Check Step
After cutover, verify the seeded cases are visible through CCD search and, optionally, direct Elasticsearch indexes:
```bash
yarn test:verify-cutover-search
```
This reads `functional-output/cutover/seed-manifest.json` and writes a report to
`functional-output/cutover/search-report.json`. It always checks CCD search by CCD reference and
ET reference where available. Direct index checks are only attempted when `CUTOVER_ELASTICSEARCH_URL`
is set.

Optional environment variables:
```bash
CUTOVER_SEARCH_MANIFEST_FILE=/tmp/seed-manifest.json
CUTOVER_SEARCH_OUTPUT_FILE=/tmp/search-report.json
CUTOVER_SEARCH_PROFILE_IDS=accepted-case-details,accepted-jurisdiction
CUTOVER_SEARCH_ATTEMPTS=20
CUTOVER_SEARCH_INTERVAL_MS=15000
CUTOVER_ELASTICSEARCH_URL=https://<elasticsearch-host>
CUTOVER_SEARCH_REQUIRE_ELASTICSEARCH=true
CUTOVER_SEARCH_REQUIRE_GLOBAL=true
```

### Cutover Work Allocation Check Step
After cutover, verify the seeded cases are queryable through the Work Allocation task API:
```bash
yarn test:verify-cutover-tasks
```
This reads `functional-output/cutover/seed-manifest.json` and writes a report to
`functional-output/cutover/tasks-report.json`. By default it verifies the task API can be queried
for each case. Set `CUTOVER_TASKS_REQUIRE_TASKS=true` once ET confirms which seeded cases should
produce tasks.

Optional environment variables:
```bash
CUTOVER_WA_TASK_MANAGEMENT_URL=https://wa-task-management-api-<preview-service-fqdn>
CUTOVER_TASKS_MANIFEST_FILE=/tmp/seed-manifest.json
CUTOVER_TASKS_OUTPUT_FILE=/tmp/tasks-report.json
CUTOVER_TASKS_PROFILE_IDS=accepted-case-details,accepted-jurisdiction
CUTOVER_TASKS_ATTEMPTS=10
CUTOVER_TASKS_INTERVAL_MS=15000
CUTOVER_TASKS_REQUIRE_TASKS=true
```

### Cutover Notice of Change Readiness Check Step
After cutover, check whether the seeded cases have the organisation policy, representative and
case assignment data needed for Notice of Change:
```bash
yarn test:verify-cutover-noc
```
This reads `functional-output/cutover/seed-manifest.json` and writes a report to
`functional-output/cutover/noc-readiness-report.json`. It does not perform a full Notice of Change
journey because that requires ET-owned professional users, organisation ids and expected case roles.
Set the require flags once ET confirms those expectations.

Optional environment variables:
```bash
CUTOVER_NOC_CASE_ASSIGNMENT_URL=https://ccd-data-store-api-<preview-service-fqdn>
CUTOVER_NOC_MANIFEST_FILE=/tmp/seed-manifest.json
CUTOVER_NOC_OUTPUT_FILE=/tmp/noc-readiness-report.json
CUTOVER_NOC_PROFILE_IDS=accepted-case-details,accepted-jurisdiction
CUTOVER_NOC_REQUIRE_READY=true
CUTOVER_NOC_REQUIRE_ASSIGNMENTS=true
```

To run all test (using script in package.json)
```bash
yarn test:fullfunctional
```
To run a specific test with specific tag
```bash
yarn test:local --grep @<yourtag>
```

### Coding Style Tests
To run all checks (including unit tests):

```bash
./gradlew check
```

### OWASP Dependency Vulnerability Checks
To run the OWASP checks for vulnerabilities in dependencies:

```bash
./gradlew dependencyCheckAggregate
```

## License
This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
