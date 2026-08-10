package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "HearingPreference", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ClaimantHearingPreference {
    @CCD(
            label = "What are the claimant's hearing preferences",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_HearingPreferences"
    )
    @JsonProperty("hearing_preferences")
    private List<String> hearingPreferences;
    @CCD(
            label = "Why is the claimant unable to take part in video or phone hearings",
            showCondition = "hearing_preferences CONTAINS \"Neither\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("hearing_assistance")
    private String hearingAssistance;
    @CCD(
            label = "Do you have a physical, mental or learning disability or long term health condition that means you need support during your case?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("reasonable_adjustments")
    private String reasonableAdjustments;
    @CCD(
            label = "Tell us what support you need to request",
            showCondition = "reasonable_adjustments=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("reasonable_adjustments_detail")
    private String reasonableAdjustmentsDetail;
    @CCD(
            label = "Contact language",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_languages"
    )
    @JsonProperty("contact_language")
    private String contactLanguage;
    @CCD(
            label = "If a hearing is required, what language do you want to speak at a hearing?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_languages"
    )
    @JsonProperty("hearing_language")
    private String hearingLanguage;
    @CCD(
            label = "Hearing panel preference",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "claimant_hearingPanelPreference"
    )
    @JsonProperty("claimant_hearing_panel_preference")
    private String claimantHearingPanelPreference;
    @CCD(
            label = "Panel preference reason",
            showCondition = "claimant_hearing_panel_preference=\"Judge\" OR claimant_hearing_panel_preference=\"Panel\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("claimant_hearing_panel_preference_why")
    private String claimantHearingPanelPreferenceWhy;
}