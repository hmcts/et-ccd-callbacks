package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.InjectMocks;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.enums.RespondentSolicitorType;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.AmendRepresentativeContactService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.CcdCaseAssignment;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_CASE_DATA_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_CASE_ROLES_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_INVALID_CASE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_INVALID_USER_TOKEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_NO_REPRESENTED_RESPONDENT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_ORGANISATION_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_USER_ID_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_USER_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.REPRESENTATIVE_CONTACT_CHANGE_OPTION_USE_MYHMCTS_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.SYSTEM_ERROR;

@ExtendWith(SpringExtension.class)
class AmendRepresentativeContactServiceTest {

    @MockitoBean
    private UserIdamService  userIdamService;
    @MockitoBean
    private MyHmctsService myHmctsService;
    @MockitoBean
    private CcdCaseAssignment ccdCaseAssignment;
    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private AmendRepresentativeContactService amendRepresentativeContactService;

    private static final String INVALID_USER_TOKEN = "invalidUserToken";
    private static final String VALID_USER_TOKEN = "validUserToken";
    private static final String VALID_USER_TOKEN_RETURNS_INVALID_USER = "validUserTokenReturnsInvalidUser";
    private static final String INVALID_CASE_ID = "invalidCaseId";
    private static final String INVALID_CASE_ID_FOR_GET_USER_ROLES = "invalidCaseIdForGetUserRoles";
    private static final String VALID_CASE_ID = "validCaseId";
    private static final String VALID_CASE_ID_RETURNS_INVALID_CASE = "validCaseIdReturnsInvalidCase";
    private static final String VALID_CASE_ID_RETURNS_INVALID_USER = "validCaseIdReturnsInvalidUser";
    private static final String USER_ID = "userId";
    private static final String INVALID_USER_ID = "invalidUserId";
    private static final String ADDRESS_LINE_1 = "addressLine1";
    private static final String ADDRESS_LINE_2 = "addressLine2";
    private static final String ADDRESS_LINE_3 = "addressLine3";
    private static final String POST_CODE = "postalCode";
    private static final String COUNTRY = "country";
    private static final String COUNTY = "county";
    private static final String POST_TOWN = "postTown";
    private static final String PHONE_NUMBER = "1234567890";
    private static final String RESPONDENT_ID_1 = "respondentId1";

    @BeforeEach
    void setUp() {
        amendRepresentativeContactService = new AmendRepresentativeContactService(userIdamService,
                myHmctsService,
                ccdCaseAssignment);
    }

