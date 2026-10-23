# GitHub Copilot

The operating manual is `AGENTS.md` at the repository root. Read it before editing. If this file and `AGENTS.md` disagree, follow `AGENTS.md`.

This service is `et-ccd-callbacks` (et-cos): Java 21, Spring Boot 3.5, CCD callbacks. The Spring MVC API in this repo is `uk.gov.hmcts.reform.et.syaapi`. The archived `et-sya-api` repository is not the live backend.

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

Follow the "Before a pull request" section in `AGENTS.md`. Before the note, read the JaCoCo HTML report for the changed class and review the diff for Fortify-class issues: logs, paths and URLs, secrets, `VerifyTokenService` on a new callback, and authorisation on a new endpoint. Cover new branches, including null case data. Fix duplicated blocks, possible null dereferences, duplicated constants, and null collections in code. Add `enable_fortify_scan` for a new endpoint, file or URL handling, authorisation, or document generation. The note has a JIRA link, the behaviour change, the commands that were run, and whether the change is breaking. After a push, read Jenkins and the Sonar quality gate. A green unit-test check can sit beside a failed quality gate. A Playwright UI failure is recorded as unstable.
