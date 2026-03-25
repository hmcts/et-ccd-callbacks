package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseNotesService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class AddCaseNoteCaseNotesSinglesCallbackHandler extends CallbackHandlerBase {

    private final CaseNotesService caseNotesService;

    @Autowired
    public AddCaseNoteCaseNotesSinglesCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseNotesService caseNotesService
    ) {
        super(caseDetailsConverter);
        this.caseNotesService = caseNotesService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("addCaseNote");
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
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        CaseData caseData = toCcdRequest(caseDetails).getCaseDetails().getCaseData();
        caseNotesService.addCaseNote(caseData, authorizationToken);
        return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        throw new IllegalStateException("Handler does not support submitted callbacks for events: "
            + getHandledEventIds());
    }
}
