package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.Data;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

@Data
public class RespondentTSEApplicationTypeData {

    private UploadedDocumentType resTseDocument;
    private String selectedTextBox;

    public RespondentTSEApplicationTypeData(UploadedDocumentType resTseDocument, String selectedTextBox) {
        this.resTseDocument = resTseDocument;
        this.selectedTextBox = selectedTextBox;
    }

}
