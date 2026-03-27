package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.callback.CaseActionsForCaseWorkerCallbackService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

@Component
public class PostDefaultValuesCallbackHandler extends CallbackHandlerBase {

    private final CaseActionsForCaseWorkerCallbackService caseActionsForCaseWorkerCallbackService;

    @Autowired
    public PostDefaultValuesCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseActionsForCaseWorkerCallbackService caseActionsForCaseWorkerCallbackService
    ) {
        super(caseDetailsConverter);
        this.caseActionsForCaseWorkerCallbackService = caseActionsForCaseWorkerCallbackService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of(
            "UPDATE_CASE_SUBMITTED",
            "caseTransferMultiple",
            "processCaseTransfer",
            "returnCaseTransfer",
            "createEcmCase"
        );
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
    CallbackResponse<CaseData> aboutToSubmit(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toCallbackResponse(caseActionsForCaseWorkerCallbackService.postDefaultValues(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }
}
