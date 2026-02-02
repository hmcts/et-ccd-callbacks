package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.HubLinksStatuses;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.EmailUtils;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_STARTED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_VIEWED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SEND_NOTIFICATION_RESPONSE_REQUIRED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TRIBUNAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService.CASE_MANAGEMENT_ORDERS_REQUESTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService.EMPLOYER_CONTRACT_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService.NOTICE_OF_EMPLOYER_CONTRACT_CLAIM;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TornadoService.NOTIFICATION_SUMMARY_PDF;

@ExtendWith(SpringExtension.class)
class SendNotificationServiceTest {

    @Mock
    private HearingSelectionService hearingSelectionService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private CaseAccessService caseAccessService;
    @MockBean
    private AdminUserService adminUserService;
    @Mock
    private TornadoService tornadoService;
    @MockBean
    private EmailNotificationService emailNotificationService;
    private CaseData caseData;
    private CaseDetails caseDetails;
    private SendNotificationService sendNotificationService;

    private EmailService emailService;

    @Captor
    ArgumentCaptor<Map<String, String>> personalisationCaptor;
    private static final String CLAIMANT_SEND_NOTIFICATION_TEMPLATE_ID =
            "claimantSendNotificationTemplateId";
    private static final String RESPONDENT_SEND_NOTIFICATION_TEMPLATE_ID =
            "respondentSendNotificationTemplateId";
    private static final String BUNDLES_SUBMITTED_NOTIFICATION_FOR_CLAIMANT_TEMPLATE_ID =
            "bundlesSubmittedNotificationForClaimantTemplateId";
    private static final String BUNDLES_SUBMITTED_NOTIFICATION_FOR_TRIBUNAL_TEMPLATE_ID =
            "bundlesSubmittedNotificationForTribunalTemplateId";
    private static final String ERROR_MSG_PARTY_TO_NOTIFY_MUST_INCLUDE_SELECTED =
        "Select the party or parties to notify must include the party or parties who must respond";
    private static final String CLAIMANT_ONLY = "Claimant only";
    private static final String DUMMY_ADMIN_USER_TOKEN = "DUMMY_ADMIN_USER_TOKEN";

    @BeforeEach
     void setUp() {
        emailService = spy(new EmailUtils());
        sendNotificationService = new SendNotificationService(hearingSelectionService,
                emailService, featureToggleService, caseAccessService, tornadoService, emailNotificationService);
        ReflectionTestUtils.setField(sendNotificationService,
                RESPONDENT_SEND_NOTIFICATION_TEMPLATE_ID,
                "respondentSendNotificationTemplateId");
        ReflectionTestUtils.setField(sendNotificationService,
                CLAIMANT_SEND_NOTIFICATION_TEMPLATE_ID,
                "claimantSendNotificationTemplateId");
        ReflectionTestUtils.setField(sendNotificationService,
                BUNDLES_SUBMITTED_NOTIFICATION_FOR_CLAIMANT_TEMPLATE_ID,
                "bundlesSubmittedNotificationForClaimantTemplateId");
        ReflectionTestUtils.setField(sendNotificationService,
                BUNDLES_SUBMITTED_NOTIFICATION_FOR_TRIBUNAL_TEMPLATE_ID,
                "bundlesSubmittedNotificationForTribunalTemplateId");

        caseDetails = CaseDataBuilder.builder().withEthosCaseReference("1234")
                .withClaimantType("claimant@email.com")
                .withRespondent("Name", YES, "2020-01-02", "respondent@email.com", false)
                .withRespondentRepresentative("Name", "Sally", "respondentRep@email.com")
                .withRepresentativeClaimantType("Mr Claimant Represent",  "claimantRep@email.com")
                .buildAsCaseDetails(SCOTLAND_CASE_TYPE_ID);

        caseDetails.setCaseId("1234");

        caseData = caseDetails.getCaseData();
        caseData.setClaimant("claimant");
        caseData.setRespondent("claimant");
        caseData.setTargetHearingDate("2020-01-02");

        caseData.setSendNotificationTitle("title");
        caseData.setSendNotificationLetter("no");
        caseData.setSendNotificationUploadDocument(new ArrayList<>());
        caseData.setSendNotificationSubject(List.of("Hearing", "Judgment"));
        caseData.setSendNotificationAdditionalInfo("info");
        caseData.setSendNotificationNotify("Both parties");
        caseData.setSendNotificationSelectHearing(null);
        caseData.setSendNotificationCaseManagement("");
        caseData.setSendNotificationResponseTribunal("no");
        caseData.setSendNotificationWhoCaseOrder("Judge");
        caseData.setSendNotificationSelectParties("Both parties");
        caseData.setSendNotificationFullName("John Doe");
        caseData.setSendNotificationFullName2("John Doe");
        caseData.setSendNotificationDecision("Other");
        caseData.setSendNotificationDetails("details");
        caseData.setSendNotificationRequestMadeBy("Judge");
        caseData.setNotificationSentFrom("60001");

        //ensures that the case is for system user
        caseDetails.getCaseData().setEt1OnlineSubmission("Yes");
        caseDetails.getCaseData().setHubLinksStatuses(new HubLinksStatuses());
    }

