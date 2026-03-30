package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.ExcelActionsController;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import java.util.List;

@Component
public class FixMultipleTransferAPICallbackHandler extends MultipleCallbackHandlerBase {

    private final ExcelActionsController aboutController;

    @Autowired
    public FixMultipleTransferAPICallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        ExcelActionsController aboutController
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
        return List.of("fixMultipleTransferAPI");
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
    Object aboutToSubmit(MultipleRequest multipleRequest) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return aboutController.fixMultipleCaseApi(
            multipleRequest,
            authorizationToken
        );
    }
}
