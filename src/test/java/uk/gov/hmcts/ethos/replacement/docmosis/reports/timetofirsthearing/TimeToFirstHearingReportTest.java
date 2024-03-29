package uk.gov.hmcts.ethos.replacement.docmosis.reports.timetofirsthearing;

import org.assertj.core.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.et.common.model.listing.types.AdhocReportType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_FAST_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_NO_CONCILIATION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_OPEN_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_STANDARD_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_REMEDY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

class TimeToFirstHearingReportTest {

    private ListingDetails listingDetails;
    private List<SubmitEvent> submitEvents;
    private TimeToFirstHearingReport timeToFirstHearingReport;

    @BeforeEach
    void setup() {
        listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData caseData = new ListingData();
        caseData.setManagingOffice(TribunalOffice.NEWCASTLE.getOfficeName());
        listingDetails.setCaseData(caseData);

        submitEvents = new ArrayList<>();

        timeToFirstHearingReport = new TimeToFirstHearingReport();
    }

    @Test
    void testReportHeaderTotalsAreZeroIfNoCasesExist() {
        ListingData listingData = timeToFirstHearingReport.generateReportData(listingDetails, submitEvents);
        verifyReportHeaderIsZero(listingData);
    }

    private void verifyReportHeaderIsZero(ListingData listingData) {
        AdhocReportType adhocReportType = listingData.getLocalReportsDetailHdr();
        assertEquals(0, Strings.isNullOrEmpty(
                adhocReportType.getTotal()) ? 0 : Integer.parseInt(
                        adhocReportType.getTotal()));
        assertEquals(0, Strings.isNullOrEmpty(
                adhocReportType.getTotal26wk()) ? 0 : Integer.parseInt(
                        adhocReportType.getTotal26wk()));
        assertEquals(0, Strings.isNullOrEmpty(
                adhocReportType.getTotalx26wk()) ? 0 : Integer.parseInt(
                        adhocReportType.getTotalx26wk()));
        assertEquals(0.00, Strings.isNullOrEmpty(
                adhocReportType.getTotal26wkPerCent()) ? 0.00 : Float.parseFloat(
                        adhocReportType.getTotal26wkPerCent()), .00);
        assertEquals(0.00, Strings.isNullOrEmpty(
                adhocReportType.getTotalx26wkPerCent()) ? 0.00 : Float.parseFloat(
                        adhocReportType.getTotalx26wkPerCent()), .00);
    }

    @Test
    void testIgnoreCaseIfItContainsNoHearings() {
        submitEvents.add(createSubmitEvent(Collections.emptyList(), "1970-01-01", CONCILIATION_TRACK_FAST_TRACK));

        ListingData listingData = timeToFirstHearingReport.generateReportData(listingDetails, submitEvents);

        verifyReportHeaderIsZero(listingData);
    }

