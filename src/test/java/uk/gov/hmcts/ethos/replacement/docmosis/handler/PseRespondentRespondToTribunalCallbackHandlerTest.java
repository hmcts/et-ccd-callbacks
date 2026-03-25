package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.PseRespondToTribunalService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PseRespondentRespondToTribunalCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private PseRespondToTribunalService pseRespondToTribunalService;

    private PseRespondentRespondToTribunalCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PseRespondentRespondToTribunalCallbackHandler(caseDetailsConverter, pseRespondToTribunalService);
    }

    @Test
    void aboutToSubmitShouldSendRespondentResponseAndNotifications() {
        CaseData caseData = new CaseData();
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails = stubConverter(caseData);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(pseRespondToTribunalService).addRespondentResponseToJON(caseData, null);
        verify(pseRespondToTribunalService).sendAcknowledgeEmail(ccdCaseDetails, null);
        verify(pseRespondToTribunalService).sendClaimantEmail(ccdCaseDetails);
        verify(pseRespondToTribunalService).sendTribunalEmail(ccdCaseDetails, "Respondent");
        verify(pseRespondToTribunalService).clearRespondentResponse(caseData);
    }

    @Test
    void submittedShouldReturnRespondentConfirmationBody() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(pseRespondToTribunalService.getRespondentSubmittedBody(caseData)).thenReturn("submitted body");

        var response = handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("submitted body");
    }

    private uk.gov.hmcts.et.common.model.ccd.CaseDetails stubConverter(CaseData caseData) {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails =
            new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        ccdCaseDetails.setCaseData(caseData);
        ccdCaseDetails.setCaseTypeId("ET_EnglandWales");
        ccdCaseDetails.setCaseId("123");
        when(caseDetailsConverter.convert(any(CaseDetails.class))).thenReturn(ccdCaseDetails);
        return ccdCaseDetails;
    }

    private CaseDetails callbackCaseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .caseTypeId("ET_EnglandWales")
            .state("Open")
            .build();
    }
}
