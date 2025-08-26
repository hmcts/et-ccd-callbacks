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
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserWithOrganisationRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserWithOrganisationRolesRequest;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
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
    public static final String ERROR_FROM_CCD = "Error from CCD - {}";
    public static final String CASE_USERS = "/case-users";

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
                response.getStatusCode().value(), callback.getCaseDetails().getCaseId());

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

    public void removeCaseUserRoles(
            CaseAssignmentUserWithOrganisationRolesRequest caseAssignmentUserWithOrganisationRolesRequest)
            throws IOException {
        String userToken = adminUserService.getAdminUserToken();
        HttpEntity<CaseAssignmentUserWithOrganisationRolesRequest> requestEntity =
                new HttpEntity<>(caseAssignmentUserWithOrganisationRolesRequest, ccdClient.buildHeaders(userToken));
        ResponseEntity<CaseAssignmentUserRolesResponse> response;
        try {
            response = restTemplate.exchange(
                    ccdDataStoreUrl + CASE_USERS,
                HttpMethod.DELETE,
                requestEntity,
                CaseAssignmentUserRolesResponse.class);
        } catch (RestClientResponseException exception) {
            log.info(ERROR_FROM_CCD, exception.getMessage());
            throw exception;
        }

        log.info("Remove case user roles. Http status received from CCD API; {}", response.getStatusCode().value());
    }

    public void addCaseUserRoles(
            CaseAssignmentUserWithOrganisationRolesRequest caseAssignmentUserWithOrganisationRolesRequest)
            throws IOException {
        String userToken = adminUserService.getAdminUserToken();
        HttpEntity<CaseAssignmentUserWithOrganisationRolesRequest> requestEntity =
                new HttpEntity<>(caseAssignmentUserWithOrganisationRolesRequest, ccdClient.buildHeaders(userToken));
        ResponseEntity<CaseAssignmentUserRolesResponse> response;
        try {
            response = restTemplate.exchange(
                    ccdDataStoreUrl + CASE_USERS,
                HttpMethod.POST,
                requestEntity,
                    CaseAssignmentUserRolesResponse.class);
        } catch (RestClientResponseException exception) {
            log.info(ERROR_FROM_CCD, exception.getMessage());
            throw exception;
        }

        log.info("Add case user roles. Http status received from CCD API; {}", response.getStatusCode().value());
    }

    public CaseAssignmentUserWithOrganisationRolesRequest getCaseAssignmentRequest(
            Long caseId, String userId, String orgId, String role) {
        return CaseAssignmentUserWithOrganisationRolesRequest.builder()
                .caseAssignmentUserRoles(
                        List.of(getCaseAssignmentUserRole(caseId, orgId, role, userId))
                ).build();
    }

    private CaseAssignmentUserWithOrganisationRole getCaseAssignmentUserRole(Long caseId,
                                                                             String orgId,
                                                                             String role,
                                                                             String userId) {
        return CaseAssignmentUserWithOrganisationRole.builder()
                .organisationId(orgId)
                .caseDataId(String.valueOf(caseId))
                .caseRole(role)
                .userId(userId)
                .build();
    }

    /**
     * Retrieve the users and roles assigned to a given an ID.
     * @param caseId the case ID
     * @return the response from the CCD API which contains a list of users and roles
     * @throws IOException if there is an error with the request
     */
    public CaseUserAssignmentData getCaseUserRoles(String caseId) throws IOException {
        String authToken = adminUserService.getAdminUserToken();
        return ccdClient.retrieveCaseAssignments(authToken, caseId);
    }

    /**
     * Add a user role to a case.
     * @param caseAssignmentUserRolesRequest the request containing the user role to add
     * @throws IOException if there is an error with the request
     */
    public void addCaseUserRole(CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest) throws IOException {
        String userToken = adminUserService.getAdminUserToken();
        HttpEntity<CaseAssignmentUserRolesRequest> requestEntity =
                new HttpEntity<>(caseAssignmentUserRolesRequest, ccdClient.buildHeaders(userToken));
        ResponseEntity<CaseAssignmentUserRolesResponse> response;
        try {
            response = restTemplate.exchange(
                    ccdDataStoreUrl + CASE_USERS,
                    HttpMethod.POST,
                    requestEntity,
                    CaseAssignmentUserRolesResponse.class);
        } catch (RestClientResponseException exception) {
            log.info(ERROR_FROM_CCD, exception.getMessage());
            throw exception;
        }

        log.info("Add case user roles. Http status received from CCD API; {}", response.getStatusCode().value());
    }
}