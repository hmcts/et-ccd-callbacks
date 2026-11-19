package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.OrganisationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.RespondentService;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.LoggingUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_YOURSELF;

@ExtendWith(SpringExtension.class)
class NocRemoveClaimantRepNotificationServiceTest {
    @Mock
    private EmailService emailService;
    @Mock
    private NocNotificationService nocNotificationService;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private RespondentService respondentService;

    @InjectMocks
    private NocRemoveClaimantRepNotificationService nocRemoveClaimantRepNotificationService;

    private CaseDetails caseDetails;

    private static final String REMOVE_REP_TEST_DATA_SOURCE_FILE = "nocRemoveRepTest.json";

    private static final String ORG_ADMIN_EMAIL = "org.admin@example.com";
    private static final String ORGANISATION_NAME = "Organisation Name";
    private static final String REPRESENTATIVE_NAME = "John Doe";
    private static final String USER_ID = "6281d99e-1a94-4369-870e-9f527801d913";
    private static final String USER_EMAIL = "user@example.com";
    private static final String USER_NAME = "John Smith";
    private static final String CLAIMANT_EMAIL = "claimant@test.com";
    private static final String RESPONDENT_ID = "6281d99e-1a94-4369-870e-9f527801d000";
    private static final String RESPONDENT_NAME = "Abuzer Kadayif";
    private static final String RESPONDENT_EMAIL = "respondent@test.com";
    private static final String DUMMY_NOC_REMOVE_OPTION = "dummyNocRemoveOption";

    private static final String CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_ORG_ADMIN_TEMPLATE_ID_FIELD =
            "claimantRepresentativeSelfRemovalOrgAdminTemplateId";
    private static final String CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_ORG_ADMIN_TEMPLATE_ID =
            "6d53b5ec-9247-4abe-b48a-c796ee66fe92";
    private static final String CLAIMANT_REPRESENTATIVE_ORGANISATION_REMOVAL_ORG_ADMIN_TEMPLATE_ID_FIELD =
            "claimantRepresentativeOrganisationRemovalOrgAdminTemplateId";
    private static final String CLAIMANT_REPRESENTATIVE_ORGANISATION_REMOVAL_ORG_ADMIN_TEMPLATE_ID =
            "039c5bc5-f2a7-4ba0-b217-ef493c582d2e";
    private static final String CLAIMANT_REPRESENTATIVE_REMOVAL_REPRESENTATIVE_TEMPLATE_ID_FIELD =
            "claimantRepresentativeRemovalRepTemplateId";
    private static final String CLAIMANT_REPRESENTATIVE_REMOVAL_REPRESENTATIVE_TEMPLATE_ID =
            "fe52b39f-852c-43ca-a42a-b9a27c43b130";
    private static final String CLAIMANT_REPRESENTATIVE_REMOVAL_CLAIMANT_TEMPLATE_ID_FIELD =
            "claimantRepresentativeOrganisationRemovalClaimantTemplateId";
    private static final String CLAIMANT_REPRESENTATIVE_REMOVAL_CLAIMANT_TEMPLATE_ID =
            "7bee670f-9110-4000-98ef-5f96274a68fb";
    private static final String CLAIMANT_REPRESENTATIVE_REMOVAL_RESPONDENT_TEMPLATE_ID_FIELD =
            "claimantRepresentativeOrganisationRemovalRespondentTemplateId";
    private static final String CLAIMANT_REPRESENTATIVE_REMOVAL_RESPONDENT_TEMPLATE_ID =
            "4ecc836b-ad03-4b6e-b449-51d0e9d48241";

    private static final String CITIZEN_CASE_DETAILS_URL_FIELD = "citizenUrl";
    private static final String CITIZEN_CASE_DETAILS_URL = "http://localhost:3001/citizen-hub/";
    private static final String CITIZIEN_CASE_DETAILS_LINK = "http://localhost:3001/citizen-hub/1775651960650043";
    private static final String RESPONDENT_CASE_DETAILS_URL_FIELD = "syrUrl";
    private static final String RESPONDENT_CASE_DETAILS_URL = "http://localhost:3003/case-details/";
    private static final String RESPONDENT_CASE_DETAILS_LINK =
            "http://localhost:3003/case-details/1775651960650043/6281d99e-1a94-4369-870e-9f527801d913";

