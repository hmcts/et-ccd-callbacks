package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.jpaservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.VenueRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.VenueService;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Service
public class JpaVenueService implements VenueService {

    private final VenueRepository venueRepository;

    @Override
    public List<DynamicValueType> getVenues(TribunalOffice tribunalOffice) {
        return venueRepository.findByTribunalOffice(tribunalOffice).stream()
                .map(venue -> DynamicValueType.create(venue.getCode(), venue.getName()))
                .sorted(Comparator.comparing(DynamicValueType::getLabel))
                .toList();
    }
}
