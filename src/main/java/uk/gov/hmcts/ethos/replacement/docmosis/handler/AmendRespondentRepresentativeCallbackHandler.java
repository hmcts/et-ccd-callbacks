package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.CaseActionsForCaseWorkerController;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import java.util.List;

@Component
public class AmendRespondentRepresentativeCallbackHandler extends CallbackHandlerBase {

    private final CaseActionsForCaseWorkerController caseActionsForCaseWorkerController;

    @Autowired
    public AmendRespondentRepresentativeCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseActionsForCaseWorkerController caseActionsForCaseWorkerController
    ) {
        super(caseDetailsConverter);
        this.caseActionsForCaseWorkerController = caseActionsForCaseWorkerController;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("amendRespondentRepresentative");
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
            caseActionsForCaseWorkerController.amendRespondentRepresentative(
                toCcdRequest(caseDetails),
                authorizationToken
            )
        );
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        caseActionsForCaseWorkerController.amendRespondentRepSubmitted(
            toCallbackRequest(caseDetails),
            authorizationToken
        );
        return emptyResponse();
    }
}
