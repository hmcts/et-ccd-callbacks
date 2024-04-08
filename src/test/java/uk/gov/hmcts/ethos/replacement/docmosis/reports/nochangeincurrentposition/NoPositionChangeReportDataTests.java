package uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_LINE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.REPORT_OFFICE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.TOTAL_CASES;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition.NoPositionChangeReportData.REPORT_DATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition.NoPositionChangeReportData.REPORT_DETAILS_MULTIPLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition.NoPositionChangeReportData.REPORT_DETAILS_SINGLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition.NoPositionChangeReportData.TOTAL_MULTIPLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition.NoPositionChangeReportData.TOTAL_SINGLE;

class NoPositionChangeReportDataTests {

    @Test
    void shouldReturnValidJson() throws JsonProcessingException {
        NoPositionChangeReportData reportData = setupValidReportData();
        StringBuilder resultJsonString = reportData.toReportObjectString();

        StringBuilder expectedJsonString = getExpectedJsonString(reportData);
        assertEquals(expectedJsonString.toString(), resultJsonString.toString());
    }

    @Test
    void shouldReturnValidJsonWithEmptyValues() throws JsonProcessingException {
        NoPositionChangeReportSummary reportSummary = new NoPositionChangeReportSummary("Office");
        NoPositionChangeReportData reportData = new NoPositionChangeReportData(reportSummary, "2021-07-07");
        StringBuilder resultJsonString = reportData.toReportObjectString();

        StringBuilder expectedJsonString = getExpectedJsonString(reportData);
        assertEquals(expectedJsonString.toString(), resultJsonString.toString());
    }

    private NoPositionChangeReportData setupValidReportData() {
        NoPositionChangeReportSummary reportSummary = new NoPositionChangeReportSummary("Office");
        reportSummary.setTotalCases("0");
        reportSummary.setTotalSingleCases("2");
        reportSummary.setTotalMultipleCases("1");
        NoPositionChangeReportDetailSingle reportDetailSingle1 = new NoPositionChangeReportDetailSingle();
        reportDetailSingle1.setCaseReference("caseRef1");
        reportDetailSingle1.setDateToPosition("2021-02-03");
        reportDetailSingle1.setCurrentPosition("Test position1");
        reportDetailSingle1.setYear("2021");
        reportDetailSingle1.setRespondent("R1");
        NoPositionChangeReportData reportData = new NoPositionChangeReportData(reportSummary, "2021-06-07");
        reportData.addReportDetailsSingle(reportDetailSingle1);
        NoPositionChangeReportDetailSingle reportDetailSingle2 = new NoPositionChangeReportDetailSingle();
        reportDetailSingle2.setCaseReference("caseRef2");
        reportDetailSingle2.setDateToPosition("2021-02-04");
        reportDetailSingle2.setCurrentPosition("Test position2");
        reportDetailSingle2.setYear("2022");
        reportDetailSingle2.setRespondent("R2 & Others");
        reportData.addReportDetailsSingle(reportDetailSingle2);
        NoPositionChangeReportDetailMultiple reportDetailMultiple = new NoPositionChangeReportDetailMultiple();
        reportDetailMultiple.setCaseReference("caseRef3");
        reportDetailMultiple.setDateToPosition("2021-02-05");
        reportDetailMultiple.setCurrentPosition("Test position3");
        reportDetailMultiple.setYear("2020");
        reportDetailMultiple.setMultipleName("Multi");
        reportData.addReportDetailsMultiple(reportDetailMultiple);
        return reportData;
    }

