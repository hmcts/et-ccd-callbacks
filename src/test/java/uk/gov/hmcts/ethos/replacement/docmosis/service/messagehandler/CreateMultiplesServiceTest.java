package uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.multiples.AdditionalClaimant;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.ClaimantContactDetailsDocumentService;
import uk.gov.hmcts.reform.et.syaapi.service.NotificationService;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ET1_ONLINE_CASE_SOURCE;

@ExtendWith(MockitoExtension.class)
class CreateMultiplesServiceTest {

    private static final String TOKEN = "test-token";

    @Mock
    private CcdClient ccdClient;

    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private DocumentManagementService documentManagementService;

    @InjectMocks
    private CreateMultiplesService createMultiplesService;

    @InjectMocks
    private ClaimantContactDetailsDocumentService claimantContactDetailsDocumentService;

    @Test
    void retrieveLeadCaseReturnsNullWhenNoCasesFound() throws IOException {
        CreateUpdatesMsg msg = CreateUpdatesMsg.builder()
                .caseTypeId("ET_EnglandWales")
                .ethosCaseRefCollection(List.of("6000001/2024"))
                .build();
        when(ccdClient.retrieveCasesElasticSearch(TOKEN, "ET_EnglandWales", List.of("6000001/2024")))
                .thenReturn(List.of());

        SubmitEvent result = createMultiplesService.retrieveLeadCase(TOKEN, msg);

        assertNull(result);
    }

    @Test
    void retrieveLeadCaseReturnsFirstCaseWhenFound() throws IOException {
        CreateUpdatesMsg msg = CreateUpdatesMsg.builder()
                .caseTypeId("ET_EnglandWales")
                .ethosCaseRefCollection(List.of("6000001/2024"))
                .build();
        SubmitEvent submitEvent = new SubmitEvent();
        when(ccdClient.retrieveCasesElasticSearch(TOKEN, "ET_EnglandWales", List.of("6000001/2024")))
                .thenReturn(List.of(submitEvent));

        SubmitEvent result = createMultiplesService.retrieveLeadCase(TOKEN, msg);

        assertEquals(submitEvent, result);
    }

    @Test
    void createCaseReturnsCreatedEthosReference() throws IOException {
        SubmitEvent leadCase = new SubmitEvent();
        CaseData leadData = new CaseData();
        leadData.setEthosCaseReference("6000001/2024");
        leadCase.setCaseData(leadData);

        CCDRequest startRequest = new CCDRequest();
        when(ccdClient.startMultipleCaseCreation(anyString(), any(CaseDetails.class))).thenReturn(startRequest);

        SubmitEvent createdCase = new SubmitEvent();
        CaseData createdData = new CaseData();
        createdData.setEthosCaseReference("6000002/2024");
        createdCase.setCaseData(createdData);
        when(ccdClient.submitCaseCreation(anyString(), any(CaseDetails.class), eq(startRequest), anyString()))
                .thenReturn(createdCase);

        CCDRequest eventRequest = new CCDRequest();
        CaseDetails eventCaseDetails = new CaseDetails();
        eventRequest.setCaseDetails(eventCaseDetails);
        when(ccdClient.startEventForCase(anyString(), anyString(), anyString(), anyString())).thenReturn(eventRequest);

        CreateUpdatesMsg msg = CreateUpdatesMsg.builder()
                .caseTypeId("ET_EnglandWales")
                .jurisdiction("EMPLOYMENT")
                .build();
        AdditionalClaimant claimant = AdditionalClaimant.builder()
                .firstName("Alice")
                .lastName("One")
                .dob("01/02/1990")
                .build();
        String result = createMultiplesService.createCase(leadCase, TOKEN, msg, claimant);

        assertEquals("6000002/2024", result);
        ArgumentCaptor<CaseDetails> detailsCaptor = ArgumentCaptor.forClass(CaseDetails.class);
        verify(ccdClient).startMultipleCaseCreation(eq(TOKEN), detailsCaptor.capture());
        assertEquals("1990-02-01",
                detailsCaptor.getValue().getCaseData().getClaimantIndType().getClaimantDateOfBirth());
        verify(caseManagementForCaseWorkerService).setHmctsServiceIdSupplementary(eventCaseDetails);
    }

