package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.util.UUID;

public final class DocumentFixtures {

    public static final String MOCK_DM_STORE_URL = "http://dm-store:8080/%s";

    private DocumentFixtures() {
        // Access through static methods
    }

    public static UploadedDocumentType getUploadedDocumentType(String documentName) {
        String uuid = UUID.randomUUID().toString();
        String url = String.format(MOCK_DM_STORE_URL, uuid);
        return UploadedDocumentType.builder()
                .documentBinaryUrl(url)
                .documentFilename(documentName)
                .documentUrl(url)
                .build();
    }

    public static UploadedDocumentType getUploadedDocumentType() {
        return getUploadedDocumentType("test document.docx");
    }

    public static DocumentType getDocumentType() {
        return DocumentType.from(getUploadedDocumentType());
    }

    public static DocumentType getDocumentType(String documentName, String type) {
        DocumentType from = DocumentType.from(getUploadedDocumentType(documentName));
        from.setTypeOfDocument(type);
        return from;
    }

    public static DocumentTypeItem getDocumentTypeItem(String documentName, String type) {
        DocumentTypeItem item = new DocumentTypeItem();
        item.setId(UUID.randomUUID().toString());
        item.setValue(getDocumentType(documentName, type));
        return item;
    }
}
