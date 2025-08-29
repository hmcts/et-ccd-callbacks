package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SendNotificationService;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.JUDGMENT;

class NotificationDocumentHelperTest {

    private static final String ACCESS_KEY = "access-key";
    private static final String TEST_CASE_REFERENCE = "1234567/2025";
    
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void testMinimalBuildNotificationDocumentData() throws JsonProcessingException {

        SendNotificationType notification = new SendNotificationType();
        notification.setNumber("1");
        notification.setSendNotificationTitle("Title");
        notification.setDate("2025-01-01");
        notification.setSendNotificationNotify("Both parties");
        notification.setSendNotificationSubject(new ArrayList<>());
        notification.setSendNotificationSubjectString("");
        
        String notificationId = UUID.randomUUID().toString();
        CaseData caseData = caseDataWithNotification(notification, notificationId);

        String json = NotificationDocumentHelper.buildNotificationDocumentData(caseData, ACCESS_KEY);
        JsonNode root = parseJson(json);

        assertEquals(ACCESS_KEY, root.get("accessKey").asText());
        assertEquals("EM-TRB-EGW-ENG-00070.docx", root.get("templateName").asText());
        assertEquals("Notification 1 Summary.pdf", root.get("outputName").asText());
        
        JsonNode data = root.get("data");
        assertEquals(TEST_CASE_REFERENCE, data.get("ethosCaseReference").asText());
        assertEquals("1", data.get("notificationNumber").asText());
        assertEquals("Title", data.get("notificationTitle").asText());
        assertEquals("", data.get("notificationSubject").asText());
        assertEquals("Both parties", data.get("partyToNotify").asText());
        assertEquals("-", data.get("additionalInformation").asText());
        
        assertFalse(data.hasNonNull("areThereLetters"));
        assertFalse(data.hasNonNull("documents"));
        assertFalse(data.hasNonNull("areThereResponses"));
        assertFalse(data.hasNonNull("areThereTribunalResponses"));
    }

    @Test
    void testHearingSubjectMapping() throws JsonProcessingException {

        SendNotificationType notification = new SendNotificationType();
        notification.setNumber("2");
        notification.setSendNotificationTitle("Hearing Notice");
        notification.setDate("2025-01-02");
        notification.setSendNotificationNotify("Both parties");
        notification.setSendNotificationSubject(List.of("Hearing"));
        notification.setSendNotificationSubjectString("Hearing");
        notification.setSendNotificationSelectHearing(dynamic("1", "Hearing 1 - 2025-01-02"));
        
        String notificationId = UUID.randomUUID().toString();
        CaseData caseData = caseDataWithNotification(notification, notificationId);

        String json = NotificationDocumentHelper.buildNotificationDocumentData(caseData, ACCESS_KEY);
        JsonNode data = parseJson(json).get("data");

        assertEquals(YES, data.get("isHearingSubject").asText());
        assertEquals("Hearing 1 - 2025-01-02", data.get("hearing").asText());
    }

    @Test
    void testCaseManagementOrdersRequestsSubjectMapping() throws JsonProcessingException {

        SendNotificationType notification = new SendNotificationType();
        notification.setNumber("3");
        notification.setSendNotificationTitle("CMO Notice");
        notification.setDate("2025-01-03");
        notification.setSendNotificationNotify("Both parties");
        notification.setSendNotificationSubject(List.of(SendNotificationService.CASE_MANAGEMENT_ORDERS_REQUESTS));
        notification.setSendNotificationSubjectString("Case management orders / requests");
        notification.setSendNotificationCaseManagement("Order");
        notification.setSendNotificationFullName("Judge A");
        notification.setSendNotificationResponseTribunal("Yes");

        String notificationId = UUID.randomUUID().toString();
        CaseData caseData = caseDataWithNotification(notification, notificationId);

        String json = NotificationDocumentHelper.buildNotificationDocumentData(caseData, ACCESS_KEY);
        JsonNode data = parseJson(json).get("data");

        assertEquals(YES, data.get("isCmoSubject").asText());
        assertEquals("Order", data.get("cmoOrRequest").asText());
        assertEquals("Judge A", data.get("cmoRequestMadeBy").asText());
        assertEquals("Yes", data.get("cmoRequestResponseRequired").asText());
    }

