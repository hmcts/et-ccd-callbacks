package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;

import java.util.ArrayList;
import java.util.Iterator;

@RequiredArgsConstructor
public class TestSheetHandler implements SheetHandler {

    @Override
    public Iterator<OfficeSheet> sheets(XSSFWorkbook workbook) {
        var list = new ArrayList<OfficeSheet>();
        for (var i = 0; i < workbook.getNumberOfSheets(); i++) {
            var sheet = workbook.getSheetAt(i);
            list.add(new OfficeSheet(TribunalOffice.valueOfOfficeName(sheet.getSheetName()), sheet));
        }

        return list.iterator();
    }
}