    private StringBuilder getExpectedJsonString(NoPositionChangeReportData reportData) {
        StringBuilder sb = new StringBuilder(180);
        NoPositionChangeReportSummary reportSummary = reportData.getReportSummary();
        sb.append(buildSummaryJsonString(
                reportSummary.getOffice(), reportData.getReportDate(), reportSummary.getTotalCases(),
                reportSummary.getTotalSingleCases(), reportSummary.getTotalMultipleCases()))
                .append('\"').append(REPORT_DETAILS_SINGLE).append("\":[\n");
        if (CollectionUtils.isNotEmpty(reportData.getReportDetailsSingle())
                && reportData.getReportDetailsSingle().get(0) != null) {
            NoPositionChangeReportDetailSingle rdSingle1 = reportData.getReportDetailsSingle().get(0);
            sb.append(buildDetailSingleJsonString(
                    rdSingle1.getCaseReference(), rdSingle1.getYear(), rdSingle1.getCurrentPosition(),
                    rdSingle1.getDateToPosition(), rdSingle1.getRespondent()));
        }
        if (CollectionUtils.isNotEmpty(reportData.getReportDetailsSingle())
                && reportData.getReportDetailsSingle().get(1) != null) {
            NoPositionChangeReportDetailSingle rdSingle2 = reportData.getReportDetailsSingle().get(1);
            sb.append(",\n");
            sb.append(buildDetailSingleJsonString(
                    rdSingle2.getCaseReference(), rdSingle2.getYear(), rdSingle2.getCurrentPosition(),
                    rdSingle2.getDateToPosition(), rdSingle2.getRespondent()
            ));
            sb.append('\n');
        }
        sb.append("],\n").append('\"').append(REPORT_DETAILS_MULTIPLE).append("\":[\n");
        if (CollectionUtils.isNotEmpty(reportData.getReportDetailsSingle())) {
            NoPositionChangeReportDetailMultiple rdMultiple = reportData.getReportDetailsMultiple().get(0);
            sb.append(buildDetailMultipleJsonString(
                    rdMultiple.getCaseReference(), rdMultiple.getYear(), rdMultiple.getCurrentPosition(),
                    rdMultiple.getDateToPosition(), rdMultiple.getMultipleName()
            ));
            sb.append('\n');
        }
        sb.append("],\n");
        return sb;
    }

    private StringBuilder buildSummaryJsonString(String office, String reportDate, String totalCases,
                                                  String totalSingle, String totalMultiple) {
        StringBuilder sb = new StringBuilder();
        sb.append(REPORT_OFFICE).append(StringUtils.defaultIfEmpty(office, "")).append(NEW_LINE)
                .append(REPORT_DATE).append(UtilHelper.listingFormatLocalDate(reportDate)).append(NEW_LINE)
                .append(TOTAL_CASES).append(StringUtils.defaultIfEmpty(totalCases, "0")).append(NEW_LINE)
                .append(TOTAL_SINGLE).append(StringUtils.defaultIfEmpty(totalSingle, "0")).append(NEW_LINE)
                .append(TOTAL_MULTIPLE).append(StringUtils.defaultIfEmpty(totalMultiple, "0"))
                .append(NEW_LINE);
        return sb;
    }

    private StringBuilder buildDetailSingleJsonString(String caseReference, String year, String currentPosition,
                                                      String dateToPosition, String respondent) {
        StringBuilder sb = new StringBuilder(120);
        sb.append('{').append("\"caseReference\":\"").append(caseReference).append("\",")
                .append("\"year\":\"").append(year).append("\",").append("\"currentPosition\":\"")
                .append(currentPosition).append("\",").append("\"dateToPosition\":\"").append(dateToPosition)
                .append("\",").append("\"respondent\":\"").append(respondent).append('\"').append('}');
        return sb;
    }

    private StringBuilder buildDetailMultipleJsonString(String caseReference, String year, String currentPosition,
                                                        String dateToPosition, String multipleName) {
        StringBuilder sb = new StringBuilder(120);
        sb.append('{').append("\"caseReference\":\"").append(caseReference).append("\",").append("\"year\":\"")
                .append(year).append("\",").append("\"currentPosition\":\"").append(currentPosition).append("\",")
                .append("\"dateToPosition\":\"").append(dateToPosition).append("\",").append("\"multipleName\":\"")
                .append(multipleName).append('\"').append('}');
        return sb;
    }
}
