package uk.gov.hmcts.ethos.replacement.docmosis.domain.digitalcasefile;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.ccd.sdk.testing.CcdEventTestSupport;
import uk.gov.hmcts.ccd.sdk.testing.CcdSdkTest;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.BundleDetails;
import uk.gov.hmcts.et.common.model.bundle.DocumentLink;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.DigitalCaseFileType;
import uk.gov.hmcts.ethos.replacement.docmosis.client.BundleApiClient;
import uk.gov.hmcts.ethos.replacement.docmosis.config.EtJsonCcdConfig;
import uk.gov.hmcts.ethos.replacement.docmosis.config.JacksonConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.ETCaseView;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.DigitalCaseFile;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.HubLinkStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.NotificationView;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.DigitalCaseFileRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.HubLinkStatusRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.NotificationViewRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DigitalCaseFilePersistenceService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DigitalCaseFileService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@CcdSdkTest(
    components = {EtJsonCcdConfig.class, JacksonConfiguration.class,
        AsyncStitchingCompleteEvent.class, ETCaseView.class,
        DigitalCaseFileService.class, DigitalCaseFilePersistenceService.class},
    repositories = {DigitalCaseFileRepository.class, HubLinkStatusRepository.class,
        NotificationViewRepository.class},
    entities = {DigitalCaseFile.class, HubLinkStatus.class, NotificationView.class}
)
class AsyncStitchingCompleteEventIntegrationTest {

    @Autowired
    private CcdEventTestSupport<CaseData, CaseState> events;

    @Autowired
    private DigitalCaseFileRepository digitalCaseFileRepository;

    @MockitoBean
    private BundleApiClient bundleApiClient;

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    @ParameterizedTest
    @ValueSource(strings = {ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID})
    void completionUpdatesTheTableWithoutChangingTheCcdBlob(String caseType) {
        var cases = events.forCaseType(caseType);
        CaseData blob = new CaseData();
        blob.setEthosCaseReference("123456/2021");
        long reference = cases.seed(CaseState.Accepted, blob);

        UUID pendingId = UUID.randomUUID();
        digitalCaseFileRepository.saveAndFlush(DigitalCaseFile.create(
            reference, new DigitalCaseFileType(), pendingId));
        final var before = cases.snapshot(reference);

        CaseData submitted = new CaseData();
        submitted.setCaseBundles(List.of(Bundle.builder()
            .id(pendingId.toString())
            .value(BundleDetails.builder()
                .id(pendingId.toString())
                .stitchStatus("DONE")
                .stitchedDocument(DocumentLink.builder()
                    .documentUrl("http://documents.example/documents/generated")
                    .documentBinaryUrl("http://documents.example/documents/generated/binary")
                    .documentFilename("generated.pdf")
                    .build())
                .build())
            .build()));

        var result = cases.event(reference, AsyncStitchingCompleteEvent.EVENT_ID, submitted)
            .submitExpectingSuccess();

        assertThat(result.rawData()).isEqualTo(before.rawData());
        assertThat(result.blobVersion()).isEqualTo(before.blobVersion());
        assertThat(result.projectedCase().getCaseBundles()).isNull();

        var storedDcf = digitalCaseFileRepository.findById(reference).orElseThrow();
        assertThat(storedDcf.getPendingBundleId()).isNull();
        assertThat(storedDcf.getData()).isEqualTo(result.projectedCase().getDigitalCaseFile());
        assertThat(storedDcf.getData().getStatus()).startsWith("DCF Generated:");
        assertThat(storedDcf.getData().getUploadedDocument().getDocumentFilename())
            .isEqualTo("generated.pdf");
    }
}
