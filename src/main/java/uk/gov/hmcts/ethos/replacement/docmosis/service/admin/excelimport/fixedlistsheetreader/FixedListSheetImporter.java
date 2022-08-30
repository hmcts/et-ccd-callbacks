package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;

public interface FixedListSheetImporter {

    void importSheet(TribunalOffice tribunalOffice, XSSFSheet sheet);
}
