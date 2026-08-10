package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.et.common.model.ccd.DocumentUpload;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "tseAdminDecision", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class TseAdminRecordDecisionType {

    @CCD(label = "Decision date")
    @JsonProperty("date")
    private String date;
    @CCD(label = "Enter notification title")
    @JsonProperty("enterNotificationTitle")
    private String enterNotificationTitle;
    @CCD(label = "Decision")
    @JsonProperty("decision")
    private String decision;
    @CCD(label = "Decision details", typeOverride = FieldType.TextArea)
    @JsonProperty("decisionDetails")
    private String decisionDetails;
    @CCD(label = "Type of decision")
    @JsonProperty("typeOfDecision")
    private String typeOfDecision;
    @CCD(label = "Is a response to the tribunal required?")
    @JsonProperty("isResponseRequired")
    private String isResponseRequired;
    @CCD(label = "Select the party or parties who must respond")
    @JsonProperty("selectPartyRespond")
    private String selectPartyRespond;
    @CCD(label = "Additional information", typeOverride = FieldType.TextArea)
    @JsonProperty("additionalInformation")
    private String additionalInformation;
    @CCD(label = "Add document", typeOverride = FieldType.Collection, typeParameterOverride = "DocumentUpload")
    @JsonProperty("responseRequiredDoc")
    private List<GenericTypeItem<DocumentUpload>> responseRequiredDoc;
    @CCD(label = "Decision was made by")
    @JsonProperty("decisionMadeBy")
    private String decisionMadeBy;
    @CCD(label = "Enter their full name")
    @JsonProperty("decisionMadeByFullName")
    private String decisionMadeByFullName;
    @CCD(label = "Select the party or parties to notify")
    @JsonProperty("selectPartyNotify")
    private String selectPartyNotify;
    @CCD(label = "Decision State", showCondition = "date=\"dummy\"")
    @JsonProperty("decisionState")
    private String decisionState;
}
