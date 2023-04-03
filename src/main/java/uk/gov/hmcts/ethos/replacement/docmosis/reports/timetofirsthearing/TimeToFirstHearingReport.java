package uk.gov.hmcts.ethos.replacement.docmosis.reports.timetofirsthearing;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.Strings;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.et.common.model.listing.items.AdhocReportTypeItem;
import uk.gov.hmcts.et.common.model.listing.types.AdhocReportType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReportHelper;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_FAST_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_NO_CONCILIATION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_OPEN_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_STANDARD_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_PERLIMINARY_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Service
@Slf4j
public class TimeToFirstHearingReport {

    static final String ZERO = "0";
    static final String ZERO_DECIMAL = "0.00";

    public ListingData generateReportData(ListingDetails listingDetails, List<SubmitEvent> submitEvents) {
        String managingOffice = listingDetails.getCaseData().getManagingOffice();
        String reportOffice = ReportHelper.getReportOffice(listingDetails.getCaseTypeId(), managingOffice);

        initReport(listingDetails, reportOffice);

        if (CollectionUtils.isNotEmpty(submitEvents)) {
            executeReport(listingDetails, submitEvents, reportOffice);
        }

        listingDetails.getCaseData().clearReportFields();
        return listingDetails.getCaseData();
    }

    private void initReport(ListingDetails listingDetails, String reportOffice) {

        AdhocReportType adhocReportType = new AdhocReportType();

        //LocalReportsSummary fields
        adhocReportType.setConNoneTotal(ZERO);
        adhocReportType.setConStdTotal(ZERO);
        adhocReportType.setConFastTotal(ZERO);
        adhocReportType.setConOpenTotal(ZERO);
        adhocReportType.setConNone26wkTotal(ZERO);
        adhocReportType.setConStd26wkTotal(ZERO);
        adhocReportType.setConFast26wkTotal(ZERO);
        adhocReportType.setConOpen26wkTotal(ZERO);
        adhocReportType.setConNone26wkTotalPerCent(ZERO_DECIMAL);
        adhocReportType.setConStd26wkTotalPerCent(ZERO_DECIMAL);
        adhocReportType.setConFast26wkTotalPerCent(ZERO_DECIMAL);
        adhocReportType.setConOpen26wkTotalPerCent(ZERO_DECIMAL);
        adhocReportType.setNotConNone26wkTotal(ZERO);
        adhocReportType.setNotConStd26wkTotal(ZERO);
        adhocReportType.setNotConFast26wkTotal(ZERO);
        adhocReportType.setNotConOpen26wkTotal(ZERO);
        adhocReportType.setNotConNone26wkTotalPerCent(ZERO_DECIMAL);
        adhocReportType.setNotConStd26wkTotalPerCent(ZERO_DECIMAL);
        adhocReportType.setNotConFast26wkTotalPerCent(ZERO_DECIMAL);
        adhocReportType.setNotConOpen26wkTotalPerCent(ZERO_DECIMAL);

        //localReportsSummaryHdr fields
        adhocReportType.setTotalCases(ZERO);
        adhocReportType.setTotal26wk(ZERO);
        adhocReportType.setTotal26wkPerCent(ZERO_DECIMAL);
        adhocReportType.setTotalx26wk(ZERO);
        adhocReportType.setTotalx26wkPerCent(ZERO_DECIMAL);

        //localReportsDetail fields
        adhocReportType.setReportOffice(reportOffice);
        adhocReportType.setCaseReference("");
        adhocReportType.setConciliationTrack("");
        adhocReportType.setReceiptDate("");
        adhocReportType.setHearingDate("");
        adhocReportType.setTotal("");

        ListingData listingData = listingDetails.getCaseData();
        listingData.setLocalReportsDetailHdr(adhocReportType);

        // Init localReportSummary
        AdhocReportTypeItem adhocReportTypeItem = new AdhocReportTypeItem();
        adhocReportTypeItem.setId(UUID.randomUUID().toString());
        adhocReportTypeItem.setValue(adhocReportType);

        listingData.setLocalReportsSummary(Collections.singletonList(adhocReportTypeItem));
        listingData.setLocalReportsDetail(new ArrayList<>());
    }

