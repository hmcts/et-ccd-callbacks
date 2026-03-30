package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.applications.respondent.RespondentTellSomethingElseController;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import java.util.List;

@Component
public class RespondentTSECallbackHandler extends CallbackHandlerBase {

    private final RespondentTellSomethingElseController respondentTellSomethingElseController;

    @Autowired
    public RespondentTSECallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        RespondentTellSomethingElseController respondentTellSomethingElseController
    ) {
        super(caseDetailsConverter);
        this.respondentTellSomethingElseController = respondentTellSomethingElseController;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("respondentTSE");
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
        return toCallbackResponse(
            respondentTellSomethingElseController.aboutToSubmitRespondentTSE(
                toCcdRequest(caseDetails),
                authorizationToken
            )
        );
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toSubmittedCallbackResponse(
            respondentTellSomethingElseController.completeApplication(
                toCcdRequest(caseDetails),
                authorizationToken
            )
        );
    }
}
