package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ET1_ONLINE_CASE_SOURCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MANUALLY_CREATED_POSITION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MIGRATION_CASE_SOURCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.VETTED_STATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper.isEmptyOrWhitespace;

@Slf4j
@Service("multipleCreationMidEventValidationService")

public class MultipleCreationMidEventValidationService {

    public static final String CASE_STATE_VETTED_WARNING =
            " cases have not been Accepted, but are Vetted. If this is permissible please click Ignore and Continue";
    public static final String CASE_STATE_SUBMITTED_WARNING =
            " cases have not been Accepted, but are Submitted. If this is permissible please click Ignore and Continue";

    public static final String CASE_STATE_ERROR = " cases have not been Accepted, Vetted, or Submitted.";
    public static final String CASE_STATE_ERROR_OLD = " cases have not been Accepted.";
    public static final String CASE_BELONG_MULTIPLE_ERROR = " cases already belong to a different multiple";
    public static final String CASE_EXIST_ERROR = " cases do not exist.";

    public static final String LEAD_STATE_ERROR = " lead case has not been Accepted, Vetted, or Submitted.";
    public static final String LEAD_STATE_ERROR_OLD = " lead case has not been Accepted.";
    public static final String LEAD_BELONG_MULTIPLE_ERROR = " lead case already belongs to a different multiple";
    public static final String LEAD_EXIST_ERROR = " lead case does not exist.";
    public static final String CASE_BELONGS_DIFFERENT_OFFICE = "Case %s is managed by %s";
    public static final String LEAD_CASE_BELONGS_DIFFERENT_OFFICE = "Lead case %s is managed by %s";
    public static final String CASE_COLLECTION_EXCEEDED_MAX_SIZE =
            "There are %s cases in the multiple. The limit is %s.";
    public static final String LEAD_CASE_CANNOT_BE_REMOVED = " lead case cannot be removed.";
    public static final String CASE_NOT_BELONG_TO_MULTIPLE_ERROR = " cases are not a part of the multiple.";
    public static final int MULTIPLE_MAX_SIZE = 50;

    private final SingleCasesReadingService singleCasesReadingService;
    private final FeatureToggleService featureToggleService;

    @Autowired
    public MultipleCreationMidEventValidationService(SingleCasesReadingService singleCasesReadingService,
                                                     FeatureToggleService featureToggleService) {
        this.singleCasesReadingService = singleCasesReadingService;
        this.featureToggleService = featureToggleService;
    }

    public void multipleCreationValidationLogic(String userToken,
                                                MultipleDetails multipleDetails,
                                                List<String> errors,
                                                List<String> warnings,
                                                boolean amendAction) {

        MultipleData multipleData = multipleDetails.getCaseData();
        String multipleSource = multipleData.getMultipleSource();
        if (!amendAction
                && (ET1_ONLINE_CASE_SOURCE.equals(multipleSource) || MIGRATION_CASE_SOURCE.equals(multipleSource))) {
            log.info("Skipping validation as ET1 Online Case");

        } else {
            log.info("Validating multiple creation");
            log.info("Checking lead case");

            String caseTypeId = multipleDetails.getCaseTypeId();
            String managingOffice = multipleDetails.getCaseData().getManagingOffice();
            if (!amendAction && !isNullOrEmpty(multipleData.getLeadCase())) {
                log.info("Validating lead case introduced by user: {}", multipleData.getLeadCase());

                validateCases(userToken, caseTypeId, managingOffice,
                        new ArrayList<>(Collections.singletonList(multipleData.getLeadCase())), errors, warnings, true);
            }

            List<String> ethosCaseRefCollection =
                    MultiplesHelper.getCaseIdsForMidEvent(multipleData.getCaseIdCollection());

            validateCaseReferenceCollectionSize(ethosCaseRefCollection, errors);

            validateCases(userToken, caseTypeId, managingOffice, ethosCaseRefCollection, errors, warnings, false);
        }
    }

