package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public interface ImportStrategy {
    void importWorkbook(XSSFWorkbook workbook);
}
