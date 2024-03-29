package uk.gov.hmcts.ethos.replacement.docmosis.reports.servingclaims;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.common.Strings;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.et.common.model.listing.items.AdhocReportTypeItem;
import uk.gov.hmcts.et.common.model.listing.types.AdhocReportType;
import uk.gov.hmcts.et.common.model.listing.types.ClaimServedType;
import uk.gov.hmcts.et.common.model.listing.types.ClaimServedTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReportHelper;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN2;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.WEEKEND_DAYS_LIST;

@Service
@Slf4j
public class ServingClaimsReport {

    public ListingData generateReportData(ListingDetails listingDetails, List<SubmitEvent> submitEvents) {
        initReport(listingDetails);
        executeReport(listingDetails, submitEvents);
        return listingDetails.getCaseData();
    }

    private void initReport(ListingDetails listingDetails) {
        ListingData listingData = listingDetails.getCaseData();
        String managingOffice = listingDetails.getCaseData().getManagingOffice();
        String reportOffice = ReportHelper.getReportOffice(listingDetails.getCaseTypeId(), managingOffice);
        AdhocReportType adhocReportType = new AdhocReportType();
        adhocReportType.setReportOffice(reportOffice);
        listingData.setLocalReportsDetailHdr(adhocReportType);
        listingData.setLocalReportsDetail(new ArrayList<>());
    }

    private void executeReport(ListingDetails listingDetails, List<SubmitEvent> submitEvents) {
        populateLocalReportDetail(listingDetails, submitEvents);
        populateLocalReportSummary(listingDetails.getCaseData());
    }

    private void populateLocalReportDetail(ListingDetails listingDetails, List<SubmitEvent> submitEvents) {
        AdhocReportTypeItem adhocReportTypeItem = new AdhocReportTypeItem();
        AdhocReportType adhocReportType = new AdhocReportType();
        adhocReportType.setClaimServedItems(new ArrayList<>());

        if (CollectionUtils.isNotEmpty(submitEvents)) {
            for (SubmitEvent submitEvent : submitEvents) {
                setLocalReportsDetail(adhocReportType, submitEvent.getCaseData());
            }
        }

        adhocReportTypeItem.setValue(adhocReportType);

        int servedClaimItemsCount = adhocReportType.getClaimServedItems().size();
        adhocReportType.setClaimServedTotal(String.valueOf(servedClaimItemsCount));

        ListingData listingData = listingDetails.getCaseData();
        List<AdhocReportTypeItem> reportsDetails = listingData.getLocalReportsDetail();
        reportsDetails.add(adhocReportTypeItem);
        listingDetails.getCaseData().setLocalReportsDetail(reportsDetails);
    }

    private void setLocalReportsDetail(AdhocReportType adhocReportType, CaseData caseData) {

        if (!Strings.isNullOrEmpty(caseData.getReceiptDate())
                && !Strings.isNullOrEmpty(caseData.getClaimServedDate())) {
            LocalDate caseReceiptDate = LocalDate.parse(caseData.getReceiptDate(), OLD_DATE_TIME_PATTERN2);
            LocalDate caseClaimServedDate = LocalDate.parse(caseData.getClaimServedDate(), OLD_DATE_TIME_PATTERN2);
            long actualNumberOfDaysToServingClaim = getNumberOfDays(caseReceiptDate, caseClaimServedDate) + 1;
            long reportedNumberOfDaysToServingClaim = getReportedNumberOfDays(caseReceiptDate, caseClaimServedDate);

            ClaimServedType claimServedType = new ClaimServedType();
            claimServedType.setReportedNumberOfDays(String.valueOf(reportedNumberOfDaysToServingClaim));
            claimServedType.setActualNumberOfDays(String.valueOf(actualNumberOfDaysToServingClaim));
            claimServedType.setCaseReceiptDate(caseReceiptDate.toString());
            claimServedType.setClaimServedDate(caseClaimServedDate.toString());
            claimServedType.setClaimServedCaseNumber(caseData.getEthosCaseReference());

            ClaimServedTypeItem claimServedTypeItem = new ClaimServedTypeItem();
            claimServedTypeItem.setId(String.valueOf(UUID.randomUUID()));
            claimServedTypeItem.setValue(claimServedType);

            adhocReportType.getClaimServedItems().add(claimServedTypeItem);
        }

    }

