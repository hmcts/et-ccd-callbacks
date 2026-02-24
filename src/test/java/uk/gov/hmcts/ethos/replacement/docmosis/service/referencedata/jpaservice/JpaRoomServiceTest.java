package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.jpaservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Room;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.RoomRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class JpaRoomServiceTest {
    @Test
    void testGetRooms_regularVenue_sortedAlphabetically() {
        String venueCode = "Venue1";
        List<Room> rooms = List.of(
                createRoom("room3", "C Room"),
                createRoom("room1", "A Room"),
                createRoom("room2", "B Room"));
        RoomRepository roomRepository = mock(RoomRepository.class);
        when(roomRepository.findByVenueCode(venueCode)).thenReturn(rooms);

        JpaRoomService roomService = new JpaRoomService(roomRepository);
        List<DynamicValueType> values = roomService.getRooms(venueCode);

        assertEquals(3, values.size());
        verifyValue(values.get(0), "room1", "A Room");
        verifyValue(values.get(1), "room2", "B Room");
        verifyValue(values.get(2), "room3", "C Room");
    }

    @Test
    void testGetRooms_regularVenue_zPrefixedRoomsMovedToEnd() {
        String venueCode = "Manchester";
        List<Room> rooms = List.of(
                createRoom("room1", "B Room"),
                createRoom("room2", "z Not Available"),
                createRoom("room3", "A Room"),
                createRoom("room4", "z Other"));
        RoomRepository roomRepository = mock(RoomRepository.class);
        when(roomRepository.findByVenueCode(venueCode)).thenReturn(rooms);

        JpaRoomService roomService = new JpaRoomService(roomRepository);
        List<DynamicValueType> values = roomService.getRooms(venueCode);

        assertEquals(4, values.size());
        verifyValue(values.get(0), "room3", "A Room");
        verifyValue(values.get(1), "room1", "B Room");
        verifyValue(values.get(2), "room2", "z Not Available");
        verifyValue(values.get(3), "room4", "z Other");
    }

    @Test
    void testGetRooms_londonTribunalsCentre_returnsInStoredOrder() {
        String venueCode = "London Tribunals Centre";
        List<Room> rooms = List.of(
                createRoom("room3", "C Room"),
                createRoom("room1", "A Room"),
                createRoom("room2", "B Room"));
        RoomRepository roomRepository = mock(RoomRepository.class);
        when(roomRepository.findByVenueCode(venueCode)).thenReturn(rooms);

        JpaRoomService roomService = new JpaRoomService(roomRepository);
        List<DynamicValueType> values = roomService.getRooms(venueCode);

        assertEquals(3, values.size());
        // Should be in the order they were stored, not sorted
        verifyValue(values.get(0), "room3", "C Room");
        verifyValue(values.get(1), "room1", "A Room");
        verifyValue(values.get(2), "room2", "B Room");
    }

    @Test
    void testGetRooms_londonTribunalsCentre_doesNotSortZPrefixed() {
        String venueCode = "London Tribunals Centre";
        List<Room> rooms = List.of(
                createRoom("room2", "z Not Available"),
                createRoom("room1", "A Room"),
                createRoom("room3", "B Room"));
        RoomRepository roomRepository = mock(RoomRepository.class);
        when(roomRepository.findByVenueCode(venueCode)).thenReturn(rooms);

        JpaRoomService roomService = new JpaRoomService(roomRepository);
        List<DynamicValueType> values = roomService.getRooms(venueCode);

        assertEquals(3, values.size());
        // z prefixed items should stay in their original position for London Tribunals Centre
        verifyValue(values.get(0), "room2", "z Not Available");
        verifyValue(values.get(1), "room1", "A Room");
        verifyValue(values.get(2), "room3", "B Room");
    }

    private Room createRoom(String code, String name) {
        Room room = new Room();
        room.setCode(code);
        room.setName(name);
        return room;
    }

    private void verifyValue(DynamicValueType value, String expectedCode, String expectedLabel) {
        assertEquals(expectedCode, value.getCode());
        assertEquals(expectedLabel, value.getLabel());
    }
}
