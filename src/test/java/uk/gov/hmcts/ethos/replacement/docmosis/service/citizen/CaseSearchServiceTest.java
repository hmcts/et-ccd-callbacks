package uk.gov.hmcts.ethos.replacement.docmosis.service.citizen;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignedUserRolesResponse;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.RoleConstants.DEFENDANT;

@ExtendWith(SpringExtension.class)
public class CaseSearchServiceTest {

    private static final String VARIABLE_CCD_DATA_STORE_API_BASE_URL_NAME = "ccdDataStoreApiBaseUrl";
    private static final String VARIABLE_CCD_DATA_STORE_API_BASE_URL_VALUE = "http://localhost:4452";
    private static final String CCD_DATA_STORE_API_SEARCH_API_URL = "/case-users/search";
    private static final String TEST_AUTHORIZATION = "Bearer testAuthorization";
    private static final String TEST_USER_ID = "testUserId";
    private static final String TEST_CASE_SUBMISSION_REFERENCE = "123";
    private static final String TEST_USER_ROLE = "DEFENDANT";
    private static final String TEST_EXCEPTION_UNABLE_TO_RETRIEVE_CASE_CASE_DETAILS_WITH_CCD_API =
            "Unable to retrieve case details with CCD API for the given user id(s) %s and submission reference(s) %s.";
    private static final String TEST_EXPECTED_EXCEPTION_MESSAGE_CASE_NOT_FOUND_ROLE =
            "Unable to retrieve case details with CCD API for the given user id(s) "
                    + "testUserId and submission reference(s) [123].";
    private static final String TEST_EXPECTED_EXCEPTION_MESSAGE_CASE_NOT_FOUND =
            "Case details not found for the given submission reference 123.";

