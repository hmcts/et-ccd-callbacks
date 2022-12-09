package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RespondentTellSomethingElseData {

    @JsonProperty("caseNumber")
    private String caseNumber;
    @JsonProperty("resTseSelectApplication")
    private String resTseSelectApplication;
    @JsonProperty("resTseDocument")
    private String resTseDocument;
    @JsonProperty("resTseTextBox")
    private String resTseTextBox;

}
