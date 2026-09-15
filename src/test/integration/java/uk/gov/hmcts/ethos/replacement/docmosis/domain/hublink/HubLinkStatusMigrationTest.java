package uk.gov.hmcts.ethos.replacement.docmosis.domain.hublink;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

class HubLinkStatusMigrationTest {

    private static final long BACKFILLED_CASE_REFERENCE = 1234567890123456L;
    private static final long DUAL_WRITTEN_CASE_REFERENCE = 1234567890123457L;
    private static final DockerImageName POSTGRES_IMAGE = DockerImageName
        .parse("hmctspublic.azurecr.io/imported/postgres:16-alpine")
        .asCompatibleSubstituteFor("postgres");
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(POSTGRES_IMAGE);

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        POSTGRES.start();

        try (Connection connection = connection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA ccd");
            statement.execute("""
                CREATE TABLE ccd.case_data (
                    reference BIGINT PRIMARY KEY,
                    data JSONB NOT NULL
                )
                """);
        }

        Flyway flyway = flywayTo("18");
        flyway.baseline();
        flyway.migrate();
    }

    @AfterAll
    static void stopDatabase() {
        POSTGRES.stop();
    }

    @Test
    void backfillsStatusesWithoutOverwritingDualWrittenStatus() throws SQLException {
        insertCase(BACKFILLED_CASE_REFERENCE, "completed");
        insertCase(DUAL_WRITTEN_CASE_REFERENCE, "old");
        insertHubLinkStatus(DUAL_WRITTEN_CASE_REFERENCE, "new");

        flywayTo("19").migrate();

        assertThat(storedStatus(BACKFILLED_CASE_REFERENCE)).isEqualTo("completed");
        assertThat(storedStatus(DUAL_WRITTEN_CASE_REFERENCE)).isEqualTo("new");
    }

    private static Flyway flywayTo(String target) {
        return Flyway.configure()
            .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
            .locations("classpath:db/migration")
            .baselineVersion(MigrationVersion.fromVersion("17"))
            .target(MigrationVersion.fromVersion(target))
            .load();
    }

    private static void insertCase(long caseReference, String status) throws SQLException {
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement("""
                 INSERT INTO ccd.case_data (reference, data)
                 VALUES (?, jsonb_build_object(
                     'hubLinksStatuses',
                     jsonb_build_object('personalDetails', ?)
                 ))
                 """)) {
            statement.setLong(1, caseReference);
            statement.setString(2, status);
            statement.executeUpdate();
        }
    }

    private static void insertHubLinkStatus(long caseReference, String status) throws SQLException {
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement("""
                 INSERT INTO public.hub_link_status (case_reference, data)
                 VALUES (?, jsonb_build_object('personalDetails', ?))
                 """)) {
            statement.setLong(1, caseReference);
            statement.setString(2, status);
            statement.executeUpdate();
        }
    }

    private static String storedStatus(long caseReference) throws SQLException {
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement("""
                 SELECT data ->> 'personalDetails'
                 FROM public.hub_link_status
                 WHERE case_reference = ?
                 """)) {
            statement.setLong(1, caseReference);
            try (ResultSet result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                return result.getString(1);
            }
        }
    }

    private static Connection connection() throws SQLException {
        return POSTGRES.createConnection("");
    }
}
