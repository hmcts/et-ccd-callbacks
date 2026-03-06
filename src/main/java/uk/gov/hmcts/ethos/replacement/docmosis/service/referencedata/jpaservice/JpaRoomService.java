package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.jpaservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Room;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.RoomRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.RoomService;

import java.util.List;
import java.util.Locale;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.partitioningBy;

@RequiredArgsConstructor
@Service
public class JpaRoomService implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    public List<DynamicValueType> getRooms(String venueCode) {
        List<Room> rooms = roomRepository.findByVenueCode(venueCode);
        // For London Tribunals Centre, return the list of rooms in the order they are stored
        if ("London Tribunals Centre".equals(venueCode)) {
            return rooms.stream()
                .map(r -> DynamicValueType.create(r.getCode(), r.getName()))
                .toList();
        } else {
            return rooms.stream()
                .map(r -> DynamicValueType.create(r.getCode(), r.getName()))
                .sorted(comparing(dv -> dv.getLabel().toLowerCase(Locale.ROOT)))
                .collect(partitioningBy(dv -> dv.getLabel().startsWith("z ")))
                .values()
                .stream()
                .flatMap(List::stream)
                .toList();

        }
    }
}
