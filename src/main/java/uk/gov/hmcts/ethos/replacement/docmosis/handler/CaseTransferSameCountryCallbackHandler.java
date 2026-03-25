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
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferSameCountryService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;

@Component
public class CaseTransferSameCountryCallbackHandler extends CallbackHandlerBase {

    private final VerifyTokenService verifyTokenService;
    private final CaseTransferSameCountryService caseTransferSameCountryService;
    private final CaseManagementLocationService caseManagementLocationService;
    private final FeatureToggleService featureToggleService;

    @Autowired
    public CaseTransferSameCountryCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService,
        CaseTransferSameCountryService caseTransferSameCountryService,
        CaseManagementLocationService caseManagementLocationService,
        FeatureToggleService featureToggleService
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
        this.caseTransferSameCountryService = caseTransferSameCountryService;
        this.caseManagementLocationService = caseManagementLocationService;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("caseTransferSameCountry");
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
        return toCallbackResponse(transferSameCountry(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        throw new IllegalStateException("Handler does not support submitted callbacks for events: "
            + getHandledEventIds());
    }

    private ResponseEntity<CCDCallbackResponse> transferSameCountry(
        uk.gov.hmcts.et.common.model.ccd.CCDRequest request,
        String userToken
    ) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = caseTransferSameCountryService.transferCase(request.getCaseDetails(), userToken);
        request.getCaseDetails().getCaseData().setSuggestedHearingVenues(null);
        if (featureToggleService.isHmcEnabled() || featureToggleService.isWorkAllocationEnabled()) {
            caseManagementLocationService.setCaseManagementLocationCode(request.getCaseDetails().getCaseData());
            caseManagementLocationService.setCaseManagementLocation(request.getCaseDetails().getCaseData());
        }

        return getCallbackRespEntityErrors(errors, request.getCaseDetails().getCaseData());
    }
}
