package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.letters.InvalidCharacterCheck;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.removeSpacesFromPartyNames;

@Slf4j
@Component
public class AmendRespondentDetailsCallbackHandler extends CallbackHandlerBase {

    private static final String LOG_MESSAGE = "received notification request for case reference :    ";
    private static final String EVENT_FIELDS_VALIDATION = "Event fields validation: ";

    private final EventValidationService eventValidationService;
    private final NocRespondentHelper nocRespondentHelper;
    private final FeatureToggleService featureToggleService;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private final CaseFlagsService caseFlagsService;

    @Autowired
    public AmendRespondentDetailsCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        EventValidationService eventValidationService,
        NocRespondentHelper nocRespondentHelper,
        FeatureToggleService featureToggleService,
        CaseManagementForCaseWorkerService caseManagementForCaseWorkerService,
        CaseFlagsService caseFlagsService
    ) {
        super(caseDetailsConverter);
        this.eventValidationService = eventValidationService;
        this.nocRespondentHelper = nocRespondentHelper;
        this.featureToggleService = featureToggleService;
        this.caseManagementForCaseWorkerService = caseManagementForCaseWorkerService;
        this.caseFlagsService = caseFlagsService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("amendRespondentDetails");
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
        var request = toCcdRequest(caseDetails);
        log.info("AMEND RESPONDENT DETAILS ---> " + LOG_MESSAGE + request.getCaseDetails().getCaseId());

        CaseData caseData = request.getCaseDetails().getCaseData();
        List<String> errors = eventValidationService.validateActiveRespondents(caseData);

        if (errors.isEmpty()) {
            errors = eventValidationService.validateET3ResponseFields(caseData);
            if (errors.isEmpty()) {
                errors = InvalidCharacterCheck.checkNamesForInvalidCharacters(caseData, "respondent");
            }
            if (errors.isEmpty()) {
                caseManagementForCaseWorkerService.continuingRespondent(request);
                caseManagementForCaseWorkerService.struckOutRespondents(request);
            }
        }

        eventValidationService.validateMaximumSize(caseData).ifPresent(errors::add);

        if (errors.isEmpty() && isNotEmpty(caseData.getRepCollection())) {
            nocRespondentHelper.amendRespondentNameRepresentativeNames(caseData);
        }

        if (errors.isEmpty() && isNotEmpty(caseData.getRespondentCollection())) {
            caseManagementForCaseWorkerService.updateListOfRespondentsWithAnEcc(caseData);
        }

        if (featureToggleService.isGlobalSearchEnabled()) {
            caseManagementForCaseWorkerService.setCaseNameHmctsInternal(caseData);
        }

        if (featureToggleService.isHmcEnabled()) {
            caseManagementForCaseWorkerService.setPublicCaseName(caseData);
        }

        caseFlagsService.setupCaseFlags(caseData);
        caseManagementForCaseWorkerService.updateWorkAllocationField(errors, caseData);
        removeSpacesFromPartyNames(caseData);

        log.info(EVENT_FIELDS_VALIDATION + errors);

        return toCallbackResponse(getCallbackRespEntityErrors(errors, caseData));
    }
}
