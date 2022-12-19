package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FilterExcelType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.ExcelReadingService;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.UPDATING_STATE;

@Service
@RequiredArgsConstructor
@Slf4j
public class MultipleTransferDifferentCountryService {

    public static final String CASE_TRANSFERRED_POSITION_TYPE = "Case transferred - other country";

    @Value("${ccd_gateway_base_url}")
    private String ccdGatewayBaseUrl;

    private final ExcelReadingService excelReadingService;
    private final CaseTransferEventService caseTransferEventService;

    public List<String> transferMultiple(MultipleDetails multipleDetails, String userToken) {
        MultipleData multipleData = multipleDetails.getCaseData();
        List<String> errors = new ArrayList<>();

        String excelUrl = MultiplesHelper.getExcelBinaryUrl(multipleData);
        SortedMap<String, Object> multipleObjects = excelReadingService.readExcel(userToken, excelUrl, errors,
            multipleData, FilterExcelType.ALL);

        String multipleReference = multipleDetails.getCaseData().getMultipleReference();
        if (multipleObjects.keySet().isEmpty()) {
            log.info("No cases in the multiple {}", multipleReference);
            errors.add("No cases in the multiple");
        } else {
            multipleDetails.getCaseData().setState(UPDATING_STATE);
            transferSingleCases(userToken, multipleDetails, errors, multipleObjects);

            MultiplesHelper.resetMidFields(multipleDetails.getCaseData());
        }

        return errors;
    }

    private void transferSingleCases(String userToken, MultipleDetails multipleDetails,
                                     List<String> errors, SortedMap<String, Object> multipleObjects) {
        List<String> ethosCaseReferences = new ArrayList<>(multipleObjects.keySet());
        MultipleData multipleData = multipleDetails.getCaseData();
        String newManagingOffice = multipleData.getOfficeMultipleCT().getSelectedCode();
        String multipleReferenceLink = MultiplesHelper.generateMarkUp(ccdGatewayBaseUrl, multipleDetails.getCaseId(),
                multipleData.getMultipleReference());
        CaseTransferEventParams params = CaseTransferEventParams.builder()
                .userToken(userToken)
                .caseTypeId(multipleDetails.getCaseTypeId())
                .jurisdiction(multipleDetails.getJurisdiction())
                .ethosCaseReferences(ethosCaseReferences)
                .sourceEthosCaseReference(multipleData.getMultipleReference())
                .newManagingOffice(newManagingOffice)
                .positionType(CASE_TRANSFERRED_POSITION_TYPE)
                .reason(multipleDetails.getCaseData().getReasonForCT())
                .multipleReference(multipleData.getMultipleReference())
                .confirmationRequired(true)
                .multipleReferenceLink(multipleReferenceLink)
                .transferSameCountry(false)
                .build();

        log.info("Creating Multiple Transfer Different Country event for {}", multipleData.getMultipleReference());
        List<String> transferErrors = caseTransferEventService.transfer(params);
        if (!transferErrors.isEmpty()) {
            errors.addAll(transferErrors);
        }
    }
}
