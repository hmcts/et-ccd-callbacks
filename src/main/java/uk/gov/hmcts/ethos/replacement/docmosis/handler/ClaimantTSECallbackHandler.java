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
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.TseService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.claimant.ClaimantTellSomethingElseService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.CLAIMANT_REP_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class ClaimantTSECallbackHandler extends CallbackHandlerBase {

    private final ClaimantTellSomethingElseService claimantTseService;
    private final TseService tseService;

    @Autowired
    public ClaimantTSECallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        ClaimantTellSomethingElseService claimantTseService,
        TseService tseService
    ) {
        super(caseDetailsConverter);
        this.claimantTseService = claimantTseService;
        this.tseService = tseService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("claimantTSE");
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
        return toCallbackResponse(aboutToSubmitClaimantTse(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        return toSubmittedCallbackResponse(completeClaimantApplication(
                    toCcdRequest(caseDetails)
                ));
    }

    private ResponseEntity<CCDCallbackResponse> aboutToSubmitClaimantTse(CCDRequest ccdRequest, String userToken) {
        var details = ccdRequest.getCaseDetails();
        CaseData caseData = details.getCaseData();

        claimantTseService.populateClaimantTse(caseData);
        tseService.createApplication(caseData, CLAIMANT_REP_TITLE);
        claimantTseService.generateAndAddApplicationPdf(caseData, userToken, details.getCaseTypeId());
        claimantTseService.sendEmails(details);
        tseService.clearApplicationData(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    private ResponseEntity<CCDCallbackResponse> completeClaimantApplication(CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        String body = claimantTseService.buildApplicationCompleteResponse(caseData);
        return ResponseEntity.ok(CCDCallbackResponse.builder().confirmation_body(body).build());
    }
}
