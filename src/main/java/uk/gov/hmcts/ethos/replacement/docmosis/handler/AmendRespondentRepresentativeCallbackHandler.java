package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.ccd.sdk.SubmittedCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.NocRespondentRepresentativeService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;

@Slf4j
@Component
public class AmendRespondentRepresentativeCallbackHandler extends CallbackHandlerBase {

    private static final String LOG_MESSAGE = "received notification request for case reference :    ";
    private static final String EVENT_FIELDS_VALIDATION = "Event fields validation: ";

    private final EventValidationService eventValidationService;
    private final NocRespondentHelper nocRespondentHelper;
    private final NocRespondentRepresentativeService nocRespondentRepresentativeService;
    private final FeatureToggleService featureToggleService;

    @Autowired
    public AmendRespondentRepresentativeCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        EventValidationService eventValidationService,
        NocRespondentHelper nocRespondentHelper,
        NocRespondentRepresentativeService nocRespondentRepresentativeService,
        FeatureToggleService featureToggleService
    ) {
        super(caseDetailsConverter);
        this.eventValidationService = eventValidationService;
        this.nocRespondentHelper = nocRespondentHelper;
        this.nocRespondentRepresentativeService = nocRespondentRepresentativeService;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("amendRespondentRepresentative");
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

        log.info("AMEND RESPONDENT REPRESENTATIVE ---> " + LOG_MESSAGE + request.getCaseDetails().getCaseId());

        CaseData caseData = request.getCaseDetails().getCaseData();
        List<String> errors = eventValidationService.validateRespRepNames(caseData);

        if (errors.isEmpty()) {
            nocRespondentHelper.updateWithRespondentIds(caseData);
            caseData = nocRespondentRepresentativeService.prepopulateOrgAddress(caseData, authorizationToken);

            if (featureToggleService.isHmcEnabled()) {
                nocRespondentRepresentativeService.updateNonMyHmctsOrgIds(caseData.getRepCollection());
            }
        }

        log.info(EVENT_FIELDS_VALIDATION + errors);

        return toCallbackResponse(getCallbackRespEntityErrors(errors, caseData));
    }

    @Override
    SubmittedCallbackResponse submitted(CaseDetails caseDetails) {
        var request = toCallbackRequest(caseDetails);
        log.info("AMEND RESPONDENT REPRESENTATIVE SUBMITTED ---> "
            + LOG_MESSAGE + request.getCaseDetails().getCaseId());
        try {
            nocRespondentRepresentativeService.updateRespondentRepresentativesAccess(request);
        } catch (IOException exception) {
            throw new CcdInputOutputException("Failed to update respondent representatives accesses", exception);
        } catch (GenericServiceException exception) {
            throw new RuntimeException(exception);
        }

        return emptyResponse();
    }
}
