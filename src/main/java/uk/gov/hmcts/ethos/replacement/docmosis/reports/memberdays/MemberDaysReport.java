package uk.gov.hmcts.ethos.replacement.docmosis.reports.memberdays;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReportHelper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.groupingBy;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MEMBER_DAYS_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportCommonMethods.getHearingDurationInMinutes;

@Slf4j
@SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops", "PMD.LiteralsFirstInComparisons",
    "PMD.FieldNamingConventions", "PMD.LawOfDemeter"})
public class MemberDaysReport {
    private static final int MINUTES = 60;
    private static final String FULL_PANEL = "Full Panel";
    public static final DateTimeFormatter OLD_DATE_TIME_PATTERN3 =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public MemberDaysReportData runReport(ListingDetails listings, List<SubmitEvent> submitEventList) {

        MemberDaysReportData memberDaysReportData = initiateReport(listings);

        if (!CollectionUtils.isEmpty(submitEventList)) {
            addReportDetails(memberDaysReportData, submitEventList, listings.getCaseData());
            addReportSummary(memberDaysReportData);
            addReportSummaryHeader(memberDaysReportData);
        }

        return memberDaysReportData;
    }

    private MemberDaysReportData initiateReport(ListingDetails listingDetails) {
        MemberDaysReportData reportData = new MemberDaysReportData();
        ListingData caseData = listingDetails.getCaseData();
        String reportOffice = ReportHelper.getReportOffice(listingDetails.getCaseTypeId(),
            caseData.getManagingOffice());

        reportData.setOffice(reportOffice);
        reportData.setManagingOffice(reportOffice);
        reportData.setHearingDateType(caseData.getHearingDateType());
        reportData.setReportType(MEMBER_DAYS_REPORT);
        reportData.setDocumentName(MEMBER_DAYS_REPORT);
        reportData.setListingDate(caseData.getListingDate());
        reportData.setListingDateFrom(caseData.getListingDateFrom());
        reportData.setListingDateTo(caseData.getListingDateTo());
        return reportData;
    }

    private void addReportDetails(MemberDaysReportData reportData, List<SubmitEvent> submitEvents,
                                  ListingData listingData) {

        List<MemberDaysReportDetail> interimReportDetails = new ArrayList<>();

        for (SubmitEvent submitEvent : submitEvents) {
            if (CollectionUtils.isEmpty(submitEvent.getCaseData().getHearingCollection())) {
                continue;
            }
            addValidHearingsFromCurrentCase(submitEvent.getCaseData(), interimReportDetails, listingData);
        }

        List<MemberDaysReportDetail> sortedReportDetails = interimReportDetails.stream()
            .sorted(MemberDaysReportDetail::comparedTo).collect(Collectors.toList());
        reportData.getReportDetails().clear();
        sortedReportDetails.forEach(d -> reportData.getReportDetails().add(d));
    }

