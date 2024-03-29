package uk.gov.hmcts.ethos.replacement.docmosis.reports.eccreport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.reports.eccreport.EccReportSubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportException;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportParams;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class EccReportCcdReportDataSourceTest {

    @Test
    void shouldReturnSearchResultsForManagingOffice() throws IOException {
        String authToken = "token";
        String caseTypeId = ENGLANDWALES_CASE_TYPE_ID;
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String fromDate = "1-1-2022";
        String toDate = "10-1-2022";
        CcdClient ccdClient = mock(CcdClient.class);
        EccReportSubmitEvent submitEvent = new EccReportSubmitEvent();
        List<EccReportSubmitEvent> submitEvents = List.of(submitEvent);
        when(ccdClient.eccReportSearch(eq(authToken), eq(caseTypeId), anyString())).thenReturn(submitEvents);

        EccReportCcdDataSource ccdReportDataSource = new EccReportCcdDataSource(authToken, ccdClient);

        List<EccReportSubmitEvent> results = ccdReportDataSource.getData(new ReportParams(caseTypeId, managingOffice,
            fromDate, toDate));
        assertEquals(1, results.size());
        assertEquals(submitEvent, results.get(0));
    }

    @Test
    void shouldReturnSearchResultsForNullManagingOffice() throws IOException {
        String authToken = "token";
        String caseTypeId = SCOTLAND_CASE_TYPE_ID;
        String fromDate = "1-1-2022";
        String toDate = "10-1-2022";
        CcdClient ccdClient = mock(CcdClient.class);
        EccReportSubmitEvent submitEvent = new EccReportSubmitEvent();
        List<EccReportSubmitEvent> submitEvents = List.of(submitEvent);
        when(ccdClient.eccReportSearch(eq(authToken), eq(caseTypeId), anyString())).thenReturn(submitEvents);

        EccReportCcdDataSource ccdReportDataSource = new EccReportCcdDataSource(authToken, ccdClient);

        List<EccReportSubmitEvent> results = ccdReportDataSource.getData(new ReportParams(caseTypeId, null,
            fromDate, toDate));
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
        when(ccdClient.eccReportSearch(anyString(), anyString(), anyString())).thenThrow(new IOException());

        EccReportCcdDataSource ccdReportDataSource = new EccReportCcdDataSource(authToken, ccdClient);
        assertThrows(ReportException.class, () ->
                ccdReportDataSource.getData(new ReportParams(caseTypeId, managingOffice, fromDate, toDate))
        );
    }
}
