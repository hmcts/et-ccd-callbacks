package uk.gov.hmcts.et.common.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EtICFurtherInfoAnswers {
    @CCD(
            label = "Give details to include in the letter",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICFurtherInformationGiveDetails")
    private String etICFurtherInformationGiveDetails;
    @CCD(label = "How much time to comply? (days)", hint = "Give details", searchable = false)
    @JsonProperty("etICFurtherInformationTimeToComply")
    private String etICFurtherInformationTimeToComply;
}


