package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.Data;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

@Data
public class TSEApplicationTypeData {

    private UploadedDocumentType uploadedTseDocument;
    private String selectedTextBox;

    public TSEApplicationTypeData(UploadedDocumentType uploadedTseDocument, String selectedTextBox) {
        this.uploadedTseDocument = uploadedTseDocument;
        this.selectedTextBox = selectedTextBox;
    }

}
