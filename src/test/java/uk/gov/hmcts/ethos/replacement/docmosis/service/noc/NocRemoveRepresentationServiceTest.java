package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.REMOVE_ONLY_ME;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.REMOVE_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EMPTY_STRING;

@ExtendWith(SpringExtension.class)
class NocRemoveRepresentationServiceTest {

    @InjectMocks
    private NocRemoveRepresentationService nocRemoveRepresentationService;
    @Mock
    private NocCcdService nocCcdService;
    @Mock
    private NocNotificationService nocNotificationService;
    @Mock
    private EmailService emailService;
    @Mock
    private CaseAccessService caseAccessService;
    @Mock
    private EmailNotificationService emailNotificationService;
    @Mock
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;

    private static final String USER_TOKEN = "userToken";

    private static final String TEMPLATE_NOC_ORG_ADMIN_NOT_REPRESENTING = "nocOrgAdminNotRepresentingTemplateId";
    private static final String TEMPLATE_NOC_LEGAL_REP_NO_LONGER_ASSIGNED = "nocLegalRepNoLongerAssignedTemplateId";
    private static final String TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED = "nocCitizenNoLongerRepresentedTemplateId";
    private static final String TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED = "nocOtherPartyNotRepresentedTemplateId";
    private static final String CASE_REFERENCE = "123456789/1234";
    private static final String ORG_A_NAME = "Org A";
    private static final String ORG_A_EMAIL = "org.a@test.com";
    private static final String ORG_CLAIMANT_NAME = "Org C";
    private static final String ORG_CLAIMANT_EMAIL = "org.c@test.com";
    private static final String REP_A1_NAME = "Legal Rep A1";
    private static final String REP_A1_EMAIL = "rep.a1@test.com";
    private static final String REP_A2_NAME = "Legal Rep A2";
    private static final String REP_A2_EMAIL = "rep.a2@test.com";
    private static final String REP_B_EMAIL = "rep.b@test.com";
    private static final String REP_CLAIMANT_NAME = "Legal Rep C";
    private static final String REP_CLAIMANT_EMAIL = "rep.c@test.com";
    private static final String CLAIMANT_NAME = "Chris Claimant";
    private static final String CLAIMANT_EMAIL = "claimant@test.com";
    private static final String RESPONDENT_1_NAME = "Rich Respondent";
    private static final String RESPONDENT_1_EMAIL = "rich@test.com";
    private static final String RESPONDENT_2_NAME = "Robert Respondent";
    private static final String RESPONDENT_2_EMAIL = "robert@test.com";
    private static final String RESPONDENT_3_NAME = "Rachel Respondent";
    private static final String RESPONDENT_3_EMAIL = "rachel@test.com";
    private static final String RESPONDENT_4_NAME = "Ryan Respondent";
    private static final String RESPONDENT_5_ID = "53f2060b-d0ce-4e73-bfbe-9b50e2af62d2";
    private static final String RESPONDENT_5_NAME = "Ruth Respondent";
    private static final String RESPONDENT_5_EMAIL = "ruth@test.com";
    private static final String RESPONDENT_LIST = RESPONDENT_1_NAME + " " + RESPONDENT_2_NAME
        + " " + RESPONDENT_3_NAME + " " + RESPONDENT_4_NAME + " " + RESPONDENT_5_NAME;
    private static final String LINK_SYA_CITIZEN_CASE = "linkClaimantCitizenCase";
    private static final String LINK_SYR_CITIZEN_CASE = "linkRespondentCitizenCase";
    private static final String LINK_EXUI_CASE = "linkExUICase";

