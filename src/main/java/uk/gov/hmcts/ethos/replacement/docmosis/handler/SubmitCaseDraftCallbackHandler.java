package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1SubmissionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.callback.CaseActionsForCaseWorkerCallbackService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class SubmitCaseDraftCallbackHandler extends CallbackHandlerBase {

    private final CaseActionsForCaseWorkerCallbackService caseActionsForCaseWorkerCallbackService;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private final Et1SubmissionService et1SubmissionService;

    @Autowired
    public SubmitCaseDraftCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseActionsForCaseWorkerCallbackService caseActionsForCaseWorkerCallbackService,
        CaseManagementForCaseWorkerService caseManagementForCaseWorkerService,
        Et1SubmissionService et1SubmissionService
    ) {
        super(caseDetailsConverter);
        this.caseActionsForCaseWorkerCallbackService = caseActionsForCaseWorkerCallbackService;
        this.caseManagementForCaseWorkerService = caseManagementForCaseWorkerService;
        this.et1SubmissionService = et1SubmissionService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("SUBMIT_CASE_DRAFT");
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
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        try {
            return toSubmittedCallbackResponse(submittedEt1Draft(
                toCcdRequest(caseDetails),
                authorizationToken
            ));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to submit ET1 draft", exception);
        }
    }

    private ResponseEntity<CCDCallbackResponse> submittedEt1Draft(CCDRequest ccdRequest, String userToken)
        throws IOException {
        var details = ccdRequest.getCaseDetails();
        caseManagementForCaseWorkerService.setHmctsServiceIdSupplementary(details);
        et1SubmissionService.sendEt1ConfirmationClaimant(details, userToken);
        return getCallbackRespEntityNoErrors(details.getCaseData());
    }
}
