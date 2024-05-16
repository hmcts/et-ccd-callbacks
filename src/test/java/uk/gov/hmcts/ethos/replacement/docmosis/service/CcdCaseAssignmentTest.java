package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.SubCaseLegalRepDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleCaseSearchResult;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCasesSendingService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class CcdCaseAssignmentTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private CcdClient ccdClient;
    @Mock
    private NocCcdService nocCcdService;

    @InjectMocks
    private CcdCaseAssignment ccdCaseAssignment;

    private CallbackRequest callbackRequest;
    @Mock
    private MultipleCasesSendingService multipleCasesSendingService;
    private MultipleDetails multipleDetails;

    @BeforeEach
    void setUp() {
        CaseData caseData = new CaseData();
        caseData.setMultipleFlag(YES);

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId("600/11");
        caseDetails.setCaseTypeId("ET_EnglandWales_Multiple");
        caseDetails.setJurisdiction("EMPLOYMENT");

        callbackRequest = new CallbackRequest();
        callbackRequest.setCaseDetails(caseDetails);
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        multipleCasesSendingService = new MultipleCasesSendingService(ccdClient);
    }

    @Test
    void applyNocAsAdmin() throws IOException {
        CCDCallbackResponse expected = new CCDCallbackResponse(callbackRequest.getCaseDetails().getCaseData());

        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(CCDCallbackResponse.class))).thenReturn(ResponseEntity.ok(expected));
        when(adminUserService.getAdminUserToken()).thenReturn("adminToken");
        when(serviceAuthTokenGenerator.generate()).thenReturn("token");
        when(this.featureToggleService.isMultiplesEnabled()).thenReturn(false);

        CCDCallbackResponse actual = ccdCaseAssignment.applyNocAsAdmin(callbackRequest);

        assertThat(expected).isEqualTo(actual);
        verify(ccdClient, never()).addUserToMultiple(any(), any(), any(), any(), any());
    }

    @Test
    void shouldCallCaseAssignmentNoc_Success() throws IOException {
        CCDCallbackResponse expected = new CCDCallbackResponse(callbackRequest.getCaseDetails().getCaseData());

        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(CCDCallbackResponse.class))).thenReturn(ResponseEntity.ok(expected));
        when(serviceAuthTokenGenerator.generate()).thenReturn("token");
        when(this.featureToggleService.isMultiplesEnabled()).thenReturn(false);

        CCDCallbackResponse actual = ccdCaseAssignment.applyNoc(callbackRequest, "token");

        assertThat(expected).isEqualTo(actual);
        verify(ccdClient, never()).addUserToMultiple(any(), any(), any(), any(), any());
    }

    @Test
    void shouldCallCaseAssignmentNoc_Fail() {
        when(serviceAuthTokenGenerator.generate()).thenReturn("token");
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(CCDCallbackResponse.class)))
                .thenThrow(new RestClientResponseException("call failed", 400, "Bad Request", null, null, null));
        when(this.featureToggleService.isMultiplesEnabled()).thenReturn(false);

        Exception exception = assertThrows(
                RestClientResponseException.class,
                () -> ccdCaseAssignment.applyNoc(callbackRequest, "token"));

        String exceptionMessage = exception.getMessage();
        if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
            assertTrue(exceptionMessage.contains("call failed"));
        }
    }

    @Test
    void shouldCallCaseAssignmentNocMultiple_Success() throws IOException {
        CCDCallbackResponse expected = new CCDCallbackResponse(callbackRequest.getCaseDetails().getCaseData());
        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(CCDCallbackResponse.class))
        ).thenReturn(ResponseEntity.ok(expected));

        when(serviceAuthTokenGenerator.generate()).thenReturn("token");
        when(this.featureToggleService.isMultiplesEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn("adminToken");
        when(nocCcdService.getLatestAuditEventByName(any(), any(), any())).thenReturn(Optional.of(getAuditEvent()));

        MultipleCaseSearchResult expectedMultipleCaseSearchResult =
                new MultipleCaseSearchResult(1L, getMultipleEvents());
        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(MultipleCaseSearchResult.class))
        ).thenReturn(ResponseEntity.ok(expectedMultipleCaseSearchResult));

        ListTypeItem<SubCaseLegalRepDetails> legalRepCollection = new ListTypeItem<>();
        GenericTypeItem<String> legalRepIdItem = GenericTypeItem.from("someId", "someLegalRepId");
        SubCaseLegalRepDetails subCaseLegalRepDetails = SubCaseLegalRepDetails.builder()
                .caseReference("someCaseReference")
                .legalRepIds(ListTypeItem.from(legalRepIdItem))
                .build();
        GenericTypeItem<SubCaseLegalRepDetails> listItem = GenericTypeItem.from("subCaseId", subCaseLegalRepDetails);
        legalRepCollection.add(listItem);
        MultipleData multiDataToUpdate = multipleDetails.getCaseData();
        multiDataToUpdate.setLegalRepCollection(legalRepCollection);
        expectedMultipleCaseSearchResult.getCases().get(0).setCaseData(multiDataToUpdate);

        when(ccdClient.addUserToMultiple(any(), any(), any(), any(), any())).thenReturn(ResponseEntity.ok().build());

        multipleCasesSendingService.sendUpdateToMultiple(
                "token",
                "EnglandWales_Multiple",
                "EMPLOYMENT",
                multiDataToUpdate,
                "123");
        when(ccdClient.addUserToMultiple(any(), any(), any(), any(), any())).thenReturn(ResponseEntity.ok().build());

        CCDCallbackResponse actual = ccdCaseAssignment.applyNoc(callbackRequest, "token");

        assertThat(expected).isEqualTo(actual);
        verify(ccdClient, times(1)).addUserToMultiple(any(), any(), any(), any(), any());
    }

    @Test
    void shouldCallCaseAssignmentNocMultiple_CaseNotMultiple() throws IOException {
        callbackRequest.getCaseDetails().getCaseData().setMultipleFlag(NO);

        CCDCallbackResponse expected = new CCDCallbackResponse(callbackRequest.getCaseDetails().getCaseData());
        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(CCDCallbackResponse.class))
        ).thenReturn(ResponseEntity.ok(expected));

        when(serviceAuthTokenGenerator.generate()).thenReturn("token");
        when(this.featureToggleService.isMultiplesEnabled()).thenReturn(true);

        CCDCallbackResponse actual = ccdCaseAssignment.applyNoc(callbackRequest, "token");

        assertThat(expected).isEqualTo(actual);
        verify(ccdClient, never()).addUserToMultiple(any(), any(), any(), any(), any());
    }

    @Test
    void shouldCallCaseAssignmentNocMultiple_UserNotFound() throws IOException {
        CCDCallbackResponse expected = new CCDCallbackResponse(callbackRequest.getCaseDetails().getCaseData());
        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(CCDCallbackResponse.class))
        ).thenReturn(ResponseEntity.ok(expected));

        when(serviceAuthTokenGenerator.generate()).thenReturn("token");
        when(this.featureToggleService.isMultiplesEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn("adminToken");
        when(nocCcdService.getLatestAuditEventByName(any(), any(), any())).thenReturn(Optional.empty());

        CCDCallbackResponse actual = ccdCaseAssignment.applyNoc(callbackRequest, "token");

        assertThat(expected).isEqualTo(actual);
        verify(ccdClient, never()).addUserToMultiple(any(), any(), any(), any(), any());
    }

    @Test
    void shouldCallCaseAssignmentNocMultiple_MultipleId_NotFound() throws IOException {
        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(CCDCallbackResponse.class))
        ).thenReturn(ResponseEntity.ok(new CCDCallbackResponse(callbackRequest.getCaseDetails().getCaseData())));
        when(serviceAuthTokenGenerator.generate()).thenReturn("token");
        when(this.featureToggleService.isMultiplesEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn("adminToken");
        when(nocCcdService.getLatestAuditEventByName(any(), any(), any())).thenReturn(Optional.of(getAuditEvent()));

        MultipleCaseSearchResult expectedMultipleCaseSearchResult =
                new MultipleCaseSearchResult(1L, new ArrayList<>());
        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(MultipleCaseSearchResult.class))
        ).thenReturn(ResponseEntity.ok(expectedMultipleCaseSearchResult));

        CaseCreationException thrown = assertThrows(CaseCreationException.class, () -> {
            ccdCaseAssignment.applyNoc(callbackRequest, "token");
        });

        assertThat(thrown.getMessage()).contains("Call to add legal rep to Multiple Case failed for");
        verify(ccdClient, never()).addUserToMultiple(any(), any(), any(), eq(null), any());
    }

    @Test
    void shouldCallCaseAssignmentNocMultiple_MultipleId_Empty() throws IOException {
        CCDCallbackResponse expected = new CCDCallbackResponse(callbackRequest.getCaseDetails().getCaseData());
        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(CCDCallbackResponse.class))
        ).thenReturn(ResponseEntity.ok(expected));

        when(serviceAuthTokenGenerator.generate()).thenReturn("token");
        when(this.featureToggleService.isMultiplesEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn("adminToken");
        when(nocCcdService.getLatestAuditEventByName(any(), any(), any())).thenReturn(Optional.of(getAuditEvent()));

        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(MultipleCaseSearchResult.class))
        ).thenThrow(new RestClientResponseException("call failed", 400, "Bad Request", null, null, null));

        Exception exception = assertThrows(RestClientResponseException.class,
                () -> ccdCaseAssignment.applyNoc(callbackRequest, "token"));

        String exceptionMessage = exception.getMessage();
        if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
            assertTrue(exceptionMessage.contains("call failed"));
        }
    }

    @Test
    void shouldCallCaseAssignmentNocMultiple_ccdClientReturn_Empty() throws IOException {
        CCDCallbackResponse expected = new CCDCallbackResponse(callbackRequest.getCaseDetails().getCaseData());
        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(CCDCallbackResponse.class))
        ).thenReturn(ResponseEntity.ok(expected));

        when(serviceAuthTokenGenerator.generate()).thenReturn("token");
        when(this.featureToggleService.isMultiplesEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn("adminToken");
        when(nocCcdService.getLatestAuditEventByName(any(), any(), any())).thenReturn(Optional.of(getAuditEvent()));

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
                () -> ccdCaseAssignment.applyNoc(callbackRequest, "token"));
        assertEquals("Call to add legal rep to Multiple Case failed for 123", exception.getMessage());
    }

    @Test
    void shouldCallCaseAssignmentNocMultiple_ccdClientReturn_Fail() throws IOException {
        CCDCallbackResponse expected = new CCDCallbackResponse(callbackRequest.getCaseDetails().getCaseData());
        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(CCDCallbackResponse.class))
        ).thenReturn(ResponseEntity.ok(expected));

        when(serviceAuthTokenGenerator.generate()).thenReturn("token");
        when(this.featureToggleService.isMultiplesEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn("adminToken");
        when(nocCcdService.getLatestAuditEventByName(any(), any(), any())).thenReturn(Optional.of(getAuditEvent()));

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
                () -> ccdCaseAssignment.applyNoc(callbackRequest, "token"));
        assertEquals("Call to add legal rep to Multiple Case failed for 123 with call failed", exception.getMessage());
    }

    private AuditEvent getAuditEvent() {
        return AuditEvent.builder()
                .userId("123")
                .build();
    }

    private List<SubmitMultipleEvent> getMultipleEvents() {
        SubmitMultipleEvent returnMultiple = new SubmitMultipleEvent();
        returnMultiple.setCaseId(123);

        return List.of(returnMultiple);
    }
}
