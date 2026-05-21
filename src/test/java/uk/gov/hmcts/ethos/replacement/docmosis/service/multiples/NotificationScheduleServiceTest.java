package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.model.schedule.NotificationSchedulePayloadEvent;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.SingleCasesReadingService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class NotificationScheduleServiceTest {
    @MockitoBean
    SingleCasesReadingService singleCasesReadingService;

    private String userToken;
    private List<String> caseIds;

    private NotificationScheduleService notificationScheduleService;

    @BeforeEach
    public void setUp() {
        notificationScheduleService = new NotificationScheduleService(singleCasesReadingService);
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID);
        multipleDetails.setCaseData(MultipleUtil.getMultipleDataForNotification());
        userToken = "authString";
        caseIds = Arrays.asList("245000/2020", "245003/2020");
        ReflectionTestUtils.setField(notificationScheduleService,
                "esPartitionSize",
                3000);
    }

    @Test
    void verifyServiceReturnsSuccessfully() {
        Set<NotificationSchedulePayloadEvent> schedulePayloadEvents =
                MultipleUtil.getNotificationSchedulePayloadEvents();

        when(singleCasesReadingService
                .retrieveNotificationScheduleCases(userToken, ENGLANDWALES_BULK_CASE_TYPE_ID, caseIds))
                .thenReturn(schedulePayloadEvents);
        var result = notificationScheduleService.getSchedulePayloadCollection(
                userToken,
                ENGLANDWALES_BULK_CASE_TYPE_ID,
                caseIds);

        Assertions.assertEquals(2, result.size());
    }

    @Test
    void verifySizeIsZeroWhenNoSendNotificationCollection() {
        Set<NotificationSchedulePayloadEvent> schedulePayloadEvents = new HashSet<>(List.of(
                MultipleUtil.getNotificationSchedulePayloadEventNoNotifications("245000/2020")));
        when(singleCasesReadingService
                .retrieveNotificationScheduleCases(userToken, ENGLANDWALES_BULK_CASE_TYPE_ID, caseIds))
                .thenReturn(schedulePayloadEvents);
        var result = notificationScheduleService.getSchedulePayloadCollection(userToken,
                ENGLANDWALES_BULK_CASE_TYPE_ID,
                caseIds);

        Assertions.assertEquals(0, result.size());
    }
}
