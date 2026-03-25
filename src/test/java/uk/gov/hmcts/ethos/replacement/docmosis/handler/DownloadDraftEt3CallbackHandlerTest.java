package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et3ResponseService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DownloadDraftEt3CallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private Et3ResponseService et3ResponseService;

    private DownloadDraftEt3CallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DownloadDraftEt3CallbackHandler(caseDetailsConverter, et3ResponseService);
    }

    @Test
    void aboutToSubmitShouldGenerateDraftEt3AndSetMarkup() {
        CaseData caseData = caseDataWithRespondentLabel();
        stubConverter(caseData);
        DocumentInfo documentInfo = DocumentInfo.builder().markUp("Document link").build();
        when(et3ResponseService.generateEt3ResponseDocument(caseData, null, "ET_EnglandWales", null))
            .thenReturn(documentInfo);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(et3ResponseService).generateEt3ResponseDocument(caseData, null, "ET_EnglandWales", null);
        assertThat(caseData.getDocMarkUp()).isEqualTo("Draft ET3 - Respondent A link");
    }

    @Test
    void submittedShouldReturnConfirmationWithDraftEt3Markup() {
        CaseData caseData = caseDataWithRespondentLabel();
        caseData.setDocMarkUp("Draft ET3 - Respondent A link");
        stubConverter(caseData);

        var response = handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("Draft ET3 - Respondent A link");
        assertThat(response.getConfirmationBody()).contains("/cases/case-details/123/trigger/downloadDraftEt3");
    }

    private CaseData caseDataWithRespondentLabel() {
        CaseData caseData = new CaseData();
        caseData.setSubmitEt3Respondent(DynamicFixedListType.from("resp-1", "Respondent A", true));
        return caseData;
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
