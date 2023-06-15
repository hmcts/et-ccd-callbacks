package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.SelectionServiceTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.RoomService;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class RoomSelectionServiceTest {
    @Test
    void testCreateRoomSelectionNoSelectedRoom() {
        CaseData caseData = mockCaseData();
        RoomService roomService = mockRoomService();
        DateListedType selectedListing = mockSelectedListing(null);

        RoomSelectionService roomSelectionService = new RoomSelectionService(roomService);
        DynamicFixedListType actualResult = roomSelectionService.createRoomSelection(caseData, selectedListing, false);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(actualResult, "room", "Room ");
    }

    @Test
    void testCreateRoomSelectionWithSelectedRoom() {
        CaseData caseData = mockCaseData();
        RoomService roomService = mockRoomService();
        DynamicValueType selectedRoom = DynamicValueType.create("room2", "Room 2");
        DateListedType selectedListing = mockSelectedListing(selectedRoom);

        RoomSelectionService roomSelectionService = new RoomSelectionService(roomService);
        DynamicFixedListType actualResult = roomSelectionService.createRoomSelection(caseData, selectedListing, false);

        SelectionServiceTestUtils.verifyDynamicFixedListSelected(actualResult, "room", "Room ", selectedRoom);
    }

    @Test
    void testCreateRoomSelectionVenueChangedNoSelectedRoom() {
        CaseData caseData = mockCaseData();
        RoomService roomService = mockRoomService();
        DateListedType selectedListing = mockSelectedListing(null);

        RoomSelectionService roomSelectionService = new RoomSelectionService(roomService);
        DynamicFixedListType actualResult = roomSelectionService.createRoomSelection(caseData, selectedListing, true);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(actualResult, "room", "Room ");
    }

    @Test
    void testCreateRoomSelectionVenueChangedWithSelectedRoom() {
        CaseData caseData = mockCaseData();
        RoomService roomService = mockRoomService();
        DynamicValueType selectedRoom = DynamicValueType.create("room2", "Room 2");
        DateListedType selectedListing = mockSelectedListing(selectedRoom);

        RoomSelectionService roomSelectionService = new RoomSelectionService(roomService);
        DynamicFixedListType actualResult = roomSelectionService.createRoomSelection(caseData, selectedListing, true);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(actualResult, "room", "Room ");
    }

    private CaseData mockCaseData() {
        CaseData caseData = mock(CaseData.class);
        DynamicFixedListType venue = new DynamicFixedListType();
        venue.setValue(DynamicValueType.create("venue1", "Venue 1"));
        when(caseData.getAllocateHearingVenue()).thenReturn(venue);

        return caseData;
    }

    private RoomService mockRoomService() {
        List<DynamicValueType> dynamicValues = SelectionServiceTestUtils.createListItems("room", "Room ");

        RoomService roomService = mock(RoomService.class);
        when(roomService.getRooms("venue1")).thenReturn(dynamicValues);
        return roomService;
    }

    private DateListedType mockSelectedListing(DynamicValueType selectedRoom) {
        DateListedType listing = new DateListedType();
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        listing.setHearingRoom(dynamicFixedListType);
        if (selectedRoom != null) {
            dynamicFixedListType.setValue(selectedRoom);
        }

        return listing;
    }
}
