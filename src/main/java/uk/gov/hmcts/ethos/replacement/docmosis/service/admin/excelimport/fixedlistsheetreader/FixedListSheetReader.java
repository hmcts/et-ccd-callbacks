package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

public class FixedListSheetReader {
    private final FixedListSheetImporter sheetImporter;

    static final String FIXED_LIST_SHEET_NAME = "Scrubbed";

    private FixedListSheetReader(FixedListSheetImporter sheetImporter) {
        this.sheetImporter = sheetImporter;
    }

    public static FixedListSheetReader create(FixedListSheetImporter sheetImporter) {
        return new FixedListSheetReader(sheetImporter);
    }

    public void handle(TribunalOffice tribunalOffice, XSSFWorkbook workbook) throws FixedListSheetReaderException {
        var sheet = getFixedListSheet(workbook);
        sheetImporter.importSheet(tribunalOffice, sheet);
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
