package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.ImportStrategy;

public class RowHandlerImportStrategy implements ImportStrategy {

    private RowHandler rowHandler;

    public static RowHandlerImportStrategy create(RowHandler rowHandler) {
        RowHandlerImportStrategy strategy = new RowHandlerImportStrategy();
        strategy.rowHandler = rowHandler;
        return strategy;
    }

    @Override
    public void importWorkbook(XSSFWorkbook workbook) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            var sheet = workbook.getSheetAt(i);
            var tribunalOffice = TribunalOffice.valueOfOfficeName(sheet.getSheetName());
            for (Row row : sheet) {
                if (rowHandler.accept(row)) {
                    rowHandler.handle(tribunalOffice, row);
                }
            }
        }
    }
}
