package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.SubCaseLegalRepDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleObject;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.et.common.model.multiples.types.MoveCasesType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FilterExcelType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleReferenceService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
@Service("multipleBatchUpdate2Service")
public class MultipleBatchUpdate2Service {
    public static final String REMOVE_USER_ERROR = "Call to remove legal rep from Multiple Case failed for %s";

    private final ExcelDocManagementService excelDocManagementService;
    private final MultipleCasesReadingService multipleCasesReadingService;
    private final ExcelReadingService excelReadingService;
    private final MultipleHelperService multipleHelperService;
    private final CcdClient ccdClient;
    private final MultipleReferenceService multipleReferenceService;

    @Autowired
    public MultipleBatchUpdate2Service(ExcelDocManagementService excelDocManagementService,
                                       MultipleCasesReadingService multipleCasesReadingService,
                                       ExcelReadingService excelReadingService,
                                       MultipleHelperService multipleHelperService,
                                       CcdClient ccdClient,
                                       MultipleReferenceService multipleReferenceService) {
        this.excelDocManagementService = excelDocManagementService;
        this.multipleCasesReadingService = multipleCasesReadingService;
        this.excelReadingService = excelReadingService;
        this.multipleHelperService = multipleHelperService;
        this.ccdClient = ccdClient;
        this.multipleReferenceService = multipleReferenceService;
    }

    public void batchUpdate2Logic(String userToken,
                                  MultipleDetails multipleDetails,
                                  List<String> errors,
                                  SortedMap<String, Object> multipleObjects) {
        MultipleData multipleData = multipleDetails.getCaseData();
        log.info("Batch update type = 2");

        String convertToSingle = multipleData.getMoveCases().getConvertToSingle();
        log.info("Convert to singles {}", convertToSingle);

        List<String> multipleObjectsFiltered = new ArrayList<>(multipleObjects.keySet());
        log.info("Update multiple state to open when batching update2 as there will be No Confirmation when updates");

        multipleDetails.getCaseData().setState(OPEN_STATE);

        if (convertToSingle.equals(YES)) {
            removeCasesFromCurrentMultiple(userToken, multipleDetails, errors, multipleObjectsFiltered);

            log.info("Sending detach updates to singles");
            multipleHelperService.sendDetachUpdatesToSinglesWithoutConfirmation(
                    userToken, multipleDetails, errors, multipleObjects);

        } else {
            MoveCasesType moveCasesType = multipleData.getMoveCases();
            String updatedMultipleRef = moveCasesType.getUpdatedMultipleRef();
            String updatedSubMultipleRef = moveCasesType.getUpdatedSubMultipleRef();
            String currentMultipleRef = multipleData.getMultipleReference();

            if (currentMultipleRef.equals(updatedMultipleRef)) {
                log.info("Updates to the same multiple");

                if (isNullOrEmpty(updatedSubMultipleRef)) {
                    log.info("Keep cases in the same sub-multiple");

                } else {
                    log.info("Reading excel and add sub multiple references");
                    readExcelAndAddSubMultipleRef(userToken, multipleDetails, errors,
                            multipleObjectsFiltered, updatedSubMultipleRef);

                }

            } else {
                log.info("Updates to different multiple");
                updateToDifferentMultiple(userToken, multipleDetails, errors,
                        multipleObjectsFiltered, updatedMultipleRef, updatedSubMultipleRef);

            }
        }
    }

    public void removeCasesFromCurrentMultiple(String userToken,
                                               MultipleDetails multipleDetails,
                                               List<String> errors,
                                               List<String> multipleObjectsFiltered) {
        log.info("Read current excel and remove cases in multiple");
        readCurrentExcelAndRemoveCasesInMultiple(userToken, multipleDetails, errors, multipleObjectsFiltered);

        log.info("Perform actions with the new lead if exists");
        String oldLeadCase = MultiplesHelper.getCurrentLead(multipleDetails.getCaseData().getLeadCase());
        performActionsWithNewLeadCase(userToken, multipleDetails, errors, oldLeadCase, multipleObjectsFiltered);

    }

