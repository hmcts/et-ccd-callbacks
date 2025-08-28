package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.HearingDetailTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingDetailType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_POSTPONED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_SETTLED;
import static uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType.create;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.HEARING_BREAK_FUTURE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.HEARING_BREAK_RESUME_INVALID;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.HEARING_FINISH_FUTURE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.HEARING_FINISH_INVALID;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.HEARING_RESUME_FUTURE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.HEARING_START_FUTURE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.findHearingNumber;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.HEARING_CREATION_DAY_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.HEARING_CREATION_NUMBER_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.TWO_JUDGES;

@ExtendWith(SpringExtension.class)
class HearingsHelperTest {

    private CaseData caseData;

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {
        CaseDetails caseDetails1 = generateCaseDetails();
        caseData = caseDetails1.getCaseData();
    }

    private CaseDetails generateCaseDetails() throws URISyntaxException, IOException {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
            .getContextClassLoader().getResource("caseDetailsTest1.json")).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    void hearingMidEventValidationNumberError() {

        caseData.getHearingCollection().get(0).getValue().setHearingNumber(null);

        assertEquals(1, HearingsHelper.hearingMidEventValidation(caseData).size());

        assertEquals(HEARING_CREATION_NUMBER_ERROR,
                HearingsHelper.hearingMidEventValidation(caseData).get(0));

        caseData.getHearingCollection().get(0).getValue().setHearingNumber("");

        assertEquals(1, HearingsHelper.hearingMidEventValidation(caseData).size());

        assertEquals(HEARING_CREATION_NUMBER_ERROR,
                HearingsHelper.hearingMidEventValidation(caseData).get(0));

    }

    @Test
    void hearingMidEventValidationDayError() {

        caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().setListedDate(null);

        assertEquals(1, HearingsHelper.hearingMidEventValidation(caseData).size());

        assertEquals(HEARING_CREATION_DAY_ERROR,
                HearingsHelper.hearingMidEventValidation(caseData).get(0));

        caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().setListedDate("");

        assertEquals(1, HearingsHelper.hearingMidEventValidation(caseData).size());

        assertEquals(HEARING_CREATION_DAY_ERROR,
                HearingsHelper.hearingMidEventValidation(caseData).get(0));

        caseData.getHearingCollection().get(0).getValue()
                .setHearingDateCollection(null);

        assertEquals(0, HearingsHelper.hearingMidEventValidation(caseData).size());

    }

    @Test
    void updatePostponedDate() {

        caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().setHearingStatus(HEARING_STATUS_POSTPONED);

        Helper.updatePostponedDate(caseData);

        assertNotNull(caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getPostponedDate());

        caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().setHearingStatus(HEARING_STATUS_SETTLED);

        Helper.updatePostponedDate(caseData);

        assertNull(caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getPostponedDate());
    }

    @Test
    void findDateOfHearingTest() {
        String hearingDate = caseData.getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getListedDate().substring(0, 10);
        String hearingNumber = findHearingNumber(caseData, hearingDate);
        assertEquals(hearingNumber,
                caseData.getHearingCollection().get(0).getValue().getHearingNumber());
    }

    @Test
    void findDateOfHearing_DateNotInHearing() {
        assertNull(findHearingNumber(caseData, "1970-10-01"));
    }

    @Test
    void validateStartFinishTime_validTime() {
        setValidHearingStartFinishTimes();
        List<String> errors = HearingsHelper.hearingTimeValidation(caseData);
        assertEquals(0, errors.size());
    }

    @Test
    void validateBreakResumeTime_invalidBreak() {
        setValidHearingStartFinishTimes();
        caseData.getHearingDetailsCollection().get(0)
                .getValue()
                .setHearingDetailsTimingBreak("2019-11-01T00:00:00.000");
        setHearingDetails();
        List<String> errors = HearingsHelper.hearingTimeValidation(caseData);
        assertEquals(1, errors.size());
        assertEquals(String.format(HEARING_BREAK_RESUME_INVALID, "Hearing 1"), errors.get(0));
    }

