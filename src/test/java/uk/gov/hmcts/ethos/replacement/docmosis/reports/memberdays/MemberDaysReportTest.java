package uk.gov.hmcts.ethos.replacement.docmosis.reports.memberdays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_LISTED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_PERLIMINARY_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.JURISDICTION_OUTCOME_SUCCESSFUL_AT_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MEMBER_DAYS_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_HEARING_DATE_TYPE;

@SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.LawOfDemeter", "PMD.NcssCount", "PMD.TooManyMethods"})
class MemberDaysReportTest {
    private List<SubmitEvent> submitEvents;
    private ListingDetails listingDetails;
    private MemberDaysReport memberDaysReport;
    private static final String SIT_ALONE_PANEL = "Sit Alone";
    private static final String FULL_PANEL = "Full Panel";

    @BeforeEach
    void setUp() {
        memberDaysReport = new MemberDaysReport();
        listingDetails = new ListingDetails();
        ListingData listingData = new ListingData();
        listingData.setListingDateFrom("2019-12-08");
        listingData.setListingDateTo("2019-12-20");
        listingData.setListingVenue(new DynamicFixedListType("Leeds"));
        listingData.setReportType(MEMBER_DAYS_REPORT);
        listingData.setHearingDateType(RANGE_HEARING_DATE_TYPE);

        listingDetails.setCaseData(listingData);
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.setJurisdiction("EMPLOYMENT");

        SubmitEvent submitEvent1 = new SubmitEvent();
        submitEvent1.setCaseId(1);
        submitEvent1.setState(ACCEPTED_STATE);

        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("1800522/2020");
        caseData.setReceiptDate("2018-08-10");
        CasePreAcceptType casePreAcceptType = new CasePreAcceptType();
        casePreAcceptType.setDateAccepted("2018-08-10");
        caseData.setPreAcceptCase(casePreAcceptType);
        DateListedType dateListedType = new DateListedType();
        dateListedType.setHearingStatus(HEARING_STATUS_HEARD);
        dateListedType.setHearingClerk(new DynamicFixedListType("Clerk A"));
        dateListedType.setHearingAberdeen(new DynamicFixedListType("AberdeenVenue"));
        dateListedType.setHearingVenueDay(new DynamicFixedListType("Aberdeen"));
        dateListedType.setListedDate("2019-12-11T12:11:00.000");
        dateListedType.setHearingTimingStart("2019-12-11T12:11:00.000");
        dateListedType.setHearingTimingBreak("2019-12-11T12:11:00");
        dateListedType.setHearingTimingResume("2019-12-11T12:11:00");
        dateListedType.setHearingTimingFinish("2019-12-11T14:11:00.000");
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setId("12300");
        dateListedTypeItem.setValue(dateListedType);
        DateListedType dateListedType1 = new DateListedType();
        dateListedType1.setHearingStatus(HEARING_STATUS_HEARD);
        dateListedType1.setHearingClerk(new DynamicFixedListType("Clerk B"));
        dateListedType1.setHearingAberdeen(new DynamicFixedListType("AberdeenVenue"));
        dateListedType1.setHearingVenueDay(new DynamicFixedListType("Aberdeen"));
        dateListedType1.setListedDate("2019-12-10T12:11:00.000");
        dateListedType1.setHearingTimingStart("2019-12-10T11:00:00.000");
        dateListedType1.setHearingTimingBreak("2019-12-10T12:00:00");
        dateListedType1.setHearingTimingResume("2019-12-10T13:00:00");
        dateListedType1.setHearingTimingFinish("2019-12-10T14:00:00.000");
        DateListedTypeItem dateListedTypeItem1 = new DateListedTypeItem();
        dateListedTypeItem1.setId("12400");
        dateListedTypeItem1.setValue(dateListedType1);
        DateListedType dateListedType2 = new DateListedType();
        dateListedType2.setHearingStatus(HEARING_STATUS_LISTED);
        dateListedType2.setHearingClerk(new DynamicFixedListType("Clerk Space"));
        dateListedType2.setHearingAberdeen(new DynamicFixedListType("AberdeenVenue2"));
        dateListedType2.setHearingVenueDay(new DynamicFixedListType("Aberdeen"));
        dateListedType2.setListedDate("2019-12-12T12:11:30.000");
        dateListedType2.setHearingTimingStart("2019-12-12T12:30:00.000");
        dateListedType2.setHearingTimingBreak("2019-12-12T12:30:00");
        dateListedType2.setHearingTimingResume("2019-12-12T12:30:00");
        dateListedType2.setHearingTimingFinish("2019-12-12T14:30:00.000");
        DateListedTypeItem dateListedTypeItem2 = new DateListedTypeItem();
        dateListedTypeItem2.setId("12500");
        dateListedTypeItem2.setValue(dateListedType2);
        DateListedType dateListedType3 = new DateListedType();
        dateListedType3.setHearingStatus(HEARING_STATUS_HEARD);
        dateListedType3.setHearingClerk(new DynamicFixedListType("Clerk3"));
        dateListedType3.setHearingAberdeen(new DynamicFixedListType("AberdeenVenue2"));
        dateListedType3.setHearingVenueDay(new DynamicFixedListType("Aberdeen"));
        dateListedType3.setListedDate("2019-12-13T12:11:55.000");
        dateListedType3.setHearingTimingStart("2019-12-13T14:11:55.000");
        dateListedType3.setHearingTimingBreak("2019-12-13T15:11:55");
        dateListedType3.setHearingTimingResume("2019-12-13T15:30:55");
        dateListedType3.setHearingTimingFinish("2019-12-13T16:30:55.000");
        DateListedTypeItem dateListedTypeItem3 = new DateListedTypeItem();
        dateListedTypeItem3.setId("12600");
        dateListedTypeItem3.setValue(dateListedType3);
        HearingType hearingType = new HearingType();
        hearingType.setHearingNumber("33");
        hearingType.setHearingSitAlone(FULL_PANEL);
        hearingType.setHearingVenue(new DynamicFixedListType("Aberdeen"));
        hearingType.setHearingEstLengthNum("2");
        hearingType.setHearingEstLengthNumType("hours");
        hearingType.setHearingType(HEARING_TYPE_PERLIMINARY_HEARING);
        hearingType.setHearingERMember(new DynamicFixedListType("er memb 0"));
        hearingType.setHearingEEMember(new DynamicFixedListType("ee memb 0"));
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setId("12345");
        hearingTypeItem.setValue(hearingType);
        hearingType.setHearingDateCollection(new ArrayList<>(Arrays.asList(dateListedTypeItem,
            dateListedTypeItem1, dateListedTypeItem2, dateListedTypeItem3)));

        JurCodesTypeItem jurCodesTypeItem = new JurCodesTypeItem();
        JurCodesType jurCodesType = new JurCodesType();
        jurCodesType.setJuridictionCodesList("ABC");
        jurCodesType.setJudgmentOutcome(JURISDICTION_OUTCOME_SUCCESSFUL_AT_HEARING);
        jurCodesTypeItem.setId("000");
        jurCodesTypeItem.setValue(jurCodesType);

        List<HearingTypeItem> hearingTypeItems = new ArrayList<>();
        hearingTypeItems.add(hearingTypeItem);
        caseData.setHearingCollection(hearingTypeItems);
        submitEvent1.setCaseData(caseData);

        CaseData caseData2 = new CaseData();
        caseData2.setEthosCaseReference("1800522/2020");
        caseData2.setReceiptDate("2018-08-10");
        CasePreAcceptType casePreAcceptType2 = new CasePreAcceptType();
        casePreAcceptType2.setDateAccepted("2018-08-10");
        caseData2.setPreAcceptCase(casePreAcceptType2);
        caseData2.setEcmCaseType(SINGLE_CASE_TYPE);
        HearingType hearingType2 = new HearingType();
        hearingType2.setHearingNumber("53");
        hearingType2.setHearingSitAlone(SIT_ALONE_PANEL);
        hearingType2.setHearingVenue(new DynamicFixedListType("Aberdeen"));
        hearingType2.setHearingEstLengthNum("3");
        hearingType2.setHearingEstLengthNumType("hours");
        hearingType2.setHearingType(HEARING_TYPE_PERLIMINARY_HEARING);
        HearingTypeItem hearingTypeItem2 = new HearingTypeItem();
        hearingTypeItem2.setId("12345000");
        hearingType2.setHearingEEMember(new DynamicFixedListType("ee memb 2"));
        hearingType2.setHearingERMember(new DynamicFixedListType("er memb 2"));
        hearingType2.setHearingDateCollection(new ArrayList<>(Arrays.asList(dateListedTypeItem,
            dateListedTypeItem1, dateListedTypeItem2, dateListedTypeItem3)));
        hearingTypeItem2.setValue(hearingType2);

        List<HearingTypeItem> hearingTypeItems2 = new ArrayList<>();
        hearingTypeItems2.add(hearingTypeItem2);
        caseData2.setHearingCollection(hearingTypeItems2);

        CaseData caseData3 = new CaseData();
        caseData3.setEthosCaseReference("1800522/2020");
        caseData3.setReceiptDate("2018-08-12");
        DateListedType dateListedType5 = new DateListedType();
        dateListedType5.setHearingStatus(HEARING_STATUS_HEARD);
        dateListedType5.setHearingClerk(new DynamicFixedListType("Clerk3"));
        dateListedType5.setHearingAberdeen(new DynamicFixedListType("AberdeenVenue2"));
        dateListedType5.setHearingVenueDay(new DynamicFixedListType("Aberdeen"));
        dateListedType5.setListedDate("2019-12-14T12:11:55.000");
        dateListedType5.setHearingTimingStart("2019-12-14T14:11:55.000");
        dateListedType5.setHearingTimingBreak("2019-12-14T15:11:55");
        dateListedType5.setHearingTimingResume("2019-12-14T15:30:55");
        dateListedType5.setHearingTimingFinish("2019-12-14T16:30:55.000");
        DateListedTypeItem dateListedTypeItem5 = new DateListedTypeItem();
        dateListedTypeItem5.setId("12600");
        dateListedTypeItem5.setValue(dateListedType5);

        SubmitEvent submitEvent2 = new SubmitEvent();
        submitEvent2.setCaseId(2);
        submitEvent2.setState(ACCEPTED_STATE);
        submitEvent2.setCaseData(caseData2);

        CasePreAcceptType casePreAcceptType3 = new CasePreAcceptType();
        casePreAcceptType3.setDateAccepted("2018-08-12");
        caseData3.setPreAcceptCase(casePreAcceptType3);
        caseData3.setEcmCaseType(SINGLE_CASE_TYPE);
        HearingType hearingType3 = new HearingType();
        hearingType3.setHearingNumber("56");
        hearingType3.setHearingSitAlone(FULL_PANEL);
        hearingType3.setHearingVenue(new DynamicFixedListType("Aberdeen"));
        hearingType3.setHearingEstLengthNum("1");
        hearingType3.setHearingEstLengthNumType("hours");
        hearingType3.setHearingType(HEARING_TYPE_PERLIMINARY_HEARING);
        hearingType3.setHearingEEMember(new DynamicFixedListType("ee memb 1"));
        hearingType3.setHearingERMember(new DynamicFixedListType("er memb 1"));
        HearingTypeItem hearingTypeItem3 = new HearingTypeItem();
        hearingTypeItem3.setId("1234500033");

        List<HearingTypeItem> hearingTypeItems3 = new ArrayList<>();
        hearingTypeItems3.add(hearingTypeItem3);
        DateListedType dateListedType6 = new DateListedType();
        dateListedType6.setHearingStatus(HEARING_STATUS_HEARD);
        dateListedType6.setHearingClerk(new DynamicFixedListType("Clerk3"));
        dateListedType6.setHearingAberdeen(new DynamicFixedListType("AberdeenVenue2"));
        dateListedType6.setHearingVenueDay(new DynamicFixedListType("Aberdeen"));
        dateListedType6.setListedDate("2019-12-14T12:11:55.000");
        dateListedType6.setHearingTimingStart("2019-12-14T13:11:55.000");
        dateListedType6.setHearingTimingBreak("2019-12-14T15:11:55");
        dateListedType6.setHearingTimingResume("2019-12-14T15:30:55");
        dateListedType6.setHearingTimingFinish("2019-12-14T19:30:55.000");
        DateListedTypeItem dateListedTypeItem6 = new DateListedTypeItem();
        dateListedTypeItem6.setId("12600334");
        dateListedTypeItem6.setValue(dateListedType6);

        hearingType3.setHearingDateCollection(new ArrayList<>(Arrays.asList(dateListedTypeItem2, dateListedTypeItem3,
            dateListedTypeItem6)));
        hearingTypeItem3.setValue(hearingType3);
        caseData3.setHearingCollection(hearingTypeItems3);

        SubmitEvent submitEvent3 = new SubmitEvent();
        submitEvent3.setCaseId(3);
        submitEvent3.setState(ACCEPTED_STATE);
        submitEvent3.setCaseData(caseData3);
        submitEvents = new ArrayList<>();
        submitEvents.add(submitEvent1);
        submitEvents.add(submitEvent2);
        submitEvents.add(submitEvent3);
    }

