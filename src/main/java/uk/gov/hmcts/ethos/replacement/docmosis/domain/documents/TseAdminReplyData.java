package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * This object captures information related to the TseAdminReply object during
 * the document creation event for Docmosis.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TseAdminReplyData extends TseReplyData {
    @JsonProperty("responseTitle")
    private String responseTitle;
    @JsonProperty("responseAdditionalInfo")
    private String responseAdditionalInfo;
    @JsonProperty("isCmoOrRequest")
    private String cmoOrRequest;
    @JsonProperty("cmoMadeBy")
    private String cmoMadeBy;
    @JsonProperty("cmoEnterFullName")
    private String cmoEnterFullName;
    @JsonProperty("cmoIsResponseRequired")
    private String cmoIsResponseRequired;
    @JsonProperty("cmoSelectPartyRespond")
    private String cmoSelectPartyRespond;
    @JsonProperty("requestMadeBy")
    private String requestMadeBy;
    @JsonProperty("requestEnterFullName")
    private String requestEnterFullName;
    @JsonProperty("requestIsResponseRequired")
    private String requestIsResponseRequired;
    @JsonProperty("requestSelectPartyRespond")
    private String requestSelectPartyRespond;
    @JsonProperty("selectPartyNotify")
    private String selectPartyNotify;
}