    private static final String EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION =
            "Failed to send NOC notification email to organisation admin, case id: 1775651960650043, error: ";
    private static final String EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_REPRESENTATIVE =
            "Failed to send NOC notification email to representative, case id: 1775651960650043, error: ";
    private static final String EXPECTED_WARNING_INVALID_REMOVE_OPTION =
            "Invalid remove option, case id: 1775651960650043, remove option: " + DUMMY_NOC_REMOVE_OPTION;
    private static final String EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT =
            "Failed to send noc notification email to claimant, case id: 1775651960650043, error: ";
    private static final String EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_RESPONDENT =
            "Failed to send noc notification email to respondent, case id: 1775651960650043, error: ";

    private static final String WARNING_CLAIMANT_EMAIL_NOT_FOUND = "Claimant email not found";
    private static final String WARNING_ORGANISATION_ADMIN_EMAIL_NOT_FOUND = "Organisation admin email not found";
    private static final String WARNING_RESPONDENT_EMAIL_NOT_FOUND = "Respondent email not found";
    private static final String WARNING_SYSTEM = "System error";

    @BeforeEach
    @SneakyThrows
    void setUp() {
        ReflectionTestUtils.setField(nocRemoveClaimantRepNotificationService,
                CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_ORG_ADMIN_TEMPLATE_ID_FIELD,
                CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_ORG_ADMIN_TEMPLATE_ID);
        ReflectionTestUtils.setField(nocRemoveClaimantRepNotificationService,
                CLAIMANT_REPRESENTATIVE_ORGANISATION_REMOVAL_ORG_ADMIN_TEMPLATE_ID_FIELD,
                CLAIMANT_REPRESENTATIVE_ORGANISATION_REMOVAL_ORG_ADMIN_TEMPLATE_ID);
        ReflectionTestUtils.setField(nocRemoveClaimantRepNotificationService,
                CLAIMANT_REPRESENTATIVE_REMOVAL_REPRESENTATIVE_TEMPLATE_ID_FIELD,
                CLAIMANT_REPRESENTATIVE_REMOVAL_REPRESENTATIVE_TEMPLATE_ID);
        ReflectionTestUtils.setField(nocRemoveClaimantRepNotificationService,
                CLAIMANT_REPRESENTATIVE_REMOVAL_CLAIMANT_TEMPLATE_ID_FIELD,
                CLAIMANT_REPRESENTATIVE_REMOVAL_CLAIMANT_TEMPLATE_ID);
        ReflectionTestUtils.setField(nocRemoveClaimantRepNotificationService,
                CLAIMANT_REPRESENTATIVE_REMOVAL_RESPONDENT_TEMPLATE_ID_FIELD,
                CLAIMANT_REPRESENTATIVE_REMOVAL_RESPONDENT_TEMPLATE_ID);
        ReflectionTestUtils.setField(emailService, CITIZEN_CASE_DETAILS_URL_FIELD, CITIZEN_CASE_DETAILS_URL);
        ReflectionTestUtils.setField(emailService, RESPONDENT_CASE_DETAILS_URL_FIELD, RESPONDENT_CASE_DETAILS_URL);

        caseDetails = generateCaseDetails();
        LoggerTestUtils.initializeLogger(LoggingUtils.class);
    }

