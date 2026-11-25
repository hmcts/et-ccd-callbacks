package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_CASE_SHARING_STATUS_NOT_SHARED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REPRESENTATIVE_TYPE_LEAD;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_REPRESENTATIVE_EMAIL_NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class NocRemoveRepNotificationServiceTest {
    @Mock
    private EmailService emailService;
    @Mock
    private NocNotificationService nocNotificationService;

    private NocRemoveRepNotificationService nocRemoveRepNotificationService;

    private CaseDetails caseDetails;

    private static final String REMOVE_REP_TEST_DATA_SOURCE_FILE = "nocRemoveRepTest.json";

    private static final String ORG_ADMIN_EMAIL = "org.admin@example.com";
    private static final String REPRESENTATIVE_NAME = "John Doe";
    private static final String USER_ID = "6281d99e-1a94-4369-870e-9f527801d913";
    private static final String USER_EMAIL = "user@example.com";
    private static final String USER_NAME = "John Smith";
    private static final String CLAIMANT_NON_SHARED_REP_REMOVAL_ORG_ADMIN_TEMPLATE_ID =
            "b29450e0-b047-49a3-b465-5d4df3f67401";
    private static final String CLAIMANT_REP_REMOVAL_REPRESENTATIVE_TEMPLATE_ID =
            "fe52b39f-852c-43ca-a42a-b9a27c43b130";

    private static final String EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION =
            "Failed to send NOC notification email to organisation admin, case id: 1775651960650043, error: ";
    private static final String EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_REPRESENTATIVE =
            "Failed to send NOC notification email to representative, case id: 1775651960650043, error: ";
    private static final String EXPECTED_WARNING_FAILED_TO_SEND_NOTIFICATION_EMAIL_REMOVED_REPRESENTATIVE_NOT_FOUND =
            "Failed to send NOC notification email, case id: 1775651960650043, error: Removed representative not found";

    private static final String ERROR_ORGANISATION_ADMIN_EMAIL_NOT_FOUND = "Organisation admin email not found";
    private static final String ERROR_SYSTEM = "System error";

    @BeforeEach
    @SneakyThrows
    void setUp() {
        nocRemoveRepNotificationService = new NocRemoveRepNotificationService(
                emailService,
                nocNotificationService,
                CLAIMANT_NON_SHARED_REP_REMOVAL_ORG_ADMIN_TEMPLATE_ID,
                CLAIMANT_REP_REMOVAL_REPRESENTATIVE_TEMPLATE_ID);
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
        // when removed representative is empty should log remove representative not found warning
        nocRemoveRepNotificationService.sendClaimantRepresentativeRemovalNotifications(userDetails, caseDetails, null);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_FAILED_TO_SEND_NOTIFICATION_EMAIL_REMOVED_REPRESENTATIVE_NOT_FOUND);
        // when removed representative is not lead should not send any notification
        nocRemoveRepNotificationService.sendClaimantRepresentativeRemovalNotifications(userDetails, caseDetails, null);
        verify(emailService, never()).sendEmail(eq(CLAIMANT_NON_SHARED_REP_REMOVAL_ORG_ADMIN_TEMPLATE_ID),
                eq(ORG_ADMIN_EMAIL), anyMap());
        // when representative is lead representative and sharing status is not shared should
        // send organisation admin notification with CLAIMANT_NON_SHARED_REP_REMOVAL_ORG_ADMIN_TEMPLATE_ID
        // and send representative notification with CLAIMANT_REP_REMOVAL_REPRESENTATIVE_TEMPLATE_ID
        caseDetails.getCaseData().setNocRemoveOption(NOC_REMOVE_OPTION_ORGANISATION);
        caseDetails.getCaseData().setNocCaseSharingStatus(NOC_CASE_SHARING_STATUS_NOT_SHARED);
        caseDetails.getCaseData().setNocRepresentativeType(NOC_REPRESENTATIVE_TYPE_LEAD);
        nocRemoveRepNotificationService.sendClaimantRepresentativeRemovalNotifications(userDetails, caseDetails,
                caseDetails.getCaseData().getRepresentativeClaimantType());
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmail(
                eq(CLAIMANT_NON_SHARED_REP_REMOVAL_ORG_ADMIN_TEMPLATE_ID), eq(ORG_ADMIN_EMAIL), anyMap());
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmail(
                eq(CLAIMANT_REP_REMOVAL_REPRESENTATIVE_TEMPLATE_ID), eq(USER_EMAIL), anyMap());
    }

    @Test
    void theSendClaimantRepRemovalOrgAdminNotification() {
        // when organisation admin email is blank, should log a warning and not send email
        nocRemoveRepNotificationService.sendClaimantNonSharedLeadRepRemovalOrgAdminNotification(caseDetails,
                StringUtils.EMPTY, REPRESENTATIVE_NAME);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION
                        + ERROR_ORGANISATION_ADMIN_EMAIL_NOT_FOUND);
        // when email is successfully sent, should invoke sendEmail at least once
        nocRemoveRepNotificationService.sendClaimantNonSharedLeadRepRemovalOrgAdminNotification(caseDetails,
                ORG_ADMIN_EMAIL, REPRESENTATIVE_NAME);
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE))
                .sendEmail(eq(CLAIMANT_NON_SHARED_REP_REMOVAL_ORG_ADMIN_TEMPLATE_ID), eq(ORG_ADMIN_EMAIL),
                        anyMap());
        // when email sending fails, should log a warning
        doThrow(new RuntimeException(ERROR_SYSTEM))
                .when(emailService).sendEmail(eq(CLAIMANT_NON_SHARED_REP_REMOVAL_ORG_ADMIN_TEMPLATE_ID),
                        eq(ORG_ADMIN_EMAIL), anyMap());
        nocRemoveRepNotificationService.sendClaimantNonSharedLeadRepRemovalOrgAdminNotification(caseDetails,
                ORG_ADMIN_EMAIL, REPRESENTATIVE_NAME);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_TWO,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_ORGANISATION + ERROR_SYSTEM);
    }

    @Test
    void theSendClaimantRepresentativeSelfRemovalRepNotification() {
        // when representative email not found should log a warning and return
        nocRemoveRepNotificationService.sendClaimantNonSharedLeadRepRemovalRepresentativeNotification(caseDetails,
                StringUtils.EMPTY);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_REPRESENTATIVE
                        + WARNING_REPRESENTATIVE_EMAIL_NOT_FOUND);
        // when email is successfully sent, should invoke sendEmail at least once
        nocRemoveRepNotificationService.sendClaimantNonSharedLeadRepRemovalRepresentativeNotification(caseDetails,
                USER_EMAIL);
        verify(emailService, times(LoggerTestUtils.INTEGER_ONE))
                .sendEmail(eq(CLAIMANT_REP_REMOVAL_REPRESENTATIVE_TEMPLATE_ID), eq(USER_EMAIL),
                        anyMap());
        // when email sending fails, should log a warning
        doThrow(new RuntimeException(ERROR_SYSTEM))
                .when(emailService).sendEmail(eq(CLAIMANT_REP_REMOVAL_REPRESENTATIVE_TEMPLATE_ID),
                        eq(USER_EMAIL), anyMap());
        nocRemoveRepNotificationService.sendClaimantNonSharedLeadRepRemovalRepresentativeNotification(caseDetails,
                USER_EMAIL);
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_TWO,
                EXPECTED_WARNING_FAILED_TO_SEND_NOC_NOTIFICATION_EMAIL_REPRESENTATIVE + ERROR_SYSTEM);
    }
}
