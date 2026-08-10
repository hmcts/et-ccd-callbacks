package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "JurisdictionCode", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class VettingJurisdictionCodesType {

    @CCD(
            label = "Jurisdiction code",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_jurisdictionCodes"
    )
    @JsonProperty("et1VettingJurCodeList")
    private String et1VettingJurCodeList;
}
