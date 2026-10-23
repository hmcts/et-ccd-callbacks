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
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_CASE_SHARING_STATUS_NOT_SHARED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_CASE_SHARING_STATUS_SHARED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_YOURSELF;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REPRESENTATIVE_TYPE_LEAD;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REPRESENTATIVE_TYPE_SHARED;

@ExtendWith(SpringExtension.class)
class NocRemoveClaimantRepresentationServiceTest {

    @Mock
    private NocCcdService nocCcdService;
    @Mock
    private UserService userService;

    @InjectMocks
    private NocRemoveClaimantRepresentationService nocRemoveClaimantRepresentationService;

    private static final String CLAIMANT_REPRESENTATIVE_EMAIL_VALID = "rep.c@test.com";
    private static final String CLAIMANT_REPRESENTATIVE_EMAIL_INVALID = "rep.r@test.com";
    private static final String CLAIMANT_REPRESENTATIVE_ID = "claimantRepresentativeId";
    private static final String CLAIMANT_REPRESENTATIVE_IDAM_ID_1 = "claimantRepresentativeIdamId1";
    private static final String CLAIMANT_REPRESENTATIVE_IDAM_ID_2 = "claimantRepresentativeIdamId2";
    private static final String CASE_ID = "1775651960650043";
    private static final String DUMMY_USER_TOKEN = "dummyUserToken";
    private static final String ORGANISATION_ID = "organisationId";

    private static final String EXPECTED_EXCEPTION_INVALID_CASE_DETAILS = "Case details are required";
    private static final String EXPECTED_EXCEPTION_REPRESENTATIVE_NOT_FOUND =
            "Representative not found for case ID " + CASE_ID + ".";

    private CaseDetails caseDetails;
    private CaseUserAssignment caseUserAssignment1;
    private CaseUserAssignment caseUserAssignment2;
    private UserDetails userDetails1;
    private UserDetails userDetails2;

    @BeforeEach
    @SneakyThrows
    void setUp() {
        caseDetails = generateCaseDetails();
        caseUserAssignment1 = CaseUserAssignment.builder().userId(CLAIMANT_REPRESENTATIVE_IDAM_ID_1).build();
        caseUserAssignment2 = CaseUserAssignment.builder().userId(CLAIMANT_REPRESENTATIVE_IDAM_ID_2).build();
        userDetails1 = new UserDetails();
        userDetails1.setUid(CLAIMANT_REPRESENTATIVE_IDAM_ID_1);
        userDetails1.setEmail(CLAIMANT_REPRESENTATIVE_EMAIL_VALID);
        userDetails2 = new UserDetails();
        userDetails2.setUid(CLAIMANT_REPRESENTATIVE_IDAM_ID_2);
        userDetails2.setEmail(CLAIMANT_REPRESENTATIVE_EMAIL_INVALID);
    }

