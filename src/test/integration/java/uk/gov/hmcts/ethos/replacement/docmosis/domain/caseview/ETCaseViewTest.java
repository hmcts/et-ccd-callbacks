package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.ccd.sdk.config.DecentralisedDataConfiguration;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.HubLinksStatuses;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.HubLinkStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.EtCosPostgresqlContainer;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.HubLinkStatusRepository;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@DataJpaTest(properties = "core_case_data.api.url=localhost:4452")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({DecentralisedDataConfiguration.class, ETCaseView.class})
class ETCaseViewTest {

    private static final long CASE_REFERENCE = 1_234_567_890_123_456L;
    private static final PostgreSQLContainer<?> POSTGRES = EtCosPostgresqlContainer.getInstance();

    static {
        POSTGRES.start();
    }

    @Autowired
    private ETCaseView caseView;

    @Autowired
    private HubLinkStatusRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        jdbc.update("delete from ccd.case_data where reference = ?", CASE_REFERENCE);
        jdbc.update("""
            insert into ccd.case_data (
                id,
                reference,
                security_classification,
                jurisdiction,
                case_type_id,
                state,
                data
            ) values (?, ?, 'PUBLIC', 'EMPLOYMENT', 'ET_EnglandWales', 'Accepted', '{}'::jsonb)
            """, CASE_REFERENCE, CASE_REFERENCE);
    }

    @Test
    void returnsConfiguredCaseTypes() {
        assertThat(caseView.caseTypeIds())
            .containsExactlyInAnyOrderElementsOf(Set.of(ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID));
    }

    @Test
    void readsHubLinkStatusesFromDedicatedTable() {
        HubLinksStatuses storedStatuses = statuses("completed");
        repository.saveAndFlush(HubLinkStatus.create(CASE_REFERENCE, storedStatuses));
        entityManager.clear();
        CaseData caseData = new CaseData();

        CaseData result = caseView.getCase(new CaseViewRequest<>(CASE_REFERENCE, CaseState.Accepted), caseData);

        assertThat(result).isSameAs(caseData);
        assertThat(result.getHubLinksStatuses().getPersonalDetails()).isEqualTo("completed");
    }

    @Test
    void clearsBlobHubLinkStatusesWhenDedicatedTableHasNoRow() {
        CaseData caseData = new CaseData();
        caseData.setHubLinksStatuses(statuses("stale"));

        CaseData result = caseView.getCase(new CaseViewRequest<>(CASE_REFERENCE, CaseState.Accepted), caseData);

        assertThat(result).isSameAs(caseData);
        assertThat(result.getHubLinksStatuses()).isNull();
    }

    private static HubLinksStatuses statuses(String personalDetails) {
        HubLinksStatuses statuses = new HubLinksStatuses();
        statuses.setPersonalDetails(personalDetails);
        return statuses;
    }
}
