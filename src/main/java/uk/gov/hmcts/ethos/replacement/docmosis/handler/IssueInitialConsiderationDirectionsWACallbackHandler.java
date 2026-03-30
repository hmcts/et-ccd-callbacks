package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.IssueInitialConsiderationDirectionsWAController;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import java.util.List;

@Component
public class IssueInitialConsiderationDirectionsWACallbackHandler extends CallbackHandlerBase {

    private final IssueInitialConsiderationDirectionsWAController issueInitialConsiderationDirectionsWAController;

    @Autowired
    public IssueInitialConsiderationDirectionsWACallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        IssueInitialConsiderationDirectionsWAController issueInitialConsiderationDirectionsWAController
    ) {
        super(caseDetailsConverter);
        this.issueInitialConsiderationDirectionsWAController = issueInitialConsiderationDirectionsWAController;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("issueInitialConsiderationDirectionsWA");
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
            issueInitialConsiderationDirectionsWAController.submitInitialConsideration(
                toCcdRequest(caseDetails),
                authorizationToken
            )
        );
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toSubmittedCallbackResponse(
            issueInitialConsiderationDirectionsWAController.completeInitialConsideration(
                toCcdRequest(caseDetails),
                authorizationToken
            )
        );
    }
}