    @Test
    void shouldReturnMembersDayReportType() {
        MemberDaysReportData resultListingData = memberDaysReport.runReport(listingDetails, submitEvents);
        assertEquals(MEMBER_DAYS_REPORT, resultListingData.getReportType());
    }

    @Test
    void shouldReturnZeroReportDetailsEntriesForEmptySubmitEvents() {
        MemberDaysReportData resultListingData = memberDaysReport.runReport(listingDetails, null);
        int actualHeardHearingsCount  = resultListingData.getReportDetails().size();
        int expectedHeardHearingsCount = 0;
        assertEquals(expectedHeardHearingsCount, actualHeardHearingsCount);
    }

    @Test
    void shouldIncludeOnlyCasesWithHeardHearingStatus() {
        MemberDaysReportData resultListingData = memberDaysReport.runReport(listingDetails, submitEvents);
        int actualHeardHearingsCount  = resultListingData.getReportDetails().size();
        int expectedHeardHearingsCount = 5;
        assertEquals(expectedHeardHearingsCount, actualHeardHearingsCount);
    }

    @Test
    void shouldIncludeOnlyCasesWithFullPanelHearing() {
        List<Long> validHearingsCountList = new ArrayList<>();
        submitEvents.forEach(s -> validHearingsCountList.add(getValidHearingsInCurrentSubmitEvent(s)));
        long expectedFullPanelHearingsCount = validHearingsCountList.stream().filter(x -> x > 0).count();
        String expectedReportDateType = "Range";
        MemberDaysReportData resultListingData = memberDaysReport.runReport(listingDetails, submitEvents);
        long actualFullPanelHearingsCount  = resultListingData.getReportDetails()
            .stream().map(MemberDaysReportDetail::getParentHearingId)
            .collect(Collectors.toList()).stream().distinct().count();
        String actualReportDateType = resultListingData.getHearingDateType();
        assertEquals(expectedFullPanelHearingsCount, actualFullPanelHearingsCount);
        assertEquals(expectedReportDateType, actualReportDateType);
    }

