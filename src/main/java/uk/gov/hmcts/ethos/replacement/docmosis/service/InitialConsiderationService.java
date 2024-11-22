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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.CLAIMANT_HEARING_PANEL_PREFERENCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.CLAIMANT_HEARING_PANEL_PREFERENCE_MISSING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.CODES_URL_ENGLAND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.CODES_URL_SCOTLAND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.CVP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.DOC_GEN_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.HEARING_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.HEARING_MISSING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.HEARING_NOT_LISTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.IC_OUTPUT_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.JSA;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.JURISDICTION_HEADER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.LIST_FOR_FINAL_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.LIST_FOR_PRELIMINARY_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.RESPONDENT_HEARING_PANEL_PREFERENCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.RESPONDENT_MISSING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.RESPONDENT_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.SEEK_COMMENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.SEEK_COMMENTS_SC;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.TELEPHONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.UDL_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.VIDEO;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.WITH_MEMBERS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.hearingTypeMappings;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.MONTH_STRING_DATE_FORMAT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.getHearingDuration;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;

@Service
@RequiredArgsConstructor
public class InitialConsiderationService {

    private final TornadoService tornadoService;

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
            throw new DocumentManagementException(String.format(DOC_GEN_ERROR, caseData.getEthosCaseReference()), e);
        }
    }

    /**
     * Clear value for hidden fields.
     * @param caseData gets the CaseData
     */
    public void clearHiddenValue(CaseData caseData) {
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

    public void mapOldIcHearingNotListedOptionsToNew(CaseData caseData, String caseTypeId) {
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
            mapUdlHearingToFinalHearing(caseData, caseTypeId);
        }
        if (etICHearingNotListedList.contains(SEEK_COMMENTS) || etICHearingNotListedList.contains(SEEK_COMMENTS_SC)) {
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
                    .map(type -> CVP.equals(type) ? VIDEO : type)
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
                    .map(type -> CVP.equals(type) ? VIDEO : type)
                    .toList();
            updatedFinalHearing.setEtICTypeOfFinalHearing(filteredTypes);
            updatedFinalHearing.setEtICLengthOfFinalHearing(finalHearing.getEtICLengthOfFinalHearing());
            updatedFinalHearing.setFinalHearingLengthNumType(finalHearing.getFinalHearingLengthNumType());
            caseData.setEtICHearingNotListedListForFinalHearingUpdated(updatedFinalHearing);
        }
    }

    private void mapUdlHearingToFinalHearing(CaseData caseData, String caseTypeId) {
        EtIcudlHearing udlHearing = caseData.getEtICHearingNotListedUDLHearing();
        if (udlHearing != null) {
            EtICListForFinalHearingUpdated updatedFinalHearing = new EtICListForFinalHearingUpdated();
            List<String> mappedTypes = Stream.of(udlHearing.getEtIcudlHearFormat())
                    .map(type -> hearingTypeMappings.getOrDefault(type, type))
                    .toList();

            updatedFinalHearing.setEtICTypeOfFinalHearing(mappedTypes);

            if (caseTypeId.equals(ENGLANDWALES_CASE_TYPE_ID)) {
                String ejSitAlone = YES.equals(udlHearing.getEtIcejSitAlone()) ? JSA : WITH_MEMBERS;
                updatedFinalHearing.setEtICFinalHearingIsEJSitAlone(ejSitAlone);
            } else {
                updatedFinalHearing.setEtICFinalHearingIsEJSitAlone(udlHearing.getEtIcejSitAlone());
            }
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

        // clear new values
        caseData.setEtICHearingNotListedListUpdated(null);
        caseData.setEtICHearingNotListedListForPrelimHearingUpdated(null);
        caseData.setEtICHearingNotListedListForFinalHearingUpdated(null);
    }

    private void removeEtICHearingAlreadyListedNoValue(CaseData caseData) {
        caseData.setEtICHearingListed(null);
        caseData.setEtICExtendDurationGiveDetails(null);
        caseData.setEtICOtherGiveDetails(null);
        caseData.setEtICHearingAnyOtherDirections(null);
        caseData.setEtICPostponeGiveDetails(null);
        caseData.setEtICConvertPreliminaryGiveDetails(null);
        caseData.setEtICConvertF2fGiveDetails(null);
        caseData.setEtICHearingListedAnswers(null);
    }
}
