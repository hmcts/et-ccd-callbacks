package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.multiples.AdditionalClaimant;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleObject;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FilterExcelType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.CONSTRAINT_KEY;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.HEADER_3;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.HEADER_5;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.SHEET_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil.TESTING_FILE_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil.TESTING_FILE_NAME_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesScheduleHelper.NOT_ALLOCATED;

@ExtendWith(SpringExtension.class)
class ExcelReadingServiceTest {

    @Mock
    private ExcelDocManagementService excelDocManagementService;
    @Mock
    private CcdClient ccdClient;
    @InjectMocks
    private ExcelReadingService excelReadingService;

    private String documentBinaryUrl;
    private Resource body;
    private List<String> errors;
    private MultipleData multipleData;
    private String userToken;

    @BeforeEach
    public void setUp() {
        documentBinaryUrl = "http://127.0.0.1:3453/documents/20d8a494-4232-480a-aac3-23ad0746c07b/binary";
        errors = new ArrayList<>();
        multipleData = MultipleUtil.getMultipleData();
        userToken = "authString";
    }

    @Test
    void readExcelAll() throws IOException {

        body = new ClassPathResource(TESTING_FILE_NAME);
        when(excelDocManagementService.downloadExcelDocument(userToken, documentBinaryUrl))
                .thenReturn(body.getInputStream());
        SortedMap<String, Object> multipleObjects = excelReadingService.readExcel(userToken, documentBinaryUrl,
                errors, multipleData, FilterExcelType.ALL);
        assertEquals(6, multipleObjects.size());
        assertEquals("2", ((MultipleObject) multipleObjects.get("1820001/2019")).getFlag2());
        assertEquals("AA", ((MultipleObject) multipleObjects.get("1820002/2019")).getFlag1());
        assertEquals("", ((MultipleObject) multipleObjects.get("1820005/2019")).getFlag2());
        assertEquals("", ((MultipleObject) multipleObjects.get("1820005/2019")).getFlag3());
        assertEquals("", ((MultipleObject) multipleObjects.get("1820005/2019")).getFlag4());
        assertEquals(0, errors.size());
    }

    @Test
    void readExcelFlags() throws IOException {

        body = new ClassPathResource(TESTING_FILE_NAME);
        when(excelDocManagementService.downloadExcelDocument(userToken, documentBinaryUrl))
                .thenReturn(body.getInputStream());
        SortedMap<String, Object> multipleObjects = excelReadingService.readExcel(userToken, documentBinaryUrl,
                errors, multipleData, FilterExcelType.FLAGS);
        assertEquals(3, multipleObjects.size());
        assertNull(multipleObjects.get("1820001/2019"));
        assertEquals("1820002/2019", multipleObjects.get("1820002/2019"));
        assertEquals(0, errors.size());
    }

    @Test
    void readExcelSubMultiple() throws IOException {

        body = new ClassPathResource(TESTING_FILE_NAME);
        when(excelDocManagementService.downloadExcelDocument(userToken, documentBinaryUrl))
                .thenReturn(body.getInputStream());
        SortedMap<String, Object> multipleObjects = excelReadingService.readExcel(userToken, documentBinaryUrl,
                errors, multipleData, FilterExcelType.SUB_MULTIPLE);

        List<String> listSub = (List<String>) multipleObjects.get("Sub");
        assertEquals(2, listSub.size());

        List<String> listSub1 = (List<String>) multipleObjects.get("Sub1");
        assertEquals(1, listSub1.size());

        assertEquals("1820004/2019", listSub.get(0));
        assertEquals("1820005/2019", listSub.get(1));
        assertEquals(0, errors.size());
    }

    @Test
    void setSubMultipleFieldInSingleCaseDataTest() throws IOException {
        SubmitEvent submitEvent = new SubmitEvent();
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("1234");
        submitEvent.setCaseData(caseData);
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setJurisdiction("EMPLOYMENT");
        multipleDetails.setCaseTypeId("Leeds_Multiple");
        when(ccdClient.retrieveCasesElasticSearch(anyString(),
                anyString(), anyList()))
                .thenReturn(List.of(submitEvent));
        excelReadingService.setSubMultipleFieldInSingleCaseData(userToken,
                multipleDetails,
                "1234",
                "subMultiple");
        assertEquals("subMultiple", caseData.getSubMultipleName());
    }