    private void addValidHearingsFromCurrentCase(CaseData caseData,
                                                 List<MemberDaysReportDetail> reportDetails,
                                                 ListingData listingData) {
        List<HearingTypeItem> fullPanelHearings = caseData.getHearingCollection().stream()
            .filter(this::isFullPanelHearing).collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(fullPanelHearings)) {
            for (HearingTypeItem hearing : fullPanelHearings) {
                extractValidHearingDates(hearing, reportDetails, listingData, caseData.getEthosCaseReference());
            }
        }
    }

    private boolean isFullPanelHearing(HearingTypeItem hearing) {
        return FULL_PANEL.equals(hearing.getValue().getHearingSitAlone());
    }

    private boolean isHeardHearingDate(DateListedTypeItem hearingDate) {
        return HEARING_STATUS_HEARD.equals(hearingDate.getValue().getHearingStatus());
    }

    private void extractValidHearingDates(HearingTypeItem hearing,
                                                 List<MemberDaysReportDetail> interimReportDetails,
                                                 ListingData listingData, String ethosCaseReference) {
        List<DateListedTypeItem> hearingDatesWithHeardStatus = hearing.getValue().getHearingDateCollection().stream()
            .filter(this::isHeardHearingDate).collect(Collectors.toList());

        for (DateListedTypeItem hearingDate : hearingDatesWithHeardStatus) {
            String currentHearingDate = ReportHelper.getFormattedLocalDate(hearingDate.getValue().getListedDate());
            if (currentHearingDate != null && isValidHearingDate(currentHearingDate, listingData)) {
                MemberDaysReportDetail reportDetail = new MemberDaysReportDetail();
                reportDetail.setSortingHearingDate(currentHearingDate);
                reportDetail.setHearingDate(UtilHelper.formatCurrentDate(LocalDate.parse(currentHearingDate)));
                if (hearing.getValue().hasHearingEmployeeMember()) {
                    reportDetail.setEmployeeMember(hearing.getValue().getHearingEEMember().getSelectedLabel());
                }
                if (hearing.getValue().hasHearingEmployerMember()) {
                    reportDetail.setEmployerMember(hearing.getValue().getHearingERMember().getSelectedLabel());
                }
                reportDetail.setCaseReference(ethosCaseReference);
                reportDetail.setHearingNumber(hearing.getValue().getHearingNumber());
                reportDetail.setHearingType(hearing.getValue().getHearingType());
                if (hearingDate.getValue().hasHearingClerk()) {
                    reportDetail.setHearingClerk(hearingDate.getValue().getHearingClerk().getSelectedLabel());
                }
                reportDetail.setHearingDuration(getHearingDurationInMinutes(hearingDate));
                reportDetail.setParentHearingId(hearing.getId());
                interimReportDetails.add(reportDetail);
            }
        }
    }

    private boolean isValidHearingDate(String dateListed, ListingData listingData) {
        if (SINGLE_HEARING_DATE_TYPE.equals(listingData.getHearingDateType())) {
            return isDateInRange(dateListed, listingData.getListingDate(),
                listingData.getListingDate());
        } else {
            return isDateInRange(dateListed, listingData.getListingDateFrom(),
                listingData.getListingDateTo());
        }
    }

    private boolean isDateInRange(String listedDate, String dateFrom, String dateTo) {
        LocalDate hearingListedDate = LocalDate.parse(listedDate);
        LocalDate hearingDatesFrom = LocalDate.parse(dateFrom);
        LocalDate hearingDatesTo = LocalDate.parse(dateTo);

        return  (hearingListedDate.isEqual(hearingDatesFrom) ||  hearingListedDate.isAfter(hearingDatesFrom))
            && (hearingListedDate.isEqual(hearingDatesTo) || hearingListedDate.isBefore(hearingDatesTo));
    }

    private void addReportSummary(MemberDaysReportData reportData) {
        Map<String, List<MemberDaysReportDetail>> groupedByDate = reportData.getReportDetails()
                .stream().distinct().collect(groupingBy(MemberDaysReportDetail::getSortingHearingDate));
        List<String> uniqueDatesList = groupedByDate.keySet().stream().sorted()
            .collect(Collectors.toList());

        for (String listingDate : uniqueDatesList) {
            MemberDaySummaryItem memberDaySummaryItem = new MemberDaySummaryItem();
            memberDaySummaryItem.setHearingDate(UtilHelper.formatCurrentDate(LocalDate.parse(listingDate)));
            setDayCounts(groupedByDate.get(listingDate), memberDaySummaryItem);
            reportData.getMemberDaySummaryItems().add(memberDaySummaryItem);
        }
    }

    private void setDayCounts(List<MemberDaysReportDetail> reportDetails, MemberDaySummaryItem summaryItem) {
        List<Integer> dayCounts = getFullMembersDayCount(reportDetails);
        Integer fullDaysTotal = dayCounts.get(0);
        summaryItem.setFullDays(String.valueOf(fullDaysTotal));
        Integer halfDaysTotal = dayCounts.get(1);
        summaryItem.setHalfDays(String.valueOf(halfDaysTotal));
        double totalDays = (double)fullDaysTotal + (double)halfDaysTotal / 2.0;
        summaryItem.setTotalDays(String.valueOf(totalDays));
    }

    private List<Integer> getFullMembersDayCount(List<MemberDaysReportDetail> reportDetails) {
        int fullDayTotal = 0;
        int halfDayTotal = 0;
        int fullDayHearingDuration = 3;
        for (MemberDaysReportDetail detail: reportDetails) {
            if ((Integer.parseInt(detail.getHearingDuration()) / MINUTES) >= fullDayHearingDuration) {
                fullDayTotal = fullDayTotal + getPanelMembersTotalDuration(detail);
            } else {
                halfDayTotal = halfDayTotal + getPanelMembersTotalDuration(detail);
            }
        }

        return List.of(fullDayTotal, halfDayTotal);
    }

    private int getPanelMembersTotalDuration(MemberDaysReportDetail currentDetail) {
        int employeeValue = getPanelMemberValue(currentDetail.getEmployeeMember());
        int employerValue = getPanelMemberValue(currentDetail.getEmployerMember());
        return employeeValue + employerValue;
    }

    private int getPanelMemberValue(String currentMember) {
        return isNullOrEmpty(currentMember) ? 0 : 1;
    }

    private void addReportSummaryHeader(MemberDaysReportData reportData) {
        List<MemberDaySummaryItem> summaryItems = reportData.getMemberDaySummaryItems();

        String fullDaysTotal = String.valueOf(summaryItems.stream()
                .map(item -> Integer.parseInt(item.getFullDays()))
                .reduce(0, Integer::sum));
        reportData.setFullDaysTotal(fullDaysTotal);

        String halfDaysTotal = String.valueOf(summaryItems.stream()
            .map(item -> Integer.parseInt(item.getHalfDays()))
            .reduce(0, Integer::sum));
        reportData.setHalfDaysTotal(halfDaysTotal);

        String totalDays = String.valueOf(summaryItems.stream()
            .map(item -> Double.parseDouble(item.getTotalDays()))
            .reduce(0.0, Double::sum));
        reportData.setTotalDays(totalDays);
    }
}
