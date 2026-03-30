package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.AddAmendClaimantRepresentativeController;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import java.util.List;

@Component
public class AddAmendClaimantRepresentativeCallbackHandler extends CallbackHandlerBase {

    private final AddAmendClaimantRepresentativeController addAmendClaimantRepresentativeController;

    @Autowired
    public AddAmendClaimantRepresentativeCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        AddAmendClaimantRepresentativeController addAmendClaimantRepresentativeController
    ) {
        super(caseDetailsConverter);
        this.addAmendClaimantRepresentativeController = addAmendClaimantRepresentativeController;
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
        return toCallbackResponse(addAmendClaimantRepresentativeController.aboutToSubmit(toCcdRequest(caseDetails)));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        addAmendClaimantRepresentativeController.amendClaimantRepSubmitted(
            toCallbackRequest(caseDetails),
            authorizationToken
        );
        return emptyResponse();
    }
}
