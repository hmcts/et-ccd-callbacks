package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferUtils.getTransferValidationErrors;

@Service("caseTransferService")
@RequiredArgsConstructor
@Slf4j
public class CaseTransferSameCountryService {

    private final CaseTransferUtils caseTransferUtils;
    private final CaseTransferEventService caseTransferEventService;

    public List<String> transferCase(CaseDetails caseDetails, String userToken) {
        List<CaseData> caseDataList = caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken);
        CaseData caseData = caseDetails.getCaseData();
        if (caseDataList.isEmpty()) {
            throw new IllegalStateException("No cases found for Case Transfer for "
                                            + caseData.getEthosCaseReference());
        }

        // If transferring to the same office, skip validation and transfer directly
        if (caseData.getOfficeCT().getSelectedCode().equals(caseData.getManagingOffice())) {
            return transferCases(caseDetails, caseDataList, userToken);
        }
        List<String> errors = getTransferValidationErrors(caseDataList, caseTransferUtils);

        if (!errors.isEmpty()) {
            return errors;
        }

        return transferCases(caseDetails, caseDataList, userToken);
    }

    public List<String> updateEccLinkedCase(CaseDetails caseDetails, String userToken) {
        return transferCases(caseDetails, Collections.emptyList(), userToken);
    }

    private List<String> transferCases(CaseDetails caseDetails, List<CaseData> caseDataList, String userToken) {
        List<String> errors = new ArrayList<>();

        // Remove the source case as we will be updating this directly rather than sending it to the event queue
        CaseData sourceCaseData = caseDetails.getCaseData();
        List<CaseData> casesToSubmitTransferEvent = caseDataList.stream()
                .filter(caseData -> !caseData.getEthosCaseReference().equals(sourceCaseData.getEthosCaseReference()))
                .toList();

        String newManagingOffice = sourceCaseData.getOfficeCT().getSelectedCode();

        for (CaseData caseData : casesToSubmitTransferEvent) {
            CaseTransferEventParams params = CaseTransferEventParams.builder()
                    .userToken(userToken)
                    .caseTypeId(caseDetails.getCaseTypeId())
                    .jurisdiction(caseDetails.getJurisdiction())
                    .ethosCaseReferences(List.of(caseData.getEthosCaseReference()))
                    .sourceEthosCaseReference(sourceCaseData.getEthosCaseReference())
                    .newManagingOffice(newManagingOffice)
                    .reason(sourceCaseData.getReasonForCT())
                    .multipleReference(SINGLE_CASE_TYPE)
                    .confirmationRequired(false)
                    .transferSameCountry(true)
                    .build();

            log.info("Creating Case Transfer event for {}", caseData.getEthosCaseReference());
            List<String> transferErrors = caseTransferEventService.transfer(params);
            if (!transferErrors.isEmpty()) {
                errors.addAll(transferErrors);
            }
        }

        // If transferring to a different office, clear the file location and clerk responsible as offices have
        // different values
        if (!newManagingOffice.equals(sourceCaseData.getManagingOffice())) {
            sourceCaseData.setFileLocation(null);
            sourceCaseData.setClerkResponsible(null);
        }

        sourceCaseData.setManagingOffice(newManagingOffice);
        sourceCaseData.setOfficeCT(null);
        sourceCaseData.setStateAPI(null);

        return errors;
    }
}
