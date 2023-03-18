package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.AllocateHearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AllocateHearingType;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.SelectionServiceTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.CourtWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection.CourtWorkerSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection.JudgeSelectionService;
import java.util.List;
import java.util.UUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;

@SuppressWarnings({"PMD.TooManyMethods"})
public class AllocateHearingServiceTest {

    private AllocateHearingService allocateHearingService;
    private RoomSelectionService roomSelectionService;

    private CaseData caseData;
    private final TribunalOffice tribunalOffice = TribunalOffice.MANCHESTER;
    private HearingType selectedHearing;
    private DateListedType selectedListing;

    @Before
    public void setup() {
        caseData = createCaseData();

        HearingSelectionService hearingSelectionService = mockHearingSelectionService();
        JudgeSelectionService judgeSelectionService = mockJudgeSelectionService();
        VenueSelectionService venueSelectionService = mockVenueSelectionService();
        roomSelectionService = mockRoomSelectionService();
        CourtWorkerSelectionService courtWorkerSelectionService = mockCourtWorkerSelectionService();
        allocateHearingService = new AllocateHearingService(hearingSelectionService, judgeSelectionService,
                venueSelectionService, roomSelectionService, courtWorkerSelectionService);
    }

    @Test
    public void testInitialiseAllocatedHearing() {
        allocateHearingService.initialiseAllocateHearing(caseData);

        DynamicFixedListType hearings = caseData.getAllocateHearingHearing();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(hearings, "hearing", "Hearing ");
    }

    @Test
    public void testHandleListingSelectedNoExistingSelections() {
        // Arrange
        String hearingStatus = Constants.HEARING_STATUS_HEARD;
        String postponedBy = "Barney";
        selectedListing.setHearingStatus(hearingStatus);
        selectedListing.setPostponedBy(postponedBy);

        // Act
        allocateHearingService.handleListingSelected(caseData);

        // Assert
        DynamicFixedListType judges = caseData.getAllocateHearingJudge();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(judges, "judge", "Judge ");
        DynamicFixedListType venues = caseData.getAllocateHearingCollection()
                .get(0).getValue().getAllocateHearingVenue();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(
                venues, "venue", "Venue ");
        DynamicFixedListType clerks = caseData.getAllocateHearingCollection()
                .get(0).getValue().getAllocateHearingClerk();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(
                clerks, "clerk", "Clerk ");
        DynamicFixedListType employerMembers = caseData.getAllocateHearingEmployerMember();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(
                employerMembers, "employerMember", "Employer Member ");
        DynamicFixedListType employeeMembers = caseData.getAllocateHearingEmployeeMember();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(
                employeeMembers, "employeeMember", "Employee Member ");
        String hearingSitAlone = String.valueOf(Boolean.TRUE);
        assertEquals(hearingSitAlone, caseData.getAllocateHearingSitAlone());
        assertEquals(postponedBy,  caseData.getAllocateHearingCollection()
                .get(0).getValue().getAllocateHearingPostponedBy());
        assertEquals(hearingStatus,  caseData.getAllocateHearingCollection()
                .get(0).getValue().getAllocateHearingStatus());
    }

    @Test
    public void testHandleListingSelectedWithExistingSelections() {
        // Arrange
        String hearingSitAlone = String.valueOf(Boolean.TRUE);
        selectedHearing.setHearingSitAlone(hearingSitAlone);
        DynamicValueType selectedEmployerMember = DynamicValueType.create("employerMember2",
            "Employer Member 2");
        selectedHearing.setHearingERMember(DynamicFixedListType.of(selectedEmployerMember));
        DynamicValueType selectedEmployeeMember = DynamicValueType.create("employeeMember2",
            "Employee Member 2");
        selectedHearing.setHearingEEMember(DynamicFixedListType.of(selectedEmployeeMember));

        String hearingStatus = Constants.HEARING_STATUS_HEARD;
        String postponedBy = "Barney";
        selectedListing.setHearingStatus(hearingStatus);
        selectedListing.setPostponedBy(postponedBy);
        DynamicValueType selectedClerk = DynamicValueType.create("clerk2", "Clerk 2");
        selectedListing.setHearingClerk(DynamicFixedListType.of(selectedClerk));

        // Act
        allocateHearingService.handleListingSelected(caseData);

        // Assert
        AllocateHearingType allocateHearingType = caseData.getAllocateHearingCollection().get(0).getValue();
        DynamicFixedListType judges = caseData.getAllocateHearingJudge();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(judges, "judge", "Judge ");
        DynamicFixedListType venues = allocateHearingType.getAllocateHearingVenue();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(venues, "venue", "Venue ");
        DynamicFixedListType clerks = allocateHearingType.getAllocateHearingClerk();
        SelectionServiceTestUtils.verifyDynamicFixedListSelected(clerks, "clerk", "Clerk ",
            selectedClerk);
        DynamicFixedListType employerMembers = caseData.getAllocateHearingEmployerMember();
        SelectionServiceTestUtils.verifyDynamicFixedListSelected(employerMembers, "employerMember",
            "Employer Member ",
                selectedEmployerMember);
        DynamicFixedListType employeeMembers = caseData.getAllocateHearingEmployeeMember();
        SelectionServiceTestUtils.verifyDynamicFixedListSelected(employeeMembers, "employeeMember",
            "Employee Member ",
                selectedEmployeeMember);

        assertEquals(hearingSitAlone, caseData.getAllocateHearingSitAlone());
        assertEquals(postponedBy, allocateHearingType.getAllocateHearingPostponedBy());
        assertEquals(hearingStatus, allocateHearingType.getAllocateHearingStatus());
    }