    @Test
    void readExcelDynamicListFlags() throws IOException {

        body = new ClassPathResource(TESTING_FILE_NAME);
        when(excelDocManagementService.downloadExcelDocument(userToken, documentBinaryUrl))
                .thenReturn(body.getInputStream());
        SortedMap<String, Object> multipleObjects = excelReadingService.readExcel(userToken, documentBinaryUrl,
                errors, multipleData, FilterExcelType.DL_FLAGS);

        Set<String> flags1 = (HashSet<String>) multipleObjects.get(HEADER_3);
        assertEquals(2, flags1.size());

        Set<String> flags3 = (HashSet<String>) multipleObjects.get(HEADER_5);
        assertEquals(1, flags3.size());

        Assertions.assertTrue(flags1.contains("AA"));
        Assertions.assertTrue(flags3.contains(""));
    }

    @Test
    void readExcelError() throws IOException {

        body = new ClassPathResource(TESTING_FILE_NAME_ERROR);
        when(excelDocManagementService.downloadExcelDocument(userToken, documentBinaryUrl))
                .thenReturn(body.getInputStream());
        excelReadingService.readExcel(userToken, documentBinaryUrl, errors, multipleData, FilterExcelType.ALL);
        assertEquals(1, errors.size());
    }

    @Test
    void readExcelException() {
        assertThrows(Exception.class, () -> {
            body = new ClassPathResource(TESTING_FILE_NAME_ERROR);
            when(excelDocManagementService.downloadExcelDocument(userToken, documentBinaryUrl))
                    .thenThrow(new IOException());
            SortedMap<String, Object> multipleObjects = excelReadingService.readExcel(userToken, documentBinaryUrl,
                    errors, multipleData, FilterExcelType.ALL);
            assertEquals("{}", multipleObjects.toString());

        });
    }

    @Test
    void getCellValueShouldSupportNumericAndUnsupportedTypes() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("sheet");
            Row row = sheet.createRow(0);
            Cell numericCell = row.createCell(0);
            numericCell.setCellValue(1234);
            Cell unsupportedCell = row.createCell(1);
            unsupportedCell.setCellValue(true);

