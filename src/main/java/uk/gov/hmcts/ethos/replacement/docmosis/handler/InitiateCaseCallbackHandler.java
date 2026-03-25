package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.callback.CaseActionsForCaseWorkerCallbackService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class InitiateCaseCallbackHandler extends CallbackHandlerBase {

    private final CaseActionsForCaseWorkerCallbackService caseActionsForCaseWorkerCallbackService;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    @Autowired
    public InitiateCaseCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseActionsForCaseWorkerCallbackService caseActionsForCaseWorkerCallbackService,
        CaseManagementForCaseWorkerService caseManagementForCaseWorkerService
    ) {
        super(caseDetailsConverter);
        this.caseActionsForCaseWorkerCallbackService = caseActionsForCaseWorkerCallbackService;
        this.caseManagementForCaseWorkerService = caseManagementForCaseWorkerService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("initiateCase");
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
        return toCallbackResponse(caseActionsForCaseWorkerCallbackService.postDefaultValues(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        var request = toCcdRequest(caseDetails);
        try {
            caseManagementForCaseWorkerService.setHmctsServiceIdSupplementary(request.getCaseDetails());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to add service id", exception);
        }
        return toSubmittedCallbackResponse(getCallbackRespEntityNoErrors(request.getCaseDetails().getCaseData()));
    }
}
