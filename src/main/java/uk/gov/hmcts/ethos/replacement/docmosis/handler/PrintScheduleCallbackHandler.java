package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.MultipleDocGenerationController;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import java.util.List;

@Component
public class PrintScheduleCallbackHandler extends MultipleCallbackHandlerBase {

    private final MultipleDocGenerationController multipleDocGenerationController;

    @Autowired
    public PrintScheduleCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        MultipleDocGenerationController multipleDocGenerationController
    ) {
        super(caseDetailsConverter);
        this.multipleDocGenerationController = multipleDocGenerationController;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("printSchedule");
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
        return multipleDocGenerationController.printSchedule(
            multipleRequest,
            authorizationToken
        );
    }

    @Override
    Object submitted(MultipleRequest multipleRequest) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return multipleDocGenerationController.printDocumentConfirmation(
            multipleRequest,
            authorizationToken
        );
    }
}
