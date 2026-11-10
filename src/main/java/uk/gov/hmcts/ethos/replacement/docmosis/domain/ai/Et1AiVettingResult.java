package uk.gov.hmcts.ethos.replacement.docmosis.domain.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Et1AiVettingResult {

    private CanServeClaimAssessment canServeClaim;
    private List<AcasAssessment> acasAssessments;
    private String et1VettingEra;
    private SubstantiveDefectsAssessment substantiveDefects;
    private JurisdictionAssessment jurisdictionAssessment;
    private TrackAllocationAssessment trackAllocation;
    private LocationAndVenueAssessment locationAndVenue;
    private FurtherQuestionsAssessment furtherQuestions;
    private JudicialReferralAssessment judicialReferrals;
    private RejOrVpReferralAssessment rejOrVpReferrals;
    private OtherReferralAssessment otherReferrals;
    private String confidence;
    private String executiveSummaryMarkdown;
    private List<String> itemsRequiringReview;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CanServeClaimAssessment {
        private String canServe;
        private String reason;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AcasAssessment {
        private int respondentNumber;
        private String hasCertificate;
        private String isExempt;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubstantiveDefectsAssessment {
        private List<String> defectsList;
        private String rule121aTextArea;
        private String rule121bTextArea;
        private String rule121cTextArea;
        private String rule121dTextArea;
        private String rule121daTextArea;
        private String rule121eTextArea;
        private String rule121fTextArea;
        private String generalNotes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JurisdictionAssessment {
        private String areCodesCorrect;
        private List<String> suggestedJurisdictionCodes;
        private String details;
        private String generalNotes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TrackAllocationAssessment {
        private String isTrackCorrect;
        private String suggestedTrack;
        private String whyChangeTrackAllocation;
        private String generalNotes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LocationAndVenueAssessment {
        private String isLocationCorrect;
        private String suggestedOffice;
        private String whyChangeOffice;
        private String generalNotes;
        private String suggestHearingVenue;
        private String suggestedHearingVenue;
        private String hearingVenueGeneralNotes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FurtherQuestionsAssessment {
        private String isGovOrMajorEmployer;
        private String hasReasonableAdjustments;
        private String reasonableAdjustmentsDetails;
        private String videoHearingPreference;
        private String videoHearingDetails;
        private String generalNotes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JudicialReferralAssessment {
        private List<String> referralsToJudge;
        private String claimOfInterimReliefNotes;
        private String statutoryAppealNotes;
        private String sexualOffenceNotes;
        private String insolvencyNotes;
        private String jurisdictionsUnclearNotes;
        private String lengthOfServiceNotes;
        private String potentiallyLinkedCasesNotes;
        private String rule50IssuesNotes;
        private String anotherReasonNotes;
        private String generalNotes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RejOrVpReferralAssessment {
        private List<String> referralsToRejOrVp;
        private String vexatiousLitigantOrderNotes;
        private String nationalSecurityNotes;
        private String nationalMultipleNotes;
        private String transferToOtherRegionNotes;
        private String serviceAbroadNotes;
        private String sensitiveIssueNotes;
        private String potentialConflictNotes;
        private String anotherReasonNotes;
        private String generalNotes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OtherReferralAssessment {
        private List<String> otherReferrals;
        private String claimOutOfTimeNotes;
        private String multipleClaimNotes;
        private String employmentStatusIssuesNotes;
        private String pidRegulatorNotes;
        private String videoHearingNotes;
        private String rule50Notes;
        private String anotherReasonNotes;
        private String generalNotes;
    }
}
