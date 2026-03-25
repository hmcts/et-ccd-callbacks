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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et3ResponseService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class SubmitEt3CallbackHandler extends CallbackHandlerBase {

    private static final String ET3_COMPLETE_HEADER = "<h1>ET3 Response submitted</h1>";
    private static final String ET3_COMPLETE_BODY =
        """
                <h3>What happens next</h3>\r
                \r
                You should receive confirmation from the tribunal office to process your application within 5
                 working days. If you have not heard from them within 5 days, contact the office directly.""";

    private final Et3ResponseService et3ResponseService;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    @Autowired
    public SubmitEt3CallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        Et3ResponseService et3ResponseService,
        CaseManagementForCaseWorkerService caseManagementForCaseWorkerService
    ) {
        super(caseDetailsConverter);
        this.et3ResponseService = et3ResponseService;
        this.caseManagementForCaseWorkerService = caseManagementForCaseWorkerService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("submitEt3");
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
        et3ResponseService.saveEt3Response(caseData, documentInfo);
        et3ResponseService.saveRelatedDocumentsToDocumentCollection(caseData);
        FlagsImageHelper.buildFlagsImageFileName(request.getCaseDetails().getCaseTypeId(), caseData);
        et3ResponseService.sendNotifications(request.getCaseDetails());
        Et3ResponseHelper.resetEt3FormFields(caseData);
        caseManagementForCaseWorkerService.setNextListedDate(caseData);
        caseManagementForCaseWorkerService.updateWorkAllocationField(new ArrayList<>(), caseData);
        return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        var request = toCcdRequest(caseDetails);
        return toSubmittedCallbackResponse(ResponseEntity.ok(CCDCallbackResponse.builder()
            .data(request.getCaseDetails().getCaseData())
            .confirmation_header(ET3_COMPLETE_HEADER)
            .confirmation_body(ET3_COMPLETE_BODY)
            .build()));
    }
}
