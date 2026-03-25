package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AddAmendClaimantRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.NocClaimantRepresentativeService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class AddAmendClaimantRepresentativeCallbackHandler extends CallbackHandlerBase {

    private final AddAmendClaimantRepresentativeService addAmendClaimantRepresentativeService;
    private final NocClaimantRepresentativeService nocClaimantRepresentativeService;

    @Autowired
    public AddAmendClaimantRepresentativeCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        AddAmendClaimantRepresentativeService addAmendClaimantRepresentativeService,
        NocClaimantRepresentativeService nocClaimantRepresentativeService
    ) {
        super(caseDetailsConverter);
        this.addAmendClaimantRepresentativeService = addAmendClaimantRepresentativeService;
        this.nocClaimantRepresentativeService = nocClaimantRepresentativeService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("addAmendClaimantRepresentative");
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
        var ccdRequest = toCcdRequest(caseDetails);
        var caseData = ccdRequest.getCaseDetails().getCaseData();
        addAmendClaimantRepresentativeService.addAmendClaimantRepresentative(caseData);
        return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        var callbackRequest = toCallbackRequest(caseDetails);
        try {
            nocClaimantRepresentativeService.updateClaimantRepAccess(callbackRequest);
        } catch (IOException exception) {
            throw new CcdInputOutputException(
                "Failed to update claimant representatives access",
                exception
            );
        }
        return emptyResponse();
    }
}
