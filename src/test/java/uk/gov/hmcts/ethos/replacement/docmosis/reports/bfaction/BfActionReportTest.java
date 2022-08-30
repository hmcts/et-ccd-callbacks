package uk.gov.hmcts.ethos.replacement.docmosis.reports.bfaction;

import org.junit.Before;
import org.junit.Test;
import org.junit.platform.commons.util.StringUtils;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.et.common.model.listing.items.BFDateTypeItem;
import uk.gov.hmcts.et.common.model.listing.types.BFDateType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.et.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.et.common.model.helper.Constants.BROUGHT_FORWARD_REPORT;
import static uk.gov.hmcts.et.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.et.common.model.helper.Constants.RANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.et.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.et.common.model.helper.Constants.SINGLE_CASE_TYPE;

public class BfActionReportTest {
    private List<SubmitEvent> submitEvents;
    private ListingDetails listingDetails;
    private ListingData listingData;
    private BfActionReport bfActionReport;
    private SubmitEvent submitEvent;
    private CaseData caseData;

    @Before
    public void setUp() {
        listingDetails = new ListingDetails();
        listingData = new ListingData();
        listingData.setReportType(BROUGHT_FORWARD_REPORT);
        listingDetails.setJurisdiction("EMPLOYMENT");
        listingData.setListingVenue(DynamicFixedListType.of(DynamicValueType.create("Leeds", "Leeds")));
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        submitEvents = new ArrayList<>();
        listingData = new ListingData();
        listingData.setListingDateFrom("2019-12-08");
        listingData.setListingDateTo("2019-12-20");
        listingData.setHearingDateType(RANGE_HEARING_DATE_TYPE);
        listingDetails.setCaseData(listingData);
        bfActionReport = new BfActionReport();
        submitEvent = new SubmitEvent();
        submitEvent.setCaseId(2);
        submitEvent.setState(ACCEPTED_STATE);
        caseData = new CaseData();
        caseData.setEthosCaseReference("1800522/2020");
        caseData.setReceiptDate("2018-08-10");
        CasePreAcceptType casePreAcceptType2 = new CasePreAcceptType();
        casePreAcceptType2.setDateAccepted("2018-08-10");
        caseData.setPreAcceptCase(casePreAcceptType2);
        caseData.setEcmCaseType(SINGLE_CASE_TYPE);
    }

