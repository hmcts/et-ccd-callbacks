package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.MultipleReference;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefEnglandWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefScotlandRepository;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_DEV_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_OFFICE_NUMBER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_USERS_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_DEV_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_OFFICE_NUMBER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_USERS_BULK_CASE_TYPE_ID;

@Slf4j
@RequiredArgsConstructor
@Service("multipleReferenceService")
public class MultipleReferenceService {

    private final MultipleRefEnglandWalesRepository multipleRefEnglandWalesRepository;
    private final MultipleRefScotlandRepository multipleRefScotlandRepository;

    public synchronized String createReference(String caseTypeId, int numberCases) {
        switch (caseTypeId) {
            case ENGLANDWALES_DEV_BULK_CASE_TYPE_ID:
            case ENGLANDWALES_USERS_BULK_CASE_TYPE_ID:
            case ENGLANDWALES_BULK_CASE_TYPE_ID:
                return generateOfficeReference(multipleRefEnglandWalesRepository, numberCases,
                        ENGLANDWALES_OFFICE_NUMBER, ENGLANDWALES_CASE_TYPE_ID);
            case SCOTLAND_DEV_BULK_CASE_TYPE_ID:
            case SCOTLAND_USERS_BULK_CASE_TYPE_ID:
            case SCOTLAND_BULK_CASE_TYPE_ID:
                return generateOfficeReference(multipleRefScotlandRepository, numberCases,
                        SCOTLAND_OFFICE_NUMBER, SCOTLAND_CASE_TYPE_ID);
            default:
                throw new IllegalArgumentException(
                        String.format("Unable to create case reference: unexpected caseTypeId %s", caseTypeId));
        }
    }

    private String generateOfficeReference(MultipleRefRepository<? extends MultipleReference> referenceRepository,
                                           int numberCases, String officeNumber, String officeName) {
        return officeNumber + referenceRepository.ethosMultipleCaseRefGen(numberCases, officeName);
    }
}
