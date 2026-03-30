package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.ET1ServingController;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import java.util.List;

@Component
public class UploadDocumentForServingCallbackHandler extends CallbackHandlerBase {

    private final ET1ServingController submittedController;

    @Autowired
    public UploadDocumentForServingCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        ET1ServingController submittedController
    ) {
        super(caseDetailsConverter);
        this.submittedController = submittedController;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("uploadDocumentForServing");
    }

    @Override
    public boolean acceptsAboutToSubmit() {
        return false;
    }

    @Override
    public boolean acceptsSubmitted() {
        return true;
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toSubmittedCallbackResponse(
            submittedController.submitted(
                toCcdRequest(caseDetails),
                authorizationToken
            )
        );
    }
}
