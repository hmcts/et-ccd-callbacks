package uk.gov.hmcts.et.common.model.ccd.types.et3links;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ET3CaseDetailsLinksStatuses {

    @CCD(label = " ", searchable = false)
    @JsonProperty("personalDetails")
    private String personalDetails;
    @CCD(label = " ", searchable = false)
    @JsonProperty("et1ClaimForm")
    private String et1ClaimForm;
    @CCD(label = " ", searchable = false)
    @JsonProperty("respondentResponse")
    private String respondentResponse;
    @CCD(label = " ", searchable = false)
    @JsonProperty("hearingDetails")
    private String hearingDetails;
    @CCD(label = " ", searchable = false)
    @JsonProperty("respondentRequestsAndApplications")
    private String respondentRequestsAndApplications;
    @CCD(label = " ", searchable = false)
    @JsonProperty("claimantApplications")
    private String claimantApplications;
    @CCD(label = " ", searchable = false)
    @JsonProperty("otherRespondentApplications")
    private String otherRespondentApplications;
    @CCD(label = " ", searchable = false)
    @JsonProperty("contactTribunal")
    private String contactTribunal;
    @CCD(label = " ", searchable = false)
    @JsonProperty("tribunalOrders")
    private String tribunalOrders;
    @CCD(label = " ", searchable = false)
    @JsonProperty("tribunalJudgements")
    private String tribunalJudgements;
    @CCD(label = " ", searchable = false)
    @JsonProperty("documents")
    private String documents;

}
