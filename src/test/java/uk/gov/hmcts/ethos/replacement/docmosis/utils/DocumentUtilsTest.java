package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.util.ArrayList;
import java.util.List;

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
    private static final String EMPLOYER_CONTRACT_CLAIM_TEST_DOCUMENT_BINARY_URL =
            "http://dm-store-aat.service.core-compute-aat.internal/documents/f40ed136-fb26-4638-af6c-80a98d56af5d/binary";
    private static final String DUMMY_DOCUMENT_BINARY_URL = "dummy_document_binary_url";

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
    void theContainsDocumentWithBinaryUrl() {
        DocumentTypeItem validDocumentTypeItemContestClaim =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class)
                        .getRespondentCollection().get(0).getValue().getEt3ResponseContestClaimDocument().get(0);
        List<DocumentTypeItem> emptyDocumentTypeItems = new ArrayList<>();
        assertThat(DocumentUtils.containsDocumentWithBinaryUrl(
                        emptyDocumentTypeItems, EMPLOYER_CONTRACT_CLAIM_TEST_DOCUMENT_BINARY_URL)).isFalse();

        List<DocumentTypeItem> validDocumentTypeItems = List.of(validDocumentTypeItemContestClaim);
        assertThat(DocumentUtils.containsDocumentWithBinaryUrl(validDocumentTypeItems, null)).isFalse();

        assertThat(DocumentUtils.containsDocumentWithBinaryUrl(
                validDocumentTypeItems, EMPLOYER_CONTRACT_CLAIM_TEST_DOCUMENT_BINARY_URL)).isTrue();

        validDocumentTypeItemContestClaim.getValue().getUploadedDocument().setDocumentBinaryUrl(
                DUMMY_DOCUMENT_BINARY_URL);

        assertThat(DocumentUtils.containsDocumentWithBinaryUrl(
                validDocumentTypeItems, EMPLOYER_CONTRACT_CLAIM_TEST_DOCUMENT_BINARY_URL)).isFalse();

        validDocumentTypeItemContestClaim.getValue().getUploadedDocument().setDocumentBinaryUrl(null);
        assertThat(DocumentUtils.containsDocumentWithBinaryUrl(
                validDocumentTypeItems, EMPLOYER_CONTRACT_CLAIM_TEST_DOCUMENT_BINARY_URL)).isFalse();

        validDocumentTypeItemContestClaim.getValue().setUploadedDocument(null);
        assertThat(DocumentUtils.containsDocumentWithBinaryUrl(
                validDocumentTypeItems, EMPLOYER_CONTRACT_CLAIM_TEST_DOCUMENT_BINARY_URL)).isFalse();

        validDocumentTypeItemContestClaim.setValue(null);
        assertThat(DocumentUtils.containsDocumentWithBinaryUrl(
                validDocumentTypeItems, EMPLOYER_CONTRACT_CLAIM_TEST_DOCUMENT_BINARY_URL)).isFalse();
    }

    @Test
    void theAddIfBinaryUrlNotExists() {
        DocumentTypeItem validDocumentTypeItemContestClaim =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class)
                        .getRespondentCollection().get(0).getValue().getEt3ResponseContestClaimDocument().get(0);
        assertDoesNotThrow(() ->
                DocumentUtils.addIfBinaryUrlNotExists(null, validDocumentTypeItemContestClaim));

        List<DocumentTypeItem> emptyDocumentTypeItems = new ArrayList<>();
        assertDoesNotThrow(() -> DocumentUtils.addIfBinaryUrlNotExists(emptyDocumentTypeItems, null));

        DocumentUtils.addIfBinaryUrlNotExists(emptyDocumentTypeItems, validDocumentTypeItemContestClaim);
        assertThat(emptyDocumentTypeItems).hasSize(1);

        validDocumentTypeItemContestClaim.getValue().getUploadedDocument().setDocumentFilename(null);
        assertDoesNotThrow(() -> DocumentUtils.addIfBinaryUrlNotExists(emptyDocumentTypeItems,
                validDocumentTypeItemContestClaim));

        validDocumentTypeItemContestClaim.getValue().getUploadedDocument().setDocumentFilename(
                DUMMY_DOCUMENT_BINARY_URL);
        assertDoesNotThrow(() -> DocumentUtils.addIfBinaryUrlNotExists(emptyDocumentTypeItems,
                validDocumentTypeItemContestClaim));

        validDocumentTypeItemContestClaim.getValue().setUploadedDocument(null);
        assertDoesNotThrow(() -> DocumentUtils.addIfBinaryUrlNotExists(emptyDocumentTypeItems,
                validDocumentTypeItemContestClaim));
        validDocumentTypeItemContestClaim.setValue(null);
        assertDoesNotThrow(() -> DocumentUtils.addIfBinaryUrlNotExists(emptyDocumentTypeItems,
                validDocumentTypeItemContestClaim));
    }

    @Test
    void testRemoveDocumentsWithMatchingBinaryUrls() {
        // Reference list
        DocumentTypeItem ref1 = DocumentTypeItem.builder().value(DocumentType.builder().uploadedDocument(
                UploadedDocumentType.builder().documentBinaryUrl("binaryUrl1").build()).build()).build();
        DocumentTypeItem ref2 = DocumentTypeItem.builder().value(DocumentType.builder().uploadedDocument(
                UploadedDocumentType.builder().documentBinaryUrl("binaryUrl2").build()).build()).build();
        List<DocumentTypeItem> reference = List.of(ref1, ref2);

        // Primary list - initial state
        DocumentTypeItem doc1 = DocumentTypeItem.builder().value(DocumentType.builder().uploadedDocument(
                UploadedDocumentType.builder()
                        .documentBinaryUrl("binaryUrl1").build()).build()).build(); // should be removed
        DocumentTypeItem doc2 = DocumentTypeItem.builder().value(DocumentType.builder().uploadedDocument(
                UploadedDocumentType.builder().documentBinaryUrl("binaryUrl3").build()).build()).build(); // should stay
        DocumentTypeItem doc3 = DocumentTypeItem.builder().value(DocumentType.builder().uploadedDocument(
                UploadedDocumentType.builder()
                        .documentBinaryUrl("binaryUrl2").build()).build()).build(); // should be removed
        DocumentTypeItem doc4 = DocumentTypeItem.builder().value(DocumentType.builder().uploadedDocument(
                UploadedDocumentType.builder().documentBinaryUrl("binaryUrl4").build()).build()).build(); // should stay

        List<DocumentTypeItem> primary = new ArrayList<>(List.of(doc1, doc2, doc3, doc4));

        // Run method
        DocumentUtils.removeDocumentsWithMatchingBinaryUrls(primary, reference);

        // Validate results
        assertEquals(2, primary.size(), "Only unmatched and null items should remain");

        assertTrue(primary.contains(doc2), "url3 should remain");
        assertTrue(primary.contains(doc4), "url4 should remain");
        assertFalse(primary.contains(doc1), "url1 should be removed");
        assertFalse(primary.contains(doc3), "url2 should be removed");
    }
}
