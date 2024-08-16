package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.SchedulePayload;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FilterExcelType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesScheduleHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO_CASES_SEARCHED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ExcelReportHelper.getSchedulePayloadCollection;

@Slf4j
@RequiredArgsConstructor
@Service("multipleScheduleService")
public class MultipleScheduleService {

    private final ExcelReadingService excelReadingService;
    private final SingleCasesReadingService singleCasesReadingService;
    private final ExcelDocManagementService excelDocManagementService;
    private final FeatureToggleService featureToggleService;

    public DocumentInfo bulkScheduleLogic(String userToken, MultipleDetails multipleDetails, List<String> errors) {

        log.info("Read excel for schedule logic");
        MultipleData multipleData = multipleDetails.getCaseData();

        FilterExcelType filterExcelType =
                MultiplesScheduleHelper.getFilterExcelTypeByScheduleDoc(multipleData);

        SortedMap<String, Object> multipleObjects =
                excelReadingService.readExcel(
                        userToken,
                        MultiplesHelper.getExcelBinaryUrl(multipleData),
                        errors,
                        multipleDetails.getCaseData(),
                        filterExcelType);

        log.info("Pull information from single cases");

        List<String> sortedCaseIdsCollection =
                sortCollectionByEthosCaseRef(getCaseIdCollectionFromFilter(multipleObjects, filterExcelType));

        List<SchedulePayload> schedulePayloads =
                getSchedulePayloadCollection(userToken, multipleDetails.getCaseTypeId(),
                        sortedCaseIdsCollection, errors, log, singleCasesReadingService);

        if (featureToggleService.isMultiplesEnabled()) {
            if (YES.equals(multipleData.getLiveCases())) {
                log.info("Filtering live cases");
                schedulePayloads = schedulePayloads.stream()
                        .filter(schedulePayload -> !CLOSED_STATE.equals(schedulePayload.getState()))
                        .toList();
            }
            multipleData.setLiveCases(null);
        }

        log.info("Generate schedule");

        DocumentInfo documentInfo =
                generateSchedule(userToken, multipleObjects, multipleDetails, schedulePayloads, errors);

        log.info("Resetting mid fields");

        MultiplesHelper.resetMidFields(multipleDetails.getCaseData());

        return documentInfo;
    }

    private List<String> sortCollectionByEthosCaseRef(List<String> caseIdsCollection) {
        return caseIdsCollection
                .stream()
                .sorted(Comparator.comparing(ethosCaseRef -> ethosCaseRef,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private List<String> getCaseIdCollectionFromFilter(SortedMap<String, Object> multipleObjects,
                                                       FilterExcelType filterExcelType) {

        if (filterExcelType.equals(FilterExcelType.FLAGS)) {

            return new ArrayList<>(multipleObjects.keySet());

        } else {

            return MultiplesScheduleHelper.getSubMultipleCaseIds(multipleObjects);

        }

    }

    private DocumentInfo generateSchedule(String userToken, SortedMap<String, Object> multipleObjectsFiltered,
                                          MultipleDetails multipleDetails, List<SchedulePayload> schedulePayloads,
                                          List<String> errors) {

        DocumentInfo documentInfo = new DocumentInfo();

        if (multipleObjectsFiltered.isEmpty()) {
            errors.add(NO_CASES_SEARCHED);
        } else {
            documentInfo = excelDocManagementService.writeAndUploadScheduleDocument(userToken,
                    multipleObjectsFiltered, multipleDetails, schedulePayloads);
        }

        return documentInfo;
    }

}