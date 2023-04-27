package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.config.NotificationProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASE_MANAGEMENT_ORDER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class RespondNotificationServiceTest {
    private static final String CLAIMANT_EMAIL = "claimant@test.com";
    private static final String RESPONDENT_EMAIL = "repondent@test.com";

    private CaseDetails caseDetails;
    private CaseData caseData;

    @Mock
    private EmailService emailService;
    @Mock
    private HearingSelectionService hearingSelectionService;
    @SpyBean
    private NotificationProperties notificationProperties;

    private RespondNotificationService respondNotificationService;

    @BeforeEach
    void setUp() {
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyMap());

        SendNotificationService sendNotificationService =
            new SendNotificationService(hearingSelectionService, emailService, notificationProperties);

        respondNotificationService = new RespondNotificationService(emailService, sendNotificationService,
                notificationProperties);
        ReflectionTestUtils.setField(notificationProperties, "exuiUrl", "exuiUrl");
        ReflectionTestUtils.setField(notificationProperties, "citizenUrl", "citizenUrl");
        ReflectionTestUtils.setField(notificationProperties, "responseTemplateId", "responseTemplateId");
        ReflectionTestUtils.setField(notificationProperties, "noResponseTemplateId", "noResponseTemplateId");

        caseDetails = new CaseDetails();
        caseData = new CaseData();
        caseDetails.setCaseId(String.valueOf(UUID.randomUUID()));
    }

    private DocumentType createDocument(String name, String description) {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl(
            "http://dm-store:8080/documents/6fbf9470-f735-484a-9790-5b246b646fe2/binary");
        uploadedDocumentType.setDocumentFilename(name);
        uploadedDocumentType.setDocumentUrl("http://dm-store:8080/documents/6fbf9470-f735-484a-9790-5b246b646fe2");

        DocumentType documentType = new DocumentType();
        documentType.setUploadedDocument(uploadedDocumentType);
        documentType.setShortDescription(description);

        return documentType;
    }

    @Test
    void testCreateAndClearRespondNotification() throws IllegalAccessException {

        caseData.setRespondNotificationTitle("title");
        caseData.setRespondNotificationAdditionalInfo("info");
        caseData.setRespondNotificationPartyToNotify(CLAIMANT_ONLY);
        caseData.setRespondNotificationCmoOrRequest(CASE_MANAGEMENT_ORDER);
        caseData.setRespondNotificationResponseRequired(NO);
        caseData.setRespondNotificationWhoRespond(CLAIMANT_ONLY);

        caseData.setRespondNotificationFullName("John Doe");
        caseData.setClaimantType(new ClaimantType());
        caseData.setClaimant("Claimant");
        caseData.setEthosCaseReference("1234");

        String uuid = String.valueOf(UUID.randomUUID());
        DynamicValueType dynamicValueType = DynamicValueType.create(uuid, "sendNotification");
        caseData.setSelectNotificationDropdown(DynamicFixedListType.of(dynamicValueType));

        SendNotificationType notificationType = new SendNotificationType();
        notificationType.setSendNotificationTitle("test");
        SendNotificationTypeItem sendNotificationTypeItem = new SendNotificationTypeItem();
        sendNotificationTypeItem.setId(uuid);
        sendNotificationTypeItem.setValue(notificationType);
        caseData.setSendNotificationCollection(List.of(sendNotificationTypeItem));
        caseDetails.setCaseData(caseData);

        respondNotificationService.handleAboutToSubmit(caseDetails);
        var sendNotificationType = caseDetails.getCaseData().getSendNotificationCollection().get(0).getValue();
        var respondNotificationType = sendNotificationType.getRespondNotificationTypeCollection().get(0).getValue();

        assertEquals("title", respondNotificationType.getRespondNotificationTitle());
        assertEquals("info", respondNotificationType.getRespondNotificationAdditionalInfo());
        assertEquals(CLAIMANT_ONLY, respondNotificationType.getRespondNotificationPartyToNotify());
        assertEquals(CASE_MANAGEMENT_ORDER, respondNotificationType.getRespondNotificationCmoOrRequest());
        assertEquals(NO, respondNotificationType.getRespondNotificationResponseRequired());
        assertEquals(CLAIMANT_ONLY, respondNotificationType.getRespondNotificationWhoRespond());
        assertEquals("John Doe", respondNotificationType.getRespondNotificationFullName());

        assertNull(caseData.getRespondNotificationTitle());
        assertNull(caseData.getRespondNotificationAdditionalInfo());
        assertNull(caseData.getRespondNotificationUploadDocument());
        assertNull(caseData.getRespondNotificationCmoOrRequest());
        assertNull(caseData.getRespondNotificationResponseRequired());
        assertNull(caseData.getRespondNotificationWhoRespond());
        assertNull(caseData.getRespondNotificationCaseManagementMadeBy());
        assertNull(caseData.getRespondNotificationFullName());
        assertNull(caseData.getRespondNotificationPartyToNotify());

    }

    @Test
    void testGetRespondNotificationMarkdownRequiredFields() {
        SendNotificationType notificationType = new SendNotificationType();
        notificationType.setDate("01-JAN-1970");
        notificationType.setSendNotificationTitle("title");
        notificationType.setSendNotificationLetter("no");
        notificationType.setSendNotificationSubject(List.of("Judgment"));
        notificationType.setSendNotificationNotify(BOTH_PARTIES);
        notificationType.setSendNotificationCaseManagement("");
        notificationType.setSendNotificationResponseTribunal("no");
        notificationType.setSendNotificationSelectParties(BOTH_PARTIES);

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
            + "| Notification | Judgment |\r\n"
            + "| Hearing |  |\r\n"
            + "| Date Sent | 01-JAN-1970 |\r\n"
            + "| Sent By | Tribunal  |\r\n"
            + "| Case management order or request |  |\r\n"
            + "| Response due | no |\r\n"
            + "| Party or parties to respond | Both parties |\r\n"
            + "| Additional Information |  |\r\n"
            + " | Document | | \r\n"
            + "| Description | |\r\n"
            + "| Case management order made by |  |\r\n"
            + "| Name |  |\r\n"
            + "| Sent to | Both parties |\r\n"
            + "\r\n";

        assertEquals(expected, result);
    }

    @Test
    void testGetRespondNotificationMarkdownRequiredFieldsWithResponse() {
        List<DocumentTypeItem> documentTypeItems = new ArrayList<>();
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setValue(createDocument("TEST.PDF", "TEST"));
        documentTypeItems.add(documentTypeItem);

        RespondNotificationType respondNotificationType = new RespondNotificationType();
        respondNotificationType.setRespondNotificationDate("02-JAN-1970");
        respondNotificationType.setRespondNotificationTitle("TEST");
        respondNotificationType.setRespondNotificationAdditionalInfo("INFO");
        respondNotificationType.setRespondNotificationUploadDocument(documentTypeItems);
        respondNotificationType.setRespondNotificationCmoOrRequest(CASE_MANAGEMENT_ORDER);
        respondNotificationType.setRespondNotificationResponseRequired(YES);
        respondNotificationType.setRespondNotificationWhoRespond(BOTH_PARTIES);
        respondNotificationType.setRespondNotificationCaseManagementMadeBy("Judge");
        respondNotificationType.setRespondNotificationFullName("John Doe");
        respondNotificationType.setRespondNotificationPartyToNotify(BOTH_PARTIES);

        var respondNotificationTypeItem = new GenericTypeItem<RespondNotificationType>();
        respondNotificationTypeItem.setValue(respondNotificationType);
        var respondNotificationTypeItemList = new ArrayList<GenericTypeItem<RespondNotificationType>>();
        respondNotificationTypeItemList.add(respondNotificationTypeItem);

        SendNotificationType notificationType = new SendNotificationType();
        notificationType.setRespondNotificationTypeCollection(respondNotificationTypeItemList);
        notificationType.setDate("01-JAN-1970");
        notificationType.setSendNotificationTitle("title");
        notificationType.setSendNotificationLetter("no");
        notificationType.setSendNotificationSubject(List.of("Judgment"));
        notificationType.setSendNotificationNotify(BOTH_PARTIES);
        notificationType.setSendNotificationCaseManagement("");
        notificationType.setSendNotificationResponseTribunal("no");
        notificationType.setSendNotificationSelectParties(BOTH_PARTIES);

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
            + "| Notification | Judgment |\r\n"
            + "| Hearing |  |\r\n"
            + "| Date Sent | 01-JAN-1970 |\r\n"
            + "| Sent By | Tribunal  |\r\n"
            + "| Case management order or request |  |\r\n"
            + "| Response due | no |\r\n"
            + "| Party or parties to respond | Both parties |\r\n"
            + "| Additional Information |  |\r\n"
            + " | Document | | \r\n"
            + "| Description | |\r\n"
            + "| Case management order made by |  |\r\n"
            + "| Name |  |\r\n"
            + "| Sent to | Both parties |\r\n"
            + "\r\n"
            + "|  | |\r\n"
            + "| --- | --- |\r\n"
            + "| Response 1 | |\r\n"
            + "| Response from | Tribunal |\r\n"
            + "| Response date | 02-JAN-1970 |\r\n"
            + " | Supporting material | <a href=\"/documents/6fbf9470-f735-484a-9790-5b246b646fe2/binary\" "
            +  "target=\"_blank\">TEST.PDF</a>\r\n"
            + "| What's your response to the tribunal? | TEST - INFO\r\n"
            + "| Do you want to copy correspondence to the other party to satisfy the Rules of Procedure? | Yes |\r\n";
        assertEquals(expected, result);
    }

    @Test
    void testGetRespondNotificationMarkdownAllFields() {
        List<DocumentTypeItem> documentTypeItems = new ArrayList<>();
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setValue(createDocument("TEST.PDF", "TEST"));
        documentTypeItems.add(documentTypeItem);
        documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setValue(createDocument("TEST2.DOC", "TEST DOC"));
        documentTypeItems.add(documentTypeItem);

        SendNotificationType notificationType = new SendNotificationType();
        notificationType.setDate("01-JAN-1970");
        notificationType.setSendNotificationTitle("title");
        notificationType.setSendNotificationLetter("no");
        notificationType.setSendNotificationUploadDocument(documentTypeItems);
        notificationType.setSendNotificationSubject(List.of("Hearing",
            "Case management orders / requests", "Judgment"));
        notificationType.setSendNotificationAdditionalInfo("info");
        notificationType.setSendNotificationNotify(BOTH_PARTIES);
        notificationType.setSendNotificationCaseManagement("Request");
        notificationType.setSendNotificationResponseTribunal("no");
        notificationType.setSendNotificationWhoCaseOrder("Judge");
        notificationType.setSendNotificationSelectParties(BOTH_PARTIES);
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
            + "| Notification | Hearing, Case management orders / requests, Judgment |\r\n"
            + "| Hearing | 1: Hearing - Barnstaple - 16 May 2022 01:00 |\r\n"
            + "| Date Sent | 01-JAN-1970 |\r\n"
            + "| Sent By | Tribunal  |\r\n"
            + "| Case management order or request | Request |\r\n"
            + "| Response due | no |\r\n"
            + "| Party or parties to respond | Both parties |\r\n"
            + "| Additional Information | info |\r\n"
            + " | Document |<a href=\"/documents/6fbf9470-f735-484a-9790-5b246b646fe2/binary\""
            + " target=\"_blank\">TEST.PDF</a>|\r\n"
            + "| Description |TEST|\r\n"
            + "| Document |<a href=\"/documents/6fbf9470-f735-484a-9790-5b246b646fe2/binary\""
            + " target=\"_blank\">TEST2.DOC</a>|\r\n"
            + "| Description |TEST DOC|\r\n"
            + "| Case management order made by | Judge |\r\n"
            + "| Name | John Doe |\r\n"
            + "| Sent to | Both parties |\r\n"
            + "\r\n";
        assertEquals(expected, result);
    }

    private void setUpNotifyEmail() {
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentEmail(RESPONDENT_EMAIL);

        caseDetails.setCaseId("1234");
        caseData =
            CaseDataBuilder.builder()
                .withEthosCaseReference("1234")
                .withClaimant("Claimant")
                .withClaimantType(CLAIMANT_EMAIL)
                .withRespondent(respondentSumType).build();
        caseData.setSendNotificationTitle("TEST");
        ReflectionTestUtils.setField(notificationProperties,
            "noResponseTemplateId", "noResponseTemplateId");
        ReflectionTestUtils.setField(notificationProperties,
            "responseTemplateId", "responseTemplateId");

    }

    @Test
    void sendNotifyEmailsNoResponseBothParties() {
        setUpNotifyEmail();

        caseData.setRespondNotificationPartyToNotify(BOTH_PARTIES);
        caseData.setRespondNotificationResponseRequired(NO);
        caseDetails.setCaseData(caseData);
        SendNotificationType sendNotification = SendNotificationType.builder().sendNotificationTitle("TEST").build();
        respondNotificationService.sendNotifyEmails(caseDetails, sendNotification);
        verify(emailService, times(1)).sendEmail(eq("noResponseTemplateId"),
            eq(CLAIMANT_EMAIL), any());
        verify(emailService, times(1)).sendEmail(eq("noResponseTemplateId"),
            eq(RESPONDENT_EMAIL), any());

    }

    @Test
    void sendNotifyEmailsNoResponseClaimantOnly() {
        setUpNotifyEmail();

        caseData.setRespondNotificationPartyToNotify(CLAIMANT_ONLY);
        caseData.setRespondNotificationResponseRequired(NO);
        caseDetails.setCaseData(caseData);
        SendNotificationType sendNotification = SendNotificationType.builder().sendNotificationTitle("TEST").build();
        respondNotificationService.sendNotifyEmails(caseDetails, sendNotification);
        verify(emailService, times(1)).sendEmail(eq("noResponseTemplateId"),
            eq(CLAIMANT_EMAIL), any());
        verify(emailService, times(0)).sendEmail(eq("noResponseTemplateId"),
            eq(RESPONDENT_EMAIL), any());

    }

    @Test
    void sendNotifyEmailsNoResponseClaimantOnlyMissingEmail() {
        setUpNotifyEmail();

        caseData.setRespondNotificationPartyToNotify(CLAIMANT_ONLY);
        caseData.setRespondNotificationResponseRequired(NO);
        caseData.getClaimantType().setClaimantEmailAddress(null);
        caseDetails.setCaseData(caseData);
        SendNotificationType sendNotification = SendNotificationType.builder().sendNotificationTitle("TEST").build();
        respondNotificationService.sendNotifyEmails(caseDetails, sendNotification);
        verify(emailService, times(0)).sendEmail(eq("noResponseTemplateId"),
            eq(CLAIMANT_EMAIL), any());
        verify(emailService, times(0)).sendEmail(eq("noResponseTemplateId"),
            eq(RESPONDENT_EMAIL), any());

    }

    @Test
    void sendNotifyEmailsResponseRequiredRespondentOnly() {
        setUpNotifyEmail();

        caseData.setRespondNotificationPartyToNotify(RESPONDENT_ONLY);
        caseData.setRespondNotificationResponseRequired(YES);
        caseDetails.setCaseData(caseData);
        SendNotificationType sendNotification = SendNotificationType.builder().sendNotificationTitle("TEST").build();
        respondNotificationService.sendNotifyEmails(caseDetails, sendNotification);
        verify(emailService, times(0)).sendEmail(eq("responseTemplateId"),
            eq(CLAIMANT_EMAIL), any());
        verify(emailService, times(1)).sendEmail(eq("responseTemplateId"),
            eq(RESPONDENT_EMAIL), any());

    }

    @Test
    void sendNotifyEmailsResponseRequiredRespondentOnlyMissingEmail() {
        setUpNotifyEmail();

        caseData.setRespondNotificationPartyToNotify(RESPONDENT_ONLY);
        caseData.setRespondNotificationResponseRequired(YES);
        caseData.getRespondentCollection().get(0).getValue().setRespondentEmail(null);
        caseDetails.setCaseData(caseData);
        SendNotificationType sendNotification = SendNotificationType.builder().sendNotificationTitle("TEST").build();
        respondNotificationService.sendNotifyEmails(caseDetails, sendNotification);
        verify(emailService, times(0)).sendEmail(eq("responseTemplateId"),
            eq(CLAIMANT_EMAIL), any());
        verify(emailService, times(0)).sendEmail(eq("responseTemplateId"),
            eq(RESPONDENT_EMAIL), any());

    }
}
