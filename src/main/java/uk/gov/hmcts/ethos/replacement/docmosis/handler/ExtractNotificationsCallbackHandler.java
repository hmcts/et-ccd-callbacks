package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples.ExtractNotificationsController;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import java.util.List;

@Component
public class ExtractNotificationsCallbackHandler extends MultipleCallbackHandlerBase {

    private final ExtractNotificationsController extractNotificationsController;

    @Autowired
    public ExtractNotificationsCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        ExtractNotificationsController extractNotificationsController
    ) {
        super(caseDetailsConverter);
        this.extractNotificationsController = extractNotificationsController;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("extractNotifications");
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
            return extractNotificationsController.aboutToSubmit(
                multipleRequest,
                authorizationToken
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to delegate callback to controller", exception);
        }
    }

    @Override
    Object submitted(MultipleRequest multipleRequest) {
        return extractNotificationsController.submitted();
    }
}
