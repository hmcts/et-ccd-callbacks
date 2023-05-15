package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeFieldPopulator;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationApprovalStatus.APPROVED;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CaseConverter.class, NoticeOfChangeFieldPopulator.class, ObjectMapper.class})
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods", "PMD.ExcessiveMethodLength"})
class NocRespondentRepresentativeServiceTest {
    private static final String CASE_ID_ONE = "723";
    private static final String RESPONDENT_NAME = "Harry Johnson";
    private static final String RESPONDENT_NAME_TWO = "Jane Green";
    private static final String RESPONDENT_NAME_THREE = "Bad Company Inc";
    private static final String RESPONDENT_REF = "7277";
    private static final String RESPONDENT_REF_TWO = "6887";
    private static final String RESPONDENT_REF_THREE = "9292";
    private static final String RESPONDENT_EMAIL = "h.johnson@corp.co.uk";
    private static final String RESPONDENT_EMAIL_TWO = "j.green@corp.co.uk";
    private static final String RESPONDENT_EMAIL_THREE = "info@corp.co.uk";
    private static final String RESPONDENT_REP_ID = "1111-2222-3333-1111";
    private static final String RESPONDENT_REP_ID_TWO = "1111-2222-3333-1112";
    private static final String RESPONDENT_REP_ID_THREE = "1111-2222-3333-1113";
    private static final String RESPONDENT_REP_NAME = "Legal One";
    private static final String RESPONDENT_REP_NAME_TWO = "Legal Two";
    private static final String RESPONDENT_REP_NAME_THREE = "Legal Three";
    private static final String SOLICITORA = "[SOLICITORA]";
    private static final String SOLICITORB = "[SOLICITORB]";
    private static final String SOLICITORC = "[SOLICITORC]";
    private static final String ORGANISATION_ID = "ORG1";
    private static final String ORGANISATION_ID_TWO = "ORG2";
    private static final String ORGANISATION_ID_THREE = "ORG3";
    private static final String ORGANISATION_ID_NEW = "ORG_NEW";
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
    private static final String USER_ID_ONE = "891-456";
    private static final String USER_ID_TWO = "123-456";

    @Autowired
    private ObjectMapper objectMapper;
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;

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
    private CcdCaseAssignment ccdCaseAssignment;
    @MockBean
    private OrganisationClient organisationClient;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    private NocRespondentHelper nocRespondentHelper;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        nocRespondentHelper = new NocRespondentHelper();
        caseData = new CaseData();
        CaseConverter converter = new CaseConverter(objectMapper);

        nocRespondentRepresentativeService =
            new NocRespondentRepresentativeService(noticeOfChangeFieldPopulator, converter, nocCcdService,
                    adminUserService, nocRespondentHelper, nocNotificationService, ccdClient, ccdCaseAssignment,
                    organisationClient, authTokenGenerator);
                    
        // Respondent
        caseData.setRespondentCollection(new ArrayList<>());

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder()
            .respondentName(RESPONDENT_NAME)
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
            Organisation.builder().organisationID(ORGANISATION_ID).organisationName(ET_ORG_1).build();
        OrganisationPolicy orgPolicy1 =
            OrganisationPolicy.builder().organisation(org1).orgPolicyCaseAssignedRole(SOLICITORA).build();
        Organisation org2 =
            Organisation.builder().organisationID(ORGANISATION_ID_TWO).organisationName(ET_ORG_2).build();
        OrganisationPolicy orgPolicy2 =
            OrganisationPolicy.builder().organisation(org2).orgPolicyCaseAssignedRole(SOLICITORB).build();
        Organisation org3 =
            Organisation.builder().organisationID(ORGANISATION_ID_THREE).organisationName(ET_ORG_3).build();
        OrganisationPolicy orgPolicy3 =
            OrganisationPolicy.builder().organisation(org3).orgPolicyCaseAssignedRole(SOLICITORC).build();

        caseData.setRespondentOrganisationPolicy0(orgPolicy1);
        caseData.setRespondentOrganisationPolicy1(orgPolicy2);
        caseData.setRespondentOrganisationPolicy2(orgPolicy3);