    private void performActionsWithNewLeadCase(String userToken, MultipleDetails multipleDetails, List<String> errors,
                                               String oldLeadCase, List<String> multipleObjectsFiltered) {
        MultipleData multipleData = multipleDetails.getCaseData();
        String newLeadCase = multipleHelperService.getLeadCaseFromExcel(userToken, multipleData, errors);

        if (newLeadCase.isEmpty()) {
            log.info("Removing lead as it has been already taken out");
            multipleData.setLeadCase(null);

        } else {
            if (multipleObjectsFiltered.contains(oldLeadCase)) {
                log.info("Changing the lead case to: {} as old lead case: {} has been taken out",
                        newLeadCase,
                        oldLeadCase);

                String multipleCaseTypeId = multipleDetails.getCaseTypeId();
                multipleHelperService.addLeadMarkUp(userToken, multipleCaseTypeId, multipleData, newLeadCase, "");

                log.info("Sending single update with the lead flag");

                String multipleCaseId = multipleDetails.getCaseId();
                String multipleJurisdiction = multipleDetails.getJurisdiction();
                multipleHelperService.sendCreationUpdatesToSinglesWithoutConfirmation(
                        userToken, multipleCaseTypeId, multipleJurisdiction, multipleData, errors,
                        new ArrayList<>(Collections.singletonList(newLeadCase)), newLeadCase, multipleCaseId);

            }
        }
    }

    private void updateToDifferentMultiple(String userToken,
                                           MultipleDetails multipleDetails,
                                           List<String> errors,
                                           List<String> multipleObjectsFiltered,
                                           String updatedMultipleRef,
                                           String updatedSubMultipleRef) {

        MultipleData oldMultipleData = multipleDetails.getCaseData();
        log.info("Retrieve Legal Reps from current MultipleDetails: {}", oldMultipleData.getMultipleReference());
        ListTypeItem<SubCaseLegalRepDetails> oldLegalRepsCollection = oldMultipleData.getLegalRepCollection();

        removeCasesFromCurrentMultiple(userToken, multipleDetails, errors, multipleObjectsFiltered);

        String updatedCaseTypeId = multipleDetails.getCaseTypeId();
        SubmitMultipleEvent updatedMultiple = getUpdatedMultiple(userToken, updatedCaseTypeId, updatedMultipleRef);

        MultipleData updatedMultipleData = updatedMultiple.getCaseData();
        String updatedJurisdiction = multipleDetails.getJurisdiction();
        String updatedMultipleCaseId = String.valueOf(updatedMultiple.getCaseId());

        log.info("Update the legal rep collection for updated Multiple");
        if (oldLegalRepsCollection != null && !oldLegalRepsCollection.isEmpty()) {
            List<String> legalRepsToAdd =
                    transferLegalRepCollection(updatedMultipleData, multipleObjectsFiltered, oldLegalRepsCollection);
            addLegalRepsToMultiple(updatedJurisdiction, updatedCaseTypeId, updatedMultipleCaseId, legalRepsToAdd);
        }

        log.info("Add the lead case markUp");
        String updatedMultipleDataLeadCase =
                multipleHelperService.getLeadCaseFromExcel(userToken, updatedMultipleData, errors);
        String updatedLeadCase = checkIfNewMultipleWasEmpty(updatedMultipleDataLeadCase, multipleObjectsFiltered);

        multipleHelperService.addLeadMarkUp(userToken, updatedCaseTypeId, updatedMultipleData, updatedLeadCase, "");

        multipleHelperService.moveCasesAndSendUpdateToMultiple(userToken, updatedSubMultipleRef, updatedJurisdiction,
                updatedCaseTypeId, updatedMultipleCaseId, updatedMultipleData, multipleObjectsFiltered, errors);

        log.info("Sending creation updates to singles");
        multipleHelperService.sendCreationUpdatesToSinglesWithoutConfirmation(
                userToken, updatedCaseTypeId, updatedJurisdiction, updatedMultipleData,
                errors, multipleObjectsFiltered, updatedLeadCase, updatedMultipleCaseId);
    }

    private List<String> transferLegalRepCollection(MultipleData multipleData,
                                                    List<String> casesBeingMoved,
                                                    ListTypeItem<SubCaseLegalRepDetails> allOldLegalRepsCollection) {
        ListTypeItem<SubCaseLegalRepDetails> filteredLegalRepsCollection =
                filterLegalRepCollection(casesBeingMoved, allOldLegalRepsCollection);
        if (filteredLegalRepsCollection.isEmpty()) {
            return new ArrayList<>();
        }

        ListTypeItem<SubCaseLegalRepDetails> newLegalRepCollection =
                multipleData.getLegalRepCollection() == null || multipleData.getLegalRepCollection().isEmpty()
                ? new ListTypeItem<>()
                : multipleData.getLegalRepCollection();

        ListTypeItem<SubCaseLegalRepDetails> collectedLegalRepCollection =
                ListTypeItem.concat(newLegalRepCollection, filteredLegalRepsCollection);

        multipleData.setLegalRepCollection(collectedLegalRepCollection);

        return filteredLegalRepsCollection.stream()
                .flatMap(subCase -> subCase.getValue().getLegalRepIds().stream())
                .map(GenericTypeItem::getValue)
                .toList();
    }

