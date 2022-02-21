package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SingleReference;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefEnglandWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefScotlandRepository;

import java.time.LocalDate;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_OFFICE_NUMBER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_OFFICE_NUMBER;

@Slf4j
@RequiredArgsConstructor
@Service("singleReferenceService")
public class SingleReferenceService {

    private final SingleRefScotlandRepository singleRefScotlandRepository;
    private final SingleRefEnglandWalesRepository singleRefEnglandWalesRepository;

    public synchronized String createReference(String caseTypeId, int numberCases) {
        var currentYear = String.valueOf(LocalDate.now().getYear());
        switch (caseTypeId) {
            case ENGLANDWALES_CASE_TYPE_ID:
                return generateOfficeReference(singleRefEnglandWalesRepository, currentYear, numberCases,
                        ENGLANDWALES_OFFICE_NUMBER, ENGLANDWALES_CASE_TYPE_ID);
            case SCOTLAND_CASE_TYPE_ID:
                return generateOfficeReference(singleRefScotlandRepository, currentYear, numberCases,
                        SCOTLAND_OFFICE_NUMBER, SCOTLAND_CASE_TYPE_ID);

            default:
                throw new IllegalArgumentException(
                        String.format("Unable to create case reference: unexpected caseTypeId %s", caseTypeId));
        }
    }

    private String generateOfficeReference(SingleRefRepository<? extends SingleReference> referenceRepository,
                                           String currentYear, int numberCases, String officeNumber,
                                           String officeName) {
        return officeNumber + referenceRepository.ethosCaseRefGen(numberCases, Integer.parseInt(currentYear),
                officeName);
    }
}
