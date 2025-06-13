package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.jpaservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Judge;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.JudgeRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.JudgeService;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.partitioningBy;
import static org.apache.commons.lang3.StringUtils.SPACE;

@Slf4j
@RequiredArgsConstructor
@Service
public class JpaJudgeService implements JudgeService {

    private final JudgeRepository judgeRepository;

    @Override
    public List<Judge> getJudges(TribunalOffice tribunalOffice) {
        return judgeRepository.findByTribunalOffice(tribunalOffice);
    }

    /**
     * This method orders the judges by their surname and by the z value. Judges are to be ordered by their surname, and
     * if they are inactive and begin with a z, they are to be ordered at the end of the list.
     * @param tribunalOffice the tribunal office to get the judges from
     * @return a list of judges ordered by their surname and by the z value
     */
    @Override
    public List<DynamicValueType> getJudgesDynamicList(TribunalOffice tribunalOffice) {
        List<DynamicValueType> toSort = judgeRepository.findByTribunalOffice(tribunalOffice).stream()
                .map(j -> DynamicValueType.create(j.getCode(), j.getName().replace("\u00A0", SPACE)))
                .sorted(comparing(dv -> dv.getLabel().toLowerCase(Locale.ROOT)))
                .toList();
        Map<Boolean, List<DynamicValueType>> map = toSort.stream()
                .collect(partitioningBy(dv -> dv.getLabel().startsWith("z ")));

        map.values().forEach(list -> list.sort(
                comparing(dv -> dv.getLabel().split(SPACE)[dv.getLabel().split(SPACE).length - 1])));

        return map.values().stream().flatMap(List::stream).toList();
    }
}