    public void multipleRemoveCasesValidationLogic(
            String userToken,
            MultipleDetails multipleDetails,
            List<String> errors) {

        MultipleData multipleData = multipleDetails.getCaseData();

        log.info("Validating multiple case removals");
        List<String> ethosCaseRefCollection =
                MultiplesHelper.getCaseIdsForMidEvent(multipleData.getAltCaseIdCollection());

        if (ethosCaseRefCollection.isEmpty()) {
            return;
        }

        validateCaseReferenceCollectionSize(ethosCaseRefCollection, errors);

        String caseTypeId = multipleDetails.getCaseTypeId();
        List<SubmitEvent> casesToBeRemoved = singleCasesReadingService.retrieveSingleCases(
                userToken, caseTypeId, ethosCaseRefCollection, MANUALLY_CREATED_POSITION);

        String leadCaseRef = MultiplesHelper.getCurrentLead(multipleData.getLeadCase());
        String multipleRef = multipleData.getMultipleReference();
        validateCasesForRemoval(leadCaseRef, multipleRef, ethosCaseRefCollection, casesToBeRemoved, errors);
    }

    private void validateCasesForRemoval(String leadCaseReference,
                                         String multipleReference,
                                         List<String> caseRefCollection,
                                         List<SubmitEvent> casesToBeRemoved,
                                         List<String> errors) {

        validateNumberCasesReturned(casesToBeRemoved, errors, false, caseRefCollection);

        log.info("Validating cases for removal");
        List<String> listCasesNotBelongError = new ArrayList<>();
        for (SubmitEvent submitEvent : casesToBeRemoved) {
            CaseData caseBeingValidated = submitEvent.getCaseData();

            if (leadCaseReference.equals(caseBeingValidated.getEthosCaseReference())) {
                log.info("VALIDATION ERROR: case is lead case and cannot be removed");
                errors.add(leadCaseReference + LEAD_CASE_CANNOT_BE_REMOVED);
            }

            if (isNullOrEmpty(caseBeingValidated.getMultipleReference())
                    || !multipleReference.equals(caseBeingValidated.getMultipleReference())) {
                log.info("VALIDATION ERROR: case does not belong to this multiple");

                listCasesNotBelongError.add(submitEvent.getCaseData().getEthosCaseReference());
            }
        }

        if (!listCasesNotBelongError.isEmpty()) {
            errors.add(listCasesNotBelongError + CASE_NOT_BELONG_TO_MULTIPLE_ERROR);
        }
    }

    private void validateCaseReferenceCollectionSize(List<String> ethosCaseRefCollection, List<String> errors) {
        log.info("Validating case id collection size: {}", ethosCaseRefCollection.size());
        if (ethosCaseRefCollection.size() > MULTIPLE_MAX_SIZE) {
            log.info("Case id collection reached the max size");

            String errorMessage =
                    String.format(CASE_COLLECTION_EXCEEDED_MAX_SIZE, ethosCaseRefCollection.size(), MULTIPLE_MAX_SIZE);
            errors.add(errorMessage);
        }
    }

    private void validateCases(String userToken,
                               String caseTypeId,
                               String managingOffice,
                               List<String> caseRefCollection,
                               List<String> errors,
                               List<String> warnings,
                               boolean isLead) {

        if (!caseRefCollection.isEmpty()) {
            List<SubmitEvent> submitEvents = singleCasesReadingService.retrieveSingleCases(
                    userToken, caseTypeId, caseRefCollection, MANUALLY_CREATED_POSITION);

            validateNumberCasesReturned(submitEvents, errors, isLead, caseRefCollection);

            log.info("Validating case states");
            boolean isScotland = SCOTLAND_BULK_CASE_TYPE_ID.equals(caseTypeId);
            validateSingleCasesState(submitEvents, errors, warnings, isLead, managingOffice, isScotland);
        }
    }

    private void validateNumberCasesReturned(List<SubmitEvent> submitEvents,
                                             List<String> errors,
                                             boolean isLead,
                                             List<String> caseRefCollection) {
        log.info("Validate number of cases returned");

        if (caseRefCollection.size() != submitEvents.size()) {
            log.info("List returned is different");

            List<String> listCasesDoNotExistError = caseRefCollection.stream()
                    .filter(caseRef ->
                            submitEvents.stream()
                                    .noneMatch(submitEvent ->
                                            submitEvent.getCaseData().getEthosCaseReference().equals(caseRef)))
                    .toList();

            if (!listCasesDoNotExistError.isEmpty()) {
                String errorMessage = isLead ? LEAD_EXIST_ERROR : CASE_EXIST_ERROR;

                errors.add(listCasesDoNotExistError + errorMessage);
            }
        }
    }