    @Test
    public void shouldReturnOnlyOpenBfActionsWithInDateRange() {

        BFActionTypeItem bfActionTypeItem = new BFActionTypeItem();
        bfActionTypeItem.setId("123");
        BFActionType bfActionType = new BFActionType();
        bfActionType.setCwActions("Case papers prepared");
        bfActionType.setBfDate("2019-12-18");
        bfActionType.setDateEntered("2019-11-20");
        bfActionType.setNotes("test comment one");
        bfActionTypeItem.setValue(bfActionType);

        BFActionTypeItem bfActionTypeItem2 = new BFActionTypeItem();
        bfActionTypeItem2.setId("456");
        BFActionType bfActionType2 = new BFActionType();
        bfActionType2.setCwActions("Interlocutory order requested");
        bfActionType2.setBfDate("2019-12-05");
        bfActionType2.setDateEntered("2019-11-20");
        bfActionType2.setNotes("test cleared bf");
        bfActionTypeItem2.setValue(bfActionType2);

        BFActionTypeItem bfActionTypeItem3 = new BFActionTypeItem();
        bfActionTypeItem3.setId("456");
        BFActionType bfActionType3 = new BFActionType();
        bfActionType3.setCwActions("Interlocutory new order requested");
        bfActionType3.setBfDate("2019-12-08");
        bfActionType3.setDateEntered("2019-11-20");
        bfActionType3.setNotes("test non-cleared bf two");
        bfActionTypeItem3.setValue(bfActionType3);

        BFActionTypeItem bfActionTypeItem4 = new BFActionTypeItem();
        bfActionTypeItem4.setId("789");
        BFActionType bfActionType4 = new BFActionType();
        bfActionType4.setCwActions("Application of letter to ACAS/RPO");
        bfActionType4.setBfDate("2019-12-14");
        bfActionType4.setDateEntered("2019-11-20");
        bfActionType4.setNotes("test non-cleared bf three");
        bfActionTypeItem4.setValue(bfActionType4);

        List<BFActionTypeItem> items = new ArrayList<>();
        items.add(bfActionTypeItem);
        items.add(bfActionTypeItem2);
        items.add(bfActionTypeItem3);
        items.add(bfActionTypeItem4);

        caseData.setBfActions(items);
        submitEvent.setCaseData(caseData);
        submitEvents.add(submitEvent);

        ListingData resultListingData = bfActionReport.runReport(listingDetails, submitEvents);
        int actualBfDateCount  = resultListingData.getBfDateCollection().size();
        int expectedBfDateCount = 3;
        assertEquals(expectedBfDateCount, actualBfDateCount);

        BFDateType firstBFDateTypeItem = resultListingData.getBfDateCollection().get(0).getValue();
        assertEquals(bfActionType3.getBfDate(), firstBFDateTypeItem.getBroughtForwardDate());
        assertEquals(bfActionType3.getCwActions(), firstBFDateTypeItem.getBroughtForwardAction());
        assertEquals(bfActionType3.getDateEntered(), firstBFDateTypeItem.getBroughtForwardEnteredDate());
        assertEquals(bfActionType3.getNotes(), firstBFDateTypeItem.getBroughtForwardDateReason());
        assertEquals(bfActionType3.getCleared(), firstBFDateTypeItem.getBroughtForwardDateCleared());

        Stream<BFDateTypeItem> clearedBfDates = resultListingData.getBfDateCollection().stream()
            .filter(item -> !StringUtils.isBlank(item.getValue().getBroughtForwardDateCleared()));
        assertEquals(0, clearedBfDates.count());
    }

    @Test
    public void shouldReturnBfActionsWithMillisecondInBfDate() {
        listingData.setListingDateFrom("2019-12-13");
        listingData.setListingDateTo("2019-12-28");
        listingData.setHearingDateType(RANGE_HEARING_DATE_TYPE);
        listingDetails.setCaseData(listingData);
        BFActionTypeItem bfActionTypeItem = new BFActionTypeItem();
        bfActionTypeItem.setId("123");
        BFActionType bfActionType = new BFActionType();
        bfActionType.setCwActions("Case papers prepared");
        bfActionType.setBfDate("2019-12-18T19:30:55.000");
        bfActionType.setDateEntered("2019-11-20");
        bfActionType.setNotes("test comment one");
        bfActionTypeItem.setValue(bfActionType);
        List<BFActionTypeItem> items = getBFActionTypeItems();
        items.add(bfActionTypeItem);
        caseData.setBfActions(items);
        submitEvent.setCaseData(caseData);
        submitEvents.add(submitEvent);

        ListingData resultListingData = bfActionReport.runReport(listingDetails, submitEvents);
        BFDateType firstBFDateTypeItem = resultListingData.getBfDateCollection().get(2).getValue();
        assertEquals(bfActionType.getBfDate().split("T")[0], firstBFDateTypeItem.getBroughtForwardDate());
        assertEquals(bfActionType.getCwActions(), firstBFDateTypeItem.getBroughtForwardAction());
        assertEquals(bfActionType.getNotes(), firstBFDateTypeItem.getBroughtForwardDateReason());
        assertEquals(bfActionType.getDateEntered(), firstBFDateTypeItem.getBroughtForwardEnteredDate());
        assertEquals(bfActionType.getCleared(), firstBFDateTypeItem.getBroughtForwardDateCleared());
    }

