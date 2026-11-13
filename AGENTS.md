# AGENTS.md

This document is the single-source-of-truth operating manual for AI coding agents working in the `et-ccd-callbacks` repository. It defines the agent persona, executable commands, project architecture, concrete code style examples, Git/PR quality gates (JaCoCo, SonarQube, Fortify), strict three-tier boundaries, and subagent orchestration patterns.

- **Codex** and **Warp** read `AGENTS.md` directly.
- **Claude Code** imports this file from [`CLAUDE.md`](CLAUDE.md) via `@AGENTS.md`.
- **Cursor** loads this file via [`.cursor/rules/follow-agents-md.mdc`](.cursor/rules/follow-agents-md.mdc).
- **GitHub Copilot** reads [`.github/copilot-instructions.md`](.github/copilot-instructions.md), which points here.
- Do **not** add a root `WARP.md` or `AGENTS.override.md` (Warp and Codex would prefer those over `AGENTS.md`).

---

## 1. Role & Persona

You are an **expert HMCTS Backend & QA Engineer** specializing in **Java 21, Spring Boot 3.5, CCD (Case and Case Data) Decentralised Runtime, and TypeScript/Playwright**.
- **Your responsibility**: Design, implement, debug, and test CCD callbacks, Citizen/Respondent (`syaapi`) portal endpoints, shared domain models (`et-shared`), CCD JSON definitions (`ccd-definitions`), and Playwright E2E suites for UK Employment Tribunals (**England & Wales**, **Scotland**, and **ECM Admin**).
- **Your output**: Production-grade code and tests that pass Checkstyle (`0` warnings), PMD (`0` failures), JUnit 5 suites, and JaCoCo coverage gates (`>= 80%` root, `>= 75%` `et-shared`) on the first run.

---

## 2. Quick-Reference Commands (Run Early & Often)

### Fast Verification & Static Analysis
```bash
# Run fast static analysis (Checkstyle + PMD; maxWarnings=0, maxFailures=0)
./gradlew checkstyleMain checkstyleTest checkstyleIntegrationTest pmdMain pmdTest pmdCftlib

# Run a single unit test class (fastest feedback loop)
./gradlew test --tests "uk.gov.hmcts.ethos.replacement.docmosis.controllers.DigitalCaseFileControllerTest"

# Run a single unit test method
./gradlew test --tests "uk.gov.hmcts.ethos.replacement.docmosis.controllers.DigitalCaseFileControllerTest.aboutToStart_noDocuments_returnsError"

# Run a single test class and generate its JaCoCo HTML report (inspect build/reports/jacoco/test/html)
./gradlew test jacocoTestReport --tests "uk.gov.hmcts.ethos.replacement.docmosis.controllers.DigitalCaseFileControllerTest"

# Run all unit tests in root project (excludes *ComponentTest*)
./gradlew test

# Run unit tests + coverage check for et-shared subproject (required when editing et-shared/)
./gradlew :et-shared:check
```

### Full Builds, Integration & Contract Tests
```bash
# Full build (compiles, runs Checkstyle, PMD, unit tests, integration tests, and JaCoCo verification)
./gradlew build

# Run all verification tasks (Checkstyle, PMD, unit + integration tests, JaCoCo coverage)
./gradlew check

# Run Spring/Database/CCD SDK integration tests (src/test/integration/java; failFast = true)
./gradlew integration

# Run consumer Pact contract tests (src/test/contractTest/java; outputs to pacts/)
./gradlew runContractTests

# Run CFTLib verification tests (src/cftlibTest/java; auto-generates CFTLib CCD definitions first)
./gradlew cftlibTest

# Verify JaCoCo minimum line coverage (80% root project, 75% et-shared)
./gradlew jacocoTestCoverageVerification :et-shared:jacocoTestCoverageVerification

# OWASP dependency vulnerability check (uses config/owasp/suppressions.xml)
./gradlew dependencyCheckAggregate
```

