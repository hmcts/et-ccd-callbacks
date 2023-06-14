package uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.reports.casesawaitingjudgment.CasesAwaitingJudgmentSubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportException;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CcdReportDataSourceTest {

    @Test
    public void shouldReturnSearchResults() throws IOException {
        String authToken = "A test token";
        String caseTypeId = "A test case type";
        String owningOffice = TribunalOffice.LEEDS.getOfficeName();
        CcdClient ccdClient = mock(CcdClient.class);
        CasesAwaitingJudgmentSubmitEvent submitEvent = new CasesAwaitingJudgmentSubmitEvent();
        List<CasesAwaitingJudgmentSubmitEvent> submitEvents = List.of(submitEvent);
        when(ccdClient.casesAwaitingJudgmentSearch(anyString(), anyString(), anyString())).thenReturn(submitEvents);

        CcdReportDataSource ccdReportDataSource = new CcdReportDataSource(authToken, ccdClient);

        List<CasesAwaitingJudgmentSubmitEvent> results = ccdReportDataSource.getData(caseTypeId, owningOffice);
        assertEquals(1, results.size());
        assertEquals(submitEvent, results.get(0));
    }

    @Test
    public void shouldThrowReportExceptionWhenSearchFails() throws IOException {
        String authToken = "A test token";
        String caseTypeId = "A test case type";
        String owningOffice = TribunalOffice.LEEDS.getOfficeName();
        CcdClient ccdClient = mock(CcdClient.class);
        when(ccdClient.casesAwaitingJudgmentSearch(anyString(), anyString(), anyString())).thenThrow(new IOException());

        CcdReportDataSource ccdReportDataSource = new CcdReportDataSource(authToken, ccdClient);
        assertThrows(ReportException.class, () -> ccdReportDataSource.getData(caseTypeId, owningOffice));
    }
}
