package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;

@RequiredArgsConstructor
@Getter
public class OfficeSheet {
    private final TribunalOffice tribunalOffice;
    private final XSSFSheet sheet;
}
