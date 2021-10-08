package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SubMultipleReference;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SubMultipleRefEnglandWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SubMultipleRefRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SubMultipleRefScotlandRepository;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_DEV_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_USERS_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_DEV_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_USERS_BULK_CASE_TYPE_ID;

@Slf4j
@RequiredArgsConstructor
@Service("subMultipleReferenceService")
public class SubMultipleReferenceService {

    private final SubMultipleRefEnglandWalesRepository subMultipleRefEnglandWalesRepository;
    private final SubMultipleRefScotlandRepository subMultipleRefScotlandRepository;

    public synchronized String createReference(String caseTypeId, String multipleReference, int numberCases) {
        switch (caseTypeId) {
            case ENGLANDWALES_BULK_CASE_TYPE_ID:
            case ENGLANDWALES_DEV_BULK_CASE_TYPE_ID:
            case ENGLANDWALES_USERS_BULK_CASE_TYPE_ID:
                return generateOfficeReference(subMultipleRefEnglandWalesRepository, numberCases, multipleReference,
                        ENGLANDWALES_CASE_TYPE_ID);
            case SCOTLAND_BULK_CASE_TYPE_ID:
            case SCOTLAND_DEV_BULK_CASE_TYPE_ID:
            case SCOTLAND_USERS_BULK_CASE_TYPE_ID:
                return generateOfficeReference(subMultipleRefScotlandRepository, numberCases, multipleReference,
                        SCOTLAND_CASE_TYPE_ID);
            default:
                throw new IllegalArgumentException(
                        String.format("Unable to create case reference: unexpected caseTypeId %s", caseTypeId));
        }
    }

    private String generateOfficeReference(SubMultipleRefRepository<? extends SubMultipleReference> referenceRepository,
                                           int numberCases, String multipleRef, String officeName) {
        return referenceRepository.ethosSubMultipleCaseRefGen(Integer.parseInt(multipleRef), numberCases, officeName);
    }
}
