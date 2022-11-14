package uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_LINE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.REPORT_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.REPORT_OFFICE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.TOTAL_CASES;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsReportData.TOTAL_NOT_WITHIN_4WEEKS;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsReportData.TOTAL_PERCENT_NOT_WITHIN_4WEEKS;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsReportData.TOTAL_PERCENT_WITHIN_4WEEKS;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsReportData.TOTAL_WITHIN_4WEEKS;

@SuppressWarnings({"PMD.ConsecutiveAppendsShouldReuse", "PMD.ConsecutiveLiteralAppends", "PMD.LawOfDemeter"})
class HearingsToJudgmentsReportDataTest {

    @Test
    void shouldReturnValidJson() throws JsonProcessingException {
        HearingsToJudgmentsReportData reportData = setupValidReportData();
        StringBuilder resultJsonString = reportData.toReportObjectString();

        StringBuilder expectedJsonString = getExpectedJsonString(reportData);
        assertEquals(expectedJsonString.toString(), resultJsonString.toString());
    }

    @Test
    void shouldReturnValidJsonWithEmptyValues() throws JsonProcessingException {
        HearingsToJudgmentsReportSummary reportSummary = new HearingsToJudgmentsReportSummary("Office");
        HearingsToJudgmentsReportData reportData = new HearingsToJudgmentsReportData(reportSummary);
        StringBuilder resultJsonString = reportData.toReportObjectString();

        StringBuilder expectedJsonString = getExpectedJsonString(reportData);
        assertEquals(expectedJsonString.toString(), resultJsonString.toString());
    }

    private HearingsToJudgmentsReportData setupValidReportData() {
        HearingsToJudgmentsReportSummary reportSummary = new HearingsToJudgmentsReportSummary("Office");
        reportSummary.setTotalCases("2");
        reportSummary.setTotal4Wk("1");
        reportSummary.setTotal4WkPercent("50.00");
        reportSummary.setTotalX4Wk("1");
        reportSummary.setTotalX4WkPercent("50.00");
        HearingsToJudgmentsReportDetail reportDetail1 = new HearingsToJudgmentsReportDetail();
        reportDetail1.setCaseReference("caseRef1");
        reportDetail1.setHearingDate("2021-02-03");
        reportDetail1.setReportOffice("Office");
        reportDetail1.setReservedHearing("Yes");
        reportDetail1.setHearingJudge("Test1 Judge");
        reportDetail1.setJudgementDateSent("2021-04-03");
        reportDetail1.setTotalDays("25");
        HearingsToJudgmentsReportData reportData = new HearingsToJudgmentsReportData(reportSummary);
        reportData.addReportDetail(reportDetail1);

        var reportDetail2 = new HearingsToJudgmentsReportDetail();
        reportDetail2.setCaseReference("caseRef1");
        reportDetail2.setHearingDate("2021-02-03");
        reportDetail2.setReportOffice("Office");
        reportDetail2.setReservedHearing("No");
        reportDetail2.setHearingJudge("Test2 Judge");
        reportDetail2.setJudgementDateSent("2021-03-03");
        reportDetail2.setTotalDays("30");
        reportData.addReportDetail(reportDetail2);

        return reportData;
    }

    private StringBuilder getExpectedJsonString(HearingsToJudgmentsReportData reportData) {
        StringBuilder sb = new StringBuilder(14);
        var reportSummary = reportData.getReportSummary();
        sb.append(buildSummaryJsonString(
                reportSummary.getOffice(), reportSummary.getTotalCases(), reportSummary.getTotal4Wk(),
                reportSummary.getTotal4WkPercent(), reportSummary.getTotalX4Wk(), reportSummary.getTotalX4WkPercent()));

        sb.append('\"').append(REPORT_DETAILS).append("\":[\n");
        if (CollectionUtils.isNotEmpty(reportData.getReportDetails())
                && reportData.getReportDetails().get(0) != null) {
            var reportDetail1 = reportData.getReportDetails().get(0);
            sb.append(buildDetailJsonString(
                    reportDetail1.getCaseReference(), reportDetail1.getHearingDate(), reportDetail1.getReportOffice(),
                    reportDetail1.getReservedHearing(), reportDetail1.getHearingJudge(),
                    reportDetail1.getJudgementDateSent(), reportDetail1.getTotalDays()
            ));
        }
        if (CollectionUtils.isNotEmpty(reportData.getReportDetails())
                && reportData.getReportDetails().get(1) != null) {
            var reportDetail2 = reportData.getReportDetails().get(1);
            sb.append(",\n");
            sb.append(buildDetailJsonString(
                    reportDetail2.getCaseReference(), reportDetail2.getHearingDate(), reportDetail2.getReportOffice(),
                    reportDetail2.getReservedHearing(), reportDetail2.getHearingJudge(),
                    reportDetail2.getJudgementDateSent(), reportDetail2.getTotalDays()
            ));
            sb.append('\n');
        }
        sb.append("],\n");
        return sb;
    }

    private StringBuilder buildSummaryJsonString(String office, String totalCases, String total4Wk,
                                                 String total4WkPercent, String totalX4Wk, String totalX4WkPercent) {
        StringBuilder sb = new StringBuilder();
        sb.append(REPORT_OFFICE).append(office).append(NEW_LINE);
        sb.append(TOTAL_CASES).append(StringUtils.defaultString(totalCases, "0")).append(NEW_LINE);
        sb.append(TOTAL_WITHIN_4WEEKS).append(StringUtils.defaultString(total4Wk, "0")).append(NEW_LINE);
        sb.append(TOTAL_PERCENT_WITHIN_4WEEKS).append(StringUtils.defaultString(
                total4WkPercent, "0.00")).append(NEW_LINE);
        sb.append(TOTAL_NOT_WITHIN_4WEEKS).append(StringUtils.defaultString(
                totalX4Wk, "0")).append(NEW_LINE);
        sb.append(TOTAL_PERCENT_NOT_WITHIN_4WEEKS).append(StringUtils.defaultString(
                totalX4WkPercent, "0.00")).append(NEW_LINE);
        return sb;
    }

    private StringBuilder buildDetailJsonString(String caseReference, String hearingDate, String office,
                                                String reservedHearing, String hearingJudge, String judgementDateSent,
                                                String totalDays) {
        StringBuilder sb = new StringBuilder(160);
        sb.append('{');
        sb.append("\"reportOffice\":\"").append(office).append("\",");
        sb.append("\"caseReference\":\"").append(caseReference).append("\",");
        sb.append("\"hearingDate\":\"").append(hearingDate).append("\",");
        sb.append("\"judgementDateSent\":\"").append(judgementDateSent).append("\",");
        sb.append("\"totalDays\":\"").append(totalDays).append("\",");
        sb.append("\"reservedHearing\":\"").append(reservedHearing).append("\",");
        sb.append("\"hearingJudge\":\"").append(hearingJudge).append('\"');
        sb.append('}');
        return sb;
    }
}