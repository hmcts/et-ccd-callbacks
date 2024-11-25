package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class DocumentUtil {

    private DocumentUtil() {
        // Utility classes should not have a public or default constructor.
    }

    public static List<GenericTypeItem<DocumentType>> generateUploadedDocumentListFromDocumentList(
            List<GenericTypeItem<DocumentType>> documentList) {

        List<GenericTypeItem<DocumentType>> uploadedDocumentList = new ArrayList<>();
        documentList.forEach(doc -> {
            GenericTypeItem<DocumentType> genTypeItems = new GenericTypeItem<>();
            DocumentType docType = new DocumentType();
            docType.setUploadedDocument(doc.getValue().getUploadedDocument());
            docType.getUploadedDocument().setDocumentBinaryUrl(
                    doc.getValue().getUploadedDocument().getDocumentBinaryUrl());

            genTypeItems.setId(doc.getId() != null ? doc.getId() : UUID.randomUUID().toString());
            genTypeItems.setValue(docType);
            uploadedDocumentList.add(genTypeItems);
        });

        return uploadedDocumentList;
    }
}
