package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

/**
 * Contains helper methods to build a UploadedDocumentType object. Each method returns an instance of itself to aid with
 * chaining method calls to build the object.
 */
public class UploadedDocumentBuilder {
    private final UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();

    public static UploadedDocumentBuilder builder() {
        return new UploadedDocumentBuilder();
    }

    public UploadedDocumentType build() {
        return uploadedDocumentType;
    }

    public UploadedDocumentBuilder withFilename(String filename) {
        uploadedDocumentType.setDocumentFilename(filename);
        return this;
    }

    public UploadedDocumentBuilder withUuid(String uuid) {
        uploadedDocumentType.setDocumentUrl("http://dm-store:8080/documents/" + uuid);
        uploadedDocumentType.setDocumentBinaryUrl(uploadedDocumentType.getDocumentUrl() + "/binary");
        return this;
    }

    public UploadedDocumentBuilder withUrl(String url) {
        uploadedDocumentType.setDocumentUrl(url);
        uploadedDocumentType.setDocumentBinaryUrl(url + "/binary");
        return this;
    }
}