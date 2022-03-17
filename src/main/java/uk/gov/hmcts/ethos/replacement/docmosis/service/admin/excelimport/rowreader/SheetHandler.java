package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Iterator;

public interface SheetHandler {
    Iterator<OfficeSheet> sheets(XSSFWorkbook workbook);
}
