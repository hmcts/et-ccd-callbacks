package uk.gov.hmcts.et.common.model.listing.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.et.common.model.ccd.ListingItemType;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ListingType {

    @CCD(ignore = true)
    @JsonProperty("causeListDate")
    private String causeListDate;
    @CCD(ignore = true)
    @JsonProperty("causeListTime")
    private String causeListTime;
    @CCD(ignore = true)
    @JsonProperty("causeListVenue")
    private String causeListVenue;
    @CCD(ignore = true)
    @JsonProperty("elmoCaseReference")
    private String elmoCaseReference;
    @CCD(ignore = true)
    @JsonProperty("jurisdictionCodesList")
    private String jurisdictionCodesList;
    @CCD(ignore = true)
    @JsonProperty("hearingType")
    private String hearingType;
    @CCD(ignore = true)
    @JsonProperty("positionType")
    private String positionType;
    @CCD(ignore = true)
    @JsonProperty("hearingJudgeName")
    private String hearingJudgeName;
    @CCD(ignore = true)
    @JsonProperty("hearingEEMember")
    private String hearingEEMember;
    @CCD(ignore = true)
    @JsonProperty("hearingERMember")
    private String hearingERMember;
    @CCD(ignore = true)
    @JsonProperty("hearingClerk")
    private String hearingClerk;
    @CCD(ignore = true)
    @JsonProperty("hearingDay")
    private String hearingDay;
    @CCD(ignore = true)
    @JsonProperty("claimantName")
    private String claimantName;
    @CCD(ignore = true)
    @JsonProperty("claimantTown")
    private String claimantTown;
    @CCD(ignore = true)
    @JsonProperty("claimantRepresentative")
    private String claimantRepresentative;
    @CCD(ignore = true)
    @JsonProperty("respondent")
    private String respondent;
    @CCD(ignore = true)
    @JsonProperty("respondentTown")
    private String respondentTown;
    @CCD(ignore = true)
    @JsonProperty("respondentRepresentative")
    private String respondentRepresentative;
    @CCD(ignore = true)
    @JsonProperty("estHearingLength")
    private String estHearingLength;
    @CCD(ignore = true)
    @JsonProperty("Hearing_panel")
    private String hearingPanel;

    @CCD(ignore = true)
    @JsonProperty("Hearing_room")
    private String hearingRoom;
    @CCD(ignore = true)
    @JsonProperty("resp_others")
    private String respondentOthers;
    @CCD(ignore = true)
    @JsonProperty("Hearing_notes")
    private String hearingNotes;
    @CCD(ignore = true)
    @JsonProperty("judicialMediation")
    private String judicialMediation;
    @CCD(ignore = true)
    @JsonProperty("hearingFormat")
    private String hearingFormat;
    @CCD(ignore = true)
    @JsonProperty("hearingReadingDeliberationMembersChambers")
    private String hearingReadingDeliberationMembersChambers;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Single or Range", typeOverride = FieldType.FixedRadioList, typeParameterOverride = "fl_HearingDateType")
  private String hearingDateType;
  @CCD(label = "Hearing Date", showCondition = "hearingDateType=\"Single\"")
  private java.time.LocalDate listingDate;
  @CCD(label = "Hearing From", showCondition = "hearingDateType=\"Range\"")
  private java.time.LocalDate listingDateFrom;
  @CCD(label = "Hearing To", showCondition = "hearingDateType=\"Range\"")
  private java.time.LocalDate listingDateTo;
  @CCD(label = "Hearing Venue", typeOverride = FieldType.DynamicList)
  private String listingVenue;
  @CCD(label = "Daily Cause List")
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<ListingItemType>> listingCollection;
  @CCD(label = "Daily Cause List for ${listingDate}", typeOverride = FieldType.Label)
  private String listingLabel;
  @CCD(label = "Hearing Document", typeOverride = FieldType.FixedList, typeParameterOverride = "fl_HearingDocType")
  private String hearingDocType;
  @CCD(
          label = "Type",
          showCondition = "hearingDocType=\"ETCL - Cause List\"",
          typeOverride = FieldType.FixedRadioList,
          typeParameterOverride = "fl_HearingDocETCL"
  )
  private String hearingDocETCL;
  @CCD(label = "Correspondence Address")
  private uk.gov.hmcts.ccd.sdk.type.AddressUK tribunalCorrespondenceAddress;
  @CCD(label = "Correspondence Telephone", hint = " ")
  private String tribunalCorrespondenceTelephone;
  @CCD(label = "Correspondence Fax")
  private String tribunalCorrespondenceFax;
  @CCD(label = "Correspondence DX")
  private String tribunalCorrespondenceDX;
  @CCD(label = "Correspondence Email")
  private String tribunalCorrespondenceEmail;
  @CCD(label = "Is there a room?", showCondition = "hearingDocETCL=\"Public\" OR hearingDocETCL=\"Staff\"")
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo roomOrNoRoom;
  @CCD(label = "Managing Office")
  private String managingOffice;
  // ==== end synthesised definition-only fields ====
}
