package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;

import java.util.List;

/**
 * This object captures information related to the TseDecision object during creation event for docmosis.
 */
@SuperBuilder
@Data
public class TseDecisionData {
    @JsonProperty("caseNumber")
    private String caseNumber;
    @JsonProperty("notificationTitle")
    private String notificationTitle;
    @JsonProperty("decision")
    private String decision;
    @JsonProperty("decisionDetails")
    private String decisionDetails;
    @JsonProperty("typeOfDecision")
    private String typeOfDecision;
    @JsonProperty("responseRequired")
    private String responseRequired;
    @JsonProperty("selectedRespondents")
    private String selectedRespondents;
    @JsonProperty("additionalInformation")
    private String additionalInformation;
    @JsonProperty("supportingMaterial")
    private List<DocumentType> supportingMaterial;
    @JsonProperty("decisionMadeBy")
    private String decisionMadeBy;
    @JsonProperty("decisionMakerFullName")
    private String decisionMakerFullName;
    @JsonProperty("notificationParties")
    private String notificationParties;

}