    private CaseDetails caseDetails;
    private RepresentedTypeRItem repR1;
    private RepresentedTypeRItem repR2;
    private RepresentedTypeRItem repR3;

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {
        ReflectionTestUtils.setField(nocRemoveRepresentationService,
            TEMPLATE_NOC_ORG_ADMIN_NOT_REPRESENTING, TEMPLATE_NOC_ORG_ADMIN_NOT_REPRESENTING);
        ReflectionTestUtils.setField(nocRemoveRepresentationService,
            TEMPLATE_NOC_LEGAL_REP_NO_LONGER_ASSIGNED, TEMPLATE_NOC_LEGAL_REP_NO_LONGER_ASSIGNED);
        ReflectionTestUtils.setField(nocRemoveRepresentationService,
            TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED, TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED);
        ReflectionTestUtils.setField(nocRemoveRepresentationService,
            TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED, TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED);

        caseDetails = generateCaseDetails();
        repR1 = caseDetails.getCaseData().getRepCollection().get(0);
        repR2 = caseDetails.getCaseData().getRepCollection().get(1);
        repR3 = caseDetails.getCaseData().getRepCollection().get(2);
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
        when(caseAccessService.getCaseUserAssignmentsById(any()))
            .thenReturn(List.of(CaseUserAssignment.builder().build()));
        when(emailNotificationService.getRespondentsAndRepsEmailAddresses(any(), any()))
            .thenReturn(Map.of(REP_A1_EMAIL, "respondentId"));
        when(emailService.getCitizenCaseLink(anyString()))
            .thenReturn(LINK_SYA_CITIZEN_CASE);
        when(emailService.getSyrCaseLink(anyString(), anyString()))
            .thenReturn(LINK_SYR_CITIZEN_CASE);
        when(emailService.getExuiCaseLink(anyString()))
            .thenReturn(LINK_EXUI_CASE);

        nocRemoveRepresentationService.revokeClaimantLegalRep(caseDetails, USER_TOKEN);

        verify(nocCcdService, times(1))
            .revokeClaimantRepresentation(USER_TOKEN, caseDetails);
        // send email to organisation admin
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_ORG_ADMIN_NOT_REPRESENTING),
                eq(ORG_CLAIMANT_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "legalRepName", REP_CLAIMANT_NAME
                ))
            );
        // send email to removed legal rep
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_LEGAL_REP_NO_LONGER_ASSIGNED),
                eq(REP_CLAIMANT_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST
                ))
            );
        // send email to unrepresented party
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED),
                eq(CLAIMANT_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "legalRepOrg", ORG_CLAIMANT_NAME,
                    "linkToCitUI", LINK_SYA_CITIZEN_CASE
                ))
            );
        // send email to other party
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED),
                eq(REP_A1_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "party_name", CLAIMANT_NAME,
                    "linkToCitUI", LINK_SYR_CITIZEN_CASE
                ))
            );
    }

    @Test
    void shouldRevokeClaimantLegalRep_missingRepresentativeClaimantType() {
        caseDetails.getCaseData().setRepresentativeClaimantType(null);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> nocRemoveRepresentationService.revokeClaimantLegalRep(caseDetails, USER_TOKEN)
        );
        assertThat(exception.getMessage())
            .isEqualTo("Missing RepresentativeClaimantType for case id: 1775651960650043");
        verify(nocCcdService, times(0))
            .revokeClaimantRepresentation(USER_TOKEN, caseDetails);
    }

    @Test
    void hasMultipleRepresentativesForOrg_shouldReturnNo_whenNoRepresentatives() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        when(nocRespondentRepresentativeService.findRepresentativesByToken(anyString(), any()))
            .thenReturn(List.of());

        String result = nocRemoveRepresentationService.hasMultipleRepresentativesForOrg(caseDetails, USER_TOKEN);

        assertThat(result).isEqualTo("No");
    }

    @Test
    void hasMultipleRepresentativesForOrg_shouldReturnNo_whenNoOrganisationId() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        RepresentedTypeRItem repRx = RepresentedTypeRItem.builder()
            .id("1")
            .value(RepresentedTypeR.builder().build())
            .build();
        when(nocRespondentRepresentativeService.findRepresentativesByToken(anyString(), any()))
            .thenReturn(List.of(repRx));

        String result = nocRemoveRepresentationService.hasMultipleRepresentativesForOrg(caseDetails, USER_TOKEN);

        assertThat(result).isEqualTo("No");
    }

    @Test
    void hasMultipleRepresentativesForOrg_shouldReturnNo_whenBothSameRepresentative() {
        RepresentedTypeRItem repR1 = getTypeRItemWithSameOrgId();
        RepresentedTypeRItem repR2 = getTypeRItemWithSameOrgId();
        caseDetails.getCaseData().setRepCollection(List.of(repR1, repR2));
        when(nocRespondentRepresentativeService.findRepresentativesByToken(anyString(), any()))
            .thenReturn(List.of(repR1, repR2));

        String result = nocRemoveRepresentationService.hasMultipleRepresentativesForOrg(caseDetails, USER_TOKEN);

        assertThat(result).isEqualTo("No");
    }

    @Test
    void isMoreThanOneRespondent_shouldReturnYes_whenMoreThanOneRepresentative() {
        RepresentedTypeRItem repR1 = getTypeRItemWithSameOrgId();
        RepresentedTypeRItem repR2 = getTypeRItemWithSameOrgId();
        caseDetails.getCaseData().setRepCollection(List.of(repR1, repR2));
        when(nocRespondentRepresentativeService.findRepresentativesByToken(anyString(), any()))
            .thenReturn(List.of(repR1));

        String result = nocRemoveRepresentationService.hasMultipleRepresentativesForOrg(caseDetails, USER_TOKEN);

        assertThat(result).isEqualTo("Yes");
    }

    private RepresentedTypeRItem getTypeRItemWithSameOrgId() {
        return RepresentedTypeRItem.builder()
            .id(UUID.randomUUID().toString())
            .value(RepresentedTypeR.builder()
                .respondentOrganisation(Organisation.builder()
                    .organisationID("1")
                    .build())
                .build())
            .build();
    }

    @Test
    void shouldRevokeRespondentLegalRep_onlyCurrentRep() {
        caseDetails.getCaseData().setNocRemoveRepIsMoreThanOneFlag(YES);
        caseDetails.getCaseData().setNocRemoveRepOption(REMOVE_ONLY_ME);

        when(nocRespondentRepresentativeService.findRepresentativesByToken(anyString(), any()))
            .thenReturn(List.of(repR1, repR2));
        when(nocNotificationService
            .resolveRespondentRepresentativeOrganisationSuperuserEmail(any(), any(), anyString()))
            .thenReturn(ORG_A_EMAIL);
        when(caseAccessService.getCaseUserAssignmentsById(any()))
            .thenReturn(List.of(CaseUserAssignment.builder().build()));
        when(emailNotificationService.getRespondentsAndRepsEmailAddresses(any(), any()))
            .thenReturn(Map.of(
                REP_A2_EMAIL, EMPTY_STRING,
                REP_B_EMAIL, EMPTY_STRING,
                RESPONDENT_5_EMAIL, RESPONDENT_5_ID
            ));
        when(emailService.getCitizenCaseLink(anyString()))
            .thenReturn(LINK_SYA_CITIZEN_CASE);
        when(emailService.getSyrCaseLink(anyString(), anyString()))
            .thenReturn(LINK_SYR_CITIZEN_CASE);
        when(emailService.getExuiCaseLink(anyString()))
            .thenReturn(LINK_EXUI_CASE);

        nocRemoveRepresentationService.revokeRespondentLegalRep(caseDetails, USER_TOKEN);

        verify(nocRespondentRepresentativeService, times(1))
            .revokeAndRemoveRespondentRepresentatives(caseDetails, List.of(repR1, repR2));
        // send email to organisation admin
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_ORG_ADMIN_NOT_REPRESENTING),
                eq(ORG_A_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "legalRepName", REP_A1_NAME
                ))
            );
        // send email to removed legal rep
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_LEGAL_REP_NO_LONGER_ASSIGNED),
                eq(REP_A1_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST
                ))
            );
        verify(emailService, times(0))
            .sendEmail(
                eq(TEMPLATE_NOC_LEGAL_REP_NO_LONGER_ASSIGNED),
                eq(REP_A2_EMAIL),
                any()
            );
        // send email to unrepresented party
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED),
                eq(RESPONDENT_1_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "legalRepOrg", ORG_A_NAME,
                    "linkToCitUI", LINK_SYR_CITIZEN_CASE
                ))
            );
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED),
                eq(RESPONDENT_2_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "legalRepOrg", ORG_A_NAME,
                    "linkToCitUI", LINK_SYR_CITIZEN_CASE
                ))
            );
        verify(emailService, times(0))
            .sendEmail(
                eq(TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED),
                eq(RESPONDENT_3_EMAIL),
                any()
            );
        // send email to claimant
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED),
                eq(REP_CLAIMANT_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "party_name", RESPONDENT_1_NAME + ", " + RESPONDENT_2_NAME,
                    "linkToCitUI", LINK_EXUI_CASE
                ))
            );
        // send email to other respondent
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED),
                eq(REP_A2_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "party_name", RESPONDENT_1_NAME + ", " + RESPONDENT_2_NAME,
                    "linkToCitUI", LINK_EXUI_CASE
                ))
            );
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED),
                eq(REP_B_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "party_name", RESPONDENT_1_NAME + ", " + RESPONDENT_2_NAME,
                    "linkToCitUI", LINK_EXUI_CASE
                ))
            );
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED),
                eq(RESPONDENT_5_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "party_name", RESPONDENT_1_NAME + ", " + RESPONDENT_2_NAME,
                    "linkToCitUI", LINK_SYR_CITIZEN_CASE
                ))
            );
    }

    @Test
    void shouldRevokeRespondentLegalRep_removeOrg() {
        caseDetails.getCaseData().setNocRemoveRepIsMoreThanOneFlag(YES);
        caseDetails.getCaseData().setNocRemoveRepOption(REMOVE_ORGANISATION);

        when(nocRespondentRepresentativeService.findRepresentativesByToken(anyString(), any()))
            .thenReturn(List.of(repR1, repR2));
        when(nocNotificationService
            .resolveRespondentRepresentativeOrganisationSuperuserEmail(any(), any(), anyString()))
            .thenReturn(ORG_A_EMAIL);
        when(caseAccessService.getCaseUserAssignmentsById(any()))
            .thenReturn(List.of(CaseUserAssignment.builder().build()));
        when(emailNotificationService.getRespondentsAndRepsEmailAddresses(any(), any()))
            .thenReturn(Map.of(
                REP_A2_EMAIL, EMPTY_STRING,
                REP_B_EMAIL, EMPTY_STRING,
                RESPONDENT_5_EMAIL, RESPONDENT_5_ID
            ));
        when(emailService.getCitizenCaseLink(anyString()))
            .thenReturn(LINK_SYA_CITIZEN_CASE);
        when(emailService.getSyrCaseLink(anyString(), anyString()))
            .thenReturn(LINK_SYR_CITIZEN_CASE);
        when(emailService.getExuiCaseLink(anyString()))
            .thenReturn(LINK_EXUI_CASE);

        nocRemoveRepresentationService.revokeRespondentLegalRep(caseDetails, USER_TOKEN);

        verify(nocRespondentRepresentativeService, times(1))
            .revokeAndRemoveRespondentRepresentatives(caseDetails, List.of(repR1, repR2, repR3));
        // send email to organisation admin
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_ORG_ADMIN_NOT_REPRESENTING),
                eq(ORG_A_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "legalRepName", REP_A1_NAME + ", " + REP_A2_NAME
                ))
            );
        // send email to removed legal rep
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_LEGAL_REP_NO_LONGER_ASSIGNED),
                eq(REP_A1_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST
                ))
            );
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_LEGAL_REP_NO_LONGER_ASSIGNED),
                eq(REP_A2_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST
                ))
            );
        // send email to unrepresented party
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED),
                eq(RESPONDENT_1_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "legalRepOrg", ORG_A_NAME,
                    "linkToCitUI", LINK_SYR_CITIZEN_CASE
                ))
            );
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED),
                eq(RESPONDENT_2_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "legalRepOrg", ORG_A_NAME,
                    "linkToCitUI", LINK_SYR_CITIZEN_CASE
                ))
            );
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED),
                eq(RESPONDENT_3_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "legalRepOrg", ORG_A_NAME,
                    "linkToCitUI", LINK_SYR_CITIZEN_CASE
                ))
            );
        // send email to claimant
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED),
                eq(REP_CLAIMANT_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "party_name", RESPONDENT_1_NAME + ", " + RESPONDENT_2_NAME + ", " + RESPONDENT_3_NAME,
                    "linkToCitUI", LINK_EXUI_CASE
                ))
            );
        // send email to other respondent
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED),
                eq(REP_B_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "party_name", RESPONDENT_1_NAME + ", " + RESPONDENT_2_NAME + ", " + RESPONDENT_3_NAME,
                    "linkToCitUI", LINK_EXUI_CASE
                ))
            );
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED),
                eq(RESPONDENT_5_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "party_name", RESPONDENT_1_NAME + ", " + RESPONDENT_2_NAME + ", " + RESPONDENT_3_NAME,
                    "linkToCitUI", LINK_SYR_CITIZEN_CASE
                ))
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
        verify(nocRespondentRepresentativeService, times(0))
            .revokeAndRemoveRespondentRepresentatives(any(), any());
    }
}