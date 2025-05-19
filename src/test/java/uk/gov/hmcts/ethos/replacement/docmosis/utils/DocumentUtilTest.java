package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ET3_FORM_CASE_DATA_FILE;

public final class DocumentUtilTest {

    private static final String TOP_LEVEL = "RESPONSE_TO_A_CLAIM";
    private static final String SECOND_LEVEL_ET3 = "ET3";
    private static final String SECOND_LEVEL_ET3_ATTACHMENT = "ET3 Attachment";
    private static final UploadedDocumentType VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM =
            ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class)
                    .getRespondentCollection().get(0).getValue().getEt3Form();
    private static final String UPLOADED_DOCUMENT_FILE_NAME = "Test Company - ET3 Response.pdf";
    private static final String ET3_FORM_ENGLISH_DESCRIPTION = "ET3 form English version";
    private static final String EMPLOYER_CONTRACT_CLAIM_TEST_DOCUMENT_NAME = "employer_contract_claim_test_document.docx";
    private static final String DUMMY_DOCUMENT_FILE_NAME = "dummy_document.docx";

    @Test
    void theConvertUploadedDocumentTypeToDocumentTypeItemWithLevels() {
        assertThat(DocumentUtil.convertUploadedDocumentTypeToDocumentTypeItemWithLevels(null,
                TOP_LEVEL, SECOND_LEVEL_ET3)).isNull();

        assertThat(DocumentUtil
                .convertUploadedDocumentTypeToDocumentTypeItemWithLevels(VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM,
                        null, SECOND_LEVEL_ET3)).isNull();

        assertThat(DocumentUtil
                .convertUploadedDocumentTypeToDocumentTypeItemWithLevels(VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM,
                        TOP_LEVEL, null)).isNull();

        DocumentTypeItem documentTypeItemET3 = DocumentUtil.convertUploadedDocumentTypeToDocumentTypeItemWithLevels(
                VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, TOP_LEVEL, SECOND_LEVEL_ET3);
        assertThat(documentTypeItemET3).isNotNull();
        assertThat(documentTypeItemET3.getValue().getTopLevelDocuments()).isEqualTo(TOP_LEVEL);
        assertThat(documentTypeItemET3.getValue().getResponseClaimDocuments()).isEqualTo(SECOND_LEVEL_ET3);
        assertThat(documentTypeItemET3.getValue().getUploadedDocument().getDocumentFilename())
                .isEqualTo(UPLOADED_DOCUMENT_FILE_NAME);

        DocumentTypeItem documentTypeItemET3Attachment =
                DocumentUtil.convertUploadedDocumentTypeToDocumentTypeItemWithLevels(
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
        assertDoesNotThrow(() -> DocumentUtil.setDocumentTypeItemLevels(null,
                TOP_LEVEL, SECOND_LEVEL_ET3));
        assertDoesNotThrow(() -> DocumentUtil.setDocumentTypeItemLevels(validDocumentTypeItemContestClaim,
                null, SECOND_LEVEL_ET3));
        assertDoesNotThrow(() -> DocumentUtil.setDocumentTypeItemLevels(validDocumentTypeItemContestClaim,
                TOP_LEVEL, null));

        DocumentUtil.setDocumentTypeItemLevels(validDocumentTypeItemContestClaim, TOP_LEVEL, SECOND_LEVEL_ET3);
        assertThat(validDocumentTypeItemContestClaim.getValue().getTopLevelDocuments()).isEqualTo(TOP_LEVEL);
        assertThat(validDocumentTypeItemContestClaim.getValue().getResponseClaimDocuments()).isEqualTo(SECOND_LEVEL_ET3);

        DocumentUtil.setDocumentTypeItemLevels(validDocumentTypeItemContestClaim,
                TOP_LEVEL, SECOND_LEVEL_ET3_ATTACHMENT);
        assertThat(validDocumentTypeItemContestClaim.getValue().getTopLevelDocuments()).isEqualTo(TOP_LEVEL);
        assertThat(validDocumentTypeItemContestClaim.getValue().getResponseClaimDocuments())
                .isEqualTo(SECOND_LEVEL_ET3_ATTACHMENT);
    }

    @Test
    void theAddUploadedDocumentTypeToDocumentTypeItems() {

        List<DocumentTypeItem> emptyDocumentTypeItems = new ArrayList<>();
        assertDoesNotThrow(() -> DocumentUtil.addUploadedDocumentTypeToDocumentTypeItems(null,
                VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, TOP_LEVEL, SECOND_LEVEL_ET3, ET3_FORM_ENGLISH_DESCRIPTION));
        assertDoesNotThrow(() -> DocumentUtil.addUploadedDocumentTypeToDocumentTypeItems(emptyDocumentTypeItems,
                null, TOP_LEVEL, SECOND_LEVEL_ET3, ET3_FORM_ENGLISH_DESCRIPTION));
        assertDoesNotThrow(() -> DocumentUtil.addUploadedDocumentTypeToDocumentTypeItems(emptyDocumentTypeItems,
                VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, null, SECOND_LEVEL_ET3, ET3_FORM_ENGLISH_DESCRIPTION));
        assertDoesNotThrow(() -> DocumentUtil.addUploadedDocumentTypeToDocumentTypeItems(emptyDocumentTypeItems,
                VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, TOP_LEVEL, null, ET3_FORM_ENGLISH_DESCRIPTION));
        assertDoesNotThrow(() -> DocumentUtil.addUploadedDocumentTypeToDocumentTypeItems(emptyDocumentTypeItems,
                VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, TOP_LEVEL, SECOND_LEVEL_ET3, null));

        DocumentUtil.addUploadedDocumentTypeToDocumentTypeItems(emptyDocumentTypeItems,
                VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, TOP_LEVEL, SECOND_LEVEL_ET3, ET3_FORM_ENGLISH_DESCRIPTION);
        assertThat(emptyDocumentTypeItems).isNotEmpty();
        assertThat(emptyDocumentTypeItems.get(0).getValue().getTopLevelDocuments()).isEqualTo(TOP_LEVEL);
        assertThat(emptyDocumentTypeItems.get(0).getValue().getResponseClaimDocuments()).isEqualTo(SECOND_LEVEL_ET3);
        assertThat(emptyDocumentTypeItems.get(0).getValue().getUploadedDocument().getDocumentFilename())
                .isEqualTo(UPLOADED_DOCUMENT_FILE_NAME);

        // Tests when static method convertUploadedDocumentTypeToDocumentTypeItemWithLevels returns null.
        try (MockedStatic<DocumentUtil> mocked = mockStatic(DocumentUtil.class)) {
            mocked.when(() -> DocumentUtil.convertUploadedDocumentTypeToDocumentTypeItemWithLevels(
                    VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, TOP_LEVEL, SECOND_LEVEL_ET3)).thenReturn(null);
            List<DocumentTypeItem> emptyDocumentTypeItems2 = new ArrayList<>();
            mocked.when(() -> DocumentUtil.addUploadedDocumentTypeToDocumentTypeItems(emptyDocumentTypeItems2,
                    VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, TOP_LEVEL, SECOND_LEVEL_ET3, ET3_FORM_ENGLISH_DESCRIPTION))
                    .thenCallRealMethod();
            DocumentUtil.addUploadedDocumentTypeToDocumentTypeItems(emptyDocumentTypeItems2,
                    VALID_UPLOADED_DOCUMENT_TYPE_ET3_FORM, TOP_LEVEL, SECOND_LEVEL_ET3, ET3_FORM_ENGLISH_DESCRIPTION);
            assertThat(emptyDocumentTypeItems2).isEmpty();
        }
    }

    @Test
    void theContainsDocumentWithName() {
        DocumentTypeItem validDocumentTypeItemContestClaim =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class)
                        .getRespondentCollection().get(0).getValue().getEt3ResponseContestClaimDocument().get(0);
        List<DocumentTypeItem> emptyDocumentTypeItems = new ArrayList<>();
        assertThat(DocumentUtil.containsDocumentWithName(
                        emptyDocumentTypeItems, EMPLOYER_CONTRACT_CLAIM_TEST_DOCUMENT_NAME)).isFalse();

        List<DocumentTypeItem> validDocumentTypeItems = List.of(validDocumentTypeItemContestClaim);
        assertThat(DocumentUtil.containsDocumentWithName(validDocumentTypeItems, null)).isFalse();

        assertThat(DocumentUtil.containsDocumentWithName(
                validDocumentTypeItems, EMPLOYER_CONTRACT_CLAIM_TEST_DOCUMENT_NAME)).isTrue();

        validDocumentTypeItemContestClaim.getValue().getUploadedDocument().setDocumentFilename(
                DUMMY_DOCUMENT_FILE_NAME);

        assertThat(DocumentUtil.containsDocumentWithName(
                validDocumentTypeItems, EMPLOYER_CONTRACT_CLAIM_TEST_DOCUMENT_NAME)).isFalse();

        validDocumentTypeItemContestClaim.getValue().getUploadedDocument().setDocumentFilename(null);
        assertThat(DocumentUtil.containsDocumentWithName(
                validDocumentTypeItems, EMPLOYER_CONTRACT_CLAIM_TEST_DOCUMENT_NAME)).isFalse();

        validDocumentTypeItemContestClaim.getValue().setUploadedDocument(null);
        assertThat(DocumentUtil.containsDocumentWithName(
                validDocumentTypeItems, EMPLOYER_CONTRACT_CLAIM_TEST_DOCUMENT_NAME)).isFalse();

        validDocumentTypeItemContestClaim.setValue(null);
        assertThat(DocumentUtil.containsDocumentWithName(
                validDocumentTypeItems, EMPLOYER_CONTRACT_CLAIM_TEST_DOCUMENT_NAME)).isFalse();
    }

    @Test
    void theAddDocumentIfNotExists() {
        DocumentTypeItem validDocumentTypeItemContestClaim = ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class)
                        .getRespondentCollection().get(0).getValue().getEt3ResponseContestClaimDocument().get(0);
        assertDoesNotThrow(() -> DocumentUtil.addDocumentIfNotExists(null, validDocumentTypeItemContestClaim));

        List<DocumentTypeItem> emptyDocumentTypeItems = new ArrayList<>();
        assertDoesNotThrow(() -> DocumentUtil.addDocumentIfNotExists(emptyDocumentTypeItems, null));

        DocumentUtil.addDocumentIfNotExists(emptyDocumentTypeItems, validDocumentTypeItemContestClaim);
        assertThat(emptyDocumentTypeItems).hasSize(1);

        validDocumentTypeItemContestClaim.getValue().getUploadedDocument().setDocumentFilename(null);
        assertDoesNotThrow(() -> DocumentUtil.addDocumentIfNotExists(emptyDocumentTypeItems,
                validDocumentTypeItemContestClaim));

        validDocumentTypeItemContestClaim.getValue().getUploadedDocument().setDocumentFilename(
                DUMMY_DOCUMENT_FILE_NAME);
        assertDoesNotThrow(() -> DocumentUtil.addDocumentIfNotExists(emptyDocumentTypeItems,
                validDocumentTypeItemContestClaim));

        validDocumentTypeItemContestClaim.getValue().setUploadedDocument(null);
        assertDoesNotThrow(() -> DocumentUtil.addDocumentIfNotExists(emptyDocumentTypeItems,
                validDocumentTypeItemContestClaim));
        validDocumentTypeItemContestClaim.setValue(null);
        assertDoesNotThrow(() -> DocumentUtil.addDocumentIfNotExists(emptyDocumentTypeItems,
                validDocumentTypeItemContestClaim));
    }
}
