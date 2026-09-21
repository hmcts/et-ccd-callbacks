package uk.gov.hmcts.ethos.replacement.docmosis.service;

import jakarta.persistence.EntityManager;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.ccd.sdk.config.DecentralisedDataConfiguration;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.BundleDetails;
import uk.gov.hmcts.et.common.model.bundle.DocumentLink;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DigitalCaseFileType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.EtCosPostgresqlContainer;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.DigitalCaseFileRepository;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@DataJpaTest(properties = "core_case_data.api.url=localhost:4452")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({DecentralisedDataConfiguration.class, DigitalCaseFileService.class,
    DigitalCaseFilePersistenceService.class, DigitalCaseFileServiceIntegrationTest.TestConfig.class})
@ImportAutoConfiguration({FeignAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class})
class DigitalCaseFileServiceIntegrationTest {

    private static final long CASE_REFERENCE = 1_234_567_890_123_456L;
    private static final PostgreSQLContainer<?> POSTGRES = EtCosPostgresqlContainer.getInstance();
    private static final MockWebServer STITCHING = new MockWebServer();

    static {
        POSTGRES.start();
    }

    @Autowired
    private DigitalCaseFileService service;

    @Autowired
    private DigitalCaseFileRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbc;

    @DynamicPropertySource
    static void stitchingUrl(DynamicPropertyRegistry registry) {
        registry.add("em-ccd-orchestrator.api.url", () -> STITCHING.url("/").toString());
    }

    @AfterAll
    static void stopStitchingServer() throws IOException {
        STITCHING.shutdown();
    }

    @BeforeEach
    void setUp() {
        jdbc.update("delete from ccd.case_data where reference = ?", CASE_REFERENCE);
        jdbc.update("""
            insert into ccd.case_data (
                id, reference, security_classification, jurisdiction, case_type_id, state, data
            ) values (?, ?, 'PUBLIC', 'EMPLOYMENT', 'ET_EnglandWales', 'Accepted', '{}'::jsonb)
            """, CASE_REFERENCE, CASE_REFERENCE);
    }

    @Test
    void createsTheCurrentBundle() throws InterruptedException {
        CaseData caseData = start();
        final Bundle firstBundle = caseData.getCaseBundles().getFirst();
        flushAndClear();

        var storedDcf = repository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getData()).isEqualTo(caseData.getDigitalCaseFile());
        assertThat(storedDcf.getData().getStatus()).startsWith("DCF Updating:");
        assertThat(storedDcf.getActiveBundleId()).hasToString(firstBundle.value().getId());
        assertThat(UUID.fromString(firstBundle.id())).isNotEqualTo(storedDcf.getActiveBundleId());
        assertThat(firstBundle.value().getDocuments()).hasSize(1);
        assertThat(UUID.fromString(firstBundle.value().getDocuments().getFirst().id()))
            .isNotEqualTo(UUID.fromString(firstBundle.id()));
        assertThat(firstBundle.value().getDocuments().getFirst().value().sourceDocument().getDocumentUrl())
            .isEqualTo(uploadedDocument().getDocumentUrl());