    private CaseDetails generateCaseDetails() throws URISyntaxException, IOException {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
                .getContextClassLoader().getResource(REMOVE_REP_TEST_DATA_SOURCE_FILE)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    void theSendClaimantRepresentativeRemovalNotifications() {
        UserDetails userDetails = new UserDetails();
        userDetails.setUid(USER_ID);
        userDetails.setEmail(USER_EMAIL);
        userDetails.setName(USER_NAME);
        when(nocNotificationService.findClaimantRepOrgSuperUserEmail(caseDetails.getCaseData()
                .getRepresentativeClaimantType())).thenReturn(ORG_ADMIN_EMAIL);
        // when noc remove option is not valid should log warning and return without sending any notification
        caseDetails.getCaseData().setNocRemoveOption(DUMMY_NOC_REMOVE_OPTION);
        nocRemoveClaimantRepNotificationService
                .sendClaimantRepresentativeRemovalNotifications(userDetails, caseDetails);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE, EXPECTED_WARNING_INVALID_REMOVE_OPTION);
        // when noc remove option is yourself should send claimant representative self removal org admin notification
        caseDetails.getCaseData().setNocRemoveOption(NOC_REMOVE_OPTION_YOURSELF);
        doNothing().when(emailService).sendEmail(eq(CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_ORG_ADMIN_TEMPLATE_ID),
                eq(ORG_ADMIN_EMAIL), anyMap());
        nocRemoveClaimantRepNotificationService
                .sendClaimantRepresentativeRemovalNotifications(userDetails, caseDetails);
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmail(
                eq(CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_ORG_ADMIN_TEMPLATE_ID), eq(ORG_ADMIN_EMAIL), anyMap());
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmail(
                eq(CLAIMANT_REPRESENTATIVE_REMOVAL_REPRESENTATIVE_TEMPLATE_ID), eq(USER_EMAIL), anyMap());
        // when noc removal option is organisation should send notifications to claimant, representative him/herself,
        // org admin, all respondents in the claim and if it is a multiple case, to all other claimants
        caseDetails.getCaseData().setNocRemoveOption(NOC_REMOVE_OPTION_ORGANISATION);
        when(organisationService.resolveClaimantRepresentativeOrganisationName(
                caseDetails.getCaseData().getRepresentativeClaimantType(), userDetails)).thenReturn(ORGANISATION_NAME);
        when(emailService.getCitizenCaseLink(caseDetails.getCaseId())).thenReturn(CITIZIEN_CASE_DETAILS_LINK);
        when(emailService.getSyrCaseLink(eq(caseDetails.getCaseId()), anyString()))
                .thenReturn(RESPONDENT_CASE_DETAILS_LINK);
        when(respondentService.resolveRepresentativeOrRespondentEmail(eq(caseDetails.getCaseData()),
                any(RespondentSumTypeItem.class))).thenReturn(RESPONDENT_EMAIL);
        nocRemoveClaimantRepNotificationService
                .sendClaimantRepresentativeRemovalNotifications(userDetails, caseDetails);
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmail(
                eq(CLAIMANT_REPRESENTATIVE_ORGANISATION_REMOVAL_ORG_ADMIN_TEMPLATE_ID), eq(ORG_ADMIN_EMAIL), anyMap());
        verify(emailService, times(LoggerTestUtils.INTEGER_TWO)).sendEmail(
                eq(CLAIMANT_REPRESENTATIVE_REMOVAL_REPRESENTATIVE_TEMPLATE_ID), eq(USER_EMAIL), anyMap());
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmail(
                eq(CLAIMANT_REPRESENTATIVE_REMOVAL_CLAIMANT_TEMPLATE_ID), eq(CLAIMANT_EMAIL), anyMap());
        verify(emailService, times(LoggerTestUtils.INTEGER_FIVE)).sendEmail(
                eq(CLAIMANT_REPRESENTATIVE_REMOVAL_RESPONDENT_TEMPLATE_ID), eq(RESPONDENT_EMAIL), anyMap());
    }