    @Test
    void validateBreakResumeTime_invalidResume() {
        setValidHearingStartFinishTimes();
        caseData.getHearingDetailsCollection().get(0)
                .getValue()
                .setHearingDetailsTimingResume("2019-11-01T00:00:00.000");
        setHearingDetails();
        List<String> errors = HearingsHelper.hearingTimeValidation(caseData);
        assertEquals(1, errors.size());
        assertEquals(String.format(HEARING_BREAK_RESUME_INVALID, "Hearing 1"), errors.get(0));
    }

    @Test
    void validateBreakResumeTime_nullBreakResume() {
        setValidHearingStartFinishTimes();
        caseData.setHearingDetailsTimingBreak(null);
        caseData.setHearingDetailsTimingResume(null);
        List<String> errors = HearingsHelper.hearingTimeValidation(caseData);
        assertEquals(0, errors.size());
    }

    @Test
    void validateStartFinishTime_sameTime() {
        HearingDetailTypeItem hearingDetailTypeItem = new HearingDetailTypeItem();
        hearingDetailTypeItem.setId(UUID.randomUUID().toString());
        HearingDetailType hearingDetailType = new HearingDetailType();
        hearingDetailType.setHearingDetailsTimingStart("2019-11-01T12:11:00.000");
        // Same time as start time
        hearingDetailType.setHearingDetailsTimingFinish("2019-11-01T12:11:00.000");
        hearingDetailType.setHearingDetailsStatus(HEARING_STATUS_HEARD);
        hearingDetailTypeItem.setValue(hearingDetailType);
        caseData.setHearingDetailsCollection(List.of(hearingDetailTypeItem));
        setHearingDetails();
        List<String> errors = HearingsHelper.hearingTimeValidation(caseData);
        assertEquals(1, errors.size());
        assertEquals(String.format(HEARING_FINISH_INVALID, "Hearing 1"), errors.get(0));
    }

    @Test
    void validateStartFinishTime_finishTimeBeforeStart() {
        HearingDetailTypeItem hearingDetailTypeItem = new HearingDetailTypeItem();
        hearingDetailTypeItem.setId(UUID.randomUUID().toString());
        HearingDetailType hearingDetailType = new HearingDetailType();
        hearingDetailType.setHearingDetailsTimingStart("2019-11-01T12:11:00.000");
        hearingDetailType.setHearingDetailsTimingFinish("2019-11-01T12:10:00.000");
        hearingDetailType.setHearingDetailsStatus(HEARING_STATUS_HEARD);
        hearingDetailTypeItem.setValue(hearingDetailType);
        caseData.setHearingDetailsCollection(List.of(hearingDetailTypeItem));
        setHearingDetails();
        List<String> errors = HearingsHelper.hearingTimeValidation(caseData);
        assertEquals(1, errors.size());
        assertEquals(String.format(HEARING_FINISH_INVALID, "Hearing 1"), errors.get(0));
    }

    @Test
    void validateHearingDatesInPastTest() {
        HearingDetailTypeItem hearingDetailTypeItem = new HearingDetailTypeItem();
        hearingDetailTypeItem.setId(UUID.randomUUID().toString());
        HearingDetailType hearingDetailType = new HearingDetailType();
        hearingDetailType.setHearingDetailsTimingBreak("2021-12-19T10:00:00");
        hearingDetailType.setHearingDetailsTimingResume("2021-12-19T10:00:00");
        hearingDetailType.setHearingDetailsTimingFinish("2021-12-19T10:10:00");
        hearingDetailType.setHearingDetailsTimingStart("2021-12-19T10:00:00");
        hearingDetailType.setHearingDetailsStatus(HEARING_STATUS_HEARD);
        hearingDetailTypeItem.setValue(hearingDetailType);
        caseData.setHearingDetailsCollection(List.of(hearingDetailTypeItem));
        List<String> errors = HearingsHelper.hearingTimeValidation(caseData);
        assertEquals(0, errors.size());
    }

