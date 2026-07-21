package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

class EtRetainAndDisposeConfigTest {

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName
        .parse("hmctspublic.azurecr.io/imported/postgres:16-alpine")
        .asCompatibleSubstituteFor("postgres");
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(POSTGRES_IMAGE);

    private static NamedParameterJdbcTemplate jdbc;
    private EtRetainAndDisposeConfig config;

    @BeforeAll
    static void startPostgres() {
        POSTGRES.start();
        jdbc = new NamedParameterJdbcTemplate(new DriverManagerDataSource(
            POSTGRES.getJdbcUrl(),
            POSTGRES.getUsername(),
            POSTGRES.getPassword()
        ));
        jdbc.getJdbcTemplate().execute("create schema ccd");
        jdbc.getJdbcTemplate().execute("""
            create table ccd.case_data (
                reference bigint primary key,
                created_date timestamp without time zone not null,
                case_type_id text not null,
                state text not null
            )
            """);
    }

    @AfterAll
    static void stopPostgres() {
        POSTGRES.stop();
    }

    @BeforeEach
    void setUp() {
        jdbc.getJdbcTemplate().execute("truncate table ccd.case_data");
        config = new EtRetainAndDisposeConfig(jdbc);
    }

    @Test
    void describesTheSharedEnglandWalesAndScotlandDraftPolicy() {
        assertThat(config.caseTypes()).isEqualTo(Set.of(
            ENGLANDWALES_CASE_TYPE_ID,
            SCOTLAND_CASE_TYPE_ID
        ));
        assertThat(config.terminalState()).isEqualTo("Deleting");
        assertThat(config.terminalEvent()).isEqualTo("MarkForDisposal");
    }

    @Test
    void findsDeletedCasesImmediatelyAndDraftsPastTheExistingRetentionPeriod() {
        insertCase(1, ENGLANDWALES_CASE_TYPE_ID, "AWAITING_SUBMISSION_TO_HMCTS", 366);
        insertCase(2, SCOTLAND_CASE_TYPE_ID, "AWAITING_SUBMISSION_TO_HMCTS", 500);
        insertCase(3, ENGLANDWALES_CASE_TYPE_ID, "AWAITING_SUBMISSION_TO_HMCTS", 365);
        insertCase(4, ENGLANDWALES_CASE_TYPE_ID, "Submitted", 500);
        insertCase(5, "ET_Admin", "AWAITING_SUBMISSION_TO_HMCTS", 500);
        insertCase(6, SCOTLAND_CASE_TYPE_ID, "Delete", 0);
        insertCase(7, "ET_Admin", "Delete", 0);

        assertThat(config.findCandidates(10)).containsExactly(1L, 2L, 6L);
    }

    @Test
    void boundsAndOrdersTheCandidateList() {
        insertCase(3, ENGLANDWALES_CASE_TYPE_ID, "AWAITING_SUBMISSION_TO_HMCTS", 500);
        insertCase(1, ENGLANDWALES_CASE_TYPE_ID, "AWAITING_SUBMISSION_TO_HMCTS", 500);
        insertCase(2, SCOTLAND_CASE_TYPE_ID, "AWAITING_SUBMISSION_TO_HMCTS", 500);

        assertThat(config.findCandidates(2)).containsExactly(1L, 2L);
    }

    private void insertCase(long reference, String caseType, String state, int ageInDays) {
        jdbc.update(
            """
            insert into ccd.case_data (reference, created_date, case_type_id, state)
            values (:reference, current_timestamp - make_interval(days => :ageInDays), :caseType, :state)
            """,
            Map.of(
                "reference", reference,
                "caseType", caseType,
                "state", state,
                "ageInDays", ageInDays
            )
        );
    }
}
