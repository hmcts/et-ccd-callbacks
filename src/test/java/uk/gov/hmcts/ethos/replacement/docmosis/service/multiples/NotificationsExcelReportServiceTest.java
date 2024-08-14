package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.types.NotificationsExtract;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleObject;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FilterExcelType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SEND_NOTIFICATION_ALL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelDocManagementService.APPLICATION_EXCEL_VALUE;

@ExtendWith(SpringExtension.class)
class NotificationsExcelReportServiceTest {
    public static final String DOC_URL = "http://dm-store/documents/64668823-d355-4663-8d38-1ce3c61108df";
    @MockBean
    private ExcelReadingService excelReadingService;
    @MockBean
    private NotificationScheduleService notificationScheduleService;
    @MockBean
    private DocumentManagementService documentManagementService;

    private NotificationsExcelReportService notificationsExcelReportService;

    private MultipleDetails multipleDetails;
    private String userToken;

    private List<String> errors;

    @BeforeEach
    public void setUp() {
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID);
        multipleDetails.setCaseData(MultipleUtil.getMultipleDataForNotification());
        userToken = "authString";
        errors = new ArrayList<>();
        notificationsExcelReportService = new NotificationsExcelReportService(
                excelReadingService,
                notificationScheduleService,
                documentManagementService);

        var caseData = multipleDetails.getCaseData();
        caseData.setSendNotificationNotify(SEND_NOTIFICATION_ALL);
        when(notificationScheduleService
                .getSchedulePayloadCollection(
                        eq(userToken),
                        eq(ENGLANDWALES_BULK_CASE_TYPE_ID),
                        any(),
                        eq(errors)))
                .thenReturn(MultipleUtil
                        .getNotificationSchedulePayloadList("6000001/2024", "246000"));
        when(documentManagementService
                .uploadDocument(
                        eq(userToken),
                        any(),
                        eq("notifications_extract.xlsx"),
                        eq(APPLICATION_EXCEL_VALUE),
                        eq(ENGLANDWALES_BULK_CASE_TYPE_ID)))
                .thenReturn(URI.create(DOC_URL));

    }

    @Test
    void shouldGenerateReportAndSetCaseData() throws IOException {
        MultipleObject multipleObject1 = MultipleObject.builder().ethosCaseRef("6000001/2024").build();
        MultipleObject multipleObject2 = MultipleObject.builder().ethosCaseRef("6000001/2023").build();
        SortedMap<String, Object> sortedMap = new TreeMap<>();
        sortedMap.put("A", multipleObject1);
        sortedMap.put("B", multipleObject2);
        when(excelReadingService
                .readExcel(
                        userToken,
                        DOC_URL,
                        errors,
                        multipleDetails.getCaseData(),
                        FilterExcelType.ALL))
                .thenReturn(sortedMap);

        notificationsExcelReportService.generateReport(multipleDetails, userToken, errors);

        NotificationsExtract notificationsExtract = multipleDetails.getCaseData().getNotificationsExtract();
        assertEquals(DOC_URL, notificationsExtract.getNotificationsExtractFile().getDocumentUrl());
        assertEquals(DOC_URL + "/binary", notificationsExtract.getNotificationsExtractFile().getDocumentBinaryUrl());
        assertNotNull(notificationsExtract.getExtractDateTime());
    }
}