    @Test
    public void testPopulateRooms() {
        AllocateHearingType allocateHearingType = caseData.getAllocateHearingCollection().get(0).getValue();
        allocateHearingService.populateRooms(caseData);
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(allocateHearingType.getAllocateHearingRoom(),
                "code", "label");
    }

    @Test
    public void testPopulateRoomsNewVenueSelected() {
        selectedListing.setHearingVenueDay(DynamicFixedListType.of(DynamicValueType.create("code1",
            "label1")));
        selectedListing.setHearingRoom(DynamicFixedListType.of(DynamicValueType.create("code1",
            "label1")));
        caseData.setAllocateHearingVenue(DynamicFixedListType.of(DynamicValueType.create("code1",
            "label1")));
        selectedListing.setListedDate("2022-02-11 11:00:00");
        allocateHearingService.populateRooms(caseData);

        Mockito.verify(roomSelectionService, Mockito.times(1)).createRoomSelection(
                caseData.getAllocateHearingCollection().get(0).getValue().getAllocateHearingVenue(),
                selectedListing,
            false);
    }

    @Test
    public void testPopulateRoomsExistingVenueSelected() {
        DynamicValueType dynamicValueType1 = new DynamicValueType();
        dynamicValueType1.setCode("code1");
        dynamicValueType1.setLabel("label1");
        DynamicValueType dynamicValueType2 = new DynamicValueType();
        dynamicValueType2.setCode("code2");
        dynamicValueType2.setLabel("label2");
        DynamicValueType dynamicValueType3 = new DynamicValueType();
        dynamicValueType3.setCode("code3");
        dynamicValueType3.setLabel("label3");
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(List.of(dynamicValueType1, dynamicValueType2, dynamicValueType3));
        dynamicFixedListType.setValue(dynamicValueType1);
        selectedListing.setHearingVenueDay(dynamicFixedListType);
        DynamicValueType selectedRoom = DynamicValueType.create("room1", "Room 1");
        selectedListing.setHearingRoom(DynamicFixedListType.of(selectedRoom));
        caseData.setAllocateHearingVenue(DynamicFixedListType.of(DynamicValueType.create("venue1", "venue1")));
        selectedListing.setListedDate("2022-02-11 11:00:00");
        allocateHearingService.populateRooms(caseData);

        Mockito.verify(roomSelectionService, Mockito.times(1)).createRoomSelection(
                caseData.getAllocateHearingCollection().get(0).getValue().getAllocateHearingVenue(),
                selectedListing,
            false);
    }

