package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.HearingSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.SelectionServiceTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.CourtWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection.CourtWorkerSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.selection.JudgeSelectionService;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        String hearingSitAlone = String.valueOf(Boolean.TRUE);
        selectedHearing.setHearingSitAlone(hearingSitAlone);

        String hearingStatus = Constants.HEARING_STATUS_HEARD;
        String postponedBy = "Barney";
        selectedListing.setHearingStatus(hearingStatus);
        selectedListing.setPostponedBy(postponedBy);

        // Act
        allocateHearingService.handleListingSelected(caseData);

        // Assert
        DynamicFixedListType judges = caseData.getAllocateHearingJudge();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(judges, "judge", "Judge ");
        DynamicFixedListType venues = caseData.getAllocateHearingVenue();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(venues, "venue", "Venue ");
        DynamicFixedListType clerks = caseData.getAllocateHearingClerk();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(clerks, "clerk", "Clerk ");
        DynamicFixedListType employerMembers = caseData.getAllocateHearingEmployerMember();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(
                employerMembers, "employerMember", "Employer Member ");
        DynamicFixedListType employeeMembers = caseData.getAllocateHearingEmployeeMember();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(
                employeeMembers, "employeeMember", "Employee Member ");

        assertEquals(hearingSitAlone, caseData.getAllocateHearingSitAlone());
        assertEquals(postponedBy, caseData.getAllocateHearingPostponedBy());
        assertEquals(hearingStatus, caseData.getAllocateHearingStatus());
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
        DynamicFixedListType judges = caseData.getAllocateHearingJudge();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(judges, "judge", "Judge ");
        DynamicFixedListType venues = caseData.getAllocateHearingVenue();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(venues, "venue", "Venue ");
        DynamicFixedListType clerks = caseData.getAllocateHearingClerk();
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
        assertEquals(postponedBy, caseData.getAllocateHearingPostponedBy());
        assertEquals(hearingStatus, caseData.getAllocateHearingStatus());
    }

    @Test
    public void testPopulateRooms() {
        allocateHearingService.populateRooms(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(caseData.getAllocateHearingRoom(),
                "room", "Room ");
    }

    @Test
    public void testPopulateRoomsNewVenueSelected() {
        selectedListing.setHearingVenueDay(DynamicFixedListType.of(DynamicValueType.create("venue1",
            "venue1")));
        selectedListing.setHearingRoom(DynamicFixedListType.of(DynamicValueType.create("room1",
            "room1")));
        caseData.setAllocateHearingVenue(DynamicFixedListType.of(DynamicValueType.create("venue2",
            "venue2")));

        allocateHearingService.populateRooms(caseData);

        verify(roomSelectionService, times(1)).createRoomSelection(caseData, selectedListing,
            true);
    }

    @Test
    public void testPopulateRoomsExistingVenueSelected() {
        selectedListing.setHearingVenueDay(DynamicFixedListType.of(DynamicValueType.create("venue1",
            "venue1")));
        DynamicValueType selectedRoom = DynamicValueType.create("room1", "Room 1");
        selectedListing.setHearingRoom(DynamicFixedListType.of(selectedRoom));
        caseData.setAllocateHearingVenue(DynamicFixedListType.of(DynamicValueType.create("venue1",
            "venue1")));

        allocateHearingService.populateRooms(caseData);

        verify(roomSelectionService, times(1)).createRoomSelection(caseData, selectedListing,
            false);
    }

    @Test
    public void testUpdateCase() {
        // Arrange
        String sitAlone = String.valueOf(Boolean.TRUE);
        DynamicFixedListType judge = DynamicFixedListType.of(DynamicValueType.create("judge2", "Judge 2"));
        DynamicFixedListType employerMember = DynamicFixedListType.of(DynamicValueType.create("employerMember2",
            "Employer Member 2"));
        DynamicFixedListType employeeMember = DynamicFixedListType.of(DynamicValueType.create("employeeMember2",
            "Employee Member 2"));
        String hearingStatus = Constants.HEARING_STATUS_POSTPONED;
        String postponedBy = "Doris";
        DynamicFixedListType venue = DynamicFixedListType.of(DynamicValueType.create("venue2", "Venue 2"));
        DynamicFixedListType room = DynamicFixedListType.of(DynamicValueType.create("room2", "Room 2"));
        DynamicFixedListType clerk = DynamicFixedListType.of(DynamicValueType.create("clerk2", "Clerk 2"));
        caseData.setAllocateHearingSitAlone(sitAlone);
        caseData.setAllocateHearingJudge(judge);
        caseData.setAllocateHearingEmployerMember(employerMember);
        caseData.setAllocateHearingEmployeeMember(employeeMember);
        caseData.setAllocateHearingStatus(hearingStatus);
        caseData.setAllocateHearingPostponedBy(postponedBy);
        caseData.setAllocateHearingVenue(venue);
        caseData.setAllocateHearingRoom(room);
        caseData.setAllocateHearingClerk(clerk);

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
        HearingSelectionService hearingSelectionService = mock(HearingSelectionService.class);
        List<DynamicValueType> hearings = SelectionServiceTestUtils.createListItems("hearing",
            "Hearing ");
        when(hearingSelectionService.getHearingSelection(isA(CaseData.class))).thenReturn(hearings);

        when(hearingSelectionService.getSelectedHearing(isA(CaseData.class),
                isA(DynamicFixedListType.class))).thenReturn(selectedHearing);
        when(hearingSelectionService.getSelectedListing(isA(CaseData.class),
                isA(DynamicFixedListType.class))).thenReturn(selectedListing);

        return hearingSelectionService;
    }

    private JudgeSelectionService mockJudgeSelectionService() {
        JudgeSelectionService judgeSelectionService = mock(JudgeSelectionService.class);
        List<DynamicValueType> judges = SelectionServiceTestUtils.createListItems("judge", "Judge ");
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(judges);
        when(judgeSelectionService.createJudgeSelection(isA(TribunalOffice.class),
                isA(HearingType.class))).thenReturn(dynamicFixedListType);

        return judgeSelectionService;
    }

    private VenueSelectionService mockVenueSelectionService() {
        VenueSelectionService venueSelectionService = mock(VenueSelectionService.class);
        List<DynamicValueType> venues = SelectionServiceTestUtils.createListItems("venue", "Venue ");
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(venues);
        when(venueSelectionService.createVenueSelection(isA(TribunalOffice.class),
                isA(DateListedType.class))).thenReturn(dynamicFixedListType);

        return venueSelectionService;
    }

    private RoomSelectionService mockRoomSelectionService() {
        RoomSelectionService roomSelectionService = mock(RoomSelectionService.class);
        List<DynamicValueType> rooms = SelectionServiceTestUtils.createListItems("room", "Room ");
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(rooms);
        when(roomSelectionService.createRoomSelection(isA(CaseData.class),
                isA(DateListedType.class), isA(Boolean.class))).thenReturn(dynamicFixedListType);

        return roomSelectionService;
    }

    private CourtWorkerSelectionService mockCourtWorkerSelectionService() {
        CourtWorkerService courtWorkerService = mock(CourtWorkerService.class);
        List<DynamicValueType> clerks = SelectionServiceTestUtils.createListItems("clerk", "Clerk ");
        when(courtWorkerService.getCourtWorkerByTribunalOffice(tribunalOffice,
                CourtWorkerType.CLERK)).thenReturn(clerks);

        List<DynamicValueType> employerMembers = SelectionServiceTestUtils.createListItems("employerMember",
            "Employer Member ");
        when(courtWorkerService.getCourtWorkerByTribunalOffice(tribunalOffice,
                CourtWorkerType.EMPLOYER_MEMBER)).thenReturn(employerMembers);

        List<DynamicValueType> employeeMembers = SelectionServiceTestUtils.createListItems("employeeMember",
            "Employee Member ");
        when(courtWorkerService.getCourtWorkerByTribunalOffice(tribunalOffice,
                CourtWorkerType.EMPLOYEE_MEMBER)).thenReturn(employeeMembers);

        return new CourtWorkerSelectionService(courtWorkerService);
    }
}
