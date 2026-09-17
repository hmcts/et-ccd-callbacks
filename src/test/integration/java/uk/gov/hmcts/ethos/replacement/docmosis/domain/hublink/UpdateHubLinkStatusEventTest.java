package uk.gov.hmcts.ethos.replacement.docmosis.domain.hublink;

import com.google.common.collect.ImmutableSet;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.ResolvedCCDConfig;
import uk.gov.hmcts.ccd.sdk.api.EventPayload;
import uk.gov.hmcts.ccd.sdk.config.DecentralisedDataConfiguration;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.HubLinksStatuses;
import uk.gov.hmcts.ethos.replacement.docmosis.config.EtJsonCcdConfig.PlaceholderRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.HubLinkStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.EtCosPostgresqlContainer;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.HubLinkStatusRepository;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "core_case_data.api.url=localhost:4452")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(DecentralisedDataConfiguration.class)
class UpdateHubLinkStatusEventTest {

    private static final long CASE_REFERENCE = 1234567890123456L;
    private static final PostgreSQLContainer<?> POSTGRES = EtCosPostgresqlContainer.getInstance();

    static {
        POSTGRES.start();
    }

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
    void eventInsertsThenUpdatesStatus() {
        UpdateHubLinkStatusEvent eventConfig = new UpdateHubLinkStatusEvent(repository);
        ResolvedCCDConfig<CaseData, CaseState, PlaceholderRole> resolvedConfig = new ResolvedCCDConfig<>(
            CaseData.class,
            CaseState.class,
            PlaceholderRole.class,
            Map.of(),
            ImmutableSet.copyOf(CaseState.values())
        );
        ConfigBuilderImpl<CaseData, CaseState, PlaceholderRole> builder = new ConfigBuilderImpl<>(resolvedConfig);
        eventConfig.configureDecentralised(builder);
        var event = builder.build()
            .getEvents()
            .get(UpdateHubLinkStatusEvent.EVENT_ID);

        CaseData caseData = new CaseData();
        caseData.setHubLinksStatuses(statuses("notStarted"));
        var response = event.getSubmitHandler().submit(eventPayload(caseData));

        assertThat(eventConfig.caseTypeIds()).containsExactlyInAnyOrder("ET_EnglandWales", "ET_Scotland");
        assertThat(event.getAboutToSubmitCallback()).isNull();
        assertThat(response).isNotNull();
        flushAndClear();
        assertStoredStatus("notStarted");

        caseData.setHubLinksStatuses(statuses("completed"));
        event.getSubmitHandler().submit(eventPayload(caseData));
        flushAndClear();
        assertStoredStatus("completed");
    }

    private EventPayload<CaseData, CaseState> eventPayload(CaseData caseData) {
        return new EventPayload<>(CASE_REFERENCE, caseData, null);
    }

    private HubLinksStatuses statuses(String personalDetails) {
        HubLinksStatuses statuses = new HubLinksStatuses();
        statuses.setPersonalDetails(personalDetails);
        return statuses;
    }

    private void assertStoredStatus(String personalDetails) {
        HubLinkStatus stored = repository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(stored.getData().getPersonalDetails()).isEqualTo(personalDetails);
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
