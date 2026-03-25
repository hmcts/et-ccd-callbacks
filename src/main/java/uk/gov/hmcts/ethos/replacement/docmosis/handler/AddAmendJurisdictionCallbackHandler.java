package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ConciliationTrackService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper.buildFlagsImageFileName;

@Slf4j
@Component
public class AddAmendJurisdictionCallbackHandler extends CallbackHandlerBase {

    private static final String LOG_MESSAGE = "received notification request for case reference :    ";

    private final ConciliationTrackService conciliationTrackService;

    @Autowired
    public AddAmendJurisdictionCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        ConciliationTrackService conciliationTrackService
    ) {
        super(caseDetailsConverter);
        this.conciliationTrackService = conciliationTrackService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("addAmendJurisdiction");
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
        log.info("AMEND JURISDICTION ---> " + LOG_MESSAGE + request.getCaseDetails().getCaseId());

        CaseData caseData = request.getCaseDetails().getCaseData();
        conciliationTrackService.populateConciliationTrackForJurisdiction(caseData);
        buildFlagsImageFileName(request.getCaseDetails().getCaseTypeId(), caseData);
        return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        throw new IllegalStateException("Handler does not support submitted callbacks for events: "
            + getHandledEventIds());
    }
}
