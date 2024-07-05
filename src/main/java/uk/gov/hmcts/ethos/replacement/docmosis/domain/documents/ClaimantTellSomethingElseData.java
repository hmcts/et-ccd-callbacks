package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public class ClaimantTellSomethingElseData {

    @JsonProperty("claimTseApplicant")
    private String claimTseApplicant;
    @JsonProperty("caseNumber")
    private String caseNumber;
    @JsonProperty("claimTseSelectApplication")
    private String claimTseSelectApplication;
    @JsonProperty("claimTseApplicationDate")
    private String claimTseApplicationDate;
    @JsonProperty("claimTseDocument")
    private String claimTseDocument;
    @JsonProperty("claimTseTextBox")
    private String claimTseTextBox;
    @JsonProperty("claimTseCopyToOtherPartyYesOrNo")
    private String claimTseCopyToOtherPartyYesOrNo;
    @JsonProperty("claimTseCopyToOtherPartyTextArea")
    private String claimTseCopyToOtherPartyTextArea;
}
