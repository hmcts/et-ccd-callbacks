package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;

@Component
public class ClaimantTransferredCaseAccessCallbackHandler extends CallbackHandlerBase {

    private final CaseAccessService caseAccessService;

    @Autowired
    public ClaimantTransferredCaseAccessCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CaseAccessService caseAccessService
    ) {
        super(caseDetailsConverter);
        this.caseAccessService = caseAccessService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("claimantTransferredCaseAccess");
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
        var convertedCaseDetails = ccdRequest.getCaseDetails();
        List<String> errors = caseAccessService.assignExistingCaseRoles(convertedCaseDetails);
        return toCallbackResponse(getCallbackRespEntityErrors(errors, convertedCaseDetails.getCaseData()));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        throw new IllegalStateException("Handler does not support submitted callbacks for events: "
            + getHandledEventIds());
    }
}