    @Test
    void createCaseReturnsNullWhenNoClaimant() throws IOException {
        CreateUpdatesMsg msg = CreateUpdatesMsg.builder().build();
        assertNull(createMultiplesService.createCase(new SubmitEvent(), TOKEN, msg, null));
    }

    @Test
    void createCaseReturnsNullWhenCcdReturnsEmptyCase() throws IOException {
        SubmitEvent leadCase = new SubmitEvent();
        CaseData leadData = new CaseData();
        leadData.setEthosCaseReference("6000001/2024");
        leadCase.setCaseData(leadData);
        leadCase.setCaseId(11L);

        CCDRequest startRequest = new CCDRequest();
        when(ccdClient.startMultipleCaseCreation(anyString(), any(CaseDetails.class))).thenReturn(startRequest);
        when(ccdClient.submitCaseCreation(anyString(), any(CaseDetails.class), eq(startRequest), anyString()))
                .thenReturn(null);

        CreateUpdatesMsg msg = CreateUpdatesMsg.builder()
                .caseTypeId("ET_EnglandWales")
                .jurisdiction("EMPLOYMENT")
                .build();
        AdditionalClaimant claimant = AdditionalClaimant.builder()
                .firstName("Alice")
                .lastName("One")
                .build();

        String result = createMultiplesService.createCase(leadCase, TOKEN, msg, claimant);

        assertNull(result);
    }

    @Test
    void createMultipleShellCreatesMultipleWithLeadAndChildren() throws IOException {
        CaseData leadData = new CaseData();
        leadData.setEthosCaseReference("6000001/2024");
        leadData.setManagingOffice("Leeds");
        leadData.setRespondent("Respondent 1");
        SubmitEvent leadCase = new SubmitEvent();
        leadCase.setCaseData(leadData);

        CCDRequest request = new CCDRequest();
        when(ccdClient.startCaseMultipleCreation(eq(TOKEN), anyString(), eq("EMPLOYMENT"))).thenReturn(request);
        SubmitMultipleEvent createdMultiple = new SubmitMultipleEvent();
        MultipleData createdMultipleData = new MultipleData();
        createdMultipleData.setLeadEthosCaseRef("6000001/2024");
        createdMultipleData.setMultipleReference("6000123/2024");
        createdMultiple.setCaseData(createdMultipleData);
        createdMultiple.setCaseId(123_456L);
        when(ccdClient.submitMultipleCreation(eq(TOKEN), any(MultipleData.class), anyString(), eq("EMPLOYMENT"),
                eq(request))).thenReturn(createdMultiple);

        CreateUpdatesMsg msg = CreateUpdatesMsg.builder()
                .caseTypeId("ET_EnglandWales")
                .jurisdiction("EMPLOYMENT")
                .ethosCaseRefCollection(List.of("6000001/2024"))
                .build();
        SubmitMultipleEvent result = createMultiplesService.createMultipleShell(
                TOKEN, msg, leadCase, List.of("6000002/2024", "6000003/2024"), Map.of());

        assertEquals(createdMultiple, result);
        ArgumentCaptor<MultipleData> captor = ArgumentCaptor.forClass(MultipleData.class);
        verify(ccdClient).submitMultipleCreation(eq(TOKEN), captor.capture(), anyString(), eq("EMPLOYMENT"),
                eq(request));
        MultipleData multipleData = captor.getValue();
        assertEquals(ET1_ONLINE_CASE_SOURCE, multipleData.getMultipleSource());
        assertEquals("Leeds", multipleData.getManagingOffice());
        // lead ref + two child refs
        assertEquals(3, multipleData.getCaseIdCollection().size());
        // No claimant cases failed, so no failure email should be sent
        verify(notificationService, never())
                .sendFailedAdditionalClaimantsEmail(anyString(), anyString(), anyLong());
    }

