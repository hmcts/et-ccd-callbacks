package uk.gov.hmcts.ethos.replacement.docmosis.reports.claimsbyhearingvenue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.reports.claimsbyhearingvenue.ClaimsByHearingVenueSubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantWorkAddressType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReportHelper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_HEARING_DATE_TYPE;

class ClaimsByHearingVenueReportTest {
    ClaimsByHearingVenueReportDataSource claimsByHearingVenueReportDataSource;
    ClaimsByHearingVenueReport claimsByHearingVenueReport;
    ClaimsByHearingVenueCaseDataBuilder caseDataBuilder = new ClaimsByHearingVenueCaseDataBuilder();
    List<ClaimsByHearingVenueSubmitEvent> submitEvents = new ArrayList<>();
    ClaimsByHearingVenueReportParams reportParams;
    static final String RANGE_START_DATE = "2021-12-02T01:00:00.000";
    static final String RANGE_END_DATE = "2021-12-28T23:59:59.000";
    static final String SINGLE_START_DATE = "2021-12-08T01:00:00.000";
    static final String SINGLE_END_DATE = "2021-12-08T23:59:59.000";
    static final String TEST_USERNAME = "ECM Tester";
    static final String OFFICE_NAME = TribunalOffice.LEEDS.getOfficeName();

    @BeforeEach
    public void setUp() {
        submitEvents.clear();
        caseDataBuilder = new ClaimsByHearingVenueCaseDataBuilder();
        claimsByHearingVenueReportDataSource = mock(ClaimsByHearingVenueReportDataSource.class);
        claimsByHearingVenueReport = new ClaimsByHearingVenueReport(claimsByHearingVenueReportDataSource);
        reportParams = new ClaimsByHearingVenueReportParams(ENGLANDWALES_LISTING_CASE_TYPE_ID, OFFICE_NAME,
                RANGE_START_DATE, RANGE_END_DATE, RANGE_HEARING_DATE_TYPE, TEST_USERNAME);
    }

    @Test
    void shouldShowCorrectNumberOfReportDetailEntriesForDateRangeSearch() {
        // Given cases have date of Receipt value within the inclusive date range searched for
        // When report data is requested
        // Then only all cases with valid date should be in the report data detail entries

        Address claimantAddressUK = new Address();
        claimantAddressUK.setPostCode("DH3 8HL");
        ClaimantType claimant = new ClaimantType();
        claimant.setClaimantAddressUK(claimantAddressUK);

        ClaimsByHearingVenueSubmitEvent submitEventOne = caseDataBuilder
                .withEthosCaseReference("18000012/2022")
                .withReceiptDate("2021-12-14")
                .withClaimantType(claimant)
                .withClaimantWorkAddressType(null)
                .withRespondentCollection(null)
                .buildAsSubmitEvent(ACCEPTED_STATE);
        submitEvents.add(submitEventOne);

        ClaimsByHearingVenueSubmitEvent submitEventTwo = caseDataBuilder
                .withEthosCaseReference("18000013/2022")
                .withReceiptDate("2021-12-08")
                .withClaimantType(claimant)
                .withClaimantWorkAddressType(null)
                .withRespondentCollection(null)
                .buildAsSubmitEvent(ACCEPTED_STATE);
        submitEvents.add(submitEventTwo);

        when(claimsByHearingVenueReportDataSource.getData(OFFICE_NAME,
                UtilHelper.getListingCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID), RANGE_START_DATE, RANGE_END_DATE))
                .thenReturn(submitEvents);

        String expectedReportTitle = getReportTitle(RANGE_HEARING_DATE_TYPE);
        int expectedNumberOfSubmitEventEntries = submitEvents.size();
        ClaimsByHearingVenueReportData reportData = claimsByHearingVenueReport
                .generateReport(reportParams);
        String actualReportTitle = reportData.getReportPeriodDescription();