    @Test
    void testCreateSendNotification() {

        sendNotificationService.createSendNotification(caseData);
        SendNotificationType sendNotificationType = caseData.getSendNotificationCollection().getFirst().getValue();

        assertEquals("title", sendNotificationType.getSendNotificationTitle());
        assertEquals("no", sendNotificationType.getSendNotificationLetter());
        assertEquals(0, sendNotificationType.getSendNotificationUploadDocument().size());
        assertEquals("[Hearing, Judgment]", sendNotificationType.getSendNotificationSubject().toString());
        assertEquals("info", sendNotificationType.getSendNotificationAdditionalInfo());
        assertEquals("Both parties", sendNotificationType.getSendNotificationNotify());
        assertNull(sendNotificationType.getSendNotificationSelectHearing());
        assertEquals("", sendNotificationType.getSendNotificationCaseManagement());
        assertEquals("no", sendNotificationType.getSendNotificationResponseTribunal());
        assertEquals("Judge", sendNotificationType.getSendNotificationWhoCaseOrder());
        assertEquals("Both parties", sendNotificationType.getSendNotificationSelectParties());
        assertEquals("John Doe", sendNotificationType.getSendNotificationFullName());
        assertEquals("John Doe", sendNotificationType.getSendNotificationFullName2());
        assertEquals("Other", sendNotificationType.getSendNotificationDecision());
        assertEquals("details", sendNotificationType.getSendNotificationDetails());
        assertEquals("Judge", sendNotificationType.getSendNotificationRequestMadeBy());
        assertEquals(NOT_VIEWED_YET, sendNotificationType.getNotificationState());
        assertEquals(YES, sendNotificationType.getSendNotificationResponseTribunalTable());
        assertEquals("Hearing, Judgment", sendNotificationType.getSendNotificationSubjectString());
        assertEquals("0", sendNotificationType.getSendNotificationResponsesCount());
        assertEquals(TRIBUNAL, sendNotificationType.getSendNotificationSentBy());
        assertEquals("60001", sendNotificationType.getNotificationSentFrom());
    }

    @Test
    void testCreateSendNotificationWhenRespondentShouldBeNotified() {
        caseData.setSendNotificationSelectParties(RESPONDENT_ONLY);
        sendNotificationService.createSendNotification(caseData);
        SendNotificationType sendNotificationType = caseData.getSendNotificationCollection().getFirst().getValue();
        assertEquals(NOT_VIEWED_YET, sendNotificationType.getNotificationState());
    }

    @Test
    void testCreateSendNotificationWhenClaimantShouldBeNotified() {
        caseData.setSendNotificationSubject(List.of(CASE_MANAGEMENT_ORDERS_REQUESTS));
        caseData.setSendNotificationSelectParties(CLAIMANT_ONLY);
        caseData.setSendNotificationResponseTribunal(SEND_NOTIFICATION_RESPONSE_REQUIRED);
        sendNotificationService.createSendNotification(caseData);
        SendNotificationType sendNotificationType = caseData.getSendNotificationCollection().getFirst().getValue();
        assertEquals(NOT_STARTED_YET, sendNotificationType.getNotificationState());
    }

    @Test
    void testCreateSendNotificationWhenRespondentOnlyRequired() {
        caseData.setSendNotificationSubject(List.of(CASE_MANAGEMENT_ORDERS_REQUESTS));
        caseData.setSendNotificationSelectParties(RESPONDENT_ONLY);
        caseData.setSendNotificationResponseTribunal(SEND_NOTIFICATION_RESPONSE_REQUIRED);
        sendNotificationService.createSendNotification(caseData);
        SendNotificationType sendNotificationType = caseData.getSendNotificationCollection().getFirst().getValue();
        assertEquals(NOT_VIEWED_YET, sendNotificationType.getNotificationState());
    }