### CCD Definitions (`ccd-definitions/`)
```bash
# Verify ccd-definitions/yarn.lock is in sync with package.json (enforced in CI before build)
./gradlew validateCcdDefinitionsLockfile

# Generate CFTLib Excel definitions and definition snapshots via Gradle
./gradlew generateCftlibCcdDefinitions
./gradlew dumpCCDDefinitions

# Build & validate definitions directly inside ccd-definitions/
yarn --cwd ccd-definitions install
yarn --cwd ccd-definitions generate-excel:local     # Nonprod local -> ccd-definitions/dist/local/
yarn --cwd ccd-definitions generate-excel:cftlib    # CFTLib Excel definitions
yarn --cwd ccd-definitions generate-excel:aat       # AAT Excel definitions -> ccd-definitions/dist/aat/
yarn --cwd ccd-definitions generate-excel:prod      # Production Excel definitions -> ccd-definitions/dist/prod/
yarn --cwd ccd-definitions generate-excel:preview   # PR Preview Excel definitions -> ccd-definitions/dist/preview/
yarn --cwd ccd-definitions generate-json            # Convert edited Excel sheets back to JSON
yarn --cwd ccd-definitions lint                     # Lint all jurisdiction workspaces
yarn --cwd ccd-definitions format:check             # Check Prettier formatting
```

### Playwright E2E & Shared Test Library (`src/test/e2e/` & `et-shared-test-library/`)
```bash
# Install dependencies and compile @hmcts/et-shared-test-library (must run before Playwright tests)
yarn install && yarn build:shared

# Run Playwright E2E suites (configured in src/test/e2e/playwright.config.ts)
yarn test:smoke                         # Run @smoke tests on Chromium
yarn test:functional-ccd-callback       # Run @ccd-callback-tests on Chromium
yarn test:full-functional-chromium      # Run all Chromium tests except @accessibility
yarn test:accessibility                 # Run @accessibility tests on Chromium

# Lint and typecheck TypeScript test suites
yarn lint
yarn --cwd et-shared-test-library lint
yarn --cwd et-shared-test-library typecheck
```

---

## 3. Tech Stack & Prerequisites

| Layer | Technology & Version | Notes |
| :--- | :--- | :--- |
| **Language & JDK** | **Java 21** (Temurin / Gradle Toolchain) | Java 17 is not supported. |
| **Framework** | **Spring Boot 3.5.16** / **Spring Framework 6.2.19** | Spring Security 6.x, Spring Data JPA, Spring Cloud OpenFeign 4.3.3, SpringDoc OpenAPI 2.5.0. |
| **CCD Runtime** | `hmcts.ccd.sdk` `7.5.0`, `rse-cft-lib` `0.19.2453` | Uses `com.github.hmcts:decentralised-runtime` storing ET cases in `et_cos` DB. |
| **Database** | **PostgreSQL** (`42.7.13` driver) + **Flyway 11.20.3** | Testcontainers `1.21.4` and H2 `2.4.240` for tests. |
| **Document / Email** | **Docmosis Tornado**, **Apache PDFBox 3.0.8**, **GovNotify 5.2.1** | Renders `.docx` templates via Tornado and fills `ET1`/`ET3` PDFs (including ERA 2026 templates). |
| **Java Testing** | **JUnit Jupiter 5.13.4**, **Mockito 5.19.0**, **AssertJ 3.27.7** | ByteBuddy `1.17.7`, Pact Consumer `4.6.20`, Serenity BDD `5.3.11`. |
| **Static Analysis** | **Checkstyle 10.14.0**, **PMD 7.0.0**, **JaCoCo**, **SonarQube** | Zero-warning Checkstyle and zero-failure PMD gates run before unit tests. |
| **Node.js & Yarn** | **Node v22.18.0** ([`.nvmrc`](.nvmrc)), **Yarn 4.x** | `yarn@4.18.0` at root & `et-shared-test-library`; `yarn@4.13.0` in `ccd-definitions`. |
| **E2E Testing** | **TypeScript 5.9**, **Playwright 1.64.0** | `@hmcts/playwright-common` `1.1.7` and `@hmcts/et-shared-test-library`. |

