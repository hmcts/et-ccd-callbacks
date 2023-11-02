package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.EmailUtils;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_VIEWED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TRIBUNAL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class SendNotificationServiceTest {

    @Mock
    private HearingSelectionService hearingSelectionService;
    private CaseData caseData;
    private CaseDetails caseDetails;
    private SendNotificationService sendNotificationService;

    private EmailService emailService;

    @Captor
    ArgumentCaptor<Map<String, String>> personalisationCaptor;

    private static final String SEND_NOTIFICATION_TEMPLATE_ID = "sendNotificationTemplateId";
    private static final String CLAIMANT_SEND_NOTIFICATION_HEARING_OTHER_TEMPLATE_ID =
            "claimantSendNotificationHearingOtherTemplateId";
    private static final String RESPONDENT_SEND_NOTIFICATION_HEARING_OTHER_TEMPLATE_ID =
            "claimantSendNotificationHearingOtherTemplateId";

    private static final String BUNDLES_CLAIMANT_SUBMITTED_RESPONDENT_NOTIFICATION_TEMPLATE_ID =
            "bundlesClaimantSubmittedRespondentNotificationTemplateId";

    @BeforeEach
    public void setUp() {
        emailService = spy(new EmailUtils());
        sendNotificationService = new SendNotificationService(hearingSelectionService, emailService);
        ReflectionTestUtils.setField(sendNotificationService,
                SEND_NOTIFICATION_TEMPLATE_ID,
                "sendNotificationTemplateId");
        ReflectionTestUtils.setField(sendNotificationService,
                RESPONDENT_SEND_NOTIFICATION_HEARING_OTHER_TEMPLATE_ID,
                "respondentSendNotificationHearingOtherTemplateId");
        ReflectionTestUtils.setField(sendNotificationService,
                CLAIMANT_SEND_NOTIFICATION_HEARING_OTHER_TEMPLATE_ID,
                "claimantSendNotificationHearingOtherTemplateId");
        ReflectionTestUtils.setField(sendNotificationService,
                BUNDLES_CLAIMANT_SUBMITTED_RESPONDENT_NOTIFICATION_TEMPLATE_ID,
                "bundlesClaimantSubmittedRespondentNotificationTemplateId");

        caseDetails = CaseDataBuilder.builder().withEthosCaseReference("1234")
                .withClaimantType("claimant@email.com")
                .withRespondent("Name", YES, "2020-01-02", "respondent@email.com", false)
                .withRespondentRepresentative("Name", "Sally", "respondentRep@email.com")
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
    }

    @Test
    void testCreateSendNotification() {

        sendNotificationService.createSendNotification(caseData);
        SendNotificationType sendNotificationType = caseData.getSendNotificationCollection().get(0).getValue();

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
    }

    @Test
    void testCreateSendNotificationWhenRespondentShouldBeNotified() {
        caseData.setSendNotificationSelectParties(RESPONDENT_ONLY);
        sendNotificationService.createSendNotification(caseData);
        SendNotificationType sendNotificationType = caseData.getSendNotificationCollection().get(0).getValue();
        assertEquals(NOT_VIEWED_YET, sendNotificationType.getNotificationState());
    }

    @Test
    void testClearSendNotificaitonFields() {
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
        verify(emailService, times(2))
                .sendEmail(eq(SEND_NOTIFICATION_TEMPLATE_ID), any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("exuiUrl1234", val.get("environmentUrl"));
    }

    @Test
    void sendNotifyEmails_noClaimantEmail_onlySendsRespondentEmail() {
        caseData.setSendNotificationNotify(BOTH_PARTIES);
        caseData.getClaimantType().setClaimantEmailAddress(null);
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(2))
                .sendEmail(eq(SEND_NOTIFICATION_TEMPLATE_ID), any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("exuiUrl1234", val.get("environmentUrl"));
    }

    @Test
    void sendNotifyEmails_noRespondentEmail_onlySendsClaimantEmail() {
        caseData.setSendNotificationNotify(BOTH_PARTIES);
        caseData.getRespondentCollection().forEach(o -> o.getValue().setRespondentEmail(null));
        caseData.getRepCollection().forEach(o -> o.getValue().setRepresentativeEmailAddress(null));
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(1)).sendEmail(eq(SEND_NOTIFICATION_TEMPLATE_ID), any(), any());
    }

    @Test
    void sendNotifyEmails_claimantOnly() {
        caseData.setSendNotificationNotify(CLAIMANT_ONLY);
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(1))
                .sendEmail(eq(SEND_NOTIFICATION_TEMPLATE_ID), any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("citizenUrl1234", val.get("environmentUrl"));
    }

    @Test
    void sendNotifyEmails_respondentOnly() {
        caseData.setSendNotificationNotify(RESPONDENT_ONLY);
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(1))
                .sendEmail(eq(SEND_NOTIFICATION_TEMPLATE_ID), any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("exuiUrl1234", val.get("environmentUrl"));
    }

    @Test
    void sendNotifyEmails_claimantOnly_hearing() {
        caseData.setSendNotificationNotify(CLAIMANT_ONLY);
        caseData.setSendNotificationSubject(List.of("Hearing"));
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(1))
                .sendEmail(eq(CLAIMANT_SEND_NOTIFICATION_HEARING_OTHER_TEMPLATE_ID),
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
                .sendEmail(eq(RESPONDENT_SEND_NOTIFICATION_HEARING_OTHER_TEMPLATE_ID),
                        any(), personalisationCaptor.capture());
        verify(emailService, times(1))
                .sendEmail(eq(CLAIMANT_SEND_NOTIFICATION_HEARING_OTHER_TEMPLATE_ID),
                        any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("1234", val.get("caseNumber"));
        assertEquals("title", val.get("sendNotificationTitle"));
        assertEquals("citizenUrl1234", val.get("environmentUrl"));
        assertEquals("1234", val.get("caseId"));
    }

    @Test
    void sendNotifyEmails_bothParties_hearing_multiple_notification_subject_selected() {
        caseData.setSendNotificationNotify(BOTH_PARTIES);
        caseData.setSendNotificationSubject(List.of("Hearing", "Judgment"));
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(1))
                .sendEmail(eq(RESPONDENT_SEND_NOTIFICATION_HEARING_OTHER_TEMPLATE_ID),
                        any(), personalisationCaptor.capture());
        verify(emailService, times(1))
                .sendEmail(eq(CLAIMANT_SEND_NOTIFICATION_HEARING_OTHER_TEMPLATE_ID),
                        any(), personalisationCaptor.capture());
        verify(emailService, times(2))
                .sendEmail(eq(SEND_NOTIFICATION_TEMPLATE_ID),
                        any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("1234", val.get("caseNumber"));
        assertEquals("title", val.get("sendNotificationTitle"));
        assertEquals("exuiUrl1234", val.get("environmentUrl"));
        assertEquals("1234", val.get("caseId"));
    }

    @Test
    void sendNotifyEmails_claimantOnly_hearing__multiple_notification_subject_selected() {
        caseData.setSendNotificationNotify(CLAIMANT_ONLY);
        caseData.setSendNotificationSubject(List.of("Hearing", "Judgment"));
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(1))
                .sendEmail(eq(CLAIMANT_SEND_NOTIFICATION_HEARING_OTHER_TEMPLATE_ID),
                        any(), personalisationCaptor.capture());
        verify(emailService, times(1))
                .sendEmail(eq(SEND_NOTIFICATION_TEMPLATE_ID), any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("1234", val.get("caseNumber"));
        assertEquals("title", val.get("sendNotificationTitle"));
        assertEquals("citizenUrl1234", val.get("environmentUrl"));
        assertEquals("1234", val.get("caseId"));
    }

    @Test
    void sendNotifyEmailsToAdmin() {

        sendNotificationService.notifyTribunal(caseDetails);
        verify(emailService, times(1))
                .sendEmail(eq(BUNDLES_CLAIMANT_SUBMITTED_RESPONDENT_NOTIFICATION_TEMPLATE_ID),
                        any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("1234", val.get("caseNumber"));
        assertEquals("claimant", val.get("claimant"));
        assertEquals("claimant", val.get("respondentNames"));
        assertEquals("2020-01-02", val.get("hearingDate"));

    }

}