            assertEquals("1234", excelReadingService.getCellValue(numericCell));
            assertEquals("", excelReadingService.getCellValue(unsupportedCell));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void readExcelSubMultipleShouldUseNotAllocatedForBlankSubMultiple() throws IOException {
        when(excelDocManagementService.downloadExcelDocument(userToken, documentBinaryUrl))
            .thenReturn(buildWorkbookStreamWithSingleDataRow(
                SHEET_NAME,
                true,
                List.of("111111/2024", "", "", "", "", "")
            ));

        SortedMap<String, Object> multipleObjects = excelReadingService.readExcel(
            userToken,
            documentBinaryUrl,
            errors,
            new MultipleData(),
            FilterExcelType.SUB_MULTIPLE
        );

        List<String> notAllocatedCases = (List<String>) multipleObjects.get(NOT_ALLOCATED);
        assertEquals(List.of("111111/2024"), notAllocatedCases);
        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    void readExcelShouldAddSheetNameErrorWhenSheetMissing() throws IOException {
        when(excelDocManagementService.downloadExcelDocument(userToken, documentBinaryUrl))
            .thenReturn(buildWorkbookStreamWithSingleDataRow(
                "differentSheet",
                false,
                List.of("111111/2024", "", "", "", "", "")
            ));

        SortedMap<String, Object> multipleObjects = excelReadingService.readExcel(
            userToken,
            documentBinaryUrl,
            errors,
            new MultipleData(),
            FilterExcelType.ALL
        );

        assertEquals(1, errors.size());
        assertEquals("Worksheet name not found", errors.getFirst());
        Assertions.assertTrue(multipleObjects.isEmpty());
    }

    @Test
    void setSubMultipleFieldInSingleCaseDataShouldSetSingleSpaceForBlankSubMultiple() throws IOException {
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseId(1234L);
        CaseData caseData = new CaseData();
        submitEvent.setCaseData(caseData);
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setJurisdiction("EMPLOYMENT");
        multipleDetails.setCaseTypeId("Leeds_Multiple");
        when(ccdClient.retrieveCasesElasticSearch(anyString(),
            anyString(), anyList()))
            .thenReturn(List.of(submitEvent));

        excelReadingService.setSubMultipleFieldInSingleCaseData(userToken,
            multipleDetails,
            "1234",
            "");
        assertEquals(" ", caseData.getSubMultipleName());
    }

    @Test
    void readClaimantsFromSpreadsheetShouldParseRowsAndSkipBlankRow() throws IOException {
        when(excelDocManagementService.downloadExcelDocument(userToken, documentBinaryUrl))
            .thenReturn(buildClaimantsWorkbookStream(
                List.of("Teitl", "First Name", "last_name", "DOB", "e-bost", "Address Line 1",
                    "Address Line 2", "Town or City", "Gwlad", "Cod Post"),
                List.of("Mr", "John", "Doe", "1990-01-01", "john@example.com", "1 Main St", "Flat 1",
                    "Cardiff", "Wales", "CF10 1AA"),
                List.of("", "", "", "", "", "", "", "", "", "")
            ));

        List<AdditionalClaimant> claimants = excelReadingService.readClaimantsFromSpreadsheet(
            userToken, documentBinaryUrl, errors);

        assertEquals(1, claimants.size());
        AdditionalClaimant claimant = claimants.getFirst();
        assertEquals("Mr", claimant.getTitle());
        assertEquals("John", claimant.getFirstName());
        assertEquals("Doe", claimant.getLastName());
        assertEquals("1990-01-01", claimant.getDob());
        assertEquals("john@example.com", claimant.getEmail());
        assertEquals("1 Main St", claimant.getAddress().getAddressLine1());
        assertEquals("Flat 1", claimant.getAddress().getAddressLine2());
        assertEquals("Cardiff", claimant.getAddress().getPostTown());
        assertEquals("Wales", claimant.getAddress().getCountry());
        assertEquals("CF10 1AA", claimant.getAddress().getPostCode());
        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    void readClaimantsFromSpreadsheetShouldParseExcelDateCellAsIso() throws IOException {
        when(excelDocManagementService.downloadExcelDocument(userToken, documentBinaryUrl))
            .thenReturn(buildClaimantsWorkbookStreamWithDateCell(
                List.of("firstName", "lastName", "dob"),
                    LocalDate.of(2001, 2, 3)
            ));

        List<AdditionalClaimant> claimants = excelReadingService.readClaimantsFromSpreadsheet(
            userToken, documentBinaryUrl, errors);

        assertEquals(1, claimants.size());
        assertEquals("2001-02-03", claimants.getFirst().getDob());
    }

    @Test
    void readClaimantsFromSpreadsheetShouldHandleWorkbookWithoutHeaderRowZero() throws IOException {
        when(excelDocManagementService.downloadExcelDocument(userToken, documentBinaryUrl))
            .thenReturn(buildClaimantsWorkbookWithoutHeaderRowZero());

        List<AdditionalClaimant> claimants = excelReadingService.readClaimantsFromSpreadsheet(
            userToken, documentBinaryUrl, errors);

        assertEquals(0, claimants.size());
        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    void readClaimantsFromSpreadsheetShouldAppendErrorOnIoException() throws IOException {
        when(excelDocManagementService.downloadExcelDocument(userToken, documentBinaryUrl))
            .thenThrow(new IOException("cannot read"));

        List<AdditionalClaimant> claimants = excelReadingService.readClaimantsFromSpreadsheet(
            userToken, documentBinaryUrl, errors);

        assertEquals(0, claimants.size());
        assertEquals(1, errors.size());
        Assertions.assertTrue(errors.getFirst().startsWith("Error reading additional claimant spreadsheet:"));
    }

    @Test
    void readClaimantsFromSpreadsheetShouldHandleNullSheet() throws IOException {
        ExcelReadingService serviceSpy = spy(excelReadingService);
        XSSFWorkbook workbook = mock(XSSFWorkbook.class);
        doReturn(workbook).when(serviceSpy).readWorkbook(userToken, documentBinaryUrl);
        when(workbook.getSheetAt(0)).thenReturn(null);

        List<AdditionalClaimant> claimants = serviceSpy.readClaimantsFromSpreadsheet(
            userToken, documentBinaryUrl, errors);

        assertEquals(0, claimants.size());
        assertEquals(List.of("No worksheet found in additional claimant spreadsheet"), errors);
    }

    @Test
    void buildHeaderMapShouldMapKnownHeadersAndSkipUnknownOnes() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("claimants");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Teitl");
            header.createCell(1).setCellValue("first_name");
            header.createCell(2).setCellValue("cyfenw");
            header.createCell(3).setCellValue("dyddiad geni");
            header.createCell(4).setCellValue("e-bost");
            header.createCell(5).setCellValue("llinell cyfeiriad 1");
            header.createCell(6).setCellValue("llinellcyfeiriad2");
            header.createCell(7).setCellValue("tref neu ddinas");
            header.createCell(8).setCellValue("gwlad");
            header.createCell(9).setCellValue("codpost");
            header.createCell(10).setCellValue("unknown-column");

            Map<String, Integer> headerMap = excelReadingService.buildHeaderMap(header);

            assertEquals(0, headerMap.get("title"));
            assertEquals(1, headerMap.get("firstName"));
            assertEquals(2, headerMap.get("lastName"));
            assertEquals(3, headerMap.get("dob"));
            assertEquals(4, headerMap.get("email"));
            assertEquals(5, headerMap.get("address1"));
            assertEquals(6, headerMap.get("address2"));
            assertEquals(7, headerMap.get("town"));
            assertEquals(8, headerMap.get("country"));
            assertEquals(9, headerMap.get("postcode"));
            Assertions.assertFalse(headerMap.containsKey("unknown-column"));
        }
    }

    @Test
    void readClaimantsFromSpreadsheetShouldHandleMissingOptionalColumns() throws IOException {
        when(excelDocManagementService.downloadExcelDocument(userToken, documentBinaryUrl))
            .thenReturn(buildClaimantsWorkbookStream(
                List.of("firstName", "lastName"),
                List.of("Alex", "Taylor"),
                List.of("", "")
            ));

        List<AdditionalClaimant> claimants = excelReadingService.readClaimantsFromSpreadsheet(
            userToken, documentBinaryUrl, errors);

        assertEquals(1, claimants.size());
        AdditionalClaimant claimant = claimants.getFirst();
        assertEquals("Alex", claimant.getFirstName());
        assertEquals("Taylor", claimant.getLastName());
        assertEquals("", claimant.getDob());
        assertEquals("", claimant.getEmail());
        assertEquals("", claimant.getAddress().getAddressLine1());
    }

    private ByteArrayInputStream buildWorkbookStreamWithSingleDataRow(
        String sheetName,
        boolean protectSheet,
        List<String> values
    ) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet(sheetName);
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Ethos ref");
            header.createCell(1).setCellValue("Sub multiple");
            header.createCell(2).setCellValue("Flag1");
            header.createCell(3).setCellValue("Flag2");
            header.createCell(4).setCellValue("Flag3");
            header.createCell(5).setCellValue("Flag4");

            Row row = sheet.createRow(1);
            for (int i = 0; i < values.size(); i++) {
                row.createCell(i).setCellValue(values.get(i));
            }

            if (protectSheet) {
                sheet.protectSheet(CONSTRAINT_KEY);
            }
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }

