package uk.gov.hmcts.ethos.replacement.docmosis.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.ccd.sdk.config.DecentralisedDataConfiguration;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.BundleDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.DigitalCaseFileType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.EtCosPostgresqlContainer;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.DigitalCaseFileRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Autowired
    private PlatformTransactionManager transactionManager;

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
    void storesCompletionAndDeletesTheTemporaryBundle() {
        Bundle pendingBundle = bundle();
        Bundle completedBundle = completedBundle(pendingBundle);
        start(digitalCaseFile("DCF Updating"), pendingBundle);
        CaseData caseData = new CaseData();
        caseData.setCaseBundles(List.of(completedBundle));

        service.complete(CASE_REFERENCE, caseData);
        flushAndClear();

        var storedDcf = digitalCaseFileRepository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getData().getStatus()).startsWith("DCF Failed to generate:");
        assertThat(storedDcf.getActiveBundleId()).isNull();
        assertThat(storedDcf.getCompletedBundleId()).hasToString(pendingBundle.value().getId());
    }

    @Test
    void rejectsAStaleCompletionWithoutChangingTheCurrentBundle() {
        Bundle staleBundle = bundle();
        Bundle activeBundle = bundle();
        start(digitalCaseFile("DCF Updating A"), staleBundle);
        start(digitalCaseFile("DCF Updating B"), activeBundle);

        CaseData caseData = new CaseData();
        caseData.setCaseBundles(List.of(completedBundle(staleBundle)));

        assertThatThrownBy(() -> service.complete(CASE_REFERENCE, caseData))
            .isInstanceOfSatisfying(ResponseStatusException.class,
                exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        flushAndClear();

        var storedDcf = digitalCaseFileRepository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getActiveBundleId()).hasToString(activeBundle.value().getId());
        assertThat(storedDcf.getData().getStatus()).isEqualTo("DCF Updating B");
    }

    @Test
    void acceptsAnExactDuplicateCompletion() {
        Bundle pendingBundle = bundle();
        Bundle completedBundle = completedBundle(pendingBundle);
        start(digitalCaseFile("DCF Updating"), pendingBundle);
        CaseData caseData = new CaseData();
        caseData.setCaseBundles(List.of(completedBundle));
        service.complete(CASE_REFERENCE, caseData);
        flushAndClear();

        service.complete(CASE_REFERENCE, caseData);
        flushAndClear();

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
    void uploadInvalidatesAnActiveBundle() {
        Bundle activeBundle = bundle();
        start(digitalCaseFile("DCF Updating"), activeBundle);
        service.save(CASE_REFERENCE, digitalCaseFile("DCF Uploaded"));
        flushAndClear();

        var storedDcf = digitalCaseFileRepository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getData().getStatus()).isEqualTo("DCF Uploaded");
        assertThat(storedDcf.getActiveBundleId()).isNull();
        assertThat(storedDcf.getCompletedBundleId()).isNull();
        assertThatThrownBy(() -> {
            CaseData caseData = new CaseData();
            caseData.setCaseBundles(List.of(completedBundle(activeBundle)));
            service.complete(CASE_REFERENCE, caseData);
        }).isInstanceOfSatisfying(ResponseStatusException.class,
            exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void removeLeavesATombstoneThatFencesAnActiveBundle() {
        Bundle activeBundle = bundle();
        start(digitalCaseFile("DCF Updating"), activeBundle);

        service.save(CASE_REFERENCE, null);
        flushAndClear();

        var storedDcf = digitalCaseFileRepository.findById(CASE_REFERENCE).orElseThrow();
        assertThat(storedDcf.getData()).isNull();
        assertThat(storedDcf.getActiveBundleId()).isNull();
        assertThat(storedDcf.getCompletedBundleId()).isNull();
        assertThatThrownBy(() -> {
            CaseData caseData = new CaseData();
            caseData.setCaseBundles(List.of(completedBundle(activeBundle)));
            service.complete(CASE_REFERENCE, caseData);
        }).isInstanceOfSatisfying(ResponseStatusException.class,
            exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void rollsBackTableAndBlobWritesWithTheEnclosingTransaction() {
        Bundle activeBundle = bundle();
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> transaction.executeWithoutResult(status -> {
            jdbc.update(
                "update ccd.case_data set data = '{\"digitalCaseFile\":{\"status\":\"DCF Updating\"}}'::jsonb "
                    + "where reference = ?",
                CASE_REFERENCE
            );
            start(digitalCaseFile("DCF Updating"), activeBundle);
            throw new IllegalStateException("submission failed");
        })).isInstanceOf(IllegalStateException.class);

        assertThat(digitalCaseFileRepository.findById(CASE_REFERENCE)).isEmpty();
        assertThat(jdbc.queryForObject(
            "select data::text from ccd.case_data where reference = ?",
            String.class,
            CASE_REFERENCE
        )).isEqualTo("{}");
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

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
