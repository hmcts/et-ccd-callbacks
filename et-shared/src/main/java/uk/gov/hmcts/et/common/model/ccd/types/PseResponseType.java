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

@ComplexType(name = "pseRespondCollection", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class PseResponseType {

    @CCD(label = "Response date")
    @JsonProperty("date")
    private String date;
    @CCD(label = "Response from")
    @JsonProperty("from")
    private String from;
    @CCD(label = "Response from Idam id", showCondition = "date=\"dummy\"")
    @JsonProperty("fromIdamId")
    private String fromIdamId;
    @CCD(label = " ", showCondition = "date=\"dummy\"")
    @JsonProperty("author")
    private String author;

    // Response
    @CCD(label = "What's your response to the tribunal?", typeOverride = FieldType.TextArea)
    @JsonProperty("response")
    private String response;
    @CCD(label = "Has supporting material", showCondition = "date=\"dummy\"")
    @JsonProperty("hasSupportingMaterial")
    private String hasSupportingMaterial;
    @CCD(label = "Supporting material", typeOverride = FieldType.Collection, typeParameterOverride = "DocumentUpload")
    @JsonProperty("supportingMaterial")
    private List<GenericTypeItem<DocumentUpload>> supportingMaterial;
    @CCD(label = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?")
    @JsonProperty("copyToOtherParty")
    private String copyToOtherParty;
    @CCD(label = " ", showCondition = "copyToOtherParty=\"dummy\"", typeOverride = FieldType.TextArea)
    @JsonProperty("copyNoGiveDetails")
    private String copyNoGiveDetails;
    @CCD(label = " ", showCondition = "date=\"dummy\"")
    @JsonProperty("responseState")
    private String responseState;

    // Work Allocation enablers
    @CCD(label = " ", showCondition = "copyToOtherParty = \"dummy\"", retainHiddenValue = true)
    @JsonProperty("dateTime")
    private String dateTime;
    @CCD(label = " ", showCondition = "copyToOtherParty=\"dummy\"", retainHiddenValue = true)
    @JsonProperty("isECC")
    private String isECC;
}