    private ByteArrayInputStream buildClaimantsWorkbookStream(
        List<String> headerValues,
        List<String>... dataRows
    ) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("claimants");
            Row header = sheet.createRow(0);
            for (int i = 0; i < headerValues.size(); i++) {
                header.createCell(i).setCellValue(headerValues.get(i));
            }
            for (int r = 0; r < dataRows.length; r++) {
                Row row = sheet.createRow(r + 1);
                List<String> values = dataRows[r];
                for (int c = 0; c < values.size(); c++) {
                    row.createCell(c).setCellValue(values.get(c));
                }
            }
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }

    private ByteArrayInputStream buildClaimantsWorkbookStreamWithDateCell(
        List<String> headerValues,
        LocalDate dob
    ) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("claimants");
            Row header = sheet.createRow(0);
            for (int i = 0; i < headerValues.size(); i++) {
                header.createCell(i).setCellValue(headerValues.get(i));
            }
            Row row = sheet.createRow(1);
            row.createCell(0).setCellValue("Jane");
            row.createCell(1).setCellValue("Smith");
            Cell dobCell = row.createCell(2);
            dobCell.setCellValue(java.sql.Date.valueOf(dob));
            org.apache.poi.ss.usermodel.CellStyle style = workbook.createCellStyle();
            style.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("yyyy-mm-dd"));
            dobCell.setCellStyle(style);
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }

    private ByteArrayInputStream buildClaimantsWorkbookWithoutHeaderRowZero() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("claimants");
            Row rowOne = sheet.createRow(1);
            rowOne.createCell(0).setCellValue("John");
            rowOne.createCell(1).setCellValue("Doe");
            workbook.write(outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }
}
