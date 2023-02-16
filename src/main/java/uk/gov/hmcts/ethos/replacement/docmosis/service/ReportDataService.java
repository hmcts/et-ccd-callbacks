package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseRetrievalException;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportParams;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment.CasesAwaitingJudgmentReport;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment.CasesAwaitingJudgmentReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment.CcdReportDataSource;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.claimsbyhearingvenue.ClaimsByHearingVenueCcdReportDataSource;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.claimsbyhearingvenue.ClaimsByHearingVenueReport;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.claimsbyhearingvenue.ClaimsByHearingVenueReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.claimsbyhearingvenue.ClaimsByHearingVenueReportParams;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.eccreport.EccReport;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.eccreport.EccReportCcdDataSource;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.eccreport.EccReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingsbyhearingtype.HearingsByHearingTypeCcdReportDataSource;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingsbyhearingtype.HearingsByHearingTypeReport;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingsbyhearingtype.HearingsByHearingTypeReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsCcdReportDataSource;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsReport;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition.NoPositionChangeCcdDataSource;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition.NoPositionChangeReport;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition.NoPositionChangeReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.respondentsreport.RespondentsReport;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.respondentsreport.RespondentsReportCcdDataSource;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.respondentsreport.RespondentsReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.sessiondays.SessionDaysCcdReportDataSource;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.sessiondays.SessionDaysReport;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.sessiondays.SessionDaysReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.JudgeService;

import java.time.LocalDate;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMS_BY_HEARING_VENUE_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARINGS_BY_HEARING_TYPE_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN2;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SESSION_DAYS_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.CASES_AWAITING_JUDGMENT_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ECC_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.HEARINGS_TO_JUDGEMENTS_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.NO_CHANGE_IN_CURRENT_POSITION_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.RESPONDENTS_REPORT;

@RequiredArgsConstructor
@Slf4j
@Service
@SuppressWarnings({"PMD.ConfusingTernary", "PDM.CyclomaticComplexity", "PMD.LinguisticNaming",
    "PMD.LiteralsFirstInComparisons", "PMD.LawOfDemeter", "PMD.ExcessiveImports", "PMD.CyclomaticComplexity"})
public class ReportDataService {
    private final CcdClient ccdClient;
    private final ListingService listingService;
    private final JudgeService judgeService;
    private final UserService userService;

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
                case ECC_REPORT:
                    return getEccReport(listingDetails, authToken);
                case HEARINGS_BY_HEARING_TYPE_REPORT:
                    return getHearingsByHearingTypeReport(listingDetails, authToken);
                case CLAIMS_BY_HEARING_VENUE_REPORT:
                    return getClaimsByHearingVenueReport(listingDetails, authToken);
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
        CcdReportDataSource reportDataSource = new CcdReportDataSource(authToken, ccdClient);

