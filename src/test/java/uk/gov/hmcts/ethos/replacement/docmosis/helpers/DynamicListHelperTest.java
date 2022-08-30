package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.JudgementTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.JudgementType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.dynamiclists.DynamicDepositOrder;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.dynamiclists.DynamicJudgements;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.dynamiclists.DynamicLetters;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.dynamiclists.DynamicRespondentRepresentative;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.dynamiclists.DynamicRestrictedReporting;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.et.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.et.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_HEARING;
import static uk.gov.hmcts.et.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DynamicListHelper.DYNAMIC_HEARING_LABEL_FORMAT;

class DynamicListHelperTest {

    private CaseDetails caseDetails1;
    private CaseDetails caseDetails2;
    private CaseDetails caseDetails4;
    private CaseDetails caseDetails6;
    private CaseDetails caseDetailsScotTest1;
    private DynamicValueType dynamicValueType;

    @BeforeEach
    void setUp() throws Exception {
        caseDetails1 = generateCaseDetails("caseDetailsTest1.json");
        caseDetails2 = generateCaseDetails("caseDetailsTest2.json");
        caseDetails4 = generateCaseDetails("caseDetailsTest4.json");
        caseDetails6 = generateCaseDetails("caseDetailsTest6.json");
        caseDetailsScotTest1 = generateCaseDetails("caseDetailsScotTest1.json");
        dynamicValueType = new DynamicValueType();
    }

