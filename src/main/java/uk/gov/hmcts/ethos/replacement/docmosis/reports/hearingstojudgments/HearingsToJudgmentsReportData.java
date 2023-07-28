package uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.listing.ListingData;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_LINE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReportDocHelper.addJsonCollection;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.REPORT_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.REPORT_OFFICE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.TOTAL_CASES;

@Getter
public final class HearingsToJudgmentsReportData extends ListingData {
    // JsonIgnore is required on properties so that the report data is not
    // returned to CCD in any callback response.
    // Otherwise, this would trigger a CCD Case Data Validation error
    // because the properties are not in the CCD config

    public static final String TOTAL_WITHIN_4WEEKS = "\"Total_Within_4Weeks\":\"";
    public static final String TOTAL_PERCENT_WITHIN_4WEEKS = "\"Total_Percent_Within_4Weeks\":\"";
    public static final String TOTAL_NOT_WITHIN_4WEEKS = "\"Total_Not_Within_4Weeks\":\"";
    public static final String TOTAL_PERCENT_NOT_WITHIN_4WEEKS = "\"Total_Percent_Not_Within_4Weeks\":\"";

    @JsonIgnore
    private final HearingsToJudgmentsReportSummary reportSummary;
    @JsonIgnore
    private final List<HearingsToJudgmentsReportDetail> reportDetails = new ArrayList<>();

    public HearingsToJudgmentsReportData(HearingsToJudgmentsReportSummary hearingsToJudgmentsReportSummary) {
        super();
        this.reportSummary = hearingsToJudgmentsReportSummary;
    }

    public HearingsToJudgmentsReportSummary getReportSummary() {
        return reportSummary;
    }

    public void addReportDetail(HearingsToJudgmentsReportDetail reportDetail) {
        reportDetails.add(reportDetail);
    }

    public List<HearingsToJudgmentsReportDetail> getReportDetails() {
        return reportDetails;
    }

    public StringBuilder toReportObjectString() throws JsonProcessingException {
        StringBuilder sb = new StringBuilder();
        sb.append(REPORT_OFFICE).append(reportSummary.getOffice()).append(NEW_LINE);
        sb.append(TOTAL_CASES).append(
                StringUtils.defaultString(reportSummary.getTotalCases(), "0")).append(NEW_LINE);
        sb.append(TOTAL_WITHIN_4WEEKS).append(
                StringUtils.defaultString(reportSummary.getTotal4Wk(), "0")).append(NEW_LINE);
        sb.append(TOTAL_PERCENT_WITHIN_4WEEKS).append(
                StringUtils.defaultString(reportSummary.getTotal4WkPercent(), "0.00")).append(NEW_LINE);
        sb.append(TOTAL_NOT_WITHIN_4WEEKS).append(
                StringUtils.defaultString(reportSummary.getTotalX4Wk(), "0")).append(NEW_LINE);
        sb.append(TOTAL_PERCENT_NOT_WITHIN_4WEEKS).append(
                StringUtils.defaultString(reportSummary.getTotalX4WkPercent(), "0.00")).append(NEW_LINE);
        addJsonCollection(REPORT_DETAILS, reportDetails.iterator(), sb);
        return sb;
    }
}