        CasesAwaitingJudgmentReport casesAwaitingJudgmentReport = new CasesAwaitingJudgmentReport(reportDataSource);
        CasesAwaitingJudgmentReportData reportData = casesAwaitingJudgmentReport.runReport(listingDetails);
        setSharedReportDocumentFields(reportData, listingDetails, false);
        return reportData;
    }

    private HearingsToJudgmentsReportData getHearingsToJudgmentsReport(ListingDetails listingDetails,
                                                                       String authToken) {
        log.info("Hearings To Judgments for {}", listingDetails.getCaseTypeId());
        ReportParams params = setListingDateRangeForSearch(listingDetails);
        HearingsToJudgmentsCcdReportDataSource reportDataSource = new HearingsToJudgmentsCcdReportDataSource(
            authToken, ccdClient);
        HearingsToJudgmentsReport hearingsToJudgmentsReport = new HearingsToJudgmentsReport(reportDataSource, params);
        HearingsToJudgmentsReportData reportData = hearingsToJudgmentsReport.runReport(listingDetails.getCaseTypeId(),
                listingDetails.getCaseData().getManagingOffice());
        reportData.setDocumentName(listingDetails.getCaseData().getDocumentName());
        reportData.setReportType(listingDetails.getCaseData().getReportType());
        reportData.setHearingDateType(listingDetails.getCaseData().getHearingDateType());
        reportData.setListingDateFrom(listingDetails.getCaseData().getListingDateFrom());
        reportData.setListingDateTo(listingDetails.getCaseData().getListingDateTo());
        reportData.setListingDate(listingDetails.getCaseData().getListingDate());
        return reportData;
    }

    private NoPositionChangeReportData getNoPositionChangeReport(ListingDetails listingDetails, String authToken) {
        log.info("No Change In Current Position for {}", listingDetails.getCaseTypeId());
        NoPositionChangeCcdDataSource reportDataSource = new NoPositionChangeCcdDataSource(authToken, ccdClient);
        NoPositionChangeReport hearingsToJudgmentsReport = new NoPositionChangeReport(reportDataSource,
                listingDetails.getCaseData().getReportDate());
        NoPositionChangeReportData reportData = hearingsToJudgmentsReport.runReport(listingDetails);
        setSharedReportDocumentFields(reportData, listingDetails, false);
        return reportData;
    }

    private ClaimsByHearingVenueReportData getClaimsByHearingVenueReport(ListingDetails listingDetails,
                                                                         String authToken) {
        log.info("Claims By Hearing Venue Report for {}", listingDetails.getCaseTypeId());
        ReportParams genericReportParams = setListingDateRangeForSearch(listingDetails);
        ListingData listingData = listingDetails.getCaseData();
        ClaimsByHearingVenueReportParams claimsByHearingVenueReportParams = new ClaimsByHearingVenueReportParams(
                genericReportParams.getCaseTypeId(), genericReportParams.getManagingOffice(),
                genericReportParams.getDateFrom(), genericReportParams.getDateTo(), listingData.getHearingDateType(),
                getUserFullName(authToken));
        ClaimsByHearingVenueCcdReportDataSource reportDataSource = new ClaimsByHearingVenueCcdReportDataSource(
            authToken, ccdClient);
        ClaimsByHearingVenueReport claimsByHearingVenueReport = new ClaimsByHearingVenueReport(reportDataSource);
        return claimsByHearingVenueReport.generateReport(claimsByHearingVenueReportParams);
    }

    public String getUserFullName(String userToken) {
        UserDetails userDetails = userService.getUserDetails(userToken);
        String firstName = userDetails.getFirstName() != null ? userDetails.getFirstName() : "";
        String lastName = userDetails.getLastName() != null ? userDetails.getLastName() : "";
        return firstName + " " + lastName;
    }

    private EccReportData getEccReport(ListingDetails listingDetails, String authToken) {
        log.info("Ecc Report for {}", listingDetails.getCaseTypeId());
        EccReportCcdDataSource reportDataSource = new EccReportCcdDataSource(authToken, ccdClient);
        ListingData listingData = listingDetails.getCaseData();
        ReportParams params = setListingDateRangeForSearch(listingDetails);
        EccReport eccReport = new EccReport(reportDataSource);
        EccReportData reportData = eccReport.generateReport(params);
        setReportData(reportData, listingData);
        return reportData;
    }

    private void setReportData(ListingData reportData, ListingData listingData) {
        reportData.setDocumentName(listingData.getDocumentName());
        reportData.setReportType(listingData.getReportType());
        reportData.setHearingDateType(listingData.getHearingDateType());
        reportData.setListingDateFrom(listingData.getListingDateFrom());
        reportData.setListingDateTo(listingData.getListingDateTo());
        reportData.setListingDate(listingData.getListingDate());
    }

    private RespondentsReportData getRespondentsReport(ListingDetails listingDetails, String authToken) {
        log.info("Respondents Report for {}", listingDetails.getCaseData().getManagingOffice());
        RespondentsReportCcdDataSource reportDataSource = new RespondentsReportCcdDataSource(authToken, ccdClient);

        ReportParams params = setListingDateRangeForSearch(listingDetails);
        RespondentsReport respondentsReport = new RespondentsReport(reportDataSource);
        RespondentsReportData reportData = respondentsReport.generateReport(params);
        setSharedReportDocumentFields(reportData, listingDetails, true);
        return reportData;
    }

    private SessionDaysReportData getSessionDaysReport(ListingDetails listingDetails, String authToken) {
        log.info("Session Days Report for {}", listingDetails.getCaseTypeId());
        SessionDaysCcdReportDataSource reportDataSource = new SessionDaysCcdReportDataSource(authToken, ccdClient);
        setListingDateRangeForSearch(listingDetails);

        SessionDaysReport sessionDaysReport = new SessionDaysReport(reportDataSource, judgeService);
        ListingData listingData = listingDetails.getCaseData();
        ReportParams params = setListingDateRangeForSearch(listingDetails);
        SessionDaysReportData reportData = sessionDaysReport.generateReport(params);
        reportData.setDocumentName(listingData.getDocumentName());
        reportData.setReportType(listingData.getReportType());
        reportData.setHearingDateType(listingData.getHearingDateType());
        reportData.setListingDateFrom(listingData.getListingDateFrom());
        reportData.setListingDateTo(listingData.getListingDateTo());
        reportData.setListingDate(listingData.getListingDate());
        return reportData;
    }

    private HearingsByHearingTypeReportData getHearingsByHearingTypeReport(ListingDetails listingDetails,
                                                                           String authToken) {
        log.info("Hearings By Hearing Type Report for {}", listingDetails.getCaseTypeId());
        HearingsByHearingTypeCcdReportDataSource reportDataSource = new HearingsByHearingTypeCcdReportDataSource(
            authToken, ccdClient);
        setListingDateRangeForSearch(listingDetails);
        ListingData listingData = listingDetails.getCaseData();
        HearingsByHearingTypeReport hearingsByHearingTypeReport = new HearingsByHearingTypeReport(reportDataSource);
        ReportParams params = setListingDateRangeForSearch(listingDetails);
        HearingsByHearingTypeReportData reportData = hearingsByHearingTypeReport
                .generateReport(params);
        setReportData(reportData, listingData);
        return reportData;
    }

    private ReportParams setListingDateRangeForSearch(ListingDetails listingDetails) {
        ListingData listingData = listingDetails.getCaseData();
        boolean isRangeHearingDateType = listingData.getHearingDateType().equals(RANGE_HEARING_DATE_TYPE);
        String listingDateFrom;
        String listingDateTo;
        if (!isRangeHearingDateType) {
            listingDateFrom = LocalDate.parse(listingData.getListingDate(), OLD_DATE_TIME_PATTERN2)
                    .atStartOfDay().format(OLD_DATE_TIME_PATTERN);
            listingDateTo = LocalDate.parse(listingData.getListingDate(), OLD_DATE_TIME_PATTERN2)
                    .atStartOfDay().plusDays(1).minusSeconds(1).format(OLD_DATE_TIME_PATTERN);
        } else {
            listingDateFrom = LocalDate.parse(listingData.getListingDateFrom(), OLD_DATE_TIME_PATTERN2)
                    .atStartOfDay().format(OLD_DATE_TIME_PATTERN);
            listingDateTo = LocalDate.parse(listingData.getListingDateTo(), OLD_DATE_TIME_PATTERN2)
                    .atStartOfDay().plusDays(1).minusSeconds(1).format(OLD_DATE_TIME_PATTERN);
        }
        return new ReportParams(listingDetails.getCaseTypeId(), listingDetails.getCaseData().getManagingOffice(),
                listingDateFrom, listingDateTo);
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
