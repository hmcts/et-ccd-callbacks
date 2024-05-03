package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.NotificationSchedulePayload;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.SingleCasesReadingService;
import uk.gov.hmcts.ethos.replacement.docmosis.tasks.NotificationScheduleCallable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleScheduleService.ES_PARTITION_SIZE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleScheduleService.THREAD_NUMBER;

@Slf4j
@RequiredArgsConstructor
@Service("notificationScheduleService")
public class NotificationScheduleService {
    private final SingleCasesReadingService singleCasesReadingService;

    /**
     * Threaded execution of ES queries to retrieve all notifications sent from multiple.
     *
     * @param userToken        user Token
     * @param caseTypeId       multiple case type (EW or Scotland)
     * @param caseIdCollection all single cases on the multiple
     * @param errors           errors
     * @return list of notifications from single cases
     */
    public List<NotificationSchedulePayload> getSchedulePayloadCollection(String userToken,
                                                                          String caseTypeId,
                                                                          List<String> caseIdCollection,
                                                                          List<String> errors) {

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_NUMBER);

        List<Future<HashSet<NotificationSchedulePayload>>> resultList = new ArrayList<>();

        log.info("CaseIdCollectionSize: {}", caseIdCollection.size());

        for (List<String> partitionCaseIds : Lists.partition(caseIdCollection, ES_PARTITION_SIZE)) {

            NotificationScheduleCallable scheduleCallable =
                    new NotificationScheduleCallable(
                            singleCasesReadingService,
                            userToken,
                            caseTypeId,
                            partitionCaseIds
                    );

            resultList.add(executor.submit(scheduleCallable));

        }

        List<NotificationSchedulePayload> result = new ArrayList<>();

        for (Future<HashSet<NotificationSchedulePayload>> fut : resultList) {

            try {

                HashSet<NotificationSchedulePayload> schedulePayloads = fut.get();

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