        assertEquals(expectedReportTitle, actualReportTitle);
        assertEquals(expectedNumberOfSubmitEventEntries, reportData.getReportDetails().size());
    }

    @Test
    void shouldShowCorrectNumberOfReportDetailEntriesForSingleDateSearch() {
        // Given cases have date of Receipt value on the single date searched for
        // When report data is requested
        // Then only all cases with matching Receipt date should be in the report data detail entries

        Address claimantAddressUK = new Address();
        claimantAddressUK.setPostCode("DH3 8HL");
        ClaimantType claimant = new ClaimantType();
        claimant.setClaimantAddressUK(claimantAddressUK);

        ClaimsByHearingVenueSubmitEvent submitEventOne = caseDataBuilder
                .withEthosCaseReference("18000012/2022")
                .withReceiptDate("2021-12-08")
                .withClaimantType(claimant)
                .withClaimantWorkAddressType(null)
                .withRespondentCollection(null)
                .buildAsSubmitEvent(ACCEPTED_STATE);
        submitEvents.add(submitEventOne);

        when(claimsByHearingVenueReportDataSource.getData(OFFICE_NAME,
                UtilHelper.getListingCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID), SINGLE_START_DATE, SINGLE_END_DATE))
                .thenReturn(submitEvents);

        String expectedReportTitle = getReportTitle(SINGLE_HEARING_DATE_TYPE);
        int expectedNumberOfSubmitEventEntries = 1;
        ClaimsByHearingVenueReportParams singleHearingDateTypeReportParams =
                new ClaimsByHearingVenueReportParams(ENGLANDWALES_LISTING_CASE_TYPE_ID,
                OFFICE_NAME, SINGLE_START_DATE, SINGLE_END_DATE, SINGLE_HEARING_DATE_TYPE, TEST_USERNAME);
        ClaimsByHearingVenueReportData reportData = claimsByHearingVenueReport
                .generateReport(singleHearingDateTypeReportParams);
        String actualReportTitle = reportData.getReportPeriodDescription();

        assertEquals(expectedReportTitle, actualReportTitle);
        assertEquals(expectedNumberOfSubmitEventEntries, reportData.getReportDetails().size());
    }

    @Test
    void shouldShowNullStringValueForPostcodeWhenClaimantWorkAddressNotSet() {
        // Given a case has Claimant Work Address not set or is null
        // When report data is requested
        // Then on all cases with valid date, "Null" should be used for postcode in the report data detail entries
        Address claimantAddressUK = new Address();
        claimantAddressUK.setPostCode("DH3 8HL");
        ClaimantType claimant = new ClaimantType();
        claimant.setClaimantAddressUK(claimantAddressUK);

        ClaimsByHearingVenueSubmitEvent submitEventOne = caseDataBuilder
                .withEthosCaseReference("18000012/2022")
                .withReceiptDate("2021-12-14")
                .withClaimantType(claimant)
                .withClaimantWorkAddressType(null)
                .withRespondentCollection(null)
                .buildAsSubmitEvent(ACCEPTED_STATE);
        submitEvents.add(submitEventOne);
        when(claimsByHearingVenueReportDataSource.getData(OFFICE_NAME,
                UtilHelper.getListingCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID), RANGE_START_DATE, RANGE_END_DATE))
                .thenReturn(submitEvents);

        ClaimsByHearingVenueReportData reportData = claimsByHearingVenueReport
                .generateReport(reportParams);
        String actualClaimantAddressUKPostcode = reportData.getReportDetails().get(0).getClaimantPostcode();
        String actualClaimantWorkPostcode = reportData.getReportDetails().get(0).getClaimantWorkPostcode();
        String actualRespondentPostcode = reportData.getReportDetails().get(0).getRespondentPostcode();
        assertEquals("DH3 8HL", actualClaimantAddressUKPostcode);
        assertEquals("Null", actualClaimantWorkPostcode);
        assertEquals("Null", actualRespondentPostcode);
        String actualRespondentET3Postcode = reportData.getReportDetails().get(0).getRespondentET3Postcode();
        assertEquals("Null", actualRespondentET3Postcode);
    }

    @Test
    void shouldShowNullStringValueForMissingPostCodeInClaimantWorkAddressProvided() {
        // Given a case has a Claimant Work Address provided and postcode in it is not set or is null
        // When report data is requested
        // Then on all cases with valid date, "Null" should be used for postcode in the report data detail entries
        Address claimantAddressUK = new Address();
        claimantAddressUK.setPostCode("DH3 8HL");
        ClaimantType claimant = new ClaimantType();
        claimant.setClaimantAddressUK(claimantAddressUK);

        Address claimantWorkAddress = new Address();
        claimantAddressUK.setPostCode(null);
        ClaimantWorkAddressType claimantWorkAddressType = new ClaimantWorkAddressType();
        claimantWorkAddressType.setClaimantWorkAddress(claimantWorkAddress);

        ClaimsByHearingVenueSubmitEvent submitEventOne = caseDataBuilder
                .withEthosCaseReference("18000012/2022")
                .withReceiptDate("2021-12-14")
                .withClaimantType(claimant)
                .withClaimantWorkAddressType(claimantWorkAddressType)
                .withRespondentCollection(null)
                .buildAsSubmitEvent(ACCEPTED_STATE);
        submitEvents.add(submitEventOne);

        when(claimsByHearingVenueReportDataSource.getData(OFFICE_NAME,
                UtilHelper.getListingCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID), RANGE_START_DATE, RANGE_END_DATE))
                .thenReturn(submitEvents);
        ClaimsByHearingVenueReportData reportData = claimsByHearingVenueReport
                .generateReport(reportParams);

        String actualClaimantAddressUKPostcode = reportData.getReportDetails().get(0).getClaimantPostcode();
        String actualClaimantWorkPostcode = reportData.getReportDetails().get(0).getClaimantWorkPostcode();
        String actualRespondentPostcode = reportData.getReportDetails().get(0).getRespondentPostcode();
        assertEquals("Null", actualClaimantAddressUKPostcode);
        assertEquals("Null", actualClaimantWorkPostcode);
        assertEquals("Null", actualRespondentPostcode);
        String actualRespondentET3Postcode = reportData.getReportDetails().get(0).getRespondentET3Postcode();
        assertEquals("Null", actualRespondentET3Postcode);
    }

    @Test
    void shouldShowFirstRespondentPostCodeOrNullString() {
        // Given a case has a number of respondents
        // When report data is requested
        // Then only the postcode of the first respondent detail should be used to set "Respondent Postcode"
        // and "Respondent ET3 Postcode" values in the report data detail entry. "Null" should be used if
        // no postcodes found
        RespondentSumType respondentSumType = new RespondentSumType();
        Address firstRespondentAddress = new Address();
        firstRespondentAddress.setPostCode("DH1 1AE");
        respondentSumType.setRespondentAddress(firstRespondentAddress);

        Address firstRespondentET3Address = new Address();
        firstRespondentET3Address.setPostCode("DH1 1AJ");
        respondentSumType.setResponseRespondentAddress(firstRespondentET3Address);
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(respondentSumType);
        List<RespondentSumTypeItem> respondentCollection = new ArrayList<>();
        respondentCollection.add(respondentSumTypeItem);

        RespondentSumTypeItem respondentSumTypeItemTwo = new RespondentSumTypeItem();
        RespondentSumType respondentSumTypeTwo = new RespondentSumType();
        Address secondRespondentAddress = new Address();
        secondRespondentAddress.setPostCode("DH5 9AJ");
        respondentSumTypeTwo.setRespondentAddress(secondRespondentAddress);
        respondentSumTypeItemTwo.setValue(respondentSumTypeTwo);
        respondentCollection.add(respondentSumTypeItemTwo);

        Address claimantAddressUK = new Address();
        claimantAddressUK.setPostCode("DH3 8HL");
        ClaimantType claimant = new ClaimantType();
        claimant.setClaimantAddressUK(claimantAddressUK);

        ClaimsByHearingVenueSubmitEvent submitEventOne = caseDataBuilder
                .withEthosCaseReference("18000012/2022")
                .withReceiptDate("2021-12-14")
                .withClaimantType(claimant)
                .withClaimantWorkAddressType(null)
                .withRespondentCollection(respondentCollection)
                .buildAsSubmitEvent(ACCEPTED_STATE);
        submitEvents.add(submitEventOne);
        when(claimsByHearingVenueReportDataSource.getData(OFFICE_NAME,
                UtilHelper.getListingCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID), RANGE_START_DATE, RANGE_END_DATE))
                .thenReturn(submitEvents);

        String expectedRespondentPostCode = firstRespondentAddress.getPostCode();
        String expectedFirstRespondentET3AddressPostCode = firstRespondentET3Address.getPostCode();
        ClaimsByHearingVenueReportData reportData = claimsByHearingVenueReport
                .generateReport(reportParams);
        String actualRespondentPostCode = reportData.getReportDetails().get(0).getRespondentPostcode();
        int expectedReportDetailEntriesCount = submitEvents.size();
        assertEquals(expectedReportDetailEntriesCount, reportData.getReportDetails().size());
        assertEquals(expectedRespondentPostCode, actualRespondentPostCode);
        String actualRespondentET3PostCode = reportData.getReportDetails().get(0).getRespondentET3Postcode();
        assertEquals(expectedFirstRespondentET3AddressPostCode, actualRespondentET3PostCode);
    }

    @Test
    void shouldShowReportDetailEntriesSortedByEthosCaseReferenceAscending() {
        Address claimantAddressUK = new Address();
        claimantAddressUK.setPostCode("DH3 8HL");
        ClaimantType claimant = new ClaimantType();
        claimant.setClaimantAddressUK(claimantAddressUK);

        ClaimsByHearingVenueSubmitEvent submitEventOne = caseDataBuilder
                .withEthosCaseReference("18000012/2022")
                .withReceiptDate("2021-12-14")
                .withClaimantType(claimant)
                .withClaimantWorkAddressType(null)
                .withRespondentCollection(null)
                .buildAsSubmitEvent(ACCEPTED_STATE);
        submitEvents.add(submitEventOne);

        ClaimsByHearingVenueSubmitEvent submitEventTwo = caseDataBuilder
                .withEthosCaseReference("1800154/2021")
                .withReceiptDate("2021-12-08")
                .withClaimantType(claimant)
                .withClaimantWorkAddressType(null)
                .withRespondentCollection(null)
                .buildAsSubmitEvent(ACCEPTED_STATE);
        submitEvents.add(submitEventTwo);

        ClaimsByHearingVenueSubmitEvent submitEventThree = caseDataBuilder
                .withEthosCaseReference("18000003/2022")
                .withReceiptDate("2021-12-08")
                .withClaimantType(claimant)
                .withClaimantWorkAddressType(null)
                .withRespondentCollection(null)
                .buildAsSubmitEvent(ACCEPTED_STATE);
        submitEvents.add(submitEventThree);

        when(claimsByHearingVenueReportDataSource.getData(OFFICE_NAME,
                UtilHelper.getListingCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID), RANGE_START_DATE, RANGE_END_DATE))
                .thenReturn(submitEvents);

        int expectedNumberOfSubmitEventEntries = submitEvents.size();
        String expectedFirstCaseReference = submitEvents.get(1).getCaseData().getEthosCaseReference();
        String expectedSecondCaseReference = submitEvents.get(2).getCaseData().getEthosCaseReference();
        ClaimsByHearingVenueReportData reportData = claimsByHearingVenueReport
                .generateReport(reportParams);
        assertEquals(expectedNumberOfSubmitEventEntries, reportData.getReportDetails().size());
        assertEquals(expectedFirstCaseReference, reportData.getReportDetails().get(0).getCaseReference());
        assertEquals(expectedSecondCaseReference, reportData.getReportDetails().get(1).getCaseReference());
        String expectedThirdFirstCaseReference = submitEvents.get(0).getCaseData().getEthosCaseReference();
        assertEquals(expectedThirdFirstCaseReference, reportData.getReportDetails().get(2).getCaseReference());
    }

    @Test
    void shouldShowCorrectReportPrintedOnDescription() {
        // Given cases have date of Receipt value within the inclusive date range searched for
        // When report data is requested
        // Then excel report should print correct value for "ReportPrintedOnDescription" field

        Address claimantAddressUK = new Address();
        claimantAddressUK.setPostCode("DH3 8HL");
        ClaimantType claimant = new ClaimantType();
        claimant.setClaimantAddressUK(claimantAddressUK);

        ClaimsByHearingVenueSubmitEvent submitEventOne = caseDataBuilder
                .withEthosCaseReference("18000012/2022")
                .withReceiptDate("2021-12-08")
                .withClaimantType(claimant)
                .withClaimantWorkAddressType(null)
                .withRespondentCollection(null)
                .buildAsSubmitEvent(ACCEPTED_STATE);
        submitEvents.add(submitEventOne);
        ClaimsByHearingVenueReportParams singleHearingDateTypeReportParams = new ClaimsByHearingVenueReportParams(
                ENGLANDWALES_LISTING_CASE_TYPE_ID,
                OFFICE_NAME, SINGLE_START_DATE, SINGLE_END_DATE, SINGLE_HEARING_DATE_TYPE, TEST_USERNAME);
        when(claimsByHearingVenueReportDataSource.getData(OFFICE_NAME,
                UtilHelper.getListingCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID), SINGLE_START_DATE, SINGLE_END_DATE))
                .thenReturn(submitEvents);
        ClaimsByHearingVenueReportData reportData = claimsByHearingVenueReport
                .generateReport(singleHearingDateTypeReportParams);
        String actualReportTitle = reportData.getReportPrintedOnDescription();
        String expectedReportPrintedOnDescription = getTestReportPrintedDescription();
        assertEquals(expectedReportPrintedOnDescription, actualReportTitle);
    }

    private String getReportTitle(String reportDateType) {
        if (SINGLE_HEARING_DATE_TYPE.equals(reportDateType)) {
            return "   Period: " + getExpectedSingleDateReportTitle() + "       Office: " + OFFICE_NAME;
        } else {
            return "   Period: " + getExpectedDateRangeReportTitle() + "       Office: " + OFFICE_NAME;
        }
    }

    private String getExpectedDateRangeReportTitle() {
        return "Between " + UtilHelper.listingFormatLocalDate(ReportHelper.getFormattedLocalDate(RANGE_START_DATE))
                + " and " + UtilHelper.listingFormatLocalDate(ReportHelper.getFormattedLocalDate(RANGE_END_DATE));
    }

    private String getExpectedSingleDateReportTitle() {
        return "On " + UtilHelper.listingFormatLocalDate(ReportHelper.getFormattedLocalDate(SINGLE_START_DATE));
    }

    private String getTestReportPrintedDescription() {
        return "Reported on: " + UtilHelper.formatCurrentDate(LocalDate.now()) + "   By: " + TEST_USERNAME;
    }
}
