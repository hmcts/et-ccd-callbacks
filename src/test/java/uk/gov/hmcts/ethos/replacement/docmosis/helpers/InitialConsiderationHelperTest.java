package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.EtICListForFinalHearingUpdated;
import uk.gov.hmcts.et.common.model.ccd.EtICListForPreliminaryHearingUpdated;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

class InitialConsiderationHelperTest {
    private CaseData caseData;

    @Test
    void getDocumentRequest_EW_preliminaryHearing() throws JsonProcessingException {
        caseData = CaseDataBuilder.builder().build();
        setCaseDataValues(caseData);
        caseData.setEtICHearingNotListedListUpdated(Collections.singletonList("List for preliminary hearing"));
        caseData.setEtICHearingNotListedListForPrelimHearingUpdated(populatePreliminaryHearingUpdated());
        String documentRequest = InitialConsiderationHelper.getDocumentRequest(caseData,
                "key", ENGLANDWALES_CASE_TYPE_ID);

        String expected = "{\"accessKey\":\"key\",\"templateName\":\"EM-TRB-EGW-ENG-02203.docx\","
                + "\"outputName\":\"Initial Consideration.pdf\",\"data\":{\"caseNumber\":\"6000001/2024\","
                + "\"issuesJurisdiction\":\"No\",\"issuesJurCodesGiveDetails\":null,\"canProceed\":\"Yes\","
                + "\"hearingAlreadyListed\":\"No\",\"hearingListed\":null,\"hearingPostpone\":null,"
                + "\"hearingExtend\":null,\"hearingConvertFinal\":null,\"hearingConvertF2f\":null,"
                + "\"hearingOther\":null,\"hearingWithJudgeOrMembers\":null,\"hearingWithJudgeOrMembersReason\":null,"
                + "\"hearingWithJudgeOrMembersFurtherDetails\":null,\"otherDirections\":null,"
                + "\"hearingNotListed\":[\"List for preliminary hearing\"],\"cvpHearingType\":null,"
                + "\"cvpFinalDetails\":null,\"cvpPreliminaryDetails\":null,\"cvpPreliminaryYesNo\":null,"
                + "\"preliminaryHearingType\":[\"Video\",\"F2F\"],\"preliminaryHearingPurpose\":[\"Case management\"],"
                + "\"preliminaryHearingNotice\":\"Purpose of preliminary hearing\",\"preliminaryHearingLength\":\"1\","
                + "\"preliminaryHearingLengthType\":\"Hours\",\"preliminaryHearingWithMembers\":\"Yes\","
                + "\"preliminaryHearingWithMembersReason\":\"reasons for requiring members\","
                + "\"hearingNotListedListAnyOtherDirections\":null,"
                + "\"etICFinalHearingType\":null,\"etICFinalHearingLength\":null,"
                + "\"etICFinalHearingLengthType\":null,\"etICFinalHearingIsEJSitAlone\":null,"
                + "\"etICFinalHearingIsEJSitAloneReason\":null,"
                + "\"etICFinalHearingIsEJSitAloneFurtherDetails\":null,"
                + "\"udlSitAlone\":null,\"udlReasons\":null,\"udlDisputeOnFacts\":null,"
                + "\"udlLittleOrNoAgreement\":null,\"udlIssueOfLawArising\":null,\"udlViewsOfParties\":null,"
                + "\"udlNoViewsExpressedByParties\":null,\"udlConcurrentProceedings\":null,\"udlOther\":null,"
                + "\"udlHearingFormat\":null,\"udlCVPIssue\":null,\"udlFinalF2FIssue\":null,"
                + "\"udlCheckComplianceOrders\":null,\"hearingNotListedOtherDirections\":null,"
                + "\"furtherInformation\":[],\"furtherInfoGiveDetails\":null,\"furtherInfoTimeToComply\":null,"
                + "\"r27ClaimToBe\":null,\"r27WhichPart\":null,\"r27Direction\":null,\"r27DirectionReason\":null,"
                + "\"r27NoJurisdictionReason\":null,\"r27NumberOfDays\":null,\"r28ClaimToBe\":null,"
                + "\"r28WhichPart\":null,\"r28DirectionReason\":null,\"r28NumberOfDays\":null,"
                + "\"furtherInfoAnyOtherDirections\":null,\"icReceiptET3FormIssues\":null,"
                + "\"icRespondentsNameIdentityIssues\":null,\"icJurisdictionCodeIssues\":null,"
                + "\"icApplicationIssues\":null,\"icEmployersContractClaimIssues\":null,"
                + "\"icClaimProspectIssues\":null,\"icListingIssues\":null,\"icDdaDisabilityIssues\":null,"
                + "\"icOrderForFurtherInformation\":null,\"icOtherIssuesOrFinalOrders\":null,"
                + "\"icCompletedBy\":\"A User\",\"icDateCompleted\":\"20 Nov 2024\"}}";

        assertEquals(expected, documentRequest);
    }

