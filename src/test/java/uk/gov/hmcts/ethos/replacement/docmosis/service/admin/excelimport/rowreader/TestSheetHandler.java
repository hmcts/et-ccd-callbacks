package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RequiredArgsConstructor
public class TestSheetHandler implements SheetHandler {

    @Override
    public Iterator<OfficeSheet> sheets(XSSFWorkbook workbook) {
        List<OfficeSheet> list = new ArrayList<>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sheet = workbook.getSheetAt(i);
            list.add(new OfficeSheet(TribunalOffice.valueOfOfficeName(sheet.getSheetName()), sheet));
        }

        return list.iterator();
    }
}
