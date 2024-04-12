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
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CcdCaseAssignmentTest {
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private AdminUserService adminUserService;

    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;

    @InjectMocks
    private CcdCaseAssignment ccdCaseAssignment;

    private CallbackRequest callbackRequest;
    private CaseAssignmentUserRolesRequest rolesRequest;

    @BeforeEach
    void setUp() {
        CaseData caseData = new CaseData();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId("1234123412341234");
        callbackRequest = new CallbackRequest();
        callbackRequest.setCaseDetails(caseDetails);
        when(adminUserService.getAdminUserToken()).thenReturn("adminUserToken");
        when(serviceAuthTokenGenerator.generate()).thenReturn("token");

        rolesRequest = ccdCaseAssignment.getCaseAssignmentRequest(
                Long.valueOf("1234123412341234"), UUID.randomUUID().toString(), "AA11BB", "[CREATOR]");
    }

    @Test
    void shouldCallCaseAssignmentNoc() {
        CCDCallbackResponse expected = new CCDCallbackResponse(callbackRequest.getCaseDetails().getCaseData());

        when(restTemplate
            .exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(CCDCallbackResponse.class))).thenReturn(ResponseEntity.ok(expected));

        CCDCallbackResponse actual = ccdCaseAssignment.applyNoc(callbackRequest, "token");

        assertThat(expected).isEqualTo(actual);
    }

    @Test
    void removeUserRoles() {
        CaseAssignmentUserRolesResponse expected = CaseAssignmentUserRolesResponse.builder()
                .statusMessage("Case-User-Role assignments removed successfully")
                .build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class),
                    eq(CaseAssignmentUserRolesResponse.class)))
                .thenReturn(ResponseEntity.ok(expected));
        assertThatNoException().isThrownBy(() -> ccdCaseAssignment.removeCaseUserRoles(rolesRequest));
    }

    @Test
    void removeUserRolesException() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.DELETE), any(HttpEntity.class),
                eq(CaseAssignmentUserRolesResponse.class)))
                .thenThrow(new RestClientResponseException("Unauthorised S2S service", 401,
                        "Unauthorized", null, null, null));
        assertThrows(RestClientResponseException.class, () -> ccdCaseAssignment.removeCaseUserRoles(rolesRequest),
                "Unauthorised S2S service");
    }

    @Test
    void addUserRoles() {
        CaseAssignmentUserRolesResponse expected = CaseAssignmentUserRolesResponse.builder()
                .statusMessage("Case-User-Role assignments created successfully")
                .build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                    eq(CaseAssignmentUserRolesResponse.class)))
                .thenReturn(ResponseEntity.ok(expected));
        assertThatNoException().isThrownBy(() -> ccdCaseAssignment.addCaseUserRoles(rolesRequest));
    }

    @Test
    void addUserRolesException() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class),
                eq(CaseAssignmentUserRolesResponse.class)))
                .thenThrow(new RestClientResponseException("Unauthorised S2S service", 401,
                        "Unauthorized", null, null, null));
        assertThrows(RestClientResponseException.class, () -> ccdCaseAssignment.addCaseUserRoles(rolesRequest),
                "Unauthorised S2S service");
    }
}