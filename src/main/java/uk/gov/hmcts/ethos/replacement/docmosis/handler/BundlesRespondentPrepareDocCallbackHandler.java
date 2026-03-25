package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BundlesRespondentService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class BundlesRespondentPrepareDocCallbackHandler extends CallbackHandlerBase {

    private static final String BUNDLES_FEATURE_IS_NOT_AVAILABLE = "Bundles feature is not available";

    private final VerifyTokenService verifyTokenService;
    private final BundlesRespondentService bundlesRespondentService;
    private final SendNotificationService sendNotificationService;
    private final FeatureToggleService featureToggleService;

    @Autowired
    public BundlesRespondentPrepareDocCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService,
        BundlesRespondentService bundlesRespondentService,
        SendNotificationService sendNotificationService,
        FeatureToggleService featureToggleService
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
        this.bundlesRespondentService = bundlesRespondentService;
        this.sendNotificationService = sendNotificationService;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("bundlesRespondentPrepareDoc");
    }

    @Override
    public boolean acceptsAboutToSubmit() {
        return true;
    }

    @Override
    public boolean acceptsSubmitted() {
        return true;
    }

    @Override
    CallbackResponse<CaseData> aboutToSubmit(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        throwIfBundlesFlagDisabled();
        if (!verifyTokenService.verifyTokenSignature(authorizationToken)) {
            return toCallbackResponse(ResponseEntity.status(FORBIDDEN.value()).build());
        }

        CaseData caseData = toCcdRequest(caseDetails).getCaseDetails().getCaseData();
        bundlesRespondentService.addToBundlesCollection(caseData);
        bundlesRespondentService.clearInputData(caseData);
        return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        throwIfBundlesFlagDisabled();
        if (!verifyTokenService.verifyTokenSignature(authorizationToken)) {
            return toSubmittedCallbackResponse(ResponseEntity.status(FORBIDDEN.value()).build());
        }

        var details = toCcdRequest(caseDetails).getCaseDetails();
        String header = "<h1>You have sent your hearing documents to the tribunal</h1>";
        String body = """
            <html>
                <body>
                    <tag><h2>What happens next</h2></tag>
                    <h2>The tribunal will let you know
                    if they have any questions about the hearing documents you have submitted.</h2>
                </body>
            </html>""";

        sendNotificationService.notify(details);

        return toSubmittedCallbackResponse(ResponseEntity.ok(CCDCallbackResponse.builder()
            .data(details.getCaseData())
            .confirmation_header(header)
            .confirmation_body(body)
            .build()));
    }

    private void throwIfBundlesFlagDisabled() {
        if (!featureToggleService.isBundlesEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, BUNDLES_FEATURE_IS_NOT_AVAILABLE);
        }
    }
}
