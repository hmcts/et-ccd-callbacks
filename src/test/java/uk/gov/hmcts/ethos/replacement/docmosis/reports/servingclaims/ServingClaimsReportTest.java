package uk.gov.hmcts.ethos.replacement.docmosis.reports.servingclaims;

import org.assertj.core.util.Strings;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.et.common.model.listing.types.AdhocReportType;
import uk.gov.hmcts.et.common.model.listing.types.ClaimServedTypeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TRANSFERRED_STATE;

@SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.LawOfDemeter", "PMD.NcssCount", "PMD.TooManyMethods"})
public class ServingClaimsReportTest {

    private List<SubmitEvent> submitEvents;
    private ListingDetails listingDetails;

    @Before
    public void setUp() {
        listingDetails = new ListingDetails();
        ListingData listingDataRange = new ListingData();
        listingDataRange.setListingDateFrom("2020-08-02");
        listingDataRange.setListingDateTo("2020-08-24");
        listingDataRange.setListingVenue(new DynamicFixedListType("Leeds"));
        listingDataRange.setReportType("Claims Served");
        listingDetails.setCaseData(listingDataRange);
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.setJurisdiction("EMPLOYMENT");

        SubmitEvent submitEvent1 = new SubmitEvent();
        submitEvent1.setCaseId(1);
        submitEvent1.setState(ACCEPTED_STATE);

        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("1800522/2020");
        caseData.setReceiptDate("2020-08-10");
        CasePreAcceptType casePreAcceptType = new CasePreAcceptType();
        casePreAcceptType.setDateAccepted("2020-08-10");
        caseData.setPreAcceptCase(casePreAcceptType);
        caseData.setEcmCaseType(SINGLE_CASE_TYPE);

        BFActionTypeItem bfActionTypeItem = new BFActionTypeItem();
        BFActionType bfActionType = new BFActionType();
        bfActionType.setBfDate("2020-08-10");
        bfActionType.setNotes("Test Notes One");
        bfActionTypeItem.setId("0011");
        bfActionTypeItem.setValue(bfActionType);
        caseData.setBfActions(List.of(bfActionTypeItem));
        caseData.setClaimServedDate("2020-08-10");
        submitEvent1.setCaseData(caseData);

        SubmitEvent submitEvent2 = new SubmitEvent();
        submitEvent2.setCaseId(2);
        submitEvent2.setState(ACCEPTED_STATE);

        CaseData caseData2 = new CaseData();
        caseData2.setEthosCaseReference("1800523/2020");
        caseData2.setReceiptDate("2020-08-15");
        CasePreAcceptType casePreAcceptType2 = new CasePreAcceptType();
        casePreAcceptType2.setDateAccepted("2020-08-16");
        caseData2.setPreAcceptCase(casePreAcceptType2);
        caseData2.setEcmCaseType(SINGLE_CASE_TYPE);

        BFActionTypeItem bfActionTypeItem2 = new BFActionTypeItem();
        BFActionType bfActionType2 = new BFActionType();
        bfActionType2.setBfDate("2020-08-18");
        bfActionType2.setNotes("Test Notes Two");
        bfActionTypeItem2.setId("0012");
        bfActionTypeItem2.setValue(bfActionType2);
        caseData2.setBfActions(List.of(bfActionTypeItem2));
        caseData2.setClaimServedDate("2020-08-18");
        submitEvent2.setCaseData(caseData2);

        SubmitEvent submitEvent3 = new SubmitEvent();
        submitEvent3.setCaseId(3);
        submitEvent3.setState(TRANSFERRED_STATE);

        CaseData caseData3 = new CaseData();
        caseData3.setEthosCaseReference("1800524/2020");
        caseData3.setReceiptDate("2020-08-25");
        CasePreAcceptType casePreAcceptType3 = new CasePreAcceptType();
        casePreAcceptType3.setDateAccepted("2020-08-25");
        caseData3.setPreAcceptCase(casePreAcceptType3);
        caseData3.setEcmCaseType(SINGLE_CASE_TYPE);

        BFActionTypeItem bfActionTypeItem3 = new BFActionTypeItem();
        BFActionType bfActionType3 = new BFActionType();
        bfActionType3.setBfDate("2020-08-25");
        bfActionType3.setNotes("Test Notes Three");
        bfActionTypeItem3.setId("0013");
        bfActionTypeItem3.setValue(bfActionType3);
        caseData3.setBfActions(List.of(bfActionTypeItem3));
        caseData3.setClaimServedDate("2020-08-25");
        submitEvent3.setCaseData(caseData3);

        SubmitEvent submitEvent4 = new SubmitEvent();
        submitEvent4.setCaseId(4);
        submitEvent4.setState(CLOSED_STATE);

        CaseData caseData4 = new CaseData();
        caseData4.setEthosCaseReference("1800525/2020");
        caseData4.setReceiptDate("2020-04-10");
        CasePreAcceptType casePreAcceptType4 = new CasePreAcceptType();
        casePreAcceptType4.setDateAccepted("2020-08-07");
        caseData4.setPreAcceptCase(casePreAcceptType4);
        caseData4.setEcmCaseType(SINGLE_CASE_TYPE);

        BFActionTypeItem bfActionTypeItem4 = new BFActionTypeItem();
        BFActionType bfActionType4 = new BFActionType();
        bfActionType4.setBfDate("2020-08-10");
        bfActionType4.setNotes("Test Notes Four");
        bfActionTypeItem4.setId("0014");
        bfActionTypeItem4.setValue(bfActionType4);
        caseData4.setBfActions(List.of(bfActionTypeItem4));
        caseData4.setClaimServedDate("2020-08-15");
        submitEvent4.setCaseData(caseData4);

        SubmitEvent submitEvent5 = new SubmitEvent();
        submitEvent5.setCaseId(5);
        submitEvent5.setState(ACCEPTED_STATE);

        CaseData caseData5 = new CaseData();
        caseData5.setEthosCaseReference("1800528/2020");
        caseData5.setReceiptDate("2020-08-19");
        CasePreAcceptType casePreAcceptType5 = new CasePreAcceptType();
        casePreAcceptType5.setDateAccepted("2020-08-07");
        caseData5.setPreAcceptCase(casePreAcceptType5);
        caseData5.setEcmCaseType(SINGLE_CASE_TYPE);

        BFActionTypeItem bfActionTypeItem5 = new BFActionTypeItem();
        BFActionType bfActionType5 = new BFActionType();
        bfActionType5.setBfDate("2020-08-19");
        bfActionType5.setNotes("Test Notes Five");
        bfActionTypeItem5.setId("0014");
        bfActionTypeItem5.setValue(bfActionType5);
        caseData5.setBfActions(List.of(bfActionTypeItem5));
        caseData5.setClaimServedDate("2020-08-21");
        submitEvent5.setCaseData(caseData5);

        submitEvents = new ArrayList<>();
        submitEvents.add(submitEvent1);
        submitEvents.add(submitEvent2);
        submitEvents.add(submitEvent3);
        submitEvents.add(submitEvent4);
        submitEvents.add(submitEvent5);
    }

