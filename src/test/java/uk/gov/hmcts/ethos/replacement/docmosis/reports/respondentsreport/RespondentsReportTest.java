package uk.gov.hmcts.ethos.replacement.docmosis.reports.respondentsreport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.reports.respondentsreport.RespondentsReportSubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportParams;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OLD_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;

class RespondentsReportTest {

    ReportParams params;
    RespondentsReportDataSource reportDataSource;
    RespondentsReport respondentsReport;
    RespondentsReportCaseDataBuilder caseDataBuilder = new RespondentsReportCaseDataBuilder();
    List<RespondentsReportSubmitEvent> submitEvents = new ArrayList<>();
    static final LocalDateTime BASE_DATE = LocalDateTime.of(2022, 1, 1, 0, 0, 0);
    static final String DATE_FROM = BASE_DATE.minusDays(1).format(OLD_DATE_TIME_PATTERN);
    static final String DATE_TO = BASE_DATE.plusDays(15).format(OLD_DATE_TIME_PATTERN);
    static final String MANAGING_OFFICE = TribunalOffice.MANCHESTER.getOfficeName();

    @BeforeEach
    void setup() {
        submitEvents.clear();
        caseDataBuilder = new RespondentsReportCaseDataBuilder();
        reportDataSource = mock(RespondentsReportDataSource.class);
        when(reportDataSource.getData(ENGLANDWALES_CASE_TYPE_ID, MANAGING_OFFICE, DATE_FROM, DATE_TO))
                .thenReturn(submitEvents);
        params = new ReportParams(ENGLANDWALES_LISTING_CASE_TYPE_ID, MANAGING_OFFICE, DATE_FROM, DATE_TO);
        respondentsReport = new RespondentsReport(reportDataSource);
    }

    @Test
    void shouldNotShowCaseWithNoRespondent() {
        // Given a case has no respondent
        // and report data is requested
        // the case should not be in the report data

        caseDataBuilder.withNoRespondents();
        submitEvents.add(caseDataBuilder
                .buildAsSubmitEvent());

        var reportData = respondentsReport.generateReport(params);
        assertCommonValues(reportData);
        assertEquals("0", reportData.getReportSummary().getTotalCasesWithMoreThanOneRespondent());
        assertEquals(0, reportData.getReportDetails().size());
    }

    @Test
    void shouldNotShowCaseWithOneRespondent() {
        // Given a case has 1 respondent
        // and report data is requested
        // the case should not be in the report data

        caseDataBuilder.withOneRespondent();
        submitEvents.add(caseDataBuilder
                .buildAsSubmitEvent());

        var reportData = respondentsReport.generateReport(params);
        assertCommonValues(reportData);
        assertEquals("0", reportData.getReportSummary().getTotalCasesWithMoreThanOneRespondent());
        assertTrue(reportData.getReportDetails().isEmpty());
    }

    @Test
    void shouldShowCaseWithMoreThanOneRespondent() {
        // Given a case has more than 1 respondents
        // and report data is requested
        // the cases should be in the report data

        caseDataBuilder.withMoreThanOneRespondents();
        submitEvents.add(caseDataBuilder.buildAsSubmitEvent());

        var reportData = respondentsReport.generateReport(params);
        assertCommonValues(reportData);
        assertEquals("1", reportData.getReportSummary().getTotalCasesWithMoreThanOneRespondent());
        assertFalse(reportData.getReportDetails().isEmpty());
    }

    @Test
    void shouldShowCaseDetailsWithMoreThanOneRespondentRepresented() {
        // Given a case has more than 1 respondents and represented
        // and report data is requested
        // the cases should be in the report data details

        caseDataBuilder.withMoreThan1RespondentsRepresented();
        submitEvents.add(caseDataBuilder
                .buildAsSubmitEvent());
        var reportData = respondentsReport.generateReport(params);
        assertCommonValues(reportData);
        assertEquals("111", reportData.getReportDetails().get(0).getCaseNumber());
        assertEquals("Resp1", reportData.getReportDetails().get(0).getRespondentName());
        assertEquals("Rep1", reportData.getReportDetails().get(0).getRepresentativeName());
        assertEquals("Y", reportData.getReportDetails()
                .get(0).getRepresentativeHasMoreThanOneRespondent());
    }

    @Test
    void checkScotlandOfficeName() {
        when(reportDataSource.getData(SCOTLAND_CASE_TYPE_ID, null, DATE_FROM, DATE_TO))
                .thenReturn(submitEvents);
        params = new ReportParams(SCOTLAND_LISTING_CASE_TYPE_ID, null, DATE_FROM, DATE_TO);

        caseDataBuilder.withMoreThanOneRespondents();
        submitEvents.add(caseDataBuilder.buildAsSubmitEvent());

        var reportData = respondentsReport.generateReport(params);
        assertEquals(TribunalOffice.SCOTLAND.getOfficeName(), reportData.getReportSummary().getOffice());
    }

    private void assertCommonValues(RespondentsReportData reportData) {
        assertNotNull(reportData);
        assertEquals(MANAGING_OFFICE, reportData.getReportSummary().getOffice());
    }
}
