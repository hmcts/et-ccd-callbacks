package uk.gov.hmcts.ethos.replacement.docmosis.domain.documents;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * This object contains references to the data captured during the Initial Consideration event.
 * Data is stored within this object to be used by Docmosis Tornado to generate a document
 */
@SuperBuilder
@Data
public class InitialConsiderationData {

    @JsonProperty("caseNumber")
    private String caseNumber;
    @JsonProperty("issuesJurisdiction")
    private String issuesJurisdiction;
    @JsonProperty("issuesJurCodesGiveDetails")
    private String issuesJurCodesGiveDetails;
    @JsonProperty("canProceed")
    private String icCanProceed;
    @JsonProperty("hearingAlreadyListed")
    private String hearingAlreadyListed;

    //Scotland & EngWales

    //Hearing listed
    @JsonProperty("hearingListed")
    private List<String> hearingListed;
    @JsonProperty("hearingPostpone")
    private String hearingPostpone;
    @JsonProperty("hearingExtend")
    private String hearingExtend;
    @JsonProperty("hearingConvertFinal")
    private String hearingConvertFinal;
    @JsonProperty("hearingConvertF2f")
    private String hearingConvertF2f;
    @JsonProperty("hearingOther")
    private String hearingOther;
    @JsonProperty("hearingWithJudgeOrMembers")
    private String hearingWithJudgeOrMembers;
    @JsonProperty("hearingWithJudgeOrMembersReason")
    private List<String> hearingWithJudgeOrMembersReason;
    @JsonProperty("hearingWithJudgeOrMembersFurtherDetails")
    private String hearingWithJudgeOrMembersFurtherDetails;
    @JsonProperty("otherDirections")
    private String otherDirections;

    //Hearing not listed
    @JsonProperty("hearingNotListed")
    private List<String> hearingNotListed;

    @JsonProperty("cvpHearingType")
    private List<String> cvpHearingType;
    @JsonProperty("cvpFinalDetails")
    private String cvpFinalDetails;
    @JsonProperty("cvpPreliminaryDetails")
    private String cvpPreliminaryDetails;
    @JsonProperty("cvpPreliminaryYesNo")
    private String cvpPreliminaryYesNo;

    @JsonProperty("preliminaryHearingType")
    private List<String> preliminaryHearingType;
    @JsonProperty("preliminaryHearingPurpose")
    private List<String> preliminaryHearingPurpose;
    @JsonProperty("preliminaryHearingNotice")
    private String preliminaryHearingNotice;
    @JsonProperty("preliminaryHearingLength")
    private String preliminaryHearingLength;
    @JsonProperty("preliminaryHearingLengthType")
    private String preliminaryHearingLengthType;
    @JsonProperty("preliminaryHearingWithMembers")
    private String preliminaryHearingWithMembers;
    @JsonProperty("preliminaryHearingWithMembersReason")
    private String preliminaryHearingWithMembersReason;
    @JsonProperty("hearingNotListedListAnyOtherDirections")
    private String hearingNotListedListAnyOtherDirections;

    @JsonProperty("etICFinalHearingType")
    private List<String> etICFinalHearingType;
    @JsonProperty("etICTypeOfVideoHearingOrder")
    private List<String> etICTypeOfVideoHearingOrder;
    @JsonProperty("etICTypeOfF2fHearingOrder")
    private List<String> etICTypeOfF2fHearingOrder;
    @JsonProperty("etICHearingOrderBUCompliance")
    private String etICHearingOrderBUCompliance;
    @JsonProperty("etICFinalHearingLength")
    private String etICFinalHearingLength;
    @JsonProperty("etICFinalHearingLengthType")
    private String etICFinalHearingLengthType;
    @JsonProperty("etICFinalHearingIsEJSitAlone")
    private String etICFinalHearingIsEJSitAlone;
    @JsonProperty("etICFinalHearingIsEJSitAloneReason")
    private String etICFinalHearingIsEJSitAloneReason;
    @JsonProperty("etICFinalHearingIsEJSitAloneFurtherDetails")
    private String etICFinalHearingIsEJSitAloneFurtherDetails;

