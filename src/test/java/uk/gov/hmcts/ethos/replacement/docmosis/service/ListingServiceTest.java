package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.et.common.model.listing.items.AdhocReportTypeItem;
import uk.gov.hmcts.et.common.model.listing.items.ListingTypeItem;
import uk.gov.hmcts.et.common.model.listing.types.AdhocReportType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.BFHelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.casescompleted.CasesCompletedReport;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.VenueService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ALL_VENUES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BROUGHT_FORWARD_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CASES_COMPLETED_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMS_ACCEPTED_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_FAST_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_NO_CONCILIATION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_OPEN_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_STANDARD_TRACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_DOC_ETCL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_ETCL_PRESS_LIST;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_ETCL_PUBLIC;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_ETCL_STAFF;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_MEDIATION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_PERLIMINARY_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_PRIVATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.JURISDICTION_OUTCOME_SUCCESSFUL_AT_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LIVE_CASELOAD_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.POSITION_TYPE_CASE_CLOSED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.TribunalOffice.BRISTOL;
import static uk.gov.hmcts.ecm.common.model.helper.TribunalOffice.DUNDEE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ListingHelper.CAUSE_LIST_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
class ListingServiceTest {

    @InjectMocks
    private ListingService listingService;
    @Mock
    private TornadoService tornadoService;
    @Mock
    private CcdClient ccdClient;
    @Mock
    private VenueService venueService;
    @Spy
    private final CasesCompletedReport casesCompletedReport = new CasesCompletedReport();
    private CaseDetails caseDetails;
    private ListingDetails listingDetails;
    private ListingDetails listingDetailsRange;
    private DocumentInfo documentInfo;
    private List<SubmitEvent> submitEvents;
    private static final List<DynamicValueType> VENUES = List.of(DynamicValueType.create("venue1", "Venue 1"),
            DynamicValueType.create("venue2", "Venue 2"));

    @BeforeEach
    void setUp() {
        documentInfo = new DocumentInfo();
        caseDetails = new CaseDetails();
        listingDetails = new ListingDetails();
        ListingData listingData = new ListingData();
        listingData.setListingDate("2019-12-12");
        listingData.setListingVenue(new DynamicFixedListType("Aberdeen"));
        listingData.setVenueAberdeen(new DynamicFixedListType("AberdeenVenue"));
        listingData.setListingCollection(new ArrayList<>());
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingData.setReportType(BROUGHT_FORWARD_REPORT);
        listingData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingDetails.setCaseData(listingData);
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.setJurisdiction("EMPLOYMENT");

        listingDetailsRange = new ListingDetails();
        ListingData listingData1 = new ListingData();
        listingData1.setListingDateFrom("2019-12-09");
        listingData1.setListingDateTo("2019-12-12");
        listingData1.setListingVenue(new DynamicFixedListType("Aberdeen"));
        listingData1.setVenueAberdeen(new DynamicFixedListType("AberdeenVenue"));
        listingData1.setListingCollection(new ArrayList<>());
        listingData1.setHearingDateType(RANGE_HEARING_DATE_TYPE);
        listingData1.setReportType("Brought Forward Report");
        listingData1.setClerkResponsible(new DynamicFixedListType("Steve Jones"));
        listingData1.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        listingDetailsRange.setCaseData(listingData1);
        listingDetailsRange.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetailsRange.setJurisdiction("EMPLOYMENT");

        DateListedType dateListedType = new DateListedType();
        dateListedType.setHearingStatus(HEARING_STATUS_HEARD);
        dateListedType.setHearingClerk(new DynamicFixedListType("Clerk"));
        dateListedType.setHearingRoom(new DynamicFixedListType("Tribunal 4"));
        dateListedType.setHearingAberdeen(new DynamicFixedListType("AberdeenVenue"));
        dateListedType.setHearingVenueDay(new DynamicFixedListType("Aberdeen"));
        dateListedType.setHearingVenueDayScotland("Aberdeen");
        dateListedType.setListedDate("2019-12-12T12:11:00.000");
        dateListedType.setHearingTimingStart("2019-12-12T12:11:00.000");
        dateListedType.setHearingTimingBreak("2019-12-12T12:11:00.000");
        dateListedType.setHearingTimingResume("2019-12-12T12:11:00.000");
        dateListedType.setHearingTimingFinish("2019-12-12T12:11:00.000");
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setId("123");
        dateListedTypeItem.setValue(dateListedType);

        DateListedType dateListedType1 = new DateListedType();
        dateListedType.setHearingStatus(HEARING_STATUS_HEARD);
        dateListedType1.setHearingClerk(new DynamicFixedListType("Clerk"));
        dateListedType1.setHearingRoom(new DynamicFixedListType("Tribunal 4"));
        dateListedType1.setHearingAberdeen(new DynamicFixedListType("AberdeenVenue"));
        dateListedType1.setHearingVenueDay(new DynamicFixedListType("Aberdeen"));
        dateListedType1.setHearingVenueDayScotland("Aberdeen");
        dateListedType1.setListedDate("2019-12-10T12:11:00.000");
        dateListedType1.setHearingTimingStart("2019-12-10T11:00:00.000");
        dateListedType1.setHearingTimingBreak("2019-12-10T12:00:00.000");
        dateListedType1.setHearingTimingResume("2019-12-10T13:00:00.000");
        dateListedType1.setHearingTimingFinish("2019-12-10T14:00:00.000");
        DateListedTypeItem dateListedTypeItem1 = new DateListedTypeItem();
        dateListedTypeItem1.setId("124");
        dateListedTypeItem1.setValue(dateListedType1);

        DateListedType dateListedType2 = new DateListedType();
        dateListedType.setHearingStatus(HEARING_STATUS_HEARD);
        dateListedType2.setHearingClerk(new DynamicFixedListType("Clerk1"));
        dateListedType2.setHearingCaseDisposed(YES);
        dateListedType2.setHearingRoom(new DynamicFixedListType("Tribunal 5"));
        dateListedType2.setHearingAberdeen(new DynamicFixedListType("AberdeenVenue2"));
        dateListedType2.setHearingVenueDay(new DynamicFixedListType("Aberdeen"));
        dateListedType2.setHearingVenueDayScotland("Aberdeen");
        dateListedType2.setListedDate("2019-12-12T12:11:30.000");
        DateListedTypeItem dateListedTypeItem2 = new DateListedTypeItem();
        dateListedTypeItem2.setId("124");
        dateListedTypeItem2.setValue(dateListedType2);

        DateListedType dateListedType3 = new DateListedType();
        dateListedType3.setHearingStatus(HEARING_STATUS_HEARD);
        dateListedType3.setHearingClerk(new DynamicFixedListType("Clerk3"));
        dateListedType3.setHearingCaseDisposed(YES);
        dateListedType3.setHearingRoom(new DynamicFixedListType("Tribunal 5"));
        dateListedType3.setHearingAberdeen(new DynamicFixedListType("AberdeenVenue2"));
        dateListedType3.setHearingVenueDay(new DynamicFixedListType("Aberdeen"));
        dateListedType3.setHearingVenueDayScotland("Aberdeen");
        dateListedType3.setListedDate("2019-12-12T12:11:55.000");
        dateListedType3.setHearingTimingStart("2019-12-12T14:11:55.000");
        dateListedType3.setHearingTimingBreak("2019-12-12T15:11:55.000");
        dateListedType3.setHearingTimingResume("2019-12-12T15:30:55.000");
        dateListedType3.setHearingTimingFinish("2019-12-12T16:30:55.000");
        DateListedTypeItem dateListedTypeItem3 = new DateListedTypeItem();
        dateListedTypeItem3.setId("124");
        dateListedTypeItem3.setValue(dateListedType3);

        HearingType hearingType = new HearingType();
        hearingType.setHearingDateCollection(new ArrayList<>(Arrays.asList(dateListedTypeItem,
                dateListedTypeItem1, dateListedTypeItem2)));
        hearingType.setHearingVenue(new DynamicFixedListType("Aberdeen"));
        hearingType.setHearingEstLengthNum("2");
        hearingType.setHearingEstLengthNumType("hours");
        hearingType.setHearingType(HEARING_TYPE_PERLIMINARY_HEARING);
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setId("12345");
        hearingTypeItem.setValue(hearingType);

        BFActionType bfActionType = new BFActionType();
        bfActionType.setBfDate("2019-12-10");
        bfActionType.setCleared("020-12-30");
        bfActionType.setAction(BFHelperTest.getBfActionsDynamicFixedList());
        BFActionTypeItem bfActionTypeItem = new BFActionTypeItem();
        bfActionTypeItem.setId("0000");
        bfActionTypeItem.setValue(bfActionType);
        HearingTypeItem hearingTypeItem1 = new HearingTypeItem();
        HearingType hearingType1 = new HearingType();
        hearingType1.setHearingDateCollection(new ArrayList<>(Collections.singleton(dateListedTypeItem3)));
        hearingType1.setHearingType(HEARING_TYPE_PERLIMINARY_HEARING);
        hearingTypeItem1.setId("12345");
        hearingTypeItem1.setValue(hearingType1);

        BFActionType bfActionType1 = new BFActionType();
        bfActionType1.setBfDate("2019-12-11");
        bfActionType1.setCleared("");
        bfActionType1.setAction(BFHelperTest.getBfActionsDynamicFixedList());
        BFActionTypeItem bfActionTypeItem1 = new BFActionTypeItem();
        bfActionTypeItem1.setId("111");
        bfActionTypeItem1.setValue(bfActionType1);

        BFActionType bfActionType2 = new BFActionType();
        bfActionType2.setBfDate("2019-12-12");
        bfActionType2.setCleared("");
        bfActionType2.setAction(BFHelperTest.getBfActionsDynamicFixedList());
        BFActionTypeItem bfActionTypeItem2 = new BFActionTypeItem();
        bfActionTypeItem2.setId("222");
        bfActionTypeItem2.setValue(bfActionType2);

        BFActionType bfActionType3 = new BFActionType();
        bfActionType3.setBfDate("2019-12-13");
        bfActionType3.setCleared("");
        bfActionType3.setAction(BFHelperTest.getBfActionsDynamicFixedList());
        BFActionTypeItem bfActionTypeItem3 = new BFActionTypeItem();
        bfActionTypeItem3.setId("333");
        bfActionTypeItem3.setValue(bfActionType3);

        BFActionType bfActionType4 = new BFActionType();
        bfActionType4.setBfDate("2019-12-10");
        bfActionType4.setCleared("020-12-30");
        bfActionType4.setNotes("Test0");
        BFActionTypeItem bfActionTypeItem4 = new BFActionTypeItem();
        bfActionTypeItem4.setId("0000");
        bfActionTypeItem4.setValue(bfActionType4);

        JurCodesTypeItem jurCodesTypeItem = new JurCodesTypeItem();
        JurCodesType jurCodesType = new JurCodesType();
        jurCodesType.setJuridictionCodesList("ABC");
        jurCodesType.setJudgmentOutcome(JURISDICTION_OUTCOME_SUCCESSFUL_AT_HEARING);
        jurCodesTypeItem.setId("000");
        jurCodesTypeItem.setValue(jurCodesType);

        SubmitEvent submitEvent1 = new SubmitEvent();
        submitEvent1.setCaseId(1);
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("4210000/2019");
        caseData.setHearingCollection(new ArrayList<>(Collections.singleton(hearingTypeItem)));
        caseData.setBfActions(new ArrayList<>(Arrays.asList(bfActionTypeItem,
                bfActionTypeItem1, bfActionTypeItem2, bfActionTypeItem3, bfActionTypeItem4)));
        caseData.setHearingCollection(new ArrayList<>(Arrays.asList(hearingTypeItem, hearingTypeItem1)));
        caseData.setJurCodesCollection(new ArrayList<>(Collections.singleton(jurCodesTypeItem)));
        caseData.setClerkResponsible(new DynamicFixedListType("Steve Jones"));
        CasePreAcceptType casePreAcceptType = new CasePreAcceptType();
        casePreAcceptType.setDateAccepted("2019-12-12");
        caseData.setPreAcceptCase(casePreAcceptType);
        caseData.setEcmCaseType(SINGLE_CASE_TYPE);
        caseData.setPositionType("Awaiting ET3");
        caseData.setConciliationTrack(CONCILIATION_TRACK_NO_CONCILIATION);
        caseData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        submitEvent1.setCaseData(caseData);
        submitEvent1.setState(CLOSED_STATE);
        submitEvents = new ArrayList<>(Collections.singleton(submitEvent1));

        caseData.setPrintHearingDetails(listingData);
        caseData.setPrintHearingCollection(listingData);
        Address address = new Address();
        address.setAddressLine1("Manchester Avenue");
        address.setPostTown("Manchester");
        caseData.setTribunalCorrespondenceAddress(address);
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        caseDetails.setJurisdiction("EMPLOYMENT");
    }

    @Test
    void listingCaseCreationWithHearingDocType() {
        String result = "ListingData(tribunalCorrespondenceAddress=null, "
                + "tribunalCorrespondenceTelephone=null, tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, "
                + "tribunalCorrespondenceEmail=null, reportDate=null,"
                + " hearingDateType=Single, listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, "
                + "listingVenue=DynamicFixedListType("
                + "value=DynamicValueType(code=Aberdeen, label=Aberdeen),"
                + " listItems=null), listingVenueScotland=null, listingCollection=[],"
                + " listingVenueOfficeGlas=null, listingVenueOfficeAber=null, "
                + "venueGlasgow=null, "
                + "venueAberdeen=DynamicFixedListType("
                + "value=DynamicValueType(code=AberdeenVenue, label=AberdeenVenue), "
                + "listItems=null), venueDundee=null, venueEdinburgh=null, "
                + "hearingDocType=ETL Test, hearingDocETCL=null, roomOrNoRoom=null, "
                + "docMarkUp=null, bfDateCollection=null, clerkResponsible=null, "
                + "reportType=Brought Forward Report, documentName=ETL Test, "
                + "showAll=null, localReportsSummaryHdr=null, "
                + "localReportsSummary=null, localReportsSummaryHdr2=null, "
                + "localReportsSummary2=null, localReportsDetailHdr=null,"
                + " localReportsDetail=null, managingOffice=Leeds)";
        listingDetails.getCaseData().setHearingDocType("ETL Test");
        listingDetails.getCaseData().setManagingOffice("Leeds");
        ListingData listingData = listingService.listingCaseCreation(listingDetails);
        assertEquals(result, listingData.toString());
        listingDetails.getCaseData().setHearingDocType(null);
    }

    @Test
    void listingCaseCreationWithReportType() {
        String result = "ListingData(tribunalCorrespondenceAddress=null,"
                + " tribunalCorrespondenceTelephone=null, "
                + "tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, "
                + "tribunalCorrespondenceEmail=null,"
                + " reportDate=null, hearingDateType=Single,"
                + " listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, listingVenue=DynamicFixedListType(value=DynamicValueType(code=Aberdeen, "
                + "label=Aberdeen), listItems=null), listingVenueScotland=null, listingCollection=[], "
                + "listingVenueOfficeGlas=null, "
                + "listingVenueOfficeAber=null, "
                + "venueGlasgow=null, "
                + "venueAberdeen=DynamicFixedListType(value=DynamicValueType(code=AberdeenVenue, label=AberdeenVenue), "
                + "listItems=null), "
                + "venueDundee=null, "
                + "venueEdinburgh=null, "
                + "hearingDocType=null, hearingDocETCL=null, roomOrNoRoom=null, "
                + "docMarkUp=null,"
                + " bfDateCollection=null, clerkResponsible=null, "
                + "reportType=Brought Forward Report,"
                + " documentName=Brought Forward Report, showAll=null, localReportsSummaryHdr=null,"
                + " localReportsSummary=null, "
                + "localReportsSummaryHdr2=null, localReportsSummary2=null, "
                + "localReportsDetailHdr=null, localReportsDetail=null, managingOffice=Leeds)";
        ListingData listingData = listingService.listingCaseCreation(listingDetails);
        assertEquals(result, listingData.toString());
    }

