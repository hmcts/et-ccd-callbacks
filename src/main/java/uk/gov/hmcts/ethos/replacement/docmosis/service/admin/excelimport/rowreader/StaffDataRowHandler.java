package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;

import java.util.List;

@Component
public class StaffDataRowHandler implements RowHandler {

    private final List<RowHandler> rowHandlers;

    public StaffDataRowHandler(@StaffRowHandler List<RowHandler> rowHandlers) {
        this.rowHandlers = rowHandlers;
    }

    @Override
    public boolean accept(Row row) {
        for (var rowHandler : rowHandlers) {
            if (rowHandler.accept(row)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void handle(TribunalOffice tribunalOffice, Row row) {
        for (var rowHandler : rowHandlers) {
            if (rowHandler.accept(row)) {
                rowHandler.handle(tribunalOffice, row);
                return;
            }
        }
    }
}
