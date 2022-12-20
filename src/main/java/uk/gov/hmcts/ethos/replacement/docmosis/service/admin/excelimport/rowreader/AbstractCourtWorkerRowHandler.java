package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.CourtWorkerRepository;

@RequiredArgsConstructor
public abstract class AbstractCourtWorkerRowHandler implements RowHandler {
    private final CourtWorkerRepository courtWorkerRepository;
    final String rowId;
    final CourtWorkerType courtWorkerType;

    @Override
    public boolean accept(Row row) {
        Cell cell = row.getCell(0);
        return cell != null && rowId.equals(cell.getStringCellValue());
    }

    @Override
    public void handle(TribunalOffice tribunalOffice, Row row) {
        CourtWorker courtWorker = rowToCourtWorker(tribunalOffice, row);
        courtWorkerRepository.save(courtWorker);
    }

    private CourtWorker rowToCourtWorker(TribunalOffice tribunalOffice, Row row) {
        String code = row.getCell(1).getStringCellValue();
        String name = row.getCell(2).getStringCellValue();

        CourtWorker courtWorker = new CourtWorker();
        courtWorker.setType(courtWorkerType);
        courtWorker.setCode(code);
        courtWorker.setName(name);
        courtWorker.setTribunalOffice(tribunalOffice);
        return courtWorker;
    }
}
