package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class CloseReferralCloseReferralCallbackHandler extends CallbackHandlerBase {

    private static final String CLOSE_REFERRAL_BODY = "<hr>"
        + "<h3>What happens next</h3>"
        + "<p>We have closed this referral. You can still view it in the "
        + "<a href=\"/cases/case-details/%s#Referrals\" target=\"_blank\">Referrals tab (opens in new tab)</a>.</p>";

    @Autowired
    public CloseReferralCloseReferralCallbackHandler(
        CaseDetailsConverter caseDetailsConverter
    ) {
        super(caseDetailsConverter);
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("closeReferral");
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
        return toCallbackResponse(aboutToSubmitCloseReferral(
                    toCcdRequest(caseDetails)
                ));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        return toSubmittedCallbackResponse(completeCloseReferral(
                    toCcdRequest(caseDetails)
                ));
    }

    private ResponseEntity<CCDCallbackResponse> aboutToSubmitCloseReferral(CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        ReferralHelper.addReferralDocumentToDocumentCollection(caseData);
        ReferralHelper.setReferralStatusToClosed(caseData);
        ReferralHelper.clearCloseReferralDataFromCaseData(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    private ResponseEntity<CCDCallbackResponse> completeCloseReferral(CCDRequest ccdRequest) {
        String body = String.format(CLOSE_REFERRAL_BODY, ccdRequest.getCaseDetails().getCaseId());
        return ResponseEntity.ok(CCDCallbackResponse.builder().confirmation_body(body).build());
    }
}
