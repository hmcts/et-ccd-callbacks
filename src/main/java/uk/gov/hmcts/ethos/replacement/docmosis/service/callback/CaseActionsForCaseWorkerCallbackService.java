package uk.gov.hmcts.ethos.replacement.docmosis.service.callback;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.UploadDocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DefaultValuesReaderService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1SubmissionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.NocRespondentRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SingleReferenceService;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EMPTY_STRING;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper.buildFlagsImageFileName;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.removeSpacesFromPartyNames;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaseActionsForCaseWorkerCallbackService {

    private static final String LOG_MESSAGE = "received notification request for case reference :    ";
    private static final String SUBMIT_CASE_DRAFT = "SUBMIT_CASE_DRAFT";
    private static final List<String> SUBMISSION_EVENTS = List.of(SUBMIT_CASE_DRAFT, "initiateCase", "submitEt1Draft");
    private static final String CREATE_ECM_CASE = "createEcmCase";

    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private final DefaultValuesReaderService defaultValuesReaderService;
    private final SingleReferenceService singleReferenceService;
    private final EventValidationService eventValidationService;
    private final NocRespondentRepresentativeService nocRespondentRepresentativeService;
    private final FeatureToggleService featureToggleService;
    private final CaseFlagsService caseFlagsService;
    private final CaseManagementLocationService caseManagementLocationService;
    private final Et1SubmissionService et1SubmissionService;

    public ResponseEntity<CCDCallbackResponse> postDefaultValues(
        CCDRequest request,
        String userToken
    ) {
        log.info("POST DEFAULT VALUES ---> " + LOG_MESSAGE + request.getCaseDetails().getCaseId());

        CaseDetails caseDetails = request.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();
        List<String> errors = getValidationDate(request.getEventId(), caseDetails);

        if (errors.isEmpty()) {
            defaultValuesReaderService.setSubmissionReference(caseDetails);
            DefaultValues defaultValues = getPostDefaultValues(caseDetails);
            defaultValuesReaderService.setCaseData(caseData, defaultValues);
            caseManagementForCaseWorkerService.caseDataDefaults(caseData);
            generateEthosCaseReference(caseData, request);
            buildFlagsImageFileName(request.getCaseDetails());
            caseData.setMultipleFlag(caseData.getEcmCaseType() != null
                && Constants.MULTIPLE_CASE_TYPE.equals(caseData.getEcmCaseType()) ? Constants.YES : Constants.NO);
            caseData.setChangeOrganisationRequestField(null);
            UploadDocumentHelper.convertLegacyDocsToNewDocNaming(caseData);
            UploadDocumentHelper.setDocumentTypeForDocumentCollection(caseData);
            DocumentHelper.setDocumentNumbers(caseData);

            if (SUBMISSION_EVENTS.contains(defaultIfEmpty(request.getEventId(), EMPTY_STRING))) {
                caseData = nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(caseData);
            }

            defaultValuesReaderService.setPositionAndOffice(caseDetails.getCaseTypeId(), caseData);

            boolean caseFlagsToggle = featureToggleService.isCaseFlagsEnabled();
            log.info("Caseflags feature flag is {}", caseFlagsToggle);
            if (caseFlagsToggle && caseFlagsService.caseFlagsSetupRequired(caseData)) {
                caseFlagsService.setupCaseFlags(caseData);
            }

            boolean hmcToggle = featureToggleService.isHmcEnabled();
            log.info("HMC feature flag is {}", hmcToggle);
            if (hmcToggle) {
                caseManagementForCaseWorkerService.setPublicCaseName(caseData);
                caseManagementLocationService.setCaseManagementLocationCode(caseData);
            }

            if (featureToggleService.citizenEt1Generation() && SUBMIT_CASE_DRAFT.equals(request.getEventId())) {
                caseDetails.setCaseData(caseData);
                et1SubmissionService.createAndUploadEt1Docs(caseDetails, userToken);
                et1SubmissionService.vexationCheck(caseDetails, userToken);
            }

            removeSpacesFromPartyNames(caseData);
        }

        log.info("PostDefaultValues for case: {} {}", request.getCaseDetails().getCaseTypeId(),
            caseData.getEthosCaseReference());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    private List<String> getValidationDate(String eventId, CaseDetails caseDetails) {
        if (!CREATE_ECM_CASE.equals(eventId)) {
            return eventValidationService.validateReceiptDate(caseDetails);
        }
        return new ArrayList<>();
    }

    private DefaultValues getPostDefaultValues(CaseDetails caseDetails) {
        return defaultValuesReaderService.getDefaultValues(caseDetails.getCaseData().getManagingOffice());
    }

    private void generateEthosCaseReference(CaseData caseData, CCDRequest ccdRequest) {
        if (StringUtils.isBlank(caseData.getEthosCaseReference())) {
            String reference = singleReferenceService.createReference(ccdRequest.getCaseDetails().getCaseTypeId());
            log.info(String.format("Created reference %s for CCD case %s", reference,
                ccdRequest.getCaseDetails().getCaseId()));
            caseData.setEthosCaseReference(reference);
        }
    }
}
