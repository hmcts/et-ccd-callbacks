package uk.gov.hmcts.ethos.replacement.docmosis.domain.hublink;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.HubLinksStatuses;
import uk.gov.hmcts.ethos.replacement.docmosis.config.JacksonConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.HubLinkStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.HubLinkStatusRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
    "core_case_data.api.url=localhost:4452",
    "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JacksonConfiguration.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class HubLinkStatusMigrationTest {

    private static final long BACKFILLED_CASE_REFERENCE = 1234567890123456L;
    private static final long DUAL_WRITTEN_CASE_REFERENCE = 1234567890123457L;
    private static final DockerImageName POSTGRES_IMAGE = DockerImageName
        .parse("hmctspublic.azurecr.io/imported/postgres:16-alpine")
        .asCompatibleSubstituteFor("postgres");
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(POSTGRES_IMAGE);

    static {
        POSTGRES.start();
        setUpDatabase();
    }

    @Autowired
    private HubLinkStatusRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    private static void setUpDatabase() {
        Properties flywayProperties = new Properties();
        flywayProperties.setProperty("flyway.postgresql.transactional.lock", "false");

        Flyway.configure()
            .configuration(flywayProperties)
            .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
            .schemas("ccd")
            .locations("classpath:dataruntime-db/migration")
            .load()
            .migrate();

        Flyway flyway = flywayTo("18");
        flyway.baseline();
        flyway.migrate();
    }

    @AfterAll
    static void stopDatabase() {
        POSTGRES.stop();
    }

    @Test
    void backfillsStatusesWithoutOverwritingDualWrittenStatus() throws SQLException, JsonProcessingException {
        insertCase(BACKFILLED_CASE_REFERENCE, "completed");
        insertCase(DUAL_WRITTEN_CASE_REFERENCE, "old");
        repository.saveAndFlush(HubLinkStatus.create(DUAL_WRITTEN_CASE_REFERENCE, statuses("new")));

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

    private void insertCase(long caseReference, String status) throws SQLException, JsonProcessingException {
        CaseData caseData = new CaseData();
        caseData.setHubLinksStatuses(statuses(status));

        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement("""
                 INSERT INTO ccd.case_data (
                     id,
                     reference,
                     security_classification,
                     jurisdiction,
                     case_type_id,
                     state,
                     data
                 ) VALUES (?, ?, 'PUBLIC', 'EMPLOYMENT', 'ET_EnglandWales', 'Accepted', ?::jsonb)
                 """)) {
            statement.setLong(1, caseReference);
            statement.setLong(2, caseReference);
            statement.setString(3, objectMapper.writeValueAsString(caseData));
            statement.executeUpdate();
        }
    }

    private static HubLinksStatuses statuses(String personalDetails) {
        HubLinksStatuses statuses = new HubLinksStatuses();
        statuses.setPersonalDetails(personalDetails);
        return statuses;
    }

    private String storedStatus(long caseReference) {
        return repository.findById(caseReference).orElseThrow().getData().getPersonalDetails();
    }

    private static Connection connection() throws SQLException {
        return POSTGRES.createConnection("");
    }
}