        // Respondent Representative
        caseData.setRepCollection(new ArrayList<>());
        RepresentedTypeR representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REP_NAME)
                .respRepName(RESPONDENT_NAME)
                .respondentOrganisation(org1)
                .myHmctsYesNo(YES).build();
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(RESPONDENT_REP_ID);
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
        representedTypeRItem.setId(RESPONDENT_REP_ID_TWO);
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
        representedTypeRItem.setId(RESPONDENT_REP_ID_THREE);
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
        when(authTokenGenerator.generate()).thenReturn("s2sToken");
    }

    @Test
    void shouldPrepopulateWithOrganisationPolicyAndNoc() {
        caseData = nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(caseData);

        assertThat(caseData.getRespondentOrganisationPolicy0()).isNotNull();
        assertThat(caseData.getRespondentOrganisationPolicy0().getOrgPolicyCaseAssignedRole()).isNotNull()
            .isEqualTo(SOLICITORA);
        assertThat(caseData.getRespondentOrganisationPolicy0().getOrganisation().getOrganisationID()).isNotNull()
            .isEqualTo(ORGANISATION_ID);
        assertThat(caseData.getRespondentOrganisationPolicy1()).isNotNull();
        assertThat(caseData.getRespondentOrganisationPolicy1().getOrgPolicyCaseAssignedRole()).isNotNull()
            .isEqualTo(SOLICITORB);
        assertThat(caseData.getRespondentOrganisationPolicy1().getOrganisation().getOrganisationID()).isNotNull()
            .isEqualTo(ORGANISATION_ID_TWO);
    }

    @Test
    void shouldUpdateRespondentRepresentationDetails() throws IOException {
        CaseDetails caseDetails = new CaseDetails();
        Organisation oldOrganisation =
            Organisation.builder().organisationID(ORGANISATION_ID_TWO).organisationName(ET_ORG_2).build();

        Organisation newOrganisation =
            Organisation.builder().organisationID(ORGANISATION_ID_NEW).organisationName(ET_ORG_NEW).build();

        ChangeOrganisationRequest changeOrganisationRequest =
            createChangeOrganisationRequest(newOrganisation, oldOrganisation);

        caseData.setChangeOrganisationRequestField(changeOrganisationRequest);

        UserDetails mockUser = getMockUser();
        when(adminUserService.getUserDetails(any())).thenReturn(mockUser);
        caseDetails.setCaseId("111-222-111-333");
        caseDetails.setCaseData(caseData);
        when(nocCcdService.getLatestAuditEventByName(any(), any(), any())).thenReturn(
            Optional.of(mockAuditEvent()));

        nocRespondentRepresentativeService.updateRepresentation(caseDetails);

        assertThat(
            caseData.getRepCollection().get(1).getValue().getRespondentOrganisation().getOrganisationID()).isEqualTo(
            ORGANISATION_ID_NEW);
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
        dynamicValueType.setCode(NocRespondentRepresentativeServiceTest.SOLICITORB);
        dynamicValueType.setLabel(NocRespondentRepresentativeServiceTest.SOLICITORB);
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
    void updateRepresentativesAccess() throws IOException {
        CCDRequest ccdRequest = getCCDRequest();

        when(adminUserService.getAdminUserToken()).thenReturn(AUTH_TOKEN);
        when(nocCcdService.updateCaseRepresentation(any(), any(), any(), any())).thenReturn(ccdRequest);
        when(nocCcdService.getCaseAssignments(any(), any())).thenReturn(
                mockCaseAssignmentData());
        when(ccdCaseAssignment.applyNocAsAdmin(any())).thenReturn(CCDCallbackResponse.builder()
                .data(caseData)
                .build());

        nocRespondentRepresentativeService.updateRepresentativesAccess(getCallBackCallbackRequest());

        verify(nocCcdService, times(2))
            .updateCaseRepresentation(any(), any(), any(), any());

        verify(nocNotificationService, times(2))
                .sendNotificationOfChangeEmails(any(), any(), any());

        verify(ccdClient, times(2))
                .submitUpdateRepEvent(any(), any(), any(), any(), any(), any());
    }

    private CallbackRequest getCallBackCallbackRequest() {
        CallbackRequest callbackRequest = new CallbackRequest();
        CaseDetails caseDetailsBefore = new CaseDetails();
        caseDetailsBefore.setCaseData(getCaseDataBefore());
        callbackRequest.setCaseDetailsBefore(caseDetailsBefore);
        CaseDetails caseDetailsAfter = new CaseDetails();
        caseDetailsAfter.setCaseData(getCaseDataAfter());
        callbackRequest.setCaseDetails(caseDetailsAfter);
        return callbackRequest;
    }

    private CCDRequest getCCDRequest() {
        CCDRequest ccdRequest = new CCDRequest();
        CaseDetails caseDetailsAfter = new CaseDetails();
        caseDetailsAfter.setCaseData(getCaseDataAfter());
        ccdRequest.setCaseDetails(caseDetailsAfter);
        return ccdRequest;
    }

    @Test
    void shouldReturnRepresentationChanges() {
        CaseData caseDataBefore = getCaseDataBefore();
        CaseData caseDataAfter = getCaseDataAfter();

        List<ChangeOrganisationRequest> representationChanges =
            nocRespondentRepresentativeService.identifyRepresentationChanges(caseDataAfter, caseDataBefore);

        assertThat(representationChanges).usingRecursiveComparison()
            .ignoringFields("requestTimestamp")
            .isEqualTo(getChangeOrganisationRequestList());

    }

    private List<ChangeOrganisationRequest> getChangeOrganisationRequestList() {
        final Organisation orgNew =
            Organisation.builder().organisationID(ORGANISATION_ID_NEW).organisationName(ET_ORG_NEW).build();
        final Organisation org2 =
            Organisation.builder().organisationID(ORGANISATION_ID_TWO).organisationName(ET_ORG_2).build();
        final Organisation org3 =
            Organisation.builder().organisationID(ORGANISATION_ID_THREE).organisationName(ET_ORG_3).build();

        DynamicFixedListType roleItem = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(SOLICITORB);
        dynamicValueType.setLabel(SOLICITORB);
        roleItem.setValue(dynamicValueType);

        ChangeOrganisationRequest changeOrganisationRequest = ChangeOrganisationRequest.builder()
            .approvalStatus(APPROVED)
            .requestTimestamp(LocalDateTime.now())
            .caseRoleId(roleItem)
            .organisationToRemove(org2)
            .organisationToAdd(orgNew)
            .build();

        List<ChangeOrganisationRequest> changes = new ArrayList<>();

        changes.add(changeOrganisationRequest);

        DynamicFixedListType roleItem2 = new DynamicFixedListType();
        DynamicValueType dynamicValueType2 = new DynamicValueType();
        dynamicValueType2.setCode(SOLICITORC);
        dynamicValueType2.setLabel(SOLICITORC);
        roleItem2.setValue(dynamicValueType2);

        ChangeOrganisationRequest changeOrganisationRequest2 = ChangeOrganisationRequest.builder()
            .approvalStatus(APPROVED)
            .requestTimestamp(LocalDateTime.now())
            .caseRoleId(roleItem2)
            .organisationToRemove(org3)
            .organisationToAdd(org2)
            .build();

        changes.add(changeOrganisationRequest2);

        return changes;
    }

    private CaseData getCaseDataBefore() {
        CaseData caseDataBefore = new CaseData();

        caseDataBefore.setRespondentCollection(new ArrayList<>());
        caseDataBefore.setClaimant("claimant");
        caseDataBefore.setEthosCaseReference("caseRef");

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME)
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
            Organisation.builder().organisationID(ORGANISATION_ID).organisationName(ET_ORG_1).build();
        OrganisationPolicy orgPolicy1 =
            OrganisationPolicy.builder().organisation(org1).orgPolicyCaseAssignedRole(SOLICITORA).build();
        Organisation org2 =
            Organisation.builder().organisationID(ORGANISATION_ID_TWO).organisationName(ET_ORG_2).build();
        OrganisationPolicy orgPolicy2 =
            OrganisationPolicy.builder().organisation(org2).orgPolicyCaseAssignedRole(SOLICITORB).build();
        Organisation org3 =
            Organisation.builder().organisationID(ORGANISATION_ID_THREE).organisationName(ET_ORG_3).build();
        OrganisationPolicy orgPolicy3 =
            OrganisationPolicy.builder().organisation(org3).orgPolicyCaseAssignedRole(SOLICITORC).build();

        caseDataBefore.setRespondentOrganisationPolicy0(orgPolicy1);
        caseDataBefore.setRespondentOrganisationPolicy1(orgPolicy2);
        caseDataBefore.setRespondentOrganisationPolicy2(orgPolicy3);

        // Respondent Representative
        caseDataBefore.setRepCollection(new ArrayList<>());
        RepresentedTypeR representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REP_NAME)
                .respRepName(RESPONDENT_NAME)
                .respondentOrganisation(org1).build();
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(RESPONDENT_REP_ID);
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
        representedTypeRItem.setId(RESPONDENT_REP_ID_TWO);
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
        representedTypeRItem.setId(RESPONDENT_REP_ID_THREE);
        representedTypeRItem.setValue(representedType);
        representedTypeRItem.getValue().setRespondentId(RESPONDENT_ID_THREE);
        caseDataBefore.getRepCollection().add(representedTypeRItem);

        return caseDataBefore;
    }

    private CaseData getCaseDataAfter() {
        CaseData caseDataAfter = new CaseData();
        caseDataAfter.setRespondentCollection(new ArrayList<>());

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME)
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
            Organisation.builder().organisationID(ORGANISATION_ID).organisationName(ET_ORG_1).build();
        OrganisationPolicy orgPolicy1 =
            OrganisationPolicy.builder().organisation(org1).orgPolicyCaseAssignedRole(SOLICITORA).build();
        Organisation org2 =
            Organisation.builder().organisationID(ORGANISATION_ID_NEW).organisationName(ET_ORG_NEW).build();
        OrganisationPolicy orgPolicy2 =
            OrganisationPolicy.builder().organisation(org2).orgPolicyCaseAssignedRole(SOLICITORB).build();
        Organisation org3 =
            Organisation.builder().organisationID(ORGANISATION_ID_TWO).organisationName(ET_ORG_2).build();
        OrganisationPolicy orgPolicy3 =
            OrganisationPolicy.builder().organisation(org3).orgPolicyCaseAssignedRole(SOLICITORC).build();

        caseDataAfter.setRespondentOrganisationPolicy0(orgPolicy1);
        caseDataAfter.setRespondentOrganisationPolicy1(orgPolicy2);
        caseDataAfter.setRespondentOrganisationPolicy2(orgPolicy3);

        // Respondent Representative
        caseDataAfter.setRepCollection(new ArrayList<>());
        RepresentedTypeR representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REP_NAME)
                .respRepName(RESPONDENT_NAME)
                .respondentOrganisation(org1).build();
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(RESPONDENT_REP_ID);
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
        representedTypeRItem.setId(RESPONDENT_REP_ID_TWO);
        representedTypeRItem.setValue(representedType);
        representedTypeRItem.getValue().setRespondentId(RESPONDENT_ID_THREE);
        caseDataAfter.getRepCollection().add(representedTypeRItem);
        return caseDataAfter;
    }

    @Test
    void removeOrganisationRepresentativeAccess() throws IOException {
        UserDetails mockUser = getMockUser();
        when(adminUserService.getUserDetails(any())).thenReturn(mockUser);
        when(adminUserService.getAdminUserToken()).thenReturn(AUTH_TOKEN);
        when(nocCcdService.getCaseAssignments(any(), any())).thenReturn(
            mockCaseAssignmentData());
        doNothing().when(nocCcdService).revokeCaseAssignments(any(), any());

        Organisation oldOrganisation =
            Organisation.builder().organisationID(ORGANISATION_ID_TWO)
                .organisationName(ET_ORG_2).build();

        Organisation newOrganisation =
            Organisation.builder().organisationID(ORGANISATION_ID_NEW)
                .organisationName(ET_ORG_NEW).build();

        ChangeOrganisationRequest changeOrganisationRequest =
            createChangeOrganisationRequest(newOrganisation, oldOrganisation);

        nocRespondentRepresentativeService
            .removeOrganisationRepresentativeAccess(CASE_ID_ONE, changeOrganisationRequest);

        verify(nocCcdService, times(1))
            .revokeCaseAssignments(any(), any());
    }

    @Test
    void prepopulateOrgAddressAndName() {
        OrganisationsResponse resOrg1 = createOrganisationsResponse(ORGANISATION_ID, ET_ORG_1);
        OrganisationsResponse resOrg2 = createOrganisationsResponse(ORGANISATION_ID_TWO, ET_ORG_2);
        OrganisationsResponse resOrg3 = createOrganisationsResponse(ORGANISATION_ID_THREE, ET_ORG_3);

        List<OrganisationsResponse> orgDetails = new ArrayList<>();
        orgDetails.add(resOrg1);
        orgDetails.add(resOrg2);
        orgDetails.add(resOrg3);

        when(organisationClient.getOrganisations(anyString(), anyString())).thenReturn(orgDetails);

        CaseData returned = nocRespondentRepresentativeService.prepopulateOrgAddress(caseData, "someToken");

        verify(organisationClient, times(1)).getOrganisations(anyString(), anyString());

        List<RepresentedTypeRItem> repCollection = returned.getRepCollection();

        RepresentedTypeR rep1 = repCollection.get(0).getValue();
        assertThat(rep1.getRepresentativeAddress().getAddressLine1())
                .isEqualTo(resOrg1.getContactInformation().get(0).getAddressLine1());
        assertThat(rep1.getNameOfOrganisation()).isEqualTo(resOrg1.getName());

        RepresentedTypeR rep2 = repCollection.get(1).getValue();
        assertNull(rep2.getRepresentativeAddress());
        assertNull(rep2.getNameOfOrganisation());

        RepresentedTypeR rep3 = repCollection.get(2).getValue();
        assertThat(rep3.getRepresentativeAddress().getAddressLine1())
                .isEqualTo(resOrg3.getContactInformation().get(0).getAddressLine1());
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
        OrganisationsResponse resOrg1 = createOrganisationsResponse(ORGANISATION_ID, ET_ORG_1);
        resOrg1.setContactInformation(null);
        OrganisationsResponse resOrg2 = createOrganisationsResponse(ORGANISATION_ID_TWO, ET_ORG_2);
        resOrg2.setContactInformation(new ArrayList<>());
        OrganisationsResponse resOrg3 = createOrganisationsResponse(ORGANISATION_ID_THREE, ET_ORG_3);
        resOrg3.getContactInformation().get(0).setAddressLine1(null);
        resOrg3.getContactInformation().get(0).setTownCity(null);
        resOrg3.getContactInformation().get(0).setCountry(null);

        List<OrganisationsResponse> orgDetails = new ArrayList<>();
        orgDetails.add(resOrg1);
        orgDetails.add(resOrg2);
        orgDetails.add(resOrg3);

        when(organisationClient.getOrganisations(anyString(), anyString())).thenReturn(orgDetails);

        CaseData returned = nocRespondentRepresentativeService.prepopulateOrgAddress(caseData, "someToken");

        verify(organisationClient, times(1)).getOrganisations(anyString(), anyString());

        List<RepresentedTypeRItem> repCollection = returned.getRepCollection();

        assertNull(repCollection.get(0).getValue().getRepresentativeAddress());
        assertNull(repCollection.get(1).getValue().getRepresentativeAddress());
        assertNull(repCollection.get(2).getValue().getRepresentativeAddress().getAddressLine1());
    }

    @Test
    void prepopulateOrgAddress_OverwriteExistingAddress() {
        OrganisationsResponse resOrg1 = createOrganisationsResponse(ORGANISATION_ID, ET_ORG_1);
        OrganisationsResponse resOrg2 = createOrganisationsResponse(ORGANISATION_ID_TWO, ET_ORG_2);
        OrganisationsResponse resOrg3 = createOrganisationsResponse(ORGANISATION_ID_THREE, ET_ORG_3);

        List<OrganisationsResponse> orgDetails = new ArrayList<>();
        orgDetails.add(resOrg1);
        orgDetails.add(resOrg2);
        orgDetails.add(resOrg3);

        List<RepresentedTypeRItem> existingRepCollection = caseData.getRepCollection();
        Address rep1Address = new Address();
        rep1Address.setAddressLine1("Rep 1 - Address 1");
        existingRepCollection.get(0).getValue().setRepresentativeAddress(rep1Address);
        Address rep2Address = new Address();
        rep2Address.setAddressLine1("Rep 2 - Address 1");
        existingRepCollection.get(1).getValue().setRepresentativeAddress(rep2Address);
        Address rep3Address = new Address();
        rep3Address.setAddressLine1("Rep 3 - Address 1");
        existingRepCollection.get(2).getValue().setRepresentativeAddress(rep3Address);

        when(organisationClient.getOrganisations(anyString(), anyString())).thenReturn(orgDetails);

        CaseData returned = nocRespondentRepresentativeService.prepopulateOrgAddress(caseData, "someToken");

        verify(organisationClient, times(1)).getOrganisations(anyString(), anyString());

        List<RepresentedTypeRItem> repCollection = returned.getRepCollection();

        Address representative1Org = repCollection.get(0).getValue().getRepresentativeAddress();
        assertThat(representative1Org.getAddressLine1())
                .isEqualTo(resOrg1.getContactInformation().get(0).getAddressLine1());

        Address representative2Org = repCollection.get(1).getValue().getRepresentativeAddress();
        assertThat(representative2Org.getAddressLine1())
                .isEqualTo(rep2Address.getAddressLine1());

        Address representative3Org = repCollection.get(2).getValue().getRepresentativeAddress();
        assertThat(representative3Org.getAddressLine1())
                .isEqualTo(resOrg3.getContactInformation().get(0).getAddressLine1());
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
                .caseRole(SOLICITORB)
                .caseId(CASE_ID_ONE)
                .build(),
            CaseUserAssignment.builder().userId(USER_ID_TWO)
                .organisationId(ET_ORG_2)
                .caseRole(SOLICITORB)
                .caseId(CASE_ID_ONE)
                .build());

        return CaseUserAssignmentData.builder().caseUserAssignments(caseUserAssignments).build();
    }
}