    private void executeReport(ListingDetails listingDetails, List<SubmitEvent> submitEvents, String reportOffice) {
        log.info(String.format("Time to first hearing report case type id %s for office %s with search results: %d",
                listingDetails.getCaseTypeId(), listingDetails.getCaseData().getManagingOffice(), submitEvents.size()));
        populateLocalReportSummary(listingDetails.getCaseData(), submitEvents);
        populateLocalReportSummaryHdr(listingDetails, reportOffice);
        populateLocalReportSummaryDetail(listingDetails, submitEvents, reportOffice);

    }

    private void populateLocalReportSummaryDetail(ListingDetails listingDetails, List<SubmitEvent> submitEvents,
                                                  String reportOffice) {
        List<AdhocReportTypeItem> localReportsDetailList = listingDetails.getCaseData().getLocalReportsDetail();
        for (SubmitEvent submitEvent : submitEvents) {
            AdhocReportTypeItem localReportsDetailItem =
                    getLocalReportsDetail(listingDetails, submitEvent.getCaseData(), reportOffice);
            if (localReportsDetailItem != null) {
                localReportsDetailList.add(localReportsDetailItem);
            }
        }
        listingDetails.getCaseData().setLocalReportsDetail(localReportsDetailList);
    }

    private AdhocReportTypeItem getLocalReportsDetail(ListingDetails listingDetails, CaseData caseData,
                                                      String reportOffice) {

        LocalDate firstHearingDate = getFirstHearingDate(caseData);
        if (firstHearingDate == null || isFirstHearingWithin26Weeks(caseData, firstHearingDate)) {
            return null;
        }
        AdhocReportType adhocReportType = new AdhocReportType();
        adhocReportType.setHearingDate(firstHearingDate.toString());
        adhocReportType.setReportOffice(reportOffice);
        adhocReportType.setCaseReference(caseData.getEthosCaseReference());
        adhocReportType.setConciliationTrack(getConciliationTrack(caseData));
        if (!Strings.isNullOrEmpty(caseData.getReceiptDate())) {
            Duration duration = Duration.between(firstHearingDate.atStartOfDay(),
                    LocalDate.parse(caseData.getReceiptDate()).atStartOfDay()).abs();
            adhocReportType.setDelayedDaysForFirstHearing(String.valueOf(duration.toDays()));
            adhocReportType.setReceiptDate(caseData.getReceiptDate());
        }
        AdhocReportTypeItem adhocReportTypeItem = new AdhocReportTypeItem();
        adhocReportTypeItem.setId(UUID.randomUUID().toString());
        adhocReportTypeItem.setValue(adhocReportType);
        return adhocReportTypeItem;

    }

    private void populateLocalReportSummaryHdr(ListingDetails listingDetails, String reportOffice) {

        ListingData listingData = listingDetails.getCaseData();
        AdhocReportType adhocReportType = listingData.getLocalReportsSummary().get(0).getValue();
        adhocReportType.setReportOffice(reportOffice);
        int totalCases = Integer.parseInt(adhocReportType.getConOpenTotal())
                + Integer.parseInt(adhocReportType.getConStdTotal())
                + Integer.parseInt(adhocReportType.getConFastTotal())
                + Integer.parseInt(adhocReportType.getConNoneTotal());

        int totalCasesWithin26Weeks = Integer.parseInt(adhocReportType.getConOpen26wkTotal())
                + Integer.parseInt(adhocReportType.getConStd26wkTotal())
                + Integer.parseInt(adhocReportType.getConFast26wkTotal())
                + Integer.parseInt(adhocReportType.getConNone26wkTotal());

        int totalCasesNotWithin26Weeks = Integer.parseInt(adhocReportType.getNotConOpen26wkTotal())
                + Integer.parseInt(adhocReportType.getNotConStd26wkTotal())
                + Integer.parseInt(adhocReportType.getNotConFast26wkTotal())
                + Integer.parseInt(adhocReportType.getNotConNone26wkTotal());

        float totalCasesWithin26WeeksPercent = (totalCases != 0)
                ? ((float)totalCasesWithin26Weeks / totalCases) * 100 : 0;
        float totalCasesNotWithin26WeeksPercent = (totalCases != 0)
                ? ((float)totalCasesNotWithin26Weeks / totalCases) * 100 : 0;

        adhocReportType.setTotalCases(String.valueOf(totalCases));
        adhocReportType.setTotal26wk(String.valueOf(totalCasesWithin26Weeks));
        adhocReportType.setTotalx26wk(String.valueOf(totalCasesNotWithin26Weeks));
        adhocReportType.setTotal26wkPerCent(String.format("%.2f", totalCasesWithin26WeeksPercent));
        adhocReportType.setTotalx26wkPerCent(String.format("%.2f", totalCasesNotWithin26WeeksPercent));
        listingData.setLocalReportsDetailHdr(adhocReportType);
    }

