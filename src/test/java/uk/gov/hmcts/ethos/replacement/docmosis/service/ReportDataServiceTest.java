package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.reports.claimsbyhearingvenue.ClaimsByHearingVenueCaseData;
import uk.gov.hmcts.ecm.common.model.reports.claimsbyhearingvenue.ClaimsByHearingVenueSubmitEvent;
import uk.gov.hmcts.ecm.common.model.reports.eccreport.EccReportCaseData;
import uk.gov.hmcts.ecm.common.model.reports.eccreport.EccReportSubmitEvent;
import uk.gov.hmcts.ecm.common.model.reports.hearingsbyhearingtype.HearingsByHearingTypeCaseData;
import uk.gov.hmcts.ecm.common.model.reports.hearingsbyhearingtype.HearingsByHearingTypeSubmitEvent;
import uk.gov.hmcts.ecm.common.model.reports.hearingstojudgments.HearingsToJudgmentsSubmitEvent;
import uk.gov.hmcts.ecm.common.model.reports.respondentsreport.RespondentsReportCaseData;
import uk.gov.hmcts.ecm.common.model.reports.respondentsreport.RespondentsReportSubmitEvent;
import uk.gov.hmcts.ecm.common.model.reports.sessiondays.SessionDaysCaseData;
import uk.gov.hmcts.ecm.common.model.reports.sessiondays.SessionDaysSubmitEvent;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.BFActionType;
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.BFHelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment.CaseDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment.CasesAwaitingJudgmentReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.claimsbyhearingvenue.ClaimsByHearingVenueReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.eccreport.EccReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingsbyhearingtype.HearingsByHearingTypeReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition.NoPositionChangeCaseDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition.NoPositionChangeReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.nochangeincurrentposition.NoPositionChangeSearchResult;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.respondentsreport.RespondentsReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.sessiondays.SessionDaysReportData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BROUGHT_FORWARD_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMS_BY_HEARING_VENUE_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CONCILIATION_TRACK_NO_CONCILIATION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARINGS_BY_HEARING_TYPE_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARINGS_TO_JUDGEMENTS_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_PERLIMINARY_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.JURISDICTION_OUTCOME_SUCCESSFUL_AT_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RANGE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SESSION_DAYS_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_HEARING_DATE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.CASES_AWAITING_JUDGMENT_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ECC_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.NO_CHANGE_IN_CURRENT_POSITION_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.RESPONDENTS_REPORT;

@SuppressWarnings({"PMD.LawOfDemeter", "PMD.ExcessiveImports", "PMD.ExcessiveImports", "PMD.NcssCount",
    "PMD.ExcessiveMethodLength"})
@RunWith(SpringJUnit4ClassRunner.class)
public class ReportDataServiceTest {

    @InjectMocks
    private ReportDataService reportDataService;
    @Mock
    private CcdClient ccdClient;
    private ListingDetails listingDetails;
    @Mock
    private UserService userService;

