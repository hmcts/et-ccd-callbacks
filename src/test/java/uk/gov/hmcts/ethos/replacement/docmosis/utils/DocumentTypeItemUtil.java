package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;

import java.util.List;

public class DocumentTypeItemUtil {
    private DocumentTypeItemUtil() {
    }

    public static List<GenericTypeItem<DocumentType>> createSupportingMaterial() {
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId("1234");
        documentTypeItem.setValue(DocumentTypeBuilder.builder().withUploadedDocument("image.png", "1234").build());
        return List.of(documentTypeItem);
    }
}
