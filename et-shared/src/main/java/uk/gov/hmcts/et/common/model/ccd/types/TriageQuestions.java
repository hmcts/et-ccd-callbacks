package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TriageQuestions {
    @CCD(label = " ", searchable = false)
    @JsonProperty("postcode")
    private String postcode;
    @CCD(label = " ", searchable = false)
    @JsonProperty("claimJurisdiction")
    private String claimJurisdiction;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("claimantRepresentedQuestion")
    private String claimantRepresentedQuestion;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.FixedList, typeParameterOverride = "fl_CaseType")
    @JsonProperty("caseType")
    private String caseType;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("acasMultiple")
    private String acasMultiple;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("validNoAcasReason")
    private String validNoAcasReason;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_typeOfClaim"
    )
    @JsonProperty("typesOfClaim")
    private List<String> typesOfClaim;
}
