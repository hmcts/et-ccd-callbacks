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
        int currentYear = LocalDate.now().getYear();
        return switch (caseTypeId) {
            case ENGLANDWALES_CASE_TYPE_ID -> singleRefEnglandWalesRepository.ethosCaseRefGen(currentYear);
            case SCOTLAND_CASE_TYPE_ID -> singleRefScotlandRepository.ethosCaseRefGen(currentYear);
            default -> throw new IllegalArgumentException(
                    String.format("Unable to create case reference: unexpected caseTypeId %s", caseTypeId));
        };
    }
}
