package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.Document;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "Hearing", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class HearingType {

    @CCD(label = "Hearing type", typeOverride = FieldType.FixedList, typeParameterOverride = "fl_Hearing")
    @JsonProperty("Hearing_type")
    private String hearingType;
    @CCD(
            label = "Public or Private?",
            showCondition = "Hearing_type=\"Preliminary Hearing\"",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_PublicPrivate"
    )
    @JsonProperty("hearingPublicPrivate")
    private String hearingPublicPrivate;
    @CCD(label = "Hearing Number", typeOverride = FieldType.Number)
    @JsonProperty("hearingNumber")
    private String hearingNumber;
    @CCD(label = "Hearing Venue", typeOverride = FieldType.DynamicList)
    @JsonProperty("Hearing_venue")
    private DynamicFixedListType hearingVenue;
    @CCD(ignore = true)
    @JsonProperty("Hearing_venue_Scotland")
    private String hearingVenueScotland;
    @CCD(label = "Estimated hearing length", typeOverride = FieldType.Number)
    @JsonProperty("hearingEstLengthNum")
    private String hearingEstLengthNum;
    @CCD(
            label = "Days, Hours or Minutes",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_HearingLength"
    )
    @JsonProperty("hearingEstLengthNumType")
    private String hearingEstLengthNumType;
    @CCD(label = "Panel Type", typeOverride = FieldType.FixedRadioList, typeParameterOverride = "frl_SitAlone")
    @JsonProperty("hearingSitAlone")
    private String hearingSitAlone;
    @CCD(
            label = "Employer Member",
            showCondition = "hearingSitAlone = \"Full Panel\"",
            typeOverride = FieldType.DynamicList
    )
    @JsonProperty("hearingERMember")
    private DynamicFixedListType hearingERMember;
    @CCD(
            label = "Employee Member",
            showCondition = "hearingSitAlone = \"Full Panel\"",
            typeOverride = FieldType.DynamicList
    )
    @JsonProperty("hearingEEMember")
    private DynamicFixedListType hearingEEMember;
    @CCD(label = "EQP Stage Hearing", typeOverride = FieldType.FixedList, typeParameterOverride = "fl_Stage")
    @JsonProperty("Hearing_stage")
    private String hearingStage;
    @CCD(label = "Hearing Notes", typeOverride = FieldType.TextArea)
    @JsonProperty("Hearing_notes")
    private String hearingNotes;
    @CCD(label = "Employment Judge", typeOverride = FieldType.DynamicList)
    @JsonProperty("judge")
    private DynamicFixedListType judge;
    @CCD(
            label = "Employment Judge",
            showCondition = "hearingSitAlone = \"Two Judges\"",
            typeOverride = FieldType.DynamicList
    )
    @JsonProperty("additionalJudge")
    private DynamicFixedListType additionalJudge;
    @CCD(ignore = true)
    @JsonProperty("Hearing_Glasgow")
    private DynamicFixedListType hearingGlasgow;
    @CCD(ignore = true)
    @JsonProperty("Hearing_Aberdeen")
    private DynamicFixedListType hearingAberdeen;
    @CCD(ignore = true)
    @JsonProperty("Hearing_Dundee")
    private DynamicFixedListType hearingDundee;
    @CCD(ignore = true)
    @JsonProperty("Hearing_Edinburgh")
    private DynamicFixedListType hearingEdinburgh;
    @CCD(label = "Day", min = 1, typeOverride = FieldType.Collection, typeParameterOverride = "DateListed")
    @JsonProperty("hearingDateCollection")
    private List<DateListedTypeItem> hearingDateCollection;
    @CCD(
            label = "Hearing Format",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_HearingFormat"
    )
    @JsonProperty("hearingFormat")
    private List<String> hearingFormat;
    @CCD(label = "Judicial Mediation", typeOverride = FieldType.YesOrNo)
    @JsonProperty("judicialMediation")
    private String judicialMediation;
    @CCD(label = "Hearing notes")
    @JsonProperty("hearingNotesDocument")
    private Document hearingNotesDocument;
    @CCD(label = "Hearing Dates")
    @JsonProperty("hearingDates")
    private String hearingDates;

    public boolean hasHearingJudge() {
        return judge != null && judge.getValue() != null;
    }

    public boolean hasAdditionalHearingJudge() {
        return additionalJudge != null && additionalJudge.getValue() != null;
    }

    public boolean hasHearingEmployerMember() {
        return hearingERMember != null && hearingERMember.getValue() != null;
    }

    public boolean hasHearingEmployeeMember() {
        return hearingEEMember != null && hearingEEMember.getValue() != null;
    }

    public boolean hasHearingGlasgow() {
        return hearingGlasgow != null && hearingGlasgow.getValue() != null;
    }

    public boolean hasHearingAberdeen() {
        return hearingAberdeen != null && hearingAberdeen.getValue() != null;
    }

    public boolean hasHearingDundee() {
        return hearingDundee != null && hearingDundee.getValue() != null;
    }

    public boolean hasHearingEdinburgh() {
        return hearingEdinburgh != null && hearingEdinburgh.getValue() != null;
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Show Hearing Details")
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hearingShowDetails;
  // ==== end synthesised definition-only fields ====
}
