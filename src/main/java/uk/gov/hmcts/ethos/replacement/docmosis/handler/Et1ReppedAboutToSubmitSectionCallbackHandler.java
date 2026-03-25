package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1ReppedHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1ReppedService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class Et1ReppedAboutToSubmitSectionCallbackHandler extends CallbackHandlerBase {

    private final Et1ReppedService et1ReppedService;

    @Autowired
    public Et1ReppedAboutToSubmitSectionCallbackHandler(
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
        return List.of("et1SectionOne", "et1SectionTwo", "et1SectionThree");
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
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        var request = toCcdRequest(caseDetails);
        Et1ReppedHelper.setEt1SectionStatuses(request);
        et1ReppedService.addClaimantRepresentativeDetails(request.getCaseDetails().getCaseData(), authorizationToken);
        return toCallbackResponse(getCallbackRespEntityNoErrors(request.getCaseDetails().getCaseData()));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        CaseData caseData = toCcdRequest(caseDetails).getCaseDetails().getCaseData();
        return toSubmittedCallbackResponse(ResponseEntity.ok(CCDCallbackResponse.builder()
            .data(caseData)
            .confirmation_body(Et1ReppedHelper.getSectionCompleted(caseData, toCcdRequest(caseDetails)
                .getCaseDetails().getCaseId()))
            .build()));
    }
}
