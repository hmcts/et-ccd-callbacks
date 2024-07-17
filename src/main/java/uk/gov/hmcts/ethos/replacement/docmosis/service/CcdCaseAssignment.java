package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleReferenceService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.NocRespondentRepresentativeService.NOC_REQUEST;

@Slf4j
@Service
public class CcdCaseAssignment {

    private static final String LEGAL_REP_ID_NOT_FOUND_ERROR =
            "Add Respondent Representative to Multiple failed. Legal Rep Id not found for case {}";

    private final RestTemplate restTemplate;
    private final CcdClient ccdClient;
    private final FeatureToggleService featureToggleService;
    private final AdminUserService adminUserService;
    private final NocCcdService nocCcdService;
    private final MultipleReferenceService multipleReferenceService;

    private final String aacUrl;
    private final String applyNocAssignmentsApiPath;
    private final String ccdDataStoreUrl;

    public CcdCaseAssignment(RestTemplate restTemplate,
                             CcdClient ccdClient,
                             FeatureToggleService featureToggleService,
                             AdminUserService adminUserService,
                             NocCcdService nocCcdService,
                             MultipleReferenceService multipleReferenceService,
                             @Value("${assign_case_access_api_url}") String aacUrl,
                             @Value("${apply_noc_access_api_assignments_path}") String applyNocAssignmentsApiPath,
                             @Value("${ccd.data-store-api-url}") String ccdDataStoreUrl) {
        this.restTemplate = restTemplate;
        this.ccdClient = ccdClient;
        this.featureToggleService = featureToggleService;
        this.adminUserService = adminUserService;
        this.nocCcdService = nocCcdService;
        this.multipleReferenceService = multipleReferenceService;
        this.aacUrl = aacUrl;
        this.applyNocAssignmentsApiPath = applyNocAssignmentsApiPath;
        this.ccdDataStoreUrl = ccdDataStoreUrl;
    }

    public CCDCallbackResponse applyNoc(final CallbackRequest callback, String userToken) throws IOException {
        requireNonNull(callback, "callback must not be null");

        HttpEntity<CallbackRequest> requestEntity =
                new HttpEntity<>(
                        callback,
                        ccdClient.buildHeaders(userToken)
                );

        ResponseEntity<CCDCallbackResponse> response;
        try {
            response = restTemplate
                    .exchange(
                            aacUrl + applyNocAssignmentsApiPath,
                            HttpMethod.POST,
                            requestEntity,
                            CCDCallbackResponse.class
                    );

        } catch (RestClientResponseException exception) {
            log.info("Error from ccd - {}", exception.getMessage());
            throw exception;
        }

        log.info("Apply NoC. Http status received from AAC API; {} for case {}",
                response.getStatusCodeValue(), callback.getCaseDetails().getCaseId());

        if (featureToggleService.isMul2Enabled()) {
            addRespondentRepresentativeToMultiple(callback.getCaseDetails());
        }
        return response.getBody();
    }

    public CCDCallbackResponse applyNocAsAdmin(CallbackRequest callbackRequest) throws IOException {
        return this.applyNoc(callbackRequest, adminUserService.getAdminUserToken());
    }

    private void addRespondentRepresentativeToMultiple(CaseDetails caseDetails) throws IOException {
        if (!YES.equals(caseDetails.getCaseData().getMultipleFlag())) {
            return;
        }

        String adminUserToken = adminUserService.getAdminUserToken();
        String caseId = caseDetails.getCaseId();
        String userToAddId = getEventTriggerUserId(adminUserToken, caseId);

        if (userToAddId.isEmpty()) {
            log.info(LEGAL_REP_ID_NOT_FOUND_ERROR, caseId);
            return;
        }

        multipleReferenceService.addLegalRepToMultiple(caseDetails, userToAddId);
    }

    private String getEventTriggerUserId(String adminUserToken, String caseId) throws IOException {
        Optional<AuditEvent> auditEvent = nocCcdService.getLatestAuditEventByName(adminUserToken, caseId, NOC_REQUEST);

        if (auditEvent.isPresent()) {
            return auditEvent.get().getUserId();
        }
        return "";
    }

    public void removeCaseUserRoles(CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest) throws IOException {
        String userToken = adminUserService.getAdminUserToken();
        HttpEntity<CaseAssignmentUserRolesRequest> requestEntity =
                new HttpEntity<>(caseAssignmentUserRolesRequest, ccdClient.buildHeaders(userToken));
        ResponseEntity<CaseAssignmentUserRolesResponse> response;
        try {
            response = restTemplate.exchange(
                ccdDataStoreUrl + "/case-users",
                HttpMethod.DELETE,
                requestEntity,
                CaseAssignmentUserRolesResponse.class);
        } catch (RestClientResponseException exception) {
            log.info("Error from CCD - {}", exception.getMessage());
            throw exception;
        }

        log.info("Remove case user roles. Http status received from CCD API; {}",
            response.getStatusCodeValue());
    }

    public void addCaseUserRoles(CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest) throws IOException {
        String userToken = adminUserService.getAdminUserToken();
        HttpEntity<CaseAssignmentUserRolesRequest> requestEntity =
                new HttpEntity<>(caseAssignmentUserRolesRequest, ccdClient.buildHeaders(userToken));
        ResponseEntity<CaseAssignmentUserRolesResponse> response;
        try {
            response = restTemplate.exchange(
                ccdDataStoreUrl + "/case-users",
                HttpMethod.POST,
                requestEntity,
                    CaseAssignmentUserRolesResponse.class);
        } catch (RestClientResponseException exception) {
            log.info("Error from CCD - {}", exception.getMessage());
            throw exception;
        }

        log.info("Add case user roles. Http status received from CCD API; {}",
            response.getStatusCodeValue());
    }

    public CaseAssignmentUserRolesRequest getCaseAssignmentRequest(Long caseId, String userId, String orgId,
                                                                   String role) {
        return CaseAssignmentUserRolesRequest.builder()
                .caseAssignmentUserRolesWithOrganisation(
                        List.of(getCaseAssignmentUserRole(caseId, orgId, role, userId))
                ).build();
    }

    private CaseAssignmentUserRoleWithOrganisation getCaseAssignmentUserRole(Long caseId, String orgId,
                                                                             String role, String userId) {
        return CaseAssignmentUserRoleWithOrganisation.builder()
                .organisationId(orgId)
                .caseDataId(String.valueOf(caseId))
                .caseRole(role)
                .userId(userId)
                .build();
    }
}