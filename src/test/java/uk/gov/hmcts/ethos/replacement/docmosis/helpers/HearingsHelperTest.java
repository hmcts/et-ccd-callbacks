package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.HearingDetailTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.HearingDetailType;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static java.time.ZoneOffset.UTC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_POSTPONED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_SETTLED;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.HEARING_BREAK_FUTURE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.HEARING_BREAK_RESUME_INVALID;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.HEARING_FINISH_FUTURE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.HEARING_FINISH_INVALID;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.HEARING_RESUME_FUTURE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.HEARING_START_FUTURE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper.findHearingNumber;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.HEARING_CREATION_DAY_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper.HEARING_CREATION_NUMBER_ERROR;

public class HearingsHelperTest {

    private CaseDetails caseDetails1;

    @BeforeEach
    public void setUp() throws Exception {
        caseDetails1 = generateCaseDetails("caseDetailsTest1.json");
    }

    private CaseDetails generateCaseDetails(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
                .getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    public void hearingMidEventValidationNumberError() {

        caseDetails1.getCaseData().getHearingCollection().get(0).getValue().setHearingNumber(null);

        assertEquals(1, HearingsHelper.hearingMidEventValidation(caseDetails1.getCaseData()).size());

        assertEquals(HEARING_CREATION_NUMBER_ERROR,
                HearingsHelper.hearingMidEventValidation(caseDetails1.getCaseData()).get(0));

        caseDetails1.getCaseData().getHearingCollection().get(0).getValue().setHearingNumber("");

        assertEquals(1, HearingsHelper.hearingMidEventValidation(caseDetails1.getCaseData()).size());

        assertEquals(HEARING_CREATION_NUMBER_ERROR,
                HearingsHelper.hearingMidEventValidation(caseDetails1.getCaseData()).get(0));

    }

    @Test
    public void hearingMidEventValidationDayError() {

        caseDetails1.getCaseData().getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().setListedDate(null);

        assertEquals(1, HearingsHelper.hearingMidEventValidation(caseDetails1.getCaseData()).size());

        assertEquals(HEARING_CREATION_DAY_ERROR,
                HearingsHelper.hearingMidEventValidation(caseDetails1.getCaseData()).get(0));

        caseDetails1.getCaseData().getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().setListedDate("");

        assertEquals(1, HearingsHelper.hearingMidEventValidation(caseDetails1.getCaseData()).size());

        assertEquals(HEARING_CREATION_DAY_ERROR,
                HearingsHelper.hearingMidEventValidation(caseDetails1.getCaseData()).get(0));

        caseDetails1.getCaseData().getHearingCollection().get(0).getValue()
                .setHearingDateCollection(null);

        assertEquals(0, HearingsHelper.hearingMidEventValidation(caseDetails1.getCaseData()).size());

    }

    @Test
    public void updatePostponedDate() {

        caseDetails1.getCaseData().getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().setHearingStatus(HEARING_STATUS_POSTPONED);

        HearingsHelper.updatePostponedDate(caseDetails1.getCaseData());

        assertNotNull(caseDetails1.getCaseData().getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getPostponedDate());

        caseDetails1.getCaseData().getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().setHearingStatus(HEARING_STATUS_SETTLED);

        HearingsHelper.updatePostponedDate(caseDetails1.getCaseData());

        assertNull(caseDetails1.getCaseData().getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getPostponedDate());
    }

    @Test
    public void findDateOfHearingTest() {
        String hearingDate = caseDetails1.getCaseData().getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(0).getValue().getListedDate().substring(0, 10);
        String hearingNumber = findHearingNumber(caseDetails1.getCaseData(), hearingDate);
        assertEquals(hearingNumber,
                caseDetails1.getCaseData().getHearingCollection().get(0).getValue().getHearingNumber());
    }

    @Test
    public void findDateOfHearing_DateNotInHearing() {
        assertNull(findHearingNumber(caseDetails1.getCaseData(), "1970-10-01"));
    }

    @Test
    public void validateStartFinishTime_validTime() {
        setValidHearingStartFinishTimes();
        List<String> errors = HearingsHelper.hearingTimeValidation(caseDetails1.getCaseData());
        assertEquals(0, errors.size());
    }

    @Test
    public void validateBreakResumeTime_invalidBreak() {
        setValidHearingStartFinishTimes();
        caseDetails1.getCaseData().getHearingDetailsCollection().get(0)
                .getValue()
                .setHearingDetailsTimingBreak("2019-11-01T00:00:00.000");
        setHearingDetails();
        List<String> errors = HearingsHelper.hearingTimeValidation(caseDetails1.getCaseData());
        assertEquals(1, errors.size());
        assertEquals(String.format(HEARING_BREAK_RESUME_INVALID, "Hearing 1"), errors.get(0));
    }

    @Test
    public void validateBreakResumeTime_invalidResume() {
        setValidHearingStartFinishTimes();
        caseDetails1.getCaseData().getHearingDetailsCollection().get(0)
                .getValue()
                .setHearingDetailsTimingResume("2019-11-01T00:00:00.000");
        setHearingDetails();
        List<String> errors = HearingsHelper.hearingTimeValidation(caseDetails1.getCaseData());
        assertEquals(1, errors.size());
        assertEquals(String.format(HEARING_BREAK_RESUME_INVALID, "Hearing 1"), errors.get(0));
    }

    @Test
    public void validateBreakResumeTime_nullBreakResume() {
        setValidHearingStartFinishTimes();
        caseDetails1.getCaseData().setHearingDetailsTimingBreak(null);
        caseDetails1.getCaseData().setHearingDetailsTimingResume(null);
        List<String> errors = HearingsHelper.hearingTimeValidation(caseDetails1.getCaseData());
        assertEquals(0, errors.size());
    }

    @Test
    public void validateStartFinishTime_sameTime() {
        HearingDetailTypeItem hearingDetailTypeItem = new HearingDetailTypeItem();
        hearingDetailTypeItem.setId(UUID.randomUUID().toString());
        HearingDetailType hearingDetailType = new HearingDetailType();
        hearingDetailType.setHearingDetailsTimingStart("2019-11-01T12:11:00.000");
        // Same time as start time
        hearingDetailType.setHearingDetailsTimingFinish("2019-11-01T12:11:00.000");
        hearingDetailType.setHearingDetailsStatus(HEARING_STATUS_HEARD);
        hearingDetailTypeItem.setValue(hearingDetailType);
        caseDetails1.getCaseData().setHearingDetailsCollection(List.of(hearingDetailTypeItem));
        setHearingDetails();
        List<String> errors = HearingsHelper.hearingTimeValidation(caseDetails1.getCaseData());
        assertEquals(1, errors.size());
        assertEquals(String.format(HEARING_FINISH_INVALID, "Hearing 1"), errors.get(0));
    }

    @Test
    public void validateStartFinishTime_finishTimeBeforeStart() {
        HearingDetailTypeItem hearingDetailTypeItem = new HearingDetailTypeItem();
        hearingDetailTypeItem.setId(UUID.randomUUID().toString());
        HearingDetailType hearingDetailType = new HearingDetailType();
        hearingDetailType.setHearingDetailsTimingStart("2019-11-01T12:11:00.000");
        hearingDetailType.setHearingDetailsTimingFinish("2019-11-01T12:10:00.000");
        hearingDetailType.setHearingDetailsStatus(HEARING_STATUS_HEARD);
        hearingDetailTypeItem.setValue(hearingDetailType);
        caseDetails1.getCaseData().setHearingDetailsCollection(List.of(hearingDetailTypeItem));
        setHearingDetails();
        List<String> errors = HearingsHelper.hearingTimeValidation(caseDetails1.getCaseData());
        assertEquals(1, errors.size());
        assertEquals(String.format(HEARING_FINISH_INVALID, "Hearing 1"), errors.get(0));
    }

    @Test
    public void validateHearingDatesInPastTest() {
        HearingDetailTypeItem hearingDetailTypeItem = new HearingDetailTypeItem();
        hearingDetailTypeItem.setId(UUID.randomUUID().toString());
        HearingDetailType hearingDetailType = new HearingDetailType();
        hearingDetailType.setHearingDetailsTimingBreak("2021-12-19T10:00:00");
        hearingDetailType.setHearingDetailsTimingResume("2021-12-19T10:00:00");
        hearingDetailType.setHearingDetailsTimingFinish("2021-12-19T10:10:00");
        hearingDetailType.setHearingDetailsTimingStart("2021-12-19T10:00:00");
        hearingDetailType.setHearingDetailsStatus(HEARING_STATUS_HEARD);
        hearingDetailTypeItem.setValue(hearingDetailType);
        caseDetails1.getCaseData().setHearingDetailsCollection(List.of(hearingDetailTypeItem));
        List<String> errors = HearingsHelper.hearingTimeValidation(caseDetails1.getCaseData());
        assertEquals(0, errors.size());
    }

    @Test
    public void validateIsDateInFutureConsideringDST() {
        LocalDateTime dateTime = LocalDateTime.now().minusMinutes(25);
        LocalDateTime now = LocalDateTime.now(UTC);
        boolean val = HearingsHelper.isDateInFuture(dateTime.toString(), now);
        assertFalse(val);
    }

    @Test
    public void invalidateHearingDatesInFutureTest() {
        HearingDetailTypeItem hearingDetailTypeItem = new HearingDetailTypeItem();
        hearingDetailTypeItem.setId(UUID.randomUUID().toString());
        HearingDetailType hearingDetailType = new HearingDetailType();
        hearingDetailType.setHearingDetailsTimingStart("2222-11-01T12:11:00.000");
        hearingDetailType.setHearingDetailsTimingFinish("2222-11-01T12:11:20.000");
        hearingDetailType.setHearingDetailsTimingResume("2222-11-01T12:11:20.000");
        hearingDetailType.setHearingDetailsTimingBreak("2222-11-01T12:11:20.000");
        hearingDetailType.setHearingDetailsStatus(HEARING_STATUS_HEARD);
        hearingDetailTypeItem.setValue(hearingDetailType);
        caseDetails1.getCaseData().setHearingDetailsCollection(List.of(hearingDetailTypeItem));
        List<String> errors = HearingsHelper.hearingTimeValidation(caseDetails1.getCaseData());
        assertEquals(4, errors.size());
        assertTrue(errors.contains(HEARING_START_FUTURE));
        assertTrue(errors.contains(HEARING_FINISH_FUTURE));
        assertTrue(errors.contains(HEARING_BREAK_FUTURE));
        assertTrue(errors.contains(HEARING_RESUME_FUTURE));
    }

    @Test
    public void invalidateHearingDatesInFutureTestNullCheck() {
        HearingDetailTypeItem hearingDetailTypeItem = new HearingDetailTypeItem();
        hearingDetailTypeItem.setId(UUID.randomUUID().toString());
        HearingDetailType hearingDetailType = new HearingDetailType();
        hearingDetailType.setHearingDetailsTimingStart("2222-11-01T12:11:00.000");
        hearingDetailType.setHearingDetailsTimingFinish("2222-11-01T12:11:20.000");
        hearingDetailType.setHearingDetailsTimingResume(null);
        hearingDetailType.setHearingDetailsTimingBreak(null);
        hearingDetailType.setHearingDetailsStatus(HEARING_STATUS_HEARD);
        hearingDetailTypeItem.setValue(hearingDetailType);
        caseDetails1.getCaseData().setHearingDetailsCollection(List.of(hearingDetailTypeItem));
        List<String> errors = HearingsHelper.hearingTimeValidation(caseDetails1.getCaseData());
        assertEquals(2, errors.size());
        assertTrue(errors.contains(HEARING_START_FUTURE));
        assertTrue(errors.contains(HEARING_FINISH_FUTURE));
        assertFalse(errors.contains(HEARING_BREAK_FUTURE));
        assertFalse(errors.contains(HEARING_RESUME_FUTURE));
    }

    @Test
    public void earliestDateReturnsEarliestDate() {
        List<HearingTypeItem> hearingCollection = caseDetails1.getCaseData().getHearingCollection();
        setListingDate(hearingCollection, 0, 2, "2100-02-01T01:01:01.000");
        setListingDate(hearingCollection, 1, 0, "2100-03-01T01:01:01.000");
        setListingDate(hearingCollection, 1, 1, "2100-01-01T01:01:01.000");
        setListingDate(hearingCollection, 2, 1, "2100-04-01T01:01:01.000");
        String actual = HearingsHelper.getEarliestFutureHearingDate(hearingCollection);
        assertEquals("2100-01-01T01:01:01.000", actual);
    }

    @Test
    public void earliestDateHandlesAHearingWithNoDatesInFuture() {
        List<HearingTypeItem> hearingCollection = caseDetails1.getCaseData().getHearingCollection();
        setListingDate(hearingCollection, 0, 2, "2100-02-01T01:01:01.000");
        setListingDate(hearingCollection, 1, 0, "1999-01-01T01:01:01.000");
        setListingDate(hearingCollection, 1, 1, "1999-01-01T01:01:01.000");
        setListingDate(hearingCollection, 1, 2, "1999-01-01T01:01:01.000");
        setListingDate(hearingCollection, 2, 1, "2100-04-01T01:01:01.000");
        String actual = HearingsHelper.getEarliestFutureHearingDate(hearingCollection);
        assertEquals("2100-04-01T01:01:01.000", actual);
    }

    @Test
    public void earliestDateReturnsNullWhenHearingCollectionIsEmpty() {
        caseDetails1.getCaseData().setHearingCollection(new ArrayList<>());
        String actual = HearingsHelper.getEarliestFutureHearingDate(caseDetails1.getCaseData().getHearingCollection());
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
        caseDetails1.getCaseData().setHearingDetailsCollection(List.of(hearingDetailTypeItem));
    }

    private void setHearingDetails() {
        DynamicFixedListType listType = new DynamicFixedListType();
        DynamicValueType valueType = new DynamicValueType();
        valueType.setLabel("Hearing 1, 23 June 2022");
        listType.setValue(valueType);
        caseDetails1.getCaseData().setHearingDetailsHearing(listType);
    }
}