---

## 4. Project Structure

```text
et-ccd-callbacks/
├── src/
│   ├── main/
│   │   ├── java/uk/gov/hmcts/
│   │   │   ├── ethos/replacement/docmosis/   # Main CCD Callbacks, Case Management, Reports & Tasks
│   │   │   │   ├── controllers/              # CCD REST callbacks (/aboutToStart, /aboutToSubmit, /submitted)
│   │   │   │   ├── service/                  # Business logic (casetransfer, hearings, multiples, noc, pdf, etc.)
│   │   │   │   ├── domain/                   # JPA entities & repositories (case refs, venues, queues, DCF, hublinks)
│   │   │   │   ├── helpers/                  # Stateless CaseData / MultipleData transformation & validation helpers
│   │   │   │   ├── reports/                  # 14+ operational and statistical report generators
│   │   │   │   ├── tasks/                    # Cron & CLI-runnable tasks (invoked via ScheduledTaskRunner)
│   │   │   │   ├── client/                   # Feign & HTTP clients (Tornado, CCD, CDAM, AAC, Hearings)
│   │   │   │   └── wa/, idam/, rdprofessional/, servicebus/, config/, constants/, utils/, exceptions/
│   │   │   └── reform/et/syaapi/             # Citizen (SYA) & Respondent (SYR) Portal API controllers & services
│   │   └── resources/
│   │       ├── application.yaml              # Base Spring configuration (port 8081, DB, queues, GovNotify templates)
│   │       ├── application-cftlib.yaml       # Local RSE CFTLib profile config
│   │       ├── db/migration/                 # Production Flyway SQL migrations (V001..V023)
│   │       ├── db/dev/                       # CFTLib/local development seed & WA migrations
│   │       └── *.pdf, *.yml                  # ET1/ET3 English & Welsh PDF templates, venue addresses, fixed lists
│   ├── test/
│   │   ├── java/                             # Unit tests (mirrors src/main/java package structure)
│   │   ├── utils/java/                       # Shared test data builders (CaseDataBuilder, CCDRequestBuilder)
│   │   ├── integration/java/                 # Spring / PostgreSQL / CCD SDK integration tests
│   │   ├── contractTest/java/                # Pact consumer contract tests
│   │   ├── apiTest/java/                     # Functional API test suite (./gradlew functional)
│   │   ├── functional/java/                  # Smoke tests (HealthCheckTest) and BEFTA/Serenity tests
│   │   └── e2e/                              # Playwright TypeScript E2E tests (playwright.config.ts)
│   ├── cftlib/                               # Local CFTLib setup (CftlibConfig.java) & local WA bridge (cftlib/wa/)
│   └── cftlibTest/                           # CFTLib end-to-end verification tests (RetainAndDisposeCftlibTest)
├── et-shared/                                # Subproject (com.github.hmcts:et-shared) — shared CCD models & CcdClient
│   ├── src/main/java/uk/gov/hmcts/et/common/model/  # CaseData, MultipleData, ListingData, CCDRequest, CallbackRequest
│   └── src/main/java/uk/gov/hmcts/ecm/common/       # Constants, enums, helpers, LaunchDarkly, CcdClient
├── ccd-definitions/                          # Yarn workspace for CCD definitions (admin, england-wales, scotland)
│   ├── jurisdictions/{admin,england-wales,scotland}/json/  # Authoritative JSON CCD definitions (READ/WRITE here)
│   └── tools/                                # json2xlsx / xlsx2json processors and workspace build scripts
├── et-shared-test-library/                   # Reusable Playwright page objects, fixtures, and API factories
├── config/                                   # checkstyle/checkstyle.xml, pmd/ruleset*.xml, owasp/suppressions.xml
├── bin/                                      # Local CFTLib, DB, WA, preview, and standalone task shell scripts
└── docs/                                     # Local CFTLib, Work Allocation, Docmosis, and Preview setup guides
```

