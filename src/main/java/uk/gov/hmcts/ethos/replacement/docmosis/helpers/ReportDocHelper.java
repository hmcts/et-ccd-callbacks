package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.items.AdhocReportTypeItem;
import uk.gov.hmcts.et.common.model.listing.types.AdhocReportType;
import uk.gov.hmcts.et.common.model.listing.types.ClaimServedType;
import uk.gov.hmcts.et.common.model.listing.types.ClaimServedTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportException;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment.CasesAwaitingJudgmentReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.eccreport.EccReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingsbyhearingtype.HearingsByHearingTypeReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.memberdays.MemberDaysReportDoc;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition.NoPositionChangeReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.respondentsreport.RespondentsReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.sessiondays.SessionDaysReportData;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASES_COMPLETED_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_SOURCE_LOCAL_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMS_ACCEPTED_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FILE_EXTENSION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARINGS_BY_HEARING_TYPE_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LIVE_CASELOAD_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MEMBER_DAYS_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_LINE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OUTPUT_FILE_NAME;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SERVING_CLAIMS_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SESSION_DAYS_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TIME_TO_FIRST_HEARING_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.CASES_AWAITING_JUDGMENT_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ECC_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.HEARINGS_TO_JUDGEMENTS_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.NO_CHANGE_IN_CURRENT_POSITION_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.REPORT_OFFICE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.RESPONDENTS_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.TOTAL_CASES;

@Slf4j
public final class ReportDocHelper {
    private static final String REPORT_LIST = "\"Report_List\":[\n";
    private static final String DAY_1_LIST = "\"Day_1_List\":[\n";
    private static final String DAY_2_LIST = "\"Day_2_List\":[\n";
    private static final String DAY_3_LIST = "\"Day_3_List\":[\n";
    private static final String DAY_4_LIST = "\"Day_4_List\":[\n";
    private static final String DAY_5_LIST = "\"Day_5_List\":[\n";
    private static final String DAY_6_LIST = "\"Day_6_List\":[\n";
    private static final String CASE_REFERENCE = "{\"Case_Reference\":\"";
    private static final String CANNOT_CREATE_REPORT_DATA_EXCEPTION = "Unable to create report data";
    private static final String LISTING_DATA_STATE_EXCEPTION = "ListingData is not instanceof ";
    private static  final String REPORT_DETAILS = "reportDetails";
    private static final int FIFTH_DAY = 5;

    private ReportDocHelper() {
    }

    public static StringBuilder buildReportDocumentContent(ListingData listingData, String accessKey,
                                                           String templateName, UserDetails userDetails) {
        log.info("Building {} report document data", listingData.getReportType());

        StringBuilder sb = new StringBuilder(120);
        sb.append("{\n\"accessKey\":\"").append(accessKey).append(NEW_LINE).append("\"templateName\":\"")
                .append(templateName).append(FILE_EXTENSION).append(NEW_LINE).append("\"outputName\":\"")
                .append(OUTPUT_FILE_NAME).append(NEW_LINE).append("\"data\":{\n");

        switch (listingData.getReportType()) {
            case CLAIMS_ACCEPTED_REPORT -> {
                sb.append(ListingHelper.getListingDate(listingData));
                addReportOffice(listingData, sb);
                sb.append(getCasesAcceptedReport(listingData));
            }
            case LIVE_CASELOAD_REPORT -> {
                sb.append(ListingHelper.getListingDate(listingData));
                addReportOffice(listingData, sb);
                sb.append(getLiveCaseLoadReport(listingData));
            }
            case CASES_COMPLETED_REPORT -> {
                sb.append(ListingHelper.getListingDate(listingData));
                addReportOffice(listingData, sb);
                sb.append(getCasesCompletedReport(listingData));
            }
            case TIME_TO_FIRST_HEARING_REPORT -> {
                sb.append(ListingHelper.getListingDate(listingData));
                addReportOffice(listingData, sb);
                sb.append(getTimeToFirstHearingReport(listingData));
            }
            case CASE_SOURCE_LOCAL_REPORT -> {
                sb.append(ListingHelper.getListingDate(listingData));
                addReportOffice(listingData, sb);
                sb.append(getCaseSourceLocalReport(listingData));
            }
            case SERVING_CLAIMS_REPORT -> {
                sb.append(ListingHelper.getListingDate(listingData));
                addReportOffice(listingData, sb);
                sb.append(getServedClaimsReport(listingData));
            }
            case HEARINGS_BY_HEARING_TYPE_REPORT -> {
                try {
                    sb.append(ListingHelper.getListingDate(listingData));
                    sb.append(getHearingsByHearingTypeReport(listingData));
                } catch (JsonProcessingException e) {
                    throw new ReportException(CANNOT_CREATE_REPORT_DATA_EXCEPTION, e);
                }
            }
            case CASES_AWAITING_JUDGMENT_REPORT -> {
                try {
                    sb.append(getCasesAwaitingJudgmentReport(listingData));
                } catch (JsonProcessingException e) {
                    throw new ReportException(CANNOT_CREATE_REPORT_DATA_EXCEPTION, e);
                }
            }
            case HEARINGS_TO_JUDGEMENTS_REPORT -> {
                sb.append(ListingHelper.getListingDate(listingData));
                sb.append(getHearingsToJudgmentsReport(listingData));
            }
            case RESPONDENTS_REPORT -> {
                try {
                    sb.append(ListingHelper.getListingDate(listingData));
                    sb.append(getRespondentsReport(listingData));
                } catch (JsonProcessingException e) {
                    throw new ReportException(CANNOT_CREATE_REPORT_DATA_EXCEPTION, e);
                }
            }
            case SESSION_DAYS_REPORT -> {
                try {
                    sb.append(ListingHelper.getListingDate(listingData));
                    sb.append(getSessionDaysReport(listingData));
                } catch (JsonProcessingException e) {
                    throw new ReportException(CANNOT_CREATE_REPORT_DATA_EXCEPTION, e);
                }
            }
            case ECC_REPORT -> {
                try {
                    sb.append(ListingHelper.getListingDate(listingData));
                    sb.append(getEccReport(listingData));
                } catch (JsonProcessingException e) {
                    throw new ReportException(CANNOT_CREATE_REPORT_DATA_EXCEPTION, e);
                }
            }
            case NO_CHANGE_IN_CURRENT_POSITION_REPORT -> sb.append(getNoPositionChangeReport(listingData));
            case MEMBER_DAYS_REPORT -> sb.append(new MemberDaysReportDoc().getReportDocPart(listingData));
            default ->
                    throw new IllegalStateException("Report type - Unexpected value: " + listingData.getReportType());
        }

        String userName = nullCheck(userDetails.getFirstName() + " " + userDetails.getLastName());
        sb.append("\"Report_Clerk\":\"").append(nullCheck(userName)).append(NEW_LINE).append("\"Today_date\":\"")
                .append(UtilHelper.formatCurrentDate(LocalDate.now())).append("\"\n}\n}\n");
        return sb;
    }

