package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.ccd.sdk.config.DecentralisedDataConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.EtCosPostgresqlContainer;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@SpringBootTest(
    classes = EtRetainAndDisposePolicyTest.TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
class EtRetainAndDisposePolicyTest {

    private static final EtCosPostgresqlContainer POSTGRES = EtCosPostgresqlContainer.getInstance();

    static {
        POSTGRES.start();
    }

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    @Autowired
    private EtRetainAndDisposePolicy policy;

    @SpringBootConfiguration
    @Import({DecentralisedDataConfiguration.class, EtRetainAndDisposePolicy.class})
    @ImportAutoConfiguration({
        DataSourceAutoConfiguration.class,
        FlywayAutoConfiguration.class,
        JdbcTemplateAutoConfiguration.class
    })
    static class TestApplication {
    }

    @BeforeEach
    void setUp() {
        jdbc.getJdbcTemplate().execute("truncate table ccd.case_data cascade");
    }

    @Test
    void describesTheSharedEnglandWalesAndScotlandDraftPolicy() {
        assertThat(policy.caseTypes()).isEqualTo(Set.of(
            ENGLANDWALES_CASE_TYPE_ID,
            SCOTLAND_CASE_TYPE_ID
        ));
        assertThat(policy.terminalState()).isEqualTo("Deleting");
        assertThat(policy.terminalEvent()).isEqualTo("MarkForDisposal");
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

        assertThat(policy.findCandidates()).containsExactly(1L, 2L, 6L);
    }

    @Test
    void ordersTheCompleteCandidateList() {
        insertCase(3, ENGLANDWALES_CASE_TYPE_ID, "AWAITING_SUBMISSION_TO_HMCTS", 500);
        insertCase(1, ENGLANDWALES_CASE_TYPE_ID, "AWAITING_SUBMISSION_TO_HMCTS", 500);
        insertCase(2, SCOTLAND_CASE_TYPE_ID, "AWAITING_SUBMISSION_TO_HMCTS", 500);

        assertThat(policy.findCandidates()).containsExactly(1L, 2L, 3L);
    }

    private void insertCase(long reference, String caseType, String state, int ageInDays) {
        jdbc.update(
            """
            insert into ccd.case_data (
                id,
                reference,
                created_date,
                security_classification,
                jurisdiction,
                case_type_id,
                state,
                data
            ) values (
                :reference,
                :reference,
                current_timestamp - make_interval(days => :ageInDays),
                'PUBLIC',
                'EMPLOYMENT',
                :caseType,
                :state,
                '{}'::jsonb
            )
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
