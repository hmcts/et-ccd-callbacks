package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class SendNotificationServiceTest {

    @Mock
    private HearingSelectionService hearingSelectionService;
    private CaseData caseData;
    private SendNotificationService sendNotificationService;

    @BeforeEach
    public void setUp() {
        caseData = new CaseData();
        sendNotificationService = new SendNotificationService(hearingSelectionService);

        caseData.setSendNotificationTitle("title");
        caseData.setSendNotificationLetter("no");
        caseData.setSendNotificationUploadDocument(new ArrayList<>());
        caseData.setSendNotificationSubject(List.of("Hearing"));
        caseData.setSendNotificationAdditionalInfo("info");
        caseData.setSendNotificationNotify("Both parties");
        caseData.setSendNotificationAnotherLetter("no");
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
        assertEquals("no", sendNotificationType.getSendNotificationAnotherLetter());
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
        assertNull(caseData.getSendNotificationAnotherLetter());
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

}
