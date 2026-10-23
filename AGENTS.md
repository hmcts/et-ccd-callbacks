# AGENTS.md

Operating manual for coding agents in `et-ccd-callbacks` (deployed as `et-cos`). Employment Tribunals CCD callbacks for England & Wales, Scotland, and ECM Admin, plus the citizen and respondent API in `uk.gov.hmcts.reform.et.syaapi`.

ChatGPT Codex and Warp read this file directly. Do not add a root `WARP.md` or `AGENTS.override.md`: Warp prefers `WARP.md` over this file, and Codex uses `AGENTS.override.md` in place of it. Other tools:

- Claude Code loads this file through the `@AGENTS.md` import in `CLAUDE.md`.
- Cursor loads this file, plus `.cursor/rules/follow-agents-md.mdc`.
- GitHub Copilot in IntelliJ and VS Code loads `.github/copilot-instructions.md` for Chat. Copilot agent mode also reads this file. If the two disagree, follow this file.

Java 21. Spring Boot 3.5. Dependency versions live in `build.gradle` and `package.json`. Default timezone is `Europe/London`.

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
- `src/main/java/uk/gov/hmcts/reform/et/syaapi/` — live citizen (SYA) and respondent (SYR) API. The standalone `et-sya-api` repository is archived.
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

`./gradlew check` is the local Gradle gate. Jenkins then runs that build, a Sonar scan, a preview deploy, smoke tests, API functional tests, and Playwright. Pact consumer tests run on pull requests. Fortify runs only when the pull request has the `enable_fortify_scan` label, and that scan does not fail the pipeline by default.

Two coverage gates:

- JaCoCo line coverage is at least 80% in the root project and 75% in `et-shared`. `./gradlew check` enforces this across the whole tree, using unit and integration execution data in the root project.
- Sonar is separate. Unless this project has a custom quality gate, Sonar way applies to new code: new bugs, new vulnerabilities, unreviewed security hotspots, duplication, and coverage of the changed lines. Overall JaCoCo above 80% can still fail that gate. Cover every new branch.
- In the pull-request note, name the test command and its result. Do not report a pass for a command that was not run.

Review the diff for Fortify-class issues before opening the pull request:

- Do not log case data, bearer tokens, or other credentials.
- Do not build a file path or URL from unsanitised case input.
- Do not add a secret or a real key.
- A new callback verifies the service token the same way existing controllers do (`VerifyTokenService`).
- Add the `enable_fortify_scan` label when the change is security-sensitive.

Other gates:

- Update Pact tests when a Feign client request or response changes.
- Leave `et-shared/` unchanged unless the shared model has to change. A change there publishes a new artifact.
- Dependency CVE checks run on the nightly pipeline. Do not silence one with a suppression.

Pull-request note:

- JIRA link.
- What behaviour changed, and the risk, rather than a file list.
- Tests added or updated, and the commands that were run.
- Breaking change: yes or no. Use no unless an API, route, or case-data contract changes incompatibly.

After a push, read the Jenkins result and the Sonar quality gate. A Playwright UI failure is recorded as unstable, so the build can look successful while that stage is not clean.
