package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.noc.RepresentativesCaseAssignments;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.OrganisationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EMAIL_TYPE_TO_ORG_ADMIN_NO_REP_LEFT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EMAIL_TYPE_TO_ORG_ADMIN_REMOVED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_TYPE_REMOVAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.REMOVE_ORGANISATION;

@ExtendWith(SpringExtension.class)
class NocRemoveRepresentationServiceTest {

    @Mock
    private NocCcdService nocCcdService;
    @Mock
    private NocNotificationService nocNotificationService;
    @Mock
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;
    @Mock
    private NocRemoveRepresentationEmailService nocRemoveRepresentationEmailService;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private UserIdamService userIdamService;
    @Mock
    private OrganisationService organisationService;

    @InjectMocks
    private NocRemoveRepresentationService nocRemoveRepresentationService;

    private static final String USER_TOKEN = "userToken";
    private static final String ADMIN_TOKEN = "adminToken";

    private static final String ORG_A_NAME = "Org A";
    private static final String ORG_A_EMAIL = "org.a@test.com";
    private static final String ORG_A_ID = "ABC123A";
    private static final String ORG_CLAIMANT_NAME = "Org C";
    private static final String ORG_CLAIMANT_EMAIL = "org.c@test.com";
    private static final String REP_A1_NAME = "Legal Rep A1";
    private static final String REP_A1_EMAIL = "rep.a1@test.com";
    private static final String REP_A2_EMAIL = "rep.a2@test.com";
    private static final String REP_B_EMAIL = "rep.b@test.com";
    private static final String REP_CLAIMANT_NAME = "Legal Rep C";
    private static final String REP_CLAIMANT_EMAIL = "rep.c@test.com";
    private static final String CLAIMANT_NAME = "Chris Claimant";
    private static final List<RespondentSumTypeItem> RESPONDENTS = new ArrayList<>();
    private static final String RESPONDENT_1_ID = "84ac5b15-f28a-40fe-a5f4-c7127366fb41";
    private static final String RESPONDENT_1_NAME = "Rich Respondent";
    private static final String RESPONDENT_2_ID = "dc890d23-21f1-4290-bc4f-db9ec272badf";
    private static final String RESPONDENT_2_NAME = "Robert Respondent";
    private static final String RESPONDENT_3_ID = "51f53e15-727f-4df8-85b9-085b0c0e866a";
    private static final String RESPONDENT_3_NAME = "Rachel Respondent";
    private static final String RESPONDENT_4_ID = "86473456-de5f-4dc4-ab53-5f9596621700";
    private static final String RESPONDENT_4_NAME = "Ryan Respondent";
    private static final String SUBMISSION_REFERENCE = "1775651960650043";
    private static final String REMOVE_YOURSELF = "Remove yourself";

    private CaseDetails caseDetails;
    private RepresentedTypeRItem repR1;
    private RepresentedTypeRItem repR2;
    private RepresentedTypeRItem repR3;
    private RepresentedTypeRItem repR4;

    private static final String EXPECTED_EXCEPTION_INVALID_PARAMETERS_TO_REVOKE_REPRESENTATIVE_WITHOUT_CASE_ID =
            "uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException: Invalid parameters to "
                    + "revoke representative of case with id: ";
    private static final String EXPECTED_EXCEPTION_INVALID_PARAMETERS_TO_REVOKE_REPRESENTATIVE_WITH_CASE_ID =
            "uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException: Invalid parameters to "
                    + "revoke representative of case with id: " + SUBMISSION_REFERENCE;
    private static final String EXPECTED_EXCEPTION_REPRESENTATIVE_NOT_FOUND =
            "uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException: No representative found for "
                    + "case ID " + SUBMISSION_REFERENCE + ".";