    @Test
    void getDocumentRequest_EW_finalHearing() throws JsonProcessingException {
        caseData = CaseDataBuilder.builder().build();
        setCaseDataValues(caseData);
        caseData.setEtICHearingNotListedListUpdated(Collections.singletonList("List for final hearing"));
        caseData.setEtICHearingNotListedListForFinalHearingUpdated(populateFinalHearingUpdated());
        String documentRequest = InitialConsiderationHelper.getDocumentRequest(caseData,
                "key", ENGLANDWALES_CASE_TYPE_ID);

        String expected = "{\"accessKey\":\"key\",\"templateName\":\"EM-TRB-EGW-ENG-02203.docx\","
                + "\"outputName\":\"Initial Consideration.pdf\",\"data\":{\"caseNumber\":\"6000001/2024\","
                + "\"issuesJurisdiction\":\"No\",\"issuesJurCodesGiveDetails\":null,\"canProceed\":\"Yes\","
                + "\"hearingAlreadyListed\":\"No\",\"hearingListed\":null,\"hearingPostpone\":null,"
                + "\"hearingExtend\":null,\"hearingConvertFinal\":null,\"hearingConvertF2f\":null,"
                + "\"hearingOther\":null,\"hearingWithJudgeOrMembers\":null,\"hearingWithJudgeOrMembersReason\":null,"
                + "\"hearingWithJudgeOrMembersFurtherDetails\":null,\"otherDirections\":null,"
                + "\"hearingNotListed\":[\"List for final hearing\"],\"cvpHearingType\":null,"
                + "\"cvpFinalDetails\":null,\"cvpPreliminaryDetails\":null,\"cvpPreliminaryYesNo\":null,"
                + "\"preliminaryHearingType\":null,\"preliminaryHearingPurpose\":null,"
                + "\"preliminaryHearingNotice\":null,\"preliminaryHearingLength\":null,"
                + "\"preliminaryHearingLengthType\":null,\"preliminaryHearingWithMembers\":null,"
                + "\"preliminaryHearingWithMembersReason\":null,"
                + "\"hearingNotListedListAnyOtherDirections\":null,"
                + "\"etICFinalHearingType\":[\"Video\",\"F2F\"],\"etICFinalHearingLength\":\"1\","
                + "\"etICFinalHearingLengthType\":\"Hours\",\"etICFinalHearingIsEJSitAlone\":\"JSA\","
                + "\"etICFinalHearingIsEJSitAloneReason\":\"Members experience is likely to add significant value to "
                + "the process of adjudication\","
                + "\"etICFinalHearingIsEJSitAloneFurtherDetails\":null,"
                + "\"udlSitAlone\":null,\"udlReasons\":null,\"udlDisputeOnFacts\":null,"
                + "\"udlLittleOrNoAgreement\":null,\"udlIssueOfLawArising\":null,\"udlViewsOfParties\":null,"
                + "\"udlNoViewsExpressedByParties\":null,\"udlConcurrentProceedings\":null,\"udlOther\":null,"
                + "\"udlHearingFormat\":null,\"udlCVPIssue\":null,\"udlFinalF2FIssue\":null,"
                + "\"udlCheckComplianceOrders\":null,\"hearingNotListedOtherDirections\":null,"
                + "\"furtherInformation\":[],\"furtherInfoGiveDetails\":null,\"furtherInfoTimeToComply\":null,"
                + "\"r27ClaimToBe\":null,\"r27WhichPart\":null,\"r27Direction\":null,\"r27DirectionReason\":null,"
                + "\"r27NoJurisdictionReason\":null,\"r27NumberOfDays\":null,\"r28ClaimToBe\":null,"
                + "\"r28WhichPart\":null,\"r28DirectionReason\":null,\"r28NumberOfDays\":null,"
                + "\"furtherInfoAnyOtherDirections\":null,\"icReceiptET3FormIssues\":null,"
                + "\"icRespondentsNameIdentityIssues\":null,\"icJurisdictionCodeIssues\":null,"
                + "\"icApplicationIssues\":null,\"icEmployersContractClaimIssues\":null,"
                + "\"icClaimProspectIssues\":null,\"icListingIssues\":null,\"icDdaDisabilityIssues\":null,"
                + "\"icOrderForFurtherInformation\":null,\"icOtherIssuesOrFinalOrders\":null,"
                + "\"icCompletedBy\":\"A User\",\"icDateCompleted\":\"20 Nov 2024\"}}";

        assertEquals(expected, documentRequest);
    }

