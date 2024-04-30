package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.utils.DateUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.NotificationSchedulePayload;
import uk.gov.hmcts.et.common.model.ccd.types.NotificationsExtract;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleObject;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.multiples.NotificationGroup;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FilterExcelType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesSchedulePrinter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;

import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.ScheduleConstants.HEADER_1;
import static uk.gov.hmcts.ecm.common.model.helper.ScheduleConstants.RESPONSE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelDocManagementService.APPLICATION_EXCEL_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleScheduleService.SCHEDULE_LIMIT_CASES;

@Slf4j
@RequiredArgsConstructor
@Service("notificationsExcelReportService")
public class NotificationsExcelReportService {
    private static final String FILE_NAME = "notifications_extract.xlsx";
    private final List<String> multipleHeaders = new ArrayList<>(Arrays.asList(HEADER_1, RESPONSE));
    private final ExcelReadingService excelReadingService;
    private final NotificationScheduleService notificationScheduleService;
    private final DocumentManagementService documentManagementService;

    public void generateReport(MultipleDetails multipleDetails,
                               String userToken,
                               List<String> errors) throws IOException {
        log.info("Reading excel file for cases");
        SortedMap<String, Object> multipleObjects = excelReadingService.readExcel(
                userToken, MultiplesHelper.getExcelBinaryUrl(multipleDetails.getCaseData()),
                errors, multipleDetails.getCaseData(), FilterExcelType.ALL);

        List<String> ethosCaseRefCollection = new ArrayList<>();
        multipleObjects.forEach((key, value) -> {
            MultipleObject excelRow = (MultipleObject) value;
            ethosCaseRefCollection.add(excelRow.getEthosCaseRef());
        });
        if (ethosCaseRefCollection.size() > SCHEDULE_LIMIT_CASES) {

            log.error("Number of cases exceed the limit of " + SCHEDULE_LIMIT_CASES);

            errors.add("Number of cases exceed the limit of " + SCHEDULE_LIMIT_CASES);

        } else {
            // Retrieve relevant case data from sub cases using ES query
            log.info("Retrieving case data from single cases");
            List<NotificationSchedulePayload> schedulePayloads =
                    notificationScheduleService.getSchedulePayloadCollection(
                            userToken,
                            multipleDetails.getCaseTypeId(),
                            ethosCaseRefCollection, errors
                    );

            MultipleData multipleData = multipleDetails.getCaseData();
            // Transform data for the extract
            List<NotificationGroup> notificationGroups = flattenNotificationsWithCaseRef(schedulePayloads,
                    multipleData.getMultipleReference());
            Map<Pair<String, String>, List<NotificationGroup>> notificationsGroupedByTitle =
                    groupNotificationsByTitleAndDate(notificationGroups);
            List<Map.Entry<Pair<String, String>, List<NotificationGroup>>> sortedList =
                    groupedNotificationsSortedByDate(notificationsGroupedByTitle);

            log.info("Generating extract");
            byte[] schedule = generateSchedule(multipleData, sortedList);

            log.info("Upload extract to doc store");
            URI documentSelfPath = documentManagementService.uploadDocument(userToken, schedule,
                    FILE_NAME, APPLICATION_EXCEL_VALUE, multipleDetails.getCaseTypeId());

            log.info("Set case data with extract");
            setCaseDataWithExtractedDocument(multipleData, documentSelfPath);
        }
    }

