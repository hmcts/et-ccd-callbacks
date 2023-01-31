package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeFieldPopulator;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.RespondentService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationApprovalStatus.APPROVED;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CaseConverter.class, NoticeOfChangeFieldPopulator.class, ObjectMapper.class})
@SuppressWarnings({"PMD.ExcessiveImports"})
class RespondentRepresentativeServiceTest {
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
    public static final String ET_ORG_1 = "ET Org 1";
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
    @Autowired
    private ObjectMapper objectMapper;
    private RespondentRepresentativeService respondentRepresentativeService;
    @MockBean
    private NoticeOfChangeFieldPopulator noticeOfChangeFieldPopulator;
    @MockBean
    private AdminUserService adminUserService;
    @MockBean
    private NocCcdService nocCcdService;
    private RespondentService respondentService;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        respondentService = new RespondentService();
        caseData = new CaseData();
        CaseConverter converter = new CaseConverter(objectMapper);
        respondentRepresentativeService = new RespondentRepresentativeService(noticeOfChangeFieldPopulator, converter,
            nocCcdService, adminUserService, respondentService);

        // Respondent
        caseData.setRespondentCollection(new ArrayList<>());

        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME)
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
                .respondentOrganisation(org1).build();
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(RESPONDENT_REP_ID);
        representedTypeRItem.setValue(representedType);
        representedTypeRItem.getValue().setRespondentId(RESPONDENT_ID_ONE);
        caseData.getRepCollection().add(representedTypeRItem);

        representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REP_NAME_TWO)
                .respRepName(RESPONDENT_NAME_TWO)
                .respondentOrganisation(org2).build();
        representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(RESPONDENT_REP_ID_TWO);
        representedTypeRItem.setValue(representedType);
        representedTypeRItem.getValue().setRespondentId(RESPONDENT_ID_TWO);
        caseData.getRepCollection().add(representedTypeRItem);

        representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REP_NAME_THREE)
                .respRepName(RESPONDENT_NAME_THREE)
                .respondentOrganisation(org3).build();
        representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(RESPONDENT_REP_ID_THREE);
        representedTypeRItem.setValue(representedType);
        representedTypeRItem.getValue().setRespondentId(RESPONDENT_ID_THREE);
        caseData.getRepCollection().add(representedTypeRItem);
    }

    @Test
    void shouldPrepopulateWithOrganisationPolicyAndNoc() {
        caseData = respondentRepresentativeService.prepopulateOrgPolicyAndNoc(caseData);

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

        respondentRepresentativeService.updateRepresentation(caseDetails);

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
        dynamicValueType.setCode(RespondentRepresentativeServiceTest.SOLICITORB);
        dynamicValueType.setLabel(RespondentRepresentativeServiceTest.SOLICITORB);
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
        RespondentSumType respondent = respondentService.getRespondent(RESPONDENT_NAME_THREE, caseData);
        assertThat(respondent.getResponseReference()).isEqualTo(RESPONDENT_REF_THREE);
    }

    @Test
    void updateRepresentativesAccess() throws IOException {
        CallbackRequest callbackRequest = getCallBackCallbackRequest();

        when(adminUserService.getAdminUserToken()).thenReturn(AUTH_TOKEN);
        doNothing().when(nocCcdService).updateCaseRepresentation(any(), any(), any(), any(), any());

        respondentRepresentativeService.updateRepresentativesAccess(callbackRequest);

        verify(nocCcdService, times(2))
            .updateCaseRepresentation(any(), any(), any(), any(), any());
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

    @Test
    void shouldReturnRepresentationChanges() {
        CaseData caseDataBefore = getCaseDataBefore();
        CaseData caseDataAfter = getCaseDataAfter();

        List<ChangeOrganisationRequest> representationChanges =
            respondentRepresentativeService.getRepresentationChanges(caseDataAfter, caseDataBefore);

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
                .respondentOrganisation(org2).build();
        representedTypeRItem = new RepresentedTypeRItem();
        representedTypeRItem.setId(RESPONDENT_REP_ID_TWO);
        representedTypeRItem.setValue(representedType);
        representedTypeRItem.getValue().setRespondentId(RESPONDENT_ID_TWO);
        caseDataBefore.getRepCollection().add(representedTypeRItem);

        representedType =
            RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_REP_NAME_THREE)
                .respRepName(RESPONDENT_NAME_THREE)
                .respondentOrganisation(org3).build();
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
}