package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

@SuppressWarnings({"PMD.LinguisticNaming"})
@SuperBuilder
@Data
public class RespondentTellSomethingElseData {

    @JsonProperty("caseNumber")
    private String caseNumber;
    @JsonProperty("resTseSelectApplication")
    private String resTseSelectApplication;
    @JsonProperty("resTseDocument")
    private UploadedDocumentType resTseDocument;
    @JsonProperty("resTseTextBox")
    private String resTseTextBox;

}
