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
import uk.gov.hmcts.ethos.replacement.docmosis.service.PseRespondToTribunalService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class PseRespondentRespondToTribunalCallbackHandler extends CallbackHandlerBase {

    private final PseRespondToTribunalService pseRespondToTribunalService;

    @Autowired
    public PseRespondentRespondToTribunalCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        PseRespondToTribunalService pseRespondToTribunalService
    ) {
        super(caseDetailsConverter);
        this.pseRespondToTribunalService = pseRespondToTribunalService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("pseRespondentRespondToTribunal");
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
        return toCallbackResponse(aboutToSubmitRespondToTribunal(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        return toSubmittedCallbackResponse(submittedRespondToTribunal(toCcdRequest(caseDetails)));
    }

    private ResponseEntity<CCDCallbackResponse> aboutToSubmitRespondToTribunal(
        CCDRequest ccdRequest,
        String userToken
    ) {
        var details = ccdRequest.getCaseDetails();
        pseRespondToTribunalService.addRespondentResponseToJON(details.getCaseData(), userToken);
        pseRespondToTribunalService.sendAcknowledgeEmail(details, userToken);
        pseRespondToTribunalService.sendClaimantEmail(details);
        pseRespondToTribunalService.sendTribunalEmail(details, RESPONDENT_TITLE);
        pseRespondToTribunalService.clearRespondentResponse(details.getCaseData());
        return getCallbackRespEntityNoErrors(details.getCaseData());
    }

    private ResponseEntity<CCDCallbackResponse> submittedRespondToTribunal(CCDRequest ccdRequest) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        String body = pseRespondToTribunalService.getRespondentSubmittedBody(caseData);
        return ResponseEntity.ok(CCDCallbackResponse.builder().confirmation_body(body).build());
    }
}