    @BeforeEach
    @SneakyThrows
    void setUp() {
        caseDetails = generateCaseDetails();
        repR1 = caseDetails.getCaseData().getRepCollection().get(LoggerTestUtils.INTEGER_ZERO);
        repR2 = caseDetails.getCaseData().getRepCollection().get(LoggerTestUtils.INTEGER_ONE);
        repR3 = caseDetails.getCaseData().getRepCollection().get(LoggerTestUtils.INTEGER_TWO);
        repR4 = caseDetails.getCaseData().getRepCollection().get(LoggerTestUtils.INTEGER_THREE);
        RespondentSumTypeItem respondent1 = new RespondentSumTypeItem();
        respondent1.setId(RESPONDENT_1_ID);
        respondent1.setValue(RespondentSumType.builder().respondentName(RESPONDENT_1_NAME).build());
        RespondentSumTypeItem respondent2 = new RespondentSumTypeItem();
        respondent2.setId(RESPONDENT_2_ID);
        respondent2.setValue(RespondentSumType.builder().respondentName(RESPONDENT_2_NAME).build());
        RespondentSumTypeItem respondent3 = new RespondentSumTypeItem();
        respondent3.setId(RESPONDENT_3_ID);
        respondent3.setValue(RespondentSumType.builder().respondentName(RESPONDENT_3_NAME).build());
        respondent3.setId(RESPONDENT_4_ID);
        respondent3.setValue(RespondentSumType.builder().respondentName(RESPONDENT_4_NAME).build());
        RESPONDENTS.addAll(List.of(respondent1,  respondent2, respondent3));
    }

    private CaseDetails generateCaseDetails() throws URISyntaxException, IOException {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
            .getContextClassLoader().getResource("nocRemoveRepTest.json")).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    @SneakyThrows
    void shouldRevokeClaimantLegalRep_happyPath() {
        when(nocNotificationService.findClaimantRepOrgSuperUserEmail(any()))
            .thenReturn(ORG_CLAIMANT_EMAIL);
        when(adminUserService.getAdminUserToken())
            .thenReturn(ADMIN_TOKEN);

        nocRemoveRepresentationService.revokeClaimantLegalRep(caseDetails);

        verify(nocCcdService, times(LoggerTestUtils.INTEGER_ONE))
            .revokeClaimantRepresentation(ADMIN_TOKEN, caseDetails);
        // send email to organisation admin
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
            .sendEmailToOrgAdmin(
                    any(CaseDetails.class),
                    eq(ORG_CLAIMANT_EMAIL),
                    eq(REP_CLAIMANT_NAME),
                    eq(EMAIL_TYPE_TO_ORG_ADMIN_REMOVED)
            );
        // send email to removed legal rep
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
            .sendEmailToRemovedLegalRep(any(CaseDetails.class), eq(REP_CLAIMANT_EMAIL));
        // send email to unrepresented party
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
            .sendEmailToUnrepresentedClaimant(any(CaseDetails.class), eq(ORG_CLAIMANT_NAME));
        // send email to other party
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
            .sendEmailToOtherRespondents(any(CaseDetails.class), eq(List.of()), eq(CLAIMANT_NAME));
    }