    @Test
    void testCreateSendNotificationWhenCmoAndNoResponseRequired() {
        caseData.setSendNotificationSubject(List.of(CASE_MANAGEMENT_ORDERS_REQUESTS));
        caseData.setSendNotificationSelectParties(RESPONDENT_ONLY);
        sendNotificationService.createSendNotification(caseData);
        SendNotificationType sendNotificationType = caseData.getSendNotificationCollection().getFirst().getValue();
        assertEquals(NOT_VIEWED_YET, sendNotificationType.getNotificationState());
    }

    @Test
    void testClearSendNotificationFields() {
        sendNotificationService.clearSendNotificationFields(caseData);

        assertNull(caseData.getSendNotificationTitle());
        assertNull(caseData.getSendNotificationLetter());
        assertNull(caseData.getSendNotificationUploadDocument());
        assertNull(caseData.getSendNotificationSubject());
        assertNull(caseData.getSendNotificationAdditionalInfo());
        assertNull(caseData.getSendNotificationNotify());
        assertNull(caseData.getSendNotificationSelectHearing());
        assertNull(caseData.getSendNotificationCaseManagement());
        assertNull(caseData.getSendNotificationResponseTribunal());
        assertNull(caseData.getSendNotificationWhoCaseOrder());
        assertNull(caseData.getSendNotificationSelectParties());
        assertNull(caseData.getSendNotificationFullName());
        assertNull(caseData.getSendNotificationFullName2());
        assertNull(caseData.getSendNotificationDecision());
        assertNull(caseData.getSendNotificationDetails());
        assertNull(caseData.getSendNotificationRequestMadeBy());
    }

    @Test
    void testPopulateHearingSelection() {
        sendNotificationService.populateHearingSelection(caseData);
        verify(hearingSelectionService, times(1)).getHearingSelectionSortedByDateTime(any());
    }

    @Test
    void sendNotifyEmails_bothParties() {
        caseData.setSendNotificationNotify(BOTH_PARTIES);
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(1))
                .sendEmail(eq(CLAIMANT_SEND_NOTIFICATION_TEMPLATE_ID), any(), personalisationCaptor.capture());
        verify(emailService, times(1))
                .sendEmail(eq(RESPONDENT_SEND_NOTIFICATION_TEMPLATE_ID), any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("exuiUrl1234", val.get("environmentUrl"));
    }

    @Test
    void sendNotifyEmails_noClaimantEmail_onlySendsRespondentEmail() {
        caseData.setSendNotificationNotify(BOTH_PARTIES);
        caseData.getClaimantType().setClaimantEmailAddress(null);
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(1))
                .sendEmail(eq(RESPONDENT_SEND_NOTIFICATION_TEMPLATE_ID), any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("exuiUrl1234", val.get("environmentUrl"));
    }

