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
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_YOURSELF;

@ExtendWith(SpringExtension.class)
class NocRemoveRepNotificationServiceTest {
    @Mock
    private EmailService emailService;
    @Mock
    private NocNotificationService nocNotificationService;

    @InjectMocks
    private NocRemoveRepNotificationService nocRemoveRepNotificationService;

    private CaseDetails caseDetails;

    private static final String REMOVE_REP_TEST_DATA_SOURCE_FILE = "nocRemoveRepTest.json";

    private static final String ORG_ADMIN_EMAIL = "org.admin@example.com";
    private static final String REPRESENTATIVE_NAME = "John Doe";
    private static final String USER_ID = "6281d99e-1a94-4369-870e-9f527801d913";
    private static final String USER_EMAIL = "user@example.com";
    private static final String USER_NAME = "John Smith";
    private static final String CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_ORG_ADMIN_TEMPLATE_ID =
            "6d53b5ec-9247-4abe-b48a-c796ee66fe92";
    private static final String CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_ORG_ADMIN_TEMPLATE_ID_FIELD =
            "claimantRepresentativeSelfRemovalOrgAdminTemplateId";
    private static final String CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_REPRESENTATIVE_TEMPLATE_ID =
            "fe52b39f-852c-43ca-a42a-b9a27c43b130";
    private static final String CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_REPRESENTATIVE_TEMPLATE_ID_FIELD =
            "claimantRepresentativeSelfRemovalRepTemplateId";

    private static final String EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION =
            "Failed to send NOC notification email to organisation admin, case id: 1775651960650043, error: ";
    private static final String EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_REPRESENTATIVE =
            "Failed to send NOC notification email to representative, case id: 1775651960650043, error: ";

    private static final String ERROR_ORGANISATION_ADMIN_EMAIL_NOT_FOUND = "Organisation admin email not found";
    private static final String ERROR_SYSTEM = "System error";

    @BeforeEach
    @SneakyThrows
    void setUp() {
        ReflectionTestUtils.setField(nocRemoveRepNotificationService,
                CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_ORG_ADMIN_TEMPLATE_ID_FIELD,
                CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_ORG_ADMIN_TEMPLATE_ID);
        ReflectionTestUtils.setField(nocRemoveRepNotificationService,
                CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_REPRESENTATIVE_TEMPLATE_ID_FIELD,
                CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_REPRESENTATIVE_TEMPLATE_ID);
        caseDetails = generateCaseDetails();
        LoggerTestUtils.initializeLogger(NocRemoveRepNotificationService.class);
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
        // when noc remove option is yourself should send claimant representative self removal org admin notification
        caseDetails.getCaseData().setNocRemoveOption(NOC_REMOVE_OPTION_YOURSELF);
        doNothing().when(emailService).sendEmail(eq(CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_ORG_ADMIN_TEMPLATE_ID),
                eq(ORG_ADMIN_EMAIL), anyMap());
        nocRemoveRepNotificationService.sendClaimantRepresentativeRemovalNotifications(userDetails, caseDetails);
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmail(
                eq(CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_ORG_ADMIN_TEMPLATE_ID), eq(ORG_ADMIN_EMAIL), anyMap());
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmail(
                eq(CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_REPRESENTATIVE_TEMPLATE_ID), eq(USER_EMAIL), anyMap());
        // when noc remove option is organisation should not send claimant representative self removal org admin
        // notification
        caseDetails.getCaseData().setNocRemoveOption(NOC_REMOVE_OPTION_ORGANISATION);
        nocRemoveRepNotificationService.sendClaimantRepresentativeRemovalNotifications(userDetails, caseDetails);
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmail(
                eq(CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_ORG_ADMIN_TEMPLATE_ID), eq(ORG_ADMIN_EMAIL), anyMap());
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmail(
                eq(CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_REPRESENTATIVE_TEMPLATE_ID), eq(USER_EMAIL), anyMap());
    }

    @Test
    void theSendClaimantRepresentativeSelfRemovalOrgAdminNotification() {
        // when organisation admin email is blank, should log a warning and not send email
        nocRemoveRepNotificationService.sendClaimantRepresentativeSelfRemovalOrgAdminNotification(caseDetails,
                StringUtils.EMPTY, REPRESENTATIVE_NAME);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION
                        + ERROR_ORGANISATION_ADMIN_EMAIL_NOT_FOUND);
        // when email is successfully sent, should invoke sendEmail at least once
        nocRemoveRepNotificationService.sendClaimantRepresentativeSelfRemovalOrgAdminNotification(caseDetails,
                ORG_ADMIN_EMAIL, REPRESENTATIVE_NAME);
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE))
                .sendEmail(eq(CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_ORG_ADMIN_TEMPLATE_ID), eq(ORG_ADMIN_EMAIL),
                        anyMap());
        // when email sending fails, should log a warning
        doThrow(new RuntimeException(ERROR_SYSTEM))
                .when(emailService).sendEmail(eq(CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_ORG_ADMIN_TEMPLATE_ID),
                        eq(ORG_ADMIN_EMAIL), anyMap());
        nocRemoveRepNotificationService.sendClaimantRepresentativeSelfRemovalOrgAdminNotification(caseDetails,
                ORG_ADMIN_EMAIL, REPRESENTATIVE_NAME);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_TWO,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION + ERROR_SYSTEM);
    }

    @Test
    void theSendClaimantRepresentativeSelfRemovalRepNotification() {
        // when email is successfully sent, should invoke sendEmail at least once
        nocRemoveRepNotificationService.sendClaimantRepresentativeSelfRemovalRepNotification(caseDetails, USER_EMAIL);
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE))
                .sendEmail(eq(CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_REPRESENTATIVE_TEMPLATE_ID), eq(USER_EMAIL),
                        anyMap());
        // when email sending fails, should log a warning
        doThrow(new RuntimeException(ERROR_SYSTEM))
                .when(emailService).sendEmail(eq(CLAIMANT_REPRESENTATIVE_SELF_REMOVAL_REPRESENTATIVE_TEMPLATE_ID),
                        eq(USER_EMAIL), anyMap());
        nocRemoveRepNotificationService.sendClaimantRepresentativeSelfRemovalRepNotification(caseDetails, USER_EMAIL);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_REPRESENTATIVE + ERROR_SYSTEM);
    }
}