    @Test
    public void testUpdateCase() {
        // Arrange
        String sitAlone = String.valueOf(Boolean.TRUE);
        DynamicFixedListType employerMember = DynamicFixedListType.of(DynamicValueType.create("employerMember2",
                "Employer Member 2"));
        DynamicFixedListType employeeMember = DynamicFixedListType.of(DynamicValueType.create("employeeMember2",
                "Employee Member 2"));
        caseData.setAllocateHearingSitAlone(sitAlone);
        DynamicFixedListType judge = DynamicFixedListType.of(DynamicValueType.create("judge2", "Judge 2"));
        caseData.setAllocateHearingJudge(judge);
        caseData.setAllocateHearingEmployerMember(employerMember);
        caseData.setAllocateHearingEmployeeMember(employeeMember);
        AllocateHearingTypeItem allocateHearingTypeItem = new AllocateHearingTypeItem();
        allocateHearingTypeItem.setId(UUID.randomUUID().toString());
        AllocateHearingType allocateHearingType = new AllocateHearingType();
        String hearingStatus = Constants.HEARING_STATUS_POSTPONED;
        String postponedBy = "Doris";
        allocateHearingType.setAllocateHearingStatus(hearingStatus);
        allocateHearingType.setAllocateHearingPostponedBy(postponedBy);
        DynamicFixedListType venue = DynamicFixedListType.of(DynamicValueType.create("venue2", "Venue 2"));
        DynamicFixedListType room = DynamicFixedListType.of(DynamicValueType.create("room2", "Room 2"));
        DynamicFixedListType clerk = DynamicFixedListType.of(DynamicValueType.create("clerk2", "Clerk 2"));
        allocateHearingType.setAllocateHearingVenue(venue);
        allocateHearingType.setAllocateHearingRoom(room);
        allocateHearingType.setAllocateHearingClerk(clerk);
        allocateHearingType.setAllocateHearingDate("2022-02-11 11:00:00");
        allocateHearingTypeItem.setValue(allocateHearingType);
        caseData.setAllocateHearingCollection(List.of(allocateHearingTypeItem));
        selectedListing.setListedDate("2022-02-11 11:00:00");
        // Act
        allocateHearingService.updateCase(caseData);

        // Assert
        assertEquals(sitAlone, selectedHearing.getHearingSitAlone());
        assertEquals(judge.getSelectedCode(), selectedHearing.getJudge().getSelectedCode());
        assertEquals(judge.getSelectedLabel(), selectedHearing.getJudge().getSelectedLabel());
        assertEquals(employerMember.getSelectedCode(), selectedHearing.getHearingERMember().getSelectedCode());
        assertEquals(employerMember.getSelectedLabel(), selectedHearing.getHearingERMember().getSelectedLabel());
        assertEquals(employeeMember.getSelectedCode(), selectedHearing.getHearingEEMember().getSelectedCode());
        assertEquals(employeeMember.getSelectedLabel(), selectedHearing.getHearingEEMember().getSelectedLabel());
        assertEquals(hearingStatus, selectedListing.getHearingStatus());
        assertEquals(postponedBy, selectedListing.getPostponedBy());
        assertEquals(venue.getSelectedCode(), selectedListing.getHearingVenueDay().getSelectedCode());
        assertEquals(venue.getSelectedLabel(), selectedListing.getHearingVenueDay().getSelectedLabel());
        assertEquals(room.getSelectedCode(), selectedListing.getHearingRoom().getSelectedCode());
        assertEquals(room.getSelectedLabel(), selectedListing.getHearingRoom().getSelectedLabel());
        assertEquals(clerk.getSelectedCode(), selectedListing.getHearingClerk().getSelectedCode());
        assertEquals(clerk.getSelectedLabel(), selectedListing.getHearingClerk().getSelectedLabel());
        assertNotNull(selectedListing.getPostponedDate());
    }

    private CaseData createCaseData() {
        CaseData caseData = SelectionServiceTestUtils.createCaseData(tribunalOffice);
        caseData.setAllocateHearingHearing(new DynamicFixedListType());
        AllocateHearingTypeItem allocateHearingTypeItem = new AllocateHearingTypeItem();
        allocateHearingTypeItem.setId(UUID.randomUUID().toString());
        DynamicValueType dynamicValueType1 = new DynamicValueType();
        DynamicValueType dynamicValueType2 = new DynamicValueType();
        dynamicValueType1.setCode("code1");
        dynamicValueType1.setLabel("label1");
        dynamicValueType2.setCode("code2");
        dynamicValueType2.setLabel("label2");
        DynamicValueType dynamicValueType3 = new DynamicValueType();
        dynamicValueType3.setCode("code3");
        dynamicValueType3.setLabel("label3");
        AllocateHearingType allocateHearingType = new AllocateHearingType();
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(List.of(dynamicValueType1, dynamicValueType2, dynamicValueType3));
        dynamicFixedListType.setValue(dynamicValueType1);
        allocateHearingType.setAllocateHearingRoom(dynamicFixedListType);
        allocateHearingType.setAllocateHearingVenue(dynamicFixedListType);
        allocateHearingType.setAllocateHearingClerk(dynamicFixedListType);
        allocateHearingType.setAllocateHearingPostponedBy("clerk");
        allocateHearingType.setAllocateHearingStatus("Heard");
        allocateHearingType.setAllocateHearingDate("2022-02-11 11:00:00");
        allocateHearingTypeItem.setValue(allocateHearingType);
        caseData.setAllocateHearingCollection(List.of(allocateHearingTypeItem));
        selectedHearing = new HearingType();
        selectedListing = new DateListedType();
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setValue(selectedListing);
        selectedHearing.setHearingDateCollection(List.of(dateListedTypeItem));
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setValue(selectedHearing);
        caseData.setHearingCollection(List.of(hearingTypeItem));

        return caseData;
    }

