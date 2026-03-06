# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Employment Tribunals CCD Callbacks Service - A Spring Boot 3.3.0 application (Java 21) that handles all CCD (Case and Case Data) callback requests for Employment Tribunal cases in the UK. The application serves England/Wales and Scotland jurisdictions with separate CCD definitions and integrates with HMCTS common platform components.

## Build and Test Commands

### Building
```bash
./gradlew build           # Full build with tests and checks
./gradlew check           # Run all checks including checkstyle, PMD, and unit tests
```

### Testing
```bash
# Unit tests (excludes ComponentTest classes)
./gradlew test

# Run a single test class
./gradlew test --tests "uk.gov.hmcts.ethos.replacement.docmosis.service.ClassName"

# Run a single test method
./gradlew test --tests "uk.gov.hmcts.ethos.replacement.docmosis.service.ClassName.methodName"

# Integration tests
./gradlew integration

# Functional API tests (requires AAT environment and F5 VPN)
./gradlew functional

# UI functional tests (requires Node 18+)
yarn test:fullfunctional                    # Run all UI tests
yarn test:local --grep @<yourtag>          # Run specific tagged tests

# Contract tests
./gradlew runContractTests
```

### Code Quality
```bash
./gradlew dependencyCheckAggregate    # OWASP dependency vulnerability checks
```

### Running the Application

#### With RSE CFT Lib (Recommended)
```bash
# 1. Set up environment variables (edit paths in script first)
source ./bin/set_env.sh

# 2. Pull latest RSE IdAM Simulator image
docker pull hmctspublic.azurecr.io/hmcts/rse/rse-idam-simulator:latest

# 3. Run with CCD environment
./gradlew bootWithCCD
```

Access the application at:
- API: http://localhost:8081
- ExUI: http://localhost:3000
- Swagger: http://localhost:8081/swagger-ui.html

Default login credentials: `password` for all users (see docs/cftlib.md for user list)

#### Running Scheduled Tasks
Tasks can be run via command line or shell scripts:

```bash
# Using environment variables
SERVER_PORT=4551 TASK_NAME=BatchReconfigurationTask SPRING_PROFILES_ACTIVE=cftlib CRON_RECONFIGURATION_CASE_IDS=1756969501994958 ./gradlew bootRun

# Using shell scripts (if available)
./bin/tasks/BatchReconfigurationTask.sh 1756969501994958
```

### CCD Definitions Management
```bash
# Import CCD definitions manually
./bin/import-ccd-definition.sh e    # England/Wales
./bin/import-ccd-definition.sh s    # Scotland
./bin/import-ccd-definition.sh a    # Admin

# Generate cftlib versions (run in CCD definition repos)
yarn generate-excel-cftlib
```

## Architecture Overview

### Technology Stack
- **Java 21** with Spring Boot 3.3.0, Spring Security 6.x
- **PostgreSQL** database with Flyway migrations (src/main/resources/db)
- **Feign** for HTTP clients
- **Docmosis Tornado** for document generation
- **Lombok** for code generation (requires IDE plugin and annotation processing enabled)

### Package Structure

The codebase follows a layered architecture under `uk.gov.hmcts.ethos.replacement.docmosis`:

