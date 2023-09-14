package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * This object captures information related to the TseAdminDecisionRecord object during
 * the doc creation event for Docmosis.
 */
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@Data
public class TseAdminDecisionRecordData extends TseReplyData {
    @JsonProperty("notificationTitle")
    private String notificationTitle;
    @JsonProperty("tseAdminDecision")
    private String tseAdminDecision;
    @JsonProperty("tseAdminTypeOfDecision")
    private String tseAdminTypeOfDecision;
    @JsonProperty("tseAdminIsResponseRequired")
    private String tseAdminIsResponseRequired;
    @JsonProperty("tseAdminSelectPartyRespond")
    private String tseAdminSelectPartyRespond;
    @JsonProperty("tseAdminAdditionalInformation")
    private String tseAdminAdditionalInformation;
    @JsonProperty("tseAdminDecisionMadeBy")
    private String tseAdminDecisionMadeBy;
    @JsonProperty("tseAdminDecisionMadeByFullName")
    private String tseAdminDecisionMadeByFullName;
    @JsonProperty("tseAdminSelectPartyNotify")
    private String tseAdminSelectPartyNotify;
}