    public void setCaseDataValues(CaseData caseData) {
        caseData.setEthosCaseReference("6000001/2024");
        caseData.setEtICJuridictionCodesInvalid("No");
        caseData.setEtICInvalidDetails(null);
        caseData.setEtICCanProceed("Yes");
        caseData.setEtICHearingAlreadyListed("No");
        caseData.setEtICHearingListed(null);
        caseData.setEtICPostponeGiveDetails(null);
        caseData.setEtICConvertF2fGiveDetails(null);
        caseData.setEtICConvertPreliminaryGiveDetails(null);
        caseData.setEtICExtendDurationGiveDetails(null);
        caseData.setEtICOtherGiveDetails(null);
        caseData.setEtICHearingListedAnswers(null);
        caseData.setEtICHearingAnyOtherDirections(null);
        caseData.setEtICHearingNotListedSeekComments(null);
        caseData.setEtICHearingNotListedUDLHearing(null);
        caseData.setEtICFurtherInformation(Collections.emptyList());
        caseData.setEtICFurtherInformationGiveDetails(null);
        caseData.setEtICFurtherInformationTimeToComply(null);
        caseData.setIcDateCompleted("20 Nov 2024");
        caseData.setIcCompletedBy("A User");
        caseData.setEtICHearingNotListedOtherDirections("other directions");
    }

    private EtICListForPreliminaryHearingUpdated populatePreliminaryHearingUpdated() {
        EtICListForPreliminaryHearingUpdated preliminaryHearingUpdated = new EtICListForPreliminaryHearingUpdated();
        preliminaryHearingUpdated.setEtICTypeOfPreliminaryHearing(List.of("Video", "F2F"));
        preliminaryHearingUpdated.setEtICPurposeOfPreliminaryHearing(List.of("Case management"));
        preliminaryHearingUpdated.setEtICGiveDetailsOfHearingNotice("Purpose of preliminary hearing");
        preliminaryHearingUpdated.setEtICLengthOfPrelimHearing("1");
        preliminaryHearingUpdated.setPrelimHearingLengthNumType("Hours");
        preliminaryHearingUpdated.setEtICIsPreliminaryHearingWithMembers("Yes");
        preliminaryHearingUpdated.setEtICIsPreliminaryHearingWithMembersReason("reasons for requiring members");
        return preliminaryHearingUpdated;
    }

    private EtICListForFinalHearingUpdated populateFinalHearingUpdated() {
        EtICListForFinalHearingUpdated finalHearingUpdated = new EtICListForFinalHearingUpdated();
        finalHearingUpdated.setEtICTypeOfFinalHearing(List.of("Video", "F2F"));
        finalHearingUpdated.setEtICLengthOfFinalHearing("1");
        finalHearingUpdated.setFinalHearingLengthNumType("Hours");
        finalHearingUpdated.setEtICFinalHearingIsEJSitAlone("JSA");
        finalHearingUpdated.setEtICFinalHearingIsEJSitAloneReason("Members experience is likely to add significant "
                + "value to the process of adjudication");
        return finalHearingUpdated;
    }
}