        assertThat(caseData.getUploadOrRemoveDcf()).isNull();
    }

    @Test
    void storesSuccessfulCompletionAndClearsResponseBundles() throws InterruptedException {
        CaseData caseData = start();
        Bundle pendingBundle = caseData.getCaseBundles().getFirst();
        caseData.setCaseBundles(List.of(completedBundle(pendingBundle, "DONE")));

        service.completeDcf(caseDetails(caseData));
        flushAndClear();

        var storedDcf = repository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getData().getStatus()).startsWith("DCF Generated:");
        assertThat(storedDcf.getData().getUploadedDocument())
            .extracting("documentUrl", "documentBinaryUrl", "documentFilename")
            .containsExactly("http://documents.example/documents/generated",
                "http://documents.example/documents/generated/binary", "generated.pdf");
        assertThat(storedDcf.getData()).isEqualTo(caseData.getDigitalCaseFile());
        assertThat(storedDcf.getActiveBundleId()).isNull();
        assertThat(caseData.getCaseBundles()).isNull();
    }

    @ParameterizedTest
    @EnumSource(ExistingTableState.class)
    void completesAJobStartedByAnOlderInstance(ExistingTableState tableState) throws InterruptedException {
        switch (tableState) {
            case ABSENT -> { }
            case PENDING -> start();
            case REMOVED -> {
                CaseData removed = new CaseData();
                removed.setUploadOrRemoveDcf("Remove");
                service.createUploadRemoveDcf("user-token", caseDetails(removed));
            }
            default -> throw new IllegalArgumentException("Unexpected table state: " + tableState);
        }
        flushAndClear();

        Bundle completed = completedBundle(bundle(), "FAILED");
        CaseData caseData = new CaseData();
        DigitalCaseFileType previousDcf = new DigitalCaseFileType();
        previousDcf.setUploadedDocument(uploadedDocument());
        caseData.setDigitalCaseFile(previousDcf);
        caseData.setCaseBundles(List.of(completed));

        service.completeDcf(caseDetails(caseData));
        flushAndClear();

        var storedDcf = repository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getData().getStatus()).startsWith("DCF Failed to generate:");
        assertThat(storedDcf.getData().getError()).isEqualTo("Failed to generate");
        assertThat(storedDcf.getData().getUploadedDocument()).isEqualTo(uploadedDocument());
        assertThat(storedDcf.getData()).isEqualTo(caseData.getDigitalCaseFile());
        assertThat(storedDcf.getActiveBundleId()).isNull();
        assertThat(caseData.getCaseBundles()).isNull();
    }

    @Test
    void completionRetryStillPopulatesTheResponse() throws InterruptedException {
        CaseData caseData = start();
        Bundle completed = completedBundle(caseData.getCaseBundles().getFirst(), "DONE");
        caseData.setCaseBundles(List.of(completed));
        service.completeDcf(caseDetails(caseData));
        flushAndClear();

        CaseData retryData = new CaseData();
        retryData.setCaseBundles(List.of(completed));
        service.completeDcf(caseDetails(retryData));
        flushAndClear();

        var storedDcf = repository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getData()).isEqualTo(retryData.getDigitalCaseFile());
        assertThat(retryData.getDigitalCaseFile().getStatus()).startsWith("DCF Generated:");
        assertThat(retryData.getDigitalCaseFile().getUploadedDocument().getDocumentFilename())
            .isEqualTo("generated.pdf");
        assertThat(storedDcf.getActiveBundleId()).isNull();
        assertThat(retryData.getCaseBundles()).isNull();
    }

    @Test
    void doesNotStoreAnUnfinishedBundleAsCompleted() throws InterruptedException {
        CaseData caseData = start();
        Bundle activeBundle = caseData.getCaseBundles().getFirst();
        caseData.setCaseBundles(List.of(activeBundle.toBuilder()
            .value(activeBundle.value().toBuilder().stitchStatus("IN_PROGRESS").build())
            .build()));

        service.completeDcf(caseDetails(caseData));
        flushAndClear();

        var storedDcf = repository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getData().getStatus()).startsWith("DCF Updating:");
        assertThat(storedDcf.getActiveBundleId()).hasToString(activeBundle.value().getId());
    }

    @Test
    void uploadUpdatesTheTableAndTheResponse() throws InterruptedException {
        CaseData caseData = start();
        caseData.getDigitalCaseFile().setUploadedDocument(uploadedDocument());
        caseData.getDigitalCaseFile().setError("Previous failure");
        caseData.getDigitalCaseFile().setDateGenerated("Previous date");
        caseData.setUploadOrRemoveDcf("Upload");

        service.createUploadRemoveDcf("user-token", caseDetails(caseData));
        flushAndClear();

        var storedDcf = repository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getData().getStatus()).startsWith("DCF Uploaded:");
        assertThat(storedDcf.getData().getUploadedDocument()).isEqualTo(uploadedDocument());
        assertThat(storedDcf.getData().getError()).isNull();
        assertThat(storedDcf.getData().getDateGenerated()).isNull();
        assertThat(storedDcf.getData()).isEqualTo(caseData.getDigitalCaseFile());
        assertThat(storedDcf.getActiveBundleId()).isNull();
        assertThat(caseData.getUploadOrRemoveDcf()).isNull();
    }

    @Test
    void removeStoresATombstoneAndClearsTheResponseDcf() throws InterruptedException {
        CaseData caseData = start();
        caseData.setUploadOrRemoveDcf("Remove");

        service.createUploadRemoveDcf("user-token", caseDetails(caseData));
        flushAndClear();

        var storedDcf = repository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getData()).isNull();
        assertThat(storedDcf.getActiveBundleId()).isNull();
        assertThat(caseData.getDigitalCaseFile()).isNull();
        assertThat(caseData.getUploadOrRemoveDcf()).isNull();
    }

    private CaseData start() throws InterruptedException {
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("123456/2021");
        DocumentTypeItem document = new DocumentTypeItem();
        document.setId(UUID.randomUUID().toString());
        document.setValue(DocumentType.builder().uploadedDocument(uploadedDocument()).build());
        DocumentTypeItem excludedDocument = new DocumentTypeItem();
        excludedDocument.setId(UUID.randomUUID().toString());
        excludedDocument.setValue(DocumentType.builder().uploadedDocument(uploadedDocument())
            .excludeFromDcf(List.of(YES)).build());
        caseData.setDocumentCollection(List.of(document, excludedDocument));
        caseData.setUploadOrRemoveDcf("Create");
        STITCHING.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody("{}"));

        service.createUploadRemoveDcf("user-token", caseDetails(caseData));

        var request = STITCHING.takeRequest(5, TimeUnit.SECONDS);
        assertThat(request).isNotNull();
        assertThat(request.getPath()).isEqualTo("/api/async-stitch-ccd-bundles");
        assertThat(request.getHeader("Authorization")).isEqualTo("user-token");
        assertThat(request.getHeader("ServiceAuthorization")).isEqualTo("service-token");
        assertThat(request.getBody().readUtf8()).contains(caseData.getCaseBundles().getFirst().value().getId());
        return caseData;
    }

    private CaseDetails caseDetails(CaseData caseData) {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(Long.toString(CASE_REFERENCE));
        caseDetails.setCaseTypeId("ET_EnglandWales");
        caseDetails.setCaseData(caseData);
        return caseDetails;
    }

    private UploadedDocumentType uploadedDocument() {
        return UploadedDocumentType.builder()
            .documentUrl("http://documents.example/documents/uploaded")
            .documentBinaryUrl("http://documents.example/documents/uploaded/binary")
            .documentFilename("uploaded.pdf")
            .build();
    }

    private Bundle bundle() {
        return Bundle.builder().id(UUID.randomUUID().toString())
            .value(BundleDetails.builder().id(UUID.randomUUID().toString()).build()).build();
    }

    private Bundle completedBundle(Bundle pendingBundle, String stitchStatus) {
        return pendingBundle.toBuilder()
            .value(pendingBundle.value().toBuilder()
                .stitchStatus(stitchStatus)
                .stitchingFailureMessage("Failed to generate")
                .stitchedDocument(DocumentLink.builder()
                    .documentUrl("http://documents.example/documents/generated")
                    .documentBinaryUrl("http://documents.example/documents/generated/binary")
                    .documentFilename("generated.pdf").build())
                .build())
            .build();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private enum ExistingTableState { ABSENT, PENDING, REMOVED }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestConfig {
        @Bean
        AuthTokenGenerator authTokenGenerator() {
            return () -> "service-token";
        }
    }
}
