package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples.UpdateReferralMultiplesController;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import java.util.List;

@Component
public class UpdateReferralMultiplesUpdateReferralCallbackHandler extends MultipleCallbackHandlerBase {

    private final UpdateReferralMultiplesController aboutController;

    @Autowired
    public UpdateReferralMultiplesUpdateReferralCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        UpdateReferralMultiplesController aboutController
    ) {
        super(caseDetailsConverter);
        this.aboutController = aboutController;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("updateReferral");
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
    @SneakyThrows
    Object aboutToSubmit(MultipleRequest multipleRequest) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return aboutController.aboutToSubmitUpdateReferralDetails(
                multipleRequest,
                authorizationToken
            );
    }
}
