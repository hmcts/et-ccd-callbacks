package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseRetrievalException;
import uk.gov.hmcts.ecm.common.model.listing.ListingData;
import uk.gov.hmcts.ecm.common.model.listing.ListingDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment.CasesAwaitingJudgmentReport;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment.CasesAwaitingJudgmentReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment.CcdReportDataSource;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsCcdDataSource;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsReport;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition.NoPositionChangeCcdDataSource;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition.NoPositionChangeReport;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition.NoPositionChangeReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.respondentsreport.RespondentsReport;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.respondentsreport.RespondentsReportCcdDataSource;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.respondentsreport.RespondentsReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.respondentsreport.RespondentsReportParams;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.sessiondays.SessionDaysCcdReportDataSource;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.sessiondays.SessionDaysReport;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.sessiondays.SessionDaysReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.sessiondays.SessionDaysReportParams;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.JudgeService;

import java.time.LocalDate;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN2;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SESSION_DAYS_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.CASES_AWAITING_JUDGMENT_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.HEARINGS_TO_JUDGEMENTS_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.NO_CHANGE_IN_CURRENT_POSITION_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.RESPONDENTS_REPORT;

@RequiredArgsConstructor
@Slf4j
@Service
public class ReportDataService {
    private final CcdClient ccdClient;
    private final ListingService listingService;
    private final JudgeService judgeService;

    private String listingDateFrom;
    private String listingDateTo;

    private static final String REPORT_DATA_GENERATION_FAILED_ERROR = "Failed to generate report data for case id : ";

    public ListingData generateReportData(ListingDetails listingDetails, String authToken) {
        try {
            String reportType = listingDetails.getCaseData().getReportType();
            switch (reportType) {
                case CASES_AWAITING_JUDGMENT_REPORT:
                    return getCasesAwaitingJudgmentReport(listingDetails, authToken);
                case HEARINGS_TO_JUDGEMENTS_REPORT:
                    return getHearingsToJudgmentsReport(listingDetails, authToken);
                case NO_CHANGE_IN_CURRENT_POSITION_REPORT:
                    return getNoPositionChangeReport(listingDetails, authToken);
                case RESPONDENTS_REPORT:
                    return getRespondentsReport(listingDetails, authToken);
                case SESSION_DAYS_REPORT:
                    return getSessionDaysReport(listingDetails, authToken);
                default:
                    return listingService.getDateRangeReport(listingDetails, authToken);
            }
        } catch (Exception ex) {
            throw new CaseRetrievalException(REPORT_DATA_GENERATION_FAILED_ERROR + listingDetails.getCaseId(), ex);
        }
    }

    private CasesAwaitingJudgmentReportData getCasesAwaitingJudgmentReport(
            ListingDetails listingDetails, String authToken) {
        log.info("Cases Awaiting Judgment for {}, Office {}", listingDetails.getCaseTypeId(),
                listingDetails.getCaseData().getManagingOffice());
        var reportDataSource = new CcdReportDataSource(authToken, ccdClient);

        var casesAwaitingJudgmentReport = new CasesAwaitingJudgmentReport(reportDataSource);
        var reportData = casesAwaitingJudgmentReport.runReport(listingDetails);
        setSharedReportDocumentFields(reportData, listingDetails, false);
        return reportData;
    }

    private HearingsToJudgmentsReportData getHearingsToJudgmentsReport(ListingDetails listingDetails,
                                                                       String authToken) {
        log.info("Hearings To Judgments for {}", listingDetails.getCaseTypeId());
        setListingDateRangeForSearch(listingDetails);
        var reportDataSource = new HearingsToJudgmentsCcdDataSource(authToken, ccdClient);
        var hearingsToJudgmentsReport = new HearingsToJudgmentsReport(reportDataSource, listingDateFrom, listingDateTo);
        var reportData = hearingsToJudgmentsReport.runReport(
                listingDetails.getCaseTypeId(), listingDetails.getCaseData().getManagingOffice());
        setSharedReportDocumentFields(reportData, listingDetails, true);
        return reportData;
    }