    @Test
    public void shouldIncludeCasesWithClaimsServedDate() {
        ServingClaimsReport servingClaimsReport = new ServingClaimsReport();
        ListingData resultListingData = servingClaimsReport.generateReportData(listingDetails, submitEvents);
        String caseTotalCount =  resultListingData.getLocalReportsDetail().get(0)
                .getValue().getClaimServedTotal();
        int actualCount = Strings.isNullOrEmpty(caseTotalCount)  ? 0 : Integer.parseInt(caseTotalCount);
        assertEquals(5, actualCount);
    }

    @Test
    public void shouldReturnCorrectCasesCountByServingDay() {
        ServingClaimsReport servingClaimsReport = new ServingClaimsReport();
        ListingData resultListingData = servingClaimsReport.generateReportData(listingDetails, submitEvents);
        AdhocReportType adhocReportType =  resultListingData.getLocalReportsDetail().get(0)
                .getValue();
        List<ClaimServedTypeItem> claimServedItems = adhocReportType.getClaimServedItems();
        long expectedDay1Count = claimServedItems.stream()
                .filter(x -> Integer.parseInt(x.getValue().getReportedNumberOfDays()) == 0).count();
        long expectedDay2Count = claimServedItems.stream()
                .filter(x -> Integer.parseInt(x.getValue().getReportedNumberOfDays()) == 1).count();
        long expectedDay3Count = claimServedItems.stream()
                .filter(x -> Integer.parseInt(x.getValue().getReportedNumberOfDays()) == 2).count();
        assertEquals(2, expectedDay1Count);
        assertEquals(1, expectedDay2Count);
        assertEquals(1, expectedDay3Count);
        long expectedDay4Count = claimServedItems.stream()
                .filter(x -> Integer.parseInt(x.getValue().getReportedNumberOfDays()) == 3).count();
        assertEquals(0, expectedDay4Count);
        long expectedDay5Count = claimServedItems.stream()
                .filter(x -> Integer.parseInt(x.getValue().getReportedNumberOfDays()) == 4).count();
        assertEquals(0, expectedDay5Count);
        long expectedDay6PlusCount = claimServedItems.stream()
                .filter(x -> Integer.parseInt(x.getValue().getReportedNumberOfDays()) >= 5).count();
        assertEquals(1, expectedDay6PlusCount);
    }

