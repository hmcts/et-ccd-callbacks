package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples.CreateReferralMultiplesController;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import java.util.List;

@Component
public class CreateReferralMultiplesCreateReferralCallbackHandler extends MultipleCallbackHandlerBase {

    private final CreateReferralMultiplesController createReferralMultiplesController;

    @Autowired
    public CreateReferralMultiplesCreateReferralCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CreateReferralMultiplesController createReferralMultiplesController
    ) {
        super(caseDetailsConverter);
        this.createReferralMultiplesController = createReferralMultiplesController;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("createReferral");
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
    Object aboutToSubmit(MultipleRequest multipleRequest) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        try {
            return createReferralMultiplesController.aboutToSubmitReferralDetails(
                multipleRequest,
                authorizationToken
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to delegate callback to controller", exception);
        }
    }

    @Override
    Object submitted(MultipleRequest multipleRequest) {
        return createReferralMultiplesController.completeCreateReferral(multipleRequest);
    }
}
