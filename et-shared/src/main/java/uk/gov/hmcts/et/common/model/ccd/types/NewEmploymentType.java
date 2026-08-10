package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "NewEmploymentDetails", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class NewEmploymentType {

    @CCD(label = "Have you got a new job", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("new_job")
    private String newJob;
    @CCD(label = "Employed from", searchable = false, typeOverride = FieldType.Date)
    @JsonProperty("newly_employed_from")
    private String newlyEmployedFrom;
    @CCD(label = "Pay before tax", searchable = false, typeOverride = FieldType.Number)
    @JsonProperty("new_pay_before_tax")
    private String newPayBeforeTax;
    @CCD(
            label = "Is this your weekly, monthly or annual pay",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_pay_cycle"
    )
    @JsonProperty("new_job_pay_interval")
    private String newJobPayInterval;
}
