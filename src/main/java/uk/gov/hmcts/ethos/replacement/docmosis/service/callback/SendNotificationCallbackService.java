package uk.gov.hmcts.ethos.replacement.docmosis.service.callback;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Service
@RequiredArgsConstructor
public class SendNotificationCallbackService {

    private final SendNotificationService sendNotificationService;

    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(
        CCDRequest request,
        String userToken
    ) {
        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();
        sendNotificationService.createSendNotification(caseData);
        sendNotificationService.sendNotifyEmails(caseDetails);
        sendNotificationService.createBfAction(caseData);
        sendNotificationService.clearSendNotificationFields(caseData);
        caseData.setDraftAndSignJudgement(null);
        return getCallbackRespEntityNoErrors(caseData);
    }
}
