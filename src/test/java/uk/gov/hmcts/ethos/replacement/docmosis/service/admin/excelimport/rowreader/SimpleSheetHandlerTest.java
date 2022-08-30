package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SimpleSheetHandlerTest {

    @Test
    void testCreatesIteratorForExpectedSheets() {
        var workbook = createWorkbook("Bristol", "Leeds", "LondonCentral", "LondonEast", "LondonSouth",
                "Manchester", "MidlandsEast", "MidlandsWest", "Newcastle", "Scotland", "Wales", "Watford");

        var simpleSheetHandler = new SimpleSheetHandler();
        var iterator = simpleSheetHandler.sheets(workbook);
        var expectedOfficeSheets = List.of(TribunalOffice.BRISTOL, TribunalOffice.LEEDS, TribunalOffice.LONDON_CENTRAL,
                TribunalOffice.LONDON_EAST, TribunalOffice.LONDON_SOUTH, TribunalOffice.MANCHESTER,
                TribunalOffice.MIDLANDS_EAST, TribunalOffice.MIDLANDS_WEST, TribunalOffice.NEWCASTLE,
                TribunalOffice.SCOTLAND, TribunalOffice.WALES, TribunalOffice.WATFORD);
        int index = 0;
        while (iterator.hasNext()) {
            var officeSheet = iterator.next();
            var expectedTribunalOffice = expectedOfficeSheets.get(index++);
            assertEquals(expectedTribunalOffice, officeSheet.getTribunalOffice());
        }
    }

    @Test
    void testCreatesIteratorForUnexpectedSheets() {
        var workbook = createWorkbook("unexpected sheet name");

        var simpleSheetHandler = new SimpleSheetHandler();
        var iterator = simpleSheetHandler.sheets(workbook);

        assertFalse(iterator.hasNext());
    }

    private XSSFWorkbook createWorkbook(String... sheetNames) {
        var workbook = new XSSFWorkbook();
        for (var sheetName : sheetNames) {
            workbook.createSheet(sheetName);
        }
        return workbook;
    }
}
