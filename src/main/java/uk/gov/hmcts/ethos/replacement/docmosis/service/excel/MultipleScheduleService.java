package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.SchedulePayload;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FilterExcelType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesScheduleHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.tasks.ScheduleCallable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.SortedMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO_CASES_SEARCHED;

@Slf4j
@RequiredArgsConstructor
@Service("multipleScheduleService")
public class MultipleScheduleService {

    private final ExcelReadingService excelReadingService;
    private final SingleCasesReadingService singleCasesReadingService;
    private final ExcelDocManagementService excelDocManagementService;

    public static final int ES_PARTITION_SIZE = 500;
    public static final int THREAD_NUMBER = 20;
    public static final int SCHEDULE_LIMIT_CASES = 10_000;

    public DocumentInfo bulkScheduleLogic(String userToken, MultipleDetails multipleDetails, List<String> errors) {

        log.info("Read excel for schedule logic");

        FilterExcelType filterExcelType =
                MultiplesScheduleHelper.getFilterExcelTypeByScheduleDoc(multipleDetails.getCaseData());

        SortedMap<String, Object> multipleObjects =
                excelReadingService.readExcel(
                        userToken,
                        MultiplesHelper.getExcelBinaryUrl(multipleDetails.getCaseData()),
                        errors,
                        multipleDetails.getCaseData(),
                        filterExcelType);

        DocumentInfo documentInfo = new DocumentInfo();

        log.info("Validate limit of cases to generate schedules");

        if (multipleObjects.keySet().size() > SCHEDULE_LIMIT_CASES) {

            log.info("Number of cases exceed the limit of " + SCHEDULE_LIMIT_CASES);

            errors.add("Number of cases exceed the limit of " + SCHEDULE_LIMIT_CASES);

        } else {

            log.info("Pull information from single cases");

            List<String> sortedCaseIdsCollection =
                    sortCollectionByEthosCaseRef(getCaseIdCollectionFromFilter(multipleObjects, filterExcelType));

            List<SchedulePayload> schedulePayloads =
                    getSchedulePayloadCollection(userToken, multipleDetails.getCaseTypeId(),
                            sortedCaseIdsCollection, errors);

            log.info("Generate schedule");

            documentInfo = generateSchedule(userToken, multipleObjects, multipleDetails, schedulePayloads, errors);

        }

        log.info("Resetting mid fields");

        MultiplesHelper.resetMidFields(multipleDetails.getCaseData());

        return documentInfo;

    }

    private List<String> sortCollectionByEthosCaseRef(List<String> caseIdsCollection) {
        return caseIdsCollection
                .stream()
                .sorted(Comparator.comparing(ethosCaseRef -> ethosCaseRef,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(toList());
    }

    private List<String> getCaseIdCollectionFromFilter(SortedMap<String, Object> multipleObjects,
                                                       FilterExcelType filterExcelType) {

        if (filterExcelType.equals(FilterExcelType.FLAGS)) {

            return new ArrayList<>(multipleObjects.keySet());

        } else {

            return MultiplesScheduleHelper.getSubMultipleCaseIds(multipleObjects);

        }

    }

    private List<SchedulePayload> getSchedulePayloadCollection(String userToken, String caseTypeId,
                                                               List<String> caseIdCollection, List<String> errors) {

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_NUMBER);

        List<Future<HashSet<SchedulePayload>>> resultList = new ArrayList<>();

        log.info("CaseIdCollectionSize: " + caseIdCollection.size());

        for (List<String> partitionCaseIds : Lists.partition(caseIdCollection, ES_PARTITION_SIZE)) {

            ScheduleCallable scheduleCallable =
                    new ScheduleCallable(singleCasesReadingService, userToken, caseTypeId, partitionCaseIds);

            resultList.add(executor.submit(scheduleCallable));

        }

        List<SchedulePayload> result = new ArrayList<>();

        for (Future<HashSet<SchedulePayload>> fut : resultList) {

            try {

                HashSet<SchedulePayload> schedulePayloads = fut.get();

                log.info("PartialSize: " + schedulePayloads.size());

                result.addAll(schedulePayloads);

            } catch (InterruptedException | ExecutionException e) {

                errors.add("Error Generating Schedules");

                log.error(e.getMessage(), e);

                Thread.currentThread().interrupt();

            }

        }

        executor.shutdown();

        log.info("ResultSize: " + result.size());

        return result;

    }

    private DocumentInfo generateSchedule(String userToken, SortedMap<String, Object> multipleObjectsFiltered,
                                          MultipleDetails multipleDetails, List<SchedulePayload> schedulePayloads,
                                          List<String> errors) {

        DocumentInfo documentInfo = new DocumentInfo();

        if (!multipleObjectsFiltered.keySet().isEmpty()) {

            documentInfo = excelDocManagementService.writeAndUploadScheduleDocument(userToken,
                    multipleObjectsFiltered, multipleDetails, schedulePayloads);

        } else {

            errors.add(NO_CASES_SEARCHED);

        }

        return documentInfo;

    }

}
