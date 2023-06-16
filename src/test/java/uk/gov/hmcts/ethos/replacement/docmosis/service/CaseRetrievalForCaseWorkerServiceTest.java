package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
class CaseRetrievalForCaseWorkerServiceTest {

    @InjectMocks
    private CaseRetrievalForCaseWorkerService caseRetrievalForCaseWorkerService;
    @Mock
    private CcdClient ccdClient;
    private CCDRequest ccdRequest;
    private SubmitEvent submitEvent;

    @BeforeEach
    public void setUp() {
        CaseDetails caseDetails;
        ccdRequest = new CCDRequest();
        caseDetails = new CaseDetails();
        caseDetails.setJurisdiction("TRIBUNALS");
        caseDetails.setCaseTypeId("Manchester_V3");
        ccdRequest.setCaseDetails(caseDetails);
        submitEvent = new SubmitEvent();
        caseRetrievalForCaseWorkerService = new CaseRetrievalForCaseWorkerService(ccdClient);
    }

    @Test
    void caseRetrievalRequestException() {
        assertThrows(Exception.class, () -> {
            when(ccdClient.retrieveCase(anyString(), anyString(),
                    anyString(), any())).thenThrow(new InternalException(ERROR_MESSAGE));
            caseRetrievalForCaseWorkerService.caseRetrievalRequest(
                    "authToken", ccdRequest.getCaseDetails().getCaseTypeId(),
                    ccdRequest.getCaseDetails().getJurisdiction(), "11111");

        });
    }

    @Test
    void caseRetrievalRequest() throws IOException {
        when(ccdClient.retrieveCase(anyString(), anyString(), anyString(), any())).thenReturn(submitEvent);
        SubmitEvent submitEvent1 = caseRetrievalForCaseWorkerService.caseRetrievalRequest("authToken",
                ccdRequest.getCaseDetails().getCaseTypeId(), ccdRequest.getCaseDetails().getJurisdiction(), "11111");
        assertEquals(submitEvent, submitEvent1);
    }

    @Test
    void casesRetrievalRequestException() throws IOException {
        when(ccdClient.retrieveCases(anyString(), any(), any())).thenThrow(new InternalException(ERROR_MESSAGE));

        assertThrows(Exception.class, () ->
                caseRetrievalForCaseWorkerService.casesRetrievalRequest(ccdRequest, "authToken")
        );
    }

    @Test
    void casesRetrievalRequest() throws IOException {
        List<SubmitEvent> submitEventList = Collections.singletonList(submitEvent);
        when(ccdClient.retrieveCases(anyString(), any(), any())).thenReturn(submitEventList);
        List<SubmitEvent> submitEventList1 = caseRetrievalForCaseWorkerService.casesRetrievalRequest(
                ccdRequest, "authToken");
        assertEquals(submitEventList, submitEventList1);
    }

    @Test
    void casesRetrievalESRequestException() throws IOException {
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), any()))
                .thenThrow(new InternalException(ERROR_MESSAGE));

        assertThrows(Exception.class, () ->
                caseRetrievalForCaseWorkerService.casesRetrievalESRequest("1111", "authToken",
                        ccdRequest.getCaseDetails().getCaseTypeId(), new ArrayList<>(Collections.singleton("1")))
        );
    }

    @Test
    void casesRetrievalESRequest() throws IOException {
        List<SubmitEvent> submitEventList = Collections.singletonList(submitEvent);
        when(ccdClient.retrieveCasesElasticSearch(anyString(), anyString(), any())).thenReturn(submitEventList);
        List<SubmitEvent> submitEventList1 = caseRetrievalForCaseWorkerService.casesRetrievalESRequest(
                "1111", "authToken",
                ccdRequest.getCaseDetails().getCaseTypeId(), new ArrayList<>(
                        Collections.singleton("1")));
        assertEquals(submitEventList, submitEventList1);
    }

}