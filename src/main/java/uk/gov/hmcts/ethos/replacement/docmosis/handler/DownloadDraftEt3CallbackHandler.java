package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et3ResponseService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class DownloadDraftEt3CallbackHandler extends CallbackHandlerBase {

    private static final String GENERATED_DOCUMENT_URL = "Please download the draft ET3 : ";
    private static final String SECTION_COMPLETE_BODY = """
        You may want to complete the rest of the ET3 Form using the links below\
        <br><a href="/cases/case-details/%s/trigger/et3Response/et3Response1">ET3 - Respondent Details</a>\
        <br><a href="/cases/case-details/%s/trigger/et3ResponseEmploymentDetails/et3ResponseEmploymentDetails1\
        ">ET3 - Employment Details</a>\
        <br><a href="/cases/case-details/%s/trigger/et3ResponseDetails/et3ResponseDetails1">ET3 - \
        Response Details</a>
        <br><a href="/cases/case-details/%s/trigger/downloadDraftEt3/downloadDraftEt31">Download draft ET3 Form</a>""";

    private final Et3ResponseService et3ResponseService;

    @Autowired
    public DownloadDraftEt3CallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        Et3ResponseService et3ResponseService
    ) {
        super(caseDetailsConverter);
        this.et3ResponseService = et3ResponseService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("downloadDraftEt3");
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
        var request = toCcdRequest(caseDetails);
        CaseData caseData = request.getCaseDetails().getCaseData();
        DocumentInfo documentInfo = et3ResponseService.generateEt3ResponseDocument(
            caseData, authorizationToken, request.getCaseDetails().getCaseTypeId(), request.getEventId());
        documentInfo.setMarkUp(
            documentInfo.getMarkUp().replace("Document",
                "Draft ET3 - " + caseData.getSubmitEt3Respondent().getSelectedLabel()));
        caseData.setDocMarkUp(documentInfo.getMarkUp());
        return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        var request = toCcdRequest(caseDetails);
        CaseData caseData = request.getCaseDetails().getCaseData();
        String ccdId = request.getCaseDetails().getCaseId();
        return toSubmittedCallbackResponse(ResponseEntity.ok(CCDCallbackResponse.builder()
            .data(caseData)
            .confirmation_body(GENERATED_DOCUMENT_URL + caseData.getDocMarkUp()
                + "\r\n\r\n" + SECTION_COMPLETE_BODY.formatted(ccdId, ccdId, ccdId, ccdId))
            .build()));
    }
}
