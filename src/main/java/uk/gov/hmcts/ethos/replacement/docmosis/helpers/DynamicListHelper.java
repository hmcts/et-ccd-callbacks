package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.MONTH_STRING_DATE_FORMAT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.dynamiclists.DynamicJudgements.NO_HEARINGS;

public final class DynamicListHelper {

    /** Format for the label property of a Hearing DynamicList item. */
    static final String DYNAMIC_HEARING_LABEL_FORMAT = "%s : %s - %s - %s";

    private DynamicListHelper() {
    }

    public static List<DynamicValueType> createDynamicRespondentName(List<RespondentSumTypeItem> respondentCollection) {
        List<DynamicValueType> listItems = new ArrayList<>();
        if (respondentCollection != null) {
            for (RespondentSumTypeItem respondentSumTypeItem : respondentCollection) {
                DynamicValueType dynamicValueType = new DynamicValueType();
                RespondentSumType respondentSumType = respondentSumTypeItem.getValue();
                dynamicValueType.setCode("R: " + respondentSumType.getRespondentName());
                dynamicValueType.setLabel(respondentSumType.getRespondentName());
                listItems.add(dynamicValueType);
            }
        }
        return listItems;
    }

    public static DynamicValueType getDynamicValue(String value) {
        return getDynamicCodeLabel(value, value);
    }

    public static DynamicValueType getDynamicCodeLabel(String code, String label) {
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(code);
        dynamicValueType.setLabel(label);
        return dynamicValueType;
    }

    public static DynamicValueType getDynamicValueParty(CaseData caseData, List<DynamicValueType> listItems,
                                                        String party) {
        DynamicValueType dynamicValueType;
        if (party.equals(CLAIMANT_TITLE)) {
            dynamicValueType = getDynamicCodeLabel("C: " + caseData.getClaimant(), caseData.getClaimant());
        } else if (party.equals(RESPONDENT_TITLE)) {
            dynamicValueType = listItems.getFirst();
        } else {
            dynamicValueType = getDynamicValue(party);
        }
        return dynamicValueType;
    }

    public static List<DynamicValueType> createDynamicHearingList(CaseData caseData) {
        List<DynamicValueType> listItems = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseData.getHearingCollection())) {
            for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {
                HearingType hearing = hearingTypeItem.getValue();
                String hearingNumber = hearing.getHearingNumber();
                String hearingType = hearing.getHearingType();
                String venue = getHearingVenue(hearing);
                String listedDate = getListedDate(hearing.getHearingDateCollection().getFirst().getValue());

                String hearingData = String.format(DYNAMIC_HEARING_LABEL_FORMAT, hearingNumber, hearingType, venue,
                        listedDate);
                listItems.add(getDynamicCodeLabel(hearingNumber, hearingData));
            }
        } else {
            listItems.add(getDynamicValue(NO_HEARINGS));
        }
        return listItems;
    }

    private static String getHearingVenue(HearingType hearing) {
        DynamicFixedListType hearingVenue;
        if (StringUtils.isNotBlank(hearing.getHearingVenueScotland())) {
            TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(hearing.getHearingVenueScotland());
            hearingVenue = switch (tribunalOffice) {
                case GLASGOW -> hearing.getHearingGlasgow();
                case ABERDEEN -> hearing.getHearingAberdeen();
                case DUNDEE -> hearing.getHearingDundee();
                case EDINBURGH -> hearing.getHearingEdinburgh();
                default -> throw new IllegalStateException("Unexpected Scotland tribunal office " + tribunalOffice);
            };
        } else {
            hearingVenue = hearing.getHearingVenue();
        }

        return hearingVenue != null ? hearingVenue.getSelectedLabel() : null;
    }

    public static List<DynamicValueType> createDynamicJurisdictionCodes(CaseData caseData) {
        List<DynamicValueType> listItems = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseData.getJurCodesCollection())) {
            for (JurCodesTypeItem jurCodesTypeItem : caseData.getJurCodesCollection()) {
                listItems.add(getDynamicValue(jurCodesTypeItem.getValue().getJuridictionCodesList()));
            }
        }
        return listItems;
    }

    public static DynamicValueType findDynamicValue(List<DynamicValueType> listItems, String code) {
        DynamicValueType dynamicValue = new DynamicValueType();
        for (DynamicValueType dynamicValueType : listItems) {
            if (dynamicValueType.getCode().equals(code)) {
                dynamicValue.setCode(code);
                dynamicValue.setLabel(dynamicValueType.getLabel());
                return dynamicValue;
            }
        }
        return dynamicValue;
    }

    private static String getListedDate(DateListedType dateListedType) {
        String listedDate = dateListedType.getListedDate().substring(0, 10);
        LocalDate date = LocalDate.parse(listedDate);
        return date.format(DateTimeFormatter.ofPattern(MONTH_STRING_DATE_FORMAT));
    }

    public static List<DynamicValueType> createDynamicRespondentWithEccList(CaseData caseData) {
        List<DynamicValueType> listItems = new ArrayList<>();

        emptyIfNull(caseData.getRespondentCollection()).forEach(respondent -> {
            RespondentSumType respondentValue = respondent.getValue();
            if (YES.equals(defaultIfEmpty(respondentValue.getRespondentEcc(), NO))) {
                listItems.add(getDynamicCodeLabel(respondent.getId(), respondentValue.getRespondentName()));
            }
        });

        return listItems;
    }
}
