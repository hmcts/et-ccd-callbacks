package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferDifferentCountryService.CASE_TRANSFERRED_POSITION_TYPE;

@Slf4j
@RequiredArgsConstructor
@Service
public class CaseTransferToEcmService {

    public static final String CASE_TRANSFERRED_TO_ECM = "Case transferred - ECM";

    public static final String NO_CASES_FOUND = "No cases to transfer found for case %s";

    private final CaseTransferUtils caseTransferUtils;
    private final CaseTransferEventService caseTransferEventService;

    public List<String> createCaseTransferToEcm(CaseDetails caseDetails, String userToken) {
        var errors = new ArrayList<String>();
        List<CaseData> caseDataList = caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken);

        if (caseDataList.isEmpty()) {
            errors.add(String.format(NO_CASES_FOUND, caseDetails.getCaseData().getEthosCaseReference()));
            return errors;
        }

        errors.addAll(validateCases(caseDataList));

        if (!errors.isEmpty()) {
            return errors;
        }

        return transferCase(caseDetails, caseDataList, userToken);
    }

    private List<String> transferCase(CaseDetails caseDetails, List<CaseData> caseDataList, String userToken) {
        List<String> errors = new ArrayList<>();
        var sourceCaseData = caseDetails.getCaseData();
        for (var caseData : caseDataList) {
            var params = CaseTransferToEcmParams.builder()
                .userToken(userToken)
                .caseTypeId(caseDetails.getCaseTypeId())
                .jurisdiction(caseDetails.getJurisdiction())
                .ethosCaseReferences(List.of(caseData.getEthosCaseReference()))
                .newCaseTypeId(caseData.getEcmOfficeCT())
                .positionType(CASE_TRANSFERRED_TO_ECM)
                .reason(sourceCaseData.getReasonForCT())
                .confirmationRequired(false)
                .build();

            log.info("Creating ECM Case Transfer event for {}", caseData.getEthosCaseReference());

            var transferErrors = caseTransferEventService.transferToEcm(params);
            if (!transferErrors.isEmpty()) {
                errors.addAll(transferErrors);
            }
        }

        sourceCaseData.setLinkedCaseCT("Transferred to ECM");
        sourceCaseData.setPositionType(CASE_TRANSFERRED_POSITION_TYPE);
        sourceCaseData.setOfficeCT(null);
        sourceCaseData.setStateAPI(null);

        return errors;
    }

    private List<String> validateCases(List<CaseData> caseDataList) {
        var errors = new ArrayList<String>();
        for (var caseData : caseDataList) {
            errors.addAll(caseTransferUtils.validateCase(caseData));
        }
        return errors;
    }

}
