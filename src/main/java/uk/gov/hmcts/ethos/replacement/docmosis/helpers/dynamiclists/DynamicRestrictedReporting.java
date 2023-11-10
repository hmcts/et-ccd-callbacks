package uk.gov.hmcts.ethos.replacement.docmosis.helpers.dynamiclists;

import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.RestrictedReportingType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DynamicListHelper;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

public final class DynamicRestrictedReporting {
    private DynamicRestrictedReporting() {
    }

    public static void dynamicRestrictedReporting(CaseData caseData) {
        List<DynamicValueType> listItems = DynamicListHelper.createDynamicRespondentName(
                caseData.getRespondentCollection());
        listItems.add(DynamicListHelper.getDynamicCodeLabel("C: " + caseData.getClaimant(), caseData.getClaimant()));
        listItems.add(DynamicListHelper.getDynamicValue("Judge"));
        listItems.add(DynamicListHelper.getDynamicValue("Both Parties"));
        listItems.add(DynamicListHelper.getDynamicValue("Other"));
        listItems.add(DynamicListHelper.getDynamicValue("None"));

        if (!listItems.isEmpty()) {
            RestrictedReportingType restrictedReporting = caseData.getRestrictedReporting();
            DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
            dynamicFixedListType.setListItems(listItems);
            if (restrictedReporting != null) {
                DynamicValueType dynamicValueType;
                if (restrictedReporting.getDynamicRequestedBy() == null) {
                    restrictedReporting.setDynamicRequestedBy(dynamicFixedListType);
                    if (isNullOrEmpty(restrictedReporting.getRequestedBy())) {
                        dynamicValueType = listItems.get(listItems.size() - 1);
                    } else {
                        dynamicValueType = DynamicListHelper.getDynamicValueParty(caseData, listItems,
                                restrictedReporting.getRequestedBy());
                    }
                } else {
                    dynamicValueType = restrictedReporting.getDynamicRequestedBy().getValue();
                    restrictedReporting.getDynamicRequestedBy().setListItems(listItems);
                }
                restrictedReporting.getDynamicRequestedBy().setValue(dynamicValueType);
            } else {
                RestrictedReportingType restrictedReportingType = new RestrictedReportingType();
                restrictedReportingType.setDynamicRequestedBy(dynamicFixedListType);
                caseData.setRestrictedReporting(restrictedReportingType);
            }
        }
    }

}
