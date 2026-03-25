package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BundlesRespondentService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BundlesRespondentPrepareDocCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private BundlesRespondentService bundlesRespondentService;
    @Mock
    private SendNotificationService sendNotificationService;
    @Mock
    private FeatureToggleService featureToggleService;

    private BundlesRespondentPrepareDocCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new BundlesRespondentPrepareDocCallbackHandler(
            caseDetailsConverter,
            verifyTokenService,
            bundlesRespondentService,
            sendNotificationService,
            featureToggleService
        );
    }

    @Test
    void aboutToSubmitShouldThrowWhenBundlesFeatureDisabled() {
        when(featureToggleService.isBundlesEnabled()).thenReturn(false);

        assertThatThrownBy(() -> handler.aboutToSubmit(callbackCaseDetails()))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("Bundles feature is not available");
    }

    @Test
    void aboutToSubmitShouldNotPrepareBundleWhenTokenInvalid() {
        when(featureToggleService.isBundlesEnabled()).thenReturn(true);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(bundlesRespondentService, never()).addToBundlesCollection(any());
        verify(bundlesRespondentService, never()).clearInputData(any());
    }

    @Test
    void submittedShouldNotifyAndReturnConfirmationWhenTokenValid() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(featureToggleService.isBundlesEnabled()).thenReturn(true);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        var response = handler.submitted(callbackCaseDetails());

        verify(sendNotificationService).notify(any());
        assertThat(response.getConfirmationHeader()).contains("hearing documents");
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
