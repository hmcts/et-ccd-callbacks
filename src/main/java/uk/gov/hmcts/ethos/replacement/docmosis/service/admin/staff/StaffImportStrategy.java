package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.CourtWorkerRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.JudgeRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.ImportStrategy;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader.RowHandlerImportStrategy;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader.SheetHandler;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader.StaffDataRowHandler;

@Component
public class StaffImportStrategy implements ImportStrategy {

    private final RowHandlerImportStrategy rowHandlerImportStrategy;
    private final JudgeRepository judgeRepository;
    private final CourtWorkerRepository courtWorkerRepository;

    public StaffImportStrategy(SheetHandler sheetHandler, StaffDataRowHandler rowHandler,
                               JudgeRepository judgeRepository, CourtWorkerRepository courtWorkerRepository) {
        rowHandlerImportStrategy = RowHandlerImportStrategy.create(sheetHandler, rowHandler);
        this.judgeRepository = judgeRepository;
        this.courtWorkerRepository = courtWorkerRepository;
    }

    @Override
    public void importWorkbook(XSSFWorkbook workbook) {
        deleteExistingData();
        rowHandlerImportStrategy.importWorkbook(workbook);
    }

    private void deleteExistingData() {
        judgeRepository.deleteAll();
        courtWorkerRepository.deleteAll();
    }
}
