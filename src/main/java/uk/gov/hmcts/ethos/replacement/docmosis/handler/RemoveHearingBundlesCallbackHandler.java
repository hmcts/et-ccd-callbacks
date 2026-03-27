package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BundlesRespondentService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class RemoveHearingBundlesCallbackHandler extends CallbackHandlerBase {

    private static final String BUNDLES_FEATURE_IS_NOT_AVAILABLE = "Bundles feature is not available";

    private final BundlesRespondentService bundlesRespondentService;
    private final FeatureToggleService featureToggleService;

    @Autowired
    public RemoveHearingBundlesCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        BundlesRespondentService bundlesRespondentService,
        FeatureToggleService featureToggleService
    ) {
        super(caseDetailsConverter);
        this.bundlesRespondentService = bundlesRespondentService;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("removeHearingBundles");
    }

    @Override
    public boolean acceptsAboutToSubmit() {
        return true;
    }

    @Override
    public boolean acceptsSubmitted() {
        return false;
    }

    @Override
    CallbackResponse<CaseData> aboutToSubmit(CaseDetails caseDetails) {
        return toCallbackResponse(removeHearingBundle(
                    toCcdRequest(caseDetails)
                ));
    }

    private ResponseEntity<CCDCallbackResponse> removeHearingBundle(
        uk.gov.hmcts.et.common.model.ccd.CCDRequest request
    ) {
        throwIfBundlesFlagDisabled();
        CaseData caseData = request.getCaseDetails().getCaseData();
        try {
            bundlesRespondentService.removeHearingBundles(caseData);
            bundlesRespondentService.clearInputData(caseData);
            return getCallbackRespEntityNoErrors(caseData);
        } catch (Exception exception) {
            return getCallbackRespEntityErrors(List.of(exception.getMessage()), caseData);
        }
    }

    private void throwIfBundlesFlagDisabled() {
        if (!featureToggleService.isBundlesEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, BUNDLES_FEATURE_IS_NOT_AVAILABLE);
        }
    }
}
