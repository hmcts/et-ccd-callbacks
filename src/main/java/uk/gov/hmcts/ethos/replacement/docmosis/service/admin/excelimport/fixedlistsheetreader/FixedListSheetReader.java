package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;

import java.util.List;

public class FixedListSheetReader {
    private final List<FixedListSheetImporter> sheetImporters;

    static final String FIXED_LIST_SHEET_NAME = "Scrubbed";

    private FixedListSheetReader(List<FixedListSheetImporter> sheetImporters) {
        this.sheetImporters = sheetImporters;
    }

    public static FixedListSheetReader create(List<FixedListSheetImporter> sheetImporters) {
        return new FixedListSheetReader(sheetImporters);
    }

    public void handle(TribunalOffice tribunalOffice, XSSFWorkbook workbook) throws FixedListSheetReaderException {
        var sheet = getFixedListSheet(workbook);
        sheetImporters.forEach(s -> s.importSheet(tribunalOffice, sheet));
    }

    private XSSFSheet getFixedListSheet(XSSFWorkbook workbook) throws FixedListSheetReaderException {
        for (var i = 0; i < workbook.getNumberOfSheets(); i++) {
            var sheet = workbook.getSheetAt(i);
            if (sheet.getSheetName().contains(FIXED_LIST_SHEET_NAME)) {
                return sheet;
            }
        }

        throw new FixedListSheetReaderException("No FixedList sheet found in workbook");
    }
}
