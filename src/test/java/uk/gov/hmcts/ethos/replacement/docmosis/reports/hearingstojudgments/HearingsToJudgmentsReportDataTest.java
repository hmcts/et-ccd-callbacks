package uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_LINE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.REPORT_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.REPORT_OFFICE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.TOTAL_CASES;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsReportData.TOTAL_NOT_WITHIN_4WEEKS;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsReportData.TOTAL_PERCENT_NOT_WITHIN_4WEEKS;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsReportData.TOTAL_PERCENT_WITHIN_4WEEKS;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsReportData.TOTAL_WITHIN_4WEEKS;

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

        HearingsToJudgmentsReportDetail reportDetail2 = new HearingsToJudgmentsReportDetail();
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
        StringBuilder sb = new StringBuilder(24);
        HearingsToJudgmentsReportSummary reportSummary = reportData.getReportSummary();

        sb.append(buildSummaryJsonString(
                        reportSummary.getOffice(), reportSummary.getTotalCases(), reportSummary.getTotal4Wk(),
                        reportSummary.getTotal4WkPercent(), reportSummary.getTotalX4Wk(),
                        reportSummary.getTotalX4WkPercent()))
                .append('\"').append(REPORT_DETAILS).append("\":[\n");

        List<HearingsToJudgmentsReportDetail> reportDetails = reportData.getReportDetails();
        if (CollectionUtils.isEmpty(reportDetails)) {
            sb.append("],\n");
            return sb;
        }

        for (int i = 0; i < reportDetails.size(); i++) {
            HearingsToJudgmentsReportDetail reportDetail = reportDetails.get(i);
            if (reportDetail == null) {
                continue;
            }
            if (i > 0) {
                sb.append(",\n");
            }
            sb.append(buildDetailJsonString(
                    reportDetail.getCaseReference(), reportDetail.getHearingDate(), reportDetail.getReportOffice(),
                    reportDetail.getReservedHearing(), reportDetail.getHearingJudge(),
                    reportDetail.getJudgementDateSent(), reportDetail.getTotalDays()
            ));
        }
        sb.append("\n],\n");
        return sb;
    }

    private StringBuilder buildSummaryJsonString(String office, String totalCases, String total4Wk,
                                                 String total4WkPercent, String totalX4Wk, String totalX4WkPercent) {
        return new StringBuilder()
                .append(REPORT_OFFICE).append(office).append(NEW_LINE)
                .append(TOTAL_CASES).append(StringUtils.defaultIfEmpty(totalCases, "0")).append(NEW_LINE)
                .append(TOTAL_WITHIN_4WEEKS).append(StringUtils.defaultIfEmpty(total4Wk, "0")).append(NEW_LINE)
                .append(TOTAL_PERCENT_WITHIN_4WEEKS)
                .append(StringUtils.defaultIfEmpty(total4WkPercent, "0.00")).append(NEW_LINE)
                .append(TOTAL_NOT_WITHIN_4WEEKS)
                .append(StringUtils.defaultIfEmpty(totalX4Wk, "0")).append(NEW_LINE)
                .append(TOTAL_PERCENT_NOT_WITHIN_4WEEKS)
                .append(StringUtils.defaultIfEmpty(totalX4WkPercent, "0.00")).append(NEW_LINE);
    }

    private StringBuilder buildDetailJsonString(String caseReference, String hearingDate, String office,
                                                String reservedHearing, String hearingJudge, String judgementDateSent,
                                                String totalDays) {
        return new StringBuilder(160)
                .append("{\"reportOffice\":\"").append(office)
                .append("\",\"caseReference\":\"").append(caseReference)
                .append("\",\"hearingDate\":\"").append(hearingDate)
                .append("\",\"judgementDateSent\":\"").append(judgementDateSent)
                .append("\",\"totalDays\":\"").append(totalDays)
                .append("\",\"reservedHearing\":\"").append(reservedHearing)
                .append("\",\"hearingJudge\":\"").append(hearingJudge)
                .append("\"}");
    }
}