    private CaseSearchService caseSearchService;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private UserIdamService userIdamService;
    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
        caseSearchService = new CaseSearchService(
                coreCaseDataApi, authTokenGenerator, userIdamService, restTemplate);
        ReflectionTestUtils.setField(caseSearchService,
                VARIABLE_CCD_DATA_STORE_API_BASE_URL_NAME, VARIABLE_CCD_DATA_STORE_API_BASE_URL_VALUE);
    }

    @Test
    @SneakyThrows
    void theFetchCaseUserRolesFromCcd() {
        CaseDetails caseDetails = CaseDetails.builder().build();
        CaseAssignedUserRolesResponse emptyCaseAssignedUserRolesResponse =
                CaseAssignedUserRolesResponse.builder().build();
        // when authorization is null
        assertThat(caseSearchService.fetchCaseUserRolesFromCcd(null, List.of(caseDetails))).isEqualTo(
                emptyCaseAssignedUserRolesResponse);

        // when caseDetailsList is null
        assertThat(caseSearchService.fetchCaseUserRolesFromCcd(TEST_AUTHORIZATION, null)).isEqualTo(
                emptyCaseAssignedUserRolesResponse);

        // when caseDetailsList case id is empty
        assertThat(caseSearchService.fetchCaseUserRolesFromCcd(TEST_AUTHORIZATION, List.of(caseDetails))).isEqualTo(
                emptyCaseAssignedUserRolesResponse);

        // when caseDetailsList case id is not empty but not able to find user details by authorization
        caseDetails.setId(123L);
        assertThat(caseSearchService.fetchCaseUserRolesFromCcd(TEST_AUTHORIZATION, List.of(caseDetails))).isEqualTo(
                emptyCaseAssignedUserRolesResponse);

        // when caseDetailsList case id is not empty and user details found but unable to post for object
        UserDetails userDetails = new UserDetails();
        userDetails.setUid(TEST_USER_ID);
        when(userIdamService.getUserDetails(TEST_AUTHORIZATION)).thenReturn(userDetails);
        String exceptionMessage = String.format(TEST_EXCEPTION_UNABLE_TO_RETRIEVE_CASE_CASE_DETAILS_WITH_CCD_API,
                TEST_USER_ID, TEST_CASE_SUBMISSION_REFERENCE);
        when(restTemplate.postForObject(
                eq(VARIABLE_CCD_DATA_STORE_API_BASE_URL_VALUE + CCD_DATA_STORE_API_SEARCH_API_URL),
                any(HttpEntity.class),
                eq(CaseAssignedUserRolesResponse.class))).thenThrow(
                        new RestClientResponseException(
                                exceptionMessage, 400, "Bad Request", null, null, null));
        GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                () -> caseSearchService.fetchCaseUserRolesFromCcd(TEST_AUTHORIZATION, List.of(caseDetails)));
        assertThat(genericServiceException.getMessage()).isEqualTo(TEST_EXPECTED_EXCEPTION_MESSAGE_CASE_NOT_FOUND_ROLE);

        // when caseDetailsList case id is not empty and user details found and able to post for object
        CaseAssignedUserRolesResponse caseAssignedUserRolesResponse =
                CaseAssignedUserRolesResponse.builder().build();
        when(restTemplate.postForObject(
                eq(VARIABLE_CCD_DATA_STORE_API_BASE_URL_VALUE + CCD_DATA_STORE_API_SEARCH_API_URL),
                any(HttpEntity.class),
                eq(CaseAssignedUserRolesResponse.class))).thenReturn(caseAssignedUserRolesResponse);
        assertThat(caseSearchService.fetchCaseUserRolesFromCcd(TEST_AUTHORIZATION, List.of(caseDetails)))
                .isEqualTo(caseAssignedUserRolesResponse);
    }

    @Test
    @SneakyThrows
    void theFindAuthorizedCaseBySubmissionReferenceAndRole() {
        // when authorization is null
        assertThat(caseSearchService.findAuthorizedCaseBySubmissionReferenceAndRole(null,
                TEST_CASE_SUBMISSION_REFERENCE, TEST_USER_ROLE)).isNull();
        // when submissionReference is null
        assertThat(caseSearchService.findAuthorizedCaseBySubmissionReferenceAndRole(TEST_AUTHORIZATION,
                null, TEST_USER_ROLE)).isNull();
        // when userRole is null
        assertThat(caseSearchService.findAuthorizedCaseBySubmissionReferenceAndRole(TEST_AUTHORIZATION,
                TEST_CASE_SUBMISSION_REFERENCE, null)).isNull();

        // when all parameters are valid but no case found
        when(authTokenGenerator.generate()).thenReturn(TEST_AUTHORIZATION);
        when(coreCaseDataApi.getCase(TEST_AUTHORIZATION,
                TEST_AUTHORIZATION, TEST_CASE_SUBMISSION_REFERENCE)).thenReturn(null);
        GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                () -> caseSearchService.findAuthorizedCaseBySubmissionReferenceAndRole(TEST_AUTHORIZATION,
                TEST_CASE_SUBMISSION_REFERENCE, TEST_USER_ROLE));
        assertThat(genericServiceException.getMessage()).isEqualTo(TEST_EXPECTED_EXCEPTION_MESSAGE_CASE_NOT_FOUND);

        // when caseDetailsList case id is not empty and user details found and able to post for object
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        UserDetails userDetails = new UserDetails();
        userDetails.setUid(TEST_USER_ID);
        when(userIdamService.getUserDetails(TEST_AUTHORIZATION)).thenReturn(userDetails);
        when(coreCaseDataApi.getCase(TEST_AUTHORIZATION,
                TEST_AUTHORIZATION, TEST_CASE_SUBMISSION_REFERENCE)).thenReturn(caseDetails);
        CaseAssignedUserRolesResponse caseAssignedUserRolesResponse =
                CaseAssignedUserRolesResponse.builder().caseAssignedUserRoles(
                        List.of(CaseAssignmentUserRole.builder().caseRole(DEFENDANT)
                                .userId(TEST_USER_ID).caseDataId(TEST_CASE_SUBMISSION_REFERENCE).build())).build();
        when(restTemplate.postForObject(
                eq(VARIABLE_CCD_DATA_STORE_API_BASE_URL_VALUE + CCD_DATA_STORE_API_SEARCH_API_URL),
                any(HttpEntity.class),
                eq(CaseAssignedUserRolesResponse.class))).thenReturn(caseAssignedUserRolesResponse);
        assertThat(caseSearchService.findAuthorizedCaseBySubmissionReferenceAndRole(TEST_AUTHORIZATION,
                TEST_CASE_SUBMISSION_REFERENCE, DEFENDANT))
                .isEqualTo(caseDetails);
    }
}
