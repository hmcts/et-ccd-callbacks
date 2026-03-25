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
import uk.gov.hmcts.ethos.replacement.docmosis.service.RespondNotificationService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class RespondNotificationCallbackHandler extends CallbackHandlerBase {

    private static final String RESPOND_SUBMITTED_BODY_TEMPLATE = """
        ### What happens next

        You can still view the response in the <a href="/cases/case-details/%s#Notifications" target="_blank">
        Notifications tab (opens in a new tab)</a>
        """;
    private final RespondNotificationService respondNotificationService;

    @Autowired
    public RespondNotificationCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        RespondNotificationService respondNotificationService
    ) {
        super(caseDetailsConverter);
        this.respondNotificationService = respondNotificationService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("respondNotification");
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
        return toCallbackResponse(aboutToSubmitRespondNotification(toCcdRequest(caseDetails)));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        return toSubmittedCallbackResponse(submittedRespondNotification(toCcdRequest(caseDetails)));
    }

    private ResponseEntity<CCDCallbackResponse> aboutToSubmitRespondNotification(CCDRequest ccdRequest) {
        var details = ccdRequest.getCaseDetails();
        respondNotificationService.handleAboutToSubmit(details);
        return getCallbackRespEntityNoErrors(details.getCaseData());
    }

    private ResponseEntity<CCDCallbackResponse> submittedRespondNotification(CCDRequest ccdRequest) {
        String body = String.format(RESPOND_SUBMITTED_BODY_TEMPLATE, ccdRequest.getCaseDetails().getCaseId());
        return ResponseEntity.ok(CCDCallbackResponse.builder().confirmation_body(body).build());
    }
}
