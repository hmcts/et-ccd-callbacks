package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "acceptOrRejectCase", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CasePreAcceptType {

    @CCD(label = "Case Accepted?", typeOverride = FieldType.YesOrNo)
    @JsonProperty("caseAccepted")
    private String caseAccepted;
    @CCD(
            label = "Date Accepted",
            showCondition = "caseAccepted=\"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.Date
    )
    @JsonProperty("dateAccepted")
    private String dateAccepted;
    @CCD(
            label = "Date Rejected",
            showCondition = "caseAccepted=\"No\"",
            retainHiddenValue = true,
            typeOverride = FieldType.Date
    )
    @JsonProperty("dateRejected")
    private String dateRejected;
    @CCD(
            label = "Reason for the rejection",
            showCondition = "caseAccepted=\"No\"",
            retainHiddenValue = true,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_PreAcceptanceResponse"
    )
    @JsonProperty("rejectReason")
    private List<String> rejectReason;

}
