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
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class IssueInitialConsiderationDirectionsWACallbackHandler extends CallbackHandlerBase {

    private static final String COMPLETE_IICD_HDR =
        "<h1>Issue Initial Consideration Directions for Work Allocation complete</h1>";
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private final VerifyTokenService verifyTokenService;

    @Autowired
    public IssueInitialConsiderationDirectionsWACallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseManagementForCaseWorkerService caseManagementForCaseWorkerService,
        VerifyTokenService verifyTokenService
    ) {
        super(caseDetailsConverter);
        this.caseManagementForCaseWorkerService = caseManagementForCaseWorkerService;
        this.verifyTokenService = verifyTokenService;
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
        return toCallbackResponse(submitInitialConsideration(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toSubmittedCallbackResponse(completeInitialConsideration(
                    authorizationToken
                ));
    }

    private ResponseEntity<CCDCallbackResponse> submitInitialConsideration(CCDRequest ccdRequest, String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.setNextListedDate(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    private ResponseEntity<CCDCallbackResponse> completeInitialConsideration(String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        return ResponseEntity.ok(CCDCallbackResponse.builder().confirmation_header(COMPLETE_IICD_HDR).build());
    }
}