    @Test
    void testJudgmentSubjectMapping() throws JsonProcessingException {

        SendNotificationType notification = new SendNotificationType();
        notification.setNumber("4");
        notification.setSendNotificationTitle("Judgment Notice");
        notification.setDate("2025-01-04");
        notification.setSendNotificationNotify("Both parties");
        notification.setSendNotificationSubject(List.of(JUDGMENT));
        notification.setSendNotificationSubjectString("Judgment");
        notification.setSendNotificationFullName2("Judgment Name");
        notification.setSendNotificationDecision("Decision text");

        String notificationId = UUID.randomUUID().toString();
        CaseData caseData = caseDataWithNotification(notification, notificationId);

        String json = NotificationDocumentHelper.buildNotificationDocumentData(caseData, ACCESS_KEY);
        JsonNode data = parseJson(json).get("data");

        assertEquals(YES, data.get("isJudgmentSubject").asText());
        assertEquals("Judgment Name", data.get("judgmentName").asText());
        assertEquals("Decision text", data.get("judgmentDecision").asText());
    }

    @Test
    void testEccSubjectMappingResponseRequired() throws JsonProcessingException {

        SendNotificationType notification = new SendNotificationType();
        notification.setNumber("5");
        notification.setSendNotificationTitle("ECC Notice");
        notification.setDate("2025-01-05");
        notification.setSendNotificationNotify("Both parties");
        notification.setSendNotificationSubject(List.of(SendNotificationService.EMPLOYER_CONTRACT_CLAIM));
        notification.setSendNotificationSubjectString("Employer Contract Claim");
        notification.setSendNotificationEccQuestion("Acceptance of ECC response");
        notification.setSendNotificationResponseTribunal("Yes");
        notification.setSendNotificationSelectParties("Respondent only");
        
        String notificationId = UUID.randomUUID().toString();
        CaseData caseData = caseDataWithNotification(notification, notificationId);

        String json = NotificationDocumentHelper.buildNotificationDocumentData(caseData, ACCESS_KEY);
        JsonNode data = parseJson(json).get("data");

        assertEquals(YES, data.get("isEccSubject").asText());
        assertEquals("Acceptance of ECC response", data.get("eccType").asText());
        assertEquals("Yes - Respondent only", data.get("eccResponseRequired").asText());
    }

    @Test
    void testEccSubjectMappingNoResponseRequired() throws JsonProcessingException {

        SendNotificationType notification = new SendNotificationType();
        notification.setNumber("6");
        notification.setSendNotificationTitle("ECC Notice");
        notification.setDate("2025-01-06");
        notification.setSendNotificationNotify("Both parties");
        notification.setSendNotificationSubject(List.of(SendNotificationService.EMPLOYER_CONTRACT_CLAIM));
        notification.setSendNotificationSubjectString("Employer Contract Claim");
        notification.setSendNotificationEccQuestion("Acceptance of ECC response");
        notification.setSendNotificationResponseTribunal(NO);

        String notificationId = UUID.randomUUID().toString();
        CaseData caseData = caseDataWithNotification(notification, notificationId);

        String json = NotificationDocumentHelper.buildNotificationDocumentData(caseData, ACCESS_KEY);
        JsonNode data = parseJson(json).get("data");

        assertEquals(YES, data.get("isEccSubject").asText());
        assertEquals("Acceptance of ECC response", data.get("eccType").asText());
        assertEquals(NO, data.get("eccResponseRequired").asText());
    }

    @Test
    void testWithUploadedDocuments() throws JsonProcessingException {

        SendNotificationType notification = new SendNotificationType();
        notification.setNumber("7");
        notification.setSendNotificationTitle("Document Notice");
        notification.setDate("2025-01-07");
        notification.setSendNotificationNotify("Both parties");
        notification.setSendNotificationSubject(new ArrayList<>());
        notification.setSendNotificationSubjectString("");
        
        List<DocumentTypeItem> documents = new ArrayList<>();
        documents.add(doc("/documents/123/456", "/documents/123/456/binary", "doc1.pdf", "Document 1"));
        notification.setSendNotificationUploadDocument(documents);
        
        String notificationId = UUID.randomUUID().toString();
        CaseData caseData = caseDataWithNotification(notification, notificationId);

        String json = NotificationDocumentHelper.buildNotificationDocumentData(caseData, ACCESS_KEY);
        JsonNode data = parseJson(json).get("data");

        assertEquals(YES, data.get("areThereLetters").asText());
        assertTrue(data.hasNonNull("documents"));
        assertEquals(1, data.get("documents").size());
    }

