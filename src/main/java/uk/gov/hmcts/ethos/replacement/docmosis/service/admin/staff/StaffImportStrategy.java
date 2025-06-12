package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public void importWorkbook(XSSFWorkbook workbook) {
        judgeRepository.deleteAll();
        courtWorkerRepository.deleteAll();
        rowHandlerImportStrategy.importWorkbook(workbook);
    }
}
