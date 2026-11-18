package uk.gov.hmcts.et.common.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.et.common.model.generic.GenericRequest;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CCDRequest extends GenericRequest {

    @JsonProperty("case_details")
    private CaseDetails caseDetails;

    @JsonProperty("case_details_before")
    private CaseDetails caseDetailsBefore;

    public CCDRequest(CaseDetails caseDetails) {
        this.caseDetails = caseDetails;
    }
}
