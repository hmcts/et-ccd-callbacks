package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EMAIL_TYPE_TO_ORG_ADMIN_NO_REP_LEFT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EMAIL_TYPE_TO_ORG_ADMIN_REMOVED;

@ExtendWith(SpringExtension.class)
class NocRemoveRepresentationEmailServiceTest {
    @Mock
    private EmailService emailService;

    @InjectMocks
    private NocRemoveRepresentationEmailService nocRemoveRepresentationEmailService;

    private static final String TEMPLATE_NOC_ORG_ADMIN_NOT_REPRESENTING = "nocOrgAdminNotRepresentingTemplateId";
    private static final String TEMPLATE_NOC_ORG_ADMIN_NO_REP_LEFT = "nocOrgAdminNoRepLeftTemplateId";
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
    private static final String RESPONDENT_ID_6 = "respondent_id_6";
    private static final String RESPONDENT_NAME_6 = "respondent_name_6";
    private static final String RESPONDENT_EMAIL_6 = "respondent_email_6@gmail.com";
    private static final String RESPONDENT_1_NAME = "Rich Respondent";
    private static final String RESPONDENT_1_EMAIL = "rich@test.com";
    private static final String RESPONDENT_2_NAME = "Robert Respondent";
    private static final String RESPONDENT_2_EMAIL = "robert@test.com";
    private static final String RESPONDENT_3_NAME = "Rachel Respondent";
    private static final String RESPONDENT_4_NAME = "Ryan Respondent";
    private static final String RESPONDENT_5_NAME = "Ruth Respondent";
    private static final String RESPONDENT_LIST_1 = RESPONDENT_1_NAME + " " + RESPONDENT_2_NAME
            + " " + RESPONDENT_3_NAME + " " + RESPONDENT_4_NAME + " " + RESPONDENT_5_NAME;
    private static final String RESPONDENT_LIST_2 = RESPONDENT_1_NAME + " " + RESPONDENT_2_NAME
        + " " + RESPONDENT_3_NAME + " " + RESPONDENT_4_NAME + " " + RESPONDENT_5_NAME + " " + RESPONDENT_NAME_6;
    private static final String LINK_SYA_CITIZEN_CASE = "linkClaimantCitizenCase";
    private static final String LINK_SYR_CITIZEN_CASE = "linkRespondentCitizenCase";
    private static final String LINK_EXUI_CASE = "linkExUICase";
    private static final String PARTY_NAME = "Party Name";

    private static final String PROPERTY_CASE_NUMBER = "case_number";
    private static final String PROPERTY_CLAIMANT_NAME = "claimant";
    private static final String PROPERTY_RESPONDENT_LIST = "list_of_respondents";
    private static final String PROPERTY_LEGAL_REP_NAME = "legalRepName";
    private static final String PROPERTY_ORGANISATION_LEGAL_REP = "legalRepOrg";
    private static final String PROPERTY_PARTY_NAME = "party_name";
    private static final String PROPERTY_LINK_TO_CIT_UI = "linkToCitUI";

    private static final String EXCEPTION_FAILED_TO_SEND_EMAIL = "Failed to send email";
    private static final String EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_RESPONDENT =
            "Failed to send noc notification email to respondent, case id: 1775651960650043, error: Failed to send "
                    + "email";
    private static final String EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION =
            "Failed to send NOC notification email to organisation admin, case id: 1775651960650043, error: Failed to "
                    + "send email";

