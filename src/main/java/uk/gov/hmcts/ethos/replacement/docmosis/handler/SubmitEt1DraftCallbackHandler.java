package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1ReppedHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1ReppedService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1SubmissionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.NocRespondentRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.callback.CaseActionsForCaseWorkerCallbackService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class SubmitEt1DraftCallbackHandler extends CallbackHandlerBase {

    private final Et1ReppedService et1ReppedService;
    private final CaseActionsForCaseWorkerCallbackService caseActionsForCaseWorkerCallbackService;
    private final FeatureToggleService featureToggleService;
    private final Et1SubmissionService et1SubmissionService;
    private final NocRespondentRepresentativeService nocRespondentRepresentativeService;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    @Autowired
    public SubmitEt1DraftCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        Et1ReppedService et1ReppedService,
        CaseActionsForCaseWorkerCallbackService caseActionsForCaseWorkerCallbackService,
        FeatureToggleService featureToggleService,
        Et1SubmissionService et1SubmissionService,
        NocRespondentRepresentativeService nocRespondentRepresentativeService,
        CaseManagementForCaseWorkerService caseManagementForCaseWorkerService
    ) {
        super(caseDetailsConverter);
        this.et1ReppedService = et1ReppedService;
        this.caseActionsForCaseWorkerCallbackService = caseActionsForCaseWorkerCallbackService;
        this.featureToggleService = featureToggleService;
        this.et1SubmissionService = et1SubmissionService;
        this.nocRespondentRepresentativeService = nocRespondentRepresentativeService;
        this.caseManagementForCaseWorkerService = caseManagementForCaseWorkerService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("submitEt1Draft");
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
        var caseData = details.getCaseData();
        Et1ReppedHelper.setEt1SubmitData(caseData);
        et1ReppedService.addDefaultData(details.getCaseTypeId(), caseData);
        et1ReppedService.addClaimantRepresentativeDetails(caseData, authorizationToken);
        caseActionsForCaseWorkerCallbackService.postDefaultValues(request, authorizationToken);
        if (featureToggleService.isEt1DocGenEnabled()) {
            caseData.setRequiresSubmissionDocuments(YES);
        } else {
            et1SubmissionService.createAndUploadEt1Docs(details, authorizationToken);
        }
        et1SubmissionService.sendEt1ConfirmationMyHmcts(details, authorizationToken);
        Et1ReppedHelper.clearEt1ReppedCreationFields(caseData);
        caseData = nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(caseData);
        return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        var request = toCcdRequest(caseDetails);
        try {
            caseManagementForCaseWorkerService.setHmctsServiceIdSupplementary(request.getCaseDetails());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to set HMCTS service id", exception);
        }

        return toSubmittedCallbackResponse(org.springframework.http.ResponseEntity.ok(
            uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse.builder()
                .data(request.getCaseDetails().getCaseData())
                .confirmation_header("<h1>You have submitted the ET1 claim</h1>")
                .confirmation_body("""
                                       <h3>What happens next</h3>

                                       The tribunal will send you updates as the claim progresses.
                                       """)
                .build()
        ));
    }
}
