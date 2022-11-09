package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefEnglandWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefScotlandRepository;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;

@Slf4j
@RequiredArgsConstructor
@Service("multipleReferenceService")
public class MultipleReferenceService {

    private final MultipleRefEnglandWalesRepository multipleRefEnglandWalesRepository;
    private final MultipleRefScotlandRepository multipleRefScotlandRepository;

    public synchronized String createReference(String caseTypeId) {
        switch (caseTypeId) {
            case ENGLANDWALES_BULK_CASE_TYPE_ID:
                return multipleRefEnglandWalesRepository.ethosMultipleCaseRefGen();
            case SCOTLAND_BULK_CASE_TYPE_ID:
                return multipleRefScotlandRepository.ethosMultipleCaseRefGen();
            default:
                throw new IllegalArgumentException(
                        String.format("Unable to create case reference: unexpected caseTypeId %s", caseTypeId));
        }
    }
}
