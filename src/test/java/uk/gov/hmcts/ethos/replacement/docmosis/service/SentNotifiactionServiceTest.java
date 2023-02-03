package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.SendNotificationController;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
public class SentNotifiactionServiceTest {

    private CaseData caseData;

    private SendNotificationService sendNotificationService;

    @BeforeEach
    public void setUp(){
        caseData = new CaseData();
    }

    @Test
    public void testCreateSendNotification() {

        caseData.setSendNotificationTitle("title");
        caseData.setSendNotificationLetter("no");
        caseData.setSendNotificationUploadDocument(new ArrayList<DocumentTypeItem>());
        caseData.setSendNotificationSubject(new ArrayList<String>(){{add("Hearing");}});
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
        assertEquals("Both Parties", sendNotificationType.getSendNotificationSelectParties());
        assertEquals("John Doe", sendNotificationType.getSendNotificationFullName());
        assertEquals("John Doe", sendNotificationType.getSendNotificationFullName2());
        assertEquals("details", sendNotificationType.getSendNotificationDetails());
        assertEquals("Judge", sendNotificationType.getSendNotificationRequestMadeBy());


    }

}
