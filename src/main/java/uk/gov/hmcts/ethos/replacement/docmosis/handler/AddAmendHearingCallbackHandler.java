package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@Component
public class AddAmendHearingCallbackHandler extends CallbackHandlerBase {

    private static final String LOG_MESSAGE = "received notification request for case reference :    ";

    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private final FeatureToggleService featureToggleService;

    @Autowired
    public AddAmendHearingCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseManagementForCaseWorkerService caseManagementForCaseWorkerService,
        FeatureToggleService featureToggleService
    ) {
        super(caseDetailsConverter);
        this.caseManagementForCaseWorkerService = caseManagementForCaseWorkerService;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("addAmendHearing");
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
        log.info("AMEND HEARING ---> " + LOG_MESSAGE + request.getCaseDetails().getCaseId());

        CaseData caseData = request.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.amendHearing(caseData, request.getCaseDetails().getCaseTypeId());
        caseManagementForCaseWorkerService.setNextEarliestListedHearing(caseData);
        caseManagementForCaseWorkerService.setNextListedDate(caseData);

        if (featureToggleService.isMul2Enabled()) {
            try {
                caseManagementForCaseWorkerService.setNextListedDateOnMultiple(request.getCaseDetails());
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to update next listed date on multiple", exception);
            }
        }

        return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        throw new IllegalStateException("Handler does not support submitted callbacks for events: "
            + getHandledEventIds());
    }
}
