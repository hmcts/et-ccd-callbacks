package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1SubmissionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class GenerateEt1DocumentsCallbackHandler extends CallbackHandlerBase {

    private final FeatureToggleService featureToggleService;
    private final Et1SubmissionService et1SubmissionService;

    @Autowired
    public GenerateEt1DocumentsCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        FeatureToggleService featureToggleService,
        Et1SubmissionService et1SubmissionService
    ) {
        super(caseDetailsConverter);
        this.featureToggleService = featureToggleService;
        this.et1SubmissionService = et1SubmissionService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("generateEt1Documents");
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
        var request = toCcdRequest(caseDetails);
        var details = request.getCaseDetails();
        if (featureToggleService.isEt1DocGenEnabled()) {
            et1SubmissionService.createAndUploadEt1Docs(details, authorizationToken);
            details.getCaseData().setRequiresSubmissionDocuments(null);
        }
        return toCallbackResponse(getCallbackRespEntityNoErrors(details.getCaseData()));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        throw new IllegalStateException("Handler does not support submitted callbacks for events: "
            + getHandledEventIds());
    }
}