    @Test
    public void shouldReturnBfActionsWithOnlyDateAndTimeInBfDate() {
        listingData.setListingDateFrom("2019-12-08");
        listingData.setListingDateTo("2019-12-20");
        listingData.setHearingDateType(RANGE_HEARING_DATE_TYPE);
        listingDetails.setCaseData(listingData);

        // Two bf action entries added and the second BFActionTypeItem has
        // the "YYYY-MM-DD HH:MM:SS" date and time pattern without milliseconds for the BfDate
        List<BFActionTypeItem> items = getBFActionTypeItems();
        caseData.setBfActions(items);
        submitEvent.setCaseData(caseData);
        submitEvents.add(submitEvent);

        ListingData resultListingData = bfActionReport.runReport(listingDetails, submitEvents);
        int expectedBfDateCount = 2;
        assertEquals(expectedBfDateCount, resultListingData.getBfDateCollection().size());
    }

    @Test
    public void shouldNotReturnClearedBfActionsWithInDateRange() {
        // Given three BFActionTypeItems, where the first two are still open and the third
        // one is with cleared BF status, only the two open (i.e. not cleared) BFs should be returned.
        BFActionTypeItem bfActionTypeItem4 = new BFActionTypeItem();
        bfActionTypeItem4.setId("116");
        BFActionType bfActionType4 = new BFActionType();
        bfActionType4.setCwActions("Interlocutory order requested");
        bfActionType4.setBfDate("2019-12-13");
        bfActionType4.setDateEntered("2019-11-20");
        bfActionType4.setCleared("2019-12-24");
        bfActionType4.setNotes("test case with cleared bfs");
        bfActionTypeItem4.setValue(bfActionType4);
        List<BFActionTypeItem> items = getBFActionTypeItems();
        items.add(bfActionTypeItem4);
        caseData.setBfActions(items);
        submitEvent.setCaseData(caseData);
        submitEvents.add(submitEvent);

        ListingData resultListingData = bfActionReport.runReport(listingDetails, submitEvents);
        int expectedBfDateCount = 2;
        assertEquals(expectedBfDateCount, resultListingData.getBfDateCollection().size());
    }

    @Test
    public void shouldReturnBfActionsSortedByBfDate() {
        listingData.setListingDateFrom("2019-12-08");
        listingData.setListingDateTo("2019-12-25");
        listingData.setHearingDateType(RANGE_HEARING_DATE_TYPE);
        listingDetails.setCaseData(listingData);
        BFActionTypeItem bfActionTypeItem6 = new BFActionTypeItem();
        bfActionTypeItem6.setId("456");
        BFActionType bfActionType6 = new BFActionType();
        bfActionType6.setCwActions("Interlocutory new order requested");
        bfActionType6.setBfDate("2019-12-08");
        bfActionType6.setDateEntered("2019-11-20");
        bfActionType6.setNotes("test non-cleared bf sixth");
        bfActionTypeItem6.setValue(bfActionType6);
        List<BFActionTypeItem> items = getBFActionTypeItems();
        items.add(bfActionTypeItem6);
        caseData.setBfActions(items);
        submitEvent.setCaseData(caseData);
        submitEvents.add(submitEvent);

        BfActionReport bfActionReport = new BfActionReport();
        ListingData resultListingData = bfActionReport.runReport(listingDetails, submitEvents);
        // bfActionType3 is added last. But it has the earliest bfDate. As the returned listingData from
        // bfActionReport.runReport method call should be ordered by bfDate, bfActionType3
        // should be the first element
        BFActionType bfActionType3 = items.get(2).getValue();
        BFDateType firstBFDateTypeItem = resultListingData.getBfDateCollection().get(0).getValue();
        assertEquals(bfActionType3.getBfDate(), firstBFDateTypeItem.getBroughtForwardDate());
        assertEquals(bfActionType3.getCwActions(), firstBFDateTypeItem.getBroughtForwardAction());
        assertEquals(bfActionType3.getNotes(), firstBFDateTypeItem.getBroughtForwardDateReason());
        assertEquals(bfActionType3.getDateEntered(), firstBFDateTypeItem.getBroughtForwardEnteredDate());
        assertEquals(bfActionType3.getCleared(), firstBFDateTypeItem.getBroughtForwardDateCleared());
    }

