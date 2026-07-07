package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.util.Optional;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@Slf4j
public final class BundlesCallbackHelper {

    public static final String BUNDLES_LOG = "Bundles feature flag is {}";
    public static final String BUNDLES_FEATURE_IS_NOT_AVAILABLE = "Bundles feature is not available";
    private static final String INVALID_TOKEN = "Invalid Token {}";

    public static final String SUBMITTED_CONFIRMATION_HEADER =
            "<h1>You have sent your hearing documents to the tribunal</h1>";
    public static final String SUBMITTED_CONFIRMATION_BODY = """
        <html>
            <body>
                <tag><h2>What happens next</h2></tag>
                <h2>The tribunal will let you know
                if they have any questions about the hearing documents you have submitted.</h2>
            </body>
        </html>""";

    private BundlesCallbackHelper() {
    }

    public static void throwIfBundlesFlagDisabled(FeatureToggleService featureToggleService) {
        boolean bundlesToggle = featureToggleService.isBundlesEnabled();
        log.info(BUNDLES_LOG, bundlesToggle);
        if (!bundlesToggle) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, BUNDLES_FEATURE_IS_NOT_AVAILABLE);
        }
    }

    public static Optional<ResponseEntity<CCDCallbackResponse>> validateBundlesCallback(
            VerifyTokenService verifyTokenService,
            FeatureToggleService featureToggleService,
            String userToken) {
        throwIfBundlesFlagDisabled(featureToggleService);
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return Optional.of(ResponseEntity.status(FORBIDDEN.value()).build());
        }
        return Optional.empty();
    }

    public static ResponseEntity<CCDCallbackResponse> buildSubmittedResponse(CCDRequest ccdRequest) {
        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .data(ccdRequest.getCaseDetails().getCaseData())
                .confirmation_header(SUBMITTED_CONFIRMATION_HEADER)
                .confirmation_body(SUBMITTED_CONFIRMATION_BODY)
                .build());
    }
}
