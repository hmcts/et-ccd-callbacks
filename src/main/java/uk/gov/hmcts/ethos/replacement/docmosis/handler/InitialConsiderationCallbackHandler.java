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
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.InitialConsiderationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.InitialConsiderationService;
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
public class InitialConsiderationCallbackHandler extends CallbackHandlerBase {

    private static final String COMPLETE_IC_HDR = "<h1>Initial consideration complete</h1>";

    private final VerifyTokenService verifyTokenService;
    private final InitialConsiderationService initialConsiderationService;
    private final DocumentManagementService documentManagementService;
    private final ReportDataService reportDataService;
    private final CaseFlagsService caseFlagsService;
    private final FeatureToggleService featureToggleService;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    @Autowired
    public InitialConsiderationCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService,
        InitialConsiderationService initialConsiderationService,
        DocumentManagementService documentManagementService,
        ReportDataService reportDataService,
        CaseFlagsService caseFlagsService,
        FeatureToggleService featureToggleService,
        CaseManagementForCaseWorkerService caseManagementForCaseWorkerService
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
        this.initialConsiderationService = initialConsiderationService;
        this.documentManagementService = documentManagementService;
        this.reportDataService = reportDataService;
        this.caseFlagsService = caseFlagsService;
        this.featureToggleService = featureToggleService;
        this.caseManagementForCaseWorkerService = caseManagementForCaseWorkerService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("initialConsideration");
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
        return toCallbackResponse(submitInitialConsideration(
                toCcdRequest(caseDetails),
                authorizationToken
            ));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toSubmittedCallbackResponse(completeInitialConsideration(authorizationToken));
    }

    private ResponseEntity<CCDCallbackResponse> submitInitialConsideration(CCDRequest ccdRequest, String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        initialConsiderationService.processIcDocumentCollections(caseData);
        caseData.setIcCompletedBy(reportDataService.getUserFullName(userToken));
        caseData.setIcDateCompleted(LocalDate.now().format(DateTimeFormatter.ofPattern(MONTH_STRING_DATE_FORMAT)));

        DocumentInfo documentInfo = initialConsiderationService.generateDocument(
            caseData,
            userToken,
            ccdRequest.getCaseDetails().getCaseTypeId()
        );
        caseData.setEtInitialConsiderationDocument(documentManagementService.addDocumentToDocumentField(documentInfo));
        InitialConsiderationHelper.addToDocumentCollection(caseData);

        if (featureToggleService.isHmcEnabled()) {
            caseFlagsService.setPrivateHearingFlag(caseData);
        }

        setDocumentNumbers(caseData);
        caseManagementForCaseWorkerService.setNextListedDate(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    private ResponseEntity<CCDCallbackResponse> completeInitialConsideration(String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        return ResponseEntity.ok(CCDCallbackResponse.builder().confirmation_header(COMPLETE_IC_HDR).build());
    }
}
