package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.EtICListForFinalHearingUpdated;
import uk.gov.hmcts.et.common.model.ccd.EtICListForPreliminaryHearingUpdated;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.ISSUE_RULE_27_NOTICE_AND_ORDER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.ISSUE_RULE_27_NOTICE_AND_ORDER_SC;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.ISSUE_RULE_28_NOTICE_AND_ORDER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.ISSUE_RULE_28_NOTICE_AND_ORDER_SC;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.MONTH_STRING_DATE_FORMAT;

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
                + "\"hearingOther\":null,\"hearingWithJudgeOrMembers\":null,\"hearingWithJudgeOrMembersReason\":[\"\"],"
                + "\"hearingWithJsa\":null,\"hearingWithMembersLabel\":null,\"hearingWithMembers\":null,"
                + "\"hearingWithJudgeOrMembersFurtherDetails\":null,\"otherDirections\":null,"
                + "\"hearingNotListed\":[\"List for preliminary hearing\"],\"cvpHearingType\":null,"
                + "\"cvpFinalDetails\":null,\"cvpPreliminaryDetails\":null,\"cvpPreliminaryYesNo\":null,"
                + "\"preliminaryHearingType\":[\"Video\",\"F2F\"],\"preliminaryHearingPurpose\":[\"Case management\"],"
                + "\"preliminaryHearingNotice\":\"Purpose of preliminary hearing\",\"preliminaryHearingLength\":\"1\","
                + "\"preliminaryHearingLengthType\":\"Hours\",\"preliminaryHearingWithMembers\":\"Yes\","
                + "\"preliminaryHearingWithMembersReason\":\"reasons for requiring members\","
                + "\"hearingNotListedListAnyOtherDirections\":null,"
                + "\"etICFinalHearingType\":null,"
                + "\"etICTypeOfVideoHearingOrder\":null,\"etICTypeOfF2fHearingOrder\":null,"
                + "\"etICHearingOrderBUCompliance\":null,"
                + "\"etICFinalHearingLength\":null,"
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
                + "\"hearingOther\":null,\"hearingWithJudgeOrMembers\":null,\"hearingWithJudgeOrMembersReason\":[\"\"],"
                + "\"hearingWithJsa\":null,\"hearingWithMembersLabel\":null,\"hearingWithMembers\":null,"
                + "\"hearingWithJudgeOrMembersFurtherDetails\":null,\"otherDirections\":null,"
                + "\"hearingNotListed\":[\"List for final hearing\"],\"cvpHearingType\":null,"
                + "\"cvpFinalDetails\":null,\"cvpPreliminaryDetails\":null,\"cvpPreliminaryYesNo\":null,"
                + "\"preliminaryHearingType\":null,\"preliminaryHearingPurpose\":null,"
                + "\"preliminaryHearingNotice\":null,\"preliminaryHearingLength\":null,"
                + "\"preliminaryHearingLengthType\":null,\"preliminaryHearingWithMembers\":null,"
                + "\"preliminaryHearingWithMembersReason\":null,"
                + "\"hearingNotListedListAnyOtherDirections\":null,"
                + "\"etICFinalHearingType\":[\"Video\",\"F2F\"],"
                + "\"etICTypeOfVideoHearingOrder\":null,\"etICTypeOfF2fHearingOrder\":null,"
                + "\"etICHearingOrderBUCompliance\":null,"
                + "\"etICFinalHearingLength\":\"1\","
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

    @Test
    void getDocumentRequestSC_withValidCaseData_And_FinalListHearing_returnsExpectedJson()
            throws JsonProcessingException {
        CaseData caseDataScotland = CaseDataBuilder.builder().build();
        setCaseDataValues(caseDataScotland);
        caseDataScotland.setEtICHearingNotListedListUpdated(Collections.singletonList("List for final hearing"));
        caseDataScotland.setEtICHearingNotListedListForFinalHearingUpdated(populateFinalHearingUpdated());
        caseDataScotland.getEtICHearingNotListedListForFinalHearingUpdated()
                .setEtICFinalHearingIsEJSitAloneFurtherDetails("Test SC - EJ Sit Alone Further Details");
        String documentRequest = InitialConsiderationHelper.getDocumentRequest(caseDataScotland,
                "key", "ET_Scotland");

        String expected = "{\"accessKey\":\"key\",\"templateName\":\"EM-TRB-SCO-ENG-02204.docx\","
                + "\"outputName\":\"Initial Consideration.pdf\",\"data\":{\"caseNumber\":\"6000001/2024\","
                + "\"issuesJurisdiction\":\"No\",\"issuesJurCodesGiveDetails\":null,\"canProceed\":\"Yes\","
                + "\"hearingAlreadyListed\":\"No\",\"hearingListed\":null,\"hearingPostpone\":null,"
                + "\"hearingExtend\":null,\"hearingConvertFinal\":null,\"hearingConvertF2f\":null,"
                + "\"hearingOther\":null,\"hearingWithJudgeOrMembers\":null,\"hearingWithJudgeOrMembersReason\":null,"
                + "\"hearingWithJsa\":null,\"hearingWithMembersLabel\":null,\"hearingWithMembers\":null,"
                + "\"hearingWithJudgeOrMembersFurtherDetails\":null,\"otherDirections\":null,"
                + "\"hearingNotListed\":[\"List for final hearing\"],\"cvpHearingType\":null,"
                + "\"cvpFinalDetails\":null,\"cvpPreliminaryDetails\":null,\"cvpPreliminaryYesNo\":null,"
                + "\"preliminaryHearingType\":null,\"preliminaryHearingPurpose\":null,"
                + "\"preliminaryHearingNotice\":null,\"preliminaryHearingLength\":null,"
                + "\"preliminaryHearingLengthType\":null,\"preliminaryHearingWithMembers\":null,"
                + "\"preliminaryHearingWithMembersReason\":null,\"hearingNotListedListAnyOtherDirections\":null,"
                + "\"etICFinalHearingType\":[\"Video\",\"F2F\"],"
                + "\"etICTypeOfVideoHearingOrder\":null,\"etICTypeOfF2fHearingOrder\":null,"
                + "\"etICHearingOrderBUCompliance\":null,"
                + "\"etICFinalHearingLength\":\"1\","
                + "\"etICFinalHearingLengthType\":\"Hours\",\"etICFinalHearingIsEJSitAlone\":\"JSA\","
                + "\"etICFinalHearingIsEJSitAloneReason\":\"Members experience is likely to add significant "
                + "value to the process of adjudication\",\"etICFinalHearingIsEJSitAloneFurtherDetails\":"
                + "\"Test SC - EJ Sit Alone Further Details\","
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
                + "\"icApplicationIssues\":null,"
                + "\"icEmployersContractClaimIssues\":null,\"icClaimProspectIssues\":null,\"icListingIssues\":null,"
                + "\"icDdaDisabilityIssues\":null,\"icOrderForFurtherInformation\":null,"
                + "\"icOtherIssuesOrFinalOrders\":null,"
                + "\"icCompletedBy\":\"A User\",\"icDateCompleted\":\"20 Nov 2024\"}}";
        assertEquals(expected, documentRequest);
    }

    @Test
    void getDocumentRequestSC_withValidCaseData_And_PreliminaryHearingWithMembersReason_returnsExpectedJson()
            throws JsonProcessingException {
        CaseData caseDataScotland = CaseDataBuilder.builder().build();
        setCaseDataValues(caseDataScotland);
        caseDataScotland.setEtICHearingNotListedListUpdated(Collections.singletonList("List for preliminary hearing"));
        caseDataScotland.setEtICHearingNotListedListForPrelimHearingUpdated(populatePreliminaryHearingUpdated());
        caseDataScotland.getEtICHearingNotListedListForPrelimHearingUpdated()
                .setEtICIsPreliminaryHearingWithMembersReason("reasons for requiring members");
        String documentRequest = InitialConsiderationHelper.getDocumentRequest(caseDataScotland,
                "key", "ET_Scotland");

        String expected = "{\"accessKey\":\"key\",\"templateName\":\"EM-TRB-SCO-ENG-02204.docx\","
                + "\"outputName\":\"Initial Consideration.pdf\",\"data\":{\"caseNumber\":\"6000001/2024\","
                + "\"issuesJurisdiction\":\"No\",\"issuesJurCodesGiveDetails\":null,\"canProceed\":\"Yes\","
                + "\"hearingAlreadyListed\":\"No\",\"hearingListed\":null,\"hearingPostpone\":null,"
                + "\"hearingExtend\":null,\"hearingConvertFinal\":null,\"hearingConvertF2f\":null,"
                + "\"hearingOther\":null,\"hearingWithJudgeOrMembers\":null,\"hearingWithJudgeOrMembersReason\":null,"
                + "\"hearingWithJsa\":null,\"hearingWithMembersLabel\":null,\"hearingWithMembers\":null,"
                + "\"hearingWithJudgeOrMembersFurtherDetails\":null,\"otherDirections\":null,"
                + "\"hearingNotListed\":[\"List for preliminary hearing\"],\"cvpHearingType\":null,"
                + "\"cvpFinalDetails\":null,\"cvpPreliminaryDetails\":null,\"cvpPreliminaryYesNo\":null,"
                + "\"preliminaryHearingType\":[\"Video\",\"F2F\"],\"preliminaryHearingPurpose\":[\"Case management\"],"
                + "\"preliminaryHearingNotice\":\"Purpose of preliminary hearing\",\"preliminaryHearingLength\":\"1\","
                + "\"preliminaryHearingLengthType\":\"Hours\",\"preliminaryHearingWithMembers\":\"Yes\","
                + "\"preliminaryHearingWithMembersReason\":\"reasons for requiring members\","
                + "\"hearingNotListedListAnyOtherDirections\":null,"
                + "\"etICFinalHearingType\":null,"
                + "\"etICTypeOfVideoHearingOrder\":null,\"etICTypeOfF2fHearingOrder\":null,"
                + "\"etICHearingOrderBUCompliance\":null,"
                + "\"etICFinalHearingLength\":null,"
                + "\"etICFinalHearingLengthType\":null,\"etICFinalHearingIsEJSitAlone\":null,"
                + "\"etICFinalHearingIsEJSitAloneReason\":null,\"etICFinalHearingIsEJSitAloneFurtherDetails\":"
                + "null,\"udlSitAlone\":null,\"udlReasons\":null,\"udlDisputeOnFacts\":null,"
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
                + "\"icApplicationIssues\":null,"
                + "\"icEmployersContractClaimIssues\":null,\"icClaimProspectIssues\":null,\"icListingIssues\":null,"
                + "\"icDdaDisabilityIssues\":null,\"icOrderForFurtherInformation\":null,"
                + "\"icOtherIssuesOrFinalOrders\":null,"
                + "\"icCompletedBy\":\"A User\",\"icDateCompleted\":\"20 Nov 2024\"}}";
        assertEquals(expected, documentRequest);
    }

    @Test
    void getDocumentRequestSC_withNullCaseData_returnsEmptyJson() throws JsonProcessingException {
        CaseData caseDataSC = new CaseData();
        String documentRequest = InitialConsiderationHelper.getDocumentRequest(caseDataSC, "key",
                "ET_Scotland");
        String formattedNow = LocalDate.now().format(DateTimeFormatter.ofPattern(MONTH_STRING_DATE_FORMAT));
        String expected = "{\"accessKey\":\"key\",\"templateName\":\"EM-TRB-SCO-ENG-02204.docx\","
                + "\"outputName\":\"Initial Consideration.pdf\",\"data\":{\"caseNumber\":null,"
                + "\"issuesJurisdiction\":null,\"issuesJurCodesGiveDetails\":null,\"canProceed\":null,"
                + "\"hearingAlreadyListed\":null,\"hearingListed\":null,\"hearingPostpone\":null,"
                + "\"hearingExtend\":null,\"hearingConvertFinal\":null,\"hearingConvertF2f\":null,"
                + "\"hearingOther\":null,\"hearingWithJudgeOrMembers\":null,\"hearingWithJudgeOrMembersReason\":null,"
                + "\"hearingWithJsa\":null,\"hearingWithMembersLabel\":null,\"hearingWithMembers\":null,"
                + "\"hearingWithJudgeOrMembersFurtherDetails\":null,\"otherDirections\":null,"
                + "\"hearingNotListed\":null,\"cvpHearingType\":null,\"cvpFinalDetails\":null,"
                + "\"cvpPreliminaryDetails\":null,\"cvpPreliminaryYesNo\":null,\"preliminaryHearingType\":null,"
                + "\"preliminaryHearingPurpose\":null,\"preliminaryHearingNotice\":null,"
                + "\"preliminaryHearingLength\":null,\"preliminaryHearingLengthType\":null,"
                + "\"preliminaryHearingWithMembers\":null,\"preliminaryHearingWithMembersReason\":null,"
                + "\"hearingNotListedListAnyOtherDirections\":null,\"etICFinalHearingType\":null,"
                + "\"etICTypeOfVideoHearingOrder\":null,\"etICTypeOfF2fHearingOrder\":null,"
                + "\"etICHearingOrderBUCompliance\":null,"
                + "\"etICFinalHearingLength\":null,\"etICFinalHearingLengthType\":null,"
                + "\"etICFinalHearingIsEJSitAlone\":null,\"etICFinalHearingIsEJSitAloneReason\":null,"
                + "\"etICFinalHearingIsEJSitAloneFurtherDetails\":null,\"udlSitAlone\":null,\"udlReasons\":null,"
                + "\"udlDisputeOnFacts\":null,\"udlLittleOrNoAgreement\":null,\"udlIssueOfLawArising\":null,"
                + "\"udlViewsOfParties\":null,\"udlNoViewsExpressedByParties\":null,\"udlConcurrentProceedings\":null,"
                + "\"udlOther\":null,\"udlHearingFormat\":null,\"udlCVPIssue\":null,\"udlFinalF2FIssue\":null,"
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
                + "\"icCompletedBy\":null,\"icDateCompleted\":\"" + formattedNow + "\"}}";
        assertEquals(expected, documentRequest);
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

    @Test
    void getFurtherInformationRuleChange_EngWales() throws JsonProcessingException {
        caseData = CaseDataBuilder.builder().build();
        setCaseDataValues(caseData);
        caseData.setEtICFurtherInformation(List.of(ISSUE_RULE_28_NOTICE_AND_ORDER, ISSUE_RULE_27_NOTICE_AND_ORDER));
        String documentRequest = InitialConsiderationHelper.getDocumentRequest(caseData,
                "key", ENGLANDWALES_CASE_TYPE_ID);

        String expected = "{\"accessKey\":\"key\",\"templateName\":\"EM-TRB-EGW-ENG-02203.docx\","
                + "\"outputName\":\"Initial Consideration.pdf\",\"data\":{\"caseNumber\":\"6000001/2024\","
                + "\"issuesJurisdiction\":\"No\",\"issuesJurCodesGiveDetails\":null,\"canProceed\":\"Yes\","
                + "\"hearingAlreadyListed\":\"No\",\"hearingListed\":null,\"hearingPostpone\":null,"
                + "\"hearingExtend\":null,\"hearingConvertFinal\":null,\"hearingConvertF2f\":null,"
                + "\"hearingOther\":null,\"hearingWithJudgeOrMembers\":null,\"hearingWithJudgeOrMembersReason\":[\"\"],"
                + "\"hearingWithJsa\":null,\"hearingWithMembersLabel\":null,\"hearingWithMembers\":null,"
                + "\"hearingWithJudgeOrMembersFurtherDetails\":null,\"otherDirections\":null,"
                + "\"hearingNotListed\":null,\"cvpHearingType\":null,"
                + "\"cvpFinalDetails\":null,\"cvpPreliminaryDetails\":null,\"cvpPreliminaryYesNo\":null,"
                + "\"preliminaryHearingType\":null,\"preliminaryHearingPurpose\":null,"
                + "\"preliminaryHearingNotice\":null,\"preliminaryHearingLength\":null,"
                + "\"preliminaryHearingLengthType\":null,\"preliminaryHearingWithMembers\":null,"
                + "\"preliminaryHearingWithMembersReason\":null,"
                + "\"hearingNotListedListAnyOtherDirections\":null,"
                + "\"etICFinalHearingType\":null,"
                + "\"etICTypeOfVideoHearingOrder\":null,\"etICTypeOfF2fHearingOrder\":null,"
                + "\"etICHearingOrderBUCompliance\":null,"
                + "\"etICFinalHearingLength\":null,"
                + "\"etICFinalHearingLengthType\":null,\"etICFinalHearingIsEJSitAlone\":null,"
                + "\"etICFinalHearingIsEJSitAloneReason\":null,"
                + "\"etICFinalHearingIsEJSitAloneFurtherDetails\":null,"
                + "\"udlSitAlone\":null,\"udlReasons\":null,\"udlDisputeOnFacts\":null,"
                + "\"udlLittleOrNoAgreement\":null,\"udlIssueOfLawArising\":null,\"udlViewsOfParties\":null,"
                + "\"udlNoViewsExpressedByParties\":null,\"udlConcurrentProceedings\":null,\"udlOther\":null,"
                + "\"udlHearingFormat\":null,\"udlCVPIssue\":null,\"udlFinalF2FIssue\":null,"
                + "\"udlCheckComplianceOrders\":null,\"hearingNotListedOtherDirections\":null,"
                + "\"furtherInformation\":[\"Issue Rule 29 Notice and order\",\"Issue Rule 28 Notice and order\"],"
                + "\"furtherInfoGiveDetails\":null,\"furtherInfoTimeToComply\":null,"
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
    void getFurtherInformationRuleChange_Scotland() throws JsonProcessingException {
        caseData = CaseDataBuilder.builder().build();
        setCaseDataValues(caseData);
        caseData.setEtICFurtherInformation(List.of(ISSUE_RULE_28_NOTICE_AND_ORDER_SC,
                ISSUE_RULE_27_NOTICE_AND_ORDER_SC));
        String documentRequest = InitialConsiderationHelper.getDocumentRequest(caseData,
                "key", SCOTLAND_CASE_TYPE_ID);

        String expected = "{\"accessKey\":\"key\",\"templateName\":\"EM-TRB-SCO-ENG-02204.docx\","
                + "\"outputName\":\"Initial Consideration.pdf\",\"data\":{\"caseNumber\":\"6000001/2024\","
                + "\"issuesJurisdiction\":\"No\",\"issuesJurCodesGiveDetails\":null,\"canProceed\":\"Yes\","
                + "\"hearingAlreadyListed\":\"No\",\"hearingListed\":null,\"hearingPostpone\":null,"
                + "\"hearingExtend\":null,\"hearingConvertFinal\":null,\"hearingConvertF2f\":null,"
                + "\"hearingOther\":null,\"hearingWithJudgeOrMembers\":null,\"hearingWithJudgeOrMembersReason\":null,"
                + "\"hearingWithJsa\":null,\"hearingWithMembersLabel\":null,\"hearingWithMembers\":null,"
                + "\"hearingWithJudgeOrMembersFurtherDetails\":null,\"otherDirections\":null,"
                + "\"hearingNotListed\":null,\"cvpHearingType\":null,"
                + "\"cvpFinalDetails\":null,\"cvpPreliminaryDetails\":null,\"cvpPreliminaryYesNo\":null,"
                + "\"preliminaryHearingType\":null,\"preliminaryHearingPurpose\":null,"
                + "\"preliminaryHearingNotice\":null,\"preliminaryHearingLength\":null,"
                + "\"preliminaryHearingLengthType\":null,\"preliminaryHearingWithMembers\":null,"
                + "\"preliminaryHearingWithMembersReason\":null,"
                + "\"hearingNotListedListAnyOtherDirections\":null,"
                + "\"etICFinalHearingType\":null,"
                + "\"etICTypeOfVideoHearingOrder\":null,\"etICTypeOfF2fHearingOrder\":null,"
                + "\"etICHearingOrderBUCompliance\":null,"
                + "\"etICFinalHearingLength\":null,"
                + "\"etICFinalHearingLengthType\":null,\"etICFinalHearingIsEJSitAlone\":null,"
                + "\"etICFinalHearingIsEJSitAloneReason\":null,"
                + "\"etICFinalHearingIsEJSitAloneFurtherDetails\":null,"
                + "\"udlSitAlone\":null,\"udlReasons\":null,\"udlDisputeOnFacts\":null,"
                + "\"udlLittleOrNoAgreement\":null,\"udlIssueOfLawArising\":null,\"udlViewsOfParties\":null,"
                + "\"udlNoViewsExpressedByParties\":null,\"udlConcurrentProceedings\":null,\"udlOther\":null,"
                + "\"udlHearingFormat\":null,\"udlCVPIssue\":null,\"udlFinalF2FIssue\":null,"
                + "\"udlCheckComplianceOrders\":null,\"hearingNotListedOtherDirections\":null,"
                + "\"furtherInformation\":[\"Issue Rule 29 Notice and order\",\"Issue Rule 28 Notice and order\"],"
                + "\"furtherInfoGiveDetails\":null,\"furtherInfoTimeToComply\":null,"
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
}
