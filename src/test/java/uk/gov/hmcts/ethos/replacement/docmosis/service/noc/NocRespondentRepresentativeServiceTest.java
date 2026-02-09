package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import uk.gov.hmcts.et.common.model.ccd.types.UpdateRespondentRepresentativeRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeFieldPopulator;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationApprovalStatus.APPROVED;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CaseConverter.class, NoticeOfChangeFieldPopulator.class, ObjectMapper.class})
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods", "PMD.ExcessiveMethodLength"})
class NocRespondentRepresentativeServiceTest {
    private static final String JURISDICTION_EMPLOYMENT = "EMPLOYMENT";
    private static final String CASE_TYPE_ID_ENGLAND_WALES = "ET_EnglandWales";
    private static final String SUBMISSION_REFERENCE_ONE = "1234567890123456";
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
    private static final String RESPONDENT_REP_EMAIL = "respondent@rep.email.com";
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
    private static final String USER_EMAIL = "test@hmcts.net";
    private static final String USER_FIRST_NAME = "John";
    private static final String USER_LAST_NAME = "Brown";
    private static final String USER_FULL_NAME = "John Brown";

    private static final String RESPONDENT_ID_ONE = "106001";
    private static final String RESPONDENT_ID_TWO = "106002";
    private static final String RESPONDENT_ID_THREE = "106003";
    private static final String RESPONDENT_REP_NAME_NEW = "New Dawn Solicitors";
    private static final String RESPONDENT_REP_ID_NEW = "1111-5555-8888-1113";
    private static final String ADMIN_USER_TOKEN = "adminUserToken";
    private static final String USER_TOKEN = "userToken";
    private static final String S2S_TOKEN = "someS2SToken";
    private static final String USER_ID_ONE = "891-456";
    private static final String USER_ID_TWO = "123-456";
    private static final String EVENT_UPDATE_CASE_SUBMITTED = "UPDATE_CASE_SUBMITTED";

    private static final String EXCEPTION_DUMMY_MESSAGE = "Something went wrong";

    private static final String EXPECTED_EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND =
            "Organisation not found for representative Legal One.";

    private static final String EXPECTED_ERROR_UNABLE_TO_NOTIFY_REPRESENTATION_REMOVAL =
            "Unable to send notification for representative removal for case: 1234567890123456. Exception: Something "
                    + "went wrong";
    private static final String EXPECTED_ERROR_SOLICITOR_ROLE_NOT_FOUND =
            "Solicitor role not found, case id: 1234567890123456";
    private static final String EXPECTED_ERROR_UNABLE_TO_SET_ROLE =
            "Unable to set role [SOLICITORA]. Case Id: 1234567890123456. Error: Something went wrong";
    private static final String EXPECTED_ERROR_FAILED_TO_REMOVE_ORGANISATION_POLICIES =
            "Failed to remove organisation policies for case 1234567890123456. Exception: Something went wrong";
    private static final String EXPECTED_ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_INVALID_CASE_DETAILS =
            "Failed to add organisation policy. Reason: invalid case details";
    private static final String EXPECTED_ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_INVALID_INPUTS =
            "Failed to add organisation policy for case1234567890123456. Reason: invalid inputs";
    private static final String EXPECTED_ERROR_UNABLE_TO_START_EVENT_TO_UPDATE_REPRESENTATIVE_AND_ORGANISATION_POLICY =
            "Unable to start update case submitted event to update representative role and organisation policy for "
                    + "case: 1234567890123456";
    private static final String EXPECTED_ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_REPRESENTATIVE_NOT_FOUND =
            "Failed to add organisation policy for case 1234567890123456. Reason: representative not found";
    private static final String EXPECTED_ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES =
            "Failed to add organisation policy for case 1234567890123456. Exception: Something went wrong";

    private static final String EXPECTED_WARNING_REPRESENTATIVE_MISSING_EMAIL_ADDRESS =
            "Representative Legal One is missing an email address.\n";
    private static final String EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL =
            "Representative 'Legal One' could not be found using respondent@rep.email.com. "
                    + "Case access will not be defined for this representative.\n";

    private static final String CASE_ID_1 = "1234567890123456";
    private static final String REPRESENTATIVE_NAME = "Representative Name";
    private static final String REPRESENTATIVE_EMAIL = "representative1@gmail.com";
    private static final int INTEGER_THREE = 3;
    private static final int INTEGER_FOUR = 4;
    private static final int INTEGER_FIVE = 5;
    private static final int INTEGER_SIX = 6;
    private static final int INTEGER_SEVEN = 7;
    private static final int INTEGER_EIGHT = 8;
    private static final int INTEGER_NINE = 9;
    private static final int INTEGER_TEN = 10;
    private static final int INTEGER_ELEVEN = 11;

    private ListAppender<ILoggingEvent> appender;

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

    @InjectMocks
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;

    private NocRespondentHelper nocRespondentHelper;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger(NocRespondentRepresentativeService.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        nocRespondentHelper = new NocRespondentHelper();
        caseData = new CaseData();
        CaseConverter converter = new CaseConverter(objectMapper);

        nocRespondentRepresentativeService =
            new NocRespondentRepresentativeService(noticeOfChangeFieldPopulator, converter, nocCcdService,
                    adminUserService, nocRespondentHelper, nocNotificationService, ccdClient, organisationClient,
                    authTokenGenerator, nocService);
                    
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
    void shouldUpdateRespondentRepresentationDetails() throws IOException {
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
        caseDetails.setCaseId("111-222-111-333");
        caseDetails.setCaseData(caseData);
        when(nocCcdService.getLatestAuditEventByName(any(), any(), any())).thenReturn(
            Optional.of(mockAuditEvent()));

        nocRespondentRepresentativeService.updateRespondentRepresentation(caseDetails);

        assertThat(
            caseData.getRepCollection().get(1).getValue().getRespondentOrganisation().getOrganisationID()).isEqualTo(
                ORGANISATION_ID_FOUR);
        assertThat(
            caseData.getRepCollection().get(1).getValue().getRespondentOrganisation().getOrganisationName()).isEqualTo(
            ET_ORG_NEW);
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
    @SneakyThrows
    void updateRespondentRepresentativesAccess() {
        CCDRequest ccdRequest = getCCDRequest();

        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(ccdClient.startEventForCase(ADMIN_USER_TOKEN,
                ccdRequest.getCaseDetails().getCaseTypeId(),
                ccdRequest.getCaseDetails().getJurisdiction(),
                ccdRequest.getCaseDetails().getCaseId(),
                EVENT_UPDATE_CASE_SUBMITTED)).thenReturn(ccdRequest);
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, ccdRequest.getCaseDetails().getCaseId()))
                .thenReturn(mockCaseAssignmentData());
        when(ccdClient.submitEventForCase(eq(ADMIN_USER_TOKEN),
                any(CaseData.class),
                eq(ccdRequest.getCaseDetails().getCaseTypeId()),
                eq(ccdRequest.getCaseDetails().getJurisdiction()),
                any(CCDRequest.class),
                eq(ccdRequest.getCaseDetails().getCaseId()))).thenReturn(null);
        nocRespondentRepresentativeService.updateRespondentRepresentativesAccess(getCallBackCallbackRequest());

        verify(ccdClient, times(NumberUtils.INTEGER_TWO)).startEventForCase(ADMIN_USER_TOKEN,
                ccdRequest.getCaseDetails().getCaseTypeId(),
                ccdRequest.getCaseDetails().getJurisdiction(),
                ccdRequest.getCaseDetails().getCaseId(),
                EVENT_UPDATE_CASE_SUBMITTED
        );

        verify(nocNotificationService, times(NumberUtils.INTEGER_TWO)).sendNotificationOfChangeEmails(
                any(CaseDetails.class),
                any(CaseDetails.class),
                any(ChangeOrganisationRequest.class));

        verify(ccdClient, times(NumberUtils.INTEGER_TWO))
                .submitEventForCase(eq(ADMIN_USER_TOKEN),
                        any(CaseData.class),
                        eq(ccdRequest.getCaseDetails().getCaseTypeId()),
                        eq(ccdRequest.getCaseDetails().getJurisdiction()),
                        any(CCDRequest.class),
                        eq(ccdRequest.getCaseDetails().getCaseId()));
    }

