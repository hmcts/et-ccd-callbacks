package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
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
    private static final String ORGANISATION_ID_FOUR = "ORG_NEW";
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
    private static final String AUTH_TOKEN = "someToken";
    private static final String S2S_TOKEN = "someS2SToken";
    private static final String USER_ID_ONE = "891-456";
    private static final String USER_ID_TWO = "123-456";
    private static final String EVENT_UPDATE_CASE_SUBMITTED = "UPDATE_CASE_SUBMITTED";

    private static final String EXCEPTION_DUMMY_MESSAGE = "Something went wrong";

    private static final String EXPECTED_EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND =
            "Organisation not found for representative Legal One.";

    private static final String EXPECTED_WARNING_REPRESENTATIVE_MISSING_EMAIL_ADDRESS =
            "Representative Legal One is missing an email address.\n";
    private static final String EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL =
            "Representative 'Legal One' could not be found using respondent@rep.email.com. "
                    + "Case access will not be defined for this representative.\n";
    private static final String EXPECTED_ERROR_REPRESENTATIVE_ID_NOT_FOUND =
            "Representative ID not found for case ID 1234567890123456.";

    private static final String CASE_ID_1 = "1234567890123456";
    private static final String REPRESENTATIVE_EMAIL = "representative1@gmail.com";
    private static final int INTEGER_THREE = 3;
    private static final int INTEGER_FOUR = 4;
    private static final int INTEGER_FIVE = 5;
    private static final int INTEGER_SIX = 6;
    private static final int INTEGER_SEVEN = 7;
    private static final int INTEGER_EIGHT = 8;
    private static final int INTEGER_NINE = 9;
    private static final int INTEGER_TEN = 10;

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
        when(adminUserService.getAdminUserToken()).thenReturn(AUTH_TOKEN);
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

        when(adminUserService.getAdminUserToken()).thenReturn(AUTH_TOKEN);
        when(ccdClient.startEventForCase(AUTH_TOKEN,
                ccdRequest.getCaseDetails().getCaseTypeId(),
                ccdRequest.getCaseDetails().getJurisdiction(),
                ccdRequest.getCaseDetails().getCaseId(),
                EVENT_UPDATE_CASE_SUBMITTED)).thenReturn(ccdRequest);
        when(nocCcdService.getCaseAssignments(AUTH_TOKEN, ccdRequest.getCaseDetails().getCaseId())).thenReturn(
                mockCaseAssignmentData());
        when(ccdClient.submitEventForCase(eq(AUTH_TOKEN),
                any(CaseData.class),
                eq(ccdRequest.getCaseDetails().getCaseTypeId()),
                eq(ccdRequest.getCaseDetails().getJurisdiction()),
                any(CCDRequest.class),
                eq(ccdRequest.getCaseDetails().getCaseId()))).thenReturn(null);
        nocRespondentRepresentativeService.updateRespondentRepresentativesAccess(getCallBackCallbackRequest());

        verify(ccdClient, times(NumberUtils.INTEGER_TWO)).startEventForCase(AUTH_TOKEN,
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
                .submitEventForCase(eq(AUTH_TOKEN),
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
        when(adminUserService.getAdminUserToken()).thenReturn(AUTH_TOKEN);
        when(nocCcdService.getCaseAssignments(any(), any())).thenReturn(
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
        when(organisationClient.getAccountIdByEmail(AUTH_TOKEN, S2S_TOKEN, RESPONDENT_REP_EMAIL)).thenReturn(null);
        nocRespondentRepresentativeService.validateRepresentativeOrganisationAndEmail(caseData,
                SUBMISSION_REFERENCE_ONE);
        assertThat(caseData.getNocWarning()).isNotEmpty();
        assertThat(caseData.getNocWarning()).isEqualTo(EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL);

        // when organisation client response body is empty should return warning
        ResponseEntity<AccountIdByEmailResponse> organisationClientResponse = ResponseEntity.ok(null);
        when(organisationClient.getAccountIdByEmail(AUTH_TOKEN, S2S_TOKEN, RESPONDENT_REP_EMAIL))
                .thenReturn(organisationClientResponse);
        assertThat(caseData.getNocWarning()).isNotEmpty();
        assertThat(caseData.getNocWarning()).isEqualTo(EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL);
        // when organisation client response body not has user identifier
        AccountIdByEmailResponse accountIdByEmailResponse = new AccountIdByEmailResponse();
        organisationClientResponse = ResponseEntity.ok(accountIdByEmailResponse);
        when(organisationClient.getAccountIdByEmail(AUTH_TOKEN, S2S_TOKEN, RESPONDENT_REP_EMAIL))
                .thenReturn(organisationClientResponse);
        assertThat(caseData.getNocWarning()).isNotEmpty();
        assertThat(caseData.getNocWarning()).isEqualTo(EXPECTED_WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL);
        // when organisation client response body not has user identifier
        accountIdByEmailResponse.setUserIdentifier(RESPONDENT_REP_EMAIL);
        organisationClientResponse = ResponseEntity.ok(accountIdByEmailResponse);
        when(organisationClient.getAccountIdByEmail(AUTH_TOKEN, S2S_TOKEN, RESPONDENT_REP_EMAIL))
                .thenReturn(organisationClientResponse);
        nocRespondentRepresentativeService.validateRepresentativeOrganisationAndEmail(caseData,
                SUBMISSION_REFERENCE_ONE);
        assertThat(caseData.getNocWarning()).isEmpty();
    }

    @Test
    @SneakyThrows
    void theGrantNewRespondentRepresentativeAccess() {
        // when case data is empty should not throw any exception
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.grantNewRespondentRepresentativeAccess(
                null, SUBMISSION_REFERENCE_ONE));
        verifyNoInteractions(nocService);
        // when case data representative collection is empty should not throw any exception
        CaseData caseData = new CaseData();
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.grantNewRespondentRepresentativeAccess(
                caseData, SUBMISSION_REFERENCE_ONE));
        verifyNoInteractions(nocService);
        // when submission reference is empty should not throw any exception
        RepresentedTypeRItem representedTypeRItem = RepresentedTypeRItem.builder().build();
        caseData.setRepCollection(List.of(representedTypeRItem));
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.grantNewRespondentRepresentativeAccess(
                caseData, StringUtils.EMPTY));
        verifyNoInteractions(nocService);
        // when representative doesn't have id should log exception
        Logger logger = (Logger) LoggerFactory.getLogger(NocRespondentRepresentativeService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.grantNewRespondentRepresentativeAccess(
                caseData, SUBMISSION_REFERENCE_ONE));
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.ERROR)
                .extracting(ILoggingEvent::getFormattedMessage)
                .containsExactly(EXPECTED_ERROR_REPRESENTATIVE_ID_NOT_FOUND);
        verifyNoInteractions(nocService);
        // when representative is valid but not a hmcts organisation should not throw any exception
        caseData.getRepCollection().getFirst().setId(REPRESENTATIVE_ID_ONE);
        caseData.getRepCollection().getFirst().setValue(RepresentedTypeR.builder().build());
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.grantNewRespondentRepresentativeAccess(
                caseData, SUBMISSION_REFERENCE_ONE));
        verifyNoInteractions(nocService);
        // when representative email is empty should not throw any exception
        caseData.getRepCollection().getFirst().getValue().setMyHmctsYesNo(YES);
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.grantNewRespondentRepresentativeAccess(
                caseData, SUBMISSION_REFERENCE_ONE));
        verifyNoInteractions(nocService);
        // when organisation is empty should not throw any exception
        caseData.getRepCollection().getFirst().getValue().setRepresentativeEmailAddress(USER_EMAIL);
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.grantNewRespondentRepresentativeAccess(
                caseData, SUBMISSION_REFERENCE_ONE));
        verifyNoInteractions(nocService);
        // when organisation id is empty should not throw any exception
        caseData.getRepCollection().getFirst().getValue().setRespondentOrganisation(Organisation.builder().build());
        assertDoesNotThrow(() -> nocRespondentRepresentativeService.grantNewRespondentRepresentativeAccess(
                caseData, SUBMISSION_REFERENCE_ONE));
        verifyNoInteractions(nocService);
        // when nocService.grantRepresentativeAccess throws exception should log exception in here exception is null
        caseData.getRepCollection().getFirst().getValue().getRespondentOrganisation()
                .setOrganisationID(ORGANISATION_ID_ONE);
        when(adminUserService.getAdminUserToken()).thenReturn(AUTH_TOKEN);
        doThrow(new GenericServiceException(EXCEPTION_DUMMY_MESSAGE)).when(nocService)
                .grantRepresentativeAccess(AUTH_TOKEN, USER_EMAIL, SUBMISSION_REFERENCE_ONE,
                        caseData.getRepCollection().getFirst().getValue().getRespondentOrganisation(), ROLE_SOLICITORA);
        assertDoesNotThrow(() -> nocRespondentRepresentativeService
                .grantNewRespondentRepresentativeAccess(caseData, SUBMISSION_REFERENCE_ONE));
        verify(nocService, times(NumberUtils.INTEGER_ONE))
                .grantRepresentativeAccess(AUTH_TOKEN, USER_EMAIL, SUBMISSION_REFERENCE_ONE,
                        caseData.getRepCollection().getFirst().getValue().getRespondentOrganisation(), ROLE_SOLICITORA);
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.ERROR)
                .extracting(ILoggingEvent::getFormattedMessage)
                .containsExactly(EXPECTED_ERROR_REPRESENTATIVE_ID_NOT_FOUND, EXCEPTION_DUMMY_MESSAGE);
        // when nocService.grantRepresentativeAccess not throws any exception should finish successfully and set
        // representative role as [SOLICITORA]
        doNothing().when(nocService).grantRepresentativeAccess(AUTH_TOKEN, USER_EMAIL, SUBMISSION_REFERENCE_ONE,
                caseData.getRepCollection().getFirst().getValue().getRespondentOrganisation(), ROLE_SOLICITORA);
        assertDoesNotThrow(() -> nocRespondentRepresentativeService
                .grantNewRespondentRepresentativeAccess(caseData, SUBMISSION_REFERENCE_ONE));
        verify(nocService, times(NumberUtils.INTEGER_TWO))
                .grantRepresentativeAccess(AUTH_TOKEN, USER_EMAIL, SUBMISSION_REFERENCE_ONE,
                        caseData.getRepCollection().getFirst().getValue().getRespondentOrganisation(), ROLE_SOLICITORA);
        assertThat(caseData.getRepCollection().getFirst().getValue().getRole()).isEqualTo(ROLE_SOLICITORA);
    }

    @Test
    void theRevokeOldRespondentRepresentativeAccessTest() {
        // when old case details is empty should not do anything
        List<RepresentedTypeRItem> representativesToRemove = new ArrayList<>();
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(null,
                representativesToRemove)).isEmpty();
        verifyNoInteractions(nocCcdService);
        verifyNoInteractions(adminUserService);
        // when old case details does not have case id should do nothing
        CaseDetails tmpCaseDetails = new CaseDetails();
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(tmpCaseDetails,
                representativesToRemove)).isEmpty();
        verifyNoInteractions(nocCcdService);
        verifyNoInteractions(adminUserService);
        // when old case details does not have case data should do nothing
        tmpCaseDetails.setCaseId(CASE_ID_1);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(tmpCaseDetails,
                representativesToRemove)).isEmpty();
        verifyNoInteractions(nocCcdService);
        verifyNoInteractions(adminUserService);
        // when representatives to remove is empty should do nothing
        CaseData tmpCaseData = new CaseData();
        tmpCaseDetails.setCaseData(tmpCaseData);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(tmpCaseDetails,
                representativesToRemove)).isEmpty();
        verifyNoInteractions(nocCcdService);
        verifyNoInteractions(adminUserService);
        // when nocCcdService.getCaseAssignments returns null should not revoke case user assignments
        RepresentedTypeRItem tmpRepresentative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_ONE)
                .value(RepresentedTypeR.builder().build()).build();
        representativesToRemove.add(tmpRepresentative);
        when(adminUserService.getAdminUserToken()).thenReturn(AUTH_TOKEN);
        when(nocCcdService.getCaseAssignments(AUTH_TOKEN, CASE_ID_1)).thenReturn(null);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(tmpCaseDetails,
                representativesToRemove)).isEmpty();
        verify(nocCcdService, times(NumberUtils.INTEGER_ONE)).getCaseAssignments(AUTH_TOKEN, CASE_ID_1);
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).revokeCaseAssignments(eq(AUTH_TOKEN),
                any(CaseUserAssignmentData.class));
        // when nocCcdService.getCaseAssignments returns case assignments data without any case user assignments should
        // not revoke case user assignments
        CaseUserAssignmentData caseUserAssignmentData = CaseUserAssignmentData.builder().build();
        when(nocCcdService.getCaseAssignments(AUTH_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(tmpCaseDetails,
                representativesToRemove)).isEmpty();
        verify(nocCcdService, times(NumberUtils.INTEGER_TWO)).getCaseAssignments(AUTH_TOKEN, CASE_ID_1);
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).revokeCaseAssignments(eq(AUTH_TOKEN),
                any(CaseUserAssignmentData.class));
        // when there is no respondent representative role in case user assignments should not revoke case user
        // assignments
        caseUserAssignmentData.setCaseUserAssignments(List.of(CaseUserAssignment.builder().caseRole(
                ROLE_CLAIMANT_SOLICITOR).build()));
        when(nocCcdService.getCaseAssignments(AUTH_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(tmpCaseDetails,
                representativesToRemove)).isEmpty();
        verify(nocCcdService, times(INTEGER_THREE)).getCaseAssignments(AUTH_TOKEN, CASE_ID_1);
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).revokeCaseAssignments(eq(AUTH_TOKEN),
                any(CaseUserAssignmentData.class));
        // when representative in representative list is not a valid representative should not revoke case user
        // assignments
        caseUserAssignmentData.getCaseUserAssignments().getFirst().setCaseRole(ROLE_SOLICITORA);
        tmpRepresentative.setId(StringUtils.EMPTY);
        when(nocCcdService.getCaseAssignments(AUTH_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(tmpCaseDetails,
                representativesToRemove)).isEmpty();
        verify(nocCcdService, times(INTEGER_FOUR)).getCaseAssignments(AUTH_TOKEN, CASE_ID_1);
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).revokeCaseAssignments(eq(AUTH_TOKEN),
                any(CaseUserAssignmentData.class));
        // when representative is not able to removed should not revoke case user assignment
        tmpRepresentative.setId(REPRESENTATIVE_ID_ONE);
        when(nocCcdService.getCaseAssignments(AUTH_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(tmpCaseDetails,
                representativesToRemove)).isEmpty();
        verify(nocCcdService, times(INTEGER_FIVE)).getCaseAssignments(AUTH_TOKEN, CASE_ID_1);
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).revokeCaseAssignments(eq(AUTH_TOKEN),
                any(CaseUserAssignmentData.class));
        // when respondent name not exists should not revoke case user assignment
        tmpRepresentative.getValue().setMyHmctsYesNo(YES);
        tmpRepresentative.getValue().setRespondentOrganisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_ONE).build());
        tmpRepresentative.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(tmpCaseDetails,
                representativesToRemove)).isEmpty();
        verify(nocCcdService, times(INTEGER_SIX)).getCaseAssignments(AUTH_TOKEN, CASE_ID_1);
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).revokeCaseAssignments(eq(AUTH_TOKEN),
                any(CaseUserAssignmentData.class));
        // when respondent name is found but different from the name of the represented respondent should not revoke
        // case user assignment
        tmpCaseDetails.getCaseData().setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder()
                .respondentName(RESPONDENT_NAME_ONE).build());
        tmpRepresentative.getValue().setRespRepName(RESPONDENT_NAME_TWO);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(tmpCaseDetails,
                representativesToRemove)).isEmpty();
        verify(nocCcdService, times(INTEGER_SEVEN)).getCaseAssignments(AUTH_TOKEN, CASE_ID_1);
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).revokeCaseAssignments(eq(AUTH_TOKEN),
                any(CaseUserAssignmentData.class));
        // when case user assignment role is not blank but not equal to the role in representative should not revoke
        // case user assignment
        caseUserAssignmentData.getCaseUserAssignments().getFirst().setCaseRole(ROLE_SOLICITORA);
        tmpRepresentative.getValue().setRole(ROLE_SOLICITORB);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(tmpCaseDetails,
                representativesToRemove)).isEmpty();
        verify(nocCcdService, times(INTEGER_EIGHT)).getCaseAssignments(AUTH_TOKEN, CASE_ID_1);
        verify(nocCcdService, times(NumberUtils.INTEGER_ZERO)).revokeCaseAssignments(eq(AUTH_TOKEN),
                any(CaseUserAssignmentData.class));
        // when case user assignment role is equal to the role in representative should revoke case user assignment
        tmpRepresentative.getValue().setRole(ROLE_SOLICITORA);
        doNothing().when(nocCcdService).revokeCaseAssignments(AUTH_TOKEN, caseUserAssignmentData);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(tmpCaseDetails,
                representativesToRemove)).hasSize(NumberUtils.INTEGER_ONE).isEqualTo(List.of(tmpRepresentative));
        verify(nocCcdService, times(INTEGER_NINE)).getCaseAssignments(AUTH_TOKEN, CASE_ID_1);
        verify(nocCcdService, times(NumberUtils.INTEGER_ONE)).revokeCaseAssignments(eq(AUTH_TOKEN),
                any(CaseUserAssignmentData.class));
        // when respondent name is found and same with the name of the represented respondent should revoke case user
        // assignment
        tmpRepresentative.getValue().setRole(ROLE_SOLICITORB);
        tmpRepresentative.getValue().setRespRepName(RESPONDENT_NAME_ONE);
        doNothing().when(nocCcdService).revokeCaseAssignments(AUTH_TOKEN, caseUserAssignmentData);
        assertThat(nocRespondentRepresentativeService.revokeOldRespondentRepresentativeAccess(tmpCaseDetails,
                representativesToRemove)).hasSize(NumberUtils.INTEGER_ONE).isEqualTo(List.of(tmpRepresentative));
        verify(nocCcdService, times(INTEGER_TEN)).getCaseAssignments(AUTH_TOKEN, CASE_ID_1);
        verify(nocCcdService, times(NumberUtils.INTEGER_TWO)).revokeCaseAssignments(eq(AUTH_TOKEN),
                any(CaseUserAssignmentData.class));
    }

    @Test
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
        nocRespondentRepresentativeService.removeOldRepresentatives(callbackRequest);
        verify(nocNotificationService, times(NumberUtils.INTEGER_ZERO)).sendRemovedRepresentationEmails(
                eq(oldCaseDetails), eq(newCaseDetails), anyList());
        // when representatives to remove is not empty should send email
        RepresentedTypeRItem representative1 = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_ONE).value(
                RepresentedTypeR.builder().respondentId(RESPONDENT_ID_ONE).build()).build();
        oldCaseDetails.getCaseData().setRepCollection(List.of(representative1));
        RepresentedTypeRItem representative2 = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_TWO).value(
                RepresentedTypeR.builder().respondentId(RESPONDENT_ID_TWO).build()).build();
        newCaseDetails.getCaseData().setRepCollection(List.of(representative2));
        nocRespondentRepresentativeService.removeOldRepresentatives(callbackRequest);
        verify(nocNotificationService, times(NumberUtils.INTEGER_ONE)).sendRemovedRepresentationEmails(
                any(CaseDetails.class), any(CaseDetails.class), anyList());
        // when representatives revoked is not empty should remove organisation policies and noc answers
        when(adminUserService.getAdminUserToken()).thenReturn(AUTH_TOKEN);
        CaseUserAssignmentData caseUserAssignmentData = new CaseUserAssignmentData();
        caseUserAssignmentData.setCaseUserAssignments(List.of(CaseUserAssignment.builder().caseRole(ROLE_SOLICITORA)
                .build()));
        when(nocCcdService.getCaseAssignments(AUTH_TOKEN, CASE_ID_1)).thenReturn(caseUserAssignmentData);
        oldCaseDetails.getCaseData().setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder()
                .respondentName(RESPONDENT_NAME_ONE).build());
        oldCaseDetails.getCaseData().setRespondentOrganisationPolicy0(OrganisationPolicy.builder().organisation(
                Organisation.builder().organisationID(ORGANISATION_ID_ONE).build()).build());
        newCaseDetails.getCaseData().setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder()
                .respondentName(RESPONDENT_NAME_ONE).build());
        newCaseDetails.getCaseData().setRespondentOrganisationPolicy0(OrganisationPolicy.builder().organisation(
                Organisation.builder().organisationID(ORGANISATION_ID_ONE).build()).build());
        representative1.getValue().setRole(ROLE_SOLICITORA);
        representative1.getValue().setMyHmctsYesNo(YES);
        representative1.getValue().setRespRepName(RESPONDENT_NAME_ONE);
        representative1.getValue().setRespondentOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_ONE)
                .build());
        representative1.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL);
        doNothing().when(nocCcdService).revokeCaseAssignments(eq(AUTH_TOKEN), any(CaseUserAssignmentData.class));
        nocRespondentRepresentativeService.removeOldRepresentatives(callbackRequest);
        verify(nocNotificationService, times(NumberUtils.INTEGER_TWO)).sendRemovedRepresentationEmails(
                any(CaseDetails.class), any(CaseDetails.class), anyList());
        assertThat(newCaseDetails.getCaseData().getRespondentOrganisationPolicy0()).isEqualTo(OrganisationPolicy
                .builder().build());
        assertThat(newCaseDetails.getCaseData().getNoticeOfChangeAnswers0()).isEqualTo(NoticeOfChangeAnswers.builder()
                .build());
    }
}