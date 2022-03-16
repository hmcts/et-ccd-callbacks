package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.JudgeRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.ImportStrategy;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader.RowHandlerImportStrategy;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader.StaffDataRowHandler;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class StaffImportService {

    private final UserService userService;
    private final ExcelReadingService excelReadingService;
    private final JudgeRepository judgeRepository;
    private final ImportStrategy importStrategy;

    public StaffImportService(UserService userService, ExcelReadingService excelReadingService,
                              JudgeRepository judgeRepository,
                              StaffDataRowHandler rowHandler) {
        this.userService = userService;
        this.excelReadingService = excelReadingService;
        this.judgeRepository = judgeRepository;
        importStrategy = RowHandlerImportStrategy.create(rowHandler);
    }

    @Transactional
    public void importStaff(AdminData adminData, String userToken) throws IOException {
        judgeRepository.deleteAll();

        var documentUrl = adminData.getStaffImportFile().getFile().getBinaryUrl();
        var workbook = excelReadingService.readWorkbook(userToken, documentUrl);
        importStrategy.importWorkbook(workbook);

        var user = userService.getUserDetails(userToken);
        adminData.getStaffImportFile().setUser(user.getName());
        adminData.getStaffImportFile().setLastImported(LocalDateTime.now().toString());
    }
}