    private CallbackRequest getCallBackCallbackRequest() {
        CallbackRequest callbackRequest = new CallbackRequest();
        CaseDetails caseDetailsBefore = new CaseDetails();
        caseDetailsBefore.setCaseId(SUBMISSION_REFERENCE_ONE);
        caseDetailsBefore.setCaseData(getCaseDataBefore());
        callbackRequest.setCaseDetailsBefore(caseDetailsBefore);
        CaseDetails caseDetailsAfter = new CaseDetails();
        caseDetailsAfter.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        caseDetailsAfter.setCaseId(SUBMISSION_REFERENCE_ONE);
        caseDetailsAfter.setJurisdiction(EMPLOYMENT);
        caseDetailsAfter.setCaseData(getCaseDataAfter());
        callbackRequest.setCaseDetails(caseDetailsAfter);
        return callbackRequest;
    }

    private CCDRequest getCCDRequest() {
        CaseDetails caseDetailsAfter = new CaseDetails();
        caseDetailsAfter.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        caseDetailsAfter.setCaseId(SUBMISSION_REFERENCE_ONE);
        caseDetailsAfter.setJurisdiction(EMPLOYMENT);
        caseDetailsAfter.setCaseData(getCaseDataAfter());
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetailsAfter);
        return ccdRequest;
    }

    @Test
    @SneakyThrows
    void shouldReturnRepresentationChanges() {
        CaseData caseDataBefore = getCaseDataBefore();
        CaseData caseDataAfter = getCaseDataAfter();

        List<UpdateRespondentRepresentativeRequest> representationChanges =
            nocRespondentRepresentativeService.identifyRepresentationChanges(caseDataAfter, caseDataBefore);

        assertThat(representationChanges).usingRecursiveComparison()
            .ignoringFields("changeOrganisationRequest.requestTimestamp")
            .isEqualTo(getUpdateRespondentRepresentativeRequestList());

    }

    private List<UpdateRespondentRepresentativeRequest> getUpdateRespondentRepresentativeRequestList() {
        final Organisation orgNew =
            Organisation.builder().organisationID(ORGANISATION_ID_FOUR).organisationName(ET_ORG_NEW).build();
        final Organisation org2 =
            Organisation.builder().organisationID(ORGANISATION_ID_TWO).organisationName(ET_ORG_2).build();
        final Organisation org3 =
            Organisation.builder().organisationID(ORGANISATION_ID_THREE).organisationName(ET_ORG_3).build();

        DynamicFixedListType roleItem = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(ROLE_SOLICITORB);
        dynamicValueType.setLabel(ROLE_SOLICITORB);
        roleItem.setValue(dynamicValueType);

        ChangeOrganisationRequest changeOrganisationRequest = ChangeOrganisationRequest.builder()
            .approvalStatus(APPROVED)
            .requestTimestamp(LocalDateTime.now())
            .caseRoleId(roleItem)
            .organisationToRemove(org2)
            .organisationToAdd(orgNew)
            .build();
        List<UpdateRespondentRepresentativeRequest> changes = new ArrayList<>();
        changes.add(UpdateRespondentRepresentativeRequest.builder()
                .changeOrganisationRequest(changeOrganisationRequest).respondentName(RESPONDENT_NAME_TWO).build());
        DynamicFixedListType roleItem2 = new DynamicFixedListType();
        DynamicValueType dynamicValueType2 = new DynamicValueType();
        dynamicValueType2.setCode(ROLE_SOLICITORC);
        dynamicValueType2.setLabel(ROLE_SOLICITORC);
        roleItem2.setValue(dynamicValueType2);
        ChangeOrganisationRequest changeOrganisationRequest2 = ChangeOrganisationRequest.builder()
            .approvalStatus(APPROVED)
            .requestTimestamp(LocalDateTime.now())
            .caseRoleId(roleItem2)
            .organisationToRemove(org3)
            .organisationToAdd(org2)
            .build();
        changes.add(UpdateRespondentRepresentativeRequest.builder()
                .changeOrganisationRequest(changeOrganisationRequest2).respondentName(RESPONDENT_NAME_THREE).build());
        return changes;
    }

    private CaseData getCaseDataBefore() {
        CaseData caseDataBefore = new CaseData();

        caseDataBefore.setRespondentCollection(new ArrayList<>());
        caseDataBefore.setClaimant("claimant");
        caseDataBefore.setEthosCaseReference("caseRef");

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_ONE)
            .build());
        respondentSumTypeItem.setId(RESPONDENT_ID_ONE);
        caseDataBefore.getRespondentCollection().add(respondentSumTypeItem);

        respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_TWO)
            .build());
        respondentSumTypeItem.setId(RESPONDENT_ID_TWO);
        caseDataBefore.getRespondentCollection().add(respondentSumTypeItem);

        respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_THREE)
            .build());
        respondentSumTypeItem.setId(RESPONDENT_ID_THREE);
        caseDataBefore.getRespondentCollection().add(respondentSumTypeItem);

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

        caseDataBefore.setRespondentOrganisationPolicy0(orgPolicy1);
        caseDataBefore.setRespondentOrganisationPolicy1(orgPolicy2);
        caseDataBefore.setRespondentOrganisationPolicy2(orgPolicy3);

        // Respondent Representative
        caseDataBefore.setRepCollection(new ArrayList<>());
        RepresentedTypeR representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REP_NAME)
                .respRepName(RESPONDENT_NAME_ONE)
                .respondentOrganisation(org1).build();
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(REPRESENTATIVE_ID_ONE);
        representedTypeRItem.setValue(representedType);
        representedTypeRItem.getValue().setRespondentId(RESPONDENT_ID_ONE);
        caseDataBefore.getRepCollection().add(representedTypeRItem);

        representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REP_NAME_TWO)
                .respRepName(RESPONDENT_NAME_TWO)
                .respondentOrganisation(org2)
                .representativeEmailAddress("oldRep1@test.com").build();
        representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(REPRESENTATIVE_ID_TWO);
        representedTypeRItem.setValue(representedType);
        representedTypeRItem.getValue().setRespondentId(RESPONDENT_ID_TWO);
        caseDataBefore.getRepCollection().add(representedTypeRItem);

        representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REP_NAME_THREE)
                .respRepName(RESPONDENT_NAME_THREE)
                .respondentOrganisation(org3)
                .representativeEmailAddress("oldRep2@test.com").build();
        representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(REPRESENTATIVE_ID_THREE);
        representedTypeRItem.setValue(representedType);
        representedTypeRItem.getValue().setRespondentId(RESPONDENT_ID_THREE);
        caseDataBefore.getRepCollection().add(representedTypeRItem);

        return caseDataBefore;
    }

    private CaseData getCaseDataAfter() {
        CaseData caseDataAfter = new CaseData();
        caseDataAfter.setRespondentCollection(new ArrayList<>());

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_ONE)
            .build());
        respondentSumTypeItem.setId(RESPONDENT_ID_ONE);
        caseDataAfter.getRespondentCollection().add(respondentSumTypeItem);

        respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_TWO)
            .build());
        respondentSumTypeItem.setId(RESPONDENT_ID_TWO);
        caseDataAfter.getRespondentCollection().add(respondentSumTypeItem);

        respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_THREE)
            .build());
        respondentSumTypeItem.setId(RESPONDENT_ID_THREE);
        caseDataAfter.getRespondentCollection().add(respondentSumTypeItem);

        //Organisation
        Organisation org1 =
            Organisation.builder().organisationID(ORGANISATION_ID_ONE).organisationName(ET_ORG_1).build();
        OrganisationPolicy orgPolicy1 =
            OrganisationPolicy.builder().organisation(org1).orgPolicyCaseAssignedRole(ROLE_SOLICITORA).build();
        Organisation org2 =
            Organisation.builder().organisationID(ORGANISATION_ID_FOUR).organisationName(ET_ORG_NEW).build();
        OrganisationPolicy orgPolicy2 =
            OrganisationPolicy.builder().organisation(org2).orgPolicyCaseAssignedRole(ROLE_SOLICITORB).build();
        Organisation org3 =
            Organisation.builder().organisationID(ORGANISATION_ID_TWO).organisationName(ET_ORG_2).build();
        OrganisationPolicy orgPolicy3 =
            OrganisationPolicy.builder().organisation(org3).orgPolicyCaseAssignedRole(ROLE_SOLICITORC).build();

        caseDataAfter.setRespondentOrganisationPolicy0(orgPolicy1);
        caseDataAfter.setRespondentOrganisationPolicy1(orgPolicy2);
        caseDataAfter.setRespondentOrganisationPolicy2(orgPolicy3);

        // Respondent Representative
        caseDataAfter.setRepCollection(new ArrayList<>());
        RepresentedTypeR representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REP_NAME)
                .respRepName(RESPONDENT_NAME_ONE)
                .respondentOrganisation(org1).build();
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(REPRESENTATIVE_ID_ONE);
        representedTypeRItem.setValue(representedType);
        representedTypeRItem.getValue().setRespondentId(RESPONDENT_ID_ONE);
        caseDataAfter.getRepCollection().add(representedTypeRItem);

        representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REP_NAME_NEW)
                .respRepName(RESPONDENT_NAME_TWO)
                .respondentOrganisation(org2).build();
        representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(RESPONDENT_REP_ID_NEW);
        representedTypeRItem.setValue(representedType);
        representedTypeRItem.getValue().setRespondentId(RESPONDENT_ID_TWO);
        caseDataAfter.getRepCollection().add(representedTypeRItem);

        representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REP_NAME_TWO)
                .respRepName(RESPONDENT_NAME_THREE)
                .respondentOrganisation(org3).build();
        representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(REPRESENTATIVE_ID_TWO);
        representedTypeRItem.setValue(representedType);
        representedTypeRItem.getValue().setRespondentId(RESPONDENT_ID_THREE);
        caseDataAfter.getRepCollection().add(representedTypeRItem);
        return caseDataAfter;
    }

    @Test
    void removeOrganisationRepresentativeAccess() {
        UserDetails mockUser = getMockUser();
        when(adminUserService.getUserDetails(anyString(), any())).thenReturn(mockUser);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(nocCcdService.retrieveCaseUserAssignments(any(), any())).thenReturn(
            mockCaseAssignmentData());
        doNothing().when(nocCcdService).revokeCaseAssignments(any(), any());

        Organisation oldOrganisation =
            Organisation.builder().organisationID(ORGANISATION_ID_TWO)
                .organisationName(ET_ORG_2).build();

        Organisation newOrganisation =
            Organisation.builder().organisationID(ORGANISATION_ID_FOUR)
                .organisationName(ET_ORG_NEW).build();

        ChangeOrganisationRequest changeOrganisationRequest =
            createChangeOrganisationRequest(newOrganisation, oldOrganisation);

        nocRespondentRepresentativeService
            .removeOrganisationRepresentativeAccess(SUBMISSION_REFERENCE_ONE, changeOrganisationRequest);

        verify(nocCcdService, times(1))
            .revokeCaseAssignments(any(), any());
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

        return  OrganisationsResponse.builder()
                        .organisationIdentifier(orgId)
                        .name(orgName)
                        .contactInformation(Collections.singletonList(orgAddress)).build();
    }

    private CaseUserAssignmentData mockCaseAssignmentData() {
        List<CaseUserAssignment> caseUserAssignments = List.of(CaseUserAssignment.builder().userId(USER_ID_ONE)
                .organisationId(ET_ORG_2)
                .caseRole(ROLE_SOLICITORB)
                .caseId(SUBMISSION_REFERENCE_ONE)
                .build(),
            CaseUserAssignment.builder().userId(USER_ID_TWO)
                .organisationId(ET_ORG_2)
                .caseRole(ROLE_SOLICITORB)
                .caseId(SUBMISSION_REFERENCE_ONE)
                .build());

        return CaseUserAssignmentData.builder().caseUserAssignments(caseUserAssignments).build();
    }

    @Test
    @SneakyThrows
    void theValidateRepresentativeOrganisationAndEmail() {
        // when case data is empty should return empty list
        assertDoesNotThrow(() -> nocRespondentRepresentativeService
                .validateRepresentativeOrganisationAndEmail(null, SUBMISSION_REFERENCE_ONE));

        // when representative not exists in hmcts organisation. (my Hmcts is selected as NO) should return empty list
        CaseData caseData = new CaseData();
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setLabel(RESPONDENT_NAME_ONE);
        dynamicFixedListType.setValue(dynamicValueType);
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().id(RESPONDENT_REP_NAME).value(
                RepresentedTypeR.builder().myHmctsYesNo(NO).dynamicRespRepName(dynamicFixedListType)
                        .nameOfRepresentative(RESPONDENT_REP_NAME).build()).build()));
        nocRespondentRepresentativeService.validateRepresentativeOrganisationAndEmail(caseData,
                SUBMISSION_REFERENCE_ONE);
        assertThat(caseData.getNocWarning()).isEmpty();

        // when representative my hmcts is yes and does not have organisation should throw exception
        caseData.getRepCollection().getFirst().getValue().setMyHmctsYesNo(YES);
        GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                () -> nocRespondentRepresentativeService.validateRepresentativeOrganisationAndEmail(caseData,
                        SUBMISSION_REFERENCE_ONE));
        assertThat(genericServiceException.getMessage()).isEqualTo(
                EXPECTED_EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND);

        // when representative organisation does not have id should throw exception
        caseData.getRepCollection().getFirst().getValue().setRespondentOrganisation(Organisation.builder().build());
        caseData.getRepCollection().getFirst().getValue().setMyHmctsYesNo(YES);
        genericServiceException = assertThrows(GenericServiceException.class,
                () -> nocRespondentRepresentativeService.validateRepresentativeOrganisationAndEmail(caseData,
                        SUBMISSION_REFERENCE_ONE));
        assertThat(genericServiceException.getMessage()).isEqualTo(
                EXPECTED_EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND);

        // when representative does not have email address
        caseData.getRepCollection().getFirst().getValue().getRespondentOrganisation().setOrganisationID(ET_ORG_1);
        nocRespondentRepresentativeService.validateRepresentativeOrganisationAndEmail(caseData,
                SUBMISSION_REFERENCE_ONE);
        assertThat(caseData.getNocWarning()).isNotEmpty();
        assertThat(caseData.getNocWarning()).isEqualTo(EXPECTED_WARNING_REPRESENTATIVE_MISSING_EMAIL_ADDRESS);

        // when organisation client returns empty response should return warning
        caseData.getRepCollection().getFirst().getValue().setRepresentativeEmailAddress(RESPONDENT_REP_EMAIL);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, S2S_TOKEN, RESPONDENT_REP_EMAIL))
                .thenReturn(null);
        nocRespondentRepresentativeService.validateRepresentativeOrganisationAndEmail(caseData,
                SUBMISSION_REFERENCE_ONE);
        assertThat(caseData.getNocWarning()).isNotEmpty();
        assertThat(caseData.getNocWarning()).isEqualTo(EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL);

        // when organisation client response body is empty should return warning
        ResponseEntity<AccountIdByEmailResponse> organisationClientResponse = ResponseEntity.ok(null);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, S2S_TOKEN, RESPONDENT_REP_EMAIL))
                .thenReturn(organisationClientResponse);
        assertThat(caseData.getNocWarning()).isNotEmpty();
        assertThat(caseData.getNocWarning()).isEqualTo(EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL);
        // when organisation client response body not has user identifier
        AccountIdByEmailResponse accountIdByEmailResponse = new AccountIdByEmailResponse();
        organisationClientResponse = ResponseEntity.ok(accountIdByEmailResponse);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, S2S_TOKEN, RESPONDENT_REP_EMAIL))
                .thenReturn(organisationClientResponse);
        assertThat(caseData.getNocWarning()).isNotEmpty();
        assertThat(caseData.getNocWarning()).isEqualTo(EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL);
        // when organisation client response body not has user identifier
        accountIdByEmailResponse.setUserIdentifier(RESPONDENT_REP_EMAIL);
        organisationClientResponse = ResponseEntity.ok(accountIdByEmailResponse);
        when(organisationClient.getAccountIdByEmail(ADMIN_USER_TOKEN, S2S_TOKEN, RESPONDENT_REP_EMAIL))
                .thenReturn(organisationClientResponse);
        nocRespondentRepresentativeService.validateRepresentativeOrganisationAndEmail(caseData,
                SUBMISSION_REFERENCE_ONE);
        assertThat(caseData.getNocWarning()).isEmpty();
    }

    @Test
    @SneakyThrows
    void theRevokeOldRespondentRepresentativeAccessTest() {
        // when callback request is empty should return empty list
        List<RepresentedTypeRItem> representativesToRemove = new ArrayList<>();
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(null,
                USER_TOKEN, representativesToRemove));
        // when user token is empty should return empty list
        CallbackRequest callbackRequest = CallbackRequest.builder().build();
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                StringUtils.EMPTY, representativesToRemove));
        // when old case details is empty should return empty list
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verifyNoInteractions(nocCcdService);
        verifyNoInteractions(ccdClient);
        // when old case details does not have case id should return empty list
        CaseDetails tmpCaseDetails = new CaseDetails();
        callbackRequest.setCaseDetailsBefore(tmpCaseDetails);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verifyNoInteractions(nocCcdService);
        verifyNoInteractions(ccdClient);
        // when old case details does not have case data should return empty list
        tmpCaseDetails.setCaseId(CASE_ID_1);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verifyNoInteractions(nocCcdService);
        verifyNoInteractions(ccdClient);
        // when representatives to remove is empty should return empty list
        CaseData tmpCaseData = new CaseData();
        tmpCaseDetails.setCaseData(tmpCaseData);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verifyNoInteractions(nocCcdService);
        verifyNoInteractions(ccdClient);
        // when nocCcdService.getCaseAssignments returns null should return empty list
        RepresentedTypeRItem tmpRepresentative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_ONE)
                .value(RepresentedTypeR.builder().build()).build();
        representativesToRemove.add(tmpRepresentative);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(null);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verify(nocCcdService, times(NumberUtils.INTEGER_ONE)).retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1);
        verifyNoInteractions(ccdClient);
        // when nocCcdService.getCaseAssignments returns case assignments data without any case user assignments should
        // not revoke case user assignments
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder().build();
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verify(nocCcdService, times(NumberUtils.INTEGER_TWO)).retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1);
        verifyNoInteractions(ccdClient);
        // when there is no respondent representative role in case user assignments should not revoke case user
        // assignments
        caseUserAssignmentData.setCaseUserAssignments(List.of(CaseUserAssignment.builder().caseRole(
                ROLE_CLAIMANT_SOLICITOR).build()));
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verify(nocCcdService, times(INTEGER_THREE)).retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1);
        verifyNoInteractions(ccdClient);
        // when representative in representative list is not a valid representative should not revoke case user
        // assignments
        caseUserAssignmentData.getCaseUserAssignments().getFirst().setCaseRole(ROLE_SOLICITORA);
        tmpRepresentative.setId(StringUtils.EMPTY);
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verify(nocCcdService, times(INTEGER_FOUR)).retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1);
        verifyNoInteractions(ccdClient);
        // when representative is not able to removed should not revoke case user assignment
        tmpRepresentative.setId(REPRESENTATIVE_ID_ONE);
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verify(nocCcdService, times(INTEGER_FIVE)).retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1);
        verifyNoInteractions(ccdClient);
        // when respondent name not exists should not revoke case user assignment
        tmpRepresentative.getValue().setMyHmctsYesNo(YES);
        tmpRepresentative.getValue().setRespondentOrganisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_ONE).build());
        tmpRepresentative.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verify(nocCcdService, times(INTEGER_SIX)).retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1);
        verifyNoInteractions(ccdClient);
        // when respondent name is found but different from the name of the represented respondent should not revoke
        // case user assignment
        tmpCaseDetails.getCaseData().setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder()
                .respondentName(RESPONDENT_NAME_ONE).build());
        tmpRepresentative.getValue().setRespRepName(RESPONDENT_NAME_TWO);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verify(nocCcdService, times(INTEGER_SEVEN)).retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1);
        verifyNoInteractions(ccdClient);
        // when case user assignment role is not blank but not equal to the role in representative should not revoke
        // case user assignment
        caseUserAssignmentData.getCaseUserAssignments().getFirst().setCaseRole(ROLE_SOLICITORA);
        tmpRepresentative.getValue().setRole(ROLE_SOLICITORB);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).isEmpty();
        verify(nocCcdService, times(INTEGER_EIGHT)).retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1);
        verifyNoInteractions(ccdClient);
        // when case user assignment role is equal to the role in representative should revoke case user assignment
        tmpRepresentative.getValue().setRole(ROLE_SOLICITORA);
        when(ccdClient.revokeCaseAssignments(USER_TOKEN, caseUserAssignmentData)).thenReturn(
                String.valueOf(HTTPResponse.SC_OK));
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).hasSize(NumberUtils.INTEGER_ONE)
                .isEqualTo(List.of(tmpRepresentative));
        verify(nocCcdService, times(INTEGER_NINE)).retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1);
        verify(ccdClient, times(NumberUtils.INTEGER_ONE)).revokeCaseAssignments(USER_TOKEN, caseUserAssignmentData);
        // when respondent name is found and same with the name of the represented respondent should revoke case user
        // assignment
        tmpRepresentative.getValue().setRole(ROLE_SOLICITORB);
        tmpRepresentative.getValue().setRespRepName(RESPONDENT_NAME_ONE);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(callbackRequest,
                USER_TOKEN, representativesToRemove)).hasSize(NumberUtils.INTEGER_ONE)
                .isEqualTo(List.of(tmpRepresentative));
        verify(nocCcdService, times(INTEGER_TEN)).retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1);
        verify(ccdClient, times(NumberUtils.INTEGER_TWO)).revokeCaseAssignments(USER_TOKEN, caseUserAssignmentData);
    }

    @Test
    @SneakyThrows
    void theRemoveOldRepresentatives() {
        CaseDetails oldCaseDetails = new CaseDetails();
        oldCaseDetails.setCaseId(CASE_ID_1);
        oldCaseDetails.setCaseData(new CaseData());
        CallbackRequest callbackRequest = new CallbackRequest();
        callbackRequest.setCaseDetailsBefore(oldCaseDetails);
        CaseDetails newCaseDetails = new CaseDetails();
        newCaseDetails.setCaseData(new CaseData());
        callbackRequest.setCaseDetails(newCaseDetails);
        // when representatives to remove is empty should not send any email
        nocRespondentRepresentativeService.removeOldRepresentatives(callbackRequest, USER_TOKEN);
        verify(nocNotificationService, times(NumberUtils.INTEGER_ZERO))
                .sendRespondentRepresentationRemovalNotifications(eq(oldCaseDetails), eq(newCaseDetails), anyList());
        // when representatives to remove is not empty should send email
        RepresentedTypeRItem representative1 = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_ONE).value(
                RepresentedTypeR.builder().respondentId(RESPONDENT_ID_ONE).build()).build();
        oldCaseDetails.getCaseData().setRepCollection(List.of(representative1));
        RepresentedTypeRItem representative2 = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_TWO).value(
                RepresentedTypeR.builder().respondentId(RESPONDENT_ID_TWO).build()).build();
        newCaseDetails.getCaseData().setRepCollection(List.of(representative2));
        doNothing().when(nocNotificationService).sendRespondentRepresentationRemovalNotifications(
                any(CaseDetails.class), any(CaseDetails.class), anyList());
        nocRespondentRepresentativeService.removeOldRepresentatives(callbackRequest, USER_TOKEN);
        verify(nocNotificationService, times(NumberUtils.INTEGER_ONE)).sendRespondentRepresentationRemovalNotifications(
                any(CaseDetails.class), any(CaseDetails.class), anyList());
        // when unable to send removed representation emails should log error.
        doThrow(new RuntimeException(EXCEPTION_DUMMY_MESSAGE)).when(nocNotificationService)
                .sendRespondentRepresentationRemovalNotifications(any(CaseDetails.class), any(CaseDetails.class),
                        anyList());
        nocRespondentRepresentativeService.removeOldRepresentatives(callbackRequest, USER_TOKEN);
        verify(nocNotificationService, times(NumberUtils.INTEGER_TWO)).sendRespondentRepresentationRemovalNotifications(
                any(CaseDetails.class), any(CaseDetails.class), anyList());
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.INFO)
                .extracting(ILoggingEvent::getFormattedMessage)
                .containsExactly(EXPECTED_ERROR_UNABLE_TO_NOTIFY_REPRESENTATION_REMOVAL);
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
        representative1.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL);
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(new CaseDetails());
        ccdRequest.getCaseDetails().setCaseData(newCaseDetails.getCaseData());
        when(ccdClient.startEventForCase(ADMIN_USER_TOKEN, CASE_TYPE_ID_ENGLAND_WALES, JURISDICTION_EMPLOYMENT,
                CASE_ID_1, EVENT_UPDATE_CASE_SUBMITTED)).thenReturn(ccdRequest);
        when(ccdClient.submitEventForCase(eq(ADMIN_USER_TOKEN), any(CaseData.class),
                eq(CASE_TYPE_ID_ENGLAND_WALES), eq(JURISDICTION_EMPLOYMENT), any(CCDRequest.class), eq(CASE_ID_1)))
                .thenReturn(new SubmitEvent());
        nocRespondentRepresentativeService.removeOldRepresentatives(callbackRequest, USER_TOKEN);
        verify(nocNotificationService, times(INTEGER_THREE)).sendRespondentRepresentationRemovalNotifications(
                any(CaseDetails.class), any(CaseDetails.class), anyList());
        assertThat(newCaseDetails.getCaseData().getRespondentOrganisationPolicy0()).isEqualTo(OrganisationPolicy
                .builder().orgPolicyCaseAssignedRole(ROLE_SOLICITORA).organisation(Organisation.builder().build())
                .build());
        assertThat(newCaseDetails.getCaseData().getNoticeOfChangeAnswers0()).isEqualTo(
                NoticeOfChangeAnswers.builder().respondentName(RESPONDENT_NAME_ONE).build());
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
                .representativeEmailAddress(REPRESENTATIVE_EMAIL).build());
        List<RepresentedTypeRItem> assignableRepresentatives = nocRespondentRepresentativeService
                .findRepresentativesToAssign(null, representatives);
        assertThat(assignableRepresentatives).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(assignableRepresentatives.getFirst()).isEqualTo(representative);
        // when case details does not have caseId should return assignable representatives
        assignableRepresentatives = nocRespondentRepresentativeService.findRepresentativesToAssign(caseDetails,
                representatives);
        assertThat(assignableRepresentatives).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(assignableRepresentatives.getFirst()).isEqualTo(representative);
        // when there case user assignments is null should return assignable representatives
        caseDetails.setCaseId(CASE_ID_1);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(null);
        assignableRepresentatives = nocRespondentRepresentativeService.findRepresentativesToAssign(caseDetails,
                representatives);
        assertThat(assignableRepresentatives).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(assignableRepresentatives.getFirst()).isEqualTo(representative);
        // when case user assignments doesn't have any assignment should return assignable representatives
        CaseUserAssignmentData caseUserAssignmentData = new CaseUserAssignmentData();
        when(nocCcdService.retrieveCaseUserAssignments(ADMIN_USER_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        assignableRepresentatives = nocRespondentRepresentativeService.findRepresentativesToAssign(caseDetails,
                representatives);
        assertThat(assignableRepresentatives).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(assignableRepresentatives.getFirst()).isEqualTo(representative);
        // when representative doesn't have respondent name should return assignable representatives
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().build();
        caseUserAssignmentData.setCaseUserAssignments(List.of(caseUserAssignment));
        assignableRepresentatives = nocRespondentRepresentativeService.findRepresentativesToAssign(caseDetails,
                representatives);
        assertThat(assignableRepresentatives).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(assignableRepresentatives.getFirst()).isEqualTo(representative);
        // when case user assignment does not have respondent representative role should return assignable
        // representatives
        representative.getValue().setRespRepName(RESPONDENT_NAME_ONE);
        assignableRepresentatives = nocRespondentRepresentativeService.findRepresentativesToAssign(caseDetails,
                representatives);
        assertThat(assignableRepresentatives).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(assignableRepresentatives.getFirst()).isEqualTo(representative);
        // when respondent name not equals to representative's respondent name
        caseUserAssignment.setCaseRole(ROLE_SOLICITORA);
        CaseData caseData = new  CaseData();
        caseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder().respondentName(RESPONDENT_NAME_TWO).build());
        caseDetails.setCaseData(caseData);
        assignableRepresentatives = nocRespondentRepresentativeService.findRepresentativesToAssign(caseDetails,
                representatives);
        assertThat(assignableRepresentatives).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(assignableRepresentatives.getFirst()).isEqualTo(representative);
        // when respondent name is equal to representative's respondent name
        caseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder().respondentName(RESPONDENT_NAME_ONE).build());
        assignableRepresentatives = nocRespondentRepresentativeService.findRepresentativesToAssign(caseDetails,
                representatives);
        assertThat(assignableRepresentatives).isEmpty();
    }

    private static void setAllRespondentOrganisationPolicy(CaseData caseData) {
        caseData.setRespondentOrganisationPolicy0(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_ONE).build()).build());
        caseData.setRespondentOrganisationPolicy1(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_TWO).build()).build());
        caseData.setRespondentOrganisationPolicy2(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_THREE).build()).build());
        caseData.setRespondentOrganisationPolicy3(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_FOUR).build()).build());
        caseData.setRespondentOrganisationPolicy4(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_FIVE).build()).build());
        caseData.setRespondentOrganisationPolicy5(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_SIX).build()).build());
        caseData.setRespondentOrganisationPolicy6(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_SEVEN).build()).build());
        caseData.setRespondentOrganisationPolicy7(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_EIGHT).build()).build());
        caseData.setRespondentOrganisationPolicy8(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_NINE).build()).build());
        caseData.setRespondentOrganisationPolicy9(OrganisationPolicy.builder().organisation(Organisation.builder()
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
        CaseDetails caseDetails = new  CaseDetails();
        nocRespondentRepresentativeService.grantRespondentRepresentativesAccess(caseDetails, representatives);
        verifyNoInteractions(nocCcdService);
        // when case details not has case id should do nothing
        CaseData caseData = new  CaseData();
        caseDetails.setCaseData(caseData);
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
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.ERROR)
                .extracting(ILoggingEvent::getFormattedMessage)
                .containsExactly(EXPECTED_ERROR_SOLICITOR_ROLE_NOT_FOUND);
        verifyNoInteractions(nocCcdService);
        // when not able to grant representative access should log error
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_ONE).build());
        respondentSumTypeItem.setId(RESPONDENT_ID_ONE);
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        doThrow(new GenericServiceException(EXCEPTION_DUMMY_MESSAGE)).when(nocService)
                .grantRepresentativeAccess(eq(ADMIN_USER_TOKEN), any(), any(), any(), any());
        nocRespondentRepresentativeService.grantRespondentRepresentativesAccess(caseDetails, representatives);
        verify(nocService, times(NumberUtils.INTEGER_ONE)).grantRepresentativeAccess(eq(ADMIN_USER_TOKEN), any(),
                any(), any(), any());
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.INFO)
                .extracting(ILoggingEvent::getFormattedMessage)
                .containsExactly(EXPECTED_ERROR_UNABLE_TO_SET_ROLE);
        // when role successfully assigned should run setRepresentativesAccess without any error
        doNothing().when(nocService).grantRepresentativeAccess(eq(ADMIN_USER_TOKEN), any(), any(), any(), any());
        caseDetails.setCaseTypeId(CASE_TYPE_ID_ENGLAND_WALES);
        caseDetails.setJurisdiction(JURISDICTION_EMPLOYMENT);
        CCDRequest ccdRequest = new  CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        caseData.setRepCollection(representatives);
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
        verify(nocService, times(NumberUtils.INTEGER_TWO)).grantRepresentativeAccess(eq(ADMIN_USER_TOKEN), any(), any(),
                any(), any());
        assertThat(representatives.getFirst().getValue().getRole()).isEqualTo(ROLE_SOLICITORA);
    }

    @Test
    @SneakyThrows
    void theResetOrganisationPolicies() {
        // when case details is empty should not do anything
        List<RepresentedTypeRItem> representatives = new ArrayList<>();
        assertDoesNotThrow(() -> nocRespondentRepresentativeService
                .resetOrganisationPolicies(null, representatives));
        verifyNoInteractions(ccdClient);
        // when case details not have case data should not do anything
        CaseDetails caseDetails = new  CaseDetails();
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.resetOrganisationPolicies(
                caseDetails, representatives));
        verifyNoInteractions(ccdClient);
        // when case details not have case type id should not do anything
        caseDetails.setCaseData(new CaseData());
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.resetOrganisationPolicies(
                caseDetails, representatives));
        verifyNoInteractions(ccdClient);
        // when case details not have jurisdiction should not do anything
        caseDetails.setCaseTypeId(CASE_TYPE_ID_ENGLAND_WALES);
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.resetOrganisationPolicies(
                caseDetails, representatives));
        verifyNoInteractions(ccdClient);
        // when case details not have case id should not do anything
        caseDetails.setJurisdiction(JURISDICTION_EMPLOYMENT);
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.resetOrganisationPolicies(
                caseDetails, representatives));
        verifyNoInteractions(ccdClient);
        // where there is no revoked representatives should not do anything
        caseDetails.setCaseId(CASE_ID_1);
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.resetOrganisationPolicies(
                caseDetails, null));
        verifyNoInteractions(ccdClient);
        // when not able to startEventForCase should log exception
        representatives.add(RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_ONE).value(RepresentedTypeR.builder()
                .build()).build());
        when(ccdClient.startEventForCase(ADMIN_USER_TOKEN, CASE_TYPE_ID_ENGLAND_WALES, JURISDICTION_EMPLOYMENT,
                CASE_ID_1, EVENT_UPDATE_CASE_SUBMITTED)).thenThrow(new IOException(EXCEPTION_DUMMY_MESSAGE));
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.resetOrganisationPolicies(
                caseDetails, representatives));
        verify(ccdClient, times(NumberUtils.INTEGER_ONE)).startEventForCase(ADMIN_USER_TOKEN,
                CASE_TYPE_ID_ENGLAND_WALES, JURISDICTION_EMPLOYMENT, CASE_ID_1, EVENT_UPDATE_CASE_SUBMITTED);
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.INFO)
                .extracting(ILoggingEvent::getFormattedMessage)
                .containsExactly(EXPECTED_ERROR_FAILED_TO_REMOVE_ORGANISATION_POLICIES);
        // when not able to submitEventForCase should log exception
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        representatives.getFirst().getValue().setRole(ROLE_SOLICITORA);
        caseDetails.getCaseData().setRepCollection(representatives);
        caseDetails.getCaseData().setRespondentOrganisationPolicy0(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITORA).organisation(Organisation.builder().organisationID(
                        ORGANISATION_ID_ONE).build()).build());
        doReturn(ccdRequest).when(ccdClient).startEventForCase(ADMIN_USER_TOKEN, CASE_TYPE_ID_ENGLAND_WALES,
                JURISDICTION_EMPLOYMENT, CASE_ID_1, EVENT_UPDATE_CASE_SUBMITTED);
        when(ccdClient.submitEventForCase(eq(ADMIN_USER_TOKEN), any(CaseData.class), eq(CASE_TYPE_ID_ENGLAND_WALES),
                eq(JURISDICTION_EMPLOYMENT), any(CCDRequest.class), eq(CASE_ID_1)))
                .thenThrow(new IOException(EXCEPTION_DUMMY_MESSAGE));
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.resetOrganisationPolicies(caseDetails,
                representatives));
        verify(ccdClient, times(NumberUtils.INTEGER_TWO)).startEventForCase(ADMIN_USER_TOKEN,
                CASE_TYPE_ID_ENGLAND_WALES, JURISDICTION_EMPLOYMENT, CASE_ID_1, EVENT_UPDATE_CASE_SUBMITTED);
        verify(ccdClient, times(NumberUtils.INTEGER_ONE)).submitEventForCase(eq(ADMIN_USER_TOKEN), any(CaseData.class),
                eq(CASE_TYPE_ID_ENGLAND_WALES), eq(JURISDICTION_EMPLOYMENT), any(CCDRequest.class), eq(CASE_ID_1));
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.INFO)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains(EXPECTED_ERROR_FAILED_TO_REMOVE_ORGANISATION_POLICIES);
        // removes organisation policies and noc answers successfully
        doReturn(new SubmitEvent()).when(ccdClient).submitEventForCase(eq(ADMIN_USER_TOKEN), any(CaseData.class),
                eq(CASE_TYPE_ID_ENGLAND_WALES), eq(JURISDICTION_EMPLOYMENT), any(CCDRequest.class), eq(CASE_ID_1));
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.resetOrganisationPolicies(caseDetails,
                representatives));
        verify(ccdClient, times(INTEGER_THREE)).startEventForCase(ADMIN_USER_TOKEN,
                CASE_TYPE_ID_ENGLAND_WALES, JURISDICTION_EMPLOYMENT, CASE_ID_1, EVENT_UPDATE_CASE_SUBMITTED);
        verify(ccdClient, times(NumberUtils.INTEGER_TWO)).submitEventForCase(eq(ADMIN_USER_TOKEN), any(CaseData.class),
                eq(CASE_TYPE_ID_ENGLAND_WALES), eq(JURISDICTION_EMPLOYMENT), any(CCDRequest.class), eq(CASE_ID_1));
        assertThat(caseDetails.getCaseData().getRespondentOrganisationPolicy0()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITORA).organisation(Organisation.builder().build()).build());
    }

    @Test
    @SneakyThrows
    void theUpdateRepresentativeRoleAndOrganisationPolicy() {
        // when case details is empty should log invalid case details error
        nocRespondentRepresentativeService.updateRepresentativeRoleAndOrganisationPolicy(null,
                REPRESENTATIVE_ID_ONE, ROLE_SOLICITORA);
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.ERROR)
                .extracting(ILoggingEvent::getFormattedMessage)
                .hasSize(NumberUtils.INTEGER_ONE)
                .contains(EXPECTED_ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_INVALID_CASE_DETAILS);
        // when case details not have case id should log invalid case details error
        CaseDetails caseDetails = new  CaseDetails();
        nocRespondentRepresentativeService.updateRepresentativeRoleAndOrganisationPolicy(caseDetails,
                REPRESENTATIVE_ID_ONE, ROLE_SOLICITORA);
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.ERROR)
                .extracting(ILoggingEvent::getFormattedMessage)
                .hasSize(NumberUtils.INTEGER_TWO)
                .contains(EXPECTED_ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_INVALID_CASE_DETAILS);
        // when case details not have case type id should log invalid case details error
        caseDetails.setCaseId(CASE_ID_1);
        nocRespondentRepresentativeService.updateRepresentativeRoleAndOrganisationPolicy(caseDetails,
                REPRESENTATIVE_ID_ONE, ROLE_SOLICITORA);
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.ERROR)
                .extracting(ILoggingEvent::getFormattedMessage)
                .hasSize(INTEGER_THREE)
                .contains(EXPECTED_ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_INVALID_CASE_DETAILS);
        // when case details not have jurisdiction id should log invalid case details error
        caseDetails.setCaseTypeId(CASE_TYPE_ID_ENGLAND_WALES);
        nocRespondentRepresentativeService.updateRepresentativeRoleAndOrganisationPolicy(caseDetails,
                REPRESENTATIVE_ID_ONE, ROLE_SOLICITORA);
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.ERROR)
                .extracting(ILoggingEvent::getFormattedMessage)
                .hasSize(INTEGER_FOUR)
                .contains(EXPECTED_ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_INVALID_CASE_DETAILS);
        // when representative id is empty should log invalid inputs error
        caseDetails.setJurisdiction(JURISDICTION_EMPLOYMENT);
        nocRespondentRepresentativeService.updateRepresentativeRoleAndOrganisationPolicy(caseDetails,
                StringUtils.EMPTY, ROLE_SOLICITORA);
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.ERROR)
                .extracting(ILoggingEvent::getFormattedMessage)
                .hasSize(INTEGER_FIVE)
                .contains(EXPECTED_ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_INVALID_INPUTS);
        // when role is empty should log invalid inputs error
        nocRespondentRepresentativeService.updateRepresentativeRoleAndOrganisationPolicy(caseDetails,
                REPRESENTATIVE_ID_ONE, StringUtils.EMPTY);
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.ERROR)
                .extracting(ILoggingEvent::getFormattedMessage)
                .hasSize(INTEGER_SIX)
                .contains(EXPECTED_ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_INVALID_INPUTS);
        // when start event for update organisation policies and representative role returns empty ccd request should
        // log unable to start event error
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        when(ccdClient.startEventForCase(ADMIN_USER_TOKEN,
                CASE_TYPE_ID_ENGLAND_WALES,
                JURISDICTION_EMPLOYMENT,
                CASE_ID_1,
                EVENT_UPDATE_CASE_SUBMITTED)).thenReturn(null);
        nocRespondentRepresentativeService.updateRepresentativeRoleAndOrganisationPolicy(caseDetails,
                REPRESENTATIVE_ID_ONE, ROLE_SOLICITORA);
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.ERROR)
                .extracting(ILoggingEvent::getFormattedMessage)
                .hasSize(INTEGER_SEVEN)
                .contains(EXPECTED_ERROR_UNABLE_TO_START_EVENT_TO_UPDATE_REPRESENTATIVE_AND_ORGANISATION_POLICY);
        // when CCD request not has case details should log unable to start event error
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(new CaseDetails());
        when(nocCcdService.startEventForUpdateCaseSubmitted(ADMIN_USER_TOKEN,
                CASE_TYPE_ID_ENGLAND_WALES,
                JURISDICTION_EMPLOYMENT,
                CASE_ID_1)).thenReturn(ccdRequest);
        nocRespondentRepresentativeService.updateRepresentativeRoleAndOrganisationPolicy(caseDetails,
                REPRESENTATIVE_ID_ONE, ROLE_SOLICITORA);
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.ERROR)
                .extracting(ILoggingEvent::getFormattedMessage)
                .hasSize(INTEGER_EIGHT)
                .contains(EXPECTED_ERROR_UNABLE_TO_START_EVENT_TO_UPDATE_REPRESENTATIVE_AND_ORGANISATION_POLICY);
        // when case details not have case data should log unable to start event error
        ccdRequest.setCaseDetails(new CaseDetails());
        when(nocCcdService.startEventForUpdateCaseSubmitted(ADMIN_USER_TOKEN,
                CASE_TYPE_ID_ENGLAND_WALES,
                JURISDICTION_EMPLOYMENT,
                CASE_ID_1)).thenReturn(ccdRequest);
        nocRespondentRepresentativeService.updateRepresentativeRoleAndOrganisationPolicy(caseDetails,
                REPRESENTATIVE_ID_ONE, ROLE_SOLICITORA);
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.ERROR)
                .extracting(ILoggingEvent::getFormattedMessage)
                .hasSize(INTEGER_NINE)
                .contains(EXPECTED_ERROR_UNABLE_TO_START_EVENT_TO_UPDATE_REPRESENTATIVE_AND_ORGANISATION_POLICY);
        // when representative in the case data is not a valid representative should log representative not found error
        ccdRequest.setCaseDetails(caseDetails);
        CaseData caseData = new CaseData();
        caseDetails.setCaseData(caseData);
        nocRespondentRepresentativeService.updateRepresentativeRoleAndOrganisationPolicy(caseDetails,
                REPRESENTATIVE_ID_ONE, ROLE_SOLICITORA);
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.ERROR)
                .extracting(ILoggingEvent::getFormattedMessage)
                .hasSize(INTEGER_TEN)
                .contains(EXPECTED_ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES_REPRESENTATIVE_NOT_FOUND);
        // when submit event for case to update role and organisation policy throws exception should log that exception
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        representative.setValue(RepresentedTypeR.builder().respondentId(RESPONDENT_ID_ONE).build());
        representative.setId(REPRESENTATIVE_ID_ONE);
        Organisation organisation = Organisation.builder().organisationID(ORGANISATION_ID_ONE).build();
        representative.getValue().setRespondentOrganisation(organisation);
        caseData.setRepCollection(List.of(representative));
        when(ccdClient.submitEventForCase(eq(ADMIN_USER_TOKEN),
                any(CaseData.class),
                eq(CASE_TYPE_ID_ENGLAND_WALES),
                eq(JURISDICTION_EMPLOYMENT),
                any(CCDRequest.class),
                eq(CASE_ID_1))).thenThrow(new IOException(EXCEPTION_DUMMY_MESSAGE));
        nocRespondentRepresentativeService.updateRepresentativeRoleAndOrganisationPolicy(caseDetails,
                REPRESENTATIVE_ID_ONE, ROLE_SOLICITORA);
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.ERROR)
                .extracting(ILoggingEvent::getFormattedMessage)
                .hasSize(INTEGER_ELEVEN)
                .contains(EXPECTED_ERROR_FAILED_TO_ADD_ORGANISATION_POLICIES);
        // should successfully add organisation policy, role and reset Noc Warning
        caseData.setRespondentOrganisationPolicy0(null);
        representative.getValue().setRole(null);
        when(ccdClient.submitEventForCase(eq(ADMIN_USER_TOKEN),
                any(CaseData.class),
                eq(CASE_TYPE_ID_ENGLAND_WALES),
                eq(JURISDICTION_EMPLOYMENT),
                any(CCDRequest.class),
                eq(CASE_ID_1))).thenReturn(new SubmitEvent());
        nocRespondentRepresentativeService.updateRepresentativeRoleAndOrganisationPolicy(caseDetails,
                REPRESENTATIVE_ID_ONE, ROLE_SOLICITORA);
        assertThat(representative.getValue().getRole()).isEqualTo(ROLE_SOLICITORA);
        assertThat(caseData.getRespondentOrganisationPolicy0()).isEqualTo(OrganisationPolicy.builder()
                .organisation(organisation).orgPolicyCaseAssignedRole(ROLE_SOLICITORA).build());
    }

    @Test
    @SneakyThrows
    void theRemoveClaimantRepresentativeIfOrganisationExistsInRespondent() {
        // when case details empty should not revoke claimant representative case assignment.
        nocRespondentRepresentativeService.removeClaimantRepresentativeIfOrganisationExistsInRespondent(null);
        CaseDetails caseDetails = new CaseDetails();
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).removeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        // when case details not have case id should not revoke claimant representative case assignment.
        nocRespondentRepresentativeService.removeClaimantRepresentativeIfOrganisationExistsInRespondent(caseDetails);
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).removeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        // when case details not have case data should not revoke claimant representative case assignment.
        caseDetails.setCaseId(CASE_ID_1);
        nocRespondentRepresentativeService.removeClaimantRepresentativeIfOrganisationExistsInRespondent(caseDetails);
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).removeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        // when case data not has representative collection should not revoke claimant representative case assignment.
        CaseData caseData = new CaseData();
        caseDetails.setCaseData(caseData);
        nocRespondentRepresentativeService.removeClaimantRepresentativeIfOrganisationExistsInRespondent(caseDetails);
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).removeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        // when claimant represented question not equals to Yes should not revoke claimant representative case
        // assignment.
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setId(RESPONDENT_ID_ONE);
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_ONE).respondentOrganisation(
                ORGANISATION_ID_ONE).representativeId(REPRESENTATIVE_ID_ONE).represented(YES).build());
        Organisation respondentRepresentativeOrganisation = Organisation.builder().organisationID(StringUtils.EMPTY)
                .build();
        RepresentedTypeRItem respondentRepresentative = RepresentedTypeRItem.builder().value(RepresentedTypeR.builder()
                        .nameOfRepresentative(REPRESENTATIVE_NAME).representativeEmailAddress(REPRESENTATIVE_EMAIL)
                        .respondentOrganisation(respondentRepresentativeOrganisation).build())
                .id(REPRESENTATIVE_ID_ONE).build();
        caseData.setRespondentCollection(List.of(respondent));
        caseData.setRepCollection(List.of(respondentRepresentative));
        caseData.setClaimantRepresentedQuestion(NO);
        nocRespondentRepresentativeService.removeClaimantRepresentativeIfOrganisationExistsInRespondent(caseDetails);
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).removeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        // when claimant representative is empty should not revoke claimant representative case assignment.
        caseData.setClaimantRepresentedQuestion(YES);
        nocRespondentRepresentativeService.removeClaimantRepresentativeIfOrganisationExistsInRespondent(caseDetails);
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).removeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        // when claimant representative does not have organisation and organisation id should not revoke claimant
        // representative case assignment.
        RepresentedTypeC claimantRepresentative = RepresentedTypeC.builder().nameOfRepresentative(REPRESENTATIVE_NAME)
                .representativeEmailAddress(REPRESENTATIVE_EMAIL).representativeId(REPRESENTATIVE_ID_ONE).build();
        caseData.setRepresentativeClaimantType(claimantRepresentative);
        nocRespondentRepresentativeService.removeClaimantRepresentativeIfOrganisationExistsInRespondent(caseDetails);
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).removeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        Organisation claimantRepresentativeOrganisation = Organisation.builder().build();
        claimantRepresentative.setMyHmctsOrganisation(claimantRepresentativeOrganisation);
        nocRespondentRepresentativeService.removeClaimantRepresentativeIfOrganisationExistsInRespondent(caseDetails);
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).removeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        // when respondent representative does not have any organisation id but claimant representative has organisation
        // id or organisation with id revoke claimant representative case assignment.
        claimantRepresentative.setOrganisationId(ORGANISATION_ID_ONE);
        nocRespondentRepresentativeService.removeClaimantRepresentativeIfOrganisationExistsInRespondent(caseDetails);
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).removeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        claimantRepresentative.setOrganisationId(StringUtils.EMPTY);
        claimantRepresentative.getMyHmctsOrganisation().setOrganisationID(ORGANISATION_ID_ONE);
        nocRespondentRepresentativeService.removeClaimantRepresentativeIfOrganisationExistsInRespondent(caseDetails);
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).removeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        // when respondent representative organisation id not equal to claimant representative organisation id should
        // not revoke claimant representative case assignment.
        respondentRepresentativeOrganisation.setOrganisationID(ORGANISATION_ID_ONE);
        claimantRepresentativeOrganisation.setOrganisationID(ORGANISATION_ID_TWO);
        nocRespondentRepresentativeService.removeClaimantRepresentativeIfOrganisationExistsInRespondent(caseDetails);
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).removeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
        // when respondent representative organisation id equals to claimant representative organisation id should
        // revoke claimant representative case assignment.
        claimantRepresentativeOrganisation.setOrganisationID(ORGANISATION_ID_ONE);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_USER_TOKEN);
        doNothing().when(nocCcdService).removeClaimantRepresentation(ADMIN_USER_TOKEN, caseDetails);
        nocRespondentRepresentativeService.removeClaimantRepresentativeIfOrganisationExistsInRespondent(caseDetails);
        verify(nocCcdService, times(NumberUtils.INTEGER_ONE)).removeClaimantRepresentation(ADMIN_USER_TOKEN,
                caseDetails);
    }
}