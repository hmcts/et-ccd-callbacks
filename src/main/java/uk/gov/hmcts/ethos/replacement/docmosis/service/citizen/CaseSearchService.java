package uk.gov.hmcts.ethos.replacement.docmosis.service.citizen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignedUserRolesResponse;
import uk.gov.hmcts.ecm.common.model.ccd.SearchCaseAssignedUserRolesRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.URI_CCD_DATA_STORE_SEARCH_API;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.citizen.utils.CitizenCaseSearchServiceUtils.filterCaseDetailsByUserRole;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.HttpUtils.buildHeaders;

/**
 * Provides services for searching case(s) with different criteria.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseSearchService {

    // Exception messages
    private static final String EXCEPTION_CASE_DETAILS_NOT_FOUND =
            "Case details not found for the given submission reference %s.";
    private static final String EXCEPTION_UNABLE_TO_RETRIEVE_CASE_CASE_DETAILS_WITH_CCD_API =
            "Unable to retrieve case details with CCD API for the given user id(s) %s and submission reference(s) %s.";

    @Value("${ccd.data-store-api-url}")
    private String ccdDataStoreApiBaseUrl;

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final UserIdamService userIdamService;
    private final RestTemplate restTemplate;

    public CaseDetails findAuthorizedCaseBySubmissionReferenceAndRole(String authorization,
                                                                      String caseSubmissionReference,
                                                                      String userRole) throws GenericServiceException {
        if (StringUtils.isBlank(authorization)
                || StringUtils.isBlank(caseSubmissionReference)
                || StringUtils.isBlank(userRole)) {
            return null;
        }
        CaseDetails caseDetails =
                coreCaseDataApi.getCase(authorization, authTokenGenerator.generate(), caseSubmissionReference);
        if (ObjectUtils.isEmpty(caseDetails)) {
            throw new GenericServiceException(String.format(EXCEPTION_CASE_DETAILS_NOT_FOUND, caseSubmissionReference),
                    new Exception(String.format(EXCEPTION_CASE_DETAILS_NOT_FOUND, caseSubmissionReference)),
                    String.format(EXCEPTION_CASE_DETAILS_NOT_FOUND, caseSubmissionReference),
                    caseSubmissionReference,
                    "CaseSearchService",
                    "findCaseBySubmissionReferenceAndUserRole");
        }
        List<CaseDetails> caseDetailsListByCaseUserRole =
                filterCasesByUserRoleFromCcd(List.of(caseDetails), authorization, userRole);
        return CollectionUtils.isNotEmpty(caseDetailsListByCaseUserRole)
                ? caseDetailsListByCaseUserRole.getFirst()
                : null;
    }

    /**
     * Retrieves and filters the given list of {@link CaseDetails} for cases associated
     * with the specified user role, using role assignments retrieved from CCD.
     * <p>
     * The method fetches case-user-role assignments from CCD based on the provided
     * authorization, filters the input case list accordingly, and removes documents
     * not permitted for the given user role.
     * </p>
     *
     * @param caseDetailsList  the list of cases to filter
     * @param authorization    the user's authorization token
     * @param caseUserRole     the case user role to filter by
     * @return a list of {@link CaseDetails} matching the specified user role
     * @throws GenericServiceException if CCD role retrieval or filtering fails
     */
    private List<CaseDetails> filterCasesByUserRoleFromCcd(
            List<CaseDetails> caseDetailsList, String authorization, String caseUserRole)
            throws GenericServiceException {
        List<CaseDetails> caseDetailsListByRole;
        try {
            CaseAssignedUserRolesResponse caseAssignedUserRolesResponse =
                    fetchCaseUserRolesFromCcd(authorization, caseDetailsList);
            caseDetailsListByRole = filterCaseDetailsByUserRole(caseDetailsList,
                            caseAssignedUserRolesResponse.getCaseAssignedUserRoles(),
                            caseUserRole);
            DocumentUtils.filterCasesDocumentsByCaseUserRole(caseDetailsListByRole, caseUserRole);
        } catch (GenericServiceException e) {
            throw new GenericServiceException(e.getMessage(),
                    e,
                    e.getMessage(),
                    "List of cases",
                    "CaseSearchService",
                    "getCasesByCaseDetailsListAuthorizationAndCaseUserRole");
        }
        return caseDetailsListByRole;
    }

    /**
     * Retrieves case-user-role assignments for the specified cases from the CCD Data Store API.
     * <p>
     * This method builds a list of case IDs from the provided {@link CaseDetails},
     * obtains the current user's ID from IDAM, and queries CCD's
     * "search case assigned user roles" endpoint to fetch role assignments.
     * </p>
     *
     * @param authorisation     the authorisation token of the current user
     * @param caseDetailsList   the list of case details whose user role assignments are to be retrieved
     * @return a {@link CaseAssignedUserRolesResponse} containing role assignments for the given cases and user
     * @throws GenericServiceException if the CCD API call fails or the response cannot be retrieved
     */
    public CaseAssignedUserRolesResponse fetchCaseUserRolesFromCcd(String authorisation,
                                                                   List<CaseDetails> caseDetailsList)
            throws GenericServiceException {
        CaseAssignedUserRolesResponse caseAssignedUserRolesResponse = CaseAssignedUserRolesResponse.builder().build();
        if (StringUtils.isBlank(authorisation) || CollectionUtils.isEmpty(caseDetailsList)) {
            return caseAssignedUserRolesResponse;
        }
        List<String> caseIds = new ArrayList<>();
        for (CaseDetails caseDetails : caseDetailsList) {
            if (ObjectUtils.isNotEmpty(caseDetails) && ObjectUtils.isNotEmpty(caseDetails.getId())) {
                caseIds.add(caseDetails.getId().toString());
            }
        }
        if (CollectionUtils.isEmpty(caseIds)) {
            return caseAssignedUserRolesResponse;
        }
        UserDetails userDetails = userIdamService.getUserDetails(authorisation);
        if (ObjectUtils.isEmpty(userDetails)) {
            return caseAssignedUserRolesResponse;
        }
        SearchCaseAssignedUserRolesRequest searchCaseAssignedUserRolesRequest = SearchCaseAssignedUserRolesRequest
                .builder()
                .caseIds(caseIds)
                .userIds(List.of(userDetails.getUid()))
                .build();
        try {
            HttpEntity<SearchCaseAssignedUserRolesRequest> requestHttpEntity =
                    new HttpEntity<>(searchCaseAssignedUserRolesRequest,
                            buildHeaders(authorisation, authTokenGenerator.generate()));
            caseAssignedUserRolesResponse = restTemplate
                    .postForObject(ccdDataStoreApiBaseUrl + URI_CCD_DATA_STORE_SEARCH_API,
                            requestHttpEntity,
                            CaseAssignedUserRolesResponse.class);
        } catch (RestClientResponseException | GenericServiceException exception) {
            String message = String.format(
                    EXCEPTION_UNABLE_TO_RETRIEVE_CASE_CASE_DETAILS_WITH_CCD_API,
                    userDetails.getUid(),
                    caseIds);
            throw new GenericServiceException(
                    message,
                    exception,
                    message,
                    caseIds.toString(),
                    "CaseSearchService",
                    "getCaseUserRolesByCaseAndUserIdsCcd");
        }
        return caseAssignedUserRolesResponse;
    }
}
