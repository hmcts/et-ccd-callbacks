package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ecm.common.model.helper.SchedulePayload;
import uk.gov.hmcts.ecm.common.model.schedule.SchedulePayloadEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesScheduleHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.SingleCasesReadingService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

@Slf4j
public class ScheduleCallable implements Callable<HashSet<SchedulePayload>> {

    private final SingleCasesReadingService singleCasesReadingService;
    private final String userToken;
    private final String caseTypeId;
    private final List<String> partitionCaseIds;

    public ScheduleCallable(SingleCasesReadingService singleCasesReadingService, String userToken, String caseTypeId,
                            List<String> partitionCaseIds) {
        this.singleCasesReadingService = singleCasesReadingService;
        this.userToken = userToken;
        this.caseTypeId = caseTypeId;
        this.partitionCaseIds = partitionCaseIds;
    }

    @Override
    public HashSet<SchedulePayload> call() {
        HashSet<SchedulePayload> schedulePayloads = new HashSet<>();
        Set<SchedulePayloadEvent> schedulePayloadEvents = singleCasesReadingService.retrieveScheduleCases(userToken,
                caseTypeId, partitionCaseIds);

        for (SchedulePayloadEvent schedulePayloadEvent : schedulePayloadEvents) {
            schedulePayloads.add(MultiplesScheduleHelper.getSchedulePayloadFromSchedulePayloadES(schedulePayloadEvent));
        }

        return schedulePayloads;
    }

}
