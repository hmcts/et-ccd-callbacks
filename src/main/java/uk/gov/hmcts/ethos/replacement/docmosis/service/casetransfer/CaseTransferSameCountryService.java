package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.ccd.CaseDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;

@RequiredArgsConstructor
@Service("caseTransferService")
public class CaseTransferSameCountryService {

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

    public List<String> updateEccLinkedCase(CaseDetails caseDetails, String userToken) {
        return transferCases(caseDetails, Collections.emptyList(), userToken);
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

        // Remove source case as we will be updating this directly rather than sending it to the event queue
        var sourceCaseData = caseDetails.getCaseData();
        var casesToSubmitTransferEvent = caseDataList.stream()
                .filter(caseData -> !caseData.getEthosCaseReference().equals(sourceCaseData.getEthosCaseReference()))
                .collect(Collectors.toList());

        var newManagingOffice = caseDetails.getCaseData().getOfficeCT().getSelectedCode();

        for (CaseData caseData : casesToSubmitTransferEvent) {
            var params = CaseTransferEventParams.builder()
                    .userToken(userToken)
                    .caseTypeId(caseDetails.getCaseTypeId())
                    .jurisdiction(caseDetails.getJurisdiction())
                    .ethosCaseReference(caseData.getEthosCaseReference())
                    .sourceEthosCaseReference(sourceCaseData.getEthosCaseReference())
                    .newManagingOffice(newManagingOffice)
                    .positionType(caseDetails.getCaseData().getPositionTypeCT())
                    .reason(caseDetails.getCaseData().getReasonForCT())
                    .ecmCaseType(SINGLE_CASE_TYPE)
                    .transferSameCountry(true)
                    .build();

            var transferErrors = caseTransferEventService.transfer(params);
            if (!transferErrors.isEmpty()) {
                errors.addAll(transferErrors);
            }
        }

        sourceCaseData.setManagingOffice(newManagingOffice);
        sourceCaseData.setOfficeCT(null);
        sourceCaseData.setPositionTypeCT(null);
        sourceCaseData.setStateAPI(null);

        return errors;
    }
}
