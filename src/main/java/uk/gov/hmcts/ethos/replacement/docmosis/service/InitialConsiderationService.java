package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.Et3VettingType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JurisdictionCode;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.JurisdictionCodeHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MarkdownHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1_VETTING;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3_PROCESSING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.APPLICATIONS_FOR_STRIKE_OUT_OR_DEPOSIT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.CLAIMANT_HEARING_FORMAT_NEITHER_PREFERENCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.CLAIMANT_HEARING_PANEL_PREFERENCE_MISSING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.CODES_URL_ENGLAND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.CODES_URL_SCOTLAND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.CVP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.DETAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.DOC_GEN_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.DOES_THE_RESPONDENT_S_NAME_MATCH;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.DO_WE_HAVE_THE_RESPONDENT_S_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.GIVE_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.HEARING_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.HEARING_FORMAT_PREFERENCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.HEARING_MISSING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.HEARING_NOT_LISTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.HEARING_PANEL_PREFERENCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.HEARING_TYPE_MAPPINGS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.IC_OUTPUT_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.INTERPRETERS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.JSA;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.JURISDICTIONAL_ISSUES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.JURISDICTION_HEADER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.LIST_FOR_FINAL_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.LIST_FOR_PRELIMINARY_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.NEWLINE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.NONE_PROVIDED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.PARTIES_HEARING_FORMAT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.PARTIES_HEARING_PANEL_PREFERENCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.REFERRAL_ISSUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.REQUEST_FOR_ADJUSTMENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.RESPONDENT_MISSING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.RESPONDENT_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.RULE_49;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.SEEK_COMMENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.SEEK_COMMENTS_SC;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.TABLE_END;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.TELEPHONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.TIME_POINTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.UDL_HEARING;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.VIDEO;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.WITH_MEMBERS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_ET1_IC;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_ET1_VETTING_IC;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_ET3_IC;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_ET3_PROCESSING_IC;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.BEFORE_LABEL_REFERRALS_IC;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.CASE_DETAILS_URL_PARTIAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.MONTH_STRING_DATE_FORMAT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.REFERRALS_PAGE_FRAGMENT_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.TO_HELP_YOU_COMPLETE_IC_EVENT_LABEL;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.getHearingDuration;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.nullCheck;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitialConsiderationService {

    private final TornadoService tornadoService;
    private static final String[] HEADER = {"Issue / Question", "Details / Answer"};
    private static final List<String> IC_DOC_TYPES = List.of(ET1, ET1_VETTING, ET3, ET3_PROCESSING);
    private static final Map<String, String> IC_LABELS = Map.of(
            ET1, BEFORE_LABEL_ET1_IC, ET1_VETTING, BEFORE_LABEL_ET1_VETTING_IC,
            ET3, BEFORE_LABEL_ET3_IC, ET3_PROCESSING, BEFORE_LABEL_ET3_PROCESSING_IC
    );

    public void initialiseInitialConsideration(CaseDetails caseDetails) {
        List<DocumentTypeItem> documents = emptyIfNull(caseDetails.getCaseData().getDocumentCollection());

        if (documents.isEmpty()) {
            caseDetails.getCaseData().setInitialConsiderationBeforeYouStart("");
            return;
        }

        Map<String, List<String>> linksByType = documents.stream()
                .filter(item -> item != null && item.getValue() != null)
                .filter(item ->
                        IC_DOC_TYPES.contains(defaultIfEmpty(item.getValue().getDocumentType(), "")))
                .sorted(Comparator.comparingInt(item -> IC_DOC_TYPES.indexOf(
                        defaultIfEmpty(item.getValue().getDocumentType(), "")))).collect(
                                Collectors.groupingBy(item -> item.getValue().getDocumentType(),
                        LinkedHashMap::new, // preserve type order
                        Collectors.mapping(DocumentManagementService::createLinkToBinaryDocument, Collectors.toList())
                ));

        String docLinksMarkUp = formatDocLinks(linksByType);
        String referralLinks = generateReferralLinks(caseDetails);
        String beforeYouStart = String.format(TO_HELP_YOU_COMPLETE_IC_EVENT_LABEL, docLinksMarkUp, referralLinks);
        caseDetails.getCaseData().setInitialConsiderationBeforeYouStart(beforeYouStart);
    }

    private String formatDocLinks(Map<String, List<String>> linksByType) {
        if (linksByType == null || linksByType.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : linksByType.entrySet()) {
            String linkType = entry.getKey();
            List<String> links = entry.getValue();

            if (linkType != null && !linkType.isEmpty() && IC_LABELS.containsKey(linkType)) {
                sb.append(String.format(IC_LABELS.get(linkType), String.join("", links)));
            }
        }

        return sb.toString();
    }

    private String generateReferralLinks(CaseDetails caseDetails) {
        List<?> referralCollection = caseDetails.getCaseData().getReferralCollection();
        if (CollectionUtils.isNotEmpty(referralCollection)) {
            return String.format(BEFORE_LABEL_REFERRALS_IC,
                    CASE_DETAILS_URL_PARTIAL + caseDetails.getCaseId() + REFERRALS_PAGE_FRAGMENT_ID);
        }
        return "";
    }

    public void setHearingRegionAndVenue(CaseData caseData) {
        caseData.setRegionalOffice(caseData.getRegionalOfficeList() != null
                ? caseData.getRegionalOfficeList().getSelectedLabel() : null);
        caseData.setEt1TribunalRegion(caseData.getEt1HearingVenues() != null
                ? caseData.getEt1HearingVenues().getSelectedLabel() : null);
    }

    public String setRespondentDetails(CaseData caseData) {
        // table head section
        StringBuilder respondentDetailsHtmlFragment = new StringBuilder();
        String tableHeaderSection = """
                <br/>
                <table>
                  <thead>
                    <tr>
                        <th colspan="3"><h2>Respondent Name Details</h2></th>
                    </tr>
                    <tr>
                      <th width="25%">Respondent</th>
                      <th width="25%">Name given in ET1</th>
                      <th>Name given in ET3</th>
                    </tr>
                  </thead>
                  <tbody>
                """;

        if (caseData == null || CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return respondentDetailsHtmlFragment
                    .append(tableHeaderSection)
                    .append(RESPONDENT_MISSING)
                    .append(TABLE_END).toString();
        }

        List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();
        IntWrapper respondentCount = new IntWrapper(0);

        // For each respondent, set the name details and then panel preference details
        if (respondentCollection != null) {
            respondentDetailsHtmlFragment.append(tableHeaderSection);
            respondentCollection.forEach(respondentSumType -> {
                if (respondentSumType != null && respondentSumType.getValue() != null
                        && respondentSumType.getValue().getRespondentName() != null
                        && !respondentSumType.getValue().getRespondentName().isEmpty()
                ) {
                    // Set respondent name details
                    respondentDetailsHtmlFragment.append(getRespondentNameDetails(respondentSumType,
                            respondentCount.incrementAndReturnValue()));
                }
            });
        }

        //close table
        respondentDetailsHtmlFragment.append(TABLE_END);
        return respondentDetailsHtmlFragment.toString();
    }

    private String getRespondentNameDetails(RespondentSumTypeItem respondent, int currentRespondentCount) {
        if (respondent == null || respondent.getValue() == null) {
            return RESPONDENT_MISSING;
        }

        return String.format(RESPONDENT_NAME, currentRespondentCount,
                nullCheck(respondent.getValue().getRespondentName()),
                nullCheck(respondent.getValue().getResponseRespondentName()));
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

        StringBuilder hearingPreferencesTable = new StringBuilder();

        hearingPreferencesTable.append("""            
               <table>
               <thead>
               <tr>
               <th colspan="3"><h2>Respondents' Panel Preferences</h2></th>
               </tr>
               <tr>
               <th width="25%">Respondent</th>
               <th width="25%">Preference</th>
               <th>Reason</th>
               </tr>
               <thead>
               <tbody>
               """);

        respondentCollection.stream()
                .filter(respondent -> respondent.getValue() != null)
                .forEach(respondent ->
                        hearingPreferencesTable.append(String.format(HEARING_PANEL_PREFERENCE,
                        respondent.getValue().getRespondentName() != null
                                ? respondent.getValue().getRespondentName() : "-",
                        respondent.getValue()  != null
                                && respondent.getValue().getRespondentHearingPanelPreference() != null
                        ? respondent.getValue().getRespondentHearingPanelPreference() :
                                "-",
                        respondent.getValue() != null
                                && respondent.getValue().getRespondentHearingPanelPreferenceReason() != null
                                ? respondent.getValue().getRespondentHearingPanelPreferenceReason()
                                : "-")
                ));

        hearingPreferencesTable.append(TABLE_END);
        return hearingPreferencesTable.toString();
    }

    /**
     * Creates hearing detail section for Initial Consideration.
     * Display details of the hearing with the earliest hearing date from the collection of hearings,
     * where the selected hearing is of "Listed" status and hearing listed date is in the future
     *
     * @param hearingCollection the collection of hearings
     * @return return table with details of hearing Listed status
     */
    public String getHearingDetails(List<HearingTypeItem> hearingCollection, String caseTypeId) {

        StringBuilder hearingDetailsTable = new StringBuilder();

        hearingDetailsTable.append("""
                <br/>
                <table>
                <thead>
                <tr>
                <th colspan="2"><h2>Listed Hearing Details</h2></th>
                </tr>
                <tr>
                <th width="30%">Aspect</th>
                <th width="70%">Detail</th>
                </tr>
                <thead>
                <tbody>
               """);

        if (hearingCollection == null) {
            return hearingDetailsTable.append(HEARING_MISSING).append(TABLE_END).toString();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(MONTH_STRING_DATE_FORMAT);

        return hearingCollection.stream()
            .filter(hearingTypeItem -> hearingTypeItem != null && hearingTypeItem.getValue() != null)
            .map(HearingTypeItem::getValue)
            .filter(
                hearing -> hearing.getHearingDateCollection() != null
                    && !hearing.getHearingDateCollection().isEmpty())
            .min(Comparator.comparing((HearingType hearing) ->
                            getEarliestHearingDateForListedHearings(hearing.getHearingDateCollection()).orElse(
                            LocalDate.now().plusYears(100))))
            .map(hearing -> getFormattedHearingDetails(hearing, formatter, caseTypeId))
            .orElse(HEARING_MISSING);
    }

    private String getFormattedHearingDetails(HearingType hearing, DateTimeFormatter formatter, String caseType) {
        StringBuilder hearingDetailsTable = new StringBuilder();

        hearingDetailsTable.append("""
                <br/>
                <table>
                <thead>
                <tr>
                <th colspan="2"><h2>Listed Hearing Details</h2></th>
                </tr>
                <tr>
                <th width="30%">Aspect</th>
                <th width="70%">Detail</th>
                </tr>
                <thead>
                <tbody>
               """);

        Optional<LocalDate> earliestHearingDate = getEarliestHearingDateForListedHearings(
            hearing.getHearingDateCollection());
        if (earliestHearingDate.isPresent()) {
            hearingDetailsTable.append(String.format(HEARING_DETAILS,
                    earliestHearingDate.map(formatter::format).orElse(""),
                    getAdjustedHearingTypeName(hearing.getHearingType()),
                    getHearingDuration(hearing),
                    hearing.getHearingFormat() != null
                            ? String.join(", ", hearing.getHearingFormat().stream().toList()) : "-",
                    Optional.ofNullable(hearing.getHearingSitAlone()).orElse("-"),
                    getHearingVenueDetails(hearing, caseType)));
        } else {
            hearingDetailsTable.append(String.format(HEARING_DETAILS, "-", "-", "-", "-", "-", "-"));
        }

        hearingDetailsTable.append(TABLE_END);
        return hearingDetailsTable.toString();
    }

    public static String getAdjustedHearingTypeName(String hearingTypeName) {
        return switch (hearingTypeName) {
            case "Hearing" -> "Final Hearing";
            case "Reconsideration" -> "Reconsideration Hearing";
            case "Remedy" -> "Remedy Hearing";
            default -> hearingTypeName;
        };
    }

    private static String getHearingVenueDetails(HearingType hearing, String caseType) {
        if (hearing == null) {
            return "-";
        }

        if (ENGLANDWALES_CASE_TYPE_ID.equals(caseType)) {
            return hearing.getHearingVenue() != null ? hearing.getHearingVenue().getSelectedLabel() : "-";
        } else if (SCOTLAND_CASE_TYPE_ID.equals(caseType)) {
            //Map Scottish hearing venues
            //Aberdeen, Dundee, Edinburgh, Glasgow, Inverness, Paisley, Stirling
            String hearingVenueScotland = hearing.getHearingVenueScotland();

            switch (hearingVenueScotland) {
                case "Aberdeen" -> {
                    return hearing.getHearingAberdeen().getSelectedLabel();
                }
                case "Dundee" -> {
                    return hearing.getHearingDundee().getSelectedLabel();
                }
                case "Edinburgh" -> {
                    return hearing.getHearingEdinburgh().getSelectedLabel();
                }
                case "Glasgow" -> {
                    return hearing.getHearingGlasgow().getSelectedLabel();
                }
                default -> {
                    return "-";
                }
            }
        } else {
            return "-";
        }
    }

    public String setPartiesHearingPanelPreferenceDetails(CaseData caseData) {

        return String.format(PARTIES_HEARING_PANEL_PREFERENCE,
                getClaimantHearingPanelPreference(caseData.getClaimant(), caseData.getClaimantHearingPreference()),
                getIcRespondentHearingPanelPreference(caseData.getRespondentCollection()));
    }

    public String getClaimantHearingPanelPreference(String claimantName,
                                                    ClaimantHearingPreference claimantHearingPreference) {
        if (claimantHearingPreference == null) {
            return CLAIMANT_HEARING_PANEL_PREFERENCE_MISSING;
        }

        StringBuilder claimantHhearingPreferencesTable = new StringBuilder();

        claimantHhearingPreferencesTable.append("""
               <br/>
               <table>
               <thead>
               <tr>
                <th colspan="3"><h1>Parties Hearing Panel Preferences</h1></th>
               </tr>
               <tr>
               <th colspan="3"><h2>Claimant's Panel Preference</h2></th>
               </tr>
               <tr>
               <th width="25%">Claimant</th>
               <th width="25%">Preference</th>
               <th>Reason</th>
               </tr>
               </thead>
               <tbody>
               """);

        claimantHhearingPreferencesTable.append(String.format(HEARING_PANEL_PREFERENCE,
                claimantName,
                Optional.ofNullable(claimantHearingPreference.getClaimantHearingPanelPreference())
                        .orElse("-"),
                Optional.ofNullable(claimantHearingPreference.getClaimantHearingPanelPreferenceWhy())
                        .orElse("-")));

        claimantHhearingPreferencesTable.append(TABLE_END);

        return claimantHhearingPreferencesTable.toString();
    }

    public String setPartiesHearingFormatDetails(CaseData caseData) {
        return String.format(PARTIES_HEARING_FORMAT,
                getClaimantHearingFormatDetails(caseData),
                getRespondentHearingFormatDetails(caseData));
    }

    public String getRespondentHearingFormatDetails(CaseData caseData) {
        StringBuilder hearingFormatTable = new StringBuilder();
        hearingFormatTable.append("""
          <table>
           <thead>
              <tr>
                <th colspan="2"><h2>Respondents Hearing Format</h2></th>
              </tr>
              <tr>
                <th width="30%"><h3>Respondent</h3></th>
                <th width="70%"><span class="bold">Hearing Format</span></th>
              </tr>
          </thead>
          <tbody>
            """);

        String respondentRepHeaderRow = """
                        <tr>
                        <th width="30%"><h3>Respondent Representative</h3></th>
                        <th width="70%"><span class="bold">Hearing Format</span></th>
                        </tr>
                        """;

        caseData.getRespondentCollection().stream()
                .filter(respondent -> respondent.getValue() != null)
                .forEach(respondent -> {
                    //set respondent hearing format preference
                    if (respondent.getValue().getEt3ResponseHearingRespondent() != null
                            && !respondent.getValue().getEt3ResponseHearingRespondent().isEmpty()) {

                        hearingFormatTable.append(String.format(
                                HEARING_FORMAT_PREFERENCE,
                                respondent.getValue().getRespondentName(),
                                String.join(", ",
                                        respondent.getValue().getEt3ResponseHearingRespondent().stream().toList())
                        ));
                    }

                    //set respondent rep hearing format preference
                    Optional<RepresentedTypeRItem> matchingRespondentRep = caseData.getRepCollection() != null
                            ? caseData.getRepCollection().stream().filter(
                                    r -> r.getValue().getRespRepName().equals(
                                            respondent.getValue().getRespondentName())).findFirst() : Optional.empty();

                    if (matchingRespondentRep.isPresent()
                            && respondent.getValue().getEt3ResponseHearingRepresentative() != null) {
                        hearingFormatTable.append(respondentRepHeaderRow);
                        hearingFormatTable.append(String.format(
                                HEARING_FORMAT_PREFERENCE,
                                matchingRespondentRep.get().getValue().getNameOfRepresentative(),
                                String.join(", ",
                                        respondent.getValue().getEt3ResponseHearingRepresentative().stream().toList())
                        ));
                    }
                });

        hearingFormatTable.append(TABLE_END);

        return hearingFormatTable.toString();
    }

    public String getClaimantHearingFormatDetails(CaseData caseData) {
        //set Claimant Hearing Format details
        StringBuilder hearingFormatTable = new StringBuilder();

        hearingFormatTable.append("""
        <br/>
        <table>
          <thead>
          <tr>
            <th colspan="2"><h1>Parties Hearing Format Details</h1></th>
          </tr>
          <tr>
            <th colspan="2"><h2>Claimant Hearing Format</h2></th>
          </tr>
          <tr>
            <th width="30%">Claimant</th>
            <th width="70%">Hearing Format</th>
          </tr>
          </thead>
          <tbody>
            """);

        // Concatenate all hearing formats for claimant
        if (caseData != null) {
            if (caseData.getClaimantHearingPreference() != null
                    && caseData.getClaimantHearingPreference().getHearingPreferences() != null
                    && !caseData.getClaimantHearingPreference().getHearingPreferences().isEmpty()
                    &&  caseData.getClaimantHearingPreference().getHearingPreferences().contains("Neither")) {

                String reasonDetails = caseData.getClaimantHearingPreference().getHearingAssistance() != null
                    ? caseData.getClaimantHearingPreference().getHearingAssistance() : "-";
                hearingFormatTable.append(String.format(
                        CLAIMANT_HEARING_FORMAT_NEITHER_PREFERENCE,
                        caseData.getClaimant(),
                        "Neither (of Phone or Video)",
                        Optional.of(reasonDetails)
                                .orElse("-")
                ));
            } else {
                if (caseData.getClaimantHearingPreference() != null
                        && caseData.getClaimantHearingPreference().getHearingPreferences() != null
                        && ! caseData.getClaimantHearingPreference().getHearingPreferences().isEmpty()) {
                    String hearingFormates = String.join(", ",
                            caseData.getClaimantHearingPreference().getHearingPreferences());
                    hearingFormatTable.append(String.format(
                        HEARING_FORMAT_PREFERENCE,
                        caseData.getClaimant(),
                            Optional.of(hearingFormates)
                                    .orElse("-"))
                    );
                } else {
                    hearingFormatTable.append(String.format(
                            HEARING_FORMAT_PREFERENCE,
                            caseData.getClaimant(),
                            "-"));
                }
            }
        }

        hearingFormatTable.append(TABLE_END);
        return hearingFormatTable.toString();
    }

    /**
     * Select and return the earliest future hearings date for Initial Consideration.
     *
     * @param hearingDates the list of hearing dates in the case
     * @return earliest future hearing date
     */
    public Optional<LocalDate> getEarliestHearingDateForListedHearings(List<DateListedTypeItem> hearingDates) {
        return HearingsHelper.getEarliestListedFutureHearingDate(hearingDates);
    }

    /**
     * Creates the jurisdiction section for Initial Consideration.
     *
     * @param jurisdictionCodes the list of jurisdiction codes assigned to the case
     * @return jurisdiction code section
     */
    public String generateJurisdictionCodesHtml(List<JurCodesTypeItem> jurisdictionCodes, String caseTypeId) {
        final String format = String.format(JURISDICTION_HEADER,
                caseTypeId.startsWith(ENGLANDWALES_CASE_TYPE_ID) ? CODES_URL_ENGLAND : CODES_URL_SCOTLAND);

        StringBuilder sb = new StringBuilder();
        if (jurisdictionCodes == null) {
            sb.append(format);
            return sb.toString();
        }

        sb.append(format);

        List<String> validJurisdictionCodes = jurisdictionCodes.stream()
                .map(JurCodesTypeItem::getValue)
                .map(JurCodesType::getJuridictionCodesList)
                .filter(jurisdictionCode -> {
                    String codeTxtOnly = jurisdictionCode.replaceAll("[^a-zA-Z]+", "");
                    return EnumUtils.isValidEnum(JurisdictionCode.class, codeTxtOnly);
                }).toList();

        if (!validJurisdictionCodes.isEmpty()) {
            validJurisdictionCodes.forEach(jurisdictionCode ->
                    JurisdictionCodeHelper.populateCodeNameAndDescriptionHtml(sb, jurisdictionCode));
        }

        return sb.toString();
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
     * Clear value for hidden fields and old values.
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
        caseData.setEtICHearingAlreadyListed(HEARING_MISSING.equals(caseData.getEtInitialConsiderationHearing())
            ? NO : YES
        );
    }

    public void clearOldValues(CaseData caseData) {
        clearHiddenValue(caseData);

        //clear old values
        if (caseData.getEtICHearingListedAnswers() != null) {
            caseData.getEtICHearingListedAnswers().setEtInitialConsiderationListedHearingType(null);
            caseData.getEtICHearingListedAnswers().setEtICIsHearingWithJsaReasonOther(null);
            caseData.getEtICHearingListedAnswers().setEtICIsHearingWithMembers(null);

            caseData.getEtICHearingListedAnswers().setEtICJsaFinalHearingReasonOther(null);
            caseData.getEtICHearingListedAnswers().setEtICMembersFinalHearingReasonOther(null);

            caseData.getEtICHearingListedAnswers().setEtICIsHearingWithJudgeOrMembersFurtherDetails(null);
            caseData.getEtICHearingListedAnswers().setEtICIsHearingWithJudgeOrMembersReason(null);
            caseData.getEtICHearingListedAnswers().setEtICIsFinalHearingWithJudgeOrMembersJsaReason(null);
            caseData.getEtICHearingListedAnswers().setEtICIsFinalHearingWithJudgeOrMembersReason(null);

            caseData.getEtICHearingListedAnswers().setEtICIsHearingWithJsa(null);
            caseData.getEtICHearingListedAnswers().setEtICHearingListed(null);
            caseData.getEtICHearingListedAnswers().setEtICIsHearingWithJudgeOrMembers(null);
            caseData.getEtICHearingListedAnswers().setEtICIsHearingWithJudgeOrMembersReasonOther(null);
        }

        caseData.setEtICHearingNotListedListForPrelimHearingUpdated(null);
        caseData.setEtICHearingNotListedListForFinalHearingUpdated(null);
        caseData.setEtICHearingNotListedListUpdated(null);
        caseData.setEtICHearingNotListedListForPrelimHearing(null);
        caseData.setEtICHearingNotListedListForPrelimHearingUpdated(null);
        caseData.setEtICHearingNotListedListForFinalHearing(null);
        caseData.setEtICHearingNotListedUDLHearing(null);
        caseData.setEtICHearingNotListedAnyOtherDirections(null);

    }

    public void mapOldIcHearingNotListedOptionsToNew(CaseData caseData, String caseTypeId) {
        if (CollectionUtils.isNotEmpty(caseData.getEtICHearingNotListedList())) {
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
            if (etICHearingNotListedList.contains(SEEK_COMMENTS)
                    || etICHearingNotListedList.contains(SEEK_COMMENTS_SC)) {
                etICHearingNotListedListUpdated.add(HEARING_NOT_LISTED);
            }

            caseData.setEtICHearingNotListedListUpdated(etICHearingNotListedListUpdated);
        }
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
        caseData.setEtICHearingNotListedListUpdated(null);

        caseData.setEtICHearingNotListedListForPrelimHearing(null);
        caseData.setEtICHearingNotListedListForPrelimHearingUpdated(null);

        caseData.setEtICHearingNotListedListForFinalHearing(null);
        caseData.setEtICHearingNotListedListForFinalHearingUpdated(null);

        caseData.setEtICHearingNotListedUDLHearing(null);
        caseData.setEtICHearingNotListedAnyOtherDirections(null);
        caseData.setEtICHearingNotListedListUpdated(null);
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

    public String setIcEt3VettingIssuesDetailsForEachRespondent(CaseData caseData) {
        if (caseData.getRespondentCollection() == null || caseData.getRespondentCollection().isEmpty()) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        List<String[]> et3VettingIssuesPairsList = new ArrayList<>();

        for (var respondent : caseData.getRespondentCollection()) {
            Et3VettingType et3Vetting = respondent.getValue().getEt3Vetting();
            if (et3Vetting == null) {
                continue;
            }

            addPair(et3VettingIssuesPairsList, "<h3>Respondent " + respondent.getValue().getRespondentName()
                    + "<h3>", "");

            processEt3Response(et3Vetting, et3VettingIssuesPairsList);
            processResponseInTime(et3Vetting, et3VettingIssuesPairsList);

            processRespondentName(et3Vetting, et3VettingIssuesPairsList);
            processAddressMatch(et3Vetting, et3VettingIssuesPairsList);
            processContestClaim(et3Vetting, et3VettingIssuesPairsList);
            processContractClaim(et3Vetting, et3VettingIssuesPairsList);
            processCaseListed(et3Vetting, et3VettingIssuesPairsList);

            processLocationCorrect(et3Vetting, et3VettingIssuesPairsList);
            processRule26(et3Vetting, et3VettingIssuesPairsList);
            processSuggestedIssues(et3Vetting, et3VettingIssuesPairsList);

            addPair(et3VettingIssuesPairsList, "Additional information",
                    defaultIfNull(et3Vetting.getEt3AdditionalInformation()));
        }

        stringBuilder.append(MarkdownHelper.createTwoColumnTable(HEADER, et3VettingIssuesPairsList));

        return MarkdownHelper.detailsWrapper("Details of ET3 Processing Issues", stringBuilder.toString());
    }

    public void processEt3Response(Et3VettingType et3Vetting, List<String[]> pairsList) {
        if (et3Vetting == null) {
            return;
        }

        addPair(pairsList, "Is there an ET3 response?", et3Vetting.getEt3IsThereAnEt3Response());
        if (NO.equals(et3Vetting.getEt3IsThereAnEt3Response())) {
            addPair(pairsList, GIVE_DETAILS, defaultIfNull(et3Vetting.getEt3NoEt3Response()));
            addPair(pairsList, "General notes (No ET3 Response):", defaultIfNull(et3Vetting.getEt3GeneralNotes()));
        }
    }

    private void processRespondentName(Et3VettingType et3Vetting, List<String[]> pairsList) {
        if (et3Vetting == null) {
            return;
        }

        addPair(pairsList, DO_WE_HAVE_THE_RESPONDENT_S_NAME, et3Vetting.getEt3DoWeHaveRespondentsName());
        if (YES.equals(et3Vetting.getEt3DoWeHaveRespondentsName())) {
            addPair(pairsList, DOES_THE_RESPONDENT_S_NAME_MATCH, et3Vetting.getEt3DoesRespondentsNameMatch());
            if (NO.equals(et3Vetting.getEt3DoesRespondentsNameMatch())) {
                addPair(pairsList, GIVE_DETAILS, defaultIfNull(et3Vetting.getEt3RespondentNameMismatchDetails()));
            }
        }
    }

    private void processResponseInTime(Et3VettingType et3Vetting, List<String[]> pairsList) {
        if (et3Vetting == null) {
            return;
        }

        addPair(pairsList, "Did we receive the ET3 response in time?", et3Vetting.getEt3ResponseInTime());
        if (NO.equals(et3Vetting.getEt3ResponseInTime())) {
            addPair(pairsList, GIVE_DETAILS, defaultIfNull(et3Vetting.getEt3ResponseInTimeDetails()));
        }
    }

    private void processAddressMatch(Et3VettingType et3Vetting, List<String[]> pairsList) {
        if (et3Vetting == null) {
            return;
        }

        addPair(pairsList, "Does the respondent's address match?",
                et3Vetting.getEt3DoesRespondentsAddressMatch());
        if (NO.equals(et3Vetting.getEt3DoesRespondentsAddressMatch())) {
            addPair(pairsList, GIVE_DETAILS, defaultIfNull(et3Vetting.getEt3RespondentAddressMismatchDetails()));
        }
    }

    private void processContestClaim(Et3VettingType et3Vetting, List<String[]> pairsList) {
        if (et3Vetting == null) {
            return;
        }

        addPair(pairsList, "Does the respondent wish to contest any part of the claim?",
                et3Vetting.getEt3ContestClaim());
        if (et3Vetting.getEt3ContestClaim() != null) {
            addPair(pairsList, GIVE_DETAILS, defaultIfNull(et3Vetting.getEt3ContestClaimGiveDetails()));
        }
        addPair(pairsList, "General notes (Contest Claim)",
                defaultIfNull(et3Vetting.getEt3GeneralNotesContestClaim()));
    }

    private void processContractClaim(Et3VettingType et3Vetting, List<String[]> pairsList) {
        if (et3Vetting == null) {
            return;
        }

        addPair(pairsList, "Is there an Employer's Contract Claim in section 7 of the ET3 response?",
                et3Vetting.getEt3ContractClaimSection7());
        if (YES.equals(et3Vetting.getEt3ContractClaimSection7())) {
            addPair(pairsList, GIVE_DETAILS, defaultIfNull(et3Vetting.getEt3ContractClaimSection7Details()));
        }
    }

    private void processCaseListed(Et3VettingType et3Vetting, List<String[]> pairsList) {
        if (et3Vetting == null) {
            return;
        }
        addPair(pairsList, "Is the case listed for hearing?", et3Vetting.getEt3IsCaseListedForHearing());
        if (NO.equals(et3Vetting.getEt3IsCaseListedForHearing())) {
            addPair(pairsList, GIVE_DETAILS, defaultIfNull(et3Vetting.getEt3IsCaseListedForHearingDetails()));
        }
    }

    private void processLocationCorrect(Et3VettingType et3Vetting, List<String[]> pairsList) {
        if (et3Vetting == null) {
            return;
        }

        addPair(pairsList, "Is this location correct?", et3Vetting.getEt3IsThisLocationCorrect());
        if (NO.equals(et3Vetting.getEt3IsThisLocationCorrect())) {
            addPair(pairsList, "Regional Office selected:", defaultIfNull(et3Vetting.getEt3RegionalOffice()));
            addPair(pairsList, "Why should we change the office?",
                    defaultIfNull(et3Vetting.getEt3WhyWeShouldChangeTheOffice()));
        }
        addPair(pairsList, "General notes (Location)",
                defaultIfNull(et3Vetting.getEt3GeneralNotesTransferApplication()));
    }

    private void processRule26(Et3VettingType et3Vetting, List<String[]> pairsList) {
        if (et3Vetting == null) {
            return;
        }

        addPair(pairsList, "Are there any issues identified for the judge's initial consideration - prospects "
                + "of claim / response arguable? (Rule 27)", et3Vetting.getEt3Rule26());
        if (YES.equals(et3Vetting.getEt3Rule26())) {
            addPair(pairsList, GIVE_DETAILS, defaultIfNull(et3Vetting.getEt3Rule26Details()));
        }
    }

    private void processSuggestedIssues(Et3VettingType et3Vetting, List<String[]> pairsList) {
        if (et3Vetting == null || et3Vetting.getEt3SuggestedIssues() == null) {
            addPair(pairsList, "Are there any other suggested orders, directions or issues?", NONE_PROVIDED);
            return;
        }

        if (et3Vetting.getEt3SuggestedIssues() != null && !et3Vetting.getEt3SuggestedIssues().isEmpty()) {
            addPair(pairsList, "Are there any other suggested orders, directions or issues?", "");
            et3Vetting.getEt3SuggestedIssues().forEach(issue -> addPair(pairsList, issue,
                    getSuggestedIssueDetails(et3Vetting, issue)));
        }
    }

    private static void addPair(List<String[]> list, String key, String value) {
        if (key != null && value != null) {
            list.add(new String[]{key, value});
        }
    }

    private String defaultIfNull(String value) {
        return value != null ? value : NONE_PROVIDED;
    }

    private String getSuggestedIssueDetails(Et3VettingType et3Vetting, String issue) {
        return switch (issue) {
            case APPLICATIONS_FOR_STRIKE_OUT_OR_DEPOSIT -> et3Vetting.getEt3SuggestedIssuesStrikeOut();
            case INTERPRETERS -> et3Vetting.getEt3SuggestedIssueInterpreters();
            case JURISDICTIONAL_ISSUES -> et3Vetting.getEt3SuggestedIssueJurisdictional();
            case REQUEST_FOR_ADJUSTMENTS -> et3Vetting.getEt3SuggestedIssueAdjustments();
            case RULE_49 -> et3Vetting.getEt3SuggestedIssueRule50();
            case TIME_POINTS -> et3Vetting.getEt3SuggestedIssueTimePoints();
            default -> NONE_PROVIDED;
        };
    }

    public void setEt1VettingAndEt3ProcessingDetails(CaseData caseData, String caseTypeId) {
        if (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId)) {
            // ET1 Vetting Issues
            caseData.setIcEt1VettingIssuesDetail(setIcEt1VettingIssuesDetails(caseData));

            // ET3 Vetting Issues
            caseData.setIcEt3ProcessingIssuesDetail(
                    setIcEt3VettingIssuesDetailsForEachRespondent(caseData));
        } else {
            // ET3 Vetting Issues for Scotland and Wales
            caseData.setIcEt3ProcessingIssuesDetail(null);
            caseData.setIcEt1VettingIssuesDetail(null);
        }
    }

    public String setIcEt1VettingIssuesDetails(CaseData caseData) {

        if (caseData == null) {
            return null;
        }

        StringBuilder et1VettingIssuesTablesMarkup = new StringBuilder();

        //serving claims
        composeServingClaimsTableMarkUp(caseData, et1VettingIssuesTablesMarkup);

        //Substantive Defects
        composeSubstantiveDefectsTableMarkUp(caseData, et1VettingIssuesTablesMarkup);

        //Is track allocation correct?
        composeTrackAllocationTableMarkUp(caseData, et1VettingIssuesTablesMarkup);

        //Is this location correct?
        composeLocationalIssuesTableMarkUp(caseData, et1VettingIssuesTablesMarkup);

        //Do you want to suggest a hearing venue?
        composeHearingVenueIssuesTableMarkUp(caseData, et1VettingIssuesTablesMarkup);

        //respondent type, reasonable adjustments and video hearing related issues
        composeRespondentTypeRelatedIssuesTableMarkUp(caseData, et1VettingIssuesTablesMarkup);

        //Referral to Judge or LO
        composeReferralToJudgeOrLoTableMarkUp(caseData, et1VettingIssuesTablesMarkup);

        //Referral to REJ or VP
        composeReferralToREJOrVPTableMarkUp(caseData, et1VettingIssuesTablesMarkup);

        //Other referrals - commented out as per requirements
        composeIcEt1OtherReferralTableMarkUp(caseData, et1VettingIssuesTablesMarkup);

        return et1VettingIssuesTablesMarkup.toString();
    }

    private static void composeRespondentTypeRelatedIssuesTableMarkUp(CaseData caseData,
                                                                      StringBuilder et1VettingIssuesTablesMarkup) {
        List<String[]> respondentTypeIssuesPairsList = new ArrayList<>();

        // Respondent type
        addPairIfNotNull(respondentTypeIssuesPairsList, "Is the respondent a government agency or a major employer?",
                caseData.getEt1GovOrMajorQuestion());

        // Reasonable adjustments for respondent
        if (caseData.getEt1ReasonableAdjustmentsQuestion() != null) {
            addPair(respondentTypeIssuesPairsList, "Are reasonable adjustments required?",
                    caseData.getEt1ReasonableAdjustmentsQuestion());
            addPairIfNotEmpty(respondentTypeIssuesPairsList, "Give details (Respondent Type)",
                    caseData.getEt1ReasonableAdjustmentsTextArea());
        }

        // Video hearing for respondent
        if (caseData.getEt1VideoHearingQuestion() != null) {

            addPair(respondentTypeIssuesPairsList, "Can the claimant attend a video hearing?",
                    caseData.getEt1VideoHearingQuestion());
            addPairIfNotEmpty(respondentTypeIssuesPairsList, "Give details (Video Hearing)",
                    caseData.getEt1VideoHearingTextArea());
        }

        //General notes
        addPairIfNotEmpty(respondentTypeIssuesPairsList, "General Notes",
                caseData.getEt1FurtherQuestionsGeneralNotes());

        if (!respondentTypeIssuesPairsList.isEmpty()) {
            String table = MarkdownHelper.createTwoColumnTable(
                    new String[]{"Respondent Type Related Issues", DETAILS}, respondentTypeIssuesPairsList);
            et1VettingIssuesTablesMarkup.append(MarkdownHelper.detailsWrapper(
                    "Details of Respondent Type Related Issues", table)).append(NEWLINE);
        }
    }

    private static void addPairIfNotNull(List<String[]> list, String key, String value) {
        if (value != null) {
            addPair(list, key, defaultIfEmpty(value, ""));
        }
    }

    private static void addPairIfNotEmpty(List<String[]> list, String key, String value) {
        if (value != null && !value.isEmpty()) {
            addPair(list, key, defaultIfEmpty(value, ""));
        }
    }

    private static void composeHearingVenueIssuesTableMarkUp(CaseData caseData,
                                                           StringBuilder et1VettingIssuesTablesMarkup) {
        StringBuilder hearingVenueIssueStringBuilder = new StringBuilder();

        if (caseData.getEt1SuggestHearingVenue() != null
                && YES.equals(caseData.getEt1SuggestHearingVenue())) {
            List<String[]> hearingVenueIssuesPairsList = new ArrayList<>();

            addPair(hearingVenueIssuesPairsList, "Do you want to suggest a hearing venue?",
                    defaultIfEmpty(caseData.getEt1SuggestHearingVenue(), ""));

            //et1HearingVenues
            if (caseData.getEt1HearingVenues() != null
                    && !caseData.getEt1HearingVenues().getSelectedCode().isEmpty()) {
                addPair(hearingVenueIssuesPairsList, "Hearing venue selected",
                        defaultIfEmpty(caseData.getEt1HearingVenues().getSelectedLabel(), ""));
            }

            //et1HearingVenues
            if (caseData.getEt1HearingVenueGeneralNotes() != null
                    && !caseData.getEt1HearingVenueGeneralNotes().isEmpty()) {
                addPair(hearingVenueIssuesPairsList, "General Notes (Hearing Venue)",
                        defaultIfEmpty(caseData.getEt1HearingVenueGeneralNotes(), ""));
            }

            if (!hearingVenueIssuesPairsList.isEmpty()) {
                hearingVenueIssueStringBuilder.append(MarkdownHelper.createTwoColumnTable(
                        new String[]{"Hearing Venue Issue", DETAILS},
                        hearingVenueIssuesPairsList));
                et1VettingIssuesTablesMarkup.append(MarkdownHelper.detailsWrapper(
                        "Details of Hearing Venue Issues",
                        hearingVenueIssueStringBuilder.toString()));
                et1VettingIssuesTablesMarkup.append(NEWLINE);
            }
        }
    }

    private static void composeLocationalIssuesTableMarkUp(CaseData caseData,
                                                        StringBuilder et1VettingIssuesTablesMarkup) {
        StringBuilder locationIssueStringBuilder = new StringBuilder();

        if (caseData.getIsLocationCorrect() != null
                && NO.equals(caseData.getIsLocationCorrect())) {
            List<String[]> locationIssuesPairsList = new ArrayList<>();

            addPair(locationIssuesPairsList, "Is this location correct?",
                    defaultIfEmpty(caseData.getIsLocationCorrect(), ""));
            //Newly selected regional office
            if (caseData.getRegionalOfficeList() != null
                    && !caseData.getRegionalOfficeList().getSelectedCode().isEmpty()) {
                addPair(locationIssuesPairsList, "Local or regional office selected",
                        defaultIfEmpty(caseData.getRegionalOfficeList().getSelectedLabel(), ""));
            }
            //Reason for changing regional office
            if (caseData.getWhyChangeOffice() != null
                    && !caseData.getWhyChangeOffice().isEmpty()) {
                addPair(locationIssuesPairsList, "Why should we change the office?",
                        defaultIfEmpty(caseData.getWhyChangeOffice(), ""));
            }
            if (!locationIssuesPairsList.isEmpty()) {
                locationIssueStringBuilder.append(MarkdownHelper.createTwoColumnTable(
                        new String[]{"Locational Issue", DETAILS},
                        locationIssuesPairsList));
                et1VettingIssuesTablesMarkup.append(MarkdownHelper.detailsWrapper(
                        "Details of Locational Issues",
                        locationIssueStringBuilder.toString()));
                et1VettingIssuesTablesMarkup.append(NEWLINE);
            }
        }
    }

    private static void composeServingClaimsTableMarkUp(CaseData caseData,
                                                        StringBuilder et1VettingIssuesTablesMarkup) {
        StringBuilder servingClaimsStringBuilder = new StringBuilder();

        if (caseData.getEt1VettingCanServeClaimYesOrNo() != null
                && NO.equals(caseData.getEt1VettingCanServeClaimYesOrNo())) {
            List<String[]> canServeClaimIssuesPairsList = new ArrayList<>();

            addPair(canServeClaimIssuesPairsList, "Can we serve the claim with these contact details?",
                    defaultIfEmpty(caseData.getEt1VettingCanServeClaimYesOrNo(), ""));

            if (caseData.getEt1VettingCanServeClaimNoReason() != null
                    && !caseData.getEt1VettingCanServeClaimNoReason().isEmpty()) {
                addPair(canServeClaimIssuesPairsList, "Reason for not serving",
                        defaultIfEmpty(caseData.getEt1VettingCanServeClaimNoReason(), ""));
            }

            if (caseData.getEt1VettingCanServeClaimGeneralNote() != null
                    && !caseData.getEt1VettingCanServeClaimGeneralNote().isEmpty()) {
                addPair(canServeClaimIssuesPairsList, "General Note (Serve Claim)",
                        defaultIfEmpty(caseData.getEt1VettingCanServeClaimGeneralNote(), ""));
            }

            if (!canServeClaimIssuesPairsList.isEmpty()) {
                servingClaimsStringBuilder.append(MarkdownHelper.createTwoColumnTable(
                        new String[]{"Serving Claim Issue", DETAILS},
                        canServeClaimIssuesPairsList));
                et1VettingIssuesTablesMarkup.append(MarkdownHelper.detailsWrapper(
                        "Details of Serving Claims",
                        servingClaimsStringBuilder.toString()));
                et1VettingIssuesTablesMarkup.append(NEWLINE);
            }
        }
    }

    private static void composeIcEt1OtherReferralTableMarkUp(CaseData caseData,
                                                            StringBuilder et1VettingIssuesTablesMarkup) {
        StringBuilder otherReferralStringBuilder = new StringBuilder();
        List<List<String>> otherReferralsIssues = composeIcEt1OtherReferralListDetails(caseData);
        List<String[]> otherReferralIssuesPairsList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(otherReferralsIssues)) {
            otherReferralsIssues.forEach(referralAndDetailPair ->
                    addPair(otherReferralIssuesPairsList, referralAndDetailPair.getFirst(),
                            referralAndDetailPair.get(1)));
            otherReferralStringBuilder.append(MarkdownHelper.createTwoColumnTable(
                    new String[]{REFERRAL_ISSUE, DETAIL},
                    otherReferralIssuesPairsList));
            et1VettingIssuesTablesMarkup.append(MarkdownHelper.detailsWrapper(
                    "Details of Other Referral",
                    otherReferralStringBuilder.toString()));
            et1VettingIssuesTablesMarkup.append(NEWLINE);
        }
    }

    private static void composeReferralToREJOrVPTableMarkUp(CaseData caseData,
                                                            StringBuilder et1VettingIssuesTablesMarkup) {
        StringBuilder referralToJudgeOrLOStringBuilder = new StringBuilder();

        List<List<String>> referralsIssues = composeIcEt1ReferralToREJOrVPListWithDetails(caseData);
        List<String[]> et1VettingReferralIssuesPairsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(referralsIssues)) {
            referralsIssues.forEach(referralAndDetailPair ->
                    addPair(et1VettingReferralIssuesPairsList, referralAndDetailPair.getFirst(),
                            referralAndDetailPair.get(1)));
            referralToJudgeOrLOStringBuilder.append(MarkdownHelper.createTwoColumnTable(
                    new String[]{REFERRAL_ISSUE, DETAIL},
                    et1VettingReferralIssuesPairsList));
            et1VettingIssuesTablesMarkup.append(MarkdownHelper.detailsWrapper(
                    "Details of Referral To REJ or VP",
                    referralToJudgeOrLOStringBuilder.toString()));
            et1VettingIssuesTablesMarkup.append(NEWLINE);
        }
    }

    private static List<List<String>> composeIcEt1ReferralToREJOrVPListWithDetails(CaseData caseData) {
        if (caseData == null) {
            return Collections.emptyList();
        }

        List<List<String>> referralToREJOrVPDetails = new ArrayList<>();

        if (caseData.getReferralToREJOrVPList() != null && !caseData.getReferralToREJOrVPList().isEmpty()) {
            caseData.getReferralToREJOrVPList().forEach(referral -> {
                switch (referral) {
                    case "vexatiousLitigantOrder":
                        referralToREJOrVPDetails.add(List.of("A claimant covered by vexatious litigant order",
                                defaultIfEmpty(caseData.getVexatiousLitigantOrderTextArea(), "")));
                        break;
                    case "aNationalSecurityIssue":
                        referralToREJOrVPDetails.add(List.of("A national security issue",
                                defaultIfEmpty(caseData.getAnationalSecurityIssueTextArea(), "")));
                        break;
                    case "nationalMultipleOrPresidentialOrder":
                        referralToREJOrVPDetails.add(List.of("A part of national multiple / covered by "
                                + "Presidential case management order",
                                defaultIfEmpty(caseData.getNationalMultipleOrPresidentialOrderTextArea(),
                                        "")));
                        break;
                    case "transferToOtherRegion":
                        referralToREJOrVPDetails.add(List.of("A request for transfer to another ET region",
                                        defaultIfEmpty(caseData.getTransferToOtherRegionTextArea(),  "")));
                        break;
                    case "serviceAbroad":
                        referralToREJOrVPDetails.add(List.of("A request for service abroad",
                                defaultIfEmpty(caseData.getServiceAbroadTextArea(), "")));
                        break;
                    case "aSensitiveIssue":
                        referralToREJOrVPDetails.add(List.of("A sensitive issue which may attract publicity "
                                + "or need early allocation to a specific judge",
                                defaultIfEmpty(caseData.getAsensitiveIssueTextArea(), "")));
                        break;
                    case "anyPotentialConflict":
                        referralToREJOrVPDetails.add(List.of("Any potential conflict involving judge, "
                                + "non-legal member or HMCTS staff member",
                                defaultIfEmpty(caseData.getAnyPotentialConflictTextArea(), "")));
                        break;
                    case "anotherReasonREJOrVP":
                        referralToREJOrVPDetails.add(List.of("Another reason for Regional Employment Judge / "
                                + "Vice-President referral",
                                defaultIfEmpty(caseData.getAnotherReasonREJOrVPTextArea(), "")));
                        break;
                    default:
                        // do nothing
                }
            });
        }

        return referralToREJOrVPDetails;
    }

    private static void composeReferralToJudgeOrLoTableMarkUp(CaseData caseData,
                                                         StringBuilder et1VettingIssuesTablesMarkup) {
        StringBuilder referralToJudgeOrLOStringBuilder = new StringBuilder();
        List<List<String>>  referralsIssues = composeIcEt1ReferralToJudgeOrLOListWithDetails(caseData);
        List<String[]> et1VettingReferralIssuesPairsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(referralsIssues)) {
            referralsIssues.forEach(referralAndDetailPair ->
                    addPair(et1VettingReferralIssuesPairsList, referralAndDetailPair.getFirst(),
                            referralAndDetailPair.get(1)));
            referralToJudgeOrLOStringBuilder.append(MarkdownHelper.createTwoColumnTable(
                    new String[]{REFERRAL_ISSUE, DETAIL},
                    et1VettingReferralIssuesPairsList));
            et1VettingIssuesTablesMarkup.append(MarkdownHelper.detailsWrapper(
                    "Details of Referral To Judge or LO",
                    referralToJudgeOrLOStringBuilder.toString()));
            et1VettingIssuesTablesMarkup.append(NEWLINE);
        }
    }

    private static void composeSubstantiveDefectsTableMarkUp(CaseData caseData, StringBuilder stringBuilder) {
        List<List<String>> icEt1SubstantiveDefects = composeIcEt1SubstantiveDefectsDetail(caseData);

        if (CollectionUtils.isNotEmpty(icEt1SubstantiveDefects)) {

            if (caseData.getEt1SubstantiveDefectsGeneralNotes() != null
                    && !caseData.getEt1SubstantiveDefectsGeneralNotes().isEmpty()) {
                icEt1SubstantiveDefects.add(List.of("General notes", caseData.getEt1SubstantiveDefectsGeneralNotes()));
            }

            List<String[]> et1VettingIssuesPairsList = icEt1SubstantiveDefects.stream()
                    .map(defectAndDetailPair -> new String[]{defectAndDetailPair.getFirst(),
                            defectAndDetailPair.get(1)})
                    .toList();

            String twoColumnTable = MarkdownHelper.createTwoColumnTable(
                    new String[]{"Substantive Defects", DETAIL}, et1VettingIssuesPairsList);

            stringBuilder.append(MarkdownHelper.detailsWrapper("Details of Substantive Defects",
                            twoColumnTable)).append(NEWLINE);
        }
    }

    private void composeTrackAllocationTableMarkUp(CaseData caseData, StringBuilder trackAllocationTableMarkUp) {
        List<List<String>> icEt1TrackAllocationIssues = composeTrackAllocationDetails(caseData);

        if (CollectionUtils.isNotEmpty(icEt1TrackAllocationIssues)) {
            List<String[]> trackAllocationIssuePairsList = icEt1TrackAllocationIssues.stream()
                    .map(defectAndDetailPair -> new String[]{defectAndDetailPair.getFirst(),
                            defectAndDetailPair.get(1)})
                    .toList();

            String table = MarkdownHelper.createTwoColumnTable(
                    new String[]{"Track Allocation Issue", DETAIL}, trackAllocationIssuePairsList);

            trackAllocationTableMarkUp.append(MarkdownHelper.detailsWrapper(
                    "Details of Track Allocation Issue", table))
                    .append(NEWLINE);
        }
    }

    public List<List<String>> composeTrackAllocationDetails(CaseData caseData) {
        if (caseData == null) {
            return Collections.emptyList();
        }

        List<List<String>> trackAllocationDetails = new ArrayList<>();

        if (caseData.getIsTrackAllocationCorrect() != null && NO.equals(caseData.getIsTrackAllocationCorrect())) {
            trackAllocationDetails.add(List.of("Is the track allocation correct?",
                    defaultIfNull(caseData.getIsTrackAllocationCorrect())));
            trackAllocationDetails.add(List.of("Suggested Track: ",
                    defaultIfNull(caseData.getSuggestAnotherTrack())));
            trackAllocationDetails.add(List.of("Why Change Track Allocation?",
                    defaultIfNull(caseData.getWhyChangeTrackAllocation())));
            trackAllocationDetails.add(List.of("Track Allocation General Notes",
                    defaultIfNull(caseData.getTrackAllocationGeneralNotes())));
        }

        return trackAllocationDetails;
    }

    private static List<List<String>> composeIcEt1SubstantiveDefectsDetail(CaseData caseData) {
        List<List<String>> substantiveDefectsDetails = new ArrayList<>();

        if (caseData.getSubstantiveDefectsList() != null && !caseData.getSubstantiveDefectsList().isEmpty()) {
            caseData.getSubstantiveDefectsList().forEach(defect -> {
                switch (defect) {
                    case "rule121a" :
                        substantiveDefectsDetails.add(
                                List.of("The tribunal has no jurisdiction to consider - Rule 13(1)(a)",
                                caseData.getRule121aTextArea()));
                        break;
                    case "rule121b":
                        substantiveDefectsDetails.add(
                                List.of("Is in a form which cannot sensibly be responded to or otherwise an "
                                        + "abuse of process - Rule 13(1)(b)",
                                        caseData.getRule121bTextArea()));
                        break;
                    case "rule121c":
                        substantiveDefectsDetails.add(
                                List.of("Has neither an EC number nor claims one of the EC exemptions - Rule 13(1)(c)",
                                        caseData.getRule121cTextArea()));
                        break;
                    case "rule121d":
                        substantiveDefectsDetails.add(
                                List.of("States that one of the EC exceptions applies but it might not - Rule 13(1)(d)",
                                caseData.getRule121dTextArea()));
                        break;
                    case "rule121 da":
                        substantiveDefectsDetails.add(
                                List.of("Institutes relevant proceedings and the EC number on the claim form "
                                        + "does not match the EC number on the Acas certificate - Rule 13(1)(e)",
                                caseData.getRule121daTextArea()));
                        break;
                    case "rule121e":
                        substantiveDefectsDetails.add(List.of("Has a different claimant name on the ET1 to the "
                                        + "claimant name on the Acas certificate - Rule 13(1)(f)",
                                caseData.getRule121eTextArea()));
                        break;
                    case "rule121f":
                        substantiveDefectsDetails.add(
                                List.of("Has a different respondent name on the ET1 to the respondent name on the Acas "
                                        + "certificate - Rule 13(1)(g)", caseData.getRule121fTextArea()
                                ));
                        break;
                    default:
                        // do nothing
                }
            });
        }
        return substantiveDefectsDetails;
    }

    private static List<List<String>> composeIcEt1ReferralToJudgeOrLOListWithDetails(CaseData caseData) {
        List<List<String>> referralToJudgeOrLODetails = new ArrayList<>();

        if (caseData.getReferralToJudgeOrLOList() != null && !caseData.getReferralToJudgeOrLOList().isEmpty()) {
            for (String listItem : caseData.getReferralToJudgeOrLOList()) {
                switch (listItem) {
                    case "aClaimOfInterimRelief" :
                        referralToJudgeOrLODetails.add(List.of("A claim of interim relief",
                                    caseData.getAclaimOfInterimReliefTextArea()));
                        break;
                    case "aStatutoryAppeal" :
                        referralToJudgeOrLODetails.add(List.of("A statutory appeal",
                                caseData.getAstatutoryAppealTextArea()));
                        break;
                    case "anAllegationOfCommissionOfSexualOffence" :
                        referralToJudgeOrLODetails.add(List.of("An allegation of the commission of a sexual offence",
                                caseData.getAnAllegationOfCommissionOfSexualOffenceTextArea()));
                        break;
                    case "insolvency" :
                        referralToJudgeOrLODetails.add(List.of("Insolvency",
                                caseData.getInsolvencyTextArea()));
                        break;
                    case "jurisdictionsUnclear" :
                        referralToJudgeOrLODetails.add(List.of("Jurisdictions unclear",
                                caseData.getJurisdictionsUnclearTextArea()));
                        break;
                    case "lengthOfService" :
                        referralToJudgeOrLODetails.add(List.of("Length of service",
                                caseData.getLengthOfServiceTextArea()));
                        break;
                    case "potentiallyLinkedCasesInTheEcm" :
                        referralToJudgeOrLODetails.add(List.of("Potentially linked cases in the ECM",
                                caseData.getPotentiallyLinkedCasesInTheEcmTextArea()));
                        break;
                    case "rule50Issues" :
                        referralToJudgeOrLODetails.add(List.of("Rule 49 issues",
                                caseData.getRule50IssuesTextArea()));
                        break;
                    case "anotherReasonForJudicialReferral" :
                        referralToJudgeOrLODetails.add(List.of("Another reason for judicial referral",
                                caseData.getAnotherReasonForJudicialReferralTextArea()));
                        break;
                    default:
                        break;
                }
            }
        }
        return referralToJudgeOrLODetails;
    }

    private static List<List<String>>  composeIcEt1OtherReferralListDetails(CaseData caseData) {
        List<List<String>> otherReferralDetails = new ArrayList<>();

        if (caseData.getOtherReferralList() != null && !caseData.getOtherReferralList().isEmpty()) {
            for (String referralReason : caseData.getOtherReferralList()) {
                switch (referralReason) {
                    case "claimOutOfTime" :
                        otherReferralDetails.add(List.of("Claim out of time",
                                "The whole or any part of the claim is out of time"));
                        break;
                    case "multipleClaim" :
                        otherReferralDetails.add(List.of("Multiple claims",
                                "The claim is part of a multiple claim"));
                        break;
                    case "employmentStatusIssues" :
                        otherReferralDetails.add(List.of("Employment status issues",
                                "The claim has a potential issue about employment status"));
                        break;
                    case "pidJurisdictionRegulator" :
                        otherReferralDetails.add(List.of("Pid jurisdiction regulator",
                                "The claim has PID jurisdiction and claimant wants it forwarded to relevant "
                                + "regulator - Box 10.1"));
                        break;
                    case "videoHearingPreference" :
                        otherReferralDetails.add(List.of("Video hearing preference",
                                "The claimant prefers a video hearing"));
                        break;
                    case "rule50IssuesOtherFactors" :
                        otherReferralDetails.add(List.of("Rule49 issues - other factors",
                                "The claim has Rule 49 issues"));
                        break;
                    case "otherRelevantFactors" :
                        otherReferralDetails.add(List.of("Other relevant factors",
                                "The claim has other relevant factors for judicial referral"));
                        break;
                    default:
                        break;
                }
            }
        }

        return  otherReferralDetails;
    }
}
