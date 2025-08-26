package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import uk.gov.hmcts.ecm.common.model.helper.SchedulePayload;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.SingleCasesReadingService;
import uk.gov.hmcts.ethos.replacement.docmosis.tasks.ScheduleCallable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ES_PARTITION_SIZE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.THREAD_NUMBER;

public final class ExcelReportHelper {
    private ExcelReportHelper() {
    }

    public static CellStyle getReportTitleCellStyle(XSSFWorkbook workbook) {
        Font font = getFont(workbook);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        font.setFontHeightInPoints((short) 25);
        CellStyle cellStyle = getHeadersCellStyle(workbook);
        cellStyle.setFont(font);
        cellStyle.setFillBackgroundColor(IndexedColors.BLUE_GREY.getIndex());
        return cellStyle;
    }

    public static CellStyle getHeaderCellStyle(XSSFWorkbook workbook) {
        Font font = getFont(workbook);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        CellStyle cellStyle = getHeadersCellStyle(workbook);
        cellStyle.setFont(font);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return cellStyle;
    }

    private static CellStyle getHeadersCellStyle(XSSFWorkbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return cellStyle;
    }

    private static Font getFont(XSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontName("Calibre");
        font.setColor(IndexedColors.DARK_GREEN.getIndex());
        font.setFontHeightInPoints((short) 16);
        return font;
    }

    public static CellStyle getReportSubTitleCellStyle(XSSFWorkbook workbook) {
        Font font = getFont(workbook);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        font.setFontHeightInPoints((short) 20);
        CellStyle cellStyle = getHeadersCellStyle(workbook);
        cellStyle.setFont(font);
        return cellStyle;
    }

    public static void addReportAdminDetails(XSSFWorkbook workbook, XSSFSheet sheet, int rowIndex,
                                             String reportPrintedOnDescription, int lastCol) {
        CellRangeAddress reportTitleCellRange = new CellRangeAddress(rowIndex, rowIndex, 0, lastCol);
        sheet.addMergedRegion(reportTitleCellRange);
        XSSFRow rowReportTitle = sheet.createRow(rowIndex);
        rowReportTitle.setHeight((short) (rowReportTitle.getHeight() * 8));
        CellStyle styleForHeaderCell = getCellStyle(workbook);
        styleForHeaderCell.setAlignment(HorizontalAlignment.CENTER);
        styleForHeaderCell.setBorderTop(BorderStyle.THIN);
        styleForHeaderCell.setBorderLeft(BorderStyle.THIN);
        styleForHeaderCell.setBorderRight(BorderStyle.THIN);
        styleForHeaderCell.setBorderBottom(BorderStyle.THIN);
        styleForHeaderCell.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
        styleForHeaderCell.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        styleForHeaderCell.setFont(getFont(workbook));
        createCell(rowReportTitle, 0, reportPrintedOnDescription, styleForHeaderCell);
    }

    public static void initializeHeaders(XSSFSheet sheet, CellStyle styleForLocking) {
        XSSFRow rowHead = sheet.createRow(0);

        List<String> headers = MultiplesHelper.getHeaders();
        for (int j = 0; j < headers.size(); j++) {
            rowHead.createCell(j).setCellValue(headers.get(j));
            createCell(rowHead, j, headers.get(j), styleForLocking);
        }
    }

    public static CellStyle getCellStyle(XSSFWorkbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        Font font = getFont(workbook);
        font.setColor(IndexedColors.BLACK1.getIndex());
        font.setFontHeightInPoints((short) 14);
        font.setBold(false);
        cellStyle.setFont(font);
        cellStyle.setFillForegroundColor(IndexedColors.WHITE1.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setBorderTop(BorderStyle.NONE);
        cellStyle.setBorderLeft(BorderStyle.NONE);
        cellStyle.setBorderRight(BorderStyle.NONE);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return cellStyle;
    }

    public static void initializeReportHeaders(String documentName, String periodDescription, XSSFWorkbook workbook,
                                               XSSFSheet sheet, List<String> headers) {
        CellRangeAddress reportTitleCellRange = new CellRangeAddress(0, 0, 0, headers.size() - 1);
        sheet.addMergedRegion(reportTitleCellRange);
        XSSFRow rowReportTitle = sheet.createRow(0);
        rowReportTitle.setHeight((short) (rowReportTitle.getHeight() * 8));
        CellStyle styleForHeaderCell = getReportTitleCellStyle(workbook);
        createCell(rowReportTitle, 0, documentName, styleForHeaderCell);

        CellRangeAddress reportPeriodCellRange = new CellRangeAddress(1, 1, 0, headers.size() - 1);
        sheet.addMergedRegion(reportPeriodCellRange);
        XSSFRow rowReportPeriod = sheet.createRow(1);
        rowReportPeriod.setHeight((short) (rowReportPeriod.getHeight() * 6));
        CellStyle styleForSubTitleCell = getReportSubTitleCellStyle(workbook);
        createCell(rowReportPeriod, 0, periodDescription, styleForSubTitleCell);

        XSSFRow rowHead = sheet.createRow(2);
        rowHead.setHeight((short) (rowHead.getHeight() * 4));
        CellStyle styleForColHeaderCell = getHeaderCellStyle(workbook);
        for (int j = 0; j < headers.size(); j++) {
            rowHead.createCell(j).setCellValue(headers.get(j));
            createCell(rowHead, j, headers.get(j), styleForColHeaderCell);
        }
    }

    public static void createCell(XSSFRow row, int cellIndex, String value, CellStyle style) {
        Cell cell = row.createCell(cellIndex);
        cell.setCellStyle(style);

        if (!Strings.isNullOrEmpty(value) && !value.isBlank()) {
            cell.setCellValue(value);
        }
    }

    public static List<SchedulePayload> getSchedulePayloadCollection(
            String userToken, String caseTypeId,
            List<String> caseIdCollection, List<String> errors, Logger log,
            SingleCasesReadingService singleCasesReadingService) {

        List<SchedulePayload> result;
        try (ExecutorService executor = Executors.newFixedThreadPool(THREAD_NUMBER)) {

            List<Future<HashSet<SchedulePayload>>> resultList = new ArrayList<>();

            log.info("CaseIdCollectionSize: {}", caseIdCollection.size());

            for (List<String> partitionCaseIds : Lists.partition(caseIdCollection, ES_PARTITION_SIZE)) {

                ScheduleCallable scheduleCallable =
                        new ScheduleCallable(singleCasesReadingService, userToken, caseTypeId, partitionCaseIds);

                resultList.add(executor.submit(scheduleCallable));

            }

            result = new ArrayList<>();

            for (Future<HashSet<SchedulePayload>> fut : resultList) {

                try {

                    HashSet<SchedulePayload> schedulePayloads = fut.get();

                    log.info("PartialSize: {}", schedulePayloads.size());

                    result.addAll(schedulePayloads);

                } catch (InterruptedException | ExecutionException e) {

                    errors.add("Error Generating Schedules");

                    log.error(e.getMessage(), e);

                    Thread.currentThread().interrupt();

                }

            }

            executor.shutdown();
        }

        log.info("ResultSize: {}", result.size());

        return result;
    }
}
