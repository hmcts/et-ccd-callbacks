package uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing;

import org.junit.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.SelectionServiceTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.RoomService;
import java.util.List;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RoomSelectionServiceTest {
    @Test
    public void testCreateRoomSelectionNoSelectedRoom() {
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode("venue1");
        dynamicValueType.setLabel("venue1");
        dynamicFixedListType.setValue(dynamicValueType);
        dynamicFixedListType.setListItems(List.of(dynamicValueType));
        RoomService roomService = mockRoomService();
        RoomSelectionService roomSelectionService = new RoomSelectionService(roomService);
        DateListedType selectedListing = mockSelectedListing(null);
        DynamicFixedListType actualResult = roomSelectionService.createRoomSelection(
                dynamicFixedListType,
                selectedListing,
                false);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(actualResult, "room", "Room ");
    }

    @Test
    public void testCreateRoomSelectionWithSelectedRoom() {
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode("venue1");
        dynamicValueType.setLabel("venue1");
        dynamicFixedListType.setValue(dynamicValueType);
        dynamicFixedListType.setListItems(List.of(dynamicValueType));
        RoomService roomService = mockRoomService();
        RoomSelectionService roomSelectionService = new RoomSelectionService(roomService);
        DynamicValueType selectedRoom = DynamicValueType.create("room2", "Room 2");
        DateListedType selectedListing = mockSelectedListing(selectedRoom);
        DynamicFixedListType actualResult = roomSelectionService.createRoomSelection(
                dynamicFixedListType, selectedListing, false);

        SelectionServiceTestUtils.verifyDynamicFixedListSelected(
                actualResult, "room", "Room ", selectedRoom);
    }

    @Test
    public void testCreateRoomSelectionVenueChangedNoSelectedRoom() {
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode("venue1");
        dynamicValueType.setLabel("venue1");
        dynamicFixedListType.setValue(dynamicValueType);
        dynamicFixedListType.setListItems(List.of(dynamicValueType));
        DateListedType selectedListing = mockSelectedListing(null);
        RoomService roomService = mockRoomService();
        RoomSelectionService roomSelectionService = new RoomSelectionService(roomService);
        DynamicFixedListType actualResult = roomSelectionService.createRoomSelection(
                dynamicFixedListType, selectedListing, true);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(
                actualResult, "room", "Room ");
    }

    @Test
    public void testCreateRoomSelectionVenueChangedWithSelectedRoom() {
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode("venue1");
        dynamicValueType.setLabel("venue1");
        dynamicFixedListType.setValue(dynamicValueType);
        dynamicFixedListType.setListItems(List.of(dynamicValueType));
        RoomService roomService = mockRoomService();
        RoomSelectionService roomSelectionService = new RoomSelectionService(roomService);
        DynamicValueType selectedRoom = DynamicValueType.create("room2", "Room 2");
        DateListedType selectedListing = mockSelectedListing(selectedRoom);
        DynamicFixedListType actualResult = roomSelectionService.createRoomSelection(
                dynamicFixedListType,
                selectedListing,
                true);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(actualResult, "room", "Room ");
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
