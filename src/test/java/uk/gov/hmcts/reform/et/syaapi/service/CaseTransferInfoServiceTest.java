package uk.gov.hmcts.reform.et.syaapi.service;

import feign.FeignException;
import feign.Request;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignedUserRolesResponse;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.config.interceptors.ResourceNotFoundException;
import uk.gov.hmcts.reform.et.syaapi.exception.CaseUserRoleNotFoundException;
import uk.gov.hmcts.reform.et.syaapi.exception.CaseUserRoleValidationException;
import uk.gov.hmcts.reform.et.syaapi.models.CaseTransferInfoResponse;
import uk.gov.hmcts.reform.et.syaapi.models.CaseTransferType;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_DEFENDANT;
import static uk.gov.hmcts.reform.et.syaapi.service.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class CaseTransferInfoServiceTest {

    private static final String CASE_ID = "1646225213651590";
    private static final String USER_ID = "1234";
    private static final String ADMIN_TOKEN = "admin-token";
    private static final String S2S_TOKEN = "s2s-token";
    private static final String ETHOS_REFERENCE = "60000001/2022";
    private static final String NEW_ETHOS_REFERENCE = "18850001/2020";
    private static final String NEW_CASE_ID = "1234567890123456";
    private static final String TRANSFERRED_CASE_LINK =
        "<a target=\"_blank\" href=\"http://ccd-gateway/cases/case-details/" + NEW_CASE_ID + "\">"
            + NEW_ETHOS_REFERENCE + "</a>";

    @Mock
    private AdminUserService adminUserService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi ccdApi;
    @Mock
    private ManageCaseRoleService manageCaseRoleService;
    @Mock
    private IdamClient idamClient;

    private CaseTransferInfoService caseTransferInfoService;

    @BeforeEach
    void setUp() {
        caseTransferInfoService = new CaseTransferInfoService(
            adminUserService,
            authTokenGenerator,
            ccdApi,
            manageCaseRoleService,
            idamClient
        );
    }

    @Test
    @SneakyThrows
    void shouldReturnEcmTransferInfoWhenTransferComplete() {
        mockUserHasCreatorRole();
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(ccdApi.getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID)).thenReturn(buildTransferredCaseDetails(
            CaseTransferInfoService.TRANSFERRED_TO_ECM,
            TRANSFERRED_CASE_LINK
        ));

        CaseTransferInfoResponse response = caseTransferInfoService.getCaseTransferInfo(
            TEST_SERVICE_AUTH_TOKEN,
            CASE_ID,
            CASE_USER_ROLE_CREATOR
        );

        assertTrue(response.isTransferred());
        assertEquals(CaseTransferType.ECM, response.getTransferType());
        assertEquals(CaseTransferInfoService.TRANSFERRED_STATE, response.getCaseState());
        assertEquals(CASE_ID, response.getOriginalCaseId());
        assertEquals(ETHOS_REFERENCE, response.getOriginalEthosCaseReference());
        assertEquals(NEW_ETHOS_REFERENCE, response.getNewEthosCaseReference());
        assertEquals(NEW_CASE_ID, response.getNewCaseId());
        assertEquals("ECM", response.getDestinationOffice());
        assertEquals("Office move", response.getReasonForCT());
        assertTrue(response.isTransferComplete());
    }

    @Test
    @SneakyThrows
    void shouldReturnCrossCountryTransferInfoWhenTransferPending() {
        mockUserHasCreatorRole();
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(ccdApi.getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID)).thenReturn(buildTransferredCaseDetails(
            "Transferred to Scotland",
            null
        ));

        CaseTransferInfoResponse response = caseTransferInfoService.getCaseTransferInfo(
            TEST_SERVICE_AUTH_TOKEN,
            CASE_ID,
            CASE_USER_ROLE_CREATOR
        );

        assertTrue(response.isTransferred());
        assertEquals(CaseTransferType.CROSS_COUNTRY, response.getTransferType());
        assertEquals("Scotland", response.getDestinationOffice());
        assertFalse(response.isTransferComplete());
        assertNull(response.getNewEthosCaseReference());
        assertNull(response.getNewCaseId());
    }

    @Test
    @SneakyThrows
    void shouldReturnEcmTransferTypeWhenTransferredStateWithoutLinkedCaseCT() {
        mockUserHasCreatorRole();
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(ccdApi.getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID)).thenReturn(buildCaseDetails(
            CaseTransferInfoService.TRANSFERRED_STATE,
            null,
            null
        ));

        CaseTransferInfoResponse response = caseTransferInfoService.getCaseTransferInfo(
            TEST_SERVICE_AUTH_TOKEN,
            CASE_ID,
            CASE_USER_ROLE_CREATOR
        );

        assertTrue(response.isTransferred());
        assertEquals(CaseTransferType.ECM, response.getTransferType());
        assertEquals(CaseTransferInfoService.TRANSFERRED_STATE, response.getCaseState());
        assertFalse(response.isTransferComplete());
    }

    @Test
    void shouldThrowWhenCaseIdIsInvalid() {
        assertThrows(
            CaseUserRoleValidationException.class,
            () -> caseTransferInfoService.getCaseTransferInfo(
                TEST_SERVICE_AUTH_TOKEN,
                "not-a-case-id",
                CASE_USER_ROLE_CREATOR
            )
        );
    }

    @Test
    @SneakyThrows
    void shouldThrowWhenUserHasRoleOnDifferentCase() {
        when(idamClient.getUserInfo(TEST_SERVICE_AUTH_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());
        when(manageCaseRoleService.getCaseUserRolesByCaseAndUserIdsCcd(eq(TEST_SERVICE_AUTH_TOKEN), any()))
            .thenReturn(CaseAssignedUserRolesResponse.builder()
                            .caseAssignedUserRoles(List.of(CaseAssignmentUserRole.builder()
                                                                 .caseDataId("9999999999999999")
                                                                 .userId(USER_ID)
                                                                 .caseRole(CASE_USER_ROLE_CREATOR)
                                                                 .build()))
                            .build());

        assertThrows(
            CaseUserRoleNotFoundException.class,
            () -> caseTransferInfoService.getCaseTransferInfo(
                TEST_SERVICE_AUTH_TOKEN,
                CASE_ID,
                CASE_USER_ROLE_CREATOR
            )
        );
    }

    @Test
    @SneakyThrows
    void shouldThrowWhenUserDoesNotHaveCaseRole() {
        when(idamClient.getUserInfo(TEST_SERVICE_AUTH_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());
        when(manageCaseRoleService.getCaseUserRolesByCaseAndUserIdsCcd(eq(TEST_SERVICE_AUTH_TOKEN), any()))
            .thenReturn(CaseAssignedUserRolesResponse.builder().caseAssignedUserRoles(List.of()).build());

        assertThrows(
            CaseUserRoleNotFoundException.class,
            () -> caseTransferInfoService.getCaseTransferInfo(
                TEST_SERVICE_AUTH_TOKEN,
                CASE_ID,
                CASE_USER_ROLE_CREATOR
            )
        );
    }

    @Test
    @SneakyThrows
    void shouldReturnFallbackTransferInfoWhenCaseNotFoundInCcd() {
        mockUserHasCreatorRole();
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(ccdApi.getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID)).thenThrow(notFoundException());

        CaseTransferInfoResponse response = caseTransferInfoService.getCaseTransferInfo(
            TEST_SERVICE_AUTH_TOKEN,
            CASE_ID,
            CASE_USER_ROLE_CREATOR
        );

        assertTrue(response.isTransferred());
        assertEquals(CaseTransferType.ECM, response.getTransferType());
        assertEquals(CASE_ID, response.getOriginalCaseId());
        assertFalse(response.isTransferComplete());
    }

    @Test
    @SneakyThrows
    void shouldThrowWhenCaseHasNotBeenTransferred() {
        mockUserHasCreatorRole();
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(ccdApi.getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID)).thenReturn(buildCaseDetails(
            "Accepted",
            null,
            null
        ));

        assertThrows(
            ResourceNotFoundException.class,
            () -> caseTransferInfoService.getCaseTransferInfo(
                TEST_SERVICE_AUTH_TOKEN,
                CASE_ID,
                CASE_USER_ROLE_CREATOR
            )
        );
    }

    @ParameterizedTest
    @MethodSource("transferDetectionCases")
    void shouldDetectTransferredCases(String caseState, String linkedCaseCT, boolean expected) {
        assertEquals(expected, CaseTransferInfoService.isTransferredCase(caseState, linkedCaseCT));
    }

    @ParameterizedTest
    @MethodSource("transferTypeCases")
    void shouldResolveTransferType(String linkedCaseCT, String caseState, CaseTransferType expectedType) {
        assertEquals(expectedType, CaseTransferInfoService.resolveTransferType(linkedCaseCT, caseState));
    }

    @Test
    @SneakyThrows
    void shouldThrowWhenUserDoesNotHaveRequestedCaseRole() {
        when(idamClient.getUserInfo(TEST_SERVICE_AUTH_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());
        when(manageCaseRoleService.getCaseUserRolesByCaseAndUserIdsCcd(eq(TEST_SERVICE_AUTH_TOKEN), any()))
            .thenReturn(CaseAssignedUserRolesResponse.builder()
                            .caseAssignedUserRoles(List.of(CaseAssignmentUserRole.builder()
                                                                 .caseDataId(CASE_ID)
                                                                 .userId(USER_ID)
                                                                 .caseRole(CASE_USER_ROLE_CREATOR)
                                                                 .build()))
                            .build());

        assertThrows(
            CaseUserRoleNotFoundException.class,
            () -> caseTransferInfoService.getCaseTransferInfo(
                TEST_SERVICE_AUTH_TOKEN,
                CASE_ID,
                CASE_USER_ROLE_DEFENDANT
            )
        );
    }

    @Test
    @SneakyThrows
    void shouldTreatMalformedTransferredCaseLinkAsIncomplete() {
        mockUserHasCreatorRole();
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(ccdApi.getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID)).thenReturn(buildTransferredCaseDetails(
            CaseTransferInfoService.TRANSFERRED_TO_ECM,
            "not-a-valid-transferred-case-link"
        ));

        CaseTransferInfoResponse response = caseTransferInfoService.getCaseTransferInfo(
            TEST_SERVICE_AUTH_TOKEN,
            CASE_ID,
            CASE_USER_ROLE_CREATOR
        );

        assertFalse(response.isTransferComplete());
        assertNull(response.getNewCaseId());
        assertNull(response.getNewEthosCaseReference());
    }

    @Test
    @SneakyThrows
    void shouldPropagateCcdErrorsOtherThanNotFound() {
        mockUserHasCreatorRole();
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(ccdApi.getCase(ADMIN_TOKEN, S2S_TOKEN, CASE_ID)).thenThrow(
            new FeignException.InternalServerError(
                "Server Error",
                Request.create(Request.HttpMethod.GET, "/cases/" + CASE_ID, Map.of(), null, null, null),
                null,
                Map.of()
            )
        );

        assertThrows(
            FeignException.InternalServerError.class,
            () -> caseTransferInfoService.getCaseTransferInfo(
                TEST_SERVICE_AUTH_TOKEN,
                CASE_ID,
                CASE_USER_ROLE_CREATOR
            )
        );
    }

    @Test
    void shouldParseRelativeTransferredCaseLinkFromGenerateMarkUpFormat() {
        String relativeLink = "<a target=\"_blank\" href=\"/cases/case-details/" + NEW_CASE_ID + "\">"
            + NEW_ETHOS_REFERENCE + "</a>";
        CaseTransferInfoService.ParsedTransferredCaseLink parsed =
            CaseTransferInfoService.parseTransferredCaseLink(relativeLink);

        assertEquals(NEW_CASE_ID, parsed.caseId());
        assertEquals(NEW_ETHOS_REFERENCE, parsed.ethosCaseReference());
        assertTrue(parsed.isComplete());
    }

    @Test
    void shouldParseTransferredCaseLink() {
        CaseTransferInfoService.ParsedTransferredCaseLink parsed =
            CaseTransferInfoService.parseTransferredCaseLink(TRANSFERRED_CASE_LINK);

        assertEquals(NEW_CASE_ID, parsed.caseId());
        assertEquals(NEW_ETHOS_REFERENCE, parsed.ethosCaseReference());
    }

    private static Stream<Arguments> transferDetectionCases() {
        return Stream.of(
            Arguments.of(CaseTransferInfoService.TRANSFERRED_STATE, null, true),
            Arguments.of("Accepted", CaseTransferInfoService.TRANSFERRED_TO_ECM, true),
            Arguments.of("Accepted", "Transferred to Scotland", true),
            Arguments.of("Accepted", null, false),
            Arguments.of("Accepted", "Some other value", false)
        );
    }

    private static Stream<Arguments> transferTypeCases() {
        return Stream.of(
            Arguments.of(CaseTransferInfoService.TRANSFERRED_TO_ECM, null, CaseTransferType.ECM),
            Arguments.of("Transferred to Scotland", "Transferred", CaseTransferType.CROSS_COUNTRY),
            Arguments.of(null, CaseTransferInfoService.TRANSFERRED_STATE, CaseTransferType.ECM)
        );
    }

    @SneakyThrows
    private void mockUserHasCreatorRole() {
        when(idamClient.getUserInfo(TEST_SERVICE_AUTH_TOKEN)).thenReturn(UserInfo.builder().uid(USER_ID).build());
        when(manageCaseRoleService.getCaseUserRolesByCaseAndUserIdsCcd(eq(TEST_SERVICE_AUTH_TOKEN), any()))
            .thenReturn(CaseAssignedUserRolesResponse.builder()
                            .caseAssignedUserRoles(List.of(CaseAssignmentUserRole.builder()
                                                                 .caseDataId(CASE_ID)
                                                                 .userId(USER_ID)
                                                                 .caseRole(CASE_USER_ROLE_CREATOR)
                                                                 .build()))
                            .build());
    }

    private CaseDetails buildTransferredCaseDetails(String linkedCaseCT, String transferredCaseLink) {
        return buildCaseDetails(CaseTransferInfoService.TRANSFERRED_STATE, linkedCaseCT, transferredCaseLink);
    }

    private FeignException.NotFound notFoundException() {
        return new FeignException.NotFound(
            "Not Found",
            Request.create(Request.HttpMethod.GET, "/cases/" + CASE_ID, Map.of(), null, null, null),
            null,
            Map.of()
        );
    }

    private CaseDetails buildCaseDetails(String state, String linkedCaseCT, String transferredCaseLink) {
        Map<String, Object> data = new HashMap<>();
        data.put("ethosCaseReference", ETHOS_REFERENCE);
        data.put(CaseTransferInfoService.LINKED_CASE_CT_FIELD, linkedCaseCT);
        data.put(CaseTransferInfoService.TRANSFERRED_CASE_LINK_FIELD, transferredCaseLink);
        data.put(CaseTransferInfoService.REASON_FOR_CT_FIELD, "Office move");

        return CaseDetails.builder()
            .id(Long.valueOf(CASE_ID))
            .state(state)
            .data(data)
            .build();
    }
}
