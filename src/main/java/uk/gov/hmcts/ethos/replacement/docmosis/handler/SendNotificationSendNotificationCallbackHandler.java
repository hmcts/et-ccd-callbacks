package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.callback.SendNotificationCallbackService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

@Component
public class SendNotificationSendNotificationCallbackHandler extends CallbackHandlerBase {

    private static final String SUBMITTED_BODY_TEMPLATE = """
            ### What happens next

            The selected parties will receive the notification.

            You can view the notification in the <a href="/cases/case-details/%s#Notifications" target="_blank">
            Notifications tab (opens in a new tab)</a>

            Another notification can be sent <a href="/cases/case-details/%s/trigger/sendNotification/sendNotification1">
            using this link</a>
            """;

    private final SendNotificationCallbackService sendNotificationCallbackService;

    @Autowired
    public SendNotificationSendNotificationCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        SendNotificationCallbackService sendNotificationCallbackService
    ) {
        super(caseDetailsConverter);
        this.sendNotificationCallbackService = sendNotificationCallbackService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("sendNotification");
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
        return toCallbackResponse(sendNotificationCallbackService.aboutToSubmit(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        return toSubmittedCallbackResponse(submitted(
                    toCcdRequest(caseDetails)
                ));
    }

    private ResponseEntity<CCDCallbackResponse> submitted(
        uk.gov.hmcts.et.common.model.ccd.CCDRequest request
    ) {
        String caseId = request.getCaseDetails().getCaseId();
        String body = String.format(SUBMITTED_BODY_TEMPLATE, caseId, caseId);
        return ResponseEntity.ok(CCDCallbackResponse.builder().confirmation_body(body).build());
    }
}
