# GitHub Copilot instructions for et-ccd-callbacks

[`AGENTS.md`](../AGENTS.md) at the repository root is the operating manual for this repository. Read it for the full command table, repo layout, style rules, PR quality gates, and boundaries. The essentials are repeated below because GitHub Copilot does not follow links automatically during inline completion.

## Stack

Java 21, Spring Boot 3.5.16, Gradle wrapper, PostgreSQL with Flyway, Node 22 with Yarn 4.x (`ccd-definitions/`, `et-shared-test-library/`, Playwright in `src/test/e2e/`).

## Commands

- Fast static analysis: `./gradlew checkstyleMain checkstyleTest checkstyleIntegrationTest pmdMain pmdTest pmdCftlib`
- One test class + JaCoCo HTML report: `./gradlew test jacocoTestReport --tests "uk.gov.hmcts.ethos.replacement.docmosis.service.SomeServiceTest"`
- Shared module check (when `et-shared/` is touched): `./gradlew :et-shared:check`
- CCD definitions check (when `ccd-definitions/` is touched): `./gradlew validateCcdDefinitionsLockfile && yarn --cwd ccd-definitions lint && yarn --cwd ccd-definitions format:check`

## Style and safety

- 4-space indent, 120-character line limit, no wildcard imports (`AvoidStarImport`), variable names at least 2 characters (`ShortVariable`), no raw `throws Exception` on production methods (`SignatureDeclareThrowsException`).
- Import order: third-party and `uk.gov.hmcts.*`, blank line, `java.*` and `javax.*`, blank line, `static` imports.
- Constructor injection with Lombok `@RequiredArgsConstructor`.
- Bearer token verification is enforced by `RequestInterceptor` (verified by `InterceptorRouteCoverageTest`); do not add redundant inline `VerifyTokenService` checks in controllers.
- Never log case PII (names, addresses, emails, document contents) or bearer tokens; log the case reference only.
- Edit CCD definitions in `ccd-definitions/jurisdictions/{admin,england-wales,scotland}/json/`, never in `.xlsx` files.
- Never modify an existing `V001`..`V023` Flyway migration in `src/main/resources/db/migration/`; add a new versioned file.
- Bump `"version"` in `et-shared-test-library/package.json` whenever files in `et-shared-test-library/` are modified.
