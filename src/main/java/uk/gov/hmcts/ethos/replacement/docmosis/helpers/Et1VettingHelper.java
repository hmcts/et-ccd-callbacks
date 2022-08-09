package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.VettingJurCodesTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.Et1VettingData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.Et1VettingDocument;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType.getSelectedLabel;

/**
 * ET1 Vetting Helper provides methods to assist with the ET1 vetting event.
 */
public class Et1VettingHelper {

    private static final String TEMPLATE_NAME = "Et1VettingHelper.docx";
    private static final String OUTPUT_NAME = "Test.pdf";

    private Et1VettingHelper() {
        // Access through static methods
    }

    /**
     * This method generates the data in a JSON format stored in a String which allows Tornado to process the
     * information.
     * @param caseData contains the data from ET1 Vetting
     * @param userToken contains the authentication token
     * @return a string which contains the data in JSON Format which Tornado can use to process and generate the
     document
     * @throws JsonProcessingException if the JSON cannot be generated correctly, an error would be thrown. This could
     be due to an illegal character potentially existing in the data
     */
    public static String getDocumentRequest(CaseData caseData, String userToken) throws JsonProcessingException {
        Et1VettingData et1VettingData = Et1VettingData.builder()
                .ethosCaseReference(caseData.getEthosCaseReference())
                .et1VettingCanServeClaimYesOrNo(nullChecker(caseData.getEt1VettingCanServeClaimYesOrNo()))
                .et1VettingCanServeClaimNoReason(nullChecker(caseData.getEt1VettingCanServeClaimNoReason()))
                .et1VettingCanServeClaimGeneralNote(nullChecker(caseData.getEt1VettingCanServeClaimGeneralNote()))
                .et1VettingAcasCertIsYesOrNo1(nullChecker(caseData.getEt1VettingAcasCertIsYesOrNo1()))
                .et1VettingAcasCertExemptYesOrNo1(nullChecker(caseData.getEt1VettingAcasCertExemptYesOrNo1()))
                .et1VettingAcasCertIsYesOrNo2(nullChecker(caseData.getEt1VettingAcasCertIsYesOrNo2()))
                .et1VettingAcasCertExemptYesOrNo2(nullChecker(caseData.getEt1VettingAcasCertExemptYesOrNo2()))
                .et1VettingAcasCertIsYesOrNo3(nullChecker(caseData.getEt1VettingAcasCertIsYesOrNo3()))
                .et1VettingAcasCertExemptYesOrNo3(nullChecker(caseData.getEt1VettingAcasCertExemptYesOrNo3()))
                .et1VettingAcasCertIsYesOrNo4(nullChecker(caseData.getEt1VettingAcasCertIsYesOrNo4()))
                .et1VettingAcasCertExemptYesOrNo4(nullChecker(caseData.getEt1VettingAcasCertExemptYesOrNo4()))
                .et1VettingAcasCertIsYesOrNo5(nullChecker(caseData.getEt1VettingAcasCertIsYesOrNo5()))
                .et1VettingAcasCertExemptYesOrNo5(nullChecker(caseData.getEt1VettingAcasCertExemptYesOrNo5()))
                .et1VettingAcasCertIsYesOrNo6(nullChecker(caseData.getEt1VettingAcasCertIsYesOrNo6()))
                .et1VettingAcasCertExemptYesOrNo6(nullChecker(caseData.getEt1VettingAcasCertExemptYesOrNo6()))
                .et1VettingAcasCertGeneralNote(nullChecker(caseData.getEt1VettingAcasCertGeneralNote()))
                .substantiveDefectsList(CollectionUtils.isEmpty(caseData.getSubstantiveDefectsList())
                        ? null
                        : caseData.getSubstantiveDefectsList().toString())
                .rule121aTextArea(nullChecker(caseData.getRule121aTextArea()))
                .rule121bTextArea(nullChecker(caseData.getRule121bTextArea()))
                .rule121cTextArea(nullChecker(caseData.getRule121cTextArea()))
                .rule121dTextArea(nullChecker(caseData.getRule121dTextArea()))
                .rule121daTextArea(nullChecker(caseData.getRule121daTextArea()))
                .rule121eTextArea(nullChecker(caseData.getRule121eTextArea()))
                .rule121fTextArea(nullChecker(caseData.getRule121fTextArea()))
                .et1SubstantiveDefectsGeneralNotes(nullChecker(caseData.getEt1SubstantiveDefectsGeneralNotes()))
                .areTheseCodesCorrect(nullChecker(caseData.getAreTheseCodesCorrect()))
                .codesCorrectGiveDetails(nullChecker(caseData.getCodesCorrectGiveDetails()))
                .vettingJurisdictionCodeCollection(formatJurCodes(caseData.getVettingJurisdictionCodeCollection()))
                .et1JurisdictionCodeGeneralNotes(nullChecker(caseData.getEt1JurisdictionCodeGeneralNotes()))
                .isTrackAllocationCorrect(nullChecker(caseData.getIsTrackAllocationCorrect()))
                .suggestAnotherTrack(nullChecker(caseData.getSuggestAnotherTrack()))
                .whyChangeTrackAllocation(nullChecker(caseData.getWhyChangeTrackAllocation()))
                .trackAllocationGeneralNotes(nullChecker(caseData.getTrackAllocationGeneralNotes()))
                .isLocationCorrect(nullChecker(caseData.getIsLocationCorrect()))
                .regionalOfficeList(getSelectedLabel(caseData.getRegionalOfficeList()).isPresent()
                        ? nullChecker(caseData.getRegionalOfficeList().getSelectedLabel())
                        : null)
                .whyChangeOffice(nullChecker(caseData.getWhyChangeOffice()))
                .et1LocationGeneralNotes(nullChecker(caseData.getEt1LocationGeneralNotes()))
                .et1SuggestHearingVenue(nullChecker(caseData.getEt1SuggestHearingVenue()))
                .et1HearingVenues(getSelectedLabel(caseData.getEt1HearingVenues()).isPresent()
                        ? nullChecker(caseData.getEt1HearingVenues().getSelectedLabel())
                        : null)
                .et1HearingVenueGeneralNotes(nullChecker(caseData.getEt1HearingVenueGeneralNotes()))
                .et1GovOrMajorQuestion(nullChecker(caseData.getEt1GovOrMajorQuestion()))
                .et1ReasonableAdjustmentsQuestion(nullChecker(caseData.getEt1ReasonableAdjustmentsQuestion()))
                .et1ReasonableAdjustmentsTextArea(nullChecker(caseData.getEt1ReasonableAdjustmentsTextArea()))
                .et1VideoHearingQuestion(nullChecker(caseData.getEt1VideoHearingQuestion()))
                .et1VideoHearingTextArea(nullChecker(caseData.getEt1VideoHearingTextArea()))
                .referralToJudgeOrLOList(CollectionUtils.isEmpty(caseData.getReferralToJudgeOrLOList())
                        ? null
                        : caseData.getReferralToJudgeOrLOList().toString())
                .claimOfInterimReliefTextArea(nullChecker(caseData.getAclaimOfInterimReliefTextArea()))
                .statutoryAppealTextArea(nullChecker(caseData.getAstatutoryAppealTextArea()))
                .anAllegationOfCommissionOfSexualOffenceTextArea(nullChecker(
                        caseData.getAnAllegationOfCommissionOfSexualOffenceTextArea()))
                .insolvencyTextArea(nullChecker(caseData.getInsolvencyTextArea()))
                .jurisdictionsUnclearTextArea(nullChecker(caseData.getJurisdictionsUnclearTextArea()))
                .lengthOfServiceTextArea(nullChecker(caseData.getLengthOfServiceTextArea()))
                .potentiallyLinkedCasesInTheEcmTextArea(nullChecker(
                        caseData.getPotentiallyLinkedCasesInTheEcmTextArea()))
                .rule50IssuesTextArea(nullChecker(caseData.getRule50IssuesTextArea()))
                .anotherReasonForJudicialReferralTextArea(nullChecker(
                        caseData.getAnotherReasonForJudicialReferralTextArea()))
                .et1JudgeReferralGeneralNotes(nullChecker(caseData.getEt1JudgeReferralGeneralNotes()))
                .referralToREJOrVPList(CollectionUtils.isEmpty(caseData.getReferralToREJOrVPList())
                        ? null
                        : caseData.getReferralToREJOrVPList().toString())
                .vexatiousLitigantOrderTextArea(nullChecker(caseData.getVexatiousLitigantOrderTextArea()))
                .nationalSecurityIssueTextArea(nullChecker(caseData.getAnationalSecurityIssueTextArea()))
                .nationalMultipleOrPresidentialOrderTextArea(nullChecker(
                        caseData.getNationalMultipleOrPresidentialOrderTextArea()))
                .transferToOtherRegionTextArea(nullChecker(caseData.getTransferToOtherRegionTextArea()))
                .serviceAbroadTextArea(nullChecker(caseData.getServiceAbroadTextArea()))
                .sensitiveIssueTextArea(nullChecker(caseData.getAsensitiveIssueTextArea()))
                .anyPotentialConflictTextArea(nullChecker(caseData.getAnyPotentialConflictTextArea()))
                .anotherReasonREJOrVPTextArea(nullChecker(caseData.getAnotherReasonREJOrVPTextArea()))
                .et1REJOrVPReferralGeneralNotes(nullChecker(caseData.getEt1REJOrVPReferralGeneralNotes()))
                .otherReferralList(CollectionUtils.isEmpty(caseData.getOtherReferralList())
                        ? null
                        : caseData.getOtherReferralList().toString())
                .claimOutOfTimeTextArea(nullChecker(caseData.getClaimOutOfTimeTextArea()))
                .multipleClaimTextArea(nullChecker(caseData.getMultipleClaimTextArea()))
                .employmentStatusIssuesTextArea(nullChecker(caseData.getEmploymentStatusIssuesTextArea()))
                .pidJurisdictionRegulatorTextArea(nullChecker(caseData.getPidJurisdictionRegulatorTextArea()))
                .videoHearingPreferenceTextArea(nullChecker(caseData.getVideoHearingPreferenceTextArea()))
                .rule50IssuesForOtherReferralTextArea(nullChecker(caseData.getRule50IssuesForOtherReferralTextArea()))
                .anotherReasonForOtherReferralTextArea(nullChecker(caseData.getAnotherReasonForOtherReferralTextArea()))
                .et1OtherReferralGeneralNotes(nullChecker(caseData.getEt1OtherReferralGeneralNotes()))
                .et1VettingAdditionalInformationTextArea(
                        nullChecker(caseData.getEt1VettingAdditionalInformationTextArea()))
                .build();

        Et1VettingDocument et1VettingDocument = Et1VettingDocument.builder()
                .accessKey(userToken)
                .outputName(OUTPUT_NAME)
                .templateName(TEMPLATE_NAME)
                .data(et1VettingData)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return objectMapper.writeValueAsString(et1VettingDocument);
    }

    private static String formatJurCodes(List<VettingJurCodesTypeItem> vettingJurisdictionCodeCollection) {
        if (CollectionUtils.isEmpty(vettingJurisdictionCodeCollection)) {
            return "";
        }
        return vettingJurisdictionCodeCollection.stream()
                .map(j -> j.getValue().getEt1VettingJurCodeList())
                .collect(Collectors.joining(", "));
    }
    
    private static String nullChecker(String value) {
        return isNullOrEmpty(value)
                ? null
                : value;
    }
}
