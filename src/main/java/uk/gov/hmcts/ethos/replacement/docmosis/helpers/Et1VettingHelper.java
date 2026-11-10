package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.VettingJurCodesTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.Et1VettingData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.Et1VettingDocument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1_VETTING;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.STARTING_A_CLAIM;
import static uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType.getSelectedLabel;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.MONTH_STRING_DATE_FORMAT;

/**
 * ET1 Vetting Helper provides methods to assist with the ET1 vetting event.
 */
public final class Et1VettingHelper {

    public static final String DD_MM_YYYY_DATE_FORMAT = "dd-MM-yyyy";
    private static final DateTimeFormatter DD_MM_YYYY_FORMATTER =
            DateTimeFormatter.ofPattern(DD_MM_YYYY_DATE_FORMAT);
    private static final List<DateTimeFormatter> PARSE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DD_MM_YYYY_FORMATTER,
            DateTimeFormatter.ofPattern(MONTH_STRING_DATE_FORMAT, Locale.ENGLISH),
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
    );
    private static final Pattern ISO_DATE_PATTERN =
            Pattern.compile("\\b((?:19|20)\\d{2})-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])\\b");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String TEMPLATE_NAME = "EM-TRB-EGW-ENG-01140.docx";
    public static final String ET1_VETTING_OUTPUT_NAME = "ET1 Vetting - %s.pdf";

    private Et1VettingHelper() {
        // Access through static methods
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * This method generates the data in a JSON format stored in a String which allows Tornado to process the
     * information.
     *
     * @param caseData            contains the data from ET1 Vetting
     * @param userToken           contains the authentication token
     * @param dmStoreDocumentName contains the name of the document to be stored in DM Store
     * @return a string which contains the data in JSON Format which Tornado can use to process and generate the
     *      document
     * @throws JsonProcessingException if the JSON cannot be generated correctly, an error would be thrown. This could
     *                                 be due to an illegal character potentially existing in the data
     */
    public static String getDocumentRequest(CaseData caseData, String userToken, String dmStoreDocumentName)
            throws JsonProcessingException {
        Et1VettingData et1VettingData = Et1VettingData.builder()
                .ethosCaseReference(caseData.getEthosCaseReference())
                .et1VettingCanServeClaimYesOrNo(defaultIfEmpty(caseData.getEt1VettingCanServeClaimYesOrNo(), null))
                .et1VettingCanServeClaimNoReason(normalizeFieldText(caseData.getEt1VettingCanServeClaimNoReason()))
                .et1VettingCanServeClaimGeneralNote(
                        normalizeFieldText(caseData.getEt1VettingCanServeClaimGeneralNote()))
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
                .et1VettingAcasCertGeneralNote(normalizeFieldText(caseData.getEt1VettingAcasCertGeneralNote()))
                .substantiveDefectsList(CollectionUtils.isEmpty(caseData.getSubstantiveDefectsList())
                        ? null
                        : caseData.getSubstantiveDefectsList().toString())
                .et1VettingEra(defaultIfEmpty(caseData.getEt1VettingEra(), null))
                .rule121aTextArea(normalizeFieldText(caseData.getRule121aTextArea()))
                .rule121bTextArea(normalizeFieldText(caseData.getRule121bTextArea()))
                .rule121cTextArea(normalizeFieldText(caseData.getRule121cTextArea()))
                .rule121dTextArea(normalizeFieldText(caseData.getRule121dTextArea()))
                .rule121daTextArea(normalizeFieldText(caseData.getRule121daTextArea()))
                .rule121eTextArea(normalizeFieldText(caseData.getRule121eTextArea()))
                .rule121fTextArea(normalizeFieldText(caseData.getRule121fTextArea()))
                .et1SubstantiveDefectsGeneralNotes(
                        normalizeFieldText(caseData.getEt1SubstantiveDefectsGeneralNotes()))
                .areTheseCodesCorrect(defaultIfEmpty(caseData.getAreTheseCodesCorrect(), null))
                .codesCorrectGiveDetails(normalizeFieldText(caseData.getCodesCorrectGiveDetails()))
                .vettingJurisdictionCodeCollection(formatJurCodes(caseData.getVettingJurisdictionCodeCollection()))
                .et1JurisdictionCodeGeneralNotes(normalizeFieldText(caseData.getEt1JurisdictionCodeGeneralNotes()))
                .trackAllocationCorrect(defaultIfEmpty(caseData.getIsTrackAllocationCorrect(), null))
                .suggestAnotherTrack(defaultIfEmpty(caseData.getSuggestAnotherTrack(), null))
                .whyChangeTrackAllocation(normalizeFieldText(caseData.getWhyChangeTrackAllocation()))
                .trackAllocationGeneralNotes(normalizeFieldText(caseData.getTrackAllocationGeneralNotes()))
                .correctLocation(defaultIfEmpty(caseData.getIsLocationCorrect(), null))
                .regionalOfficeList(getSelectedLabel(caseData.getRegionalOfficeList()).isPresent()
                        ? defaultIfEmpty(caseData.getRegionalOfficeList().getSelectedLabel(), null)
                        : null)
                .whyChangeOffice(normalizeFieldText(caseData.getWhyChangeOffice()))
                .et1LocationGeneralNotes(normalizeFieldText(caseData.getEt1LocationGeneralNotes()))
                .et1SuggestHearingVenue(defaultIfEmpty(caseData.getEt1SuggestHearingVenue(), null))
                .et1HearingVenues(getSelectedLabel(caseData.getEt1HearingVenues()).isPresent()
                        ? defaultIfEmpty(caseData.getEt1HearingVenues().getSelectedLabel(), null)
                        : null)
                .et1HearingVenueGeneralNotes(normalizeFieldText(caseData.getEt1HearingVenueGeneralNotes()))
                .et1GovOrMajorQuestion(defaultIfEmpty(caseData.getEt1GovOrMajorQuestion(), null))
                .et1ReasonableAdjustmentsQuestion(defaultIfEmpty(caseData.getEt1ReasonableAdjustmentsQuestion(), null))
                .et1ReasonableAdjustmentsTextArea(normalizeFieldText(caseData.getEt1ReasonableAdjustmentsTextArea()))
                .et1VideoHearingQuestion(defaultIfEmpty(caseData.getEt1VideoHearingQuestion(), null))
                .et1VideoHearingTextArea(normalizeFieldText(caseData.getEt1VideoHearingTextArea()))
                .et1FurtherQuestionsGeneralNotes(normalizeFieldText(caseData.getEt1FurtherQuestionsGeneralNotes()))
                .referralToJudgeOrLOList(CollectionUtils.isEmpty(caseData.getReferralToJudgeOrLOList())
                        ? null
                        : caseData.getReferralToJudgeOrLOList().toString())
                .claimOfInterimReliefTextArea(normalizeFieldText(caseData.getAclaimOfInterimReliefTextArea()))
                .statutoryAppealTextArea(normalizeFieldText(caseData.getAstatutoryAppealTextArea()))
                .anAllegationOfCommissionOfSexualOffenceTextArea(normalizeFieldText(
                        caseData.getAnAllegationOfCommissionOfSexualOffenceTextArea()))
                .insolvencyTextArea(normalizeFieldText(caseData.getInsolvencyTextArea()))
                .jurisdictionsUnclearTextArea(normalizeFieldText(caseData.getJurisdictionsUnclearTextArea()))
                .lengthOfServiceTextArea(normalizeFieldText(caseData.getLengthOfServiceTextArea()))
                .potentiallyLinkedCasesInTheEcmTextArea(normalizeFieldText(
                        caseData.getPotentiallyLinkedCasesInTheEcmTextArea()))
                .rule50IssuesTextArea(normalizeFieldText(caseData.getRule50IssuesTextArea()))
                .anotherReasonForJudicialReferralTextArea(normalizeFieldText(
                        caseData.getAnotherReasonForJudicialReferralTextArea()))
                .et1JudgeReferralGeneralNotes(normalizeFieldText(caseData.getEt1JudgeReferralGeneralNotes()))
                .referralToREJOrVPList(CollectionUtils.isEmpty(caseData.getReferralToREJOrVPList())
                        ? null
                        : caseData.getReferralToREJOrVPList().toString())
                .vexatiousLitigantOrderTextArea(normalizeFieldText(caseData.getVexatiousLitigantOrderTextArea()))
                .nationalSecurityIssueTextArea(normalizeFieldText(caseData.getAnationalSecurityIssueTextArea()))
                .nationalMultipleOrPresidentialOrderTextArea(normalizeFieldText(
                        caseData.getNationalMultipleOrPresidentialOrderTextArea()))
                .transferToOtherRegionTextArea(normalizeFieldText(caseData.getTransferToOtherRegionTextArea()))
                .serviceAbroadTextArea(normalizeFieldText(caseData.getServiceAbroadTextArea()))
                .sensitiveIssueTextArea(normalizeFieldText(caseData.getAsensitiveIssueTextArea()))
                .anyPotentialConflictTextArea(normalizeFieldText(caseData.getAnyPotentialConflictTextArea()))
                .anotherReasonREJOrVPTextArea(normalizeFieldText(caseData.getAnotherReasonREJOrVPTextArea()))
                .et1REJOrVPReferralGeneralNotes(normalizeFieldText(caseData.getEt1REJOrVPReferralGeneralNotes()))
                .otherReferralList(CollectionUtils.isEmpty(caseData.getOtherReferralList())
                        ? null
                        : caseData.getOtherReferralList().toString())
                .claimOutOfTimeTextArea(normalizeFieldText(caseData.getClaimOutOfTimeTextArea()))
                .multipleClaimTextArea(normalizeFieldText(caseData.getMultipleClaimTextArea()))
                .employmentStatusIssuesTextArea(normalizeFieldText(caseData.getEmploymentStatusIssuesTextArea()))
                .pidJurisdictionRegulatorTextArea(normalizeFieldText(caseData.getPidJurisdictionRegulatorTextArea()))
                .videoHearingPreferenceTextArea(normalizeFieldText(caseData.getVideoHearingPreferenceTextArea()))
                .rule50IssuesForOtherReferralTextArea(
                        normalizeFieldText(caseData.getRule50IssuesForOtherReferralTextArea()))
                .anotherReasonForOtherReferralTextArea(
                        normalizeFieldText(caseData.getAnotherReasonForOtherReferralTextArea()))
                .et1OtherReferralGeneralNotes(normalizeFieldText(caseData.getEt1OtherReferralGeneralNotes()))
                .et1VettingAdditionalInformationTextArea(
                        normalizeFieldText(caseData.getEt1VettingAdditionalInformationTextArea()))
                .et1DateCompleted(formatCompletedDateDdMmYyyy(caseData.getEt1DateCompleted()))

                .et1VettingCompletedBy(defaultIfEmpty(caseData.getEt1VettingCompletedBy(), null))
                .build();

        Et1VettingDocument et1VettingDocument = Et1VettingDocument.builder()
                .accessKey(userToken)
                .outputName(dmStoreDocumentName)
                .templateName(TEMPLATE_NAME)
                .et1VettingData(et1VettingData)
                .build();

        return OBJECT_MAPPER.writeValueAsString(et1VettingDocument);
    }

    /**
     * Converts any {@code yyyy-MM-dd} dates embedded inside text to {@code dd-MM-yyyy}.
     *
     * @param text input text that may contain ISO date strings
     * @return text with {@code yyyy-MM-dd} dates replaced by {@code dd-MM-yyyy}
     */
    public static String normalizeDatesToDdMmYyyy(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }
        return ISO_DATE_PATTERN.matcher(text).replaceAll("$3-$2-$1");
    }

    /**
     * Formats a single date string as {@code dd-MM-yyyy} if parseable, otherwise normalizes embedded dates.
     *
     * @param value raw date value
     * @return formatted date string in {@code dd-MM-yyyy}, or original/blank value
     */
    public static String formatDateDdMmYyyy(String value) {
        if (StringUtils.isBlank(value)) {
            return value;
        }
        String trimmed = value.trim();
        for (DateTimeFormatter formatter : PARSE_FORMATTERS) {
            try {
                return LocalDate.parse(trimmed, formatter).format(DD_MM_YYYY_FORMATTER);
            } catch (DateTimeParseException ignored) {
                // Try next known format
            }
        }
        return normalizeDatesToDdMmYyyy(value);
    }

    /**
     * Formats the completed date as {@code dd-MM-yyyy}, defaulting to today's date when blank.
     *
     * @param value existing completed date string, if any
     * @return completed date in {@code dd-MM-yyyy}
     */
    public static String formatCompletedDateDdMmYyyy(String value) {
        if (StringUtils.isBlank(value)) {
            return LocalDate.now().format(DD_MM_YYYY_FORMATTER);
        }
        return formatDateDdMmYyyy(value);
    }

    private static String normalizeFieldText(String value) {
        String defaulted = defaultIfEmpty(value, null);
        return defaulted == null ? null : normalizeDatesToDdMmYyyy(defaulted);
    }

    private static String formatJurCodes(List<VettingJurCodesTypeItem> vettingJurisdictionCodeCollection) {
        if (CollectionUtils.isEmpty(vettingJurisdictionCodeCollection)) {
            return "";
        }
        return vettingJurisdictionCodeCollection.stream()
                .map(j -> j.getValue().getEt1VettingJurCodeList())
                .collect(Collectors.joining(", "));
    }

    public static void addEt1VettingToDocTab(CaseData caseData) {
        if (caseData.getEt1VettingDocument() == null) {
            return;
        }
        DocumentTypeItem documentTypeItem =
                DocumentHelper.createDocumentTypeItemFromTopLevel(caseData.getEt1VettingDocument(), STARTING_A_CLAIM,
                        ET1_VETTING, null);
        if (caseData.getDocumentCollection() == null) {
            caseData.setDocumentCollection(new ArrayList<>());
        }
        caseData.getDocumentCollection().add(documentTypeItem);
    }

}
