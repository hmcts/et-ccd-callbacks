package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaseTransferDifferentCountryService {

    private final CaseTransferUtils caseTransferUtils;
    private final CaseTransferEventService caseTransferEventService;

    public List<String> transferCase(CaseDetails caseDetails, String userToken) {
        var caseDataList = caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken);
        if (caseDataList.isEmpty()) {
            throw new IllegalStateException("No cases found for Case Transfer for "
                    + caseDetails.getCaseData().getEthosCaseReference());
        }

        var errors = validate(caseDataList);
        if (!errors.isEmpty()) {
            return errors;
        }

        return transferCases(caseDetails, caseDataList, userToken);
    }

    private List<String> validate(List<CaseData> cases) {
        var errors = new ArrayList<String>();

        for (var caseData : cases) {
            var validationErrors = caseTransferUtils.validateCase(caseData);
            if (!validationErrors.isEmpty()) {
                errors.addAll(validationErrors);
            }
        }

        return errors;
    }

    private List<String> transferCases(CaseDetails caseDetails, List<CaseData> caseDataList, String userToken) {
        var errors = new ArrayList<String>();
        var sourceCaseData = caseDetails.getCaseData();
        var newManagingOffice = caseDetails.getCaseData().getOfficeCT().getSelectedCode();

        for (CaseData caseData : caseDataList) {
            var params = CaseTransferEventParams.builder()
                    .userToken(userToken)
                    .caseTypeId(caseDetails.getCaseTypeId())
                    .jurisdiction(caseDetails.getJurisdiction())
                    .ethosCaseReference(caseData.getEthosCaseReference())
                    .sourceEthosCaseReference(sourceCaseData.getEthosCaseReference())
                    .newManagingOffice(newManagingOffice)
                    .positionType(sourceCaseData.getPositionTypeCT())
                    .reason(sourceCaseData.getReasonForCT())
                    .ecmCaseType(SINGLE_CASE_TYPE)
                    .transferSameCountry(false)
                    .build();

            log.info("Creating Case Transfer event for {}", sourceCaseData.getEthosCaseReference());
            var transferErrors = caseTransferEventService.transfer(params);
            if (!transferErrors.isEmpty()) {
                errors.addAll(transferErrors);
            }
        }

        sourceCaseData.setLinkedCaseCT("Transferred to " + newManagingOffice);
        sourceCaseData.setPositionType(sourceCaseData.getPositionTypeCT());
        sourceCaseData.setOfficeCT(null);
        sourceCaseData.setPositionTypeCT(null);
        sourceCaseData.setStateAPI(null);

        return errors;
    }
}
