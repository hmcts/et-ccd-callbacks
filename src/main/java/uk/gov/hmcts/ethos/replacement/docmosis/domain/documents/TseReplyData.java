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
    @JsonProperty("applicationNumber")
    private String applicationNumber;
    @JsonProperty("type")
    private String type;
    @JsonProperty("responseFrom")
    private String responseFrom;
    @JsonProperty("responseDate")
    private String responseDate;
    @JsonProperty("response")
    private String response;
    @JsonProperty("supportingYesNo")
    private String supportingYesNo;
    @JsonProperty("documentCollection")
    private List<GenericTypeItem<DocumentType>> documentCollection;
    @JsonProperty("copy")
    private String copy;
    @JsonProperty("respondentParty")
    private String respondentParty;
}
