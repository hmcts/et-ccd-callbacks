package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.NotificationSchedulePayload;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.SingleCasesReadingService;
import uk.gov.hmcts.ethos.replacement.docmosis.tasks.NotificationScheduleCallable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service("notificationScheduleService")
public class NotificationScheduleService {

    @Value("${es.partition.notifications}")
    private int esPartitionSize;

    private final SingleCasesReadingService singleCasesReadingService;

    /**
     * Execution of ES queries to retrieve all notifications sent from multiple.
     *
     * @param userToken        user Token
     * @param caseTypeId       multiple case type (EW or Scotland)
     * @param caseIdCollection all single cases on the multiple
     * @return list of notifications from single cases
     */
    public List<NotificationSchedulePayload> getSchedulePayloadCollection(String userToken,
                                                                          String caseTypeId,
                                                                          List<String> caseIdCollection) {

        log.info("CaseIdCollectionSize: {}", caseIdCollection.size());
        List<NotificationSchedulePayload> result = new ArrayList<>();

        for (List<String> partitionCaseIds : Lists.partition(caseIdCollection, esPartitionSize)) {

            NotificationScheduleCallable scheduleCallable =
                    new NotificationScheduleCallable(
                            singleCasesReadingService,
                            userToken,
                            caseTypeId,
                            partitionCaseIds
                    );
            try {
                HashSet<NotificationSchedulePayload> schedulePayloads = scheduleCallable.call();
                log.info("PartialSize: {}", schedulePayloads.size());
                result.addAll(schedulePayloads);
            } catch (Exception e) {
                log.error("Error calling NotificationScheduleCallable", e);
            }
        }
        log.info("ResultSize: {}", result.size());
        return result;
    }
}