    private ListTypeItem<SubCaseLegalRepDetails> filterLegalRepCollection(
            List<String> casesBeingMoved,
            ListTypeItem<SubCaseLegalRepDetails> allLegalRepsCollection) {
        ListTypeItem<SubCaseLegalRepDetails> legalRepsToMoveCollection = new ListTypeItem<>();

        for (GenericTypeItem<SubCaseLegalRepDetails> caseDetails : allLegalRepsCollection) {
            SubCaseLegalRepDetails currentCaseDetails = caseDetails.getValue();
            if (casesBeingMoved.contains(currentCaseDetails.getCaseReference())) {
                legalRepsToMoveCollection.add(caseDetails);
            }
        }

        return legalRepsToMoveCollection;
    }

    private void addLegalRepsToMultiple(String jurisdiction,
                                        String caseType,
                                        String multipleId,
                                        List<String> legalRepsToAdd) {
        if (!legalRepsToAdd.isEmpty()) {
            multipleReferenceService.addUsersToMultiple(jurisdiction,
                    caseType,
                    multipleId,
                    legalRepsToAdd);
        }
    }

    private String checkIfNewMultipleWasEmpty(String updatedLeadCase, List<String> multipleObjectsFiltered) {
        if (updatedLeadCase.isEmpty() && !multipleObjectsFiltered.isEmpty()) {
            return multipleObjectsFiltered.get(0);
        }

        return updatedLeadCase;
    }

    private void readExcelAndAddSubMultipleRef(String userToken,
                                               MultipleDetails multipleDetails,
                                               List<String> errors,
                                               List<String> multipleObjectsFiltered,
                                               String updatedSubMultipleRef) {

        SortedMap<String, Object> multipleObjects =
                excelReadingService.readExcel(
                        userToken,
                        MultiplesHelper.getExcelBinaryUrl(multipleDetails.getCaseData()),
                        errors,
                        multipleDetails.getCaseData(),
                        FilterExcelType.ALL);

        List<MultipleObject> newMultipleObjectsUpdated = addSubMultipleRefToMultipleObjects(multipleObjectsFiltered,
                multipleObjects, updatedSubMultipleRef, userToken, multipleDetails);

        excelDocManagementService.generateAndUploadExcel(newMultipleObjectsUpdated, userToken, multipleDetails);

    }

    private List<MultipleObject> addSubMultipleRefToMultipleObjects(List<String> multipleObjectsFiltered,
                                                                    SortedMap<String, Object> multipleObjects,
                                                                    String updatedSubMultipleRef,
                                                                    String userToken,
                                                                    MultipleDetails multipleDetails) {

        List<MultipleObject> newMultipleObjectsUpdated = new ArrayList<>();
        multipleObjects.forEach((key, value) -> {
            MultipleObject multipleObject = (MultipleObject) value;
            if (multipleObjectsFiltered.contains(key)) {
                multipleObject.setSubMultiple(updatedSubMultipleRef);
                try {
                    excelReadingService.setSubMultipleFieldInSingleCaseData(userToken,
                            multipleDetails,
                            multipleObject.getEthosCaseRef(),
                            updatedSubMultipleRef);
                } catch (IOException e) {
                    log.error(String.format("Error in setting subMultiple for case %s: %s",
                            multipleObject.getEthosCaseRef(), e));
                }
            }
            newMultipleObjectsUpdated.add(multipleObject);
        });

        return newMultipleObjectsUpdated;

    }

