package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.InitialConsiderationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReportDataService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitialConsiderationCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private InitialConsiderationService initialConsiderationService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private ReportDataService reportDataService;
    @Mock
    private CaseFlagsService caseFlagsService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    private InitialConsiderationCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new InitialConsiderationCallbackHandler(
            caseDetailsConverter,
            verifyTokenService,
            initialConsiderationService,
            documentManagementService,
            reportDataService,
            caseFlagsService,
            featureToggleService,
            caseManagementForCaseWorkerService
        );
    }

    @Test
    void aboutToSubmitShouldNotProcessWhenTokenInvalid() {
        stubConverter(new CaseData());
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(initialConsiderationService, never()).processIcDocumentCollections(any());
    }

    @Test
    void aboutToSubmitShouldProcessAndSetCaseFlagsWhenEnabled() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        when(featureToggleService.isHmcEnabled()).thenReturn(true);
        when(reportDataService.getUserFullName(null)).thenReturn("Judge User");
        when(initialConsiderationService.generateDocument(caseData, null, "ET_EnglandWales"))
            .thenReturn(DocumentInfo.builder().markUp("ic-doc").build());

        handler.aboutToSubmit(callbackCaseDetails());

        verify(initialConsiderationService).processIcDocumentCollections(caseData);
        verify(initialConsiderationService).generateDocument(caseData, null, "ET_EnglandWales");
        verify(caseFlagsService).setPrivateHearingFlag(caseData);
        verify(caseManagementForCaseWorkerService).setNextListedDate(caseData);
    }

    @Test
    void submittedShouldReturnCompletionHeaderWhenTokenValid() {
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        var response = handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationHeader()).contains("Initial consideration complete");
    }

    private void stubConverter(CaseData caseData) {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails =
            new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        ccdCaseDetails.setCaseData(caseData);
        ccdCaseDetails.setCaseTypeId("ET_EnglandWales");
        ccdCaseDetails.setCaseId("123");
        when(caseDetailsConverter.convert(any(CaseDetails.class))).thenReturn(ccdCaseDetails);
    }

    private CaseDetails callbackCaseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .caseTypeId("ET_EnglandWales")
            .state("Open")
            .build();
    }
}