    private CaseDetails caseDetails;
    private RespondentSumTypeItem respondent1;
    private RespondentSumTypeItem respondent2;

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {
        ReflectionTestUtils.setField(nocRemoveRepresentationEmailService,
            TEMPLATE_NOC_ORG_ADMIN_NOT_REPRESENTING, TEMPLATE_NOC_ORG_ADMIN_NOT_REPRESENTING);
        ReflectionTestUtils.setField(nocRemoveRepresentationEmailService,
                TEMPLATE_NOC_ORG_ADMIN_NO_REP_LEFT, TEMPLATE_NOC_ORG_ADMIN_NO_REP_LEFT);
        ReflectionTestUtils.setField(nocRemoveRepresentationEmailService,
            TEMPLATE_NOC_LEGAL_REP_NO_LONGER_ASSIGNED, TEMPLATE_NOC_LEGAL_REP_NO_LONGER_ASSIGNED);
        ReflectionTestUtils.setField(nocRemoveRepresentationEmailService,
            TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED, TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED);
        ReflectionTestUtils.setField(nocRemoveRepresentationEmailService,
            TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED, TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED);
        LoggerTestUtils.initializeLogger(NocRemoveRepresentationEmailService.class);
        caseDetails = generateCaseDetails();
        respondent1 = caseDetails.getCaseData().getRespondentCollection().get(0);
        respondent2 = caseDetails.getCaseData().getRespondentCollection().get(1);
    }

