package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RowHandlerImportStrategyTest {

    @Test
    void testCreate() {
        SheetHandler sheetHandler = mock(SheetHandler.class);
        RowHandler rowHandler = mock(RowHandler.class);
        assertNotNull(RowHandlerImportStrategy.create(sheetHandler, rowHandler));
    }

    @Test
    void testImportWorkbookRowHandlerAccepts() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        TribunalOffice[] tribunalOffices = TribunalOffice.values();
        for (TribunalOffice tribunalOffice : tribunalOffices) {
            createSheet(workbook, tribunalOffice.getOfficeName());
        }

        TestSheetHandler sheetHandler = new TestSheetHandler();
        RowHandler rowHandler = mock(RowHandler.class);
        when(rowHandler.accept(isA(Row.class))).thenReturn(true);
        RowHandlerImportStrategy strategy = RowHandlerImportStrategy.create(sheetHandler, rowHandler);

        strategy.importWorkbook(workbook);

        verify(rowHandler, times(tribunalOffices.length)).accept(isA(Row.class));
        for (TribunalOffice tribunalOffice : tribunalOffices) {
            verify(rowHandler, times(1)).handle(eq(tribunalOffice), isA(Row.class));
        }
    }

    @Test
    void testImportWorkbookRowHandlerNotAccepts() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        TribunalOffice[] tribunalOffices = TribunalOffice.values();
        for (TribunalOffice tribunalOffice : tribunalOffices) {
            createSheet(workbook, tribunalOffice.getOfficeName());
        }

        TestSheetHandler sheetHandler = new TestSheetHandler();
        RowHandler rowHandler = mock(RowHandler.class);
        when(rowHandler.accept(isA(Row.class))).thenReturn(false);
        RowHandlerImportStrategy strategy = RowHandlerImportStrategy.create(sheetHandler, rowHandler);

        strategy.importWorkbook(workbook);

        verify(rowHandler, times(tribunalOffices.length)).accept(isA(Row.class));
        for (TribunalOffice tribunalOffice : tribunalOffices) {
            verify(rowHandler, never()).handle(eq(tribunalOffice), isA(Row.class));
        }
    }

    private void createSheet(XSSFWorkbook workbook, String sheetName) {
        XSSFSheet sheet = workbook.createSheet(sheetName);
        sheet.createRow(0);
    }
}
