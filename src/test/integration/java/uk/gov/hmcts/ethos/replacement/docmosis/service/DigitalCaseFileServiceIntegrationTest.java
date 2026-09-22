package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.google.common.collect.ImmutableSet;
import jakarta.persistence.EntityManager;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
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
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.ResolvedCCDConfig;
import uk.gov.hmcts.ccd.sdk.api.EventPayload;
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
import uk.gov.hmcts.ethos.replacement.docmosis.config.EtJsonCcdConfig;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.ETCaseView;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.digitalcasefile.AsyncStitchingCompleteEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.EtCosPostgresqlContainer;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.DigitalCaseFileRepository;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@DataJpaTest(properties = "core_case_data.api.url=localhost:4452")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({DecentralisedDataConfiguration.class, DigitalCaseFileService.class,
    DigitalCaseFilePersistenceService.class, ETCaseView.class,
    DigitalCaseFileServiceIntegrationTest.TestConfig.class})
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
    private ETCaseView caseView;

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
        assertThat(storedDcf.getPendingBundleId()).hasToString(firstBundle.value().getId());
        assertThat(UUID.fromString(firstBundle.id())).isNotEqualTo(storedDcf.getPendingBundleId());
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
        assertThat(storedDcf.getPendingBundleId()).isNull();
        assertThat(caseData.getCaseBundles()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"DONE", "FAILED"})
    void completesUsingOnlyTheProjectedPendingBundle(String status) throws InterruptedException {
        CaseData started = start();
        String pendingId = started.getCaseBundles().getFirst().value().getId();
        flushAndClear();

        // EM starts asyncStitchingComplete using a projected case, matches value.id,
        // then adds the status and document from the stitching result.
        CaseData projected = caseView.getCase(new CaseViewRequest<>(CASE_REFERENCE, null), new CaseData());
        Bundle pending = projected.getCaseBundles().getFirst();
        assertThat(pending.value().getId()).isEqualTo(pendingId);
        assertThat(pending.value().getDocuments()).isNull();
        projected.setCaseBundles(List.of(completedBundle(pending, status)));

        service.completeDcf(caseDetails(projected));
        flushAndClear();

        var stored = repository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(stored.getData()).isEqualTo(projected.getDigitalCaseFile());
        assertThat(stored.getPendingBundleId()).isNull();
        assertThat(projected.getDigitalCaseFile().getStatus())
            .startsWith("DONE".equals(status) ? "DCF Generated:" : "DCF Failed to generate:");
        assertThat(projected.getCaseBundles()).isNull();
        CaseData refreshed = caseView.getCase(new CaseViewRequest<>(CASE_REFERENCE, null), started);
        assertThat(refreshed.getCaseBundles()).isNull();
        assertThat(refreshed.getDigitalCaseFile()).isEqualTo(stored.getData());
    }

    @ParameterizedTest
    @ValueSource(strings = {ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID})
    void decentralisedCompletionPersistsOnlyToTheDigitalCaseFileTable(String caseType) throws InterruptedException {
        jdbc.update("update ccd.case_data set case_type_id = ? where reference = ?", caseType, CASE_REFERENCE);
        final CaseData started = start();
        flushAndClear();

        CaseData projected = caseView.getCase(new CaseViewRequest<>(CASE_REFERENCE, null), new CaseData());
        projected.setCaseBundles(List.of(completedBundle(projected.getCaseBundles().getFirst(), "DONE")));

        AsyncStitchingCompleteEvent eventConfig = new AsyncStitchingCompleteEvent(service);
        ResolvedCCDConfig<CaseData, CaseState, EtJsonCcdConfig.PlaceholderRole> resolvedConfig =
            new ResolvedCCDConfig<>(CaseData.class, CaseState.class, EtJsonCcdConfig.PlaceholderRole.class,
                Map.of(), ImmutableSet.copyOf(CaseState.values()));
        ConfigBuilderImpl<CaseData, CaseState, EtJsonCcdConfig.PlaceholderRole> builder =
            new ConfigBuilderImpl<>(resolvedConfig);
        eventConfig.configureDecentralised(builder);
        var event = builder.build().getEvents().get(AsyncStitchingCompleteEvent.EVENT_ID);

        assertThat(eventConfig.caseTypeIds()).contains(caseType);
        assertThat(event.getAboutToSubmitCallback()).isNull();
        assertThat(event.getSubmitHandler().submit(new EventPayload<>(CASE_REFERENCE, projected, null))).isNotNull();
        flushAndClear();

        assertThat(repository.findById(CASE_REFERENCE).orElseThrow().getData().getStatus())
            .startsWith("DCF Generated:");
        assertThat(caseView.getCase(new CaseViewRequest<>(CASE_REFERENCE, null), started)
            .getDigitalCaseFile().getUploadedDocument().getDocumentFilename()).isEqualTo("generated.pdf");
        assertThat(jdbc.queryForObject("select data::text from ccd.case_data where reference = ?",
            String.class, CASE_REFERENCE)).isEqualTo("{}");
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
        assertThat(storedDcf.getPendingBundleId()).isNull();
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
        assertThat(storedDcf.getPendingBundleId()).isNull();
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
        assertThat(storedDcf.getPendingBundleId()).hasToString(activeBundle.value().getId());
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
        assertThat(storedDcf.getPendingBundleId()).isNull();
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
        assertThat(storedDcf.getPendingBundleId()).isNull();
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
