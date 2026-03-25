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
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class GenerateNotificationSummaryCallbackHandler extends CallbackHandlerBase {

    private final SendNotificationService sendNotificationService;

    @Autowired
    public GenerateNotificationSummaryCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        SendNotificationService sendNotificationService
    ) {
        super(caseDetailsConverter);
        this.sendNotificationService = sendNotificationService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("generateNotificationSummary");
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
        return toCallbackResponse(aboutToSubmitNotificationDocument(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        return toSubmittedCallbackResponse(submittedNotificationDocument(toCcdRequest(caseDetails)));
    }

    private ResponseEntity<CCDCallbackResponse> aboutToSubmitNotificationDocument(
        CCDRequest ccdRequest,
        String userToken
    ) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        DocumentInfo documentInfo = sendNotificationService.createNotificationSummary(caseData, userToken,
            ccdRequest.getCaseDetails().getCaseTypeId());
        caseData.setDocMarkUp(documentInfo.getMarkUp());
        return getCallbackRespEntityNoErrors(caseData);
    }

    private ResponseEntity<CCDCallbackResponse> submittedNotificationDocument(CCDRequest ccdRequest) {
        return ResponseEntity.ok(
            CCDCallbackResponse.builder()
                .data(ccdRequest.getCaseDetails().getCaseData())
                .confirmation_body("Download the notification summary from this link: "
                    + ccdRequest.getCaseDetails().getCaseData().getDocMarkUp())
                .build()
        );
    }
}
