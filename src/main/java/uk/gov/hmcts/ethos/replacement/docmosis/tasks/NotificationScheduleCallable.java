package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import uk.gov.hmcts.ecm.common.model.helper.NotificationSchedulePayload;
import uk.gov.hmcts.ecm.common.model.schedule.NotificationSchedulePayloadEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesScheduleHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.SingleCasesReadingService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

public class NotificationScheduleCallable implements Callable<HashSet<NotificationSchedulePayload>> {
    private final SingleCasesReadingService singleCasesReadingService;
    private final String userToken;
    private final String caseTypeId;
    private final List<String> partitionCaseIds;

    public NotificationScheduleCallable(SingleCasesReadingService singleCasesReadingService,
                                        String userToken,
                                        String caseTypeId,
                                        List<String> partitionCaseIds) {
        this.singleCasesReadingService = singleCasesReadingService;
        this.userToken = userToken;
        this.caseTypeId = caseTypeId;
        this.partitionCaseIds = partitionCaseIds;
    }

    @Override
    public HashSet<NotificationSchedulePayload> call() {
        HashSet<NotificationSchedulePayload> schedulePayloads = new HashSet<>();
        Set<NotificationSchedulePayloadEvent> schedulePayloadEvents =
                singleCasesReadingService.retrieveNotificationScheduleCases(userToken, caseTypeId, partitionCaseIds);

        for (NotificationSchedulePayloadEvent schedulePayloadEvent : schedulePayloadEvents) {
            NotificationSchedulePayload payload = MultiplesScheduleHelper.getNotificationSchedulePayload(
                    schedulePayloadEvent.getSchedulePayloadES());
            if (isNotEmpty(payload.getSendNotificationCollection())) {
                schedulePayloads.add(payload);
            }

        }

        return schedulePayloads;
    }
}