    @Test
    void testIgnoreCaseIfHearingTypeInvalid() {
        DateListedTypeItem dateListedTypeItem = createHearingDateListed("2020-01-01T00:00:00");
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_JUDICIAL_REMEDY,
                dateListedTypeItem));
        submitEvents.add(createSubmitEvent(hearings, "2021-01-01T00:00:00", CONCILIATION_TRACK_FAST_TRACK));

        ListingData reportListingData = timeToFirstHearingReport.generateReportData(listingDetails, submitEvents);

        verifyReportHeaderIsZero(reportListingData);
    }

    @Test
    void testConsiderCaseIfHearingTypeValid() {
        DateListedTypeItem dateListedTypeItem = createHearingDateListed("1970-06-01T00:00:00.000");
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_JUDICIAL_HEARING,
                dateListedTypeItem));
        submitEvents.add(createSubmitEvent(hearings, "1970-04-01", CONCILIATION_TRACK_FAST_TRACK));

        ListingData reportListingData = timeToFirstHearingReport.generateReportData(listingDetails, submitEvents);

        AdhocReportType adhocReportType = reportListingData.getLocalReportsDetailHdr();
        assertEquals(1, Strings.isNullOrEmpty(
                adhocReportType.getTotalCases()) ? 0 : Integer.parseInt(
                        adhocReportType.getTotalCases()));
        assertEquals(1, Strings.isNullOrEmpty(
                adhocReportType.getTotal26wk()) ? 0 : Integer.parseInt(
                        adhocReportType.getTotal26wk()));
        assertEquals(0, Strings.isNullOrEmpty(
                adhocReportType.getTotalx26wk()) ? 0 : Integer.parseInt(
                        adhocReportType.getTotalx26wk()));
        assertEquals(100, Strings.isNullOrEmpty(
                adhocReportType.getTotal26wkPerCent()) ? 0 : Float.parseFloat(
                        adhocReportType.getTotal26wkPerCent()), .00);
        assertEquals(0, Strings.isNullOrEmpty(
                adhocReportType.getTotalx26wkPerCent()) ? 0 : Float.parseFloat(
                        adhocReportType.getTotalx26wkPerCent()), .00);
    }

    @Test
    void testFirstHearingNotWithin26Weeks() {
        DateListedTypeItem dateListedTypeItem = createHearingDateListed("2021-01-01T00:00:00.000");
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_JUDICIAL_HEARING,
                dateListedTypeItem));
        submitEvents.add(createSubmitEvent(hearings, "2020-04-01", CONCILIATION_TRACK_FAST_TRACK));

        ListingData reportListingData = timeToFirstHearingReport.generateReportData(listingDetails, submitEvents);

        AdhocReportType adhocReportType = reportListingData.getLocalReportsDetailHdr();
        assertEquals(1, Integer.parseInt(adhocReportType.getTotalCases()));
        assertEquals(0, Integer.parseInt(adhocReportType.getTotal26wk()));
        assertEquals(1, Integer.parseInt(adhocReportType.getTotalx26wk()));
        assertEquals(0, Float.parseFloat(adhocReportType.getTotal26wkPerCent()), .00);
        assertEquals(100, Float.parseFloat(adhocReportType.getTotalx26wkPerCent()), .00);
    }

    @Test
    void checkReportOffice_EngWales() {
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        ListingData reportData = timeToFirstHearingReport.generateReportData(listingDetails, submitEvents);
        assertEquals(reportData.getLocalReportsDetailHdr().getReportOffice(), TribunalOffice.LEEDS.getOfficeName());
    }

    @Test
    void testLocalReportsDetailHdrPercentages() {
        DateListedTypeItem dateListedTypeItem = createHearingDateListed("1970-06-01T00:00:00.000");
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_JUDICIAL_HEARING,
                dateListedTypeItem));

        submitEvents.add(createSubmitEvent(hearings, "1969-06-01", CONCILIATION_TRACK_FAST_TRACK));
        submitEvents.add(createSubmitEvent(hearings, "1955-01-01", CONCILIATION_TRACK_FAST_TRACK));
        submitEvents.add(createSubmitEvent(hearings, "2020-05-31", CONCILIATION_TRACK_FAST_TRACK));
        submitEvents.add(createSubmitEvent(hearings, "1931-09-22", CONCILIATION_TRACK_FAST_TRACK));

        submitEvents.add(createSubmitEvent(hearings, "1960-04-01", CONCILIATION_TRACK_OPEN_TRACK));
        submitEvents.add(createSubmitEvent(hearings, "1970-06-01", CONCILIATION_TRACK_OPEN_TRACK));
        submitEvents.add(createSubmitEvent(hearings, "1970-05-31", CONCILIATION_TRACK_OPEN_TRACK));
        submitEvents.add(createSubmitEvent(hearings, "2009-08-11", CONCILIATION_TRACK_OPEN_TRACK));
        submitEvents.add(createSubmitEvent(hearings, "1921-01-31", CONCILIATION_TRACK_OPEN_TRACK));

        submitEvents.add(createSubmitEvent(hearings, "1919-04-01", CONCILIATION_TRACK_STANDARD_TRACK));
        submitEvents.add(createSubmitEvent(hearings, "1970-06-01", CONCILIATION_TRACK_STANDARD_TRACK));
        submitEvents.add(createSubmitEvent(hearings, "1987-03-03", CONCILIATION_TRACK_STANDARD_TRACK));
        submitEvents.add(createSubmitEvent(hearings, "2000-07-12", CONCILIATION_TRACK_STANDARD_TRACK));
        submitEvents.add(createSubmitEvent(hearings, "2022-05-01", CONCILIATION_TRACK_STANDARD_TRACK));

        submitEvents.add(createSubmitEvent(hearings, "1960-04-01", CONCILIATION_TRACK_NO_CONCILIATION));
        submitEvents.add(createSubmitEvent(hearings, "1999-04-01", CONCILIATION_TRACK_NO_CONCILIATION));
        submitEvents.add(createSubmitEvent(hearings, "1970-05-31", CONCILIATION_TRACK_NO_CONCILIATION));

        ListingData reportListingData = timeToFirstHearingReport.generateReportData(listingDetails, submitEvents);

        AdhocReportType adhocReportType = reportListingData.getLocalReportsDetailHdr();
        assertEquals("66.67", adhocReportType.getConNone26wkTotalPerCent());
        assertEquals("80.00", adhocReportType.getConStd26wkTotalPerCent());
        assertEquals("25.00", adhocReportType.getConFast26wkTotalPerCent());
        assertEquals("60.00", adhocReportType.getConOpen26wkTotalPerCent());
        assertEquals("33.33", adhocReportType.getNotConNone26wkTotalPerCent());
        assertEquals("20.00", adhocReportType.getNotConStd26wkTotalPerCent());
        assertEquals("75.00", adhocReportType.getNotConFast26wkTotalPerCent());
        assertEquals("40.00", adhocReportType.getNotConOpen26wkTotalPerCent());
    }

    @Test
    void checkReportOffice_Scotland() {
        listingDetails.setCaseTypeId(SCOTLAND_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        ListingData reportData = timeToFirstHearingReport.generateReportData(listingDetails, submitEvents);
        assertEquals(reportData.getLocalReportsDetailHdr().getReportOffice(), TribunalOffice.SCOTLAND.getOfficeName());
    }

    private SubmitEvent createSubmitEvent(List<HearingTypeItem> hearingCollection, String receiptDate,
                                          String conciliationTrack) {
        CaseData caseData = new CaseData();
        caseData.setConciliationTrack(conciliationTrack);
        caseData.setReceiptDate(receiptDate);
        caseData.setHearingCollection(hearingCollection);
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);
        return submitEvent;
    }

    private DateListedTypeItem createHearingDateListed(String listedDate) {
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate(listedDate);
        dateListedType.setHearingStatus(HEARING_STATUS_HEARD);
        dateListedType.setHearingCaseDisposed(YES);
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setValue(dateListedType);

        return dateListedTypeItem;
    }

    private HearingTypeItem createHearing(String type, DateListedTypeItem... dateListedTypeItems) {
        HearingType hearingType = new HearingType();
        hearingType.setHearingType(type);
        List<DateListedTypeItem> hearingDateCollection = new ArrayList<>();
        Collections.addAll(hearingDateCollection, dateListedTypeItems);
        hearingType.setHearingDateCollection(hearingDateCollection);
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setValue(hearingType);
        return hearingTypeItem;
    }

    private List<HearingTypeItem> createHearingCollection(HearingTypeItem... hearings) {
        List<HearingTypeItem> hearingTypeItems = new ArrayList<>();
        Collections.addAll(hearingTypeItems, hearings);
        return hearingTypeItems;
    }
}
