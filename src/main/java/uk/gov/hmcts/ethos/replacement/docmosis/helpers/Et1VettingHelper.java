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

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType.getSelectedLabel;

/**
 * ET1 Vetting Helper provides methods to assist with the ET1 vetting event.
 */
@SuppressWarnings({"PMD.ClassWithOnlyPrivateConstructorsShouldBeFinal", "PMD.LinguisticNaming", "PDM.TooManyFields",
    "PMD.ConfusingTernary", "PMD.ExcessiveMethodLength"})
public class Et1VettingHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TEMPLATE_NAME = "EM-TRB-EGW-ENG-01140.docx";
    private static final String OUTPUT_NAME = "ET1 Vetting.pdf";

    private Et1VettingHelper() {
        // Access through static methods
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
                .et1VettingCanServeClaimYesOrNo(defaultIfEmpty(caseData.getEt1VettingCanServeClaimYesOrNo(), null))
                .et1VettingCanServeClaimNoReason(defaultIfEmpty(caseData.getEt1VettingCanServeClaimNoReason(), null))
                .et1VettingCanServeClaimGeneralNote(
                        defaultIfEmpty(caseData.getEt1VettingCanServeClaimGeneralNote(), null))
                .et1VettingAcasCertIsYesOrNo1(defaultIfEmpty(caseData.getEt1VettingAcasCertIsYesOrNo1(), null))
                .et1VettingAcasCertExemptYesOrNo1(defaultIfEmpty(caseData.getEt1VettingAcasCertExemptYesOrNo1(), null))
                .et1VettingAcasCertIsYesOrNo2(defaultIfEmpty(caseData.getEt1VettingAcasCertIsYesOrNo2(), null))
                .et1VettingAcasCertExemptYesOrNo2(defaultIfEmpty(caseData.getEt1VettingAcasCertExemptYesOrNo2(), null))
                .et1VettingAcasCertIsYesOrNo3(defaultIfEmpty(caseData.getEt1VettingAcasCertIsYesOrNo3(), null))
                .et1VettingAcasCertExemptYesOrNo3(defaultIfEmpty(caseData.getEt1VettingAcasCertExemptYesOrNo3(), null))
                .et1VettingAcasCertIsYesOrNo4(defaultIfEmpty(caseData.getEt1VettingAcasCertIsYesOrNo4(), null))
                .et1VettingAcasCertExemptYesOrNo4(defaultIfEmpty(caseData.getEt1VettingAcasCertExemptYesOrNo4(), null))
                .et1VettingAcasCertIsYesOrNo5(defaultIfEmpty(caseData.getEt1VettingAcasCertIsYesOrNo5(), null))
                .et1VettingAcasCertExemptYesOrNo5(defaultIfEmpty(caseData.getEt1VettingAcasCertExemptYesOrNo5(), null))
                .et1VettingAcasCertIsYesOrNo6(defaultIfEmpty(caseData.getEt1VettingAcasCertIsYesOrNo6(), null))
                .et1VettingAcasCertExemptYesOrNo6(defaultIfEmpty(caseData.getEt1VettingAcasCertExemptYesOrNo6(), null))
                .et1VettingAcasCertGeneralNote(defaultIfEmpty(caseData.getEt1VettingAcasCertGeneralNote(), null))
                .substantiveDefectsList(CollectionUtils.isEmpty(caseData.getSubstantiveDefectsList())
                        ? null
                        : caseData.getSubstantiveDefectsList().toString())
                .rule121aTextArea(defaultIfEmpty(caseData.getRule121aTextArea(), null))
                .rule121bTextArea(defaultIfEmpty(caseData.getRule121bTextArea(), null))
                .rule121cTextArea(defaultIfEmpty(caseData.getRule121cTextArea(), null))
                .rule121dTextArea(defaultIfEmpty(caseData.getRule121dTextArea(), null))
                .rule121daTextArea(defaultIfEmpty(caseData.getRule121daTextArea(), null))
                .rule121eTextArea(defaultIfEmpty(caseData.getRule121eTextArea(), null))
                .rule121fTextArea(defaultIfEmpty(caseData.getRule121fTextArea(), null))
                .et1SubstantiveDefectsGeneralNotes(
                        defaultIfEmpty(caseData.getEt1SubstantiveDefectsGeneralNotes(), null))
                .areTheseCodesCorrect(defaultIfEmpty(caseData.getAreTheseCodesCorrect(), null))
                .codesCorrectGiveDetails(defaultIfEmpty(caseData.getCodesCorrectGiveDetails(), null))
                .vettingJurisdictionCodeCollection(formatJurCodes(caseData.getVettingJurisdictionCodeCollection()))
                .et1JurisdictionCodeGeneralNotes(defaultIfEmpty(caseData.getEt1JurisdictionCodeGeneralNotes(), null))
                .isTrackAllocationCorrect(defaultIfEmpty(caseData.getIsTrackAllocationCorrect(), null))
                .suggestAnotherTrack(defaultIfEmpty(caseData.getSuggestAnotherTrack(), null))
                .whyChangeTrackAllocation(defaultIfEmpty(caseData.getWhyChangeTrackAllocation(), null))
                .trackAllocationGeneralNotes(defaultIfEmpty(caseData.getTrackAllocationGeneralNotes(), null))
                .isLocationCorrect(defaultIfEmpty(caseData.getIsLocationCorrect(), null))
                .regionalOfficeList(getSelectedLabel(caseData.getRegionalOfficeList()).isPresent()
                        ? defaultIfEmpty(caseData.getRegionalOfficeList().getSelectedLabel(), null)
                        : null)
                .whyChangeOffice(defaultIfEmpty(caseData.getWhyChangeOffice(), null))
                .et1LocationGeneralNotes(defaultIfEmpty(caseData.getEt1LocationGeneralNotes(), null))
                .et1SuggestHearingVenue(defaultIfEmpty(caseData.getEt1SuggestHearingVenue(), null))
                .et1HearingVenues(getSelectedLabel(caseData.getEt1HearingVenues()).isPresent()
                        ? defaultIfEmpty(caseData.getEt1HearingVenues().getSelectedLabel(), null)
                        : null)
                .et1HearingVenueGeneralNotes(defaultIfEmpty(caseData.getEt1HearingVenueGeneralNotes(), null))
                .et1GovOrMajorQuestion(defaultIfEmpty(caseData.getEt1GovOrMajorQuestion(), null))
                .et1ReasonableAdjustmentsQuestion(defaultIfEmpty(caseData.getEt1ReasonableAdjustmentsQuestion(), null))
                .et1ReasonableAdjustmentsTextArea(defaultIfEmpty(caseData.getEt1ReasonableAdjustmentsTextArea(), null))
                .et1VideoHearingQuestion(defaultIfEmpty(caseData.getEt1VideoHearingQuestion(), null))
                .et1VideoHearingTextArea(defaultIfEmpty(caseData.getEt1VideoHearingTextArea(), null))
                .et1FurtherQuestionsGeneralNotes(defaultIfEmpty(caseData.getEt1FurtherQuestionsGeneralNotes(), null))
                .referralToJudgeOrLOList(CollectionUtils.isEmpty(caseData.getReferralToJudgeOrLOList())
                        ? null
                        : caseData.getReferralToJudgeOrLOList().toString())
                .claimOfInterimReliefTextArea(defaultIfEmpty(caseData.getAclaimOfInterimReliefTextArea(), null))
                .statutoryAppealTextArea(defaultIfEmpty(caseData.getAstatutoryAppealTextArea(), null))
                .anAllegationOfCommissionOfSexualOffenceTextArea(defaultIfEmpty(
                        caseData.getAnAllegationOfCommissionOfSexualOffenceTextArea(), null))
                .insolvencyTextArea(defaultIfEmpty(caseData.getInsolvencyTextArea(), null))
                .jurisdictionsUnclearTextArea(defaultIfEmpty(caseData.getJurisdictionsUnclearTextArea(), null))
                .lengthOfServiceTextArea(defaultIfEmpty(caseData.getLengthOfServiceTextArea(), null))
                .potentiallyLinkedCasesInTheEcmTextArea(defaultIfEmpty(
                        caseData.getPotentiallyLinkedCasesInTheEcmTextArea(), null))
                .rule50IssuesTextArea(defaultIfEmpty(caseData.getRule50IssuesTextArea(), null))
                .anotherReasonForJudicialReferralTextArea(defaultIfEmpty(
                        caseData.getAnotherReasonForJudicialReferralTextArea(), null))
                .et1JudgeReferralGeneralNotes(defaultIfEmpty(caseData.getEt1JudgeReferralGeneralNotes(), null))
                .referralToREJOrVPList(CollectionUtils.isEmpty(caseData.getReferralToREJOrVPList())
                        ? null
                        : caseData.getReferralToREJOrVPList().toString())
                .vexatiousLitigantOrderTextArea(defaultIfEmpty(caseData.getVexatiousLitigantOrderTextArea(), null))
                .nationalSecurityIssueTextArea(defaultIfEmpty(caseData.getAnationalSecurityIssueTextArea(), null))
                .nationalMultipleOrPresidentialOrderTextArea(defaultIfEmpty(
                        caseData.getNationalMultipleOrPresidentialOrderTextArea(), null))
                .transferToOtherRegionTextArea(defaultIfEmpty(caseData.getTransferToOtherRegionTextArea(), null))
                .serviceAbroadTextArea(defaultIfEmpty(caseData.getServiceAbroadTextArea(), null))
                .sensitiveIssueTextArea(defaultIfEmpty(caseData.getAsensitiveIssueTextArea(), null))
                .anyPotentialConflictTextArea(defaultIfEmpty(caseData.getAnyPotentialConflictTextArea(), null))
                .anotherReasonREJOrVPTextArea(defaultIfEmpty(caseData.getAnotherReasonREJOrVPTextArea(), null))
                .et1REJOrVPReferralGeneralNotes(defaultIfEmpty(caseData.getEt1REJOrVPReferralGeneralNotes(), null))
                .otherReferralList(CollectionUtils.isEmpty(caseData.getOtherReferralList())
                        ? null
                        : caseData.getOtherReferralList().toString())
                .claimOutOfTimeTextArea(defaultIfEmpty(caseData.getClaimOutOfTimeTextArea(), null))
                .multipleClaimTextArea(defaultIfEmpty(caseData.getMultipleClaimTextArea(), null))
                .employmentStatusIssuesTextArea(defaultIfEmpty(caseData.getEmploymentStatusIssuesTextArea(), null))
                .pidJurisdictionRegulatorTextArea(defaultIfEmpty(caseData.getPidJurisdictionRegulatorTextArea(), null))
                .videoHearingPreferenceTextArea(defaultIfEmpty(caseData.getVideoHearingPreferenceTextArea(), null))
                .rule50IssuesForOtherReferralTextArea(
                        defaultIfEmpty(caseData.getRule50IssuesForOtherReferralTextArea(), null))
                .anotherReasonForOtherReferralTextArea(
                        defaultIfEmpty(caseData.getAnotherReasonForOtherReferralTextArea(), null))
                .et1OtherReferralGeneralNotes(defaultIfEmpty(caseData.getEt1OtherReferralGeneralNotes(), null))
                .et1VettingAdditionalInformationTextArea(
                        defaultIfEmpty(caseData.getEt1VettingAdditionalInformationTextArea(), null))
                .build();

        Et1VettingDocument et1VettingDocument = Et1VettingDocument.builder()
                .accessKey(userToken)
                .outputName(OUTPUT_NAME)
                .templateName(TEMPLATE_NAME)
                .et1VettingData(et1VettingData)
                .build();

        return OBJECT_MAPPER.writeValueAsString(et1VettingDocument);
    }

    private static String formatJurCodes(List<VettingJurCodesTypeItem> vettingJurisdictionCodeCollection) {
        if (CollectionUtils.isEmpty(vettingJurisdictionCodeCollection)) {
            return "";
        }
        return vettingJurisdictionCodeCollection.stream()
                .map(j -> j.getValue().getEt1VettingJurCodeList())
                .collect(Collectors.joining(", "));
    }

}
