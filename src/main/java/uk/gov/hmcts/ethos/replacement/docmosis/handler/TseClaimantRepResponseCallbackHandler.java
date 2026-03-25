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
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.claimant.TseClaimantRepReplyService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class TseClaimantRepResponseCallbackHandler extends CallbackHandlerBase {

    private static final String SUBMITTED_BODY = """
        ### What happens next \r
        \r
        You have sent your response to the tribunal%s.\r
        \r
        The tribunal will consider all correspondence and let you know what happens next.""";
    private static final String SUBMITTED_COPY = " and copied it to the claimant";
    private final TseClaimantRepReplyService tseClaimantRepReplyService;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    @Autowired
    public TseClaimantRepResponseCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        TseClaimantRepReplyService tseClaimantRepReplyService,
        CaseManagementForCaseWorkerService caseManagementForCaseWorkerService
    ) {
        super(caseDetailsConverter);
        this.tseClaimantRepReplyService = tseClaimantRepReplyService;
        this.caseManagementForCaseWorkerService = caseManagementForCaseWorkerService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("tseClaimantRepResponse");
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
        return toCallbackResponse(aboutToSubmitClaimantRepReply(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        return toSubmittedCallbackResponse(submittedClaimantRepReply(
                    toCcdRequest(caseDetails)
                ));
    }

    private ResponseEntity<CCDCallbackResponse> aboutToSubmitClaimantRepReply(CCDRequest ccdRequest, String userToken) {
        var details = ccdRequest.getCaseDetails();
        CaseData caseData = details.getCaseData();
        tseClaimantRepReplyService.addTseClaimantRepReplyPdfToDocCollection(caseData, userToken,
            details.getCaseTypeId());
        tseClaimantRepReplyService.claimantReplyToTse(details, caseData);
        caseManagementForCaseWorkerService.setNextListedDate(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    private ResponseEntity<CCDCallbackResponse> submittedClaimantRepReply(CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        String body = String.format(SUBMITTED_BODY,
            YES.equals(caseData.getTseResponseCopyToOtherParty()) ? SUBMITTED_COPY : "");
        return ResponseEntity.ok(CCDCallbackResponse.builder().confirmation_body(body).build());
    }
}
