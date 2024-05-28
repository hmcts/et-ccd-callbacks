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
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleReferenceService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    private FeatureToggleService featureToggleService;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private NocCcdService nocCcdService;
    @Mock
    private MultipleReferenceService multipleReferenceService;

    @InjectMocks
    private CcdCaseAssignment ccdCaseAssignment;

    private CallbackRequest callbackRequest;

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
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
    }

    @Test
    void applyNocAsAdmin() throws IOException {
        CCDCallbackResponse expected = new CCDCallbackResponse(callbackRequest.getCaseDetails().getCaseData());

        when(serviceAuthTokenGenerator.generate()).thenReturn("token");
        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(CCDCallbackResponse.class))).thenReturn(ResponseEntity.ok(expected));
        when(this.featureToggleService.isMul2Enabled()).thenReturn(false);

        CCDCallbackResponse actual = ccdCaseAssignment.applyNocAsAdmin(callbackRequest);

        assertThat(expected).isEqualTo(actual);
        verify(multipleReferenceService, never()).addLegalRepToMultiple(any(), any());
    }

    @Test
    void shouldCallCaseAssignmentNoc_Success() throws IOException {
        CCDCallbackResponse expected = new CCDCallbackResponse(callbackRequest.getCaseDetails().getCaseData());

        when(serviceAuthTokenGenerator.generate()).thenReturn("token");
        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(CCDCallbackResponse.class))).thenReturn(ResponseEntity.ok(expected));
        when(this.featureToggleService.isMul2Enabled()).thenReturn(false);

        CCDCallbackResponse actual = ccdCaseAssignment.applyNoc(callbackRequest, "token");

        assertThat(expected).isEqualTo(actual);
        verify(multipleReferenceService, never()).addLegalRepToMultiple(any(), any());
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
        when(this.featureToggleService.isMul2Enabled()).thenReturn(false);

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
        when(serviceAuthTokenGenerator.generate()).thenReturn("token");
        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(CCDCallbackResponse.class))
        ).thenReturn(ResponseEntity.ok(expected));
        when(this.featureToggleService.isMul2Enabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn("adminToken");
        when(nocCcdService.getLatestAuditEventByName(any(), any(), any())).thenReturn(Optional.of(getAuditEvent()));

        CCDCallbackResponse actual = ccdCaseAssignment.applyNoc(callbackRequest, "token");

        assertThat(expected).isEqualTo(actual);
        verify(multipleReferenceService, times(1)).addLegalRepToMultiple(any(), any());
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
        when(this.featureToggleService.isMul2Enabled()).thenReturn(true);

        CCDCallbackResponse actual = ccdCaseAssignment.applyNoc(callbackRequest, "token");

        assertThat(expected).isEqualTo(actual);
        verify(multipleReferenceService, never()).addLegalRepToMultiple(any(), any());
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
        when(this.featureToggleService.isMul2Enabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn("adminToken");
        when(nocCcdService.getLatestAuditEventByName(any(), any(), any())).thenReturn(Optional.empty());

        CCDCallbackResponse actual = ccdCaseAssignment.applyNoc(callbackRequest, "token");

        assertThat(expected).isEqualTo(actual);
        verify(multipleReferenceService, never()).addLegalRepToMultiple(any(), any());
    }

    private AuditEvent getAuditEvent() {
        return AuditEvent.builder()
                .userId("123")
                .build();
    }
}
