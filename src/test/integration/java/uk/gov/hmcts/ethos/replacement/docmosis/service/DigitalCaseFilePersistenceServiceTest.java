package uk.gov.hmcts.ethos.replacement.docmosis.service;

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
import uk.gov.hmcts.ccd.sdk.config.DecentralisedDataConfiguration;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.BundleDetails;
import uk.gov.hmcts.et.common.model.bundle.DocumentLink;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.DigitalCaseFileType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.EtCosPostgresqlContainer;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.DigitalCaseFileRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "core_case_data.api.url=localhost:4452")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(DecentralisedDataConfiguration.class)
class DigitalCaseFilePersistenceServiceTest {

    private static final long CASE_REFERENCE = 1234567890123456L;
    private static final PostgreSQLContainer<?> POSTGRES = EtCosPostgresqlContainer.getInstance();

    static {
        POSTGRES.start();
    }

    @Autowired
    private DigitalCaseFileRepository digitalCaseFileRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbc;

    private DigitalCaseFilePersistenceService service;

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
        service = new DigitalCaseFilePersistenceService(digitalCaseFileRepository);
    }

    @Test
    void dualWritesAndReplacesTheCurrentBundle() {
        DigitalCaseFileType updating = digitalCaseFile("DCF Updating");
        Bundle firstBundle = bundle();
        start(updating, firstBundle);
        flushAndClear();

        var storedDcf = digitalCaseFileRepository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getData().getStatus()).isEqualTo("DCF Updating");
        assertThat(storedDcf.getActiveBundleId()).hasToString(firstBundle.value().getId());

        Bundle replacementBundle = bundle();
        start(digitalCaseFile("DCF Updating again"), replacementBundle);
        flushAndClear();

        storedDcf = digitalCaseFileRepository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getData().getStatus()).isEqualTo("DCF Updating again");
        assertThat(storedDcf.getActiveBundleId()).hasToString(replacementBundle.value().getId());
    }

    @Test
    void storesSuccessfulCompletionWithItsDocument() {
        Bundle pendingBundle = bundle();
        Bundle completedBundle = successfullyCompletedBundle(pendingBundle);
        start(digitalCaseFile("DCF Updating"), pendingBundle);
        CaseData caseData = new CaseData();
        caseData.setCaseBundles(List.of(completedBundle));

        service.complete(CASE_REFERENCE, caseData);
        flushAndClear();

        var storedDcf = digitalCaseFileRepository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getData().getStatus()).startsWith("DCF Generated:");
        assertThat(storedDcf.getData().getUploadedDocument())
            .extracting("documentUrl", "documentBinaryUrl", "documentFilename")
            .containsExactly(
                "http://documents.example/documents/generated",
                "http://documents.example/documents/generated/binary",
                "generated.pdf"
            );
        assertThat(storedDcf.getActiveBundleId()).isNull();
        assertThat(storedDcf.getCompletedBundleId()).hasToString(pendingBundle.value().getId());
    }

    @Test
    void completesAReplacementStartedByAnOlderInstanceDespiteTheStaleTableBundle() {
        Bundle previousBundle = bundle();
        start(digitalCaseFile("DCF Updating previous job"), previousBundle);
        flushAndClear();

        Bundle replacementBundle = bundle();
        CaseData caseData = new CaseData();
        caseData.setCaseBundles(List.of(completedBundle(replacementBundle)));

        service.complete(CASE_REFERENCE, caseData);
        flushAndClear();

        var storedDcf = digitalCaseFileRepository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getActiveBundleId()).isNull();
        assertThat(storedDcf.getCompletedBundleId()).hasToString(replacementBundle.value().getId());
        assertThat(storedDcf.getData()).isEqualTo(caseData.getDigitalCaseFile());
        assertThat(caseData.getDigitalCaseFile().getStatus()).startsWith("DCF Failed to generate:");
    }

    @Test
    void completionRetryStillPopulatesTheBlobResponse() {
        Bundle pendingBundle = bundle();
        Bundle completedBundle = completedBundle(pendingBundle);
        start(digitalCaseFile("DCF Updating"), pendingBundle);
        CaseData caseData = new CaseData();
        caseData.setCaseBundles(List.of(completedBundle));
        service.complete(CASE_REFERENCE, caseData);
        flushAndClear();

        CaseData retryData = new CaseData();
        retryData.setDigitalCaseFile(digitalCaseFile("DCF Updating"));
        retryData.setCaseBundles(List.of(completedBundle));
        service.complete(CASE_REFERENCE, retryData);
        flushAndClear();

        assertThat(retryData.getDigitalCaseFile().getStatus()).startsWith("DCF Failed to generate:");
        var storedDcf = digitalCaseFileRepository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getActiveBundleId()).isNull();
        assertThat(storedDcf.getCompletedBundleId()).hasToString(pendingBundle.value().getId());
    }

    @Test
    void adoptsCompletionForAJobCreatedBeforeDualWriteDeployment() {
        Bundle completedBundle = completedBundle(bundle());
        CaseData caseData = new CaseData();
        caseData.setCaseBundles(List.of(completedBundle));

        service.complete(CASE_REFERENCE, caseData);
        flushAndClear();

        assertThat(digitalCaseFileRepository.findById(CASE_REFERENCE).orElseThrow().getCompletedBundleId())
            .hasToString(completedBundle.value().getId());
    }

    @Test
    void ignoresCaseDataWithoutACompletedBundle() {
        Bundle activeBundle = bundle();
        start(digitalCaseFile("DCF Updating"), activeBundle);
        CaseData caseData = new CaseData();
        caseData.setCaseBundles(List.of(activeBundle.toBuilder()
            .value(activeBundle.value().toBuilder().stitchStatus("IN_PROGRESS").build())
            .build()));

        service.complete(CASE_REFERENCE, caseData);
        flushAndClear();

        var storedDcf = digitalCaseFileRepository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getData().getStatus()).isEqualTo("DCF Updating");
        assertThat(storedDcf.getActiveBundleId()).hasToString(activeBundle.value().getId());
        assertThat(storedDcf.getCompletedBundleId()).isNull();
    }

    @Test
    void uploadReplacesTheTableDataAndClearsBundleIds() {
        Bundle activeBundle = bundle();
        start(digitalCaseFile("DCF Updating"), activeBundle);
        service.save(CASE_REFERENCE, digitalCaseFile("DCF Uploaded"));
        flushAndClear();

        var storedDcf = digitalCaseFileRepository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getData().getStatus()).isEqualTo("DCF Uploaded");
        assertThat(storedDcf.getActiveBundleId()).isNull();
        assertThat(storedDcf.getCompletedBundleId()).isNull();
    }

    @Test
    void removeStoresATombstoneAndClearsBundleIds() {
        Bundle activeBundle = bundle();
        start(digitalCaseFile("DCF Updating"), activeBundle);

        service.save(CASE_REFERENCE, null);
        flushAndClear();

        var storedDcf = digitalCaseFileRepository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getData()).isNull();
        assertThat(storedDcf.getActiveBundleId()).isNull();
        assertThat(storedDcf.getCompletedBundleId()).isNull();
    }

    @Test
    void completesAJobStartedByAnOlderInstanceAfterATableRemoval() {
        service.save(CASE_REFERENCE, null);
        flushAndClear();
        Bundle completedBundle = completedBundle(bundle());
        CaseData caseData = new CaseData();
        caseData.setCaseBundles(List.of(completedBundle));

        service.complete(CASE_REFERENCE, caseData);
        flushAndClear();

        var storedDcf = digitalCaseFileRepository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getCompletedBundleId()).hasToString(completedBundle.value().getId());
        assertThat(storedDcf.getData()).isEqualTo(caseData.getDigitalCaseFile());
        assertThat(caseData.getDigitalCaseFile().getStatus()).startsWith("DCF Failed to generate:");
    }

    private DigitalCaseFileType digitalCaseFile(String status) {
        DigitalCaseFileType digitalCaseFile = new DigitalCaseFileType();
        digitalCaseFile.setStatus(status);
        return digitalCaseFile;
    }

    private void start(DigitalCaseFileType digitalCaseFile, Bundle bundle) {
        CaseData caseData = new CaseData();
        caseData.setDigitalCaseFile(digitalCaseFile);
        caseData.setCaseBundles(List.of(bundle));
        service.start(CASE_REFERENCE, caseData);
    }

    private Bundle bundle() {
        return Bundle.builder()
            .id(UUID.randomUUID().toString())
            .value(BundleDetails.builder().id(UUID.randomUUID().toString()).build())
            .build();
    }

    private Bundle completedBundle(Bundle pendingBundle) {
        return pendingBundle.toBuilder()
            .value(pendingBundle.value().toBuilder()
                .stitchStatus("FAILED")
                .stitchingFailureMessage("Failed to generate")
                .build())
            .build();
    }

    private Bundle successfullyCompletedBundle(Bundle pendingBundle) {
        return pendingBundle.toBuilder()
            .value(pendingBundle.value().toBuilder()
                .stitchStatus("DONE")
                .stitchedDocument(DocumentLink.builder()
                    .documentUrl("http://documents.example/documents/generated")
                    .documentBinaryUrl("http://documents.example/documents/generated/binary")
                    .documentFilename("generated.pdf")
                    .build())
                .build())
            .build();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
