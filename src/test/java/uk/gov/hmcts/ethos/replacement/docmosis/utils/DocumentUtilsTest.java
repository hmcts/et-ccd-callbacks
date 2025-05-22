package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ET3_FORM_CASE_DATA_FILE;

final class DocumentUtilsTest {

    private static final String TOP_LEVEL = "RESPONSE_TO_A_CLAIM";
    private static final String SECOND_LEVEL_ET3 = "ET3";
    private static final String SECOND_LEVEL_ET3_ATTACHMENT = "ET3 Attachment";
    private static final UploadedDocumentType VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM =
            ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class)
                    .getRespondentCollection().get(0).getValue().getEt3Form();
    private static final String UPLOADED_DOCUMENT_FILE_NAME = "Test Company - ET3 Response.pdf";
    private static final String ET3_FORM_ENGLISH_DESCRIPTION = "ET3 form English version";
    private static final String DUMMY_DOCUMENT_BINARY_URL_1 = "dummy_document_binary_url_1";
    private static final String DUMMY_DOCUMENT_BINARY_URL_2 = "dummy_document_binary_url_2";
    private static final String DUMMY_DOCUMENT_BINARY_URL_3 = "dummy_document_binary_url_3";
    private static final String DUMMY_DOCUMENT_BINARY_URL_4 = "dummy_document_binary_url_4";
    private static final String DUMMY_DOCUMENT_BINARY_URL_5 = "dummy_document_binary_url_5";
    private static final String DUMMY_DOCUMENT_BINARY_URL_6 = "dummy_document_binary_url_6";

    @Test
    void theConvertUploadedDocumentTypeToDocumentTypeItemWithLevels() {
        assertThat(DocumentUtils.convertUploadedDocumentTypeToDocumentTypeItemWithLevels(null,
                TOP_LEVEL, SECOND_LEVEL_ET3)).isNull();

        assertThat(DocumentUtils
                .convertUploadedDocumentTypeToDocumentTypeItemWithLevels(VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM,
                        null, SECOND_LEVEL_ET3)).isNull();

        assertThat(DocumentUtils
                .convertUploadedDocumentTypeToDocumentTypeItemWithLevels(VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM,
                        TOP_LEVEL, null)).isNull();

        DocumentTypeItem documentTypeItemET3 = DocumentUtils.convertUploadedDocumentTypeToDocumentTypeItemWithLevels(
                VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, TOP_LEVEL, SECOND_LEVEL_ET3);
        assertThat(documentTypeItemET3).isNotNull();
        assertThat(documentTypeItemET3.getValue().getTopLevelDocuments()).isEqualTo(TOP_LEVEL);
        assertThat(documentTypeItemET3.getValue().getResponseClaimDocuments()).isEqualTo(SECOND_LEVEL_ET3);
        assertThat(documentTypeItemET3.getValue().getUploadedDocument().getDocumentFilename())
                .isEqualTo(UPLOADED_DOCUMENT_FILE_NAME);

        DocumentTypeItem documentTypeItemET3Attachment =
                DocumentUtils.convertUploadedDocumentTypeToDocumentTypeItemWithLevels(
                        VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, TOP_LEVEL, SECOND_LEVEL_ET3_ATTACHMENT);
        assertThat(documentTypeItemET3Attachment).isNotNull();
        assertThat(documentTypeItemET3Attachment.getValue().getTopLevelDocuments()).isEqualTo(TOP_LEVEL);
        assertThat(documentTypeItemET3Attachment.getValue().getResponseClaimDocuments())
                .isEqualTo(SECOND_LEVEL_ET3_ATTACHMENT);
        assertThat(documentTypeItemET3Attachment.getValue().getUploadedDocument().getDocumentFilename())
                .isEqualTo(UPLOADED_DOCUMENT_FILE_NAME);
    }

    @Test
    void theSetDocumentTypeItemLevels() {
        DocumentTypeItem validDocumentTypeItemContestClaim =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class)
                        .getRespondentCollection().get(0).getValue().getEt3ResponseContestClaimDocument().get(0);
        assertDoesNotThrow(() -> DocumentUtils.setDocumentTypeItemLevels(null,
                TOP_LEVEL, SECOND_LEVEL_ET3));
        assertDoesNotThrow(() -> DocumentUtils.setDocumentTypeItemLevels(validDocumentTypeItemContestClaim,
                null, SECOND_LEVEL_ET3));
        assertDoesNotThrow(() -> DocumentUtils.setDocumentTypeItemLevels(validDocumentTypeItemContestClaim,
                TOP_LEVEL, null));

        DocumentUtils.setDocumentTypeItemLevels(validDocumentTypeItemContestClaim, TOP_LEVEL, SECOND_LEVEL_ET3);
        assertThat(validDocumentTypeItemContestClaim.getValue().getTopLevelDocuments()).isEqualTo(TOP_LEVEL);
        assertThat(validDocumentTypeItemContestClaim.getValue().getResponseClaimDocuments())
                .isEqualTo(SECOND_LEVEL_ET3);

        DocumentUtils.setDocumentTypeItemLevels(validDocumentTypeItemContestClaim,
                TOP_LEVEL, SECOND_LEVEL_ET3_ATTACHMENT);
        assertThat(validDocumentTypeItemContestClaim.getValue().getTopLevelDocuments()).isEqualTo(TOP_LEVEL);
        assertThat(validDocumentTypeItemContestClaim.getValue().getResponseClaimDocuments())
                .isEqualTo(SECOND_LEVEL_ET3_ATTACHMENT);
    }

    @Test
    void theAddUploadedDocumentTypeToDocumentTypeItems() {

        List<DocumentTypeItem> emptyDocumentTypeItems = new ArrayList<>();
        assertDoesNotThrow(() -> DocumentUtils.addUploadedDocumentTypeToDocumentTypeItems(null,
                VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, TOP_LEVEL, SECOND_LEVEL_ET3, ET3_FORM_ENGLISH_DESCRIPTION));
        assertDoesNotThrow(() -> DocumentUtils.addUploadedDocumentTypeToDocumentTypeItems(emptyDocumentTypeItems,
                null, TOP_LEVEL, SECOND_LEVEL_ET3, ET3_FORM_ENGLISH_DESCRIPTION));
        assertDoesNotThrow(() -> DocumentUtils.addUploadedDocumentTypeToDocumentTypeItems(emptyDocumentTypeItems,
                VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, null, SECOND_LEVEL_ET3, ET3_FORM_ENGLISH_DESCRIPTION));
        assertDoesNotThrow(() -> DocumentUtils.addUploadedDocumentTypeToDocumentTypeItems(emptyDocumentTypeItems,
                VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, TOP_LEVEL, null, ET3_FORM_ENGLISH_DESCRIPTION));
        assertDoesNotThrow(() -> DocumentUtils.addUploadedDocumentTypeToDocumentTypeItems(emptyDocumentTypeItems,
                VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, TOP_LEVEL, SECOND_LEVEL_ET3, null));

        DocumentUtils.addUploadedDocumentTypeToDocumentTypeItems(emptyDocumentTypeItems,
                VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, TOP_LEVEL, SECOND_LEVEL_ET3, ET3_FORM_ENGLISH_DESCRIPTION);
        assertThat(emptyDocumentTypeItems).isNotEmpty();
        assertThat(emptyDocumentTypeItems.get(0).getValue().getTopLevelDocuments()).isEqualTo(TOP_LEVEL);
        assertThat(emptyDocumentTypeItems.get(0).getValue().getResponseClaimDocuments()).isEqualTo(SECOND_LEVEL_ET3);
        assertThat(emptyDocumentTypeItems.get(0).getValue().getUploadedDocument().getDocumentFilename())
                .isEqualTo(UPLOADED_DOCUMENT_FILE_NAME);

        // Tests when static method convertUploadedDocumentTypeToDocumentTypeItemWithLevels returns null.
        try (MockedStatic<DocumentUtils> mocked = mockStatic(DocumentUtils.class)) {
            mocked.when(() -> DocumentUtils.convertUploadedDocumentTypeToDocumentTypeItemWithLevels(
                    VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, TOP_LEVEL, SECOND_LEVEL_ET3)).thenReturn(null);
            List<DocumentTypeItem> emptyDocumentTypeItems2 = new ArrayList<>();
            mocked.when(() -> DocumentUtils.addUploadedDocumentTypeToDocumentTypeItems(emptyDocumentTypeItems2,
                    VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, TOP_LEVEL, SECOND_LEVEL_ET3, ET3_FORM_ENGLISH_DESCRIPTION))
                    .thenCallRealMethod();
            DocumentUtils.addUploadedDocumentTypeToDocumentTypeItems(emptyDocumentTypeItems2,
                    VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, TOP_LEVEL, SECOND_LEVEL_ET3, ET3_FORM_ENGLISH_DESCRIPTION);
            assertThat(emptyDocumentTypeItems2).isEmpty();
        }
    }

    @Test
    void theHasMatchingId() {
        // Helper to create a DocumentTypeItem with a given ID
        Function<String, DocumentTypeItem> itemWithId = id -> DocumentTypeItem.builder().id(id).build();
        // Case 1: targetItem is null → should return true
        assertTrue(DocumentUtils.hasMatchingId(List.of(itemWithId.apply("id1")), null));
        // Case 2: targetItem ID is blank → should return true
        assertTrue(DocumentUtils.hasMatchingId(List.of(itemWithId.apply("id1")), itemWithId.apply("")));
        // Case 3: document list is null → should return false
        assertFalse(DocumentUtils.hasMatchingId(null, itemWithId.apply("id1")));
        // Case 4: document list is empty → should return false
        assertFalse(DocumentUtils.hasMatchingId(new ArrayList<>(), itemWithId.apply("id1")));
        // Case 5: matching ID exists → should return true
        assertTrue(DocumentUtils.hasMatchingId(
                List.of(itemWithId.apply("id1"), itemWithId.apply("id2")),
                itemWithId.apply("id2")
        ));
        // Case 6: no matching ID → should return false
        assertFalse(DocumentUtils.hasMatchingId(
                List.of(itemWithId.apply("id1"), itemWithId.apply("id3")),
                itemWithId.apply("id2")
        ));
    }

    @Test
    void theHasMatchingBinaryUrl() {
        // Helper to create a DocumentTypeItem with a given ID
        Function<String, DocumentTypeItem> itemWithBinaryUrl = binaryUrl -> DocumentTypeItem.builder()
                .value(DocumentType.builder().uploadedDocument(UploadedDocumentType.builder()
                        .documentBinaryUrl(binaryUrl).build()).build()).build();
        // Case 1: targetItem is null → should return true
        assertTrue(DocumentUtils.hasMatchingBinaryUrl(
                List.of(itemWithBinaryUrl.apply(DUMMY_DOCUMENT_BINARY_URL_1)), null));
        // Case 2: targetItem ID is blank → should return true
        assertTrue(DocumentUtils.hasMatchingBinaryUrl(List.of(itemWithBinaryUrl.apply(DUMMY_DOCUMENT_BINARY_URL_1)),
                itemWithBinaryUrl.apply(StringUtils.EMPTY)));
        // Case 3: document list is null → should return false
        assertFalse(DocumentUtils.hasMatchingBinaryUrl(null,
                itemWithBinaryUrl.apply(DUMMY_DOCUMENT_BINARY_URL_1)));
        // Case 4: document list is empty → should return false
        assertFalse(DocumentUtils.hasMatchingBinaryUrl(new ArrayList<>(),
                itemWithBinaryUrl.apply(DUMMY_DOCUMENT_BINARY_URL_1)));
        // Case 5: matching ID exists → should return true
        assertTrue(DocumentUtils.hasMatchingBinaryUrl(
                List.of(itemWithBinaryUrl.apply(DUMMY_DOCUMENT_BINARY_URL_1),
                        itemWithBinaryUrl.apply(DUMMY_DOCUMENT_BINARY_URL_2)),
                itemWithBinaryUrl.apply(DUMMY_DOCUMENT_BINARY_URL_2)
        ));
        // Case 6: no matching ID → should return false
        assertFalse(DocumentUtils.hasMatchingBinaryUrl(
                List.of(itemWithBinaryUrl.apply(DUMMY_DOCUMENT_BINARY_URL_1),
                        itemWithBinaryUrl.apply(DUMMY_DOCUMENT_BINARY_URL_3)),
                itemWithBinaryUrl.apply(DUMMY_DOCUMENT_BINARY_URL_2)
        ));
    }

    @Test
    void theAddDocumentIfUnique() {
        Function<String, DocumentTypeItem> itemWithId = id -> DocumentTypeItem.builder().id(id).build();
        List<DocumentTypeItem> documentTypeItems = new ArrayList<>();
        // CASE 1: Already has a document with matching id
        documentTypeItems.add(itemWithId.apply("id1"));
        DocumentUtils.addDocumentIfUnique(documentTypeItems, itemWithId.apply("id1"));
        assertThat(documentTypeItems).hasSize(NumberUtils.INTEGER_ONE);
        Function<String, DocumentTypeItem> itemWithBinaryUrl = binaryUrl ->
                DocumentTypeItem.builder().value(DocumentType.builder().uploadedDocument(
                        UploadedDocumentType.builder().documentBinaryUrl(binaryUrl).build()).build()).build();
        // CASE 2: Already has a document with matching binaryUrl
        documentTypeItems.add(itemWithBinaryUrl.apply("binaryUrl1"));
        DocumentUtils.addDocumentIfUnique(documentTypeItems, itemWithBinaryUrl.apply("binaryUrl1"));
        assertThat(documentTypeItems).hasSize(NumberUtils.INTEGER_TWO);

        // CASE 3: Not have same id and binary url
        DocumentTypeItem documentTypeItem = itemWithBinaryUrl.apply("binaryUrl2");
        documentTypeItem.setId("id3");
        DocumentUtils.addDocumentIfUnique(documentTypeItems, documentTypeItem);
        assertThat(documentTypeItems).hasSize(NumberUtils.INTEGER_TWO + NumberUtils.INTEGER_ONE);
    }

    @Test
    void theRemoveDocumentsWithMatchingIDs() {
        // Case 1: Matching IDs should be removed
        List<DocumentTypeItem> primary = new ArrayList<>(List.of(
                DocumentTypeItem.builder().id("doc1").build(),
                DocumentTypeItem.builder().id("doc2").build(),
                DocumentTypeItem.builder().id("doc3").build()
        ));
        List<DocumentTypeItem> reference = List.of(
                DocumentTypeItem.builder().id("doc2").build(),
                DocumentTypeItem.builder().id("doc3").build()
        );
        DocumentUtils.removeDocumentsWithMatchingIDs(primary, reference);
        assertEquals(1, primary.size());
        assertEquals("doc1", primary.get(0).getId());
        // Case 2: No matching IDs, list remains unchanged
        primary = new ArrayList<>(List.of(
                DocumentTypeItem.builder().id("docA").build(),
                DocumentTypeItem.builder().id("docB").build()
        ));
        reference = List.of(
                DocumentTypeItem.builder().id("docX").build(),
                DocumentTypeItem.builder().id("docY").build()
        );
        DocumentUtils.removeDocumentsWithMatchingIDs(primary, reference);
        assertEquals(2, primary.size());
        assertTrue(primary.stream().anyMatch(i -> "docA".equals(i.getId())));
        assertTrue(primary.stream().anyMatch(i -> "docB".equals(i.getId())));
        // Case 3: reference list is empty
        primary = new ArrayList<>(List.of(
                DocumentTypeItem.builder().id("doc1").build(),
                DocumentTypeItem.builder().id("doc2").build()
        ));
        reference = Collections.emptyList();
        DocumentUtils.removeDocumentsWithMatchingIDs(primary, reference);
        assertEquals(2, primary.size());
        // Case 4: primary list is empty
        primary = new ArrayList<>();
        reference = List.of(DocumentTypeItem.builder().id("doc1").build());
        DocumentUtils.removeDocumentsWithMatchingIDs(primary, reference);
        assertTrue(primary.isEmpty());
        // Case 5: both lists are empty
        primary = new ArrayList<>();
        reference = new ArrayList<>();
        DocumentUtils.removeDocumentsWithMatchingIDs(primary, reference);
        assertTrue(primary.isEmpty());
        // Case 6: one null ID in reference list
        primary = new ArrayList<>(List.of(
                DocumentTypeItem.builder().id("doc1").build(),
                DocumentTypeItem.builder().id("doc2").build()
        ));
        reference = List.of(
                DocumentTypeItem.builder().id(null).build(),
                DocumentTypeItem.builder().id("doc1").build()
        );
        DocumentUtils.removeDocumentsWithMatchingIDs(primary, reference);
        assertEquals(1, primary.size());
        assertEquals("doc2", primary.get(0).getId());
    }

    @Test
    void theRemoveDocumentsWithMatchingBinaryURLs() {
        List<DocumentTypeItem> primaryDocumentTypeItems = new ArrayList<>(List.of());
        List<DocumentTypeItem> referenceDocumentTypeItems = new ArrayList<>();
        // CASE 1: Both lists are null
        assertDoesNotThrow(() -> DocumentUtils
                .removeDocumentsWithMatchingBinaryURLs(null, null));
        // CASE 2: Both lists are empty
        DocumentUtils.removeDocumentsWithMatchingBinaryURLs(primaryDocumentTypeItems, referenceDocumentTypeItems);
        assertEquals(0, primaryDocumentTypeItems.size());

        primaryDocumentTypeItems.add(DocumentTypeItem.builder()
                .value(DocumentType.builder()
                        .uploadedDocument(UploadedDocumentType.builder()
                                .documentBinaryUrl(DUMMY_DOCUMENT_BINARY_URL_1).build()).build()).build());
        // CASE 3: Only reference list is null
        assertDoesNotThrow(() -> DocumentUtils
                .removeDocumentsWithMatchingBinaryURLs(primaryDocumentTypeItems, null));

        referenceDocumentTypeItems.add(DocumentTypeItem.builder()
                .value(DocumentType.builder()
                        .uploadedDocument(UploadedDocumentType.builder()
                                .documentBinaryUrl(DUMMY_DOCUMENT_BINARY_URL_4).build()).build()).build());
        //CASE 4: Only primary list is null
        assertDoesNotThrow(() -> DocumentUtils
                .removeDocumentsWithMatchingBinaryURLs(null, referenceDocumentTypeItems));

        primaryDocumentTypeItems.addAll(List.of(
                DocumentTypeItem.builder().value(DocumentType.builder().uploadedDocument(UploadedDocumentType.builder()
                                .documentBinaryUrl(DUMMY_DOCUMENT_BINARY_URL_2).build()).build()).build(),
                DocumentTypeItem.builder().value(DocumentType.builder().uploadedDocument(UploadedDocumentType.builder()
                        .documentBinaryUrl(DUMMY_DOCUMENT_BINARY_URL_3).build()).build()).build()));
        referenceDocumentTypeItems.addAll(List.of(
                DocumentTypeItem.builder().value(DocumentType.builder().uploadedDocument(UploadedDocumentType.builder()
                        .documentBinaryUrl(DUMMY_DOCUMENT_BINARY_URL_5).build()).build()).build(),
                DocumentTypeItem.builder().value(DocumentType.builder().uploadedDocument(UploadedDocumentType.builder()
                        .documentBinaryUrl(DUMMY_DOCUMENT_BINARY_URL_6).build()).build()).build()));
        // CASE 5: None of the binary urls matches
        DocumentUtils.removeDocumentsWithMatchingBinaryURLs(primaryDocumentTypeItems, referenceDocumentTypeItems);
        assertEquals(3, primaryDocumentTypeItems.size());
        // CASE 6: Only one of the binary urls matches
        referenceDocumentTypeItems.add(DocumentTypeItem.builder()
                .value(DocumentType.builder()
                        .uploadedDocument(UploadedDocumentType.builder()
                                .documentBinaryUrl(DUMMY_DOCUMENT_BINARY_URL_3).build()).build()).build());
        DocumentUtils.removeDocumentsWithMatchingBinaryURLs(primaryDocumentTypeItems, referenceDocumentTypeItems);
        assertEquals(2, primaryDocumentTypeItems.size());
        // CASE 7: Two of the binary urls match
        referenceDocumentTypeItems.add(DocumentTypeItem.builder()
                .value(DocumentType.builder()
                        .uploadedDocument(UploadedDocumentType.builder()
                                .documentBinaryUrl(DUMMY_DOCUMENT_BINARY_URL_2).build()).build()).build());
        DocumentUtils.removeDocumentsWithMatchingBinaryURLs(primaryDocumentTypeItems, referenceDocumentTypeItems);
        assertEquals(1, primaryDocumentTypeItems.size());
        // CASE 8: All binary urls match
        referenceDocumentTypeItems.add(DocumentTypeItem.builder()
                .value(DocumentType.builder()
                        .uploadedDocument(UploadedDocumentType.builder()
                                .documentBinaryUrl(DUMMY_DOCUMENT_BINARY_URL_1).build()).build()).build());
        DocumentUtils.removeDocumentsWithMatchingBinaryURLs(primaryDocumentTypeItems, referenceDocumentTypeItems);
        assertEquals(0, primaryDocumentTypeItems.size());
    }
}
