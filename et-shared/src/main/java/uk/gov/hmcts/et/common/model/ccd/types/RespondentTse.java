package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class RespondentTse {
    @CCD(label = "Respondent id")
    @JsonProperty("respondentIdamId")
    private String respondentIdamId;
    @CCD(label = " ")
    @JsonProperty("contactApplicationType")
    private String contactApplicationType;
    @CCD(ignore = true)
    @JsonProperty("contactApplicationClaimantType")
    private String contactApplicationClaimantType;
    @CCD(label = " ")
    @JsonProperty("contactApplicationText")
    private String contactApplicationText;
    @CCD(label = " ", categoryID = "C4", typeOverride = FieldType.Document)
    @JsonProperty("contactApplicationFile")
    private UploadedDocumentType contactApplicationFile;
    @CCD(label = " ", typeOverride = FieldType.YesOrNo)
    @JsonProperty("copyToOtherPartyYesOrNo")
    private String copyToOtherPartyYesOrNo;
    @CCD(label = " ")
    @JsonProperty("copyToOtherPartyText")
    private String copyToOtherPartyText;
    @CCD(label = "Stored Application ID")
    @JsonProperty("storedApplicationId")
    private String storedApplicationId;
}