    private static void addReportOffice(ListingData listingData, StringBuilder sb) {
        if (listingData.getLocalReportsDetailHdr() != null) {
            sb.append(REPORT_OFFICE).append(
                    nullCheck(listingData.getLocalReportsDetailHdr().getReportOffice())).append(NEW_LINE);
        } else if (CollectionUtils.isNotEmpty(listingData.getLocalReportsSummary())) {
            sb.append(REPORT_OFFICE).append(
                    nullCheck(listingData.getLocalReportsSummary().get(0)
                            .getValue().getReportOffice())).append(NEW_LINE);
        }
    }

    private static StringBuilder getHearingsToJudgmentsReport(ListingData listingData) {
        if (!(listingData instanceof HearingsToJudgmentsReportData)) {
            throw new IllegalStateException(LISTING_DATA_STATE_EXCEPTION + "HearingsToJudgmentsReportData");
        }

        StringBuilder sb = new StringBuilder();
        try {
            HearingsToJudgmentsReportData reportData = (HearingsToJudgmentsReportData) listingData;
            sb.append(reportData.toReportObjectString());
        } catch (JsonProcessingException e) {
            throw new ReportException(CANNOT_CREATE_REPORT_DATA_EXCEPTION, e);
        }
        return sb;
    }

    private static StringBuilder getNoPositionChangeReport(ListingData listingData) {
        if (!(listingData instanceof NoPositionChangeReportData)) {
            throw new IllegalStateException(LISTING_DATA_STATE_EXCEPTION + "NoPositionChangeReportData");
        }

        StringBuilder sb = new StringBuilder();
        try {
            NoPositionChangeReportData reportData = (NoPositionChangeReportData) listingData;
            sb.append(reportData.toReportObjectString());
        } catch (JsonProcessingException e) {
            throw new ReportException(CANNOT_CREATE_REPORT_DATA_EXCEPTION, e);
        }
        return sb;
    }

    private static StringBuilder getCasesAwaitingJudgmentReport(ListingData listingData)
            throws JsonProcessingException {
        if (!(listingData instanceof CasesAwaitingJudgmentReportData)) {
            throw new IllegalStateException(LISTING_DATA_STATE_EXCEPTION + "CasesAwaitingJudgmentReportData");
        }
        CasesAwaitingJudgmentReportData reportData = (CasesAwaitingJudgmentReportData) listingData;
        StringBuilder sb = new StringBuilder();
        sb.append(REPORT_OFFICE).append(reportData.getReportSummary().getOffice()).append(NEW_LINE);
        addJsonCollection("positionTypes", reportData.getReportSummary().getPositionTypes().iterator(), sb);
        addJsonCollection(REPORT_DETAILS, reportData.getReportDetails().iterator(), sb);
        return sb;
    }