    @Test
    void validateIsDateInFutureConsideringDST() {
        LocalDateTime dateTime = LocalDateTime.now().minusMinutes(25);
        LocalDateTime now = LocalDateTime.now(UTC);
        boolean val = HearingsHelper.isDateInFuture(dateTime.toString(), now);
        assertFalse(val);
    }

    @Test
    void invalidateHearingDatesInFutureTest() {
        HearingDetailTypeItem hearingDetailTypeItem = new HearingDetailTypeItem();
        hearingDetailTypeItem.setId(UUID.randomUUID().toString());
        HearingDetailType hearingDetailType = new HearingDetailType();
        hearingDetailType.setHearingDetailsTimingStart("2222-11-01T12:11:00.000");
        hearingDetailType.setHearingDetailsTimingFinish("2222-11-01T12:11:20.000");
        hearingDetailType.setHearingDetailsTimingResume("2222-11-01T12:11:20.000");
        hearingDetailType.setHearingDetailsTimingBreak("2222-11-01T12:11:20.000");
        hearingDetailType.setHearingDetailsStatus(HEARING_STATUS_HEARD);
        hearingDetailTypeItem.setValue(hearingDetailType);
        caseData.setHearingDetailsCollection(List.of(hearingDetailTypeItem));
        List<String> errors = HearingsHelper.hearingTimeValidation(caseData);
        assertEquals(4, errors.size());
        assertTrue(errors.contains(HEARING_START_FUTURE));
        assertTrue(errors.contains(HEARING_FINISH_FUTURE));
        assertTrue(errors.contains(HEARING_BREAK_FUTURE));
        assertTrue(errors.contains(HEARING_RESUME_FUTURE));
    }

    @Test
    void invalidateHearingDatesInFutureTestNullCheck() {
        HearingDetailTypeItem hearingDetailTypeItem = new HearingDetailTypeItem();
        hearingDetailTypeItem.setId(UUID.randomUUID().toString());
        HearingDetailType hearingDetailType = new HearingDetailType();
        hearingDetailType.setHearingDetailsTimingStart("2222-11-01T12:11:00.000");
        hearingDetailType.setHearingDetailsTimingFinish("2222-11-01T12:11:20.000");
        hearingDetailType.setHearingDetailsTimingResume(null);
        hearingDetailType.setHearingDetailsTimingBreak(null);
        hearingDetailType.setHearingDetailsStatus(HEARING_STATUS_HEARD);
        hearingDetailTypeItem.setValue(hearingDetailType);
        caseData.setHearingDetailsCollection(List.of(hearingDetailTypeItem));
        List<String> errors = HearingsHelper.hearingTimeValidation(caseData);
        assertEquals(2, errors.size());
        assertTrue(errors.contains(HEARING_START_FUTURE));
        assertTrue(errors.contains(HEARING_FINISH_FUTURE));
        assertFalse(errors.contains(HEARING_BREAK_FUTURE));
        assertFalse(errors.contains(HEARING_RESUME_FUTURE));
    }

    @Test
    void earliestDateReturnsEarliestDate() {
        List<HearingTypeItem> hearingCollection = caseData.getHearingCollection();
        setListingDate(hearingCollection, 0, 2, "2100-02-01T01:01:01.000");
        setListingDate(hearingCollection, 1, 0, "2100-03-01T01:01:01.000");
        setListingDate(hearingCollection, 1, 1, "2100-01-01T01:01:01.000");
        setListingDate(hearingCollection, 2, 1, "2100-04-01T01:01:01.000");
        String actual = HearingsHelper.getEarliestFutureHearingDate(hearingCollection);
        assertEquals("2100-01-01T01:01:01.000", actual);
    }

    @Test
    void earliestDateHandlesAHearingWithNoDatesInFuture() {
        List<HearingTypeItem> hearingCollection = caseData.getHearingCollection();
        setListingDate(hearingCollection, 0, 2, "2100-02-01T01:01:01.000");
        setListingDate(hearingCollection, 1, 0, "1999-01-01T01:01:01.000");
        setListingDate(hearingCollection, 1, 1, "1999-01-01T01:01:01.000");
        setListingDate(hearingCollection, 1, 2, "1999-01-01T01:01:01.000");
        setListingDate(hearingCollection, 2, 1, "2100-04-01T01:01:01.000");
        String actual = HearingsHelper.getEarliestFutureHearingDate(hearingCollection);
        assertEquals("2100-04-01T01:01:01.000", actual);
    }

