package uk.gov.hmcts.ethos.replacement.docmosis.reports.eccreport;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.reports.eccreport.EccReportSubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportParams;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;

class EccReportTest {

    static final LocalDateTime BASE_DATE = LocalDateTime.of(2022, 1, 1, 0, 0, 0);
    static final String DATE_FROM = BASE_DATE.minusDays(1).format(OLD_DATE_TIME_PATTERN);
    static final String DATE_TO = BASE_DATE.plusDays(15).format(OLD_DATE_TIME_PATTERN);

    @Test
    void shouldShowEnglandWalesOffice() {
        TribunalOffice managingOffice = TribunalOffice.MANCHESTER;
        EccReportSubmitEvent submitEvent = EccReportCaseDataBuilder.builder()
                .withNoEcc()
                .buildAsSubmitEvent();
        List<EccReportSubmitEvent> submitEvents = List.of(submitEvent);
        EccReportDataSource reportDataSource = mockDataSource(ENGLANDWALES_CASE_TYPE_ID, managingOffice.getOfficeName(),
            submitEvents);

        EccReport eccReport = new EccReport(reportDataSource);
        ReportParams reportParams = new ReportParams(ENGLANDWALES_LISTING_CASE_TYPE_ID, managingOffice.getOfficeName(),
                DATE_FROM, DATE_TO);
        EccReportData reportData = eccReport.generateReport(reportParams);

        assertEquals(managingOffice.getOfficeName(), reportData.getOffice());
    }

    @Test
    void shouldShowScotlandOffice() {
        EccReportSubmitEvent submitEvent = EccReportCaseDataBuilder.builder()
                .withNoEcc()
                .buildAsSubmitEvent();
        List<EccReportSubmitEvent> submitEvents = List.of(submitEvent);
        EccReportDataSource reportDataSource = mockDataSource(SCOTLAND_CASE_TYPE_ID, null, submitEvents);

        EccReport eccReport = new EccReport(reportDataSource);
        ReportParams reportParams = new ReportParams(SCOTLAND_LISTING_CASE_TYPE_ID, null,
            DATE_FROM, DATE_TO);
        EccReportData reportData = eccReport.generateReport(reportParams);

        assertEquals(TribunalOffice.SCOTLAND.getOfficeName(), reportData.getOffice());
        assertTrue(reportData.getReportDetails().isEmpty());
    }

    @Test
    void shouldNotShowCaseWithNoECC() {
        // Given a case has no Ecc cases
        // and report data is requested
        // the case should not be in the report data
        EccReportSubmitEvent submitEvent = EccReportCaseDataBuilder.builder()
                .withNoEcc()
                .buildAsSubmitEvent();
        List<EccReportSubmitEvent> submitEvents = List.of(submitEvent);
        TribunalOffice managingOffice = TribunalOffice.MANCHESTER;
        EccReportDataSource reportDataSource = mockDataSource(ENGLANDWALES_CASE_TYPE_ID, managingOffice.getOfficeName(),
            submitEvents);

        EccReport eccReport = new EccReport(reportDataSource);
        ReportParams reportParams = new ReportParams(ENGLANDWALES_LISTING_CASE_TYPE_ID, managingOffice.getOfficeName(),
                DATE_FROM, DATE_TO);
        EccReportData reportData = eccReport.generateReport(reportParams);

        assertEquals(managingOffice.getOfficeName(), reportData.getOffice());
        assertTrue(reportData.getReportDetails().isEmpty());
    }

    @Test
    void shouldShowCaseWithRespondentsAndEcc() {
        // Given a case has respondents and ecc
        // and report data is requested
        // the cases should be in the report data
        EccReportSubmitEvent submitEvent = EccReportCaseDataBuilder.builder()
                .withRespondents()
                .withEccs()
                .buildAsSubmitEvent();
        List<EccReportSubmitEvent> submitEvents = List.of(submitEvent);
        TribunalOffice managingOffice = TribunalOffice.MANCHESTER;
        EccReportDataSource reportDataSource = mockDataSource(ENGLANDWALES_CASE_TYPE_ID, managingOffice.getOfficeName(),
                submitEvents);

        EccReport eccReport = new EccReport(reportDataSource);
        ReportParams reportParams = new ReportParams(ENGLANDWALES_LISTING_CASE_TYPE_ID, managingOffice.getOfficeName(),
                DATE_FROM, DATE_TO);
        EccReportData reportData = eccReport.generateReport(reportParams);

        assertEquals(managingOffice.getOfficeName(), reportData.getOffice());
        assertEquals("2", reportData.getReportDetails().get(0).getRespondentsCount());
        assertEquals("Accepted", reportData.getReportDetails().get(0).getState());
        assertEquals("2", reportData.getReportDetails().get(0).getEccCasesCount());
        assertEquals("ecc1\necc2", reportData.getReportDetails().get(0).getEccCaseList());
    }

    private EccReportDataSource mockDataSource(String caseTypeId, String managingOffice,
                                               List<EccReportSubmitEvent> submitEvents) {
        EccReportDataSource reportDataSource = mock(EccReportDataSource.class);

        ReportParams reportParams = new ReportParams(caseTypeId, managingOffice, DATE_FROM, DATE_TO);
        when(reportDataSource.getData(reportParams)).thenReturn(submitEvents);

        return reportDataSource;
    }
}
