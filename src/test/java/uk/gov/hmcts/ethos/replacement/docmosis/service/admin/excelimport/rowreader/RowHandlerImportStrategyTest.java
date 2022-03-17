package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import org.apache.poi.ss.usermodel.Row;
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
        var sheetHandler = mock(SheetHandler.class);
        var rowHandler = mock(RowHandler.class);
        assertNotNull(RowHandlerImportStrategy.create(sheetHandler, rowHandler));
    }

    @Test
    void testImportWorkbookRowHandlerAccepts() {
        var workbook = new XSSFWorkbook();
        var tribunalOffices = TribunalOffice.values();
        for (var tribunalOffice : tribunalOffices) {
            createSheet(workbook, tribunalOffice.getOfficeName());
        }

        var sheetHandler = new TestSheetHandler();
        var rowHandler = mock(RowHandler.class);
        when(rowHandler.accept(isA(Row.class))).thenReturn(true);
        var strategy = RowHandlerImportStrategy.create(sheetHandler, rowHandler);

        strategy.importWorkbook(workbook);

        verify(rowHandler, times(tribunalOffices.length)).accept(isA(Row.class));
        for (var tribunalOffice : tribunalOffices) {
            verify(rowHandler, times(1)).handle(eq(tribunalOffice), isA(Row.class));
        }
    }

    @Test
    void testImportWorkbookRowHandlerNotAccepts() {
        var workbook = new XSSFWorkbook();
        var tribunalOffices = TribunalOffice.values();
        for (var tribunalOffice : tribunalOffices) {
            createSheet(workbook, tribunalOffice.getOfficeName());
        }

        var sheetHandler = new TestSheetHandler();
        var rowHandler = mock(RowHandler.class);
        when(rowHandler.accept(isA(Row.class))).thenReturn(false);
        var strategy = RowHandlerImportStrategy.create(sheetHandler, rowHandler);

        strategy.importWorkbook(workbook);

        verify(rowHandler, times(tribunalOffices.length)).accept(isA(Row.class));
        for (var tribunalOffice : tribunalOffices) {
            verify(rowHandler, never()).handle(eq(tribunalOffice), isA(Row.class));
        }
    }

    private void createSheet(XSSFWorkbook workbook, String sheetName) {
        var sheet = workbook.createSheet(sheetName);
        sheet.createRow(0);
    }
}
