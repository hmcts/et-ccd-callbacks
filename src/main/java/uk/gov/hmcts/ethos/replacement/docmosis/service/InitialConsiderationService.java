package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.EtICListForFinalHearing;
import uk.gov.hmcts.et.common.model.ccd.EtICListForFinalHearingUpdated;
import uk.gov.hmcts.et.common.model.ccd.EtICListForPreliminaryHearing;
import uk.gov.hmcts.et.common.model.ccd.EtICListForPreliminaryHearingUpdated;
import uk.gov.hmcts.et.common.model.ccd.EtIcudlHearing;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JurisdictionCode;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TableMarkupConstants.DATE_MARKUP;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.MONTH_STRING_DATE_FORMAT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.getHearingDuration;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;

@Service
@RequiredArgsConstructor
public class InitialConsiderationService {

    private final TornadoService tornadoService;

    private static final String RESPONDENT_NAME =
        "| Respondent %s name given | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|In ET1 by claimant | %s|\r\n"
            + "|In ET3 by respondent | %s|\r\n"
            + "\r\n";

    private static final String RESPONDENT_HEARING_PANEL_PREFERENCE =
        "| Respondent %s hearing panel preference | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|Preference | %s|\r\n"
            + "|Reason | %s|\r\n"
            + "\r\n";

    private static final String HEARING_DETAILS =
        "|Hearing details | |\r\n"
            + "|-------------|:------------|\r\n"
            + DATE_MARKUP
            + "|Type | %s|\r\n"
            + "|Duration | %s|";

    private static final String CLAIMANT_HEARING_PANEL_PREFERENCE = """
            |Claimant's hearing panel preference | |
            |-------------|:------------|
            |Panel Preference | %s|
            |Reason for Panel Preference | %s|
            """;

    private static final String CLAIMANT_HEARING_PANEL_PREFERENCE_MISSING =
            String.format(CLAIMANT_HEARING_PANEL_PREFERENCE, "-", "-");

    private static final String JURISDICTION_HEADER = "<h2>Jurisdiction codes</h2><a target=\"_blank\" "
        + "href=\"%s\">View all jurisdiction codes and descriptors (opens in new tab)</a><br><br>";
    private static final String CODES_URL_ENGLAND = "https://judiciary.sharepoint"
        + ".com/:b:/s/empjudgesew/EZowDqUAYpBEl9NkTirLUdYBjXdpi3-7b18HlsDqZNV3xA?e=tR7Wof";
    private static final String CODES_URL_SCOTLAND = "https://judiciary.sharepoint"
        + ".com/:b:/r/sites/ScotlandEJs/Shared%20Documents/Jurisdictional%20Codes%20List/ET%20jurisdiction%20list%20"
        + "(2019).pdf?csf=1&web=1&e=9bCQ8P";
    private static final String HEARING_MISSING = String.format(HEARING_DETAILS, "-", "-", "-");
    private static final String RESPONDENT_MISSING = String.format(RESPONDENT_NAME, "", "", "", "", "");
    private static final String DOCGEN_ERROR = "Failed to generate document for case id: %s";
    private static final String IC_OUTPUT_NAME = "Initial Consideration.pdf";
    private static final String LIST_FOR_PRELIMINARY_HEARING = "List for preliminary hearing";
    private static final String LIST_FOR_FINAL_HEARING = "List for final hearing";
    private static final String UDL_HEARING = "UDL hearing";
    private static final String SEEK_COMMENTS = "Seek comments on the video hearing";
    private static final String HEARING_NOT_LISTED = "Do not list at present (give other directions below)";
    private static final String TELEPHONE = "Telephone";
    Map<String, String> hearingTypeMappings = Map.of(
            "Video hearing", "Video",
            "Final F2F hearings (not Aberdeen)", "F2F"
    );


    /**
     * Creates the respondent detail section for Initial Consideration.
     * Only shows details from the first record
     *
     * @param respondentCollection collection of respondents
     * @return table with respondent details
     */
    public String getRespondentName(List<RespondentSumTypeItem> respondentCollection) {
        if (respondentCollection == null) {
            return RESPONDENT_MISSING;
        }

        IntWrapper respondentCount = new IntWrapper(0);
        return respondentCollection.stream()
                .map(respondent -> String.format(
                        RESPONDENT_NAME,
                         respondentCount.incrementAndReturnValue(),
                        nullCheck(respondent.getValue().getRespondentName()),
                        nullCheck(respondent.getValue().getResponseRespondentName())))
                .collect(Collectors.joining());
    }

