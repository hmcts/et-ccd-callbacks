package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class BundlesCallbackHelperTest {

    private static final String AUTH_TOKEN = "Bearer test-token";

    @Mock
    private VerifyTokenService verifyTokenService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Test
    void throwIfBundlesFlagDisabled_whenDisabled_throwsServiceUnavailable() {
        when(featureToggleService.isBundlesEnabled()).thenReturn(false);

        assertThatThrownBy(() -> BundlesCallbackHelper.throwIfBundlesFlagDisabled(featureToggleService))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    void validateBundlesCallback_whenTokenInvalid_returnsForbidden() {
        when(featureToggleService.isBundlesEnabled()).thenReturn(true);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        var result = BundlesCallbackHelper.validateBundlesCallback(
                verifyTokenService, featureToggleService, AUTH_TOKEN);

        assertThat(result).isPresent();
        assertThat(result.get().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void validateBundlesCallback_whenValid_returnsEmpty() {
        when(featureToggleService.isBundlesEnabled()).thenReturn(true);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        var result = BundlesCallbackHelper.validateBundlesCallback(
                verifyTokenService, featureToggleService, AUTH_TOKEN);

        assertThat(result).isEmpty();
    }

    @Test
    void buildSubmittedResponse_includesConfirmationContent() {
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(CaseDataBuilder.builder().build())
                .build();

        var response = BundlesCallbackHelper.buildSubmittedResponse(ccdRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        CCDCallbackResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getConfirmation_header()).isEqualTo(BundlesCallbackHelper.SUBMITTED_CONFIRMATION_HEADER);
        assertThat(body.getConfirmation_body()).isEqualTo(BundlesCallbackHelper.SUBMITTED_CONFIRMATION_BODY);
        assertThat(body.getData()).isEqualTo(ccdRequest.getCaseDetails().getCaseData());
    }
}