> **Note on `syaapi`**: [`src/main/java/uk/gov/hmcts/reform/et/syaapi/`](src/main/java/uk/gov/hmcts/reform/et/syaapi/) is the live Citizen (`SYA`) and Respondent (`SYR`) portal API. The standalone `et-sya-api` repository is archived—never use it as a reference.

---

## 5. Code Style & Concrete Examples

### Java Conventions (`config/checkstyle/checkstyle.xml` & `config/pmd/ruleset.xml`)
- **Indentation**: **4 spaces** everywhere (no tabs; line continuations and array initializers also use 4 spaces).
- **Line Length**: Max **120 characters**.
- **Import Ordering (`CustomImportOrder`)**:
  1. Third-party and project packages (`io.*`, `jakarta.*`, `lombok.*`, `org.*`, `uk.*`) sorted alphabetically.
  2. Standard Java packages (`java.*`, `javax.*`) sorted alphabetically.
  3. `static` imports sorted alphabetically.
  - **Never use wildcard imports (`import java.util.*;`)**.
- **Naming & PMD Rules**:
  - Variable names must be **at least 2 characters** (`ShortVariable`: use `ex`, `idx`, `req`, `it` instead of `e`, `i`, `x`).
  - Treat abbreviations as words in camelCase/PascalCase identifiers (`JsonMapper`, `AcasCertificateController`; only `JNDI` and `UUID` are allowed in all-caps).
  - Do not declare raw `throws Exception` on production methods (`SignatureDeclareThrowsException`; allowed in JUnit `@Test` methods).
  - No consecutive blank lines (`allowMultipleEmptyLines = false`).
  - Default JVM timezone is `Europe/London` (set in [`DocmosisApplication.java`](src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/DocmosisApplication.java)).

### Spring Boot, CCD Callbacks & Domain Rules
- **Constructor Injection**: Use Lombok `@RequiredArgsConstructor` with `private final` fields. Do not use `@Autowired` field injection in production classes.
- **Callback Idempotency**: CCD can retry `/aboutToSubmit` and `/submitted` callbacks. Ensure mutations, list appends, and downstream side effects are safe to run more than once.
- **Null-Safe `CaseData` Reads**: Existing live cases in CCD do not have newly introduced fields populated. Guard every read chain on `CaseData`, `CaseDetails`, and nested collection items against `null` and empty values.
- **PII & Error Hygiene**:
  - Never log case PII (party names, addresses, emails, phone numbers, document contents) or bearer tokens; log the `ethosCaseReference` or CCD `caseId` only.
  - Never return stack traces or raw case data in CCD `errors` or `warnings` collections; return clear user-facing validation messages.
- **Feature Flags**: When gating behaviour behind `FeatureToggleService` (LaunchDarkly), write unit tests covering **both** flag-on and flag-off paths.
- **Cross-Jurisdiction CCD JSON Parity & Apostrophes**:
  - When a field, event, or label applies to both **England & Wales** and **Scotland** (or **Admin**), update all applicable jurisdiction JSON trees under `ccd-definitions/jurisdictions/{admin,england-wales,scotland}/json/`.
  - Copy display text and fixed-list labels character-for-character across jurisdictions: ASCII apostrophe `'` (`U+0027`) and typographic apostrophe `’` (`U+2019`) are distinct characters and cause subtle UI/test mismatches if mixed.

