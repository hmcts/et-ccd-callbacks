package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferDifferentCountryService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseTransferDifferentCountryCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private CaseTransferDifferentCountryService caseTransferDifferentCountryService;
    @Mock
    private CaseManagementLocationService caseManagementLocationService;
    @Mock
    private FeatureToggleService featureToggleService;

    private CaseTransferDifferentCountryCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CaseTransferDifferentCountryCallbackHandler(
            caseDetailsConverter,
            verifyTokenService,
            caseTransferDifferentCountryService,
            caseManagementLocationService,
            featureToggleService
        );
    }

    @Test
    void aboutToSubmitShouldNotTransferWhenTokenInvalid() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(caseTransferDifferentCountryService, never()).transferCase(any(), any());
    }

    @Test
    void aboutToSubmitShouldTransferAndSetCaseManagementLocationWhenHmcEnabled() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        when(featureToggleService.isHmcEnabled()).thenReturn(true);
        when(caseTransferDifferentCountryService.transferCase(any(), any())).thenReturn(List.of());

        handler.aboutToSubmit(callbackCaseDetails());

        verify(caseTransferDifferentCountryService).transferCase(any(), any());
        verify(caseManagementLocationService).setCaseManagementLocationCode(caseData);
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
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
