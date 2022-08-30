package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import org.apache.poi.ss.usermodel.Row;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;

public interface RowHandler {
    boolean accept(Row row);

    void handle(TribunalOffice tribunalOffice, Row row);
}