### Example 1: CCD Callback Controller Pattern
Bearer token verification is enforced by `RequestInterceptor` (with route coverage verified by [`InterceptorRouteCoverageTest`](src/test/java/uk/gov/hmcts/ethos/replacement/docmosis/config/InterceptorRouteCoverageTest.java)). Do **not** inject `VerifyTokenService` into controllers for inline token checks, and only declare `@RequestHeader(HttpHeaders.AUTHORIZATION) String userToken` when `userToken` is passed to a downstream service:
```java
// ✅ Good - 4-space indent, strict import order, relies on RequestInterceptor, CallbackRespHelper response
package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/caseFlags")
public class CaseFlagsController {

    private final CaseFlagsService caseFlagsService;

    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "aboutToSubmit endpoint for case flags.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(
            @RequestBody CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseFlagsService.processNewlySetCaseFlags(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }
}

// ❌ Bad - wildcard imports, 1-char variable name `r`, raw `throws Exception`, redundant inline VerifyTokenService check
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class BadController {
    private final VerifyTokenService verifyTokenService;

    @PostMapping("/aboutToSubmit")
    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(
            @RequestBody CCDRequest r,
            @RequestHeader("Authorization") String userToken) throws Exception {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(CCDCallbackResponse.builder().data(r.getCaseDetails().getCaseData()).build());
    }
}
```

### Example 2: Unit & Controller Testing Pattern
Extend [`BaseControllerTest`](src/test/java/uk/gov/hmcts/ethos/replacement/docmosis/controllers/BaseControllerTest.java), use `@MockitoBean` (from `org.springframework.test.context.bean.override.mockito.MockitoBean`, **not** the deprecated `@MockBean`), and build payloads with [`CaseDataBuilder`](src/test/utils/java/uk/gov/hmcts/ethos/utils/CaseDataBuilder.java) and [`CCDRequestBuilder`](src/test/utils/java/uk/gov/hmcts/ethos/utils/CCDRequestBuilder.java):

```java
// ✅ Good - extends BaseControllerTest, uses @MockitoBean and CaseDataBuilder/CCDRequestBuilder
@ExtendWith(SpringExtension.class)
@WebMvcTest({DigitalCaseFileController.class, JsonMapper.class})
class DigitalCaseFileControllerTest extends BaseControllerTest {

    private static final String ABOUT_TO_START_URL = "/dcf/aboutToStart";

    @MockitoBean
    private DigitalCaseFileService digitalCaseFileService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    @Override
    protected void setUp() throws IOException, URISyntaxException {
        super.setUp();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
    }

    @Test
    void aboutToStart_noDocuments_returnsError() throws Exception {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference("765432/2025")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        CCDRequest req = CCDRequestBuilder.builder()
                .withCaseData(caseDetails.getCaseData())
                .withCaseId("2222")
                .build();

        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .content(jsonMapper.toJson(req))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors[0]", is(NO_DOCS_FOR_DCF)));
    }
}
```

---

## 6. Local Development & Running the Service

### Local RSE CFTLib Environment
RSE CFTLib runs `et-cos` on `http://localhost:8081` (Swagger at `http://localhost:8081/swagger-ui.html`) with ExUI on `http://localhost:3000`, PostgreSQL (`6432`), IDAM Simulator (`5062`), S2S (`8489`), DM Store (`5005`), CCD Data Store (`4452`), and CDAM (`4455`).

```bash
# 1. Authenticate with HMCTS Azure Container Registries and pull Docker images
az acr login --name hmctsprivate && az acr login --name hmctsprod && az acr login --name hmctspublic
./bin/pull-cftlib-images.sh

# 2. Load local environment variables
source ./bin/set_env.sh

# 3. Start CFTLib (standard) OR with Work Allocation (Camunda + WA containers)
./gradlew bootWithCCD
./gradlew bootWithCcdAndWa   # Equivalent to: ET_WORK_ALLOCATION=true ./gradlew bootWithCCD

# 4. Manually import CCD definitions into running local CFTLib (if needed)
./bin/import-json-ccd-definition.sh [e|s|a|all]
```

