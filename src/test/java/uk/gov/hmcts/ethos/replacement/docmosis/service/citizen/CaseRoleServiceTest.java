package uk.gov.hmcts.ethos.replacement.docmosis.service.citizen;

import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CallbacksRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.RoleConstants.CREATOR;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TEST_CASE_ID_LONG;

@EqualsAndHashCode
@ExtendWith(MockitoExtension.class)
public class CaseRoleServiceTest {

    private static final String CCD_API_URL_PARAMETER_NAME = "ccdDataStoreApiBaseUrl";
    private static final String CCD_API_URL_PARAMETER_TEST_VALUE = "https://test.url.com";
    public static final String CASE_USER_ROLE_CLAIMANT_SOLICITOR = "[CLAIMANTSOLICITOR]";
    private static final String DUMMY_AUTHORISATION_TOKEN = "dummy_authorisation_token";
    private static final String CASE_ID = "1646225213651590";
    private static final String USER_ID = "1234564789";

    @Mock
    private AdminUserService adminUserService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private RestTemplate restTemplate;

    private CaseRoleService caseRoleService;

    @BeforeEach
    void setup() {
        caseRoleService = new CaseRoleService(adminUserService, authTokenGenerator, restTemplate);
    }

    @Test
    @SneakyThrows
    void theFetchCaseUserAssignmentsByCaseId() {
        ReflectionTestUtils.setField(caseRoleService,
                CCD_API_URL_PARAMETER_NAME,
                CCD_API_URL_PARAMETER_TEST_VALUE);
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder()
                .caseUserAssignments(List.of(CaseUserAssignment.builder()
                        .caseId(CASE_ID)
                        .userId(USER_ID)
                        .caseRole(CASE_USER_ROLE_CLAIMANT_SOLICITOR)
                        .build()))
                .build();
        when(adminUserService.getAdminUserToken()).thenReturn(DUMMY_AUTHORISATION_TOKEN);
        when(restTemplate.exchange(anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(CaseUserAssignmentData.class))).thenReturn(
                        new ResponseEntity<>(caseUserAssignmentData, HttpStatus.OK));
        CaseUserAssignmentData actualCaseUserAssignmentData =
                caseRoleService.fetchCaseUserAssignmentsByCaseId(CASE_ID);
        assertThat(actualCaseUserAssignmentData.getCaseUserAssignments().getFirst().getUserId()).isEqualTo(USER_ID);
    }

    @Test
    @SneakyThrows
    void theFindCaseUserAssignmentsByRoleAndCase() {
        when(adminUserService.getAdminUserToken()).thenReturn(DUMMY_AUTHORISATION_TOKEN);
        when(restTemplate.exchange(anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(CaseUserAssignmentData.class))).thenReturn(
                        new ResponseEntity<>(null, HttpStatus.OK));
        CaseDetails caseDetails = CaseDetails.builder().id(TEST_CASE_ID_LONG).build();
        CallbacksRuntimeException caseRoleException = assertThrows(CallbacksRuntimeException.class, () ->
                caseRoleService.findCaseUserAssignmentsByRoleAndCase(
                        CASE_USER_ROLE_CLAIMANT_SOLICITOR, caseDetails));
        assertThat(caseRoleException.getMessage())
                .isEqualTo("java.lang.Exception: Case user roles not found for caseId: 123");
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder()
                .caseUserAssignments(List.of(CaseUserAssignment.builder()
                        .caseId(CASE_ID)
                        .userId(USER_ID)
                        .caseRole(CASE_USER_ROLE_CLAIMANT_SOLICITOR)
                        .build()))
                .build();
        when(restTemplate.exchange(anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(CaseUserAssignmentData.class))).thenReturn(
                        new ResponseEntity<>(caseUserAssignmentData, HttpStatus.OK));
        assertThat(caseRoleService.findCaseUserAssignmentsByRoleAndCase(CREATOR,
                CaseDetails.builder().id(TEST_CASE_ID_LONG).build())).isNullOrEmpty();
        assertThat(caseRoleService.findCaseUserAssignmentsByRoleAndCase(CASE_USER_ROLE_CLAIMANT_SOLICITOR,
                CaseDetails.builder().id(TEST_CASE_ID_LONG).build())).isNotNull();
    }
}