    static class ReportSummary {
        int conNoneTotal;
        int conStdTotal;
        int conFastTotal;
        int conOpenTotal;
        int conNone26WkTotal;
        int conStd26WkTotal;
        int conFast26WkTotal;
        int conOpen26WkTotal;
        int notConNone26WkTotal;
        int notConStd26WkTotal;
        int notConFast26WkTotal;
        int notConOpen26WkTotal;
    }

    private void populateLocalReportSummary(ListingData listingData, List<SubmitEvent> submitEvents) {

        ReportSummary reportSummary = new ReportSummary();
        AdhocReportType adhocReportType = listingData.getLocalReportsDetailHdr();
        LocalDate firstHearingDate;
        for (SubmitEvent submitEvent : submitEvents) {
            firstHearingDate = getFirstHearingDate(submitEvent.getCaseData());
            if (firstHearingDate == null) {
                continue;
            }
            boolean isFirstHearingWithin26Weeks = isFirstHearingWithin26Weeks(
                    submitEvent.getCaseData(),
                    firstHearingDate);

            switch (getConciliationTrack(submitEvent.getCaseData())) {
                case CONCILIATION_TRACK_NO_CONCILIATION:
                    reportSummary = updateNoTrack(reportSummary, isFirstHearingWithin26Weeks);
                    break;
                case CONCILIATION_TRACK_STANDARD_TRACK:
                    reportSummary = updateStandardTrack(reportSummary, isFirstHearingWithin26Weeks);
                    break;
                case CONCILIATION_TRACK_FAST_TRACK:
                    reportSummary =  updateFastTrack(reportSummary, isFirstHearingWithin26Weeks);
                    break;
                case CONCILIATION_TRACK_OPEN_TRACK:
                    reportSummary = updateOpenTrack(reportSummary, isFirstHearingWithin26Weeks);
                    break;
                default:
                    break;
            }
        }

        adhocReportType.setConNoneTotal(String.valueOf(reportSummary.conNoneTotal));
        adhocReportType.setConStdTotal(String.valueOf(reportSummary.conStdTotal));
        adhocReportType.setConFastTotal(String.valueOf(reportSummary.conFastTotal));
        adhocReportType.setConOpenTotal(String.valueOf(reportSummary.conOpenTotal));
        adhocReportType.setConNone26wkTotal(String.valueOf(reportSummary.conNone26WkTotal));
        adhocReportType.setConStd26wkTotal(String.valueOf(reportSummary.conStd26WkTotal));
        adhocReportType.setConFast26wkTotal(String.valueOf(reportSummary.conFast26WkTotal));
        adhocReportType.setConOpen26wkTotal(String.valueOf(reportSummary.conOpen26WkTotal));
        adhocReportType.setNotConNone26wkTotal(String.valueOf(reportSummary.notConNone26WkTotal));
        adhocReportType.setNotConStd26wkTotal(String.valueOf(reportSummary.notConStd26WkTotal));
        adhocReportType.setNotConFast26wkTotal(String.valueOf(reportSummary.notConFast26WkTotal));
        adhocReportType.setNotConOpen26wkTotal(String.valueOf(reportSummary.notConOpen26WkTotal));
        setPercent(adhocReportType);

        AdhocReportTypeItem adhocReportTypeItem = new AdhocReportTypeItem();
        adhocReportTypeItem.setId(UUID.randomUUID().toString());
        adhocReportTypeItem.setValue(adhocReportType);
        listingData.setLocalReportsSummary(Collections.singletonList(adhocReportTypeItem));
    }

