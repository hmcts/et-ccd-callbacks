package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class RespondNotificationServiceTest {

    private CaseData caseData;

    @Mock
    private EmailService emailService;
    @Mock
    private HearingSelectionService hearingSelectionService;

    private RespondNotificationService respondNotificationService;

    @BeforeEach
    void setUp() {
        SendNotificationService sendNotificationService =
                new SendNotificationService(hearingSelectionService, emailService);
        respondNotificationService = new RespondNotificationService(sendNotificationService);
        caseData = new CaseData();
    }

    @Test
    void testGetRespondNotificationMarkdownRequiredFields() {
        SendNotificationType notificationType = new SendNotificationType();
        notificationType.setDate("01-JAN-1970");
        notificationType.setSendNotificationTitle("title");
        notificationType.setSendNotificationLetter("no");
        notificationType.setSendNotificationSubject(List.of("Judgment"));
        notificationType.setSendNotificationNotify("Both parties");
        notificationType.setSendNotificationCaseManagement("");
        notificationType.setSendNotificationResponseTribunal("no");
        notificationType.setSendNotificationSelectParties("Both parties");

        SendNotificationTypeItem notificationTypeItem = new SendNotificationTypeItem();
        String uuid = UUID.randomUUID().toString();
        notificationTypeItem.setId(uuid);
        notificationTypeItem.setValue(notificationType);

        List<SendNotificationTypeItem> notificationTypeItems = new ArrayList<>();
        notificationTypeItems.add(notificationTypeItem);
        caseData.setSelectNotificationDropdown(DynamicFixedListType.of(DynamicValueType.create(uuid, "")));
        caseData.setSendNotificationCollection(notificationTypeItems);

        String result = respondNotificationService.getNotificationMarkDown(caseData);

        String expected = "|  | |\r\n"
                + "| --- | --- |\r\n"
                + "| Subject | title |\r\n"
                + "| Notification | [Judgment] |\r\n"
                + "| Hearing |  |\r\n"
                + "| Date Sent | 01-JAN-1970 |\r\n"
                + "| Sent By | TRIBUNAL  |\r\n"
                + "| Case management order request |  |\r\n"
                + "| Response due | no |\r\n"
                + "| Party or parties to respond | Both parties |\r\n"
                + "| Additional Information |  |\r\n"
                + "| Description | title |\r\n"
                + " | Document | |\r\n"
                + "| Case management order made by |  |\r\n"
                + "| Name |  |\r\n"
                + "| Sent to | Both parties |\r\n";
        assertEquals(expected, result);
    }

    @Test
    void testGetRespondNotificationMarkdownAllFields() {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl("http://dm-store:8080/documents/6fbf9470-f735-484a-9790-5b246b646fe2/binary");
        uploadedDocumentType.setDocumentFilename("TEST.PDF");
        uploadedDocumentType.setDocumentUrl("http://dm-store:8080/documents/6fbf9470-f735-484a-9790-5b246b646fe2");

        DocumentType documentType = new DocumentType();
        documentType.setUploadedDocument(uploadedDocumentType);
        documentType.setShortDescription("TEST");

        List<DocumentTypeItem> documents = new ArrayList<>();
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setValue(documentType);
        documents.add(documentTypeItem);

        SendNotificationType notificationType = new SendNotificationType();
        notificationType.setDate("01-JAN-1970");
        notificationType.setSendNotificationTitle("title");
        notificationType.setSendNotificationLetter("no");
        notificationType.setSendNotificationUploadDocument(documents);
        notificationType.setSendNotificationSubject(List.of("Hearing",
                "Case management orders / requests", "Judgment"));
        notificationType.setSendNotificationAdditionalInfo("info");
        notificationType.setSendNotificationNotify("Both parties");
        notificationType.setSendNotificationCaseManagement("Request");
        notificationType.setSendNotificationResponseTribunal("no");
        notificationType.setSendNotificationWhoCaseOrder("Judge");
        notificationType.setSendNotificationSelectParties("Both parties");
        notificationType.setSendNotificationFullName("John Doe");
        notificationType.setSendNotificationFullName2("John Doe");
        notificationType.setSendNotificationDetails("details");
        notificationType.setSendNotificationRequestMadeBy("Judge");

        List<DynamicValueType> hearings = new ArrayList<>();
        hearings.add(DynamicValueType.create("42", "1: Hearing - Barnstaple - 16 May 2022 01:00"));
        DynamicFixedListType dynamicFixedListType = mock(DynamicFixedListType.class);
        when(dynamicFixedListType.getSelectedLabel()).thenReturn("1: Hearing - Barnstaple - 16 May 2022 01:00");
        notificationType.setSendNotificationSelectHearing(dynamicFixedListType);

        String uuid = UUID.randomUUID().toString();
        SendNotificationTypeItem notificationTypeItem = new SendNotificationTypeItem();
        notificationTypeItem.setId(uuid);
        notificationTypeItem.setValue(notificationType);

        List<SendNotificationTypeItem> notificationTypeItems = new ArrayList<>();
        notificationTypeItems.add(notificationTypeItem);

        caseData.setSelectNotificationDropdown(DynamicFixedListType.of(DynamicValueType.create(uuid, "")));
        caseData.setSendNotificationCollection(notificationTypeItems);

        String result = respondNotificationService.getNotificationMarkDown(caseData);

        String expected = "|  | |\r\n"
                + "| --- | --- |\r\n"
                + "| Subject | title |\r\n"
                + "| Notification | [Hearing, Case management orders / requests, Judgment] |\r\n"
                + "| Hearing | 1: Hearing - Barnstaple - 16 May 2022 01:00 |\r\n"
                + "| Date Sent | 01-JAN-1970 |\r\n"
                + "| Sent By | TRIBUNAL  |\r\n"
                + "| Case management order request | Request |\r\n"
                + "| Response due | no |\r\n"
                + "| Party or parties to respond | Both parties |\r\n"
                + "| Additional Information | info |\r\n"
                + "| Description | title |\r\n"
                + " | Document | <a href=\"/documents/6fbf9470-f735-484a-9790-5b246b646fe2/binary\""
                + " target=\"_blank\">TEST.PDF</a>\r\n"
                + "| Case management order made by | Judge |\r\n"
                + "| Name | John Doe |\r\n"
                + "| Sent to | Both parties |\r\n";
        assertEquals(expected, result);
    }
}
