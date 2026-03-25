package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing.AllocateHearingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing.ScotlandAllocateHearingService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllocateHearingCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private AllocateHearingService allocateHearingService;
    @Mock
    private ScotlandAllocateHearingService scotlandAllocateHearingService;

    private AllocateHearingCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AllocateHearingCallbackHandler(
            caseDetailsConverter,
            verifyTokenService,
            allocateHearingService,
            scotlandAllocateHearingService
        );
    }

    @Test
    void aboutToSubmitShouldReturnForbiddenWhenTokenInvalid() {
        CaseData caseData = new CaseData();
        stubConverter(caseData, "ET_EnglandWales");
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(allocateHearingService, never()).updateCase(any());
        verify(scotlandAllocateHearingService, never()).updateCase(any());
    }

    @Test
    void aboutToSubmitShouldUseEnglandAndWalesServiceForEnglandAndWalesCase() {
        CaseData caseData = new CaseData();
        stubConverter(caseData, "ET_EnglandWales");
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(allocateHearingService).updateCase(caseData);
        verify(scotlandAllocateHearingService, never()).updateCase(any());
    }

    @Test
    void aboutToSubmitShouldUseScotlandServiceForScotlandCase() {
        CaseData caseData = new CaseData();
        stubConverter(caseData, "ET_Scotland");
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(scotlandAllocateHearingService).updateCase(caseData);
        verify(allocateHearingService, never()).updateCase(any());
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
    }

    private void stubConverter(CaseData caseData, String caseTypeId) {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails =
            new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        ccdCaseDetails.setCaseData(caseData);
        ccdCaseDetails.setCaseTypeId(caseTypeId);
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
