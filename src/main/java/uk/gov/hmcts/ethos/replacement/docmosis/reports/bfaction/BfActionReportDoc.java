package uk.gov.hmcts.ethos.replacement.docmosis.reports.bfaction;

import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.items.BFDateTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ListingHelper;

import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_LINE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.REPORT_OFFICE;

@SuppressWarnings({"PMD.ConfusingTernary", "PDM.CyclomaticComplexity",
    "PMD.ClassWithOnlyPrivateConstructorsShouldBeFinal", "PMD.CognitiveComplexity",
    "PMD.InsufficientStringBufferDeclaration", "PMD.LiteralsFirstInComparisons",
    "PMD.ConsecutiveAppendsShouldReuse" })
public class BfActionReportDoc {

    private static final int ONE_REMAINING_ITEM = 1;

    public StringBuilder getReportDocPart(ListingData data) {
        return getBfActionReportDoc(data);
    }

    private StringBuilder getBfActionReportDoc(ListingData listingData) {
        if (!(listingData instanceof BfActionReportData)) {
            throw new IllegalStateException("ListingData is not instance of BfActionReportData");
        }

        BfActionReportData reportData = (BfActionReportData) listingData;
        StringBuilder sb = ListingHelper.getListingDate(reportData);
        sb.append(REPORT_OFFICE).append(nullCheck(reportData.getOffice())).append(NEW_LINE);
        sb.append("\"bf_list\":[\n");
        sb.append(addBfActionItemsList(reportData.getBfDateCollection()));
        sb.append("],\n");
        return sb;
    }

    private StringBuilder addBfActionItemsList(List<BFDateTypeItem> bfDateTypeItems) {
        StringBuilder bfActionItemsListContent = new StringBuilder();

        if (CollectionUtils.isEmpty(bfDateTypeItems)) {
            return bfActionItemsListContent;
        }

        int itemsCount = bfDateTypeItems.size();
        for (int i = 0; i < itemsCount; i++) {
            bfActionItemsListContent.append(getBfActionRow(bfDateTypeItems.get(i)));
            if ((itemsCount - i) > ONE_REMAINING_ITEM) {
                bfActionItemsListContent.append(",\n");
            }
        }

        return bfActionItemsListContent;
    }

    private StringBuilder getBfActionRow(BFDateTypeItem bfActionItem) {
        StringBuilder rowContent = new StringBuilder();
        rowContent.append("{\n\"Case_No\":\"").append(
            nullCheck(bfActionItem.getValue().getCaseReference())).append(NEW_LINE);
        rowContent.append("\"Bf_Action\":\"").append(
            nullCheck(bfActionItem.getValue().getBroughtForwardAction())).append(NEW_LINE);
        rowContent.append("\"Date_Taken\":\"").append(
            nullCheck(bfActionItem.getValue().getBroughtForwardEnteredDate())).append(NEW_LINE);
        rowContent.append("\"Bf_Date\":\"").append(
            nullCheck(bfActionItem.getValue().getBroughtForwardDate())).append(NEW_LINE);
        rowContent.append("\"Comments\":\"").append(nullCheck(bfActionItem.getValue().getBroughtForwardDateReason()))
            .append("\"\n}");
        return rowContent;
    }
}
