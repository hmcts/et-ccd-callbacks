package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper.buildFlagsImageFileName;

@Slf4j
@Component
public class RestrictedCasesCallbackHandler extends CallbackHandlerBase {

    private static final String LOG_MESSAGE = "received notification request for case reference :    ";

    private final EventValidationService eventValidationService;
    private final FeatureToggleService featureToggleService;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private final CaseFlagsService caseFlagsService;

    @Autowired
    public RestrictedCasesCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        EventValidationService eventValidationService,
        FeatureToggleService featureToggleService,
        CaseManagementForCaseWorkerService caseManagementForCaseWorkerService,
        CaseFlagsService caseFlagsService
    ) {
        super(caseDetailsConverter);
        this.eventValidationService = eventValidationService;
        this.featureToggleService = featureToggleService;
        this.caseManagementForCaseWorkerService = caseManagementForCaseWorkerService;
        this.caseFlagsService = caseFlagsService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("restrictedCases");
    }

    @Override
    public boolean acceptsAboutToSubmit() {
        return true;
    }

    @Override
    public boolean acceptsSubmitted() {
        return false;
    }

    @Override
    CallbackResponse<CaseData> aboutToSubmit(CaseDetails caseDetails) {
        var request = toCcdRequest(caseDetails);
        log.info("RESTRICTED CASES ---> " + LOG_MESSAGE + request.getCaseDetails().getCaseId());

        CaseData caseData = request.getCaseDetails().getCaseData();
        buildFlagsImageFileName(request.getCaseDetails());
        eventValidationService.validateRestrictedReportingNames(caseData);

        if (featureToggleService.isHmcEnabled()) {
            caseManagementForCaseWorkerService.setPublicCaseName(caseData);
            caseFlagsService.setPrivateHearingFlag(caseData);
        }

        return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        throw new IllegalStateException("Handler does not support submitted callbacks for events: "
            + getHandledEventIds());
    }
}