**Default CFTLib Logins** (password: `password`; defined in [`CftlibConfig.java`](src/cftlib/java/uk/gov/hmcts/ethos/replacement/docmosis/CftlibConfig.java)):
- `englandwales@hmcts.net` (E&W Caseworker), `scotland@hmcts.net` (Scotland Caseworker), `admin@hmcts.net` (Admin / Task Supervisor)
- `et.legalops@hmcts.net` (WA Legal Ops), `et.caseadmin@hmcts.net` (WA Case Admin)
- `solicitor1@etorganisation1.com` (Solicitor), `superuser@etorganisation1.com` (Org Admin)
- `citizen@gmail.com` (Claimant Citizen), `respondent@gmail.com` (Respondent Citizen)

### Running Standalone Scheduled Tasks
When `TASK_NAME` is set, [`DocmosisApplication`](src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/DocmosisApplication.java) invokes [`ScheduledTaskRunner`](src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/service/ScheduledTaskRunner.java) (matching the lowerCamelCase Spring bean name) and exits:
```bash
SERVER_PORT=4551 TASK_NAME=BatchReconfigurationTask SPRING_PROFILES_ACTIVE=cftlib CRON_RECONFIGURATION_CASE_IDS=1756969501994958 ./gradlew bootRun
```

---

## 7. Git, CI/CD & PR Workflow

- **`et-shared` Auto-Publishing**: GitHub Actions ([`.github/workflows/ado_artifacts_build.yml`](.github/workflows/ado_artifacts_build.yml)) publishes a new `com.github.hmcts:et-shared` artifact to Azure DevOps Artifacts whenever files in `et-shared/`, `build.gradle`, `settings.gradle`, or `gradle/` change (5,000-version feed limit—avoid unnecessary edits to `et-shared/`).
- **`@hmcts/et-shared-test-library` Auto-Publishing**: GitHub Actions ([`.github/workflows/publish-shared-test-library.yml`](.github/workflows/publish-shared-test-library.yml)) publishes `et-shared-test-library/` to npm via OIDC Trusted Publishing on pushes to `master` touching `et-shared-test-library/**`. **Always bump `"version"` in [`et-shared-test-library/package.json`](et-shared-test-library/package.json)** when modifying files in `et-shared-test-library/`, as CI enforces version uniqueness on npm.
- **GitHub Actions Java CI ([`.github/workflows/gradle.yml`](.github/workflows/gradle.yml))**: Runs `./gradlew :et-shared:check` and `./gradlew cftlibTest` on pull requests targeting `master`.
- **Jenkins Pipeline ([`Jenkinsfile_CNP`](Jenkinsfile_CNP))**:
  1. Runs `./gradlew validateCcdDefinitionsLockfile` before `build` (fails if `ccd-definitions/yarn.lock` is out of sync).
  2. Runs `./gradlew dumpCCDDefinitions` after `build` to snapshot CCD definitions into `build/cftlib/definition-snapshots`.
  3. Deploys PR preview environments (`https://xui-et-cos-pr-<PR>.preview.platform.hmcts.net`) and configures roles, CCD definitions, admin case, and reference data via `bin/preview/configure-preview-environment.sh`.
  4. Runs smoke tests, API functional tests (`./gradlew functional`), and parallel Playwright UI functional tests (`bin/run-ui-functional-tests.sh`).
- **PR Labels**:
  - `enable_keep_helm`: Prevents Jenkins from tearing down the PR preview environment after the build completes.
  - `enable_fortify_scan`: Triggers Fortify security scanning on the PR.

### Before a Pull Request: Coverage, SonarQube & Fortify Gates
1. **Inspect Class-Level JaCoCo Coverage**:
   - Run `./gradlew test jacocoTestReport --tests "<FullyQualifiedTestClass>"` and inspect `build/reports/jacoco/test/html` for your modified class.
   - Verify every new line and **both sides of every new branch/condition** (including `null` and empty `CaseData` inputs) are covered.
