package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples.ReplyToReferralMultiplesController;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import java.util.List;

@Component
public class ReplyToReferralMultiplesReplyReferralCallbackHandler extends MultipleCallbackHandlerBase {

    private final ReplyToReferralMultiplesController replyToReferralMultiplesController;

    @Autowired
    public ReplyToReferralMultiplesReplyReferralCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        ReplyToReferralMultiplesController replyToReferralMultiplesController
    ) {
        super(caseDetailsConverter);
        this.replyToReferralMultiplesController = replyToReferralMultiplesController;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("replyToReferral");
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
            return replyToReferralMultiplesController.aboutToSubmitReferralReply(
                multipleRequest,
                authorizationToken
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to delegate callback to controller", exception);
        }
    }

    @Override
    Object submitted(MultipleRequest multipleRequest) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return replyToReferralMultiplesController.completeReplyToReferral(
            multipleRequest,
            authorizationToken
        );
    }
}
