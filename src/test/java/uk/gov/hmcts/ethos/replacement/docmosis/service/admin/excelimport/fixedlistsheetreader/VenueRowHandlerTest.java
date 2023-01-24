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
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Venue;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VenueRowHandlerTest {

    private XSSFWorkbook workbook;

    @BeforeEach
    void setup() {
        workbook = new XSSFWorkbook();
        workbook.createSheet("Test");
    }

    @AfterEach
    void tearDown() throws IOException {
        workbook.close();
    }

    @Test
    void testAcceptTrue() {
        Row row = createRow("VenueDundee", "Tribunal", "Dundee Tribunal");

        VenueRowHandler venueRowHandler = new VenueRowHandler("VenueDundee");
        assertTrue(venueRowHandler.accept(row));
    }

    @Test
    void testAcceptFalse() {
        Row row = createRow("VenueDundee", "Tribunal", "Dundee Tribunal");

        VenueRowHandler venueRowHandler = new VenueRowHandler("VenueEdinburgh");
        assertFalse(venueRowHandler.accept(row));
    }

    @Test
    void testHandleSingleRow() {
        Row row = createRow("VenueDundee", "Tribunal", "Dundee Tribunal");
        VenueRowHandler venueRowHandler = new VenueRowHandler("VenueDundee");
        venueRowHandler.handle(TribunalOffice.DUNDEE, row);

        List<Venue> venues = venueRowHandler.getVenues();
        assertEquals(1, venues.size());
        verifyVenue(venues.get(0), "Tribunal", "Dundee Tribunal");
    }

    @Test
    void testHandleMultipleRows() {
        List<Row> rows = List.of(
            createRow("VenueDundee", "Tribunal", "Dundee Tribunal"),
            createRow("VenueEdinburgh", "OET Scotland", "OET Scotland"),
            createRow("VenueDundee", "1", "Room 1")
        );

        VenueRowHandler venueRowHandler = new VenueRowHandler("VenueDundee");
        for (Row row : rows) {
            if (venueRowHandler.accept(row)) {
                venueRowHandler.handle(TribunalOffice.DUNDEE, row);
            }
        }
        List<Venue> venues = venueRowHandler.getVenues();

        assertEquals(2, venues.size());
        verifyVenue(venues.get(0), "Tribunal", "Dundee Tribunal");
        verifyVenue(venues.get(1), "1", "Room 1");
    }

    private Row createRow(String listId, String code, String name) {
        XSSFSheet sheet = workbook.getSheet("Test");
        XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(0, CellType.STRING);
        row.getCell(0).setCellValue(listId);
        row.createCell(1, CellType.STRING);
        row.getCell(1).setCellValue(code);
        row.createCell(2, CellType.STRING);
        row.getCell(2).setCellValue(name);

        return row;
    }

    private void verifyVenue(Venue venue, String expectedCode, String expectedName) {
        assertEquals(expectedCode, venue.getCode());
        assertEquals(expectedName, venue.getName());
        assertEquals(TribunalOffice.DUNDEE, venue.getTribunalOffice());
    }
}
