package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaseTransferDifferentCountryService {

    public static final String CASE_TRANSFERRED_POSITION_TYPE = "Case transferred - other country";

    private final CaseTransferUtils caseTransferUtils;
    private final CaseTransferEventService caseTransferEventService;

    public List<String> transferCase(CaseDetails caseDetails, String userToken) {
        List<CaseData> caseDataList = caseTransferUtils.getAllCasesToBeTransferred(caseDetails, userToken);
        if (caseDataList.isEmpty()) {
            throw new IllegalStateException("No cases found for Case Transfer for "
                    + caseDetails.getCaseData().getEthosCaseReference());
        }

        List<String> errors = validate(caseDataList);
        if (!errors.isEmpty()) {
            return errors;
        }

        return transferCases(caseDetails, caseDataList, userToken);
    }

    private List<String> validate(List<CaseData> cases) {
        List<String> errors = new ArrayList<>();

        for (CaseData caseData : cases) {
            List<String> validationErrors = caseTransferUtils.validateCase(caseData);
            if (!validationErrors.isEmpty()) {
                errors.addAll(validationErrors);
            }
        }

        return errors;
    }

    private List<String> transferCases(CaseDetails caseDetails, List<CaseData> caseDataList, String userToken) {
        List<String> errors = new ArrayList<>();
        CaseData sourceCaseData = caseDetails.getCaseData();
        String newManagingOffice = caseDetails.getCaseData().getOfficeCT().getSelectedCode();

        for (CaseData caseData : caseDataList) {
            CaseTransferEventParams params = CaseTransferEventParams.builder()
                    .userToken(userToken)
                    .caseTypeId(caseDetails.getCaseTypeId())
                    .jurisdiction(caseDetails.getJurisdiction())
                    .ethosCaseReferences(List.of(caseData.getEthosCaseReference()))
                    .sourceEthosCaseReference(sourceCaseData.getEthosCaseReference())
                    .newManagingOffice(newManagingOffice)
                    .positionType(CASE_TRANSFERRED_POSITION_TYPE)
                    .reason(sourceCaseData.getReasonForCT())
                    .multipleReference(SINGLE_CASE_TYPE)
                    .confirmationRequired(false)
                    .transferSameCountry(false)
                    .build();

            log.info("Creating Case Transfer event for {}", caseData.getEthosCaseReference());
            List<String> transferErrors = caseTransferEventService.transfer(params);
            if (!transferErrors.isEmpty()) {
                errors.addAll(transferErrors);
            }
        }

        sourceCaseData.setLinkedCaseCT("Transferred to " + newManagingOffice);
        sourceCaseData.setPositionType(CASE_TRANSFERRED_POSITION_TYPE);
        sourceCaseData.setOfficeCT(null);
        sourceCaseData.setStateAPI(null);

        return errors;
    }
}
