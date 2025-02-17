package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.jpaservice;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.FileLocationRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.FileLocationService;

import java.util.List;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.partitioningBy;

@Service
public class JpaFileLocationService implements FileLocationService {

    private final FileLocationRepository fileLocationRepository;

    public JpaFileLocationService(FileLocationRepository fileLocationRepository) {
        this.fileLocationRepository = fileLocationRepository;
    }

    @Override
    public List<DynamicValueType> getFileLocations(TribunalOffice tribunalOffice) {
        return fileLocationRepository.findByTribunalOffice(tribunalOffice).stream()
                .map(fl -> DynamicValueType.create(fl.getCode(), fl.getName()))
                .sorted(comparing(dv -> dv.getLabel().toLowerCase()))
                .collect(partitioningBy(dv -> dv.getLabel().startsWith("z ")))
                .values()
                .stream()
                .flatMap(List::stream)
                .toList();
    }
}
