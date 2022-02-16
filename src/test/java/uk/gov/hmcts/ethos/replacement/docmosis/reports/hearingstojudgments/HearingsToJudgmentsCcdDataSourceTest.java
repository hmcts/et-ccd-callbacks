package uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments;

import org.junit.Test;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportException;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HearingsToJudgmentsCcdDataSourceTest {

    @Test
    public void shouldReturnSearchResults() throws IOException {
        var authToken = "A test token";
        var caseTypeId = "A test case type";
        var fromDate = "10-10-2021";
        var toDate = "10-11-2021";
        var ccdClient = mock(CcdClient.class);
        var submitEvent = new HearingsToJudgmentsSubmitEvent();
        var result = new HearingsToJudgmentsSearchResult();
        result.setCases(List.of(submitEvent));
        when(ccdClient.runElasticSearch(anyString(), anyString(), anyString(), eq(HearingsToJudgmentsSearchResult.class))).thenReturn(result);

        var ccdReportDataSource = new HearingsToJudgmentsCcdDataSource(authToken, ccdClient);

        var results = ccdReportDataSource.getData(caseTypeId, TribunalOffice.LEEDS.getOfficeName(), fromDate, toDate);
        assertEquals(1, results.size());
        assertEquals(submitEvent, results.get(0));
    }

    @Test(expected = ReportException.class)
    public void shouldThrowReportExceptionWhenSearchFails() throws IOException {
        var authToken = "A test token";
        var caseTypeId = "A test case type";
        var fromDate = "10-10-2021";
        var toDate = "10-11-2021";
        var ccdClient = mock(CcdClient.class);
        when(ccdClient.runElasticSearch(anyString(), anyString(), anyString(), eq(HearingsToJudgmentsSearchResult.class))).thenThrow(new IOException());

        var ccdReportDataSource = new HearingsToJudgmentsCcdDataSource(authToken, ccdClient);
        ccdReportDataSource.getData(caseTypeId, TribunalOffice.LEEDS.getOfficeName(), fromDate, toDate);
        fail("Should throw exception instead");
    }

}