    /**
     * Creates the respondent's hearing panel preference section for Initial Consideration.
     * Shows details for each respondent that specified hearing panel preference
     *
     * @param respondentCollection collection of respondents
     * @return table with respondent's hearing panel preference details
     */
    public String getIcRespondentHearingPanelPreference(List<RespondentSumTypeItem> respondentCollection) {
        if (respondentCollection == null) {
            return null;
        }

        IntWrapper respondentCount = new IntWrapper(0);
        return respondentCollection.stream()
                .map(respondent -> String.format(RESPONDENT_HEARING_PANEL_PREFERENCE,
                        respondentCount.incrementAndReturnValue(),
                        Optional.ofNullable(respondent.getValue().getRespondentHearingPanelPreference())
                                .orElse("-"),
                        Optional.ofNullable(respondent.getValue().getRespondentHearingPanelPreferenceReason())
                                .orElse("-")
                        ))
                .collect(Collectors.joining());
    }

    /**
     * Creates hearing detail section for Initial Consideration.
     * Display details of the hearing with the earliest hearing date from the collection of hearings,
     * where the selected hearing is of "Listed" status and hearing listed date is in the future
     *
     * @param hearingCollection the collection of hearings
     * @return return table with details of hearing Listed status
     */
    public String getHearingDetails(List<HearingTypeItem> hearingCollection) {
        if (hearingCollection == null) {
            return HEARING_MISSING;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(MONTH_STRING_DATE_FORMAT);

        return hearingCollection.stream()
            .filter(hearingTypeItem -> hearingTypeItem != null && hearingTypeItem.getValue() != null)
            .map(HearingTypeItem::getValue)
            .filter(
                hearing -> hearing.getHearingDateCollection() != null
                    && !hearing.getHearingDateCollection().isEmpty())
            .min(Comparator.comparing(
                    (HearingType hearing) ->
                        getEarliestHearingDateForListedHearings(hearing.getHearingDateCollection()).orElse(
                            LocalDate.now().plusYears(100))))
            .map(hearing -> getFormattedHearingDetails(hearing, formatter))
            .orElse(HEARING_MISSING);
    }

    private String getFormattedHearingDetails(HearingType hearing, DateTimeFormatter formatter) {
        Optional<LocalDate> earliestHearingDate = getEarliestHearingDateForListedHearings(
            hearing.getHearingDateCollection());
        if (earliestHearingDate.isPresent()) {
            return String.format(HEARING_DETAILS, earliestHearingDate.map(formatter::format).orElse(""),
                hearing.getHearingType(), getHearingDuration(hearing));
        } else {
            return String.format(HEARING_DETAILS, "-", "-", "-");
        }
    }

    public String getClaimantHearingPanelPreference(ClaimantHearingPreference claimantHearingPreference) {
        if (claimantHearingPreference == null) {
            return CLAIMANT_HEARING_PANEL_PREFERENCE_MISSING;
        }
        return String.format(CLAIMANT_HEARING_PANEL_PREFERENCE,
                Optional.ofNullable(claimantHearingPreference.getClaimantHearingPanelPreference()).orElse("-"),
                Optional.ofNullable(claimantHearingPreference.getClaimantHearingPanelPreferenceWhy()).orElse("-")
        );
    }

    /**
     * Select and return the earliest future hearings date for Initial Consideration.
     *
     * @param hearingDates the list of hearing dates in the case
     * @return earliest future hearing date
     */
    public Optional<LocalDate> getEarliestHearingDateForListedHearings(List<DateListedTypeItem> hearingDates) {
        return hearingDates.stream()
        .filter(dateListedTypeItem -> dateListedTypeItem != null && dateListedTypeItem.getValue() != null
            && HEARING_STATUS_LISTED.equals(dateListedTypeItem.getValue().getHearingStatus())
            && LocalDateTime.parse(dateListedTypeItem.getValue().getListedDate())
            .toLocalDate().isAfter(LocalDate.now()))
        .map(DateListedTypeItem::getValue)
        .filter(hearingDate -> hearingDate.getListedDate() != null && !hearingDate.getListedDate().isEmpty())
        .map(hearingDateItem -> LocalDateTime.parse(hearingDateItem.getListedDate()).toLocalDate())
        .min(Comparator.naturalOrder());
    }

    /**
     * Creates the jurisdiction section for Initial Consideration.
     *
     * @param jurisdictionCodes the list of jurisdiction codes assigned to the case
     * @return jurisdiction code section
     */
    public String generateJurisdictionCodesHtml(List<JurCodesTypeItem> jurisdictionCodes, String caseTypeId) {
        if (jurisdictionCodes == null) {
            return "";
        }

        List<String> validJurisdictionCodes = jurisdictionCodes.stream().map(JurCodesTypeItem::getValue)
            .map(JurCodesType::getJuridictionCodesList)
            .filter(code -> EnumUtils.isValidEnum(JurisdictionCode.class, code))
            .toList();

        if (validJurisdictionCodes.isEmpty()) {
            return  "";
        }

        StringBuilder sb = new StringBuilder()
            .append(String.format(JURISDICTION_HEADER, caseTypeId.startsWith(ENGLANDWALES_CASE_TYPE_ID)
                ? CODES_URL_ENGLAND : CODES_URL_SCOTLAND));

        validJurisdictionCodes
            .forEach(codeName -> sb.append("<strong>")
                .append(codeName)
                .append("</strong> - ")
                .append(JurisdictionCode.valueOf(codeName).getDescription())
                .append("<br><br>"));

        return sb.append("<hr>").toString();
    }

    /**
     * This calls the Tornado service to generate the pdf for the ET1 Vetting journey.
     * @param caseData gets the CaseData
     * @param userToken user authentication token
     * @param caseTypeId reference which CaseType the document will be uploaded to
     * @return DocumentInfo which contains the url and markup for the uploaded document
     */
    public DocumentInfo generateDocument(CaseData caseData, String userToken, String caseTypeId) {
        try {
            return tornadoService.generateEventDocument(caseData, userToken, caseTypeId, IC_OUTPUT_NAME);
        } catch (Exception e) {
            throw new DocumentManagementException(String.format(DOCGEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }

    /**
     * Clear value for hidden fields.
     * @param caseData gets the CaseData
     * @param caseTypeId reference which CaseType the document will be uploaded to
     */
    public void clearHiddenValue(CaseData caseData, String caseTypeId) {
        if (caseTypeId.equals(SCOTLAND_CASE_TYPE_ID)) {
            if (YES.equals(caseData.getEtICCanProceed())) {
                removeEtIcCanProceedYesValue(caseData);
                if (YES.equals(caseData.getEtICHearingAlreadyListed())) {
                    removeEtICHearingAlreadyListedYesValue(caseData);
                } else {
                    removeEtICHearingAlreadyListedNoValue(caseData);
                }
            } else {
                removeEtICHearingAlreadyListedYesValue(caseData);
                removeEtICHearingAlreadyListedNoValue(caseData);
            }
        }
    }

    /**
     * Sets etICHearingAlreadyListed if the case has a hearing listed.
     * @param caseData data about the current case
     */
    public void setIsHearingAlreadyListed(CaseData caseData, String caseTypeId) {
        if (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId)) {
            return;
        }
        caseData.setEtICHearingAlreadyListed(HEARING_MISSING.equals(caseData.getEtInitialConsiderationHearing())
            ? NO : YES
        );
    }

    public void mapOldIcHearingNotListedOptionsToNew(CaseData caseData) {
        List<String> etICHearingNotListedList = caseData.getEtICHearingNotListedList();
        List<String> etICHearingNotListedListUpdated = new ArrayList<>();
        if (etICHearingNotListedList.contains(LIST_FOR_PRELIMINARY_HEARING)) {
            etICHearingNotListedListUpdated.add(LIST_FOR_PRELIMINARY_HEARING);
            mapPreliminaryHearingToPreliminaryHearing(caseData);
        }
        if (etICHearingNotListedList.contains(LIST_FOR_FINAL_HEARING)) {
            etICHearingNotListedListUpdated.add(LIST_FOR_FINAL_HEARING);
            mapFinalHearingToFinalHearing(caseData);
        }
        if (etICHearingNotListedList.contains(UDL_HEARING)) {
            etICHearingNotListedListUpdated.add(LIST_FOR_FINAL_HEARING);
            mapUdlHearingToFinalHearing(caseData);
        }
        if (etICHearingNotListedList.contains(SEEK_COMMENTS)) {
            etICHearingNotListedListUpdated.add(HEARING_NOT_LISTED);
        }

        caseData.setEtICHearingNotListedListUpdated(etICHearingNotListedListUpdated);
    }

    private void mapPreliminaryHearingToPreliminaryHearing(CaseData caseData) {
        EtICListForPreliminaryHearing prelimHearing = caseData.getEtICHearingNotListedListForPrelimHearing();
        if (prelimHearing != null) {
            EtICListForPreliminaryHearingUpdated updatedPrelimHearing = new EtICListForPreliminaryHearingUpdated();
            List<String> filteredTypes = prelimHearing.getEtICTypeOfPreliminaryHearing().stream()
                    .filter(type -> !TELEPHONE.equals(type))
                    .toList();
            updatedPrelimHearing.setEtICTypeOfPreliminaryHearing(filteredTypes);

            updatedPrelimHearing.setEtICPurposeOfPreliminaryHearing(prelimHearing.getEtICPurposeOfPreliminaryHearing());
            updatedPrelimHearing.setEtICGiveDetailsOfHearingNotice(prelimHearing.getEtICGiveDetailsOfHearingNotice());
            updatedPrelimHearing.setEtICLengthOfPrelimHearing(prelimHearing.getEtICLengthOfPrelimHearing());
            updatedPrelimHearing.setPrelimHearingLengthNumType(prelimHearing.getPrelimHearingLengthNumType());
            caseData.setEtICHearingNotListedListForPrelimHearingUpdated(updatedPrelimHearing);
        }
    }

    private void mapFinalHearingToFinalHearing(CaseData caseData) {
        EtICListForFinalHearing finalHearing = caseData.getEtICHearingNotListedListForFinalHearing();
        if (finalHearing != null) {
            EtICListForFinalHearingUpdated updatedFinalHearing = new EtICListForFinalHearingUpdated();
            List<String> filteredTypes = finalHearing.getEtICTypeOfFinalHearing().stream()
                    .filter(type -> !TELEPHONE.equals(type))
                    .toList();
            updatedFinalHearing.setEtICTypeOfFinalHearing(filteredTypes);
            updatedFinalHearing.setEtICLengthOfFinalHearing(finalHearing.getEtICLengthOfFinalHearing());
            updatedFinalHearing.setFinalHearingLengthNumType(finalHearing.getFinalHearingLengthNumType());
            caseData.setEtICHearingNotListedListForFinalHearingUpdated(updatedFinalHearing);
        }
    }

    private void mapUdlHearingToFinalHearing(CaseData caseData) {
        EtIcudlHearing udlHearing = caseData.getEtICHearingNotListedUDLHearing();
        if (udlHearing != null) {
            EtICListForFinalHearingUpdated updatedFinalHearing = new EtICListForFinalHearingUpdated();
            List<String> mappedTypes = Stream.of(udlHearing.getEtIcudlHearFormat())
                    .map(type -> hearingTypeMappings.getOrDefault(type, type))
                    .toList();

            updatedFinalHearing.setEtICTypeOfFinalHearing(mappedTypes);

            String ejSitAlone = YES.equals(udlHearing.getEtIcejSitAlone()) ? "JSA" : "With members";
            updatedFinalHearing.setEtICFinalHearingIsEJSitAlone(ejSitAlone);

            updatedFinalHearing.setEtICFinalHearingIsEJSitAlone(udlHearing.getEtIcejSitAlone());
            caseData.setEtICHearingNotListedListForFinalHearingUpdated(updatedFinalHearing);
        }
    }

    public void clearIcHearingNotListedOldValues(CaseData caseData) {
        caseData.setEtICHearingNotListedList(null);
        caseData.setEtICHearingNotListedListForPrelimHearing(null);
        caseData.setEtICHearingNotListedListForFinalHearing(null);
        caseData.setEtICHearingNotListedUDLHearing(null);
        caseData.setEtICHearingNotListedAnyOtherDirections(null);
    }

    private void removeEtIcCanProceedYesValue(CaseData caseData) {
        caseData.setEtICFurtherInformation(null);
        caseData.setEtICFurtherInformationHearingAnyOtherDirections(null);
        caseData.setEtICFurtherInformationGiveDetails(null);
        caseData.setEtICFurtherInformationTimeToComply(null);
        caseData.setEtInitialConsiderationRule27(null);
        caseData.setEtInitialConsiderationRule28(null);
    }

    private void removeEtICHearingAlreadyListedYesValue(CaseData caseData) {
        caseData.setEtICHearingNotListedList(null);
        caseData.setEtICHearingNotListedSeekComments(null);
        caseData.setEtICHearingNotListedListForPrelimHearing(null);
        caseData.setEtICHearingNotListedListForFinalHearing(null);
        caseData.setEtICHearingNotListedUDLHearing(null);
        caseData.setEtICHearingNotListedAnyOtherDirections(null);
    }

    private void removeEtICHearingAlreadyListedNoValue(CaseData caseData) {
        caseData.setEtICHearingListed(null);
        caseData.setEtICExtendDurationGiveDetails(null);
        caseData.setEtICOtherGiveDetails(null);
        caseData.setEtICHearingAnyOtherDirections(null);
        caseData.setEtICPostponeGiveDetails(null);
        caseData.setEtICConvertPreliminaryGiveDetails(null);
        caseData.setEtICConvertF2fGiveDetails(null);
    }
}
