package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FixedListSheetReaderTest {

    @Test
    void testHandleImportsFixedListSheet() throws FixedListSheetReaderException {
        var fixedListSheetName = "Scotland Scrubbed";
        var sheetImporter = mock(FixedListSheetImporter.class);
        var sheetReader = FixedListSheetReader.create(List.of(sheetImporter));
        var workbook = createWorkbook("Sheet1", fixedListSheetName, "Sheet3");

        sheetReader.handle(TribunalOffice.SCOTLAND, workbook);

        var captor = ArgumentCaptor.forClass(XSSFSheet.class);
        verify(sheetImporter, times(1)).importSheet(eq(TribunalOffice.SCOTLAND), captor.capture());
        var sheet = captor.getValue();
        assertEquals(fixedListSheetName, sheet.getSheetName());
    }

    @Test
    void testHandleThrowsExceptionWhenFixedListSheetNotFound() {
        var sheetImporter = mock(FixedListSheetImporter.class);
        var sheetReader = FixedListSheetReader.create(List.of(sheetImporter));
        var workbook = createWorkbook("Sheet1", "Sheet2", "Sheet3");

        assertThrows(FixedListSheetReaderException.class, () -> sheetReader.handle(TribunalOffice.SCOTLAND, workbook));
    }

    @Test
    void testHandleThrowsExceptionWhenNoSheetsExistInWorkbook() {
        var sheetImporter = mock(FixedListSheetImporter.class);
        var sheetReader = FixedListSheetReader.create(List.of(sheetImporter));
        var workbook = createWorkbook();

        assertThrows(FixedListSheetReaderException.class, () -> sheetReader.handle(TribunalOffice.SCOTLAND, workbook));
    }

    private XSSFWorkbook createWorkbook(String... sheetNames) {
        var workbook = new XSSFWorkbook();
        for (var sheetName : sheetNames) {
            workbook.createSheet(sheetName);
        }
        return workbook;
    }
}
