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
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.BundleDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.DigitalCaseFileType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.HubLinksStatuses;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.DigitalCaseFile;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.HubLinkStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.EtCosPostgresqlContainer;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.DigitalCaseFileRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.HubLinkStatusRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.NotificationViewRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_VIEWED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.VIEWED;

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
    private HubLinkStatusRepository repository;

    @Autowired
    private DigitalCaseFileRepository digitalCaseFileRepository;

    @Autowired
    private NotificationViewRepository notificationViewRepository;

    @Autowired
    private EntityManager entityManager;

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
        repository.saveAndFlush(HubLinkStatus.create(CASE_REFERENCE, statuses("table")));
        CaseData caseData = new CaseData();
        caseData.setHubLinksStatuses(statuses("blob"));

        CaseData result = caseView().getCase(
            new CaseViewRequest<>(CASE_REFERENCE, null),
            caseData
        );

        assertThat(result.getHubLinksStatuses().getPersonalDetails()).isEqualTo("table");
    }

    @Test
    void fallsBackToBlobDcfWhenNoTableRowExists() {
        CaseData caseData = new CaseData();
        DigitalCaseFileType blobDcf = dcf("blob.pdf");
        caseData.setDigitalCaseFile(blobDcf);
        List<Bundle> blobBundles = List.of(bundle(UUID.randomUUID()));
        caseData.setCaseBundles(blobBundles);

        CaseData result = caseView().getCase(
            new CaseViewRequest<>(CASE_REFERENCE, null), caseData);

        assertThat(result.getDigitalCaseFile()).isSameAs(blobDcf);
        assertThat(result.getCaseBundles()).isSameAs(blobBundles);
    }

    @Test
    void usesTableDcfInsteadOfBlobDcf() {
        DigitalCaseFileType tableDcf = dcf("table.pdf");
        digitalCaseFileRepository.saveAndFlush(DigitalCaseFile.create(CASE_REFERENCE, tableDcf, null));
        entityManager.clear();
        CaseData caseData = new CaseData();
        caseData.setDigitalCaseFile(dcf("blob.pdf"));
        caseData.setCaseBundles(List.of(bundle(UUID.randomUUID())));

        CaseData result = caseView().getCase(
            new CaseViewRequest<>(CASE_REFERENCE, null), caseData);

        assertThat(result.getDigitalCaseFile()).isEqualTo(tableDcf);
        assertThat(result.getCaseBundles()).isNull();
    }

    @Test
    void projectsThePendingBundleInsteadOfStaleBlobBundles() {
        UUID pendingBundleId = UUID.randomUUID();
        DigitalCaseFileType tableDcf = dcf("previous.pdf");
        tableDcf.setStatus("DCF Updating");
        digitalCaseFileRepository.saveAndFlush(DigitalCaseFile.create(CASE_REFERENCE, tableDcf, pendingBundleId));
        entityManager.clear();
        CaseData caseData = new CaseData();
        caseData.setCaseBundles(List.of(bundle(UUID.randomUUID())));

        CaseData result = caseView().getCase(
            new CaseViewRequest<>(CASE_REFERENCE, null), caseData);

        assertThat(result.getDigitalCaseFile()).isEqualTo(tableDcf);
        assertThat(result.getCaseBundles()).singleElement().satisfies(bundle -> {
            assertThat(bundle.value().getId()).isEqualTo(pendingBundleId.toString());
            assertThat(bundle.value().getStitchStatus()).isEqualTo("IN_PROGRESS");
            assertThat(bundle.value().getDocuments()).isNull();
        });
    }

    @Test
    void doesNotResurrectRemovedDcfFromBlob() {
        digitalCaseFileRepository.saveAndFlush(DigitalCaseFile.create(CASE_REFERENCE, null, null));
        entityManager.clear();
        CaseData caseData = new CaseData();
        caseData.setDigitalCaseFile(dcf("removed.pdf"));
        caseData.setCaseBundles(List.of(bundle(UUID.randomUUID())));

        CaseData result = caseView().getCase(
            new CaseViewRequest<>(CASE_REFERENCE, null), caseData);

        assertThat(result.getDigitalCaseFile()).isNull();
        assertThat(result.getCaseBundles()).isNull();
    }

    @Test
    void appliesViewsRecordedInTheTable() {
        jdbc.update("insert into public.notification_view (case_reference, item_id) values (?, 'notification')",
            CASE_REFERENCE);
        CaseData caseData = new CaseData();
        caseData.setSendNotificationCollection(List.of(SendNotificationTypeItem.builder()
            .id("notification")
            .value(SendNotificationType.builder().notificationState(NOT_VIEWED_YET).build())
            .build()));

        CaseData result = caseView().getCase(new CaseViewRequest<>(CASE_REFERENCE, null), caseData);

        assertThat(result.getSendNotificationCollection().getFirst().getValue().getNotificationState())
            .isEqualTo(VIEWED);
    }

    private ETCaseView caseView() {
        return new ETCaseView(repository, digitalCaseFileRepository, notificationViewRepository);
    }

    private DigitalCaseFileType dcf(String filename) {
        DigitalCaseFileType dcf = new DigitalCaseFileType();
        dcf.setUploadedDocument(UploadedDocumentType.builder().documentFilename(filename).build());
        return dcf;
    }

    private Bundle bundle(UUID id) {
        return Bundle.builder().id(UUID.randomUUID().toString())
            .value(BundleDetails.builder().id(id.toString()).build()).build();
    }

    private HubLinksStatuses statuses(String personalDetails) {
        HubLinksStatuses statuses = new HubLinksStatuses();
        statuses.setPersonalDetails(personalDetails);
        return statuses;
    }
}