    @Test
    public void shouldReturnBfActionsWithFormattedComment() {
        listingData.setListingDateFrom("2019-12-03");
        listingData.setListingDateTo("2019-12-28");
        listingData.setHearingDateType(RANGE_HEARING_DATE_TYPE);
        listingDetails.setCaseData(listingData);
        BFActionTypeItem bfActionTypeItemFour = new BFActionTypeItem();
        bfActionTypeItemFour.setId("1488");
        BFActionType bfActionTypeFour = new BFActionType();
        bfActionTypeFour.setCwActions("Another order requested");
        bfActionTypeFour.setBfDate("2019-12-07");
        bfActionTypeFour.setDateEntered("2019-11-25");
        bfActionTypeFour.setNotes("test non-cleared bf two\n Second line comment");
        bfActionTypeItemFour.setValue(bfActionTypeFour);
        List<BFActionTypeItem> items = getBFActionTypeItems();
        items.add(bfActionTypeItemFour);
        caseData.setBfActions(items);
        submitEvent.setCaseData(caseData);
        submitEvents.add(submitEvent);

        BfActionReport bfActionReport = new BfActionReport();
        ListingData resultListingData = bfActionReport.runReport(listingDetails, submitEvents);
        //Because the Bf entries are sorted by bf date, bfActionTypeItemFour is the first entry in the
        //result listing data
        BFDateType firstBFDateTypeItem = resultListingData.getBfDateCollection().get(0).getValue();
        String correctedComment = bfActionTypeItemFour.getValue().getNotes()
            .replace("\n", ". ");
        assertEquals(correctedComment, firstBFDateTypeItem.getBroughtForwardDateReason());
    }

    @Test
    public void shouldShowReportOfficeName_EngWales() {
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        ListingData resultListingData = bfActionReport.runReport(listingDetails, submitEvents);
        BfActionReportData bfActionReportData = (BfActionReportData) resultListingData;
        assertEquals(TribunalOffice.MANCHESTER.getOfficeName(), bfActionReportData.getOffice());
    }

    @Test
    public void shouldShowReportOfficeName_Scotland() {
        listingDetails.getCaseData().setManagingOffice(null);
        listingDetails.setCaseTypeId(SCOTLAND_LISTING_CASE_TYPE_ID);
        ListingData resultListingData = bfActionReport.runReport(listingDetails, submitEvents);
        BfActionReportData bfActionReportData = (BfActionReportData) resultListingData;
        assertEquals(TribunalOffice.SCOTLAND.getOfficeName(), bfActionReportData.getOffice());
    }

    private List<BFActionTypeItem> getBFActionTypeItems() {

        BFActionTypeItem bfActionTypeItem3 = new BFActionTypeItem();
        bfActionTypeItem3.setId("116");
        BFActionType bfActionType3 = new BFActionType();
        bfActionType3.setCwActions("Interlocutory order requested");
        bfActionType3.setBfDate("2019-12-13");
        bfActionType3.setDateEntered("2019-11-20");
        bfActionType3.setNotes("test non-cleared bf two");
        bfActionTypeItem3.setValue(bfActionType3);

        BFActionTypeItem bfActionTypeItem4 = new BFActionTypeItem();
        bfActionTypeItem4.setId("99456");
        BFActionType bfActionType4 = new BFActionType();
        bfActionType4.setCwActions("Interlocutory new order requested");
        bfActionType4.setBfDate("2019-12-16 08:30:55");
        bfActionType4.setDateEntered("2019-11-23");
        bfActionType4.setNotes("test another non-cleared bf three");
        bfActionTypeItem4.setValue(bfActionType4);

        List<BFActionTypeItem> items = new ArrayList<>();
        items.add(bfActionTypeItem3);
        items.add(bfActionTypeItem4);

        return items;
    }
}
