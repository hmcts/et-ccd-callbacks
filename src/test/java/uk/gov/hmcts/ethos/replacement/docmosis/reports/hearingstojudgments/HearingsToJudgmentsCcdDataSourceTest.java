package uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.reports.hearingstojudgments.HearingsToJudgmentsSubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportException;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HearingsToJudgmentsCcdDataSourceTest {

    @Test
    public void shouldReturnSearchResults() throws IOException {
        String authToken = "A test token";
        String caseTypeId = "A test case type";
        String fromDate = "10-10-2021";
        String toDate = "10-11-2021";
        CcdClient ccdClient = mock(CcdClient.class);
        HearingsToJudgmentsSubmitEvent submitEvent = new HearingsToJudgmentsSubmitEvent();
        List<HearingsToJudgmentsSubmitEvent> submitEvents = List.of(submitEvent);
        when(ccdClient.hearingsToJudgementsSearch(anyString(), anyString(), anyString())).thenReturn(submitEvents);

        HearingsToJudgmentsCcdReportDataSource ccdReportDataSource = new HearingsToJudgmentsCcdReportDataSource(
            authToken, ccdClient);

        List<HearingsToJudgmentsSubmitEvent> results = ccdReportDataSource.getData(caseTypeId,
            TribunalOffice.LEEDS.getOfficeName(), fromDate, toDate);
        assertEquals(1, results.size());
        assertEquals(submitEvent, results.get(0));
    }

    @Test
    public void shouldThrowReportExceptionWhenSearchFails() throws IOException {
        String authToken = "A test token";
        String caseTypeId = "A test case type";
        String fromDate = "10-10-2021";
        String toDate = "10-11-2021";
        CcdClient ccdClient = mock(CcdClient.class);
        when(ccdClient.hearingsToJudgementsSearch(anyString(), anyString(), anyString())).thenThrow(new IOException());

        HearingsToJudgmentsCcdReportDataSource ccdReportDataSource = new HearingsToJudgmentsCcdReportDataSource(
            authToken, ccdClient);

        assertThrows(ReportException.class, () ->
                ccdReportDataSource.getData(caseTypeId, TribunalOffice.LEEDS.getOfficeName(), fromDate, toDate)
        );
    }
}
