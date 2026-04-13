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
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class NocRemoveRepresentationEmailServiceTest {
    @Mock
    private EmailService emailService;
    @Mock
    private CaseAccessService caseAccessService;
    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private NocRemoveRepresentationEmailService nocRemoveRepresentationEmailService;

    private static final String TEMPLATE_NOC_ORG_ADMIN_NOT_REPRESENTING = "nocOrgAdminNotRepresentingTemplateId";
    private static final String TEMPLATE_NOC_LEGAL_REP_NO_LONGER_ASSIGNED = "nocLegalRepNoLongerAssignedTemplateId";
    private static final String TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED = "nocCitizenNoLongerRepresentedTemplateId";
    private static final String TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED = "nocOtherPartyNotRepresentedTemplateId";
    private static final String CASE_REFERENCE = "123456789/1234";
    private static final String ORG_A_NAME = "Org A";
    private static final String ORG_A_EMAIL = "org.a@test.com";
    private static final String ORG_CLAIMANT_NAME = "Org C";
    private static final String REP_A1_NAME = "Legal Rep A1";
    private static final String REP_A1_EMAIL = "rep.a1@test.com";
    private static final String REP_A2_EMAIL = "rep.a2@test.com";
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
    private static final String RESPONDENT_4_EMAIL = "ryan@test.com";
    private static final String RESPONDENT_5_NAME = "Ruth Respondent";
    private static final String RESPONDENT_5_EMAIL = "ruth@test.com";
    private static final String RESPONDENT_LIST = RESPONDENT_1_NAME + " " + RESPONDENT_2_NAME
        + " " + RESPONDENT_3_NAME + " " + RESPONDENT_4_NAME + " " + RESPONDENT_5_NAME;
    private static final String LINK_SYA_CITIZEN_CASE = "linkClaimantCitizenCase";
    private static final String LINK_SYR_CITIZEN_CASE = "linkRespondentCitizenCase";
    private static final String LINK_EXUI_CASE = "linkExUICase";
    private static final String PARTY_NAME = "Party Name";

    private CaseDetails caseDetails;
    private RepresentedTypeRItem repR1;
    private RepresentedTypeRItem repR2;

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {
        ReflectionTestUtils.setField(nocRemoveRepresentationEmailService,
            TEMPLATE_NOC_ORG_ADMIN_NOT_REPRESENTING, TEMPLATE_NOC_ORG_ADMIN_NOT_REPRESENTING);
        ReflectionTestUtils.setField(nocRemoveRepresentationEmailService,
            TEMPLATE_NOC_LEGAL_REP_NO_LONGER_ASSIGNED, TEMPLATE_NOC_LEGAL_REP_NO_LONGER_ASSIGNED);
        ReflectionTestUtils.setField(nocRemoveRepresentationEmailService,
            TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED, TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED);
        ReflectionTestUtils.setField(nocRemoveRepresentationEmailService,
            TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED, TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED);

        caseDetails = generateCaseDetails();
        repR1 = caseDetails.getCaseData().getRepCollection().get(0);
        repR2 = caseDetails.getCaseData().getRepCollection().get(1);
    }

    private CaseDetails generateCaseDetails() throws URISyntaxException, IOException {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
            .getContextClassLoader().getResource("nocRemoveRepTest.json")).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    void sendEmailToOrgAdmin_sendsEmail_whenEmailPresent() {
        nocRemoveRepresentationEmailService.sendEmailToOrgAdmin(caseDetails, ORG_A_EMAIL, REP_A1_NAME);

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
    }

    @Test
    void sendEmailToOrgAdmin_doesNothing_whenEmailMissing() {
        nocRemoveRepresentationEmailService.sendEmailToOrgAdmin(caseDetails, null, REP_A1_NAME);

        verify(emailService, never())
            .sendEmail(any(), any(), any());
    }

    @Test
    void sendEmailToListOfRemovedLegalRep_callsSendEmailToRemovedLegalRepForEach() {
        List<String> emails = Arrays.asList(REP_A1_EMAIL, REP_A2_EMAIL);

        nocRemoveRepresentationEmailService.sendEmailToListOfRemovedLegalRep(caseDetails, emails);

        verify(emailService, times(2))
            .sendEmail(any(), any(), any());
    }

    @Test
    void sendEmailToRemovedLegalRep_sendsEmail() {
        nocRemoveRepresentationEmailService.sendEmailToRemovedLegalRep(caseDetails, REP_A1_EMAIL);

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
    }

    @Test
    void sendEmailToUnrepresentedClaimant_sendsEmail_whenEmailPresent() {
        when(emailService.getCitizenCaseLink(anyString()))
            .thenReturn(LINK_SYA_CITIZEN_CASE);

        nocRemoveRepresentationEmailService.sendEmailToUnrepresentedClaimant(caseDetails, ORG_CLAIMANT_NAME);

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
    }

    @Test
    void sendEmailToUnrepresentedClaimant_doesNothing_whenNoEmail() {
        caseDetails.getCaseData().getClaimantType().setClaimantEmailAddress(null);

        nocRemoveRepresentationEmailService.sendEmailToUnrepresentedClaimant(caseDetails, ORG_CLAIMANT_NAME);

        verify(emailService, never())
            .sendEmail(any(), any(), any());
    }

    @Test
    void sendEmailToUnrepresentedRespondent_sendsEmailForEachValidRespondent() {
        List<RepresentedTypeRItem> repListToRevoke = List.of(repR1, repR2);
        when(emailService.getSyrCaseLink(anyString(), anyString()))
            .thenReturn(LINK_SYR_CITIZEN_CASE);

        nocRemoveRepresentationEmailService.sendEmailToUnrepresentedRespondent(
            caseDetails, repListToRevoke, ORG_A_NAME);

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
    }

    @Test
    void sendEmailToOtherPartyClaimant_sendsEmail_whenClaimantRepresented() {
        when(emailService.getExuiCaseLink(anyString()))
            .thenReturn(LINK_EXUI_CASE);

        nocRemoveRepresentationEmailService.sendEmailToOtherPartyClaimant(caseDetails, PARTY_NAME);

        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED),
                eq(REP_CLAIMANT_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "party_name", PARTY_NAME,
                    "linkToCitUI", LINK_EXUI_CASE
                ))
            );
    }

    @Test
    void sendEmailToOtherPartyClaimant_sendsEmail_whenClaimantCitizen() {
        caseDetails.getCaseData().setRepresentativeClaimantType(null);
        when(emailService.getCitizenCaseLink(anyString()))
            .thenReturn(LINK_SYA_CITIZEN_CASE);

        nocRemoveRepresentationEmailService.sendEmailToOtherPartyClaimant(caseDetails, PARTY_NAME);

        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED),
                eq(CLAIMANT_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "party_name", PARTY_NAME,
                    "linkToCitUI", LINK_SYA_CITIZEN_CASE
                ))
            );
    }

    @Test
    void sendEmailToOtherPartyClaimant_doesNothing_whenNoEmail() {
        caseDetails.getCaseData().getRepresentativeClaimantType().setRepresentativeEmailAddress(null);

        nocRemoveRepresentationEmailService.sendEmailToOtherPartyClaimant(caseDetails, PARTY_NAME);

        verify(emailService, never())
            .sendEmail(any(), any(), any());
    }

    @Test
    void sendEmailToOtherPartyRespondent_sendsEmailToLegalRepAndRespondent() {
        when(caseAccessService.getCaseUserAssignmentsById(any()))
            .thenReturn(List.of(CaseUserAssignment.builder().build()));
        when(emailNotificationService.getRespondentSolicitorEmails(any()))
            .thenReturn(Set.of(REP_A1_EMAIL, REP_A2_EMAIL));
        when(emailService.getExuiCaseLink(anyString()))
            .thenReturn(LINK_EXUI_CASE);
        when(emailService.getSyrCaseLink(anyString(), anyString()))
            .thenReturn(LINK_SYR_CITIZEN_CASE);

        nocRemoveRepresentationEmailService.sendEmailToOtherPartyRespondent(caseDetails, List.of(), PARTY_NAME);

        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED),
                eq(REP_A1_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "party_name", PARTY_NAME,
                    "linkToCitUI", LINK_EXUI_CASE
                ))
            );
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED),
                eq(REP_A2_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "party_name", PARTY_NAME,
                    "linkToCitUI", LINK_EXUI_CASE
                ))
            );
        verify(emailService, never())
            .sendEmail(
                eq(TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED),
                eq(RESPONDENT_1_EMAIL),
                any()
            );
        verify(emailService, never())
            .sendEmail(
                eq(TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED),
                eq(RESPONDENT_2_EMAIL),
                any()
            );
        verify(emailService, never())
            .sendEmail(
                eq(TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED),
                eq(RESPONDENT_3_EMAIL),
                any()
            );
        verify(emailService, never())
            .sendEmail(
                eq(TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED),
                eq(RESPONDENT_4_EMAIL),
                any()
            );
        verify(emailService, times(1))
            .sendEmail(
                eq(TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED),
                eq(RESPONDENT_5_EMAIL),
                eq(Map.of(
                    "case_number", CASE_REFERENCE,
                    "claimant", CLAIMANT_NAME,
                    "list_of_respondents", RESPONDENT_LIST,
                    "party_name", PARTY_NAME,
                    "linkToCitUI", LINK_SYR_CITIZEN_CASE
                ))
            );
    }
}
