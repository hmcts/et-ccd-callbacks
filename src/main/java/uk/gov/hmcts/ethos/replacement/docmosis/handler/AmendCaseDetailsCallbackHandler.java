package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.UploadDocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AddSingleCaseToMultipleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DefaultValuesReaderService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper.buildFlagsImageFileName;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.removeSpacesFromPartyNames;

@Slf4j
@Component
public class AmendCaseDetailsCallbackHandler extends CallbackHandlerBase {

    private static final String LOG_MESSAGE = "received notification request for case reference :    ";

    private final EventValidationService eventValidationService;
    private final DefaultValuesReaderService defaultValuesReaderService;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private final AddSingleCaseToMultipleService addSingleCaseToMultipleService;
    private final FeatureToggleService featureToggleService;
    private final CaseManagementLocationService caseManagementLocationService;

    @Autowired
    public AmendCaseDetailsCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        EventValidationService eventValidationService,
        DefaultValuesReaderService defaultValuesReaderService,
        CaseManagementForCaseWorkerService caseManagementForCaseWorkerService,
        AddSingleCaseToMultipleService addSingleCaseToMultipleService,
        FeatureToggleService featureToggleService,
        CaseManagementLocationService caseManagementLocationService
    ) {
        super(caseDetailsConverter);
        this.eventValidationService = eventValidationService;
        this.defaultValuesReaderService = defaultValuesReaderService;
        this.caseManagementForCaseWorkerService = caseManagementForCaseWorkerService;
        this.addSingleCaseToMultipleService = addSingleCaseToMultipleService;
        this.featureToggleService = featureToggleService;
        this.caseManagementLocationService = caseManagementLocationService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("amendCaseDetails", "amendCaseDetailsClosed");
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
        log.info("AMEND CASE DETAILS ---> " + LOG_MESSAGE + request.getCaseDetails().getCaseId());

        var details = request.getCaseDetails();
        CaseData caseData = details.getCaseData();
        List<String> errors = eventValidationService.validateReceiptDate(details);

        if (!eventValidationService.validateCaseState(details)) {
            errors.add(caseData.getEthosCaseReference() + " Case has not been Accepted.");
        }

        if (!eventValidationService.validateCurrentPosition(details)) {
            errors.add("To set the current position to 'Case closed' and to close the case, "
                + "please take the Close Case action.");
        }

        if (errors.isEmpty()) {
            DefaultValues defaultValues = defaultValuesReaderService.getDefaultValues(caseData.getManagingOffice());
            log.info("Post Default values loaded: " + defaultValues);
            defaultValuesReaderService.setCaseData(caseData, defaultValues);
            caseManagementForCaseWorkerService.dateToCurrentPosition(caseData);
            caseManagementForCaseWorkerService.setEt3ResponseDueDate(caseData);
            caseManagementForCaseWorkerService.setNextListedDate(caseData);
            buildFlagsImageFileName(details);
            UploadDocumentHelper.convertLegacyDocsToNewDocNaming(caseData);
            UploadDocumentHelper.setDocumentTypeForDocumentCollection(caseData);
            String caseTypeId = details.getCaseTypeId();
            addSingleCaseToMultipleService.addSingleCaseToMultipleLogic(
                authorizationToken,
                caseData,
                caseTypeId,
                details.getJurisdiction(),
                details.getCaseId(),
                errors
            );

            if (featureToggleService.isWorkAllocationEnabled() && Constants.SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
                caseManagementLocationService.setCaseManagementLocation(caseData);
            }

            removeSpacesFromPartyNames(caseData);
        }

        return toCallbackResponse(getCallbackRespEntityErrors(errors, caseData));
    }
}
