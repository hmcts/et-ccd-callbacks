package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.TseService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.claimant.ClaimantTellSomethingElseService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_REP_TITLE;

@ExtendWith(MockitoExtension.class)
class ClaimantTSECallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private ClaimantTellSomethingElseService claimantTseService;
    @Mock
    private TseService tseService;

    private ClaimantTSECallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ClaimantTSECallbackHandler(caseDetailsConverter, claimantTseService, tseService);
    }

    @Test
    void aboutToSubmitShouldCreateClaimantApplicationAndClearData() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(claimantTseService).populateClaimantTse(caseData);
        verify(tseService).createApplication(caseData, CLAIMANT_REP_TITLE);
        verify(claimantTseService).generateAndAddApplicationPdf(caseData, null, "ET_EnglandWales");
        verify(claimantTseService).sendEmails(any());
        verify(tseService).clearApplicationData(caseData);
    }

    @Test
    void submittedShouldReturnApplicationCompleteBody() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(claimantTseService.buildApplicationCompleteResponse(caseData)).thenReturn("done");

        var response = handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).isEqualTo("done");
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
