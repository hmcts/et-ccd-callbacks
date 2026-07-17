package uk.gov.hmcts.et.common.model.hmc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.et.common.model.hmc.validator.ListingReasonCodeEnum;

import java.util.List;

import static uk.gov.hmcts.et.common.model.hmc.ValidationError.AMEND_REASON_CODE_MAX_LENGTH;
import static uk.gov.hmcts.et.common.model.hmc.ValidationError.FACILITIES_REQUIRED_MAX_LENGTH_MSG;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingDetails {
    @CCD(ignore = true)
    @JsonProperty("autolistFlag")
    @NotNull(message = ValidationError.AUTO_LIST_FLAG_NULL_EMPTY)
    private Boolean autoListFlag;

    @CCD(ignore = true)
    @ListingReasonCodeEnum(enumClass = ListingReasonCode.class, fieldName = "listingAutoChangeReasonCode")
    @Size(max = 70, message = ValidationError.LISTING_REASON_CODE_MAX_LENGTH)
    private String listingAutoChangeReasonCode;

    @CCD(ignore = true)
    @NotEmpty(message = ValidationError.HEARING_TYPE_NULL_EMPTY)
    @Size(max = 40, message = ValidationError.HEARING_TYPE_MAX_LENGTH)
    private String hearingType;

    @CCD(ignore = true)
    private HearingWindow hearingWindow;

    @CCD(ignore = true)
    @NotNull(message = ValidationError.DURATION_EMPTY)
    @Min(value = 0, message = ValidationError.DURATION_MIN_VALUE)
    private Integer duration;

    @CCD(ignore = true)
    private List<@Size(max = 70, message = ValidationError.NON_STANDARD_HEARING_DURATION_REASONS_MAX_LENGTH_MSG) String>
            nonStandardHearingDurationReasons;

    @CCD(ignore = true)
    @NotEmpty(message = ValidationError.HEARING_PRIORITY_TYPE)
    @Size(max = 60, message = ValidationError.HEARING_PRIORITY_TYPE_MAX_LENGTH)
    private String hearingPriorityType;

    @CCD(ignore = true)
    @Min(value = 0, message = ValidationError.NUMBER_OF_PHYSICAL_ATTENDEES_MIN_VALUE)
    private Integer numberOfPhysicalAttendees;

    @CCD(ignore = true)
    private Boolean hearingInWelshFlag;

    @CCD(ignore = true)
    @Valid
    @NotNull(message = ValidationError.HEARING_LOCATION_EMPTY)
    @NotEmpty(message = ValidationError.INVALID_HEARING_LOCATION)
    private List<HearingLocation> hearingLocations;

    @CCD(ignore = true)
    private List<@Size(max = 70, message = FACILITIES_REQUIRED_MAX_LENGTH_MSG) String> facilitiesRequired;

    @CCD(ignore = true)
    @Size(max = 2000, message = ValidationError.LISTING_COMMENTS_MAX_LENGTH)
    private String listingComments;

    @CCD(ignore = true)
    @Size(max = 60, message = ValidationError.HEARING_REQUESTER_MAX_LENGTH)
    private String hearingRequester;

    @CCD(ignore = true)
    @Builder.Default()
    private Boolean privateHearingRequiredFlag = true;

    @CCD(ignore = true)
    @Size(max = 70, message = ValidationError.LEAD_JUDGE_CONTRACT_TYPE_MAX_LENGTH)
    private String leadJudgeContractType;

    @CCD(ignore = true)
    @Valid
    @NotNull(message = ValidationError.INVALID_PANEL_REQUIREMENTS)
    private PanelRequirements panelRequirements;

    @CCD(ignore = true)
    private Boolean hearingIsLinkedFlag;

    @CCD(ignore = true)
    private List<@Size(min = 1, max = 70, message = AMEND_REASON_CODE_MAX_LENGTH) String> amendReasonCodes;

    @CCD(ignore = true)
    @Valid
    @NotNull(message = ValidationError.HEARING_CHANNEL_EMPTY)
    private List<@Size(max = 70, message = ValidationError.CHANNEL_TYPE_MAX_LENGTH)String> hearingChannels;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Hearing Date")
  private java.time.LocalDateTime hearingDetailsDate;
  @CCD(label = "Hearing Status", typeOverride = FieldType.FixedList, typeParameterOverride = "fl_HearingStatus")
  private String hearingDetailsStatus;
  @CCD(
          label = "Postponed by",
          showCondition = "hearingDetailsStatus=\"Postponed\"",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "fl_PostponedBy"
  )
  private String hearingDetailsPostponedBy;
  @CCD(label = "Has the case or part of the case been disposed?", showCondition = "hearingDetailsStatus=\"Heard\"")
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hearingDetailsCaseDisposed;
  @CCD(label = "Has the hearing been part heard?", showCondition = "hearingDetailsStatus=\"Heard\"")
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hearingDetailsPartHeard;
  @CCD(label = "Is there a reserved Judgment?", showCondition = "hearingDetailsStatus=\"Heard\"")
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo hearingDetailsReservedJudgment;
  @CCD(
          label = "Attendees (Claimant)",
          showCondition = "hearingDetailsStatus=\"Heard\"",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "fl_Attendee"
  )
  private String hearingDetailsAttendeeClaimant;
  @CCD(label = "Number of Non Attendees (Respondent) ", showCondition = "hearingDetailsStatus=\"Heard\"")
  private Integer hearingDetailsAttendeeNonAttendees;
  @CCD(label = "Respondent Attended - No Representative", showCondition = "hearingDetailsStatus=\"Heard\"")
  private Integer hearingDetailsAttendeeRespNoRep;
  @CCD(label = "Respondent and Representative Attended", showCondition = "hearingDetailsStatus=\"Heard\"")
  private Integer hearingDetailsAttendeeRespAndRep;
  @CCD(label = "Respondent representative only attended", showCondition = "hearingDetailsStatus=\"Heard\"")
  private Integer hearingDetailsAttendeeRepOnly;
  @CCD(label = "Start Time", showCondition = "hearingDetailsStatus=\"Heard\"")
  private java.time.LocalDateTime hearingDetailsTimingStart;
  @CCD(label = "Break", showCondition = "hearingDetailsStatus=\"Heard\"")
  private java.time.LocalDateTime hearingDetailsTimingBreak;
  @CCD(label = "Resume", showCondition = "hearingDetailsStatus=\"Heard\"")
  private java.time.LocalDateTime hearingDetailsTimingResume;
  @CCD(label = "Finish", showCondition = "hearingDetailsStatus=\"Heard\"")
  private java.time.LocalDateTime hearingDetailsTimingFinish;
  @CCD(label = "Duration", showCondition = "hearingDetailsStatus=\"Heard\"")
  private Integer hearingDetailsTimingDuration;
  @CCD(label = "Hearing Notes", typeOverride = FieldType.TextArea)
  private String hearingDetailsHearingNotes2;
  // ==== end synthesised definition-only fields ====
}
