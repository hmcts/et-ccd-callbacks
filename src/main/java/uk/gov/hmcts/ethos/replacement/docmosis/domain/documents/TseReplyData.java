package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.ccd.items.TypeItem;
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
    private List<TypeItem<DocumentType>> documentCollection;
    @JsonProperty("copy")
    private String copy;
    @JsonProperty("response")
    private String response;
    @JsonProperty("respondentParty")
    private String respondentParty;
}
