package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddAmendHearingCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    @Mock
    private FeatureToggleService featureToggleService;

    private AddAmendHearingCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AddAmendHearingCallbackHandler(
            caseDetailsConverter,
            caseManagementForCaseWorkerService,
            featureToggleService
        );
    }

    @Test
    void aboutToSubmitShouldInvokeHearingServicesWhenMul2Disabled() throws IOException {
        CaseData caseData = new CaseData();
        stubConverter(caseData, "ET_EnglandWales");
        when(featureToggleService.isMul2Enabled()).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(caseManagementForCaseWorkerService).amendHearing(caseData, "ET_EnglandWales");
        verify(caseManagementForCaseWorkerService).setNextEarliestListedHearing(caseData);
        verify(caseManagementForCaseWorkerService).setNextListedDate(caseData);
        verify(caseManagementForCaseWorkerService, never()).setNextListedDateOnMultiple(any());
    }

    @Test
    void aboutToSubmitShouldUpdateMultipleWhenMul2Enabled() throws IOException {
        CaseData caseData = new CaseData();
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails = stubConverter(caseData, "ET_EnglandWales");
        when(featureToggleService.isMul2Enabled()).thenReturn(true);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(caseManagementForCaseWorkerService).setNextListedDateOnMultiple(ccdCaseDetails);
    }

    @Test
    void aboutToSubmitShouldThrowWhenMultipleUpdateFails() throws IOException {
        CaseData caseData = new CaseData();
        stubConverter(caseData, "ET_EnglandWales");
        when(featureToggleService.isMul2Enabled()).thenReturn(true);
        doThrowIOException();

        assertThatThrownBy(() -> handler.aboutToSubmit(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to update next listed date on multiple")
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
    }

    private void doThrowIOException() throws IOException {
        org.mockito.Mockito.doThrow(new IOException("boom"))
            .when(caseManagementForCaseWorkerService)
            .setNextListedDateOnMultiple(any(uk.gov.hmcts.et.common.model.ccd.CaseDetails.class));
    }

    private uk.gov.hmcts.et.common.model.ccd.CaseDetails stubConverter(CaseData caseData, String caseTypeId) {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails =
            new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        ccdCaseDetails.setCaseData(caseData);
        ccdCaseDetails.setCaseTypeId(caseTypeId);
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
