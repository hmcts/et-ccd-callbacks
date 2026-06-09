package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    @InjectMocks
    private NocRemoveRepresentationService nocRemoveRepresentationService;

    private static final String USER_TOKEN = "userToken";
    private static final String ADMIN_TOKEN = "adminToken";

    private static final String ORG_A_NAME = "Org A";
    private static final String ORG_A_EMAIL = "org.a@test.com";
    private static final String ORG_CLAIMANT_NAME = "Org C";
    private static final String ORG_CLAIMANT_EMAIL = "org.c@test.com";
    private static final String REP_A1_NAME = "Legal Rep A1";
    private static final String REP_A1_EMAIL = "rep.a1@test.com";
    private static final String REP_A2_NAME = "Legal Rep A2";
    private static final String REP_A2_EMAIL = "rep.a2@test.com";
    private static final String REP_CLAIMANT_NAME = "Legal Rep C";
    private static final String REP_CLAIMANT_EMAIL = "rep.c@test.com";
    private static final String CLAIMANT_NAME = "Chris Claimant";
    private static final List<String> REP_EMAIL_LIST = new ArrayList<>(
            Arrays.asList("rep.a1@test.com", "rep.a1@test.com", "rep.a2@test.com", "rep.b@test.com", "ruth@test.com"));
    private static final String RESPONDENT_1_ID = "84ac5b15-f28a-40fe-a5f4-c7127366fb41";
    private static final String RESPONDENT_1_NAME = "Rich Respondent";
    private static final String RESPONDENT_2_ID = "dc890d23-21f1-4290-bc4f-db9ec272badf";
    private static final String RESPONDENT_2_NAME = "Robert Respondent";
    private static final String RESPONDENT_3_ID = "51f53e15-727f-4df8-85b9-085b0c0e866a";
    private static final String RESPONDENT_3_NAME = "Rachel Respondent";

    private CaseDetails caseDetails;
    private RepresentedTypeRItem repR1;
    private RepresentedTypeRItem repR2;
    private RepresentedTypeRItem repR3;

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {
        caseDetails = generateCaseDetails();
        repR1 = caseDetails.getCaseData().getRepCollection().get(LoggerTestUtils.INTEGER_ZERO);
        repR2 = caseDetails.getCaseData().getRepCollection().get(LoggerTestUtils.INTEGER_ONE);
        repR3 = caseDetails.getCaseData().getRepCollection().get(LoggerTestUtils.INTEGER_TWO);
    }

    private CaseDetails generateCaseDetails() throws URISyntaxException, IOException {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
            .getContextClassLoader().getResource("nocRemoveRepTest.json")).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
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
                caseDetails,
                ORG_CLAIMANT_EMAIL,
                REP_CLAIMANT_NAME
            );
        // send email to removed legal rep
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
            .sendEmailToRemovedLegalRep(
                caseDetails,
                REP_CLAIMANT_EMAIL
            );
        // send email to unrepresented party
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
            .sendEmailToUnrepresentedClaimant(
                caseDetails,
                ORG_CLAIMANT_NAME
            );
        // send email to other party
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
            .sendEmailToOtherPartyRespondent(
                    caseDetails,
                    REP_EMAIL_LIST,
                    CLAIMANT_NAME
            );
    }

    @Test
    void shouldRevokeClaimantLegalRep_missingRepresentativeClaimantType() {
        caseDetails.getCaseData().setRepresentativeClaimantType(null);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> nocRemoveRepresentationService.revokeClaimantLegalRep(caseDetails)
        );
        assertThat(exception.getMessage())
            .isEqualTo("Missing RepresentativeClaimantType for case id: 1775651960650043");
        verify(nocCcdService, times(LoggerTestUtils.INTEGER_ZERO))
            .revokeClaimantRepresentation(anyString(), any());
    }

    @Test
    void shouldRevokeRespondentLegalRep_onlyCurrentRep() {
        when(nocRespondentRepresentativeService.findRepresentativesByToken(anyString(), any()))
            .thenReturn(List.of(repR1, repR2));
        when(nocNotificationService
            .resolveRespondentRepresentativeOrganisationSuperuserEmail(any(), any(), anyString()))
            .thenReturn(ORG_A_EMAIL);

        nocRemoveRepresentationService.revokeRespondentLegalRep(caseDetails, USER_TOKEN);

        verify(nocRespondentRepresentativeService, times(1))
            .revokeAndRemoveRespondentRepresentatives(caseDetails, List.of(repR1, repR2));
        // send email to organisation admin
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
            .sendEmailToOrgAdmin(
                caseDetails,
                ORG_A_EMAIL,
                REP_A1_NAME
            );
        // send email to removed legal rep
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
            .sendEmailToListOfRemovedLegalRep(
                caseDetails,
                List.of(REP_A1_EMAIL)
            );
        // send email to unrepresented party
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
            .sendEmailToUnrepresentedRespondent(
                caseDetails,
                List.of(repR1, repR2),
                ORG_A_NAME
            );
        // send email to claimant
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
            .sendEmailToOtherPartyClaimant(
                caseDetails,
                RESPONDENT_1_NAME + ", " + RESPONDENT_2_NAME
            );
        // send email to other respondent
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
            .sendEmailToOtherPartyRespondent(
                caseDetails,
                List.of(RESPONDENT_1_ID, RESPONDENT_2_ID),
                RESPONDENT_1_NAME + ", " + RESPONDENT_2_NAME
            );
    }

    @Test
    void shouldRevokeRespondentLegalRep_removeOrg() {
        caseDetails.getCaseData().setNocRemoveRepOption(REMOVE_ORGANISATION);

        when(nocRespondentRepresentativeService.findRepresentativesByToken(anyString(), any()))
            .thenReturn(List.of(repR1, repR2));
        when(nocNotificationService
            .resolveRespondentRepresentativeOrganisationSuperuserEmail(any(), any(), anyString()))
            .thenReturn(ORG_A_EMAIL);

        nocRemoveRepresentationService.revokeRespondentLegalRep(caseDetails, USER_TOKEN);

        verify(nocRespondentRepresentativeService, times(LoggerTestUtils.INTEGER_ONE))
            .revokeAndRemoveRespondentRepresentatives(caseDetails, List.of(repR1, repR2, repR3));
        // send email to organisation admin
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
            .sendEmailToOrgAdmin(
                caseDetails,
                ORG_A_EMAIL,
                REP_A1_NAME + ", " + REP_A2_NAME
            );
        // send email to removed legal rep
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
            .sendEmailToListOfRemovedLegalRep(
                caseDetails,
                List.of(REP_A1_EMAIL, REP_A2_EMAIL)
            );
        // send email to unrepresented party
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
            .sendEmailToUnrepresentedRespondent(
                caseDetails,
                List.of(repR1, repR2, repR3),
                ORG_A_NAME
            );
        // send email to claimant
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
            .sendEmailToOtherPartyClaimant(
                caseDetails,
                RESPONDENT_1_NAME + ", " + RESPONDENT_2_NAME + ", " + RESPONDENT_3_NAME
            );
        // send email to other respondent
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
            .sendEmailToOtherPartyRespondent(
                caseDetails,
                List.of(RESPONDENT_1_ID, RESPONDENT_2_ID, RESPONDENT_3_ID),
                RESPONDENT_1_NAME + ", " + RESPONDENT_2_NAME + ", " + RESPONDENT_3_NAME
            );
    }

    @Test
    void shouldRevokeRespondentLegalRep_currentRepNotFound() {
        when(nocRespondentRepresentativeService.findRepresentativesByToken(anyString(), any()))
            .thenReturn(List.of());

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> nocRemoveRepresentationService.revokeRespondentLegalRep(caseDetails, USER_TOKEN)
        );
        assertThat(exception.getMessage())
            .isEqualTo("Missing RepresentedTypeRItem list for case id: 1775651960650043");
        verify(nocRespondentRepresentativeService, times(LoggerTestUtils.INTEGER_ZERO))
            .revokeAndRemoveRespondentRepresentatives(any(), any());
    }
}