    @Test
    public void shouldSetCorrectCountForDay1Serving() {
        ServingClaimsReport servingClaimsReport = new ServingClaimsReport();
        ListingData resultListingData = servingClaimsReport.generateReportData(listingDetails, submitEvents);
        AdhocReportType adhocReportType =  resultListingData.getLocalReportsDetail().get(0)
                .getValue();
        String expectedDay1Count = adhocReportType.getClaimServedDay1Total();
        String expectedDay1Percent = adhocReportType.getClaimServedDay1Percent();
        assertEquals("2", expectedDay1Count);
        assertEquals("40", expectedDay1Percent);
    }

    @Test
    public void shouldSetCorrectCountForDay2Serving() {
        ServingClaimsReport servingClaimsReport = new ServingClaimsReport();
        ListingData resultListingData = servingClaimsReport.generateReportData(listingDetails, submitEvents);
        AdhocReportType adhocReportType =  resultListingData.getLocalReportsDetail().get(0)
                .getValue();
        String expectedDay2Count = adhocReportType.getClaimServedDay2Total();
        String expectedDay2Percent = adhocReportType.getClaimServedDay2Percent();
        assertEquals("1", expectedDay2Count);
        assertEquals("20", expectedDay2Percent);
    }

    @Test
    public void shouldSetCorrectCountForDay3Serving() {
        ServingClaimsReport servingClaimsReport = new ServingClaimsReport();
        ListingData resultListingData = servingClaimsReport.generateReportData(listingDetails, submitEvents);
        AdhocReportType adhocReportType =  resultListingData.getLocalReportsDetail().get(0)
                .getValue();
        String expectedDay3Count = adhocReportType.getClaimServedDay3Total();
        String expectedDay3Percent = adhocReportType.getClaimServedDay3Percent();
        assertEquals("1", expectedDay3Count);
        assertEquals("20", expectedDay3Percent);
    }

    @Test
    public void shouldSetCorrectCountFor6PlusDaysServing() {
        ServingClaimsReport servingClaimsReport = new ServingClaimsReport();
        ListingData resultListingData = servingClaimsReport.generateReportData(listingDetails, submitEvents);
        AdhocReportType adhocReportType =  resultListingData.getLocalReportsDetail().get(0)
                .getValue();
        String expectedDay6PlusDaysCount = adhocReportType.getClaimServed6PlusDaysTotal();
        String expectedDay6PlusDaysPercent = adhocReportType.getClaimServed6PlusDaysPercent();
        assertEquals("1", expectedDay6PlusDaysCount);
        assertEquals("20", expectedDay6PlusDaysPercent);
    }

    @Test
    public void shouldSetCorrectActualAndReportedDayCountFor6PlusDaysServing() {
        ServingClaimsReport servingClaimsReport = new ServingClaimsReport();
        ListingData resultListingData = servingClaimsReport.generateReportData(listingDetails, submitEvents);
        List<ClaimServedTypeItem> claimServedItems = resultListingData.getLocalReportsDetail()
                .get(0).getValue().getClaimServedItems();
        List<ClaimServedTypeItem> expectedDay6PlusItems = claimServedItems.stream()
            .filter(x -> Integer.parseInt(x.getValue().getReportedNumberOfDays()) >= 5)
                .collect(Collectors.toList());
        ClaimServedTypeItem firstClaimServedItem = expectedDay6PlusItems.get(0);

        String reportedNumberOfDays = firstClaimServedItem.getValue().getReportedNumberOfDays();
        String actualNumberOfDays = firstClaimServedItem.getValue().getActualNumberOfDays();
        assertEquals("5", reportedNumberOfDays);
        assertEquals("92", actualNumberOfDays);
    }

    @Test
    public void shouldSetCorrectDayForLessThan6DaysServingClaim() {
        ServingClaimsReport servingClaimsReport = new ServingClaimsReport();
        ListingData resultListingData = servingClaimsReport.generateReportData(listingDetails, submitEvents);
        List<ClaimServedTypeItem> claimServedItems = resultListingData.getLocalReportsDetail()
                .get(0).getValue().getClaimServedItems();
        ClaimServedTypeItem secondClaimServedItem = claimServedItems.get(1);
        String numberOfDays = secondClaimServedItem.getValue().getActualNumberOfDays();
        assertEquals("2", numberOfDays);
    }

