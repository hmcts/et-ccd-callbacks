package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;

import java.util.List;

/**
 * This object captures information related to the TseReply object during creation event for docmosis.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TseReplyData {
    @JsonProperty("caseNumber")
    private String caseNumber;
    @JsonProperty("type")
    private String type;
    @JsonProperty("responseDate")
    private String responseDate;
    @JsonProperty("supportingYesNo")
    private String supportingYesNo;
    @JsonProperty("documentCollection")
    private List<GenericTypeItem<DocumentType>> documentCollection;
    @JsonProperty("copy")
    private String copy;
    @JsonProperty("response")
    private String response;
    // Called party to represent the party who is replying to the application, but all the templates use respondentParty
    @JsonProperty("respondentParty")
    private String party;
}
