package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
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
import uk.gov.hmcts.ethos.replacement.docmosis.tasks.ScheduleCallable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.CONSTRAINT_KEY;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.HIDDEN_SHEET_NAME;
import static uk.gov.hmcts.et.common.model.multiples.MultipleConstants.SHEET_NAME;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ExcelReportHelper.createCell;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ExcelReportHelper.initializeHeaders;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.ES_PARTITION_SIZE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.THREAD_NUMBER;

@Slf4j
@Service("excelCreationService")
@RequiredArgsConstructor
public class ExcelCreationService {
    private static final int WIDTH = 256;
    private static final int EXTRA_SPACE = 6;
    private static final String CLAIMANT_NOT_FOUND = "Claimant not found";
    private final SingleCasesReadingService singleCasesReadingService;

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
        return getSchedulePayloadCollection(userToken, caseTypeId, ethosCaseRefCollection, new ArrayList<>());
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

    private List<SchedulePayload> getSchedulePayloadCollection(String userToken, String caseTypeId,
                                                              List<String> caseIdCollection, List<String> errors) {

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_NUMBER);

        List<Future<HashSet<SchedulePayload>>> resultList = new ArrayList<>();

        log.info("CaseIdCollectionSize: {}", caseIdCollection.size());

        for (List<String> partitionCaseIds : Lists.partition(caseIdCollection, ES_PARTITION_SIZE)) {

            ScheduleCallable scheduleCallable =
                    new ScheduleCallable(singleCasesReadingService, userToken, caseTypeId, partitionCaseIds);

            resultList.add(executor.submit(scheduleCallable));

        }

        List<SchedulePayload> result = new ArrayList<>();

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

        log.info("ResultSize: {}", result.size());

        return result;

    }

}
