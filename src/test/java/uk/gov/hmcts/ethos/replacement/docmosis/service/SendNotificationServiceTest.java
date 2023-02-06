package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
public class SendNotificationServiceTest {

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
        caseData.setSendNotificationUploadDocument(new ArrayList<DocumentTypeItem>());
        caseData.setSendNotificationSubject(new ArrayList<String>() {{
            add("Hearing");
        }});
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
        assertEquals(null, sendNotificationType.getSendNotificationSelectHearing());
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
        sendNotificationService.clearSendNotificaitonFields(caseData);

        assertEquals(null, caseData.getSendNotificationTitle());
        assertEquals(null, caseData.getSendNotificationLetter());
        assertEquals(null, caseData.getSendNotificationUploadDocument());
        assertEquals(null, caseData.getSendNotificationSubject());
        assertEquals(null, caseData.getSendNotificationAdditionalInfo());
        assertEquals(null, caseData.getSendNotificationNotify());
        assertEquals(null, caseData.getSendNotificationAnotherLetter());
        assertEquals(null, caseData.getSendNotificationSelectHearing());
        assertEquals(null, caseData.getSendNotificationCaseManagement());
        assertEquals(null, caseData.getSendNotificationResponseTribunal());
        assertEquals(null, caseData.getSendNotificationWhoCaseOrder());
        assertEquals(null, caseData.getSendNotificationSelectParties());
        assertEquals(null, caseData.getSendNotificationFullName());
        assertEquals(null, caseData.getSendNotificationFullName2());
        assertEquals(null, caseData.getSendNotificationDetails());
        assertEquals(null, caseData.getSendNotificationRequestMadeBy());
    }

    @Test
    void testPopulateHearingSelection() {
        sendNotificationService.populateHearingSelection(caseData);
        verify(hearingSelectionService, times(1)).getHearingSelection(any(), eq("%s: %s - %s - %s"));
    }

}