    @Test
    void testPartyResponsesMapping() throws JsonProcessingException {

        SendNotificationType notification = new SendNotificationType();
        notification.setNumber("8");
        notification.setSendNotificationTitle("Response Notice");
        notification.setDate("2025-01-08");
        notification.setSendNotificationNotify("Both parties");
        notification.setSendNotificationSubject(new ArrayList<>());
        notification.setSendNotificationSubjectString("");
        
        List<PseResponseTypeItem> responses = new ArrayList<>();
        responses.add(pseResponse("Claimant", "2025-01-08", "Response 1", true, YES, null));
        responses.add(pseResponse("Respondent", "2025-01-09", "Response 2", false, NO, "Reason here"));
        notification.setRespondCollection(responses);
        
        String notificationId = UUID.randomUUID().toString();
        CaseData caseData = caseDataWithNotification(notification, notificationId);

        String json = NotificationDocumentHelper.buildNotificationDocumentData(caseData, ACCESS_KEY);
        JsonNode data = parseJson(json).get("data");

        assertEquals(YES, data.get("areThereResponses").asText());
        assertEquals(2, data.get("responses").size());
        
        JsonNode firstResponse = data.get("responses").get(0);
        assertEquals("Claimant", firstResponse.get("party").asText());
        assertEquals(YES, firstResponse.get("areThereDocuments").asText());
        assertEquals(YES, firstResponse.get("copyToOtherParty").asText());
        
        JsonNode secondResponse = data.get("responses").get(1);
        assertEquals("Respondent", secondResponse.get("party").asText());
        assertEquals(NO, secondResponse.get("areThereDocuments").asText());
        assertEquals("No - Reason here", secondResponse.get("copyToOtherParty").asText());
    }

    @Test
    void testTribunalResponsesMapping() throws JsonProcessingException {

        SendNotificationType notification = new SendNotificationType();
        notification.setNumber("9");
        notification.setSendNotificationTitle("Tribunal Response Notice");
        notification.setDate("2025-01-09");
        notification.setSendNotificationNotify("Both parties");
        notification.setSendNotificationSubject(new ArrayList<>());
        notification.setSendNotificationSubjectString("");
        
        List<GenericTypeItem<RespondNotificationType>> tribunalResponses = new ArrayList<>();
        tribunalResponses.add(tribunalResponse("Response Title", "2025-01-09", "Order", 
                                             "Both parties", YES, "Both parties", "Judge B", true, null));
        notification.setRespondNotificationTypeCollection(tribunalResponses);
        
        String notificationId = UUID.randomUUID().toString();
        CaseData caseData = caseDataWithNotification(notification, notificationId);

        String json = NotificationDocumentHelper.buildNotificationDocumentData(caseData, ACCESS_KEY);
        JsonNode data = parseJson(json).get("data");

        assertEquals(YES, data.get("areThereTribunalResponses").asText());
        assertEquals(1, data.get("tribunalResponses").size());
        
        JsonNode tribunalResponse = data.get("tribunalResponses").get(0);
        assertEquals("Response Title", tribunalResponse.get("title").asText());
        assertEquals("Yes - Both parties", tribunalResponse.get("responseRequired").asText());
        assertEquals(YES, tribunalResponse.get("areThereDocuments").asText());
        assertEquals("-", tribunalResponse.get("additionalInformation").asText());
    }

    @Test
    void testGetDocumentName() {
        SendNotificationType notification = new SendNotificationType();
        notification.setNumber("3");
        
        String notificationId = UUID.randomUUID().toString();
        CaseData caseData = caseDataWithNotification(notification, notificationId);
        
        String documentName = NotificationDocumentHelper.getDocumentName(caseData);
 
        assertEquals("Notification 3 Summary.pdf", documentName);
    }