    @Test
    void createMultipleShellAddsFailedClaimantNoteForManualEntry() throws IOException {
        CaseData leadData = new CaseData();
        leadData.setEthosCaseReference("6000001/2024");
        leadData.setManagingOffice("Leeds");
        leadData.setRespondent("Respondent 1");
        leadData.setAddClaimantMethod("manually");
        SubmitEvent leadCase = new SubmitEvent();
        leadCase.setCaseData(leadData);

        CCDRequest request = new CCDRequest();
        when(ccdClient.startCaseMultipleCreation(eq(TOKEN), anyString(), eq("EMPLOYMENT"))).thenReturn(request);
        SubmitMultipleEvent createdMultiple = new SubmitMultipleEvent();
        MultipleData createdMultipleData = new MultipleData();
        createdMultipleData.setLeadEthosCaseRef("6000001/2024");
        createdMultipleData.setMultipleReference("6000123/2024");
        createdMultiple.setCaseData(createdMultipleData);
        createdMultiple.setCaseId(4444L);
        when(ccdClient.submitMultipleCreation(eq(TOKEN), any(MultipleData.class), anyString(), eq("EMPLOYMENT"),
                eq(request))).thenReturn(createdMultiple);

        Map<Integer, AdditionalClaimant> failedCases = new LinkedHashMap<>();
        failedCases.put(0, AdditionalClaimant.builder().firstName("Jane").lastName("Doe").build());

        CreateUpdatesMsg msg = CreateUpdatesMsg.builder()
                .caseTypeId("ET_EnglandWales")
                .jurisdiction("EMPLOYMENT")
                .ethosCaseRefCollection(List.of("6000001/2024"))
                .build();
        createMultiplesService.createMultipleShell(
                TOKEN, msg, leadCase, List.of("6000002/2024"), failedCases);

        ArgumentCaptor<MultipleData> captor = ArgumentCaptor.forClass(MultipleData.class);
        verify(ccdClient).submitMultipleCreation(eq(TOKEN), captor.capture(), anyString(), eq("EMPLOYMENT"),
                eq(request));
        assertTrue(captor.getValue().getMultipleNote().contains("manual entry"));
        assertTrue(captor.getValue().getMultipleNote().contains("Additional claimant 1 (Jane Doe)"));
        verify(notificationService).sendFailedAdditionalClaimantsEmail("6000001/2024", "6000123/2024", 4444L);
    }

    @Test
    void createMultipleShellAddsFailedClaimantNoteForSpreadsheetUploadWithMissingData() throws IOException {
        CaseData leadData = new CaseData();
        leadData.setEthosCaseReference("6000001/2024");
        leadData.setManagingOffice("Leeds");
        leadData.setRespondent("Respondent 1");
        leadData.setAddClaimantMethod("spreadsheet");
        SubmitEvent leadCase = new SubmitEvent();
        leadCase.setCaseData(leadData);

        CCDRequest request = new CCDRequest();
        when(ccdClient.startCaseMultipleCreation(eq(TOKEN), anyString(), eq("EMPLOYMENT"))).thenReturn(request);
        SubmitMultipleEvent createdMultiple = new SubmitMultipleEvent();
        MultipleData createdMultipleData = new MultipleData();
        createdMultipleData.setLeadEthosCaseRef("6000001/2024");
        createdMultipleData.setMultipleReference("6000123/2024");
        createdMultiple.setCaseData(createdMultipleData);
        createdMultiple.setCaseId(5555L);
        when(ccdClient.submitMultipleCreation(eq(TOKEN), any(MultipleData.class), anyString(), eq("EMPLOYMENT"),
                eq(request))).thenReturn(createdMultiple);

        Map<Integer, AdditionalClaimant> failedCases = new LinkedHashMap<>();
        failedCases.put(0, null);

        CreateUpdatesMsg msg = CreateUpdatesMsg.builder()
                .caseTypeId("ET_EnglandWales")
                .jurisdiction("EMPLOYMENT")
                .ethosCaseRefCollection(List.of("6000001/2024"))
                .build();
        createMultiplesService.createMultipleShell(
                TOKEN, msg, leadCase, List.of("6000002/2024"), failedCases);

        ArgumentCaptor<MultipleData> captor = ArgumentCaptor.forClass(MultipleData.class);
        verify(ccdClient).submitMultipleCreation(eq(TOKEN), captor.capture(), anyString(), eq("EMPLOYMENT"),
                eq(request));
        assertTrue(captor.getValue().getMultipleNote().contains("spreadsheet upload"));
        assertTrue(captor.getValue().getMultipleNote().contains("Row 1: no claimant data supplied"));
        verify(notificationService).sendFailedAdditionalClaimantsEmail("6000001/2024", "6000123/2024", 5555L);
    }

