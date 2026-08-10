package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "RestrictedCase", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class RestrictedReportingType {

    @CCD(label = "Requested By", typeOverride = FieldType.DynamicList)
    @JsonProperty("dynamicRequestedBy")
    private DynamicFixedListType dynamicRequestedBy;
    @CCD(
            label = "Requested By",
            showCondition = "excludedRegister=\"dummy\" OR dynamicRequestedBy!=\"*\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_RestrictedRequestedBy"
    )
    @JsonProperty("requestedBy")
    private String requestedBy;
    @CCD(label = "Date Ceased", typeOverride = FieldType.Date)
    @JsonProperty("dateCeased")
    private String dateCeased;
    @CCD(label = "Rule 49(3)(d) Applies", typeOverride = FieldType.YesOrNo)
    @JsonProperty("imposed")
    private String imposed;
    @CCD(label = "Rule 49(3)(b) Applies", typeOverride = FieldType.YesOrNo)
    @JsonProperty("rule503b")
    private String rule503b;
    @CCD(
            label = "Excluded from Register",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_RestrictedExcludedRegister"
    )
    @JsonProperty("excludedRegister")
    private String excludedRegister;
    @CCD(label = "Start Date", typeOverride = FieldType.Date)
    @JsonProperty("startDate")
    private String startDate;
    @CCD(label = "Names not for public release", typeOverride = FieldType.TextArea)
    @JsonProperty("excludedNames")
    private String excludedNames;
    @CCD(label = "Deleted from Physical Register", typeOverride = FieldType.YesOrNo)
    @JsonProperty("deletedPhyRegister")
    private String deletedPhyRegister;

}