- **controllers/** - REST endpoints that handle CCD callbacks (POST endpoints accepting `APPLICATION_JSON_VALUE`)
  - All controllers follow CCD callback pattern: `/aboutToStart`, `/aboutToSubmit`, `/submitted`
  - Grouped by functionality: admin/, applications/, case management, etc.

- **service/** - Business logic layer
  - Services named by domain (e.g., `CaseManagementForCaseWorkerService`, `CaseFlagsService`)
  - Handles orchestration between repositories, external clients, and helpers

- **domain/** - Domain models and entities
  - **repository/** - JPA repositories for database access
  - **referencedata/** - Reference data models
  - **documents/** - Document-related domain models
  - **admin/** - Admin-specific domain models

- **helpers/** - Utility classes for specific operations
  - Large collection of helper classes for various business logic transformations

- **client/** - Feign clients for external service integration

- **config/** - Spring configuration classes
  - Security, database, Feign client configurations

- **tasks/** - Scheduled tasks that run via Spring Scheduling or command line
  - Examples: `AcasCertificateTask`, `Et1DocumentGenerationTask`, `NoticeOfChangeFieldsTask`

- **wa/** - Work Allocation integration

- **idam/** - Identity and Access Management integration

- **rdprofessional/** - Reference Data Professional API integration

### Key Architectural Patterns

1. **CCD Callback Pattern**: Controllers expose endpoints that CCD calls at different lifecycle stages:
   - `/aboutToStart` - Called when event starts
   - `/aboutToSubmit` - Called before submission
   - `/submitted` - Called after submission
   - Endpoints consume and return JSON in CCD's format

2. **Jurisdiction Separation**: The system handles three separate CCD definitions:
   - England/Wales (`et-ccd-definitions-englandwales`)
   - Scotland (`et-ccd-definitions-scotland`)
   - Admin (`et-ccd-definitions-admin`)

3. **Task Execution**: Two modes for running tasks:
   - **Spring Scheduling**: Tasks run within the main application on schedules
   - **Command Line**: Tasks can be triggered via `TASK_NAME` environment variable for one-off execution

4. **Database Migrations**: All database changes managed via Flyway (automatically applied on startup)

5. **Multi-Source Set Structure**: Gradle is configured with multiple source sets for different test types:
   - `test` - Unit tests
   - `functional` - Functional tests
   - `apiTest` - API tests
   - `contractTest` - Contract tests
   - `integrationTest` - Integration tests
   - `cftlib` - CFT Lib specific code

### Local Development Environment Options

1. **RSE CFT Lib** (Recommended): Gradle plugin that provides lightweight CCD/ExUI environment
   - Lower resource usage (minimizes Docker containers)
   - Version controlled (prevents issues from component updates)
   - Supports Spring Boot DevTools auto-restart
   - See docs/cftlib.md for full setup

2. **ECM CCD Docker**: Full Docker-based CCD environment
   - See docs/ecm-ccd-docker.md

### Important Configuration Files

- `build.gradle` - Build configuration with source sets, dependencies, tasks, and RSE CFT Lib integration
- `src/main/resources/application.yaml` - Base application configuration
- `src/main/resources/application-cftlib.yaml` - CFT Lib profile configuration
- `src/main/resources/application-dev.yaml` - Development profile configuration
- `lombok.config` - Lombok configuration
- `config/checkstyle/` - Checkstyle rules
- `config/pmd/` - PMD rules
- `config/owasp/` - OWASP suppression rules

### Testing Guidelines

- Unit tests use JUnit 5 (Jupiter) with Mockito 5.x
- Test classes follow naming convention: `<ClassName>Test.java`
- Integration tests use Testcontainers for PostgreSQL
- Functional tests use Serenity BDD framework
- ByteBuddy 1.17.7+ required for Mockito with Java 21

### External Service Dependencies

- **CCD Data Store API**: Core case data service
- **IDAM**: Identity and Access Management (uses RSE IdAM Simulator locally)
- **Docmosis Tornado**: Document generation (requires `TORNADO_ACCESS_KEY` and `TORNADO_URL`)
- **Document Management**: DM Store for document storage
- **Case Document AM API**: Document access management
- **RD Professional API**: Reference data for professional users
- **XUI**: Expert UI for case workers
- **Work Allocation**: Task management (optional, requires extra setup)

### Important Notes

- Java 21 is required (breaking change from Java 17)
- The application is sometimes referred to as "et-cos" (Employment Tribunals Case Orchestration Service) in configuration
- All timestamps use Europe/London timezone (set in `DocmosisApplication.init()`)
- PMD and Checkstyle are enforced on build
- The bootJar task produces `et-cos.jar`
