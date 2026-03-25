package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1ReppedService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;

@Component
public class AmendClaimantRepresentativeContactCallbackHandler extends CallbackHandlerBase {

    private final Et1ReppedService et1ReppedService;

    @Autowired
    public AmendClaimantRepresentativeContactCallbackHandler(
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
        return List.of("amendClaimantRepresentativeContact");
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
        List<String> errors = new ArrayList<>();
        try {
            et1ReppedService.setClaimantRepresentativeValues(authorizationToken, caseData);
            caseData.setMyHmctsAddressText(null);
        } catch (GenericServiceException genericServiceException) {
            errors.add(genericServiceException.getMessage());
        }
        return toCallbackResponse(getCallbackRespEntityErrors(errors, caseData));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        throw new IllegalStateException("Handler does not support submitted callbacks for events: "
            + getHandledEventIds());
    }
}
