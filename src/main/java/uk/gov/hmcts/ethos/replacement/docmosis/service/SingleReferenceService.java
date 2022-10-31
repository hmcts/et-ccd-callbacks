package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefEnglandWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefScotlandRepository;

import java.time.LocalDate;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@Slf4j
@RequiredArgsConstructor
@Service("singleReferenceService")
public class SingleReferenceService {

    private final SingleRefScotlandRepository singleRefScotlandRepository;
    private final SingleRefEnglandWalesRepository singleRefEnglandWalesRepository;

    public synchronized String createReference(String caseTypeId) {
        var currentYear = LocalDate.now().getYear();
        switch (caseTypeId) {
            case ENGLANDWALES_CASE_TYPE_ID:
                return singleRefEnglandWalesRepository.ethosCaseRefGen(currentYear);
            case SCOTLAND_CASE_TYPE_ID:
                return singleRefScotlandRepository.ethosCaseRefGen(currentYear);
            default:
                throw new IllegalArgumentException(
                        String.format("Unable to create case reference: unexpected caseTypeId %s", caseTypeId));
        }
    }
}
