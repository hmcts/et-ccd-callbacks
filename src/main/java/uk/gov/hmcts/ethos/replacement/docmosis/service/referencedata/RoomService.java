package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata;

import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;

import java.util.List;

public interface RoomService {
    List<DynamicValueType> getRooms(String venueCode);
}
