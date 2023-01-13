package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SubMultipleRefEnglandWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SubMultipleRefScotlandRepository;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;

@Slf4j
@RequiredArgsConstructor
@Service("subMultipleReferenceService")
@SuppressWarnings({"PMD.AvoidSynchronizedAtMethodLevel"})
public class SubMultipleReferenceService {

    private final SubMultipleRefEnglandWalesRepository subMultipleRefEnglandWalesRepository;
    private final SubMultipleRefScotlandRepository subMultipleRefScotlandRepository;

    public synchronized String createReference(String caseTypeId, String multipleReference, int numberCases) {
        switch (caseTypeId) {
            case ENGLANDWALES_BULK_CASE_TYPE_ID:
                return subMultipleRefEnglandWalesRepository.ethosSubMultipleCaseRefGen(
                        Integer.parseInt(multipleReference), numberCases);
            case SCOTLAND_BULK_CASE_TYPE_ID:
                return subMultipleRefScotlandRepository.ethosSubMultipleCaseRefGen(
                        Integer.parseInt(multipleReference), numberCases);
            default:
                throw new IllegalArgumentException(
                        String.format("Unable to create case reference: unexpected caseTypeId %s", caseTypeId));
        }
    }
}
