package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoticeOfChangeAnswers {
    @CCD(label = " ")
    @JsonProperty("respondentName")
    private final String respondentName;

    @CCD(label = " ")
    @JsonProperty("claimantFirstName")
    private final String claimantFirstName;
    
    @CCD(label = " ")
    @JsonProperty("claimantLastName")
    private final String claimantLastName;
}
