package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ExcelReportHelper.addReportAdminDetails;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ExcelReportHelper.getCellStyle;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ExcelReportHelper.getHeaderCellStyle;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ExcelReportHelper.getReportSubTitleCellStyle;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ExcelReportHelper.getReportTitleCellStyle;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ExcelReportHelper.initializeReportHeaders;

@ExtendWith(SpringExtension.class)
class ExcelReportHelperTest {

    @Test
    void reportTitleCellStyleTest() throws IOException {
        try (XSSFWorkbook workbook = createWorkbook()) {
            CellStyle actualCellStyle = getReportTitleCellStyle(workbook);
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
            CellStyle actualCellStyle = getHeaderCellStyle(workbook);
            assertEquals(BorderStyle.THIN, actualCellStyle.getBorderBottom());
            assertEquals(IndexedColors.GREY_25_PERCENT.getIndex(), actualCellStyle.getBottomBorderColor());
            assertEquals(FillPatternType.SOLID_FOREGROUND, actualCellStyle.getFillPattern());
        }
    }

    @Test
    void reportSubTitleCellStyleTest() throws IOException {
        try (XSSFWorkbook workbook = createWorkbook()) {
            CellStyle actualCellStyle = getReportSubTitleCellStyle(workbook);
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
            addReportAdminDetails(workbook,
                    workbook.getSheetAt(0),
                    1,
                    "description", 6);
            assertEquals("description", sheet.getRow(1).getCell(0).getStringCellValue());
        }

    }

    @Test
    void cellStyleTest() throws IOException {
        try (XSSFWorkbook workbook = createWorkbook()) {
            CellStyle actualCellStyle = getCellStyle(workbook);
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
            initializeReportHeaders("document1",
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

    private XSSFWorkbook createWorkbook() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        workbook.createSheet("Sheet1");
        return workbook;
    }
}
