package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.Document;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "Judgment", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class JudgementType {

    @CCD(label = "Non Hearing Judgment?", typeOverride = FieldType.YesOrNo)
    @JsonProperty("non_hearing_judgment")
    private String nonHearingJudgment;
    @CCD(label = "Hearing Details", showCondition = "non_hearing_judgment=\"No\"", typeOverride = FieldType.DynamicList)
    @JsonProperty("dynamicJudgementHearing")
    private DynamicFixedListType dynamicJudgementHearing;
    @CCD(
            label = "Date of Hearing",
            showCondition = "non_hearing_judgment=\"No\"",
            retainHiddenValue = true,
            typeOverride = FieldType.Date
    )
    @JsonProperty("judgmentHearingDate")
    private String judgmentHearingDate;
    @CCD(label = "Judgment Type", typeOverride = FieldType.FixedList, typeParameterOverride = "fl_JudgementType")
    @JsonProperty("judgement_type")
    private String judgementType;
    @CCD(label = "Liability", typeOverride = FieldType.FixedList, typeParameterOverride = "fl_Liability")
    @JsonProperty("liability_optional")
    private String liabilityOptional;
    @CCD(label = "Jurisdiction", typeOverride = FieldType.Collection, typeParameterOverride = "Jurisdiction")
    @JsonProperty("jurisdictionCodes")
    private List<JurCodesTypeItem> jurisdictionCodes;
    @CCD(label = "Date Judgment made", typeOverride = FieldType.Date)
    @JsonProperty("date_judgment_made")
    private String dateJudgmentMade;
    @CCD(label = "Date Judgment sent", typeOverride = FieldType.Date)
    @JsonProperty("date_judgment_sent")
    private String dateJudgmentSent;
    @CCD(label = "Judgment Notes", typeOverride = FieldType.TextArea)
    @JsonProperty("judgment_notes")
    private String judgmentNotes;
    @CCD(label = "Upload outcome of Judgment", categoryID = "C60")
    @JsonProperty("judgement_outcome_doc")
    private Document judgementOutcomeDoc;
    @CCD(label = "Judgment details", hint = " ")
    @JsonProperty("judgement_details")
    private JudgementDetailsType judgementDetails;
    @CCD(label = "Reconsideration", hint = " ")
    @JsonProperty("reconsiderations")
    private JudgmentReconsiderationType judgementReconsiderations;
    @CCD(label = "Costs")
    @JsonProperty("Judgement_costs")
    private CostsType judgementCosts;
}
