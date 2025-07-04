package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.jpaservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.CourtWorkerRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.CourtWorkerService;

import java.util.List;
import java.util.Locale;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.partitioningBy;

@RequiredArgsConstructor
@Service
public class JpaCourtWorkerService implements CourtWorkerService {

    private final CourtWorkerRepository courtWorkerRepository;

    public List<DynamicValueType> getCourtWorkerByTribunalOffice(TribunalOffice tribunalOffice,
                                                                 CourtWorkerType courtWorkerType) {
        return courtWorkerRepository.findByTribunalOfficeAndType(tribunalOffice, courtWorkerType).stream()
                .map(cw -> DynamicValueType.create(cw.getCode(), cw.getName().replace("\u00A0", " ")))
                .sorted(comparing(dv -> dv.getLabel().toLowerCase(Locale.ROOT)))
                .collect(partitioningBy(dv -> dv.getLabel().startsWith("z ")))
                .values()
                .stream()
                .flatMap(List::stream)
                .toList();
    }
}