    private ReportSummary updateNoTrack(ReportSummary reportSummary, boolean isFirstHearingWithin26Weeks) {
        reportSummary.conNoneTotal = reportSummary.conNoneTotal + 1;
        if (isFirstHearingWithin26Weeks) {
            reportSummary.conNone26WkTotal = reportSummary.conNone26WkTotal + 1;
        } else {
            reportSummary.notConNone26WkTotal = reportSummary.notConNone26WkTotal + 1;
        }
        return reportSummary;
    }

    private ReportSummary updateStandardTrack(ReportSummary reportSummary, boolean isFirstHearingWithin26Weeks) {
        reportSummary.conStdTotal = reportSummary.conStdTotal + 1;
        if (isFirstHearingWithin26Weeks) {
            reportSummary.conStd26WkTotal = reportSummary.conStd26WkTotal + 1;
        } else {
            reportSummary.notConStd26WkTotal = reportSummary.notConStd26WkTotal + 1;
        }
        return reportSummary;
    }

    private ReportSummary updateFastTrack(ReportSummary reportSummary, boolean isFirstHearingWithin26Weeks) {
        reportSummary.conFastTotal = reportSummary.conFastTotal + 1;
        if (isFirstHearingWithin26Weeks) {
            reportSummary.conFast26WkTotal = reportSummary.conFast26WkTotal + 1;
        } else {
            reportSummary.notConFast26WkTotal = reportSummary.notConFast26WkTotal + 1;
        }
        return reportSummary;
    }

    private ReportSummary updateOpenTrack(ReportSummary reportSummary, boolean isFirstHearingWithin26Weeks) {
        reportSummary.conOpenTotal = reportSummary.conOpenTotal + 1;
        if (isFirstHearingWithin26Weeks) {
            reportSummary.conOpen26WkTotal = reportSummary.conOpen26WkTotal + 1;
        } else {
            reportSummary.notConOpen26WkTotal = reportSummary.notConOpen26WkTotal + 1;
        }
        return reportSummary;
    }

