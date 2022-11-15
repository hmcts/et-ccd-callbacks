package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class FixedListSheetReaderTest {

    @Test
    void testHandleImportsFixedListSheet() throws FixedListSheetReaderException, IOException {
        String fixedListSheetName = "Scotland Scrubbed";
        FixedListSheetImporter sheetImporter = mock(FixedListSheetImporter.class);
        FixedListSheetReader sheetReader = FixedListSheetReader.create(List.of(sheetImporter));
        try (XSSFWorkbook workbook = createWorkbook("Sheet1", fixedListSheetName, "Sheet3")) {
            sheetReader.handle(TribunalOffice.SCOTLAND, workbook);
        }

        ArgumentCaptor<XSSFSheet> captor = ArgumentCaptor.forClass(XSSFSheet.class);
        verify(sheetImporter, times(1)).importSheet(eq(TribunalOffice.SCOTLAND), captor.capture());
        XSSFSheet sheet = captor.getValue();
        assertEquals(fixedListSheetName, sheet.getSheetName());
    }

    @Test
    void testHandleThrowsExceptionWhenFixedListSheetNotFound() throws IOException {
        FixedListSheetImporter sheetImporter = mock(FixedListSheetImporter.class);
        FixedListSheetReader sheetReader = FixedListSheetReader.create(List.of(sheetImporter));
        try (XSSFWorkbook workbook = createWorkbook("Sheet1", "Sheet2", "Sheet3")) {
            assertThrows(FixedListSheetReaderException.class,
                () -> sheetReader.handle(TribunalOffice.SCOTLAND, workbook));
        }
    }

    @Test
    void testHandleThrowsExceptionWhenNoSheetsExistInWorkbook() throws IOException {
        FixedListSheetImporter sheetImporter = mock(FixedListSheetImporter.class);
        FixedListSheetReader sheetReader = FixedListSheetReader.create(List.of(sheetImporter));
        try (XSSFWorkbook workbook = createWorkbook()) {
            assertThrows(FixedListSheetReaderException.class,
                () -> sheetReader.handle(TribunalOffice.SCOTLAND, workbook));
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