    private void readCurrentExcelAndRemoveCasesInMultiple(String userToken,
                                                          MultipleDetails multipleDetails,
                                                          List<String> errors,
                                                          List<String> multipleObjectsFiltered) {
        MultipleData multipleData = multipleDetails.getCaseData();

        SortedMap<String, Object> multipleObjects =
                excelReadingService.readExcel(
                        userToken,
                        MultiplesHelper.getExcelBinaryUrl(multipleData),
                        errors,
                        multipleDetails.getCaseData(),
                        FilterExcelType.ALL);

        List<MultipleObject> newMultipleObjectsUpdated =
                removeCasesInMultiple(multipleObjectsFiltered, multipleObjects);

        excelDocManagementService.generateAndUploadExcel(newMultipleObjectsUpdated, userToken, multipleDetails);

        if (multipleData.getLegalRepCollection() != null && !multipleData.getLegalRepCollection().isEmpty()) {
            log.info("Remove LR access for removed singles of: {}", multipleData.getMultipleReference());
            removeOldLegalRepsFromMultiple(userToken, multipleDetails, multipleObjectsFiltered);
        }
    }

    private void removeOldLegalRepsFromMultiple(
            String userToken,
            MultipleDetails multipleDetails,
            List<String> casesBeingRemoved) {
        String multipleReference = multipleDetails.getCaseData().getMultipleReference();
        ListTypeItem<SubCaseLegalRepDetails> legalRepCollection = multipleDetails.getCaseData().getLegalRepCollection();

        List<String> legalRepsToRetain = legalRepCollection.stream()
                .filter(subCase -> !casesBeingRemoved.contains(subCase.getValue().getCaseReference()))
                .flatMap(subCase -> subCase.getValue().getLegalRepIds().stream())
                .map(GenericTypeItem::getValue)
                .toList();

        List<String> legalRepsToRemove = legalRepCollection.stream()
                .filter(subCase -> casesBeingRemoved.contains(subCase.getValue().getCaseReference()))
                .flatMap(subCase -> subCase.getValue().getLegalRepIds().stream())
                .filter(legalRep -> !legalRepsToRetain.contains(legalRep.getValue()))
                .map(GenericTypeItem::getValue)
                .toList();

        if (legalRepsToRemove.isEmpty()) {
            log.info("No LegalReps to be removed from : {}", multipleReference);
            return;
        }

        String jurisdiction = multipleDetails.getJurisdiction();
        String caseType = multipleDetails.getCaseTypeId();
        String multipleId = multipleDetails.getCaseId();
        for (String legalRepId : legalRepsToRemove) {
            removeUserFromCase(userToken, jurisdiction, caseType, multipleId, legalRepId);
        }

        log.info("Update legalRepCollection for: {}", multipleReference);
        ListTypeItem<SubCaseLegalRepDetails> newLegalRepCollection = new ListTypeItem<>();
        for (GenericTypeItem<SubCaseLegalRepDetails> caseDetails : legalRepCollection) {
            SubCaseLegalRepDetails currentCaseDetails = caseDetails.getValue();
            if (!casesBeingRemoved.contains(currentCaseDetails.getCaseReference())) {
                newLegalRepCollection.add(GenericTypeItem.from(currentCaseDetails));
            }
        }
        multipleDetails.getCaseData().setLegalRepCollection(newLegalRepCollection);
    }

    private List<MultipleObject> removeCasesInMultiple(List<String> multipleObjectsFiltered,
                                                       SortedMap<String, Object> multipleObjects) {
        List<MultipleObject> newMultipleObjectsUpdated = new ArrayList<>();

        multipleObjects.forEach((key, value) -> {
            MultipleObject multipleObject = (MultipleObject) value;
            if (!multipleObjectsFiltered.contains(key)) {
                newMultipleObjectsUpdated.add(multipleObject);
            }
        });

        return newMultipleObjectsUpdated;
    }

    private SubmitMultipleEvent getUpdatedMultiple(String userToken, String caseTypeId, String updatedMultipleRef) {
        return multipleCasesReadingService.retrieveMultipleCasesWithRetries(
                userToken,
                caseTypeId,
                updatedMultipleRef).get(0);
    }

    private void removeUserFromCase(String userToken,
                               String jurisdiction,
                               String caseType,
                               String multipleId,
                               String userToRemoveId) {
        String errorMessage = String.format(REMOVE_USER_ERROR, multipleId);

        try {
            ResponseEntity<Object> response =
                    ccdClient.removeUserFromMultiple(
                            userToken,
                            jurisdiction,
                            caseType,
                            multipleId,
                            userToRemoveId);

            if (response == null) {
                throw new CaseCreationException(errorMessage);
            }

            log.info("Http status received from CCD removeUserFromMultiple API; {}", response.getStatusCodeValue());
        } catch (IOException | RestClientResponseException e) {
            throw (CaseCreationException)
                    new CaseCreationException(String.format("%s with %s", errorMessage, e.getMessage())).initCause(e);
        }
    }

}
