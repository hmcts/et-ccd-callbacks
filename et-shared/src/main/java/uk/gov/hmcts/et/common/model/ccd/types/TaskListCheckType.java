package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "TaskListCheck", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TaskListCheckType {

    @CCD(label = "Have you completed this section?", typeOverride = FieldType.YesOrNo)
    @JsonProperty("personalDetailsCheck")
    private String personalDetailsCheck;
    @CCD(label = "Have you completed this section?", typeOverride = FieldType.YesOrNo)
    @JsonProperty("employmentAndRespondentCheck")
    private String employmentAndRespondentCheck;
    @CCD(label = "Have you completed this section?", typeOverride = FieldType.YesOrNo)
    @JsonProperty("claimDetailsCheck")
    private String claimDetailsCheck;
}
