package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;

import java.util.UUID;

public final class DocumentUtil {

    private DocumentUtil() {
        // Utility classes should not have a public or default constructor.
    }

    public static ListTypeItem<DocumentType> generateUploadedDocumentListFromDocumentList(
            ListTypeItem<DocumentType> documentList) {

        ListTypeItem<DocumentType> uploadedDocumentList = new ListTypeItem<DocumentType>();
        documentList.forEach(doc -> {
            TypeItem<DocumentType> genTypeItems = new TypeItem<>();
            DocumentType docType = new DocumentType();
            docType.setUploadedDocument(doc.getValue().getUploadedDocument());
            docType.getUploadedDocument().setDocumentBinaryUrl(doc.getValue().getUploadedDocument().getDocumentUrl());

            genTypeItems.setId(doc.getId() != null ? doc.getId() : UUID.randomUUID().toString());
            genTypeItems.setValue(docType);
            uploadedDocumentList.add(genTypeItems);
        });

        return uploadedDocumentList;
    }
}
