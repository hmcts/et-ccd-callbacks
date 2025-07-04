package uk.gov.hmcts.ethos.replacement.docmosis.reports.casescompleted;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.et.common.model.listing.types.AdhocReportType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_FAST_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_NO_CONCILIATION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_OPEN_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_STANDARD_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_WITHDRAWN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_REMEDY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_PERLIMINARY_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.JURISDICTION_OUTCOME_DISMISSED_AT_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.POSITION_TYPE_CASE_INPUT_IN_ERROR;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.POSITION_TYPE_CASE_TRANSFERRED_OTHER_COUNTRY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.POSITION_TYPE_CASE_TRANSFERRED_SAME_COUNTRY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.TWO_JUDGES;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.casescompleted.CasesCompletedReport.COMPLETED_PER_SESSION_FORMAT;

@ExtendWith(SpringExtension.class)
class CaseCompletedReportTest {

    @Test
    void testReportHeaderTotalsAreZeroIfNoCasesExist() {
        // given no cases exist
        // when we generate report data
        // then totals are all zero

        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData caseData = new ListingData();
        caseData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingDetails.setCaseData(caseData);
        List<SubmitEvent> submitEvents = new ArrayList<>();

        CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
        ListingData listingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

        verifyReportHeaderIsZero(listingData);
    }

    @Test
    void testIgnoreCaseIfNotClosed() {
        // given case is not closed
        // when we generate report data
        // then no data returned

        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData caseData = new ListingData();
        caseData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingDetails.setCaseData(caseData);
        List<SubmitEvent> submitEvents = new ArrayList<>();
        submitEvents.add(createSubmitEvent(SUBMITTED_STATE));

        CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
        ListingData listingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

        verifyReportHeaderIsZero(listingData);
    }

    @Test
    void testIgnoreCaseIfPositionTypeInvalid() {
        // given case is closed
        // given position type is invalid
        // when we generate report data
        // then no data returned
        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData listingData = new ListingData();
        listingData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingDetails.setCaseData(listingData);
        List<SubmitEvent> submitEvents = new ArrayList<>();
        submitEvents.add(createSubmitEvent(CLOSED_STATE));

        List<String> invalidPositionTypes = Arrays.asList(POSITION_TYPE_CASE_INPUT_IN_ERROR,
                POSITION_TYPE_CASE_TRANSFERRED_SAME_COUNTRY,
                POSITION_TYPE_CASE_TRANSFERRED_OTHER_COUNTRY);

        CaseData caseData = submitEvents.get(0).getCaseData();
        for (String positionType : invalidPositionTypes) {
            caseData.setPositionType(positionType);
            CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
            ListingData reportListingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

            verifyReportHeaderIsZero(reportListingData);
        }
    }

    @Test
    void testIgnoreCaseIfJurisdictionOutcomeInvalid() {
        // given case is closed
        // given position type is valid
        // given jurisdiction outcome is invalid
        // when we generate report data
        // then no data returned
        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData listingData = new ListingData();
        listingData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingDetails.setCaseData(listingData);
        List<SubmitEvent> submitEvents = new ArrayList<>();
        submitEvents.add(createSubmitEvent(CLOSED_STATE));

        List<String> invalidOutcomes = Arrays.asList("This is not a valid outcome", null);

        CaseData caseData = submitEvents.get(0).getCaseData();
        caseData.setJurCodesCollection(new ArrayList<>());
        for (String outcome : invalidOutcomes) {
            caseData.getJurCodesCollection().add(createJurisdiction(outcome));
            CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
            ListingData reportListingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

            verifyReportHeaderIsZero(reportListingData);
            caseData.getJurCodesCollection().clear();
        }
    }

