package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.EtICHearingListedAnswers;
import uk.gov.hmcts.et.common.model.ccd.EtICListForFinalHearingUpdated;
import uk.gov.hmcts.et.common.model.ccd.EtICListForPreliminaryHearingUpdated;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void getDocumentRequest_returnsExpectedJsonForEnglandWales() throws JsonProcessingException {
        CaseData caseData = CaseDataBuilder.builder().build();
        setCaseDataValues(caseData);
        caseData.setEtICHearingNotListedListUpdated(Collections.singletonList("List for preliminary hearing"));
        caseData.setEtICHearingNotListedListForPrelimHearingUpdated(populatePreliminaryHearingUpdated());

        String documentRequest = InitialConsiderationHelper.getDocumentRequest(caseData, "key",
                ENGLANDWALES_CASE_TYPE_ID);

        assertNotNull(documentRequest);
        assertTrue(documentRequest.contains("\"templateName\":\"EM-TRB-EGW-ENG-02203.docx\""));
        assertTrue(documentRequest.contains("\"caseNumber\":\"6000001/2024\""));
    }

    @Test
    void getDocumentRequest_returnsExpectedJsonForScotland() throws JsonProcessingException {
        CaseData caseData = CaseDataBuilder.builder().build();
        setCaseDataValues(caseData);
        caseData.setEtICHearingNotListedListUpdated(Collections.singletonList("List for final hearing"));
        caseData.setEtICHearingNotListedListForFinalHearingUpdated(populateFinalHearingUpdated());

        String documentRequest = InitialConsiderationHelper.getDocumentRequest(caseData, "key",
                SCOTLAND_CASE_TYPE_ID);

        assertNotNull(documentRequest);
        assertTrue(documentRequest.contains("\"templateName\":\"EM-TRB-SCO-ENG-02204.docx\""));
        assertTrue(documentRequest.contains("\"caseNumber\":\"6000001/2024\""));
    }

    @Test
    void getDocumentRequest_returnsEmptyJsonWhenCaseDataIsNull() throws JsonProcessingException {
        CaseData caseDataEmpty = new CaseData();

        String documentRequest = InitialConsiderationHelper.getDocumentRequest(caseDataEmpty, "key",
                ENGLANDWALES_CASE_TYPE_ID);

        assertNotNull(documentRequest);
        assertTrue(documentRequest.contains("\"caseNumber\":null"));
        assertTrue(documentRequest.contains("\"templateName\":\"EM-TRB-EGW-ENG-02203.docx\""));
    }

    @Test
    void addToDocumentCollection_doesNotAddWhenDocumentIsNull() {
        CaseData caseDataWithNullDoc = new CaseData();
        caseDataWithNullDoc.setDocumentCollection(new ArrayList<>());

        InitialConsiderationHelper.addToDocumentCollection(caseDataWithNullDoc);

        assertTrue(caseDataWithNullDoc.getDocumentCollection().isEmpty());
    }

    @Test
    void addToDocumentCollection_addsDocumentToCollection() {
        CaseData caseDataWithDoc = new CaseData();
        caseDataWithDoc.setEtInitialConsiderationDocument(Mockito.mock(
                uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType.class));
        caseDataWithDoc.setDocumentCollection(new ArrayList<>());

        InitialConsiderationHelper.addToDocumentCollection(caseDataWithDoc);

        assertEquals(1, caseDataWithDoc.getDocumentCollection().size());
    }

    @Test
    void getSortedEJSitAloneReasons_returnsEmptyListWhenInputIsNull() {
        List<String> result = InitialConsiderationHelper.getSortedEJSitAloneReasons(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getSortedEJSitAloneReasons_sortsReasonsAlphabeticallyWithOtherLast() {
        List<String> reasons = List.of("Reason C", "Other", "Reason A");

        List<String> result = InitialConsiderationHelper.getSortedEJSitAloneReasons(reasons);

        assertEquals(List.of("Reason A", "Reason C", "Other"), result);
    }

    @Test
    void updateHearingWithJudgeOrMembersDetails_returnsEmptyStringWhenAnswersAreNull() {
        CaseData caseData = new CaseData();
        caseData.setEtICHearingListedAnswers(null);

        String result = InitialConsiderationHelper.updateHearingWithJudgeOrMembersDetails(caseData);

        assertEquals("", result);
    }

    @Test
    void updateHearingWithJudgeOrMembersDetails_returnsDetailsForFinalHearingWithJsa() {
        EtICHearingListedAnswers answers = new EtICHearingListedAnswers();
        answers.setEtInitialConsiderationListedHearingType("Final Hearing");
        answers.setEtICIsHearingWithJudgeOrMembers("JSA");
        answers.setEtICIsFinalHearingWithJudgeOrMembersJsaReason(List.of("Reason A", "Other"));
        answers.setEtICJsaFinalHearingReasonOther("Custom Reason");

        CaseData caseData = new CaseData();
        caseData.setEtICHearingListedAnswers(answers);

        String result = InitialConsiderationHelper.updateHearingWithJudgeOrMembersDetails(caseData);

        String expected = """

        - Reason A

        - Other
        Details: Custom Reason
            """;
        assertEquals(expected, result);
    }

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
                + "\"etICFinalHearingIsEJSitAloneReasonYes\":null,\"etICFinalHearingIsEJSitAloneReasonYesOther\":null,"
                + "\"etICFinalHearingIsEJSitAloneReasonNo\":null,\"etICFinalHearingIsEJSitAloneReasonNoOther\":null,"
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
                + "\"etICFinalHearingIsEJSitAloneReasonYes\":null,\"etICFinalHearingIsEJSitAloneReasonYesOther\":null,"
                + "\"etICFinalHearingIsEJSitAloneReasonNo\":null,\"etICFinalHearingIsEJSitAloneReasonNoOther\":null,"
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

    @Test
    void getSortedEJSitAloneReasons_returnsSortedListWhenOtherIsPresent() {
        List<String> reasons = List.of("Reason A", "Other", "Reason B");

        List<String> result = InitialConsiderationHelper.getSortedEJSitAloneReasons(reasons);

        assertEquals(List.of("Reason A", "Reason B", "Other"), result);
    }

    @Test
    void getSortedEJSitAloneReasons_returnsSortedListWhenOtherIsAbsent() {
        List<String> reasons = List.of("Reason C", "Reason A", "Reason B");

        List<String> result = InitialConsiderationHelper.getSortedEJSitAloneReasons(reasons);

        assertEquals(List.of("Reason A", "Reason B", "Reason C"), result);
    }

    @Test
    void getSortedEJSitAloneReasons_returnsNullWhenInputIsEmpty() {
        List<String> reasons = Collections.emptyList();

        List<String> result = InitialConsiderationHelper.getSortedEJSitAloneReasons(reasons);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getSortedEJSitAloneReasons_returnsNullWhenInputIsNull() {
        List<String> result = InitialConsiderationHelper.getSortedEJSitAloneReasons(null);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void getSortedEJSitAloneReasons_handlesCaseInsensitivityForOther() {
        List<String> reasons = List.of("Reason A", "other", "Reason B");

        List<String> result = InitialConsiderationHelper.getSortedEJSitAloneReasons(reasons);

        assertEquals(List.of("Reason A", "Reason B", "other"), result);
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
                + "\"hearingOther\":null,\"hearingWithJudgeOrMembers\":null,\"hearingWithJudgeOrMembersReason\":[\"\"],"
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
                + "\"etICFinalHearingIsEJSitAloneReasonYes\":[],\"etICFinalHearingIsEJSitAloneReasonYesOther\":null,"
                + "\"etICFinalHearingIsEJSitAloneReasonNo\":[],\"etICFinalHearingIsEJSitAloneReasonNoOther\":null,"
                + "\"etICFinalHearingIsEJSitAloneReason\":null,\"etICFinalHearingIsEJSitAloneFurtherDetails\":"
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
                + "\"etICFinalHearingIsEJSitAloneReasonYes\":null,\"etICFinalHearingIsEJSitAloneReasonYesOther\":null,"
                + "\"etICFinalHearingIsEJSitAloneReasonNo\":null,\"etICFinalHearingIsEJSitAloneReasonNoOther\":null,"
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
                + "\"hearingOther\":null,\"hearingWithJudgeOrMembers\":null,\"hearingWithJudgeOrMembersReason\":[\"\"],"
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
                + "\"etICFinalHearingIsEJSitAlone\":null,\"etICFinalHearingIsEJSitAloneReasonYes\":null,"
                + "\"etICFinalHearingIsEJSitAloneReasonYesOther\":null,\"etICFinalHearingIsEJSitAloneReasonNo\":null,"
                + "\"etICFinalHearingIsEJSitAloneReasonNoOther\":null,"
                + "\"etICFinalHearingIsEJSitAloneReason\":null,"
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
                + "\"etICFinalHearingIsEJSitAloneReasonYes\":null,\"etICFinalHearingIsEJSitAloneReasonYesOther\":null,"
                + "\"etICFinalHearingIsEJSitAloneReasonNo\":null,\"etICFinalHearingIsEJSitAloneReasonNoOther\":null,"
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
                + "\"etICFinalHearingIsEJSitAloneReasonYes\":null,\"etICFinalHearingIsEJSitAloneReasonYesOther\":null,"
                + "\"etICFinalHearingIsEJSitAloneReasonNo\":null,\"etICFinalHearingIsEJSitAloneReasonNoOther\":null,"
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
    void updateHearingWithJudgeOrMembersDetails_returnsEmptyString_whenAnswersAreNull() {
        CaseData caseDataWithJsaOrMembers = new CaseData();
        caseDataWithJsaOrMembers.setEtICHearingListedAnswers(null);
        String result = InitialConsiderationHelper.updateHearingWithJudgeOrMembersDetails(caseDataWithJsaOrMembers);

        assertEquals("", result);
    }

    @Test
    void updateHearingWithJudgeOrMembersDetails_returnsEmptyString_whenHearingTypeIsNull() {
        EtICHearingListedAnswers answers = new EtICHearingListedAnswers();
        CaseData caseDataWithEmptyHearing = new CaseData();
        caseDataWithEmptyHearing.setEtICHearingListedAnswers(answers);
        String result = InitialConsiderationHelper.updateHearingWithJudgeOrMembersDetails(caseDataWithEmptyHearing);

        assertEquals("", result);
    }

    @Test
    void updateHearingWithJudgeOrMembersDetails_returnsJsaDetails_whenPreliminaryHearingAndJsa() {
        EtICHearingListedAnswers answers = new EtICHearingListedAnswers();
        answers.setEtInitialConsiderationListedHearingType("Preliminary Hearing(CM)");
        answers.setEtICIsHearingWithJudgeOrMembers("JSA");
        answers.setEtICIsHearingWithJsa("Other");
        answers.setEtICIsHearingWithJsaReasonOther("Custom Reason");

        CaseData caseDataWithPhcm = new CaseData();
        caseDataWithPhcm.setEtICHearingListedAnswers(answers);
        String result = InitialConsiderationHelper.updateHearingWithJudgeOrMembersDetails(caseDataWithPhcm);

        assertEquals("JSA - Custom Reason", result);
    }

    @Test
    void updateHearingWithJudgeOrMembersDetails_returnsWithMembersDetails_whenPreliminaryHearingAndWithMembers() {
        EtICHearingListedAnswers answers = new EtICHearingListedAnswers();
        answers.setEtInitialConsiderationListedHearingType("Preliminary Hearing(CM)");
        answers.setEtICIsHearingWithJudgeOrMembers("With members");
        answers.setEtICIsHearingWithMembers("Reason A");

        CaseData caseDataWithCmph = new CaseData();
        caseDataWithCmph.setEtICHearingListedAnswers(answers);
        String result = InitialConsiderationHelper.updateHearingWithJudgeOrMembersDetails(caseDataWithCmph);

        assertEquals("With members - Reason A", result);
    }

    @Test
    void updateHearingWithJudgeOrMembersDetails_returnsFinalHearingDetails_whenFinalHearingAndJsa() {
        EtICHearingListedAnswers answers = new EtICHearingListedAnswers();
        answers.setEtInitialConsiderationListedHearingType("Final Hearing");
        answers.setEtICIsHearingWithJudgeOrMembers("JSA");
        answers.setEtICIsFinalHearingWithJudgeOrMembersJsaReason(List.of("Reason B", "Other"));
        answers.setEtICJsaFinalHearingReasonOther("Other Reason");

        CaseData caseDataWithFinalHearing = new CaseData();
        caseDataWithFinalHearing.setEtICHearingListedAnswers(answers);

        String result = InitialConsiderationHelper.updateHearingWithJudgeOrMembersDetails(caseDataWithFinalHearing);

        String expected = """
        
        - Reason B
        
        - Other
        Details: Other Reason
            """;
        assertEquals(expected, result);
    }

    @Test
    void updateHearingWithJudgeOrMembersDetails_returnsDefaultDetails_whenOtherHearingType() {
        EtICHearingListedAnswers answers = new EtICHearingListedAnswers();
        answers.setEtInitialConsiderationListedHearingType("Other Hearing");
        answers.setEtICIsHearingWithJudgeOrMembersReason(List.of("Default Normal Reason", "Other"));
        answers.setEtICIsHearingWithJudgeOrMembersReasonOther("Other Default Reason");

        CaseData caseDataWithOtherHearing = new CaseData();
        caseDataWithOtherHearing.setEtICHearingListedAnswers(answers);
        String result = InitialConsiderationHelper.updateHearingWithJudgeOrMembersDetails(caseDataWithOtherHearing);
        String expected = """
        
        - Default Normal Reason
        
        - Other
        Details: Other Default Reason
            """;
        assertEquals(expected, result);
    }

}