    @Test
    void shouldReturnZeroCasesWhenForNoHearingsWithFullPanelHearing() {
        submitEvents.forEach(s -> s.getCaseData().getHearingCollection()
            .forEach(h -> h.getValue().setHearingSitAlone(SIT_ALONE_PANEL)));
        String expectedReportDateType = "Range";
        MemberDaysReportData resultListingData = memberDaysReport.runReport(listingDetails, submitEvents);
        long actualFullPanelHearingsCount  = resultListingData.getReportDetails()
            .stream().map(MemberDaysReportDetail::getParentHearingId)
            .collect(Collectors.toList()).stream().distinct().count();
        String actualReportDateType = resultListingData.getHearingDateType();
        assertEquals(0, actualFullPanelHearingsCount);
        assertEquals(expectedReportDateType, actualReportDateType);
    }

    @Test
    void shouldIncludeOnlyCasesWithValidHearingDates() {
        SubmitEvent thirdSubmitEvent = submitEvents.get(2);
        HearingTypeItem caseData3FirstHearing = thirdSubmitEvent.getCaseData().getHearingCollection().get(0);
        DateListedType dateListedTypeToSetToInvalidRange = caseData3FirstHearing.getValue()
            .getHearingDateCollection().get(2).getValue();
        dateListedTypeToSetToInvalidRange.setListedDate("2019-12-29T12:11:55.000");
        List<DateListedTypeItem> dateListedTypeItems = extractDateListedTypeItems(submitEvents);
        long expectedValidHearingDatesCount = dateListedTypeItems.stream().distinct().count();
        String expectedReportDateType = "Range";
        MemberDaysReportData resultListingData = memberDaysReport.runReport(listingDetails, submitEvents);
        int actualValidHearingDatesCount  = resultListingData.getReportDetails().size();
        String actualReportDateType = resultListingData.getHearingDateType();
        assertEquals(expectedValidHearingDatesCount, actualValidHearingDatesCount);
        assertEquals(expectedReportDateType, actualReportDateType);
    }

