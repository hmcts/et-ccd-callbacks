package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FilterExcelType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADD_CASES_TO_MULTIPLE_AMENDMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LEAD_CASE_AMENDMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REMOVE_CASES_FROM_MULTIPLE_AMENDMENT;

@Slf4j
@Service("multipleAmendService")
public class MultipleAmendService {

    private final ExcelReadingService excelReadingService;
    private final ExcelDocManagementService excelDocManagementService;
    private final MultipleAmendLeadCaseService multipleAmendLeadCaseService;
    private final MultipleAmendCaseIdsService multipleAmendCaseIdsService;

    private static final List<String> TYPES_OF_AMENDMENT = Arrays.asList(
            LEAD_CASE_AMENDMENT,
            ADD_CASES_TO_MULTIPLE_AMENDMENT,
            REMOVE_CASES_FROM_MULTIPLE_AMENDMENT);

    @Autowired
    public MultipleAmendService(ExcelReadingService excelReadingService,
                                ExcelDocManagementService excelDocManagementService,
                                MultipleAmendLeadCaseService multipleAmendLeadCaseService,
                                MultipleAmendCaseIdsService multipleAmendCaseIdsService) {
        this.excelReadingService = excelReadingService;
        this.excelDocManagementService = excelDocManagementService;
        this.multipleAmendLeadCaseService = multipleAmendLeadCaseService;
        this.multipleAmendCaseIdsService = multipleAmendCaseIdsService;
    }

    public void bulkAmendMultipleLogic(String userToken, MultipleDetails multipleDetails, List<String> errors) {

        log.info("Read excel to amend multiple");
        MultipleData multipleData = multipleDetails.getCaseData();
        SortedMap<String, Object> multipleObjects = excelReadingService.readExcel(
                        userToken, MultiplesHelper.getExcelBinaryUrl(multipleData),
                        errors, multipleData, FilterExcelType.ALL);

        List<?> newMultipleObjects = new ArrayList<>();

        List<String> typesOfAmendment = multipleData.getTypeOfAmendmentMSL();
        if (TYPES_OF_AMENDMENT.stream().anyMatch(typesOfAmendment::contains)) {

            if (typesOfAmendment.contains(LEAD_CASE_AMENDMENT)) {
                log.info("Amend lead case logic");
                newMultipleObjects = multipleAmendLeadCaseService.bulkAmendLeadCaseLogic(
                        userToken, multipleDetails, errors, multipleObjects);
            }

            if (typesOfAmendment.contains(ADD_CASES_TO_MULTIPLE_AMENDMENT) && errors.isEmpty()) {
                log.info("Amend case ids logic");
                newMultipleObjects = multipleAmendCaseIdsService.bulkAmendCaseIdsLogic(
                        userToken, multipleDetails, errors, multipleObjects);
            }

            if (typesOfAmendment.contains(REMOVE_CASES_FROM_MULTIPLE_AMENDMENT) && errors.isEmpty()) {
                log.info("Remove case ids logic");
                multipleAmendCaseIdsService.bulkRemoveCaseIdsLogic(
                        userToken, multipleDetails, errors, multipleObjects);
            }

            if (errors.isEmpty() && !newMultipleObjects.isEmpty()) {
                log.info("Create a new Excel");
                excelDocManagementService.generateAndUploadExcel(newMultipleObjects, userToken, multipleDetails);
            }

        }

        log.info("Clearing the payload");

        multipleDetails.getCaseData().setAmendLeadCase(null);
        multipleDetails.getCaseData().setCaseIdCollection(null);
        multipleDetails.getCaseData().setTypeOfAmendmentMSL(null);
        multipleDetails.getCaseData().setAltCaseIdCollection(null);
    }
}