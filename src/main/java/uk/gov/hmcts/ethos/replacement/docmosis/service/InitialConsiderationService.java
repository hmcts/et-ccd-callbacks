package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.EtICListForFinalHearing;
import uk.gov.hmcts.et.common.model.ccd.EtICListForFinalHearingUpdated;
import uk.gov.hmcts.et.common.model.ccd.EtICListForPreliminaryHearing;
import uk.gov.hmcts.et.common.model.ccd.EtICListForPreliminaryHearingUpdated;
import uk.gov.hmcts.et.common.model.ccd.EtIcudlHearing;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JurisdictionCode;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.JurisdictionCodeHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
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
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.HEARING_TYPE_MAPPINGS;
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
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_ET1_IC;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_ET1_VETTING_IC;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_ET3_IC;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_ET3_PROCESSING_IC;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_REFERRALS_IC;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.CASE_DETAILS_URL_PARTIAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ET1_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ET1_VETTING_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ET3_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.ET3_PROCESSING_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.MONTH_STRING_DATE_FORMAT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.NOT_AVAILABLE_FOR_VIDEO_HEARINGS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.REFERRALS_PAGE_FRAGMENT_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.TO_HELP_YOU_COMPLETE_IC_EVENT_LABEL;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.getHearingDuration;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitialConsiderationService {

    private final TornadoService tornadoService;
    private static final String BULLET_POINT = "\n -  ";
    private static final String NEWLINE_FOR_DETAILS = "\n\nDetails: \n";
    private static final String NEWLINE = "\n";

    public void initialiseInitialConsideration(CaseDetails caseDetails) {
        caseDetails.getCaseData().setInitialConsiderationBeforeYouStart(initiateBeforeYouStart(caseDetails));
    }

    private String initiateBeforeYouStart(CaseDetails caseDetails) {
        List<DocumentTypeItem> documentCollection = caseDetails.getCaseData().getDocumentCollection();

        String beforeYouStart = "";
        if (documentCollection != null) {
            //ET1
            String et1Form = documentCollection
                    .stream()
                    .filter(d -> ET1_DOC_TYPE.equals(
                            defaultIfEmpty(d.getValue().getDocumentType(), "")))
                    .map(d -> String.format(BEFORE_LABEL_ET1_IC,
                            DocumentManagementService.createLinkToBinaryDocument(d)))
                    .collect(Collectors.joining());
            //ET1 VETTING
            String et1Vetting  = documentCollection
                    .stream()
                    .filter(d -> ET1_VETTING_DOC_TYPE.equals(
                            defaultIfEmpty(d.getValue().getDocumentType(), "")))
                    .map(d -> String.format(BEFORE_LABEL_ET1_VETTING_IC,
                            DocumentManagementService.createLinkToBinaryDocument(d)))
                    .collect(Collectors.joining());

            //ET3
            String et3Form  = documentCollection
                    .stream()
                    .filter(d -> ET3_DOC_TYPE.equals(
                            defaultIfEmpty(d.getValue().getDocumentType(), "")))
                    .map(d -> String.format(BEFORE_LABEL_ET3_IC,
                            DocumentManagementService.createLinkToBinaryDocument(d)))
                    .collect(Collectors.joining());

            //ET3 PROCESSING
            String et3Processing  = documentCollection
                    .stream()
                    .filter(d -> ET3_PROCESSING_DOC_TYPE.equals(
                            defaultIfEmpty(d.getValue().getDocumentType(), "")))
                    .map(d -> String.format(BEFORE_LABEL_ET3_PROCESSING_IC,
                            DocumentManagementService.createLinkToBinaryDocument(d)))
                    .collect(Collectors.joining());

            //REFERRALS, if any
            String referralLinks = "";
            if (caseDetails.getCaseData().getReferralCollection() != null
                    && !caseDetails.getCaseData().getReferralCollection().isEmpty()) {
                referralLinks =  String.format(BEFORE_LABEL_REFERRALS_IC, CASE_DETAILS_URL_PARTIAL
                        + caseDetails.getCaseId() +  REFERRALS_PAGE_FRAGMENT_ID);
            }
            beforeYouStart = String.format(TO_HELP_YOU_COMPLETE_IC_EVENT_LABEL, et1Form, et1Vetting, et3Form,
                    et3Processing, referralLinks);
        }

        return beforeYouStart;
    }

    public String setRespondentDetails(CaseData caseData) {
        List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();
        IntWrapper respondentCount = new IntWrapper(0);
        StringBuilder respondentDetailsHtmlFragment = new StringBuilder();
        // For each respondent, set the name details and then panel preference details
        if (respondentCollection != null) {
            respondentCollection.forEach(respondentSumType -> {
                int updatedRespondentCount = respondentCount.incrementAndReturnValue();
                // Set respondent name details
                respondentDetailsHtmlFragment.append(getRespondentNameDetails(respondentSumType,
                        updatedRespondentCount));

                // Set respondent panel preference details
                respondentDetailsHtmlFragment.append(String.format(RESPONDENT_HEARING_PANEL_PREFERENCE,
                        Optional.ofNullable(respondentSumType.getValue().getRespondentHearingPanelPreference())
                                .orElse("-"),
                        Optional.ofNullable(respondentSumType.getValue().getRespondentHearingPanelPreferenceReason())
                                .orElse("-")
                ));

                // If Respondent is available for video hearing or not
                if (respondentSumType.getValue() != null) {
                    List<String> hearingRespondent = respondentSumType.getValue().getEt3ResponseHearingRespondent();
                    if (hearingRespondent == null || hearingRespondent.stream().noneMatch(
                            pr -> pr.contains(VIDEO))) {
                        respondentDetailsHtmlFragment.append(NOT_AVAILABLE_FOR_VIDEO_HEARINGS.toUpperCase(Locale.UK));
                    }
                }
            });
        }

        return respondentDetailsHtmlFragment.toString();
    }

    private String getRespondentNameDetails(RespondentSumTypeItem respondent, int currentRespondentCount) {
        if (respondent == null) {
            return RESPONDENT_MISSING;
        }

        return String.format(RESPONDENT_NAME, currentRespondentCount,
                nullCheck(respondent.getValue().getRespondentName()),
                nullCheck(respondent.getValue().getResponseRespondentName()));
    }

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

        return respondentCollection.stream()
                .map(respondent -> String.format(RESPONDENT_HEARING_PANEL_PREFERENCE,
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

        StringBuilder claimantPanelPreferenceHtmlFragment = new StringBuilder();

        claimantPanelPreferenceHtmlFragment.append(String.format(CLAIMANT_HEARING_PANEL_PREFERENCE,
                Optional.ofNullable(claimantHearingPreference.getClaimantHearingPanelPreference()).orElse("-"),
                Optional.ofNullable(claimantHearingPreference.getClaimantHearingPanelPreferenceWhy()).orElse("-")
        ));

        // If Claimant is available for video hearing or not
        boolean isAvailableForVideoHearing = claimantHearingPreference.getHearingPreferences() != null
                && claimantHearingPreference.getHearingPreferences().stream()
                        .anyMatch(hp -> hp != null && hp.contains(VIDEO));
        if (!isAvailableForVideoHearing) {
            claimantPanelPreferenceHtmlFragment.append(NOT_AVAILABLE_FOR_VIDEO_HEARINGS.toUpperCase(Locale.UK));
        }

        return claimantPanelPreferenceHtmlFragment.toString();
    }

    /**
     * Select and return the earliest future hearings date for Initial Consideration.
     *
     * @param hearingDates the list of hearing dates in the case
     * @return earliest future hearing date
     */
    public Optional<LocalDate> getEarliestHearingDateForListedHearings(List<DateListedTypeItem> hearingDates) {
        return hearingDates.stream()
        .filter(dateListedTypeItem -> dateListedTypeItem != null
                && dateListedTypeItem.getValue() != null
                && HEARING_STATUS_LISTED.equals(dateListedTypeItem.getValue().getHearingStatus())
                && LocalDateTime.parse(dateListedTypeItem.getValue().getListedDate())
            .toLocalDate().isAfter(LocalDate.now()))
        .map(DateListedTypeItem::getValue)
        .filter(hearingDate -> hearingDate.getListedDate() != null
                && !hearingDate.getListedDate().isEmpty())
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

        StringBuilder sb = new StringBuilder();

        List<String> validJurisdictionCodes = jurisdictionCodes.stream()
                .map(JurCodesTypeItem::getValue)
                .map(JurCodesType::getJuridictionCodesList)
                .filter(jurisdictionCode -> {
                    String codeTxtOnly = jurisdictionCode.replaceAll("[^a-zA-Z]+", "");
                    return EnumUtils.isValidEnum(JurisdictionCode.class, codeTxtOnly);
                })
                .toList();

        if (validJurisdictionCodes.isEmpty()) {
            return "";
        }

        sb.append(String.format(JURISDICTION_HEADER,
                caseTypeId.startsWith(ENGLANDWALES_CASE_TYPE_ID) ? CODES_URL_ENGLAND : CODES_URL_SCOTLAND));

        validJurisdictionCodes.forEach(
                jurisdictionCode ->
                        JurisdictionCodeHelper.populateCodeNameAndDescriptionHtml(sb, jurisdictionCode));

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
     * @param caseTypeId the case type that is used for applying hearing listed check for Scotland case types only
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
                    .map(type -> HEARING_TYPE_MAPPINGS.getOrDefault(type, type))
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
    
    public void processIcDocumentCollections(CaseData caseData) {
        List<DocumentTypeItem> mergedCollection = new ArrayList<>();
        if (caseData.getIcDocumentCollection1() != null) {
            mergedCollection.addAll(caseData.getIcDocumentCollection1());
        }
        if (caseData.getIcDocumentCollection2() != null) {
            mergedCollection.addAll(caseData.getIcDocumentCollection2());
        }
        if (caseData.getIcDocumentCollection3() != null) {
            mergedCollection.addAll(caseData.getIcDocumentCollection3());
        }
        caseData.setIcAllDocumentCollection(mergedCollection);
    }

    public String composeIcEt1ReferralToJudgeOrLOListWithDetails(CaseData caseData) {
        StringBuilder icEt1ReferralToJudgeOrLOListText = new StringBuilder();

        if (caseData.getReferralToJudgeOrLOList() != null && !caseData.getReferralToJudgeOrLOList().isEmpty()) {
            caseData.getReferralToJudgeOrLOList().forEach(listItem -> {
                icEt1ReferralToJudgeOrLOListText.append(NEWLINE).append(BULLET_POINT);

                switch (listItem) {
                    case "aClaimOfInterimRelief":
                        icEt1ReferralToJudgeOrLOListText.append("A claim of interim relief");
                        if (caseData.getAclaimOfInterimReliefTextArea() != null
                                && !caseData.getAclaimOfInterimReliefTextArea().isEmpty()) {
                            icEt1ReferralToJudgeOrLOListText.append(NEWLINE_FOR_DETAILS)
                                    .append(caseData.getAclaimOfInterimReliefTextArea());
                        }
                        icEt1ReferralToJudgeOrLOListText.append(NEWLINE);
                        break;
                    case "aStatutoryAppeal":
                        icEt1ReferralToJudgeOrLOListText.append("A statutory appeal");
                        if (caseData.getAstatutoryAppealTextArea() != null
                                && !caseData.getAstatutoryAppealTextArea().isEmpty()) {
                            icEt1ReferralToJudgeOrLOListText.append(NEWLINE_FOR_DETAILS)
                                    .append(caseData.getAstatutoryAppealTextArea());
                        }
                        icEt1ReferralToJudgeOrLOListText.append(NEWLINE);
                        break;
                    case "anAllegationOfCommissionOfSexualOffence":
                        icEt1ReferralToJudgeOrLOListText.append("An allegation of the commission of a sexual offence");
                        if (caseData.getAnAllegationOfCommissionOfSexualOffenceTextArea() != null
                                && !caseData.getAnAllegationOfCommissionOfSexualOffenceTextArea().isEmpty()) {
                            icEt1ReferralToJudgeOrLOListText.append(NEWLINE_FOR_DETAILS)
                                    .append(caseData.getAnAllegationOfCommissionOfSexualOffenceTextArea());
                        }
                        icEt1ReferralToJudgeOrLOListText.append(NEWLINE);
                        break;
                    case "insolvency":
                        icEt1ReferralToJudgeOrLOListText.append("Insolvency");
                        if (caseData.getInsolvencyTextArea() != null
                                && !caseData.getInsolvencyTextArea().isEmpty()) {
                            icEt1ReferralToJudgeOrLOListText.append(NEWLINE_FOR_DETAILS)
                                    .append(caseData.getInsolvencyTextArea());
                        }
                        icEt1ReferralToJudgeOrLOListText.append(NEWLINE);
                        break;
                    case "jurisdictionsUnclear":
                        icEt1ReferralToJudgeOrLOListText.append("Jurisdictions unclear");
                        if (caseData.getJurisdictionsUnclearTextArea() != null
                                && !caseData.getJurisdictionsUnclearTextArea().isEmpty()) {
                            icEt1ReferralToJudgeOrLOListText.append(NEWLINE_FOR_DETAILS)
                                    .append(caseData.getJurisdictionsUnclearTextArea());
                        }
                        icEt1ReferralToJudgeOrLOListText.append(NEWLINE);
                        break;
                    case "lengthOfService":
                        icEt1ReferralToJudgeOrLOListText.append("Length of service");
                        if (caseData.getLengthOfServiceTextArea() != null
                                && !caseData.getLengthOfServiceTextArea().isEmpty()) {
                            icEt1ReferralToJudgeOrLOListText.append(NEWLINE_FOR_DETAILS)
                                    .append(caseData.getLengthOfServiceTextArea());
                        }
                        icEt1ReferralToJudgeOrLOListText.append(NEWLINE);
                        break;
                    case "potentiallyLinkedCasesInTheEcm":
                        icEt1ReferralToJudgeOrLOListText.append("Potentially linked cases in the ECM");
                        if (caseData.getPotentiallyLinkedCasesInTheEcmTextArea() != null
                                && !caseData.getPotentiallyLinkedCasesInTheEcmTextArea().isEmpty()) {
                            icEt1ReferralToJudgeOrLOListText.append(NEWLINE_FOR_DETAILS)
                                    .append(caseData.getPotentiallyLinkedCasesInTheEcmTextArea());
                        }
                        icEt1ReferralToJudgeOrLOListText.append(NEWLINE);
                        break;
                    case "rule50Issues":
                        icEt1ReferralToJudgeOrLOListText.append("Rule 49 issues");
                        if (caseData.getRule50IssuesTextArea() != null
                                && !caseData.getRule50IssuesTextArea().isEmpty()) {
                            icEt1ReferralToJudgeOrLOListText.append(NEWLINE_FOR_DETAILS)
                                    .append(caseData.getRule50IssuesTextArea());
                        }
                        icEt1ReferralToJudgeOrLOListText.append(NEWLINE);
                        break;
                    case "anotherReasonForJudicialReferral":
                        icEt1ReferralToJudgeOrLOListText.append("Another reason for judicial referral");
                        if (caseData.getAnotherReasonForJudicialReferralTextArea() != null
                                && !caseData.getAnotherReasonForJudicialReferralTextArea().isEmpty()) {
                            icEt1ReferralToJudgeOrLOListText.append(NEWLINE_FOR_DETAILS)
                                    .append(caseData.getAnotherReasonForJudicialReferralTextArea());
                        }
                        icEt1ReferralToJudgeOrLOListText.append(NEWLINE);
                        break;
                    default:
                        // do nothing
                }
            });
        }
        return icEt1ReferralToJudgeOrLOListText.toString();
    }

    public String composeIcEt1ReferralToREJOrVPListWithDetails(CaseData caseData) {
        StringBuilder icEt1ReferralToREJOrVPListText = new StringBuilder();

        if (caseData.getReferralToREJOrVPList() != null && !caseData.getReferralToREJOrVPList().isEmpty()) {
            caseData.getReferralToREJOrVPList().forEach(referral -> {
                switch (referral) {
                    case "vexatiousLitigantOrder":
                        appendReferralDetails(icEt1ReferralToREJOrVPListText,
                                "A claimant covered by vexatious litigant order",
                                caseData.getVexatiousLitigantOrderTextArea());
                        break;
                    case "aNationalSecurityIssue":
                        appendReferralDetails(icEt1ReferralToREJOrVPListText,
                                "A national security issue",
                                caseData.getAnationalSecurityIssueTextArea());
                        break;
                    case "nationalMultipleOrPresidentialOrder":
                        appendReferralDetails(icEt1ReferralToREJOrVPListText,
                                "A part of national multiple / covered by Presidential case management order",
                                caseData.getNationalMultipleOrPresidentialOrderTextArea());
                        break;
                    case "transferToOtherRegion":
                        appendReferralDetails(icEt1ReferralToREJOrVPListText,
                                "A request for transfer to another ET region",
                                caseData.getTransferToOtherRegionTextArea());
                        break;
                    case "serviceAbroad":
                        appendReferralDetails(icEt1ReferralToREJOrVPListText,
                                "A request for service abroad",
                                caseData.getServiceAbroadTextArea());
                        break;
                    case "aSensitiveIssue":
                        appendReferralDetails(icEt1ReferralToREJOrVPListText,
                                "A sensitive issue which may attract publicity or need early allocation "
                                + "to a specific judge",
                                caseData.getAsensitiveIssueTextArea());
                        break;
                    case "anyPotentialConflict":
                        appendReferralDetails(icEt1ReferralToREJOrVPListText,
                                "Any potential conflict involving judge, non-legal member "
                                + "or HMCTS staff member",
                                caseData.getAnyPotentialConflictTextArea());
                        break;
                    case "anotherReasonREJOrVP":
                        appendReferralDetails(icEt1ReferralToREJOrVPListText,
                                "Another reason for Regional Employment Judge / Vice-President referral",
                                caseData.getAnotherReasonREJOrVPTextArea());
                        break;
                    default:
                        // do nothing
                }
            });
        }
        return icEt1ReferralToREJOrVPListText.toString();
    }

    public String composeIcEt1OtherReferralListDetails(CaseData caseData) {
        StringBuilder icEt1OtherReferralListDetailText = new StringBuilder();
        if (caseData.getOtherReferralList() != null && !caseData.getOtherReferralList().isEmpty()) {
            caseData.getOtherReferralList().forEach(
                    referralToListEntry -> processReferralToREJOrVPListEntries(caseData,
                            referralToListEntry, icEt1OtherReferralListDetailText));
        }
        return  icEt1OtherReferralListDetailText.toString();
    }

    private void processReferralToREJOrVPListEntries(CaseData caseData, String referralToREJOrVPListEntry,
                                                       StringBuilder referralListEntryWithDetailsText) {
        switch (referralToREJOrVPListEntry) {
            case "claimOutOfTime": appendReferralDetails(referralListEntryWithDetailsText,
                        "The whole or any part of the claim is out of time",
                        caseData.getClaimOutOfTimeTextArea());
                break;
            case "multipleClaim": appendReferralDetails(referralListEntryWithDetailsText,
                        "The claim is part of a multiple claim",
                        caseData.getMultipleClaimTextArea());
                break;
            case "employmentStatusIssues": appendReferralDetails(referralListEntryWithDetailsText,
                        "The claim has a potential issue about employment status",
                        caseData.getEmploymentStatusIssuesTextArea());
                break;
            case "pidJurisdictionRegulator": appendReferralDetails(referralListEntryWithDetailsText,
                        "The claim has PID jurisdiction and claimant wants it forwarded to "
                        + "relevant regulator - Box 10.1",
                        caseData.getPidJurisdictionRegulatorTextArea());
                break;
            case "videoHearingPreference": appendReferralDetails(referralListEntryWithDetailsText,
                        "The claimant prefers a video hearing",
                        caseData.getVideoHearingPreferenceTextArea());
                break;
            case "rule50IssuesOtherFactors": appendReferralDetails(referralListEntryWithDetailsText,
                        "The claim has Rule 49 issues",
                        caseData.getRule50IssuesForOtherReferralTextArea());
                break;
            case "otherRelevantFactors": appendReferralDetails(referralListEntryWithDetailsText,
                        "The claim has other relevant factors for judicial referral",
                        caseData.getAnotherReasonForOtherReferralTextArea());
                break;
            default:
                // do nothing
        }
    }

    private void appendReferralDetails(StringBuilder textBuilder, String message, String textAreaContent) {
        textBuilder.append(BULLET_POINT).append(message);
        if (textAreaContent != null && !textAreaContent.isBlank()) {
            textBuilder.append(NEWLINE_FOR_DETAILS).append(textAreaContent);
        }
        textBuilder.append(NEWLINE);
    }

    public String composeIcEt1SubstantiveDefectsDetail(CaseData caseData) {
        StringBuilder icEt1VettingIssuesDetailText = new StringBuilder();
        if (caseData.getSubstantiveDefectsList() != null && !caseData.getSubstantiveDefectsList().isEmpty()) {
            caseData.getSubstantiveDefectsList().forEach(defect -> {
                icEt1VettingIssuesDetailText.append(NEWLINE).append("- ");
                switch (defect) {
                    case "rule121a" :
                        icEt1VettingIssuesDetailText.append(
                                "The tribunal has no jurisdiction to consider - Rule 13(1)(a)").append(NEWLINE);
                        break;
                    case "rule121b":
                        icEt1VettingIssuesDetailText.append("Is in a form which cannot sensibly be responded to or "
                             + "otherwise an abuse of process - Rule 13(1)(b)").append(NEWLINE);
                        break;
                    case "rule121c" :
                        icEt1VettingIssuesDetailText.append("Has neither an EC number nor claims one of the EC "
                             + "exemptions - Rule 13(1)(c)").append(NEWLINE);
                        break;
                    case "rule121d":
                        icEt1VettingIssuesDetailText.append("States that one of the EC exceptions applies but it "
                             + "might not - Rule 13(1)(d)").append(NEWLINE);
                        break;
                    case "rule121 da":
                        icEt1VettingIssuesDetailText.append("Institutes relevant proceedings and the EC number on the "
                             + "claim form does not match the EC number on the Acas certificate - Rule 13(1)(e)"
                        ).append(NEWLINE);
                        break;
                    case "rule121e" :
                        icEt1VettingIssuesDetailText.append("Has a different claimant name on the ET1 to the claimant "
                             + "name on the Acas certificate - Rule 13(1)(f)").append(NEWLINE);
                        break;
                    case "rule121f":
                        icEt1VettingIssuesDetailText.append("Has a different respondent name on the ET1 to the "
                             + "respondent name on the Acas certificate - Rule 13(1)(g)").append(NEWLINE);
                        break;
                    default:
                        // do nothing
                }
            });
        }
        return icEt1VettingIssuesDetailText.toString();
    }

}