    private CaseDetails generateCaseDetails() throws URISyntaxException, IOException {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
                .getContextClassLoader().getResource("nocRemoveRepTest.json")).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    @SneakyThrows
    void theSetNocRemoveOption() {
        // when case details are not valid should throw generic service exception
        GenericServiceException gse = assertThrows(GenericServiceException.class,
                () -> nocRemoveClaimantRepresentationService.populateNocRemovalDetails(DUMMY_USER_TOKEN, null));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_INVALID_CASE_DETAILS);
        // when representative is lead representative, should set nocRepresentativeType to lead
        when(userService.getValidatedUserDetails(DUMMY_USER_TOKEN, CASE_ID)).thenReturn(userDetails1);
        nocRemoveClaimantRepresentationService.populateNocRemovalDetails(DUMMY_USER_TOKEN, caseDetails);
        assertThat(caseDetails.getCaseData().getNocRepresentativeType()).isEqualTo(NOC_REPRESENTATIVE_TYPE_LEAD);
        assertThat(caseDetails.getCaseData().getNocCaseSharingStatus()).isEqualTo(NOC_CASE_SHARING_STATUS_NOT_SHARED);
        assertThat(caseDetails.getCaseData().getNocRemoveOption()).isNull();
        // when representative is not lead representative and a shared case should set nocRepresentativeType to lead,
        // nocCaseSharingStatus to shared and nocRemoveOption to yourself
        List<CaseUserAssignment> caseUserAssignments = new ArrayList<>(Arrays.asList(caseUserAssignment1,
                caseUserAssignment2));
        when(nocCcdService.findCaseUserAssignmentsByCaseIdAndRole(caseDetails.getCaseId(),
                ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())).thenReturn(caseUserAssignments);
        when(userService.getValidatedUserDetails(DUMMY_USER_TOKEN, CASE_ID)).thenReturn(userDetails2);
        when(userService.findUserDetailsByIdamId(CLAIMANT_REPRESENTATIVE_IDAM_ID_1)).thenReturn(userDetails1);
        nocRemoveClaimantRepresentationService.populateNocRemovalDetails(DUMMY_USER_TOKEN, caseDetails);
        assertThat(caseDetails.getCaseData().getNocRepresentativeType()).isEqualTo(NOC_REPRESENTATIVE_TYPE_SHARED);
        assertThat(caseDetails.getCaseData().getNocCaseSharingStatus()).isEqualTo(NOC_CASE_SHARING_STATUS_SHARED);
        assertThat(caseDetails.getCaseData().getNocRemoveOption()).isEqualTo(NOC_REMOVE_OPTION_YOURSELF);
    }

    @Test
    void theFindOtherClaimantRepresentative() {
        // when no claimant representative found should return null
        when(nocCcdService.findCaseUserAssignmentsByCaseIdAndRole(caseDetails.getCaseId(),
                ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())).thenReturn(List.of());
        assertThat(nocRemoveClaimantRepresentationService.findOtherClaimantRepresentative(caseDetails, userDetails1))
                .isNull();
        // when no other claimant representative found should return null

        List<CaseUserAssignment> caseUserAssignments = new ArrayList<>(Collections.singletonList(caseUserAssignment1));
        when(nocCcdService.findCaseUserAssignmentsByCaseIdAndRole(caseDetails.getCaseId(),
                ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())).thenReturn(caseUserAssignments);
        assertThat(nocRemoveClaimantRepresentationService.findOtherClaimantRepresentative(caseDetails, userDetails1))
                .isNull();
        // when other claimant representative found should return user details of the other representative
        caseUserAssignments = new ArrayList<>(Arrays.asList(caseUserAssignment1,
                caseUserAssignment2));
        when(nocCcdService.findCaseUserAssignmentsByCaseIdAndRole(caseDetails.getCaseId(),
                ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())).thenReturn(caseUserAssignments);

        when(userService.findUserDetailsByIdamId(CLAIMANT_REPRESENTATIVE_IDAM_ID_2)).thenReturn(userDetails2);
        assertThat(nocRemoveClaimantRepresentationService.findOtherClaimantRepresentative(caseDetails, userDetails1))
                .isEqualTo(userDetails2);
    }

    @Test
    void theHasSharedRepresentative() {
        // when not able to find any shared representative should return false
        List<CaseUserAssignment> caseUserAssignments = new ArrayList<>(Collections.singletonList(caseUserAssignment1));
        when(nocCcdService.findCaseUserAssignmentsByCaseIdAndRole(caseDetails.getCaseId(),
                ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())).thenReturn(caseUserAssignments);
        assertThat(nocRemoveClaimantRepresentationService.hasSharedRepresentative(caseDetails, userDetails1)).isFalse();
        // when other claimant representative found should return true
        caseUserAssignments = new ArrayList<>(Arrays.asList(caseUserAssignment1, caseUserAssignment2));
        when(nocCcdService.findCaseUserAssignmentsByCaseIdAndRole(caseDetails.getCaseId(),
                ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())).thenReturn(caseUserAssignments);
        when(userService.findUserDetailsByIdamId(CLAIMANT_REPRESENTATIVE_IDAM_ID_2)).thenReturn(userDetails2);
        assertThat(nocRemoveClaimantRepresentationService.hasSharedRepresentative(caseDetails, userDetails1)).isTrue();
    }

    @Test
    @SneakyThrows
    void theRevokeClaimantLegalRep() {
        // when there is no claimant representative should throw representative not found exception
        CaseDetails tmpCaseDetails = new CaseDetails();
        tmpCaseDetails.setCaseData(new CaseData());
        tmpCaseDetails.setCaseId(CASE_ID);
        GenericServiceException gse = assertThrows(GenericServiceException.class,
                () -> nocRemoveClaimantRepresentationService.revokeClaimantLegalRep(DUMMY_USER_TOKEN, tmpCaseDetails));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_REPRESENTATIVE_NOT_FOUND);
        // when claimant representative exists, but noc remove option is not organisation should revoke only
        // representation
        tmpCaseDetails.getCaseData().setRepresentativeClaimantType(RepresentedTypeC.builder()
                .representativeId(CLAIMANT_REPRESENTATIVE_ID).build());
        tmpCaseDetails.getCaseData().setClaimantRepresentativeRemoved(NO);
        tmpCaseDetails.getCaseData().setClaimantRepresentedQuestion(YES);
        tmpCaseDetails.getCaseData().setNocRemoveOption(NOC_REMOVE_OPTION_YOURSELF);
        tmpCaseDetails.getCaseData().setClaimantRepresentativeOrganisationPolicy(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())
                .organisation(Organisation.builder().organisationID(ORGANISATION_ID).build()).build());
        nocRemoveClaimantRepresentationService.revokeClaimantLegalRep(DUMMY_USER_TOKEN, tmpCaseDetails);
        verify(nocCcdService, times(LoggerTestUtils.INTEGER_ONE))
                .revokeClaimantRepresentation(DUMMY_USER_TOKEN, tmpCaseDetails);
        assertThat(tmpCaseDetails.getCaseData().getRepresentativeClaimantType()).isEqualTo(RepresentedTypeC.builder()
                .representativeId(CLAIMANT_REPRESENTATIVE_ID).build());
        assertThat(tmpCaseDetails.getCaseData().getClaimantRepresentativeRemoved()).isEqualTo(NO);
        assertThat(tmpCaseDetails.getCaseData().getClaimantRepresentedQuestion()).isEqualTo(YES);
        assertThat(tmpCaseDetails.getCaseData().getClaimantRepresentativeOrganisationPolicy()).isEqualTo(
                OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                        ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel()).organisation(
                                Organisation.builder().organisationID(ORGANISATION_ID).build()).build());
        // when claimant representative exists, and noc remove option is organisation should revoke representation and
        // mark claimant as unrepresented
        tmpCaseDetails.getCaseData().setNocRemoveOption(NOC_REMOVE_OPTION_ORGANISATION);
        nocRemoveClaimantRepresentationService.revokeClaimantLegalRep(DUMMY_USER_TOKEN, tmpCaseDetails);
        verify(nocCcdService, times(LoggerTestUtils.INTEGER_TWO))
                .revokeClaimantRepresentation(DUMMY_USER_TOKEN, tmpCaseDetails);
        assertThat(tmpCaseDetails.getCaseData().getRepresentativeClaimantType()).isNull();
        assertThat(tmpCaseDetails.getCaseData().getClaimantRepresentativeRemoved()).isEqualTo(YES);
        assertThat(tmpCaseDetails.getCaseData().getClaimantRepresentedQuestion()).isEqualTo(NO);
        assertThat(tmpCaseDetails.getCaseData().getClaimantRepresentativeOrganisationPolicy()).isEqualTo(
                OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                        ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel()).build());
    }
}