    private NoPositionChangeReportData getNoPositionChangeReport(ListingDetails listingDetails, String authToken) {
        log.info("No Change In Current Position for {}", listingDetails.getCaseTypeId());
        var reportDataSource = new NoPositionChangeCcdDataSource(authToken, ccdClient);
        var hearingsToJudgmentsReport = new NoPositionChangeReport(reportDataSource,
                listingDetails.getCaseData().getReportDate());
        var reportData = hearingsToJudgmentsReport.runReport(listingDetails.getCaseTypeId());
        setSharedReportDocumentFields(reportData, listingDetails, false);
        return reportData;
    }

    private RespondentsReportData getRespondentsReport(ListingDetails listingDetails, String authToken) {
        log.info("Respondents Report for {}", listingDetails.getCaseData().getManagingOffice());
        var reportDataSource = new RespondentsReportCcdDataSource(authToken, ccdClient);
        setListingDateRangeForSearch(listingDetails);
        var listingData = listingDetails.getCaseData();

        var params = new RespondentsReportParams(listingDetails.getCaseTypeId(), listingData.getManagingOffice(),
                listingDateFrom, listingDateTo);
        var respondentsReport = new RespondentsReport(reportDataSource);
        var reportData = respondentsReport.generateReport(params);
        setSharedReportDocumentFields(reportData, listingDetails, true);
        return reportData;
    }

    private SessionDaysReportData getSessionDaysReport(ListingDetails listingDetails, String authToken) {
        log.info("Session Days Report for {}", listingDetails.getCaseData().getManagingOffice());
        var reportDataSource = new SessionDaysCcdReportDataSource(authToken, ccdClient);
        setListingDateRangeForSearch(listingDetails);

        var sessionDaysReport = new SessionDaysReport(reportDataSource, judgeService);
        var listingData = listingDetails.getCaseData();
        var params = new SessionDaysReportParams(listingDetails.getCaseTypeId(), listingData.getManagingOffice(),
                listingDateFrom, listingDateTo);
        var reportData = sessionDaysReport.generateReport(params);
        reportData.setDocumentName(listingData.getDocumentName());
        reportData.setReportType(listingData.getReportType());
        reportData.setHearingDateType(listingData.getHearingDateType());
        reportData.setListingDateFrom(listingData.getListingDateFrom());
        reportData.setListingDateTo(listingData.getListingDateTo());
        reportData.setListingDate(listingData.getListingDate());
        return reportData;
    }

    private void setListingDateRangeForSearch(ListingDetails listingDetails) {
        var listingData = listingDetails.getCaseData();
        boolean isRangeHearingDateType = listingData.getHearingDateType().equals(RANGE_HEARING_DATE_TYPE);
        if (!isRangeHearingDateType) {
            listingDateFrom = LocalDate.parse(listingData.getListingDate(), OLD_DATE_TIME_PATTERN2)
                    .atStartOfDay().format(OLD_DATE_TIME_PATTERN);
            listingDateTo = LocalDate.parse(listingData.getListingDate(), OLD_DATE_TIME_PATTERN2)
                    .atStartOfDay().plusDays(1).minusSeconds(1).format(OLD_DATE_TIME_PATTERN);
        } else {
            listingDateFrom = LocalDate.parse(listingData.getListingDateFrom(), OLD_DATE_TIME_PATTERN2)
                    .atStartOfDay().format(OLD_DATE_TIME_PATTERN);
            listingDateTo = LocalDate.parse(listingData.getListingDateTo(), OLD_DATE_TIME_PATTERN2)
                    .atStartOfDay().format(OLD_DATE_TIME_PATTERN);
        }
    }

    private void setSharedReportDocumentFields(ListingData reportData, ListingDetails listingDetails,
                                               boolean isDateRangeReport) {
        reportData.setDocumentName(listingDetails.getCaseData().getDocumentName());
        reportData.setReportType(listingDetails.getCaseData().getReportType());
        reportData.setManagingOffice(listingDetails.getCaseData().getManagingOffice());

        if (isDateRangeReport) {
            reportData.setHearingDateType(listingDetails.getCaseData().getHearingDateType());
            reportData.setListingDateFrom(listingDetails.getCaseData().getListingDateFrom());
            reportData.setListingDateTo(listingDetails.getCaseData().getListingDateTo());
            reportData.setListingDate(listingDetails.getCaseData().getListingDate());
        }
    }
}