    private Long getValidHearingsInCurrentSubmitEvent(SubmitEvent submitEvent) {
        return submitEvent.getCaseData().getHearingCollection().stream()
            .filter(h -> FULL_PANEL.equals(h.getValue().getHearingSitAlone())).count();
    }

    @Test
    void shouldReturnCorrectHearingDateCountForListedDateWithSpaceAndMilliseconds() {
        SubmitEvent thirdSubmitEvent = submitEvents.get(2);
        HearingTypeItem caseData3FirstHearing = thirdSubmitEvent.getCaseData().getHearingCollection().get(0);
        caseData3FirstHearing.getValue().getHearingDateCollection().get(0)
            .getValue().setListedDate("2019-12-16 12:11:55.000");
        List<DateListedTypeItem> dateListedTypeItems = extractDateListedTypeItems(submitEvents);
        long expectedValidHearingDatesCount = dateListedTypeItems.stream().distinct().count();
        String expectedReportDateType = "Range";
        MemberDaysReportData resultListingData = memberDaysReport.runReport(listingDetails, submitEvents);
        int actualValidHearingDatesCount  = resultListingData.getReportDetails().size();
        String actualReportDateType = resultListingData.getHearingDateType();
        assertEquals(expectedValidHearingDatesCount, actualValidHearingDatesCount);
        assertEquals(expectedReportDateType, actualReportDateType);
    }

