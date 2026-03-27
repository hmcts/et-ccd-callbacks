package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.PreAcceptanceCaseService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;

@Component
public class PreAcceptanceCaseCallbackHandler extends CallbackHandlerBase {

    private final PreAcceptanceCaseService preAcceptanceCaseService;

    @Autowired
    public PreAcceptanceCaseCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        PreAcceptanceCaseService preAcceptanceCaseService
    ) {
        super(caseDetailsConverter);
        this.preAcceptanceCaseService = preAcceptanceCaseService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("preAcceptanceCase");
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
        var ccdRequest = toCcdRequest(caseDetails);
        var caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = preAcceptanceCaseService.validateAcceptanceDate(caseData);
        return toCallbackResponse(getCallbackRespEntityErrors(errors, caseData));
    }
}
