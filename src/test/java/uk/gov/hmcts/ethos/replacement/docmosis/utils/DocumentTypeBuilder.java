package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

/**
 * Contains helper methods to build a DocumentType object. Each method returns an instance of itself to aid with
 * chaining method calls to build the object.
 */
public class DocumentTypeBuilder {
    private final DocumentType documentType = new DocumentType();

    public static DocumentTypeBuilder builder() {
        return new DocumentTypeBuilder();
    }

    public DocumentType build() {
        return documentType;
    }

    public DocumentTypeBuilder withUploadedDocument(UploadedDocumentType uploadedDocument) {
        documentType.setUploadedDocument(uploadedDocument);
        return this;
    }

    public DocumentTypeBuilder withUploadedDocument(String filename, String uuid) {
        UploadedDocumentType build = UploadedDocumentBuilder.builder().withFilename(filename).withUuid(uuid).build();
        documentType.setUploadedDocument(build);
        return this;
    }
    
    public DocumentTypeBuilder withShortDescription(String shortDescription) {
        documentType.setShortDescription(shortDescription);
        return this;
    }

    public DocumentTypeBuilder withType(String type) {
        documentType.setTypeOfDocument(type);
        return this;
    }

    public DocumentTypeBuilder withOwnerDocument(String ownerDocument) {
        documentType.setOwnerDocument(ownerDocument);
        return this;
    }

    public DocumentTypeBuilder withCreationDate(String creationDate) {
        documentType.setCreationDate(creationDate);
        return this;
    }
}