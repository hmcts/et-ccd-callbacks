package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReportDataService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Et1VettingCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private Et1VettingService et1VettingService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private ReportDataService reportDataService;

    private Et1VettingCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new Et1VettingCallbackHandler(
            caseDetailsConverter,
            verifyTokenService,
            et1VettingService,
            documentManagementService,
            reportDataService
        );
    }

    @Test
    void aboutToSubmitShouldNotProcessWhenTokenInvalid() {
        stubConverter(new CaseData());
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(et1VettingService, never()).generateEt1VettingDocument(any(), any(), any());
    }

    @Test
    void aboutToSubmitShouldGenerateDocumentWhenTokenValid() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        when(reportDataService.getUserFullName(null)).thenReturn("Tester User");
        when(et1VettingService.generateEt1VettingDocument(caseData, null, "ET_EnglandWales"))
            .thenReturn(DocumentInfo.builder().markUp("vetting").build());

        handler.aboutToSubmit(callbackCaseDetails());

        verify(et1VettingService).generateEt1VettingDocument(caseData, null, "ET_EnglandWales");
        verify(et1VettingService).clearEt1FieldsFromCaseData(caseData);
    }

    @Test
    void submittedShouldReturnProcessingCompleteMessageWhenTokenValid() {
        stubConverter(new CaseData());
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        var response = handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("accept or reject the case");
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