    private void validateSingleCasesState(List<SubmitEvent> submitEvents,
                                          List<String> errors,
                                          List<String> warnings,
                                          boolean isLead,
                                          String multipleManagingOffice,
                                          boolean isScotland) {

        List<String> listCasesStateVettingWarnings = new ArrayList<>();
        List<String> listCasesStateSubmittedWarnings = new ArrayList<>();
        List<String> listCasesStateError = new ArrayList<>();

        List<String> listCasesMultipleError = new ArrayList<>();

        for (SubmitEvent submitEvent : submitEvents) {
            String ethosCaseReference = submitEvent.getCaseData().getEthosCaseReference();

            if (featureToggleService.isMultiplesEnabled()) {
                validateState(submitEvent.getState(),
                        listCasesStateVettingWarnings,
                        listCasesStateSubmittedWarnings,
                        listCasesStateError,
                        ethosCaseReference);
            }
            else {
                if (!ACCEPTED_STATE.equals(submitEvent.getState())) {
                    log.info("VALIDATION ERROR: state of single case not Accepted");
                    listCasesStateError.add(submitEvent.getCaseData().getEthosCaseReference());
                }
            }

            CaseData caseData = submitEvent.getCaseData();
            validateMultipleReference(caseData.getMultipleReference(), listCasesMultipleError, ethosCaseReference);

            validateManagingOffice(isScotland,
                    isLead,
                    caseData.getManagingOffice(),
                    multipleManagingOffice,
                    errors,
                    ethosCaseReference);
        }

        if (!listCasesStateError.isEmpty()) {
            String errorMessage = isLead ? LEAD_STATE_ERROR_OLD : CASE_STATE_ERROR_OLD;
            if (featureToggleService.isMultiplesEnabled()) {
                errorMessage = isLead ? LEAD_STATE_ERROR : CASE_STATE_ERROR;
            }

            errors.add(listCasesStateError + errorMessage);
        }

        if (!listCasesStateVettingWarnings.isEmpty()) {
            warnings.add(listCasesStateVettingWarnings + CASE_STATE_VETTED_WARNING);
        }
        if (!listCasesStateSubmittedWarnings.isEmpty()) {
            warnings.add(listCasesStateSubmittedWarnings + CASE_STATE_SUBMITTED_WARNING);
        }

        if (!listCasesMultipleError.isEmpty()) {
            String errorMessage = isLead ? LEAD_BELONG_MULTIPLE_ERROR : CASE_BELONG_MULTIPLE_ERROR;

            errors.add(listCasesMultipleError + errorMessage);
        }
    }

    private void validateManagingOffice(boolean isScotland,
                                        boolean isLead,
                                        String singleManagingOffice,
                                        String multipleManagingOffice,
                                        List<String> errors,
                                        String ethosCaseReference) {
        if (!isScotland
                && !isNullOrEmpty(singleManagingOffice)
                && !multipleManagingOffice.equals(singleManagingOffice)) {

            String errorMessage = isLead ? LEAD_CASE_BELONGS_DIFFERENT_OFFICE : CASE_BELONGS_DIFFERENT_OFFICE;
            errors.add(String.format(errorMessage, ethosCaseReference, singleManagingOffice));
        }
    }

    private void validateMultipleReference(String multipleReference,
                                           List<String> listCasesMultipleError,
                                           String ethosCaseReference) {
        if (!isEmptyOrWhitespace(multipleReference)) {
            log.info("VALIDATION ERROR: already another multiple");

            listCasesMultipleError.add(ethosCaseReference);
        }
    }

    private void validateState(String state,
                               List<String> listCasesStateVettingWarnings,
                               List<String> listCasesStateSubmittedWarnings,
                               List<String> listCasesStateError,
                               String ethosCaseReference) {
        switch (state) {
            case ACCEPTED_STATE:
                break;
            case VETTED_STATE:
                log.info("VALIDATION WARNING: state of single case is vetted");
                listCasesStateVettingWarnings.add(ethosCaseReference);
                break;
            case SUBMITTED_STATE:
                log.info("VALIDATION WARNING: state of single case is submitted");
                listCasesStateSubmittedWarnings.add(ethosCaseReference);
                break;
            default:
                log.info("VALIDATION ERROR: state of single case not Accepted, Vetted, or Submitted");
                listCasesStateError.add(ethosCaseReference);
                break;
        }
    }
}