    @Test
    void testIgnoreCaseIfItContainsNoHearings() {
        // given case is closed
        // given case position type is valid
        // given case jurisdiction outcome is valid
        // given case has no hearings
        // when we generate report data
        // then no data returned

        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData caseData = new ListingData();
        caseData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingDetails.setCaseData(caseData);
        List<SubmitEvent> submitEvents = new ArrayList<>();
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING,
                Collections.emptyList()));

        CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
        ListingData listingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

        verifyReportHeaderIsZero(listingData);
    }

    @Test
    void testIgnoreCaseIfHearingTypeInvalid() {
        // given case is closed
        // given case position type is valid
        // given case jurisdiction outcome is valid
        // given case has a hearing with a type that is invalid
        // when we generate report data
        // then no data returned

        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData listingData = new ListingData();
        listingData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingDetails.setCaseData(listingData);

        List<SubmitEvent> submitEvents = new ArrayList<>();
        DateListedTypeItem dateListedTypeItem = createHearingDateListed("1970-01-01T00:00:00",
                HEARING_STATUS_HEARD, YES);
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_JUDICIAL_REMEDY,
                dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings));

        CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
        ListingData reportListingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

        verifyReportHeaderIsZero(reportListingData);
    }

    @Test
    void testIgnoreCaseIfHearingListingDateNotInSearchRange() {
        // given case is closed
        // given case position type is valid
        // given case jurisdiction outcome is valid
        // given case has a hearing that was disposed
        // given case has a hearing listing date that is different to report search date
        // when we generate report data
        // then no data returned

        String searchDate = "1970-01-01";
        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData listingData = new ListingData();
        listingData.setListingDate(searchDate);
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingDetails.setCaseData(listingData);
        String listingDate = "1970-01-02T00:00:00";
        List<SubmitEvent> submitEvents = new ArrayList<>();
        DateListedTypeItem dateListedTypeItem = createHearingDateListed(listingDate, HEARING_STATUS_HEARD, YES);
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING,
                dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings));

        CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
        ListingData reportListingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

        verifyReportHeaderIsZero(reportListingData);
    }

    @Test
    void testIgnoreCaseIfHearingNotDisposed() {
        // given case is closed
        // given case position type is valid
        // given case jurisdiction outcome is valid
        // given case has a hearing with a valid type
        // given case has a hearing that was not disposed
        // when we generate report data
        // then no data returned

        String searchDate = "1970-01-01";
        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData listingData = new ListingData();
        listingData.setListingDate(searchDate);
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingDetails.setCaseData(listingData);
        String listingDate = "1970-01-01T00:00:00";
        List<SubmitEvent> submitEvents = new ArrayList<>();
        DateListedTypeItem dateListedTypeItem = createHearingDateListed(listingDate, HEARING_STATUS_HEARD, NO);
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING,
                dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings));

        CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
        ListingData reportListingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

        verifyReportHeaderIsZero(reportListingData);
    }

    @Test
    void testValidNullConciliationTrackCaseIsAddedToReport() {
        // given case is closed
        // given case position type is valid
        // given case jurisdiction outcome is valid
        // given case has a hearing with a valid type
        // given case has a hearing that was disposed
        // given case has a hearing listed date that is within report search range
        // given case has a null conciliation track i.e. it is not set
        // when we generate report data
        // then we have some data

        String searchDate = "1970-01-01";
        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData listingData = new ListingData();
        listingData.setListingDate(searchDate);
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingDetails.setCaseData(listingData);
        String listingDate = "1970-01-01T00:00:00";
        List<SubmitEvent> submitEvents = new ArrayList<>();
        DateListedTypeItem dateListedTypeItem = createHearingDateListed(listingDate, HEARING_STATUS_HEARD, YES);
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING,
                dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings, null));

        CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
        ListingData reportListingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

        ReportHeaderValues reportHeaderValues = new ReportHeaderValues(
                1, 1, 1.0, "Leeds",
                1, 1, 1.0,
                0, 0, 0,
                0, 0, 0,
                0, 0, 0);
        verifyReportHeader(reportListingData, reportHeaderValues);
        verifyReportDetails(reportListingData, 1);
    }

    @Test
    void testValidNoneConciliationTrackCaseIsAddedToReport() {
        // given case is closed
        // given case position type is valid
        // given case jurisdiction outcome is valid
        // given case has a hearing with a valid type
        // given case has a hearing that was disposed
        // given case has a hearing listed date that is within report search range
        // given case is for none conciliation track
        // when we generate report data
        // then we have some data

        String searchDate = "1970-01-01";
        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData listingData = new ListingData();
        listingData.setListingDate(searchDate);
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingDetails.setCaseData(listingData);
        String listingDate = "1970-01-01T00:00:00";
        List<SubmitEvent> submitEvents = new ArrayList<>();
        DateListedTypeItem dateListedTypeItem = createHearingDateListed(listingDate, HEARING_STATUS_HEARD, YES);
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING,
                dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_NO_CONCILIATION));

        CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
        ListingData reportListingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

        ReportHeaderValues reportHeaderValues = new ReportHeaderValues(
                1, 1, 1.0, "Leeds",
                1, 1, 1.0,
                0, 0, 0,
                0, 0, 0,
                0, 0, 0);
        verifyReportHeader(reportListingData, reportHeaderValues);
        verifyReportDetails(reportListingData, 1);
    }

    @Test
    void testValidFastConciliationTrackCaseIsAddedToReport() {
        // given case is closed
        // given case position type is valid
        // given case jurisdiction outcome is valid
        // given case has a hearing with a valid type
        // given case has a hearing that was disposed
        // given case has a hearing listed date that is within report search range
        // given case is for fast conciliation track
        // when we generate report data
        // then we have some data

        String searchDate = "1970-01-01";
        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData listingData = new ListingData();
        listingData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingData.setListingDate(searchDate);
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingDetails.setCaseData(listingData);
        String listingDate = "1970-01-01T00:00:00";
        List<SubmitEvent> submitEvents = new ArrayList<>();
        DateListedTypeItem dateListedTypeItem = createHearingDateListed(listingDate, HEARING_STATUS_HEARD, YES);
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING,
                dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_FAST_TRACK));

        CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
        ListingData reportListingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

        ReportHeaderValues reportHeaderValues = new ReportHeaderValues(
                1, 1, 1.0, "Leeds",
                0, 0, 0,
                1, 1, 1.0,
                0, 0, 0,
                0, 0, 0);
        verifyReportHeader(reportListingData, reportHeaderValues);
        verifyReportDetails(reportListingData, 1);
    }

    @Test
    void testValidStdConciliationTrackCaseIsAddedToReport() {
        // given case is closed
        // given case position type is valid
        // given case jurisdiction outcome is valid
        // given case has a hearing with a valid type
        // given case has a hearing that was disposed
        // given case has a hearing listed date that is within report search range
        // given case is for standard conciliation track
        // when we generate report data
        // then we have some data

        String searchDate = "1970-01-01";
        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData listingData = new ListingData();
        listingData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingData.setListingDate(searchDate);
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingDetails.setCaseData(listingData);
        String listingDate = "1970-01-01T00:00:00";
        List<SubmitEvent> submitEvents = new ArrayList<>();
        DateListedTypeItem dateListedTypeItem = createHearingDateListed(listingDate, HEARING_STATUS_HEARD, YES);
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING,
                dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_STANDARD_TRACK));

        CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
        ListingData reportListingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

        ReportHeaderValues reportHeaderValues = new ReportHeaderValues(
                1, 1, 1.0, "Leeds",
                0, 0, 0,
                0, 0, 0,
                1, 1, 1.0,
                0, 0, 0);
        verifyReportHeader(reportListingData, reportHeaderValues);
        verifyReportDetails(reportListingData, 1);
    }

    @Test
    void testValidOpenConciliationTrackCaseIsAddedToReport() {
        // given case is closed
        // given case position type is valid
        // given case jurisdiction outcome is valid
        // given case has a hearing with a valid type
        // given case has a hearing that was disposed
        // given case has a hearing listed date that is within report search range
        // given case is for open conciliation track
        // when we generate report data
        // then we have some data

        String searchDate = "1970-01-01";
        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData listingData = new ListingData();
        listingData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingData.setListingDate(searchDate);
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingDetails.setCaseData(listingData);
        String listingDate = "1970-01-01T00:00:00";
        List<SubmitEvent> submitEvents = new ArrayList<>();
        DateListedTypeItem dateListedTypeItem = createHearingDateListed(listingDate, HEARING_STATUS_HEARD, YES);
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING,
                dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_OPEN_TRACK));

        CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
        ListingData reportListingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

        ReportHeaderValues reportHeaderValues = new ReportHeaderValues(
                1, 1, 1.0, "Leeds",
                0, 0, 0,
                0, 0, 0,
                0, 0, 0,
                1, 1, 1.0);
        verifyReportHeader(reportListingData, reportHeaderValues);
        verifyReportDetails(reportListingData, 1);
    }

    @Test
    void testMultipleCasesAreSummedUpInTotals() {
        // given we have multiple cases that are valid
        // when we generate report data
        // then we have data for all cases

        String searchDate = "1970-01-01";
        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData listingData = new ListingData();
        listingData.setListingDate(searchDate);
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingDetails.setCaseData(listingData);
        String listingDate = "1970-01-01T00:00:00";
        List<SubmitEvent> submitEvents = new ArrayList<>();
        DateListedTypeItem dateListedTypeItem = createHearingDateListed(listingDate, HEARING_STATUS_HEARD, YES);
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING,
                dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_NO_CONCILIATION));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_FAST_TRACK));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_STANDARD_TRACK));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_OPEN_TRACK));

        CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
        ListingData reportListingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

        ReportHeaderValues reportHeaderValues = new ReportHeaderValues(
                4, 4, 1.0, "Leeds",
                1, 1, 1.0,
                1, 1, 1.0,
                1, 1, 1.0,
                1, 1, 1.0);
        verifyReportHeader(reportListingData, reportHeaderValues);
        verifyReportDetails(reportListingData, 4);
    }

    @Test
    void testMultipleCasesOnlyValidAreSummedUpInTotals() {
        // given we have two cases that are valid
        // given we have two cases that are not valid
        // when we generate report data
        // then we have data for only valid cases

        String searchDate = "1970-01-01";
        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData listingData = new ListingData();
        listingData.setListingDate(searchDate);
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingDetails.setCaseData(listingData);
        String listingDate = "1970-01-01T00:00:00";
        List<SubmitEvent> submitEvents = new ArrayList<>();
        DateListedTypeItem dateListedTypeItem = createHearingDateListed(listingDate, HEARING_STATUS_HEARD, YES);
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING,
                dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_NO_CONCILIATION));
        submitEvents.add(createSubmitEvent(SUBMITTED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_FAST_TRACK));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_STANDARD_TRACK));
        submitEvents.add(createSubmitEvent(SUBMITTED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_OPEN_TRACK));

        CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
        ListingData reportListingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

        ReportHeaderValues reportHeaderValues = new ReportHeaderValues(
                2, 2, 1.0, "Leeds",
                1, 1, 1.0,
                0, 0, 0,
                1, 1, 1.0,
                0, 0, 0);
        verifyReportHeader(reportListingData, reportHeaderValues);
        verifyReportDetails(reportListingData, 2);
    }

    @Test
    void testSessionDaysSingleCase() {
        // given the case is valid
        // given the case has a single hearing over multiple days
        // when we generate report data
        // then we have some data

        String searchDate = "1970-01-04";

        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData listingData = new ListingData();
        listingData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingData.setListingDate(searchDate);
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingDetails.setCaseData(listingData);

        List<SubmitEvent> submitEvents = new ArrayList<>();
        DateListedTypeItem[] dateListedTypeItem = {
                createHearingDateListed("1970-01-01T00:00:00", HEARING_STATUS_HEARD, NO),
                createHearingDateListed("1970-01-02T00:00:00", HEARING_STATUS_WITHDRAWN, NO), // should be ignored
                createHearingDateListed("1970-01-03T00:00:00", HEARING_STATUS_HEARD, NO),
                createHearingDateListed("1970-01-04T00:00:00", HEARING_STATUS_HEARD, YES),
                createHearingDateListed("1970-01-05T00:00:00", HEARING_STATUS_WITHDRAWN, YES)
        };
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING,
                dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_NO_CONCILIATION));

        CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
        ListingData reportListingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

        ReportHeaderValues reportHeaderValues = new ReportHeaderValues(
                1, 3, 0.33, "Leeds",
                1, 3, 0.33,
                0, 0, 0,
                0, 0, 0,
                0, 0, 0);
        verifyReportHeader(reportListingData, reportHeaderValues);
        verifyReportDetails(reportListingData, 1);
    }

    @Test
    void testSessionDaysMultipleCases() {
        // given there are multiple valid cases
        // when we generate report data
        // then we have some data

        String searchDate = "1970-01-04";

        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData listingData = new ListingData();
        listingData.setListingDate(searchDate);
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingDetails.setCaseData(listingData);

        List<SubmitEvent> submitEvents = new ArrayList<>();

        // Case 1: 4 session days no conciliation
        DateListedTypeItem[] dateListedTypeItem = {
                createHearingDateListed("1970-01-01T00:00:00", HEARING_STATUS_HEARD, NO),
                createHearingDateListed("1970-01-02T00:00:00", HEARING_STATUS_HEARD, NO),
                createHearingDateListed("1970-01-03T00:00:00", HEARING_STATUS_HEARD, NO),
                createHearingDateListed("1970-01-04T00:00:00", HEARING_STATUS_HEARD, YES),
                createHearingDateListed("1970-01-05T00:00:00", HEARING_STATUS_WITHDRAWN, YES)
        };
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING,
                dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_NO_CONCILIATION));

        // Case 2: 2 session days fast track
        dateListedTypeItem = new DateListedTypeItem[] {
                createHearingDateListed("1970-01-03T00:00:00", HEARING_STATUS_HEARD, NO),
                createHearingDateListed("1970-01-04T00:00:00", HEARING_STATUS_HEARD, YES)
        };
        hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING, dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_FAST_TRACK));

        // Case 3: 1 session day standard conciliation
        dateListedTypeItem = new DateListedTypeItem[] {
                createHearingDateListed("1970-01-04T00:00:00", HEARING_STATUS_HEARD, YES)
        };
        hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING, dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_STANDARD_TRACK));

        // Case 4: 2 session day open conciliation
        dateListedTypeItem = new DateListedTypeItem[] {
                createHearingDateListed("1970-01-03T00:00:00", HEARING_STATUS_HEARD, YES),
                createHearingDateListed("1970-01-04T00:00:00", HEARING_STATUS_HEARD, YES)
        };
        hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING, dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_OPEN_TRACK));

        CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
        ListingData reportListingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

        ReportHeaderValues reportHeaderValues = new ReportHeaderValues(
                4, 9, 0.44, "Leeds",
                1, 4, 0.25,
                1, 2, 0.5,
                1, 1, 1.0,
                1, 2, 0.5);
        verifyReportHeader(reportListingData, reportHeaderValues);
        verifyReportDetails(reportListingData, 4);
    }

    @Test
    void testSessionDaysMultipleTracks() {
        // given there are multiple valid cases for different conciliation tracks
        // when we generate report data
        // then we have some data

        String searchDate = "1970-01-04";

        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData listingData = new ListingData();
        listingData.setListingDate(searchDate);
        listingData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingDetails.setCaseData(listingData);

        List<SubmitEvent> submitEvents = new ArrayList<>();

        // Case 1: 4 session days
        DateListedTypeItem[] dateListedTypeItem = {
                createHearingDateListed("1970-01-01T00:00:00", HEARING_STATUS_HEARD, NO),
                createHearingDateListed("1970-01-02T00:00:00", HEARING_STATUS_HEARD, NO),
                createHearingDateListed("1970-01-03T00:00:00", HEARING_STATUS_HEARD, NO),
                createHearingDateListed("1970-01-04T00:00:00", HEARING_STATUS_HEARD, YES),
                createHearingDateListed("1970-01-05T00:00:00", HEARING_STATUS_WITHDRAWN, YES)
        };
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING,
                dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_NO_CONCILIATION));

        // Case 2: 2 session days
        dateListedTypeItem = new DateListedTypeItem[] {
                createHearingDateListed("1970-01-03T00:00:00", HEARING_STATUS_HEARD, NO),
                createHearingDateListed("1970-01-04T00:00:00", HEARING_STATUS_HEARD, YES)
        };
        hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING, dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_NO_CONCILIATION));

        // Case 3: 1 session days
        dateListedTypeItem = new DateListedTypeItem[] {
                createHearingDateListed("1970-01-04T00:00:00", HEARING_STATUS_HEARD, YES)
        };
        hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING, dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_NO_CONCILIATION));

        CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
        ListingData reportListingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

        ReportHeaderValues reportHeaderValues = new ReportHeaderValues(
                3, 7, 0.43, "Leeds",
                3, 7, 0.43,
                0, 0, 0,
                0, 0, 0,
                0, 0, 0);
        verifyReportHeader(reportListingData, reportHeaderValues);
        verifyReportDetails(reportListingData, 3);
    }

    @Test
    void initReport_ReportOfficeName_Scotland() {
        // given case office in Scotland
        // when we generate report data
        // then we have some data

        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(SCOTLAND_LISTING_CASE_TYPE_ID);
        ListingData listingData = new ListingData();
        listingData.setListingDate("1970-01-01");
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingData.setManagingOffice(null);
        listingDetails.setCaseData(listingData);

        List<SubmitEvent> submitEvents = new ArrayList<>();
        DateListedTypeItem dateListedTypeItem = createHearingDateListed("1970-01-01T00:00:00",
                HEARING_STATUS_HEARD, YES);
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING,
            dateListedTypeItem));
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_NO_CONCILIATION));

        CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
        ListingData reportListingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);

        ReportHeaderValues reportHeaderValues = new ReportHeaderValues(
                1, 1, 1.0, "Scotland",
                1, 1, 1.0,
                0, 0, 0,
                0, 0, 0,
                0, 0, 0);
        verifyReportHeader(reportListingData, reportHeaderValues);
        verifyReportDetails(reportListingData, 1);
    }

    private SubmitEvent createSubmitEvent(String state) {
        return createSubmitEvent(state, null, null);
    }

    private SubmitEvent createSubmitEvent(String state, String jurisdictionOutcome,
                                          List<HearingTypeItem> hearingCollection) {
        return createSubmitEvent(state, jurisdictionOutcome, hearingCollection, CONCILIATION_TRACK_NO_CONCILIATION);
    }

    private SubmitEvent createSubmitEvent(String state, String jurisdictionOutcome,
                                          List<HearingTypeItem> hearingCollection, String conciliationTrack) {
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setState(state);

        CaseData caseData = new CaseData();
        caseData.setConciliationTrack(conciliationTrack);
        if (jurisdictionOutcome != null) {
            caseData.setJurCodesCollection(new ArrayList<>());
            caseData.getJurCodesCollection().add(createJurisdiction(jurisdictionOutcome));
        }

        caseData.setHearingCollection(hearingCollection);

        submitEvent.setCaseData(caseData);

        return submitEvent;
    }

    private JurCodesTypeItem createJurisdiction(String outcome) {
        JurCodesTypeItem jurCodesTypeItem = new JurCodesTypeItem();
        JurCodesType jurCodesType = new JurCodesType();
        jurCodesType.setJudgmentOutcome(outcome);
        jurCodesTypeItem.setValue(jurCodesType);
        return jurCodesTypeItem;
    }

    private DateListedTypeItem createHearingDateListed(String listedDate, String status, String disposed) {
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate(listedDate);
        dateListedType.setHearingStatus(status);
        dateListedType.setHearingCaseDisposed(disposed);
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

    private void verifyReportHeaderIsZero(ListingData listingData) {
        ReportHeaderValues reportHeaderValues = new ReportHeaderValues(0, 0, 0, "Leeds",
                0, 0, 0,
                0, 0, 0,
                0, 0, 0,
                0, 0, 0);
        verifyReportHeader(listingData, reportHeaderValues);
        verifyReportDetails(listingData, 0);
    }

    private void verifyReportHeader(ListingData listingData, ReportHeaderValues reportHeaderValues) {
        AdhocReportType adhocReportType = listingData.getLocalReportsDetailHdr();

        // Report header
        assertEquals(String.valueOf(reportHeaderValues.casesCompletedHearingTotal),
                adhocReportType.getCasesCompletedHearingTotal());
        assertEquals(String.valueOf(reportHeaderValues.sessionDaysTotal), adhocReportType.getSessionDaysTotal());
        assertEquals(String.format(
                Locale.ROOT, COMPLETED_PER_SESSION_FORMAT, reportHeaderValues.completedPerSessionTotal),
                adhocReportType.getCompletedPerSessionTotal());
        assertEquals(reportHeaderValues.reportOffice, adhocReportType.getReportOffice());

        // Conciliation - No Conciliation
        assertEquals(String.valueOf(reportHeaderValues.conNoneCasesCompletedHearing),
                adhocReportType.getConNoneCasesCompletedHearing());
        assertEquals(String.valueOf(reportHeaderValues.conNoneSessionDays), adhocReportType.getConNoneSessionDays());
        assertEquals(String.format(
                Locale.ROOT, COMPLETED_PER_SESSION_FORMAT, reportHeaderValues.conNoneCompletedPerSession),
                adhocReportType.getConNoneCompletedPerSession());

        // Conciliation - Fast Track
        assertEquals(String.valueOf(reportHeaderValues.conFastCasesCompletedHearing),
                adhocReportType.getConFastCasesCompletedHearing());
        assertEquals(String.valueOf(
                reportHeaderValues.conFastSessionDays), adhocReportType.getConFastSessionDays());
        assertEquals(String.format(
                Locale.ROOT, COMPLETED_PER_SESSION_FORMAT, reportHeaderValues.conFastCompletedPerSession),
                adhocReportType.getConFastCompletedPerSession());

        // Conciliation - Standard Track
        assertEquals(String.valueOf(
                reportHeaderValues.conStdCasesCompletedHearing),
                adhocReportType.getConStdCasesCompletedHearing());
        assertEquals(String.valueOf(
                reportHeaderValues.conStdSessionDays), adhocReportType.getConStdSessionDays());
        assertEquals(String.format(
                Locale.ROOT, COMPLETED_PER_SESSION_FORMAT, reportHeaderValues.conStdCompletedPerSession),
                adhocReportType.getConStdCompletedPerSession());

        // Conciliation - Open Track
        assertEquals(String.valueOf(reportHeaderValues.conOpenCasesCompletedHearing),
                adhocReportType.getConOpenCasesCompletedHearing());
        assertEquals(String.valueOf(
                reportHeaderValues.conOpenSessionDays), adhocReportType.getConOpenSessionDays());
        assertEquals(String.format(
                Locale.ROOT, COMPLETED_PER_SESSION_FORMAT, reportHeaderValues.conOpenCompletedPerSession),
                adhocReportType.getConOpenCompletedPerSession());
    }

    private void verifyReportDetails(ListingData listingData, int size) {
        assertEquals(size, listingData.getLocalReportsDetail().size());
    }

    @Test
    void testAdditionalJudgeIsPresentOnReport() {
        String searchDate = "1970-01-01";
        ListingDetails listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData listingData = new ListingData();
        listingData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingData.setListingDate(searchDate);
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingDetails.setCaseData(listingData);
        String listingDate = "1970-01-01T00:00:00";
        DateListedTypeItem dateListedTypeItem = createHearingDateListed(listingDate, HEARING_STATUS_HEARD, YES);
        List<HearingTypeItem> hearings = createHearingCollection(createHearing(HEARING_TYPE_PERLIMINARY_HEARING,
                dateListedTypeItem));
        hearings.get(0).getValue().setHearingSitAlone(TWO_JUDGES);
        hearings.get(0).getValue().setJudge(new DynamicFixedListType("Judge 1"));
        hearings.get(0).getValue().setAdditionalJudge(new DynamicFixedListType("Judge 2"));
        List<SubmitEvent> submitEvents = new ArrayList<>();
        submitEvents.add(createSubmitEvent(CLOSED_STATE, JURISDICTION_OUTCOME_DISMISSED_AT_HEARING, hearings,
                CONCILIATION_TRACK_OPEN_TRACK));

        CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
        ListingData reportListingData = casesCompletedReport.generateReportData(listingDetails, submitEvents);
        verifyReportDetails(reportListingData, 1);
        assertEquals("Judge 1, Judge 2", reportListingData.getLocalReportsDetail().get(0).getValue().getHearingJudge());
    }
}