    @Test
    void shouldReturnZeroHearingDurationForNullHearingTimingStart() {
        submitEvents.remove(2);
        submitEvents.remove(1);
        CaseData caseData = submitEvents.get(0).getCaseData();
        // submitEvent3 has 4 hearingDates with 3 "Heard" status and one "Listed". Hence, the returned result
        // has to have three hearingDate entries.
        // and setting the first hearingDate HearingTimingStart time to null
        // should exclude it from the returned result
        HearingType caseData3FirstHearingType = caseData.getHearingCollection().get(0).getValue();
        DateListedTypeItem firstHearingDate = caseData3FirstHearingType.getHearingDateCollection().get(0);
        firstHearingDate.getValue().setHearingTimingStart(null);
        int expectedHearingDatesCount = 3;
        int expectedHearingDateEntry1Duration = 0;
        MemberDaysReportData resultListingData = memberDaysReport.runReport(listingDetails, submitEvents);
        long actualHearingDatesCount  = resultListingData.getReportDetails().stream().distinct().count();
        // As the result valid hearing dates get chronologically sorted in ascending order, the 1st element should
        // be the entry with null HearingTimingStart and, hence, zero hearing duration
        String actualHearingDateEntry1Duration = resultListingData.getReportDetails().get(1).getHearingDuration();
        assertEquals(expectedHearingDatesCount, actualHearingDatesCount);
        assertEquals(String.valueOf(expectedHearingDateEntry1Duration), actualHearingDateEntry1Duration);
    }

    @Test
    void shouldReturnZeroHearingDurationForNullHearingTimingFinish() {
        submitEvents.remove(2);
        submitEvents.remove(1);
        CaseData caseData = submitEvents.get(0).getCaseData();
        HearingType caseData3FirstHearingType = caseData.getHearingCollection().get(0).getValue();
        DateListedTypeItem firstHearingDate = caseData3FirstHearingType.getHearingDateCollection().get(0);
        firstHearingDate.getValue().setHearingTimingFinish(null);
        long expectedHearingDatesCount = 3;
        int expectedHearingDateEntry1Duration = 0;
        MemberDaysReportData resultListingData = memberDaysReport.runReport(listingDetails, submitEvents);
        long actualHearingDatesCount  = resultListingData.getReportDetails().stream().distinct().count();
        String actualHearingDateEntry1Duration = resultListingData.getReportDetails().get(1).getHearingDuration();
        assertEquals(expectedHearingDatesCount, actualHearingDatesCount);
        assertEquals(String.valueOf(expectedHearingDateEntry1Duration), actualHearingDateEntry1Duration);
    }

    @Test
    void shouldReturnCorrectHearingDurationDaysCount() {
        MemberDaysReportData resultListingData = memberDaysReport.runReport(listingDetails, submitEvents);
        assertEquals("8", resultListingData.getHalfDaysTotal());
        assertEquals("2", resultListingData.getFullDaysTotal());
        assertEquals("6.0", resultListingData.getTotalDays());
        int uniqueDaysCount = resultListingData.getMemberDaySummaryItems().size();
        assertEquals("4", String.valueOf(uniqueDaysCount));
        int totalDetailEntriesCount = resultListingData.getReportDetails().size();
        assertEquals("5", String.valueOf(totalDetailEntriesCount));
    }