    private static void setCaseDataWithExtractedDocument(MultipleData multipleData,
                                                         URI documentSelfPath) {

        UploadedDocumentType uploadedDocumentType = UploadedDocumentType.builder()
                .documentFilename(FILE_NAME)
                .documentUrl(String.valueOf(documentSelfPath))
                .documentBinaryUrl(String.valueOf(documentSelfPath))
                .uploadTimestamp(new DateTime().toString())
                .categoryId("C4")
                .build();

        log.warn("Generated doc binary + {}", uploadedDocumentType.getDocumentBinaryUrl());

        if (multipleData.getNotificationsExtract() == null) {
            multipleData.setNotificationsExtract(new NotificationsExtract());
        }
        multipleData.getNotificationsExtract().setNotificationsExtractFile(uploadedDocumentType);

        DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.ENGLISH);
        multipleData.getNotificationsExtract().setExtractDateTime(formatter.format(new Date()));
    }

    private static @NotNull Map<Pair<String, String>, List<NotificationGroup>> groupNotificationsByTitleAndDate(
            List<NotificationGroup> notificationGroups) {
        return notificationGroups.stream()
                .collect(groupingBy(notificationGroup ->
                        new ImmutablePair<>(
                                notificationGroup.getNotificationTitle(),
                                notificationGroup.getDate()))
                );
    }

    private static @NotNull List<NotificationGroup> flattenNotificationsWithCaseRef(
            List<NotificationSchedulePayload> schedulePayloads, String multipleRef) {
        List<NotificationGroup> notificationGroups = new ArrayList<>();
        for (NotificationSchedulePayload schedulePayload : schedulePayloads) {
            for (SendNotificationTypeItem sendNotificationTypeItem : schedulePayload.getSendNotificationCollection()) {
                SendNotificationType notification = sendNotificationTypeItem.getValue();
                // Filter notifications sent from the multiple
                if (StringUtils.isNotEmpty(notification.getNotificationSentFrom())
                        && notification.getNotificationSentFrom().equals(multipleRef)) {
                    String responseReceived = isEmpty(notification.getRespondCollection()) ? NO : YES;
                    notificationGroups.add(NotificationGroup.builder()
                            .caseNumber(schedulePayload.getEthosCaseRef())
                            .date(notification.getDate())
                            .responseReceived(responseReceived)
                            .notificationTitle(notification.getSendNotificationTitle())
                            .notificationSubjectString(notification.getSendNotificationSubjectString())
                            .build()
                    );
                }
            }
        }
        return notificationGroups;
    }

    private static @NotNull List<Map.Entry<Pair<String, String>,
            List<NotificationGroup>>> groupedNotificationsSortedByDate(Map<Pair<String, String>,
            List<NotificationGroup>> notificationsGroupedByTitle) {
        return
                notificationsGroupedByTitle.entrySet().stream().sorted(
                        Comparator.comparing(e ->
                                DateUtils.parseDate(e.getKey().getRight(), new String[]{"dd MMM yyyy"})
                        )
                ).toList();
    }

    public byte[] generateSchedule(MultipleData multipleData,
                                   List<Map.Entry<Pair<String, String>,
                                           List<NotificationGroup>>> notificationsGroupedByTitle) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("NotificationsExtract");

            String multipleTitle = multipleData.getMultipleReference() + " - " + multipleData.getMultipleName();
            XSSFRow rowHead1 = sheet.createRow(0);
            CellStyle header1CellStyle = MultiplesSchedulePrinter.getHeader1CellStyle(workbook);
            createCell(rowHead1, 0, "List of notifications for ", header1CellStyle);
            createCell(rowHead1, 1, multipleTitle, header1CellStyle);

            log.warn("Init data");
            initData(workbook, sheet, notificationsGroupedByTitle);
            log.warn("Completed init data");

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            workbook.write(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("Error generating the excel");
            throw e;
        }
    }

    private void initData(XSSFWorkbook workbook,
                          XSSFSheet sheet,
                          List<Map.Entry<Pair<String, String>, List<NotificationGroup>>> notificationsGroupedByTitle) {
        if (notificationsGroupedByTitle.isEmpty()) {
            return;
        }

        CellStyle cellStyle = MultiplesSchedulePrinter.getRowCellStyle(workbook);
        int startingRow = 2;
        final int[] rowIndex = {0};
        for (Map.Entry<Pair<String, String>, List<NotificationGroup>> entry : notificationsGroupedByTitle) {
            setSectionHeaders(workbook, sheet, rowIndex, startingRow, entry);
            for (NotificationGroup notificationGroup : entry.getValue()) {
                int columnIndex = 0;
                XSSFRow row = sheet.createRow(rowIndex[0] + startingRow);
                createCell(row, columnIndex, notificationGroup.getCaseNumber(), cellStyle);
                columnIndex++;
                createCell(row, columnIndex, notificationGroup.getResponseReceived(), cellStyle);
                rowIndex[0]++;
            }
        }
    }

    private void setSectionHeaders(XSSFWorkbook workbook,
                                   XSSFSheet sheet,
                                   int[] rowIndex,
                                   int startingRow,
                                   Map.Entry<Pair<String, String>, List<NotificationGroup>> entry) {
        // Add empty row between notifications
        rowIndex[0]++;

        XSSFRow notificationTitleRow = sheet.createRow(rowIndex[0] + startingRow);
        rowIndex[0]++;

        String notificationTitle = entry.getKey().getKey()
                + " - "
                + entry.getKey().getValue()
                + " - "
                + entry.getValue().get(0).getNotificationSubjectString();

        createCell(notificationTitleRow, 0, notificationTitle,
                MultiplesSchedulePrinter.getHeader3CellStyle(workbook));

        XSSFRow tableTitleRow = sheet.createRow(rowIndex[0] + startingRow);
        rowIndex[0]++;
        for (int j = 0; j < multipleHeaders.size(); j++) {
            createCell(tableTitleRow, j, multipleHeaders.get(j),
                    MultiplesSchedulePrinter.getHeader3CellStyle(workbook));
        }
    }

    private void createCell(XSSFRow row, int cellIndex, String value, CellStyle style) {
        Cell cell = row.createCell(cellIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
