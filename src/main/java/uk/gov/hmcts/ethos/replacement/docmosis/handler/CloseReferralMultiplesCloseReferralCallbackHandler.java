package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.multipleResponse;

@Component
public class CloseReferralMultiplesCloseReferralCallbackHandler extends CallbackHandlerBase {

    private static final String CLOSE_REFERRAL_BODY = "<hr>"
        + "<h3>What happens next</h3>"
        + "<p>We have closed this referral. You can still view it in the "
        + "<a href=\"/cases/case-details/%s#Referrals\" target=\"_blank\">Referrals tab (opens in new tab)</a>.</p>";

    @Autowired
    public CloseReferralMultiplesCloseReferralCallbackHandler(
        CaseDetailsConverter caseDetailsConverter
    ) {
        super(caseDetailsConverter);
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
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
                    toMultipleRequest(caseDetails)
                ));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        return toSubmittedCallbackResponse(completeInitialConsideration(
                    toCcdRequest(caseDetails)
                ));
    }

    private ResponseEntity<MultipleCallbackResponse> aboutToSubmitCloseReferral(MultipleRequest multipleRequest) {
        MultipleData multipleData = multipleRequest.getCaseDetails().getCaseData();
        ReferralHelper.addReferralDocumentToDocumentCollection(multipleData);
        ReferralHelper.setReferralStatusToClosed(multipleData);
        ReferralHelper.clearCloseReferralDataFromCaseData(multipleData);
        return multipleResponse(multipleData, null);
    }

    private ResponseEntity<MultipleCallbackResponse> completeInitialConsideration(CCDRequest multipleRequest) {
        String body = String.format(CLOSE_REFERRAL_BODY, multipleRequest.getCaseDetails().getCaseId());
        return ResponseEntity.ok(MultipleCallbackResponse.builder().confirmation_body(body).build());
    }
}
