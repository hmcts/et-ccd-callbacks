package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeFieldPopulator;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.OrganisationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.et.syaapi.enums.CaseEvent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CaseConverter.class, NoticeOfChangeFieldPopulator.class, ObjectMapper.class})
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods", "PMD.ExcessiveMethodLength"})
class NocRespondentRepresentativeServiceTest {
    private static final String CASE_ID_1 = "1234567890123456";
    private static final String CASE_TYPE_ID_ENGLAND_WALES = "ET_EnglandWales";
    private static final String JURISDICTION_EMPLOYMENT = "EMPLOYMENT";
    private static final String RESPONDENT_NAME_ONE = "Harry Johnson";
    private static final String RESPONDENT_NAME_TWO = "Jane Green";
    private static final String RESPONDENT_NAME_THREE = "Bad Company Inc";
    private static final String RESPONDENT_REF = "7277";
    private static final String RESPONDENT_REF_TWO = "6887";
    private static final String RESPONDENT_REF_THREE = "9292";
    private static final String RESPONDENT_EMAIL = "h.johnson@corp.co.uk";
    private static final String RESPONDENT_EMAIL_TWO = "j.green@corp.co.uk";
    private static final String RESPONDENT_EMAIL_THREE = "info@corp.co.uk";
    private static final String REPRESENTATIVE_ID_ONE = "1111-2222-3333-1111";
    private static final String REPRESENTATIVE_ID_TWO = "1111-2222-3333-1112";
    private static final String REPRESENTATIVE_ID_THREE = "1111-2222-3333-1113";
    private static final String RESPONDENT_REP_NAME = "Legal One";
    private static final String RESPONDENT_REP_NAME_TWO = "Legal Two";
    private static final String RESPONDENT_REP_NAME_THREE = "Legal Three";
    private static final String ROLE_CLAIMANT_SOLICITOR = "[CLAIMANTSOLICITOR]";
    private static final String ROLE_SOLICITORA = "[SOLICITORA]";
    private static final String ROLE_SOLICITORB = "[SOLICITORB]";
    private static final String ROLE_SOLICITORC = "[SOLICITORC]";
    private static final String ORGANISATION_ID_ONE = "ORG1";
    private static final String ORGANISATION_ID_TWO = "ORG2";
    private static final String ORGANISATION_ID_THREE = "ORG3";
    private static final String ORGANISATION_ID_FOUR = "ORG4";
    private static final String ORGANISATION_ID_FIVE = "ORG5";
    private static final String ORGANISATION_ID_SIX = "ORG6";
    private static final String ORGANISATION_ID_SEVEN = "ORG7";
    private static final String ORGANISATION_ID_EIGHT = "ORG8";
    private static final String ORGANISATION_ID_NINE = "ORG9";
    private static final String ORGANISATION_ID_TEN = "ORG10";
    private static final String ET_ORG_1 = "ET Org 1";
    private static final String ET_ORG_2 = "ET Org 2";
    private static final String ET_ORG_3 = "ET Org 3";
    private static final String ET_ORG_NEW = "ET Org New";
    private static final String USER_ID = "test_user_id";
    private static final String USER_EMAIL = "test@hmcts.net";
    private static final String USER_FIRST_NAME = "John";
    private static final String USER_LAST_NAME = "Brown";
    private static final String USER_FULL_NAME = "John Brown";
    private static final String REPRESENTATIVE_EMAIL_1_CAPITALISED = "REPRESENTATIVE1@TESTMAIL.COM";
    private static final String REPRESENTATIVE_NAME = "Representative Name";
    private static final String RESPONDENT_REPRESENTATIVE_EMAIL = "respondentRepresentative@gmail.com";
    private static final String CLAIMANT_REPRESENTATIVE_EMAIL = "claimantRepresentative@gmail.com";
    private static final String REPRESENTATIVE_EMAIL_1 = "representative1@gmail.com";
    private static final String REPRESENTATIVE_EMAIL_2 = "representative2@gmail.com";
    private static final String RESPONDENT_ID_ONE = "106001";
    private static final String RESPONDENT_ID_TWO = "106002";
    private static final String RESPONDENT_ID_THREE = "106003";
    private static final String ADMIN_USER_TOKEN = "adminUserToken";
    private static final String USER_TOKEN = "userToken";
    private static final String S2S_TOKEN = "someS2SToken";
    private static final String EVENT_UPDATE_CASE_SUBMITTED = "UPDATE_CASE_SUBMITTED";

    private static final String EXCEPTION_DUMMY_MESSAGE = "Something went wrong";

    private static final String EXPECTED_EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND =
            "Organisation not found for representative.";
    private static final String EXPECTED_EXCEPTION_UNABLE_TO_START_EVENT_WITHOUT_CASE_ID =
            "uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException: Unable to start event "
                    + "UPDATE_RESP_ORG_POLICY.";
    private static final String EXPECTED_EXCEPTION_UNABLE_TO_START_EVENT_WITH_CASE_ID =
            "uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException: Unable to start event "
                    + "UPDATE_RESP_ORG_POLICY for case 1234567890123456";

    private static final String EXPECTED_ERROR_UNABLE_TO_NOTIFY_REPRESENTATION_REMOVAL =
            "Unable to send notification for representative removal for case: 1234567890123456. Exception: Something "
                    + "went wrong";
    private static final String EXPECTED_ERROR_SOLICITOR_ROLE_NOT_FOUND =
            "Solicitor role not found, case id: 1234567890123456";
    private static final String EXPECTED_ERROR_UNABLE_TO_SET_ROLE =
            "Unable to set role [SOLICITORA]. Case Id: 1234567890123456. Error: Something went wrong";
    private static final String EXPECTED_ERROR_FAILED_TO_REMOVE_ORGANISATION_POLICIES =
            "Failed to remove organisation policies for case 1234567890123456. Exception: Something went wrong";
    private static final String EXPECTED_ERROR_SELECTED_ORGANISATION_REPRESENTATIVE_ORGANISATION_NOT_MATCHES =
            "Representative Representative Name organisation does not match with selected organisation ORG1";

    private static final String EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL =
            "We have been unable to assign 'Legal One' access to this case via MyHMCTS. They must check with their "
                    + "organisation administrator to ensure they have a valid MyHMCTS account, who will need to "
                    + "assign the case to them.\n";
    private static final String EXPECTED_WARNING_REPRESENTATIVE_EMAIL_NOT_FOUND =
            "Representative email address not found.\n";
    private static final String EXPECTED_WARNING_FAILED_TO_RETRIEVE_CASE_ASSIGNMENTS =
            "Failed to retrieve case assignments for case id: 1234567890123456, error: Something went wrong";

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NoticeOfChangeFieldPopulator noticeOfChangeFieldPopulator;
    @MockBean
    private AdminUserService adminUserService;
    @MockBean
    private NocCcdService nocCcdService;
    @MockBean
    private NocNotificationService nocNotificationService;
    @MockBean
    private CcdClient ccdClient;
    @MockBean
    private OrganisationClient organisationClient;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private NocService nocService;
    @MockBean
    private UserIdamService userIdamService;
    @MockBean
    private OrganisationService organisationService;

    @InjectMocks
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;

    private NocRespondentHelper nocRespondentHelper;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        LoggerTestUtils.initializeLogger(NocRespondentRepresentativeService.class);
        nocRespondentHelper = new NocRespondentHelper();
        caseData = new CaseData();
        CaseConverter converter = new CaseConverter(objectMapper);

        nocRespondentRepresentativeService =
                new NocRespondentRepresentativeService(noticeOfChangeFieldPopulator, converter, nocCcdService,
                        adminUserService, nocRespondentHelper, nocNotificationService, ccdClient, organisationClient,
                        authTokenGenerator, nocService, userIdamService, organisationService);

