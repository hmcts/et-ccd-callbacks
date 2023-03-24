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
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class SendNotificationServiceTest {

    @Mock
    private HearingSelectionService hearingSelectionService;
    private CaseData caseData;
    private CaseDetails caseDetails;
    private SendNotificationService sendNotificationService;
    @MockBean
    private EmailService emailService;
    @Captor
    ArgumentCaptor<Map<String, String>> personalisationCaptor;

    @BeforeEach
    public void setUp() {
        sendNotificationService = new SendNotificationService(hearingSelectionService, emailService);
        ReflectionTestUtils.setField(sendNotificationService, "templateId", "templateId");
        ReflectionTestUtils.setField(sendNotificationService, "citizenUrl", "citizenUrl");
        ReflectionTestUtils.setField(sendNotificationService, "exuiUrl", "exuiUrl");

        caseDetails = CaseDataBuilder.builder().withEthosCaseReference("1234")
            .withClaimantType("claimant@email.com")
            .withRespondent("Name", YES, "2020-01-02", "respondent@email.com", false)
            .withRespondentRepresentative("Name", "Sally", "respondentRep@email.com")
            .buildAsCaseDetails(SCOTLAND_CASE_TYPE_ID);

        caseDetails.setCaseId("1234");

        caseData = caseDetails.getCaseData();

        caseData.setSendNotificationTitle("title");
        caseData.setSendNotificationLetter("no");
        caseData.setSendNotificationUploadDocument(new ArrayList<>());
        caseData.setSendNotificationSubject(List.of("Hearing"));
        caseData.setSendNotificationAdditionalInfo("info");
        caseData.setSendNotificationNotify("Both parties");
        caseData.setSendNotificationSelectHearing(null);
        caseData.setSendNotificationCaseManagement("");
        caseData.setSendNotificationResponseTribunal("no");
        caseData.setSendNotificationWhoCaseOrder("Judge");
        caseData.setSendNotificationSelectParties("Both parties");
        caseData.setSendNotificationFullName("John Doe");
        caseData.setSendNotificationFullName2("John Doe");
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
        assertEquals("Hearing", sendNotificationType.getSendNotificationSubject().get(0));
        assertEquals("info", sendNotificationType.getSendNotificationAdditionalInfo());
        assertEquals("Both parties", sendNotificationType.getSendNotificationNotify());
        assertNull(sendNotificationType.getSendNotificationSelectHearing());
        assertEquals("", sendNotificationType.getSendNotificationCaseManagement());
        assertEquals("no", sendNotificationType.getSendNotificationResponseTribunal());
        assertEquals("Judge", sendNotificationType.getSendNotificationWhoCaseOrder());
        assertEquals("Both parties", sendNotificationType.getSendNotificationSelectParties());
        assertEquals("John Doe", sendNotificationType.getSendNotificationFullName());
        assertEquals("John Doe", sendNotificationType.getSendNotificationFullName2());
        assertEquals("details", sendNotificationType.getSendNotificationDetails());
        assertEquals("Judge", sendNotificationType.getSendNotificationRequestMadeBy());

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
        assertNull(caseData.getSendNotificationDetails());
        assertNull(caseData.getSendNotificationRequestMadeBy());
    }

    @Test
    void testPopulateHearingSelection() {
        sendNotificationService.populateHearingSelection(caseData);
        verify(hearingSelectionService, times(1)).getHearingSelection(any(), eq("%s: %s - %s - %s"));
    }

    @Test
    void sendNotifyEmails_bothParties() {
        caseData.setSendNotificationNotify(BOTH_PARTIES);
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(2)).sendEmail(eq("templateId"), any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("exuiUrl1234", val.get("environmentUrl"));
    }

    @Test
    void sendNotifyEmails_claimantOnly() {
        caseData.setSendNotificationNotify(CLAIMANT_ONLY);
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(1)).sendEmail(eq("templateId"), any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("citizenUrl1234", val.get("environmentUrl"));
    }

    @Test
    void sendNotifyEmails_respondentOnly() {
        caseData.setSendNotificationNotify(RESPONDENT_ONLY);
        sendNotificationService.sendNotifyEmails(caseDetails);
        verify(emailService, times(1)).sendEmail(eq("templateId"), any(), personalisationCaptor.capture());
        Map<String, String> val = personalisationCaptor.getValue();
        assertEquals("exuiUrl1234", val.get("environmentUrl"));
    }

}
