package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.respondent.TseRespondentReplyService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(MockitoExtension.class)
class TseRespondCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private TseRespondentReplyService tseRespondentReplyService;
    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    private TseRespondCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TseRespondCallbackHandler(
            caseDetailsConverter,
            verifyTokenService,
            tseRespondentReplyService,
            caseManagementForCaseWorkerService
        );
    }

    @Test
    void aboutToSubmitShouldNotReplyWhenTokenInvalid() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(tseRespondentReplyService, never()).addTseRespondentReplyPdfToDocCollection(any(), any(), any());
        verify(tseRespondentReplyService, never()).respondentReplyToTse(any(), any(), any());
    }

    @Test
    void aboutToSubmitShouldReplyWhenTokenValid() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(tseRespondentReplyService).addTseRespondentReplyPdfToDocCollection(caseData, null, "ET_EnglandWales");
        verify(tseRespondentReplyService).respondentReplyToTse(any(), any(), any());
        verify(caseManagementForCaseWorkerService).setNextListedDate(caseData);
    }

    @Test
    void submittedShouldIncludeCopyTextWhenRule92Yes() {
        CaseData caseData = new CaseData();
        caseData.setTseResponseCopyToOtherParty(YES);
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        var response = handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("copied it to the claimant");
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
