package uk.gov.hmcts.et.common.model.ccd.types.et3links;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ET3HubLinksStatuses {

    @CCD(label = " ", searchable = false)
    @JsonProperty("contactDetails")
    private String contactDetails;
    @CCD(label = " ", searchable = false)
    @JsonProperty("employerDetails")
    private String employerDetails;
    @CCD(label = " ", searchable = false)
    @JsonProperty("conciliationAndEmployeeDetails")
    private String conciliationAndEmployeeDetails;
    @CCD(label = " ", searchable = false)
    @JsonProperty("payPensionBenefitDetails")
    private String payPensionBenefitDetails;
    @CCD(label = " ", searchable = false)
    @JsonProperty("contestClaim")
    private String contestClaim;
    @CCD(label = " ", searchable = false)
    @JsonProperty("employersContractClaim")
    private String employersContractClaim;
    @CCD(label = " ", searchable = false)
    @JsonProperty("checkYorAnswers")
    private String checkYorAnswers;

}
