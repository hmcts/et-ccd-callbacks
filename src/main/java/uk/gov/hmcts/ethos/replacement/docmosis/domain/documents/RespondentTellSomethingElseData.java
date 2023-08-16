package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public class RespondentTellSomethingElseData {

    @JsonProperty("resTseApplicant")
    private String resTseApplicant;
    @JsonProperty("caseNumber")
    private String caseNumber;
    @JsonProperty("resTseSelectApplication")
    private String resTseSelectApplication;
    @JsonProperty("resTseApplicationDate")
    private String resTseApplicationDate;
    @JsonProperty("resTseDocument")
    private String resTseDocument;
    @JsonProperty("resTseTextBox")
    private String resTseTextBox;

}