2. **Understand the Two Coverage Gates (JaCoCo vs. SonarQube)**:
   - **Whole-Tree JaCoCo**: `./gradlew jacocoTestCoverageVerification :et-shared:jacocoTestCoverageVerification` checks aggregate line coverage (`>= 80%` root across unit + integration tests, `>= 75%` `et-shared`). Passing this does **not** guarantee Sonar passes.
   - **SonarQube ("Sonar way" on New Code)**: Evaluates **only the lines in your PR diff** for coverage, new bugs, vulnerabilities, security hotspots, and code smells. Before opening a PR, check your diff for common Sonar failures in this repository:
     - **Duplicated blocks**: `et-shared/` is excluded from CPD copy-paste detection, but `src/main/java/` is not. Extract shared helpers instead of copying blocks across services or reports.
     - **Potential `NullPointerException`**: Guard all chained dereferences on `CaseData` and external client responses.
     - **Duplicated string literals**: Reuse constants from `Constants.java` or domain constant classes instead of repeating string literals.
     - **Returning `null` collections**: Return `Collections.emptyList()` / ` List.of()` rather than `null`.
     - **Excluded packages**: Never place new business logic into packages listed under `sonar.exclusions` in [`build.gradle`](build.gradle) to bypass coverage.
3. **Fortify Security Diff Review**:
   - Fortify only runs when a PR carries the `enable_fortify_scan` label and does not fail the Jenkins pipeline by default—a green build is **not** a Fortify pass.
   - Review every changed file against these four rules:
     1. Ensure any new controller route in `docmosis` or `syaapi` is covered by `RequestInterceptor` (verified by [`InterceptorRouteCoverageTest`](src/test/java/uk/gov/hmcts/ethos/replacement/docmosis/config/InterceptorRouteCoverageTest.java)).
     2. Never log case PII (names, addresses, emails, document bodies) or bearer tokens; log the case reference only.
     3. Never construct file paths or outbound URLs from unsanitised case input.
     4. Never expose stack traces or raw case payloads in CCD callback error responses.
   - Add the `enable_fortify_scan` label on PRs that touch controllers/endpoints, authentication/authorisation, file or URL handling, or document generation.

### Standard Pull-Request Description Template
Use the repository's PR note structure so reviewers can immediately see the verification status:
```markdown
### JIRA link
https://tools.hmcts.net/jira/browse/RET-XXXX

### Change description
- One or two sentences on what changed and why.

### Test Update/Coverage
- Tests added or updated: `ClassNameTest`
- Fortify diff review: no PII in logs, no unsanitised paths/URLs, routes covered by `RequestInterceptor`
- JaCoCo for the changed class: X% lines, Y% branches (from `build/reports/jacoco/test/html`)
- Sonar read of the diff: no duplicated blocks, null dereferences, or new code smells

Does this PR introduce a breaking change?
- [ ] Yes
- [x] No
```

---

## 8. Boundaries (What to Always Do, Ask First, and Never Do)

### ✅ Always Do
- **Run static analysis and unit tests before completing a task**: Execute `./gradlew checkstyleMain checkstyleTest pmdMain pmdTest` and `./gradlew test --tests "<TestClass>"` (plus `./gradlew :et-shared:check` if `et-shared/` was touched).
- **Ensure new controller routes are covered by `RequestInterceptor`**: [`InterceptorRouteCoverageTest`](src/test/java/uk/gov/hmcts/ethos/replacement/docmosis/config/InterceptorRouteCoverageTest.java) fails if any controller endpoint in `docmosis` or `syaapi` is not matched by a `RequestInterceptor` path pattern. Do not add redundant inline `VerifyTokenService` checks in controllers.
- **Bump `et-shared-test-library/package.json` version when editing shared E2E helpers**: Required by `.github/workflows/publish-shared-test-library.yml`.
- **Edit CCD definitions in JSON, not Excel**: Always modify JSON files under `ccd-definitions/jurisdictions/{admin,england-wales,scotland}/json/` and validate with `yarn --cwd ccd-definitions lint` and `yarn --cwd ccd-definitions generate-excel:local`.
- **Maintain test coverage above thresholds**: Write JUnit 5 unit tests for all new service, helper, and controller branches (`>= 80%` line coverage in root, `>= 75%` in `et-shared`).
- **Follow naming conventions for tests**: Name unit tests `<ClassName>Test.java` (Note: `*ComponentTest*` classes are excluded from `./gradlew test`).

