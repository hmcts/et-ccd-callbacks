package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        bfActionTypeItemList.get(0).getValue().setDateEntered(null);
        caseData.setBfActions(bfActionTypeItemList);
        BFHelper.updateBfActionItems(caseData);
        assertEquals(1, caseData.getBfActions().size());
        assertNotNull(caseData.getBfActions().get(0).getValue().getDateEntered());
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
        bfActionType.setBfDate("24-08-2020");
        bfActionType.setNotes("Notes");
        bfActionType.setAction(getBfActionsDynamicFixedList());
        bfActionType.setDateEntered("01-01-2020 23:00:00");
        BFActionTypeItem bfActionTypeItem = new BFActionTypeItem();
        bfActionTypeItem.setId(UUID.randomUUID().toString());
        bfActionTypeItem.setValue(bfActionType);
        return new ArrayList<>(Collections.singletonList(bfActionTypeItem));
    }

}
