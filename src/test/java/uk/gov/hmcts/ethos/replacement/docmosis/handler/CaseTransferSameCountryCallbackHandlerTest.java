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
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferSameCountryService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseTransferSameCountryCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private CaseTransferSameCountryService caseTransferSameCountryService;
    @Mock
    private CaseManagementLocationService caseManagementLocationService;
    @Mock
    private FeatureToggleService featureToggleService;

    private CaseTransferSameCountryCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CaseTransferSameCountryCallbackHandler(
            caseDetailsConverter,
            verifyTokenService,
            caseTransferSameCountryService,
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

        verify(caseTransferSameCountryService, never()).transferCase(any(), any());
    }

    @Test
    void aboutToSubmitShouldTransferWithoutLocationUpdatesWhenFeatureFlagsDisabled() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        when(featureToggleService.isHmcEnabled()).thenReturn(false);
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(false);
        when(caseTransferSameCountryService.transferCase(any(), any())).thenReturn(List.of());

        handler.aboutToSubmit(callbackCaseDetails());

        verify(caseTransferSameCountryService).transferCase(any(), any());
        verify(caseManagementLocationService, never()).setCaseManagementLocationCode(any());
        verify(caseManagementLocationService, never()).setCaseManagementLocation(any());
    }

    @Test
    void aboutToSubmitShouldTransferAndSetLocationWhenHmcEnabled() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        when(featureToggleService.isHmcEnabled()).thenReturn(true);
        when(caseTransferSameCountryService.transferCase(any(), any())).thenReturn(List.of());

        handler.aboutToSubmit(callbackCaseDetails());

        verify(caseManagementLocationService).setCaseManagementLocationCode(caseData);
        verify(caseManagementLocationService).setCaseManagementLocation(caseData);
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