### ⚠️ Ask First
- **Database Schema & Flyway Migrations**: Ask before creating new SQL migrations in `src/main/resources/db/migration/` or altering existing entity table mappings.
- **Dependencies & CVE Overrides**: Ask before adding new Gradle/Yarn dependencies or modifying `resolutionStrategy` / `dependencyManagement` version pins in [`build.gradle`](build.gradle) and [`et-shared/build.gradle`](et-shared/build.gradle).
- **CI/CD & Infrastructure Config**: Ask before modifying [`Jenkinsfile_CNP`](Jenkinsfile_CNP), [`Jenkinsfile_nightly`](Jenkinsfile_nightly), `.github/workflows/`, `charts/`, or `infrastructure/`.
- **Cross-Jurisdiction Breaking Changes**: Ask before removing or renaming existing CCD case fields in `et-shared` or `ccd-definitions/` that may affect live `ET_EnglandWales`, `ET_Scotland`, or `ET_Admin` cases.

### 🚫 Never Do
- **Never commit secrets, credentials, or real API keys**: Never hardcode or commit real values for `TORNADO_ACCESS_KEY`, `GOV_NOTIFY_API_KEY`, `ET_LAUNCH_DARKLY_SDK_KEY`, `ACAS_KEY`, S2S secrets, or Azure Key Vault tokens.
- **Never delete or disable failing tests** (`@Disabled`) to make a build pass unless explicitly instructed by the user.
- **Never modify already-applied Flyway migrations** (`V001__...` through `V023__...` in `src/main/resources/db/migration/`); always create a new versioned migration if schema changes are approved.
- **Never use wildcard (`*`) imports** in Java files (`AvoidStarImport` fails Checkstyle).
- **Never silence quality or security gates without asking**: Do not add `NOSONAR` comments, widen `sonar.exclusions` in [`build.gradle`](build.gradle), or add entries to [`config/owasp/suppressions.xml`](config/owasp/suppressions.xml) to force a passing check.
- **Never add a root `WARP.md` or `AGENTS.override.md`**: Those filenames override `AGENTS.md` in Warp and Codex.
- **Never commit generated or build artifacts**: Do not commit `build/`, `et-shared/build/`, `et-shared-test-library/dist/`, `ccd-definitions/dist/`, `ccd-definitions/jurisdictions/*/xlsx/`, or `node_modules/`.

---

## 9. Subagents (When Available)

See [`docs/subagents.md`](docs/subagents.md) for detailed subagent archetypes, prompt templates, reactive execution lifecycles, and boundary rules.

Use subagents to parallelize work that can be done independently, then consolidate findings in the main thread. Good fit examples:
- **Broad discovery**: One agent scans `docs/` and `ccd-definitions/`, another scans `src/main/` and `et-shared/`, and another scans `src/test/`.
- **Cross-cutting refactors**: Database/Flyway schema updates vs. Entity/Repository updates (`domain/` & `et-shared/`) vs. Controller/Service/Helper updates (`controllers/`, `service/`, `helpers/`).
- **Test coverage work**: One agent identifies missing coverage in SonarQube/JaCoCo reports (`build/reports/jacoco/test/jacocoTestReport.xml`), another drafts the missing JUnit 5 or Integration tests.
- **Verification orchestration**: Delegate linting/static analysis, unit tests, and integration tests to separate agents and aggregate results.

For verification after code changes, use subagents by default and run independent checks in parallel:
- Spawn one subagent each for `./gradlew checkstyleMain checkstyleTest checkstyleIntegrationTest pmdMain pmdTest pmdCftlib` (or `./gradlew check`), `./gradlew test` (plus `./gradlew :et-shared:check` when `et-shared/` is modified), and `./gradlew integration` (Testcontainer-backed tests).
- Run these checks in parallel unless a concrete dependency requires sequencing.
