package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.UploadDocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRespondentRepresentativeService;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_SUBMIT_CASE_DRAFT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.SupportTaskConstants.EVENT_UPDATE_CASE_SUBMITTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EMPTY_STRING;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper.buildFlagsImageFileName;

/**
 * Applies the default values a case needs once it has been submitted.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostDefaultValuesService {

    public static final String CREATE_ECM_CASE = "createEcmCase";
    private static final List<String> SUBMISSION_EVENTS = List.of(EVENT_SUBMIT_CASE_DRAFT, "initiateCase",
            "submitEt1Draft", CREATE_ECM_CASE);
    private static final List<String> REVIEW_SUPPORT_TASK_EVENTS =
            List.of(EVENT_SUBMIT_CASE_DRAFT, EVENT_UPDATE_CASE_SUBMITTED);

    private final EventValidationService eventValidationService;
    private final EmploymentRightsActService employmentRightsActService;
    private final DefaultValuesReaderService defaultValuesReaderService;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private final SingleReferenceService singleReferenceService;
    private final NocRespondentRepresentativeService nocRespondentRepresentativeService;
    private final CaseFlagsService caseFlagsService;
    private final SupportTaskService supportTaskService;
    private final FeatureToggleService featureToggleService;
    private final CaseManagementLocationService caseManagementLocationService;
    private final Et1SubmissionService et1SubmissionService;

    /**
     * Validates the receipt date and, if it is valid, applies the post-submission default values.
     *
     * <p>The updated case data is set back on the request's case details, which may hold a different
     * {@link CaseData} instance afterwards, so callers must read it from there rather than keep a reference.
     *
     * @param ccdRequest the callback request whose case details are updated
     * @param userToken the user's bearer token
     * @return validation errors; when not empty, no default values have been applied
     */
    public List<String> applyPostDefaultValues(CCDRequest ccdRequest, String userToken) {
        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        String eventId = ccdRequest.getEventId();
        List<String> errors = CREATE_ECM_CASE.equals(eventId)
            ? List.of()
            : eventValidationService.validateReceiptDate(caseDetails);
        if (!errors.isEmpty()) {
            return errors;
        }

        CaseData caseData = caseDetails.getCaseData();
        employmentRightsActService.setEraFlagByReceiptDate(caseData);
        defaultValuesReaderService.setSubmissionReference(caseDetails);
        DefaultValues defaultValues = defaultValuesReaderService.getDefaultValues(caseData.getManagingOffice());
        defaultValuesReaderService.setCaseData(caseData, defaultValues);
        caseManagementForCaseWorkerService.caseDataDefaults(caseData);
        generateEthosCaseReference(caseDetails);
        buildFlagsImageFileName(caseDetails);
        caseData.setMultipleFlag(caseData.getEcmCaseType() != null
                && Constants.MULTIPLE_CASE_TYPE.equals(caseData.getEcmCaseType()) ? Constants.YES : Constants.NO);
        caseData.setChangeOrganisationRequestField(null);
        UploadDocumentHelper.convertLegacyDocsToNewDocNaming(caseData);
        UploadDocumentHelper.setDocumentTypeForDocumentCollection(caseData);
        DocumentHelper.setDocumentNumbers(caseData);
        // Create the NoC answers section only on case submission events
        if (SUBMISSION_EVENTS.contains(defaultIfEmpty(eventId, EMPTY_STRING))) {
            // Returns a new CaseData, so the defaults below must be applied to it and it must replace the original
            caseData = nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(caseData);
            caseDetails.setCaseData(caseData);
        }
        defaultValuesReaderService.setPositionAndOffice(caseDetails.getCaseTypeId(), caseData);

        if (caseFlagsSetupRequired(caseDetails.getCaseTypeId(), caseData)) {
            setupCaseFlags(caseDetails.getCaseTypeId(), caseData);
        }

        if (featureToggleService.isCaseFlagsV2Enabled(caseDetails.getCaseTypeId())
                && REVIEW_SUPPORT_TASK_EVENTS.contains(eventId)) {
            caseManagementForCaseWorkerService.setNextListedDate(caseData);
            CaseData caseDataBefore = ccdRequest.getCaseDetailsBefore() == null
                    ? null
                    : ccdRequest.getCaseDetailsBefore().getCaseData();
            if (EVENT_SUBMIT_CASE_DRAFT.equals(eventId)) {
                supportTaskService.prepareReviewSupportTasks(caseData);
            } else {
                supportTaskService.prepareUpdatedCaseReviewSupportTasks(caseData, caseDataBefore);
            }
            supportTaskService.prepareArrangeSupportTask(caseData, caseDataBefore);
        }

        boolean hmcToggle = featureToggleService.isHmcEnabled();
        log.info("HMC feature flag is {}", hmcToggle);
        if (hmcToggle) {
            caseManagementForCaseWorkerService.setPublicCaseName(caseData);
            caseManagementLocationService.setCaseManagementLocationCode(caseData);
        }

        if (featureToggleService.citizenEt1Generation() && EVENT_SUBMIT_CASE_DRAFT.equals(eventId)) {
            et1SubmissionService.createAndUploadEt1Docs(caseDetails, userToken);
            et1SubmissionService.vexationCheck(caseDetails, userToken);
        }

        log.info("PostDefaultValues for case: {} {}", caseDetails.getCaseTypeId(), caseData.getEthosCaseReference());
        return List.of();
    }

    private boolean caseFlagsSetupRequired(String caseTypeId, CaseData caseData) {
        return featureToggleService.isCaseFlagsV2Enabled(caseTypeId)
                ? caseFlagsService.caseFlagsSetupRequired(caseData)
                : caseFlagsService.legacyCaseFlagsSetupRequired(caseData);
    }

    private void setupCaseFlags(String caseTypeId, CaseData caseData) {
        if (featureToggleService.isCaseFlagsV2Enabled(caseTypeId)) {
            caseFlagsService.setupCaseFlags(caseData);
        } else {
            caseFlagsService.setupLegacyCaseFlags(caseData);
        }
    }

    private void generateEthosCaseReference(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        if (StringUtils.isBlank(caseData.getEthosCaseReference())) {
            String reference = singleReferenceService.createReference(caseDetails.getCaseTypeId());
            log.info("Created reference {} for CCD case {}", reference, caseDetails.getCaseId());
            caseData.setEthosCaseReference(reference);
        }
    }
}