    @Test
    void generateAndUploadClaimantContactDetails_uploadsPdfAndStoresOnMultiple() throws Exception {
        // Lead claimant setup
        ClaimantIndType indType = new ClaimantIndType();
        indType.setClaimantTitle("Mr");
        indType.setClaimantFirstNames("Alice");
        indType.setClaimantLastName("Smith");
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantEmailAddress("alice@example.com");
        Address address = new Address();
        address.setAddressLine1("1 Main St");
        address.setPostTown("London");
        address.setPostCode("SW1A 1AA");
        claimantType.setClaimantAddressUK(address);
        CaseData leadData = new CaseData();
        leadData.setClaimantIndType(indType);
        leadData.setClaimantType(claimantType);
        SubmitEvent leadCase = new SubmitEvent();
        leadCase.setCaseData(leadData);

        // Created multiple with fresh MultipleData
        MultipleData multipleData = new MultipleData();
        SubmitMultipleEvent createdMultiple = new SubmitMultipleEvent();
        createdMultiple.setCaseData(multipleData);
        createdMultiple.setCaseId(99999L);

        URI mockUri = URI.create("http://dm-store/documents/test-uuid");
        when(documentManagementService.uploadDocument(
                anyString(), any(byte[].class), anyString(), anyString(), anyString()))
                .thenReturn(mockUri);
        when(documentManagementService.generateDownloadableURL(mockUri))
                .thenReturn("http://gateway/documents/test-uuid/binary");

        CCDRequest amendRequest = new CCDRequest();
        when(ccdClient.startBulkAmendEventForCase(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(amendRequest);

        CreateUpdatesMsg msg = CreateUpdatesMsg.builder()
                .caseTypeId("ET_EnglandWales")
                .jurisdiction("EMPLOYMENT")
                .build();

        claimantContactDetailsDocumentService.generateAndUploadClaimantContactDetails(
                TOKEN, msg, leadCase, createdMultiple);

        // Verify PDF was uploaded
        verify(documentManagementService).uploadDocument(
                eq(TOKEN), any(byte[].class),
                eq("Claimant Contact Details.pdf"), anyString(), eq("ET_EnglandWales"));

        // Verify the multiple shell was updated with the document
        verify(ccdClient).submitMultipleEventForCase(eq(TOKEN), eq(multipleData),
                anyString(), eq("EMPLOYMENT"), eq(amendRequest), eq("99999"));
        assertNotNull(multipleData.getClaimantContactDetailsDocument());
        assertEquals("Claimant Contact Details.pdf",
                multipleData.getClaimantContactDetailsDocument().getDocumentFilename());
    }

    @Test
    void generateAndUploadClaimantContactDetails_skipsWhenFiveOrFewerClaimants() throws IOException {
        SubmitMultipleEvent createdMultiple = new SubmitMultipleEvent();
        createdMultiple.setCaseData(new MultipleData());
        createdMultiple.setCaseId(77777L);

        CreateUpdatesMsg msg = CreateUpdatesMsg.builder()
                .caseTypeId("ET_EnglandWales")
                .jurisdiction("EMPLOYMENT")
                .build();

        claimantContactDetailsDocumentService.generateAndUploadClaimantContactDetails(
                TOKEN, msg, new SubmitEvent(), createdMultiple);

        verify(documentManagementService, never()).uploadDocument(
                anyString(), any(), anyString(), anyString(), anyString());
        verify(ccdClient, never()).submitMultipleEventForCase(
                anyString(), any(MultipleData.class), anyString(), anyString(), any(CCDRequest.class), anyString());
    }

    @Test
    void generateAndUploadClaimantContactDetails_skipsWhenMultipleIsNull() throws IOException {
        CreateUpdatesMsg msg = CreateUpdatesMsg.builder()
                .caseTypeId("ET_EnglandWales")
                .jurisdiction("EMPLOYMENT")
                .build();

        claimantContactDetailsDocumentService.generateAndUploadClaimantContactDetails(
                TOKEN, msg, new SubmitEvent(), null);

        verify(documentManagementService, never()).uploadDocument(
                anyString(), any(), anyString(), anyString(), anyString());
        verify(ccdClient, never()).submitMultipleEventForCase(
                anyString(), any(MultipleData.class), anyString(), anyString(), any(CCDRequest.class), anyString());
    }

    @Test
    void generateAndUploadClaimantContactDetails_logsErrorButDoesNotThrowOnFailure() throws IOException {
        SubmitMultipleEvent createdMultiple = new SubmitMultipleEvent();
        createdMultiple.setCaseData(new MultipleData());
        createdMultiple.setCaseId(12345L);

        when(documentManagementService.uploadDocument(
                anyString(), any(byte[].class), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("DM unavailable"));

        CreateUpdatesMsg msg = CreateUpdatesMsg.builder()
                .caseTypeId("ET_EnglandWales")
                .jurisdiction("EMPLOYMENT")
                .build();

        // Must not throw; failure is swallowed and logged
        claimantContactDetailsDocumentService.generateAndUploadClaimantContactDetails(
                TOKEN, msg, new SubmitEvent(), createdMultiple);

        verify(ccdClient, never()).submitMultipleEventForCase(
                anyString(), any(MultipleData.class), anyString(), anyString(), any(CCDRequest.class), anyString());
    }

    @Test
    void createMultipleShellSendsFailureEmailWhenShellCreationFails() throws IOException {
        CaseData leadData = new CaseData();
        leadData.setEthosCaseReference("6000001/2024");
        leadData.setManagingOffice("Leeds");
        leadData.setRespondent("Respondent 1");
        leadData.setFeeGroupReference("987654321");
        SubmitEvent leadCase = new SubmitEvent();
        leadCase.setCaseData(leadData);

        CCDRequest request = new CCDRequest();
        when(ccdClient.startCaseMultipleCreation(eq(TOKEN), anyString(), eq("EMPLOYMENT"))).thenReturn(request);
        when(ccdClient.submitMultipleCreation(eq(TOKEN), any(MultipleData.class), anyString(), eq("EMPLOYMENT"),
                eq(request))).thenReturn(null);

        CreateUpdatesMsg msg = CreateUpdatesMsg.builder()
                .caseTypeId("ET_EnglandWales")
                .jurisdiction("EMPLOYMENT")
                .ethosCaseRefCollection(List.of("6000001/2024"))
                .build();
        SubmitMultipleEvent result = createMultiplesService.createMultipleShell(
                TOKEN, msg, leadCase, List.of("6000002/2024"), Map.of());

        assertNull(result);
        verify(notificationService).sendFailedMultiplesShellCreationEmail("6000001/2024", 987_654_321L);
    }
}
