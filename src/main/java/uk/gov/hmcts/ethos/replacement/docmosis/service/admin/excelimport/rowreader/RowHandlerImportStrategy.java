package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.ImportStrategy;

import java.util.Iterator;

public class RowHandlerImportStrategy implements ImportStrategy {

    private SheetHandler sheetHandler;
    private RowHandler rowHandler;

    public static RowHandlerImportStrategy create(SheetHandler sheetHandler, RowHandler rowHandler) {
        RowHandlerImportStrategy strategy = new RowHandlerImportStrategy();
        strategy.sheetHandler = sheetHandler;
        strategy.rowHandler = rowHandler;
        return strategy;
    }

    @Override
    public void importWorkbook(XSSFWorkbook workbook) {
        Iterator<OfficeSheet> iterator = sheetHandler.sheets(workbook);

        while (iterator.hasNext()) {
            OfficeSheet officeSheet = iterator.next();
            for (Row row : officeSheet.getSheet()) {
                if (rowHandler.accept(row)) {
                    rowHandler.handle(officeSheet.getTribunalOffice(), row);
                }
            }
        }
    }
}
