package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultiplesSendNotificationService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntity;

@Component
public class SendNotificationMultiplesSendNotificationCallbackHandler extends MultipleCallbackHandlerBase {

    private final MultiplesSendNotificationService multiplesSendNotificationService;

    @Autowired
    public SendNotificationMultiplesSendNotificationCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        MultiplesSendNotificationService multiplesSendNotificationService
    ) {
        super(caseDetailsConverter);
        this.multiplesSendNotificationService = multiplesSendNotificationService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
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
    Object aboutToSubmit(MultipleRequest multipleRequest) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        var multipleCaseDetails = multipleRequest.getCaseDetails();
        var caseData = multipleCaseDetails.getCaseData();
        List<String> errors = new ArrayList<>();
        multiplesSendNotificationService.sendNotificationToSingles(
            caseData,
            multipleCaseDetails,
            authorizationToken,
            errors
        );
        multiplesSendNotificationService.setSendNotificationDocumentsToDocumentCollection(caseData);
        multiplesSendNotificationService.clearSendNotificationFields(caseData);
        return getMultipleCallbackRespEntity(errors, multipleCaseDetails);
    }

    @Override
    Object submitted(MultipleRequest multipleRequest) {
        String caseId = multipleRequest.getCaseDetails().getCaseId();
        String body = String.format("""
                ### What happens next
                The selected parties will receive the notification. </br>
                The notifications will be stored on the individual case, not on the Multiple. </br>
                Another notification can be sent <a href="/cases/case-details/%s/trigger/sendNotification/sendNotification1">using this link</a>
                """, caseId);

        return ResponseEntity.ok(
            MultipleCallbackResponse.builder()
                .confirmation_body(body)
                .build()
        );
    }
}
