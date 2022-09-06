package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * This object contains references to the data captured during the ET1 Vetting event. Data is stored within this object
 * to be used by Docmosis Tornado to generate a document
 */
@SuperBuilder
@Data
@SuppressWarnings({"PMD.LinguisticNaming", "PMD.TooManyFields"})
public class Et1VettingData {

    @JsonProperty("ethosCaseReference")
    private String ethosCaseReference;

    // ET1 Vetting - Can we serve the claim?
    @JsonProperty("et1VettingCanServeClaimYesOrNo")
    private String et1VettingCanServeClaimYesOrNo;
    @JsonProperty("et1VettingCanServeClaimNoReason")
    private String et1VettingCanServeClaimNoReason;
    @JsonProperty("et1VettingCanServeClaimGeneralNote")
    private String et1VettingCanServeClaimGeneralNote;
    // ET1 Vetting - Acas certificate?
    @JsonProperty("et1VettingAcasCertIsYesOrNo1")
    private String et1VettingAcasCertIsYesOrNo1;
    @JsonProperty("et1VettingAcasCertExemptYesOrNo1")
    private String et1VettingAcasCertExemptYesOrNo1;
    @JsonProperty("et1VettingAcasCertIsYesOrNo2")
    private String et1VettingAcasCertIsYesOrNo2;
    @JsonProperty("et1VettingAcasCertExemptYesOrNo2")
    private String et1VettingAcasCertExemptYesOrNo2;
    @JsonProperty("et1VettingAcasCertIsYesOrNo3")
    private String et1VettingAcasCertIsYesOrNo3;
    @JsonProperty("et1VettingAcasCertExemptYesOrNo3")
    private String et1VettingAcasCertExemptYesOrNo3;
    @JsonProperty("et1VettingAcasCertIsYesOrNo4")
    private String et1VettingAcasCertIsYesOrNo4;
    @JsonProperty("et1VettingAcasCertExemptYesOrNo4")
    private String et1VettingAcasCertExemptYesOrNo4;
    @JsonProperty("et1VettingAcasCertIsYesOrNo5")
    private String et1VettingAcasCertIsYesOrNo5;
    @JsonProperty("et1VettingAcasCertExemptYesOrNo5")
    private String et1VettingAcasCertExemptYesOrNo5;
    @JsonProperty("et1VettingAcasCertIsYesOrNo6")
    private String et1VettingAcasCertIsYesOrNo6;
    @JsonProperty("et1VettingAcasCertExemptYesOrNo6")
    private String et1VettingAcasCertExemptYesOrNo6;
    @JsonProperty("et1VettingAcasCertGeneralNote")
    private String et1VettingAcasCertGeneralNote;

    //ET1 Vetting -  Substantive Defects
    @JsonProperty("substantiveDefectsList")
    private String substantiveDefectsList;
    @JsonProperty("rule121aTextArea")
    private String rule121aTextArea;
    @JsonProperty("rule121bTextArea")
    private String rule121bTextArea;
    @JsonProperty("rule121cTextArea")
    private String rule121cTextArea;
    @JsonProperty("rule121dTextArea")
    private String rule121dTextArea;
    @JsonProperty("rule121daTextArea")
    private String rule121daTextArea;
    @JsonProperty("rule121eTextArea")
    private String rule121eTextArea;
    @JsonProperty("rule121fTextArea")
    private String rule121fTextArea;
    @JsonProperty("et1SubstantiveDefectsGeneralNotes")
    private String et1SubstantiveDefectsGeneralNotes;

    // ET1 Vetting - Jurisdiction codes
    @JsonProperty("areTheseCodesCorrect")
    private String areTheseCodesCorrect;
    @JsonProperty("codesCorrectGiveDetails")
    private String codesCorrectGiveDetails;
    @JsonProperty("et1JurisdictionCodeGeneralNotes")
    private String et1JurisdictionCodeGeneralNotes;
    @JsonProperty("vettingJurisdictionCodeCollection")
    private String vettingJurisdictionCodeCollection;

    // ET1 Vetting - Track allocation
    @JsonProperty("isTrackAllocationCorrect")
    private String isTrackAllocationCorrect;
    @JsonProperty("suggestAnotherTrack")
    private String suggestAnotherTrack;
    @JsonProperty("whyChangeTrackAllocation")
    private String whyChangeTrackAllocation;
    @JsonProperty("trackAllocationGeneralNotes")
    private String trackAllocationGeneralNotes;
    @JsonProperty("isLocationCorrect")
    private String isLocationCorrect;
    @JsonProperty("whyChangeOffice")
    private String whyChangeOffice;
    @JsonProperty("et1LocationGeneralNotes")
    private String et1LocationGeneralNotes;
    @JsonProperty("trackAllocation")
    private String trackAllocation;
    @JsonProperty("tribunalAndOfficeLocation")
    private String tribunalAndOfficeLocation;
    @JsonProperty("regionalOffice")
    private String regionalOffice;
    @JsonProperty("regionalOfficeList")
    private String regionalOfficeList;

