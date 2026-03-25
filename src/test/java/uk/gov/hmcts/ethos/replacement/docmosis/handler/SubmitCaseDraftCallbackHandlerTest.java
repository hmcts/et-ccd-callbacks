package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1SubmissionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.callback.CaseActionsForCaseWorkerCallbackService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmitCaseDraftCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CaseActionsForCaseWorkerCallbackService caseActionsForCaseWorkerCallbackService;
    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    @Mock
    private Et1SubmissionService et1SubmissionService;

    private SubmitCaseDraftCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SubmitCaseDraftCallbackHandler(
            caseDetailsConverter,
            caseActionsForCaseWorkerCallbackService,
            caseManagementForCaseWorkerService,
            et1SubmissionService
        );
    }

    @Test
    void aboutToSubmitShouldDelegateToPostDefaultValues() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(caseActionsForCaseWorkerCallbackService.postDefaultValues(any(), eq(null)))
            .thenReturn(ResponseEntity.ok(CCDCallbackResponse.builder().data(caseData).build()));

        handler.aboutToSubmit(callbackCaseDetails());

        verify(caseActionsForCaseWorkerCallbackService).postDefaultValues(any(), eq(null));
    }

    @Test
    void submittedShouldSendConfirmationWhenNoIOException() throws IOException {
        CaseData caseData = new CaseData();
        stubConverter(caseData);

        handler.submitted(callbackCaseDetails());

        verify(caseManagementForCaseWorkerService).setHmctsServiceIdSupplementary(any());
        verify(et1SubmissionService).sendEt1ConfirmationClaimant(any(), eq(null));
    }

    @Test
    void submittedShouldWrapIOException() throws IOException {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        doThrow(new IOException("failed"))
            .when(caseManagementForCaseWorkerService).setHmctsServiceIdSupplementary(any());

        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to submit ET1 draft");
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
