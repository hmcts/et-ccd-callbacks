package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.jpaservice;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.FileLocationRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.FileLocationService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class JpaFileLocationService implements FileLocationService {

    private final FileLocationRepository fileLocationRepository;

    public JpaFileLocationService(FileLocationRepository fileLocationRepository) {
        this.fileLocationRepository = fileLocationRepository;
    }

    @Override
    public List<DynamicValueType> getFileLocations(TribunalOffice tribunalOffice) {
        return fileLocationRepository.findByTribunalOffice(tribunalOffice)
                .stream()
                .map(fl -> DynamicValueType.create(fl.getCode(), fl.getName()))
                .collect(Collectors.toList());
    }
}
