package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BundlesRespondentService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@ExtendWith(MockitoExtension.class)
class RemoveHearingBundlesCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private BundlesRespondentService bundlesRespondentService;
    @Mock
    private FeatureToggleService featureToggleService;

    private RemoveHearingBundlesCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RemoveHearingBundlesCallbackHandler(
            caseDetailsConverter,
            bundlesRespondentService,
            featureToggleService
        );
    }

    @Test
    void aboutToSubmitShouldThrowWhenBundlesFeatureDisabled() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(featureToggleService.isBundlesEnabled()).thenReturn(false);

        assertThatThrownBy(() -> handler.aboutToSubmit(callbackCaseDetails()))
            .isInstanceOf(ResponseStatusException.class)
            .satisfies(exception -> {
                ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                assertThat(responseStatusException.getStatusCode()).isEqualTo(SERVICE_UNAVAILABLE);
            });
    }

    @Test
    void aboutToSubmitShouldRemoveAndClearWhenFeatureEnabled() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(featureToggleService.isBundlesEnabled()).thenReturn(true);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(bundlesRespondentService).removeHearingBundles(caseData);
        verify(bundlesRespondentService).clearInputData(caseData);
    }

    @Test
    void aboutToSubmitShouldReturnErrorsWhenRemoveFails() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(featureToggleService.isBundlesEnabled()).thenReturn(true);
        doThrow(new RuntimeException("bundle remove failed"))
            .when(bundlesRespondentService).removeHearingBundles(caseData);

        CCDCallbackResponse response = (CCDCallbackResponse) handler.aboutToSubmit(callbackCaseDetails());

        assertThat(response.getErrors()).containsExactly("bundle remove failed");
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
