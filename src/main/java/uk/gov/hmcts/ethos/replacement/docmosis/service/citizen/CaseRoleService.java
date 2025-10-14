package uk.gov.hmcts.ethos.replacement.docmosis.service.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants;
import uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CallbacksRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.citizen.utils.CaseRoleServiceUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.HttpUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ecm.common.client.CcdClient.EXPERIMENTAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.RoleConstants.ROLE_MODIFICATION_TYPE_REVOKE;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.HttpUtils.buildHeaders;

/**
 * Provides services for modifying case roles.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseRoleService {

    private final AdminUserService adminUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final RestTemplate restTemplate;

    @Value("${ccd.data-store-api-url}")
    private String ccdDataStoreApiBaseUrl;

    /**
     * Fetches the case user assignment data for a given case ID from the CCD Data Store API.
     *
     *  <p>
     *      This method constructs a case access URL using the provided case ID and sends a GET request
     *      to the CCD Data Store API to retrieve user assignment information associated with that case.
     *      It uses an admin authentication token and a service authorization token for secure access.
     *  </p>
     *
     *  @param caseId the unique identifier of the case whose user assignments are to be retrieved
     *  @return a {@link CaseUserAssignmentData} object containing the user assignments for the specified case
     *  @throws GenericServiceException if there is an error during the API request or response processing
     *
     *      <p>
     *          <b>Implementation details:</b>
     *      </p>
     *      <ul>
     *          <li>Builds the target URI using {@link HttpUtils#buildCaseAccessUrl(String, String)}.</li>
     *          <li>Retrieves the admin authentication token via {@link AdminUserService#getAdminUserToken()}.</li>
     *          <li>Generates a service-to-service authorization token via {@code authTokenGenerator.generate()}.</li>
     *          <li>Sets both tokens in the HTTP headers, along with an {@code EXPERIMENTAL} flag.</li>
     *          <li>Executes an HTTP GET request using RestTemplate exchange method.</li>
     *      </ul>
     *
     *      <p>
     *          This method assumes that both admin and service tokens are valid and that the CCD Data Store API is
     *          accessible at the configured base URL.
     *      </p>
     */
    public CaseUserAssignmentData fetchCaseUserAssignmentsByCaseId(String caseId) throws GenericServiceException {
        String uri = HttpUtils.buildCaseAccessUrl(ccdDataStoreApiBaseUrl, caseId);
        String authToken = adminUserService.getAdminUserToken();
        HttpHeaders httpHeaders = buildHeaders(authToken, authTokenGenerator.generate());
        httpHeaders.add(EXPERIMENTAL, "true");
        HttpEntity<String> request = new HttpEntity<>(httpHeaders);
        return restTemplate.exchange(uri, HttpMethod.GET, request, CaseUserAssignmentData.class).getBody();
    }

    /**
     * Retrieves a list of {@link CaseUserAssignment} entries associated with a specific case role
     * for a given case.
     *
     * <p>This method fetches all user assignments for the provided case by calling
     * {@link #fetchCaseUserAssignmentsByCaseId(String)}, then filters the results to include
     * only those assignments matching the specified {@code caseRole}. It is typically used
     * to determine which users are assigned to a given case under a particular role
     * (e.g., respondent solicitor, claimant representative, etc.).</p>
     *
     * <p><b>Processing steps:</b></p>
     * <ul>
     *   <li>Fetches the complete list of case-user assignments for the given case ID.</li>
     *   <li>Validates that the retrieved data and assignment list are not empty.</li>
     *   <li>Iterates over all case-user assignments and selects only those whose role matches {@code caseRole}.</li>
     *   <li>Returns a list of matching {@link CaseUserAssignment} objects.</li>
     * </ul>
     *
     * @param caseRole     the case role (e.g., "[DEFENDANT]", "[CLAIMANT]") to filter user assignments by
     * @param caseDetails  the {@link CaseDetails} object representing the case whose assignments are to be searched
     * @return a list of {@link CaseUserAssignment} objects matching the specified case role;
     *         may be empty if no users are assigned to that role
     *
     * @throws GenericServiceException if an error occurs while fetching case-user assignment data
     * @throws CallbacksRuntimeException if:
     *      <ul>
     *          <li>the fetched case-user assignment data is null or empty;</li>
     *          <li>no case-user roles are found for the given case ID.</li>
     *      </ul>
     *
     * @see #fetchCaseUserAssignmentsByCaseId(String)
     * @see CaseUserAssignment
     * @see CaseUserAssignmentData
     * @see CaseDetails
     */
    public List<CaseUserAssignment> findCaseUserAssignmentsByRoleAndCase(String caseRole, CaseDetails caseDetails)
            throws GenericServiceException {
        CaseUserAssignmentData caseUserAssignmentData =
                fetchCaseUserAssignmentsByCaseId(caseDetails.getId().toString());
        if (ObjectUtils.isEmpty(caseUserAssignmentData)
                || CollectionUtils.isEmpty(caseUserAssignmentData.getCaseUserAssignments())) {
            throw new CallbacksRuntimeException(new Exception(
                    String.format(NOCConstants.EXCEPTION_CASE_USER_ROLES_NOT_FOUND, caseDetails.getId())));
        }
        List<CaseUserAssignment> selectedCaseUserAssignments = new ArrayList<>();
        for (CaseUserAssignment caseAssignedUserRole : caseUserAssignmentData.getCaseUserAssignments()) {
            if (caseRole.equals(caseAssignedUserRole.getCaseRole())) {
                selectedCaseUserAssignments.add(caseAssignedUserRole);
            }
        }
        return selectedCaseUserAssignments;
    }

    public void revokeCaseUserRole(CaseDetails caseDetails, String role) throws GenericServiceException {
        List<CaseUserAssignment> caseUserAssignments = findCaseUserAssignmentsByRoleAndCase(
                role, caseDetails);
        if (CollectionUtils.isEmpty(caseUserAssignments)) {
            throw new CallbacksRuntimeException(new Exception(
                    String.format(NOCConstants.EXCEPTION_CASE_USER_ROLES_NOT_FOUND, caseDetails.getId())));
        }
        for (CaseUserAssignment caseUserAssignment : caseUserAssignments) {
            CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest = CaseRoleServiceUtils
                    .createCaseUserRoleRequest(
                            caseUserAssignment.getUserId(),
                            caseDetails,
                            role
                    );
            HttpMethod httpMethod = HttpUtils.getHttpMethodByCaseUserRoleModificationType(
                    ROLE_MODIFICATION_TYPE_REVOKE);
            restCallToModifyUserCaseRoles(caseAssignmentUserRolesRequest, httpMethod);
        }
    }

    private void restCallToModifyUserCaseRoles(
            CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest, HttpMethod httpMethod)
            throws GenericServiceException {
        try {
            String adminToken = adminUserService.getAdminUserToken();
            HttpEntity<CaseAssignmentUserRolesRequest> requestEntity =
                    new HttpEntity<>(caseAssignmentUserRolesRequest,
                            buildHeaders(adminToken, authTokenGenerator.generate()));
            restTemplate.exchange(ccdDataStoreApiBaseUrl + HttpConstants.CASE_USERS_API_URL,
                    httpMethod,
                    requestEntity,
                    CaseAssignmentUserRolesResponse.class);
        } catch (RestClientResponseException | GenericServiceException exception) {
            log.info("Error from CCD - {}", exception.getMessage());
            throw exception;
        }
    }
}