    private CaseDetails generateCaseDetails() throws URISyntaxException, IOException {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
            .getContextClassLoader().getResource("nocRemoveRepTest.json")).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    void sendEmailToOrgAdmin() {
        // when email not exists should not send email
        nocRemoveRepresentationEmailService.sendEmailToOrgAdmin(caseDetails, StringUtils.EMPTY, REP_A1_NAME,
                EMAIL_TYPE_TO_ORG_ADMIN_REMOVED);
        verifyNoInteractions(emailService);
        // when email type is orgAdminRemoved should send with org admin not representing template
        nocRemoveRepresentationEmailService.sendEmailToOrgAdmin(caseDetails, ORG_A_EMAIL, REP_A1_NAME,
                EMAIL_TYPE_TO_ORG_ADMIN_REMOVED);
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE))
            .sendEmail(
                TEMPLATE_NOC_ORG_ADMIN_NOT_REPRESENTING,
                ORG_A_EMAIL,
                Map.of(
                        PROPERTY_CASE_NUMBER, CASE_REFERENCE,
                        PROPERTY_CLAIMANT_NAME, CLAIMANT_NAME,
                        PROPERTY_RESPONDENT_LIST, RESPONDENT_LIST_1,
                        PROPERTY_LEGAL_REP_NAME, REP_A1_NAME
                )
            );
        // when email type is orgAdminNoRepLeft should send with org admin not representing template
        nocRemoveRepresentationEmailService.sendEmailToOrgAdmin(caseDetails, ORG_A_EMAIL, REP_A1_NAME,
                EMAIL_TYPE_TO_ORG_ADMIN_NO_REP_LEFT);
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmail(TEMPLATE_NOC_ORG_ADMIN_NO_REP_LEFT,
            ORG_A_EMAIL,
            Map.of(
                    PROPERTY_CASE_NUMBER, CASE_REFERENCE,
                    PROPERTY_CLAIMANT_NAME, CLAIMANT_NAME,
                    PROPERTY_RESPONDENT_LIST, RESPONDENT_LIST_1,
                    PROPERTY_LEGAL_REP_NAME, REP_A1_NAME
            )
        );
        // when not able to send email should log exception as a warning
        doThrow(new RuntimeException(EXCEPTION_FAILED_TO_SEND_EMAIL)).when(emailService).sendEmail(
                TEMPLATE_NOC_ORG_ADMIN_NO_REP_LEFT,
                ORG_A_EMAIL,
                Map.of(
                        PROPERTY_CASE_NUMBER, CASE_REFERENCE,
                        PROPERTY_CLAIMANT_NAME, CLAIMANT_NAME,
                        PROPERTY_RESPONDENT_LIST, RESPONDENT_LIST_1,
                        PROPERTY_LEGAL_REP_NAME, REP_A1_NAME
                )
        );
        nocRemoveRepresentationEmailService.sendEmailToOrgAdmin(caseDetails, ORG_A_EMAIL, REP_A1_NAME,
                EMAIL_TYPE_TO_ORG_ADMIN_NO_REP_LEFT);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION);
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
                TEMPLATE_NOC_LEGAL_REP_NO_LONGER_ASSIGNED,
                REP_A1_EMAIL,
                Map.of(
                    PROPERTY_CASE_NUMBER, CASE_REFERENCE,
                    PROPERTY_CLAIMANT_NAME, CLAIMANT_NAME,
                    PROPERTY_RESPONDENT_LIST, RESPONDENT_LIST_1
                )
            );
    }

    @Test
    void sendEmailToUnrepresentedClaimant_sendsEmail_whenEmailPresent() {
        when(emailService.getCitizenCaseLink(anyString()))
            .thenReturn(LINK_SYA_CITIZEN_CASE);

        nocRemoveRepresentationEmailService.sendEmailToUnrepresentedClaimant(caseDetails, ORG_CLAIMANT_NAME);

        verify(emailService, times(1))
            .sendEmail(
                TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED,
                CLAIMANT_EMAIL,
                Map.of(
                        PROPERTY_CASE_NUMBER, CASE_REFERENCE,
                        PROPERTY_CLAIMANT_NAME, CLAIMANT_NAME,
                        PROPERTY_RESPONDENT_LIST, RESPONDENT_LIST_1,
                        PROPERTY_ORGANISATION_LEGAL_REP, ORG_CLAIMANT_NAME,
                        PROPERTY_LINK_TO_CIT_UI, LINK_SYA_CITIZEN_CASE
                )
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
        List<RespondentSumTypeItem> repsRevokedRespondents = List.of(respondent1, respondent2);
        when(emailService.getSyrCaseLink(anyString(), anyString()))
            .thenReturn(LINK_SYR_CITIZEN_CASE);

        nocRemoveRepresentationEmailService.sendRepresentationRemovedEmailToRespondents(
            caseDetails, repsRevokedRespondents, ORG_A_NAME);

        verify(emailService, times(1))
            .sendEmail(
                TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED,
                RESPONDENT_1_EMAIL,
                Map.of(
                        PROPERTY_CASE_NUMBER, CASE_REFERENCE,
                        PROPERTY_CLAIMANT_NAME, CLAIMANT_NAME,
                        PROPERTY_RESPONDENT_LIST,
                        RESPONDENT_LIST_1,
                        PROPERTY_ORGANISATION_LEGAL_REP,
                        ORG_A_NAME,
                        PROPERTY_LINK_TO_CIT_UI, LINK_SYR_CITIZEN_CASE
                )
            );
        verify(emailService, times(1))
            .sendEmail(
                TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED,
                RESPONDENT_2_EMAIL,
                Map.of(
                        PROPERTY_CASE_NUMBER, CASE_REFERENCE,
                        PROPERTY_CLAIMANT_NAME, CLAIMANT_NAME,
                        PROPERTY_RESPONDENT_LIST, RESPONDENT_LIST_1,
                        PROPERTY_ORGANISATION_LEGAL_REP, ORG_A_NAME,
                        PROPERTY_LINK_TO_CIT_UI, LINK_SYR_CITIZEN_CASE
                ));
    }

    @Test
    void sendEmailToOtherPartyClaimant_sendsEmail_whenClaimantRepresented() {
        when(emailService.getExuiCaseLink(anyString()))
            .thenReturn(LINK_EXUI_CASE);

        nocRemoveRepresentationEmailService.sendEmailToOtherPartyClaimant(caseDetails, PARTY_NAME);

        verify(emailService, times(1))
            .sendEmail(
                TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED,
                REP_CLAIMANT_EMAIL,
                Map.of(
                        PROPERTY_CASE_NUMBER, CASE_REFERENCE,
                        PROPERTY_CLAIMANT_NAME, CLAIMANT_NAME,
                        PROPERTY_RESPONDENT_LIST, RESPONDENT_LIST_1,
                        PROPERTY_PARTY_NAME, PARTY_NAME,
                        PROPERTY_LINK_TO_CIT_UI, LINK_EXUI_CASE
                )
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
                TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED,
                CLAIMANT_EMAIL,
                Map.of(
                        PROPERTY_CASE_NUMBER, CASE_REFERENCE,
                        PROPERTY_CLAIMANT_NAME, CLAIMANT_NAME,
                        PROPERTY_RESPONDENT_LIST, RESPONDENT_LIST_1,
                        PROPERTY_PARTY_NAME, PARTY_NAME,
                        PROPERTY_LINK_TO_CIT_UI, LINK_SYA_CITIZEN_CASE
                )
            );
    }

    @Test
    void sendEmailToOtherPartyClaimant_doesNothing_whenNoEmail() {
        caseDetails.getCaseData().getRepresentativeClaimantType().setRepresentativeEmailAddress(null);
        caseDetails.getCaseData().getClaimantType().setClaimantEmailAddress(null);
        nocRemoveRepresentationEmailService.sendEmailToOtherPartyClaimant(caseDetails, PARTY_NAME);

        verify(emailService, never())
            .sendEmail(any(), any(), any());
    }

    @Test
    void sendEmailToOtherRespondents() {
        // when there is no other party left should not send email
        List<RespondentSumTypeItem> removedRespondents =
                new ArrayList<>(caseDetails.getCaseData().getRespondentCollection());
        nocRemoveRepresentationEmailService.sendEmailToOtherRespondents(caseDetails, removedRespondents, PARTY_NAME);
        verifyNoInteractions(emailService);
        // when other respondent exist, but they are not valid should not send e-mail
        RespondentSumTypeItem notRemovedRespondent = new RespondentSumTypeItem();
        caseDetails.getCaseData().getRespondentCollection().add(notRemovedRespondent);
        nocRemoveRepresentationEmailService.sendEmailToOtherRespondents(caseDetails, removedRespondents, PARTY_NAME);
        verifyNoInteractions(emailService);
        // when other respondent is valid but not has email address should not send email
        notRemovedRespondent.setId(RESPONDENT_ID_6);
        notRemovedRespondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_6).build());
        nocRemoveRepresentationEmailService.sendEmailToOtherRespondents(caseDetails, removedRespondents, PARTY_NAME);
        verifyNoInteractions(emailService);
        // when other respondent is valid and has email address should send email
        notRemovedRespondent.getValue().setRespondentEmail(RESPONDENT_EMAIL_6);
        when(emailService.getSyrCaseLink(caseDetails.getCaseId(), RESPONDENT_ID_6)).thenReturn(LINK_SYR_CITIZEN_CASE);
        doNothing().when(emailService).sendEmail(
                TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED,
                RESPONDENT_EMAIL_6,
                Map.of(
                        PROPERTY_CLAIMANT_NAME, CLAIMANT_NAME,
                        PROPERTY_RESPONDENT_LIST, RESPONDENT_LIST_2,
                        PROPERTY_CASE_NUMBER, CASE_REFERENCE,
                        PROPERTY_PARTY_NAME, PARTY_NAME,
                        PROPERTY_LINK_TO_CIT_UI, LINK_SYR_CITIZEN_CASE
                )
        );
        nocRemoveRepresentationEmailService.sendEmailToOtherRespondents(caseDetails, removedRespondents, PARTY_NAME);
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmail(
                TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED,
                RESPONDENT_EMAIL_6,
                Map.of(
                        PROPERTY_CLAIMANT_NAME, CLAIMANT_NAME,
                        PROPERTY_RESPONDENT_LIST, RESPONDENT_LIST_2,
                        PROPERTY_CASE_NUMBER, CASE_REFERENCE,
                        PROPERTY_PARTY_NAME, PARTY_NAME,
                        PROPERTY_LINK_TO_CIT_UI, LINK_SYR_CITIZEN_CASE
                )
        );
        // when not able to send email should log exception as a warning
        doThrow(new RuntimeException(EXCEPTION_FAILED_TO_SEND_EMAIL)).when(emailService).sendEmail(
                TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED,
                RESPONDENT_EMAIL_6,
                Map.of(
                        PROPERTY_CLAIMANT_NAME, CLAIMANT_NAME,
                        PROPERTY_RESPONDENT_LIST, RESPONDENT_LIST_2,
                        PROPERTY_CASE_NUMBER, CASE_REFERENCE,
                        PROPERTY_PARTY_NAME, PARTY_NAME,
                        PROPERTY_LINK_TO_CIT_UI, LINK_SYR_CITIZEN_CASE
                )
        );
        nocRemoveRepresentationEmailService.sendEmailToOtherRespondents(caseDetails, removedRespondents, PARTY_NAME);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_RESPONDENT);
    }
}
