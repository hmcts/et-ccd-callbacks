package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Room;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Venue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoomRowHandlerTest {

    private XSSFWorkbook workbook;
    private FixedListMappings fixedListMappings;
    private List<Venue> venues;

    @BeforeEach
    void setup() {
        workbook = new XSSFWorkbook();
        workbook.createSheet("Test");

        venues = new ArrayList<>();
        venues.add(createVenue("GTC"));
        venues.add(createVenue("Glasgow COET"));

        fixedListMappings = new FixedListMappings();
        fixedListMappings.getRooms().put(TribunalOffice.GLASGOW, new HashMap<>());
        fixedListMappings.getRooms().get(TribunalOffice.GLASGOW).put("CambeltownSC", "Cambeltown HC");
        fixedListMappings.getRooms().get(TribunalOffice.GLASGOW).put("DumfriesSC", "Dumfries HC");
    }

    @AfterEach
    void tearDown() throws IOException {
        workbook.close();
    }

    @Test
    void testAcceptMatchingVenue() {
        RoomRowHandler roomRowHandler = new RoomRowHandler(TribunalOffice.GLASGOW, fixedListMappings, venues);
        Row row = createRow("GTC", "201", "201");

        assertTrue(roomRowHandler.accept(row));
    }

    @Test
    void testAcceptMatchingVenueWithWhitespace() {
        RoomRowHandler roomRowHandler = new RoomRowHandler(TribunalOffice.GLASGOW, fixedListMappings, venues);
        Row row = createRow("GlasgowCOET", "201", "201");

        assertTrue(roomRowHandler.accept(row));
    }

    @Test
    void testAcceptMatchingVenueMapping() {
        RoomRowHandler roomRowHandler = new RoomRowHandler(TribunalOffice.GLASGOW, fixedListMappings, venues);
        Row row = createRow("CambeltownSC", "1", "1");

        assertTrue(roomRowHandler.accept(row));
    }

    @Test
    void testAcceptFalse() {
        RoomRowHandler roomRowHandler = new RoomRowHandler(TribunalOffice.GLASGOW, fixedListMappings, venues);
        Row row = createRow("NoVenueFound", "201", "201");

        assertFalse(roomRowHandler.accept(row));
    }

    @Test
    void testHandle() {
        RoomRowHandler roomRowHandler = new RoomRowHandler(TribunalOffice.GLASGOW, fixedListMappings, venues);
        Row row = createRow("GTC", "201", "201");

        roomRowHandler.handle(row);
        List<Room> rooms = roomRowHandler.getRooms();
        assertEquals(1, rooms.size());
        verifyRoom(rooms.get(0), "GTC", "201", "201");
    }

    @Test
    void testHandleNumericCodeAndName() {
        RoomRowHandler roomRowHandler = new RoomRowHandler(TribunalOffice.GLASGOW, fixedListMappings, venues);
        Row row = createRow("CambeltownSC", 1, 1);

        roomRowHandler.handle(row);
        List<Room> rooms = roomRowHandler.getRooms();
        assertEquals(1, rooms.size());
        verifyRoom(rooms.get(0), "Cambeltown HC", "1", "1");
    }

    @Test
    void testHandleMultipleRows() {
        RoomRowHandler roomRowHandler = new RoomRowHandler(TribunalOffice.GLASGOW, fixedListMappings, venues);
        List<Row> rows = List.of(
                createRow("VenueGlasgow", "GTC", "GTC"),
                createRow("GTC", "201", "201"),
                createRow("GTC", "202", "202"),
                createRow("CambeltownSC", 1, 1),
                createRow("CambeltownSC", 2, 2),
                createRow("DumfriesSC", 1, 1),
                createRow("GlasgowCOET", "12 - AIT", "12 - AIT"),
                createRow("flLocations", "code", "label")
        );

        for (Row row : rows) {
            if (roomRowHandler.accept(row)) {
                roomRowHandler.handle(row);
            }
        }

        List<Room> rooms = roomRowHandler.getRooms();
        assertEquals(6, rooms.size());
        verifyRoom(rooms.get(0), "GTC", "201", "201");
        verifyRoom(rooms.get(1), "GTC", "202", "202");
        verifyRoom(rooms.get(2), "Cambeltown HC", "1", "1");
        verifyRoom(rooms.get(3), "Cambeltown HC", "2", "2");
        verifyRoom(rooms.get(4), "Dumfries HC", "1", "1");
        verifyRoom(rooms.get(5), "Glasgow COET", "12 - AIT", "12 - AIT");
    }

    @Test
    void testHandleNoVenueFoundThrowsException() {
        RoomRowHandler roomRowHandler = new RoomRowHandler(TribunalOffice.GLASGOW, fixedListMappings, venues);
        Row row = createRow("NoVenueFound", "201", "201");
        assertThrows(IllegalArgumentException.class, () -> roomRowHandler.handle(row));
    }

    @Test
    void testHandleUnexpectedCellTypeThrowsException() {
        RoomRowHandler roomRowHandler = new RoomRowHandler(TribunalOffice.GLASGOW, fixedListMappings, venues);
        Row row = createInvalidRow();
        assertThrows(IllegalArgumentException.class, () -> roomRowHandler.handle(row));
    }

    private Venue createVenue(String code) {
        Venue venue = new Venue();
        venue.setCode(code);
        return venue;
    }

    private Row createRow(String listId, String code, String name) {
        Row row = createRow(listId);
        row.createCell(1, CellType.STRING);
        row.getCell(1).setCellValue(code);
        row.createCell(2, CellType.STRING);
        row.getCell(2).setCellValue(name);

        return row;
    }

    private Row createRow(String listId, int code, int name) {
        Row row = createRow(listId);
        row.createCell(1, CellType.NUMERIC);
        row.getCell(1).setCellValue(code);
        row.createCell(2, CellType.NUMERIC);
        row.getCell(2).setCellValue(name);

        return row;
    }

    private Row createRow(String listId) {
        XSSFSheet sheet = workbook.getSheet("Test");
        XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(0, CellType.STRING);
        row.getCell(0).setCellValue(listId);

        return row;
    }

    private Row createInvalidRow() {
        XSSFSheet sheet = workbook.getSheet("Test");
        XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(0, CellType.BOOLEAN);
        row.getCell(0).setCellValue(true);

        return row;
    }

    void verifyRoom(Room room, String expectedVenueCode, String expectedCode, String expectedName) {
        assertEquals(expectedVenueCode, room.getVenueCode());
        assertEquals(expectedCode, room.getCode());
        assertEquals(expectedName, room.getName());
    }
}
