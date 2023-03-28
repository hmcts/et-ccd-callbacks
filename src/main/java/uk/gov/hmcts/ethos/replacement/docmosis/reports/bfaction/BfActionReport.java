package uk.gov.hmcts.ethos.replacement.docmosis.reports.bfaction;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.et.common.model.listing.items.BFDateTypeItem;
import uk.gov.hmcts.et.common.model.listing.items.BFDateTypeItemComparator;
import uk.gov.hmcts.et.common.model.listing.types.BFDateType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReportHelper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN2;

public class BfActionReport {
    public ListingData runReport(ListingDetails listingDetails,
                                 List<SubmitEvent> submitEvents,
                                 String userName) {
        BfActionReportData bfActionReportData = new BfActionReportData();
        ListingData caseData = listingDetails.getCaseData();
        bfActionReportData.setHearingDateType(caseData.getHearingDateType());

        if (!CollectionUtils.isEmpty(submitEvents)) {
            List<BFDateTypeItem> bfDateTypeItems = new ArrayList<>();
            for (SubmitEvent submitEvent : submitEvents) {
                addBfDateTypeItems(submitEvent, caseData, bfDateTypeItems);
            }
            bfDateTypeItems.sort(new BFDateTypeItemComparator());
            bfActionReportData.setBfDateCollection(bfDateTypeItems);
        }
        bfActionReportData.clearReportFields();
        bfActionReportData.setReportType(Constants.BROUGHT_FORWARD_REPORT);
        bfActionReportData.setDocumentName(Constants.BROUGHT_FORWARD_REPORT);
        bfActionReportData.setReportPrintedOnDescription(
                getReportedOnDetail(userName));
        String dateSearchFrom;
        String dateSearchTo;
        if (Constants.RANGE_HEARING_DATE_TYPE.equals(caseData.getHearingDateType())) {
            dateSearchFrom = LocalDate.parse(caseData.getListingDateFrom(), OLD_DATE_TIME_PATTERN2).toString();
            dateSearchTo = LocalDate.parse(caseData.getListingDateTo(), OLD_DATE_TIME_PATTERN2).toString();
        } else {
            dateSearchFrom = LocalDate.parse(caseData.getListingDate(), OLD_DATE_TIME_PATTERN2).toString();
            dateSearchTo = dateSearchFrom;
        }
        setReportListingDate(bfActionReportData, dateSearchFrom, dateSearchTo, caseData.getHearingDateType());

        String managingOffice = caseData.getManagingOffice();
        bfActionReportData.setManagingOffice(
                ReportHelper.getReportOfficeForDisplay(listingDetails.getCaseTypeId(), managingOffice));
        bfActionReportData.setListingDate(caseData.getListingDate());
        bfActionReportData.setListingDateFrom(caseData.getListingDateFrom());
        bfActionReportData.setListingDateTo(caseData.getListingDateTo());
        return bfActionReportData;
    }

    private void setReportListingDate(BfActionReportData reportData,
                                      String listingDateFrom, String listingDateTo, String hearingDateType) {
        if (Constants.SINGLE_HEARING_DATE_TYPE.equals(hearingDateType)) {
            reportData.setListingDate(ReportHelper.getFormattedLocalDate(listingDateFrom));
            reportData.setListingDateFrom(null);
            reportData.setListingDateTo(null);
            reportData.setHearingDateType(hearingDateType);
            String reportedOn = "On " + UtilHelper.listingFormatLocalDate(
                    ReportHelper.getFormattedLocalDate(listingDateFrom));
            reportData.setReportPeriodDescription(getReportTitle(reportedOn, reportData.getOffice()));
        } else {
            reportData.setListingDate(null);
            reportData.setListingDateFrom(ReportHelper.getFormattedLocalDate(listingDateFrom));
            reportData.setListingDateTo(ReportHelper.getFormattedLocalDate(listingDateTo));
            reportData.setHearingDateType(hearingDateType);
            String reportedBetween = "Between " + UtilHelper.listingFormatLocalDate(reportData.getListingDateFrom())
                    + " and " + UtilHelper.listingFormatLocalDate(reportData.getListingDateTo());
            reportData.setReportPeriodDescription(getReportTitle(reportedBetween, reportData.getOffice()));
        }
    }

    private String getReportedOnDetail(String userName) {
        return "Reported on: " + UtilHelper.formatCurrentDate(LocalDate.now()) + "   By: " + userName;
    }

    private String getReportTitle(String reportPeriod, String officeName) {
        return "   Period: " + reportPeriod + "       Office: " + officeName;
    }

    private void addBfDateTypeItems(SubmitEvent submitEvent, ListingData listingData,
        List<BFDateTypeItem> bfDateTypeItems) {
        if (!CollectionUtils.isEmpty(submitEvent.getCaseData().getBfActions())) {
            for (BFActionTypeItem bfActionTypeItem : submitEvent.getCaseData().getBfActions()) {
                BFDateTypeItem bfDateTypeItem = getBFDateTypeItem(bfActionTypeItem, listingData,
                    submitEvent.getCaseData());
                if (bfDateTypeItem != null && bfDateTypeItem.getValue() != null) {
                    bfDateTypeItems.add(bfDateTypeItem);
                }
            }
        }
    }

    private BFDateTypeItem getBFDateTypeItem(BFActionTypeItem bfActionTypeItem,
                                                    ListingData listingData, CaseData caseData) {
        BFActionType bfActionType = bfActionTypeItem.getValue();
        if (!isNullOrEmpty(bfActionType.getBfDate()) && isNullOrEmpty(bfActionType.getCleared())) {
            String bfDate = ReportHelper.getFormattedLocalDate(bfActionType.getBfDate());
            boolean isValidBfDate = ReportHelper.validateMatchingDate(listingData, bfDate);

            if (isValidBfDate) {
                return createBFDateTypeItem(bfActionTypeItem, bfDate, caseData.getEthosCaseReference());
            }
        }
        return null;
    }

    private BFDateTypeItem createBFDateTypeItem(BFActionTypeItem bfActionTypeItem, String bfDate,
                                             String ethosCaseReference) {
        BFActionType bfActionType = bfActionTypeItem.getValue();
        BFDateType bfDateType = new BFDateType();
        bfDateType.setCaseReference(ethosCaseReference);

        if (!isNullOrEmpty(bfActionType.getAllActions())) {
            bfDateType.setBroughtForwardAction(bfActionType.getAllActions());
        } else if (!isNullOrEmpty(bfActionType.getCwActions())) {
            bfDateType.setBroughtForwardAction(bfActionType.getCwActions());
        }

        bfDateType.setBroughtForwardEnteredDate(bfActionType.getDateEntered());
        bfDateType.setBroughtForwardDate(bfDate);

        if (!isNullOrEmpty(bfActionType.getNotes())) {
            String bfReason = bfActionType.getNotes().replace("\n", ". ");
            bfDateType.setBroughtForwardDateReason(bfReason);
        }

        BFDateTypeItem bfDateTypeItem = new BFDateTypeItem();
        bfDateTypeItem.setId(String.valueOf(bfActionTypeItem.getId()));
        bfDateTypeItem.setValue(bfDateType);
        return bfDateTypeItem;
    }
}
