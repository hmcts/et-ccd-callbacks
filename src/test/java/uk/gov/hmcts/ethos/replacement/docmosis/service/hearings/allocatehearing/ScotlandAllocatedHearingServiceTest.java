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
import static org.mockito.Mockito.when;

public class ScotlandAllocatedHearingServiceTest {

    private ScotlandAllocateHearingService scotlandAllocateHearingService;

    private CaseData caseData;
    private final TribunalOffice tribunalOffice = TribunalOffice.ABERDEEN;
    private HearingType selectedHearing;
    private DateListedType selectedListing;

    @Before
    public void setup() {
        caseData = createCaseData();

        var hearingSelectionService = mockHearingSelectionService();
        var judgeSelectionService = mockJudgeSelectionService();
        var scotlandVenueSelectionService = mockScotlandVenueSelectionService();
        var courtWorkerSelectionService = mockCourtWorkerSelectionService();
        var roomSelectionService = mockRoomSelectionService();
        scotlandAllocateHearingService = new ScotlandAllocateHearingService(hearingSelectionService,
                judgeSelectionService, scotlandVenueSelectionService, courtWorkerSelectionService, roomSelectionService);
    }

    @Test
    public void testHandleListingSelected() {
        var selectedHearingVenue = tribunalOffice.getOfficeName();
        selectedListing.setHearingVenueDayScotland(selectedHearingVenue);

        scotlandAllocateHearingService.handleListingSelected(caseData);

        assertEquals(selectedHearingVenue, caseData.getAllocateHearingManagingOffice());
    }

    @Test
    public void testHandleManagingOfficeSelected() {
        // Arrange
        var hearingSitAlone = String.valueOf(Boolean.TRUE);
        selectedHearing.setHearingSitAlone(hearingSitAlone);
        var readingDeliberation = "Reading Day";
        selectedListing.setHearingTypeReadingDeliberation(readingDeliberation);
        var hearingStatus = Constants.HEARING_STATUS_HEARD;
        var postponedBy = "Barney";
        selectedListing.setHearingStatus(hearingStatus);
        selectedListing.setPostponedBy(postponedBy);
        caseData.setAllocateHearingManagingOffice(tribunalOffice.getOfficeName());

        // Act
        scotlandAllocateHearingService.handleManagingOfficeSelected(caseData);

        // Assert
        var judges = caseData.getAllocateHearingJudge();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(judges, "judge", "Judge ");
        var venues = caseData.getAllocateHearingVenue();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(venues, "venue", "Venue ");
        var clerks = caseData.getAllocateHearingClerk();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(clerks, "clerk", "Clerk ");
        var employerMembers = caseData.getAllocateHearingEmployerMember();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(employerMembers,
                "employerMember", "Employer Member ");
        var employeeMembers = caseData.getAllocateHearingEmployeeMember();
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(employeeMembers,
                "employeeMember", "Employee Member ");

        assertEquals(hearingSitAlone, caseData.getAllocateHearingSitAlone());
        assertEquals(readingDeliberation, caseData.getAllocateHearingReadingDeliberation());
        assertEquals(postponedBy, caseData.getAllocateHearingPostponedBy());
        assertEquals(hearingStatus, caseData.getAllocateHearingStatus());
    }

