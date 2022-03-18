package uk.gov.hmcts.ethos.replacement.docmosis.reports.eccreport;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.reports.eccreport.EccReportSubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportParams;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;

public class EccReportTest {

    EccReportDataSource reportDataSource;
    EccReport eccReport;
    ReportParams reportParams;
    EccReportCaseDataBuilder caseDataBuilder = new EccReportCaseDataBuilder();
    List<EccReportSubmitEvent> submitEvents = new ArrayList<>();
    static final LocalDateTime BASE_DATE = LocalDateTime.of(2022, 1, 1, 0, 0,0);
    static final String DATE_FROM = BASE_DATE.minusDays(1).format(OLD_DATE_TIME_PATTERN);
    static final String DATE_TO = BASE_DATE.plusDays(15).format(OLD_DATE_TIME_PATTERN);
    static final String MANAGING_OFFICE = TribunalOffice.MANCHESTER.getOfficeName();

    @Before
    public void setup() {
        submitEvents.clear();
        caseDataBuilder = new EccReportCaseDataBuilder();
        reportDataSource = mock(EccReportDataSource.class);
        reportParams = new ReportParams(ENGLANDWALES_LISTING_CASE_TYPE_ID, MANAGING_OFFICE, DATE_FROM, DATE_TO);
        when(reportDataSource.getData(new ReportParams(ENGLANDWALES_CASE_TYPE_ID, MANAGING_OFFICE, DATE_FROM, DATE_TO)))
                .thenReturn(submitEvents);
        eccReport = new EccReport(reportDataSource);
    }

    @Test
    public void shouldNotShowCaseWithNoECC() {
        // Given a case has no Ecc cases
        // and report data is requested
        // the case should not be in the report data

        caseDataBuilder.withNoEcc();
        submitEvents.add(caseDataBuilder
                .buildAsSubmitEvent());
        var reportData = eccReport.generateReport(reportParams);
        assertTrue(reportData.getReportDetails().isEmpty());
    }

    @Test
    public void shouldShowCaseWithRespondentsAndEcc() {
        // Given a case has respondents and ecc
        // and report data is requested
        // the cases should be in the report data

        caseDataBuilder.withRespondents();
        caseDataBuilder.withEccs();
        submitEvents.add(caseDataBuilder.buildAsSubmitEvent());

        var reportData = eccReport.generateReport(reportParams);
        assertCommonValues(reportData);
        assertEquals("2", reportData.getReportDetails().get(0).getRespondentsCount());
        assertEquals("Accepted", reportData.getReportDetails().get(0).getState());
        assertEquals("2", reportData.getReportDetails().get(0).getEccCasesCount());
        assertEquals("ecc1\necc2", reportData.getReportDetails().get(0).getEccCaseList());
    }

    private void assertCommonValues(EccReportData reportData) {
        assertNotNull(reportData);
        assertEquals(MANAGING_OFFICE, reportData.getOffice());
    }
}