    @Before
    public void setUp() {
        listingDetails = new ListingDetails();
        ListingData listingData = new ListingData();
        listingData.setListingDate("2019-12-12");
        listingData.setListingVenue(new DynamicFixedListType("Aberdeen"));
        listingData.setVenueAberdeen(new DynamicFixedListType("AberdeenVenue"));
        listingData.setListingCollection(new ArrayList<>());
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingData.setReportType(BROUGHT_FORWARD_REPORT);
        listingDetails.setCaseData(listingData);
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.setJurisdiction("EMPLOYMENT");
        DateListedType dateListedType = new DateListedType();
        dateListedType.setHearingStatus(HEARING_STATUS_HEARD);
        dateListedType.setHearingClerk(new DynamicFixedListType("Clerk"));
        dateListedType.setHearingRoom(new DynamicFixedListType("Tribunal 4"));
        dateListedType.setHearingAberdeen(new DynamicFixedListType("AberdeenVenue"));
        dateListedType.setHearingVenueDay(new DynamicFixedListType("Aberdeen"));
        dateListedType.setListedDate("2019-12-12T12:11:00.000");
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setId("123");
        dateListedTypeItem.setValue(dateListedType);
        DateListedType dateListedType1 = new DateListedType();
        dateListedType.setHearingStatus(HEARING_STATUS_HEARD);
        dateListedType1.setHearingClerk(new DynamicFixedListType("Clerk"));
        dateListedType1.setHearingRoom(new DynamicFixedListType("Tribunal 4"));
        dateListedType1.setHearingAberdeen(new DynamicFixedListType("AberdeenVenue"));
        dateListedType1.setHearingVenueDay(new DynamicFixedListType("Aberdeen"));
        dateListedType1.setListedDate("2019-12-10T12:11:00.000");
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
        dateListedType3.setListedDate("2019-12-12T12:11:55.000");
        DateListedTypeItem dateListedTypeItem3 = new DateListedTypeItem();
        dateListedTypeItem3.setId("124");
        dateListedTypeItem3.setValue(dateListedType3);
        HearingType hearingType = new HearingType();
        hearingType.setHearingDateCollection(new ArrayList<>(
                Arrays.asList(dateListedTypeItem, dateListedTypeItem1, dateListedTypeItem2)));
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
        JurCodesType jurCodesType = new JurCodesType();
        jurCodesType.setJuridictionCodesList("ABC");
        jurCodesType.setJudgmentOutcome(JURISDICTION_OUTCOME_SUCCESSFUL_AT_HEARING);
        JurCodesTypeItem jurCodesTypeItem = new JurCodesTypeItem();
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
        submitEvent1.setCaseData(caseData);
        submitEvent1.setState(CLOSED_STATE);

        caseData.setPrintHearingDetails(listingData);
        caseData.setPrintHearingCollection(listingData);
        Address address = new Address();
        address.setAddressLine1("Manchester Avenue");
        address.setPostTown("Manchester");
        caseData.setTribunalCorrespondenceAddress(address);
        caseData.setManagingOffice(TribunalOffice.LEEDS.getOfficeName());
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        caseDetails.setJurisdiction("EMPLOYMENT");
    }