    @Test
    void shouldReturnSortedSummaryItemsList() {
        MemberDaysReportData resultListingData = memberDaysReport.runReport(listingDetails, submitEvents);
        List<MemberDaySummaryItem> memberDaySummaryItems = resultListingData.getMemberDaySummaryItems();
        assertEquals("10 December 2019", memberDaySummaryItems.get(0).getHearingDate());
        assertEquals("11 December 2019", memberDaySummaryItems.get(1).getHearingDate());
        assertEquals("13 December 2019", memberDaySummaryItems.get(2).getHearingDate());
        assertEquals("14 December 2019", memberDaySummaryItems.get(3).getHearingDate());
    }

    @Test
    void shouldReturnSortedDetailedItemsList() {
        MemberDaysReportData resultListingData = memberDaysReport.runReport(listingDetails, submitEvents);
        List<MemberDaysReportDetail> reportDetails = resultListingData.getReportDetails();
        assertEquals("10 December 2019", reportDetails.get(0).getHearingDate());
        assertEquals("1800522/2020", reportDetails.get(0).getCaseReference());
        assertEquals("33", reportDetails.get(0).getHearingNumber());
        assertEquals("120", reportDetails.get(0).getHearingDuration());
        assertEquals("14 December 2019", reportDetails.get(4).getHearingDate());
        assertEquals("1800522/2020", reportDetails.get(4).getCaseReference());
        assertEquals("56", reportDetails.get(4).getHearingNumber());
        assertEquals("360", reportDetails.get(4).getHearingDuration());
    }

    @Test
    void shouldReturnOnlySelectedSingleDateDetailedItemsList() {
        listingDetails.getCaseData().setListingDate("2019-12-11");
        listingDetails.getCaseData().setListingDateFrom(null);
        listingDetails.getCaseData().setListingDateTo(null);
        listingDetails.getCaseData().setListingVenue(new DynamicFixedListType("Leeds"));
        listingDetails.getCaseData().setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        MemberDaysReportData resultListingData = memberDaysReport.runReport(listingDetails, submitEvents);
        List<MemberDaysReportDetail> reportDetails = resultListingData.getReportDetails();
        assertEquals("11 December 2019", reportDetails.get(0).getHearingDate());
        assertEquals("1800522/2020", reportDetails.get(0).getCaseReference());
        assertEquals("33", reportDetails.get(0).getHearingNumber());
        assertEquals("120", reportDetails.get(0).getHearingDuration());
    }

    @Test
    void checkReportingOffice_EnglandWales() {
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        MemberDaysReportData resultListingData = memberDaysReport.runReport(listingDetails, submitEvents);
        assertEquals(TribunalOffice.LEEDS.getOfficeName(), resultListingData.getOffice());
    }

    @Test
    void checkReportingOffice_Scotland() {
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        listingDetails.setCaseTypeId(SCOTLAND_LISTING_CASE_TYPE_ID);
        MemberDaysReportData resultListingData = memberDaysReport.runReport(listingDetails, submitEvents);
        assertEquals(TribunalOffice.SCOTLAND.getOfficeName(), resultListingData.getOffice());
    }

    private List<DateListedTypeItem> extractDateListedTypeItems(List<SubmitEvent> submitEvents) {
        List<DateListedTypeItem> dateListedTypeItems = new ArrayList<>();
        for (SubmitEvent submitEvent : submitEvents) {
            for (HearingTypeItem hearing : submitEvent.getCaseData().getHearingCollection()) {
                List<DateListedTypeItem> validDates = hearing.getValue().getHearingDateCollection()
                    .stream().filter(h -> isValidDate(h.getValue().getListedDate(),
                        listingDetails.getCaseData().getListingDateFrom(),
                        listingDetails.getCaseData().getListingDateTo()))
                    .collect(Collectors.toList());
                dateListedTypeItems.addAll(validDates);
            }
        }
        return dateListedTypeItems;
    }

    private boolean isValidDate(String dateListed, String dateFrom, String dateTo) {
        if (dateListed == null) {
            return false;
        }
        String datePart;
        if (dateListed.contains("T")) {
            datePart = dateListed.split("T")[0];
        } else if (dateListed.contains(" ")) {
            datePart = dateListed.split(" ")[0];
        } else {
            return false;
        }

        LocalDate hearingListedDate = LocalDate.parse(datePart);
        LocalDate hearingDatesFrom = LocalDate.parse(dateFrom);
        LocalDate hearingDatesTo = LocalDate.parse(dateTo);
        return (hearingListedDate.isEqual(hearingDatesFrom) ||  hearingListedDate.isAfter(hearingDatesFrom))
            && (hearingListedDate.isEqual(hearingDatesTo) || hearingListedDate.isBefore(hearingDatesTo));
    }
}