    // ET1 Vetting - Hearing venues
    @JsonProperty("et1AddressDetails")
    private String et1AddressDetails;
    @JsonProperty("et1TribunalRegion")
    private String et1TribunalRegion;
    @JsonProperty("et1HearingVenues")
    private String et1HearingVenues;
    @JsonProperty("et1SuggestHearingVenue")
    private String et1SuggestHearingVenue;
    @JsonProperty("et1HearingVenueGeneralNotes")
    private String et1HearingVenueGeneralNotes;
    @JsonProperty("et1GovOrMajorQuestion")
    private String et1GovOrMajorQuestion;

    // ET1 Vetting - Further questions
    @JsonProperty("et1ReasonableAdjustmentsQuestion")
    private String et1ReasonableAdjustmentsQuestion;
    @JsonProperty("et1ReasonableAdjustmentsTextArea")
    private String et1ReasonableAdjustmentsTextArea;
    @JsonProperty("et1VideoHearingQuestion")
    private String et1VideoHearingQuestion;
    @JsonProperty("et1VideoHearingTextArea")
    private String et1VideoHearingTextArea;
    @JsonProperty("et1FurtherQuestionsGeneralNotes")
    private String et1FurtherQuestionsGeneralNotes;

    // ET1 Vetting - Referral to judge
    @JsonProperty("referralToJudgeOrLOList")
    private String referralToJudgeOrLOList;
    @JsonProperty("aClaimOfInterimReliefTextArea")
    private String claimOfInterimReliefTextArea;
    @JsonProperty("aStatutoryAppealTextArea")
    private String statutoryAppealTextArea;
    @JsonProperty("anAllegationOfCommissionOfSexualOffenceTextArea")
    private String anAllegationOfCommissionOfSexualOffenceTextArea;
    @JsonProperty("insolvencyTextArea")
    private String insolvencyTextArea;
    @JsonProperty("jurisdictionsUnclearTextArea")
    private String jurisdictionsUnclearTextArea;
    @JsonProperty("lengthOfServiceTextArea")
    private String lengthOfServiceTextArea;
    @JsonProperty("potentiallyLinkedCasesInTheEcmTextArea")
    private String potentiallyLinkedCasesInTheEcmTextArea;
    @JsonProperty("rule50IssuesTextArea")
    private String rule50IssuesTextArea;
    @JsonProperty("anotherReasonForJudicialReferralTextArea")
    private String anotherReasonForJudicialReferralTextArea;
    @JsonProperty("et1JudgeReferralGeneralNotes")
    private String et1JudgeReferralGeneralNotes;

    // ET1 Vetting - Referral to Regional Employment judge
    @JsonProperty("referralToREJOrVPList")
    private String referralToREJOrVPList;
    @JsonProperty("vexatiousLitigantOrderTextArea")
    private String vexatiousLitigantOrderTextArea;
    @JsonProperty("aNationalSecurityIssueTextArea")
    private String nationalSecurityIssueTextArea;
    @JsonProperty("nationalMultipleOrPresidentialOrderTextArea")
    private String nationalMultipleOrPresidentialOrderTextArea;
    @JsonProperty("transferToOtherRegionTextArea")
    private String transferToOtherRegionTextArea;
    @JsonProperty("serviceAbroadTextArea")
    private String serviceAbroadTextArea;
    @JsonProperty("aSensitiveIssueTextArea")
    private String sensitiveIssueTextArea;
    @JsonProperty("anyPotentialConflictTextArea")
    private String anyPotentialConflictTextArea;
    @JsonProperty("anotherReasonREJOrVPTextArea")
    private String anotherReasonREJOrVPTextArea;
    @JsonProperty("et1REJOrVPReferralGeneralNotes")
    private String et1REJOrVPReferralGeneralNotes;

    // ET1 Vetting - Other Factors
    @JsonProperty("otherReferralList")
    private String otherReferralList;
    @JsonProperty("claimOutOfTimeTextArea")
    private String claimOutOfTimeTextArea;
    @JsonProperty("multipleClaimTextArea")
    private String multipleClaimTextArea;
    @JsonProperty("employmentStatusIssuesTextArea")
    private String employmentStatusIssuesTextArea;
    @JsonProperty("pidJurisdictionRegulatorTextArea")
    private String pidJurisdictionRegulatorTextArea;
    @JsonProperty("videoHearingPreferenceTextArea")
    private String videoHearingPreferenceTextArea;
    @JsonProperty("rule50IssuesForOtherReferralTextArea")
    private String rule50IssuesForOtherReferralTextArea;
    @JsonProperty("anotherReasonForOtherReferralTextArea")
    private String anotherReasonForOtherReferralTextArea;
    @JsonProperty("et1OtherReferralGeneralNotes")
    private String et1OtherReferralGeneralNotes;

    // ET! Vetting - Addtional Information
    @JsonProperty("et1VettingAdditionalInformationTextArea")
    private String et1VettingAdditionalInformationTextArea;
}
