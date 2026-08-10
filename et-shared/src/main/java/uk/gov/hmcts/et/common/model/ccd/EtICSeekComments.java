package uk.gov.hmcts.et.common.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "etICHearingNotListedSeekComments", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EtICSeekComments {
    @CCD(
            label = "Type of Video hearing",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICTypeOfCvpHearing"
    )
    @JsonProperty("etICTypeOfCvpHearing")
    private List<String> etICTypeOfCvpHearing;
    @CCD(
            label = "Give details of final hearing",
            showCondition = "etICTypeOfCvpHearing CONTAINS \"Final\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICFinalHearingDetails")
    private String etICFinalHearingDetails;
    @CCD(
            label = "Give details of preliminary hearing",
            showCondition = "etICTypeOfCvpHearing CONTAINS \"Preliminary\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICPrelimHearingDetails")
    private String etICPrelimHearingDetails;
    @CCD(
            label = "Should the case be listed for a private preliminary hearing?",
            showCondition = "etICTypeOfCvpHearing CONTAINS \"Preliminary\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("etICPrelimHearingYesNo")
    private String etICPrelimHearingYesNo;
}