    @ParameterizedTest
    @MethodSource("generateTestGetDataForRepresentedRespondentIndexes")
    @SneakyThrows
    void theGetRepresentedRespondentIndexesTest(String userToken, String caseId) {
        when(userIdamService.getUserDetails(INVALID_USER_TOKEN)).thenReturn(null);

        UserDetails validUserDetails = new UserDetails();
        validUserDetails.setUid(USER_ID);
        when(userIdamService.getUserDetails(VALID_USER_TOKEN)).thenReturn(validUserDetails);

        UserDetails invalidUserDetails = new UserDetails();
        invalidUserDetails.setUid(null);
        when(userIdamService.getUserDetails(VALID_USER_TOKEN_RETURNS_INVALID_USER)).thenReturn(invalidUserDetails);

        when(ccdCaseAssignment.getCaseUserRoles(INVALID_CASE_ID)).thenReturn(null);

        CaseUserAssignmentData caseUserAssignmentData = getValidCaseUserAssignmentData();
        when(ccdCaseAssignment.getCaseUserRoles(VALID_CASE_ID)).thenReturn(caseUserAssignmentData);

        CaseUserAssignmentData invalidCaseUserAssignmentData = getInvalidCaseUserAssignmentData();
        when(ccdCaseAssignment.getCaseUserRoles(VALID_CASE_ID_RETURNS_INVALID_CASE))
                .thenReturn(invalidCaseUserAssignmentData);

        CaseUserAssignmentData validCaseInvalidUserAssignmentData = getValidCaseInvalidUserAssignmentData();
        when(ccdCaseAssignment.getCaseUserRoles(VALID_CASE_ID_RETURNS_INVALID_USER))
                .thenReturn(validCaseInvalidUserAssignmentData);

        when(ccdCaseAssignment.getCaseUserRoles(INVALID_CASE_ID_FOR_GET_USER_ROLES))
                .thenThrow(new IOException(SYSTEM_ERROR));

        if (StringUtils.isBlank(userToken)) {
            GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                    () -> amendRepresentativeContactService.getRepresentedRespondentIndexes(userToken, caseId));
            assertThat(genericServiceException.getMessage()).isEqualTo(ERROR_INVALID_USER_TOKEN);
            return;
        }
        if (StringUtils.isBlank(caseId)) {
            GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                    () -> amendRepresentativeContactService.getRepresentedRespondentIndexes(userToken, caseId));
            assertThat(genericServiceException.getMessage()).isEqualTo(ERROR_INVALID_CASE_ID);
            return;
        }
        if (INVALID_USER_TOKEN.equals(userToken)) {
            GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                    () -> amendRepresentativeContactService.getRepresentedRespondentIndexes(userToken, caseId));
            assertThat(genericServiceException.getMessage()).isEqualTo(ERROR_USER_NOT_FOUND);
            return;
        }
        if (VALID_USER_TOKEN_RETURNS_INVALID_USER.equals(userToken)) {
            GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                    () -> amendRepresentativeContactService.getRepresentedRespondentIndexes(userToken, caseId));
            assertThat(genericServiceException.getMessage()).isEqualTo(ERROR_USER_ID_NOT_FOUND);
        }
        if (VALID_USER_TOKEN.equals(userToken) && INVALID_CASE_ID.equals(caseId)) {
            GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                    () -> amendRepresentativeContactService.getRepresentedRespondentIndexes(userToken, caseId));
            assertThat(genericServiceException.getMessage()).isEqualTo(ERROR_CASE_ROLES_NOT_FOUND);
        }
        if (VALID_USER_TOKEN.equals(userToken) && VALID_CASE_ID_RETURNS_INVALID_CASE.equals(caseId)) {
            GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                    () -> amendRepresentativeContactService.getRepresentedRespondentIndexes(userToken, caseId));
            assertThat(genericServiceException.getMessage()).isEqualTo(ERROR_CASE_ROLES_NOT_FOUND);
        }
        if (VALID_USER_TOKEN.equals(userToken) && INVALID_CASE_ID_FOR_GET_USER_ROLES.equals(caseId)) {
            GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                    () -> amendRepresentativeContactService.getRepresentedRespondentIndexes(userToken, caseId));
            assertThat(genericServiceException.getMessage()).isEqualTo(SYSTEM_ERROR);
        }
        if (VALID_USER_TOKEN.equals(userToken) && VALID_CASE_ID_RETURNS_INVALID_USER.equals(caseId)) {
            assertThat(amendRepresentativeContactService.getRepresentedRespondentIndexes(userToken, caseId)).isEmpty();
        }
        if (VALID_USER_TOKEN.equals(userToken) && VALID_CASE_ID.equals(caseId)) {
            List<Integer> solicitorIndexList = amendRepresentativeContactService
                    .getRepresentedRespondentIndexes(userToken, caseId);
            assertThat(solicitorIndexList.getFirst()).isZero();
        }
    }

    private static Stream<Arguments> generateTestGetDataForRepresentedRespondentIndexes() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(null, VALID_CASE_ID),
                Arguments.of(VALID_USER_TOKEN, null),
                Arguments.of(INVALID_USER_TOKEN, INVALID_CASE_ID),
                Arguments.of(INVALID_USER_TOKEN, VALID_CASE_ID),
                Arguments.of(VALID_USER_TOKEN_RETURNS_INVALID_USER, VALID_CASE_ID),
                Arguments.of(VALID_USER_TOKEN, INVALID_CASE_ID),
                Arguments.of(VALID_USER_TOKEN, VALID_CASE_ID_RETURNS_INVALID_CASE),
                Arguments.of(VALID_USER_TOKEN, INVALID_CASE_ID_FOR_GET_USER_ROLES),
                Arguments.of(VALID_USER_TOKEN, VALID_CASE_ID_RETURNS_INVALID_USER),
                Arguments.of(VALID_USER_TOKEN, VALID_CASE_ID)
        );
    }

    @ParameterizedTest
    @MethodSource("generateTestSetRespondentRepresentsContactDetails")
    @SneakyThrows
    void theSetRespondentRepresentsContactDetails(String userToken, CaseData caseData) {
        // When both user token and case data are empty
        if (StringUtils.isBlank(userToken) && ObjectUtils.isEmpty(caseData)) {
            GenericServiceException gex = assertThrows(GenericServiceException.class,
                    () -> amendRepresentativeContactService
                            .setRespondentRepresentsContactDetails(userToken, caseData, null));
            assertThat(gex.getMessage()).isEqualTo(ERROR_CASE_DATA_NOT_FOUND);
            return;
        }
        // When only case data is empty
        if (ObjectUtils.isEmpty(caseData)) {
            GenericServiceException gex = assertThrows(GenericServiceException.class,
                    () -> amendRepresentativeContactService
                            .setRespondentRepresentsContactDetails(userToken, caseData, null));
            assertThat(gex.getMessage()).isEqualTo(ERROR_CASE_DATA_NOT_FOUND);
            return;
        }
        // When only user token is empty
        if (ObjectUtils.isEmpty(userToken)) {
            GenericServiceException gex = assertThrows(GenericServiceException.class,
                    () -> amendRepresentativeContactService.setRespondentRepresentsContactDetails(
                            userToken, caseData, caseData.getCcdID()));
            assertThat(gex.getMessage()).isEqualTo(ERROR_INVALID_USER_TOKEN);
            return;
        }
        // when et3ResponseService.getRepresentedRespondentIndexes(INVALID_USER_TOKEN, INVALID_CASE_ID)
        // gives case roles not found exception.
        if (VALID_USER_TOKEN.equals(userToken) && INVALID_CASE_ID.equals(caseData.getCcdID())) {
            UserDetails validUserDetails = new UserDetails();
            validUserDetails.setUid(USER_ID);
            when(userIdamService.getUserDetails(VALID_USER_TOKEN)).thenReturn(validUserDetails);
            when(ccdCaseAssignment.getCaseUserRoles(INVALID_CASE_ID)).thenReturn(getInvalidCaseUserAssignmentData());
            GenericServiceException gex = assertThrows(GenericServiceException.class,
                    () -> amendRepresentativeContactService.setRespondentRepresentsContactDetails(
                            userToken, caseData, caseData.getCcdID()));
            assertThat(gex.getMessage()).isEqualTo(SYSTEM_ERROR);
        }

        // when case data doesn't have any representative in representative collection
        if (VALID_USER_TOKEN.equals(userToken) && VALID_CASE_ID.equals(caseData.getCcdID())) {
            UserDetails validUserDetails = new UserDetails();
            validUserDetails.setUid(USER_ID);
            when(userIdamService.getUserDetails(VALID_USER_TOKEN)).thenReturn(validUserDetails);
            when(ccdCaseAssignment.getCaseUserRoles(VALID_CASE_ID)).thenReturn(getValidCaseUserAssignmentData());
            GenericServiceException gex = assertThrows(GenericServiceException.class,
                    () -> amendRepresentativeContactService.setRespondentRepresentsContactDetails(
                            userToken, caseData, caseData.getCcdID()));
            assertThat(gex.getMessage()).isEqualTo(ERROR_NO_REPRESENTED_RESPONDENT_FOUND);
        }

        // when there is no user role found should throw no represented respondent found exception
        if (VALID_USER_TOKEN.equals(userToken) && VALID_CASE_ID_RETURNS_INVALID_USER.equals(caseData.getCcdID())) {
            UserDetails validUserDetails = new UserDetails();
            validUserDetails.setUid(USER_ID);
            when(userIdamService.getUserDetails(VALID_USER_TOKEN)).thenReturn(validUserDetails);
            when(ccdCaseAssignment.getCaseUserRoles(VALID_CASE_ID_RETURNS_INVALID_USER))
                    .thenReturn(getValidCaseInvalidUserAssignmentData());
            GenericServiceException gex = assertThrows(GenericServiceException.class,
                    () -> amendRepresentativeContactService.setRespondentRepresentsContactDetails(
                            userToken, caseData, caseData.getCcdID()));
            assertThat(gex.getMessage()).isEqualTo(ERROR_NO_REPRESENTED_RESPONDENT_FOUND);
        }

        // when case data has no representative but representative has no value
        if (VALID_USER_TOKEN.equals(userToken) && VALID_CASE_ID.equals(caseData.getCcdID())) {
            caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().value(null).build()));
            UserDetails validUserDetails = new UserDetails();
            validUserDetails.setUid(USER_ID);
            when(userIdamService.getUserDetails(VALID_USER_TOKEN)).thenReturn(validUserDetails);
            when(ccdCaseAssignment.getCaseUserRoles(VALID_CASE_ID)).thenReturn(getValidCaseUserAssignmentData());
            amendRepresentativeContactService
                    .setRespondentRepresentsContactDetails(userToken, caseData, caseData.getCcdID());
            assertThat(caseData.getRepCollection().getFirst().getValue()).isNull();
        }

        // when case data has representative in representative collection
        if (VALID_USER_TOKEN.equals(userToken) && VALID_CASE_ID.equals(caseData.getCcdID())) {
            RepresentedTypeR representedTypeR = RepresentedTypeR.builder().respondentId(RESPONDENT_ID_1).build();
            caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().id(USER_ID)
                    .value(representedTypeR).build()));
            RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
            respondentSumTypeItem.setId(RESPONDENT_ID_1);
            caseData.setRespondentCollection(List.of(respondentSumTypeItem));
            UserDetails validUserDetails = new UserDetails();
            validUserDetails.setUid(USER_ID);
            when(userIdamService.getUserDetails(VALID_USER_TOKEN)).thenReturn(validUserDetails);
            when(ccdCaseAssignment.getCaseUserRoles(VALID_CASE_ID)).thenReturn(getValidCaseUserAssignmentData());
            amendRepresentativeContactService
                    .setRespondentRepresentsContactDetails(userToken, caseData, caseData.getCcdID());
            assertThat(caseData.getRepCollection().getFirst().getValue().getRepresentativeAddress()
                    .getAddressLine1()).isEqualTo("Address Line 1");
            assertThat(caseData.getRepCollection().getFirst().getValue().getRepresentativePhoneNumber())
                    .isEqualTo("1234567890");
        }
    }

    private static Stream<Arguments> generateTestSetRespondentRepresentsContactDetails() {
        CaseData validCaseData = CaseDataBuilder.builder()
                .withClaimantIndType("Doris", "Johnson")
                .withClaimantType("232 Petticoat Square", "3 House", null,
                        "London", "W10 4AG", "United Kingdom")
                .withRespondentWithAddress("Antonio Vazquez",
                        "11 Small Street", "22 House", null,
                        "Manchester", "M12 42R", "United Kingdom",
                        "1234/5678/90")
                .withEt3RepresentingRespondent("Antonio Vazquez")
                .withSubmitEt3Respondent("Antonio Vazquez")
                .build();
        validCaseData.setCcdID(VALID_CASE_ID);
        Address address = new Address();
        address.setAddressLine1("Address Line 1");
        validCaseData.setEt3ResponseAddress(address);
        validCaseData.setEt3ResponsePhone("1234567890");

        CaseData invalidCaseData = CaseDataBuilder.builder()
                .withClaimantIndType("Doris", "Johnson")
                .withClaimantType("232 Petticoat Square", "3 House", null,
                        "London", "W10 4AG", "United Kingdom")
                .withRespondentWithAddress("Antonio Vazquez",
                        "11 Small Street", "22 House", null,
                        "Manchester", "M12 42R", "United Kingdom",
                        "1234/5678/90")
                .withEt3RepresentingRespondent("Antonio Vazquez")
                .withSubmitEt3Respondent("Antonio Vazquez")
                .build();
        invalidCaseData.setCcdID(INVALID_CASE_ID);

        CaseData notRepresentedCaseData = CaseDataBuilder.builder()
                .withClaimantIndType("Doris", "Johnson")
                .withClaimantType("232 Petticoat Square", "3 House", null,
                        "London", "W10 4AG", "United Kingdom")
                .withRespondentWithAddress("Antonio Vazquez",
                        "11 Small Street", "22 House", null,
                        "Manchester", "M12 42R", "United Kingdom",
                        "1234/5678/90")
                .withEt3RepresentingRespondent("Antonio Vazquez")
                .withSubmitEt3Respondent("Antonio Vazquez")
                .build();
        invalidCaseData.setCcdID(VALID_CASE_ID_RETURNS_INVALID_USER);
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(VALID_USER_TOKEN, null),
                Arguments.of(null, validCaseData),
                Arguments.of(VALID_USER_TOKEN, invalidCaseData),
                Arguments.of(VALID_USER_TOKEN, notRepresentedCaseData),
                Arguments.of(VALID_USER_TOKEN, validCaseData)
        );
    }

    private static @NotNull CaseUserAssignmentData getValidCaseInvalidUserAssignmentData() {
        CaseUserAssignment validRespondentSolicitorCaseUserAssignment = new CaseUserAssignment();
        validRespondentSolicitorCaseUserAssignment.setCaseId(VALID_CASE_ID);
        validRespondentSolicitorCaseUserAssignment.setUserId(INVALID_USER_ID);
        validRespondentSolicitorCaseUserAssignment.setCaseRole("[SOLICITORA]");
        CaseUserAssignmentData caseUserAssignmentData = new CaseUserAssignmentData();
        caseUserAssignmentData.setCaseUserAssignments(List.of(validRespondentSolicitorCaseUserAssignment));
        return caseUserAssignmentData;
    }

    private static @NotNull CaseUserAssignmentData getValidCaseUserAssignmentData() {
        CaseUserAssignment validRespondentSolicitorCaseUserAssignment = new CaseUserAssignment();
        validRespondentSolicitorCaseUserAssignment.setCaseId(VALID_CASE_ID);
        validRespondentSolicitorCaseUserAssignment.setUserId(USER_ID);
        validRespondentSolicitorCaseUserAssignment.setCaseRole("[SOLICITORA]");
        CaseUserAssignmentData caseUserAssignmentData = new CaseUserAssignmentData();
        caseUserAssignmentData.setCaseUserAssignments(List.of(validRespondentSolicitorCaseUserAssignment));
        return caseUserAssignmentData;
    }

    private static @NotNull CaseUserAssignmentData getInvalidCaseUserAssignmentData() {
        CaseUserAssignment validRespondentSolicitorCaseUserAssignment = new CaseUserAssignment();
        validRespondentSolicitorCaseUserAssignment.setCaseId(VALID_CASE_ID);
        validRespondentSolicitorCaseUserAssignment.setUserId(USER_ID);
        validRespondentSolicitorCaseUserAssignment.setCaseRole("[CLAIMANTSOLICITOR]");
        CaseUserAssignmentData caseUserAssignmentData = new CaseUserAssignmentData();
        caseUserAssignmentData.setCaseUserAssignments(List.of(validRespondentSolicitorCaseUserAssignment));
        return caseUserAssignmentData;
    }

    @Test
    @SneakyThrows
    void testSetRepresentativeContactInfo() {
        String userToken = "mockToken";
        String submissionReference = "mockRef";
        UserDetails userDetails = new UserDetails();
        userDetails.setUid(USER_ID);
        when(userIdamService.getUserDetails(userToken)).thenReturn(userDetails);
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder()
                .caseUserAssignments(List.of(CaseUserAssignment.builder().userId(USER_ID)
                        .caseRole(RespondentSolicitorType.SOLICITORA.getLabel()).build())).build();
        when(ccdCaseAssignment.getCaseUserRoles(submissionReference)).thenReturn(caseUserAssignmentData);
        // Scenario 2: Use MyHMCTS contact details
        CaseData caseData2 = new CaseData();
        caseData2.setRepCollection(
                List.of(RepresentedTypeRItem.builder().value(RepresentedTypeR.builder().build()).build()));
        caseData2.setEt3ResponsePhone(PHONE_NUMBER);
        caseData2.setRepresentativeContactChangeOption(REPRESENTATIVE_CONTACT_CHANGE_OPTION_USE_MYHMCTS_DETAILS);
        when(userIdamService.getUserDetails(userToken)).thenReturn(userDetails);
        OrganisationAddress organisationAddress = OrganisationAddress.builder()
                .addressLine1(ADDRESS_LINE_1)
                .addressLine2(ADDRESS_LINE_2)
                .addressLine3(ADDRESS_LINE_3)
                .townCity(POST_TOWN)
                .postCode(POST_CODE)
                .county(COUNTY)
                .country(COUNTRY)
                .build();
        when(authTokenGenerator.generate()).thenReturn(userToken);
        when(myHmctsService.getOrganisationAddress(userToken)).thenReturn(organisationAddress);
        amendRepresentativeContactService
                .setRespondentRepresentsContactDetails(userToken, caseData2, submissionReference);
        assertThat(caseData2.getEt3ResponseAddress()).isNotNull();
        assertThat(caseData2.getEt3ResponseAddress().getAddressLine1()).isEqualTo(ADDRESS_LINE_1);
        assertThat(caseData2.getEt3ResponseAddress().getAddressLine2()).isEqualTo(ADDRESS_LINE_2);
        assertThat(caseData2.getEt3ResponseAddress().getAddressLine3()).isEqualTo(ADDRESS_LINE_3);
        assertThat(caseData2.getEt3ResponseAddress().getPostTown()).isEqualTo(POST_TOWN);
        assertThat(caseData2.getEt3ResponseAddress().getPostCode()).isEqualTo(POST_CODE);
        assertThat(caseData2.getEt3ResponseAddress().getCounty()).isEqualTo(COUNTY);
        assertThat(caseData2.getEt3ResponseAddress().getCountry()).isEqualTo(COUNTRY);
        assertThat(caseData2.getEt3ResponsePhone()).isEqualTo(PHONE_NUMBER);

        // Scenario 3: Throws when user not found
        CaseData caseData3 = new CaseData();
        caseData3.setRepresentativeContactChangeOption(REPRESENTATIVE_CONTACT_CHANGE_OPTION_USE_MYHMCTS_DETAILS);

        when(userIdamService.getUserDetails(userToken)).thenReturn(null);

        GenericServiceException userNotFound = assertThrows(GenericServiceException.class, () ->
                amendRepresentativeContactService
                        .setRespondentRepresentsContactDetails(userToken, caseData3, submissionReference));
        assertThat(userNotFound.getMessage()).isEqualTo("User not found");

        // Scenario 3: Throws when organisation not found
        CaseData caseData4 = new CaseData();
        caseData4.setRepresentativeContactChangeOption(REPRESENTATIVE_CONTACT_CHANGE_OPTION_USE_MYHMCTS_DETAILS);

        when(userIdamService.getUserDetails(userToken)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(userToken);
        doThrow(new GenericServiceException(ERROR_ORGANISATION_DETAILS_NOT_FOUND,
                new Exception(ERROR_ORGANISATION_DETAILS_NOT_FOUND),
                ERROR_ORGANISATION_DETAILS_NOT_FOUND,
                org.apache.commons.lang3.StringUtils.EMPTY,
                "MyHmctsService",
                "getOrganisationAddress - organisation details not found"))
                .when(myHmctsService).getOrganisationAddress(userToken);
        GenericServiceException organisationNotFound = assertThrows(GenericServiceException.class, () ->
                amendRepresentativeContactService
                        .setRespondentRepresentsContactDetails(userToken, caseData4, submissionReference));
        assertThat(organisationNotFound.getMessage()).isEqualTo("Organisation details not found");
    }

}
