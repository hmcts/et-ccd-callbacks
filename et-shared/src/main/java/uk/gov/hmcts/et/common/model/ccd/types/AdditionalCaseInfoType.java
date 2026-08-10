package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "AdditionalCaseDetails", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AdditionalCaseInfoType {

    @CCD(label = "Live appeal?", typeOverride = FieldType.YesOrNo)
    @JsonProperty("additional_live_appeal")
    private String additionalLiveAppeal;
    @CCD(label = "Sensitive case?", typeOverride = FieldType.YesOrNo)
    @JsonProperty("additional_sensitive")
    private String additionalSensitive;
    @CCD(label = "Do not postpone", typeOverride = FieldType.YesOrNo)
    @JsonProperty("doNotPostpone")
    private String doNotPostpone;
    @CCD(label = "Digital File?", typeOverride = FieldType.YesOrNo)
    @JsonProperty("digitalFile")
    private String digitalFile;
    @CCD(label = "Reasonable Adjustment", typeOverride = FieldType.YesOrNo)
    @JsonProperty("reasonableAdjustment")
    private String reasonableAdjustment;
    @CCD(label = "Speak to REJ", typeOverride = FieldType.YesOrNo)
    @JsonProperty("interventionRequired")
    private String interventionRequired;
    @CCD(label = "Reserved to Judge", typeOverride = FieldType.YesOrNo)
    @JsonProperty("reservedToJudge")
    private String reservedToJudge;
}