    private HearingSelectionService mockHearingSelectionService() {
        HearingSelectionService hearingSelectionService = Mockito.mock(HearingSelectionService.class);
        List<DynamicValueType> hearings = SelectionServiceTestUtils.createListItems("hearing",
            "Hearing ");
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setId(UUID.randomUUID().toString());
        dateListedTypeItem.setValue(selectedListing);
        Mockito.when(hearingSelectionService.getHearingSelection(isA(CaseData.class))).thenReturn(hearings);

        Mockito.when(hearingSelectionService.getSelectedHearing(isA(CaseData.class),
                isA(DynamicFixedListType.class))).thenReturn(selectedHearing);
        Mockito.when(hearingSelectionService.getListings(isA(CaseData.class),
                isA(DynamicFixedListType.class))).thenReturn(List.of(dateListedTypeItem));

        return hearingSelectionService;
    }

    private JudgeSelectionService mockJudgeSelectionService() {
        JudgeSelectionService judgeSelectionService = Mockito.mock(JudgeSelectionService.class);
        List<DynamicValueType> judges = SelectionServiceTestUtils.createListItems("judge", "Judge ");
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(judges);
        Mockito.when(judgeSelectionService.createJudgeSelection(isA(TribunalOffice.class),
                isA(HearingType.class))).thenReturn(dynamicFixedListType);

        return judgeSelectionService;
    }

    private VenueSelectionService mockVenueSelectionService() {
        VenueSelectionService venueSelectionService = Mockito.mock(VenueSelectionService.class);
        List<DynamicValueType> venues = SelectionServiceTestUtils.createListItems("venue", "Venue ");
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(venues);
        Mockito.when(venueSelectionService.createVenueSelection(isA(TribunalOffice.class),
                isA(DateListedType.class))).thenReturn(dynamicFixedListType);

        return venueSelectionService;
    }

    private RoomSelectionService mockRoomSelectionService() {
        RoomSelectionService roomSelectionService = Mockito.mock(RoomSelectionService.class);
        List<DynamicValueType> rooms = SelectionServiceTestUtils.createListItems("room", "Room ");
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(rooms);
        Mockito.when(roomSelectionService.createRoomSelection(isA(DynamicFixedListType.class),
                isA(DateListedType.class), isA(Boolean.class))).thenReturn(dynamicFixedListType);

        return roomSelectionService;
    }

    private CourtWorkerSelectionService mockCourtWorkerSelectionService() {
        CourtWorkerService courtWorkerService = Mockito.mock(CourtWorkerService.class);
        List<DynamicValueType> clerks = SelectionServiceTestUtils.createListItems("clerk", "Clerk ");
        Mockito.when(courtWorkerService.getCourtWorkerByTribunalOffice(tribunalOffice,
                CourtWorkerType.CLERK)).thenReturn(clerks);

        List<DynamicValueType> employerMembers = SelectionServiceTestUtils.createListItems("employerMember",
            "Employer Member ");
        Mockito.when(courtWorkerService.getCourtWorkerByTribunalOffice(tribunalOffice,
                CourtWorkerType.EMPLOYER_MEMBER)).thenReturn(employerMembers);

        List<DynamicValueType> employeeMembers = SelectionServiceTestUtils.createListItems("employeeMember",
            "Employee Member ");
        Mockito.when(courtWorkerService.getCourtWorkerByTribunalOffice(tribunalOffice,
                CourtWorkerType.EMPLOYEE_MEMBER)).thenReturn(employeeMembers);

        return new CourtWorkerSelectionService(courtWorkerService);
    }
}
