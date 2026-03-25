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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1VettingHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReportDataService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.MONTH_STRING_DATE_FORMAT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.setDocumentNumbers;

@Component
public class Et1VettingCallbackHandler extends CallbackHandlerBase {

    private static final String PROCESSING_COMPLETE_TEXT = "<hr><h2>Do this next</h2>"
        + "<p>You must <a href=\"/cases/case-details/%s/trigger/preAcceptanceCase/preAcceptanceCase1\">"
        + "accept or reject the case</a> or refer the case.</p>";

    private final VerifyTokenService verifyTokenService;
    private final Et1VettingService et1VettingService;
    private final DocumentManagementService documentManagementService;
    private final ReportDataService reportDataService;

    @Autowired
    public Et1VettingCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService,
        Et1VettingService et1VettingService,
        DocumentManagementService documentManagementService,
        ReportDataService reportDataService
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
        this.et1VettingService = et1VettingService;
        this.documentManagementService = documentManagementService;
        this.reportDataService = reportDataService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("et1Vetting");
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
        return toCallbackResponse(et1VettingAboutToSubmit(authorizationToken, toCcdRequest(caseDetails)));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toSubmittedCallbackResponse(et1VettingProcessingComplete(toCcdRequest(caseDetails), authorizationToken));
    }

    private ResponseEntity<CCDCallbackResponse> et1VettingAboutToSubmit(String userToken, CCDRequest ccdRequest) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setEt1VettingCompletedBy(reportDataService.getUserFullName(userToken));
        caseData.setEt1DateCompleted(LocalDate.now().format(DateTimeFormatter.ofPattern(MONTH_STRING_DATE_FORMAT)));

        DocumentInfo documentInfo = et1VettingService.generateEt1VettingDocument(
            caseData,
            userToken,
            ccdRequest.getCaseDetails().getCaseTypeId()
        );
        caseData.setEt1VettingDocument(documentManagementService.addDocumentToDocumentField(documentInfo));
        Et1VettingHelper.addEt1VettingToDocTab(caseData);
        caseData.setSuggestedHearingVenues(caseData.getEt1HearingVenues());
        setDocumentNumbers(caseData);
        et1VettingService.clearEt1FieldsFromCaseData(ccdRequest.getCaseDetails().getCaseData());

        return getCallbackRespEntityNoErrors(caseData);
    }

    private ResponseEntity<CCDCallbackResponse> et1VettingProcessingComplete(CCDRequest ccdRequest, String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        String caseNumber = ccdRequest.getCaseDetails().getCaseId();
        return ResponseEntity.ok(
            CCDCallbackResponse.builder()
                .data(ccdRequest.getCaseDetails().getCaseData())
                .confirmation_body(String.format(PROCESSING_COMPLETE_TEXT, caseNumber))
                .build()
        );
    }
}
