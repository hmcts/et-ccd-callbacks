package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.util.UUID;

public class DocumentFixtures {
    public DocumentFixtures() {
        // Access through static methods
    }

    public static UploadedDocumentType getUploadedDocumentType(String documentName) {
        String uuid = UUID.randomUUID().toString();
        return UploadedDocumentType.builder()
                .documentBinaryUrl("http://dm-store:8080/" + uuid + "/binary")
                .documentFilename(documentName)
                .documentUrl("http://dm-store:8080/" + uuid)
                .build();
    }

    public static UploadedDocumentType getUploadedDocumentType() {
        return getUploadedDocumentType("test document.docx");
    }

    public static DocumentType getDocumentType() {
        return DocumentType.from(getUploadedDocumentType());
    }

    public static DocumentType getDocumentType(String documentName) {
        return DocumentType.from(getUploadedDocumentType(documentName));
    }
}
