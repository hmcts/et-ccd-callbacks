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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MigrateCaseCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    private MigrateCaseCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MigrateCaseCallbackHandler(
            caseDetailsConverter,
            verifyTokenService,
            caseManagementForCaseWorkerService
        );
    }

    @Test
    void aboutToSubmitShouldReturnForbiddenWhenTokenInvalid() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(caseManagementForCaseWorkerService, never()).setGlobalSearchDefaults(any());
    }

    @Test
    void aboutToSubmitShouldSetGlobalSearchDefaultsWhenTokenValid() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(caseManagementForCaseWorkerService).setGlobalSearchDefaults(caseData);
    }

    @Test
    void submittedShouldReturnForbiddenWhenTokenInvalid() throws IOException {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.submitted(callbackCaseDetails());

        verify(caseManagementForCaseWorkerService, never()).setHmctsServiceIdSupplementary(any());
    }

    @Test
    void submittedShouldSetHmctsServiceIdWhenTokenValid() throws IOException {
        CaseData caseData = new CaseData();
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails = stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        handler.submitted(callbackCaseDetails());

        verify(caseManagementForCaseWorkerService).setHmctsServiceIdSupplementary(ccdCaseDetails);
    }

    @Test
    void submittedShouldWrapIOExceptionFromCaseManagementService() throws IOException {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        org.mockito.Mockito.doThrow(new IOException("write failed"))
            .when(caseManagementForCaseWorkerService)
            .setHmctsServiceIdSupplementary(any());

        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to complete migrated case submission")
            .hasCauseInstanceOf(IOException.class);
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