    @Test
    void testInvalidNotificationIdThrowsIllegalArgumentException() {

        SendNotificationType notification = new SendNotificationType();
        notification.setNumber("1");
        
        CaseData caseData = caseDataWithNotification(notification, "A");
        // Override the dropdown to select a different ID
        caseData.setSelectNotificationDropdown(dynamic("B", "Non-existent"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
                () -> NotificationDocumentHelper.getDocumentName(caseData));
        assertTrue(exception.getMessage().contains("Cannot find notification with id: B"));
        assertTrue(exception.getMessage().contains("in case " + TEST_CASE_REFERENCE));
        
        // Test buildNotificationDocumentData also throws
        IllegalArgumentException buildException = assertThrows(IllegalArgumentException.class, 
                () -> NotificationDocumentHelper.buildNotificationDocumentData(caseData, ACCESS_KEY));
        assertTrue(buildException.getMessage().contains("Cannot find notification with id: B"));
    }

    // Helper method to create DynamicFixedListType
    private DynamicFixedListType dynamic(String code, String label) {
        return DynamicFixedListType.of(DynamicValueType.create(code, label));
    }

    // Helper method to create DocumentTypeItem
    private DocumentTypeItem doc(String url, String binaryUrl, String fileName, String shortDescription) {
        UploadedDocumentType uploadedDoc = new UploadedDocumentType();
        uploadedDoc.setDocumentUrl(url);
        uploadedDoc.setDocumentBinaryUrl(binaryUrl);
        uploadedDoc.setDocumentFilename(fileName);

        DocumentType docType = new DocumentType();
        docType.setUploadedDocument(uploadedDoc);
        docType.setShortDescription(shortDescription);

        DocumentTypeItem docItem = new DocumentTypeItem();
        docItem.setValue(docType);
        return docItem;
    }

    // Helper method to create GenericTypeItem<DocumentType> for supporting material
    private GenericTypeItem<DocumentType> supportingDoc() {
        UploadedDocumentType uploadedDocType = new UploadedDocumentType();
        uploadedDocType.setDocumentUrl("https://dmstore.com/documents/123/456");
        uploadedDocType.setDocumentBinaryUrl("https://dmstore.com/documents/123/456/binary");
        uploadedDocType.setDocumentFilename("support.pdf");

        DocumentType docType = new DocumentType();
        docType.setUploadedDocument(uploadedDocType);

        GenericTypeItem<DocumentType> item = new GenericTypeItem<>();
        item.setId(UUID.randomUUID().toString());
        item.setValue(docType);
        return item;
    }

    // Helper method to create PseResponseTypeItem
    private PseResponseTypeItem pseResponse(String from, String date, String response,
                                            boolean withDoc, String copyToOtherParty, String copyNoDetails) {
        PseResponseType responseType = new PseResponseType();
        responseType.setFrom(from);
        responseType.setDate(date);
        responseType.setResponse(response);
        responseType.setCopyToOtherParty(copyToOtherParty);
        responseType.setCopyNoGiveDetails(copyNoDetails);

        List<GenericTypeItem<DocumentType>> supportingMaterial = new ArrayList<>();
        if (withDoc) {
            supportingMaterial.add(supportingDoc());
        }
        responseType.setSupportingMaterial(supportingMaterial);

        PseResponseTypeItem item = new PseResponseTypeItem();
        item.setValue(responseType);
        return item;
    }

    // Helper method to create tribunal response
    private GenericTypeItem<RespondNotificationType> tribunalResponse(String title, String date, String cmoOrRequest,
                                                                      String partyToNotify, String responseRequired,
                                                                      String whoRespond, String madeBy, boolean withDoc,
                                                                      String additionalInfo) {
        RespondNotificationType respondType = new RespondNotificationType();
        respondType.setRespondNotificationTitle(title);
        respondType.setRespondNotificationDate(date);
        respondType.setRespondNotificationCmoOrRequest(cmoOrRequest);
        respondType.setRespondNotificationPartyToNotify(partyToNotify);
        respondType.setRespondNotificationResponseRequired(responseRequired);
        respondType.setRespondNotificationWhoRespond(whoRespond);
        respondType.setRespondNotificationFullName(madeBy);
        respondType.setRespondNotificationAdditionalInfo(additionalInfo);

        if (withDoc) {
            List<DocumentTypeItem> documents = new ArrayList<>();
            documents.add(doc("/documents/789/abc", "/documents/789/abc/binary", "tribunal.pdf", "Tribunal doc"));
            respondType.setRespondNotificationUploadDocument(documents);
        }

        GenericTypeItem<RespondNotificationType> item = new GenericTypeItem<>();
        item.setValue(respondType);
        return item;
    }

    // Helper method to create CaseData with notification
    private CaseData caseDataWithNotification(SendNotificationType notification, String notificationId) {

        SendNotificationTypeItem item = new SendNotificationTypeItem();
        item.setId(notificationId);
        item.setValue(notification);

        List<SendNotificationTypeItem> notifications = new ArrayList<>();
        notifications.add(item);
        CaseData caseData = CaseDataBuilder.builder()
                .withEthosCaseReference(TEST_CASE_REFERENCE)
                .build();
        caseData.setSendNotificationCollection(notifications);

        DynamicFixedListType dropdown = dynamic(notificationId, "Notification " + notification.getNumber());
        caseData.setSelectNotificationDropdown(dropdown);

        return caseData;
    }

    // Helper method to parse JSON
    private JsonNode parseJson(String json) throws JsonProcessingException {
        return objectMapper.readTree(json);
    }
}