    @Test
    public void testPopulateRooms() {
        scotlandAllocateHearingService.populateRooms(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(caseData.getAllocateHearingRoom(),
                "room", "Room ");
    }

    @Test
    public void testUpdateCase() {
        // Arrange
        var sitAlone = String.valueOf(Boolean.TRUE);
        var judge = DynamicFixedListType.of(DynamicValueType.create("judge2", "Judge 2"));
        var employerMember = DynamicFixedListType.of(DynamicValueType.create("employerMember2", "Employer Member 2"));
        var employeeMember = DynamicFixedListType.of(DynamicValueType.create("employeeMember2", "Employee Member 2"));
        var readingDeliberation = "Reading Day";
        var hearingStatus = Constants.HEARING_STATUS_POSTPONED;
        var postponedBy = "Doris";
        var venue = DynamicFixedListType.of(DynamicValueType.create("venue2", "Venue 2"));
        var room = DynamicFixedListType.of(DynamicValueType.create("room2", "Room 2"));
        var clerk = DynamicFixedListType.of(DynamicValueType.create("clerk2", "Clerk 2"));
        caseData.setAllocateHearingSitAlone(sitAlone);
        caseData.setAllocateHearingJudge(judge);
        caseData.setAllocateHearingEmployerMember(employerMember);
        caseData.setAllocateHearingEmployeeMember(employeeMember);
        caseData.setAllocateHearingReadingDeliberation(readingDeliberation);
        caseData.setAllocateHearingStatus(hearingStatus);
        caseData.setAllocateHearingPostponedBy(postponedBy);
        caseData.setAllocateHearingVenue(venue);
        caseData.setAllocateHearingRoom(room);
        caseData.setAllocateHearingClerk(clerk);

        for (TribunalOffice scotlandTribunalOffice : TribunalOffice.SCOTLAND_OFFICES) {
            caseData.setAllocateHearingManagingOffice(scotlandTribunalOffice.getOfficeName());

            // Act
            scotlandAllocateHearingService.updateCase(caseData);

            // Assert
            assertEquals(sitAlone, selectedHearing.getHearingSitAlone());
            assertEquals(judge.getSelectedCode(), selectedHearing.getJudge().getSelectedCode());
            assertEquals(judge.getSelectedLabel(), selectedHearing.getJudge().getSelectedLabel());
            assertEquals(employerMember.getSelectedCode(), selectedHearing.getHearingERMember().getSelectedCode());
            assertEquals(employerMember.getSelectedLabel(), selectedHearing.getHearingERMember().getSelectedLabel());
            assertEquals(employeeMember.getSelectedCode(), selectedHearing.getHearingEEMember().getSelectedCode());
            assertEquals(employeeMember.getSelectedLabel(), selectedHearing.getHearingEEMember().getSelectedLabel());
            assertEquals(readingDeliberation, selectedListing.getHearingTypeReadingDeliberation());
            assertEquals(hearingStatus, selectedListing.getHearingStatus());
            assertEquals(postponedBy, selectedListing.getPostponedBy());

            switch (scotlandTribunalOffice) {
                case ABERDEEN:
                    verifyVenue(venue, selectedHearing.getHearingAberdeen(), selectedListing.getHearingAberdeen());
                    break;
                case DUNDEE:
                    verifyVenue(venue, selectedHearing.getHearingDundee(), selectedListing.getHearingDundee());
                    break;
                case EDINBURGH:
                    verifyVenue(venue, selectedHearing.getHearingEdinburgh(), selectedListing.getHearingEdinburgh());
                    break;
                case GLASGOW:
                    verifyVenue(venue, selectedHearing.getHearingGlasgow(), selectedListing.getHearingGlasgow());
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected Scotland Tribunal Office " + scotlandTribunalOffice);
            }

            assertEquals(room.getSelectedCode(), selectedListing.getHearingRoom().getSelectedCode());
            assertEquals(room.getSelectedLabel(), selectedListing.getHearingRoom().getSelectedLabel());
            assertEquals(clerk.getSelectedCode(), selectedListing.getHearingClerk().getSelectedCode());
            assertEquals(clerk.getSelectedLabel(), selectedListing.getHearingClerk().getSelectedLabel());
            assertNotNull(selectedListing.getPostponedDate());
        }
    }

    private void verifyVenue(DynamicFixedListType expectedValue, DynamicFixedListType hearingVenue,
                             DynamicFixedListType listingVenue) {
        assertEquals(expectedValue.getSelectedCode(), hearingVenue.getSelectedCode());
        assertEquals(expectedValue.getSelectedLabel(), hearingVenue.getSelectedLabel());
        assertEquals(expectedValue.getSelectedCode(), listingVenue.getSelectedCode());
        assertEquals(expectedValue.getSelectedLabel(), listingVenue.getSelectedLabel());
    }

    private CaseData createCaseData() {
        var caseData = SelectionServiceTestUtils.createCaseData(tribunalOffice);
        caseData.setAllocateHearingHearing(
                SelectionServiceTestUtils.createSelectedDynamicList("hearing ", "Hearing ", 1));

        selectedHearing = new HearingType();
        selectedListing = new DateListedType();
        var dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setValue(selectedListing);
        selectedHearing.setHearingDateCollection(List.of(dateListedTypeItem));
        var hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setValue(selectedHearing);
        caseData.setHearingCollection(List.of(hearingTypeItem));

        return caseData;
    }

    private HearingSelectionService mockHearingSelectionService() {
        var hearingSelectionService = mock(HearingSelectionService.class);
        var hearings = SelectionServiceTestUtils.createListItems("hearing", "Hearing ");
        when(hearingSelectionService.getHearingSelection(isA(CaseData.class))).thenReturn(hearings);

        when(hearingSelectionService.getSelectedHearing(isA(CaseData.class),
                isA(DynamicFixedListType.class))).thenReturn(selectedHearing);
        when(hearingSelectionService.getSelectedListing(isA(CaseData.class),
                isA(DynamicFixedListType.class))).thenReturn(selectedListing);

        return hearingSelectionService;
    }

    private JudgeSelectionService mockJudgeSelectionService() {
        var judgeSelectionService = mock(JudgeSelectionService.class);
        var judges = SelectionServiceTestUtils.createListItems("judge", "Judge ");
        var dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(judges);
        when(judgeSelectionService.createJudgeSelection(isA(TribunalOffice.class),
                isA(HearingType.class))).thenReturn(dynamicFixedListType);

        return judgeSelectionService;
    }

    private ScotlandVenueSelectionService mockScotlandVenueSelectionService() {
        var venueSelectionService = mock(ScotlandVenueSelectionService.class);
        var venues = SelectionServiceTestUtils.createListItems("venue", "Venue ");
        var dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(venues);
        when(venueSelectionService.createVenueSelection(isA(TribunalOffice.class),
                isA(DateListedType.class))).thenReturn(dynamicFixedListType);

        return venueSelectionService;
    }

    private CourtWorkerSelectionService mockCourtWorkerSelectionService() {
        var courtWorkerService = mock(CourtWorkerService.class);
        var clerks = SelectionServiceTestUtils.createListItems("clerk", "Clerk ");
        when(courtWorkerService.getCourtWorkerByTribunalOffice(TribunalOffice.SCOTLAND,
                CourtWorkerType.CLERK)).thenReturn(clerks);

        var employerMembers = SelectionServiceTestUtils.createListItems("employerMember", "Employer Member ");
        when(courtWorkerService.getCourtWorkerByTribunalOffice(TribunalOffice.SCOTLAND,
                CourtWorkerType.EMPLOYER_MEMBER)).thenReturn(employerMembers);

        var employeeMembers = SelectionServiceTestUtils.createListItems("employeeMember", "Employee Member ");
        when(courtWorkerService.getCourtWorkerByTribunalOffice(TribunalOffice.SCOTLAND,
                CourtWorkerType.EMPLOYEE_MEMBER)).thenReturn(employeeMembers);

        return new CourtWorkerSelectionService(courtWorkerService);
    }

    private RoomSelectionService mockRoomSelectionService() {
        var roomSelectionService = mock(RoomSelectionService.class);
        var rooms = SelectionServiceTestUtils.createListItems("room", "Room ");
        var dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(rooms);
        when(roomSelectionService.createRoomSelection(isA(CaseData.class),
                isA(DateListedType.class), isA(Boolean.class))).thenReturn(dynamicFixedListType);

        return roomSelectionService;
    }
}
