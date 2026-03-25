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
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.admin.TseAdminService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.APPLICATION_SUBMITTED_BODY_TEMPLATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.isClaimantNonSystemUser;

@Component
public class TseAdminCallbackHandler extends CallbackHandlerBase {

    private final VerifyTokenService verifyTokenService;
    private final TseAdminService tseAdminService;
    private final CaseFlagsService caseFlagsService;
    private final FeatureToggleService featureToggleService;

    @Autowired
    public TseAdminCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService,
        TseAdminService tseAdminService,
        CaseFlagsService caseFlagsService,
        FeatureToggleService featureToggleService
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
        this.tseAdminService = tseAdminService;
        this.caseFlagsService = caseFlagsService;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("tseAdmin");
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
        if (!verifyTokenService.verifyTokenSignature(authorizationToken)) {
            return toCallbackResponse(ResponseEntity.status(FORBIDDEN.value()).build());
        }

        var ccdRequest = toCcdRequest(caseDetails);
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        tseAdminService.saveTseAdminDataFromCaseData(caseData);
        if (!isClaimantNonSystemUser(caseData)) {
            tseAdminService.sendEmailToClaimant(ccdRequest.getCaseDetails().getCaseId(), caseData);
        }
        tseAdminService.sendNotifyEmailsToRespondents(ccdRequest.getCaseDetails());
        tseAdminService.clearTseAdminDataFromCaseData(caseData);

        if (featureToggleService.isHmcEnabled()) {
            caseFlagsService.setPrivateHearingFlag(caseData);
        }

        return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        if (!verifyTokenService.verifyTokenSignature(authorizationToken)) {
            return toSubmittedCallbackResponse(ResponseEntity.status(FORBIDDEN.value()).build());
        }

        var ccdRequest = toCcdRequest(caseDetails);
        String body = String.format(APPLICATION_SUBMITTED_BODY_TEMPLATE, ccdRequest.getCaseDetails().getCaseId());
        return toSubmittedCallbackResponse(
            ResponseEntity.ok(CCDCallbackResponse.builder().confirmation_body(body).build())
        );
    }
}
