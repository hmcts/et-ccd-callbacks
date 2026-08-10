package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "DateListed", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DateListedType {

    @CCD(label = "Hearing Date", typeOverride = FieldType.DateTime)
    @JsonProperty("listedDate")
    private String listedDate;
    @CCD(label = "Hearing Status", typeOverride = FieldType.FixedList, typeParameterOverride = "fl_HearingStatus")
    @JsonProperty("Hearing_status")
    private String hearingStatus;
    @CCD(
            label = "Postponed by",
            showCondition = "Hearing_status=\"Postponed\"",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_PostponedBy"
    )
    @JsonProperty("Postponed_by")
    private String postponedBy;
    @CCD(label = "Hearing Venue", typeOverride = FieldType.DynamicList)
    @JsonProperty("hearingVenueDay")
    private DynamicFixedListType hearingVenueDay;
    @CCD(ignore = true)
    @JsonProperty("hearingVenueDayScotland")
    private String hearingVenueDayScotland;
    @CCD(label = "Room", typeOverride = FieldType.DynamicList)
    @JsonProperty("hearingRoom")
    private DynamicFixedListType hearingRoom;
    @CCD(label = "Clerk", typeOverride = FieldType.DynamicList)
    @JsonProperty("hearingClerk")
    private DynamicFixedListType hearingClerk;
    @CCD(ignore = true)
    @JsonProperty("Hearing_typeReadingDeliberation")
    private String hearingTypeReadingDeliberation;
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
    @CCD(
            label = "Has the case or part of the case been disposed?",
            showCondition = "Hearing_status=\"Heard\"",
            retainHiddenValue = true,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("hearingCaseDisposed")
    private String hearingCaseDisposed;
    @CCD(
            label = "Has the hearing been part heard?",
            showCondition = "Hearing_status=\"Heard\"",
            retainHiddenValue = true,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("Hearing_part_heard")
    private String hearingPartHeard;
    @CCD(
            label = "Is there a reserved Judgment?",
            showCondition = "Hearing_status=\"Heard\"",
            retainHiddenValue = true,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("Hearing_reserved_judgement")
    private String hearingReservedJudgement;
    @CCD(
            label = "Attendees (Claimant)",
            showCondition = "Hearing_status=\"Heard\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Attendee"
    )
    @JsonProperty("attendee_claimant")
    private String attendeeClaimant;
    @CCD(
            label = "Number of Non Attendees (Respondent) ",
            showCondition = "Hearing_status=\"Heard\"",
            retainHiddenValue = true,
            typeOverride = FieldType.Number
    )
    @JsonProperty("attendee_non_attendees")
    private String attendeeNonAttendees;
    @CCD(
            label = "Respondent Attended - No Representative",
            showCondition = "Hearing_status=\"Heard\"",
            retainHiddenValue = true,
            typeOverride = FieldType.Number
    )
    @JsonProperty("attendee_resp_no_rep")
    private String attendeeRespNoRep;
    @CCD(
            label = "Respondent and Representative Attended",
            showCondition = "Hearing_status=\"Heard\"",
            retainHiddenValue = true,
            typeOverride = FieldType.Number
    )
    @JsonProperty("attendee_resp_&_rep")
    private String attendeeRespAndRep;
    @CCD(
            label = "Respondent representative only attended",
            showCondition = "Hearing_status=\"Heard\"",
            retainHiddenValue = true,
            typeOverride = FieldType.Number
    )
    @JsonProperty("attendee_rep_only")
    private String attendeeRepOnly;
    @CCD(
            label = "Start Time",
            showCondition = "Hearing_status=\"Heard\"",
            retainHiddenValue = true,
            typeOverride = FieldType.DateTime
    )
    @JsonProperty("hearingTimingStart")
    private String hearingTimingStart;
    @CCD(
            label = "Break",
            showCondition = "Hearing_status=\"Heard\"",
            retainHiddenValue = true,
            typeOverride = FieldType.DateTime
    )
    @JsonProperty("hearingTimingBreak")
    private String hearingTimingBreak;
    @CCD(
            label = "Resume",
            showCondition = "Hearing_status=\"Heard\"",
            retainHiddenValue = true,
            typeOverride = FieldType.DateTime
    )
    @JsonProperty("hearingTimingResume")
    private String hearingTimingResume;
    @CCD(
            label = "Finish",
            showCondition = "Hearing_status=\"Heard\"",
            retainHiddenValue = true,
            typeOverride = FieldType.DateTime
    )
    @JsonProperty("hearingTimingFinish")
    private String hearingTimingFinish;
    @CCD(
            label = "Duration",
            showCondition = "Hearing_status=\"Heard\"",
            retainHiddenValue = true,
            typeOverride = FieldType.Number
    )
    @JsonProperty("hearingTimingDuration")
    private String hearingTimingDuration;
    @CCD(label = "Hearing Notes", typeOverride = FieldType.TextArea)
    @JsonProperty("HearingNotes2")
    private String hearingNotes2;
    @CCD(label = "Postponed Date", showCondition = "Hearing_status=\"Postponed\"")
    @JsonProperty("postponedDate")
    private String postponedDate;

    public boolean hasHearingVenue() {
        return hearingVenueDay != null && hearingVenueDay.getValue() != null;
    }

    public boolean hasHearingRoom() {
        return hearingRoom != null && hearingRoom.getValue() != null;
    }

    public boolean hasHearingClerk() {
        return hearingClerk != null && hearingClerk.getValue() != null;
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
}