    @Test
    void sendNotifyEmails_noRespondentEmail_onlySendsClaimantEmail() {
        caseData.setSendNotificationNotify(BOTH_PARTIES);
        caseData.getRespondentCollection().forEach(o -> o.getValue().setRespondentEmail(null));
        caseData.getRepCollection().forEach(o -> o.getValue().setRepresentativeEmailAddress(null));
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(1)).sendEmail(eq(CLAIMANT_SEND_NOTIFICATION_TEMPLATE_ID), any(), any());
    }

    @Test
    void sendNotifyEmails_EccToBothParties() {
        when(featureToggleService.isEccEnabled()).thenReturn(true);
        caseData.setSendNotificationSubject(List.of("Employer Contract Claim"));
        caseData.setSendNotificationNotify(BOTH_PARTIES);
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(1)).sendEmail(eq(CLAIMANT_SEND_NOTIFICATION_TEMPLATE_ID), any(), any());
        verify(emailService, times(1)).sendEmail(eq(RESPONDENT_SEND_NOTIFICATION_TEMPLATE_ID), any(), any());
    }

    @Test
    void sendNotifyEmails_EccDisabled() {
        when(featureToggleService.isEccEnabled()).thenReturn(false);
        caseData.setSendNotificationSubject(List.of("Employer Contract Claim"));
        caseData.setSendNotificationNotify(BOTH_PARTIES);
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(0)).sendEmail(eq(CLAIMANT_SEND_NOTIFICATION_TEMPLATE_ID), any(), any());
        verify(emailService, times(0)).sendEmail(eq(RESPONDENT_SEND_NOTIFICATION_TEMPLATE_ID), any(), any());
    }

    @Test
    void sendNotifyEmails_claimantOnly() {
        caseData.setSendNotificationNotify(CLAIMANT_ONLY);
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(1))
                .sendEmail(eq(CLAIMANT_SEND_NOTIFICATION_TEMPLATE_ID), any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("citizenUrl1234", val.get("environmentUrl"));
    }

    @Test
    void sendNotifyEmails_ClaimantNotRepresented() {
        caseData.setSendNotificationSubject(List.of("OTHER_SUBJECT"));
        when(featureToggleService.isEccEnabled()).thenReturn(true);
        caseData.setSendNotificationNotify(CLAIMANT_ONLY);
        ClaimantType mockClaimantType = mock(ClaimantType.class);
        mockClaimantType.setClaimantEmailAddress("claimant@example.com");
        caseData.setClaimantType(mockClaimantType);
        when(caseData.getClaimantType().getClaimantEmailAddress()).thenReturn("claimant@example.com");

        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(1)).sendEmail(
                eq(CLAIMANT_SEND_NOTIFICATION_TEMPLATE_ID),
                eq("claimant@example.com"),
                personalisationCaptor.capture());
    }

    @Test
    void sendNotifyEmails_ClaimantRepresented() {
        caseDetails.getCaseData().setSendNotificationSubject(List.of("OTHER_SUBJECT"));
        when(featureToggleService.isEccEnabled()).thenReturn(true);
        caseDetails.getCaseData().setSendNotificationNotify(CLAIMANT_ONLY);
        caseDetails.getCaseData().setCaseSource("MyHMCTS");
        caseDetails.getCaseData().setClaimantRepresentedQuestion("Yes");
        RepresentedTypeC representedTypeC = new RepresentedTypeC();
        representedTypeC.setNameOfRepresentative("testRep");
        representedTypeC.setRepresentativeEmailAddress("rep@example.com");
        Organisation org = Organisation.builder().organisationID("myHmctsOrgId").organisationName("testOrg").build();
        representedTypeC.setMyHmctsOrganisation(org);
        caseDetails.getCaseData().setRepresentativeClaimantType(representedTypeC);
        when(emailService.getClaimantRepExuiCaseNotificationsLink(anyString()))
                .thenReturn("http://localhost:3455/cases/case-details/"
                        + caseDetails.getCaseId() + "#Notifications");
        CaseUserAssignment mockAssignment = CaseUserAssignment
                .builder()
                .userId("claimantSolicitorUserId")
                .caseRole(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())
                .build();

        when(caseAccessService.getCaseUserAssignmentsById(anyString())).thenReturn(
                List.of(mockAssignment));

        UserDetails userDetails = mock(UserDetails.class);
        when(adminUserService.getAdminUserToken()).thenReturn(DUMMY_ADMIN_USER_TOKEN);
        when(adminUserService.getUserDetails(eq(DUMMY_ADMIN_USER_TOKEN), anyString())).thenReturn(userDetails);
        when(userDetails.getEmail()).thenReturn("rep@example.com");
        when(emailNotificationService.getCaseClaimantSolicitorEmails(List.of(mockAssignment)))
                .thenReturn(List.of("rep@example.com"));

        personalisationCaptor.getAllValues().clear();
        sendNotificationService.sendNotifyEmails(caseDetails);

        verify(emailService, times(1)).sendEmail(any(), any(), anyMap());
    }

    @Test
    void sendNotifyEmails_ClaimantRepresented_WithSharedList() {
        caseDetails.getCaseData().setSendNotificationSubject(List.of("OTHER_SUBJECT"));
        when(featureToggleService.isEccEnabled()).thenReturn(true);
        caseDetails.getCaseData().setSendNotificationNotify(CLAIMANT_ONLY);
        caseDetails.getCaseData().setCaseSource("MyHMCTS");
        caseDetails.getCaseData().setClaimantRepresentedQuestion("Yes");
        RepresentedTypeC representedTypeC = new RepresentedTypeC();
        representedTypeC.setNameOfRepresentative("testRep");
        representedTypeC.setRepresentativeEmailAddress("rep@example.com");
        Organisation org = Organisation.builder().organisationID("myHmctsOrgId").organisationName("testOrg").build();
        representedTypeC.setMyHmctsOrganisation(org);
        caseDetails.getCaseData().setRepresentativeClaimantType(representedTypeC);
        when(emailService.getClaimantRepExuiCaseNotificationsLink(anyString()))
                .thenReturn("http://localhost:3455/cases/case-details/"
                        + caseDetails.getCaseId() + "#Notifications");

        CaseUserAssignment assignment1 = CaseUserAssignment.builder()
                .userId("claimantSolicitorUserId")
                .caseRole(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())
                .build();

        CaseUserAssignment assignment2 = CaseUserAssignment.builder()
                .userId("sharedListUserId")
                .caseRole(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())
                .build();

        CaseUserAssignment assignment3 = CaseUserAssignment.builder()
                .userId("sharedListUserId2")
                .caseRole(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())
                .build();

        List<CaseUserAssignment> assignments = List.of(assignment1, assignment2, assignment3);

        when(caseAccessService.getCaseUserAssignmentsById(anyString())).thenReturn(assignments);

        UserDetails userDetails = mock(UserDetails.class);
        when(adminUserService.getAdminUserToken()).thenReturn(DUMMY_ADMIN_USER_TOKEN);
        when(adminUserService.getUserDetails(eq(DUMMY_ADMIN_USER_TOKEN), anyString())).thenAnswer(invocation -> {
            String userId = invocation.getArgument(1);
            String email = switch (userId) {
                case "claimantSolicitorUserId" -> "claimant.rep@example.com";
                case "sharedListUserId" -> "shared1@example.com";
                case "sharedListUserId2" -> "shared2@example.com";
                default -> "";
            };
            when(userDetails.getEmail()).thenReturn(email);
            return userDetails;
        });
        when(emailNotificationService.getCaseClaimantSolicitorEmails(assignments))
                .thenReturn(List.of("claimant.rep@example.com",
                        "shared1@example.com",
                        "shared2@example.com"));

        personalisationCaptor.getAllValues().clear();
        sendNotificationService.sendNotifyEmails(caseDetails);

        verify(emailService, times(3)).sendEmail(any(), any(), anyMap());
    }

    @Test
    void sendNotifyEmails_ClaimantRepresented_NoEmail() {
        caseDetails.getCaseData().setSendNotificationSubject(List.of("OTHER_SUBJECT"));
        caseDetails.getCaseData().setSendNotificationNotify(CLAIMANT_ONLY);
        when(featureToggleService.isEccEnabled()).thenReturn(true);
        caseDetails.getCaseData().setCaseSource("MyHMCTS");
        caseDetails.getCaseData().setClaimantRepresentedQuestion("Yes");
        RepresentedTypeC representedTypeC = new RepresentedTypeC();
        representedTypeC.setNameOfRepresentative("testRep");
        representedTypeC.setRepresentativeEmailAddress(null);
        Organisation org = Organisation.builder().organisationID("myHmctsOrgId").organisationName("testOrg").build();
        representedTypeC.setMyHmctsOrganisation(org);
        caseDetails.getCaseData().setRepresentativeClaimantType(representedTypeC);

        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(0)).sendEmail(anyString(), anyString(), anyMap());
    }

    @Test
    void sendNotifyEmails_ClaimantNotRepresented_NonSystemUser() {
        caseDetails.getCaseData().setSendNotificationSubject(List.of("OTHER_SUBJECT"));
        caseDetails.getCaseData().setSendNotificationNotify(CLAIMANT_ONLY);
        when(featureToggleService.isEccEnabled()).thenReturn(true);
        caseDetails.getCaseData().setCaseSource("ET1Online");
        caseDetails.getCaseData().setClaimantRepresentedQuestion("Yes");
        caseDetails.getCaseData().setEt1OnlineSubmission(null);
        caseDetails.getCaseData().setHubLinksStatuses(null);
        caseDetails.getCaseData().setRepresentativeClaimantType(null);

        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(0)).sendEmail(anyString(), anyString(), anyMap());
    }

    @Test
    void sendNotifyEmails_respondentOnly() {
        caseData.setSendNotificationNotify(RESPONDENT_ONLY);
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(1))
                .sendEmail(eq(RESPONDENT_SEND_NOTIFICATION_TEMPLATE_ID), any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("exuiUrl1234", val.get("environmentUrl"));
    }

    @Test
    void sendNotifyEmails_respondentOnly_withSharedList() {
        caseData.setSendNotificationNotify(RESPONDENT_ONLY);
        caseData.setCcdID("1234");

        CaseUserAssignment assignment1 = CaseUserAssignment.builder()
                .userId("respondentSolicitorUserId")
                .caseRole(SolicitorRole.SOLICITORA.getCaseRoleLabel())
                .build();

        CaseUserAssignment assignment2 = CaseUserAssignment.builder()
                .userId("sharedListUserId")
                .caseRole(SolicitorRole.SOLICITORB.getCaseRoleLabel())
                .build();

        CaseUserAssignment assignment3 = CaseUserAssignment.builder()
                .userId("sharedListUserId2")
                .caseRole(SolicitorRole.SOLICITORC.getCaseRoleLabel())
                .build();

        List<CaseUserAssignment> assignments = List.of(assignment1, assignment2, assignment3);

        when(caseAccessService.getCaseUserAssignmentsById(anyString())).thenReturn(assignments);

        UserDetails userDetails = mock(UserDetails.class);
        when(adminUserService.getAdminUserToken()).thenReturn(DUMMY_ADMIN_USER_TOKEN);
        when(adminUserService.getUserDetails(eq(DUMMY_ADMIN_USER_TOKEN), anyString())).thenAnswer(invocation -> {
            String userId = invocation.getArgument(1);
            String email = switch (userId) {
                case "respondentSolicitorUserId" -> "respondentRep@email.com";
                case "sharedListUserId" -> "shared1@example.com";
                case "sharedListUserId2" -> "shared2@example.com";
                default -> "";
            };
            when(userDetails.getEmail()).thenReturn(email);
            return userDetails;
        });
        when(emailNotificationService.getRespondentSolicitorEmails(assignments))
                .thenReturn(Set.of("respondentRep@email.com",
                        "shared1@example.com",
                        "shared2@example.com"));

        sendNotificationService.sendNotifyEmails(caseDetails);

        // Should send to all emails in the shared list
        verify(emailService, times(3))
                .sendEmail(eq(RESPONDENT_SEND_NOTIFICATION_TEMPLATE_ID), any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("exuiUrl1234", val.get("environmentUrl"));
    }

    @Test
    void sendNotifyEmails_claimantOnly_hearing() {
        caseData.setSendNotificationNotify(CLAIMANT_ONLY);
        caseData.setSendNotificationSubject(List.of("Hearing"));
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(1))
                .sendEmail(eq(CLAIMANT_SEND_NOTIFICATION_TEMPLATE_ID),
                        any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("1234", val.get("caseNumber"));
        assertEquals("title", val.get("sendNotificationTitle"));
        assertEquals("citizenUrl1234", val.get("environmentUrl"));
        assertEquals("1234", val.get("caseId"));
    }

    @Test
    void sendNotifyEmails_bothParties_hearing() {
        caseData.setSendNotificationNotify(BOTH_PARTIES);
        caseData.setSendNotificationSubject(List.of("Hearing"));
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(1))
                .sendEmail(eq(RESPONDENT_SEND_NOTIFICATION_TEMPLATE_ID),
                        any(), personalisationCaptor.capture());
        verify(emailService, times(1))
                .sendEmail(eq(CLAIMANT_SEND_NOTIFICATION_TEMPLATE_ID),
                        any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("1234", val.get("caseNumber"));
        assertEquals("title", val.get("sendNotificationTitle"));
        assertEquals("citizenUrl1234", val.get("environmentUrl"));
        assertEquals("1234", val.get("caseId"));
    }

    @Test
    void sendNotifyEmailsToAdminAndClaimant() {
        sendNotificationService.notify(caseDetails);
        verify(emailService, times(1))
                .sendEmail(eq(BUNDLES_SUBMITTED_NOTIFICATION_FOR_CLAIMANT_TEMPLATE_ID),
                        any(), personalisationCaptor.capture());
        verify(emailService, times(1))
                .sendEmail(eq(BUNDLES_SUBMITTED_NOTIFICATION_FOR_TRIBUNAL_TEMPLATE_ID),
                        any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("1234", val.get("caseNumber"));
        assertEquals("claimant", val.get("claimant"));
        assertEquals("claimant", val.get("respondentNames"));
        assertEquals("2020-01-02", val.get("hearingDate"));

    }

    @Test
    void testCreateNotificationSummarySuccess() throws Exception {
        DocumentInfo expectedDocumentInfo = new DocumentInfo();
        expectedDocumentInfo.setDescription("Notification 1 Summary");
        expectedDocumentInfo.setMarkUp("<a href=\"http://dm-store/documents/123-456-789/binary\">Document</a>");
        expectedDocumentInfo.setUrl("http://dm-store/documents/123-456-789/binary");

        when(tornadoService.generateEventDocument(caseData, "userToken",
                SCOTLAND_CASE_TYPE_ID, NOTIFICATION_SUMMARY_PDF))
                .thenReturn(expectedDocumentInfo);

        DocumentInfo result = sendNotificationService.createNotificationSummary(
                caseData, "userToken", SCOTLAND_CASE_TYPE_ID);

        verify(tornadoService, times(1)).generateEventDocument(
                caseData, "userToken", SCOTLAND_CASE_TYPE_ID,
                NOTIFICATION_SUMMARY_PDF);
        assertSame(expectedDocumentInfo, result);
        assertEquals("<a href=\"http://dm-store/documents/123-456-789/binary\">Notification 1 Summary</a>",
                result.getMarkUp());
    }

    @Test
    void testCreateNotificationSummaryWithMarkUpReplacement() throws Exception {
        DocumentInfo documentInfo = new DocumentInfo();
        documentInfo.setDescription("Notification 1 Summary");
        documentInfo.setMarkUp("<a href=\"http://dm-store/documents/123-456-789/binary\">Document</a>");

        when(tornadoService.generateEventDocument(any(), anyString(), anyString(), anyString()))
                .thenReturn(documentInfo);

        DocumentInfo result = sendNotificationService.createNotificationSummary(
                caseData, "userToken", SCOTLAND_CASE_TYPE_ID);

        assertEquals("<a href=\"http://dm-store/documents/123-456-789/binary\">Notification 1 Summary</a>",
                result.getMarkUp());
    }

    @Test
    void testCreateNotificationSummaryThrowsDocumentManagementException() throws Exception {
        when(tornadoService.generateEventDocument(any(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Tornado down"));

        DocumentManagementException exception = assertThrows(DocumentManagementException.class,
                () -> sendNotificationService.createNotificationSummary(caseData, "userToken", SCOTLAND_CASE_TYPE_ID));

        assertTrue(exception.getMessage().contains("Failed to generate document for case id: 1234"));
        verify(tornadoService, times(1)).generateEventDocument(
                caseData, "userToken", SCOTLAND_CASE_TYPE_ID, NOTIFICATION_SUMMARY_PDF);
    }

    @Test
    void testCreateBfActionWhenEccQuestionIsNoticeOfEmployerContractClaim() {
        caseData.setSendNotificationSubject(List.of(EMPLOYER_CONTRACT_CLAIM));
        caseData.setSendNotificationEccQuestion(NOTICE_OF_EMPLOYER_CONTRACT_CLAIM);
        caseData.setBfActions(null);
        
        sendNotificationService.createBfAction(caseData);
        
        assertNotNull(caseData.getBfActions());
        assertEquals(1, caseData.getBfActions().size());
        
        BFActionTypeItem bfActionItem = caseData.getBfActions().getFirst();
        assertNotNull(bfActionItem.getId());
        
        BFActionType bfAction = bfActionItem.getValue();
        assertNotNull(bfAction);
        assertEquals(NO, bfAction.getLetters());
        assertEquals(LocalDate.now().toString(), bfAction.getDateEntered());
        assertEquals("Other action", bfAction.getCwActions());
        assertEquals("ECC served", bfAction.getAllActions());
        assertEquals(LocalDate.now().plusDays(29).toString(), bfAction.getBfDate());
    }

    @Test
    void testCreateBfActionWhenBothEccConditionsAreMet() {
        caseData.setSendNotificationSubject(List.of(EMPLOYER_CONTRACT_CLAIM, "Other Subject"));
        caseData.setSendNotificationEccQuestion(NOTICE_OF_EMPLOYER_CONTRACT_CLAIM);
        caseData.setBfActions(null);
        
        sendNotificationService.createBfAction(caseData);
        
        assertNotNull(caseData.getBfActions());
        assertEquals(1, caseData.getBfActions().size());
    }

    @Test
    void testCreateBfActionShouldNotCreateWhenNeitherConditionIsMet() {
        caseData.setSendNotificationSubject(List.of("Hearing", "Other Subject"));
        caseData.setSendNotificationEccQuestion("Other Question");
        caseData.setBfActions(null);
        
        sendNotificationService.createBfAction(caseData);
        
        assertNull(caseData.getBfActions());
    }

    @Test
    void testCreateBfActionShouldNotCreateWhenSubjectIsNullButEccQuestionMatches() {
        caseData.setSendNotificationSubject(null);
        caseData.setSendNotificationEccQuestion(NOTICE_OF_EMPLOYER_CONTRACT_CLAIM);
        caseData.setBfActions(null);
        
        sendNotificationService.createBfAction(caseData);
        
        assertNull(caseData.getBfActions());
    }

    @Test
    void testCreateBfActionShouldNotCreateWhenSubjectDoesNotContainEcc() {
        caseData.setSendNotificationSubject(List.of("Hearing", "Judgment"));
        caseData.setBfActions(null);
        
        sendNotificationService.createBfAction(caseData);
        
        assertNull(caseData.getBfActions());
    }

    @Test
    void testCreateBfActionShouldNotCreateWhenEccQuestionNotNoticeOFEcc() {
        caseData.setSendNotificationSubject(List.of(EMPLOYER_CONTRACT_CLAIM));
        caseData.setSendNotificationEccQuestion("Acceptance of Employer Contract Claim");
        caseData.setBfActions(null);

        sendNotificationService.createBfAction(caseData);

        assertNull(caseData.getBfActions());
    }

    @Test
    void testCreateBfActionAppendsToExistingBfActions() {
        BFActionType existingBfAction = new BFActionType();
        existingBfAction.setCwActions("Existing action");
        existingBfAction.setAllActions("Existing all action");
        BFActionTypeItem existingBfActionItem = new BFActionTypeItem();
        existingBfActionItem.setId("existing-id");
        existingBfActionItem.setValue(existingBfAction);
        
        List<BFActionTypeItem> existingActions = new ArrayList<>();
        existingActions.add(existingBfActionItem);

        caseData.setSendNotificationSubject(List.of(EMPLOYER_CONTRACT_CLAIM));
        caseData.setSendNotificationEccQuestion(NOTICE_OF_EMPLOYER_CONTRACT_CLAIM);
        caseData.setBfActions(existingActions);
        
        sendNotificationService.createBfAction(caseData);
        
        assertNotNull(caseData.getBfActions());
        assertEquals(2, caseData.getBfActions().size()); // Should have 2 actions now
        
        BFActionTypeItem firstAction = caseData.getBfActions().getFirst();
        assertEquals("existing-id", firstAction.getId());
        assertEquals("Existing action", firstAction.getValue().getCwActions());
        
        BFActionTypeItem newAction = caseData.getBfActions().get(1);
        assertNotNull(newAction.getId());
        assertEquals("Other action", newAction.getValue().getCwActions());
        assertEquals("ECC served", newAction.getValue().getAllActions());
    }

    @Test
    void testCreateBfActionBfActionTypeItemHasValidUuid() {
        caseData.setSendNotificationSubject(List.of(EMPLOYER_CONTRACT_CLAIM));
        caseData.setSendNotificationEccQuestion(NOTICE_OF_EMPLOYER_CONTRACT_CLAIM);
        caseData.setBfActions(null);
        
        sendNotificationService.createBfAction(caseData);
        
        BFActionTypeItem bfActionItem = caseData.getBfActions().getFirst();
        assertNotNull(bfActionItem.getId());
        assertTrue(bfActionItem.getId().matches(
                "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"));
    }

    @Test
    void validateInput_shouldReturnNoError_whenPartyToRespondIsNull() {
        caseData.setSendNotificationSelectParties(null);
        caseData.setSendNotificationNotify(CLAIMANT_ONLY);
        List<String> errors = sendNotificationService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateInput_shouldReturnNoError_whenPartyToNotifyIsBothParties() {
        caseData.setSendNotificationSelectParties(CLAIMANT_ONLY);
        caseData.setSendNotificationNotify(BOTH_PARTIES);
        List<String> errors = sendNotificationService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateInput_shouldReturnNoError_whenPartyToNotifyMatchesPartyToRespond() {
        caseData.setSendNotificationSelectParties(CLAIMANT_ONLY);
        caseData.setSendNotificationNotify(CLAIMANT_ONLY);
        List<String> errors = sendNotificationService.validateInput(caseData);
        assertThat(errors).isEmpty();
    }

    @Test
    void validateInput_shouldReturnError_whenPartyToRespondIsBothPartyButPartyToNotifyNot() {
        caseData.setSendNotificationSelectParties(BOTH_PARTIES);
        caseData.setSendNotificationNotify(CLAIMANT_ONLY);
        List<String> errors = sendNotificationService.validateInput(caseData);
        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst()).isEqualTo(ERROR_MSG_PARTY_TO_NOTIFY_MUST_INCLUDE_SELECTED);
    }

    @Test
    void validateInput_shouldReturnError_whenPartyToNotifyDoesNotMatchPartyToRespond() {
        caseData.setSendNotificationSelectParties(CLAIMANT_ONLY);
        caseData.setSendNotificationNotify(RESPONDENT_ONLY);
        List<String> errors = sendNotificationService.validateInput(caseData);
        assertThat(errors).hasSize(1);
        assertThat(errors.getFirst()).isEqualTo(ERROR_MSG_PARTY_TO_NOTIFY_MUST_INCLUDE_SELECTED);
    }
}
