package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DefaultValuesReaderService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper.buildFlagsImageFileName;

@Component
public class AssignCaseCallbackHandler extends CallbackHandlerBase {

    private final VerifyTokenService verifyTokenService;
    private final DefaultValuesReaderService defaultValuesReaderService;
    private final CaseManagementLocationService caseManagementLocationService;
    private final FeatureToggleService featureToggleService;

    @Autowired
    public AssignCaseCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService,
        DefaultValuesReaderService defaultValuesReaderService,
        CaseManagementLocationService caseManagementLocationService,
        FeatureToggleService featureToggleService
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
        this.defaultValuesReaderService = defaultValuesReaderService;
        this.caseManagementLocationService = caseManagementLocationService;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("assignCase");
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
        return toCallbackResponse(assignCase(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }

    private ResponseEntity<CCDCallbackResponse> assignCase(
        uk.gov.hmcts.et.common.model.ccd.CCDRequest request,
        String userToken
    ) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = request.getCaseDetails().getCaseData();
        CaseTransferUtils.setCaseManagingOffice(caseData, request.getCaseDetails().getCaseTypeId());

        if (featureToggleService.isHmcEnabled() || featureToggleService.isWorkAllocationEnabled()) {
            caseManagementLocationService.setCaseManagementLocationCode(caseData);
            caseManagementLocationService.setCaseManagementLocation(caseData);
        }

        DefaultValues defaultValues = defaultValuesReaderService.getDefaultValues(caseData.getManagingOffice());
        defaultValuesReaderService.setCaseData(caseData, defaultValues);
        buildFlagsImageFileName(request.getCaseDetails());

        if (featureToggleService.isHmcEnabled()) {
            caseManagementLocationService.setCaseManagementLocationCode(caseData);
        }

        return getCallbackRespEntityNoErrors(caseData);
    }
}
