package uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.multiples.MultipleCaseSearchResult;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.ReportException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class NoPositionChangeCcdDataSourceTests {

    @Test
    void shouldReturnSearchResults() throws IOException {
        String authToken = "A test token";
        String caseTypeId = "A test case type";
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String reportDate = "2021-07-10";
        CcdClient ccdClient = mock(CcdClient.class);
        NoPositionChangeSearchResult searchResult = new NoPositionChangeSearchResult();
        searchResult.setCases(List.of(new NoPositionChangeSubmitEvent()));
        when(ccdClient.runElasticSearch(anyString(), anyString(), anyString(), eq(NoPositionChangeSearchResult.class)))
                .thenReturn(searchResult);

        NoPositionChangeCcdDataSource ccdReportDataSource = new NoPositionChangeCcdDataSource(authToken, ccdClient);
        List<NoPositionChangeSubmitEvent> results = ccdReportDataSource.getData(caseTypeId, reportDate, managingOffice);

        assertEquals(1, results.size());
        assertEquals(searchResult.getCases().get(0), results.get(0));
    }

    @Test
    void shouldReturnEmptyListForNullSearchResults() throws IOException {
        String authToken = "A test token";
        String caseTypeId = "A test case type";
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String reportDate = "2021-07-10";
        CcdClient ccdClient = mock(CcdClient.class);
        when(ccdClient.runElasticSearch(anyString(), anyString(), anyString(), eq(NoPositionChangeSearchResult.class)))
                .thenReturn(null);

        NoPositionChangeCcdDataSource ccdReportDataSource = new NoPositionChangeCcdDataSource(authToken, ccdClient);
        List<NoPositionChangeSubmitEvent> results = ccdReportDataSource.getData(caseTypeId, reportDate, managingOffice);

        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    void shouldThrowReportExceptionWhenSearchFails() throws IOException {
        String authToken = "A test token";
        String caseTypeId = "A test case type";
        String managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        String reportDate = "2021-07-10";
        CcdClient ccdClient = mock(CcdClient.class);
        when(ccdClient.runElasticSearch(anyString(), anyString(), anyString(), eq(NoPositionChangeSearchResult.class)))
                .thenThrow(new IOException());

        NoPositionChangeCcdDataSource ccdReportDataSource = new NoPositionChangeCcdDataSource(authToken, ccdClient);

        assertThrows(ReportException.class, () ->
                ccdReportDataSource.getData(caseTypeId, reportDate, managingOffice)
        );
    }

    @Test
    void shouldReturnMultipleSearchResults() throws IOException {
        String authToken = "A test token";
        String caseTypeId = "A test case type";
        CcdClient ccdClient = mock(CcdClient.class);
        MultipleCaseSearchResult searchResult = new MultipleCaseSearchResult();
        when(ccdClient.runElasticSearch(anyString(), anyString(), anyString(), eq(MultipleCaseSearchResult.class)))
                .thenReturn(searchResult);

        NoPositionChangeCcdDataSource ccdReportDataSource = new NoPositionChangeCcdDataSource(authToken, ccdClient);
        List<SubmitMultipleEvent> results = ccdReportDataSource.getMultiplesData(caseTypeId, new ArrayList<>());

        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    void shouldReturnEmptyListForNullMultipleSearchResults() throws IOException {
        String authToken = "A test token";
        String caseTypeId = "A test case type";
        CcdClient ccdClient = mock(CcdClient.class);
        when(ccdClient.runElasticSearch(anyString(), anyString(), anyString(), eq(MultipleCaseSearchResult.class)))
                .thenReturn(null);

        NoPositionChangeCcdDataSource ccdReportDataSource = new NoPositionChangeCcdDataSource(authToken, ccdClient);
        List<SubmitMultipleEvent> results = ccdReportDataSource.getMultiplesData(caseTypeId, new ArrayList<>());

        assertNotNull(results);
        assertEquals(0, results.size());
    }

    @Test
    void shouldThrowReportExceptionWhenMultipleSearchFails() throws IOException {
        String authToken = "A test token";
        String caseTypeId = "A test case type";
        CcdClient ccdClient = mock(CcdClient.class);
        when(ccdClient.runElasticSearch(anyString(), anyString(), anyString(), eq(MultipleCaseSearchResult.class)))
                .thenThrow(new IOException());

        NoPositionChangeCcdDataSource ccdReportDataSource = new NoPositionChangeCcdDataSource(authToken, ccdClient);

        assertThrows(ReportException.class, () ->
                ccdReportDataSource.getMultiplesData(caseTypeId, new ArrayList<>())
        );
    }
}
