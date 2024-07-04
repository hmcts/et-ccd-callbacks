package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SuppressWarnings({"PMD.LooseCoupling", "PMD.LawOfDemeter", "PMD.TooManyMethods"})
@ExtendWith(SpringExtension.class)
class ExcelCreationServiceTest {

    @InjectMocks
    private ExcelCreationService excelCreationService;

    @Mock
    private SingleCasesReadingService singleCasesReadingService;

    String leadLink = "<a target=\"_blank\" href=\"https://www-ccd.perftest.platform.hmcts.net/v2/case/1604313560561842\">245000/2020</a>";

    private TreeMap<String, Object> multipleObjects;

    @BeforeEach
    public void setUp() {
        multipleObjects = MultipleUtil.getMultipleObjectsAll();
        when(singleCasesReadingService.retrieveSingleCase(any(), any(), any(), any()))
                .thenReturn(MultipleUtil.getSubmitEvents().get(0));
    }

    @Test
    void writeExcelObjects() {
        assertNotNull(excelCreationService.writeExcel(
                new ArrayList<>(multipleObjects.values()),
                new ArrayList<>(Arrays.asList("245000/1", "245000/1")),
                leadLink, "userToken", "caseTypeId"));
    }

    @Test
    void writeExcelObjectsEmptySubMultiples() {
        assertNotNull(excelCreationService.writeExcel(
                new ArrayList<>(multipleObjects.values()),
                new ArrayList<>(),
                leadLink, "userToken", "caseTypeId"));
    }

    @Test
    void writeExcelString() {
        assertNotNull(excelCreationService.writeExcel(
                new ArrayList<>(Arrays.asList("245000/2020", "245001/2020", "245002/2020")),
                new ArrayList<>(),
                leadLink, "userToken", "caseTypeId"));
    }

    @Test
    void writeExcelStringEmpty() {
        assertNotNull(excelCreationService.writeExcel(
                new ArrayList<>(),
                new ArrayList<>(),
                leadLink, "userToken", "caseTypeId"));
    }

    @Test
    void reportTitleCellStyleTest() throws IOException {
        try (XSSFWorkbook workbook = createWorkbook()) {
            CellStyle actualCellStyle = excelCreationService.getReportTitleCellStyle(workbook);
            assertEquals(IndexedColors.BLUE_GREY.getIndex(), actualCellStyle.getFillBackgroundColor());
            assertEquals(HorizontalAlignment.CENTER, actualCellStyle.getAlignment());
            assertEquals(FillPatternType.SOLID_FOREGROUND, actualCellStyle.getFillPattern());
            assertEquals(VerticalAlignment.CENTER, actualCellStyle.getVerticalAlignment());
            assertEquals(IndexedColors.LIGHT_GREEN.getIndex(), actualCellStyle.getFillForegroundColor());
        }
    }

    @Test
    void headerCellStyleTest() throws IOException {
        try (XSSFWorkbook workbook = createWorkbook()) {
            CellStyle actualCellStyle = excelCreationService.getHeaderCellStyle(workbook);
            assertEquals(BorderStyle.THIN, actualCellStyle.getBorderBottom());
            assertEquals(IndexedColors.GREY_25_PERCENT.getIndex(), actualCellStyle.getBottomBorderColor());
            assertEquals(FillPatternType.SOLID_FOREGROUND, actualCellStyle.getFillPattern());
        }
    }

    private XSSFWorkbook createWorkbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet("Sheet1");
        return workbook;
    }

    @Test
    void reportSubTitleCellStyleTest() throws IOException {
        try (XSSFWorkbook workbook = createWorkbook()) {
            CellStyle actualCellStyle = excelCreationService.getReportSubTitleCellStyle(workbook);
            assertEquals(IndexedColors.LIGHT_GREEN.getIndex(), actualCellStyle.getFillForegroundColor());
            assertEquals(FillPatternType.SOLID_FOREGROUND, actualCellStyle.getFillPattern());
            assertEquals(HorizontalAlignment.CENTER, actualCellStyle.getAlignment());
            assertEquals(VerticalAlignment.CENTER, actualCellStyle.getVerticalAlignment());
        }
    }

    @Test
    void addReportAdminDetailsTest() throws IOException {
        try (XSSFWorkbook workbook = createWorkbook()) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            excelCreationService.addReportAdminDetails(workbook,
                    workbook.getSheetAt(0),
                    1,
                    "description", 6);
            assertEquals("description", sheet.getRow(1).getCell(0).getStringCellValue());
        }

    }

    @Test
    void cellStyleTest() throws IOException {
        try (XSSFWorkbook workbook = createWorkbook()) {
            CellStyle actualCellStyle = excelCreationService.getCellStyle(workbook);
            assertEquals(VerticalAlignment.CENTER, actualCellStyle.getVerticalAlignment());
            assertEquals(IndexedColors.WHITE1.getIndex(), actualCellStyle.getFillForegroundColor());
            assertEquals(FillPatternType.SOLID_FOREGROUND, actualCellStyle.getFillPattern());
            assertEquals(HorizontalAlignment.CENTER, actualCellStyle.getAlignment());
            assertEquals(BorderStyle.THIN, actualCellStyle.getBorderBottom());
            assertEquals(BorderStyle.NONE, actualCellStyle.getBorderTop());
            assertEquals(BorderStyle.NONE, actualCellStyle.getBorderLeft());
            assertEquals(BorderStyle.NONE, actualCellStyle.getBorderRight());
            assertEquals(IndexedColors.GREY_25_PERCENT.getIndex(), actualCellStyle.getBottomBorderColor());
        }
    }

    @Test
    void initializeReportHeadersTest() throws IOException {
        try (XSSFWorkbook workbook = createWorkbook()) {
            Sheet sheet = workbook.getSheetAt(0);
            excelCreationService.initializeReportHeaders("document1",
                    "description",
                    workbook,
                    workbook.getSheetAt(0),
                    List.of("header1", "header2", "header3", "header4", "header5"));
            assertEquals("description", sheet.getRow(1).getCell(0).getStringCellValue());
            for (int j = 0; j < 5; j++) {
                assertEquals("header" + (j + 1), sheet.getRow(2).getCell(j).getStringCellValue());
            }
        }

    }
}