    public static void addJsonCollection(String name, Iterator<?> iterator, StringBuilder sb)
            throws JsonProcessingException {
        sb.append('"').append(name).append("\":[\n");
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        while (iterator.hasNext()) {
            sb.append(objectMapper.writeValueAsString(iterator.next()));
            if (iterator.hasNext()) {
                sb.append(',');
            }
            sb.append('\n');
        }
        sb.append("],\n");
    }

    private static StringBuilder getCasesAcceptedReport(ListingData listingData) {
        StringBuilder sb = new StringBuilder();
        AdhocReportType localReportDetailHdr = listingData.getLocalReportsDetailHdr();
        if (localReportDetailHdr != null) {
            sb.append("\"Multiple_Claims_Accepted\":\"").append(nullCheck(localReportDetailHdr.getMultiplesTotal()))
                    .append(NEW_LINE).append("\"Singles_Claims_Accepted\":\"")
                    .append(nullCheck(localReportDetailHdr.getSinglesTotal())).append(NEW_LINE)
                    .append("\"Total_Claims_Accepted\":\"").append(nullCheck(localReportDetailHdr.getTotal()))
                    .append(NEW_LINE);
        }

        if (listingData.getLocalReportsDetail() != null && !listingData.getLocalReportsDetail().isEmpty()) {
            sb.append(getClaimsAcceptedByCaseType(listingData));
        }

        return sb;
    }