    @JsonProperty("udlSitAlone")
    private String udlSitAlone;
    @JsonProperty("udlReasons")
    private List<String> udlReasons;
    @JsonProperty("udlDisputeOnFacts")
    private String udlDisputeOnFacts;
    @JsonProperty("udlLittleOrNoAgreement")
    private String udlLittleOrNoAgreement;
    @JsonProperty("udlIssueOfLawArising")
    private String udlIssueOfLawArising;
    @JsonProperty("udlViewsOfParties")
    private String udlViewsOfParties;
    @JsonProperty("udlNoViewsExpressedByParties")
    private String udlNoViewsExpressedByParties;
    @JsonProperty("udlConcurrentProceedings")
    private String udlConcurrentProceedings;
    @JsonProperty("udlOther")
    private String udlOther;
    @JsonProperty("udlHearingFormat")
    private String udlHearingFormat;
    @JsonProperty("udlCVPIssue")
    private List<String> udlCVPIssue;
    @JsonProperty("udlFinalF2FIssue")
    private List<String> udlFinalF2FIssue;
    @JsonProperty("udlCheckComplianceOrders")
    private String udlCheckComplianceOrders;
    @JsonProperty("hearingNotListedOtherDirections")
    private String hearingNotListedOtherDirections;

    //further information
    @JsonProperty("furtherInformation")
    private List<String> furtherInformation;
    @JsonProperty("furtherInfoGiveDetails")
    private String furtherInfoGiveDetails;
    @JsonProperty("furtherInfoTimeToComply")
    private String furtherInfoTimeToComply;

    @JsonProperty("r27ClaimToBe")
    private String r27ClaimToBe;
    @JsonProperty("r27WhichPart")
    private String r27WhichPart;
    @JsonProperty("r27Direction")
    private List<String> r27Direction;
    @JsonProperty("r27DirectionReason")
    private String r27DirectionReason;
    @JsonProperty("r27NoJurisdictionReason")
    private String r27NoJurisdictionReason;
    @JsonProperty("r27NumberOfDays")
    private String r27NumberOfDays;

    @JsonProperty("r28ClaimToBe")
    private String r28ClaimToBe;
    @JsonProperty("r28WhichPart")
    private String r28WhichPart;
    @JsonProperty("r28DirectionReason")
    private String r28DirectionReason;
    @JsonProperty("r28NumberOfDays")
    private String r28NumberOfDays;

    @JsonProperty("furtherInfoAnyOtherDirections")
    private String furtherInfoAnyOtherDirections;

    // Backward Compatibility for Eng/Wales
    @JsonProperty("icReceiptET3FormIssues")
    private String icReceiptET3FormIssues;
    @JsonProperty("icRespondentsNameIdentityIssues")
    private String icRespondentsNameIdentityIssues;
    @JsonProperty("icJurisdictionCodeIssues")
    private String icJurisdictionCodeIssues;
    @JsonProperty("icApplicationIssues")
    private String icApplicationIssues;
    @JsonProperty("icEmployersContractClaimIssues")
    private String icEmployersContractClaimIssues;
    @JsonProperty("icClaimProspectIssues")
    private String icClaimProspectIssues;
    @JsonProperty("icListingIssues")
    private String icListingIssues;
    @JsonProperty("icDdaDisabilityIssues")
    private String icDdaDisabilityIssues;
    @JsonProperty("icOrderForFurtherInformation")
    private String icOrderForFurtherInformation;
    @JsonProperty("icOtherIssuesOrFinalOrders")
    private String icOtherIssuesOrFinalOrders;

    // All
    @JsonProperty("icCompletedBy")
    private String icCompletedBy;
    @JsonProperty("icDateCompleted")
    private String icDateCompleted;
}