    @Test
    public void shouldNotAddServedClaimItemWhenNoClaimsServedFound() {
        ServingClaimsReport servingClaimsReport = new ServingClaimsReport();
        ListingData resultListingData = servingClaimsReport.generateReportData(listingDetails, null);
        int claimServedItemsCount = resultListingData.getLocalReportsDetail().get(0).getValue()
                .getClaimServedItems().size();
        assertEquals("0", String.valueOf(claimServedItemsCount));
    }

    @Test
    public void shouldNotIncludeCasesWithNoReceiptDateAndClaimServedDateProvided() {
        ServingClaimsReport servingClaimsReport = new ServingClaimsReport();
        SubmitEvent caseOne = submitEvents.get(0);
        caseOne.getCaseData().setReceiptDate(null);
        caseOne.getCaseData().setClaimServedDate(null);
        ListingData resultListingData = servingClaimsReport.generateReportData(listingDetails, submitEvents);
        List<ClaimServedTypeItem> claimServedItems = resultListingData.getLocalReportsDetail()
                .get(0).getValue().getClaimServedItems();
        long itemsListDoesNotContainCaseOneEntry = claimServedItems.stream()
                .filter(x -> x.getValue().getClaimServedCaseNumber()
                        .equals(caseOne.getCaseData().getEthosCaseReference())).count();
        assertEquals(4, claimServedItems.size());
        assertEquals(0, itemsListDoesNotContainCaseOneEntry);
    }

    @Test
    public void shouldSetCorrectReportedNumberOfDays() {
        ServingClaimsReport servingClaimsReport = new ServingClaimsReport();
        ListingData resultListingData = servingClaimsReport.generateReportData(listingDetails, submitEvents);
        ClaimServedTypeItem fourthClaimServedItem = resultListingData.getLocalReportsDetail().get(0).getValue()
                .getClaimServedItems().get(3);
        assertEquals("5", fourthClaimServedItem.getValue().getReportedNumberOfDays());
    }

    @Test
    public void shouldSetReportSummaryFromReportDetailsWhenReportDetailsIsNotEmpty() {
        ServingClaimsReport servingClaimsReport = new ServingClaimsReport();
        ListingData resultListingData = servingClaimsReport.generateReportData(listingDetails, submitEvents);
        int localReportsDetailSize = resultListingData.getLocalReportsDetail().size();
        int claimServedItemsCount = resultListingData.getLocalReportsDetail().get(0).getValue()
                .getClaimServedItems().size();

        assertEquals(1, localReportsDetailSize);
        assertEquals(5, claimServedItemsCount);
    }

    @Test
    public void shouldNotSetReportSummaryFromReportDetailsWhenReportDetailsIsEmpty() {
        ServingClaimsReport servingClaimsReport = new ServingClaimsReport();
        //Set ReceiptDate for each case to null to make LocalReportsDetail empty
        submitEvents.forEach(s -> s.getCaseData().setReceiptDate(null));
        ListingData resultListingData = servingClaimsReport.generateReportData(listingDetails, submitEvents);
        int localReportsDetailCount = resultListingData.getLocalReportsDetail().size();
        int claimServedItemsCount = resultListingData.getLocalReportsDetail().get(0).getValue()
                .getClaimServedItems().size();
        assertEquals(1, localReportsDetailCount);
        assertEquals(0, claimServedItemsCount);
    }

    @Test
    public void shouldShowReportOfficeName_EngWales() {
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        ServingClaimsReport servingClaimsReport = new ServingClaimsReport();
        ListingData resultListingData = servingClaimsReport.generateReportData(listingDetails, submitEvents);
        assertEquals(TribunalOffice.MANCHESTER.getOfficeName(),
                resultListingData.getLocalReportsDetailHdr().getReportOffice());
    }

    @Test
    public void shouldShowReportOfficeName_Scotland() {
        listingDetails.setCaseTypeId(SCOTLAND_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setManagingOffice(null);
        ServingClaimsReport servingClaimsReport = new ServingClaimsReport();
        ListingData resultListingData = servingClaimsReport.generateReportData(listingDetails, submitEvents);
        assertEquals(TribunalOffice.SCOTLAND.getOfficeName(),
                resultListingData.getLocalReportsDetailHdr().getReportOffice());
    }

}
