package uk.gov.hmcts.ethos.replacement.docmosis.service.prehearingdeposit;

import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.service.UserService;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.Document;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ImportFile;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class PreHearingDepositServiceTest {
    private UserDetails userDetails;
    private CCDRequest request;
    private PreHearingDepositService preHearingDepositService;
    private static final String USER_TOKEN = "userToken";
    private static final int DATA_ROW_COUNT = 4;
    private static final String EMAIL = "test@test.com";
    private static final String FIRST_NAME = "Test User First Name";
    private static final String NAME = "Test User Name";
    private static final String UID = "Test UUID";
    private static final String LAST_NAME = "Test Last Name";
    private static final String ROLES = "Test Role1";
    private static final String FILE_NAME = "Test File Name";
    private static final String URL = "testUrl";
    private static final String BINARY_URL = "Test Binary URL";
    private static final String TEST_CASE_REFERENCE_NUMBER = "12345678901";
    @Mock
    UserService userService;
    @Mock
    private CcdClient ccdClient;
    @InjectMocks
    private ExcelReadingService excelReadingService;

    @BeforeEach
    void setUp() {
        ccdClient = mock(CcdClient.class);
        userService = mock(UserService.class);
        excelReadingService = mock(ExcelReadingService.class);
        preHearingDepositService = new PreHearingDepositService(userService, excelReadingService, ccdClient);
        userDetails = new UserDetails();
        userDetails.setEmail(EMAIL);
        userDetails.setFirstName(FIRST_NAME);
        userDetails.setName(NAME);
        userDetails.setUid(UID);
        userDetails.setLastName(LAST_NAME);
        userDetails.setRoles(List.of(ROLES));
        request = new CCDRequest();
        PreHearingDepositData preHearingDepositData = new PreHearingDepositData();
        preHearingDepositData.setCaseReferenceNumber(TEST_CASE_REFERENCE_NUMBER);
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(TEST_CASE_REFERENCE_NUMBER);
        request.setCaseDetails(caseDetails);
    }

    @Test
    @SneakyThrows
    void importPreHearingDepositData() {
        when(userService.getUserDetails(USER_TOKEN)).thenReturn(userDetails);
        when(ccdClient.startGenericTypeCaseCreation(anyString(), any())).thenReturn(request);
        Document document = new Document();
        document.setFilename(FILE_NAME);
        document.setUrl(URL);
        document.setBinaryUrl(BINARY_URL);
        ImportFile importFile = new ImportFile();
        importFile.setFile(document);
        try (XSSFWorkbook workbook = createWorkbook()) {
            when(excelReadingService.readWorkbook(anyString(), anyString())).thenReturn(workbook);
            preHearingDepositService.importPreHearingDepositData(importFile, USER_TOKEN);
        }
        verify(ccdClient, times(DATA_ROW_COUNT)).startGenericTypeCaseCreation(anyString(), any());
        verify(ccdClient, times(DATA_ROW_COUNT)).submitGenericTypeCaseCreation(
                anyString(), any(), any(), anyString(), anyString());
    }

    private XSSFWorkbook createWorkbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet();
        sheet.createRow(0);
        Date date = new Date();
        for (int rowNum = 1; rowNum <= DATA_ROW_COUNT; rowNum++) {
            Row row = sheet.createRow(rowNum);
            row.createCell(0).setCellValue("2345678/2023");
            row.createCell(1).setCellValue("Test Respondent");
            row.createCell(2).setCellValue(date);
            row.createCell(3).setCellValue(date);
            row.createCell(4).setCellValue(100);
            row.createCell(5).setCellValue("YES");
            row.createCell(6).setCellValue(date);
            row.createCell(7).setCellValue("123456");
            row.createCell(8).setCellValue("Receiver");
            row.createCell(9).setCellValue("From");
            row.createCell(10).setCellValue("Comment");
            row.createCell(11).setCellValue(1234);
            row.createCell(12).setCellValue("TE123");
            row.createCell(13).setCellValue(date);
            row.createCell(14).setCellValue(date);
            row.createCell(15).setCellValue("Comments");
            row.createCell(16).setCellValue("Refunded");
            row.createCell(17).setCellValue(date);
            row.createCell(18).setCellValue(50);
            row.createCell(19).setCellValue("Payee");
            row.createCell(20).setCellValue("REF");
            row.createCell(21).setCellValue(date);
            row.createCell(25).setCellValue("Manchester");
        }
        return workbook;
    }
}