    @Test
    void listingCaseCreationWithoutDocumentName() {
        String result = "ListingData(tribunalCorrespondenceAddress=null, "
                + "tribunalCorrespondenceTelephone=null,"
                + " tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, "
                + "reportDate=null, hearingDateType=Single, "
                + "listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, "
                + "listingVenue=DynamicFixedListType(value=DynamicValueType(code=Aberdeen, "
                + "label=Aberdeen), listItems=null), "
                + "listingVenueScotland=null, listingCollection=[], "
                + "listingVenueOfficeGlas=null,"
                + " listingVenueOfficeAber=null, "
                + "venueGlasgow=null, "
                + "venueAberdeen=DynamicFixedListType("
                + "value=DynamicValueType(code=AberdeenVenue, label=AberdeenVenue), "
                + "listItems=null), venueDundee=null, "
                + "venueEdinburgh=null, "
                + "hearingDocType=null, hearingDocETCL=null, roomOrNoRoom=null, docMarkUp=null,"
                + " bfDateCollection=null, clerkResponsible=null, "
                + "reportType=null, documentName=Missing document name, "
                + "showAll=null, localReportsSummaryHdr=null, "
                + "localReportsSummary=null, localReportsSummaryHdr2=null, "
                + "localReportsSummary2=null, localReportsDetailHdr=null, localReportsDetail=null, "
                + "managingOffice=Leeds)";
        listingDetails.getCaseData().setReportType(null);
        listingDetails.getCaseData().setManagingOffice("Leeds");
        ListingData listingData = listingService.listingCaseCreation(listingDetails);
        assertEquals(result, listingData.toString());
        listingDetails.getCaseData().setReportType("Brought Forward Report");
    }

    @Test
    void processListingHearingsRequestAberdeen() throws IOException {
        final String result = "ListingData(tribunalCorrespondenceAddress=null, "
                + "tribunalCorrespondenceTelephone=null, tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, "
                + "reportDate=null, hearingDateType=Single, listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, "
                + "listingVenue=DynamicFixedListType(value=DynamicValueType(code=Aberdeen, label=Aberdeen), "
                + "listItems=null), listingVenueScotland=null, "
                + "listingCollection=[ListingTypeItem(id=123, "
                + "value=ListingType(causeListDate=12 December 2019, "
                + "causeListTime=12:11, causeListVenue=Aberdeen, "
                + "elmoCaseReference=4210000/2019, jurisdictionCodesList=ABC, "
                + "hearingType=Preliminary Hearing, positionType=Awaiting ET3, "
                + "hearingJudgeName= , hearingEEMember= , hearingERMember= , "
                + "hearingClerk=Clerk, hearingDay=1 of 3, claimantName=RYAN AIR LTD, claimantTown= , "
                + "claimantRepresentative= , respondent= , respondentTown= , "
                + "respondentRepresentative= , estHearingLength=2 hours, hearingPanel= , "
                + "hearingRoom=Tribunal 4, respondentOthers= , hearingNotes= , "
                + "judicialMediation= , hearingFormat= , hearingReadingDeliberationMembersChambers= )), "
                + "ListingTypeItem(id=124, value=ListingType(causeListDate=12 December 2019, "
                + "causeListTime=12:11, causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, "
                + "jurisdictionCodesList=ABC, hearingType=Preliminary "
                + "Hearing, positionType=Awaiting ET3, "
                + "hearingJudgeName= , hearingEEMember= , hearingERMember= , "
                + "hearingClerk=Clerk1, hearingDay=3 of 3, "
                + "claimantName=RYAN AIR LTD, claimantTown= , "
                + "claimantRepresentative= , respondent= , respondentTown= , "
                + "respondentRepresentative= , estHearingLength=2 hours, hearingPanel= , "
                + "hearingRoom=Tribunal 5, respondentOthers= , hearingNotes= , "
                + "judicialMediation= , hearingFormat= , "
                + "hearingReadingDeliberationMembersChambers= )), ListingTypeItem(id=124, "
                + "value=ListingType(causeListDate=12 December 2019, causeListTime=12:11, "
                + "causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, "
                + "jurisdictionCodesList=ABC, hearingType=Preliminary Hearing, "
                + "positionType=Awaiting ET3, hearingJudgeName= , hearingEEMember= , "
                + "hearingERMember= , hearingClerk=Clerk3, hearingDay=1 of 1, "
                + "claimantName=RYAN AIR LTD, claimantTown= , claimantRepresentative= , "
                + "respondent= , respondentTown= , respondentRepresentative= , "
                + "estHearingLength= , hearingPanel= , hearingRoom=Tribunal 5, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , "
                + "hearingFormat= , hearingReadingDeliberationMembersChambers= ))], "
                + "listingVenueOfficeGlas=null, listingVenueOfficeAber=null, "
                + "venueGlasgow=null, venueAberdeen=null, venueDundee=null, venueEdinburgh=null, "
                + "hearingDocType=null, hearingDocETCL=null, roomOrNoRoom=null, "
                + "docMarkUp=null, bfDateCollection=null, clerkResponsible=null, "
                + "reportType=Brought Forward Report, documentName=null, "
                + "showAll=null, localReportsSummaryHdr=null, localReportsSummary=null, localReportsSummaryHdr2=null, "
                + "localReportsSummary2=null, localReportsDetailHdr=null, "
                + "localReportsDetail=null, managingOffice=Aberdeen)";
        listingDetails.setCaseTypeId(SCOTLAND_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.ABERDEEN.getOfficeName());
        submitEvents.get(0).getCaseData().setClaimantCompany("RYAN AIR LTD");
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingDataResult = listingService
                .processListingHearingsRequest(listingDetails, "authToken");
        assertEquals(result, listingDataResult.toString());
    }

    @Test
    void testVenueNotFound() {
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate("2019-12-12T12:11:00.000");
        dateListedType.setHearingVenueDayScotland("Glasgow");
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setId("123");
        dateListedTypeItem.setValue(dateListedType);
        assertFalse(listingService.isListingVenueValid(listingDetails.getCaseData(),
                dateListedTypeItem, ENGLANDWALES_CASE_TYPE_ID, "123"));
    }

    @Test
    void testVenueScotlandAllOffices() {
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate("2019-12-12T12:11:00.000");
        dateListedType.setHearingVenueDayScotland("Glasgow");
        dateListedType.setHearingGlasgow(new DynamicFixedListType("GlasgowVenue"));
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setId("123");
        dateListedTypeItem.setValue(dateListedType);
        listingDetails.getCaseData().setListingVenue(new DynamicFixedListType("All"));
        listingDetails.getCaseData().setManagingOffice("All");
        assertTrue(listingService.isListingVenueValid(listingDetails.getCaseData(),
                dateListedTypeItem, SCOTLAND_CASE_TYPE_ID, "123"));
    }