    private static StringBuilder getClaimsAcceptedByCaseType(ListingData listingData) {
        StringBuilder sb = new StringBuilder(80);
        Map<Boolean, List<AdhocReportTypeItem>> unsortedMap = listingData.getLocalReportsDetail().stream()
                .collect(Collectors.partitioningBy(localReportDetail ->
                        localReportDetail.getValue().getMultipleRef() != null));
        sb.append("\"Local_Report_By_Type\":[\n");
        Iterator<Map.Entry<Boolean, List<AdhocReportTypeItem>>> entries =
                new TreeMap<>(unsortedMap).entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<Boolean, List<AdhocReportTypeItem>> localReportEntry = entries.next();
            String singleOrMultiple = Boolean.TRUE.equals(localReportEntry.getKey()) ? "Multiples" : "Singles";
            sb.append("{\"Case_Type\":\"").append(singleOrMultiple).append(NEW_LINE).append("\"Claims_Number\":\"")
                    .append(localReportEntry.getValue().size()).append(NEW_LINE).append(REPORT_LIST);
            for (int i = 0; i < localReportEntry.getValue().size(); i++) {
                sb.append(getAdhocReportCommonTypeRow(localReportEntry.getValue().get(i).getValue()));
                if (i != localReportEntry.getValue().size() - 1) {
                    sb.append(",\n");
                }
            }
            sb.append("]\n");
            if (entries.hasNext()) {
                sb.append("},\n");
            } else {
                sb.append("}],\n");
            }
        }
        return sb;
    }

    private static StringBuilder getAdhocReportCommonTypeRow(AdhocReportType adhocReportType) {
        StringBuilder sb = new StringBuilder(140);
        sb.append(CASE_REFERENCE).append(nullCheck(adhocReportType.getCaseReference())).append(NEW_LINE)
                .append("\"Date_Of_Acceptance\":\"").append(nullCheck(adhocReportType.getDateOfAcceptance()))
                .append(NEW_LINE).append("\"Multiple_Ref\":\"").append(nullCheck(adhocReportType.getMultipleRef()))
                .append(NEW_LINE).append("\"Lead_Case\":\"").append(nullCheck(adhocReportType.getLeadCase()))
                .append(NEW_LINE).append("\"Position\":\"").append(nullCheck(adhocReportType.getPosition()))
                .append(NEW_LINE).append("\"Date_To_Position\":\"")
                .append(nullCheck(adhocReportType.getDateToPosition())).append(NEW_LINE).append("\"File_Location\":\"")
                .append(nullCheck(adhocReportType.getFileLocation())).append(NEW_LINE).append("\"Clerk\":\"")
                .append(nullCheck(adhocReportType.getClerk())).append("\"}");
        return sb;
    }

    private static StringBuilder getCaseSourceLocalReport(ListingData listingData) {
        StringBuilder sb = new StringBuilder();
        if (CollectionUtils.isEmpty(listingData.getLocalReportsSummary())) {
            return sb;
        }
        AdhocReportType localReportSummary = listingData.getLocalReportsSummary().get(0).getValue();
        if (localReportSummary != null) {

            sb.append("\"Manually_Created\":\"").append(nullCheck(localReportSummary.getManuallyCreatedTotalCases()))
                    .append(NEW_LINE).append("\"Migration_Cases\":\"")
                    .append(nullCheck(localReportSummary.getMigratedTotalCases())).append(NEW_LINE)
                    .append("\"ET1_Online_Cases\":\"").append(nullCheck(localReportSummary.getEt1OnlineTotalCases()))
                    .append(NEW_LINE).append("\"ECC_Cases\":\"")
                    .append(nullCheck(localReportSummary.getEccTotalCases())).append(NEW_LINE)
                    .append("\"Manually_Created_Percent\":\"")
                    .append(nullCheck(localReportSummary.getManuallyCreatedTotalCasesPercent())).append(NEW_LINE)
                    .append("\"Migration_Cases_Percent\":\"")
                    .append(nullCheck(localReportSummary.getMigratedTotalCasesPercent())).append(NEW_LINE)
                    .append("\"ET1_Online_Cases_Percent\":\"")
                    .append(nullCheck(localReportSummary.getEt1OnlineTotalCasesPercent())).append(NEW_LINE)
                    .append("\"ECC_Cases_Percent\":\"").append(nullCheck(localReportSummary.getEccTotalCasesPercent()))
                    .append(NEW_LINE);
        }
        return sb;
    }

    private static StringBuilder getTimeToFirstHearingReport(ListingData listingData) {
        StringBuilder sb = new StringBuilder();
        AdhocReportType localReportDetailHdr = listingData.getLocalReportsDetailHdr();
        AdhocReportType localReportSummary = listingData.getLocalReportsSummary().get(0).getValue();
        if (localReportDetailHdr != null) {
            sb.append(TOTAL_CASES).append(nullCheck(localReportDetailHdr.getTotalCases())).append(NEW_LINE)
                    .append("\"Total_Within_26Weeks\":\"").append(nullCheck(localReportDetailHdr.getTotal26wk()))
                    .append(NEW_LINE).append("\"Total_Percent_Within_26Weeks\":\"")
                    .append(nullCheck(localReportDetailHdr.getTotal26wkPerCent())).append(NEW_LINE)
                    .append("\"Total_Not_Within_26Weeks\":\"").append(nullCheck(localReportDetailHdr.getTotalx26wk()))
                    .append(NEW_LINE).append("\"Total_Percent_Not_Within_26Weeks\":\"")
                    .append(nullCheck(localReportDetailHdr.getTotalx26wkPerCent())).append(NEW_LINE)
                    .append("\"ConNone_Total\":\"").append(nullCheck(localReportSummary.getConNoneTotal()))
                    .append(NEW_LINE).append("\"ConNone_Total_26_Week\":\"")
                    .append(nullCheck(localReportSummary.getConNone26wkTotal())).append(NEW_LINE)
                    .append("\"ConNone_Percent_26_Week\":\"")
                    .append(nullCheck(localReportSummary.getConNone26wkTotalPerCent())).append(NEW_LINE)
                    .append("\"ConNone_Total_Not_26_Week\":\"")
                    .append(nullCheck(localReportSummary.getNotConNone26wkTotal())).append(NEW_LINE)
                    .append("\"ConNone_Percent_Not_26_Week\":\"")
                    .append(nullCheck(localReportSummary.getNotConNone26wkTotalPerCent())).append(NEW_LINE)
                    .append("\"ConFast_Total\":\"").append(nullCheck(localReportSummary.getConFastTotal()))
                    .append(NEW_LINE).append("\"ConFast_Total_26_Week\":\"")
                    .append(nullCheck(localReportSummary.getConFast26wkTotal())).append(NEW_LINE)
                    .append("\"ConFast_Percent_26_Week\":\"")
                    .append(nullCheck(localReportSummary.getConFast26wkTotalPerCent())).append(NEW_LINE)
                    .append("\"ConFast_Total_Not_26_Week\":\"")
                    .append(nullCheck(localReportSummary.getNotConFast26wkTotal())).append(NEW_LINE)
                    .append("\"ConFast_Percent_Not_26_Week\":\"")
                    .append(nullCheck(localReportSummary.getNotConFast26wkTotalPerCent())).append(NEW_LINE)
                    .append("\"ConStd_Total\":\"").append(nullCheck(localReportSummary.getConStdTotal()))
                    .append(NEW_LINE).append("\"ConStd_Total_26_Week\":\"")
                    .append(nullCheck(localReportSummary.getConStd26wkTotal())).append(NEW_LINE)
                    .append("\"ConStd_Percent_26_Week\":\"")
                    .append(nullCheck(localReportSummary.getConStd26wkTotalPerCent())).append(NEW_LINE)
                    .append("\"ConStd_Total_Not_26_Week\":\"")
                    .append(nullCheck(localReportSummary.getNotConStd26wkTotal())).append(NEW_LINE)
                    .append("\"ConStd_Percent_Not_26_Week\":\"")
                    .append(nullCheck(localReportSummary.getNotConStd26wkTotalPerCent())).append(NEW_LINE)
                    .append("\"ConOpen_Total\":\"").append(nullCheck(localReportSummary.getConOpenTotal()))
                    .append(NEW_LINE).append("\"ConOpen_Total_26_Week\":\"")
                    .append(nullCheck(localReportSummary.getConOpen26wkTotal())).append(NEW_LINE)
                    .append("\"ConOpen_Percent_26_Week\":\"")
                    .append(nullCheck(localReportSummary.getConOpen26wkTotalPerCent())).append(NEW_LINE)
                    .append("\"ConOpen_Total_Not_26_Week\":\"")
                    .append(nullCheck(localReportSummary.getNotConOpen26wkTotal())).append(NEW_LINE)
                    .append("\"ConOpen_Percent_Not_26_Week\":\"")
                    .append(nullCheck(localReportSummary.getNotConOpen26wkTotalPerCent())).append(NEW_LINE);

        }

        if (CollectionUtils.isNotEmpty(listingData.getLocalReportsDetail())) {
            List<AdhocReportTypeItem> adhocReportTypeItems = listingData.getLocalReportsDetail();
            sb.append(REPORT_LIST);
            for (int i = 0; i < adhocReportTypeItems.size(); i++) {
                sb.append(getTimeToFirstHearingAdhocReportTypeRow(adhocReportTypeItems.get(i).getValue()));
                if (i != adhocReportTypeItems.size() - 1) {
                    sb.append(",\n");
                }
            }
            sb.append("],\n");
        }
        return sb;
    }

    private static StringBuilder getCasesCompletedReport(ListingData listingData) {
        StringBuilder sb = new StringBuilder();
        AdhocReportType localReportDetailHdr = listingData.getLocalReportsDetailHdr();
        if (localReportDetailHdr != null) {
            sb.append("\"Cases_Completed_Hearing\":\"")
                    .append(nullCheck(localReportDetailHdr.getCasesCompletedHearingTotal())).append(NEW_LINE)
                    .append("\"Session_Days_Taken\":\"").append(nullCheck(localReportDetailHdr.getSessionDaysTotal()))
                    .append(NEW_LINE).append("\"Completed_Per_Session_Day\":\"")
                    .append(nullCheck(localReportDetailHdr.getCompletedPerSessionTotal())).append(NEW_LINE)
                    .append("\"No_Conciliation_1\":\"")
                    .append(nullCheck(localReportDetailHdr.getConNoneCasesCompletedHearing())).append(NEW_LINE)
                    .append("\"No_Conciliation_2\":\"").append(nullCheck(localReportDetailHdr.getConNoneSessionDays()))
                    .append(NEW_LINE).append("\"No_Conciliation_3\":\"")
                    .append(nullCheck(localReportDetailHdr.getConNoneCompletedPerSession())).append(NEW_LINE)
                    .append("\"Fast_Track_1\":\"")
                    .append(nullCheck(localReportDetailHdr.getConFastCasesCompletedHearing())).append(NEW_LINE)
                    .append("\"Fast_Track_2\":\"").append(nullCheck(localReportDetailHdr.getConFastSessionDays()))
                    .append(NEW_LINE).append("\"Fast_Track_3\":\"")
                    .append(nullCheck(localReportDetailHdr.getConFastCompletedPerSession())).append(NEW_LINE)
                    .append("\"Standard_Track_1\":\"")
                    .append(nullCheck(localReportDetailHdr.getConStdCasesCompletedHearing())).append(NEW_LINE)
                    .append("\"Standard_Track_2\":\"").append(nullCheck(localReportDetailHdr.getConStdSessionDays()))
                    .append(NEW_LINE).append("\"Standard_Track_3\":\"")
                    .append(nullCheck(localReportDetailHdr.getConStdCompletedPerSession())).append(NEW_LINE)
                    .append("\"Open_Track_1\":\"")
                    .append(nullCheck(localReportDetailHdr.getConOpenCasesCompletedHearing())).append(NEW_LINE)
                    .append("\"Open_Track_2\":\"").append(nullCheck(localReportDetailHdr.getConOpenSessionDays()))
                    .append(NEW_LINE).append("\"Open_Track_3\":\"")
                    .append(nullCheck(localReportDetailHdr.getConOpenCompletedPerSession())).append(NEW_LINE);

        }

        if (listingData.getLocalReportsDetail() != null && !listingData.getLocalReportsDetail().isEmpty()) {
            List<AdhocReportTypeItem> adhocReportTypeItems = listingData.getLocalReportsDetail();
            sb.append(REPORT_LIST);
            for (int i = 0; i < adhocReportTypeItems.size(); i++) {
                sb.append(getAdhocReportCompletedTypeRow(adhocReportTypeItems.get(i).getValue()));
                if (i != adhocReportTypeItems.size() - 1) {
                    sb.append(",\n");
                }
            }
            sb.append("],\n");
        }
        return sb;
    }

    private static StringBuilder getAdhocReportCompletedTypeRow(AdhocReportType adhocReportType) {
        StringBuilder sb = new StringBuilder(170);
        sb.append(CASE_REFERENCE).append(nullCheck(adhocReportType.getCaseReference())).append(NEW_LINE)
                .append("\"Position\":\"").append(nullCheck(adhocReportType.getPosition())).append(NEW_LINE)
                .append("\"Conciliation_Track\":\"").append(nullCheck(adhocReportType.getConciliationTrack()))
                .append(NEW_LINE).append("\"Session_Days\":\"").append(nullCheck(adhocReportType.getSessionDays()))
                .append(NEW_LINE).append("\"Hearing_Number\":\"").append(nullCheck(adhocReportType.getHearingNumber()))
                .append(NEW_LINE).append("\"Hearing_Date\":\"")
                .append(UtilHelper.formatLocalDate(adhocReportType.getHearingDate())).append(NEW_LINE)
                .append("\"Hearing_Type\":\"").append(nullCheck(adhocReportType.getHearingType())).append(NEW_LINE)
                .append("\"Hearing_Judge\":\"").append(nullCheck(adhocReportType.getHearingJudge())).append(NEW_LINE)
                .append("\"Hearing_Clerk\":\"").append(nullCheck(adhocReportType.getHearingClerk())).append("\"}");
        return sb;
    }

    private static StringBuilder getTimeToFirstHearingAdhocReportTypeRow(AdhocReportType adhocReportType) {
        StringBuilder sb = new StringBuilder(120);
        sb.append("{\"Office\":\"").append(nullCheck(adhocReportType.getReportOffice())).append(NEW_LINE)
                .append("\"Case_Reference\":\"").append(nullCheck(adhocReportType.getCaseReference())).append(NEW_LINE)
                .append("\"Conciliation_Track\":\"").append(nullCheck(adhocReportType.getConciliationTrack()))
                .append(NEW_LINE).append("\"Receipt_Date\":\"").append(nullCheck(adhocReportType.getReceiptDate()))
                .append(NEW_LINE).append("\"Hearing_Date\":\"").append(nullCheck(adhocReportType.getHearingDate()))
                .append(NEW_LINE).append("\"Days\":\"")
                .append(nullCheck(adhocReportType.getDelayedDaysForFirstHearing())).append("\"}");
        return sb;
    }

    private static StringBuilder getLiveCaseLoadReportSummaryHdr(ListingData listingData) {
        StringBuilder sb = new StringBuilder(60);
        String singlesTotal = "0";
        String multiplesTotal = "0";
        String total = "0";
        AdhocReportType summaryHdr = listingData.getLocalReportsSummaryHdr();

        if (summaryHdr != null) {
            singlesTotal = nullCheck(summaryHdr.getSinglesTotal());
            multiplesTotal = nullCheck(summaryHdr.getMultiplesTotal());
            total = nullCheck(summaryHdr.getTotal());
        }

        sb.append("\"Multiples_Total\":\"").append(multiplesTotal).append(NEW_LINE).append("\"Singles_Total\":\"")
                .append(singlesTotal).append(NEW_LINE).append("\"Total\":\"").append(total).append(NEW_LINE);

        return sb;
    }

    private static StringBuilder getLiveCaseLoadReport(ListingData listingData) {
        StringBuilder sb = getLiveCaseLoadReportSummaryHdr(listingData);

        if (CollectionUtils.isNotEmpty(listingData.getLocalReportsDetail())) {
            List<AdhocReportTypeItem> adhocReportTypeItems = listingData.getLocalReportsDetail();
            sb.append(REPORT_LIST);
            for (int i = 0; i < adhocReportTypeItems.size(); i++) {
                sb.append(getAdhocReportCommonTypeRow(adhocReportTypeItems.get(i).getValue()));
                if (i != adhocReportTypeItems.size() - 1) {
                    sb.append(",\n");
                }
            }
            sb.append("],\n");
        }
        return sb;
    }

    private static StringBuilder getServedClaimsReport(ListingData listingData) {
        StringBuilder reportContent = getServedClaimsReportSummary(listingData);
        int claimsServedDayListUpperBoundary = 5;

        if (CollectionUtils.isNotEmpty(listingData.getLocalReportsDetail())) {
            List<String> listBlockOpeners = List.of(DAY_1_LIST, DAY_2_LIST, DAY_3_LIST, DAY_4_LIST,
                    DAY_5_LIST, DAY_6_LIST);
            for (int dayIndex = 0; dayIndex <= claimsServedDayListUpperBoundary; dayIndex++) {
                addEntriesByServingDay(dayIndex, listBlockOpeners.get(dayIndex),
                        reportContent, listingData);
            }
        }

        return reportContent;
    }

    private static void addEntriesByServingDay(int dayNumber, String listBlockOpener,
                                               StringBuilder reportContent, ListingData listingData) {
        AdhocReportTypeItem itemsList = listingData.getLocalReportsDetail().get(0);
        List<ClaimServedTypeItem> claimServedTypeItems =
                new java.util.ArrayList<>(itemsList.getValue().getClaimServedItems()
                .stream().filter(item -> Integer.parseInt(item.getValue().getReportedNumberOfDays()) == dayNumber)
                .toList());
        int claimServedTypeItemsCount = claimServedTypeItems.size();
        String claimServedTypeItemsListSize = String.valueOf(claimServedTypeItems.size());

        if (dayNumber >= FIFTH_DAY) {
            claimServedTypeItems.sort(Comparator.comparingInt(item ->
                Integer.parseInt(item.getValue().getActualNumberOfDays())));
        }

        reportContent.append(listBlockOpener);

        if (claimServedTypeItemsCount == 0) {
            reportContent.append(CASE_REFERENCE).append(claimServedTypeItemsListSize).append(NEW_LINE);
            if (dayNumber >= FIFTH_DAY) {
                reportContent.append("\"Actual_Number_Of_Days\":\"")
                        .append(claimServedTypeItemsListSize).append(NEW_LINE);
            }
            reportContent.append("\"Date_Of_Receipt\":\"").append(claimServedTypeItemsListSize).append(NEW_LINE)
                    .append("\"Date_Of_Service\":\"").append(claimServedTypeItemsListSize).append("\"},\n");
        } else {
            for (int i = 0; i < claimServedTypeItemsCount; i++) {
                reportContent.append(getServedClaimsReportRow(claimServedTypeItems.get(i).getValue(), dayNumber));
                if (i != claimServedTypeItemsCount - 1) {
                    reportContent.append(",\n");
                }
            }
        }

        reportContent.append("],\n");
        String currentDayTotal = "\"day_" + (dayNumber + 1) + "_total_count\":\"";
        reportContent.append(currentDayTotal)
                .append(claimServedTypeItemsListSize).append(NEW_LINE);
    }

    private static StringBuilder getServedClaimsReportRow(ClaimServedType claimServedTypeItem, int dayNumber) {
        StringBuilder reportRowContent = new StringBuilder(55);
        int claimsServedDayListUpperBoundary = 5;

        reportRowContent.append(CASE_REFERENCE)
                .append(nullCheck(claimServedTypeItem.getClaimServedCaseNumber())).append(NEW_LINE);

        if (dayNumber >= claimsServedDayListUpperBoundary) {
            reportRowContent.append("\"Actual_Number_Of_Days\":\"")
                    .append(nullCheck(claimServedTypeItem.getActualNumberOfDays())).append(NEW_LINE);
        }

        reportRowContent.append("\"Date_Of_Receipt\":\"").append(nullCheck(claimServedTypeItem.getCaseReceiptDate()))
                .append(NEW_LINE).append("\"Date_Of_Service\":\"")
                .append(nullCheck(claimServedTypeItem.getClaimServedDate())).append("\"}");

        return reportRowContent;
    }

    private static StringBuilder getServedClaimsReportSummary(ListingData listingData) {
        StringBuilder reportSummaryContent = new StringBuilder();

        if (CollectionUtils.isNotEmpty(listingData.getLocalReportsDetail())) {
            AdhocReportTypeItem adhocReportTypeItem = listingData.getLocalReportsDetail().get(0);
            AdhocReportType adhocReportType = adhocReportTypeItem.getValue();

            reportSummaryContent.append("\"Day_1_Tot\":\"").append(adhocReportType.getClaimServedDay1Total())
                    .append(NEW_LINE).append("\"Day_1_Pct\":\"").append(adhocReportType.getClaimServedDay1Percent())
                    .append(NEW_LINE).append("\"Day_2_Tot\":\"").append(adhocReportType.getClaimServedDay2Total())
                    .append(NEW_LINE).append("\"Day_2_Pct\":\"").append(adhocReportType.getClaimServedDay2Percent())
                    .append(NEW_LINE).append("\"Day_3_Tot\":\"").append(adhocReportType.getClaimServedDay3Total())
                    .append(NEW_LINE).append("\"Day_3_Pct\":\"").append(adhocReportType.getClaimServedDay3Percent())
                    .append(NEW_LINE).append("\"Day_4_Tot\":\"").append(adhocReportType.getClaimServedDay4Total())
                    .append(NEW_LINE).append("\"Day_4_Pct\":\"").append(adhocReportType.getClaimServedDay4Percent())
                    .append(NEW_LINE).append("\"Day_5_Tot\":\"").append(adhocReportType.getClaimServedDay5Total())
                    .append(NEW_LINE).append("\"Day_5_Pct\":\"").append(adhocReportType.getClaimServedDay5Percent())
                    .append(NEW_LINE).append("\"Day_6_Plus_Tot\":\"")
                    .append(adhocReportType.getClaimServed6PlusDaysTotal()).append(NEW_LINE)
                    .append("\"Day_6_Plus_Pct\":\"").append(adhocReportType.getClaimServed6PlusDaysPercent())
                    .append(NEW_LINE).append("\"Total_Claims\":\"").append(adhocReportType.getClaimServedTotal())
                    .append(NEW_LINE);

        }

        return reportSummaryContent;
    }

    private static StringBuilder getRespondentsReport(ListingData listingData)
            throws JsonProcessingException {
        if (!(listingData instanceof RespondentsReportData)) {
            throw new IllegalStateException(LISTING_DATA_STATE_EXCEPTION + "RespondentsReportData");
        }
        RespondentsReportData reportData = (RespondentsReportData) listingData;

        StringBuilder sb = new StringBuilder(30);
        sb.append(REPORT_OFFICE).append(reportData.getReportSummary().getOffice()).append(NEW_LINE)
                .append("\"MoreThan1Resp\":\"")
                .append(nullCheck(reportData.getReportSummary().getTotalCasesWithMoreThanOneRespondent()))
                .append(NEW_LINE);
        addJsonCollection(REPORT_DETAILS, reportData.getReportDetails().iterator(), sb);
        return sb;
    }

    private static StringBuilder getSessionDaysReport(ListingData listingData)
            throws JsonProcessingException {
        if (!(listingData instanceof SessionDaysReportData)) {
            throw new IllegalStateException(LISTING_DATA_STATE_EXCEPTION + "SessionDaysReportData");
        }
        SessionDaysReportData reportData = (SessionDaysReportData) listingData;

        StringBuilder sb = new StringBuilder(120);
        sb.append(REPORT_OFFICE).append(reportData.getReportSummary().getOffice()).append(NEW_LINE)
                .append("\"ftcSessionDays\":\"")
                .append(nullCheck(reportData.getReportSummary().getFtSessionDaysTotal())).append(NEW_LINE)
                .append("\"ptcSessionDays\":\"")
                .append(nullCheck(reportData.getReportSummary().getPtSessionDaysTotal())).append(NEW_LINE)
                .append("\"otherSessionDays\":\"")
                .append(nullCheck(reportData.getReportSummary().getOtherSessionDaysTotal())).append(NEW_LINE)
                .append("\"totalSessionDays\":\"")
                .append(nullCheck(reportData.getReportSummary().getSessionDaysTotal())).append(NEW_LINE)
                .append("\"percentPtcSessionDays\":\"")
                .append(nullCheck(reportData.getReportSummary().getPtSessionDaysPerCent())).append(NEW_LINE);
        addJsonCollection("reportSummary2", reportData.getReportSummary2List().iterator(), sb);
        addJsonCollection(REPORT_DETAILS, reportData.getReportDetails().iterator(), sb);
        return sb;
    }

    private static StringBuilder getEccReport(ListingData listingData)
            throws JsonProcessingException {
        if (!(listingData instanceof EccReportData)) {
            throw new IllegalStateException(LISTING_DATA_STATE_EXCEPTION + "EccReportData");
        }
        EccReportData reportData = (EccReportData) listingData;
        StringBuilder sb = new StringBuilder();
        sb.append(REPORT_OFFICE).append(reportData.getOffice()).append(NEW_LINE);
        if (CollectionUtils.isNotEmpty(reportData.getReportDetails())) {
            addJsonCollection(REPORT_DETAILS, reportData.getReportDetails().iterator(), sb);
        }
        return sb;
    }

    private static StringBuilder getHearingsByHearingTypeReport(ListingData listingData)
            throws JsonProcessingException {
        if (!(listingData instanceof HearingsByHearingTypeReportData)) {
            throw new IllegalStateException(LISTING_DATA_STATE_EXCEPTION + "HearingsByHearingTypeReportData");
        }
        HearingsByHearingTypeReportData reportData = (HearingsByHearingTypeReportData) listingData;
        StringBuilder sb = new StringBuilder(175);
        sb.append(REPORT_OFFICE).append(reportData.getReportSummaryHdr().getOffice()).append(NEW_LINE)
                .append("\"cm_SummaryHdr\":\"")
                .append(nullCheck(reportData.getReportSummaryHdr().getFields().getCmCount())).append(NEW_LINE)
                .append("\"hearing_SummaryHdr\":\"")
                .append(nullCheck(reportData.getReportSummaryHdr().getFields().getHearingCount())).append(NEW_LINE)
                .append("\"preLim_SummaryHdr\":\"")
                .append(nullCheck(reportData.getReportSummaryHdr().getFields().getHearingPrelimCount()))
                .append(NEW_LINE).append("\"total_SummaryHdr\":\"")
                .append(nullCheck(reportData.getReportSummaryHdr().getFields().getTotal())).append(NEW_LINE)
                .append("\"costs_SummaryHdr\":\"")
                .append(nullCheck(reportData.getReportSummaryHdr().getFields().getCostsCount())).append(NEW_LINE)
                .append("\"remedy_SummaryHdr\":\"")
                .append(nullCheck(reportData.getReportSummaryHdr().getFields().getRemedyCount())).append(NEW_LINE)
                .append("\"reconsider_SummaryHdr\":\"")
                .append(nullCheck(reportData.getReportSummaryHdr().getFields().getReconsiderCount())).append(NEW_LINE);
        addJsonCollection("reportSummary1", reportData.getReportSummaryList().iterator(), sb);
        addJsonCollection("reportSummary2Hdr", reportData.getReportSummary2HdrList().iterator(), sb);
        addJsonCollection("reportSummary2", reportData.getReportSummary2List().iterator(), sb);
        addJsonCollection(REPORT_DETAILS, reportData.getReportDetails().iterator(), sb);
        return sb;
    }
}
