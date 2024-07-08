package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;

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

    public static final String CASE_STATE_ERROR = " cases have not been Accepted, Vetted, or Submitted.";
    public static final String CASE_BELONG_MULTIPLE_ERROR = " cases already belong to a different multiple";
    public static final String CASE_EXIST_ERROR = " cases do not exist.";
    public static final String LEAD_STATE_ERROR = " lead case has not been Accepted, Vetted, or Submitted.";
    public static final String LEAD_BELONG_MULTIPLE_ERROR = " lead case already belongs to a different multiple";
    public static final String LEAD_EXIST_ERROR = " lead case does not exist.";
    public static final String CASE_BELONGS_DIFFERENT_OFFICE = "Case %s is managed by %s";
    public static final String LEAD_CASE_BELONGS_DIFFERENT_OFFICE = "Lead case %s is managed by %s";
    public static final String CASE_COLLECTION_EXCEEDED_MAX_SIZE =
            "There are %s cases in the multiple. The limit is %s.";
    public static final String LEAD_CASE_CANNOT_BE_REMOVED = " lead case cannot be removed.";
    public static final String CASE_NOT_BELONG_TO_MULTIPLE_ERROR = " cases are not a part of the multiple.";
    public static final int MULTIPLE_MAX_SIZE = 50;

    private static final List<String> VALID_STATES = List.of(ACCEPTED_STATE, VETTED_STATE, SUBMITTED_STATE);

    private final SingleCasesReadingService singleCasesReadingService;

    @Autowired
    public MultipleCreationMidEventValidationService(SingleCasesReadingService singleCasesReadingService) {
        this.singleCasesReadingService = singleCasesReadingService;
    }

    public void multipleCreationValidationLogic(String userToken,
                                                MultipleDetails multipleDetails,
                                                List<String> errors,
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
                        new ArrayList<>(Collections.singletonList(multipleData.getLeadCase())), errors, true);
            }

            List<String> ethosCaseRefCollection =
                    MultiplesHelper.getCaseIdsForMidEvent(multipleData.getCaseIdCollection());

            log.info("Validating case id collection size: {}", ethosCaseRefCollection.size());

            validateCaseReferenceCollectionSize(ethosCaseRefCollection, errors);

            validateCases(userToken, caseTypeId, managingOffice, ethosCaseRefCollection, errors, false);
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

        log.info("Validating case id collection size: {}", ethosCaseRefCollection.size());
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

        log.info("Validate number of cases returned");
        validateNumberCasesReturned(casesToBeRemoved, errors, false, caseRefCollection);

        log.info("Validating cases");
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
                               boolean isLead) {

        if (!caseRefCollection.isEmpty()) {
            List<SubmitEvent> submitEvents = singleCasesReadingService.retrieveSingleCases(
                    userToken, caseTypeId, caseRefCollection, MANUALLY_CREATED_POSITION);

            log.info("Validate number of cases returned");
            validateNumberCasesReturned(submitEvents, errors, isLead, caseRefCollection);

            log.info("Validating cases");
            boolean isScotland = SCOTLAND_BULK_CASE_TYPE_ID.equals(caseTypeId);

            validateSingleCasesState(submitEvents, errors, isLead, managingOffice, isScotland);
        }
    }

    private void validateNumberCasesReturned(List<SubmitEvent> submitEvents,
                                             List<String> errors,
                                             boolean isLead,
                                             List<String> caseRefCollection) {

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
                                          boolean isLead,
                                          String multipleManagingOffice,
                                          boolean isScotland) {

        List<String> listCasesStateError = new ArrayList<>();

        List<String> listCasesMultipleError = new ArrayList<>();

        for (SubmitEvent submitEvent : submitEvents) {
            String ethosCaseReference = submitEvent.getCaseData().getEthosCaseReference();

            if (!VALID_STATES.contains(submitEvent.getState())) {
                log.info("VALIDATION ERROR: state of single case not {}", VALID_STATES.toString());

                listCasesStateError.add(ethosCaseReference);
            }

            if (!isEmptyOrWhitespace(submitEvent.getCaseData().getMultipleReference())) {
                log.info("VALIDATION ERROR: already another multiple");

                listCasesMultipleError.add(ethosCaseReference);
            }

            String singleManagingOffice = submitEvent.getCaseData().getManagingOffice();
            if (!isScotland
                    && !isNullOrEmpty(singleManagingOffice)
                    && !multipleManagingOffice.equals(singleManagingOffice)) {

                String errorMessage = isLead ? LEAD_CASE_BELONGS_DIFFERENT_OFFICE : CASE_BELONGS_DIFFERENT_OFFICE;
                errors.add(String.format(errorMessage, ethosCaseReference, singleManagingOffice));
            }
        }

        if (!listCasesStateError.isEmpty()) {
            String errorMessage = isLead ? LEAD_STATE_ERROR : CASE_STATE_ERROR;

            errors.add(listCasesStateError + errorMessage);
        }

        if (!listCasesMultipleError.isEmpty()) {
            String errorMessage = isLead ? LEAD_BELONG_MULTIPLE_ERROR : CASE_BELONG_MULTIPLE_ERROR;

            errors.add(listCasesMultipleError + errorMessage);
        }
    }
}