        // Respondent
        caseData.setRespondentCollection(new ArrayList<>());

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder()
                .respondentName(RESPONDENT_NAME_ONE)
                .respondentEmail(RESPONDENT_EMAIL)
                .responseReference(RESPONDENT_REF)
                .build());
        respondentSumTypeItem.setId(RESPONDENT_ID_ONE);
        caseData.getRespondentCollection().add(respondentSumTypeItem);

        respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_TWO)
                .respondentEmail(RESPONDENT_EMAIL_TWO)
                .responseReference(RESPONDENT_REF_TWO)
                .build());
        respondentSumTypeItem.setId(RESPONDENT_ID_TWO);
        caseData.getRespondentCollection().add(respondentSumTypeItem);

        respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_THREE)
                .respondentEmail(RESPONDENT_EMAIL_THREE)
                .responseReference(RESPONDENT_REF_THREE)
                .build());
        respondentSumTypeItem.setId(RESPONDENT_ID_THREE);
        caseData.getRespondentCollection().add(respondentSumTypeItem);

        //Organisation
        Organisation org1 =
                Organisation.builder().organisationID(ORGANISATION_ID_ONE).organisationName(ET_ORG_1).build();
        OrganisationPolicy orgPolicy1 =
                OrganisationPolicy.builder().organisation(org1).orgPolicyCaseAssignedRole(ROLE_SOLICITORA).build();
        Organisation org2 =
                Organisation.builder().organisationID(ORGANISATION_ID_TWO).organisationName(ET_ORG_2).build();
        OrganisationPolicy orgPolicy2 =
                OrganisationPolicy.builder().organisation(org2).orgPolicyCaseAssignedRole(ROLE_SOLICITORB).build();
        Organisation org3 =
                Organisation.builder().organisationID(ORGANISATION_ID_THREE).organisationName(ET_ORG_3).build();
        OrganisationPolicy orgPolicy3 =
                OrganisationPolicy.builder().organisation(org3).orgPolicyCaseAssignedRole(ROLE_SOLICITORC).build();

        caseData.setRespondentOrganisationPolicy0(orgPolicy1);
        caseData.setRespondentOrganisationPolicy1(orgPolicy2);
        caseData.setRespondentOrganisationPolicy2(orgPolicy3);

        // Respondent Representative
        caseData.setRepCollection(new ArrayList<>());
        RepresentedTypeR representedType =
                RepresentedTypeR.builder()
                        .nameOfRepresentative(RESPONDENT_REP_NAME)
                        .respRepName(RESPONDENT_NAME_ONE)
                        .respondentOrganisation(org1)
                        .myHmctsYesNo(YES).build();
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(REPRESENTATIVE_ID_ONE);
        representedTypeRItem.setValue(representedType);
        representedTypeRItem.getValue().setRespondentId(RESPONDENT_ID_ONE);
        caseData.getRepCollection().add(representedTypeRItem);

        representedType =
                RepresentedTypeR.builder()
                        .nameOfRepresentative(RESPONDENT_REP_NAME_TWO)
                        .respRepName(RESPONDENT_NAME_TWO)
                        .respondentOrganisation(org2)
                        .myHmctsYesNo(NO).build();
        representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(REPRESENTATIVE_ID_TWO);
        representedTypeRItem.setValue(representedType);
        representedTypeRItem.getValue().setRespondentId(RESPONDENT_ID_TWO);
        caseData.getRepCollection().add(representedTypeRItem);

        representedType =
                RepresentedTypeR.builder()
                        .nameOfRepresentative(RESPONDENT_REP_NAME_THREE)
                        .respRepName(RESPONDENT_NAME_THREE)
                        .respondentOrganisation(org3)
                        .myHmctsYesNo(YES).build();
        representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(REPRESENTATIVE_ID_THREE);
        representedTypeRItem.setValue(representedType);
        representedTypeRItem.getValue().setRespondentId(RESPONDENT_ID_THREE);
        caseData.getRepCollection().add(representedTypeRItem);

        caseData.setChangeOrganisationRequestField(ChangeOrganisationRequest.builder()
                .organisationToAdd(org1)
                .organisationToRemove(org2)
                .caseRoleId(null)
                .requestTimestamp(null)
                .approvalStatus(null)
                .build());
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
    }

    @Test
    void shouldPrepopulateWithOrganisationPolicyAndNoc() {
        caseData = nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(caseData);

        assertThat(caseData.getRespondentOrganisationPolicy0()).isNotNull();
        assertThat(caseData.getRespondentOrganisationPolicy0().getOrgPolicyCaseAssignedRole()).isNotNull()
                .isEqualTo(ROLE_SOLICITORA);
        assertThat(caseData.getRespondentOrganisationPolicy0().getOrganisation().getOrganisationID()).isNotNull()
                .isEqualTo(ORGANISATION_ID_ONE);
        assertThat(caseData.getRespondentOrganisationPolicy1()).isNotNull();
        assertThat(caseData.getRespondentOrganisationPolicy1().getOrgPolicyCaseAssignedRole()).isNotNull()
                .isEqualTo(ROLE_SOLICITORB);
        assertThat(caseData.getRespondentOrganisationPolicy1().getOrganisation().getOrganisationID()).isNotNull()
                .isEqualTo(ORGANISATION_ID_TWO);
    }

    @Test
    @SneakyThrows
    void shouldUpdateRespondentRepresentationDetails() {
        CaseDetails caseDetails = new CaseDetails();
        Organisation oldOrganisation =
                Organisation.builder().organisationID(ORGANISATION_ID_TWO).organisationName(ET_ORG_2).build();

        Organisation newOrganisation =
                Organisation.builder().organisationID(ORGANISATION_ID_FOUR).organisationName(ET_ORG_NEW).build();

        ChangeOrganisationRequest changeOrganisationRequest =
                createChangeOrganisationRequest(newOrganisation, oldOrganisation);

        caseData.setChangeOrganisationRequestField(changeOrganisationRequest);

        UserDetails mockUser = getMockUser();

        when(adminUserService.getUserDetails(anyString(), any())).thenReturn(mockUser);
        caseDetails.setCaseId(CASE_ID_1);
        caseDetails.setCaseData(caseData);
        when(nocCcdService.getLatestAuditEventByName(any(), any(), any())).thenReturn(
                Optional.of(mockAuditEvent()));
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        AccountIdByEmailResponse accountIdByEmailResponse = new AccountIdByEmailResponse();
        accountIdByEmailResponse.setUserIdentifier(USER_ID);
        when(nocService.findUserByEmail(ADMIN_USER_TOKEN, USER_EMAIL, CASE_ID_1)).thenReturn(accountIdByEmailResponse);
        nocRespondentRepresentativeService.updateRespondentRepresentation(caseDetails);

        assertThat(caseData.getRepCollection().get(1).getValue().getRespondentOrganisation().getOrganisationID())
                .isEqualTo(ORGANISATION_ID_FOUR);
        assertThat(caseData.getRepCollection().get(1).getValue().getRespondentOrganisation().getOrganisationName())
                .isEqualTo(ET_ORG_NEW);
        assertThat(caseData.getRepCollection().get(1).getValue().getNameOfRepresentative()).isEqualTo(USER_FULL_NAME);
        assertThat(caseData.getRepCollection().get(1).getValue().getRepresentativeEmailAddress())
                .isEqualTo(USER_EMAIL);
    }

    private AuditEvent mockAuditEvent() {
        return AuditEvent.builder()
                .id("123")
                .userId("54321")
                .userFirstName("John")
                .userLastName("Brown")
                .createdDate(LocalDateTime.now())
                .build();
    }

    private ChangeOrganisationRequest createChangeOrganisationRequest(Organisation organisationToAdd,
                                                                      Organisation organisationToRemove) {
        DynamicFixedListType caseRole = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(NocRespondentRepresentativeServiceTest.ROLE_SOLICITORB);
        dynamicValueType.setLabel(NocRespondentRepresentativeServiceTest.ROLE_SOLICITORB);
        caseRole.setValue(dynamicValueType);

        return ChangeOrganisationRequest.builder()
                .organisationToAdd(organisationToAdd)
                .organisationToRemove(organisationToRemove)
                .caseRoleId(caseRole)
                .build();
    }

    private UserDetails getMockUser() {
        final UserDetails userDetails = new UserDetails();
        userDetails.setEmail(USER_EMAIL);
        userDetails.setFirstName(USER_FIRST_NAME);
        userDetails.setLastName(USER_LAST_NAME);
        return userDetails;
    }

    @Test
    void shouldReturnDetailsOfRespondentAssociatedWithRepCollectionItem() {
        RespondentSumType respondent = nocRespondentHelper.getRespondent(RESPONDENT_NAME_THREE, caseData);
        assertThat(respondent.getResponseReference()).isEqualTo(RESPONDENT_REF_THREE);
    }

    @Test
    void prepopulateOrgAddressAndName() {
        OrganisationsResponse resOrg1 = createOrganisationsResponse(ORGANISATION_ID_ONE, ET_ORG_1);
        OrganisationsResponse resOrg2 = createOrganisationsResponse(ORGANISATION_ID_TWO, ET_ORG_2);
        OrganisationsResponse resOrg3 = createOrganisationsResponse(ORGANISATION_ID_THREE, ET_ORG_3);

        List<OrganisationsResponse> orgDetails = new ArrayList<>();
        orgDetails.add(resOrg1);
        orgDetails.add(resOrg2);
        orgDetails.add(resOrg3);

        when(organisationClient.getOrganisations(anyString(), anyString())).thenReturn(orgDetails);
        caseData.getRepCollection().getFirst().getValue().setRepresentativeAddress(null);
        CaseData returned = nocRespondentRepresentativeService.prepopulateOrgAddress(caseData, "someToken");

        verify(organisationClient, times(1)).getOrganisations(anyString(), anyString());

        List<RepresentedTypeRItem> repCollection = returned.getRepCollection();

        RepresentedTypeR rep1 = repCollection.getFirst().getValue();
        assertThat(rep1.getRepresentativeAddress().getAddressLine1())
                .isEqualTo(resOrg1.getContactInformation().getFirst().getAddressLine1());
        assertThat(rep1.getNameOfOrganisation()).isEqualTo(resOrg1.getName());

        RepresentedTypeR rep2 = repCollection.get(1).getValue();
        assertNull(rep2.getRepresentativeAddress());
        assertNull(rep2.getNameOfOrganisation());

        RepresentedTypeR rep3 = repCollection.get(2).getValue();
        assertThat(rep3.getRepresentativeAddress().getAddressLine1())
                .isEqualTo(resOrg3.getContactInformation().getFirst().getAddressLine1());
        assertThat(rep3.getNameOfOrganisation()).isEqualTo(resOrg3.getName());
    }

    @Test
    void prepopulateOrgAddress_RepCollection_Null() {
        caseData.setRepCollection(null);
        nocRespondentRepresentativeService.prepopulateOrgAddress(caseData, "someToken");
        verify(organisationClient, never()).getOrganisations(anyString(), anyString());
    }

    @Test
    void prepopulateOrgAddress_RepCollection_Empty() {
        caseData.setRepCollection(new ArrayList<>());
        nocRespondentRepresentativeService.prepopulateOrgAddress(caseData, "someToken");
        verify(organisationClient, never()).getOrganisations(anyString(), anyString());
    }

    @Test
    void prepopulateOrgAddress_NoOrganisationAccounts() {
        for (RepresentedTypeRItem representative : caseData.getRepCollection()) {
            representative.getValue().setMyHmctsYesNo(NO);
        }
        nocRespondentRepresentativeService.prepopulateOrgAddress(caseData, "someToken");
        verify(organisationClient, never()).getOrganisations(anyString(), anyString());
    }

    @Test
    void prepopulateOrgAddress_NoActiveOrganisations() {
        when(organisationClient.getOrganisations(anyString(), anyString())).thenReturn(new ArrayList<>());

        CaseData returned = nocRespondentRepresentativeService.prepopulateOrgAddress(caseData, "someToken");

        verify(organisationClient, times(1)).getOrganisations(anyString(), anyString());

        List<RepresentedTypeRItem> repCollection = returned.getRepCollection();

        assertNull(repCollection.get(0).getValue().getRepresentativeAddress());
        assertNull(repCollection.get(1).getValue().getRepresentativeAddress());
        assertNull(repCollection.get(2).getValue().getRepresentativeAddress());
    }

    @Test
    void prepopulateOrgAddress_OrgNotFound() {
        OrganisationsResponse resOrg1 = createOrganisationsResponse("SomeOrgId", ET_ORG_1);

        List<OrganisationsResponse> orgDetails = new ArrayList<>();
        orgDetails.add(resOrg1);

        when(organisationClient.getOrganisations(anyString(), anyString())).thenReturn(orgDetails);

        CaseData returned = nocRespondentRepresentativeService.prepopulateOrgAddress(caseData, "someToken");

        verify(organisationClient, times(1)).getOrganisations(anyString(), anyString());

        List<RepresentedTypeRItem> repCollection = returned.getRepCollection();

        assertNull(repCollection.get(0).getValue().getRepresentativeAddress());
        assertNull(repCollection.get(1).getValue().getRepresentativeAddress());
        assertNull(repCollection.get(2).getValue().getRepresentativeAddress());
    }

    @Test
    void prepopulateOrgAddress_AddressNotPopulated() {
        OrganisationsResponse resOrg1 = createOrganisationsResponse(ORGANISATION_ID_ONE, ET_ORG_1);
        resOrg1.setContactInformation(null);
        OrganisationsResponse resOrg2 = createOrganisationsResponse(ORGANISATION_ID_TWO, ET_ORG_2);
        resOrg2.setContactInformation(new ArrayList<>());
        OrganisationsResponse resOrg3 = createOrganisationsResponse(ORGANISATION_ID_THREE, ET_ORG_3);
        resOrg3.getContactInformation().getFirst().setAddressLine1(null);
        resOrg3.getContactInformation().getFirst().setTownCity(null);
        resOrg3.getContactInformation().getFirst().setCountry(null);

        List<OrganisationsResponse> orgDetails = new ArrayList<>();
        orgDetails.add(resOrg1);
        orgDetails.add(resOrg2);
        orgDetails.add(resOrg3);

        when(organisationClient.getOrganisations(anyString(), anyString())).thenReturn(orgDetails);
        caseData.getRepCollection().getFirst().getValue().setRepresentativeAddress(null);
        CaseData returned = nocRespondentRepresentativeService.prepopulateOrgAddress(caseData, "someToken");

        verify(organisationClient, times(1)).getOrganisations(anyString(), anyString());

        List<RepresentedTypeRItem> repCollection = returned.getRepCollection();

        assertNull(repCollection.get(0).getValue().getRepresentativeAddress());
        assertNull(repCollection.get(1).getValue().getRepresentativeAddress());
        assertNull(repCollection.get(2).getValue().getRepresentativeAddress().getAddressLine1());
    }

    private OrganisationsResponse createOrganisationsResponse(String orgId, String orgName) {
        OrganisationAddress orgAddress =
                OrganisationAddress.builder()
                        .addressLine1(orgName + " Address 1")
                        .townCity(orgName + " TownCity 2")
                        .country(orgName + " Country 3")
                        .build();

        return OrganisationsResponse.builder()
                .organisationIdentifier(orgId)
                .name(orgName)
                .contactInformation(Collections.singletonList(orgAddress)).build();
    }

    @Test
    @SneakyThrows
    void theValidateRepresentativesOrganisationsAndEmails() {
        // when case data is empty should return empty list
        assertDoesNotThrow(() -> nocRespondentRepresentativeService
                .validateRepresentativesOrganisationsAndEmails(null));

        // when representative not exists in hmcts organisation. (my Hmcts is selected as NO) should return empty list
        CaseData tmpCaseData = new CaseData();
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setLabel(RESPONDENT_NAME_ONE);
        dynamicFixedListType.setValue(dynamicValueType);
        tmpCaseData.setRepCollection(List.of(RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_ONE).value(
                RepresentedTypeR.builder().myHmctsYesNo(NO).dynamicRespRepName(dynamicFixedListType)
                        .nameOfRepresentative(RESPONDENT_REP_NAME).build()).build()));
        nocRespondentRepresentativeService.validateRepresentativesOrganisationsAndEmails(tmpCaseData);
        assertThat(tmpCaseData.getNocWarning()).isEmpty();
        // when representative does not have email address should return empty list
        tmpCaseData.getRepCollection().getFirst().getValue().setMyHmctsYesNo(YES);
        nocRespondentRepresentativeService.validateRepresentativesOrganisationsAndEmails(tmpCaseData);
        assertThat(tmpCaseData.getNocWarning()).isEqualTo(EXPECTED_WARNING_REPRESENTATIVE_EMAIL_NOT_FOUND);
        // when representative has email and does not have organisation should throw exception
        tmpCaseData.getRepCollection().getFirst().getValue().setRepresentativeEmailAddress(
                RESPONDENT_REPRESENTATIVE_EMAIL);
        GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                () -> nocRespondentRepresentativeService.validateRepresentativesOrganisationsAndEmails(tmpCaseData));
        assertThat(genericServiceException.getMessage()).isEqualTo(
                EXPECTED_EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND);

        // when representative organisation does not have id should throw exception
        tmpCaseData.getRepCollection().getFirst().getValue().setRespondentOrganisation(Organisation.builder().build());
        tmpCaseData.getRepCollection().getFirst().getValue().setMyHmctsYesNo(YES);
        genericServiceException = assertThrows(GenericServiceException.class,
                () -> nocRespondentRepresentativeService.validateRepresentativesOrganisationsAndEmails(tmpCaseData));
        assertThat(genericServiceException.getMessage()).isEqualTo(
                EXPECTED_EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND);

        // when organisation service returns empty response should not return warning
        tmpCaseData.getRepCollection().getFirst().getValue().getRespondentOrganisation().setOrganisationID(ET_ORG_1);
        when(organisationService.checkRepresentativeAccountByEmail(RESPONDENT_REP_NAME,
                RESPONDENT_REPRESENTATIVE_EMAIL)).thenReturn(StringUtils.EMPTY);
        nocRespondentRepresentativeService.validateRepresentativesOrganisationsAndEmails(tmpCaseData);
        assertThat(tmpCaseData.getNocWarning()).isEmpty();

        // when organisation service returns warning should return that warning
        when(organisationService.checkRepresentativeAccountByEmail(RESPONDENT_REP_NAME,
                RESPONDENT_REPRESENTATIVE_EMAIL))
                .thenReturn(EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL);
        nocRespondentRepresentativeService.validateRepresentativesOrganisationsAndEmails(tmpCaseData);
        assertThat(tmpCaseData.getNocWarning()).isNotEmpty();
        assertThat(tmpCaseData.getNocWarning()).isEqualTo(EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL);
    }

    @Test
    @SneakyThrows
    void theRevokeOldRespondentRepresentativeAccessTest() {
        // when callback request is empty should return empty list
        List<RepresentedTypeRItem> representativesToRemove = new ArrayList<>();
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(null,
                USER_TOKEN, representativesToRemove)).isEmpty();
        // when user token is empty should return empty list
        CallbackRequest callbackRequest = CallbackRequest.builder().build();
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                StringUtils.EMPTY, representativesToRemove)).isEmpty();
        // when old case details is empty should return empty list
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        // when old case details does not have case id should return empty list
        CaseDetails tmpCaseDetails = new CaseDetails();
        callbackRequest.setCaseDetailsBefore(tmpCaseDetails);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        // when old case details does not have case data should return empty list
        tmpCaseDetails.setCaseId(CASE_ID_1);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        // when representatives to remove is empty should return empty list
        CaseData tmpCaseData = new CaseData();
        tmpCaseDetails.setCaseData(tmpCaseData);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        // when nocCcdService.getCaseAssignments returns null should return empty list
        RepresentedTypeRItem tmpRepresentative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_ONE)
                .value(RepresentedTypeR.builder().build()).build();
        representativesToRemove.add(tmpRepresentative);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(null);
        nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest, USER_TOKEN,
                representativesToRemove);
        verifyNocCcdServiceCaseAssignmentsCall(LoggerTestUtils.INTEGER_ONE);
        // when nocCcdService.getCaseAssignments returns case assignments data without any case user assignments should
        // not revoke case user assignments
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder().build();
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verifyNocCcdServiceCaseAssignmentsCall(LoggerTestUtils.INTEGER_TWO);
        // when there is no respondent representative role in case user assignments should not revoke case user
        // assignments
        caseUserAssignmentData.setCaseUserAssignments(List.of(CaseUserAssignment.builder().caseRole(
                ROLE_CLAIMANT_SOLICITOR).build()));
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verifyNocCcdServiceCaseAssignmentsCall(LoggerTestUtils.INTEGER_THREE);
        // when representative in representative list is not a valid representative should not revoke case user
        // assignments
        caseUserAssignmentData.getCaseUserAssignments().getFirst().setCaseRole(ROLE_SOLICITORA);
        tmpRepresentative.setId(StringUtils.EMPTY);
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verifyNocCcdServiceCaseAssignmentsCall(LoggerTestUtils.INTEGER_FOUR);
        // when representative is not able to removed should not revoke case user assignment
        tmpRepresentative.setId(REPRESENTATIVE_ID_ONE);
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verifyNocCcdServiceCaseAssignmentsCall(LoggerTestUtils.INTEGER_FIVE);
        // when respondent name not exists should not revoke case user assignment
        tmpRepresentative.getValue().setMyHmctsYesNo(YES);
        tmpRepresentative.getValue().setRespondentOrganisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_ONE).build());
        tmpRepresentative.getValue().setRepresentativeEmailAddress(RESPONDENT_REPRESENTATIVE_EMAIL);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verifyNocCcdServiceCaseAssignmentsCall(LoggerTestUtils.INTEGER_SIX);
        // when respondent name is found but different from the name of the represented respondent should not revoke
        // case user assignment
        tmpCaseDetails.getCaseData().setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder()
                .respondentName(RESPONDENT_NAME_ONE).build());
        tmpRepresentative.getValue().setRespRepName(RESPONDENT_NAME_TWO);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verifyNocCcdServiceCaseAssignmentsCall(LoggerTestUtils.INTEGER_SEVEN);
        // when case user assignment role is not blank but not equal to the role in representative should not revoke
        // case user assignment
        caseUserAssignmentData.getCaseUserAssignments().getFirst().setCaseRole(ROLE_SOLICITORA);
        tmpRepresentative.getValue().setRole(ROLE_SOLICITORB);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verifyNocCcdServiceCaseAssignmentsCall(LoggerTestUtils.INTEGER_EIGHT);
        // when case user assignment role is equal to the role in representative should revoke case user assignment
        tmpRepresentative.getValue().setRole(ROLE_SOLICITORA);
        when(ccdClient.revokeCaseAssignments(USER_TOKEN, caseUserAssignmentData)).thenReturn(
                String.valueOf(HTTPResponse.SC_OK));
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).hasSize(LoggerTestUtils.INTEGER_ONE)
                .isEqualTo(List.of(tmpRepresentative));
        verify(ccdClient, times(LoggerTestUtils.INTEGER_ONE)).revokeCaseAssignments(USER_TOKEN, caseUserAssignmentData);
        // when respondent name is found and same with the name of the represented respondent should revoke case user
        // assignment
        tmpRepresentative.getValue().setRole(ROLE_SOLICITORB);
        tmpRepresentative.getValue().setRespRepName(RESPONDENT_NAME_ONE);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).hasSize(LoggerTestUtils.INTEGER_ONE)
                .isEqualTo(List.of(tmpRepresentative));
        // when respondent name is found and same with the name of the represented respondent but not able to revoke
        // assignment should return empty list
        when(ccdClient.revokeCaseAssignments(eq(USER_TOKEN), any(CaseUserAssignmentData.class)))
                .thenThrow(new IOException(EXCEPTION_DUMMY_MESSAGE));
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
    }

    private void verifyNocCcdServiceCaseAssignmentsCall(int callNumber) {
        verify(nocCcdService, times(callNumber)).retrieveCaseUserAssignments(ADMIN_USER_TOKEN,
                CASE_ID_1);
    }

    @Test
    @SneakyThrows
    void theRevokeOldRepresentatives() {
        CaseDetails oldCaseDetails = new CaseDetails();
        oldCaseDetails.setCaseId(CASE_ID_1);
        oldCaseDetails.setCaseData(new CaseData());
        CallbackRequest callbackRequest = new CallbackRequest();
        callbackRequest.setCaseDetailsBefore(oldCaseDetails);
        CaseDetails newCaseDetails = new CaseDetails();
        newCaseDetails.setCaseData(new CaseData());
        callbackRequest.setCaseDetails(newCaseDetails);
        // when representatives to remove is empty should not send any email
        nocRespondentRepresentativeService.revokeOldRepresentatives(callbackRequest, USER_TOKEN);
        verify(nocNotificationService, times(LoggerTestUtils.INTEGER_ZERO))
                .sendRespondentRepresentationUpdateNotifications(eq(oldCaseDetails), anyList(), anyString());
        assertThat(newCaseDetails.getCaseData().getRepCollectionToRemove()).isNull();
        // when representatives to remove is not empty should send email
        RepresentedTypeRItem representative1 = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_ONE).value(
                RepresentedTypeR.builder().respondentId(RESPONDENT_ID_ONE).build()).build();
        oldCaseDetails.getCaseData().setRepCollection(List.of(representative1));
        RepresentedTypeRItem representative2 = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_TWO).value(
                RepresentedTypeR.builder().respondentId(RESPONDENT_ID_TWO).build()).build();
        newCaseDetails.getCaseData().setRepCollection(List.of(representative2));
        doNothing().when(nocNotificationService).sendRespondentRepresentationUpdateNotifications(
                any(CaseDetails.class), anyList(), anyString());
        nocRespondentRepresentativeService.revokeOldRepresentatives(callbackRequest, USER_TOKEN);
        verify(nocNotificationService, times(LoggerTestUtils.INTEGER_ONE))
                .sendRespondentRepresentationUpdateNotifications(any(CaseDetails.class), anyList(), anyString());
        assertThat(newCaseDetails.getCaseData().getRepCollectionToRemove()).isEmpty();
        // when unable to send removed representation emails should log error.
        doThrow(new RuntimeException(EXCEPTION_DUMMY_MESSAGE)).when(nocNotificationService)
                .sendRespondentRepresentationUpdateNotifications(any(CaseDetails.class), anyList(), anyString());
        nocRespondentRepresentativeService.revokeOldRepresentatives(callbackRequest, USER_TOKEN);
        verify(nocNotificationService, times(LoggerTestUtils.INTEGER_TWO))
                .sendRespondentRepresentationUpdateNotifications(any(CaseDetails.class), anyList(), anyString());
        LoggerTestUtils.checkLog(Level.INFO, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_ERROR_UNABLE_TO_NOTIFY_REPRESENTATION_REMOVAL);
        assertThat(newCaseDetails.getCaseData().getRepCollectionToRemove()).isEmpty();
        // when representatives revoked is not empty should remove organisation policies and noc answers
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        CaseUserAssignmentData caseUserAssignmentData = new CaseUserAssignmentData();
        caseUserAssignmentData.setCaseUserAssignments(List.of(CaseUserAssignment.builder().caseRole(ROLE_SOLICITORA)
                .build()));
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        oldCaseDetails.getCaseData().setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder()
                .respondentName(RESPONDENT_NAME_ONE).build());
        oldCaseDetails.getCaseData().setRespondentOrganisationPolicy0(OrganisationPolicy.builder().organisation(
                Organisation.builder().organisationID(ORGANISATION_ID_ONE).build()).build());
        newCaseDetails.getCaseData().setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder()
                .respondentName(RESPONDENT_NAME_ONE).build());
        newCaseDetails.getCaseData().setRespondentOrganisationPolicy0(OrganisationPolicy.builder().organisation(
                Organisation.builder().organisationID(ORGANISATION_ID_ONE).build()).build());
        newCaseDetails.setJurisdiction(JURISDICTION_EMPLOYMENT);
        newCaseDetails.setCaseTypeId(CASE_TYPE_ID_ENGLAND_WALES);
        newCaseDetails.setCaseId(CASE_ID_1);
        representative1.getValue().setRole(ROLE_SOLICITORA);
        representative1.getValue().setMyHmctsYesNo(YES);
        representative1.getValue().setRespRepName(RESPONDENT_NAME_ONE);
        representative1.getValue().setRespondentOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_ONE)
                .build());
        representative1.getValue().setRepresentativeEmailAddress(RESPONDENT_REPRESENTATIVE_EMAIL);
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(new CaseDetails());
        ccdRequest.getCaseDetails().setCaseData(newCaseDetails.getCaseData());
        when(ccdClient.startEventForCase(ADMIN_USER_TOKEN, CASE_TYPE_ID_ENGLAND_WALES, JURISDICTION_EMPLOYMENT,
                CASE_ID_1, EVENT_UPDATE_CASE_SUBMITTED)).thenReturn(ccdRequest);
        when(ccdClient.submitEventForCase(eq(ADMIN_USER_TOKEN), any(CaseData.class),
                eq(CASE_TYPE_ID_ENGLAND_WALES), eq(JURISDICTION_EMPLOYMENT), any(CCDRequest.class), eq(CASE_ID_1)))
                .thenReturn(new SubmitEvent());
        nocRespondentRepresentativeService.revokeOldRepresentatives(callbackRequest, USER_TOKEN);
        verify(nocNotificationService, times(LoggerTestUtils.INTEGER_THREE))
                .sendRespondentRepresentationUpdateNotifications(any(CaseDetails.class), anyList(), anyString());
        assertThat(newCaseDetails.getCaseData().getNoticeOfChangeAnswers0()).isEqualTo(
                NoticeOfChangeAnswers.builder().respondentName(RESPONDENT_NAME_ONE).build());
        assertThat(newCaseDetails.getCaseData().getRepCollectionToRemove()).isEqualTo(List.of(representative1));
    }

    @Test
    void theFindRepresentativesToAssign() {
        // when representative list is empty should return empty list
        CaseDetails caseDetails = new CaseDetails();
        List<RepresentedTypeRItem> representatives = new ArrayList<>();
        assertThat(nocRespondentRepresentativeService.findRepresentativesToAssign(caseDetails, representatives))
                .isEmpty();
        // when representative list doesn't have a modifiable representative should return an empty list
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        representatives.add(representative);
        assertThat(nocRespondentRepresentativeService.findRepresentativesToAssign(caseDetails, representatives))
                .isEmpty();
        // when case details is empty should return assignable representatives
        representative.setId(REPRESENTATIVE_ID_ONE);
        representative.setValue(RepresentedTypeR.builder().myHmctsYesNo(YES).respondentOrganisation(
                        Organisation.builder().organisationID(ORGANISATION_ID_ONE).build())
                .representativeEmailAddress(RESPONDENT_REPRESENTATIVE_EMAIL).build());
        List<RepresentedTypeRItem> assignableRepresentatives = nocRespondentRepresentativeService
                .findRepresentativesToAssign(null, representatives);
        assertThat(assignableRepresentatives).isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE);
        assertThat(assignableRepresentatives.getFirst()).isEqualTo(representative);
        // when case details does not have caseId should return assignable representatives
        assignableRepresentatives = nocRespondentRepresentativeService.findRepresentativesToAssign(caseDetails,
                representatives);
        assertThat(assignableRepresentatives).isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE);
        assertThat(assignableRepresentatives.getFirst()).isEqualTo(representative);
        // when there case user assignments is null should return assignable representatives
        caseDetails.setCaseId(CASE_ID_1);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(null);
        assignableRepresentatives = nocRespondentRepresentativeService.findRepresentativesToAssign(caseDetails,
                representatives);
        assertThat(assignableRepresentatives).isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE);
        assertThat(assignableRepresentatives.getFirst()).isEqualTo(representative);
        // when case user assignments doesn't have any assignment should return assignable representatives
        CaseUserAssignmentData caseUserAssignmentData = new CaseUserAssignmentData();
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        assignableRepresentatives = nocRespondentRepresentativeService.findRepresentativesToAssign(caseDetails,
                representatives);
        assertThat(assignableRepresentatives).isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE);
        assertThat(assignableRepresentatives.getFirst()).isEqualTo(representative);
        // when representative doesn't have respondent name should return assignable representatives
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().build();
        caseUserAssignmentData.setCaseUserAssignments(List.of(caseUserAssignment));
        assignableRepresentatives = nocRespondentRepresentativeService.findRepresentativesToAssign(caseDetails,
                representatives);
        assertThat(assignableRepresentatives).isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE);
        assertThat(assignableRepresentatives.getFirst()).isEqualTo(representative);
        // when case user assignment does not have respondent representative role should return assignable
        // representatives
        representative.getValue().setRespRepName(RESPONDENT_NAME_ONE);
        assignableRepresentatives = nocRespondentRepresentativeService.findRepresentativesToAssign(caseDetails,
                representatives);
        assertThat(assignableRepresentatives).isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE);
        assertThat(assignableRepresentatives.getFirst()).isEqualTo(representative);
        // when respondent name not equals to representative's respondent name
        caseUserAssignment.setCaseRole(ROLE_SOLICITORA);
        CaseData tmpCaseData = new CaseData();
        tmpCaseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder().respondentName(RESPONDENT_NAME_TWO)
                .build());
        caseDetails.setCaseData(tmpCaseData);
        assignableRepresentatives = nocRespondentRepresentativeService.findRepresentativesToAssign(caseDetails,
                representatives);
        assertThat(assignableRepresentatives).isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE);
        assertThat(assignableRepresentatives.getFirst()).isEqualTo(representative);
        // when respondent name is equal to representative's respondent name
        tmpCaseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder()
                .respondentName(RESPONDENT_NAME_ONE).build());
        assignableRepresentatives = nocRespondentRepresentativeService.findRepresentativesToAssign(caseDetails,
                representatives);
        assertThat(assignableRepresentatives).isEmpty();
    }

    private static void setAllRespondentOrganisationPolicy(CaseData tmpCaseData) {
        tmpCaseData.setRespondentOrganisationPolicy0(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_ONE).build()).build());
        tmpCaseData.setRespondentOrganisationPolicy1(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_TWO).build()).build());
        tmpCaseData.setRespondentOrganisationPolicy2(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_THREE).build()).build());
        tmpCaseData.setRespondentOrganisationPolicy3(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_FOUR).build()).build());
        tmpCaseData.setRespondentOrganisationPolicy4(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_FIVE).build()).build());
        tmpCaseData.setRespondentOrganisationPolicy5(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_SIX).build()).build());
        tmpCaseData.setRespondentOrganisationPolicy6(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_SEVEN).build()).build());
        tmpCaseData.setRespondentOrganisationPolicy7(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_EIGHT).build()).build());
        tmpCaseData.setRespondentOrganisationPolicy8(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_NINE).build()).build());
        tmpCaseData.setRespondentOrganisationPolicy9(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_TEN).build()).build());
    }

    @Test
    @SneakyThrows
    void theGrantRespondentRepresentativesAccess() {
        // when case details is empty should do nothing
        List<RepresentedTypeRItem> representatives = new ArrayList<>();
        nocRespondentRepresentativeService.grantRespondentRepresentativesAccess(null, representatives);
        verifyNoInteractions(nocCcdService);
        // when case details not has case data should do nothing
        CaseDetails caseDetails = new CaseDetails();
        nocRespondentRepresentativeService.grantRespondentRepresentativesAccess(caseDetails, representatives);
        verifyNoInteractions(nocCcdService);
        // when case details not has case id should do nothing
        CaseData tmpCaseData = new CaseData();
        caseDetails.setCaseData(tmpCaseData);
        nocRespondentRepresentativeService.grantRespondentRepresentativesAccess(caseDetails, representatives);
        verifyNoInteractions(nocCcdService);
        // when representative list is empty should do nothing
        caseDetails.setCaseId(CASE_ID_1);
        nocRespondentRepresentativeService.grantRespondentRepresentativesAccess(caseDetails, representatives);
        verifyNoInteractions(nocCcdService);
        // when representative in representative list is invalid should do nothing
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        representatives.add(representative);
        nocRespondentRepresentativeService.grantRespondentRepresentativesAccess(caseDetails, representatives);
        verifyNoInteractions(nocCcdService);
        // when there is no respondent organisation policy left to add should log error
        representative.setValue(RepresentedTypeR.builder().respondentId(RESPONDENT_ID_ONE).build());
        representative.setId(REPRESENTATIVE_ID_ONE);
        setAllRespondentOrganisationPolicy(caseDetails.getCaseData());
        nocRespondentRepresentativeService.grantRespondentRepresentativesAccess(caseDetails, representatives);
        LoggerTestUtils.checkLog(Level.ERROR, LoggerTestUtils.INTEGER_ONE, EXPECTED_ERROR_SOLICITOR_ROLE_NOT_FOUND);
        verifyNoInteractions(nocCcdService);
        // when not able to grant representative access should log error
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_ONE).build());
        respondentSumTypeItem.setId(RESPONDENT_ID_ONE);
        tmpCaseData.setRespondentCollection(List.of(respondentSumTypeItem));
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        doThrow(new GenericServiceException(EXCEPTION_DUMMY_MESSAGE)).when(nocService)
                .grantRepresentativeAccess(eq(ADMIN_USER_TOKEN), any(), any(), any(), any());
        nocRespondentRepresentativeService.grantRespondentRepresentativesAccess(caseDetails, representatives);
        verify(nocService, times(LoggerTestUtils.INTEGER_ONE)).grantRepresentativeAccess(eq(ADMIN_USER_TOKEN), any(),
                any(), any(), any());
        LoggerTestUtils.checkLog(Level.ERROR, LoggerTestUtils.INTEGER_TWO, EXPECTED_ERROR_UNABLE_TO_SET_ROLE);
        // when role successfully assigned should run setRepresentativesAccess without any error
        doNothing().when(nocService).grantRepresentativeAccess(eq(ADMIN_USER_TOKEN), any(), any(), any(), any());
        caseDetails.setCaseTypeId(CASE_TYPE_ID_ENGLAND_WALES);
        caseDetails.setJurisdiction(JURISDICTION_EMPLOYMENT);
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        tmpCaseData.setRepCollection(representatives);
        when(nocCcdService.startEventForUpdateCaseSubmitted(ADMIN_USER_TOKEN,
                CASE_TYPE_ID_ENGLAND_WALES,
                JURISDICTION_EMPLOYMENT,
                CASE_ID_1)).thenReturn(ccdRequest);
        when(ccdClient.submitEventForCase(eq(ADMIN_USER_TOKEN),
                any(CaseData.class),
                eq(CASE_TYPE_ID_ENGLAND_WALES),
                eq(JURISDICTION_EMPLOYMENT),
                any(CCDRequest.class),
                eq(CASE_ID_1))).thenReturn(new SubmitEvent());
        nocRespondentRepresentativeService.grantRespondentRepresentativesAccess(caseDetails, representatives);
        verify(nocService, times(LoggerTestUtils.INTEGER_TWO)).grantRepresentativeAccess(eq(ADMIN_USER_TOKEN), any(),
                any(), any(), any());
        assertThat(representatives.getFirst().getValue().getRole()).isEqualTo(ROLE_SOLICITORA);
    }

    @Test
    @SneakyThrows
    void theResetOrganisationPolicies() {
        // when case details is empty should not do anything
        assertDoesNotThrow(() -> nocRespondentRepresentativeService
                .resetOrganisationPolicies(null));
        // when case details not have case data should not do anything
        CaseDetails caseDetails = new CaseDetails();
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.resetOrganisationPolicies(
                caseDetails));
        // when case details not have case type id should not do anything
        caseDetails.setCaseData(new CaseData());
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.resetOrganisationPolicies(
                caseDetails));
        // when case details not have jurisdiction should not do anything
        caseDetails.setCaseTypeId(CASE_TYPE_ID_ENGLAND_WALES);
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.resetOrganisationPolicies(
                caseDetails));
        // when case details not have case id should not do anything
        caseDetails.setJurisdiction(JURISDICTION_EMPLOYMENT);
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.resetOrganisationPolicies(
                caseDetails));
        // where there is no revoked representatives should not do anything
        caseDetails.setCaseId(CASE_ID_1);
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.resetOrganisationPolicies(
                caseDetails));
        // when not able to startEventForCase should log exception

        RepresentedTypeRItem representative1 = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_ONE)
                .value(RepresentedTypeR.builder().build()).build();
        RepresentedTypeRItem representative2 = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_TWO)
                .value(RepresentedTypeR.builder().build()).build();

        caseDetails.getCaseData().setRepCollectionToRemove(List.of(representative1));
        caseDetails.getCaseData().setRepCollectionToAdd(List.of(representative2));
        when(ccdClient.startEventForCase(ADMIN_USER_TOKEN, CASE_TYPE_ID_ENGLAND_WALES, JURISDICTION_EMPLOYMENT,
                CASE_ID_1, CaseEvent.UPDATE_RESP_ORG_POLICY.name()))
                .thenThrow(new IOException(EXCEPTION_DUMMY_MESSAGE));
        assertThrows(GenericRuntimeException.class, () -> nocRespondentRepresentativeService
                .resetOrganisationPolicies(caseDetails));
        LoggerTestUtils.checkLog(Level.ERROR, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_ERROR_FAILED_TO_REMOVE_ORGANISATION_POLICIES);
        // when startEventForCase returns null value should log exception
        doReturn(null).when(ccdClient).startEventForCase(ADMIN_USER_TOKEN, CASE_TYPE_ID_ENGLAND_WALES,
                JURISDICTION_EMPLOYMENT, CASE_ID_1, CaseEvent.UPDATE_RESP_ORG_POLICY.name());
        GenericRuntimeException genericRuntimeException = assertThrows(GenericRuntimeException.class,
                () -> nocRespondentRepresentativeService.resetOrganisationPolicies(caseDetails));
        assertThat(genericRuntimeException.getMessage()).isEqualTo(
                EXPECTED_EXCEPTION_UNABLE_TO_START_EVENT_WITHOUT_CASE_ID);
        LoggerTestUtils.checkLog(Level.ERROR, LoggerTestUtils.INTEGER_TWO,
                EXPECTED_ERROR_FAILED_TO_REMOVE_ORGANISATION_POLICIES);
        // when startEventForCase returns CCD request without case details should log exception
        CCDRequest ccdRequest = new CCDRequest();
        doReturn(ccdRequest).when(ccdClient).startEventForCase(ADMIN_USER_TOKEN, CASE_TYPE_ID_ENGLAND_WALES,
                JURISDICTION_EMPLOYMENT, CASE_ID_1, CaseEvent.UPDATE_RESP_ORG_POLICY.name());
        genericRuntimeException = assertThrows(GenericRuntimeException.class,
                () -> nocRespondentRepresentativeService.resetOrganisationPolicies(caseDetails));
        assertThat(genericRuntimeException.getMessage()).isEqualTo(
                EXPECTED_EXCEPTION_UNABLE_TO_START_EVENT_WITHOUT_CASE_ID);
        LoggerTestUtils.checkLog(Level.ERROR, LoggerTestUtils.INTEGER_THREE,
                EXPECTED_ERROR_FAILED_TO_REMOVE_ORGANISATION_POLICIES);
        // when startEventForCase returns case details without id should log exception
        ccdRequest.setCaseDetails(new  CaseDetails());
        genericRuntimeException = assertThrows(GenericRuntimeException.class,
                () -> nocRespondentRepresentativeService.resetOrganisationPolicies(caseDetails));
        assertThat(genericRuntimeException.getMessage()).isEqualTo(
                EXPECTED_EXCEPTION_UNABLE_TO_START_EVENT_WITHOUT_CASE_ID);
        LoggerTestUtils.checkLog(Level.ERROR, LoggerTestUtils.INTEGER_FOUR,
                EXPECTED_ERROR_FAILED_TO_REMOVE_ORGANISATION_POLICIES);
        // when startEventForCase returns case details without case data should log exception
        ccdRequest.setCaseDetails(new  CaseDetails());
        ccdRequest.getCaseDetails().setCaseId(CASE_ID_1);
        genericRuntimeException = assertThrows(GenericRuntimeException.class,
                () -> nocRespondentRepresentativeService.resetOrganisationPolicies(caseDetails));
        assertThat(genericRuntimeException.getMessage()).isEqualTo(
                EXPECTED_EXCEPTION_UNABLE_TO_START_EVENT_WITH_CASE_ID);
        LoggerTestUtils.checkLog(Level.ERROR, LoggerTestUtils.INTEGER_FIVE,
                EXPECTED_ERROR_FAILED_TO_REMOVE_ORGANISATION_POLICIES);
        // when startEventForCase returns valid case details should run submit event and update repCollectionToRemove
        // and repCollectionToAdd collections on CCD Request
        ccdRequest.getCaseDetails().setCaseData(new CaseData());
        doReturn(new SubmitEvent()).when(ccdClient).submitEventForCase(eq(ADMIN_USER_TOKEN), any(CaseData.class),
                eq(CASE_TYPE_ID_ENGLAND_WALES), eq(JURISDICTION_EMPLOYMENT), any(CCDRequest.class), eq(CASE_ID_1));
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.resetOrganisationPolicies(caseDetails));
        assertThat(ccdRequest.getCaseDetails().getCaseData().getRepCollectionToRemove()).isEqualTo(caseDetails
                .getCaseData().getRepCollectionToRemove());
        assertThat(ccdRequest.getCaseDetails().getCaseData().getRepCollectionToRemove()).isEqualTo(caseDetails
                .getCaseData().getRepCollectionToRemove());
    }

    @Test
    @SneakyThrows
    void theRemoveConflictingClaimantRepresentation() {
        // when case details empty should not revoke claimant representative case assignment and return null.
        assertThat(nocRespondentRepresentativeService.removeConflictingClaimantRepresentation(null)).isNull();
        // when case details not have case id should not revoke claimant representative case assignment and return null.
        CaseDetails caseDetails = new CaseDetails();
        assertThat(nocRespondentRepresentativeService.removeConflictingClaimantRepresentation(caseDetails)).isNull();
        verify(nocCcdService, times(LoggerTestUtils.INTEGER_ZERO)).revokeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        // when case details not have case type id should not revoke claimant representative case assignment and
        // return null.
        caseDetails.setCaseId(CASE_ID_1);
        assertThat(nocRespondentRepresentativeService.removeConflictingClaimantRepresentation(caseDetails)).isNull();
        verify(nocCcdService, times(LoggerTestUtils.INTEGER_ZERO)).revokeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        // when case details not have jurisdiction should not revoke claimant representative case assignment and
        // return null.
        caseDetails.setCaseTypeId(CASE_TYPE_ID_ENGLAND_WALES);
        nocRespondentRepresentativeService.removeConflictingClaimantRepresentation(caseDetails);
        verify(nocCcdService, times(LoggerTestUtils.INTEGER_ZERO)).revokeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        // when case details not have case data should not revoke claimant representative case assignment and
        // return null.
        caseDetails.setJurisdiction(JURISDICTION_EMPLOYMENT);
        nocRespondentRepresentativeService.removeConflictingClaimantRepresentation(caseDetails);
        verify(nocCcdService, times(LoggerTestUtils.INTEGER_ZERO)).revokeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        // when case details not have respondent representative collection should not revoke claimant representative
        // case assignment and return case data.
        CaseData tmpCaseData = new CaseData();
        caseDetails.setCaseData(tmpCaseData);
        assertThat(nocRespondentRepresentativeService.removeConflictingClaimantRepresentation(caseDetails))
                .isEqualTo(tmpCaseData);
        verify(nocCcdService, times(LoggerTestUtils.INTEGER_ZERO)).revokeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        // when claimant represented question is NO should not revoke claimant representative case assignment and
        // return case data.
        RepresentedTypeRItem respondentRepresentative = RepresentedTypeRItem.builder().value(RepresentedTypeR.builder()
                        .nameOfRepresentative(REPRESENTATIVE_NAME)
                        .representativeEmailAddress(RESPONDENT_REPRESENTATIVE_EMAIL)
                        .respondentOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_ONE).build())
                        .build()).id(REPRESENTATIVE_ID_ONE).build();
        tmpCaseData.setRepCollection(List.of(respondentRepresentative));
        tmpCaseData.setClaimantRepresentedQuestion(NO);
        assertThat(nocRespondentRepresentativeService.removeConflictingClaimantRepresentation(caseDetails))
                .isEqualTo(tmpCaseData);
        verify(nocCcdService, times(LoggerTestUtils.INTEGER_ZERO)).revokeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        // when there is no matching claimant and respondent representative organisation ids should not revoke claimant
        // representative case assignment and return case data.
        RepresentedTypeC claimantRepresentative = RepresentedTypeC.builder().nameOfRepresentative(REPRESENTATIVE_NAME)
                .representativeEmailAddress(CLAIMANT_REPRESENTATIVE_EMAIL).myHmctsOrganisation(Organisation.builder()
                        .organisationID(ORGANISATION_ID_TWO).build()).build();
        tmpCaseData.setRepresentativeClaimantType(claimantRepresentative);
        tmpCaseData.setClaimantRepresentedQuestion(YES);
        tmpCaseData.setClaimantRepresentativeRemoved(NO);
        assertThat(nocRespondentRepresentativeService.removeConflictingClaimantRepresentation(caseDetails))
                .isEqualTo(tmpCaseData);
        verify(nocCcdService, times(LoggerTestUtils.INTEGER_ZERO)).revokeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        // when claimant representative email is same with respondent representative email should revoke claimant
        // representative case assignment and return case data.
        tmpCaseData.getRepresentativeClaimantType().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_1);
        tmpCaseData.getRepCollection().getFirst().getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_1);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        doNothing().when(nocCcdService).revokeClaimantRepresentation(ADMIN_USER_TOKEN, caseDetails);
        doNothing().when(nocNotificationService).notifyClaimantOfRepresentationRemoval(any(CaseDetails.class));
        assertThat(nocRespondentRepresentativeService.removeConflictingClaimantRepresentation(caseDetails))
                .isEqualTo(tmpCaseData);
        verify(nocCcdService, times(LoggerTestUtils.INTEGER_ONE)).revokeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        assertThat(tmpCaseData.getClaimantRepresentedQuestion()).isEqualTo(NO);
        assertThat(tmpCaseData.getClaimantRepresentativeRemoved()).isEqualTo(YES);
        assertThat(tmpCaseData.getRepresentativeClaimantType()).isNull();
        OrganisationPolicy tmpClaimantOrganisationPolicy = OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                ROLE_CLAIMANT_SOLICITOR).build();
        assertThat(tmpCaseData.getClaimantRepresentativeOrganisationPolicy()).isEqualTo(tmpClaimantOrganisationPolicy);
        // when claimant representative organisation id is same with respondent representative organisation id should
        // revoke claimant representative case assignment and return case data.
        respondentRepresentative.getValue().setRepresentativeEmailAddress(RESPONDENT_REPRESENTATIVE_EMAIL);
        respondentRepresentative.getValue().getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_ONE);
        claimantRepresentative.setRepresentativeEmailAddress(CLAIMANT_REPRESENTATIVE_EMAIL);
        claimantRepresentative.getMyHmctsOrganisation().setOrganisationID(ORGANISATION_ID_ONE);
        tmpCaseData.setRepresentativeClaimantType(claimantRepresentative);
        tmpCaseData.setClaimantRepresentedQuestion(YES);
        tmpCaseData.setClaimantRepresentativeRemoved(NO);
        assertThat(nocRespondentRepresentativeService.removeConflictingClaimantRepresentation(caseDetails))
                .isEqualTo(tmpCaseData);
        verify(nocCcdService, times(LoggerTestUtils.INTEGER_TWO)).revokeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        assertThat(tmpCaseData.getClaimantRepresentedQuestion()).isEqualTo(NO);
        assertThat(tmpCaseData.getClaimantRepresentativeRemoved()).isEqualTo(YES);
        assertThat(tmpCaseData.getRepresentativeClaimantType()).isNull();
        assertThat(tmpCaseData.getClaimantRepresentativeOrganisationPolicy()).isEqualTo(tmpClaimantOrganisationPolicy);
    }

    @Test
    void theFindRepresentativesByToken() {
        // when user token is empty should return null
        CaseDetails caseDetails = new CaseDetails();
        assertThat(nocRespondentRepresentativeService.findRepresentativesByToken(StringUtils.EMPTY, caseDetails))
                .isEmpty();
        // when case details is empty should return null
        assertThat(nocRespondentRepresentativeService.findRepresentativesByToken(USER_TOKEN, null)).isEmpty();
        // when case details not have case id should return null
        assertThat(nocRespondentRepresentativeService.findRepresentativesByToken(USER_TOKEN, caseDetails)).isEmpty();
        // when case data is empty should return null
        caseDetails.setCaseId(CASE_ID_1);
        assertThat(nocRespondentRepresentativeService.findRepresentativesByToken(USER_TOKEN, caseDetails)).isEmpty();
        // when representative collection is empty should return null
        CaseData tmpCaseData = new CaseData();
        caseDetails.setCaseData(tmpCaseData);
        assertThat(nocRespondentRepresentativeService.findRepresentativesByToken(USER_TOKEN, caseDetails)).isEmpty();
        // when user details are empty should return null
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_ONE).value(
                RepresentedTypeR.builder().build()).build();
        tmpCaseData.setRepCollection(List.of(representative));
        when(userIdamService.getUserDetails(USER_TOKEN)).thenReturn(null);
        assertThat(nocRespondentRepresentativeService.findRepresentativesByToken(USER_TOKEN, caseDetails)).isEmpty();
        // when user details not have id should return null
        UserDetails userDetails = new UserDetails();
        when(userIdamService.getUserDetails(USER_TOKEN)).thenReturn(userDetails);
        assertThat(nocRespondentRepresentativeService.findRepresentativesByToken(USER_TOKEN, caseDetails)).isEmpty();
        // when there is no case user assignment data should return null
        userDetails.setUid(REPRESENTATIVE_ID_ONE);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(null);
        assertThat(nocRespondentRepresentativeService.findRepresentativesByToken(USER_TOKEN, caseDetails)).isEmpty();
        // when case user assignment data not has any case user assignment should return null
        CaseUserAssignmentData caseUserAssignmentData = new CaseUserAssignmentData();
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        assertThat(nocRespondentRepresentativeService.findRepresentativesByToken(USER_TOKEN, caseDetails)).isEmpty();
        // when case user assignment role is not respondent solicitor should return null
        CaseUserAssignment caseUserAssignment = new CaseUserAssignment();
        caseUserAssignment.setCaseRole(ROLE_CLAIMANT_SOLICITOR);
        caseUserAssignmentData.setCaseUserAssignments(List.of(caseUserAssignment));
        assertThat(nocRespondentRepresentativeService.findRepresentativesByToken(USER_TOKEN, caseDetails)).isEmpty();
        // when case user assignment role user id is not same with representative user id should return null
        caseUserAssignment.setCaseRole(ROLE_SOLICITORA);
        caseUserAssignment.setUserId(REPRESENTATIVE_ID_TWO);
        assertThat(nocRespondentRepresentativeService.findRepresentativesByToken(USER_TOKEN, caseDetails)).isEmpty();
        // when representative not found by role should return null
        caseUserAssignment.setUserId(REPRESENTATIVE_ID_ONE);
        assertThat(nocRespondentRepresentativeService.findRepresentativesByToken(USER_TOKEN, caseDetails)).isEmpty();
        // when representative found by role should return that representative
        representative.getValue().setRole(ROLE_SOLICITORA);
        caseUserAssignment.setCaseRole(ROLE_SOLICITORA);
        assertThat(nocRespondentRepresentativeService.findRepresentativesByToken(USER_TOKEN, caseDetails))
                .isEqualTo(List.of(representative));
        // when gets exception while retrieving case user assignment should log that exception and return null
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenThrow(
                new CcdInputOutputException(EXCEPTION_DUMMY_MESSAGE, new IOException(EXCEPTION_DUMMY_MESSAGE)));
        assertThat(nocRespondentRepresentativeService.findRepresentativesByToken(USER_TOKEN, caseDetails)).isEmpty();
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_FAILED_TO_RETRIEVE_CASE_ASSIGNMENTS);
    }

    @Test
    @SneakyThrows
    void theAddNewRepresentatives() {
        Organisation organisation = Organisation.builder().build();
        // when callback request is empty should not do anything
        nocRespondentRepresentativeService.addNewRepresentatives(null);
        verify(nocService, times(LoggerTestUtils.INTEGER_ZERO)).grantRepresentativeAccess(ADMIN_USER_TOKEN,
                RESPONDENT_EMAIL, CASE_ID_1, organisation, ROLE_SOLICITORA);
        // when callback request does not have old case details should not do anything
        CallbackRequest callbackRequest = CallbackRequest.builder().build();
        nocRespondentRepresentativeService.addNewRepresentatives(callbackRequest);
        verify(nocService, times(LoggerTestUtils.INTEGER_ZERO)).grantRepresentativeAccess(ADMIN_USER_TOKEN,
                RESPONDENT_EMAIL, CASE_ID_1, organisation, ROLE_SOLICITORA);
        // when callback request does not have new case details should not do anything
        CaseDetails oldCaseDetails = new CaseDetails();
        callbackRequest.setCaseDetailsBefore(oldCaseDetails);
        nocRespondentRepresentativeService.addNewRepresentatives(callbackRequest);
        verify(nocService, times(LoggerTestUtils.INTEGER_ZERO)).grantRepresentativeAccess(ADMIN_USER_TOKEN,
                RESPONDENT_EMAIL, CASE_ID_1, organisation, ROLE_SOLICITORA);
        // when old case details does not have case data should do nothing
        CaseDetails newCaseDetails = new CaseDetails();
        callbackRequest.setCaseDetails(newCaseDetails);
        nocRespondentRepresentativeService.addNewRepresentatives(callbackRequest);
        verify(nocService, times(LoggerTestUtils.INTEGER_ZERO)).grantRepresentativeAccess(ADMIN_USER_TOKEN,
                RESPONDENT_EMAIL, CASE_ID_1, organisation, ROLE_SOLICITORA);
        // when new case details does not have case data should do nothing
        CaseData oldCaseData = new CaseData();
        oldCaseDetails.setCaseData(oldCaseData);
        nocRespondentRepresentativeService.addNewRepresentatives(callbackRequest);
        verify(nocService, times(LoggerTestUtils.INTEGER_ZERO)).grantRepresentativeAccess(ADMIN_USER_TOKEN,
                RESPONDENT_EMAIL, CASE_ID_1, organisation, ROLE_SOLICITORA);
        // when new case details does not have respondent collection should call grantRespondentRepresentativesAccess
        CaseData newCaseData = new CaseData();
        newCaseDetails.setCaseData(newCaseData);
        nocRespondentRepresentativeService.addNewRepresentatives(callbackRequest);
        verify(nocService, times(LoggerTestUtils.INTEGER_ZERO)).grantRepresentativeAccess(ADMIN_USER_TOKEN,
                RESPONDENT_EMAIL, CASE_ID_1, organisation, ROLE_SOLICITORA);
        // when new case details exists should grant respondent representatives access
        newCaseData.setRepCollection(List.of(RepresentedTypeRItem.builder().build()));
        nocRespondentRepresentativeService.addNewRepresentatives(callbackRequest);
        verify(nocService, times(LoggerTestUtils.INTEGER_ZERO)).grantRepresentativeAccess(ADMIN_USER_TOKEN,
                RESPONDENT_EMAIL, CASE_ID_1, organisation, ROLE_SOLICITORA);
    }

    @Test
    void theFindRepresentativesToRemove() {
        List<RepresentedTypeRItem> oldRepresentatives = new ArrayList<>();
        List<RepresentedTypeRItem> newRepresentatives = new ArrayList<>();
        // when old representatives is empty should return empty list
        assertThat(nocRespondentRepresentativeService.findRepresentativesToRemove(
                oldRepresentatives, newRepresentatives)).isEmpty();
        // when new representatives is empty should return old representative list
        oldRepresentatives.add(RepresentedTypeRItem.builder().build());
        assertThat(nocRespondentRepresentativeService.findRepresentativesToRemove(
                oldRepresentatives, newRepresentatives)).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE)
                .isEqualTo(oldRepresentatives);
        // when old representatives list has invalid representative should return empty list
        newRepresentatives.add(RepresentedTypeRItem.builder().build());
        assertThat(nocRespondentRepresentativeService.findRepresentativesToRemove(
                oldRepresentatives, newRepresentatives)).isEmpty();
        // when old representatives list has valid representative but there is no new representative should return
        // that valid representative in a list
        RepresentedTypeRItem validOldRepresentative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_ONE).value(
                RepresentedTypeR.builder().respRepName(RESPONDENT_NAME_ONE).build()).build();
        oldRepresentatives.clear();
        oldRepresentatives.add(validOldRepresentative);
        List<RepresentedTypeRItem> representativesToRemove = nocRespondentRepresentativeService
                .findRepresentativesToRemove(oldRepresentatives, newRepresentatives);
        assertThat(representativesToRemove).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(representativesToRemove.getFirst()).isEqualTo(validOldRepresentative);
        assertThat(newRepresentatives).hasSize(NumberUtils.INTEGER_ONE);
        // when new representative list has invalid representative should return list of valid old representatives
        representativesToRemove = nocRespondentRepresentativeService.findRepresentativesToRemove(
                oldRepresentatives, newRepresentatives);
        assertThat(representativesToRemove).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(representativesToRemove.getFirst()).isEqualTo(validOldRepresentative);
        assertThat(newRepresentatives).hasSize(NumberUtils.INTEGER_ONE);
        // when new representative list has valid representative that doesn't represent same respondent in old
        // representatives should return list of non-representing old representatives
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        newRepresentatives.clear();
        RepresentedTypeRItem validNewRepresentative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_ONE).value(
                RepresentedTypeR.builder().respRepName(RESPONDENT_NAME_ONE)
                        .representativeEmailAddress(REPRESENTATIVE_EMAIL_1).build()).build();
        newRepresentatives.add(validNewRepresentative);
        AccountIdByEmailResponse accountIdByEmailResponse = new AccountIdByEmailResponse();
        ResponseEntity<AccountIdByEmailResponse> responseEntity = new ResponseEntity<>(accountIdByEmailResponse,
                HttpStatus.OK);
        accountIdByEmailResponse.setUserIdentifier(REPRESENTATIVE_ID_ONE);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, S2S_TOKEN, REPRESENTATIVE_EMAIL_1))
                .thenReturn(responseEntity);
        representativesToRemove = nocRespondentRepresentativeService.findRepresentativesToRemove(
                oldRepresentatives, newRepresentatives);
        assertThat(representativesToRemove).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(representativesToRemove.getFirst()).isEqualTo(validOldRepresentative);
        assertThat(newRepresentatives).hasSize(NumberUtils.INTEGER_ONE);
        // when both new and old representatives represent the same respondent but old and new representatives not have
        // organisation and email should return empty list
        validNewRepresentative.getValue().setRespRepName(RESPONDENT_NAME_ONE);
        validNewRepresentative.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_2);
        representativesToRemove = nocRespondentRepresentativeService.findRepresentativesToRemove(
                oldRepresentatives, newRepresentatives);
        assertThat(representativesToRemove).isEmpty();
        assertThat(newRepresentatives).hasSize(NumberUtils.INTEGER_ONE);
        // when old representative has organisation but new not should return list of old representative
        validNewRepresentative.getValue().setRespRepName(RESPONDENT_NAME_TWO);
        validNewRepresentative.getValue().setRespondentOrganisation(Organisation.builder().build());
        representativesToRemove = nocRespondentRepresentativeService.findRepresentativesToRemove(
                oldRepresentatives, newRepresentatives);
        assertThat(representativesToRemove).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(representativesToRemove.getFirst()).isEqualTo(validOldRepresentative);
        assertThat(newRepresentatives).hasSize(NumberUtils.INTEGER_ONE);
        // when old representative has email but new not should return list of old representative
        validNewRepresentative.getValue().setRespondentOrganisation(null);
        validNewRepresentative.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_1_CAPITALISED);
        representativesToRemove = nocRespondentRepresentativeService.findRepresentativesToRemove(
                oldRepresentatives, newRepresentatives);
        assertThat(representativesToRemove).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(representativesToRemove.getFirst()).isEqualTo(validOldRepresentative);
        assertThat(newRepresentatives).hasSize(NumberUtils.INTEGER_ONE);
    }

    @Test
    void theIsHmctsOrganisationUser() {
        // when email is blank should return false
        assertThat(nocRespondentRepresentativeService.isHmctsOrganisationUser(StringUtils.EMPTY)).isFalse();
        // when response entity is empty should return false
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, S2S_TOKEN, REPRESENTATIVE_EMAIL_1))
                .thenReturn(null);
        assertThat(nocRespondentRepresentativeService.isHmctsOrganisationUser(REPRESENTATIVE_EMAIL_1)).isFalse();
        // when response entity does not have status code should return false
        ResponseEntity<AccountIdByEmailResponse> responseEntity = new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, S2S_TOKEN, REPRESENTATIVE_EMAIL_1))
                .thenReturn(responseEntity);
        assertThat(nocRespondentRepresentativeService.isHmctsOrganisationUser(REPRESENTATIVE_EMAIL_1)).isFalse();
        // when response entity body is empty should return false
        responseEntity = new ResponseEntity<>(null, HttpStatus.OK);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, S2S_TOKEN, REPRESENTATIVE_EMAIL_1))
                .thenReturn(responseEntity);
        assertThat(nocRespondentRepresentativeService.isHmctsOrganisationUser(REPRESENTATIVE_EMAIL_1)).isFalse();
        // when response entity body does not have account id should return false
        AccountIdByEmailResponse accountIdByEmailResponse = new AccountIdByEmailResponse();
        responseEntity = new ResponseEntity<>(accountIdByEmailResponse, HttpStatus.OK);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, S2S_TOKEN, REPRESENTATIVE_EMAIL_1))
                .thenReturn(responseEntity);
        assertThat(nocRespondentRepresentativeService.isHmctsOrganisationUser(REPRESENTATIVE_EMAIL_1)).isFalse();
        // when response entity body has account id should return true
        accountIdByEmailResponse.setUserIdentifier(REPRESENTATIVE_ID_ONE);
        assertThat(nocRespondentRepresentativeService.isHmctsOrganisationUser(REPRESENTATIVE_EMAIL_1)).isTrue();
    }

    @Test
    void theHasHmctsRepresentativeEmailChanged() {
        // when old and new representative emails are same should return false
        RepresentedTypeRItem oldRepresentative = RepresentedTypeRItem.builder().value(RepresentedTypeR.builder()
                        .representativeEmailAddress(REPRESENTATIVE_EMAIL_1).build()).build();
        RepresentedTypeRItem newRepresentative = RepresentedTypeRItem.builder().value(RepresentedTypeR.builder()
                .representativeEmailAddress(REPRESENTATIVE_EMAIL_1).build()).build();
        assertThat(nocRespondentRepresentativeService.hasHmctsRepresentativeEmailChanged(oldRepresentative,
                newRepresentative)).isFalse();
        // when old and new representative emails are different but new representative is not a hmcts organisation
        // user should return false
        oldRepresentative.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_1);
        newRepresentative.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_2);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, S2S_TOKEN, REPRESENTATIVE_EMAIL_2))
                .thenReturn(null);
        assertThat(nocRespondentRepresentativeService.hasHmctsRepresentativeEmailChanged(oldRepresentative,
                newRepresentative)).isFalse();
        // when old and new representative emails are different and new representative exists in hmcts organisation
        // should return true
        AccountIdByEmailResponse accountIdByEmailResponse = new AccountIdByEmailResponse();
        ResponseEntity<AccountIdByEmailResponse> responseEntity = new ResponseEntity<>(accountIdByEmailResponse,
                HttpStatus.OK);
        accountIdByEmailResponse.setUserIdentifier(REPRESENTATIVE_ID_ONE);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, S2S_TOKEN, REPRESENTATIVE_EMAIL_2))
                .thenReturn(responseEntity);
        assertThat(nocRespondentRepresentativeService.hasHmctsRepresentativeEmailChanged(oldRepresentative,
                newRepresentative)).isTrue();
    }

    @Test
    @SneakyThrows
    void theRevokeRespondentRepresentatives() {
        // when case user assignments data is empty should not revoke case user assignments
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(null);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(CASE_ID_1);
        nocRespondentRepresentativeService.revokeRespondentRepresentatives(caseDetails, new  ArrayList<>());
        verifyNoInteractions(ccdClient);
        // when case user assignments data does not have any assignment should not revoke case user assignments
        CaseUserAssignmentData caseUserAssignmentsData = new CaseUserAssignmentData();
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1))
                .thenReturn(caseUserAssignmentsData);
        nocRespondentRepresentativeService.revokeRespondentRepresentatives(caseDetails, new  ArrayList<>());
        verifyNoInteractions(ccdClient);
        // when role in case user assignments is a claimant role should not revoke case user assignments
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().caseRole(ROLE_CLAIMANT_SOLICITOR).build();
        caseUserAssignmentsData.setCaseUserAssignments(List.of(caseUserAssignment));
        nocRespondentRepresentativeService.revokeRespondentRepresentatives(caseDetails, new  ArrayList<>());
        verifyNoInteractions(ccdClient);
        // when representative not found should not revoke case user assignment
        caseUserAssignment.setCaseRole(ROLE_SOLICITORA);
        CaseData tmpCaseData = new CaseData();
        RepresentedTypeR representedTypeR = RepresentedTypeR.builder().role(ROLE_SOLICITORA).build();
        RepresentedTypeRItem representative = new  RepresentedTypeRItem();
        representative.setValue(representedTypeR);
        representative.setId(REPRESENTATIVE_ID_ONE);
        tmpCaseData.setRepCollection(List.of(representative));
        caseDetails.setCaseData(tmpCaseData);
        nocRespondentRepresentativeService.revokeRespondentRepresentatives(caseDetails, new  ArrayList<>());
        verifyNoInteractions(ccdClient);
        // when representative is found should revoke case user assignment
        when(ccdClient.revokeCaseAssignments(eq(ADMIN_USER_TOKEN), any(CaseUserAssignmentData.class)))
                .thenReturn(StringUtils.EMPTY);
        assertThat(nocRespondentRepresentativeService.revokeRespondentRepresentatives(caseDetails,
                List.of(representative))).isNotEmpty().isEqualTo(List.of(representative));
        verify(ccdClient, times(LoggerTestUtils.INTEGER_ONE)).revokeCaseAssignments(eq(ADMIN_USER_TOKEN),
                any(CaseUserAssignmentData.class));
        // when representative is found but not able to revoke case user assignment should return empty list.
        when(ccdClient.revokeCaseAssignments(eq(ADMIN_USER_TOKEN), any(CaseUserAssignmentData.class)))
                .thenThrow(new IOException(EXCEPTION_DUMMY_MESSAGE));
        assertThat(nocRespondentRepresentativeService.revokeRespondentRepresentatives(caseDetails,
                List.of(representative))).isEmpty();
        verify(ccdClient, times(LoggerTestUtils.INTEGER_TWO)).revokeCaseAssignments(eq(ADMIN_USER_TOKEN),
                any(CaseUserAssignmentData.class));
    }

    @Test
    @SneakyThrows
    void theRevokeRespondentRepresentativesWithSameOrganisationAsClaimant() {
        // when case details is empty should not revoke any respondent representative
        nocRespondentRepresentativeService.revokeRespondentRepresentativesWithSameOrganisationAsClaimant(null);
        verifyNoInteractions(ccdClient);
        // when case details not have case id should not revoke any respondent representative
        CaseDetails caseDetails = new CaseDetails();
        nocRespondentRepresentativeService.revokeRespondentRepresentativesWithSameOrganisationAsClaimant(caseDetails);
        verifyNoInteractions(ccdClient);
        // when case details not have case data should not revoke any respondent representative
        caseDetails.setCaseId(CASE_ID_1);
        nocRespondentRepresentativeService.revokeRespondentRepresentativesWithSameOrganisationAsClaimant(caseDetails);
        verifyNoInteractions(ccdClient);
        // when case data not has any claimant representative should not revoke any respondent representative
        CaseData tmpCaseData = new CaseData();
        caseDetails.setCaseData(tmpCaseData);
        nocRespondentRepresentativeService.revokeRespondentRepresentativesWithSameOrganisationAsClaimant(caseDetails);
        verifyNoInteractions(ccdClient);
        // when case data not has any respondent representative should not revoke any respondent representative
        RepresentedTypeC claimantRepresentative = RepresentedTypeC.builder().build();
        tmpCaseData.setRepresentativeClaimantType(claimantRepresentative);
        tmpCaseData.setRepCollection(new ArrayList<>());
        nocRespondentRepresentativeService.revokeRespondentRepresentativesWithSameOrganisationAsClaimant(caseDetails);
        verifyNoInteractions(ccdClient);
        // when claimant representative does not have hmcts organisation id should not revoke any respondent
        // representative
        RepresentedTypeRItem respondentRepresentative = new  RepresentedTypeRItem();
        respondentRepresentative.setId(REPRESENTATIVE_ID_ONE);
        tmpCaseData.getRepCollection().add(respondentRepresentative);
        nocRespondentRepresentativeService.revokeRespondentRepresentativesWithSameOrganisationAsClaimant(caseDetails);
        verifyNoInteractions(ccdClient);
        // when there is no respondent representative with claimant representative organisation should not revoke any
        // respondent representative
        claimantRepresentative.setRepresentativeId(REPRESENTATIVE_ID_ONE);
        claimantRepresentative.setMyHmctsOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_ONE)
                .build());
        nocRespondentRepresentativeService.revokeRespondentRepresentativesWithSameOrganisationAsClaimant(caseDetails);
        verifyNoInteractions(ccdClient);
        // when there is respondent representative with same organisation with representative should revoke respondent
        // representatives and reset organisation policies
        respondentRepresentative.setValue(RepresentedTypeR.builder().respondentOrganisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_ONE).build()).respRepName(RESPONDENT_NAME_ONE).build());
        tmpCaseData.setRespondentOrganisationPolicy0(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_ONE).build()).orgPolicyCaseAssignedRole(ROLE_SOLICITORA).build());
        tmpCaseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder().respondentName(RESPONDENT_NAME_ONE)
                .build());
        claimantRepresentative.setMyHmctsOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_ONE)
                .build());
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder().caseUserAssignments(
                List.of(CaseUserAssignment.builder().caseId(CASE_ID_1).caseRole(ROLE_SOLICITORA)
                        .organisationId(ORGANISATION_ID_ONE).build())).build();
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        doNothing().when(nocNotificationService).notifyRespondentOfRepresentativeUpdate(any(CaseDetails.class),
                any(RespondentSumTypeItem.class));
        nocRespondentRepresentativeService.revokeRespondentRepresentativesWithSameOrganisationAsClaimant(caseDetails);
        verify(ccdClient, times(LoggerTestUtils.INTEGER_ONE)).revokeCaseAssignments(eq(ADMIN_USER_TOKEN),
                any(CaseUserAssignmentData.class));
        assertThat(tmpCaseData.getRespondentOrganisationPolicy0()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITORA).build());
    }

    @Test
    @SneakyThrows
    void theValidateRespondentRepresentativesOrganisationMatch() {
        // when there is no representative in rep collection should return empty list
        CaseData tmpCaseData = new CaseData();
        tmpCaseData.setRepCollection(new ArrayList<>());
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(CASE_ID_1);
        caseDetails.setCaseData(tmpCaseData);
        assertThat(nocRespondentRepresentativeService.validateRespondentRepresentativesOrganisationMatch(caseDetails))
                .isEmpty();
        // when representative in rep collection is not valid should return empty list
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        tmpCaseData.getRepCollection().add(representative);
        assertThat(nocRespondentRepresentativeService.validateRespondentRepresentativesOrganisationMatch(caseDetails))
                .isEmpty();
        // when representative not has email address should return empty list
        representative.setId(REPRESENTATIVE_ID_ONE);
        representative.setValue(RepresentedTypeR.builder().build());
        assertThat(nocRespondentRepresentativeService.validateRespondentRepresentativesOrganisationMatch(caseDetails))
                .isEmpty();
        // when representative not has my hmcts selection should return empty list
        representative.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_1_CAPITALISED);
        assertThat(nocRespondentRepresentativeService.validateRespondentRepresentativesOrganisationMatch(caseDetails))
                .isEmpty();
        // when representative not has any organisation should return empty list
        representative.getValue().setMyHmctsYesNo(YES);
        assertThat(nocRespondentRepresentativeService.validateRespondentRepresentativesOrganisationMatch(caseDetails))
                .isEmpty();
        // when organisation response and representative organisation not matches should return error
        representative.getValue().setNameOfRepresentative(REPRESENTATIVE_NAME);
        representative.getValue().setRespondentOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_ONE)
                .build());
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        AccountIdByEmailResponse accountIdByEmailResponse = new AccountIdByEmailResponse();
        accountIdByEmailResponse.setUserIdentifier(USER_ID);
        when(nocService.findUserByEmail(ADMIN_USER_TOKEN, REPRESENTATIVE_EMAIL_1_CAPITALISED, CASE_ID_1))
                .thenReturn(accountIdByEmailResponse);
        OrganisationsResponse organisationsResponse = OrganisationsResponse.builder()
                .organisationIdentifier(ORGANISATION_ID_TWO).build();
        when(nocService.findOrganisationByUserId(ADMIN_USER_TOKEN, USER_ID, CASE_ID_1))
                .thenReturn(organisationsResponse);
        assertThat(nocRespondentRepresentativeService.validateRespondentRepresentativesOrganisationMatch(caseDetails))
                .isNotEmpty().contains(EXPECTED_ERROR_SELECTED_ORGANISATION_REPRESENTATIVE_ORGANISATION_NOT_MATCHES);
        // when organisation response and representative organisation matches should return empty list
        organisationsResponse.setOrganisationIdentifier(ORGANISATION_ID_ONE);
        assertThat(nocRespondentRepresentativeService.validateRespondentRepresentativesOrganisationMatch(caseDetails))
                .isEmpty();
        // when user response not found should return empty list
        when(nocService.findUserByEmail(ADMIN_USER_TOKEN, REPRESENTATIVE_EMAIL_1_CAPITALISED, CASE_ID_1))
                .thenThrow(new GenericServiceException(EXCEPTION_DUMMY_MESSAGE));
        assertThat(nocRespondentRepresentativeService.validateRespondentRepresentativesOrganisationMatch(caseDetails))
                .isEmpty();
    }

    @Test
    @SneakyThrows
    void theBuildExpectedCaseUserAssignments() {
        // when case user assignment data and added solicitor's organisation not hmcts organisation should return empty
        // list.
        RepresentedTypeR representative = RepresentedTypeR.builder().build();
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(null);
        assertThat(nocRespondentRepresentativeService.buildExpectedCaseUserAssignments(CASE_ID_1, representative))
                .isEmpty();
        // when case user assignment data does not have any case user assignment and added solicitor's organisation is
        // empty should return empty list.
        CaseUserAssignmentData caseUserAssignmentData = new CaseUserAssignmentData();
        representative.setMyHmctsYesNo(YES);
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        assertThat(nocRespondentRepresentativeService.buildExpectedCaseUserAssignments(CASE_ID_1, representative))
                .isEmpty();
        // when case user assignment data has case user assignments but added solicitor's organisation id is blank
        // should return case user assignment data's assignments list.
        CaseUserAssignment caseUserAssignment1 = CaseUserAssignment.builder().caseId(CASE_ID_1)
                .userId(REPRESENTATIVE_ID_ONE).caseRole(ROLE_SOLICITORA).organisationId(ORGANISATION_ID_ONE).build();
        caseUserAssignmentData.setCaseUserAssignments(List.of(caseUserAssignment1));
        Organisation organisation = Organisation.builder().build();
        representative.setRespondentOrganisation(organisation);
        List<GenericTypeItem<CaseUserAssignment>> actualCaseUserAssignments = nocRespondentRepresentativeService
                .buildExpectedCaseUserAssignments(CASE_ID_1, representative);

        assertThat(actualCaseUserAssignments).hasSize(LoggerTestUtils.INTEGER_ONE);
        assertThat(actualCaseUserAssignments.get(LoggerTestUtils.INTEGER_ZERO).getValue())
                .isEqualTo(caseUserAssignment1);
        // when added solicitor does not have role should return case user assignment data's assignments list.
        organisation.setOrganisationID(ORGANISATION_ID_TWO);
        actualCaseUserAssignments = nocRespondentRepresentativeService
                .buildExpectedCaseUserAssignments(CASE_ID_1, representative);
        assertThat(actualCaseUserAssignments).hasSize(LoggerTestUtils.INTEGER_ONE);
        assertThat(actualCaseUserAssignments.get(LoggerTestUtils.INTEGER_ZERO).getValue())
                .isEqualTo(caseUserAssignment1);
        // when added solicitor does not have email address should return case user assignment data's assignment list.
        representative.setRole(ROLE_SOLICITORB);
        actualCaseUserAssignments = nocRespondentRepresentativeService
                .buildExpectedCaseUserAssignments(CASE_ID_1, representative);
        assertThat(actualCaseUserAssignments).hasSize(LoggerTestUtils.INTEGER_ONE);
        assertThat(actualCaseUserAssignments.get(LoggerTestUtils.INTEGER_ZERO).getValue())
                .isEqualTo(caseUserAssignment1);
        // when account is found by email should add solicitor to the list of case user assignment data's assignment
        // list and return it.
        representative.setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_2);
        AccountIdByEmailResponse accountIdByEmailResponse = new  AccountIdByEmailResponse();
        accountIdByEmailResponse.setUserIdentifier(REPRESENTATIVE_ID_TWO);
        when(nocService.findUserByEmail(ADMIN_USER_TOKEN, REPRESENTATIVE_EMAIL_2, CASE_ID_1))
                .thenReturn(accountIdByEmailResponse);
        actualCaseUserAssignments = nocRespondentRepresentativeService
                .buildExpectedCaseUserAssignments(CASE_ID_1, representative);
        assertThat(actualCaseUserAssignments).hasSize(LoggerTestUtils.INTEGER_TWO);
        assertThat(actualCaseUserAssignments.get(LoggerTestUtils.INTEGER_ZERO).getValue())
                .isEqualTo(caseUserAssignment1);
        GenericTypeItem<CaseUserAssignment> addedAssignment = GenericTypeItem.<CaseUserAssignment>builder()
                .id(String.valueOf(randomUUID())).value(CaseUserAssignment.builder().caseId(CASE_ID_1)
                        .userId(REPRESENTATIVE_ID_TWO).caseRole(ROLE_SOLICITORB)
                        .organisationId(ORGANISATION_ID_TWO).build()).build();
        assertThat(actualCaseUserAssignments.get(LoggerTestUtils.INTEGER_ONE).getValue())
                .isEqualTo(addedAssignment.getValue());
        // when unable to find account id by email should return case user assignment data's assignment list.
        when(nocService.findUserByEmail(ADMIN_USER_TOKEN, REPRESENTATIVE_EMAIL_2, CASE_ID_1)).thenThrow(
                new GenericServiceException(EXCEPTION_DUMMY_MESSAGE));
        actualCaseUserAssignments = nocRespondentRepresentativeService
                .buildExpectedCaseUserAssignments(CASE_ID_1, representative);
        assertThat(actualCaseUserAssignments).hasSize(LoggerTestUtils.INTEGER_ONE);
        assertThat(actualCaseUserAssignments.get(LoggerTestUtils.INTEGER_ZERO).getValue())
                .isEqualTo(caseUserAssignment1);
    }
}
