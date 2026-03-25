package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et3ResponseService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitEt3CallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private Et3ResponseService et3ResponseService;
    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    private SubmitEt3CallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SubmitEt3CallbackHandler(
            caseDetailsConverter,
            et3ResponseService,
            caseManagementForCaseWorkerService
        );
    }

    @Test
    void aboutToSubmitShouldGenerateAndPersistEt3Response() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        DocumentInfo documentInfo = DocumentInfo.builder().markUp("et3").build();
        when(et3ResponseService.generateEt3ResponseDocument(caseData, null, "ET_EnglandWales", null))
            .thenReturn(documentInfo);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(et3ResponseService).generateEt3ResponseDocument(caseData, null, "ET_EnglandWales", null);
        verify(et3ResponseService).saveEt3Response(caseData, documentInfo);
        verify(et3ResponseService).saveRelatedDocumentsToDocumentCollection(caseData);
        verify(et3ResponseService).sendNotifications(any());
        verify(caseManagementForCaseWorkerService).setNextListedDate(caseData);
        verify(caseManagementForCaseWorkerService).updateWorkAllocationField(anyList(), eq(caseData));
    }

    @Test
    void submittedShouldReturnEt3CompletionConfirmation() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);

        var response = handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationHeader()).contains("ET3 Response submitted");
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
