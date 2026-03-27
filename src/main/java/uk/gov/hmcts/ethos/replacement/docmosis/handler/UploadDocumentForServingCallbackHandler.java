package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ServingService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

@Component
public class UploadDocumentForServingCallbackHandler extends CallbackHandlerBase {

    private static final String DOCUMENTS_SENT_HEADER = "<h1>Documents sent</h1>";

    private final ServingService servingService;

    @Autowired
    public UploadDocumentForServingCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        ServingService servingService
    ) {
        super(caseDetailsConverter);
        this.servingService = servingService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("uploadDocumentForServing");
    }

    @Override
    public boolean acceptsAboutToSubmit() {
        return false;
    }

    @Override
    public boolean acceptsSubmitted() {
        return true;
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        return toSubmittedCallbackResponse(submittedEt1Serving(toCcdRequest(caseDetails)));
    }

    private ResponseEntity<CCDCallbackResponse> submittedEt1Serving(CCDRequest ccdRequest) {
        servingService.sendNotifications(ccdRequest.getCaseDetails());
        return ResponseEntity.ok(
            CCDCallbackResponse.builder()
                .data(ccdRequest.getCaseDetails().getCaseData())
                .confirmation_header(DOCUMENTS_SENT_HEADER)
                .build()
        );
    }
}
