package uk.gov.hmcts.et.common.model.listing.types;

import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.et.common.model.ccd.EnglandWalesDefinition;
import uk.gov.hmcts.et.common.model.ccd.ScotlandDefinition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@ComplexType(name = "ListingItemType", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ListingType {

    @CCD(label = "Date")
    @JsonProperty("causeListDate")
    private String causeListDate;
    @CCD(label = "Time")
    @JsonProperty("causeListTime")
    private String causeListTime;
    @CCD(label = "Location")
    @JsonProperty("causeListVenue")
    private String causeListVenue;
    @CCD(label = "Case Number")
    @JsonProperty("elmoCaseReference")
    private String elmoCaseReference;
    @CCD(label = "Jurisdiction")
    @JsonProperty("jurisdictionCodesList")
    private String jurisdictionCodesList;
    @CCD(label = "Case Type")
    @JsonProperty("hearingType")
    private String hearingType;
    @CCD(label = "Position")
    @JsonProperty("positionType")
    private String positionType;
    @CCD(label = "Employment Judge")
    @JsonProperty("additionalJudge")
    private String additionalJudge;
    @CCD(label = "Employment Judge")
    @JsonProperty("hearingJudgeName")
    private String hearingJudgeName;
    @CCD(label = "Employee Member")
    @JsonProperty("hearingEEMember")
    private String hearingEEMember;
    @CCD(label = "Employer Member")
    @JsonProperty("hearingERMember")
    private String hearingERMember;
    @CCD(label = "Hearing Clerk")
    @JsonProperty("hearingClerk")
    private String hearingClerk;
    @CCD(label = "Hearing Day")
    @JsonProperty("hearingDay")
    private String hearingDay;
    @CCD(label = "Claimant")
    @JsonProperty("claimantName")
    private String claimantName;
    @CCD(label = "Claimant Town")
    @JsonProperty("claimantTown")
    private String claimantTown;
    @CCD(label = "Representative")
    @JsonProperty("claimantRepresentative")
    private String claimantRepresentative;
    @CCD(label = "Respondent")
    @JsonProperty("respondent")
    private String respondent;
    @CCD(label = "Respondent Town")
    @JsonProperty("respondentTown")
    private String respondentTown;
    @CCD(label = "Representative")
    @JsonProperty("respondentRepresentative")
    private String respondentRepresentative;
    @CCD(label = "Estimated Duration")
    @JsonProperty("estHearingLength")
    private String estHearingLength;
    @CCD(label = "Hearing Panel")
    @JsonProperty("Hearing_panel")
    private String hearingPanel;

    @CCD(label = "Room")
    @JsonProperty("Hearing_room")
    private String hearingRoom;
    @CCD(label = "Respondent Others")
    @JsonProperty("resp_others")
    private String respondentOthers;
    @CCD(label = "Hearing Notes")
    @JsonProperty("Hearing_notes")
    private String hearingNotes;
    @CCD(label = "Judicial Mediation")
    @JsonProperty("judicialMediation")
    private String judicialMediation;
    @CCD(label = "Hearing Format")
    @JsonProperty("hearingFormat")
    private String hearingFormat;
    @CCD(label = "Reading, Deliberation day, Members Meeting", showCondition = "hearingFormat=\"dummy\"", includeInProfiles = EnglandWalesDefinition.class)
    @CCD(label = "Reading, Deliberation day, Members Meeting", includeInProfiles = ScotlandDefinition.class)
    @JsonProperty("hearingReadingDeliberationMembersChambers")
    private String hearingReadingDeliberationMembersChambers;
}