    @Test
    void shouldRevokeClaimantLegalRep_missingRepresentativeClaimantType() {
        caseDetails.getCaseData().setRepresentativeClaimantType(null);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> nocRemoveRepresentationService.revokeClaimantLegalRep(caseDetails)
        );
        assertThat(exception.getMessage())
            .isEqualTo("No representative found for case ID 1775651960650043.");
        verify(nocCcdService, times(LoggerTestUtils.INTEGER_ZERO))
            .revokeClaimantRepresentation(anyString(), any());
    }

    @Test
    @SneakyThrows
    void theRevokeRespondentLegalRep() {
        // when case details is empty should return without throw invalid parameters to revoke representative exception
        GenericRuntimeException gse = assertThrows(GenericRuntimeException.class, () -> nocRemoveRepresentationService
                .revokeRespondentLegalRep(null, USER_TOKEN));
        assertThat(gse.getMessage()).isEqualTo(
                EXPECTED_EXCEPTION_INVALID_PARAMETERS_TO_REVOKE_REPRESENTATIVE_WITHOUT_CASE_ID);
        // when case details does not have case id should throw invalid parameters to revoke representative exception
        CaseDetails tmpCaseDetails = new CaseDetails();
        gse = assertThrows(GenericRuntimeException.class, () -> nocRemoveRepresentationService
                .revokeRespondentLegalRep(tmpCaseDetails, USER_TOKEN));
        assertThat(gse.getMessage()).isEqualTo(
                EXPECTED_EXCEPTION_INVALID_PARAMETERS_TO_REVOKE_REPRESENTATIVE_WITHOUT_CASE_ID);
        // when case details does not have case data should throw invalid parameters to revoke representative exception
        tmpCaseDetails.setCaseId(SUBMISSION_REFERENCE);
        gse = assertThrows(GenericRuntimeException.class, () -> nocRemoveRepresentationService
                .revokeRespondentLegalRep(tmpCaseDetails, USER_TOKEN));
        assertThat(gse.getMessage()).isEqualTo(
                EXPECTED_EXCEPTION_INVALID_PARAMETERS_TO_REVOKE_REPRESENTATIVE_WITH_CASE_ID);
        // when case details does not have respondent collection should throw invalid parameters to revoke
        // representative exception
        CaseData caseData = new CaseData();
        tmpCaseDetails.setCaseData(caseData);
        gse = assertThrows(GenericRuntimeException.class, () -> nocRemoveRepresentationService
                .revokeRespondentLegalRep(tmpCaseDetails, USER_TOKEN));
        assertThat(gse.getMessage()).isEqualTo(
                EXPECTED_EXCEPTION_INVALID_PARAMETERS_TO_REVOKE_REPRESENTATIVE_WITH_CASE_ID);
        // when case details does not have representative collection should throw invalid parameters to revoke
        // representative exception
        tmpCaseDetails.getCaseData().setRespondentCollection(RESPONDENTS);
        gse = assertThrows(GenericRuntimeException.class, () -> nocRemoveRepresentationService
                .revokeRespondentLegalRep(tmpCaseDetails, USER_TOKEN));
        assertThat(gse.getMessage()).isEqualTo(
                EXPECTED_EXCEPTION_INVALID_PARAMETERS_TO_REVOKE_REPRESENTATIVE_WITH_CASE_ID);
        // when representative user details not found by user token should throw representative not found exception
        when(userIdamService.getUserDetails(USER_TOKEN)).thenReturn(null);
        gse = assertThrows(GenericRuntimeException.class, () -> nocRemoveRepresentationService
                .revokeRespondentLegalRep(caseDetails, USER_TOKEN));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_REPRESENTATIVE_NOT_FOUND);
        // when there is no representative found to revoke should throw representative not found exception
        UserDetails userDetails = new UserDetails();
        userDetails.setUid(repR1.getId());
        userDetails.setName(repR1.getValue().getNameOfRepresentative());
        userDetails.setEmail(repR1.getValue().getRepresentativeEmailAddress());
        when(userIdamService.getUserDetails(USER_TOKEN)).thenReturn(userDetails);
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_REPRESENTATIVE_NOT_FOUND);
        when(nocRespondentRepresentativeService.findRepresentativesByToken(USER_TOKEN, caseDetails))
                .thenReturn(Collections.emptyList());
        gse = assertThrows(GenericRuntimeException.class, () -> nocRemoveRepresentationService
                .revokeRespondentLegalRep(caseDetails, USER_TOKEN));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_REPRESENTATIVE_NOT_FOUND);

        // when representative(s) found should revoke representative access and remove representative(s)
        caseDetails.getCaseData().setNocRemoveRepOption(REMOVE_YOURSELF);
        List<RepresentedTypeRItem> representativesToRemove = List.of(repR1, repR2);
        when(nocRespondentRepresentativeService.findRepresentativesByToken(USER_TOKEN, caseDetails))
            .thenReturn(representativesToRemove);
        OrganisationsResponse orgResponse = OrganisationsResponse.builder().name(ORG_A_NAME)
                .organisationIdentifier(ORG_A_ID).build();
        when(organisationService.findOrganisationByIdamUserId(userDetails.getUid())).thenReturn(orgResponse);
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().build();

        RepresentativesCaseAssignments representativesCaseAssignments = RepresentativesCaseAssignments.builder()
                .representativesToRemove(representativesToRemove)
                .revokedCaseUserAssignments(List.of(caseUserAssignment)).build();
        when(nocRespondentRepresentativeService.revokeRespondentRepresentatives(any(CaseDetails.class),
                anyList())).thenReturn(representativesCaseAssignments);
        doCallRealMethod().when(nocRespondentRepresentativeService)
                .revokeAndRemoveRespondentRepresentatives(any(CaseDetails.class), eq(representativesToRemove));
        when(nocNotificationService
            .resolveRespondentRepresentativeOrganisationSuperuserEmail(any(CaseDetails.class), eq(repR1),
                    eq(NOC_TYPE_REMOVAL))).thenReturn(ORG_A_EMAIL);

        nocRemoveRepresentationService.revokeRespondentLegalRep(caseDetails, USER_TOKEN);
        // all representatives with the same id found by representative token should be removed
        verify(nocRespondentRepresentativeService, times(LoggerTestUtils.INTEGER_ONE))
                .revokeAndRemoveRespondentRepresentatives(any(CaseDetails.class), eq(representativesToRemove));
        assertThat(caseDetails.getCaseData().getRepCollection()).doesNotContain(repR1, repR2);
        // don't send any email to organisation superuser
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ZERO))
                .sendEmailToOrgAdmin(any(CaseDetails.class), anyString(), anyString(), anyString());
        // send email to removed legal rep
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
                .sendEmailToListOfRemovedLegalRep(any(CaseDetails.class), eq(List.of(REP_A1_EMAIL)));
        // send email to unrepresented party, representative removed respondent
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
                .sendRepresentationRemovedEmailToRespondents(any(CaseDetails.class),
                anyList(), eq(ORG_A_NAME));
        // send email to other party claimant
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmailToOtherPartyClaimant(
                any(CaseDetails.class), eq(RESPONDENT_1_NAME + ", " + RESPONDENT_2_NAME));
        // send email to other party respondent
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmailToOtherRespondents(
                any(CaseDetails.class), anyList(), eq(RESPONDENT_1_NAME + ", " + RESPONDENT_2_NAME));
    }

    @Test
    @SneakyThrows
    void theRevokeRespondentLegalRep_AllRemoved() {
        // when all representatives are removed
        List<RepresentedTypeRItem> representativesToRemove = List.of(repR1, repR2, repR3, repR4);
        when(nocRespondentRepresentativeService.findRepresentativesByToken(USER_TOKEN, caseDetails))
                .thenReturn(representativesToRemove);
        UserDetails userDetails = new UserDetails();
        userDetails.setUid(repR1.getId());
        userDetails.setName(repR1.getValue().getNameOfRepresentative());
        userDetails.setEmail(repR1.getValue().getRepresentativeEmailAddress());
        when(userIdamService.getUserDetails(USER_TOKEN)).thenReturn(userDetails);
        OrganisationsResponse orgResponse = OrganisationsResponse.builder().name(ORG_A_NAME)
                .organisationIdentifier(ORG_A_ID).build();
        when(organisationService.findOrganisationByIdamUserId(userDetails.getUid())).thenReturn(orgResponse);
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().build();

        RepresentativesCaseAssignments representativesCaseAssignments = RepresentativesCaseAssignments.builder()
                .representativesToRemove(representativesToRemove)
                .revokedCaseUserAssignments(List.of(caseUserAssignment)).build();
        when(nocRespondentRepresentativeService.revokeRespondentRepresentatives(any(CaseDetails.class),
                anyList())).thenReturn(representativesCaseAssignments);
        doCallRealMethod().when(nocRespondentRepresentativeService)
                .revokeAndRemoveRespondentRepresentatives(any(CaseDetails.class), eq(representativesToRemove));
        when(nocNotificationService
                .resolveRespondentRepresentativeOrganisationSuperuserEmail(any(CaseDetails.class), eq(repR1),
                        eq(NOC_TYPE_REMOVAL))).thenReturn(ORG_A_EMAIL);
        nocRemoveRepresentationService.revokeRespondentLegalRep(caseDetails, USER_TOKEN);
        verify(nocRespondentRepresentativeService, times(LoggerTestUtils.INTEGER_ONE))
                .revokeAndRemoveRespondentRepresentatives(any(CaseDetails.class), eq(representativesToRemove));
        assertThat(caseDetails.getCaseData().getRepCollection()).isEmpty();
        // send email to organisation superuser
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmailToOrgAdmin(
                any(CaseDetails.class), eq(ORG_A_EMAIL), eq(REP_A1_NAME), eq(EMAIL_TYPE_TO_ORG_ADMIN_NO_REP_LEFT));
        // send email to removed legal rep
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
                .sendEmailToListOfRemovedLegalRep(any(CaseDetails.class), eq(List.of(REP_A1_EMAIL, REP_A2_EMAIL,
                        REP_B_EMAIL)));
        // send email to unrepresented party, representative removed respondent
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
                .sendRepresentationRemovedEmailToRespondents(any(CaseDetails.class),
                        anyList(), eq(ORG_A_NAME));
        // send email to other party claimant
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmailToOtherPartyClaimant(
                any(CaseDetails.class), eq(RESPONDENT_1_NAME + ", " + RESPONDENT_2_NAME + ", "
                        + RESPONDENT_3_NAME + ", " + RESPONDENT_4_NAME));
        // send email to other party respondent
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmailToOtherRespondents(
                any(CaseDetails.class), anyList(), eq(RESPONDENT_1_NAME + ", " + RESPONDENT_2_NAME + ", "
                        + RESPONDENT_3_NAME + ", " + RESPONDENT_4_NAME));
    }

    @Test
    @SneakyThrows
    void theRevokeRespondentLegalRep_OrganisationRemoved() {
        caseDetails.getCaseData().setNocRemoveRepOption(REMOVE_ORGANISATION);
        // when all representatives are removed
        List<RepresentedTypeRItem> representativesToRemove = List.of(repR1, repR2, repR3);
        when(nocRespondentRepresentativeService.findRepresentativesByToken(USER_TOKEN, caseDetails))
                .thenReturn(representativesToRemove);
        UserDetails userDetails = new UserDetails();
        userDetails.setUid(repR1.getId());
        userDetails.setName(repR1.getValue().getNameOfRepresentative());
        userDetails.setEmail(repR1.getValue().getRepresentativeEmailAddress());
        when(userIdamService.getUserDetails(USER_TOKEN)).thenReturn(userDetails);
        OrganisationsResponse orgResponse = OrganisationsResponse.builder().name(ORG_A_NAME)
                .organisationIdentifier(ORG_A_ID).build();
        when(organisationService.findOrganisationByIdamUserId(userDetails.getUid())).thenReturn(orgResponse);
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().build();

        RepresentativesCaseAssignments representativesCaseAssignments = RepresentativesCaseAssignments.builder()
                .representativesToRemove(representativesToRemove)
                .revokedCaseUserAssignments(List.of(caseUserAssignment)).build();
        when(nocRespondentRepresentativeService.revokeRespondentRepresentatives(any(CaseDetails.class),
                anyList())).thenReturn(representativesCaseAssignments);
        doCallRealMethod().when(nocRespondentRepresentativeService)
                .revokeAndRemoveRespondentRepresentatives(any(CaseDetails.class), eq(representativesToRemove));
        when(nocNotificationService
                .resolveRespondentRepresentativeOrganisationSuperuserEmail(any(CaseDetails.class), eq(repR1),
                        eq(NOC_TYPE_REMOVAL))).thenReturn(ORG_A_EMAIL);
        nocRemoveRepresentationService.revokeRespondentLegalRep(caseDetails, USER_TOKEN);
        verify(nocRespondentRepresentativeService, times(LoggerTestUtils.INTEGER_ONE))
                .revokeAndRemoveRespondentRepresentatives(any(CaseDetails.class), eq(representativesToRemove));
        assertThat(caseDetails.getCaseData().getRepCollection()).isEqualTo(List.of(repR4));
        // send email to organisation superuser
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmailToOrgAdmin(
                any(CaseDetails.class), eq(ORG_A_EMAIL), eq(REP_A1_NAME), eq(EMAIL_TYPE_TO_ORG_ADMIN_REMOVED));
        // send email to removed legal rep
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
                .sendEmailToListOfRemovedLegalRep(any(CaseDetails.class), eq(List.of(REP_A1_EMAIL, REP_A2_EMAIL)));
        // send email to unrepresented party, representative removed respondent
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
                .sendRepresentationRemovedEmailToRespondents(any(CaseDetails.class),
                        anyList(), eq(ORG_A_NAME));
        // send email to other party claimant
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmailToOtherPartyClaimant(
                any(CaseDetails.class), eq(RESPONDENT_1_NAME + ", " + RESPONDENT_2_NAME + ", "
                        + RESPONDENT_3_NAME));
        // send email to other party respondent
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmailToOtherRespondents(
                any(CaseDetails.class), anyList(), eq(RESPONDENT_1_NAME + ", " + RESPONDENT_2_NAME + ", "
                        + RESPONDENT_3_NAME));
    }
}