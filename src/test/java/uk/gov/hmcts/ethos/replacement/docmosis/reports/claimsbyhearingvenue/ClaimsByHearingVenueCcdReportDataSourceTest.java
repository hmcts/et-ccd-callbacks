package uk.gov.hmcts.ethos.replacement.docmosis.reports.claimsbyhearingvenue;

import org.junit.Test;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.reports.claimsbyhearingvenue.ClaimsByHearingVenueSubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportException;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClaimsByHearingVenueCcdReportDataSourceTest {
    @Test
    public void shouldReturnSearchResults() throws IOException {
        String authToken = "test token";
        String caseTypeId = "test caseTypeId";
        String fromDate = "2021-12-13";
        String toDate = "2021-12-27";
        CcdClient ccdClient = mock(CcdClient.class);
        ClaimsByHearingVenueSubmitEvent submitEventOne = new ClaimsByHearingVenueSubmitEvent();
        ClaimsByHearingVenueSubmitEvent submitEventTwo = new ClaimsByHearingVenueSubmitEvent();

        List<ClaimsByHearingVenueSubmitEvent> submitEvents = List.of(submitEventOne, submitEventTwo);
        when(ccdClient.claimsByHearingVenueSearch(anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ClaimsByHearingVenueCcdReportDataSource ccdReportDataSource = new ClaimsByHearingVenueCcdReportDataSource(
            authToken, ccdClient);
        List<ClaimsByHearingVenueSubmitEvent> results = ccdReportDataSource.getData(caseTypeId, fromDate, toDate);
        assertEquals(2, results.size());
        assertEquals(submitEventOne, results.get(0));
        assertEquals(submitEventTwo, results.get(1));
    }

    @Test
    public void shouldThrowReportExceptionWhenSearchFails() throws IOException {
        String authToken = "test token";
        String caseTypeId = "Test_caseTypeId";
        String fromDate = "2021-12-13";
        String toDate = "2021-12-27";
        CcdClient ccdClient = mock(CcdClient.class);
        when(ccdClient.claimsByHearingVenueSearch(anyString(), anyString(), anyString()))
            .thenThrow(new IOException());
        ClaimsByHearingVenueCcdReportDataSource ccdReportDataSource = new ClaimsByHearingVenueCcdReportDataSource(
            authToken, ccdClient);
        ReportException exception = assertThrows(ReportException.class, () -> {
            ccdReportDataSource.getData(caseTypeId, fromDate, toDate);
        });

        String expectedMessage = "Failed to get claims by hearing venue search results "
            + "for case type id Test_caseTypeId";
        String actualMessage = exception.getMessage();
        assertEquals(expectedMessage, actualMessage);
    }
}
