package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class StaffImportService {

    private final UserService userService;
    private final ExcelReadingService excelReadingService;
    private final StaffImportStrategy staffImportStrategy;

    public StaffImportService(UserService userService, ExcelReadingService excelReadingService,
                              StaffImportStrategy staffImportStrategy) {
        this.userService = userService;
        this.excelReadingService = excelReadingService;
        this.staffImportStrategy = staffImportStrategy;
    }

    @Transactional
    public void importStaff(AdminData adminData, String userToken) throws IOException {
        var documentUrl = adminData.getStaffImportFile().getFile().getBinaryUrl();
        var workbook = excelReadingService.readWorkbook(userToken, documentUrl);
        staffImportStrategy.importWorkbook(workbook);
        workbook.close();

        var user = userService.getUserDetails(userToken);
        adminData.getStaffImportFile().setUser(user.getName());
        adminData.getStaffImportFile().setLastImported(LocalDateTime.now().toString());
    }
}