    private long getNumberOfDays(LocalDate caseReceiptDate, LocalDate claimServedDate) {
        return caseReceiptDate.datesUntil(claimServedDate)
                .filter(d -> !WEEKEND_DAYS_LIST.contains(d.getDayOfWeek()))
                .count();
    }

    private long getReportedNumberOfDays(LocalDate caseReceiptDate, LocalDate caseClaimServedDate) {
        long period = getNumberOfDays(caseReceiptDate, caseClaimServedDate);
        return period >= 5 ? 5 : period;
    }

    private String getTotalServedClaims(AdhocReportType adhocReportType) {
        String totalCount = "0";

        if (CollectionUtils.isNotEmpty(adhocReportType.getClaimServedItems())) {
            totalCount = String.valueOf(adhocReportType.getClaimServedItems().size());
        }

        return totalCount;
    }

    private void populateLocalReportSummary(ListingData caseData) {
        List<AdhocReportTypeItem> adhocReportTypeItemsList = caseData.getLocalReportsDetail();

        if (CollectionUtils.isNotEmpty(adhocReportTypeItemsList)) {
            AdhocReportType adhocReportType = adhocReportTypeItemsList.get(0).getValue();

            for (int dayNumber = 0; dayNumber < 6; dayNumber++) {
                setServedClaimsDetailsByDay(adhocReportType, dayNumber);
            }
        }
    }

    private void setServedClaimsDetailsByDay(AdhocReportType adhocReportType, int dayNumber) {
        String totalServedClaims = getTotalServedClaims(adhocReportType);
        setServedClaimsSummary(adhocReportType, totalServedClaims, dayNumber);
    }

    private List<ClaimServedTypeItem> getServedClaimItemsByDayNumber(AdhocReportType adhocReportType, int dayNumber) {
        return adhocReportType.getClaimServedItems().stream()
            .filter(item -> Integer.parseInt(item.getValue().getReportedNumberOfDays()) == dayNumber)
            .toList();
    }

    private void setServedClaimsSummary(AdhocReportType adhocReportType, String totalServedClaims,
                                        int dayNumber) {
        List<ClaimServedTypeItem> acceptedClaimItems = getServedClaimItemsByDayNumber(adhocReportType, dayNumber);
        String acceptedClaimItemsCount = String.valueOf(acceptedClaimItems.size());

        String percentage = "0";
        if (Integer.parseInt(totalServedClaims) > 0) {
            DecimalFormat decimalFormatter = new DecimalFormat("#.##");
            double result = 100.0 * (acceptedClaimItems.size() / Double.parseDouble(totalServedClaims));
            percentage = String.valueOf(decimalFormatter.format(result));
        }

        switch (dayNumber) {
            case 0:
                adhocReportType.setClaimServedDay1Total(acceptedClaimItemsCount);
                adhocReportType.setClaimServedDay1Percent(percentage);
                break;
            case 1:
                adhocReportType.setClaimServedDay2Total(acceptedClaimItemsCount);
                adhocReportType.setClaimServedDay2Percent(percentage);
                break;
            case 2:
                adhocReportType.setClaimServedDay3Total(acceptedClaimItemsCount);
                adhocReportType.setClaimServedDay3Percent(percentage);
                break;
            case 3:
                adhocReportType.setClaimServedDay4Total(acceptedClaimItemsCount);
                adhocReportType.setClaimServedDay4Percent(percentage);
                break;
            case 4:
                adhocReportType.setClaimServedDay5Total(acceptedClaimItemsCount);
                adhocReportType.setClaimServedDay5Percent(percentage);
                break;
            default:
                adhocReportType.setClaimServed6PlusDaysTotal(acceptedClaimItemsCount);
                adhocReportType.setClaimServed6PlusDaysPercent(percentage);
                break;
        }
    }
}
