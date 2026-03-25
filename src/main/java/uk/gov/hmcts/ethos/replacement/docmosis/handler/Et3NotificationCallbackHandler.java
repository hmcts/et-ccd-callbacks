package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ET3DocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et3NotificationService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.NotificationHelper.getParties;

@Component
public class Et3NotificationCallbackHandler extends CallbackHandlerBase {

    private static final String SUBMITTED_HEADER =
        "<h1>Documents submitted</h1>\r\n\r\n<h5>We have notified the following parties:</h5>\r\n\r\n<h3>%s</h3>";

    private final Et3NotificationService et3NotificationService;

    @Autowired
    public Et3NotificationCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        Et3NotificationService et3NotificationService
    ) {
        super(caseDetailsConverter);
        this.et3NotificationService = et3NotificationService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("et3Notification");
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
        var ccdRequest = toCcdRequest(caseDetails);
        var caseData = ccdRequest.getCaseDetails().getCaseData();

        try {
            List<String> errors = new ArrayList<>();
            ET3DocumentHelper.addOrRemoveET3Documents(caseData);
            Et3ResponseHelper.setEt3NotificationAcceptedDates(caseData);
            try {
                ET3DocumentHelper.addET3NotificationDocumentsToDocumentCollection(caseData);
            } catch (IOException exception) {
                errors.add(exception.getMessage());
            }
            return toCallbackResponse(getCallbackRespEntityErrors(errors, caseData));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to process ET3 notification callback", exception);
        }
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        var ccdRequest = toCcdRequest(caseDetails);
        var caseData = ccdRequest.getCaseDetails().getCaseData();
        et3NotificationService.sendNotifications(ccdRequest.getCaseDetails());

        return toSubmittedCallbackResponse(ResponseEntity.ok(CCDCallbackResponse.builder()
            .data(caseData)
            .confirmation_header(String.format(SUBMITTED_HEADER, getParties(caseData)))
            .confirmation_body("<span></span>")
            .build()));
    }
}
