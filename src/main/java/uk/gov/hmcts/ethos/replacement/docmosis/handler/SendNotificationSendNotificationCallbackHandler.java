package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.notifications.admin.SendNotificationController;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import java.util.List;

@Component
public class SendNotificationSendNotificationCallbackHandler extends CallbackHandlerBase {

    private final SendNotificationController sendNotificationController;

    @Autowired
    public SendNotificationSendNotificationCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        SendNotificationController sendNotificationController
    ) {
        super(caseDetailsConverter);
        this.sendNotificationController = sendNotificationController;
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
        return toCallbackResponse(
            sendNotificationController.aboutToSubmit(
                toCcdRequest(caseDetails),
                authorizationToken
            )
        );
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toSubmittedCallbackResponse(
            sendNotificationController.submitted(
                toCcdRequest(caseDetails),
                authorizationToken
            )
        );
    }
}
