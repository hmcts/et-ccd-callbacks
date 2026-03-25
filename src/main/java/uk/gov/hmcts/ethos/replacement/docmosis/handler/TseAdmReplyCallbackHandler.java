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
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.admin.TseAdmReplyService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.APPLICATION_WHAT_HAPPENS_NEXT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class TseAdmReplyCallbackHandler extends CallbackHandlerBase {

    private final VerifyTokenService verifyTokenService;
    private final TseAdmReplyService tseAdmReplyService;

    @Autowired
    public TseAdmReplyCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService,
        TseAdmReplyService tseAdmReplyService
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
        this.tseAdmReplyService = tseAdmReplyService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("tseAdmReply");
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
        return toCallbackResponse(aboutToSubmitAdminReply(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toSubmittedCallbackResponse(submittedAdminReply(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }

    private ResponseEntity<CCDCallbackResponse> aboutToSubmitAdminReply(CCDRequest ccdRequest, String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        var details = ccdRequest.getCaseDetails();
        CaseData caseData = details.getCaseData();
        tseAdmReplyService.updateApplicationState(caseData);
        tseAdmReplyService.saveTseAdmReplyDataFromCaseData(caseData);
        tseAdmReplyService.addTseAdmReplyPdfToDocCollection(details, userToken);
        tseAdmReplyService.sendNotifyEmailsToClaimant(details.getCaseId(), caseData, userToken);
        tseAdmReplyService.sendNotifyEmailsToRespondents(details, userToken);
        tseAdmReplyService.clearTseAdmReplyDataFromCaseData(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    private ResponseEntity<CCDCallbackResponse> submittedAdminReply(CCDRequest ccdRequest, String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        String body = String.format(APPLICATION_WHAT_HAPPENS_NEXT, ccdRequest.getCaseDetails().getCaseId());
        return ResponseEntity.ok(CCDCallbackResponse.builder().confirmation_body(body).build());
    }
}
