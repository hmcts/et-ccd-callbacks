package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.Document;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ImportFile;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Judge;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.CourtWorkerRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.JudgeRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader.ClerkRowHandler;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader.EmployeeMemberRowHandler;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader.EmployerMemberRowHandler;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader.JudgeRowHandler;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader.SimpleSheetHandler;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.rowreader.StaffDataRowHandler;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"PMD.UseProperClassLoader", "PMD.CloseResource"})
class StaffImportServiceTest {

    @Test
    void testImportStaff() throws IOException, InvalidFormatException {
        var documentBinaryUrl = "http://dm-store/document/23232323";
        var userToken = "test-token";
        var userName = "Donald Duck";
        var user = new UserDetails();
        user.setName(userName);
        var userService = mock(UserService.class);
        when(userService.getUserDetails(userToken)).thenReturn(user);
        var excelReadingService = mockExcelReadingService(userToken, documentBinaryUrl);
        var judgeRepository = mock(JudgeRepository.class);
        var courtWorkerRepository = mock(CourtWorkerRepository.class);

        var sheetHandler = new SimpleSheetHandler();
        var rowHandler = new StaffDataRowHandler(List.of(
                new JudgeRowHandler(judgeRepository),
                new ClerkRowHandler(courtWorkerRepository),
                new EmployerMemberRowHandler(courtWorkerRepository),
                new EmployeeMemberRowHandler(courtWorkerRepository)));
        var staffImportStrategy = new StaffImportStrategy(sheetHandler, rowHandler, judgeRepository,
                courtWorkerRepository);

        var adminData = createAdminData(documentBinaryUrl);

        var staffImportService = new StaffImportService(userService, excelReadingService, staffImportStrategy);
        staffImportService.importStaff(adminData, userToken);

        verify(judgeRepository, times(1)).deleteAll();
        verify(courtWorkerRepository, times(1)).deleteAll();
        verify(excelReadingService, times(1)).readWorkbook(userToken, documentBinaryUrl);
        verify(judgeRepository, times(36)).save(any(Judge.class));
        verify(courtWorkerRepository, times(108)).save(any(CourtWorker.class));
        assertEquals(userName, adminData.getStaffImportFile().getUser());
        assertNotNull(adminData.getStaffImportFile().getLastImported());
    }

    private ExcelReadingService mockExcelReadingService(String userToken, String documentBinaryUrl)
            throws IOException, InvalidFormatException {
        var excelReadingService = mock(ExcelReadingService.class);
        var file = new File(StaffImportServiceTest.class.getClassLoader()
                .getResource("admin/StaffImportFile.xlsx").getFile());
        var workbook = new XSSFWorkbook(file);
        when(excelReadingService.readWorkbook(userToken, documentBinaryUrl)).thenReturn(workbook);
        return excelReadingService;
    }

    private AdminData createAdminData(String documentBinaryUrl) {
        var adminData = new AdminData();
        var importFile = new ImportFile();
        var document = new Document();
        document.setBinaryUrl(documentBinaryUrl);
        importFile.setFile(document);
        adminData.setStaffImportFile(importFile);

        return adminData;
    }
}
