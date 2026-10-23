# GitHub Copilot

The operating manual is `AGENTS.md` at the repository root. Read it before editing. If this file and `AGENTS.md` disagree, follow `AGENTS.md`.

This service is `et-ccd-callbacks` (et-cos): Java 21, Spring Boot 3.5, CCD callbacks, and the citizen API in `uk.gov.hmcts.reform.et.syaapi`. The archived `et-sya-api` repository is not the live backend.

## Always

- Run `./gradlew checkstyleMain checkstyleTest pmdMain pmdTest` and the unit test you changed. If you edit `et-shared/`, also run `./gradlew :et-shared:check`.
- Edit CCD definitions as JSON under `ccd-definitions/jurisdictions/{admin,england-wales,scotland}/json/`.
- Meet both coverage gates. JaCoCo line coverage is at least 80% in the root project and 75% in `et-shared`. Sonar, unless this project has a custom gate, also requires the changed lines to pass the new-code quality gate. Cover every new branch.
- Edit England & Wales and Scotland JSON together when both use a field or label. Copy label text exactly. `'` (U+0027) and `’` (U+2019) are different characters.
- Java: 4-space indent, 120-character lines, no star imports. Import order is third-party, then `java`/`javax`, then static, alphabetical, with no blank line between groups.
- Name unit tests `<ClassName>Test.java`. Use `@MockitoBean`. Do not use the deprecated `@MockBean`.

## Ask first

- Flyway migrations, Gradle or Yarn dependencies, Jenkins or GitHub workflows, Helm charts, and removing or renaming live CCD fields.

## Never

- Commit secrets (`TORNADO_ACCESS_KEY`, `GOV_NOTIFY_API_KEY`, `ET_LAUNCH_DARKLY_SDK_KEY`, `ACAS_KEY`, S2S secrets, Key Vault tokens) or log case data, bearer tokens, or other credentials.
- Edit an applied Flyway migration in `src/main/resources/db/migration/`. Add a new version.
- Delete a test, or mark it `@Disabled`, to make a build pass.
- Add `NOSONAR`, widen `sonar.exclusions`, or add an entry to `config/owasp/suppressions.xml` to force a passing gate.
- Commit `build/`, `et-shared/build/`, `et-shared-test-library/dist/`, `ccd-definitions/dist/`, generated `xlsx/`, or `node_modules/`.

## Before a pull request

Follow the "Before a pull request" section in `AGENTS.md`. Review the diff for logged case data, unsanitised paths or URLs, and a new callback that skips `VerifyTokenService`. Add the `enable_fortify_scan` label when the change is security-sensitive. In the pull-request note, include the JIRA link, the behaviour change, the test commands that were actually run, and whether the change is breaking. After a push, read Jenkins and the Sonar quality gate. A Playwright UI failure is recorded as unstable.