    @Test
    void processListingHearingsRequestGlasgow() throws IOException {
        listingDetails.setCaseTypeId(SCOTLAND_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setVenueAberdeen(null);
        listingDetails.getCaseData().setVenueGlasgow(new DynamicFixedListType("GlasgowVenue"));
        listingDetails.getCaseData().setListingVenue(new DynamicFixedListType("Glasgow"));
        listingDetails.getCaseData().setManagingOffice("Glasgow");
        String result = "ListingData(tribunalCorrespondenceAddress=null, "
                + "tribunalCorrespondenceTelephone=null, tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, "
                + "reportDate=null, hearingDateType=Single, listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, listingVenue=DynamicFixedListType(value=DynamicValueType(code=Glasgow, "
                + "label=Glasgow), listItems=null), listingVenueScotland=null, listingCollection=[], "
                + "listingVenueOfficeGlas=null, listingVenueOfficeAber=null, "
                + "venueGlasgow=null, venueAberdeen=null, venueDundee=null, "
                + "venueEdinburgh=null, hearingDocType=null, hearingDocETCL=null, "
                + "roomOrNoRoom=null, docMarkUp=null, bfDateCollection=null, clerkResponsible=null, "
                + "reportType=Brought Forward Report, documentName=null, showAll=null, "
                + "localReportsSummaryHdr=null, localReportsSummary=null, "
                + "localReportsSummaryHdr2=null, localReportsSummary2=null, "
                + "localReportsDetailHdr=null, localReportsDetail=null, managingOffice=Glasgow)";
        submitEvents.get(0).getCaseData().setClaimantCompany("RYAN AIR LTD");
        when(ccdClient.retrieveCasesVenueAndDateElasticSearch(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingDataResult = listingService.processListingHearingsRequest(listingDetails, "authToken");
        assertEquals(result, listingDataResult.toString());
    }

    @Test
    void processListingHearingsRequestEdinburgh() throws IOException {
        listingDetails.setCaseTypeId(SCOTLAND_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setVenueAberdeen(null);
        listingDetails.getCaseData().setVenueEdinburgh(new DynamicFixedListType("EdinburghVenue"));
        listingDetails.getCaseData().setListingVenue(new DynamicFixedListType("Edinburgh"));
        listingDetails.getCaseData().setManagingOffice("Edinburgh");
        String result = "ListingData(tribunalCorrespondenceAddress=null, "
                + "tribunalCorrespondenceTelephone=null, tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, "
                + "reportDate=null, hearingDateType=Single, listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, listingVenue=DynamicFixedListType("
                + "value=DynamicValueType(code=Edinburgh, label=Edinburgh), listItems=null), "
                + "listingVenueScotland=null, listingCollection=[], "
                + "listingVenueOfficeGlas=null, listingVenueOfficeAber=null, "
                + "venueGlasgow=null, venueAberdeen=null, venueDundee=null, "
                + "venueEdinburgh=null, hearingDocType=null, hearingDocETCL=null, "
                + "roomOrNoRoom=null, docMarkUp=null, bfDateCollection=null, "
                + "clerkResponsible=null, reportType=Brought Forward Report, documentName=null, showAll=null, "
                + "localReportsSummaryHdr=null, localReportsSummary=null,"
                + " localReportsSummaryHdr2=null, localReportsSummary2=null, "
                + "localReportsDetailHdr=null, "
                + "localReportsDetail=null, managingOffice=Edinburgh)";
        submitEvents.get(0).getCaseData().setClaimantCompany("RYAN AIR LTD");
        when(ccdClient.retrieveCasesVenueAndDateElasticSearch(anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingDataResult = listingService.processListingHearingsRequest(listingDetails, "authToken");
        assertEquals(result, listingDataResult.toString());
    }

    @Test
    void processListingHearingsRequestDundee() throws IOException {
        listingDetails.setCaseTypeId(SCOTLAND_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setVenueAberdeen(null);
        listingDetails.getCaseData().setVenueDundee(new DynamicFixedListType("DundeeVenue"));
        listingDetails.getCaseData().setListingVenue(new DynamicFixedListType("Dundee"));
        final String result = "ListingData(tribunalCorrespondenceAddress=null, "
                + "tribunalCorrespondenceTelephone=null, tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, "
                + "reportDate=null, hearingDateType=Single, "
                + "listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, listingVenue=DynamicFixedListType("
                + "value=DynamicValueType(code=Dundee, label=Dundee), listItems=null), "
                + "listingVenueScotland=null, listingCollection=[], "
                + "listingVenueOfficeGlas=null, listingVenueOfficeAber=null, "
                + "venueGlasgow=null, venueAberdeen=null, venueDundee=null, "
                + "venueEdinburgh=null, hearingDocType=null, hearingDocETCL=null, "
                + "roomOrNoRoom=null, docMarkUp=null, bfDateCollection=null, clerkResponsible=null, "
                + "reportType=Brought Forward Report, documentName=null, showAll=null, "
                + "localReportsSummaryHdr=null, localReportsSummary=null, "
                + "localReportsSummaryHdr2=null, localReportsSummary2=null, "
                + "localReportsDetailHdr=null, localReportsDetail=null, managingOffice=Dundee)";
        listingDetails.getCaseData().setManagingOffice("Dundee");
        submitEvents.get(0).getCaseData().setClaimantCompany("RYAN AIR LTD");
        when(ccdClient.retrieveCasesVenueAndDateElasticSearch(anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingDataResult = listingService.processListingHearingsRequest(listingDetails, "authToken");
        assertEquals(result, listingDataResult.toString());
    }

    @Test
    void processListingHearingsRequestNonScottish() throws IOException {
        listingDetails.getCaseData().setVenueAberdeen(null);
        listingDetails.getCaseData().setListingVenue(new DynamicFixedListType("Leeds"));
        listingDetails.getCaseData().setManagingOffice("Leeds");

        String result = "ListingData(tribunalCorrespondenceAddress=null, tribunalCorrespondenceTelephone=null, "
                + "tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, "
                + "reportDate=null, hearingDateType=Single, listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, listingVenue=DynamicFixedListType("
                + "value=DynamicValueType(code=Leeds, label=Leeds), listItems=null), "
                + "listingVenueScotland=null, listingCollection=[], "
                + "listingVenueOfficeGlas=null, listingVenueOfficeAber=null, "
                + "venueGlasgow=null, venueAberdeen=null, venueDundee=null, "
                + "venueEdinburgh=null, hearingDocType=null, hearingDocETCL=null, "
                + "roomOrNoRoom=null, docMarkUp=null, bfDateCollection=null, "
                + "clerkResponsible=null, reportType=Brought Forward Report, documentName=null, showAll=null, "
                + "localReportsSummaryHdr=null, localReportsSummary=null, "
                + "localReportsSummaryHdr2=null, localReportsSummary2=null, "
                + "localReportsDetailHdr=null, localReportsDetail=null, managingOffice=Leeds)";
        submitEvents.get(0).getCaseData().setClaimantCompany("RYAN AIR LTD");
        when(ccdClient.retrieveCasesVenueAndDateElasticSearch(anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingDataResult = listingService.processListingHearingsRequest(listingDetails, "authToken");
        assertEquals(result, listingDataResult.toString());
    }

    @Test
    void processListingHearingsRequestAberdeenWithValidHearingType() throws IOException {
        final String result = "ListingData(tribunalCorrespondenceAddress=null, tribunalCorrespondenceTelephone=null, "
                + "tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, "
                + "reportDate=null, hearingDateType=Single, "
                + "listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, listingVenue=DynamicFixedListType("
                + "value=DynamicValueType(code=Aberdeen, label=Aberdeen), listItems=null), "
                + "listingVenueScotland=null, listingCollection=[ListingTypeItem"
                + "(id=123, value=ListingType(causeListDate=12 December 2019, causeListTime=12:11, "
                + "causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, "
                + "jurisdictionCodesList=ABC, hearingType=Valid Hearing, positionType=Awaiting ET3, "
                + "hearingJudgeName= , hearingEEMember= , hearingERMember= , "
                + "hearingClerk=Clerk, hearingDay=1 of 3, claimantName=RYAN AIR LTD, claimantTown= , "
                + "claimantRepresentative= , respondent= , respondentTown= , "
                + "respondentRepresentative= , estHearingLength=2 hours, hearingPanel= , "
                + "hearingRoom=Tribunal 4, respondentOthers= , hearingNotes= , "
                + "judicialMediation= , hearingFormat= , hearingReadingDeliberationMembersChambers= )), "
                + "ListingTypeItem(id=124, value=ListingType(causeListDate=12 December 2019, "
                + "causeListTime=12:11, causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, "
                + "jurisdictionCodesList=ABC, hearingType=Valid Hearing, "
                + "positionType=Awaiting ET3, hearingJudgeName= , "
                + "hearingEEMember= , hearingERMember= , "
                + "hearingClerk=Clerk1, hearingDay=3 of 3, claimantName=RYAN AIR LTD, "
                + "claimantTown= , claimantRepresentative= , respondent= , respondentTown= , "
                + "respondentRepresentative= , estHearingLength=2 hours, "
                + "hearingPanel= , hearingRoom=Tribunal 5, respondentOthers= , hearingNotes= , "
                + "judicialMediation= , hearingFormat= , hearingReadingDeliberationMembersChambers= )), "
                + "ListingTypeItem(id=124, value=ListingType(causeListDate=12 December 2019, "
                + "causeListTime=12:11, causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, "
                + "jurisdictionCodesList=ABC, hearingType=Preliminary Hearing, "
                + "positionType=Awaiting ET3, hearingJudgeName= , hearingEEMember= , "
                + "hearingERMember= , hearingClerk=Clerk3, hearingDay=1 of 1, "
                + "claimantName=RYAN AIR LTD, claimantTown= , claimantRepresentative= , respondent= , "
                + "respondentTown= , respondentRepresentative= , "
                + "estHearingLength= , hearingPanel= , hearingRoom=Tribunal 5, respondentOthers= , "
                + "hearingNotes= , judicialMediation= , hearingFormat= , "
                + "hearingReadingDeliberationMembersChambers= ))], listingVenueOfficeGlas=null, "
                + "listingVenueOfficeAber=null, venueGlasgow=null, venueAberdeen=null, "
                + "venueDundee=null, venueEdinburgh=null, hearingDocType=ETCL - Cause List, "
                + "hearingDocETCL=Public, roomOrNoRoom=null, docMarkUp=null, bfDateCollection=null, "
                + "clerkResponsible=null, reportType=Brought Forward Report, documentName=null, "
                + "showAll=null, localReportsSummaryHdr=null, localReportsSummary=null, "
                + "localReportsSummaryHdr2=null, localReportsSummary2=null, "
                + "localReportsDetailHdr=null, localReportsDetail=null, managingOffice=Aberdeen)";
        submitEvents.get(0).getCaseData().setClaimantCompany("RYAN AIR LTD");
        submitEvents.get(0).getCaseData().getHearingCollection().get(0).getValue().setHearingType("Valid Hearing");
        listingDetails.getCaseData().setHearingDocType(HEARING_DOC_ETCL);
        listingDetails.getCaseData().setHearingDocETCL(HEARING_ETCL_PUBLIC);
        listingDetails.setCaseTypeId(SCOTLAND_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.ABERDEEN.getOfficeName());
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingDataResult = listingService.processListingHearingsRequest(listingDetails,
                "authToken");
        assertEquals(result, listingDataResult.toString());
    }

    @Test
    void processListingHearingsRequestAberdeenWithInValidHearingType() throws IOException {
        final String result = "ListingData(tribunalCorrespondenceAddress=null, "
                + "tribunalCorrespondenceTelephone=null, tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, "
                + "reportDate=null, hearingDateType=Single, listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, listingVenue=DynamicFixedListType("
                + "value=DynamicValueType(code=Aberdeen, label=Aberdeen), listItems=null), "
                + "listingVenueScotland=null, listingCollection=[], listingVenueOfficeGlas=null, "
                + "listingVenueOfficeAber=null, "
                + "venueGlasgow=null, venueAberdeen=null, venueDundee=null, venueEdinburgh=null, "
                + "hearingDocType=ETCL - Cause List, hearingDocETCL=Public, roomOrNoRoom=null, "
                + "docMarkUp=null, bfDateCollection=null, clerkResponsible=null, "
                + "reportType=Brought Forward Report, documentName=null, showAll=null, "
                + "localReportsSummaryHdr=null, localReportsSummary=null, localReportsSummaryHdr2=null, "
                + "localReportsSummary2=null, localReportsDetailHdr=null, "
                + "localReportsDetail=null, managingOffice=Aberdeen)";
        submitEvents.get(0).getCaseData().setClaimantCompany("RYAN AIR LTD");
        submitEvents.get(0).getCaseData().getHearingCollection().get(0)
                .getValue().setHearingType(HEARING_TYPE_JUDICIAL_MEDIATION);
        listingDetails.getCaseData().setHearingDocType(HEARING_DOC_ETCL);
        listingDetails.getCaseData().setHearingDocETCL(HEARING_ETCL_PUBLIC);
        listingDetails.getCaseData().setManagingOffice("Aberdeen");
        when(ccdClient.retrieveCasesVenueAndDateElasticSearch(anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingDataResult = listingService.processListingHearingsRequest(listingDetails,
                "authToken");
        assertEquals(result, listingDataResult.toString());
    }

    @Test
    void processListingHearingsRequestAberdeenWithPrivateHearingType() throws IOException {
        final String result = "ListingData(tribunalCorrespondenceAddress=null, "
                + "tribunalCorrespondenceTelephone=null, tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, reportDate=null, "
                + "hearingDateType=Single, listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, listingVenue=DynamicFixedListType(value=DynamicValueType"
                + "(code=Aberdeen, label=Aberdeen), listItems=null), listingVenueScotland=null, "
                + "listingCollection=[], listingVenueOfficeGlas=null, listingVenueOfficeAber=null, "
                + "venueGlasgow=null, venueAberdeen=null, venueDundee=null, venueEdinburgh=null, "
                + "hearingDocType=ETCL - Cause List, hearingDocETCL=Press List, "
                + "roomOrNoRoom=null, docMarkUp=null, bfDateCollection=null, clerkResponsible=null, "
                + "reportType=Brought Forward Report, documentName=null, showAll=null, "
                + "localReportsSummaryHdr=null, localReportsSummary=null, localReportsSummaryHdr2=null, "
                + "localReportsSummary2=null, localReportsDetailHdr=null, "
                + "localReportsDetail=null, managingOffice=Aberdeen)";
        submitEvents.get(0).getCaseData()
                .setClaimantCompany("RYAN AIR LTD");
        submitEvents.get(0).getCaseData().getHearingCollection().get(0)
                .getValue().setHearingType(HEARING_TYPE_PERLIMINARY_HEARING);
        submitEvents.get(0).getCaseData().getHearingCollection().get(0)
                .getValue().setHearingPublicPrivate(HEARING_TYPE_PRIVATE);
        listingDetails.getCaseData().setHearingDocType(HEARING_DOC_ETCL);
        listingDetails.getCaseData().setHearingDocETCL(HEARING_ETCL_PRESS_LIST);
        listingDetails.getCaseData().setManagingOffice("Aberdeen");
        when(ccdClient.retrieveCasesVenueAndDateElasticSearch(anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingDataResult = listingService.processListingHearingsRequest(listingDetails,
                "authToken");
        assertEquals(result, listingDataResult.toString());
    }

    @Test
    void processListingHearingsRequestAberdeenWithALL() throws IOException {
        final String result = "ListingData(tribunalCorrespondenceAddress=null, tribunalCorrespondenceTelephone=null, "
                + "tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, "
                + "reportDate=null, hearingDateType=Single,"
                + " listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, listingVenue=DynamicFixedListType("
                + "value=DynamicValueType(code=Aberdeen, label=Aberdeen), "
                + "listItems=null), listingVenueScotland=null, listingCollection=["
                + "ListingTypeItem(id=123, value=ListingType(causeListDate=12 "
                + "December 2019, causeListTime=12:11,"
                + " causeListVenue=Aberdeen, " + "elmoCaseReference=4210000/2019, jurisdictionCodesList=ABC, "
                + "hearingType=Preliminary Hearing, positionType=Awaiting ET3, hearingJudgeName= ,"
                + " hearingEEMember= , "
                + "hearingERMember= , hearingClerk=Clerk, hearingDay=1 of 3, claimantName=RYAN AIR LTD, "
                + "claimantTown= , "
                + "claimantRepresentative= , " + "respondent= , respondentTown= , respondentRepresentative= , "
                + "estHearingLength=2 hours, hearingPanel= , hearingRoom=Tribunal 4, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , hearingFormat= , "
                + "hearingReadingDeliberationMembersChambers= )), " + "ListingTypeItem(id=124, "
                + "value=ListingType(causeListDate=12 December 2019, causeListTime=12:11, causeListVenue=Aberdeen, "
                + "elmoCaseReference=4210000/2019, jurisdictionCodesList=ABC, hearingType=Preliminary Hearing, "
                + "positionType=Awaiting ET3, hearingJudgeName= , hearingEEMember= , "
                + "hearingERMember= , hearingClerk=Clerk1, hearingDay=3 of 3, claimantName=RYAN AIR LTD, "
                + "claimantTown= , claimantRepresentative= , "
                + "respondent= , respondentTown= , respondentRepresentative= , estHearingLength=2 hours, "
                + "hearingPanel= , hearingRoom=Tribunal 5, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , hearingFormat= , "
                + "hearingReadingDeliberationMembersChambers= )), "
                + "ListingTypeItem(id=124, value=ListingType(causeListDate=12 December 2019, "
                + "causeListTime=12:11, "
                + "causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, jurisdictionCodesList=ABC, "
                + "hearingType=Preliminary Hearing, positionType=Awaiting ET3, "
                + "hearingJudgeName= , hearingEEMember= , hearingERMember= , hearingClerk=Clerk3, "
                + "hearingDay=1 of 1, claimantName=RYAN AIR LTD, claimantTown= , "
                + "claimantRepresentative= , respondent= , respondentTown= , respondentRepresentative= , "
                + "estHearingLength= , hearingPanel= , hearingRoom=Tribunal 5, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , hearingFormat= , "
                + "hearingReadingDeliberationMembersChambers= ))], " + "listingVenueOfficeGlas=null, "
                + "listingVenueOfficeAber=null, venueGlasgow=null," + " venueAberdeen=null, venueDundee=null, "
                + "venueEdinburgh=null, " + "hearingDocType=null, hearingDocETCL=null, "
                + "roomOrNoRoom=null, docMarkUp=null, "
                + "bfDateCollection=null, clerkResponsible=null, "
                + "reportType=Brought Forward Report, documentName=null,"
                + " showAll=null, localReportsSummaryHdr=null, "
                + "localReportsSummary=null, localReportsSummaryHdr2=null, "
                + "localReportsSummary2=null, localReportsDetailHdr=null, "
                + "localReportsDetail=null, managingOffice=Aberdeen)";
        submitEvents.get(0).getCaseData().setClaimantCompany("RYAN AIR LTD");
        listingDetails.getCaseData().setVenueAberdeen(new DynamicFixedListType(ALL_VENUES));
        listingDetails.setCaseTypeId(SCOTLAND_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.ABERDEEN.getOfficeName());
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingDataResult = listingService.processListingHearingsRequest(listingDetails,
                "authToken");
        assertEquals(result, listingDataResult.toString());
    }

    @Test
    void processListingHearingsRequestDateRange() throws IOException {
        final String result = "ListingData(tribunalCorrespondenceAddress=null, "
                + "tribunalCorrespondenceTelephone=null, tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, "
                + "reportDate=null, hearingDateType=Range, listingDate=null, listingDateFrom=2019-12-09, "
                + "listingDateTo=2019-12-12, listingVenue=DynamicFixedListType"
                + "(value=DynamicValueType(code=Aberdeen, label=Aberdeen), listItems=null), "
                + "listingVenueScotland=null, listingCollection="
                + "[ListingTypeItem(id=124, value=ListingType(causeListDate=10 December 2019, "
                + "causeListTime=12:11, causeListVenue=Aberdeen, "
                + "elmoCaseReference=4210000/2019, jurisdictionCodesList=ABC, hearingType=Preliminary "
                + "Hearing, positionType=Awaiting ET3, hearingJudgeName= , hearingEEMember= , "
                + "hearingERMember= , hearingClerk=Clerk, hearingDay=2 of 3, "
                + "claimantName=RYAN AIR LTD, claimantTown= , claimantRepresentative= , "
                + "respondent= , respondentTown= , respondentRepresentative= , "
                + "estHearingLength=2 hours, hearingPanel= , hearingRoom=Tribunal 4, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , "
                + "hearingFormat= , hearingReadingDeliberationMembersChambers= )), "
                + "ListingTypeItem(id=123, value=ListingType(causeListDate=12 December 2019, "
                + "causeListTime=12:11, causeListVenue=Aberdeen, "
                + "elmoCaseReference=4210000/2019, jurisdictionCodesList=ABC, hearingType=Preliminary Hearing, "
                + "positionType=Awaiting ET3, hearingJudgeName= , hearingEEMember= , "
                + "hearingERMember= , hearingClerk=Clerk, hearingDay=1 of 3, "
                + "claimantName=RYAN AIR LTD, claimantTown= , claimantRepresentative= , "
                + "respondent= , respondentTown= , respondentRepresentative= , estHearingLength=2 hours, "
                + "hearingPanel= , hearingRoom=Tribunal 4, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , hearingFormat= , "
                + "hearingReadingDeliberationMembersChambers= )), ListingTypeItem(id=124, "
                + "value=ListingType(causeListDate=12 December 2019, causeListTime=12:11, "
                + "causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, jurisdictionCodesList=ABC, "
                + "hearingType=Preliminary Hearing, positionType=Awaiting ET3, hearingJudgeName= , "
                + "hearingEEMember= , hearingERMember= , hearingClerk=Clerk1, "
                + "hearingDay=3 of 3, claimantName=RYAN AIR LTD, claimantTown= , "
                + "claimantRepresentative= , respondent= , respondentTown= , respondentRepresentative= , "
                + "estHearingLength=2 hours, hearingPanel= , hearingRoom=Tribunal 5, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , hearingFormat= , "
                + "hearingReadingDeliberationMembersChambers= )), ListingTypeItem(id=124, "
                + "value=ListingType(causeListDate=12 December 2019, causeListTime=12:11, "
                + "causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, jurisdictionCodesList=ABC, "
                + "hearingType=Preliminary Hearing, positionType=Awaiting ET3, "
                + "hearingJudgeName= , hearingEEMember= , hearingERMember= , hearingClerk=Clerk3, "
                + "hearingDay=1 of 1, claimantName=RYAN AIR LTD, claimantTown= , "
                + "claimantRepresentative= , respondent= , respondentTown= , respondentRepresentative= , "
                + "estHearingLength= , hearingPanel= , "
                + "hearingRoom=Tribunal 5, respondentOthers= , hearingNotes= , judicialMediation= , "
                + "hearingFormat= , hearingReadingDeliberationMembersChambers= ))], "
                + "listingVenueOfficeGlas=null, listingVenueOfficeAber=null, venueGlasgow=null, "
                + "venueAberdeen=null, venueDundee=null, venueEdinburgh=null, "
                + "hearingDocType=null, hearingDocETCL=null, roomOrNoRoom=null, docMarkUp=null, "
                + "bfDateCollection=null, clerkResponsible=null, reportType=Brought Forward Report, "
                + "documentName=null, showAll=null, localReportsSummaryHdr=null, "
                + "localReportsSummary=null, localReportsSummaryHdr2=null, localReportsSummary2=null, "
                + "localReportsDetailHdr=null, localReportsDetail=null, managingOffice=Aberdeen)";
        listingDetailsRange.getCaseData().setManagingOffice(TribunalOffice.ABERDEEN.getOfficeName());
        listingDetailsRange.getCaseData().setVenueAberdeen(new DynamicFixedListType(
                TribunalOffice.ABERDEEN.getOfficeName()));
        submitEvents.get(0).getCaseData().setClaimantCompany("RYAN AIR LTD");
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingDataResult = listingService.processListingHearingsRequest(listingDetailsRange,
                "authToken");
        assertEquals(result, listingDataResult.toString());
    }

    @Test
    void processListingHearingsRequestSingleDate() throws IOException {
        final String result = "ListingData(tribunalCorrespondenceAddress=null, "
                + "tribunalCorrespondenceTelephone=null, tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, reportDate=null, "
                + "hearingDateType=Single, listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, listingVenue=DynamicFixedListType(value=DynamicValueType("
                + "code=Aberdeen, label=Aberdeen), listItems=null), listingVenueScotland=null, listingCollection=["
                + "ListingTypeItem(id=123, value=ListingType(causeListDate=12 December 2019, "
                + "causeListTime=12:11, causeListVenue=Aberdeen, "
                + "elmoCaseReference=4210000/2019, jurisdictionCodesList=ABC, "
                + "hearingType=Preliminary Hearing, positionType=Awaiting ET3, hearingJudgeName= , "
                + "hearingEEMember= , "
                + "hearingERMember= , hearingClerk=Clerk, hearingDay=1 of 3, "
                + "claimantName=RYAN AIR LTD, claimantTown= , claimantRepresentative= , "
                + "respondent= , respondentTown= , respondentRepresentative= , "
                + "estHearingLength=2 hours, hearingPanel= , hearingRoom=Tribunal 4, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , "
                + "hearingFormat= , hearingReadingDeliberationMembersChambers= )), "
                + "ListingTypeItem(id=124, value=ListingType(causeListDate=12 December 2019, "
                + "causeListTime=12:11, causeListVenue=Aberdeen, "
                + "elmoCaseReference=4210000/2019, jurisdictionCodesList=ABC, "
                + "hearingType=Preliminary Hearing, positionType=Awaiting ET3, "
                + "hearingJudgeName= , hearingEEMember= , "
                + "hearingERMember= , hearingClerk=Clerk1, hearingDay=3 of 3, "
                + "claimantName=RYAN AIR LTD, claimantTown= , claimantRepresentative= , "
                + "respondent= , respondentTown= , respondentRepresentative= , "
                + "estHearingLength=2 hours, hearingPanel= , hearingRoom=Tribunal 5, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , hearingFormat= , "
                + "hearingReadingDeliberationMembersChambers= )), ListingTypeItem(id=124, "
                + "value=ListingType(causeListDate=12 December 2019, causeListTime=12:11, "
                + "causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, jurisdictionCodesList=ABC, "
                + "hearingType=Preliminary Hearing, positionType=Awaiting ET3, "
                + "hearingJudgeName= , hearingEEMember= , hearingERMember= , hearingClerk=Clerk3, "
                + "hearingDay=1 of 1, claimantName=RYAN AIR LTD, claimantTown= , "
                + "claimantRepresentative= , respondent= , respondentTown= , respondentRepresentative= "
                + ", estHearingLength= , hearingPanel= , hearingRoom=Tribunal 5, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , hearingFormat= "
                + ", hearingReadingDeliberationMembersChambers= ))], "
                + "listingVenueOfficeGlas=null, listingVenueOfficeAber=null, venueGlasgow=null, "
                + "venueAberdeen=null, venueDundee=null, venueEdinburgh=null, "
                + "hearingDocType=null, hearingDocETCL=null, roomOrNoRoom=null, docMarkUp=null, "
                + "bfDateCollection=null, clerkResponsible=null, reportType=Brought Forward Report, "
                + "documentName=null, showAll=null, localReportsSummaryHdr=null, "
                + "localReportsSummary=null, localReportsSummaryHdr2=null, localReportsSummary2=null, "
                + "localReportsDetailHdr=null, localReportsDetail=null, managingOffice=Aberdeen)";
        submitEvents.get(0).getCaseData().setClaimantCompany("RYAN AIR LTD");
        listingDetails.getCaseData().setVenueAberdeen(new DynamicFixedListType(ALL_VENUES));
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.ABERDEEN.getOfficeName());
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingDataResult = listingService.processListingHearingsRequest(listingDetails, "authToken");
        assertEquals(result, listingDataResult.toString());
    }

    @Test
    void processListingHearingsRequestRangeAndAllVenues() throws IOException {
        final String result = "ListingData(tribunalCorrespondenceAddress=null, "
                + "tribunalCorrespondenceTelephone=null, tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, "
                + "reportDate=null, hearingDateType=Range, listingDate=null, "
                + "listingDateFrom=2019-12-09, listingDateTo=2019-12-12, "
                + "listingVenue=DynamicFixedListType(value=DynamicValueType("
                + "code=All, label=All), listItems=null), "
                + "listingVenueScotland=null, listingCollection=[ListingTypeItem(id=124, "
                + "value=ListingType(causeListDate=10 December 2019, causeListTime=12:11, "
                + "causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, j"
                + "urisdictionCodesList=ABC, hearingType=Preliminary Hearing, "
                + "positionType=Awaiting ET3, hearingJudgeName= , hearingEEMember= , "
                + "hearingERMember= , hearingClerk=Clerk, hearingDay=2 of 3, "
                + "claimantName=RYAN AIR LTD, claimantTown= , claimantRepresentative= , "
                + "respondent= , respondentTown= , respondentRepresentative= , "
                + "estHearingLength=2 hours, hearingPanel= , hearingRoom=Tribunal 4, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , "
                + "hearingFormat= , hearingReadingDeliberationMembersChambers= )), "
                + "ListingTypeItem(id=123, value=ListingType(causeListDate=12 "
                + "December 2019, causeListTime=12:11, "
                + "causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, "
                + "jurisdictionCodesList=ABC, hearingType=Preliminary Hearing, "
                + "positionType=Awaiting ET3, hearingJudgeName= , hearingEEMember= , "
                + "hearingERMember= , hearingClerk=Clerk, hearingDay=1 of 3, "
                + "claimantName=RYAN AIR LTD, claimantTown= , claimantRepresentative= , "
                + "respondent= , respondentTown= , respondentRepresentative= , "
                + "estHearingLength=2 hours, hearingPanel= , hearingRoom=Tribunal 4, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , hearingFormat= , "
                + "hearingReadingDeliberationMembersChambers= )), ListingTypeItem(id=124, "
                + "value=ListingType(causeListDate=12 December 2019, causeListTime=12:11, "
                + "causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, "
                + "jurisdictionCodesList=ABC, hearingType=Preliminary Hearing, "
                + "positionType=Awaiting ET3, hearingJudgeName= , hearingEEMember= , "
                + "hearingERMember= , hearingClerk=Clerk1, hearingDay=3 of 3, "
                + "claimantName=RYAN AIR LTD, claimantTown= , claimantRepresentative= , "
                + "respondent= , respondentTown= , respondentRepresentative= , "
                + "estHearingLength=2 hours, hearingPanel= , hearingRoom=Tribunal 5, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , "
                + "hearingFormat= , hearingReadingDeliberationMembersChambers= )), ListingTypeItem(id=124, "
                + "value=ListingType(causeListDate=12 December 2019, causeListTime=12:11, "
                + "causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, "
                + "jurisdictionCodesList=ABC, hearingType=Preliminary Hearing, "
                + "positionType=Awaiting ET3, hearingJudgeName= , hearingEEMember= , "
                + "hearingERMember= , hearingClerk=Clerk3, hearingDay=1 of 1, "
                + "claimantName=RYAN AIR LTD, claimantTown= , claimantRepresentative= , "
                + "respondent= , respondentTown= , respondentRepresentative= , "
                + "estHearingLength= , hearingPanel= , hearingRoom=Tribunal 5, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , hearingFormat= , "
                + "hearingReadingDeliberationMembersChambers= ))], listingVenueOfficeGlas=null, "
                + "listingVenueOfficeAber=null, venueGlasgow=null, venueAberdeen=null, "
                + "venueDundee=null, venueEdinburgh=null, hearingDocType=null, "
                + "hearingDocETCL=null, roomOrNoRoom=null, docMarkUp=null, "
                + "bfDateCollection=null, clerkResponsible=null, reportType=Brought Forward Report, "
                + "documentName=null, showAll=null, localReportsSummaryHdr=null, "
                + "localReportsSummary=null, localReportsSummaryHdr2=null, localReportsSummary2=null, "
                + "localReportsDetailHdr=null, localReportsDetail=null, managingOffice=Aberdeen)";
        submitEvents.get(0).getCaseData().setClaimantCompany("RYAN AIR LTD");
        listingDetailsRange.getCaseData().setListingVenue(new DynamicFixedListType(ALL_VENUES));
        listingDetailsRange.getCaseData().setManagingOffice(TribunalOffice.ABERDEEN.getOfficeName());
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingDataResult = listingService.processListingHearingsRequest(listingDetailsRange, "authToken");
        assertEquals(result, listingDataResult.toString());
        int expectedHearingsCount = 4;
        assertEquals(expectedHearingsCount, listingDataResult.getListingCollection().size());
    }

    @Test
    void processListingHearings_listedDateNullOrEmpty() {
        assertThrows(Exception.class, () -> {
            submitEvents.get(0).getCaseData().getHearingCollection().get(0).getValue().getHearingDateCollection()
                    .get(1).getValue().setListedDate(null);
            submitEvents.get(0).getCaseData().getHearingCollection().get(0).getValue().getHearingDateCollection()
                    .get(1).getValue().setListedDate("");
            when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString()))
                    .thenReturn(submitEvents);
            listingService.processListingHearingsRequest(listingDetailsRange, "authToken");

        });
    }

    @Test
    @Disabled
    // listingCollection.get(0) is null so cannot proceed
    void processListingHearingsRequest_causeListDateNull() throws IOException {
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingData = listingService.processListingHearingsRequest(listingDetailsRange, "authToken");
        List<ListingTypeItem> listingCollection = listingData.getListingCollection();
        listingCollection.get(0).getValue().setCauseListDate(null);

        assertThrows(Exception.class, () ->
                listingCollection.sort(Comparator.comparing(o -> LocalDate.parse(o.getValue().getCauseListDate(),
                        CAUSE_LIST_DATE_TIME_PATTERN)))
        );
    }

    @Test
    void processListingHearings_SameDayAndTimeDifferentMonth() throws IOException {
        final String result = "ListingData(tribunalCorrespondenceAddress=null, tribunalCorrespondenceTelephone=null, "
                + "tribunalCorrespondenceFax=null, tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, "
                + "reportDate=null, hearingDateType=Range, listingDate=null, listingDateFrom=2021-01-01, "
                + "listingDateTo=2021-12-01, listingVenue=DynamicFixedListType(value=DynamicValueType(code=Aberdeen, "
                + "label=Aberdeen), listItems=null), listingVenueScotland=null, "
                + "listingCollection=[ListingTypeItem(id=124, "
                + "value=ListingType(causeListDate=01 January 2021, causeListTime=12:00, causeListVenue=Aberdeen,"
                + " elmoCaseReference=4210000/2019, jurisdictionCodesList=ABC, hearingType=Preliminary Hearing, "
                + "positionType=Awaiting ET3, hearingJudgeName= , "
                + "hearingEEMember= , hearingERMember= , "
                + "hearingClerk=Clerk, hearingDay=2 of 3, claimantName= , claimantTown= , claimantRepresentative= , "
                + "respondent= , respondentTown= , respondentRepresentative= , estHearingLength=2 hours, "
                + "hearingPanel= , hearingRoom=Tribunal 4, respondentOthers= , hearingNotes= , judicialMediation= ,"
                + " hearingFormat= , hearingReadingDeliberationMembersChambers= )), ListingTypeItem(id=123, "
                + "value=ListingType(causeListDate=01 December 2021, causeListTime=12:00, causeListVenue=Aberdeen,"
                + " elmoCaseReference=4210000/2019, jurisdictionCodesList=ABC, hearingType=Preliminary Hearing,"
                + " positionType=Awaiting ET3, hearingJudgeName= , "
                + "hearingEEMember= , hearingERMember= , "
                + "hearingClerk=Clerk, hearingDay=1 of 3, claimantName= , claimantTown= , claimantRepresentative= , "
                + "respondent= , respondentTown= , respondentRepresentative= , estHearingLength=2 hours, "
                + "hearingPanel= , hearingRoom=Tribunal 4, respondentOthers= , hearingNotes= , judicialMediation= , "
                + "hearingFormat= , hearingReadingDeliberationMembersChambers= ))], listingVenueOfficeGlas=null, "
                + "listingVenueOfficeAber=null, venueGlasgow=null, venueAberdeen=null, venueDundee=null, "
                + "venueEdinburgh=null, hearingDocType=null, hearingDocETCL=null, roomOrNoRoom=null, docMarkUp=null, "
                + "bfDateCollection=null, clerkResponsible=null, reportType=Brought Forward Report, "
                + "documentName=null, "
                + "showAll=null, localReportsSummaryHdr=null, localReportsSummary=null, localReportsSummaryHdr2=null, "
                + "localReportsSummary2=null, localReportsDetailHdr=null, localReportsDetail=null, "
                + "managingOffice=Leeds)";

        submitEvents.get(0).getCaseData().getHearingCollection().get(0).getValue().getHearingDateCollection()
                .get(0).getValue().setListedDate("2021-12-01T12:00:00.000");
        submitEvents.get(0).getCaseData().getHearingCollection().get(0).getValue().getHearingDateCollection()
                .get(1).getValue().setListedDate("2021-01-01T12:00:00.000");
        listingDetailsRange.getCaseData().setListingDateFrom("2021-01-01");
        listingDetailsRange.getCaseData().setListingDateTo("2021-12-01");
        listingDetailsRange.getCaseData().setVenueAberdeen(new DynamicFixedListType("Aberdeen"));
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingDataResult = listingService
                .processListingHearingsRequest(listingDetailsRange, "authToken");
        assertEquals(result, listingDataResult.toString());
    }

    @Test
    @Disabled // see processListingHearingsRequest_causeListDateNull
    void processListingHearingsRequestWithException() throws IOException {
        when(ccdClient.retrieveCasesVenueAndDateElasticSearch(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString())).thenThrow(new Exception(ERROR_MESSAGE));

        assertThrows(Exception.class, () ->
                listingService.processListingHearingsRequest(listingDetails, "authToken")
        );
    }

    @Test
    void processHearingDocument() throws IOException {
        when(tornadoService.listingGeneration(anyString(), any(), anyString())).thenReturn(documentInfo);
        listingDetails.getCaseData().setReportType(CASES_COMPLETED_REPORT);
        DocumentInfo documentInfo1 = listingService
                .processHearingDocument(listingDetails.getCaseData(),
                        listingDetails.getCaseTypeId(), "authToken");
        assertEquals(documentInfo, documentInfo1);
    }

    @Test
    void processHearingDocumentWithException() throws IOException {
        when(tornadoService.listingGeneration(anyString(), any(), anyString()))
                .thenThrow(new InternalException(ERROR_MESSAGE));

        assertThrows(Exception.class, () ->
                listingService.processHearingDocument(listingDetails.getCaseData(),
                        listingDetails.getCaseTypeId(), "authToken")
        );
    }

    @Test
    void processListingHearingsRequestWithAdditionalInfo() throws IOException {
        final String result = "ListingData(tribunalCorrespondenceAddress=null, tribunalCorrespondenceTelephone=null, "
                + "tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, reportDate=null, "
                + "hearingDateType=Single, listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, listingVenue=DynamicFixedListType(value=DynamicValueType"
                + "(code=Aberdeen, label=Aberdeen), listItems=null), listingVenueScotland=null,"
                + " listingCollection=[ListingTypeItem(id=123, value=ListingType(causeListDate=12 December 2019, "
                + "causeListTime=12:11, causeListVenue=Aberdeen, "
                + "elmoCaseReference=4210000/2019, jurisdictionCodesList=ABC, hearingType=Preliminary Hearing, "
                + "positionType=Awaiting ET3, hearingJudgeName= , "
                + "hearingEEMember= , hearingERMember= , hearingClerk=Clerk, hearingDay=1 of 3, "
                + "claimantName=Juan Pedro, claimantTown=Aberdeen, "
                + "claimantRepresentative=ONG, respondent=Royal McDonal, respondentTown=Aberdeen, "
                + "respondentRepresentative=ITV, estHearingLength=2 hours, "
                + "hearingPanel= , hearingRoom=Tribunal 4, respondentOthers=Royal McDonal, hearingNotes= , "
                + "judicialMediation= , hearingFormat= , "
                + "hearingReadingDeliberationMembersChambers= )), ListingTypeItem(id=124, "
                + "value=ListingType(causeListDate=12 December 2019, causeListTime=12:11, "
                + "causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, jurisdictionCodesList=ABC, "
                + "hearingType=Preliminary Hearing, positionType=Awaiting ET3, "
                + "hearingJudgeName= , hearingEEMember= , hearingERMember= ,"
                + " hearingClerk=Clerk1, hearingDay=3 of 3, "
                + "claimantName=Juan Pedro, claimantTown=Aberdeen, "
                + "claimantRepresentative=ONG, respondent=Royal McDonal, respondentTown=Aberdeen, "
                + "respondentRepresentative=ITV, estHearingLength=2 hours, hearingPanel= , "
                + "hearingRoom=Tribunal 5, respondentOthers=Royal McDonal, hearingNotes= , judicialMediation= , "
                + "hearingFormat= , "
                + "hearingReadingDeliberationMembersChambers= )), ListingTypeItem(id=124, "
                + "value=ListingType(causeListDate=12 December 2019, "
                + "causeListTime=12:11, causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, "
                + "jurisdictionCodesList=ABC, hearingType=Preliminary Hearing, "
                + "positionType=Awaiting ET3, hearingJudgeName= ,"
                + " hearingEEMember= , hearingERMember= , "
                + "hearingClerk=Clerk3, hearingDay=1 of 1, "
                + "claimantName=Juan Pedro, claimantTown=Aberdeen, claimantRepresentative=ONG, "
                + "respondent=Royal McDonal,"
                + " respondentTown=Aberdeen, "
                + "respondentRepresentative=ITV, estHearingLength= , hearingPanel= , hearingRoom=Tribunal 5, "
                + "respondentOthers=Royal McDonal, "
                + "hearingNotes= , judicialMediation= , hearingFormat= , "
                + "hearingReadingDeliberationMembersChambers= ))], "
                + "listingVenueOfficeGlas=null, "
                + "listingVenueOfficeAber=null, venueGlasgow=null, venueAberdeen=null, venueDundee=null, "
                + "venueEdinburgh=null, hearingDocType=null, "
                + "hearingDocETCL=null, roomOrNoRoom=null, docMarkUp=null, bfDateCollection=null, "
                + "clerkResponsible=null, reportType=Brought Forward Report, "
                + "documentName=null, showAll=null, localReportsSummaryHdr=null, localReportsSummary=null, "
                + "localReportsSummaryHdr2=null, localReportsSummary2=null, "
                + "localReportsDetailHdr=null, localReportsDetail=null, managingOffice=Leeds)";
        listingDetails.getCaseData().setManagingOffice("Leeds");
        ClaimantType claimantType = new ClaimantType();
        Address address = new Address();
        address.setPostTown("Aberdeen");
        claimantType.setClaimantAddressUK(address);
        submitEvents.get(0).getCaseData().setClaimantType(claimantType);
        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantLastName("Juan Pedro");
        submitEvents.get(0).getCaseData().setClaimantIndType(claimantIndType);
        RepresentedTypeC representedTypeC = new RepresentedTypeC();
        representedTypeC.setNameOfOrganisation("ONG");
        submitEvents.get(0).getCaseData().setRepresentativeClaimantType(representedTypeC);
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setId("111");
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentAddress(address);
        respondentSumType.setRespondentName("Royal McDonal");
        respondentSumType.setResponseStruckOut(NO);
        respondentSumTypeItem.setValue(respondentSumType);
        RespondentSumTypeItem respondentSumTypeItem1 = new RespondentSumTypeItem();
        RespondentSumType respondentSumType1 = new RespondentSumType();
        respondentSumType1.setRespondentAddress(address);
        respondentSumType1.setRespondentName("Burger King");
        respondentSumTypeItem1.setId("112");
        respondentSumTypeItem1.setValue(respondentSumType);
        submitEvents.get(0).getCaseData()
                .setRespondentCollection(new ArrayList<>(Arrays
                        .asList(respondentSumTypeItem, respondentSumTypeItem1)));
        RepresentedTypeRItem representedTypeRItem = new RepresentedTypeRItem();
        RepresentedTypeR representedTypeR = RepresentedTypeR.builder()
                .nameOfOrganisation("ITV").build();
        representedTypeRItem.setId("222");
        representedTypeRItem.setValue(representedTypeR);
        submitEvents.get(0).getCaseData()
                .setRepCollection(new ArrayList<>(Collections.singleton(representedTypeRItem)));
        when(ccdClient.buildAndGetElasticSearchRequest(anyString(), anyString(), anyString())).thenReturn(submitEvents);
        listingDetails.getCaseData().setVenueAberdeen(new DynamicFixedListType("Aberdeen"));
        ListingData listingDataResult = listingService
                .processListingHearingsRequest(listingDetails, "authToken");
        assertEquals(result, listingDataResult.toString());
    }

    @Test
    void processListingSingleCasesRequest() {
        final String result = "ListingData(tribunalCorrespondenceAddress=null, tribunalCorrespondenceTelephone=null, "
                + "tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, reportDate=null, "
                + "hearingDateType=Single, listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, listingVenue=DynamicFixedListType(value=DynamicValueType"
                + "(code=Aberdeen, label=Aberdeen), listItems=null), listingVenueScotland=null,"
                + " listingCollection=[ListingTypeItem(id=123, value=ListingType(causeListDate=12 December 2019, "
                + "causeListTime=12:11, causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, "
                + "jurisdictionCodesList=ABC, hearingType=Preliminary Hearing, "
                + "positionType=Awaiting ET3, hearingJudgeName= , "
                + "hearingEEMember= , hearingERMember= , "
                + "hearingClerk=Clerk, hearingDay=1 of 3, claimantName= , "
                + "claimantTown= , claimantRepresentative= , respondent= , respondentTown= , "
                + "respondentRepresentative= , estHearingLength=2 hours, "
                + "hearingPanel= , hearingRoom=Tribunal 4, respondentOthers= , hearingNotes= , judicialMediation= , "
                + "hearingFormat= , hearingReadingDeliberationMembersChambers= )), "
                + "ListingTypeItem(id=124, value=ListingType(causeListDate=12 December 2019, causeListTime=12:11, "
                + "causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, "
                + "jurisdictionCodesList=ABC, hearingType=Preliminary Hearing, positionType=Awaiting ET3, "
                + "hearingJudgeName= , hearingEEMember= , hearingERMember= , "
                + "hearingClerk=Clerk3, hearingDay=1 of 1, claimantName= , claimantTown= , claimantRepresentative= , "
                + "respondent= , respondentTown= , respondentRepresentative= , "
                + "estHearingLength= , hearingPanel= , hearingRoom=Tribunal 5, respondentOthers= , "
                + "hearingNotes= , judicialMediation= , "
                + "hearingFormat= , hearingReadingDeliberationMembersChambers= ))], "
                + "listingVenueOfficeGlas=null, listingVenueOfficeAber=null, venueGlasgow=null, "
                + "venueAberdeen=DynamicFixedListType(value=DynamicValueType("
                + "code=Aberdeen, label=Aberdeen), listItems=null), venueDundee=null, venueEdinburgh=null, "
                + "hearingDocType=null, hearingDocETCL=null, roomOrNoRoom=null, docMarkUp=null, "
                + "bfDateCollection=null, clerkResponsible=null, "
                + "reportType=Brought Forward Report, documentName=null, showAll=null, localReportsSummaryHdr=null, "
                + "localReportsSummary=null, localReportsSummaryHdr2=null, "
                + "localReportsSummary2=null, localReportsDetailHdr=null, localReportsDetail=null, "
                + "managingOffice=Aberdeen)";
        caseDetails.getCaseData().getHearingCollection().get(0).getValue().getHearingDateCollection()
                .get(2).getValue().setHearingStatus("Settled");
        listingDetails.getCaseData().setVenueAberdeen(new DynamicFixedListType("Aberdeen"));
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.ABERDEEN.getOfficeName());
        CaseData caseData = listingService.processListingSingleCasesRequest(caseDetails);
        assertEquals(result, caseData.getPrintHearingDetails().toString());
        caseDetails.getCaseData().getHearingCollection().get(0).getValue()
                .getHearingDateCollection().get(2).getValue().setHearingStatus(null);
    }

    @Test
    void processListingSingleCasesRequestNotShowAll() {
        final String result = "ListingData(tribunalCorrespondenceAddress=null, tribunalCorrespondenceTelephone=null, "
                + "tribunalCorrespondenceFax=null, tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, "
                + "reportDate=null, hearingDateType=Single, listingDate=2019-12-12, "
                + "listingDateFrom=null, listingDateTo=null, "
                + "listingVenue=DynamicFixedListType(value=DynamicValueType"
                + "(code=Aberdeen, label=Aberdeen), listItems=null), "
                + "listingVenueScotland=null, listingCollection=[ListingTypeItem(id=123, "
                + "value=ListingType(causeListDate=12 "
                + "December 2019, causeListTime=12:11, causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, "
                + "jurisdictionCodesList=ABC, hearingType=Preliminary Hearing, positionType=Awaiting ET3, "
                + "hearingJudgeName= , hearingEEMember= , "
                + "hearingERMember= , hearingClerk=Clerk, hearingDay=1 of 3, "
                + "claimantName= , claimantTown= , claimantRepresentative= , respondent= , respondentTown= , "
                + "respondentRepresentative= , estHearingLength=2 hours, hearingPanel= , hearingRoom=Tribunal 4, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , hearingFormat= , "
                + "hearingReadingDeliberationMembersChambers= )), "
                + "ListingTypeItem(id=124, value=ListingType(causeListDate=12 December 2019, "
                + "causeListTime=12:11, causeListVenue=Aberdeen, "
                + "elmoCaseReference=4210000/2019, jurisdictionCodesList=ABC, hearingType=Preliminary Hearing, "
                + "positionType=Awaiting ET3, "
                + "hearingJudgeName= , hearingEEMember= , hearingERMember= , hearingClerk=Clerk1, "
                + "hearingDay=3 of 3, claimantName= , "
                + "claimantTown= , claimantRepresentative= , respondent= , respondentTown= , "
                + "respondentRepresentative= , estHearingLength=2 hours, "
                + "hearingPanel= , hearingRoom=Tribunal 5, respondentOthers= , hearingNotes= , "
                + "judicialMediation= , hearingFormat= , "
                + "hearingReadingDeliberationMembersChambers= )), ListingTypeItem(id=124, "
                + "value=ListingType(causeListDate=12 December 2019, "
                + "causeListTime=12:11, causeListVenue=Aberdeen, elmoCaseReference=4210000/2019, "
                + "jurisdictionCodesList=ABC, hearingType=Preliminary Hearing, "
                + "positionType=Awaiting ET3, hearingJudgeName= ,"
                + " hearingEEMember= , hearingERMember= , "
                + "hearingClerk=Clerk3, "
                + "hearingDay=1 of 1, claimantName= , claimantTown= , claimantRepresentative= , "
                + "respondent= , respondentTown= , "
                + "respondentRepresentative= , estHearingLength= , hearingPanel= , hearingRoom=Tribunal 5, "
                + "respondentOthers= , hearingNotes= , judicialMediation= , hearingFormat= , "
                + "hearingReadingDeliberationMembersChambers= ))], "
                + "listingVenueOfficeGlas=null, listingVenueOfficeAber=null, "
                + "venueGlasgow=null, venueAberdeen=DynamicFixedListType(value=DynamicValueType("
                + "code=Aberdeen, label=Aberdeen), listItems=null), venueDundee=null, venueEdinburgh=null, "
                + "hearingDocType=ETCL - Cause List, hearingDocETCL=Staff, roomOrNoRoom=null, docMarkUp=null, "
                + "bfDateCollection=null, clerkResponsible=null, reportType=Brought "
                + "Forward Report, documentName=null, "
                + "showAll=No, localReportsSummaryHdr=null, localReportsSummary=null, "
                + "localReportsSummaryHdr2=null, "
                + "localReportsSummary2=null, localReportsDetailHdr=null, localReportsDetail=null, "
                + "managingOffice=Leeds)";
        caseDetails.getCaseData().getPrintHearingDetails().setShowAll(NO);
        caseDetails.getCaseData().getPrintHearingDetails()
                .setVenueAberdeen(new DynamicFixedListType(TribunalOffice.ABERDEEN.getOfficeName()));
        caseDetails.getCaseData().getPrintHearingDetails().setHearingDocType(HEARING_DOC_ETCL);
        caseDetails.getCaseData().getPrintHearingDetails().setHearingDocETCL(HEARING_ETCL_STAFF);
        CaseData caseData = listingService.processListingSingleCasesRequest(caseDetails);
        assertEquals(result, caseData.getPrintHearingDetails().toString());
    }

    @Test
    void setManagingOfficeAndCourtAddressFromCaseData() {
        String result = "ListingData(tribunalCorrespondenceAddress=Manchester Avenue, Manchester, "
                + "tribunalCorrespondenceTelephone=null, tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, reportDate=null, "
                + "hearingDateType=Single, listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, listingVenue=DynamicFixedListType(value=DynamicValueType"
                + "(code=Aberdeen, label=Aberdeen), listItems=null), listingVenueScotland=null, "
                + "listingCollection=[], listingVenueOfficeGlas=null, listingVenueOfficeAber=null, "
                + "venueGlasgow=null, venueAberdeen=DynamicFixedListType(value=DynamicValueType"
                + "(code=AberdeenVenue, label=AberdeenVenue), listItems=null), venueDundee=null, venueEdinburgh=null, "
                + "hearingDocType=null, hearingDocETCL=null, roomOrNoRoom=null, docMarkUp=null, "
                + "bfDateCollection=null, clerkResponsible=null, "
                + "reportType=Brought Forward Report, documentName=null, showAll=null, "
                + "localReportsSummaryHdr=null, localReportsSummary=null, localReportsSummaryHdr2=null, "
                + "localReportsSummary2=null, localReportsDetailHdr=null, "
                + "localReportsDetail=null, managingOffice=Leeds)";
        ListingData listingData = listingService.setManagingOfficeAndCourtAddressFromCaseData(
                caseDetails.getCaseData());
        assertEquals(result, listingData.toString());
    }

    @Test
    void getSelectedOfficeFromPrintingDetailsEWTest() {
        CaseData caseData = new CaseData();
        ListingData listingData = new ListingData();
        listingData.setListingVenue(new DynamicFixedListType("blah blah"));
        caseData.setManagingOffice(BRISTOL.getOfficeName());
        caseData.setPrintHearingDetails(listingData);
        assertEquals(BRISTOL.getOfficeName(), listingService.getSelectedOfficeForPrintLists(caseData));
    }

    @Test
    void getSelectedOfficeFromPrintingDetailsScotlandTest() {
        CaseData caseData = new CaseData();
        ListingData listingData = new ListingData();
        listingData.setListingVenueScotland(DUNDEE.getOfficeName());
        caseData.setPrintHearingDetails(listingData);
        assertEquals(DUNDEE.getOfficeName(), listingService.getSelectedOfficeForPrintLists(caseData));
    }

    @Test
    void getSelectedOfficeFromPrintingDetailsExceptionTest() {
        CaseData caseData = new CaseData();
        caseData.setPrintHearingDetails(new ListingData());
        assertThrows(IllegalStateException.class, () -> listingService.getSelectedOfficeForPrintLists(caseData));
    }

    @Test
    void generateClaimsAcceptedReportDataForEngland() throws IOException {
        final String result = "ListingData(tribunalCorrespondenceAddress=null, "
                + "tribunalCorrespondenceTelephone=null, tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, reportDate=null, "
                + "hearingDateType=Single, listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, listingVenue=DynamicFixedListType(value=DynamicValueType(code=Aberdeen, "
                + "label=Aberdeen), listItems=null), listingVenueScotland=null, "
                + "listingCollection=[], listingVenueOfficeGlas=null, listingVenueOfficeAber=null, "
                + "venueGlasgow=null, venueAberdeen=null, venueDundee=null, venueEdinburgh=null, "
                + "hearingDocType=null, hearingDocETCL=null, roomOrNoRoom=null, docMarkUp=null, bfDateCollection=null, "
                + "clerkResponsible=null, reportType=Claims Accepted, documentName=null, "
                + "showAll=null, localReportsSummaryHdr=null, "
                + "localReportsSummary=null, localReportsSummaryHdr2=null, localReportsSummary2=null, "
                + "localReportsDetailHdr=AdhocReportType(reportDate=null, reportOffice=Leeds, "
                + "receiptDate=null, hearingDate=null, date=null, "
                + "full=null, half=null, mins=null, total=1, eeMember=null, erMember=null, "
                + "caseReference=null, multipleRef=null, multSub=null, "
                + "hearingNumber=null, hearingType=null, hearingTelConf=null, hearingDuration=null, "
                + "hearingClerk=null, clerk=null, hearingSitAlone=null, "
                + "hearingJudge=null, judgeType=null, judgementDateSent=null, position=null, "
                + "dateToPosition=null, fileLocation=null, "
                + "fileLocationGlasgow=null, fileLocationAberdeen=null, fileLocationDundee=null, "
                + "fileLocationEdinburgh=null, casesCompletedHearingTotal=null, "
                + "casesCompletedHearing=null, sessionType=null, sessionDays=null, sessionDaysTotal=null, "
                + "sessionDaysTotalDetail=null, completedPerSession=null, "
                + "completedPerSessionTotal=null, ftSessionDays=null, ftSessionDaysTotal=null, ptSessionDays=null, "
                + "ptSessionDaysTotal=null, ptSessionDaysPerCent=null, "
                + "otherSessionDaysTotal=null, otherSessionDays=null, conciliationTrack=null, "
                + "conciliationTrackNo=null, conNoneCasesCompletedHearing=null, "
                + "conNoneSessionDays=null, conNoneCompletedPerSession=null, conFastCasesCompletedHearing=null, "
                + "conFastSessionDays=null, conFastCompletedPerSession=null, "
                + "conStdCasesCompletedHearing=null, conStdSessionDays=null, conStdCompletedPerSession=null, "
                + "conOpenCasesCompletedHearing=null, conOpenSessionDays=null, "
                + "conOpenCompletedPerSession=null, totalCases=null, total26wk=null, total26wkPerCent=null, "
                + "totalx26wk=null, totalx26wkPerCent=null, total4wk=null, "
                + "total4wkPerCent=null, totalx4wk=null, totalx4wkPerCent=null, respondentName=null, "
                + "actioned=null, bfDate=null, bfDateCleared=null, reservedHearing=null, "
                + "hearingCM=null, costs=null, hearingInterloc=null, hearingPH=null, hearingPrelim=null, "
                + "stage=null, hearingStage1=null, hearingStage2=null, hearingFull=null, "
                + "hearing=null, remedy=null, review=null, reconsider=null, subSplit=null, leadCase=null, "
                + "et3ReceivedDate=null, judicialMediation=null, caseType=null, "
                + "singlesTotal=1, multiplesTotal=0, dateOfAcceptance=null, respondentET3=null, "
                + "respondentET4=null, listingHistory=null, conNoneTotal=null, conStdTotal=null, "
                + "conFastTotal=null, conOpenTotal=null, conNone26wkTotal=null, conStd26wkTotal=null, "
                + "conFast26wkTotal=null, conOpen26wkTotal=null, conNone26wkTotalPerCent=null, "
                + "conStd26wkTotalPerCent=null, conFast26wkTotalPerCent=null, conOpen26wkTotalPerCent=null, "
                + "notConNone26wkTotal=null, notConStd26wkTotal=null, notConFast26wkTotal=null, "
                + "notConOpen26wkTotal=null, "
                + "notConNone26wkTotalPerCent=null, notConStd26wkTotalPerCent=null, notConFast26wkTotalPerCent=null, "
                + "notConOpen26wkTotalPerCent=null, delayedDaysForFirstHearing=null, "
                + "claimServedDay1Total=null, claimServedDay1Percent=null, claimServedDay2Total=null, "
                + "claimServedDay2Percent=null, claimServedDay3Total=null, claimServedDay3Percent=null, "
                + "claimServedDay4Total=null, claimServedDay4Percent=null, claimServedDay5Total=null, "
                + "claimServedDay5Percent=null, claimServed6PlusDaysTotal=null, claimServed6PlusDaysPercent=null, "
                + "claimServedTotal=null, claimServedItems=null, manuallyCreatedTotalCases=null, "
                + "et1OnlineTotalCases=null, eccTotalCases=null, migratedTotalCases=null, "
                + "manuallyCreatedTotalCasesPercent=null, et1OnlineTotalCasesPercent=null, "
                + "eccTotalCasesPercent=null, migratedTotalCasesPercent=null"
                + "), "
                + "localReportsDetail=[AdhocReportTypeItem(id=null, value=AdhocReportType"
                + "(reportDate=null, reportOffice=null, receiptDate=null, "
                + "hearingDate=null, date=null, full=null, half=null, mins=null, total=null, eeMember=null, "
                + "erMember=null, caseReference=4210000/2019, "
                + "multipleRef=null, multSub=null, hearingNumber=null, hearingType=null, hearingTelConf=null, "
                + "hearingDuration=null, hearingClerk=null, "
                + "clerk=Steve Jones, hearingSitAlone=null, hearingJudge=null, "
                + "judgeType=null, judgementDateSent=null, "
                + "position=null, dateToPosition=null, "
                + "fileLocation=null, fileLocationGlasgow=null, fileLocationAberdeen=null, fileLocationDundee=null, "
                + "fileLocationEdinburgh=null, "
                + "casesCompletedHearingTotal=null, casesCompletedHearing=null, sessionType=null, "
                + "sessionDays=null, sessionDaysTotal=null, "
                + "sessionDaysTotalDetail=null, completedPerSession=null, completedPerSessionTotal=null,"
                + " ftSessionDays=null, ftSessionDaysTotal=null, "
                + "ptSessionDays=null, ptSessionDaysTotal=null, ptSessionDaysPerCent=null, "
                + "otherSessionDaysTotal=null, otherSessionDays=null, "
                + "conciliationTrack=null, conciliationTrackNo=null, conNoneCasesCompletedHearing=null, "
                + "conNoneSessionDays=null, conNoneCompletedPerSession=null, "
                + "conFastCasesCompletedHearing=null, conFastSessionDays=null, conFastCompletedPerSession=null, "
                + "conStdCasesCompletedHearing=null, conStdSessionDays=null, "
                + "conStdCompletedPerSession=null, conOpenCasesCompletedHearing=null, conOpenSessionDays=null, "
                + "conOpenCompletedPerSession=null, totalCases=null, "
                + "total26wk=null, total26wkPerCent=null, totalx26wk=null, "
                + "totalx26wkPerCent=null, total4wk=null, total4wkPerCent=null, totalx4wk=null, totalx4wkPerCent=null,"
                + " respondentName=null, actioned=null, "
                + "bfDate=null, bfDateCleared=null, reservedHearing=null, hearingCM=null, costs=null, "
                + "hearingInterloc=null, hearingPH=null, hearingPrelim=null, stage=null, "
                + "hearingStage1=null, hearingStage2=null, hearingFull=null, hearing=null, "
                + "remedy=null, review=null, reconsider=null, subSplit=null, "
                + "leadCase=null, et3ReceivedDate=null, judicialMediation=null, "
                + "caseType=Single, singlesTotal=null, multiplesTotal=null, "
                + "dateOfAcceptance=2019-12-12, respondentET3=null, respondentET4=null, "
                + "listingHistory=null, conNoneTotal=null, conStdTotal=null, conFastTotal=null, "
                + "conOpenTotal=null, conNone26wkTotal=null, conStd26wkTotal=null, "
                + "conFast26wkTotal=null, conOpen26wkTotal=null, conNone26wkTotalPerCent=null, "
                + "conStd26wkTotalPerCent=null, conFast26wkTotalPerCent=null, conOpen26wkTotalPerCent=null, "
                + "notConNone26wkTotal=null, notConStd26wkTotal=null, notConFast26wkTotal=null, "
                + "notConOpen26wkTotal=null, notConNone26wkTotalPerCent=null, notConStd26wkTotalPerCent=null, "
                + "notConFast26wkTotalPerCent=null, notConOpen26wkTotalPerCent=null, delayedDaysForFirstHearing=null, "
                + "claimServedDay1Total=null, claimServedDay1Percent=null, claimServedDay2Total=null, "
                + "claimServedDay2Percent=null, claimServedDay3Total=null, claimServedDay3Percent=null, "
                + "claimServedDay4Total=null, claimServedDay4Percent=null, claimServedDay5Total=null, "
                + "claimServedDay5Percent=null, claimServed6PlusDaysTotal=null, claimServed6PlusDaysPercent=null, "
                + "claimServedTotal=null, claimServedItems=null, manuallyCreatedTotalCases=null, "
                + "et1OnlineTotalCases=null, eccTotalCases=null, migratedTotalCases=null, "
                + "manuallyCreatedTotalCasesPercent=null, et1OnlineTotalCasesPercent=null, "
                + "eccTotalCasesPercent=null, migratedTotalCasesPercent=null"
                + "))], managingOffice=Leeds)";
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setReportType(CLAIMS_ACCEPTED_REPORT);
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        when(ccdClient.retrieveCasesGenericReportElasticSearch(anyString(), anyString(), any(),
                anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingDataResult = listingService.getDateRangeReport(listingDetails, "authToken", "userName");
        assertEquals(result, listingDataResult.toString());
        assertEquals(TribunalOffice.LEEDS.getOfficeName(), listingDataResult
                .getLocalReportsDetailHdr().getReportOffice());
    }

    @Test
    void generateClaimsAcceptedReportDataForGlasgow() throws IOException {
        final String result = "ListingData(tribunalCorrespondenceAddress=null, "
                + "tribunalCorrespondenceTelephone=null, tribunalCorrespondenceFax=null, "
                + "tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, reportDate=null, "
                + "hearingDateType=Single, listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, listingVenue=DynamicFixedListType"
                + "(value=DynamicValueType(code=Aberdeen, label=Aberdeen), listItems=null), listingVenueScotland=null, "
                + "listingCollection=[], listingVenueOfficeGlas=null, listingVenueOfficeAber=null, "
                + "venueGlasgow=null, venueAberdeen=null, venueDundee=null, venueEdinburgh=null, "
                + "hearingDocType=null, hearingDocETCL=null, roomOrNoRoom=null, docMarkUp=null, bfDateCollection=null, "
                + "clerkResponsible=null, reportType=Claims Accepted, "
                + "documentName=null, showAll=null, localReportsSummaryHdr=null, "
                + "localReportsSummary=null, localReportsSummaryHdr2=null, localReportsSummary2=null, "
                + "localReportsDetailHdr=AdhocReportType(reportDate=null, reportOffice=Scotland, "
                + "receiptDate=null, hearingDate=null, date=null, "
                + "full=null, half=null, mins=null, total=1, eeMember=null, erMember=null, "
                + "caseReference=null, multipleRef=null, multSub=null, "
                + "hearingNumber=null, hearingType=null, hearingTelConf=null, hearingDuration=null, "
                + "hearingClerk=null, clerk=null, hearingSitAlone=null, "
                + "hearingJudge=null, judgeType=null, judgementDateSent=null, position=null, "
                + "dateToPosition=null, fileLocation=null, fileLocationGlasgow=null, "
                + "fileLocationAberdeen=null, fileLocationDundee=null, fileLocationEdinburgh=null, "
                + "casesCompletedHearingTotal=null, casesCompletedHearing=null, "
                + "sessionType=null, sessionDays=null, sessionDaysTotal=null, sessionDaysTotalDetail=null, "
                + "completedPerSession=null, completedPerSessionTotal=null, "
                + "ftSessionDays=null, ftSessionDaysTotal=null, ptSessionDays=null, ptSessionDaysTotal=null, "
                + "ptSessionDaysPerCent=null, otherSessionDaysTotal=null, "
                + "otherSessionDays=null, conciliationTrack=null, conciliationTrackNo=null, "
                + "conNoneCasesCompletedHearing=null, conNoneSessionDays=null, "
                + "conNoneCompletedPerSession=null, conFastCasesCompletedHearing=null, "
                + "conFastSessionDays=null, conFastCompletedPerSession=null, "
                + "conStdCasesCompletedHearing=null, conStdSessionDays=null, conStdCompletedPerSession=null, "
                + "conOpenCasesCompletedHearing=null, "
                + "conOpenSessionDays=null, conOpenCompletedPerSession=null, totalCases=null, "
                + "total26wk=null, total26wkPerCent=null, totalx26wk=null, "
                + "totalx26wkPerCent=null, total4wk=null, total4wkPerCent=null, totalx4wk=null, "
                + "totalx4wkPerCent=null, respondentName=null, actioned=null, "
                + "bfDate=null, bfDateCleared=null, reservedHearing=null, hearingCM=null, costs=null, "
                + "hearingInterloc=null, hearingPH=null, hearingPrelim=null, stage=null, "
                + "hearingStage1=null, hearingStage2=null, hearingFull=null, hearing=null, remedy=null, "
                + "review=null, reconsider=null, subSplit=null, leadCase=null, "
                + "et3ReceivedDate=null, judicialMediation=null, caseType=null, singlesTotal=1, "
                + "multiplesTotal=0, dateOfAcceptance=null, respondentET3=null, "
                + "respondentET4=null, listingHistory=null, conNoneTotal=null, conStdTotal=null, "
                + "conFastTotal=null, conOpenTotal=null, conNone26wkTotal=null, "
                + "conStd26wkTotal=null, conFast26wkTotal=null, conOpen26wkTotal=null, conNone26wkTotalPerCent=null, "
                + "conStd26wkTotalPerCent=null, conFast26wkTotalPerCent=null, "
                + "conOpen26wkTotalPerCent=null, notConNone26wkTotal=null, notConStd26wkTotal=null, "
                + "notConFast26wkTotal=null, notConOpen26wkTotal=null, notConNone26wkTotalPerCent=null, "
                + "notConStd26wkTotalPerCent=null, notConFast26wkTotalPerCent=null, "
                + "notConOpen26wkTotalPerCent=null, delayedDaysForFirstHearing=null, "
                + "claimServedDay1Total=null, claimServedDay1Percent=null, claimServedDay2Total=null, "
                + "claimServedDay2Percent=null, claimServedDay3Total=null, claimServedDay3Percent=null, "
                + "claimServedDay4Total=null, claimServedDay4Percent=null, claimServedDay5Total=null, "
                + "claimServedDay5Percent=null, claimServed6PlusDaysTotal=null, claimServed6PlusDaysPercent=null, "
                + "claimServedTotal=null, claimServedItems=null, manuallyCreatedTotalCases=null, "
                + "et1OnlineTotalCases=null, eccTotalCases=null, migratedTotalCases=null, "
                + "manuallyCreatedTotalCasesPercent=null, et1OnlineTotalCasesPercent=null,"
                + " eccTotalCasesPercent=null, migratedTotalCasesPercent=null"
                + "), "
                + "localReportsDetail=[AdhocReportTypeItem(id=null, "
                + "value=AdhocReportType(reportDate=null, reportOffice=null, receiptDate=null, "
                + "hearingDate=null, date=null, full=null, half=null, mins=null, "
                + "total=null, eeMember=null, erMember=null, caseReference=4210000/2019, "
                + "multipleRef=null, multSub=null, hearingNumber=null, hearingType=null, "
                + "hearingTelConf=null, hearingDuration=null, hearingClerk=null, "
                + "clerk=Steve Jones, hearingSitAlone=null, hearingJudge=null, judgeType=null, "
                + "judgementDateSent=null, position=null, dateToPosition=null, "
                + "fileLocation=null, fileLocationGlasgow=null, fileLocationAberdeen=null, "
                + "fileLocationDundee=null, fileLocationEdinburgh=null, "
                + "casesCompletedHearingTotal=null, casesCompletedHearing=null, sessionType=null, "
                + "sessionDays=null, sessionDaysTotal=null, "
                + "sessionDaysTotalDetail=null, completedPerSession=null, completedPerSessionTotal=null, "
                + "ftSessionDays=null, ftSessionDaysTotal=null, "
                + "ptSessionDays=null, ptSessionDaysTotal=null, ptSessionDaysPerCent=null, "
                + "otherSessionDaysTotal=null, otherSessionDays=null, "
                + "conciliationTrack=null, conciliationTrackNo=null, conNoneCasesCompletedHearing=null, "
                + "conNoneSessionDays=null, conNoneCompletedPerSession=null, "
                + "conFastCasesCompletedHearing=null, conFastSessionDays=null, "
                + "conFastCompletedPerSession=null, conStdCasesCompletedHearing=null, "
                + "conStdSessionDays=null, conStdCompletedPerSession=null, conOpenCasesCompletedHearing=null, "
                + "conOpenSessionDays=null, conOpenCompletedPerSession=null, "
                + "totalCases=null, total26wk=null, total26wkPerCent=null, totalx26wk=null, "
                + "totalx26wkPerCent=null, total4wk=null, total4wkPerCent=null, totalx4wk=null, "
                + "totalx4wkPerCent=null, respondentName=null, actioned=null, "
                + "bfDate=null, bfDateCleared=null, reservedHearing=null, hearingCM=null, costs=null, "
                + "hearingInterloc=null, hearingPH=null, hearingPrelim=null, stage=null, "
                + "hearingStage1=null, hearingStage2=null, hearingFull=null, hearing=null, "
                + "remedy=null, review=null, reconsider=null, subSplit=null, "
                + "leadCase=null, et3ReceivedDate=null, judicialMediation=null, caseType=Single, "
                + "singlesTotal=null, multiplesTotal=null, "
                + "dateOfAcceptance=2019-12-12, respondentET3=null, respondentET4=null, "
                + "listingHistory=null, conNoneTotal=null, conStdTotal=null, "
                + "conFastTotal=null, conOpenTotal=null, conNone26wkTotal=null, conStd26wkTotal=null, "
                + "conFast26wkTotal=null, conOpen26wkTotal=null, "
                + "conNone26wkTotalPerCent=null, conStd26wkTotalPerCent=null, conFast26wkTotalPerCent=null, "
                + "conOpen26wkTotalPerCent=null, notConNone26wkTotal=null, notConStd26wkTotal=null, "
                + "notConFast26wkTotal=null, notConOpen26wkTotal=null, notConNone26wkTotalPerCent=null, "
                + "notConStd26wkTotalPerCent=null, notConFast26wkTotalPerCent=null, "
                + "notConOpen26wkTotalPerCent=null, delayedDaysForFirstHearing=null, "
                + "claimServedDay1Total=null, claimServedDay1Percent=null, claimServedDay2Total=null, "
                + "claimServedDay2Percent=null, claimServedDay3Total=null, claimServedDay3Percent=null, "
                + "claimServedDay4Total=null, claimServedDay4Percent=null, claimServedDay5Total=null, "
                + "claimServedDay5Percent=null, claimServed6PlusDaysTotal=null, claimServed6PlusDaysPercent=null, "
                + "claimServedTotal=null, claimServedItems=null, manuallyCreatedTotalCases=null, "
                + "et1OnlineTotalCases=null, eccTotalCases=null, migratedTotalCases=null, "
                + "manuallyCreatedTotalCasesPercent=null, et1OnlineTotalCasesPercent=null, "
                + "eccTotalCasesPercent=null, migratedTotalCasesPercent=null"
                + "))], managingOffice=Glasgow)";
        listingDetails.setCaseTypeId(SCOTLAND_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setReportType(CLAIMS_ACCEPTED_REPORT);
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        when(ccdClient.retrieveCasesGenericReportElasticSearch(anyString(),
                anyString(), any(), anyString(), anyString(), anyString())).thenReturn(submitEvents);
        submitEvents.get(0).getCaseData().setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        ListingData listingDataResult = listingService.getDateRangeReport(listingDetails, "authToken", "userName");
        assertEquals(result, listingDataResult.toString());
        assertEquals(TribunalOffice.SCOTLAND.getOfficeName(), listingDataResult
                .getLocalReportsDetailHdr().getReportOffice());
    }

    @Test
    void checkExistingDataInReport() throws IOException {
        AdhocReportType adhocReportType = new AdhocReportType();
        adhocReportType.setSinglesTotal("6");
        adhocReportType.setMultiplesTotal("10");
        listingDetails.getCaseData().setLocalReportsSummaryHdr(adhocReportType);
        AdhocReportType adhocReportType2 = new AdhocReportType();
        adhocReportType2.setCaseReference("1800001/2021");
        adhocReportType2.setDateOfAcceptance("2021-01-01");
        AdhocReportTypeItem adhocReportTypeItem = new AdhocReportTypeItem();
        adhocReportTypeItem.setValue(adhocReportType2);
        List<AdhocReportTypeItem> localReportsSummary = List.of(adhocReportTypeItem);
        listingDetails.getCaseData().setLocalReportsSummary(localReportsSummary);
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        listingDetails.getCaseData().setReportType(CLAIMS_ACCEPTED_REPORT);
        submitEvents.remove(0);

        when(ccdClient.retrieveCasesGenericReportElasticSearch(anyString(), anyString(), any(), anyString(),
                anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingDataResult = listingService.getDateRangeReport(listingDetails, "authToken", "userName");
        assertTrue(CollectionUtils.isEmpty(listingDataResult.getLocalReportsDetail()));
        assertTrue(CollectionUtils.isEmpty(listingDataResult.getLocalReportsSummary2()));
        assertTrue(CollectionUtils.isEmpty(listingDataResult.getLocalReportsSummary()));
        assertEquals(TribunalOffice.MANCHESTER.getOfficeName(), listingDataResult
                .getLocalReportsDetailHdr().getReportOffice());
        assertNull(listingDataResult.getLocalReportsSummaryHdr());
        assertNull(listingDataResult.getLocalReportsSummaryHdr2());

    }

    @Test
    void generateLiveCaseloadReportDataForEnglandWithValidPositionType() throws IOException {
        final String result = "ListingData(tribunalCorrespondenceAddress=null, tribunalCorrespondenceTelephone=null, "
                + "tribunalCorrespondenceFax=null, tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, "
                + "reportDate=null, hearingDateType=Single, listingDate=2019-12-12, "
                + "listingDateFrom=null, listingDateTo=null, "
                + "listingVenue=DynamicFixedListType(value=DynamicValueType(code=Aberdeen, "
                + "label=Aberdeen), listItems=null), "
                + "listingVenueScotland=null, listingCollection=[], listingVenueOfficeGlas=null, "
                + "listingVenueOfficeAber=null, venueGlasgow=null, venueAberdeen=null, venueDundee=null, "
                + "venueEdinburgh=null, hearingDocType=null, hearingDocETCL=null, roomOrNoRoom=null, docMarkUp=null, "
                + "bfDateCollection=null, clerkResponsible=null, reportType=Live Caseload, documentName=null, "
                + "showAll=null, localReportsSummaryHdr=AdhocReportType(reportDate=null, "
                + "reportOffice=null, receiptDate=null, hearingDate=null, date=null, full=null, half=null, mins=null, "
                + "total=1, eeMember=null, erMember=null, caseReference=null, multipleRef=null, multSub=null, "
                + "hearingNumber=null, hearingType=null, hearingTelConf=null, hearingDuration=null, hearingClerk=null, "
                + "clerk=null, hearingSitAlone=null, hearingJudge=null, "
                + "judgeType=null, judgementDateSent=null, "
                + "position=null, dateToPosition=null, fileLocation=null, fileLocationGlasgow=null, "
                + "fileLocationAberdeen=null, fileLocationDundee=null, fileLocationEdinburgh=null, "
                + "casesCompletedHearingTotal=null, casesCompletedHearing=null, sessionType=null, "
                + "sessionDays=null, sessionDaysTotal=null, sessionDaysTotalDetail=null, completedPerSession=null, "
                + "completedPerSessionTotal=null, ftSessionDays=null, ftSessionDaysTotal=null, ptSessionDays=null, "
                + "ptSessionDaysTotal=null, ptSessionDaysPerCent=null, otherSessionDaysTotal=null, "
                + "otherSessionDays=null, conciliationTrack=null, conciliationTrackNo=null, "
                + "conNoneCasesCompletedHearing=null, conNoneSessionDays=null, conNoneCompletedPerSession=null, "
                + "conFastCasesCompletedHearing=null, conFastSessionDays=null, conFastCompletedPerSession=null, "
                + "conStdCasesCompletedHearing=null, conStdSessionDays=null, conStdCompletedPerSession=null, "
                + "conOpenCasesCompletedHearing=null, conOpenSessionDays=null, conOpenCompletedPerSession=null, "
                + "totalCases=null, total26wk=null, total26wkPerCent=null, totalx26wk=null, totalx26wkPerCent=null, "
                + "total4wk=null, total4wkPerCent=null, totalx4wk=null, totalx4wkPerCent=null, respondentName=null, "
                + "actioned=null, bfDate=null, bfDateCleared=null, reservedHearing=null, hearingCM=null, costs=null, "
                + "hearingInterloc=null, hearingPH=null, hearingPrelim=null, stage=null, hearingStage1=null, "
                + "hearingStage2=null, hearingFull=null, hearing=null, remedy=null, review=null, reconsider=null, "
                + "subSplit=null, leadCase=null, et3ReceivedDate=null, judicialMediation=null, caseType=null, "
                + "singlesTotal=1, multiplesTotal=0, dateOfAcceptance=null, respondentET3=null, respondentET4=null, "
                + "listingHistory=null, conNoneTotal=null, conStdTotal=null, conFastTotal=null, conOpenTotal=null, "
                + "conNone26wkTotal=null, conStd26wkTotal=null, conFast26wkTotal=null, conOpen26wkTotal=null, "
                + "conNone26wkTotalPerCent=null, "
                + "conStd26wkTotalPerCent=null, conFast26wkTotalPerCent=null, conOpen26wkTotalPerCent=null, "
                + "notConNone26wkTotal=null, notConStd26wkTotal=null,"
                + " notConFast26wkTotal=null, notConOpen26wkTotal=null, notConNone26wkTotalPerCent=null, "
                + "notConStd26wkTotalPerCent=null, notConFast26wkTotalPerCent=null, "
                + "notConOpen26wkTotalPerCent=null, delayedDaysForFirstHearing=null, "
                + "claimServedDay1Total=null, claimServedDay1Percent=null, claimServedDay2Total=null, "
                + "claimServedDay2Percent=null, claimServedDay3Total=null, claimServedDay3Percent=null, "
                + "claimServedDay4Total=null, claimServedDay4Percent=null, claimServedDay5Total=null, "
                + "claimServedDay5Percent=null, claimServed6PlusDaysTotal=null, claimServed6PlusDaysPercent=null, "
                + "claimServedTotal=null, claimServedItems=null, manuallyCreatedTotalCases=null,"
                + " et1OnlineTotalCases=null, eccTotalCases=null, migratedTotalCases=null, "
                + "manuallyCreatedTotalCasesPercent=null, "
                + "et1OnlineTotalCasesPercent=null, eccTotalCasesPercent=null, "
                + "migratedTotalCasesPercent=null"
                + "), localReportsSummary=null, "
                + "localReportsSummaryHdr2=null, "
                + "localReportsSummary2=null, localReportsDetailHdr=AdhocReportType(reportDate=null, "
                + "reportOffice=Leeds, receiptDate=null, hearingDate=null, date=null, full=null, half=null, "
                + "mins=null, total=null, eeMember=null, erMember=null, caseReference=null, multipleRef=null, "
                + "multSub=null, hearingNumber=null, hearingType=null, hearingTelConf=null, hearingDuration=null, "
                + "hearingClerk=null, clerk=null, hearingSitAlone=null, "
                + "hearingJudge=null, judgeType=null, "
                + "judgementDateSent=null, position=null, dateToPosition=null, fileLocation=null, "
                + "fileLocationGlasgow=null, fileLocationAberdeen=null, fileLocationDundee=null, "
                + "fileLocationEdinburgh=null, casesCompletedHearingTotal=null, casesCompletedHearing=null, "
                + "sessionType=null, sessionDays=null, sessionDaysTotal=null, sessionDaysTotalDetail=null, "
                + "completedPerSession=null, completedPerSessionTotal=null, ftSessionDays=null, "
                + "ftSessionDaysTotal=null, "
                + "ptSessionDays=null, ptSessionDaysTotal=null, ptSessionDaysPerCent=null, otherSessionDaysTotal=null, "
                + "otherSessionDays=null, conciliationTrack=null, conciliationTrackNo=null, "
                + "conNoneCasesCompletedHearing=null, conNoneSessionDays=null, conNoneCompletedPerSession=null, "
                + "conFastCasesCompletedHearing=null, conFastSessionDays=null, conFastCompletedPerSession=null, "
                + "conStdCasesCompletedHearing=null, conStdSessionDays=null, conStdCompletedPerSession=null, "
                + "conOpenCasesCompletedHearing=null, conOpenSessionDays=null, conOpenCompletedPerSession=null, "
                + "totalCases=null, total26wk=null, total26wkPerCent=null, totalx26wk=null, totalx26wkPerCent=null, "
                + "total4wk=null, total4wkPerCent=null, totalx4wk=null, totalx4wkPerCent=null, respondentName=null, "
                + "actioned=null, bfDate=null, bfDateCleared=null, reservedHearing=null, hearingCM=null, costs=null, "
                + "hearingInterloc=null, hearingPH=null, hearingPrelim=null, stage=null, hearingStage1=null, "
                + "hearingStage2=null, hearingFull=null, hearing=null, remedy=null, review=null, reconsider=null, "
                + "subSplit=null, leadCase=null, et3ReceivedDate=null, judicialMediation=null, caseType=null, "
                + "singlesTotal=null, multiplesTotal=null, dateOfAcceptance=null, respondentET3=null, "
                + "respondentET4=null, listingHistory=null, conNoneTotal=null, conStdTotal=null, conFastTotal=null, "
                + "conOpenTotal=null, conNone26wkTotal=null, conStd26wkTotal=null, "
                + "conFast26wkTotal=null, conOpen26wkTotal=null, "
                + "conNone26wkTotalPerCent=null, conStd26wkTotalPerCent=null, conFast26wkTotalPerCent=null,"
                + " conOpen26wkTotalPerCent=null,"
                + " notConNone26wkTotal=null, notConStd26wkTotal=null, notConFast26wkTotal=null, "
                + "notConOpen26wkTotal=null, "
                + "notConNone26wkTotalPerCent=null, "
                + "notConStd26wkTotalPerCent=null, notConFast26wkTotalPerCent=null, notConOpen26wkTotalPerCent=null, "
                + "delayedDaysForFirstHearing=null, "
                + "claimServedDay1Total=null, claimServedDay1Percent=null, claimServedDay2Total=null, "
                + "claimServedDay2Percent=null, claimServedDay3Total=null, claimServedDay3Percent=null, "
                + "claimServedDay4Total=null, claimServedDay4Percent=null, claimServedDay5Total=null, "
                + "claimServedDay5Percent=null, claimServed6PlusDaysTotal=null, claimServed6PlusDaysPercent=null, "
                + "claimServedTotal=null, claimServedItems=null, manuallyCreatedTotalCases=null, "
                + "et1OnlineTotalCases=null, eccTotalCases=null, migratedTotalCases=null, "
                + "manuallyCreatedTotalCasesPercent=null, "
                + "et1OnlineTotalCasesPercent=null, eccTotalCasesPercent=null, migratedTotalCasesPercent=null"
                + "), "
                + "localReportsDetail=[AdhocReportTypeItem(id=null, "
                + "value=AdhocReportType(reportDate=null, reportOffice=Leeds, receiptDate=null, hearingDate=null, "
                + "date=null, full=null, half=null, mins=null, total=null, eeMember=null, erMember=null, "
                + "caseReference=4210000/2019, multipleRef=null, multSub=null, hearingNumber=null, hearingType=null, "
                + "hearingTelConf=null, hearingDuration=null, hearingClerk=null, clerk=Steve Jones, "
                + "hearingSitAlone=null, hearingJudge=null, "
                + "judgeType=null, judgementDateSent=null, position=null, "
                + "dateToPosition=null, fileLocation=null, fileLocationGlasgow=null, fileLocationAberdeen=null, "
                + "fileLocationDundee=null, fileLocationEdinburgh=null, casesCompletedHearingTotal=null, "
                + "casesCompletedHearing=null, sessionType=null, sessionDays=null, sessionDaysTotal=null, "
                + "sessionDaysTotalDetail=null, completedPerSession=null, completedPerSessionTotal=null, "
                + "ftSessionDays=null, ftSessionDaysTotal=null, ptSessionDays=null, ptSessionDaysTotal=null, "
                + "ptSessionDaysPerCent=null, otherSessionDaysTotal=null, otherSessionDays=null, "
                + "conciliationTrack=null, conciliationTrackNo=null, conNoneCasesCompletedHearing=null, "
                + "conNoneSessionDays=null, conNoneCompletedPerSession=null, conFastCasesCompletedHearing=null, "
                + "conFastSessionDays=null, conFastCompletedPerSession=null, conStdCasesCompletedHearing=null, "
                + "conStdSessionDays=null, conStdCompletedPerSession=null, conOpenCasesCompletedHearing=null, "
                + "conOpenSessionDays=null, conOpenCompletedPerSession=null, totalCases=null, total26wk=null, "
                + "total26wkPerCent=null, totalx26wk=null, totalx26wkPerCent=null, total4wk=null, "
                + "total4wkPerCent=null, "
                + "totalx4wk=null, totalx4wkPerCent=null, respondentName=null, actioned=null, bfDate=null, "
                + "bfDateCleared=null, reservedHearing=null, hearingCM=null, costs=null,"
                + " hearingInterloc=null, hearingPH=null, "
                + "hearingPrelim=null, stage=null, hearingStage1=null, hearingStage2=null, hearingFull=null, "
                + "hearing=null, remedy=null, review=null, reconsider=null, subSplit=null, leadCase=null, "
                + "et3ReceivedDate=null, judicialMediation=null, caseType=Single, "
                + "singlesTotal=null, multiplesTotal=null, "
                + "dateOfAcceptance=2019-12-12, respondentET3=null, respondentET4=null, listingHistory=null, "
                + "conNoneTotal=null, "
                + "conStdTotal=null, conFastTotal=null, conOpenTotal=null, conNone26wkTotal=null, "
                + "conStd26wkTotal=null, "
                + "conFast26wkTotal=null,"
                + " conOpen26wkTotal=null, conNone26wkTotalPerCent=null,"
                + " conStd26wkTotalPerCent=null, conFast26wkTotalPerCent=null, "
                + "conOpen26wkTotalPerCent=null, notConNone26wkTotal=null, notConStd26wkTotal=null, "
                + "notConFast26wkTotal=null, notConOpen26wkTotal=null, "
                + "notConNone26wkTotalPerCent=null, notConStd26wkTotalPerCent=null, notConFast26wkTotalPerCent=null, "
                + "notConOpen26wkTotalPerCent=null, delayedDaysForFirstHearing=null, "
                + "claimServedDay1Total=null, claimServedDay1Percent=null, claimServedDay2Total=null, "
                + "claimServedDay2Percent=null, claimServedDay3Total=null, claimServedDay3Percent=null, "
                + "claimServedDay4Total=null, claimServedDay4Percent=null, claimServedDay5Total=null, "
                + "claimServedDay5Percent=null, claimServed6PlusDaysTotal=null, claimServed6PlusDaysPercent=null, "
                + "claimServedTotal=null, claimServedItems=null, manuallyCreatedTotalCases=null, "
                + "et1OnlineTotalCases=null, eccTotalCases=null, migratedTotalCases=null, "
                + "manuallyCreatedTotalCasesPercent=null, et1OnlineTotalCasesPercent=null, "
                + "eccTotalCasesPercent=null, migratedTotalCasesPercent=null"
                + "))], managingOffice=Leeds)";
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setReportType(LIVE_CASELOAD_REPORT);
        listingDetails.getCaseData().setManagingOffice("Leeds");
        when(ccdClient.retrieveCasesGenericReportElasticSearch(anyString(), anyString(), any(),
                anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingDataResult = listingService.getDateRangeReport(listingDetails, "authToken", "userName");
        assertEquals(result, listingDataResult.toString());
    }

    @Test
    void generateLiveCaseloadReportDataForGlasgowWithInvalidPositionType() throws IOException {
        final String result = "ListingData(tribunalCorrespondenceAddress=null, tribunalCorrespondenceTelephone=null, "
                + "tribunalCorrespondenceFax=null, tribunalCorrespondenceDX=null, tribunalCorrespondenceEmail=null, "
                + "reportDate=null, hearingDateType=Single, listingDate=2019-12-12, listingDateFrom=null, "
                + "listingDateTo=null, "
                + "listingVenue=DynamicFixedListType(value=DynamicValueType(code=Aberdeen, label=Aberdeen), "
                + "listItems=null), "
                + "listingVenueScotland=null, listingCollection=[], listingVenueOfficeGlas=null, "
                + "listingVenueOfficeAber=null, venueGlasgow=null, venueAberdeen=null, venueDundee=null, "
                + "venueEdinburgh=null, hearingDocType=null, hearingDocETCL=null, roomOrNoRoom=null, "
                + "docMarkUp=null, bfDateCollection=null, clerkResponsible=null, reportType=Live Caseload, "
                + "documentName=null, showAll=null, localReportsSummaryHdr=AdhocReportType(reportDate=null, "
                + "reportOffice=null, receiptDate=null, hearingDate=null, date=null, full=null, half=null, "
                + "mins=null, total=0, eeMember=null, erMember=null, caseReference=null, multipleRef=null, "
                + "multSub=null, "
                + "hearingNumber=null, hearingType=null, hearingTelConf=null, hearingDuration=null, hearingClerk=null, "
                + "clerk=null, hearingSitAlone=null, hearingJudge=null, judgeType=null, "
                + "judgementDateSent=null, "
                + "position=null, dateToPosition=null, fileLocation=null, fileLocationGlasgow=null, "
                + "fileLocationAberdeen=null, fileLocationDundee=null, fileLocationEdinburgh=null, "
                + "casesCompletedHearingTotal=null, casesCompletedHearing=null, sessionType=null, "
                + "sessionDays=null, sessionDaysTotal=null, sessionDaysTotalDetail=null, completedPerSession=null, "
                + "completedPerSessionTotal=null, ftSessionDays=null, ftSessionDaysTotal=null, ptSessionDays=null, "
                + "ptSessionDaysTotal=null, ptSessionDaysPerCent=null, otherSessionDaysTotal=null, "
                + "otherSessionDays=null, conciliationTrack=null, conciliationTrackNo=null, "
                + "conNoneCasesCompletedHearing=null, conNoneSessionDays=null, conNoneCompletedPerSession=null, "
                + "conFastCasesCompletedHearing=null, conFastSessionDays=null, conFastCompletedPerSession=null, "
                + "conStdCasesCompletedHearing=null, conStdSessionDays=null, conStdCompletedPerSession=null, "
                + "conOpenCasesCompletedHearing=null, conOpenSessionDays=null, conOpenCompletedPerSession=null, "
                + "totalCases=null, total26wk=null, total26wkPerCent=null, totalx26wk=null, totalx26wkPerCent=null, "
                + "total4wk=null, total4wkPerCent=null, totalx4wk=null, totalx4wkPerCent=null, respondentName=null, "
                + "actioned=null, bfDate=null, bfDateCleared=null, reservedHearing=null, hearingCM=null, costs=null, "
                + "hearingInterloc=null, hearingPH=null, hearingPrelim=null, stage=null, hearingStage1=null, "
                + "hearingStage2=null, hearingFull=null, hearing=null, remedy=null, review=null, reconsider=null, "
                + "subSplit=null, leadCase=null, et3ReceivedDate=null, judicialMediation=null, caseType=null, "
                + "singlesTotal=0, multiplesTotal=0, dateOfAcceptance=null, respondentET3=null, respondentET4=null, "
                + "listingHistory=null, conNoneTotal=null, conStdTotal=null, conFastTotal=null, conOpenTotal=null, "
                + "conNone26wkTotal=null, conStd26wkTotal=null, conFast26wkTotal=null, conOpen26wkTotal=null, "
                + "conNone26wkTotalPerCent=null, conStd26wkTotalPerCent=null, conFast26wkTotalPerCent=null, "
                + "conOpen26wkTotalPerCent=null, notConNone26wkTotal=null, notConStd26wkTotal=null, "
                + "notConFast26wkTotal=null, "
                + "notConOpen26wkTotal=null, notConNone26wkTotalPerCent=null, notConStd26wkTotalPerCent=null, "
                + "notConFast26wkTotalPerCent=null, notConOpen26wkTotalPerCent=null, delayedDaysForFirstHearing=null, "
                + "claimServedDay1Total=null, claimServedDay1Percent=null, claimServedDay2Total=null, "
                + "claimServedDay2Percent=null, claimServedDay3Total=null, claimServedDay3Percent=null, "
                + "claimServedDay4Total=null, claimServedDay4Percent=null, claimServedDay5Total=null, "
                + "claimServedDay5Percent=null, claimServed6PlusDaysTotal=null, claimServed6PlusDaysPercent=null, "
                + "claimServedTotal=null, claimServedItems=null, manuallyCreatedTotalCases=null, "
                + "et1OnlineTotalCases=null,"
                + " eccTotalCases=null, migratedTotalCases=null, manuallyCreatedTotalCasesPercent=null,"
                + " et1OnlineTotalCasesPercent=null, eccTotalCasesPercent=null, migratedTotalCasesPercent=null"
                + "), localReportsSummary=null, "
                + "localReportsSummaryHdr2=null, "
                + "localReportsSummary2=null, localReportsDetailHdr=AdhocReportType(reportDate=null, "
                + "reportOffice=Scotland, receiptDate=null, "
                + "hearingDate=null, date=null, full=null, half=null, mins=null, total=null, eeMember=null, "
                + "erMember=null, caseReference=null, multipleRef=null, multSub=null, hearingNumber=null, "
                + "hearingType=null, hearingTelConf=null, hearingDuration=null, hearingClerk=null, clerk=null, "
                + "hearingSitAlone=null, hearingJudge=null, judgeType=null, "
                + "judgementDateSent=null, position=null, "
                + "dateToPosition=null, fileLocation=null, fileLocationGlasgow=null, fileLocationAberdeen=null, "
                + "fileLocationDundee=null, fileLocationEdinburgh=null, casesCompletedHearingTotal=null, "
                + "casesCompletedHearing=null, sessionType=null, sessionDays=null, sessionDaysTotal=null, "
                + "sessionDaysTotalDetail=null, completedPerSession=null, completedPerSessionTotal=null, f"
                + "tSessionDays=null, ftSessionDaysTotal=null, ptSessionDays=null, ptSessionDaysTotal=null, "
                + "ptSessionDaysPerCent=null, otherSessionDaysTotal=null, "
                + "otherSessionDays=null, conciliationTrack=null, "
                + "conciliationTrackNo=null, conNoneCasesCompletedHearing=null, conNoneSessionDays=null, "
                + "conNoneCompletedPerSession=null, conFastCasesCompletedHearing=null, conFastSessionDays=null, "
                + "conFastCompletedPerSession=null, conStdCasesCompletedHearing=null, conStdSessionDays=null, "
                + "conStdCompletedPerSession=null, conOpenCasesCompletedHearing=null, conOpenSessionDays=null, "
                + "conOpenCompletedPerSession=null, totalCases=null, total26wk=null, total26wkPerCent=null, "
                + "totalx26wk=null, totalx26wkPerCent=null, total4wk=null, total4wkPerCent=null, totalx4wk=null, "
                + "totalx4wkPerCent=null, respondentName=null, actioned=null, bfDate=null, bfDateCleared=null, "
                + "reservedHearing=null, hearingCM=null, costs=null, "
                + "hearingInterloc=null, hearingPH=null, hearingPrelim=null, "
                + "stage=null, hearingStage1=null, hearingStage2=null, hearingFull=null, hearing=null, remedy=null, "
                + "review=null, reconsider=null, subSplit=null, leadCase=null, et3ReceivedDate=null, "
                + "judicialMediation=null, caseType=null, singlesTotal=null, multiplesTotal=null, "
                + "dateOfAcceptance=null, respondentET3=null, respondentET4=null, listingHistory=null, "
                + "conNoneTotal=null, conStdTotal=null, conFastTotal=null, conOpenTotal=null, conNone26wkTotal=null,"
                + " conStd26wkTotal=null, conFast26wkTotal=null, conOpen26wkTotal=null, conNone26wkTotalPerCent=null, "
                + "conStd26wkTotalPerCent=null, conFast26wkTotalPerCent=null, conOpen26wkTotalPerCent=null, "
                + "notConNone26wkTotal=null, notConStd26wkTotal=null, notConFast26wkTotal=null, "
                + "notConOpen26wkTotal=null, "
                + "notConNone26wkTotalPerCent=null, notConStd26wkTotalPerCent=null, notConFast26wkTotalPerCent=null, "
                + "notConOpen26wkTotalPerCent=null, delayedDaysForFirstHearing=null, "
                + "claimServedDay1Total=null, claimServedDay1Percent=null, claimServedDay2Total=null, "
                + "claimServedDay2Percent=null, claimServedDay3Total=null, claimServedDay3Percent=null, "
                + "claimServedDay4Total=null, claimServedDay4Percent=null, claimServedDay5Total=null, "
                + "claimServedDay5Percent=null, claimServed6PlusDaysTotal=null, claimServed6PlusDaysPercent=null, "
                + "claimServedTotal=null, claimServedItems=null, manuallyCreatedTotalCases=null, "
                + "et1OnlineTotalCases=null, eccTotalCases=null, migratedTotalCases=null, "
                + "manuallyCreatedTotalCasesPercent=null, et1OnlineTotalCasesPercent=null, "
                + "eccTotalCasesPercent=null, migratedTotalCasesPercent=null), localReportsDetail=[], "
                + "managingOffice=Aberdeen)";
        listingDetails.setCaseTypeId(SCOTLAND_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setReportType(LIVE_CASELOAD_REPORT);
        listingDetails.getCaseData().setManagingOffice("Aberdeen");
        when(ccdClient.retrieveCasesGenericReportElasticSearch(anyString(), anyString(), any(),
                anyString(), anyString(), anyString())).thenReturn(submitEvents);
        submitEvents.get(0).getCaseData().setManagingOffice("Aberdeen");
        submitEvents.get(0).getCaseData().setPositionType(POSITION_TYPE_CASE_CLOSED);
        ListingData listingDataResult = listingService.getDateRangeReport(listingDetails, "authToken", "userName");
        assertEquals(result, listingDataResult.toString());
        submitEvents.get(0).getCaseData().setPositionType("Awaiting ET3");
    }

    @Test
    void generateCasesCompletedReportDataForScotland() throws IOException {
        listingDetails.setCaseTypeId(SCOTLAND_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setReportType(CASES_COMPLETED_REPORT);
        listingDetails.getCaseData().setManagingOffice(null);
        when(ccdClient.retrieveCasesGenericReportElasticSearch(anyString(), anyString(), any(),
                anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingDataResult = listingService.getDateRangeReport(listingDetails, "authToken", "userName");
        assertNotNull(listingDataResult.getLocalReportsDetailHdr());
        assertEquals(1, listingDataResult.getLocalReportsDetail().size());
    }

    @Test
    void generateCasesCompletedReportDataForEnglandWithConTrackNone() throws IOException {
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setReportType(CASES_COMPLETED_REPORT);
        listingDetails.getCaseData().setManagingOffice("Leeds");
        when(ccdClient.retrieveCasesGenericReportElasticSearch(anyString(), anyString(), any(),
                anyString(), anyString(), anyString())).thenReturn(submitEvents);
        ListingData listingDataResult = listingService.getDateRangeReport(listingDetails, "authToken", "userName");
        assertNotNull(listingDataResult.getLocalReportsDetailHdr());
        assertEquals(1, listingDataResult.getLocalReportsDetail().size());
    }

    @Test
    void generateCasesCompletedReportDataForEnglandWithConTrackFast() throws IOException {
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setReportType(CASES_COMPLETED_REPORT);
        listingDetails.getCaseData().setManagingOffice("Leeds");
        when(ccdClient.retrieveCasesGenericReportElasticSearch(anyString(), anyString(), any(),
                anyString(), anyString(), anyString())).thenReturn(submitEvents);
        submitEvents.get(0).getCaseData().setConciliationTrack(CONCILIATION_TRACK_FAST_TRACK);
        ListingData listingDataResult = listingService.getDateRangeReport(listingDetails, "authToken", "userName");
        assertNotNull(listingDataResult.getLocalReportsDetailHdr());
        assertEquals(1, listingDataResult.getLocalReportsDetail().size());
        submitEvents.get(0).getCaseData().setConciliationTrack(CONCILIATION_TRACK_NO_CONCILIATION);
    }

    @Test
    void generateCasesCompletedReportDataForEnglandWithConTrackStandard() throws IOException {
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setReportType(CASES_COMPLETED_REPORT);
        listingDetails.getCaseData().setManagingOffice("Leeds");
        when(ccdClient.retrieveCasesGenericReportElasticSearch(anyString(), anyString(), any(),
                anyString(), anyString(), anyString())).thenReturn(submitEvents);
        submitEvents.get(0).getCaseData().setConciliationTrack(CONCILIATION_TRACK_STANDARD_TRACK);
        ListingData listingDataResult = listingService.getDateRangeReport(listingDetails, "authToken", "userName");
        assertNotNull(listingDataResult.getLocalReportsDetailHdr());
        assertEquals(1, listingDataResult.getLocalReportsDetail().size());
        submitEvents.get(0).getCaseData().setConciliationTrack(CONCILIATION_TRACK_NO_CONCILIATION);
    }

    @Test
    void generateCasesCompletedReportDataForEnglandWithConTrackOpen() throws IOException {
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setReportType(CASES_COMPLETED_REPORT);
        listingDetails.getCaseData().setManagingOffice("Leeds");
        when(ccdClient.retrieveCasesGenericReportElasticSearch(anyString(), anyString(),
                any(), anyString(), anyString(), anyString())).thenReturn(submitEvents);
        submitEvents.get(0).getCaseData().setConciliationTrack(CONCILIATION_TRACK_OPEN_TRACK);
        ListingData listingDataResult = listingService.getDateRangeReport(listingDetails, "authToken", "userName");
        assertNotNull(listingDataResult.getLocalReportsDetailHdr());
        assertEquals(1, listingDataResult.getLocalReportsDetail().size());
        submitEvents.get(0).getCaseData().setConciliationTrack(CONCILIATION_TRACK_NO_CONCILIATION);
    }

    @Test
    void generateReportDataWithException() throws IOException {
        when(ccdClient.retrieveCasesGenericReportElasticSearch(anyString(), anyString(), any(),
                anyString(), anyString(), anyString())).thenThrow(new InternalException(ERROR_MESSAGE));

        assertThrows(Exception.class, () ->
                listingService.getDateRangeReport(listingDetails, "authToken", "userName")
        );
    }

    @Test
    void dynamicVenueListing_Leeds() {
        when(venueService.getVenues(any())).thenReturn(VENUES);
        listingDetails.getCaseData().setManagingOffice("Leeds");

        listingService.dynamicVenueListing(ENGLANDWALES_LISTING_CASE_TYPE_ID, listingDetails.getCaseData());
        assertEquals(3, listingDetails.getCaseData().getListingVenue().getListItems().size());
        assertTrue(listingDetails.getCaseData().getListingVenue().getListItems()
                .contains(DynamicValueType.create(ALL_VENUES, ALL_VENUES)));
    }

    @Test
    void dynamicVenueListing_Glasgow() {
        when(venueService.getVenues(any())).thenReturn(VENUES);
        listingDetails.getCaseData().setManagingOffice("Glasgow");

        listingService.dynamicVenueListing(SCOTLAND_LISTING_CASE_TYPE_ID, listingDetails.getCaseData());
        assertEquals(3, listingDetails.getCaseData().getListingVenue().getListItems().size());
        assertTrue(listingDetails.getCaseData().getListingVenue().getListItems()
                .contains(DynamicValueType.create(ALL_VENUES, ALL_VENUES)));
    }

    @Test
    void dynamicVenueListing_invalidCaseType() {
        assertThrows(IllegalArgumentException.class, () ->
                listingService.dynamicVenueListing("InvalidCaseType", listingDetails.getCaseData())
        );
    }
}
