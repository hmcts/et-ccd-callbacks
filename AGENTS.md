# AGENTS.md

Operating manual for coding agents in `et-ccd-callbacks` (deployed as `et-cos`). This is the Java 21 and Spring Boot 3.5 service for Employment Tribunals CCD callbacks: England & Wales, Scotland, and ECM Admin.

ChatGPT Codex and Warp read this file directly. Do not add a root `WARP.md` or `AGENTS.override.md`: Warp prefers `WARP.md` over this file, and Codex uses `AGENTS.override.md` in place of it. Other tools:

- Claude Code loads this file through the `@AGENTS.md` import in `CLAUDE.md`.
- Cursor loads this file, plus `.cursor/rules/follow-agents-md.mdc`.
- GitHub Copilot in IntelliJ and VS Code loads `.github/copilot-instructions.md` for Chat. Copilot agent mode also reads this file. If the two disagree, follow this file.

Dependency versions live in `build.gradle` and `package.json`. The JVM timezone for this service is `Europe/London`.

## Commands

```bash
./gradlew checkstyleMain checkstyleTest checkstyleIntegrationTest pmdMain pmdTest pmdCftlib
./gradlew test --tests "uk.gov.hmcts.ethos.replacement.docmosis.controllers.DigitalCaseFileControllerTest"
./gradlew test --tests "uk.gov.hmcts.ethos.replacement.docmosis.controllers.DigitalCaseFileControllerTest.aboutToStart_noDocuments_returnsError"
./gradlew test
./gradlew :et-shared:check
./gradlew check
./gradlew integration
./gradlew runContractTests
./gradlew cftlibTest
./gradlew validateCcdDefinitionsLockfile
./gradlew generateCftlibCcdDefinitions
./gradlew dumpCCDDefinitions
yarn --cwd ccd-definitions lint
yarn --cwd ccd-definitions generate-excel:local
yarn install && yarn build:shared
yarn test:smoke
yarn test:functional-ccd-callback
yarn lint
```

`./gradlew test` excludes `*ComponentTest*`. Functional API tests (`./gradlew functional`) and UI tests need the AAT environment. Local CCD: `source ./bin/set_env.sh` then `./gradlew bootWithCCD` (API `http://localhost:8081`, ExUI `http://localhost:3000`). Work Allocation: `./gradlew bootWithCcdAndWa`.

## Layout

- `src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/` — CCD callbacks (`controllers/`, `service/`, `helpers/`, `tasks/`, `client/`).
- `src/main/java/uk/gov/hmcts/reform/et/syaapi/` — Spring MVC API in this service. The standalone `et-sya-api` repository is archived. Do not use it as the implementation reference.
- `et-shared/` — shared CCD models (`com.github.hmcts:et-shared`). Published when this tree or the Gradle build files change.
- `ccd-definitions/jurisdictions/{admin,england-wales,scotland}/json/` — authoritative CCD JSON. Edit JSON here, not generated Excel.
- `et-shared-test-library/` and `src/test/e2e/` — Playwright. Unit tests mirror main under `src/test/java/`. Integration tests are `src/test/integration/java/`.
- `src/main/resources/db/migration/` — applied Flyway migrations. `config/checkstyle/checkstyle.xml` and `config/pmd/` are the style gates.

## Java style

Checkstyle (`maxWarnings = 0`) and PMD (`maxFailures = 0`) run before unit tests. 4-space indent, 120-character lines, no consecutive blank lines, no star imports. Variable names are at least 2 characters. Do not declare `throws Exception` on production methods. Test classes are `<ClassName>Test.java`, extend the existing base where one exists, and use `@MockitoBean` (`org.springframework.test.context.bean.override.mockito.MockitoBean`). Do not use the deprecated `@MockBean`. Build payloads with `CaseDataBuilder` and `CCDRequestBuilder`.

Import order is third-party, then `java`/`javax`, then static, alphabetical inside each group, with no blank line between groups (`separateLineBetweenGroups = false`):

```java
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import java.util.List;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
```

Do not log bearer tokens or other credentials.

## Spring Boot

Follow the controllers, services, and tasks already in the tree.

- Constructor injection. Match `lombok.RequiredArgsConstructor` where the surrounding class uses it.
- A CCD callback controller takes `VerifyTokenService` and calls it before handling the event.
- Any other new HTTP endpoint authorises the caller the same way neighbouring controllers in that package do. A case id on the request is not authorisation.
- Keep an existing request or response JSON shape. Changing it is a breaking change.
- Use `Europe/London` for dates and deadlines.
- Log a case reference only. Leave names, addresses, emails, document bodies, and tokens out of logs.
- Use the existing CCD error types. An error returned to the caller must not include a stack trace or case data.
- A test that loads a Spring context uses `@MockitoBean`. Do not use `@MockBean`.

## Boundaries

Always:

- Run Checkstyle, PMD, and the unit test class you changed before finishing. If you edit `et-shared/`, run `./gradlew :et-shared:check`.
- Meet both coverage gates in "Before a pull request": JaCoCo for the whole tree, and Sonar on the lines the pull request changes.
- Edit CCD definitions as JSON and validate with `yarn --cwd ccd-definitions lint`. When England & Wales and Scotland both use a field or label, update both JSON trees. Copy label and list text exactly. `'` (U+0027) and `’` (U+2019) are different characters.

Ask first:

- New or edited Flyway migrations, and changes to entity table mappings.
- New or upgraded Gradle or Yarn dependencies, including version pins.
- Changes to `Jenkinsfile_CNP`, `Jenkinsfile_nightly`, `.github/workflows/`, `charts/`, or `infrastructure/`.
- Removing or renaming CCD fields that existing `ET_EnglandWales`, `ET_Scotland`, or `ET_Admin` cases already use.

Never:

- Commit secrets or real values for `TORNADO_ACCESS_KEY`, `GOV_NOTIFY_API_KEY`, `ET_LAUNCH_DARKLY_SDK_KEY`, `ACAS_KEY`, S2S secrets, or Key Vault tokens.
- Edit a Flyway migration that has already been applied. Add a new version.
- Delete a test, or mark it `@Disabled`, to make a build pass.
- Add `NOSONAR`, widen `sonar.exclusions`, or add an entry to `config/owasp/suppressions.xml` to force a passing gate. Ask first if a suppression is genuinely required.
- Commit `build/`, `et-shared/build/`, `et-shared-test-library/dist/`, `ccd-definitions/dist/`, generated `xlsx/`, or `node_modules/`.

## Before a pull request

Run the checks below, then write the pull-request note. Do not report a pass for a command that was not run.

### Local build

`./gradlew check` is the local Gradle gate. It runs the unit tests, the integration tests, and JaCoCo. Jenkins then runs that build, a Sonar scan, a preview deploy, smoke tests, API functional tests, and Playwright. Pact consumer tests run on pull requests.

While editing, run Checkstyle, PMD, and the unit test class you changed. Before a push, generate the coverage report for that class:

```bash
./gradlew test jacocoTestReport --tests "uk.gov.hmcts.ethos.replacement.docmosis.controllers.DigitalCaseFileControllerTest"
```

Read the changed class under `build/reports/jacoco/test/html`. Every new line, and both sides of a new condition, must be covered, including null and empty case data.

### Coverage and Sonar

Two gates:

- JaCoCo line coverage is at least 80% in the root project and 75% in `et-shared`. `./gradlew check` enforces this across the whole tree. The root report uses unit and integration execution data. `et-shared` has its own report.
- Sonar is separate. Unless this project has a custom quality gate, Sonar way applies to new code: new bugs, new vulnerabilities, unreviewed security hotspots, duplication, and coverage of the changed lines. Overall JaCoCo above 80% can still fail that gate.

Fix these on the diff, in code. Each one has failed this service's quality gate:

- A duplicated block. Extract one helper. Main sources are included in duplication analysis. `et-shared/` is excluded from that analysis, so a copy there can pass the gate and still be the wrong structure.
- A possible null dereference on a new case-data read. Existing cases omit a newly added field.
- A duplicated string where a constant already exists.
- Returning null where callers expect an empty collection.

Do not put new behaviour in a package already listed in `sonar.exclusions` in `build.gradle`. Those packages are left out of the Sonar scan.

A green unit-test check can sit beside a failed Jenkins status, `continuous-integration/jenkins/pr-head`. That status is often the quality gate. Read the Sonar conditions.

### Fortify

Review the diff before opening the pull request. Fortify runs only when the pull request has the `enable_fortify_scan` label, and that scan does not fail the pipeline by default. A green Jenkins build is not a Fortify pass.

- Do not log case data, bearer tokens, or other credentials.
- Do not build a file path or URL from unsanitised case input.
- Do not add a secret or a real key.
- A new callback verifies the service token with `VerifyTokenService`.
- A new endpoint authorises the caller the same way existing controllers in that package do.
- An error returned to CCD must not include a stack trace or case data.
- Add `enable_fortify_scan` when the change adds an endpoint, file or URL handling, authorisation, or document generation.

### Case data and callbacks

- CCD can retry `aboutToSubmit` and `submitted`. New writes must be safe to run twice.
- Read a new case field only after a null check, and test that path.
- Use `Europe/London` for dates and deadlines.
- When England & Wales and Scotland both use a field or label, update both JSON trees. Update the admin definition as well when that field is shared. Copy label and list text exactly. `'` (U+0027) and `’` (U+2019) are different characters.
- Cover a feature flag both on and off.
- Leave an unrelated refactor out of the pull request. Moved lines become uncovered new code and duplicated-block findings.
- Update Pact tests when a Feign client request or response changes.
- Leave `et-shared/` unchanged unless the shared model has to change. A change there publishes a new artifact.
- Dependency CVE checks run on the nightly pipeline. Do not silence one with a suppression.

### Pull-request note

Write the note after the checks above:

```markdown
### JIRA link

### Change description

<behaviour and risk, rather than a file list>

### Test Update/Coverage

<tests added or updated, the commands that were run, and the result>

Fortify diff review: Pass | Fail — <one line>
JaCoCo for the changed class: Pass | Fail — <one line>
Sonar read of the diff: Pass | Risk — <one line>

**Does this PR introduce a breaking change?** (check one with "x")

[ ] Yes
[ x ] No
```

Use no unless an API, route, or case-data contract changes incompatibly.

### After a push

Read the Jenkins result and the Sonar quality gate. The unit-test stage can pass while the quality gate fails. A Playwright UI failure is recorded as unstable, so the build can look successful while that stage is not clean. Fix the condition Sonar names.

The required loop stops at the checks above. Performance stages in `Jenkinsfile_CNP` are commented out. A local `./gradlew sonarqube` needs a token.
