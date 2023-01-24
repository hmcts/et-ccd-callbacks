package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.jpaservice;

import org.junit.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Room;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.RoomRepository;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JpaRoomServiceTest {
    @Test
    public void testGetRooms() {
        String venueCode = "Venue1";
        List<Room> rooms = List.of(
                createRoom("room1", "Room 1"),
                createRoom("room2", "Room 2"),
                createRoom("room3", "Room 3"));
        RoomRepository roomRepository = mock(RoomRepository.class);
        when(roomRepository.findByVenueCode(venueCode)).thenReturn(rooms);

        JpaRoomService roomService = new JpaRoomService(roomRepository);
        List<DynamicValueType> values = roomService.getRooms(venueCode);

        assertEquals(3, values.size());
        verifyValue(values.get(0), "room1", "Room 1");
        verifyValue(values.get(1), "room2", "Room 2");
        verifyValue(values.get(2), "room3", "Room 3");
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
