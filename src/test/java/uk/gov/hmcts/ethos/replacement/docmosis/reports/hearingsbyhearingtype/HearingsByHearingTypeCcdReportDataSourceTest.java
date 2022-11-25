package uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingsbyhearingtype;

import org.junit.Test;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.reports.hearingsbyhearingtype.HearingsByHearingTypeSubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportException;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportParams;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.LawOfDemeter")
public class HearingsByHearingTypeCcdReportDataSourceTest {

    @Test
    public void shouldReturnSearchResults() throws IOException {
        String authToken = "token";
        String caseTypeId = "caseTypeId_Listings";
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String fromDate = "1-1-2022";
        String toDate = "10-1-2022";
        CcdClient ccdClient = mock(CcdClient.class);
        HearingsByHearingTypeSubmitEvent submitEvent = new HearingsByHearingTypeSubmitEvent();
        List<HearingsByHearingTypeSubmitEvent> submitEvents = List.of(submitEvent);
        when(ccdClient.hearingsByHearingTypeSearch(anyString(), anyString(), anyString())).thenReturn(submitEvents);

        HearingsByHearingTypeCcdReportDataSource ccdReportDataSource = new HearingsByHearingTypeCcdReportDataSource(
            authToken, ccdClient);

        List<HearingsByHearingTypeSubmitEvent> results = ccdReportDataSource.getData(new ReportParams(caseTypeId,
            managingOffice, fromDate, toDate));
        assertEquals(1, results.size());
        assertEquals(submitEvent, results.get(0));
    }

    @Test(expected = ReportException.class)
    public void shouldThrowReportExceptionWhenSearchFails() throws IOException {
        String authToken = "token";
        String caseTypeId = "caseTypeId_Listings";
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String fromDate = "1-1-2022";
        String toDate = "10-1-2022";
        CcdClient ccdClient = mock(CcdClient.class);
        when(ccdClient.hearingsByHearingTypeSearch(anyString(), anyString(), anyString())).thenThrow(new IOException());

        HearingsByHearingTypeCcdReportDataSource ccdReportDataSource = new HearingsByHearingTypeCcdReportDataSource(
            authToken, ccdClient);
        ccdReportDataSource.getData(new ReportParams(caseTypeId, managingOffice, fromDate, toDate));
        fail("Should throw exception instead");
    }

}