    @Test
    void theSendClaimantRepresentativeSelfRemovalOrgAdminNotification() {
        // when organisation admin email is blank, should log organisation admin email not found warning
        nocRemoveClaimantRepNotificationService.sendClaimantRepresentativeRemovalOrgAdminNotification(caseDetails,
                StringUtils.EMPTY, REPRESENTATIVE_NAME);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION
                        + WARNING_ORGANISATION_ADMIN_EMAIL_NOT_FOUND);
        // when org admin email is valid and noc removal type is yourself, should invoke sendEmail with claimant
        // representative self removal org admin template id
        caseDetails.getCaseData().setNocRemoveOption(NOC_REMOVE_OPTION_YOURSELF);
        nocRemoveClaimantRepNotificationService.sendClaimantRepresentativeRemovalOrgAdminNotification(caseDetails,
                ORG_ADMIN_EMAIL, REPRESENTATIVE_NAME);
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE))
                .sendEmail(eq(CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_ORG_ADMIN_TEMPLATE_ID), eq(ORG_ADMIN_EMAIL),
                        anyMap());
        // when org admin email is valid and noc removal type is organisation, should invoke sendEmail with claimant
        // representative organisation removal org admin template id
        caseDetails.getCaseData().setNocRemoveOption(NOC_REMOVE_OPTION_ORGANISATION);
        nocRemoveClaimantRepNotificationService.sendClaimantRepresentativeRemovalOrgAdminNotification(caseDetails,
                ORG_ADMIN_EMAIL, REPRESENTATIVE_NAME);
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE))
                .sendEmail(eq(CLAIMANT_REPRESENTATIVE_ORGANISATION_REMOVAL_ORG_ADMIN_TEMPLATE_ID), eq(ORG_ADMIN_EMAIL),
                        anyMap());
        // when email sending fails, should log failed to send noc notification email, organisation warning
        doThrow(new RuntimeException(WARNING_SYSTEM))
                .when(emailService).sendEmail(eq(CLAIMANT_REPRESENTATIVE_ORGANISATION_REMOVAL_ORG_ADMIN_TEMPLATE_ID),
                        eq(ORG_ADMIN_EMAIL), anyMap());
        nocRemoveClaimantRepNotificationService.sendClaimantRepresentativeRemovalOrgAdminNotification(caseDetails,
                ORG_ADMIN_EMAIL, REPRESENTATIVE_NAME);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_TWO,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION + WARNING_SYSTEM);
    }

    @Test
    void theSendClaimantRepresentativeSelfRemovalRepNotification() {
        // when email is successfully sent, should invoke sendEmail at least once
        nocRemoveClaimantRepNotificationService.sendClaimantRepresentativeRemovalRepNotification(caseDetails,
                USER_EMAIL);
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE))
                .sendEmail(eq(CLAIMANT_REPRESENTATIVE_REMOVAL_REPRESENTATIVE_TEMPLATE_ID), eq(USER_EMAIL),
                        anyMap());
        // when email sending fails, should log a warning
        doThrow(new RuntimeException(WARNING_SYSTEM))
                .when(emailService).sendEmail(eq(CLAIMANT_REPRESENTATIVE_REMOVAL_REPRESENTATIVE_TEMPLATE_ID),
                        eq(USER_EMAIL), anyMap());
        nocRemoveClaimantRepNotificationService.sendClaimantRepresentativeRemovalRepNotification(caseDetails,
                USER_EMAIL);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_REPRESENTATIVE + WARNING_SYSTEM);
    }

    @Test
    void theSendClaimantRepresentativeRemovalClaimantNotification() {
        // when claimant does not have e-mail address should log claimant email not found warning
        UserDetails userDetails = new UserDetails();
        CaseDetails tmpCaseDetails = new CaseDetails();
        tmpCaseDetails.setCaseId(caseDetails.getCaseId());
        nocRemoveClaimantRepNotificationService.sendClaimantRepresentativeRemovalClaimantNotification(userDetails,
                tmpCaseDetails);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT
                        + WARNING_CLAIMANT_EMAIL_NOT_FOUND);
        // when claimant has a valid email should send notification
        when(organisationService.resolveClaimantRepresentativeOrganisationName(caseDetails.getCaseData()
                .getRepresentativeClaimantType(), userDetails)).thenReturn(ORGANISATION_NAME);
        when(emailService.getCitizenCaseLink(caseDetails.getCaseId())).thenReturn(CITIZIEN_CASE_DETAILS_LINK);
        doNothing().when(emailService).sendEmail(eq(CLAIMANT_REPRESENTATIVE_REMOVAL_CLAIMANT_TEMPLATE_ID),
                eq(CLAIMANT_EMAIL), anyMap());
        nocRemoveClaimantRepNotificationService.sendClaimantRepresentativeRemovalClaimantNotification(userDetails,
                caseDetails);
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE))
                .sendEmail(eq(CLAIMANT_REPRESENTATIVE_REMOVAL_CLAIMANT_TEMPLATE_ID), eq(CLAIMANT_EMAIL), anyMap());
        // when unable to send email should log failed to send notification warning
        doThrow(new RuntimeException(WARNING_SYSTEM))
                .when(emailService).sendEmail(eq(CLAIMANT_REPRESENTATIVE_REMOVAL_CLAIMANT_TEMPLATE_ID),
                        eq(CLAIMANT_EMAIL), anyMap());
        nocRemoveClaimantRepNotificationService.sendClaimantRepresentativeRemovalClaimantNotification(userDetails,
                caseDetails);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_TWO,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_CLAIMANT + WARNING_SYSTEM);
    }

    @Test
    void theSendClaimantRepresentativeRemovalRespondentNotifications() {
        CaseData caseData = new CaseData();
        CaseDetails tmpCaseDetails = new CaseDetails();
        tmpCaseDetails.setCaseData(caseData);
        // when case data does not have any respondent should not send email
        nocRemoveClaimantRepNotificationService
                .sendClaimantRepresentativeRemovalRespondentNotifications(tmpCaseDetails);
        verifyNoInteractions(emailService);
        // when case data does not have any valid respondent should not send email
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        caseData.setRespondentCollection(List.of(respondent));
        nocRemoveClaimantRepNotificationService
                .sendClaimantRepresentativeRemovalRespondentNotifications(tmpCaseDetails);
        verifyNoInteractions(emailService);
        // when email address to send not found should not send email and log warning
        respondent.setId(RESPONDENT_ID);
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME).build());
        tmpCaseDetails.setCaseId(caseDetails.getCaseId());
        when(respondentService.resolveRepresentativeOrRespondentEmail(caseData, respondent))
                .thenReturn(StringUtils.EMPTY);
        nocRemoveClaimantRepNotificationService
                .sendClaimantRepresentativeRemovalRespondentNotifications(tmpCaseDetails);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_RESPONDENT
                        + WARNING_RESPONDENT_EMAIL_NOT_FOUND);
        verifyNoInteractions(emailService);
        // when sends email should validate interaction with email service
        tmpCaseDetails.getCaseData().setClaimant(caseDetails.getCaseData().getClaimant());
        tmpCaseDetails.getCaseData().setRespondentCollection(List.of(respondent));
        tmpCaseDetails.getCaseData().setEthosCaseReference(caseDetails.getCaseData().getEthosCaseReference());
        when(respondentService.resolveRepresentativeOrRespondentEmail(caseData, respondent))
                .thenReturn(RESPONDENT_EMAIL);
        when(emailService.getSyrCaseLink(tmpCaseDetails.getCaseId(), respondent.getId()))
                .thenReturn(RESPONDENT_CASE_DETAILS_LINK);
        doNothing().when(emailService).sendEmail(eq(CLAIMANT_REPRESENTATIVE_REMOVAL_RESPONDENT_TEMPLATE_ID),
                eq(USER_EMAIL), anyMap());
        nocRemoveClaimantRepNotificationService
                .sendClaimantRepresentativeRemovalRespondentNotifications(tmpCaseDetails);
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmail(
                eq(CLAIMANT_REPRESENTATIVE_REMOVAL_RESPONDENT_TEMPLATE_ID), eq(RESPONDENT_EMAIL), anyMap());
        // when unable to send email should log exception as a warning
        doThrow(new RuntimeException(WARNING_SYSTEM)).when(emailService)
                .sendEmail(eq(CLAIMANT_REPRESENTATIVE_REMOVAL_RESPONDENT_TEMPLATE_ID), eq(RESPONDENT_EMAIL), anyMap());
        nocRemoveClaimantRepNotificationService
                .sendClaimantRepresentativeRemovalRespondentNotifications(tmpCaseDetails);
        verify(emailService, times(LoggerTestUtils.INTEGER_TWO)).sendEmail(
                eq(CLAIMANT_REPRESENTATIVE_REMOVAL_RESPONDENT_TEMPLATE_ID), eq(RESPONDENT_EMAIL), anyMap());
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_TWO,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_RESPONDENT + WARNING_SYSTEM);
    }
}