    private void setPercent(AdhocReportType adhocReportType) {
        double conNone26wkTotalPerCent = (Integer.parseInt(adhocReportType.getConNoneTotal()) != 0)
                ? (Double.parseDouble(adhocReportType.getConNone26wkTotal())
                / Integer.parseInt(adhocReportType.getConNoneTotal())) * 100 : 0;
        double conStd26wkTotalPerCent = (Integer.parseInt(adhocReportType.getConStdTotal()) != 0)
                ? (Double.parseDouble(adhocReportType.getConStd26wkTotal())
                / Integer.parseInt(adhocReportType.getConStdTotal())) * 100 : 0;
        double conFast26wkTotalPerCent = (Integer.parseInt(adhocReportType.getConFastTotal()) != 0)
                ? (Double.parseDouble(adhocReportType.getConFast26wkTotal())
                / Integer.parseInt(adhocReportType.getConFastTotal())) * 100 : 0;
        double conOpen26wkTotalPerCent = (Integer.parseInt(adhocReportType.getConOpenTotal()) != 0)
                ? (Double.parseDouble(adhocReportType.getConOpen26wkTotal())
                / Integer.parseInt(adhocReportType.getConOpenTotal())) * 100 : 0;

        double notConNone26wkTotalPerCent = (Integer.parseInt(adhocReportType.getConNoneTotal()) != 0)
                ? (Double.parseDouble(adhocReportType.getNotConNone26wkTotal())
                / Integer.parseInt(adhocReportType.getConNoneTotal())) * 100 : 0;
        double notConStd26wkTotalPerCent = (Integer.parseInt(adhocReportType.getConStdTotal()) != 0)
                ? (Double.parseDouble(adhocReportType.getNotConStd26wkTotal())
                / Integer.parseInt(adhocReportType.getConStdTotal())) * 100 : 0;
        double notConFast26wkTotalPerCent = (Integer.parseInt(adhocReportType.getConFastTotal()) != 0)
                ? (Double.parseDouble(adhocReportType.getNotConFast26wkTotal())
                / Integer.parseInt(adhocReportType.getConFastTotal())) * 100 : 0;
        float notConOpen26wkTotalPerCent = (Integer.parseInt(adhocReportType.getConOpenTotal()) != 0)
                ? ((float)Double.parseDouble(adhocReportType.getNotConOpen26wkTotal())
                / Integer.parseInt(adhocReportType.getConOpenTotal())) * 100 : 0;

        adhocReportType.setConNone26wkTotalPerCent(String.format("%.2f", conNone26wkTotalPerCent));
        adhocReportType.setConStd26wkTotalPerCent(String.format("%.2f", conStd26wkTotalPerCent));
        adhocReportType.setConFast26wkTotalPerCent(String.format("%.2f", conFast26wkTotalPerCent));
        adhocReportType.setConOpen26wkTotalPerCent(String.format("%.2f", conOpen26wkTotalPerCent));
        adhocReportType.setNotConNone26wkTotalPerCent(String.format("%.2f", notConNone26wkTotalPerCent));
        adhocReportType.setNotConStd26wkTotalPerCent(String.format("%.2f", notConStd26wkTotalPerCent));
        adhocReportType.setNotConFast26wkTotalPerCent(String.format("%.2f", notConFast26wkTotalPerCent));
        adhocReportType.setNotConOpen26wkTotalPerCent(String.format("%.2f", notConOpen26wkTotalPerCent));
    }

    private String getConciliationTrack(CaseData caseData) {
        return StringUtils.isNotBlank(caseData.getConciliationTrack())
                ? caseData.getConciliationTrack() : CONCILIATION_TRACK_NO_CONCILIATION;
    }

    private LocalDate getFirstHearingDate(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getHearingCollection())) {
            return null;
        }
        List<LocalDate> mainDatesList = new ArrayList<>();
        List<LocalDate> datesList;
        for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {
            datesList = getHearingDateList(hearingTypeItem);
            if (CollectionUtils.isNotEmpty(datesList)) {
                mainDatesList.addAll(datesList);
            }
        }
        if (CollectionUtils.isNotEmpty(mainDatesList)) {
            Collections.sort(mainDatesList);
            return mainDatesList.get(0);
        }
        return null;
    }

    private List<LocalDate> getHearingDateList(HearingTypeItem hearingTypeItem) {
        HearingType hearingType = hearingTypeItem.getValue();
        List<LocalDate> datesList = new ArrayList<>();
        if (hearingType == null || CollectionUtils.isEmpty(hearingType.getHearingDateCollection())) {
            return datesList;
        }
        if (Constants.HEARING_TYPE_JUDICIAL_HEARING.equals(hearingType.getHearingType())
                || HEARING_TYPE_PERLIMINARY_HEARING.equals(hearingType.getHearingType())) {
            for (DateListedTypeItem dateListedItemType : hearingType.getHearingDateCollection()) {
                if (Constants.HEARING_STATUS_HEARD.equals(dateListedItemType.getValue().getHearingStatus())
                        && YES.equals(dateListedItemType.getValue().getHearingCaseDisposed())) {
                    LocalDate date = LocalDate.parse(dateListedItemType.getValue().getListedDate(),
                        OLD_DATE_TIME_PATTERN);
                    datesList.add(date);
                }
            }
        }
        return datesList;
    }

    private boolean isFirstHearingWithin26Weeks(CaseData caseData, LocalDate firstHearingDate) {
        LocalDate receiptDate = LocalDate.parse(caseData.getReceiptDate());
        return receiptDate.plusWeeks(26).equals(firstHearingDate) || receiptDate.plusWeeks(26)
                .isAfter(firstHearingDate);
    }
}