    private CaseDetails generateCaseDetails(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
                .getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    void createDynamicListForRespondentRepresentative() {
        DynamicRespondentRepresentative.dynamicRespondentRepresentativeNames(caseDetails1.getCaseData());
        assertNotNull(caseDetails1.getCaseData().getRepCollection());
        var dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode("R: Antonio Vazquez");
        dynamicValueType.setLabel("Antonio Vazquez");
        assertEquals(dynamicValueType, caseDetails1.getCaseData().getRepCollection().get(0)
                .getValue().getDynamicRespRepName().getListItems().get(0));
    }

    @Test
    void populateDynamicRespondentRepList() {
        DynamicRespondentRepresentative.dynamicRespondentRepresentativeNames(caseDetails6.getCaseData());
        assertNotNull(caseDetails6.getCaseData().getRepCollection().get(0).getValue().getDynamicRespRepName());
        var dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode("R: Antonio Vazquez");
        dynamicValueType.setLabel("Antonio Vazquez");
        assertEquals(dynamicValueType, caseDetails6.getCaseData().getRepCollection().get(0)
                .getValue().getDynamicRespRepName().getListItems().get(0));
    }

    @Test
    void createDynamicListForRestrictedReporting() {
        DynamicRestrictedReporting.dynamicRestrictedReporting(caseDetails1.getCaseData());
        assertNotNull(caseDetails1.getCaseData().getRestrictedReporting());
        dynamicValueType.setCode("R: Antonio Vazquez");
        dynamicValueType.setLabel("Antonio Vazquez");
        assertEquals(dynamicValueType, caseDetails1.getCaseData().getRestrictedReporting()
                .getDynamicRequestedBy().getListItems().get(0));
    }

    @Test
    void populateDynamicListForRestrictedReporting() {
        DynamicRestrictedReporting.dynamicRestrictedReporting(caseDetails4.getCaseData());
        assertNotNull(caseDetails4.getCaseData().getRestrictedReporting().getDynamicRequestedBy());
        dynamicValueType.setCode("Judge");
        dynamicValueType.setLabel("Judge");
        assertEquals(dynamicValueType, caseDetails4.getCaseData().getRestrictedReporting()
                .getDynamicRequestedBy().getValue());
    }

    @Test
    void dynamicValueTypeRespondent() {
        var dynamicValueRespondent = new DynamicValueType();
        dynamicValueRespondent.setCode("R: Antonio Vazquez");
        dynamicValueRespondent.setLabel("Antonio Vazquez");
        List<DynamicValueType> listItems = DynamicListHelper.createDynamicRespondentName(
                caseDetails1.getCaseData().getRespondentCollection());
        listItems.add(DynamicListHelper.getDynamicCodeLabel(
                "C: " + caseDetails1.getCaseData().getClaimant(), caseDetails1.getCaseData().getClaimant()));
        var dynamicValue = DynamicListHelper.getDynamicValueParty(
                caseDetails1.getCaseData(), listItems, "Respondent");
        assertEquals(dynamicValue, dynamicValueRespondent);
    }

    @Test
    void dynamicDepositOrder() {
        DynamicDepositOrder.dynamicDepositOrder(caseDetails1.getCaseData());
        assertNotNull(caseDetails1.getCaseData().getDepositCollection());
        dynamicValueType.setCode("R: Antonio Vazquez");
        dynamicValueType.setLabel("Antonio Vazquez");
        assertEquals(dynamicValueType, caseDetails1.getCaseData().getDepositCollection().get(0)
                .getValue().getDynamicDepositOrderAgainst().getValue());
        dynamicValueType.setCode("Tribunal");
        dynamicValueType.setLabel("Tribunal");
        assertEquals(dynamicValueType, caseDetails1.getCaseData().getDepositCollection().get(0)
                .getValue().getDynamicDepositRequestedBy().getValue());
    }

    @Test
    void dynamicDepositRefund() {
        caseDetails1.getCaseData().getDepositCollection().get(0).getValue().setDepositRefund("Yes");
        DynamicDepositOrder.dynamicDepositOrder(caseDetails1.getCaseData());
        dynamicValueType.setCode("R: Antonio Vazquez");
        dynamicValueType.setLabel("Antonio Vazquez");
        assertEquals(dynamicValueType, caseDetails1.getCaseData().getDepositCollection().get(0)
                .getValue().getDynamicDepositRefundedTo().getValue());
    }

    @Test
    void dynamicHearingList() {
        List<DynamicValueType> dynamicHearingList = DynamicListHelper.createDynamicHearingList(
                caseDetails1.getCaseData());
        dynamicValueType.setCode("1");
        dynamicValueType.setLabel("1 - Single - Manchester - 01 Nov 2019");
        assertEquals(dynamicValueType, dynamicHearingList.get(0));
        dynamicValueType.setCode("2");
        dynamicValueType.setLabel("2 - Single - Manchester - 25 Nov 2019");
        assertEquals(dynamicValueType, dynamicHearingList.get(1));
    }

    @Test
    void dynamicLettersEngWales() {
        DynamicLetters.dynamicLetters(caseDetails1.getCaseData(), ENGLANDWALES_CASE_TYPE_ID);
        dynamicValueType.setCode("1");
        dynamicValueType.setLabel("1 - Single - Manchester - 01 Nov 2019");
        assertEquals(dynamicValueType, caseDetails1.getCaseData().getCorrespondenceType()
                .getDynamicHearingNumber().getListItems().get(0));
        assertNull(caseDetails1.getCaseData().getCorrespondenceScotType());
    }

    @Test
    void dynamicLettersScotland() {
        DynamicLetters.dynamicLetters(caseDetailsScotTest1.getCaseData(), SCOTLAND_CASE_TYPE_ID);
        dynamicValueType.setCode("1");
        dynamicValueType.setLabel("1 - Single - Glasgow - 25 Nov 2019");
        assertEquals(dynamicValueType, caseDetailsScotTest1.getCaseData()
                .getCorrespondenceScotType().getDynamicHearingNumber().getListItems().get(0));
        assertNull(caseDetailsScotTest1.getCaseData().getCorrespondenceType());
    }

    @Test
    void createDynamicJurisdictionCodesTest() {
        List<DynamicValueType> listItems = DynamicListHelper
                .createDynamicJurisdictionCodes(caseDetails1.getCaseData());
        var totalJurisdictions = caseDetails1.getCaseData().getJurCodesCollection().size();
        var dynamicValue = DynamicListHelper
                .getDynamicValue(caseDetails1.getCaseData().getJurCodesCollection()
                .get(0).getValue().getJuridictionCodesList());
        assertEquals(dynamicValue, listItems.get(0));
        assertEquals(totalJurisdictions, listItems.size());
    }

    @Test
    void findDynamicValueTest() {
        List<DynamicValueType> listItems = DynamicListHelper.createDynamicJurisdictionCodes(caseDetails1.getCaseData());
        dynamicValueType.setCode("COM");
        dynamicValueType.setLabel("COM");
        assertEquals(dynamicValueType, DynamicListHelper.findDynamicValue(listItems, "COM"));
    }

    @Test
    void dynamicJudgementsTest() {
        var caseData = caseDetails1.getCaseData();
        DynamicJudgements.dynamicJudgements(caseData);
        var totalHearings = caseData.getHearingCollection().size();
        JudgementType judgementType = caseData.getJudgementCollection().get(0).getValue();
        assertEquals(totalHearings, judgementType.getDynamicJudgementHearing().getListItems().size());
    }

    @Test
    void dynamicJudgementHearing_HearingDateFilled() {
        var caseData = caseDetails1.getCaseData();
        caseData.getJudgementCollection().get(0).getValue().setJudgmentHearingDate("2019-11-01");
        DynamicJudgements.dynamicJudgements(caseData);
        dynamicValueType.setCode("1");
        dynamicValueType.setLabel("1 : Manchester - Single - 2019-11-01");
        assertEquals(dynamicValueType, caseData.getJudgementCollection()
                .get(0).getValue().getDynamicJudgementHearing().getValue());
    }

    @Test
    void dynamicJudgementHearing_DynamicValue() {
        var caseData = caseDetails1.getCaseData();
        List<DynamicValueType> hearingListItems = DynamicListHelper.createDynamicHearingList(caseData);
        var listHearing = new DynamicFixedListType();
        listHearing.setListItems(hearingListItems);
        caseData.getJudgementCollection().get(0).getValue().setDynamicJudgementHearing(listHearing);
        dynamicValueType.setCode("1");
        dynamicValueType.setLabel("1 : Manchester - Single - 2019-11-01");
        caseData.getJudgementCollection().get(0).getValue().getDynamicJudgementHearing().setValue(dynamicValueType);
        DynamicJudgements.dynamicJudgements(caseData);
        assertEquals(dynamicValueType, caseData.getJudgementCollection()
                .get(0).getValue().getDynamicJudgementHearing().getValue());
    }

    @Test
    void createDynamicJudgementHearing() {
        var caseData = caseDetails2.getCaseData();
        DynamicJudgements.dynamicJudgements(caseData);
        assertNotNull(caseData.getJudgementCollection());
        var totalHearings = caseData.getHearingCollection().size();
        JudgementType judgementType = caseData.getJudgementCollection().get(0).getValue();
        assertEquals(totalHearings, judgementType.getDynamicJudgementHearing().getListItems().size());
    }

    @Test
    void createDynamicJudgementHearingNoHearing() {
        var caseData = caseDetails2.getCaseData();
        caseData.setHearingCollection(null);
        DynamicJudgements.dynamicJudgements(caseData);
        assertNotNull(caseData.getJudgementCollection());
        JudgementType judgementType = caseData.getJudgementCollection().get(0).getValue();
        assertEquals("No Hearings", judgementType.getDynamicJudgementHearing().getListItems().get(0).getCode());
    }

    @Test
    void dynamicJudgment_ifHearingDateIsInvalid() {
        var casedata = caseDetails2.getCaseData();
        var judgmentType = new JudgementType();
        judgmentType.setJudgmentHearingDate("2022-02-02");
        var judgmentTypeItem = new JudgementTypeItem();
        judgmentTypeItem.setValue(judgmentType);
        casedata.setJudgementCollection(List.of(judgmentTypeItem));

        DynamicJudgements.dynamicJudgements(casedata);

        assertNotNull(casedata.getJudgementCollection());
        var judgementType = casedata.getJudgementCollection().get(0).getValue();
        assertNull(judgementType.getJudgmentHearingDate());
    }

    @Test
    void testCreateDynamicHearingListWithVenue() {
        String hearingNumber = "123";
        String venue = "Bristol Mags";
        CaseData caseData = CaseDataBuilder.builder()
                .withHearing(hearingNumber, HEARING_TYPE_JUDICIAL_HEARING, "Judge1", venue, null, null, null, null)
                .withHearingSession(
                        0,
                        hearingNumber,
                        "2019-11-25T12:11:00.000",
                        Constants.HEARING_STATUS_HEARD,
                        true)
                .build();

        var hearingList = DynamicListHelper.createDynamicHearingList(caseData);

        assertEquals(1, hearingList.size());
        assertEquals(hearingNumber, hearingList.get(0).getCode());
        String expectedLabel = String.format(DYNAMIC_HEARING_LABEL_FORMAT, hearingNumber, HEARING_TYPE_JUDICIAL_HEARING,
                venue, "25 Nov 2019");
        assertEquals(expectedLabel, hearingList.get(0).getLabel());
    }

    @ParameterizedTest
    @MethodSource
    void testCreateDynamicHearingListWithScotlandVenue(TribunalOffice tribunalOffice) {
        String hearingNumber = "123";
        String venue = "Some venue";
        CaseData caseData = CaseDataBuilder.builder()
                .withHearingScotland(hearingNumber, HEARING_TYPE_JUDICIAL_HEARING, "Judge1", tribunalOffice, venue)
                .withHearingSession(
                        0,
                        hearingNumber,
                        "2019-11-25T12:11:00.000",
                        Constants.HEARING_STATUS_HEARD,
                        true)
                .build();

        var hearingList = DynamicListHelper.createDynamicHearingList(caseData);

        assertEquals(1, hearingList.size());
        assertEquals(hearingNumber, hearingList.get(0).getCode());
        String expectedLabel = String.format(DYNAMIC_HEARING_LABEL_FORMAT, hearingNumber, HEARING_TYPE_JUDICIAL_HEARING,
                venue, "25 Nov 2019");
        assertEquals(expectedLabel, hearingList.get(0).getLabel());
    }

    private static Stream<Arguments> testCreateDynamicHearingListWithScotlandVenue() {
        return Stream.of(
                Arguments.of(TribunalOffice.ABERDEEN),
                Arguments.of(TribunalOffice.DUNDEE),
                Arguments.of(TribunalOffice.EDINBURGH),
                Arguments.of(TribunalOffice.GLASGOW));
    }

    @Test
    void testCreateDynamicHearingListThrowsExceptionWithInvalidScotlandVenue() {
        String hearingNumber = "123";
        String venue = "Some venue";
        CaseData caseData = CaseDataBuilder.builder()
                .withHearing(hearingNumber, HEARING_TYPE_JUDICIAL_HEARING, "Judge", venue, null, null, null, null)
                .withHearingSession(
                        0,
                        hearingNumber,
                        "2019-11-25T12:11:00.000",
                        Constants.HEARING_STATUS_HEARD,
                        true)
                .build();
        caseData.getHearingCollection().get(0).getValue().setHearingVenueScotland(TribunalOffice.LEEDS.getOfficeName());

        assertThrows(IllegalStateException.class, () -> DynamicListHelper.createDynamicHearingList(caseData));
    }

    @Test
    void testCreateDynamicHearingListNoVenue() {
        String hearingNumber = "123";
        CaseData caseData = CaseDataBuilder.builder()
                .withHearing(hearingNumber, HEARING_TYPE_JUDICIAL_HEARING, "Judge1", null, null, null, null)
                .withHearingSession(
                        0,
                        hearingNumber,
                        "2019-11-25T12:11:00.000",
                        Constants.HEARING_STATUS_HEARD,
                        true)
                .build();

        var hearingList = DynamicListHelper.createDynamicHearingList(caseData);

        assertEquals(1, hearingList.size());
        assertEquals(hearingNumber, hearingList.get(0).getCode());
        String expectedLabel = String.format(DYNAMIC_HEARING_LABEL_FORMAT, hearingNumber, HEARING_TYPE_JUDICIAL_HEARING,
                null, "25 Nov 2019");
        assertEquals(expectedLabel, hearingList.get(0).getLabel());
    }
}
