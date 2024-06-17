package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.SchedulePayload;
import uk.gov.hmcts.et.common.model.bulk.items.CaseIdTypeItem;
import uk.gov.hmcts.et.common.model.bulk.types.CaseType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleObject;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class MultiplesHelperTest {

    private MultipleData multipleData;

    @BeforeEach
    public void setUp()  {
        multipleData = MultipleUtil.getMultipleData();
    }

    @Test
    void addLeadToCaseIdsWhenEmptyCollection() {
        multipleData.setCaseIdCollection(null);
        MultiplesHelper.addLeadToCaseIds(multipleData, "245003/2020");
        assertEquals(1, multipleData.getCaseIdCollection().size());
        assertEquals("245003/2020", multipleData.getCaseIdCollection().get(0).getValue().getEthosCaseReference());
    }

    @Test
    void filterDuplicatedAndEmptyCaseIds() {
        multipleData.getCaseIdCollection().add(createCaseIdTypeItem("3", "245000/2020"));
        multipleData.getCaseIdCollection().add(createCaseIdTypeItem("4", null));
        multipleData.getCaseIdCollection().add(createCaseIdTypeItem("5", "245000/2020"));
        multipleData.getCaseIdCollection().add(createCaseIdTypeItem("6", "245000/2020"));
        multipleData.getCaseIdCollection().add(createCaseIdTypeItem("7", ""));
        assertEquals(7, multipleData.getCaseIdCollection().size());
        List<CaseIdTypeItem> list = MultiplesHelper.filterDuplicatedAndEmptyCaseIds(multipleData);
        assertEquals(2, list.size());
    }

    @Test
    void getCurrentLead() {
        String leadLink = "<a target=\"_blank\" href=\"https://www-ccd.perftest.platform.hmcts.net/v2/case/1604313560561842\">1852013/2020</a>";
        assertEquals("1852013/2020", MultiplesHelper.getCurrentLead(leadLink));
    }

    @Test
    void orderMultiplesStringRef() {
        List<String> refList = Arrays.asList("1800074/2020", "1800074/2021", "1800075/2020", "1800075/2021");
        TreeMap<String, TreeMap<String, String>> expectedResult = new TreeMap<>(Map.of(
                "2020", new TreeMap<>(Map.of("1800074", "1800074/2020", "1800075", "1800075/2020")),
                "2021", new TreeMap<>(Map.of("1800074", "1800074/2021", "1800075", "1800075/2021")))
        );

        assertEquals(expectedResult, MultiplesHelper.createCollectionOrderedByCaseRef(refList));
    }

    @Test
    void orderMultipleObjects() {
        List<MultipleObject> refList = Arrays.asList(
                MultiplesHelper.createMultipleObject("1800074/2020", ""),
                MultiplesHelper.createMultipleObject("1800074/2021", ""),
                MultiplesHelper.createMultipleObject("1800075/2020", ""),
                MultiplesHelper.createMultipleObject("1800075/2021", "")
        );
        TreeMap<String, TreeMap<String, MultipleObject>> expectedResult = new TreeMap<>(Map.of(
                "2020",
                new TreeMap<>(Map.of(
                        "1800074", MultiplesHelper.createMultipleObject("1800074/2020", ""),
                        "1800075", MultiplesHelper.createMultipleObject("1800075/2020", "")
                )),
                "2021",
                new TreeMap<>(Map.of(
                        "1800074", MultiplesHelper.createMultipleObject("1800074/2021", ""),
                        "1800075", MultiplesHelper.createMultipleObject("1800075/2021", "")
                ))
        ));

        assertEquals(expectedResult, MultiplesHelper.createCollectionOrderedByCaseRef(refList));
    }

    @Test
    void orderSchedulePayloads() {
        List<SchedulePayload> refList = Arrays.asList(
                SchedulePayload.builder().ethosCaseRef("1800074/2020").build(),
                SchedulePayload.builder().ethosCaseRef("1800074/2021").build(),
                SchedulePayload.builder().ethosCaseRef("1800075/2020").build(),
                SchedulePayload.builder().ethosCaseRef("1800075/2021").build()
        );

        TreeMap<String, TreeMap<String, SchedulePayload>> expectedResult = new TreeMap<>(Map.of(
                "2020",
                new TreeMap<>(Map.of(
                        "1800074", SchedulePayload.builder().ethosCaseRef("1800074/2020").build(),
                        "1800075", SchedulePayload.builder().ethosCaseRef("1800075/2020").build()
                )),
                "2021",
                new TreeMap<>(Map.of(
                        "1800074", SchedulePayload.builder().ethosCaseRef("1800074/2021").build(),
                        "1800075", SchedulePayload.builder().ethosCaseRef("1800075/2021").build()
                ))));

        assertEquals(expectedResult, MultiplesHelper.createCollectionOrderedByCaseRef(refList));
    }

    @Test
    void orderMultiplesObjectTypNotRecognised() {
        List<Object> refList = Arrays.asList(5, 6, 4, 5);
        TreeMap<Object, Object> expectedResult = new TreeMap<>();
        assertEquals(MultiplesHelper.createCollectionOrderedByCaseRef(refList), expectedResult);
    }

    @Test
    void getCaseIdsForMidEvent_Null() {
        multipleData.setCaseIdCollection(null);

        List<String> caseIdList = MultiplesHelper.getCaseIdsForMidEvent(multipleData.getCaseIdCollection());

        assertEquals(0, caseIdList.size());
        assertEquals("[]", caseIdList.toString());
    }

    @Test
    void getCaseIdsForMidEvent_Empty() {
        createCaseIdCollection(multipleData, 0);

        List<String> caseIdList = MultiplesHelper.getCaseIdsForMidEvent(multipleData.getCaseIdCollection());

        assertEquals(0, caseIdList.size());
        assertEquals("[]", caseIdList.toString());
    }

    @Test
    void getCaseIdsForMidEvent() {
        createCaseIdCollection(multipleData, 10);

        List<String> caseIdList = MultiplesHelper.getCaseIdsForMidEvent(multipleData.getCaseIdCollection());

        assertEquals(10, caseIdList.size());
        assertEquals("[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]", caseIdList.toString());
    }

    private void createCaseIdCollection(MultipleData multipleData, int numberCases) {
        List<CaseIdTypeItem> caseIdCollection = new ArrayList<>();

        for (int i = 0; i < numberCases; i++) {
            caseIdCollection.add(createCaseIdTypeItem(String.valueOf(i), String.valueOf(i)));
        }

        multipleData.setCaseIdCollection(caseIdCollection);

    }

    private CaseIdTypeItem createCaseIdTypeItem(String id, String value) {

        CaseType caseType = new CaseType();
        caseType.setEthosCaseReference(value);

        CaseIdTypeItem caseIdTypeItem = new CaseIdTypeItem();
        caseIdTypeItem.setId(id);
        caseIdTypeItem.setValue(caseType);
        return caseIdTypeItem;

    }
}
