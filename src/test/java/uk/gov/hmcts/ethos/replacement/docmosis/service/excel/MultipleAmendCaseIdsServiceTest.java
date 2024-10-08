package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.bulk.items.CaseIdTypeItem;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleObject;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class MultipleAmendCaseIdsServiceTest {

    @InjectMocks
    private MultipleAmendCaseIdsService multipleAmendCaseIdsService;

    @Mock
    private MultipleHelperService multipleHelperService;
    @Mock
    private MultipleBatchUpdate2Service multipleBatchUpdate2Service;

    private SortedMap<String, Object> multipleObjects;
    private MultipleDetails multipleDetails;
    private String userToken;

    @BeforeEach
    public void setUp() {
        multipleObjects = MultipleUtil.getMultipleObjectsAll();

        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());

        userToken = "authString";
    }

    @Test
    void bulkRemoveCaseIdsLogic() {
        List<CaseIdTypeItem> altCaseIdCollection = new ArrayList<>();
        altCaseIdCollection.add(CaseIdTypeItem.from("245000/2020"));
        multipleDetails.getCaseData().setAltCaseIdCollection(altCaseIdCollection);

        multipleAmendCaseIdsService.bulkRemoveCaseIdsLogic(
                userToken,
                multipleDetails,
                new ArrayList<>(),
                multipleObjects);

        verify(multipleBatchUpdate2Service, times(1))
                .removeCasesFromCurrentMultiple(any(), any(), any(), eq(List.of("245000/2020")));

        SortedMap<String, Object> expectedRemoveMultipleObjects = new TreeMap<>();
        expectedRemoveMultipleObjects.put("245000/2020", multipleObjects.get("245000/2020"));
        verify(multipleHelperService, times(1))
                .sendDetachUpdatesToSinglesWithoutConfirmation(any(), any(), any(), eq(expectedRemoveMultipleObjects));
    }

    @Test
    void bulkRemoveCaseIdsLogic_EmptyList() {
        multipleDetails.getCaseData().setAltCaseIdCollection(null);

        multipleAmendCaseIdsService.bulkRemoveCaseIdsLogic(
                userToken,
                multipleDetails,
                new ArrayList<>(),
                multipleObjects);

        verify(multipleBatchUpdate2Service, never())
                .removeCasesFromCurrentMultiple(any(), any(), any(), any());
        verify(multipleHelperService, never())
                .sendDetachUpdatesToSinglesWithoutConfirmation(any(), any(), any(), any());
    }

    @Test
    void bulkAmendCaseIdsLogic() {
        List<MultipleObject> multipleObjectList =
                multipleAmendCaseIdsService.bulkAmendCaseIdsLogic(
                        userToken,
                        multipleDetails,
                        new ArrayList<>(),
                        multipleObjects);

        verify(multipleHelperService, times(1))
                .sendUpdatesToSinglesLogic(any(), any(), any(), eq("21006/2020"), any(), any());
        assertEquals(multipleObjectList, getMultipleObjectsList());
    }

    @Test
    void bulkAmendCaseIdsLogic_EmptyLead() {
        multipleDetails.getCaseData().setLeadCase(null);
        List<MultipleObject>  multipleObjectList =
                multipleAmendCaseIdsService.bulkAmendCaseIdsLogic(
                        userToken,
                        multipleDetails,
                        new ArrayList<>(),
                        multipleObjects);

        verify(multipleHelperService, times(1))
                .sendUpdatesToSinglesLogic(any(), any(), any(), eq("245000/2020"), any(), any());
        assertEquals(multipleObjectList, getMultipleObjectsList());
    }

    @Test
    void bulkAmendCaseIdsLogic_EmptyCaseIdCollection() {
        multipleDetails.getCaseData().setCaseIdCollection(null);
        List<MultipleObject>  multipleObjectList =
                multipleAmendCaseIdsService.bulkAmendCaseIdsLogic(
                        userToken,
                        multipleDetails,
                        new ArrayList<>(),
                        multipleObjects);

        verify(multipleHelperService, never())
                .sendUpdatesToSinglesLogic(any(), any(), any(), any(), any(), any());

        List<MultipleObject> expectedList = getMultipleObjectsList();
        expectedList.remove(1);
        assertEquals(multipleObjectList, expectedList);
    }

    private List<MultipleObject> getMultipleObjectsList() {
        return new ArrayList<>(Arrays.asList(
                MultipleObject.builder()
                        .subMultiple("245000")
                        .ethosCaseRef("245000/2020")
                        .flag1("AA")
                        .flag2("BB")
                        .flag3("")
                        .flag4("")
                        .build(),
                MultipleObject.builder()
                        .subMultiple("")
                        .ethosCaseRef("245001/2020")
                        .flag1("")
                        .flag2("")
                        .flag3("")
                        .flag4("")
                        .build(),
                MultipleObject.builder()
                        .subMultiple("245003")
                        .ethosCaseRef("245003/2020")
                        .flag1("AA")
                        .flag2("EE")
                        .flag3("")
                        .flag4("")
                        .build(),
                MultipleObject.builder()
                        .subMultiple("245002")
                        .ethosCaseRef("245004/2020")
                        .flag1("AA")
                        .flag2("BB")
                        .flag3("")
                        .flag4("")
                        .build(),
                MultipleObject.builder()
                        .subMultiple("SubMultiple")
                        .ethosCaseRef("245005/2020")
                        .flag1("AA")
                        .flag2("BB")
                        .flag3("")
                        .flag4("")
                        .build()));
    }

}