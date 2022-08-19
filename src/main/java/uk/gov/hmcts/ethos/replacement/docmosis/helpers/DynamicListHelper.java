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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.dynamiclists.DynamicJudgements.NO_HEARINGS;

@SuppressWarnings({"PMD.ClassWithOnlyPrivateConstructorsShouldBeFinal", "PMD.AvoidInstantiatingObjectsInLoops",
    "PMD.ConfusingTernary"})
public class DynamicListHelper {

    /** Format for the label property of a Hearing DynamicList item. */
    static final String DYNAMIC_HEARING_LABEL_FORMAT = "%s : %s - %s - %s";

    private DynamicListHelper() {
    }

    public static List<DynamicValueType> createDynamicRespondentName(List<RespondentSumTypeItem> respondentCollection) {
        List<DynamicValueType> listItems = new ArrayList<>();
        if (respondentCollection != null) {
            for (RespondentSumTypeItem respondentSumTypeItem : respondentCollection) {
                var dynamicValueType = new DynamicValueType();
                var respondentSumType = respondentSumTypeItem.getValue();
                dynamicValueType.setCode("R: " + respondentSumType.getRespondentName());
                dynamicValueType.setLabel(respondentSumType.getRespondentName());
                listItems.add(dynamicValueType);
            }
        }
        return listItems;
    }

    public static DynamicValueType getDynamicValue(String value) {
        var dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(value);
        dynamicValueType.setLabel(value);
        return dynamicValueType;
    }

    public static DynamicValueType getDynamicCodeLabel(String code, String label) {
        var dynamicValueType = new DynamicValueType();
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
            dynamicValueType = listItems.get(0);
        } else {
            dynamicValueType = getDynamicValue(party);
        }
        return dynamicValueType;
    }

    public static List<DynamicValueType> createDynamicHearingList(CaseData caseData) {
        List<DynamicValueType> listItems = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseData.getHearingCollection())) {
            for (HearingTypeItem hearingTypeItem : caseData.getHearingCollection()) {
                var hearing = hearingTypeItem.getValue();
                var hearingNumber = hearing.getHearingNumber();
                var hearingType = hearing.getHearingType();
                var venue = getHearingVenue(hearing);
                var listedDate = getListedDate(hearing.getHearingDateCollection().get(0).getValue());

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
            switch (tribunalOffice) {
                case GLASGOW:
                    hearingVenue = hearing.getHearingGlasgow();
                    break;
                case ABERDEEN:
                    hearingVenue = hearing.getHearingAberdeen();
                    break;
                case DUNDEE:
                    hearingVenue = hearing.getHearingDundee();
                    break;
                case EDINBURGH:
                    hearingVenue = hearing.getHearingEdinburgh();
                    break;
                default:
                    throw new IllegalStateException("Unexpected Scotland tribunal office " + tribunalOffice);
            }
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
        var dynamicValue = new DynamicValueType();
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
        return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }
}
