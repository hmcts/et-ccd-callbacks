package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BF_ACTION_ACAS;

public class BFHelperTest {

    private CaseData caseData;
    private List<BFActionTypeItem> bfActionTypeItemList;

    @BeforeEach
    void setUp() {
        caseData = MultipleUtil.getCaseData("245000/2021");
        bfActionTypeItemList = generateBFActionTypeItems();
    }

    @Test
    void updateBfActionItems() {
        bfActionTypeItemList.getFirst().getValue().setDateEntered(null);
        caseData.setBfActions(bfActionTypeItemList);
        BFHelper.updateBfActionItems(caseData);
        assertEquals(1, caseData.getBfActions().size());
        assertNotNull(caseData.getBfActions().getFirst().getValue().getDateEntered());
    }

    @Test
    void populateDynamicListBfActions() {
        caseData.setBfActions(bfActionTypeItemList);
        BFHelper.populateDynamicListBfActions(caseData);
        assertEquals(1, caseData.getBfActions().size());
    }

    @Test
    void populateDynamicListBfActionsEmpty() {
        caseData.setBfActions(null);
        BFHelper.populateDynamicListBfActions(caseData);
        assertEquals(1, caseData.getBfActions().size());
    }

    public static DynamicFixedListType getBfActionsDynamicFixedList() {
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(Helper.getDefaultBfListItems());
        dynamicFixedListType.setValue(DynamicListHelper.getDynamicValue(BF_ACTION_ACAS));
        return dynamicFixedListType;
    }

    public static List<BFActionTypeItem> generateBFActionTypeItems() {
        BFActionType bfActionType = new BFActionType();
        bfActionType.setAction(getBfActionsDynamicFixedList());
        bfActionType.setCleared("Date Cleared");
        bfActionType.setBfDate("2025-08-12");
        bfActionType.setNotes("Notes");
        bfActionType.setAction(getBfActionsDynamicFixedList());
        bfActionType.setDateEntered("12-08-2025 23:00:00");
        BFActionTypeItem bfActionTypeItem = new BFActionTypeItem();
        bfActionTypeItem.setId(UUID.randomUUID().toString());
        bfActionTypeItem.setValue(bfActionType);
        return new ArrayList<>(Collections.singletonList(bfActionTypeItem));
    }

    @Test
    void updateWaTaskCreationTrackerSetsIsWaTaskCreatedToYesForExpiredBfActions() {
        bfActionTypeItemList.getFirst().getValue().setBfDate(BFHelper.getEffectiveYesterday(
                LocalDate.now()));
        bfActionTypeItemList.getFirst().getValue().setIsWaTaskCreated(null);
        bfActionTypeItemList.getFirst().getValue().setCleared(null);
        caseData.setBfActions(bfActionTypeItemList);
        BFHelper.updateWaTaskCreationTrackerOfBfActionItems(caseData);
        assertEquals("Yes", caseData.getBfActions().getFirst().getValue().getIsWaTaskCreated());
    }

    @Test
    void updateWaTaskCreationTrackerDoesNotChangeIsWaTaskCreatedForNonExpiredBfActions() {
        bfActionTypeItemList.getFirst().getValue().setBfDate(LocalDate.now().plusDays(1).toString());
        bfActionTypeItemList.getFirst().getValue().setIsWaTaskCreated(null);
        bfActionTypeItemList.getFirst().getValue().setCleared(null);
        caseData.setBfActions(bfActionTypeItemList);
        BFHelper.updateWaTaskCreationTrackerOfBfActionItems(caseData);
        assertNull(caseData.getBfActions().getFirst().getValue().getIsWaTaskCreated());
    }

    @Test
    void updateWaTaskCreationTrackerThrowsExceptionForNullBfActions() {
        caseData.setBfActions(null);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> BFHelper.updateWaTaskCreationTrackerOfBfActionItems(caseData));
        assertEquals("No BF Actions found for case reference {} " + caseData.getEthosCaseReference(),
                exception.getMessage());
    }

    @Test
    void updateWaTaskCreationTrackerDoesNotChangeIsWaTaskCreatedForClearedBfActions() {
        bfActionTypeItemList.getFirst().getValue().setBfDate(BFHelper.getEffectiveYesterday(LocalDate.now()));
        bfActionTypeItemList.getFirst().getValue().setIsWaTaskCreated(null);
        bfActionTypeItemList.getFirst().getValue().setCleared("Some cleared value");
        caseData.setBfActions(bfActionTypeItemList);
        BFHelper.updateWaTaskCreationTrackerOfBfActionItems(caseData);
        assertNull(caseData.getBfActions().getFirst().getValue().getIsWaTaskCreated());
    }

    @Test
    void isBfExpiredReturnsTrueForExpiredBfAction() {
        BFActionType bfAction = new BFActionType();
        bfAction.setBfDate(LocalDate.now().minusDays(1).toString());
        bfAction.setCleared(null);
        String yesterday = BFHelper.getEffectiveYesterday(LocalDate.now());
        assertTrue(BFHelper.isBfExpired(bfAction, yesterday));
    }

    @Test
    void testMondayCase() {
        LocalDate monday = LocalDate.of(2025, Month.AUGUST, 11); // Monday
        String result = BFHelper.getEffectiveYesterday(monday);
        assertEquals(UtilHelper.formatCurrentDate2(LocalDate.of(2025, Month.AUGUST, 8)), result);
    }

    @Test
    void testSundayCase() {
        LocalDate sunday = LocalDate.of(2025, Month.AUGUST, 10); // Sunday
        String result = BFHelper.getEffectiveYesterday(sunday);
        assertEquals(UtilHelper.formatCurrentDate2(LocalDate.of(2025, Month.AUGUST, 8)), result);
    }

    @Test
    void testRegularDayCase() {
        LocalDate wednesday = LocalDate.of(2025, Month.AUGUST, 13); // Wednesday
        String result = BFHelper.getEffectiveYesterday(wednesday);
        assertEquals(UtilHelper.formatCurrentDate2(LocalDate.of(2025, Month.AUGUST, 12)), result);
    }
}
