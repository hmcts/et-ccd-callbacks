package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SimpleSheetHandlerTest {

    @Test
    void testCreatesIteratorForExpectedSheets() throws IOException {
        try (XSSFWorkbook workbook = createWorkbook("Bristol", "Leeds", "LondonCentral", "LondonEast",
            "LondonSouth", "Manchester", "MidlandsEast", "MidlandsWest", "Newcastle", "Scotland", "Wales", "Watford")) {

            SimpleSheetHandler simpleSheetHandler = new SimpleSheetHandler();
            Iterator<OfficeSheet> iterator = simpleSheetHandler.sheets(workbook);
            List<TribunalOffice> expectedOfficeSheets = List.of(TribunalOffice.BRISTOL, TribunalOffice.LEEDS,
                TribunalOffice.LONDON_CENTRAL,
                TribunalOffice.LONDON_EAST, TribunalOffice.LONDON_SOUTH, TribunalOffice.MANCHESTER,
                TribunalOffice.MIDLANDS_EAST, TribunalOffice.MIDLANDS_WEST, TribunalOffice.NEWCASTLE,
                TribunalOffice.SCOTLAND, TribunalOffice.WALES, TribunalOffice.WATFORD);
            int index = 0;
            while (iterator.hasNext()) {
                OfficeSheet officeSheet = iterator.next();
                TribunalOffice expectedTribunalOffice = expectedOfficeSheets.get(index++);
                assertEquals(expectedTribunalOffice, officeSheet.getTribunalOffice());
            }
        }
    }

    @Test
    void testCreatesIteratorForUnexpectedSheets() throws IOException {
        try (XSSFWorkbook workbook = createWorkbook("unexpected sheet name")) {

            SimpleSheetHandler simpleSheetHandler = new SimpleSheetHandler();
            Iterator<OfficeSheet> iterator = simpleSheetHandler.sheets(workbook);

            assertFalse(iterator.hasNext());
        }
    }

    private XSSFWorkbook createWorkbook(String... sheetNames) {
        XSSFWorkbook workbook = new XSSFWorkbook();
        for (String sheetName : sheetNames) {
            workbook.createSheet(sheetName);
        }
        return workbook;
    }
}
