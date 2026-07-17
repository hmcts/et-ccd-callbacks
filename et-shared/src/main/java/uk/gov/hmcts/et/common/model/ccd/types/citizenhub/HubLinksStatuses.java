package uk.gov.hmcts.et.common.model.ccd.types.citizenhub;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class HubLinksStatuses {
    @CCD(label = " ")
    @JsonProperty("personalDetails")
    private String personalDetails;
    @CCD(label = " ")
    @JsonProperty("et1ClaimForm")
    private String et1ClaimForm;
    @CCD(label = " ")
    @JsonProperty("respondentResponse")
    private String respondentResponse;
    @CCD(label = " ")
    @JsonProperty("hearingDetails")
    private String hearingDetails;
    @CCD(label = " ")
    @JsonProperty("requestsAndApplications")
    private String requestsAndApplications;
    @CCD(label = " ")
    @JsonProperty("respondentApplications")
    private String respondentApplications;
    @CCD(label = " ")
    @JsonProperty("contactTribunal")
    private String contactTribunal;
    @CCD(label = " ")
    @JsonProperty("tribunalOrders")
    private String tribunalOrders;
    @CCD(label = " ")
    @JsonProperty("tribunalJudgements")
    private String tribunalJudgements;
    @CCD(label = " ")
    @JsonProperty("documents")
    private String documents;
}