    @Test
    void earliestDateReturnsNullWhenHearingCollectionIsEmpty() {
        caseData.setHearingCollection(new ArrayList<>());
        String actual = HearingsHelper.getEarliestFutureHearingDate(caseData.getHearingCollection());
        assertNull(actual);
    }

    private void setListingDate(List<HearingTypeItem> hearingCollection, int hearingIndex, int dateIndex, String date) {
        hearingCollection.get(hearingIndex).getValue().getHearingDateCollection()
            .get(dateIndex).getValue().setListedDate(date);
    }

    private void setValidHearingStartFinishTimes() {
        HearingDetailTypeItem hearingDetailTypeItem = new HearingDetailTypeItem();
        hearingDetailTypeItem.setId(UUID.randomUUID().toString());
        HearingDetailType hearingDetailType = new HearingDetailType();
        hearingDetailType.setHearingDetailsTimingStart("2019-11-01T12:11:00.000");
        hearingDetailType.setHearingDetailsTimingFinish("2019-11-01T12:11:20.000");
        hearingDetailType.setHearingDetailsStatus(HEARING_STATUS_HEARD);
        hearingDetailTypeItem.setValue(hearingDetailType);
        caseData.setHearingDetailsCollection(List.of(hearingDetailTypeItem));
    }

    private void setHearingDetails() {
        DynamicFixedListType listType = new DynamicFixedListType();
        DynamicValueType valueType = new DynamicValueType();
        valueType.setLabel("Hearing 1, 23 June 2022");
        listType.setValue(valueType);
        caseData.setHearingDetailsHearing(listType);
    }

    @Test
    void validateTwoJudgesSameJudgeSelected() {
        caseData.setAllocateHearingSitAlone(TWO_JUDGES);
        caseData.setAllocateHearingJudge(DynamicFixedListType.of(create("judge1", "Judge 1")));
        caseData.setAllocateHearingAdditionalJudge(DynamicFixedListType.of(create("judge1", "Judge 1")));
        List<String> errors = HearingsHelper.validateTwoJudges(caseData);
        assertEquals(1, errors.size());
        assertEquals("Please choose a different judge for the second judge as the same judge has been selected for "
                     + "both judges", errors.get(0));
    }

    @Test
    void validateTwoJudgesDifferentJudgesSelected() {
        caseData.setAllocateHearingSitAlone(TWO_JUDGES);
        caseData.setAllocateHearingJudge(DynamicFixedListType.of(create("judge1", "Judge 1")));
        caseData.setAllocateHearingAdditionalJudge(DynamicFixedListType.of(create("judge2", "Judge 2")));
        List<String> errors = HearingsHelper.validateTwoJudges(caseData);
        assertEquals(0, errors.size());
    }

    @Test
    void validateTwoJudgesEmptyJudge() {
        caseData.setAllocateHearingSitAlone(TWO_JUDGES);
        caseData.setAllocateHearingJudge(DynamicFixedListType.of(create("", "")));
        caseData.setAllocateHearingAdditionalJudge(DynamicFixedListType.of(create("judge2", "Judge 2")));
        List<String> errors = HearingsHelper.validateTwoJudges(caseData);
        assertEquals(0, errors.size());
    }

    @Test
    void validateTwoJudgesEmptyAdditionalJudge() {
        caseData.setAllocateHearingSitAlone(TWO_JUDGES);
        caseData.setAllocateHearingJudge(DynamicFixedListType.of(create("judge1", "Judge 1")));
        caseData.setAllocateHearingAdditionalJudge(DynamicFixedListType.of(create("", "")));
        List<String> errors = HearingsHelper.validateTwoJudges(caseData);
        assertEquals(0, errors.size());
    }
}
