package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.SchedulePayload;
import uk.gov.hmcts.et.common.model.multiples.MultipleObject;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.ExcelGenerationException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.CONSTRAINT_KEY;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.HIDDEN_SHEET_NAME;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.SHEET_NAME;

@Slf4j
@Service("excelCreationService")
@RequiredArgsConstructor
public class ExcelCreationService {
    private static final int WIDTH = 256;
    private static final int EXTRA_SPACE = 6;
    private static final String CLAIMANT_NOT_FOUND = "Claimant not found";
    private final MultipleScheduleService multipleScheduleService;

    public byte[] writeExcel(List<?> multipleCollection,
                             List<String> subMultipleCollection,
                             String leadCaseString,
                             String userToken,
                             String caseTypeId) {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(SHEET_NAME);
            XSSFSheet hiddenSheet = workbook.createSheet(HIDDEN_SHEET_NAME);

            enableLocking(sheet);
            enableLocking(hiddenSheet);

            CellStyle styleForLocking = getStyleForLocking(workbook, false);

            initializeHeaders(sheet, styleForLocking);
            initializeData(
                    workbook,
                    sheet,
                    multipleCollection,
                    subMultipleCollection,
                    leadCaseString,
                    styleForLocking,
                    userToken,
                    caseTypeId
            );

            adjustColumnSize(sheet);
            createHiddenSheet(hiddenSheet, subMultipleCollection, styleForLocking);
            addSubMultiplesValidation(workbook, sheet, multipleCollection, subMultipleCollection);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);

