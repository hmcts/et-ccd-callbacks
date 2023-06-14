package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.INDIVIDUAL_TYPE_CLAIMANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
public class CaseUpdateForCaseWorkerServiceTest {

    @InjectMocks
    private CaseUpdateForCaseWorkerService caseUpdateForCaseWorkerService;
    @Mock
    private DefaultValuesReaderService defaultValuesReaderService;
    @Mock
    private CcdClient ccdClient;
    private CCDRequest englandWalesCcdRequest;
    private CCDRequest scotlandCcdRequest;
    private SubmitEvent submitEvent;
    private DefaultValues englandWalesDefaultValues;
    private DefaultValues scotlandDefaultValues;

    @BeforeEach
    public void setUp() {
        submitEvent = new SubmitEvent();

        englandWalesCcdRequest = new CCDRequest();
        CaseDetails englandWalesCaseDetails = new CaseDetails();
        englandWalesCaseDetails.setCaseData(new CaseData());
        englandWalesCaseDetails.getCaseData().setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        englandWalesCaseDetails.setCaseId("123456");
        englandWalesCaseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        englandWalesCaseDetails.setJurisdiction("TRIBUNALS");
        englandWalesCcdRequest.setCaseDetails(englandWalesCaseDetails);

        scotlandCcdRequest = new CCDRequest();
        CaseDetails scotlandCaseDetails = new CaseDetails();
        scotlandCaseDetails.setCaseData(new CaseData());
        scotlandCaseDetails.getCaseData().setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        scotlandCaseDetails.setCaseId("123456");
        scotlandCaseDetails.setCaseTypeId(SCOTLAND_CASE_TYPE_ID);
        scotlandCaseDetails.setJurisdiction("TRIBUNALS");
        scotlandCcdRequest.setCaseDetails(scotlandCaseDetails);

        caseUpdateForCaseWorkerService = new CaseUpdateForCaseWorkerService(ccdClient, defaultValuesReaderService);
        englandWalesDefaultValues = DefaultValues.builder()
                .positionType("Awaiting ET3")
                .claimantTypeOfClaimant(INDIVIDUAL_TYPE_CLAIMANT)
                .caseType(SINGLE_CASE_TYPE)
                .tribunalCorrespondenceAddressLine1("Manchester Employment Tribunal,")
                .tribunalCorrespondenceAddressLine2("Alexandra House,")
                .tribunalCorrespondenceAddressLine3("14-22 The Parsonage,")
                .tribunalCorrespondenceTown("Manchester,")
                .tribunalCorrespondencePostCode("M3 2JA")
                .tribunalCorrespondenceTelephone("0161 833 6100")
                .tribunalCorrespondenceFax("7577126570")
                .tribunalCorrespondenceDX("123456")
                .tribunalCorrespondenceEmail("manchester@gmail.com")
                .build();
        scotlandDefaultValues = DefaultValues.builder()
                .positionType("Awaiting ET3")
                .claimantTypeOfClaimant(INDIVIDUAL_TYPE_CLAIMANT)
                .managingOffice(TribunalOffice.GLASGOW.getOfficeName())
                .caseType(SINGLE_CASE_TYPE)
                .tribunalCorrespondenceAddressLine1("Eagle Building,")
                .tribunalCorrespondenceAddressLine2("215 Bothwell Street,")
                .tribunalCorrespondenceTown("Glasgow,")
                .tribunalCorrespondencePostCode("G2 7TS")
                .tribunalCorrespondenceTelephone("0141 204 0730")
                .tribunalCorrespondenceFax("2937126570")
                .tribunalCorrespondenceDX("1231123")
                .tribunalCorrespondenceEmail("glasgow@gmail.com")
                .build();
    }

    @Test
    public void caseCreationEnglandWalesRequestException() throws IOException {
assertThrows(Exception.class, () -> {});        
when(ccdClient.startEventForCase(anyString(), anyString(), anyString(),
                anyString())).thenThrow(new InternalException(ERROR_MESSAGE));
        when(ccdClient.submitEventForCase(anyString(), any(), anyString(),
                anyString(), any(), anyString())).thenReturn(submitEvent);
        when(defaultValuesReaderService.getDefaultValues(TribunalOffice.MANCHESTER.getOfficeName()))
                .thenReturn(englandWalesDefaultValues);
        caseUpdateForCaseWorkerService.caseUpdateRequest(englandWalesCcdRequest, "authToken");
    }

    @Test
    public void caseCreationEnglandWalesRequest() throws IOException {
        when(ccdClient.startEventForCase(anyString(), anyString(),
                anyString(), anyString())).thenReturn(englandWalesCcdRequest);
        when(ccdClient.submitEventForCase(anyString(), any(), anyString(),
                anyString(), any(), anyString())).thenReturn(submitEvent);
        when(defaultValuesReaderService.getDefaultValues(
                TribunalOffice.MANCHESTER.getOfficeName()))
                .thenReturn(englandWalesDefaultValues);
        SubmitEvent submitEvent1 = caseUpdateForCaseWorkerService.caseUpdateRequest(
                englandWalesCcdRequest, "authToken");
        assertEquals(submitEvent, submitEvent1);
    }

    @Test
    public void caseCreationScotlandRequest() throws IOException {
        when(ccdClient.startEventForCase(anyString(), anyString(),
                anyString(), anyString())).thenReturn(scotlandCcdRequest);
        when(ccdClient.submitEventForCase(anyString(), any(),
                anyString(), anyString(), any(), anyString()))
                .thenReturn(submitEvent);
        when(defaultValuesReaderService.getDefaultValues(
                TribunalOffice.GLASGOW.getOfficeName()))
                .thenReturn(scotlandDefaultValues);
        SubmitEvent submitEvent1 = caseUpdateForCaseWorkerService
                .caseUpdateRequest(scotlandCcdRequest, "authToken");
        assertEquals(submitEvent, submitEvent1);
    }
}