    @Test
    public void generateCasesAwaitingJudgmentsReportData() throws IOException {
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.NEWCASTLE.getOfficeName());
        listingDetails.getCaseData().setReportType(CASES_AWAITING_JUDGMENT_REPORT);
        listingDetails.getCaseData().setDocumentName("name");
        CaseDataBuilder caseDataBuilder = new CaseDataBuilder();
        when(ccdClient.casesAwaitingJudgmentSearch(anyString(), anyString(), anyString())).thenReturn(
                List.of(caseDataBuilder.withPositionType("Draft with members")
                        .withHearing("1970-01-01T00:00:00.000", HEARING_STATUS_HEARD)
                        .buildAsSubmitEvent(ACCEPTED_STATE)));
        CasesAwaitingJudgmentReportData listingDataResult = (CasesAwaitingJudgmentReportData) reportDataService
                .generateReportData(listingDetails, "authToken");
        assertEquals("name", listingDataResult.getDocumentName());
        assertEquals(CASES_AWAITING_JUDGMENT_REPORT, listingDataResult.getReportType());
    }

    @Test
    public void generateNoPositionChangeReportData() throws IOException {
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setReportType(NO_CHANGE_IN_CURRENT_POSITION_REPORT);
        listingDetails.getCaseData().setDocumentName("name");
        listingDetails.getCaseData().setReportDate("2021-12-12");
        NoPositionChangeCaseDataBuilder caseDataBuilder = new NoPositionChangeCaseDataBuilder();
        NoPositionChangeSearchResult result =  new NoPositionChangeSearchResult();
        result.setCases(List.of(caseDataBuilder.withCaseType("SINGLE")
                .withCurrentPosition("Position")
                .withDateToPosition("2021-04-03")
                .withReceiptDate("2021-03-03")
                .buildAsSubmitEvent(ACCEPTED_STATE)));
        when(ccdClient.runElasticSearch(anyString(), anyString(), anyString(), eq(NoPositionChangeSearchResult.class)))
                .thenReturn(result);
        when(ccdClient.buildAndGetElasticSearchRequestWithRetriesMultiples(anyString(), anyString(), anyString()))
                .thenReturn(new ArrayList<>());
        NoPositionChangeReportData listingDataResult = (NoPositionChangeReportData) reportDataService
                .generateReportData(listingDetails, "authToken");
        assertEquals("name", listingDataResult.getDocumentName());
        assertEquals(NO_CHANGE_IN_CURRENT_POSITION_REPORT, listingDataResult.getReportType());
        assertEquals("2021-12-12", listingDataResult.getReportDate());
    }

    @Test
    public void generateNoPositionChangeReportDataWithMultiple() throws IOException {
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.NEWCASTLE.getOfficeName());
        listingDetails.getCaseData().setReportType(NO_CHANGE_IN_CURRENT_POSITION_REPORT);
        listingDetails.getCaseData().setDocumentName("name");
        listingDetails.getCaseData().setReportDate("2021-12-12");
        NoPositionChangeCaseDataBuilder caseDataBuilder = new NoPositionChangeCaseDataBuilder();
        NoPositionChangeSearchResult result =  new NoPositionChangeSearchResult();
        result.setCases(List.of(caseDataBuilder.withCaseType(MULTIPLE_CASE_TYPE)
                .withCurrentPosition("Position")
                .withDateToPosition("2021-04-03")
                .withMultipleReference("multipleRef")
                .withReceiptDate("2021-03-03")
                .buildAsSubmitEvent(ACCEPTED_STATE)));
        MultipleData multipleData = new MultipleData();
        multipleData.setMultipleReference("multipleRef");
        multipleData.setMultipleName("Multiple Name");
        SubmitMultipleEvent submitMultipleData = new SubmitMultipleEvent();
        submitMultipleData.setCaseData(multipleData);
        when(ccdClient.runElasticSearch(anyString(), anyString(), anyString(), eq(NoPositionChangeSearchResult.class)))
                .thenReturn(result);
        when(ccdClient.buildAndGetElasticSearchRequestWithRetriesMultiples(anyString(), anyString(), anyString()))
                .thenReturn(List.of(submitMultipleData));
        NoPositionChangeReportData listingDataResult = (NoPositionChangeReportData) reportDataService
                .generateReportData(listingDetails, "authToken");
        assertEquals("name", listingDataResult.getDocumentName());
        assertEquals(NO_CHANGE_IN_CURRENT_POSITION_REPORT, listingDataResult.getReportType());
        assertEquals("2021-12-12", listingDataResult.getReportDate());
    }

    @Test
    public void generateHearingToJudgmentsReportData() throws IOException {
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        listingDetails.getCaseData().setReportType(HEARINGS_TO_JUDGEMENTS_REPORT);
        listingDetails.getCaseData().setDocumentName("name");
        listingDetails.getCaseData().setHearingDateType("Ranged");
        listingDetails.getCaseData().setListingDate("2021-07-13");
        listingDetails.getCaseData().setListingDateFrom("2021-07-12");
        listingDetails.getCaseData().setListingDateTo("2021-07-14");
        when(ccdClient.hearingsToJudgementsSearch(anyString(), anyString(), anyString()))
                .thenReturn(List.of(new HearingsToJudgmentsSubmitEvent()));
        HearingsToJudgmentsReportData listingDataResult = (HearingsToJudgmentsReportData)reportDataService
                .generateReportData(listingDetails, "authToken");
        assertEquals("name", listingDataResult.getDocumentName());
        assertEquals(HEARINGS_TO_JUDGEMENTS_REPORT, listingDataResult.getReportType());
        assertEquals("Ranged", listingDataResult.getHearingDateType());
        assertEquals("2021-07-13", listingDataResult.getListingDate());
        assertEquals("2021-07-12", listingDataResult.getListingDateFrom());
        assertEquals("2021-07-14", listingDataResult.getListingDateTo());
    }

    @Test
    public void generateRespondentsReportData() throws IOException {
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        listingDetails.getCaseData().setReportType(RESPONDENTS_REPORT);
        listingDetails.getCaseData().setDocumentName("name");
        listingDetails.getCaseData().setHearingDateType("Ranged");
        listingDetails.getCaseData().setListingDate("2022-01-13");
        listingDetails.getCaseData().setListingDateFrom("2022-01-31");
        listingDetails.getCaseData().setListingDateTo("2022-02-08");
        RespondentsReportSubmitEvent submitEvent = new RespondentsReportSubmitEvent();
        submitEvent.setCaseData(new RespondentsReportCaseData());
        when(ccdClient.respondentsReportSearch(anyString(), anyString(), anyString())).thenReturn(List.of(submitEvent));
        RespondentsReportData listingDataResult = (RespondentsReportData) reportDataService
                .generateReportData(listingDetails, "authToken");
        assertEquals("name", listingDataResult.getDocumentName());
        assertEquals(RESPONDENTS_REPORT, listingDataResult.getReportType());
        assertEquals("Ranged", listingDataResult.getHearingDateType());
        assertEquals("2022-01-13", listingDataResult.getListingDate());
        assertEquals("2022-01-31", listingDataResult.getListingDateFrom());
        assertEquals("2022-02-08", listingDataResult.getListingDateTo());
    }

    @Test
    public void generateClaimsByHearingVenueReport() throws IOException {
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        listingDetails.setCaseId("caseId");
        listingDetails.getCaseData().setReportType(CLAIMS_BY_HEARING_VENUE_REPORT);
        listingDetails.getCaseData().setDocumentName(CLAIMS_BY_HEARING_VENUE_REPORT);
        listingDetails.getCaseData().setHearingDateType(RANGE_HEARING_DATE_TYPE);
        listingDetails.getCaseData().setListingDate("2021-12-15");
        listingDetails.getCaseData().setListingDateFrom("2021-12-03");
        listingDetails.getCaseData().setListingDateTo("2021-12-18");

        ClaimsByHearingVenueSubmitEvent submitEvent = new ClaimsByHearingVenueSubmitEvent();
        submitEvent.setCaseData(new ClaimsByHearingVenueCaseData());

        when(ccdClient.claimsByHearingVenueSearch(anyString(), anyString(), anyString()))
                .thenReturn(List.of(submitEvent));
        UserDetails userDetails = new UserDetails();
        when(userService.getUserDetails(anyString())).thenReturn(userDetails);

        ClaimsByHearingVenueReportData listingDataResult =
            (ClaimsByHearingVenueReportData) reportDataService.generateReportData(listingDetails, "authToken");
        assertEquals(CLAIMS_BY_HEARING_VENUE_REPORT, listingDataResult.getDocumentName());
        assertEquals(CLAIMS_BY_HEARING_VENUE_REPORT, listingDataResult.getReportType());
        assertEquals(RANGE_HEARING_DATE_TYPE, listingDataResult.getHearingDateType());
        assertNull(listingDataResult.getListingDate());
        assertEquals("2021-12-03", listingDataResult.getListingDateFrom());
        assertEquals("2021-12-18", listingDataResult.getListingDateTo());
    }

    @Test
    public void generateSessionDaysReportData() throws IOException {
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        listingDetails.setCaseId("caseId");
        listingDetails.getCaseData().setReportType(SESSION_DAYS_REPORT);
        listingDetails.getCaseData().setDocumentName("name");
        listingDetails.getCaseData().setHearingDateType("Ranged");
        listingDetails.getCaseData().setListingDate("2021-07-13");
        listingDetails.getCaseData().setListingDateFrom("2021-07-12");
        listingDetails.getCaseData().setListingDateTo("2021-07-14");
        SessionDaysSubmitEvent submitEvent = new SessionDaysSubmitEvent();
        submitEvent.setCaseData(new SessionDaysCaseData());
        when(ccdClient.sessionDaysSearch(anyString(), anyString(), anyString())).thenReturn(List.of(submitEvent));
        SessionDaysReportData listingDataResult = (SessionDaysReportData)reportDataService
                .generateReportData(listingDetails, "authToken");
        assertEquals("name", listingDataResult.getDocumentName());
        assertEquals(SESSION_DAYS_REPORT, listingDataResult.getReportType());
        assertEquals("Ranged", listingDataResult.getHearingDateType());
        assertEquals("2021-07-13", listingDataResult.getListingDate());
        assertEquals("2021-07-12", listingDataResult.getListingDateFrom());
        assertEquals("2021-07-14", listingDataResult.getListingDateTo());
    }

    @Test
    public void generateEccReportData() throws IOException {
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        listingDetails.setCaseId("caseId");
        listingDetails.getCaseData().setReportType(ECC_REPORT);
        listingDetails.getCaseData().setDocumentName("name");
        listingDetails.getCaseData().setHearingDateType("Ranged");
        listingDetails.getCaseData().setListingDate("2021-07-13");
        listingDetails.getCaseData().setListingDateFrom("2021-07-12");
        listingDetails.getCaseData().setListingDateTo("2021-07-14");
        EccReportSubmitEvent submitEvent = new EccReportSubmitEvent();
        submitEvent.setCaseData(new EccReportCaseData());
        when(ccdClient.eccReportSearch(anyString(), anyString(), anyString())).thenReturn(List.of(submitEvent));
        EccReportData listingDataResult = (EccReportData) reportDataService.generateReportData(listingDetails,
            "authToken");
        assertEquals("name", listingDataResult.getDocumentName());
        assertEquals(ECC_REPORT, listingDataResult.getReportType());
        assertEquals("Ranged", listingDataResult.getHearingDateType());
        assertEquals("2021-07-13", listingDataResult.getListingDate());
        assertEquals("2021-07-12", listingDataResult.getListingDateFrom());
        assertEquals("2021-07-14", listingDataResult.getListingDateTo());
    }

    @Test
    public void generateHearingsByHearingTypeReportData() throws IOException {
        listingDetails.setCaseTypeId(ENGLANDWALES_LISTING_CASE_TYPE_ID);
        listingDetails.getCaseData().setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        listingDetails.setCaseId("caseId");
        listingDetails.getCaseData().setReportType(HEARINGS_BY_HEARING_TYPE_REPORT);
        listingDetails.getCaseData().setDocumentName("name");
        listingDetails.getCaseData().setHearingDateType("Ranged");
        listingDetails.getCaseData().setListingDate("2021-07-13");
        listingDetails.getCaseData().setListingDateFrom("2021-07-12");
        listingDetails.getCaseData().setListingDateTo("2021-07-14");
        HearingsByHearingTypeSubmitEvent submitEvent = new HearingsByHearingTypeSubmitEvent();
        submitEvent.setCaseData(new HearingsByHearingTypeCaseData());
        when(ccdClient.hearingsByHearingTypeSearch(anyString(), anyString(), anyString()))
                .thenReturn(List.of(submitEvent));
        HearingsByHearingTypeReportData listingDataResult = (HearingsByHearingTypeReportData)reportDataService
                .generateReportData(listingDetails, "authToken");
        assertEquals("name", listingDataResult.getDocumentName());
        assertEquals(HEARINGS_BY_HEARING_TYPE_REPORT, listingDataResult.getReportType());
        assertEquals("Ranged", listingDataResult.getHearingDateType());
        assertEquals("2021-07-13", listingDataResult.getListingDate());
        assertEquals("2021-07-12", listingDataResult.getListingDateFrom());
        assertEquals("2021-07-14", listingDataResult.getListingDateTo());
    }

}
