package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ClaimantRequest", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ClaimantRequestType {
    @CCD(
            label = "What do you want if your claim is successful?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_claimOutcomes"
    )
    @JsonProperty("claim_outcome")
    private List<String> claimOutcome;
    @CCD(label = "What compensation are you seeking?", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("claimant_compensation_text")
    private String claimantCompensationText;
    @CCD(label = "Compensation amount requested", searchable = false, typeOverride = FieldType.Number)
    @JsonProperty("claimant_compensation_amount")
    private String claimantCompensationAmount;
    @CCD(label = "Tribunal recommendation request", searchable = false)
    @JsonProperty("claimant_tribunal_recommendation")
    private String claimantTribunalRecommendation;
    @CCD(label = "Whistleblowing claim", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("whistleblowing")
    private String whistleblowing;
    @CCD(label = "Whistleblowing authority", searchable = false)
    @JsonProperty("whistleblowing_authority")
    private String whistleblowingAuthority;
    @CCD(label = "Describe what happened to you", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("claim_description")
    private String claimDescription;
    @CCD(
            label = "Describe what happened to you",
            categoryID = "C12",
            searchable = false,
            typeOverride = FieldType.Document
    )
    @JsonProperty("claim_description_document")
    private UploadedDocumentType claimDescriptionDocument;
    @CCD(
            label = "What type of discrimination are you claiming?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_discriminationClaims"
    )
    @JsonProperty("discrimination_claims")
    private List<String> discriminationClaims;
    @CCD(
            label = "What type of pay claim are you making?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_payClaims"
    )
    @JsonProperty("pay_claims")
    private List<String> payClaims;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("linked_cases")
    private String linkedCases;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("linked_cases_detail")
    private String linkedCasesDetail;
    @CCD(
            label = "Please describe what type of claim you want to make",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("other_claim")
    private String otherClaim;
}
