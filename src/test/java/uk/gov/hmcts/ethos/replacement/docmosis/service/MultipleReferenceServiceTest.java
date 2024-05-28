package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.SubCaseLegalRepDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleCaseSearchResult;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefEnglandWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefScotlandRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCasesSendingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleReferenceService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class MultipleReferenceServiceTest {

    @InjectMocks
    private MultipleReferenceService multipleReferenceService;

    @Mock
    private MultipleRefEnglandWalesRepository multipleRefEnglandWalesRepository;
    @Mock
    private MultipleRefScotlandRepository multipleRefScotlandRepository;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private CcdCaseAssignment ccdCaseAssignment;
    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock
    private CcdClient ccdClient;
    @Mock
    private MultipleCasesSendingService multipleCasesSendingService;
    @Mock
    private AdminUserService adminUserService;

    @Captor
    ArgumentCaptor<MultipleData> multipleDataCaptor;

    private MultipleDetails multipleDetails;
    private CaseDetails caseDetails;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = new CaseData();
        caseData.setEthosCaseReference("6000001/2024");
        caseData.setMultipleFlag(YES);
        caseData.setMultipleReference("6000001");

        caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId("1616161616161616");
        caseDetails.setCaseTypeId("ET_EnglandWales_Multiple");
        caseDetails.setJurisdiction("EMPLOYMENT");

        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());

        when(adminUserService.getAdminUserToken()).thenReturn("adminToken");
        when(serviceAuthTokenGenerator.generate()).thenReturn("token");
        when(ccdCaseAssignment.createHeaders(any(), any())).thenReturn(new HttpHeaders());
    }

    @Test
    void createEnglandWalesReference() {
        String expectedReference = "6000001";
        when(multipleRefEnglandWalesRepository.ethosMultipleCaseRefGen()).thenReturn(expectedReference);

        assertEquals(expectedReference, multipleReferenceService.createReference(ENGLANDWALES_BULK_CASE_TYPE_ID));
    }

    @Test
    void createEnglandWalesReferenceCaseTypeNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
                multipleReferenceService.createReference("invalid-case-type-id")
        );
    }

    @Test
    void createScotlandReference() {
        String expectedReference = "8000001";
        when(multipleRefScotlandRepository.ethosMultipleCaseRefGen()).thenReturn(expectedReference);

        assertEquals(expectedReference, multipleReferenceService.createReference(SCOTLAND_BULK_CASE_TYPE_ID));
    }

    @Test
    void createScotlandReferenceCaseTypeNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
                multipleReferenceService.createReference("invalid-case-type-id")
        );
    }

    @Test
    void validateSubcaseIsOfMultiple_NoErrors() {
        List<String> actual = multipleReferenceService.validateSubcaseIsOfMultiple(caseData);
        assertEquals(0, actual.size());
    }

    @Test
    void validateSubcaseIsOfMultiple_NotMultiple() {
        caseData.setMultipleFlag(NO);
        List<String> actual = multipleReferenceService.validateSubcaseIsOfMultiple(caseData);
        assertEquals(1, actual.size());
    }

    @Test
    void validateSubcaseIsOfMultiple_NoMultipleReference() {
        caseData.setMultipleReference("");
        List<String> actual = multipleReferenceService.validateSubcaseIsOfMultiple(caseData);
        assertEquals(1, actual.size());
    }

    @Test
    void validateSubcaseIsOfMultiple_NullMultipleReferences() {
        caseData.setMultipleFlag(null);
        caseData.setMultipleReference(null);
        List<String> actual = multipleReferenceService.validateSubcaseIsOfMultiple(caseData);
        assertEquals(2, actual.size());
    }

    @Test
    void shouldAddLegalRepToMultiple_LR_Exists_In_Collection() throws IOException {
        List<SubmitMultipleEvent> multipleEvents = getMultipleEvents();

        ListTypeItem<String> newLegalRepList = ListTypeItem.from("someLegalRepId");
        GenericTypeItem<SubCaseLegalRepDetails> expectedDetails =
                GenericTypeItem.from(new SubCaseLegalRepDetails(caseData.getEthosCaseReference(), newLegalRepList));

        ListTypeItem<SubCaseLegalRepDetails> legalRepCollection = new ListTypeItem<>();
        legalRepCollection.add(expectedDetails);

        multipleEvents.get(0).getCaseData().setLegalRepCollection(legalRepCollection);

        MultipleCaseSearchResult expectedMultipleCaseSearchResult =
                new MultipleCaseSearchResult(1L, multipleEvents);

        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(MultipleCaseSearchResult.class))
        ).thenReturn(ResponseEntity.ok(expectedMultipleCaseSearchResult));
        when(ccdClient.addUserToMultiple(any(), any(), any(), any(), any())).thenReturn(ResponseEntity.ok().build());

        multipleReferenceService.addLegalRepToMultiple(caseDetails, "someLegalRepId");

        verify(restTemplate).exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(MultipleCaseSearchResult.class));
        verify(ccdClient, times(1)).addUserToMultiple(any(), any(), any(), any(), any());
        verify(multipleCasesSendingService, never()).sendUpdateToMultiple(any(), any(), any(), any(), any());
    }

    @Test
    void shouldAddLegalRepToMultiple_Existing_LR_Collection() throws IOException {
        List<SubmitMultipleEvent> multipleEvents = getMultipleEvents();

        ListTypeItem<String> newLegalRepList = ListTypeItem.from("someOtherLegalRepId");
        GenericTypeItem<SubCaseLegalRepDetails> expectedDetails =
                GenericTypeItem.from(new SubCaseLegalRepDetails(caseData.getEthosCaseReference(), newLegalRepList));

        ListTypeItem<SubCaseLegalRepDetails> legalRepCollection = new ListTypeItem<>();
        legalRepCollection.add(expectedDetails);

        multipleEvents.get(0).getCaseData().setLegalRepCollection(legalRepCollection);

        MultipleCaseSearchResult expectedMultipleCaseSearchResult =
                new MultipleCaseSearchResult(1L, multipleEvents);

        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(MultipleCaseSearchResult.class))
        ).thenReturn(ResponseEntity.ok(expectedMultipleCaseSearchResult));
        when(ccdClient.addUserToMultiple(any(), any(), any(), any(), any())).thenReturn(ResponseEntity.ok().build());

        multipleReferenceService.addLegalRepToMultiple(caseDetails, "someLegalRepId");

        verify(restTemplate).exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(MultipleCaseSearchResult.class));
        verify(ccdClient, times(1)).addUserToMultiple(any(), any(), any(), any(), any());
        verify(multipleCasesSendingService, times(1))
                .sendUpdateToMultiple(any(), any(), any(), multipleDataCaptor.capture(), any());

        MultipleData multiData = multipleDataCaptor.getValue();
        assertEquals(1, multiData.getLegalRepCollection().size());
        assertEquals(caseData.getEthosCaseReference(),
                multiData.getLegalRepCollection().get(0).getValue().getCaseReference());
        assertEquals("someOtherLegalRepId",
                multiData.getLegalRepCollection().get(0).getValue().getLegalRepIds().get(0).getValue());
        assertEquals("someLegalRepId",
                multiData.getLegalRepCollection().get(0).getValue().getLegalRepIds().get(1).getValue());
    }

    @Test
    void shouldAddLegalRepToMultiple_Empty_LR_Collection() throws IOException {
        MultipleCaseSearchResult expectedMultipleCaseSearchResult =
                new MultipleCaseSearchResult(1L, getMultipleEvents());
        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(MultipleCaseSearchResult.class))
        ).thenReturn(ResponseEntity.ok(expectedMultipleCaseSearchResult));
        when(ccdClient.addUserToMultiple(any(), any(), any(), any(), any())).thenReturn(ResponseEntity.ok().build());

        multipleReferenceService.addLegalRepToMultiple(caseDetails, "someLegalRepId");

        verify(restTemplate).exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(MultipleCaseSearchResult.class));
        verify(ccdClient, times(1)).addUserToMultiple(any(), any(), any(), any(), any());
        verify(multipleCasesSendingService, times(1))
                .sendUpdateToMultiple(any(), any(), any(), multipleDataCaptor.capture(), any());

        MultipleData multiData = multipleDataCaptor.getValue();
        assertEquals(1, multiData.getLegalRepCollection().size());
        assertEquals(caseData.getEthosCaseReference(),
                multiData.getLegalRepCollection().get(0).getValue().getCaseReference());
        assertEquals("someLegalRepId",
                multiData.getLegalRepCollection().get(0).getValue().getLegalRepIds().get(0).getValue());
    }

    @Test
    void shouldAddLegalRepToMultiple_getMultipleByReference_Fail() throws IOException {
        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(MultipleCaseSearchResult.class))
        ).thenThrow(new RestClientResponseException("call failed", 400, "Bad Request", null, null, null));

        Exception exception = assertThrows(RestClientResponseException.class,
                () -> multipleReferenceService.addLegalRepToMultiple(caseDetails, "someLegalRepId"));

        String exceptionMessage = exception.getMessage();
        if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
            assertTrue(exceptionMessage.contains("call failed"));
        }
        verify(ccdClient, never()).addUserToMultiple(any(), any(), any(), any(), any());
        verify(multipleCasesSendingService, never()).sendUpdateToMultiple(any(), any(), any(), any(), any());
    }

    @Test
    void shouldAddLegalRepToMultiple_getMultipleByReference_MultipleNotFound() throws IOException {
        MultipleCaseSearchResult expectedMultipleCaseSearchResult =
                new MultipleCaseSearchResult(1L, new ArrayList<>());
        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(MultipleCaseSearchResult.class))
        ).thenReturn(ResponseEntity.ok(expectedMultipleCaseSearchResult));

        multipleReferenceService.addLegalRepToMultiple(caseDetails, "someLegalRepId");

        verify(restTemplate).exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(MultipleCaseSearchResult.class));
        verify(ccdClient, never()).addUserToMultiple(any(), any(), any(), any(), any());
        verify(multipleCasesSendingService, never()).sendUpdateToMultiple(any(), any(), any(), any(), any());
    }

    @Test
    void shouldAddLegalRepToMultiple_addUserToMultiple_Empty() throws IOException {
        MultipleCaseSearchResult expectedMultipleCaseSearchResult =
                new MultipleCaseSearchResult(1L, getMultipleEvents());
        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(MultipleCaseSearchResult.class))
        ).thenReturn(ResponseEntity.ok(expectedMultipleCaseSearchResult));
        when(ccdClient.addUserToMultiple(any(), any(), any(), any(), any())).thenReturn(null);

        Exception exception = assertThrows(CaseCreationException.class,
                () -> multipleReferenceService.addLegalRepToMultiple(caseDetails, "someLegalRepId"));

        verify(restTemplate).exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(MultipleCaseSearchResult.class));
        assertEquals("Call to add legal rep to Multiple Case failed for 123", exception.getMessage());
        verify(multipleCasesSendingService, never()).sendUpdateToMultiple(any(), any(), any(), any(), any());
    }

    @Test
    void shouldAddLegalRepToMultiple_addUserToMultiple_Fail() throws IOException {
        MultipleCaseSearchResult expectedMultipleCaseSearchResult =
                new MultipleCaseSearchResult(1L, getMultipleEvents());
        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(MultipleCaseSearchResult.class))
        ).thenReturn(ResponseEntity.ok(expectedMultipleCaseSearchResult));
        when(ccdClient.addUserToMultiple(any(), any(), any(), any(), any()))
                .thenThrow(new RestClientResponseException("call failed", 400, "Bad Request", null, null, null));

        Exception exception = assertThrows(CaseCreationException.class,
                () -> multipleReferenceService.addLegalRepToMultiple(caseDetails, "someLegalRepId"));

        verify(restTemplate).exchange(
                anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(MultipleCaseSearchResult.class));
        assertEquals("Call to add legal rep to Multiple Case failed for 123 with call failed",
                exception.getMessage());
        verify(multipleCasesSendingService, never()).sendUpdateToMultiple(any(), any(), any(), any(), any());
    }

    private List<SubmitMultipleEvent> getMultipleEvents() {
        SubmitMultipleEvent returnMultiple = new SubmitMultipleEvent();
        returnMultiple.setCaseId(123);
        returnMultiple.setCaseData(multipleDetails.getCaseData());

        return List.of(returnMultiple);
    }
}
