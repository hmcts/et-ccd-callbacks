package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingDetailTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingDetailType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.BREAK_TIME_VALIDATION_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.HEARING_BREAK_FUTURE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.HEARING_BREAK_RESUME_INVALID;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.HEARING_FINISH_FUTURE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.HEARING_FINISH_INVALID;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.HEARING_RESUME_FUTURE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.HEARING_START_FUTURE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.RESUME_TIME_VALIDATION_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.findHearingNumber;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.HEARING_CREATION_DAY_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.HEARING_CREATION_NUMBER_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.TWO_JUDGES;

@Slf4j
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

        caseData.getHearingCollection().getFirst().getValue().setHearingNumber(null);

        assertEquals(1, HearingsHelper.hearingMidEventValidation(caseData).size());

        assertEquals(HEARING_CREATION_NUMBER_ERROR,
                HearingsHelper.hearingMidEventValidation(caseData).getFirst());

        caseData.getHearingCollection().getFirst().getValue().setHearingNumber("");

        assertEquals(1, HearingsHelper.hearingMidEventValidation(caseData).size());

        assertEquals(HEARING_CREATION_NUMBER_ERROR,
                HearingsHelper.hearingMidEventValidation(caseData).getFirst());

    }

    @Test
    void hearingMidEventValidationDayError() {

        caseData.getHearingCollection().getFirst().getValue()
                .getHearingDateCollection().getFirst().getValue().setListedDate(null);

        assertEquals(1, HearingsHelper.hearingMidEventValidation(caseData).size());

        assertEquals(HEARING_CREATION_DAY_ERROR,
                HearingsHelper.hearingMidEventValidation(caseData).getFirst());

        caseData.getHearingCollection().getFirst().getValue()
                .getHearingDateCollection().getFirst().getValue().setListedDate("");

        assertEquals(1, HearingsHelper.hearingMidEventValidation(caseData).size());

        assertEquals(HEARING_CREATION_DAY_ERROR,
                HearingsHelper.hearingMidEventValidation(caseData).getFirst());

        caseData.getHearingCollection().getFirst().getValue()
                .setHearingDateCollection(null);

        assertEquals(0, HearingsHelper.hearingMidEventValidation(caseData).size());

    }

    @Test
    void updatePostponedDate() {

        caseData.getHearingCollection().getFirst().getValue()
                .getHearingDateCollection().getFirst().getValue().setHearingStatus(HEARING_STATUS_POSTPONED);

        Helper.updatePostponedDate(caseData);

        assertNotNull(caseData.getHearingCollection().getFirst().getValue()
                .getHearingDateCollection().getFirst().getValue().getPostponedDate());

        caseData.getHearingCollection().getFirst().getValue()
                .getHearingDateCollection().getFirst().getValue().setHearingStatus(HEARING_STATUS_SETTLED);

        Helper.updatePostponedDate(caseData);

        assertNull(caseData.getHearingCollection().getFirst().getValue()
                .getHearingDateCollection().getFirst().getValue().getPostponedDate());
    }

    @Test
    void findDateOfHearingTest() {
        String hearingDate = caseData.getHearingCollection().getFirst().getValue()
                .getHearingDateCollection().getFirst().getValue().getListedDate().substring(0, 10);
        String hearingNumber = findHearingNumber(caseData, hearingDate);
        assertEquals(hearingNumber,
                caseData.getHearingCollection().getFirst().getValue().getHearingNumber());
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
        caseData.getHearingDetailsCollection().getFirst()
                .getValue()
                .setHearingDetailsTimingBreak("2019-11-01T00:00:00.000");
        caseData.getHearingDetailsCollection().getFirst()
                .getValue()
                .setHearingDetailsTimingResume("2019-11-01T12:11:10.000");
        setHearingDetails();
        List<String> errors = HearingsHelper.hearingTimeValidation(caseData);
        assertEquals(1, errors.size());
        assertEquals(String.format(HEARING_BREAK_RESUME_INVALID, "Hearing 1"), errors.getFirst());
    }

    @Test
    void validateBreakResumeTime_invalidResume() {
        setValidHearingStartFinishTimes();
        caseData.getHearingDetailsCollection().getFirst()
                .getValue()
                .setHearingDetailsTimingResume("2019-11-01T00:00:00.000");
        caseData.getHearingDetailsCollection().getFirst()
                .getValue()
                .setHearingDetailsTimingBreak("2019-11-01T12:11:05.000");
        setHearingDetails();
        List<String> errors = HearingsHelper.hearingTimeValidation(caseData);
        assertEquals(1, errors.size());
        assertEquals(String.format(HEARING_BREAK_RESUME_INVALID, "Hearing 1"), errors.getFirst());
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
        assertEquals(String.format(HEARING_FINISH_INVALID, "Hearing 1"), errors.getFirst());
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
        assertEquals(String.format(HEARING_FINISH_INVALID, "Hearing 1"), errors.getFirst());
    }

    @Test
    void validateHearingDatesInPastTest() {
        HearingDetailTypeItem hearingDetailTypeItem = new HearingDetailTypeItem();
        hearingDetailTypeItem.setId(UUID.randomUUID().toString());
        HearingDetailType hearingDetailType = new HearingDetailType();
        hearingDetailType.setHearingDetailsTimingBreak("2021-12-19T10:01:00");
        hearingDetailType.setHearingDetailsTimingResume("2021-12-19T10:05:00");
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
        hearingDetailType.setHearingDetailsTimingBreak("2222-11-01T12:11:10.000");
        hearingDetailType.setHearingDetailsTimingResume("2222-11-01T12:11:15.000");
        hearingDetailType.setHearingDetailsTimingFinish("2222-11-01T12:11:20.000");
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

    @Test
    void returnsNullWhenHearingCollectionIsEmpty() {
        assertNull(HearingsHelper.getEarliestListedHearingType(Collections.emptyList()));
    }

    @Test
    void returnsNullWhenAllHearingsHaveNoFutureDates() {
        DateListedTypeItem date = new DateListedTypeItem();
        date.setValue(new uk.gov.hmcts.et.common.model.ccd.types.DateListedType());
        date.getValue().setListedDate("2020-01-01T10:00:00.000"); // Past date
        HearingType hearingType = new HearingType();
        hearingType.setHearingDateCollection(List.of(date));
        HearingTypeItem item = new HearingTypeItem();
        item.setValue(hearingType);
        assertNull(HearingsHelper.getEarliestListedHearingType(List.of(item)));
    }

    @Test
    void returnsEarliestFutureHearingWhenMultipleHearingsWithFutureDates() {
        DateListedTypeItem date1 = new DateListedTypeItem();
        date1.setValue(new uk.gov.hmcts.et.common.model.ccd.types.DateListedType());
        date1.getValue().setListedDate("2100-01-01T10:00:00.000");
        date1.getValue().setHearingStatus("Listed");
        HearingTypeItem item1 = new HearingTypeItem();
        HearingType hearingType1 = new HearingType();
        hearingType1.setHearingDateCollection(List.of(date1));
        item1.setValue(hearingType1);

        DateListedTypeItem date2 = new DateListedTypeItem();
        date2.setValue(new uk.gov.hmcts.et.common.model.ccd.types.DateListedType());
        date2.getValue().setListedDate("2099-12-31T10:00:00.000");
        date2.getValue().setHearingStatus("Listed");
        HearingTypeItem item2 = new HearingTypeItem();
        HearingType hearingType2 = new HearingType();
        hearingType2.setHearingDateCollection(List.of(date2));
        item2.setValue(hearingType2);

        HearingType result = HearingsHelper.getEarliestListedHearingType(List.of(item1, item2));
        assertNotNull(result);
        assertEquals("2099-12-31T10:00:00.000",
                result.getHearingDateCollection().getFirst().getValue().getListedDate());
    }

    @Test
    void ignoresHearingsWithNoValidFutureDates() {
        HearingTypeItem item1 = new HearingTypeItem();
        HearingType hearingType1 = new HearingType();
        hearingType1.setHearingDateCollection(new ArrayList<>());
        item1.setValue(hearingType1);

        DateListedTypeItem date2 = new DateListedTypeItem();
        date2.setValue(new uk.gov.hmcts.et.common.model.ccd.types.DateListedType());
        date2.getValue().setListedDate("2099-12-31T10:00:00.000");
        date2.getValue().setHearingStatus("Listed");
        HearingTypeItem item2 = new HearingTypeItem();
        HearingType hearingType2 = new HearingType();
        hearingType2.setHearingDateCollection(List.of(date2));
        item2.setValue(hearingType2);

        HearingType result = HearingsHelper.getEarliestListedHearingType(List.of(item1, item2));
        assertNotNull(result);
        assertEquals("2099-12-31T10:00:00.000",
                result.getHearingDateCollection().getFirst().getValue().getListedDate());
    }

    @Test
    void returnsNullIfNoHearingsHaveListedStatus() {
        DateListedTypeItem date = new DateListedTypeItem();
        date.setValue(new uk.gov.hmcts.et.common.model.ccd.types.DateListedType());
        date.getValue().setListedDate("2099-12-31T10:00:00.000");
        HearingTypeItem item = new HearingTypeItem();
        HearingType hearingType = new HearingType();
        hearingType.setHearingDateCollection(List.of(date));
        item.setValue(hearingType);
        // Simulate isListedHearing always returns false
        // This would require mocking if isListedHearing is not static/final
        // For now, assume it returns false and test returns null
        assertNull(HearingsHelper.getEarliestListedHearingType(List.of(item)));
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
                     + "both judges", errors.getFirst());
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

    @Test
    void validateBreakTime_equalToStart_addsBreakTimeValidationErrorOnly() {
        setValidHearingStartFinishTimes();
        // start is 12:11:00.000 per helper; set break equal to start
        caseData.getHearingDetailsCollection().getFirst().getValue()
            .setHearingDetailsTimingBreak("2019-11-01T12:11:00.000");
        caseData.getHearingDetailsCollection().getFirst().getValue()
            .setHearingDetailsTimingResume("2019-11-01T12:11:10.000");
        setHearingDetails();

        List<String> errors = HearingsHelper.hearingTimeValidation(caseData);
        assertEquals(1, errors.size());
        assertEquals(String.format(BREAK_TIME_VALIDATION_MESSAGE, "Hearing 1"), errors.getFirst());
    }

    @Test
    void validateBreakTime_afterResume_addsBothBreakAndResumeErrors() {
        setValidHearingStartFinishTimes();
        // break after resume should trigger both errors
        caseData.getHearingDetailsCollection().getFirst().getValue()
            .setHearingDetailsTimingBreak("2019-11-01T12:11:15.000");
        caseData.getHearingDetailsCollection().getFirst().getValue()
            .setHearingDetailsTimingResume("2019-11-01T12:11:10.000");
        setHearingDetails();

        List<String> errors = HearingsHelper.hearingTimeValidation(caseData);
        assertEquals(2, errors.size());
        assertTrue(errors.contains(String.format(BREAK_TIME_VALIDATION_MESSAGE, "Hearing 1")));
        assertTrue(errors.contains(String.format(RESUME_TIME_VALIDATION_MESSAGE, "Hearing 1")));
    }

    @Test
    void validateResumeTime_equalToFinish_addsResumeTimeValidationErrorOnly() {
        setValidHearingStartFinishTimes();
        caseData.getHearingDetailsCollection().getFirst().getValue()
            .setHearingDetailsTimingBreak("2019-11-01T12:11:10.000");
        // resume equal to finish time
        caseData.getHearingDetailsCollection().getFirst().getValue()
            .setHearingDetailsTimingResume("2019-11-01T12:11:20.000");
        setHearingDetails();

        List<String> errors = HearingsHelper.hearingTimeValidation(caseData);
        assertEquals(1, errors.size());
        assertEquals(String.format(RESUME_TIME_VALIDATION_MESSAGE, "Hearing 1"), errors.getFirst());
    }

    @Test
    void validateBreakTime_beforeStart_addsBreakTimeValidationErrorOnly() {
        setValidHearingStartFinishTimes();
        // break before start
        caseData.getHearingDetailsCollection().getFirst().getValue()
            .setHearingDetailsTimingBreak("2019-11-01T12:10:59.000");
        caseData.getHearingDetailsCollection().getFirst().getValue()
            .setHearingDetailsTimingResume("2019-11-01T12:11:10.000");
        setHearingDetails();

        List<String> errors = HearingsHelper.hearingTimeValidation(caseData);
        assertEquals(1, errors.size());
        assertEquals(String.format(BREAK_TIME_VALIDATION_MESSAGE, "Hearing 1"), errors.getFirst());
    }

    @Test
    void validateResumeTime_afterFinish_addsResumeTimeValidationErrorOnly() {
        setValidHearingStartFinishTimes();
        caseData.getHearingDetailsCollection().getFirst().getValue()
            .setHearingDetailsTimingBreak("2019-11-01T12:11:10.000");
        // resume after finish
        caseData.getHearingDetailsCollection().getFirst().getValue()
            .setHearingDetailsTimingResume("2019-11-01T12:11:21.000");
        setHearingDetails();

        List<String> errors = HearingsHelper.hearingTimeValidation(caseData);
        assertEquals(1, errors.size());
        assertEquals(String.format(RESUME_TIME_VALIDATION_MESSAGE, "Hearing 1"), errors.getFirst());
    }
}
