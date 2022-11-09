package uk.gov.hmcts.ethos.replacement.docmosis.reports.respondentsreport;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.reports.respondentsreport.RespondentsReportSubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportException;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RespondentsReportCcdReportDataSourceTest {

    @Test
    void shouldReturnSearchResults() throws IOException {
        var authToken = "token";
        var caseTypeId = "caseTypeId";
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        var fromDate = "1-1-2022";
        var toDate = "10-1-2022";
        var ccdClient = mock(CcdClient.class);
        var submitEvent = new RespondentsReportSubmitEvent();
        var submitEvents = List.of(submitEvent);
        when(ccdClient.respondentsReportSearch(anyString(), anyString(), anyString())).thenReturn(submitEvents);

        var ccdReportDataSource = new RespondentsReportCcdDataSource(authToken, ccdClient);

        var results = ccdReportDataSource.getData(caseTypeId, managingOffice, fromDate, toDate);
        assertEquals(1, results.size());
        assertEquals(submitEvent, results.get(0));
    }

    @Test
    void shouldThrowReportExceptionWhenSearchFails() throws IOException {
        var authToken = "token";
        var caseTypeId = "caseTypeId";
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        var fromDate = "1-1-2022";
        var toDate = "10-1-2022";
        var ccdClient = mock(CcdClient.class);
        when(ccdClient.respondentsReportSearch(anyString(), anyString(), anyString())).thenThrow(new IOException());

        var ccdReportDataSource = new RespondentsReportCcdDataSource(authToken, ccdClient);
        assertThrows(ReportException.class, () -> ccdReportDataSource.getData(caseTypeId, managingOffice, fromDate, toDate));
    }

}
