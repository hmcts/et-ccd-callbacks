package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Room;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Venue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
class RoomRowHandler {

    private final TribunalOffice tribunalOffice;
    private final FixedListMappings fixedListMappings;
    private final List<Venue> venues;
    private final List<Room> rooms = new ArrayList<>();

    RoomRowHandler(TribunalOffice tribunalOffice, FixedListMappings fixedListMappings, List<Venue> venues) {
        this.tribunalOffice = tribunalOffice;
        this.fixedListMappings = fixedListMappings;
        this.venues = venues;
    }

    boolean accept(Row row) {
        Cell cell = row.getCell(0);
        if (cell == null) {
            return false;
        }
        String listId = cell.getStringCellValue();
        return getVenueCodeForRoomsListId(listId).isPresent();
    }

    void handle(Row row) {
        Room room = convertRowToRoom(row);
        rooms.add(room);
        log.info(String.format("Found room %s for venue %s", room.getCode(), room.getVenueCode()));
    }

    List<Room> getRooms() {
        return rooms;
    }

    private Optional<String> getVenueCodeForRoomsListId(String roomsListId) {
        if (fixedListMappings.getRooms().containsKey(tribunalOffice)) {
            Map<String, String> officeMappings = fixedListMappings.getRooms().get(tribunalOffice);
            if (officeMappings.containsKey(roomsListId)) {
                return Optional.of(officeMappings.get(roomsListId));
            }
        }

        return venues.stream()
                .map(Venue::getCode)
                .filter(venueCode -> venueCode.replace(" ", "").equals(roomsListId)).findFirst();
    }

    private Room convertRowToRoom(Row row) {
        String listId = getCellValue(row.getCell(0));
        Optional<String> venueCode = getVenueCodeForRoomsListId(listId);
        if (venueCode.isEmpty()) {
            throw new IllegalArgumentException(String.format("No venue found for %s", listId));
        }

        String code = getCellValue(row.getCell(1));
        String name = getCellValue(row.getCell(2));

        Room room = new Room();
        room.setCode(code);
        room.setName(name);
        room.setVenueCode(venueCode.get());

        return room;
    }

    private String getCellValue(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                DataFormatter dataFormatter = new DataFormatter();
                return dataFormatter.formatCellValue(cell);
            default:
                throw new IllegalArgumentException(String.format("Unexpected cell type %s for cell %s",
                        cell.getCellType(), cell));
        }
    }
}