            return bos.toByteArray();
        } catch (IOException e) {
            log.error("Error generating the excel");
            throw new ExcelGenerationException("Error generating the excel", e);
        }
    }

    private void enableLocking(XSSFSheet sheet) {
        sheet.lockDeleteColumns(true);
        sheet.lockDeleteRows(true);
        sheet.lockFormatCells(true);
        sheet.lockFormatColumns(true);
        sheet.lockFormatRows(true);
        sheet.lockInsertColumns(true);
        sheet.lockInsertRows(true);
        sheet.enableLocking();
        sheet.protectSheet(CONSTRAINT_KEY);
    }

    private CellStyle getStyleForUnLocking(XSSFWorkbook workbook) {
        CellStyle styleForUnLocking = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.BLUE.getIndex());

        styleForUnLocking.setLocked(false);
        styleForUnLocking.setAlignment(HorizontalAlignment.CENTER);
        styleForUnLocking.setFont(font);

        workbook.lockStructure();

        return styleForUnLocking;
    }

    private static CellStyle getStyleForLocking(XSSFWorkbook workbook, boolean lead) {
        CellStyle styleForLocking = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.BLACK.getIndex());

        if (lead) {
            font.setColor(IndexedColors.WHITE.getIndex());
            styleForLocking.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            styleForLocking.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        styleForLocking.setAlignment(HorizontalAlignment.CENTER);
        styleForLocking.setFont(font);

        return styleForLocking;
    }

    private static CellStyle getStyleForClaimant(XSSFWorkbook workbook) {
        CellStyle styleForClaimant = getStyleForLocking(workbook, false);
        styleForClaimant.setAlignment(HorizontalAlignment.CENTER);
        return styleForClaimant;
    }

    private void adjustColumnSize(XSSFSheet sheet) {
        // Adjust the column width to fit the content
        sheet.autoSizeColumn(0);
        sheet.setColumnWidth(1, 8000);
        for (int i = 2; i <= 5; i++) {
            sheet.setColumnWidth(i, 4000);
        }
        setWidthOfClaimantColumn(sheet);
    }

    private static void setWidthOfClaimantColumn(XSSFSheet sheet) {
        // Auto-sizing doesn't give enough space
        sheet.autoSizeColumn(6);
        int claimantColumnWidth = sheet.getColumnWidth(6);
        // Width is set in units of 1/256th of a character width
        claimantColumnWidth = (claimantColumnWidth / WIDTH + EXTRA_SPACE) * WIDTH;

        sheet.setColumnWidth(6, claimantColumnWidth);
    }

    private void createHiddenSheet(XSSFSheet hiddenSheet,
                                   List<String> subMultipleCollection,
                                   CellStyle styleForLocking) {
        if (!subMultipleCollection.isEmpty()) {
            for (int i = 0; i < subMultipleCollection.size(); i++) {
                XSSFRow row = hiddenSheet.createRow(i);
                createCell(row, 0, subMultipleCollection.get(i), styleForLocking);
            }
        }
    }

    private void addSubMultiplesValidation(XSSFWorkbook workbook, XSSFSheet sheet, List<?> multipleCollection,
                                           List<String> subMultipleCollection) {
        if (!subMultipleCollection.isEmpty() && !multipleCollection.isEmpty()) {
            Name namedCell = workbook.createName();
            namedCell.setNameName(HIDDEN_SHEET_NAME);
            namedCell.setRefersToFormula(HIDDEN_SHEET_NAME + "!$A$1:$A$" + subMultipleCollection.size());

            DataValidation dataValidation = getDataValidation(sheet, multipleCollection);

            workbook.setSheetHidden(1, true);
            sheet.addValidationData(dataValidation);
        }
    }

    private static @NotNull DataValidation getDataValidation(XSSFSheet sheet, List<?> multipleCollection) {
        CellRangeAddressList cellRangeAddressList =
                new CellRangeAddressList(1, multipleCollection.size(), 1, 1);
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createFormulaListConstraint(HIDDEN_SHEET_NAME);
        DataValidation dataValidation = helper.createValidation(constraint, cellRangeAddressList);
        dataValidation.setSuppressDropDownArrow(true);
        dataValidation.setShowErrorBox(true);
        return dataValidation;
    }

    private void initializeHeaders(XSSFSheet sheet, CellStyle styleForLocking) {
        XSSFRow rowHead = sheet.createRow(0);

        List<String> headers = MultiplesHelper.getHeaders();
        for (int j = 0; j < headers.size(); j++) {
            rowHead.createCell(j).setCellValue(headers.get(j));
            createCell(rowHead, j, headers.get(j), styleForLocking);
        }
    }

    public void createCell(XSSFRow row, int cellIndex, String value, CellStyle style) {
        Cell cell = row.createCell(cellIndex);
        cell.setCellStyle(style);

        if (!Strings.isNullOrEmpty(value) && !value.isBlank()) {
            cell.setCellValue(value);
        }
    }

    private void initializeData(XSSFWorkbook workbook, XSSFSheet sheet, List<?> multipleCollection,
                                List<String> subMultipleCollection, String leadCaseString,
                                CellStyle styleForLocking,
                                String userToken, String caseTypeId) {
        if (multipleCollection.isEmpty()) {
            return;
        }

        boolean isStringRefsList = multipleCollection.get(0) instanceof String;
        log.info(isStringRefsList ? "Initializing multipleRefs" : "Initializing data");

        SortedMap<String, SortedMap<String, Object>> orderedAllCasesList =
                MultiplesHelper.createCollectionOrderedByCaseRef(multipleCollection);
        if (orderedAllCasesList.isEmpty()) {
            return;
        }

        List<SchedulePayload> schedulePayloads = getSchedulePayloads(
                userToken,
                caseTypeId,
                orderedAllCasesList,
                isStringRefsList
        );

        final int[] rowIndex = {1};
        CellStyle styleForUnLocking = getStyleForUnLocking(workbook);
        CellStyle styleForClaimant = getStyleForClaimant(workbook);
        CellStyle styleForLockingLead = getStyleForLocking(workbook, true);
        String leadCase = MultiplesHelper.getCurrentLead(leadCaseString);

        log.info("Populating sheet");
        orderedAllCasesList.forEach((caseYear, caseYearList) ->
                caseYearList.forEach((caseNum, caseItem) -> {
                    if (isStringRefsList) {
                        constructCaseExcelRow(sheet, rowIndex[0], (String) caseItem, leadCase, null,
                                !subMultipleCollection.isEmpty(), styleForUnLocking, styleForLocking,
                                styleForLockingLead, styleForClaimant, schedulePayloads);
                    } else {
                        MultipleObject multipleObject = (MultipleObject) caseItem;
                        constructCaseExcelRow(sheet, rowIndex[0], multipleObject.getEthosCaseRef(), leadCase,
                                multipleObject, !subMultipleCollection.isEmpty(), styleForUnLocking, styleForLocking,
                                styleForLockingLead, styleForClaimant, schedulePayloads);
                    }
                    rowIndex[0]++;
                }));
    }

    private List<SchedulePayload> getSchedulePayloads(String userToken,
                                                      String caseTypeId,
                                                      SortedMap<String, SortedMap<String, Object>> orderedAllCasesList,
                                                      boolean isStringRefsList) {
        log.info("Extracting EthosCaseRefs");
        List<String> ethosCaseRefCollection = extractEthosCaseRefs(orderedAllCasesList, isStringRefsList);

        log.info("Pulling information from single cases");
        return multipleScheduleService.getSchedulePayloadCollection(userToken, caseTypeId,
                ethosCaseRefCollection, new ArrayList<>());
    }

    private List<String> extractEthosCaseRefs(SortedMap<String, SortedMap<String, Object>> orderedAllCasesList,
                                              boolean isStringRefsList) {
        List<String> ethosCaseRefCollection = new ArrayList<>();
        orderedAllCasesList.forEach((caseYear, caseYearList) ->
                caseYearList.forEach((caseNum, caseItem) -> {
                    if (isStringRefsList) {
                        ethosCaseRefCollection.add(caseItem.toString());
                    } else {
                        MultipleObject multipleObject = (MultipleObject) caseItem;
                        ethosCaseRefCollection.add(multipleObject.getEthosCaseRef());
                    }
                }));
        return ethosCaseRefCollection;
    }

    private void constructCaseExcelRow(XSSFSheet sheet, int rowIndex, String ethosCaseRef,
                                       String leadCase, MultipleObject multipleObject, boolean hasSubMultiples,
                                       CellStyle styleForUnLocking, CellStyle styleForLocking,
                                       CellStyle styleForLockingLead, CellStyle styleForClaimant,
                                       List<SchedulePayload> schedulePayloads) {

        XSSFRow row = sheet.createRow(rowIndex);

        int columnIndex = 0;
        createFirstColumn(ethosCaseRef, leadCase, styleForLocking, styleForLockingLead, row, columnIndex);

        if (multipleObject == null) {
            for (int k = 0; k < MultiplesHelper.getHeaders().size() - 2; k++) {
                columnIndex++;
                if (k == 0 && !hasSubMultiples) {
                    createCell(row, columnIndex, "", styleForLocking);
                } else {
                    // Create empty cells unlocked
                    createCell(row, columnIndex, "", styleForUnLocking);
                }
            }
        } else {
            columnIndex++;
            if (hasSubMultiples) {
                createCell(row, columnIndex, multipleObject.getSubMultiple(), styleForUnLocking);
            } else {
                createCell(row, columnIndex, multipleObject.getSubMultiple(), styleForLocking);
            }
            // Create these cells unlocked
            columnIndex++;
            createCell(row, columnIndex, multipleObject.getFlag1(), styleForUnLocking);
            columnIndex++;
            createCell(row, columnIndex, multipleObject.getFlag2(), styleForUnLocking);
            columnIndex++;
            createCell(row, columnIndex, multipleObject.getFlag3(), styleForUnLocking);
            columnIndex++;
            createCell(row, columnIndex, multipleObject.getFlag4(), styleForUnLocking);
        }
        columnIndex++;
        String claimant = schedulePayloads.stream()
                .filter(payload -> payload.getEthosCaseRef().equals(ethosCaseRef))
                .map(SchedulePayload::getClaimantName)
                .findFirst()
                .orElse(CLAIMANT_NOT_FOUND);
        createCell(row, columnIndex, claimant, styleForClaimant);
    }

    private void createFirstColumn(String ethosCaseRef,
                                   String leadCase,
                                   CellStyle styleForLocking,
                                   CellStyle styleForLockingLead,
                                   XSSFRow row,
                                   int columnIndex) {
        if (ethosCaseRef.equals(leadCase)) {
            log.info("Lead: {}", leadCase);
            createCell(row, columnIndex, ethosCaseRef, styleForLockingLead);
        } else {
            createCell(row, columnIndex, ethosCaseRef, styleForLocking);
        }
    }

    public CellStyle getReportTitleCellStyle(XSSFWorkbook workbook) {
        Font font = getFont(workbook);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        font.setFontHeightInPoints((short) 25);
        CellStyle cellStyle = getHeadersCellStyle(workbook);
        cellStyle.setFont(font);
        cellStyle.setFillBackgroundColor(IndexedColors.BLUE_GREY.getIndex());
        return cellStyle;
    }

    public CellStyle getHeaderCellStyle(XSSFWorkbook workbook) {
        Font font = getFont(workbook);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        CellStyle cellStyle = getHeadersCellStyle(workbook);
        cellStyle.setFont(font);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return cellStyle;
    }

    private CellStyle getHeadersCellStyle(XSSFWorkbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return cellStyle;
    }

    private Font getFont(XSSFWorkbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontName("Calibre");
        font.setColor(IndexedColors.DARK_GREEN.getIndex());
        font.setFontHeightInPoints((short) 16);
        return font;
    }

    public CellStyle getReportSubTitleCellStyle(XSSFWorkbook workbook) {
        Font font = getFont(workbook);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        font.setFontHeightInPoints((short) 20);
        CellStyle cellStyle = getHeadersCellStyle(workbook);
        cellStyle.setFont(font);
        return cellStyle;
    }

    public void addReportAdminDetails(XSSFWorkbook workbook, XSSFSheet sheet, int rowIndex,
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

    public CellStyle getCellStyle(XSSFWorkbook workbook) {
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

    public void initializeReportHeaders(String documentName, String periodDescription, XSSFWorkbook workbook,
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

}
