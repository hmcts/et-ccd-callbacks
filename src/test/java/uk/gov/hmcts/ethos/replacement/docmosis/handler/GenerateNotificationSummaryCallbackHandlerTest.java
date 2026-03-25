package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateNotificationSummaryCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private SendNotificationService sendNotificationService;

    private GenerateNotificationSummaryCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GenerateNotificationSummaryCallbackHandler(caseDetailsConverter, sendNotificationService);
    }

    @Test
    void aboutToSubmitShouldGenerateNotificationSummaryAndSetMarkup() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        DocumentInfo documentInfo = DocumentInfo.builder().markUp("summary-link").build();
        when(sendNotificationService.createNotificationSummary(caseData, null, "ET_EnglandWales"))
            .thenReturn(documentInfo);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(sendNotificationService).createNotificationSummary(caseData, null, "ET_EnglandWales");
        assertThat(caseData.getDocMarkUp()).isEqualTo("summary-link");
    }

    @Test
    void submittedShouldReturnConfirmationWithDocumentMarkup() {
        CaseData caseData = new CaseData();
        caseData.setDocMarkUp("summary-link");
        stubConverter(caseData);

        var response = handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("summary-link");
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
