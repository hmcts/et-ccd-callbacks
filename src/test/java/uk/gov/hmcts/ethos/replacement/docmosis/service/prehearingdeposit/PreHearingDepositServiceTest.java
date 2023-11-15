package uk.gov.hmcts.ethos.replacement.docmosis.service.prehearingdeposit;

import lombok.SneakyThrows;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.service.UserService;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.Document;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ImportFile;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;

import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class PreHearingDepositServiceTest {
    private UserDetails userDetails;
    private CCDRequest request;
    private PreHearingDepositService preHearingDepositService;
    private static final String USER_TOKEN = "userToken";
    private static final String EXCEL_WORKBOOK = "src/test/resources/PreHearingDepositData.xlsx";
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
        when(excelReadingService.readWorkbook(anyString(), anyString())).thenReturn(new XSSFWorkbook(EXCEL_WORKBOOK));
        when(ccdClient.startGenericTypeCaseCreation(anyString(), any())).thenReturn(request);
        Document document = new Document();
        document.setFilename(FILE_NAME);
        document.setUrl(URL);
        document.setBinaryUrl(BINARY_URL);
        ImportFile importFile = new ImportFile();
        importFile.setFile(document);
        preHearingDepositService.importPreHearingDepositData(importFile, USER_TOKEN);
        verify(ccdClient, times(4)).startGenericTypeCaseCreation(anyString(), any());
    }
}
