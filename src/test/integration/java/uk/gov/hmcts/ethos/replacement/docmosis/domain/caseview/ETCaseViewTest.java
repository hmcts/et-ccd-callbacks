package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview;

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
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.BundleDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.DigitalCaseFileType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.HubLinksStatuses;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.DigitalCaseFile;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.HubLinkStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.EtCosPostgresqlContainer;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.DigitalCaseFileRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.HubLinkStatusRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@DataJpaTest(properties = "core_case_data.api.url=localhost:4452")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(DecentralisedDataConfiguration.class)
class ETCaseViewTest {

    private static final long CASE_REFERENCE = 1234567890123456L;
    private static final PostgreSQLContainer<?> POSTGRES = EtCosPostgresqlContainer.getInstance();

    static {
        POSTGRES.start();
    }

    @Autowired
    private HubLinkStatusRepository hubLinkStatusRepository;

    @Autowired
    private DigitalCaseFileRepository digitalCaseFileRepository;

    @Autowired
    private JdbcTemplate jdbc;

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
    void returnsConfiguredCaseTypesAndFallsBackToBlobStatus() {
        CaseData caseData = new CaseData();
        caseData.setHubLinksStatuses(statuses("blob"));

        ETCaseView caseView = caseView();
        CaseData result = caseView.getCase(new CaseViewRequest<>(CASE_REFERENCE, null), caseData);

        assertThat(caseView.caseTypeIds())
            .containsExactlyInAnyOrder(ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID);
        assertThat(result).isSameAs(caseData);
        assertThat(result.getHubLinksStatuses().getPersonalDetails()).isEqualTo("blob");
    }

    @Test
    void usesTableStatusWhenTableRowExists() {
        hubLinkStatusRepository.saveAndFlush(HubLinkStatus.create(CASE_REFERENCE, statuses("table")));
        CaseData caseData = new CaseData();
        caseData.setHubLinksStatuses(statuses("blob"));

        CaseData result = caseView().getCase(
            new CaseViewRequest<>(CASE_REFERENCE, null),
            caseData
        );

        assertThat(result.getHubLinksStatuses().getPersonalDetails()).isEqualTo("table");
    }

    @Test
    void fallsBackToBlobDigitalCaseFileWhenTableRowDoesNotExist() {
        CaseData caseData = new CaseData();
        caseData.setDigitalCaseFile(digitalCaseFile("blob"));

        CaseData result = caseView().getCase(new CaseViewRequest<>(CASE_REFERENCE, null), caseData);

        assertThat(result.getDigitalCaseFile().getStatus()).isEqualTo("blob");
    }

    @Test
    void usesTableDigitalCaseFileWhenTableRowExists() {
        digitalCaseFileRepository.saveAndFlush(
            DigitalCaseFile.create(CASE_REFERENCE, digitalCaseFile("table"), null, null)
        );
        CaseData caseData = new CaseData();
        caseData.setDigitalCaseFile(digitalCaseFile("blob"));

        CaseData result = caseView().getCase(new CaseViewRequest<>(CASE_REFERENCE, null), caseData);

        assertThat(result.getDigitalCaseFile().getStatus()).isEqualTo("table");
    }

    @Test
    void clearsBlobDigitalCaseFileWhenTableContainsRemovalTombstone() {
        digitalCaseFileRepository.saveAndFlush(DigitalCaseFile.create(CASE_REFERENCE, null, null, null));
        CaseData caseData = new CaseData();
        caseData.setDigitalCaseFile(digitalCaseFile("blob"));

        CaseData result = caseView().getCase(new CaseViewRequest<>(CASE_REFERENCE, null), caseData);

        assertThat(result.getDigitalCaseFile()).isNull();
    }

    @Test
    void hidesCompletedBundleFromCaseView() {
        UUID bundleId = UUID.randomUUID();
        digitalCaseFileRepository.saveAndFlush(
            DigitalCaseFile.create(CASE_REFERENCE, digitalCaseFile("generated"), null, bundleId)
        );
        CaseData caseData = caseDataWithBundle(bundleId);

        CaseData result = caseView().getCase(new CaseViewRequest<>(CASE_REFERENCE, null), caseData);

        assertThat(result.getDigitalCaseFile().getStatus()).isEqualTo("generated");
        assertThat(result.getCaseBundles()).isNull();
    }

    @Test
    void keepsPendingBundleVisibleAfterStartingANewGeneration() {
        UUID bundleId = UUID.randomUUID();
        digitalCaseFileRepository.saveAndFlush(
            DigitalCaseFile.create(CASE_REFERENCE, digitalCaseFile("generated"), null, UUID.randomUUID())
        );
        digitalCaseFileRepository.saveAndFlush(
            DigitalCaseFile.create(CASE_REFERENCE, digitalCaseFile("updating"), bundleId, null)
        );
        CaseData caseData = caseDataWithBundle(bundleId);
        List<Bundle> pendingBundles = caseData.getCaseBundles();

        CaseData result = caseView().getCase(new CaseViewRequest<>(CASE_REFERENCE, null), caseData);

        assertThat(result.getDigitalCaseFile().getStatus()).isEqualTo("updating");
        assertThat(result.getCaseBundles()).isEqualTo(pendingBundles);
    }

    @Test
    void hidesStaleBundleAfterUploadClearsActiveBundle() {
        UUID bundleId = UUID.randomUUID();
        digitalCaseFileRepository.saveAndFlush(
            DigitalCaseFile.create(CASE_REFERENCE, digitalCaseFile("uploaded"), null, null)
        );
        CaseData caseData = caseDataWithBundle(bundleId);

        CaseData result = caseView().getCase(new CaseViewRequest<>(CASE_REFERENCE, null), caseData);

        assertThat(result.getDigitalCaseFile().getStatus()).isEqualTo("uploaded");
        assertThat(result.getCaseBundles()).isNull();
    }

    private CaseData caseDataWithBundle(UUID bundleId) {
        CaseData caseData = new CaseData();
        caseData.setCaseBundles(List.of(Bundle.builder()
            .id(UUID.randomUUID().toString())
            .value(BundleDetails.builder().id(bundleId.toString()).build())
            .build()));
        return caseData;
    }

    private ETCaseView caseView() {
        return new ETCaseView(hubLinkStatusRepository, digitalCaseFileRepository);
    }

    private DigitalCaseFileType digitalCaseFile(String status) {
        DigitalCaseFileType digitalCaseFile = new DigitalCaseFileType();
        digitalCaseFile.setStatus(status);
        return digitalCaseFile;
    }

    private HubLinksStatuses statuses(String personalDetails) {
        HubLinksStatuses statuses = new HubLinksStatuses();
        statuses.setPersonalDetails(personalDetails);
        return statuses;
    }
}
