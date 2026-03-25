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
public class CreateDraftEt1CallbackHandler extends CallbackHandlerBase {

    private static final String GENERATED_DOCUMENT_URL = "Please download the draft ET1 from : ";

    private final Et1ReppedService et1ReppedService;

    @Autowired
    public CreateDraftEt1CallbackHandler(
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
        return List.of("createDraftEt1");
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
        var details = request.getCaseDetails();
        Et1ReppedHelper.setEt1SubmitData(details.getCaseData());
        et1ReppedService.addDefaultData(details.getCaseTypeId(), details.getCaseData());
        et1ReppedService.addClaimantRepresentativeDetails(details.getCaseData(), authorizationToken);
        et1ReppedService.createDraftEt1(details, authorizationToken);
        return toCallbackResponse(getCallbackRespEntityNoErrors(details.getCaseData()));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        var request = toCcdRequest(caseDetails);
        var details = request.getCaseDetails();
        return toSubmittedCallbackResponse(ResponseEntity.ok(CCDCallbackResponse.builder()
            .data(details.getCaseData())
            .confirmation_body(GENERATED_DOCUMENT_URL + details.getCaseData().getDocMarkUp()
                + "\n\n"
                + Et1ReppedHelper.getSectionCompleted(details.getCaseData(), details.getCaseId()))
            .build()));
    }
}
