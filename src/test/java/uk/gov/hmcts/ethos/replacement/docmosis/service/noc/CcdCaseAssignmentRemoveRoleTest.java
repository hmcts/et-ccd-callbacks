package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleReferenceService;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CcdCaseAssignmentRemoveRoleTest {

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private CcdClient ccdClient;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private NocCcdService nocCcdService;
    @Mock
    private MultipleReferenceService multipleReferenceService;

    private CcdCaseAssignment ccdCaseAssignment;
    private CaseAssignmentUserRolesRequest request;

    @BeforeEach
    void setUp() throws IOException {
        ccdCaseAssignment = new CcdCaseAssignment(
                restTemplate,
                ccdClient,
                featureToggleService,
                adminUserService,
                nocCcdService,
                multipleReferenceService,
                "http://aac",
                "/apply-noc",
                "http://ccd");
        request = CaseAssignmentUserRolesRequest.builder()
                .caseAssignmentUserRoles(List.of(CaseAssignmentUserRole.builder()
                        .caseDataId("1234567890123456")
                        .userId("user-id")
                        .caseRole("[DEFENDANT]")
                        .build()))
                .build();
        when(adminUserService.getAdminUserToken()).thenReturn("admin-token");
        when(ccdClient.buildHeaders("admin-token")).thenReturn(new HttpHeaders());
    }

    @Test
    void removesCaseUserRole() {
        CaseAssignmentUserRolesResponse response = CaseAssignmentUserRolesResponse.builder()
                .statusMessage("Role removed")
                .build();
        when(restTemplate.exchange(
                eq("http://ccd/case-users"),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(CaseAssignmentUserRolesResponse.class)))
                .thenReturn(ResponseEntity.ok(response));

        assertThatNoException().isThrownBy(() -> ccdCaseAssignment.removeCaseUserRole(request));
    }

    @Test
    void propagatesCcdErrorWhenRemovingCaseUserRole() {
        RestClientResponseException exception = new RestClientResponseException(
                "Unauthorized", 401, "Unauthorized", null, null, null);
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(CaseAssignmentUserRolesResponse.class)))
                .thenThrow(exception);

        assertThatThrownBy(() -> ccdCaseAssignment.removeCaseUserRole(request))
                .isSameAs(exception);
    }
}
