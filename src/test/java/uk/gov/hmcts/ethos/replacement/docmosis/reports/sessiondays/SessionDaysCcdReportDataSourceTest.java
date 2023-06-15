package uk.gov.hmcts.ethos.replacement.docmosis.reports.sessiondays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.reports.sessiondays.SessionDaysSubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportException;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class SessionDaysCcdReportDataSourceTest {

    @Test
    void shouldReturnSearchResults() throws IOException {
        String authToken = "token";
        String caseTypeId = "caseTypeId";
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String fromDate = "1-1-2022";
        String toDate = "10-1-2022";
        CcdClient ccdClient = mock(CcdClient.class);
        SessionDaysSubmitEvent submitEvent = new SessionDaysSubmitEvent();
        List<SessionDaysSubmitEvent> submitEvents = List.of(submitEvent);
        when(ccdClient.sessionDaysSearch(anyString(), anyString(), anyString())).thenReturn(submitEvents);

        SessionDaysCcdReportDataSource ccdReportDataSource = new SessionDaysCcdReportDataSource(authToken, ccdClient);

        List<SessionDaysSubmitEvent> results = ccdReportDataSource.getData(caseTypeId, managingOffice,
                fromDate, toDate);
        assertEquals(1, results.size());
        assertEquals(submitEvent, results.get(0));
    }

    @Test
    void shouldThrowReportExceptionWhenSearchFails() throws IOException {
        String authToken = "token";
        String caseTypeId = "caseTypeId";
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String fromDate = "1-1-2022";
        String toDate = "10-1-2022";
        CcdClient ccdClient = mock(CcdClient.class);
        when(ccdClient.sessionDaysSearch(anyString(), anyString(), anyString())).thenThrow(new IOException());

        SessionDaysCcdReportDataSource ccdReportDataSource = new SessionDaysCcdReportDataSource(authToken, ccdClient);
        assertThrows(ReportException.class, () ->
                ccdReportDataSource.getData(caseTypeId, managingOffice, fromDate, toDate)
        );
    }

}
