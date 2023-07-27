package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseRetrievalException;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReportHelper;
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
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.CASES_AWAITING_JUDGMENT_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ECC_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.HEARINGS_TO_JUDGEMENTS_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.NO_CHANGE_IN_CURRENT_POSITION_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.RESPONDENTS_REPORT;

@RequiredArgsConstructor
@Slf4j
@Service
@SuppressWarnings({"PMD.ConfusingTernary", "PDM.CyclomaticComplexity", "PMD.LinguisticNaming",
    "PMD.LiteralsFirstInComparisons", "PMD.LawOfDemeter", "PMD.ExcessiveImports",
    "PMD.CyclomaticComplexity", "PMD.UnusedPrivateMethod"})
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
                case Constants.SESSION_DAYS_REPORT:
                    return getSessionDaysReport(listingDetails, authToken);
                case ECC_REPORT:
                    return getEccReport(listingDetails, authToken);
                case Constants.HEARINGS_BY_HEARING_TYPE_REPORT:
                    return getHearingsByHearingTypeReport(listingDetails, authToken);
                case Constants.CLAIMS_BY_HEARING_VENUE_REPORT:
                    return getClaimsByHearingVenueReport(listingDetails, authToken);
                default:
                    return listingService.getDateRangeReport(listingDetails, authToken, getUserFullName(authToken));
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
        ReportParams params = ReportHelper.getListingDateRangeForSearch(listingDetails);
        HearingsToJudgmentsCcdReportDataSource reportDataSource = new HearingsToJudgmentsCcdReportDataSource(
            authToken, ccdClient);
        HearingsToJudgmentsReport hearingsToJudgmentsReport = new HearingsToJudgmentsReport(reportDataSource, params);
        HearingsToJudgmentsReportData reportData = hearingsToJudgmentsReport.runReport(listingDetails.getCaseTypeId(),
                listingDetails.getCaseData().getManagingOffice());
        reportData.setDocumentName(listingDetails.getCaseData().getDocumentName());
        reportData.setReportType(listingDetails.getCaseData().getReportType());
        reportData.setManagingOffice(
                ReportHelper.getReportOfficeForDisplay(listingDetails.getCaseTypeId(),
                        listingDetails.getCaseData().getManagingOffice()));
        reportData.setHearingDateType(listingDetails.getCaseData().getHearingDateType());
        reportData.setListingDateFrom(listingDetails.getCaseData().getListingDateFrom());
        reportData.setListingDateTo(listingDetails.getCaseData().getListingDateTo());
        reportData.setListingDate(listingDetails.getCaseData().getListingDate());
        return reportData;
    }

    private NoPositionChangeReportData getNoPositionChangeReport(ListingDetails listingDetails, String authToken) {
        log.info("No Change In Current Position for {}", listingDetails.getCaseTypeId());
        NoPositionChangeCcdDataSource reportDataSource = new NoPositionChangeCcdDataSource(authToken, ccdClient);
        NoPositionChangeReport noPositionChangeReport = new NoPositionChangeReport(reportDataSource,
                listingDetails.getCaseData().getReportDate());
        NoPositionChangeReportData reportData = noPositionChangeReport.runReport(listingDetails);
        setSharedReportDocumentFields(reportData, listingDetails, false);
        return reportData;
    }

    private ClaimsByHearingVenueReportData getClaimsByHearingVenueReport(ListingDetails listingDetails,
                                                                         String authToken) {
        log.info("Claims By Hearing Venue Report for {}", listingDetails.getCaseTypeId());
        ReportParams genericReportParams = ReportHelper.getListingDateRangeForSearch(listingDetails);
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
        ReportParams params = ReportHelper.getListingDateRangeForSearch(listingDetails);
        EccReport eccReport = new EccReport(reportDataSource);
        EccReportData reportData = eccReport.generateReport(params);
        setReportData(reportData, listingData);
        reportData.setManagingOffice(
                ReportHelper.getReportOfficeForDisplay(listingDetails.getCaseTypeId(),
                        listingData.getManagingOffice()));
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

        ReportParams params = ReportHelper.getListingDateRangeForSearch(listingDetails);
        RespondentsReport respondentsReport = new RespondentsReport(reportDataSource);
        RespondentsReportData reportData = respondentsReport.generateReport(params);
        setSharedReportDocumentFields(reportData, listingDetails, true);
        return reportData;
    }

    private SessionDaysReportData getSessionDaysReport(ListingDetails listingDetails, String authToken) {
        log.info("Session Days Report for {}", listingDetails.getCaseTypeId());
        SessionDaysCcdReportDataSource reportDataSource = new SessionDaysCcdReportDataSource(authToken, ccdClient);
        ReportHelper.getListingDateRangeForSearch(listingDetails);

        SessionDaysReport sessionDaysReport = new SessionDaysReport(reportDataSource, judgeService);
        ListingData listingData = listingDetails.getCaseData();
        ReportParams params = ReportHelper.getListingDateRangeForSearch(listingDetails);
        SessionDaysReportData reportData = sessionDaysReport.generateReport(params);
        reportData.setDocumentName(listingData.getDocumentName());
        reportData.setReportType(listingData.getReportType());
        reportData.setHearingDateType(listingData.getHearingDateType());
        reportData.setManagingOffice(
                ReportHelper.getReportOfficeForDisplay(listingDetails.getCaseTypeId(),
                        listingData.getManagingOffice()));
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
        ReportHelper.getListingDateRangeForSearch(listingDetails);
        ListingData listingData = listingDetails.getCaseData();
        HearingsByHearingTypeReport hearingsByHearingTypeReport = new HearingsByHearingTypeReport(reportDataSource);
        ReportParams params = ReportHelper.getListingDateRangeForSearch(listingDetails);
        HearingsByHearingTypeReportData reportData = hearingsByHearingTypeReport
                .generateReport(params);
        setReportData(reportData, listingData);
        reportData.setManagingOffice(
                ReportHelper.getReportOfficeForDisplay(listingDetails.getCaseTypeId(),
                        listingData.getManagingOffice()));
        return reportData;
    }

    private void setSharedReportDocumentFields(ListingData reportData, ListingDetails listingDetails,
                                               boolean isDateRangeReport) {
        reportData.setDocumentName(listingDetails.getCaseData().getDocumentName());
        reportData.setReportType(listingDetails.getCaseData().getReportType());
        reportData.setManagingOffice(ReportHelper.getReportOfficeForDisplay(listingDetails.getCaseTypeId(),
                listingDetails.getCaseData().getManagingOffice()));

        if (isDateRangeReport) {
            reportData.setHearingDateType(listingDetails.getCaseData().getHearingDateType());
            reportData.setListingDateFrom(listingDetails.getCaseData().getListingDateFrom());
            reportData.setListingDateTo(listingDetails.getCaseData().getListingDateTo());
            reportData.setListingDate(listingDetails.getCaseData().getListingDate());
        }
    }
}
