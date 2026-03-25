package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1ReppedHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1ReppedService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class Et1ReppedCreateCaseCallbackHandler extends CallbackHandlerBase {

    private final Et1ReppedService et1ReppedService;

    @Autowired
    public Et1ReppedCreateCaseCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        Et1ReppedService et1ReppedService
    ) {
        super(caseDetailsConverter);
        this.et1ReppedService = et1ReppedService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("et1ReppedCreateCase");
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
        var request = toCcdRequest(caseDetails);
        CaseData caseData = request.getCaseDetails().getCaseData();
        Et1ReppedHelper.setCreateDraftData(caseData, request.getCaseDetails().getCaseId());
        caseData.setSearchCriteria(null);
        return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        var request = toCcdRequest(caseDetails);
        try {
            et1ReppedService.assignCaseAccess(request.getCaseDetails(), authorizationToken);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to assign case access", exception);
        }
        return toSubmittedCallbackResponse(getCallbackRespEntityNoErrors(request.getCaseDetails().getCaseData()));
    }
}
