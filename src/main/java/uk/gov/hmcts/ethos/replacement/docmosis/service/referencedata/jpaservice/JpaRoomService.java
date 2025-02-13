package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.jpaservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.RoomRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.RoomService;

import java.util.Comparator;
import java.util.List;

@RequiredArgsConstructor
@Service
public class JpaRoomService implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    public List<DynamicValueType> getRooms(String venueCode) {
        return roomRepository.findByVenueCode(venueCode).stream()
                .map(r -> DynamicValueType.create(r.getCode(), r.getName()))
                .sorted(Comparator.comparing(DynamicValueType::getLabel))
                .toList();
    }
}
