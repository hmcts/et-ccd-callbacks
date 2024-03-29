package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class StaffImportService {

    private final UserIdamService userIdamService;
    private final ExcelReadingService excelReadingService;
    private final StaffImportStrategy staffImportStrategy;

    public StaffImportService(UserIdamService userIdamService, ExcelReadingService excelReadingService,
                              StaffImportStrategy staffImportStrategy) {
        this.userIdamService = userIdamService;
        this.excelReadingService = excelReadingService;
        this.staffImportStrategy = staffImportStrategy;
    }

    @Transactional
    public void importStaff(AdminData adminData, String userToken) throws IOException {
        String documentUrl = adminData.getStaffImportFile().getFile().getBinaryUrl();
        try (XSSFWorkbook workbook = excelReadingService.readWorkbook(userToken, documentUrl)) {
            staffImportStrategy.importWorkbook(workbook);
        }

        UserDetails user = userIdamService.getUserDetails(userToken);
        adminData.getStaffImportFile().setUser(user.getName());
        adminData.getStaffImportFile().setLastImported(LocalDateTime.now().toString());
    }
}
