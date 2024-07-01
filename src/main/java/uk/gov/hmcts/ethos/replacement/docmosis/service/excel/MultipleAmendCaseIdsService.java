package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleObject;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;

@Slf4j
@Service("multipleAmendCaseIdsService")
public class MultipleAmendCaseIdsService {

    private final MultipleHelperService multipleHelperService;
    private final MultipleBatchUpdate2Service multipleBatchUpdate2Service;

    @Autowired
    public MultipleAmendCaseIdsService(MultipleHelperService multipleHelperService,
                                       MultipleBatchUpdate2Service multipleBatchUpdate2Service) {
        this.multipleHelperService = multipleHelperService;
        this.multipleBatchUpdate2Service = multipleBatchUpdate2Service;
    }

    public List<MultipleObject> bulkAmendCaseIdsLogic(String userToken,
                                                      MultipleDetails multipleDetails,
                                                      List<String> errors,
                                                      SortedMap<String, Object> multipleObjects) {
        MultipleData multipleData = multipleDetails.getCaseData();
        List<String> newEthosCaseRefCollection = MultiplesHelper.getCaseIds(multipleData);

        log.info("Calculate union new and old cases for multipleReference:{}", multipleData.getMultipleReference());
        List<String> unionLists = concatNewAndOldCases(multipleObjects, newEthosCaseRefCollection);

        String multipleLeadCase = getCurrentLead(multipleData, unionLists.get(0));

        if (!newEthosCaseRefCollection.isEmpty()) {
            log.info("Updating {} singles of multiple with reference {} ",
                    newEthosCaseRefCollection.size(),
                    multipleData.getMultipleReference());

            multipleHelperService.sendUpdatesToSinglesLogic(userToken, multipleDetails, errors, multipleLeadCase,
                    multipleObjects, newEthosCaseRefCollection);
        }

        return generateMultipleObjects(unionLists, multipleObjects);
    }

    public void bulkRemoveCaseIdsLogic(String userToken,
                                       MultipleDetails multipleDetails,
                                       List<String> errors,
                                       SortedMap<String, Object> allMultipleObjects) {
        List<String> toBeRemovedEthosCaseRefCollection =
                MultiplesHelper.getCaseIdsFromCollection(multipleDetails.getCaseData().getAltCaseIdCollection());

        if (!toBeRemovedEthosCaseRefCollection.isEmpty()) {
            multipleBatchUpdate2Service.removeCasesFromCurrentMultiple(
                    userToken, multipleDetails, errors, toBeRemovedEthosCaseRefCollection);

            SortedMap<String, Object> removedMultipleObjects =
                    getMultipleObjectsToRemove(allMultipleObjects, toBeRemovedEthosCaseRefCollection);

            log.info("Sending detach updates to singles");
            multipleHelperService.sendDetachUpdatesToSinglesWithoutConfirmation(
                    userToken, multipleDetails, errors, removedMultipleObjects);
        }
    }

    private SortedMap<String, Object> getMultipleObjectsToRemove(SortedMap<String, Object> allMultipleObjects,
                                                                 List<String> toBeRemovedList) {
        SortedMap<String, Object> multipleObjectsToRemove = new TreeMap<>(allMultipleObjects);

        Set<String> toBeRemovedSet = new HashSet<>(toBeRemovedList);
        multipleObjectsToRemove.entrySet().removeIf(entry -> !toBeRemovedSet.contains(entry.getKey()));

        return multipleObjectsToRemove;
    }

    private String getCurrentLead(MultipleData multipleData, String newLead) {

        if (!isNullOrEmpty(multipleData.getLeadCase())) {

            return MultiplesHelper.getCurrentLead(multipleData.getLeadCase());

        }

        return newLead;

    }

    private List<String> concatNewAndOldCases(SortedMap<String, Object> multipleObjects,
                                              List<String> newEthosCaseRefCollection) {

        log.info("EthosCaseRefCollection: {}", newEthosCaseRefCollection);

        return Stream.concat(newEthosCaseRefCollection.stream(), multipleObjects.keySet().stream())
                .distinct().toList();

    }

    private List<MultipleObject> generateMultipleObjects(List<String> unionLists,
                                                         SortedMap<String, Object> multipleObjects) {

        List<MultipleObject> multipleObjectList = new ArrayList<>();

        for (String ethosCaseRef : unionLists) {

            MultipleObject multipleObject;

            if (multipleObjects.containsKey(ethosCaseRef)) {

                multipleObject = (MultipleObject)multipleObjects.get(ethosCaseRef);

            } else {

                multipleObject = MultiplesHelper.createMultipleObject(ethosCaseRef, "");

            }

            multipleObjectList.add(multipleObject);

        }

        return multipleObjectList;
    }

}
